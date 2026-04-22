package com.datn.backend.constant;

public final class NotificationConstants {

    private NotificationConstants() {
        // Prevent instantiation
    }

    // Tiêu đề
    public static final String TITLE_NEW_COMIC = "Có truyện mới cần duyệt";
    public static final String TITLE_NEW_CHAPTER = "Có chương mới chờ duyệt";
    public static final String TITLE_MODERATION_RESULT = "Kết quả duyệt chương";

    // Nội dung: Uploader -> Admin
    public static final String MSG_NEW_COMIC = "Uploader %s vừa tạo truyện mới: %s. Vui lòng kiểm tra.";
    public static final String MSG_NEW_CHAPTER = "Uploader %s vừa tải lên chương '%s' cho truyện '%s'. Vui lòng kiểm duyệt.";

    // Nội dung: Admin -> Uploader
    public static final String MSG_CHAPTER_APPROVED = "Chương '%s' của truyện '%s' đã được phê duyệt và hiển thị.";
    public static final String MSG_CHAPTER_REJECTED = "Chương '%s' của truyện '%s' đã bị từ chối.";
    public static final String MSG_REJECT_REASON_SUFFIX = " Lý do: %s";
}
