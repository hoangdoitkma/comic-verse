package com.example.comicversev1.utils;

public final class ReaderSettings {
    private ReaderSettings() {}

    public static final String PREF_COMIC = "ComicReaderPrefs";
    public static final String PREF_NOVEL = "NovelReaderPrefs";

    public static final String KEY_COMIC_IMAGE_SPACING_DP = "image_spacing_dp";
    public static final String KEY_COMIC_FIT_WIDTH = "fit_width";
    public static final String KEY_COMIC_KEEP_SCREEN_ON = "keep_screen_on";
    public static final String KEY_COMIC_AUTO_LOAD_NEXT = "auto_load_next";

    public static final String KEY_NOVEL_TEXT_SIZE = "text_size";
    public static final String KEY_NOVEL_THEME = "theme";
    public static final String KEY_NOVEL_AUTO_SCROLL_ENABLED = "auto_scroll_enabled";
    public static final String KEY_NOVEL_SCROLL_SPEED = "scroll_speed";
    public static final String KEY_NOVEL_SCROLL_CONFLICT_MODE = "scroll_conflict_mode";

    public static final int DEFAULT_COMIC_IMAGE_SPACING_DP = 0;
    public static final boolean DEFAULT_COMIC_FIT_WIDTH = true;
    public static final boolean DEFAULT_COMIC_KEEP_SCREEN_ON = true;
    public static final boolean DEFAULT_COMIC_AUTO_LOAD_NEXT = true;

    public static final float DEFAULT_NOVEL_TEXT_SIZE = 18f;
    public static final int DEFAULT_NOVEL_THEME = 0;
    public static final boolean DEFAULT_NOVEL_AUTO_SCROLL_ENABLED = false;
    public static final int DEFAULT_NOVEL_SCROLL_SPEED = 2;
    public static final int DEFAULT_NOVEL_SCROLL_CONFLICT_MODE = 1;
}
