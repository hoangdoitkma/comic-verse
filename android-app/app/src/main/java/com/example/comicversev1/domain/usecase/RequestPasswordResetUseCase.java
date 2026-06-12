package com.example.comicversev1.domain.usecase;

import com.example.comicversev1.data.model.BaseResponse;
import com.example.comicversev1.data.model.ForgotPasswordRequest;
import com.example.comicversev1.data.repository.AuthRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class RequestPasswordResetUseCase {
    private final AuthRepository repository;

    @Inject
    public RequestPasswordResetUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    public Single<BaseResponse<Object>> execute(String email) {
        return repository.forgotPassword(new ForgotPasswordRequest(email));
    }
}
