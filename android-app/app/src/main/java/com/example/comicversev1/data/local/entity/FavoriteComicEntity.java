package com.example.comicversev1.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_comics")
public class FavoriteComicEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "slug")
    public String slug;

    @ColumnInfo(name = "comic_title")
    public String comicTitle;

    @ColumnInfo(name = "cover_url")
    public String coverUrl;

    @ColumnInfo(name = "comic_type")
    public String comicType;

    @ColumnInfo(name = "added_at")
    public long addedAt;
    
    public FavoriteComicEntity(@NonNull String slug, String comicTitle, String coverUrl, String comicType, long addedAt) {
        this.slug = slug;
        this.comicTitle = comicTitle;
        this.coverUrl = coverUrl;
        this.comicType = comicType;
        this.addedAt = addedAt;
    }
}
