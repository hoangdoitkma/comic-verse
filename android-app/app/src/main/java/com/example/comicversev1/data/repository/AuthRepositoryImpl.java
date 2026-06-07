package com.example.comicversev1.data.repository;

import android.content.SharedPreferences;

import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.local.dao.FavoriteComicDao;
import com.example.comicversev1.data.local.dao.ReadingHistoryDao;
import com.example.comicversev1.data.model.BaseResponse;
import com.example.comicversev1.data.model.ForgotPasswordRequest;
import com.example.comicversev1.data.model.GoogleLoginRequest;
import com.example.comicversev1.data.model.LoginRequest;
import com.example.comicversev1.data.model.LoginResponse;
import com.example.comicversev1.data.model.ResetPasswordRequest;
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
    private final ReadingHistoryDao readingHistoryDao;
    private final FavoriteComicDao favoriteComicDao;

    @Inject
    public AuthRepositoryImpl(ApiService apiService,
                              SharedPreferences prefs,
                              ReadingHistoryDao readingHistoryDao,
                              FavoriteComicDao favoriteComicDao) {
        this.apiService = apiService;
        this.prefs = prefs;
        this.readingHistoryDao = readingHistoryDao;
        this.favoriteComicDao = favoriteComicDao;
    }

    @Override
    public Single<BaseResponse<LoginResponse>> login(LoginRequest request) {
        return apiService.login(request)
                .flatMap(response -> readingHistoryDao.deleteAllHistory()
                        .onErrorComplete()
                        .andThen(favoriteComicDao.deleteAllFavorites().onErrorComplete())
                        .andThen(saveTokensInternal(response.getData()))
                        .andThen(Single.just(response)));
    }

    @Override
    public Single<BaseResponse<LoginResponse>> loginWithGoogle(GoogleLoginRequest request) {
        return apiService.loginWithGoogle(request)
                .flatMap(response -> readingHistoryDao.deleteAllHistory()
                        .onErrorComplete()
                        .andThen(favoriteComicDao.deleteAllFavorites().onErrorComplete())
                        .andThen(saveTokensInternal(response.getData()))
                        .andThen(Single.just(response)));
    }

    @Override
    public Single<BaseResponse<Object>> register(com.example.comicversev1.data.model.RegisterRequest request) {
        return apiService.register(request);
    }

    @Override
    public Single<BaseResponse<Object>> forgotPassword(ForgotPasswordRequest request) {
        return apiService.forgotPassword(request);
    }

    @Override
    public Single<BaseResponse<Object>> resetPassword(ResetPasswordRequest request) {
        return apiService.resetPassword(request);
    }

    @Override
    public Call<BaseResponse<TokenResponse>> refreshToken(TokenResponse request) {
        return apiService.refreshToken(request);
    }

    @Override
    public Completable saveTokens(String accessToken, String refreshToken) {
        return Completable.fromAction(() -> prefs.edit()
                .putString(Constants.KEY_ACCESS_TOKEN, accessToken)
                .putString(Constants.KEY_REFRESH_TOKEN, refreshToken) // can be null if unsupported
                .apply());
    }

    private Completable saveTokensInternal(LoginResponse data) {
        if (data == null) return Completable.complete();
        return Completable.fromAction(() -> prefs.edit()
                .putString(Constants.KEY_ACCESS_TOKEN, data.getToken())
                .putString(Constants.KEY_REFRESH_TOKEN, data.getToken())
                .putString(Constants.KEY_DISPLAY_NAME, data.getDisplayName())
                .putString(Constants.KEY_EMAIL, data.getEmail())
                .putString(Constants.KEY_AVATAR_URL, data.getAvatarUrl())
                .apply());
    }

    private Completable saveTokensInternal(String accessToken, String refreshToken) {
        return saveTokens(accessToken, refreshToken);
    }
}
