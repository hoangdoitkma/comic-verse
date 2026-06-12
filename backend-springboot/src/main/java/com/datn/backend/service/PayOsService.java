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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOsService {

    private static final String PAYOS_PAYMENT_REQUEST_URL = "https://api-merchant.payos.vn/v2/payment-requests";

    private final PayOsConfig payOsConfig;
    private final TransactionRepository transactionRepository;
    private final VipPackageRepository vipPackageRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public VipPaymentLink createVipPaymentLink(Integer userId, Integer packageId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        VipPackage vipPackage = vipPackageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        if (!Boolean.TRUE.equals(vipPackage.getIsActive())) {
            throw new RuntimeException("Package is no longer active");
        }

        Long orderCode = System.currentTimeMillis();
        int amount = vipPackage.getPrice().intValue();
        String description = "CV " + orderCode % 1000000;
        String returnUrl = "comicverse://payment.success";
        String cancelUrl = "comicverse://payment.cancel";

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

        HttpEntity<PayOsDto.CreatePaymentRequest> requestEntity = new HttpEntity<>(body, buildPayOsHeaders());

        try {
            log.info("Creating PayOS VIP order: orderCode={}, amount={}, userId={}, packageId={}",
                    orderCode, amount, userId, packageId);

            ResponseEntity<String> response = restTemplate.exchange(
                    PAYOS_PAYMENT_REQUEST_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            if (!"00".equals(rootNode.path("code").asText())) {
                log.error("PayOS returned error code={}, desc={}",
                        rootNode.path("code").asText(), rootNode.path("desc").asText());
                throw new RuntimeException("Error from PayOS: " + rootNode.path("desc").asText());
            }

            JsonNode dataNode = rootNode.path("data");
            String checkoutUrl = dataNode.path("checkoutUrl").asText();
            String paymentLinkId = dataNode.path("paymentLinkId").asText();

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
            log.info("Transaction saved as PENDING, orderCode={}", orderCode);
            return new VipPaymentLink(checkoutUrl, orderCode, paymentLinkId);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("PayOS API returned HTTP error: {} - Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Loi tu PayOS: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Failed to call PayOS create payment API", e);
            throw new RuntimeException("Tao link thanh toan that bai: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentConfirmation confirmVipPayment(Integer userId, Long orderCode) {
        if (orderCode == null) {
            throw new RuntimeException("Order code is required");
        }

        Transaction transaction = transactionRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Khong tim thay don hang ma " + orderCode));

        if (transaction.getUser() == null || !transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Don hang khong thuoc ve nguoi dung hien tai");
        }

        if (transaction.getStatus() == TransactionStatus.SUCCESS) {
            return new PaymentConfirmation(true, TransactionStatus.SUCCESS.name(), "VIP da duoc kich hoat");
        }

        return confirmTransactionWithPayOs(transaction, orderCode);
    }

    @Transactional
    public PaymentConfirmation confirmVipPaymentAsAdmin(Long orderCode) {
        if (orderCode == null) {
            throw new RuntimeException("Order code is required");
        }

        Transaction transaction = transactionRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Khong tim thay don hang ma " + orderCode));

        if (transaction.getStatus() == TransactionStatus.SUCCESS) {
            return new PaymentConfirmation(true, TransactionStatus.SUCCESS.name(), "VIP da duoc kich hoat");
        }

        return confirmTransactionWithPayOs(transaction, orderCode);
    }

    private PaymentConfirmation confirmTransactionWithPayOs(Transaction transaction, Long orderCode) {
        JsonNode dataNode = fetchPayOsPaymentRequest(orderCode);
        String payOsStatus = dataNode.path("status").asText("");
        BigDecimal amountPaid = BigDecimal.valueOf(dataNode.path("amountPaid").asLong(0));

        if ("PAID".equalsIgnoreCase(payOsStatus)) {
            boolean activated = completeVipTransaction(transaction, amountPaid, "PayOS confirm API");
            return new PaymentConfirmation(
                    activated,
                    transaction.getStatus().name(),
                    activated ? "VIP da duoc kich hoat" : "Thanh toan chua du so tien"
            );
        }

        if ("CANCELLED".equalsIgnoreCase(payOsStatus)) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            return new PaymentConfirmation(false, TransactionStatus.FAILED.name(), "Don thanh toan da bi huy");
        }

        String status = payOsStatus.isBlank() ? transaction.getStatus().name() : payOsStatus;
        return new PaymentConfirmation(false, status, "Don thanh toan dang duoc xu ly");
    }

    @Transactional
    public void handleWebhook(PayOsDto.WebhookRequest webhookRequest) {
        log.info("Received PayOS webhook: {}", webhookRequest);

        PayOsDto.WebhookData data = webhookRequest.getData();
        if (data == null) {
            throw new RuntimeException("Webhook data is required");
        }

        String dataStr = "accountNumber=" + valueOrEmpty(data.getAccountNumber()) +
                "&amount=" + data.getAmount() +
                "&code=" + data.getCode() +
                "&counterAccountBankId=" + valueOrEmpty(data.getCounterAccountBankId()) +
                "&counterAccountBankName=" + valueOrEmpty(data.getCounterAccountBankName()) +
                "&counterAccountName=" + valueOrEmpty(data.getCounterAccountName()) +
                "&counterAccountNumber=" + valueOrEmpty(data.getCounterAccountNumber()) +
                "&currency=" + data.getCurrency() +
                "&desc=" + data.getDesc() +
                "&description=" + data.getDescription() +
                "&orderCode=" + data.getOrderCode() +
                "&paymentLinkId=" + data.getPaymentLinkId() +
                "&reference=" + data.getReference() +
                "&transactionDateTime=" + data.getTransactionDateTime() +
                "&virtualAccountName=" + valueOrEmpty(data.getVirtualAccountName()) +
                "&virtualAccountNumber=" + valueOrEmpty(data.getVirtualAccountNumber());

        String expectedSignature = generateHmacSHA256(dataStr, payOsConfig.getChecksumKey());
        if (!expectedSignature.equals(webhookRequest.getSignature())) {
            log.error("Webhook signature mismatch. Expected: {}, Got: {}", expectedSignature, webhookRequest.getSignature());
            throw new RuntimeException("Xac thuc webhook khong hop le");
        }

        if (!"00".equals(data.getCode())) {
            log.warn("PayOS webhook is not successful, code={}", data.getCode());
            return;
        }

        Transaction transaction = transactionRepository.findByOrderCode(data.getOrderCode())
                .orElseThrow(() -> new RuntimeException("Khong tim thay don hang ma " + data.getOrderCode()));

        boolean activated = completeVipTransaction(
                transaction,
                BigDecimal.valueOf(data.getAmount().longValue()),
                "PayOS webhook"
        );
        if (activated) {
            log.info("VIP activated by webhook for userId={}, orderCode={}",
                    transaction.getUser().getId(), data.getOrderCode());
        }
    }

    private JsonNode fetchPayOsPaymentRequest(Long orderCode) {
        HttpEntity<Void> requestEntity = new HttpEntity<>(buildPayOsHeaders());

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    PAYOS_PAYMENT_REQUEST_URL + "/" + orderCode,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            if (!"00".equals(rootNode.path("code").asText())) {
                throw new RuntimeException("PayOS tra ve loi: " + rootNode.path("desc").asText());
            }
            return rootNode.path("data");
        } catch (Exception e) {
            log.error("Failed to confirm PayOS payment request {}", orderCode, e);
            throw new RuntimeException("Khong the xac nhan trang thai thanh toan PayOS: " + e.getMessage());
        }
    }

    private boolean completeVipTransaction(Transaction transaction, BigDecimal paidAmount, String source) {
        if (transaction.getStatus() == TransactionStatus.SUCCESS) {
            log.info("Order {} was already processed", transaction.getOrderCode());
            return true;
        }

        if (paidAmount.compareTo(transaction.getAmount()) < 0) {
            log.error("Payment amount is insufficient via {}. Required={}, paid={}",
                    source, transaction.getAmount(), paidAmount);
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            return false;
        }

        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        User user = transaction.getUser();
        VipPackage vipPackage = transaction.getVipPackage();
        LocalDateTime now = LocalDateTime.now();

        Subscription currentSubscription = subscriptionRepository.findByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE)
                .stream()
                .findFirst()
                .orElse(null);

        if (currentSubscription != null) {
            LocalDateTime baseEndDate = currentSubscription.getEndDate();
            if (baseEndDate == null || baseEndDate.isBefore(now)) {
                baseEndDate = now;
            }
            currentSubscription.setVipPackage(vipPackage);
            currentSubscription.setEndDate(baseEndDate.plusMonths(vipPackage.getDurationMonth()));
            currentSubscription.setStatus(SubscriptionStatus.ACTIVE);
            subscriptionRepository.save(currentSubscription);
        } else {
            Subscription newSubscription = Subscription.builder()
                    .user(user)
                    .vipPackage(vipPackage)
                    .startDate(now)
                    .endDate(now.plusMonths(vipPackage.getDurationMonth()))
                    .status(SubscriptionStatus.ACTIVE)
                    .build();
            subscriptionRepository.save(newSubscription);
        }

        log.info("VIP activated successfully via {}, userId={}, orderCode={}",
                source, user.getId(), transaction.getOrderCode());
        return true;
    }

    private HttpHeaders buildPayOsHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-client-id", payOsConfig.getClientId());
        headers.set("x-api-key", payOsConfig.getApiKey());
        headers.set("Content-Type", "application/json");
        return headers;
    }

    private String generateHmacSHA256(String data, String key) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);

            byte[] hashBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
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

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    public record VipPaymentLink(String checkoutUrl, Long orderCode, String paymentLinkId) {
    }

    public record PaymentConfirmation(boolean vipActivated, String status, String message) {
    }
}
