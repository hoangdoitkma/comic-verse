package com.datn.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PayOsDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePaymentRequest {
        private Long orderCode;
        private Integer amount;
        private String description;
        private String cancelUrl;
        private String returnUrl;
        private String signature;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebhookRequest {
        private String code;
        private String desc;
        private boolean success;
        private WebhookData data;
        private String signature;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebhookData {
        private Long orderCode;
        private Integer amount;
        private String description;
        private String accountNumber;
        private String reference;
        private String transactionDateTime;
        private String currency;
        private String paymentLinkId;
        private String code;
        private String desc;
        private String counterAccountBankId;
        private String counterAccountBankName;
        private String counterAccountName;
        private String counterAccountNumber;
        private String virtualAccountName;
        private String virtualAccountNumber;
    }
}
