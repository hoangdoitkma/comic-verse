package com.example.comicversev1.data.repository;

import android.content.SharedPreferences;

import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.model.BaseResponse;
import com.example.comicversev1.data.model.LoginRequest;
import com.example.comicversev1.data.model.LoginResponse;
import com.example.comicversev1.data.model.TokenResponse;
import com.example.comicversev1.utils.Constants;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import retrofit2.Call;

@Singleton
public class AuthRepositoryImpl implements AuthRepository {

    private final ApiService apiService;
    private final SharedPreferences prefs;

    @Inject
    public AuthRepositoryImpl(ApiService apiService, SharedPreferences prefs) {
        this.apiService = apiService;
        this.prefs = prefs;
    }

    @Override
    public Single<BaseResponse<LoginResponse>> login(LoginRequest request) {
        return apiService.login(request)
                .flatMap(response -> saveTokensInternal(response.getData())
                        .andThen(Single.just(response)));
    }

    @Override
    public Call<BaseResponse<TokenResponse>> refreshToken(TokenResponse request) {
        return apiService.refreshToken(request);
    }

    @Override
    public Completable saveTokens(String accessToken, String refreshToken) {
        return Completable.fromAction(() -> prefs.edit()
                .putString(Constants.KEY_ACCESS_TOKEN, accessToken)
                .putString(Constants.KEY_REFRESH_TOKEN, refreshToken)
                .apply());
    }

    private Completable saveTokensInternal(LoginResponse data) {
        if (data == null) return Completable.complete();
        return saveTokens(data.getAccessToken(), data.getRefreshToken());
    }

    private Completable saveTokensInternal(String accessToken, String refreshToken) {
        return saveTokens(accessToken, refreshToken);
    }
}
