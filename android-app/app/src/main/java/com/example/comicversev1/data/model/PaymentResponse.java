package com.example.comicversev1.data.model;

public class PaymentResponse {
    private boolean success;
    private String checkoutUrl;
    private Long orderCode;
    private String paymentLinkId;
    private boolean vipActivated;
    private String status;
    private String message;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }

    public Long getOrderCode() { return orderCode; }
    public void setOrderCode(Long orderCode) { this.orderCode = orderCode; }

    public String getPaymentLinkId() { return paymentLinkId; }
    public void setPaymentLinkId(String paymentLinkId) { this.paymentLinkId = paymentLinkId; }

    public boolean isVipActivated() { return vipActivated; }
    public void setVipActivated(boolean vipActivated) { this.vipActivated = vipActivated; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
