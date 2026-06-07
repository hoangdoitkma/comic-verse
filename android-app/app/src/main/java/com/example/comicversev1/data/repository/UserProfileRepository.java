package com.example.comicversev1.data.repository;

import com.example.comicversev1.data.model.ChangePasswordRequest;
import com.example.comicversev1.data.model.UpdateProfileRequest;
import com.example.comicversev1.data.model.UserProfileDTO;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;

public interface UserProfileRepository {
    Single<UserProfileDTO> getUserProfile();

    Completable updateProfile(UpdateProfileRequest request);

    Completable uploadAvatar(MultipartBody.Part file);

    Single<String> changePassword(ChangePasswordRequest request);
}
