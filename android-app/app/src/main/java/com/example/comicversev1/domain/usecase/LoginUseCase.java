package com.example.comicversev1.domain.usecase;

import com.example.comicversev1.data.model.BaseResponse;
import com.example.comicversev1.data.model.LoginRequest;
import com.example.comicversev1.data.model.LoginResponse;
import com.example.comicversev1.data.repository.AuthRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class LoginUseCase {
    private final AuthRepository repository;

    @Inject
    public LoginUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    public Single<BaseResponse<LoginResponse>> execute(String email, String password) {
        return repository.login(new LoginRequest(email, password));
    }
}

