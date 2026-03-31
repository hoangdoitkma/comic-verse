package com.example.comicversev1.data.model;

import java.io.Serializable;

public class CommentDTO implements Serializable {
    private Integer id;
    private Integer userId;
    private String userDisplayName;
    private String userAvatarUrl;
    private String content;
    private Integer likeCount;
    private Integer replyCount;
    private String createdAt;

    // Optional field to store loaded replies on client side
    private transient java.util.List<CommentDTO> replies;
    private transient boolean isRepliesLoaded;

    public CommentDTO() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public java.util.List<CommentDTO> getReplies() {
        return replies;
    }

    public void setReplies(java.util.List<CommentDTO> replies) {
        this.replies = replies;
        this.isRepliesLoaded = true;
    }

    public boolean isRepliesLoaded() {
        return isRepliesLoaded;
    }

    public void setRepliesLoaded(boolean repliesLoaded) {
        isRepliesLoaded = repliesLoaded;
    }
}
