package com.example.comicversev1.data.model;

public class PaymentRequest {
    private Integer packageId;
    private Integer userId;

    public PaymentRequest(Integer packageId, Integer userId) {
        this.packageId = packageId;
        this.userId = userId;
    }

    public Integer getPackageId() { return packageId; }
    public void setPackageId(Integer packageId) { this.packageId = packageId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
}
