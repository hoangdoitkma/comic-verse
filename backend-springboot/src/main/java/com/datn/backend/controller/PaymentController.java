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
            String checkoutUrl = payOsService.createVipPaymentLink(userId, request.getPackageId());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "checkoutUrl", checkoutUrl
            ));
        } catch (Exception e) {
            log.error("Create VIP order failed", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
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
}
