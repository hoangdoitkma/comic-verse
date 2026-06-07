package com.example.comicversev1.domain.usecase;

import com.example.comicversev1.data.model.BaseResponse;
import com.example.comicversev1.data.model.ResetPasswordRequest;
import com.example.comicversev1.data.repository.AuthRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class ResetPasswordUseCase {
    private final AuthRepository repository;

    @Inject
    public ResetPasswordUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    public Single<BaseResponse<Object>> execute(String email, String otp, String newPassword) {
        return repository.resetPassword(new ResetPasswordRequest(email, otp, newPassword));
    }
}
