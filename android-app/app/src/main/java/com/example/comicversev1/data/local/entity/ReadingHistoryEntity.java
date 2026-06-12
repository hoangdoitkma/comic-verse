package com.example.comicversev1.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "reading_history")
public class ReadingHistoryEntity {

    @PrimaryKey
    @ColumnInfo(name = "comic_id")
    public int comicId;

    @ColumnInfo(name = "chapter_id")
    public int chapterId;

    @ColumnInfo(name = "page_index")
    public int pageIndex;

    @ColumnInfo(name = "read_at")
    public long readAt;

    @ColumnInfo(name = "percent")
    public int percent;

    @ColumnInfo(name = "comic_title")
    public String comicTitle;

    @ColumnInfo(name = "chapter_title")
    public String chapterTitle;

    @ColumnInfo(name = "cover_url")
    public String coverUrl;

    @ColumnInfo(name = "slug")
    public String slug;

    @ColumnInfo(name = "author_name")
    public String authorName;

    @ColumnInfo(name = "view_count")
    public long viewCount;

    @ColumnInfo(name = "comic_type")
    public String comicType;
}
