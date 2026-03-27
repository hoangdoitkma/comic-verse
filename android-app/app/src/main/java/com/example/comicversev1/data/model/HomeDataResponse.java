package com.example.comicversev1.data.model;

import com.example.comicversev1.domain.entity.HomeContent;
import java.util.List;

public class HomeDataResponse {
    public List<HomeContent.Hero> heroes;
    public List<HomeContent.ComicCard> recent;
    public List<HomeContent.ComicCard> recommendations;
    public List<HomeContent.ComicCard> newUpdates;
    public List<HomeContent.ComicCard> hotComics;
    public List<HomeContent.ComicCard> completed;
    public List<HomeContent.ComicCard> newComics;
}
