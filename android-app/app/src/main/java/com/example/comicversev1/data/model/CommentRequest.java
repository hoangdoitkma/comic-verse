package com.example.comicversev1.data.model;

public class CommentRequest {
    private Integer parentId;
    private String content;

    public CommentRequest() {}

    public CommentRequest(Integer parentId, String content) {
        this.parentId = parentId;
        this.content = content;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
