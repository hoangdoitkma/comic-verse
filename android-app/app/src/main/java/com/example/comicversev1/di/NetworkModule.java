package com.example.comicversev1.di;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.comicversev1.BuildConfig;
import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.model.BaseResponse;
import com.example.comicversev1.data.model.TokenResponse;
import com.example.comicversev1.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    private static final boolean IS_DEBUG = true; // TODO: wire to BuildConfig if needed

    @Provides
    @Singleton
    Gson provideGson() {
        return new GsonBuilder()
                .setLenient()
                .create();
    }

    @Provides
    @Singleton
    HttpLoggingInterceptor provideLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(IS_DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        return interceptor;
    }

    @Provides
    @Singleton
    Interceptor provideAuthInterceptor(SharedPreferences prefs) {
        return chain -> {
            String token = prefs.getString(Constants.KEY_ACCESS_TOKEN, "");
            return chain.proceed(
                    chain.request()
                            .newBuilder()
                            .header(Constants.HEADER_AUTH, token.isEmpty() ? "" : "Bearer " + token)
                            .header("ngrok-skip-browser-warning", "69420")
                            .build()
            );
        };
    }

    @Provides
    @Singleton
    Authenticator provideAuthenticator(SharedPreferences prefs, Provider<ApiService> apiServiceProvider) {
        return (route, response) -> {
            String refresh = prefs.getString(Constants.KEY_REFRESH_TOKEN, "");
            if (refresh.isEmpty()) return null;
            try {
                retrofit2.Response<BaseResponse<TokenResponse>> refreshResp =
                        apiServiceProvider.get().refreshToken(new TokenResponse(refresh)).execute();
                if (refreshResp.isSuccessful() && refreshResp.body() != null && refreshResp.body().getData() != null) {
                    String newAccess = refreshResp.body().getData().getAccessToken();
                    String newRefresh = refreshResp.body().getData().getRefreshToken();
                    prefs.edit()
                            .putString(Constants.KEY_ACCESS_TOKEN, newAccess)
                            .putString(Constants.KEY_REFRESH_TOKEN, newRefresh)
                            .apply();
                    return response.request().newBuilder()
                            .header(Constants.HEADER_AUTH, "Bearer " + newAccess)
                            .build();
                }
            } catch (Exception ignored) {
            }
            return null;
        };
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(HttpLoggingInterceptor loggingInterceptor,
                                     Interceptor authInterceptor,
                                     Authenticator authenticator) {
        return new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .authenticator(authenticator)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    RxJava3CallAdapterFactory provideRxAdapter() {
        return RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io());
    }

    @Provides
    @Singleton
    @Named("baseUrl")
    String provideBaseUrl() {
        return BuildConfig.BASE_URL;
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(@Named("baseUrl") String baseUrl,
                             Gson gson,
                             OkHttpClient okHttpClient,
                             RxJava3CallAdapterFactory rxAdapter) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(rxAdapter)
                .build();
    }

    @Provides
    @Singleton
    ApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }
}
