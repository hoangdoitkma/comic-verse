package com.example.comicversev1.domain.entity;

import java.util.List;

public class HomeContent {
    private final String greetingTitle;
    private final String greetingSubtitle;
    private final List<Hero> heroes;
    private final List<QuickAction> quickActions;
    private final ContinueReading continueReading;
    private final List<ComicCard> recentlyRead;
    private final List<ComicCard> recommendations;
    private final List<ComicCard> newUpdates;
    private final List<ComicCard> hotComics;
    private final List<ComicCard> completed;
    private final List<ComicCard> newComics;

    public HomeContent(
            String greetingTitle,
            String greetingSubtitle,
            List<Hero> heroes,
            List<QuickAction> quickActions,
            ContinueReading continueReading,
            List<ComicCard> recentlyRead,
            List<ComicCard> recommendations,
            List<ComicCard> newUpdates,
            List<ComicCard> hotComics,
            List<ComicCard> completed,
            List<ComicCard> newComics
    ) {
        this.greetingTitle = greetingTitle;
        this.greetingSubtitle = greetingSubtitle;
        this.heroes = heroes;
        this.quickActions = quickActions;
        this.continueReading = continueReading;
        this.recentlyRead = recentlyRead;
        this.recommendations = recommendations;
        this.newUpdates = newUpdates;
        this.hotComics = hotComics;
        this.completed = completed;
        this.newComics = newComics;
    }

    public String getGreetingTitle() {
        return greetingTitle;
    }

    public String getGreetingSubtitle() {
        return greetingSubtitle;
    }

    public List<Hero> getHeroes() {
        return heroes;
    }

    public List<QuickAction> getQuickActions() {
        return quickActions;
    }

    public ContinueReading getContinueReading() {
        return continueReading;
    }

    public List<ComicCard> getRecentlyRead() {
        return recentlyRead;
    }

    public List<ComicCard> getRecommendations() {
        return recommendations;
    }

    public List<ComicCard> getNewUpdates() {
        return newUpdates;
    }

    public List<ComicCard> getHotComics() {
        return hotComics;
    }

    public List<ComicCard> getCompleted() {
        return completed;
    }

    public List<ComicCard> getNewComics() {
        return newComics;
    }

    public static class Hero {
        private final String slug;
        private final String title;
        private final String description;
        private final String cta;
        private final String imageUrl;

        public Hero(String slug, String title, String description, String cta, String imageUrl) {
            this.slug = slug;
            this.title = title;
            this.description = description;
            this.cta = cta;
            this.imageUrl = imageUrl;
        }

        public String getSlug() {
            return slug;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getCta() {
            return cta;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }

    public static class QuickAction {
        private final String id;
        private final String title;
        private final String subtitle;
        private final int iconRes;

        public QuickAction(String id, String title, String subtitle, int iconRes) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
            this.iconRes = iconRes;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public int getIconRes() {
            return iconRes;
        }
    }

    public static class ContinueReading {
        private final String comicTitle;
        private final String chapterLabel;
        private final int progress;

        public ContinueReading(String comicTitle, String chapterLabel, int progress) {
            this.comicTitle = comicTitle;
            this.chapterLabel = chapterLabel;
            this.progress = progress;
        }

        public String getComicTitle() {
            return comicTitle;
        }

        public String getChapterLabel() {
            return chapterLabel;
        }

        public int getProgress() {
            return progress;
        }
    }

    public static class ComicCard {
        private final String slug;
        private final String title;
        private final String chapterLabel;
        private final String coverUrl;
        private final long likes;
        private final long views;
        private final int progress; // percentage 0-100
        private final String timeLabel; // e.g., "24 phút trước"
        private final String accessType;
        private final String authorName;

        public ComicCard(String slug, String title, String chapterLabel, String coverUrl, long likes, long views, int progress, String timeLabel, String accessType, String authorName) {
            this.slug = slug;
            this.title = title;
            this.chapterLabel = chapterLabel;
            this.coverUrl = coverUrl;
            this.likes = likes;
            this.views = views;
            this.progress = progress;
            this.timeLabel = timeLabel;
            this.accessType = accessType;
            this.authorName = authorName;
        }

        public String getSlug() {
            return slug;
        }

        public String getTitle() {
            return title;
        }

        public String getChapterLabel() {
            return chapterLabel;
        }

        public String getCoverUrl() {
            return coverUrl;
        }

        public long getLikes() {
            return likes;
        }

        public long getViews() {
            return views;
        }

        public int getProgress() {
            return progress;
        }

        public String getTimeLabel() {
            return timeLabel;
        }

        public String getAccessType() {
            return accessType;
        }

        public String getAuthorName() {
            return authorName;
        }
    }
}
