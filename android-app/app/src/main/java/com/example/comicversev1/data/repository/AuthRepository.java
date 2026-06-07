package com.example.comicversev1.data.repository;

import com.example.comicversev1.data.model.BaseResponse;
import com.example.comicversev1.data.model.ForgotPasswordRequest;
import com.example.comicversev1.data.model.GoogleLoginRequest;
import com.example.comicversev1.data.model.LoginRequest;
import com.example.comicversev1.data.model.LoginResponse;
import com.example.comicversev1.data.model.ResetPasswordRequest;
import com.example.comicversev1.data.model.TokenResponse;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import retrofit2.Call;

public interface AuthRepository {
    Single<BaseResponse<LoginResponse>> login(LoginRequest request);
    Single<BaseResponse<LoginResponse>> loginWithGoogle(GoogleLoginRequest request);
    Single<BaseResponse<Object>> register(com.example.comicversev1.data.model.RegisterRequest request);
    Single<BaseResponse<Object>> forgotPassword(ForgotPasswordRequest request);
    Single<BaseResponse<Object>> resetPassword(ResetPasswordRequest request);
    Call<BaseResponse<TokenResponse>> refreshToken(TokenResponse request);
    Completable saveTokens(String accessToken, String refreshToken);
}

