package com.example.comicversev1.data.api;

import com.example.comicversev1.data.model.BaseResponse;
import com.example.comicversev1.data.model.ChapterDetailDTO;
import com.example.comicversev1.data.model.ComicDetailDTO;
import com.example.comicversev1.data.model.ComicDTO;
import com.example.comicversev1.data.model.LoginRequest;
import com.example.comicversev1.data.model.LoginResponse;
import com.example.comicversev1.data.model.ReadingHistoryRequest;
import com.example.comicversev1.data.model.ReadingHistoryInfoRequest;
import com.example.comicversev1.data.model.ReadingHistoryInfoDTO;
import com.example.comicversev1.data.model.TokenResponse;
import com.example.comicversev1.data.model.ChapterItemDTO;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Auth Module
    @POST("auth/login")
    Single<BaseResponse<LoginResponse>> login(@Body LoginRequest request);

    @POST("auth/refresh")
    Call<BaseResponse<TokenResponse>> refreshToken(@Body TokenResponse request);

    // Content Module
    @GET("comics")
    Single<BaseResponse<List<ComicDTO>>> getComics(
            @Query("page") int page,
            @Query("limit") int limit
    );

    @GET("comics/{slug}")
    Single<BaseResponse<ComicDetailDTO>> getComicDetail(@Path("slug") String slug);

    @GET("comics/{slug}/chapters")
    Single<BaseResponse<List<ChapterItemDTO>>> getChapters(@Path("slug") String slug);

    @GET("chapters/{id}")
    Single<BaseResponse<ChapterDetailDTO>> getChapterContent(@Path("id") int chapterId);

    @GET("comics/home")
    Single<BaseResponse<com.example.comicversev1.data.model.HomeDataResponse>> getHomeContent();

    // User Action
    @POST("reading-history")
    Completable updateReadingHistory(@Body ReadingHistoryRequest request);

    // Reading History Info (batch fetch comic info for local reading history)
    @POST("comics/reading-history-info")
    Single<BaseResponse<List<ReadingHistoryInfoDTO>>> getReadingHistoryInfo(@Body ReadingHistoryInfoRequest request);
}
