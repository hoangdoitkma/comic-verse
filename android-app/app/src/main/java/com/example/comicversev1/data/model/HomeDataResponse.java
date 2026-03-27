package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HomeDataResponse {
    @SerializedName("topTrending")
    public List<ComicDTO> topTrending;
    
    @SerializedName("recentlyUpdated")
    public List<ComicDTO> recentlyUpdated;
    
    @SerializedName("newComics")
    public List<ComicDTO> newComics;
    
    @SerializedName("recommended")
    public List<ComicDTO> recommended;
}
