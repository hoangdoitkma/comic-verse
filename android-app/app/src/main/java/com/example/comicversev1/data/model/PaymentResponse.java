package com.example.comicversev1.data.model;

public class PaymentResponse {
    private boolean success;
    private String checkoutUrl;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }
}
