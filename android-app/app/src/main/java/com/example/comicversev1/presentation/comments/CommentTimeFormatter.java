package com.example.comicversev1.presentation.comments;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class CommentTimeFormatter {
    private static final Locale VI_LOCALE = new Locale("vi", "VN");
    private static final String[] API_PATTERNS = {
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "yyyy-MM-dd'T'HH:mm:ssX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss"
    };

    private CommentTimeFormatter() {
    }

    public static String formatTimeAgo(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return "";

        Date date = parseDate(timeStr);
        if (date == null) return timeStr;

        long diffMs = System.currentTimeMillis() - date.getTime();
        if (diffMs < 0) diffMs = 0;

        long minutes = diffMs / 60000;
        if (minutes < 1) return "Vừa xong";
        if (minutes < 60) return minutes + " phút trước";

        long hours = minutes / 60;
        if (hours < 24) return hours + " giờ trước";

        long days = hours / 24;
        if (days < 30) return days + " ngày trước";

        return new SimpleDateFormat("dd/MM/yyyy", VI_LOCALE).format(date);
    }

    private static Date parseDate(String timeStr) {
        for (String pattern : API_PATTERNS) {
            try {
                return new SimpleDateFormat(pattern, Locale.getDefault()).parse(timeStr);
            } catch (ParseException ignored) {
                // Try the next known backend format.
            }
        }
        return null;
    }
}
