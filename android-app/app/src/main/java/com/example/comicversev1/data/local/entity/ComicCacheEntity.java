package com.example.comicversev1.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "comic_cache")
public class ComicCacheEntity {
    @PrimaryKey
    @ColumnInfo(name = "comic_id")
    public int comicId;

    @ColumnInfo(name = "slug")
    public String slug;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "cover_image")
    public String coverImage;

    @ColumnInfo(name = "author")
    public String author;

    @ColumnInfo(name = "view_count")
    public long viewCount;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;
}

