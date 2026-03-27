package com.example.comicversev1.data.local.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.example.comicversev1.data.local.entity.StubEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface StubDao {
    @Query("SELECT * FROM stub")
    Single<List<StubEntity>> getAll();
}

