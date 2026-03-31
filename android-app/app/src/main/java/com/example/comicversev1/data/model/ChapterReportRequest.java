package com.example.comicversev1.data.model;

public class ChapterReportRequest {
    private String type;
    private String reason;

    public ChapterReportRequest(String type, String reason) {
        this.type = type;
        this.reason = reason;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
