package com.datn.backend.service;

import com.datn.backend.config.PayOsConfig;
import com.datn.backend.dto.PayOsDto;
import com.datn.backend.entity.Subscription;
import com.datn.backend.entity.Transaction;
import com.datn.backend.entity.User;
import com.datn.backend.entity.VipPackage;
import com.datn.backend.entity.enums.PaymentMethod;
import com.datn.backend.entity.enums.SubscriptionStatus;
import com.datn.backend.entity.enums.TransactionStatus;
import com.datn.backend.repository.SubscriptionRepository;
import com.datn.backend.repository.TransactionRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.repository.VipPackageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOsService {

    private final PayOsConfig payOsConfig;
    private final TransactionRepository transactionRepository;
    private final VipPackageRepository vipPackageRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public String createVipPaymentLink(Integer userId, Integer packageId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        VipPackage vipPackage = vipPackageRepository.findById(packageId).orElseThrow(() -> new RuntimeException("Package not found"));

        if (!vipPackage.getIsActive()) {
            throw new RuntimeException("Package is no longer active");
        }

        // Tạo orderCode ngẫu nhiên (dùng Timestamp millisecond) - Đảm bảo <= 53 bits (Javascript safe limit)
        // 53 bits tương đương khoảng 9007199254740991. Timestamp millisecond khoảng 13 chữ số, hoàn toàn phù hợp.
        Long orderCode = System.currentTimeMillis();
        int amount = vipPackage.getPrice().intValue(); // Giả thuyết VND không có số thập phân

        // Description hiển thị trên app ngân hàng - PayOS giới hạn 25 ký tự
        // Gắn 6 số cuối orderCode để phân biệt giao dịch khi đối soát
        String description = "CV " + String.valueOf(orderCode % 1000000);
        String returnUrl = "comicverse://payment.success";  // App Android sẽ capture URL này
        String cancelUrl = "comicverse://payment.cancel";   // App Android sẽ capture URL này

        // Bước 1: Tính toán chữ ký (Signature) cho request tạo link
        // Theo PayOS: Data signature tạo từ các field được sort theo alphabet: amount, cancelUrl, description, orderCode, returnUrl
        String dataForSignature = "amount=" + amount +
                "&cancelUrl=" + cancelUrl +
                "&description=" + description +
                "&orderCode=" + orderCode +
                "&returnUrl=" + returnUrl;

        String signature = generateHmacSHA256(dataForSignature, payOsConfig.getChecksumKey());

        PayOsDto.CreatePaymentRequest body = PayOsDto.CreatePaymentRequest.builder()
                .orderCode(orderCode)
                .amount(amount)
                .description(description)
                .cancelUrl(cancelUrl)
                .returnUrl(returnUrl)
                .signature(signature)
                .build();

        // Bước 2: Gọi API PayOS
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-client-id", payOsConfig.getClientId());
        headers.set("x-api-key", payOsConfig.getApiKey());
        headers.set("Content-Type", "application/json");

        HttpEntity<PayOsDto.CreatePaymentRequest> requestEntity = new HttpEntity<>(body, headers);

        String payosUrl = "https://api-merchant.payos.vn/v2/payment-requests";
        try {
            log.info("=== PayOS Request ===");
            log.info("orderCode: {}, amount: {}, description: '{}', userId: {}, packageId: {}", 
                orderCode, amount, description, userId, packageId);
            log.info("signature data: {}", dataForSignature);

            ResponseEntity<String> response = restTemplate.exchange(payosUrl, HttpMethod.POST, requestEntity, String.class);
            log.info("PayOS Response: {}", response.getBody());
            
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            
            if (rootNode.path("code").asText().equals("00")) {
                JsonNode dataNode = rootNode.path("data");
                String checkoutUrl = dataNode.path("checkoutUrl").asText();
                String paymentLinkId = dataNode.path("paymentLinkId").asText();

                // Lưu lại Database Transaction trạng thái PENDING
                Transaction transaction = Transaction.builder()
                        .user(user)
                        .vipPackage(vipPackage)
                        .amount(vipPackage.getPrice())
                        .paymentMethod(PaymentMethod.PAYOS)
                        .status(TransactionStatus.PENDING)
                        .orderCode(orderCode)
                        .paymentLinkId(paymentLinkId)
                        .build();

                transactionRepository.save(transaction);
                log.info("Transaction saved PENDING, orderCode: {}", orderCode);
                return checkoutUrl;
            } else {
                log.error("PayOS returned error code: {}, desc: {}", rootNode.path("code").asText(), rootNode.path("desc").asText());
                throw new RuntimeException("Error from PayOS: " + rootNode.path("desc").asText());
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("PayOS API returned HTTP error: {} - Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Lỗi từ PayOS: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Failed to call PayOS API", e);
            throw new RuntimeException("Tạo link thanh toán thất bại: " + e.getMessage());
        }
    }

    @Transactional
    public void handleWebhook(PayOsDto.WebhookRequest webhookRequest) {
        log.info("Received PayOS webhook: {}", webhookRequest);

        PayOsDto.WebhookData data = webhookRequest.getData();

        // Bước 1: Xác thực chữ ký (Signature Verify)
        // Data webhook cũng sort the alphabet: amount, code, desc, description, orderCode, paymentLinkId, reference, returnUrl (none), transactionDateTime, counterAccount...
        // Tuy nhiên theo document PayOS Webhook Data: "amount=" + amount + "&code=" + code...
        // Chuỗi payload sẽ tự ghép từ class dữ liệu
        String dataStr = "accountNumber=" + (data.getAccountNumber() != null ? data.getAccountNumber() : "") +
                "&amount=" + data.getAmount() +
                "&code=" + data.getCode() +
                "&counterAccountBankId=" + (data.getCounterAccountBankId() != null ? data.getCounterAccountBankId() : "") +
                "&counterAccountBankName=" + (data.getCounterAccountBankName() != null ? data.getCounterAccountBankName() : "") +
                "&counterAccountName=" + (data.getCounterAccountName() != null ? data.getCounterAccountName() : "") +
                "&counterAccountNumber=" + (data.getCounterAccountNumber() != null ? data.getCounterAccountNumber() : "") +
                "&currency=" + data.getCurrency() +
                "&desc=" + data.getDesc() +
                "&description=" + data.getDescription() +
                "&orderCode=" + data.getOrderCode() +
                "&paymentLinkId=" + data.getPaymentLinkId() +
                "&reference=" + data.getReference() +
                "&transactionDateTime=" + data.getTransactionDateTime() +
                "&virtualAccountName=" + (data.getVirtualAccountName() != null ? data.getVirtualAccountName() : "") +
                "&virtualAccountNumber=" + (data.getVirtualAccountNumber() != null ? data.getVirtualAccountNumber() : "");

        log.info("Webhook signature data string: {}", dataStr);
        String generatedExpectedSignature = generateHmacSHA256(dataStr, payOsConfig.getChecksumKey());

        if (!generatedExpectedSignature.equals(webhookRequest.getSignature())) {
            log.error("Webhook signature mismatch. Expected: {}, Got: {}", generatedExpectedSignature, webhookRequest.getSignature());
            throw new RuntimeException("Xác thực Webhook không hợp lệ!");
        }

        // Bỏ qua nếu giao dịch không thành công
        // code = "00" tức là khoản chuyển thành công.
        if (!"00".equals(data.getCode())) {
            log.warn("Giao dịch PayOS không thành công, code: {}", data.getCode());
            return;
        }

        // Bước 2: Cập nhật Transaction
        Transaction transaction = transactionRepository.findByOrderCode(data.getOrderCode())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng mã " + data.getOrderCode()));

        if (transaction.getStatus() == TransactionStatus.SUCCESS) {
            log.info("Đơn hàng đã được xử lý trước đó!");
            return;
        }

        // Kiểm tra tiền
        long paidAmount = data.getAmount().longValue();
        if (paidAmount < transaction.getAmount().longValue()) {
            log.error("Khách hàng chuyển thiếu tiền. Yêu cầu {}, Đã nhận {}", transaction.getAmount(), paidAmount);
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            return;
        }

        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        // Bước 3: Cích hoạt gói VIP cho User
        User user = transaction.getUser();
        VipPackage vipPackage = transaction.getVipPackage();

        // Tìm Subscription hiện tại của user (nếu có)
        // Nếu user đã có gói đang active, ta cộng dồn hạng/ngày thêm dựa vào yêu cầu, ở đây ví dụ ta set new luôn
        Subscription currentSubscription = subscriptionRepository.findByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE)
                .stream().findFirst().orElse(null);

        LocalDateTime now = LocalDateTime.now();
        if (currentSubscription != null) {
            // Cộng dồn ngày vào gói hiện tại
            currentSubscription.setEndDate(currentSubscription.getEndDate().plusMonths(vipPackage.getDurationMonth()));
            subscriptionRepository.save(currentSubscription);
        } else {
            // Cấp mới Subscription
            Subscription newSubscription = Subscription.builder()
                    .user(user)
                    .vipPackage(vipPackage)
                    .startDate(now)
                    .endDate(now.plusMonths(vipPackage.getDurationMonth()))
                    .status(SubscriptionStatus.ACTIVE)
                    .build();
            subscriptionRepository.save(newSubscription);
        }

        log.info("Kích hoạt VIP thành công cho User [{}] qua Payload Webhook Order [{}]", user.getId(), data.getOrderCode());
    }

    /**
     * Hàm băm (Hash) chuỗi payload với Key sử dụng thuật toán HMAC-SHA256.
     * Đây là đoạn code quan trọng để chống giả mạo trên Webhook của PayOS.
     */
    private String generateHmacSHA256(String data, String key) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hashBytes = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Convert byte arrays to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Failed to generate HMAC SHA256", e);
            throw new RuntimeException("Error generating signature");
        }
    }
}
