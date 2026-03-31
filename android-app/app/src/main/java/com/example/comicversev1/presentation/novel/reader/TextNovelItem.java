package com.example.comicversev1.presentation.novel.reader;

public abstract class TextNovelItem {

    // View Types Constant
    public static final int TYPE_TITLE = 0;
    public static final int TYPE_PARAGRAPH = 1;
    public static final int TYPE_DIVIDER = 2;
    public static final int TYPE_PAYWALL = 3;
    public static final int TYPE_LOADING = 4;

    public abstract int getViewType();

    public static class TitleItem extends TextNovelItem {
        private final String title;

        public TitleItem(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        @Override
        public int getViewType() {
            return TYPE_TITLE;
        }
    }

    public static class ParagraphItem extends TextNovelItem {
        private final int chapterId;
        private final int paragraphIndex;
        private final String content;

        public ParagraphItem(int chapterId, int paragraphIndex, String content) {
            this.chapterId = chapterId;
            this.paragraphIndex = paragraphIndex;
            this.content = content;
        }

        public int getChapterId() {
            return chapterId;
        }

        public int getParagraphIndex() {
            return paragraphIndex;
        }

        public String getContent() {
            return content;
        }

        @Override
        public int getViewType() {
            return TYPE_PARAGRAPH;
        }
    }

    public static class DividerItem extends TextNovelItem {
        @Override
        public int getViewType() {
            return TYPE_DIVIDER;
        }
    }

    public static class PaywallItem extends TextNovelItem {
        private final int chapterId;

        public PaywallItem(int chapterId) {
            this.chapterId = chapterId;
        }

        public int getChapterId() {
            return chapterId;
        }

        @Override
        public int getViewType() {
            return TYPE_PAYWALL;
        }
    }

    public static class LoadingItem extends TextNovelItem {
        @Override
        public int getViewType() {
            return TYPE_LOADING;
        }
    }
}
