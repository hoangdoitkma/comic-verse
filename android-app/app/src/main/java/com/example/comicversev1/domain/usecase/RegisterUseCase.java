package com.example.comicversev1.domain.usecase;

import com.example.comicversev1.data.model.BaseResponse;
import com.example.comicversev1.data.model.RegisterRequest;
import com.example.comicversev1.data.repository.AuthRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class RegisterUseCase {
    private final AuthRepository repository;

    @Inject
    public RegisterUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    public Single<BaseResponse<Object>> execute(String email, String password, String displayName) {
        return repository.register(new RegisterRequest(email, password, displayName));
    }
}
