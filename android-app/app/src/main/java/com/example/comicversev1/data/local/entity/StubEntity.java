package com.example.comicversev1.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stub")
public class StubEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
}

