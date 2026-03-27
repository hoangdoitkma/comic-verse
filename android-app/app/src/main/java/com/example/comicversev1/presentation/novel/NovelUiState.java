package com.example.comicversev1.presentation.novel;

import com.example.comicversev1.domain.entity.HomeContent;

import java.util.Collections;
import java.util.List;

public class NovelUiState {

    private final boolean isLoading;
    private final String greetingTitle;
    private final String greetingSubtitle;
    private final List<HomeContent.Hero> heroes;
    private final List<HomeContent.QuickAction> quickActions;
    private final String continueSubtitle;
    private final int continueProgress;
    private final List<HomeContent.ComicCard> recentlyRead;
    private final List<HomeContent.ComicCard> recommendations;
    private final List<HomeContent.ComicCard> newUpdates;
    private final List<HomeContent.ComicCard> hotComics;
    private final List<HomeContent.ComicCard> completed;
    private final List<HomeContent.ComicCard> newComics;
    private final String errorMessage;

    private NovelUiState(boolean isLoading,
                         String greetingTitle,
                         String greetingSubtitle,
                         List<HomeContent.Hero> heroes,
                         List<HomeContent.QuickAction> quickActions,
                         String continueSubtitle,
                         int continueProgress,
                         List<HomeContent.ComicCard> recentlyRead,
                         List<HomeContent.ComicCard> recommendations,
                         List<HomeContent.ComicCard> newUpdates,
                         List<HomeContent.ComicCard> hotComics,
                         List<HomeContent.ComicCard> completed,
                         List<HomeContent.ComicCard> newComics,
                         String errorMessage) {
        this.isLoading = isLoading;
        this.greetingTitle = greetingTitle;
        this.greetingSubtitle = greetingSubtitle;
        this.heroes = heroes;
        this.quickActions = quickActions;
        this.continueSubtitle = continueSubtitle;
        this.continueProgress = continueProgress;
        this.recentlyRead = recentlyRead;
        this.recommendations = recommendations;
        this.newUpdates = newUpdates;
        this.hotComics = hotComics;
        this.completed = completed;
        this.newComics = newComics;
        this.errorMessage = errorMessage;
    }

    public static NovelUiState loading() {
        return new NovelUiState(true, "", "", Collections.emptyList(), Collections.emptyList(), "", 0,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);
    }

    public static NovelUiState from(HomeContent content) {
        String continueSubtitle = content.getContinueReading() != null
                ? content.getContinueReading().getComicTitle() + " - " + content.getContinueReading().getChapterLabel()
                : "";
        int progress = content.getContinueReading() != null ? content.getContinueReading().getProgress() : 0;
        return new NovelUiState(false,
                content.getGreetingTitle(),
                content.getGreetingSubtitle(),
                content.getHeroes(),
                content.getQuickActions(),
                continueSubtitle,
                progress,
                content.getRecentlyRead(),
                content.getRecommendations(),
                content.getNewUpdates(),
                content.getHotComics(),
                content.getCompleted(),
                content.getNewComics(),
                null);
    }

    public static NovelUiState error(String message) {
        return new NovelUiState(false, "", "", Collections.emptyList(), Collections.emptyList(), "", 0,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), message);
    }

    public boolean isLoading() { return isLoading; }
    public String getGreetingTitle() { return greetingTitle; }
    public String getGreetingSubtitle() { return greetingSubtitle; }
    public List<HomeContent.Hero> getHeroes() { return heroes; }
    public List<HomeContent.QuickAction> getQuickActions() { return quickActions; }
    public String getContinueSubtitle() { return continueSubtitle; }
    public int getContinueProgress() { return continueProgress; }
    public List<HomeContent.ComicCard> getRecentlyRead() { return recentlyRead; }
    public List<HomeContent.ComicCard> getRecommendations() { return recommendations; }
    public List<HomeContent.ComicCard> getNewUpdates() { return newUpdates; }
    public List<HomeContent.ComicCard> getHotComics() { return hotComics; }
    public List<HomeContent.ComicCard> getCompleted() { return completed; }
    public List<HomeContent.ComicCard> getNewComics() { return newComics; }
    public String getErrorMessage() { return errorMessage; }
}

