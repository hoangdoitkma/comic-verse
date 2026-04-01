package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class NotificationDTO {
    public Integer id;
    public String title;
    public String message;
    public String type; // NEW_CHAPTER, COMMENT_REPLY, SYSTEM, PROMOTION, UPDATE, APPROVED, REJECTED
    @SerializedName("isRead")
    public Boolean isRead;
    public String redirectUrl;
    public String createdAt;
    public Integer targetUserId;
    public String targetUserName;
}
