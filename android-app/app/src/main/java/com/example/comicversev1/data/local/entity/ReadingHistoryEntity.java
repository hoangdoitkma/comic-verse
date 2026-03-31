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
}

