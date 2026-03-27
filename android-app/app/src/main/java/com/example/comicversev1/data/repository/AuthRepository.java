package com.example.comicversev1.data.repository;

import com.example.comicversev1.data.model.BaseResponse;
import com.example.comicversev1.data.model.LoginRequest;
import com.example.comicversev1.data.model.LoginResponse;
import com.example.comicversev1.data.model.TokenResponse;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import retrofit2.Call;

public interface AuthRepository {
    Single<BaseResponse<LoginResponse>> login(LoginRequest request);
    Call<BaseResponse<TokenResponse>> refreshToken(TokenResponse request);
    Completable saveTokens(String accessToken, String refreshToken);
}

