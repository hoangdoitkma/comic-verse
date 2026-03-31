package com.example.comicversev1.data.model;

public class ChapterReportResponse {
    private int id;
    private int reporterId;
    private int chapterId;
    private String type;
    private String reason;
    private String status;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReporterId() { return reporterId; }
    public void setReporterId(int reporterId) { this.reporterId = reporterId; }

    public int getChapterId() { return chapterId; }
    public void setChapterId(int chapterId) { this.chapterId = chapterId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
