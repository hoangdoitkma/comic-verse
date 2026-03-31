package com.datn.backend.entity.enums;

public enum ChapterReportType {
    IMAGE_NOT_LOADING("Lỗi tải ảnh"),
    WRONG_CONTENT("Sai nội dung chương"),
    TYPO_ERROR("Lỗi chính tả"),
    DUPLICATE_CHAPTER("Trùng chương"),
    OTHER("Khác");

    private final String description;

    ChapterReportType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
