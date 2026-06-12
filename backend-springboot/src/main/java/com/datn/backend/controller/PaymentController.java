package com.datn.backend.controller;

import com.datn.backend.dto.PayOsDto;
import com.datn.backend.entity.User;
import com.datn.backend.service.PayOsService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PayOsService payOsService;

    @PostMapping("/create-vip-order")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createVipOrder(@RequestBody VipOrderRequest request, @AuthenticationPrincipal com.datn.backend.security.services.UserDetailsImpl userDetails) {
        try {
            Integer userId = userDetails.getId();
            PayOsService.VipPaymentLink paymentLink = payOsService.createVipPaymentLink(userId, request.getPackageId());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "checkoutUrl", paymentLink.checkoutUrl(),
                    "orderCode", paymentLink.orderCode(),
                    "paymentLinkId", paymentLink.paymentLinkId()
            ));
        } catch (Exception e) {
            log.error("Create VIP order failed", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/confirm-vip-order")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> confirmVipOrder(@RequestBody ConfirmVipOrderRequest request,
                                             @AuthenticationPrincipal com.datn.backend.security.services.UserDetailsImpl userDetails) {
        try {
            PayOsService.PaymentConfirmation confirmation =
                    payOsService.confirmVipPayment(userDetails.getId(), request.getOrderCode());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "vipActivated", confirmation.vipActivated(),
                    "status", confirmation.status(),
                    "message", confirmation.message()
            ));
        } catch (Exception e) {
            log.error("Confirm VIP order failed", e);
            String message = e.getMessage() != null ? e.getMessage() : "Confirm VIP order failed";
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "vipActivated", false,
                    "message", message
            ));
        }
    }

    @PostMapping("/admin/confirm-vip-order")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> confirmVipOrderAsAdmin(@RequestBody ConfirmVipOrderRequest request) {
        try {
            PayOsService.PaymentConfirmation confirmation =
                    payOsService.confirmVipPaymentAsAdmin(request.getOrderCode());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "vipActivated", confirmation.vipActivated(),
                    "status", confirmation.status(),
                    "message", confirmation.message()
            ));
        } catch (Exception e) {
            log.error("Admin confirm VIP order failed", e);
            String message = e.getMessage() != null ? e.getMessage() : "Admin confirm VIP order failed";
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "vipActivated", false,
                    "message", message
            ));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handlePayOsWebhook(@RequestBody PayOsDto.WebhookRequest webhookRequest) {
        try {
            payOsService.handleWebhook(webhookRequest);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("Xử lý Webhook thất bại: ", e);
            // Theo hướng dẫn từ đa số Payment Gateway, luôn trả về HTTP 200 để họ không retry rác,
            // nhưng trả về { "success": false } trong body.
            return ResponseEntity.ok(Map.of("success", false));
        }
    }

    @Data
    public static class VipOrderRequest {
        private Integer packageId;
        private Integer userId; // Optional for testing if auth is not set up properly
    }

    @Data
    public static class ConfirmVipOrderRequest {
        private Long orderCode;
    }
}
