package com.example.comicversev1.data.model;

public class PaymentConfirmRequest {
    private Long orderCode;

    public PaymentConfirmRequest(Long orderCode) {
        this.orderCode = orderCode;
    }

    public Long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }
}
