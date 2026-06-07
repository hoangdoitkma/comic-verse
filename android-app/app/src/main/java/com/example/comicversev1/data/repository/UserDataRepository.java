package com.example.comicversev1.data.repository;

import io.reactivex.rxjava3.core.Completable;

public interface UserDataRepository {
    Completable syncLocalDataToServer();

    Completable clearLocalUserData();
}
