package com.example.comicversev1.data.repository;

import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.model.BaseResponse;
import com.example.comicversev1.data.model.ChangePasswordRequest;
import com.example.comicversev1.data.model.UpdateProfileRequest;
import com.example.comicversev1.data.model.UserProfileDTO;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;

@Singleton
public class UserProfileRepositoryImpl implements UserProfileRepository {

    private final ApiService apiService;

    @Inject
    public UserProfileRepositoryImpl(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public Single<UserProfileDTO> getUserProfile() {
        return apiService.getUserProfile()
                .map(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        return response.getData();
                    }
                    throw new Exception(errorMessage(response, "Khong the tai thong tin nguoi dung"));
                });
    }

    @Override
    public Completable updateProfile(UpdateProfileRequest request) {
        return apiService.updateProfile(request)
                .flatMapCompletable(response -> response.isSuccess()
                        ? Completable.complete()
                        : Completable.error(new Exception(errorMessage(response, "Khong the cap nhat ho so"))));
    }

    @Override
    public Completable uploadAvatar(MultipartBody.Part file) {
        return apiService.uploadAvatar(file)
                .flatMapCompletable(response -> response.isSuccess()
                        ? Completable.complete()
                        : Completable.error(new Exception(errorMessage(response, "Khong the cap nhat avatar"))));
    }

    @Override
    public Single<String> changePassword(ChangePasswordRequest request) {
        return apiService.changePassword(request)
                .map(response -> {
                    if (response.isSuccess()) {
                        return response.getMessage() != null ? response.getMessage() : "Doi mat khau thanh cong";
                    }
                    throw new Exception(errorMessage(response, "Khong the doi mat khau"));
                });
    }

    private String errorMessage(BaseResponse<?> response, String fallback) {
        return response != null && response.getMessage() != null ? response.getMessage() : fallback;
    }
}
