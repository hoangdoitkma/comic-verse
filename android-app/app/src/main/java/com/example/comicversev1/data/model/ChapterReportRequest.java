package com.example.comicversev1.data.model;

public class ChapterReportRequest {
    private String type;
    private String reason;
    private String readerMode;
    private Integer pageIndex;
    private Integer paragraphIndex;
    private String contentSnapshot;
    private String pageImageUrl;

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

    public String getReaderMode() {
        return readerMode;
    }

    public void setReaderMode(String readerMode) {
        this.readerMode = readerMode;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getParagraphIndex() {
        return paragraphIndex;
    }

    public void setParagraphIndex(Integer paragraphIndex) {
        this.paragraphIndex = paragraphIndex;
    }

    public String getContentSnapshot() {
        return contentSnapshot;
    }

    public void setContentSnapshot(String contentSnapshot) {
        this.contentSnapshot = contentSnapshot;
    }

    public String getPageImageUrl() {
        return pageImageUrl;
    }

    public void setPageImageUrl(String pageImageUrl) {
        this.pageImageUrl = pageImageUrl;
    }
}
