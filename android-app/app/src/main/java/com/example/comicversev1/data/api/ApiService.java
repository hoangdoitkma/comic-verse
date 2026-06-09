package com.example.comicversev1.data.api;

import com.example.comicversev1.data.model.BaseResponse;
import com.example.comicversev1.data.model.ChapterDetailDTO;
import com.example.comicversev1.data.model.ComicDetailDTO;
import com.example.comicversev1.data.model.ComicDTO;
import com.example.comicversev1.data.model.FavoriteDTO;
import com.example.comicversev1.data.model.FavoriteRequest;
import com.example.comicversev1.data.model.ForgotPasswordRequest;
import com.example.comicversev1.data.model.GenreDTO;
import com.example.comicversev1.data.model.GoogleLoginRequest;
import com.example.comicversev1.data.model.HotSearchDTO;
import com.example.comicversev1.data.model.LoginRequest;
import com.example.comicversev1.data.model.LoginResponse;
import com.example.comicversev1.data.model.ReadingHistoryRequest;
import com.example.comicversev1.data.model.ReadingHistoryInfoRequest;
import com.example.comicversev1.data.model.ReadingHistoryInfoDTO;
import com.example.comicversev1.data.model.ReadingHistorySyncDTO;
import com.example.comicversev1.data.model.ResetPasswordRequest;
import com.example.comicversev1.data.model.SearchHistoryItemDTO;
import com.example.comicversev1.data.model.SearchHistoryRequest;
import com.example.comicversev1.data.model.TokenResponse;
import com.example.comicversev1.data.model.ChapterItemDTO;

import java.util.List;

import com.example.comicversev1.data.model.ViewTrackingRequest;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Auth Module
    @POST("auth/login")
    Single<BaseResponse<LoginResponse>> login(@Body LoginRequest request);

    @POST("auth/google")
    Single<BaseResponse<LoginResponse>> loginWithGoogle(@Body GoogleLoginRequest request);

    @POST("auth/register")
    Single<BaseResponse<Object>> register(@Body com.example.comicversev1.data.model.RegisterRequest request);

    @POST("auth/forgot-password")
    Single<BaseResponse<Object>> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("auth/reset-password")
    Single<BaseResponse<Object>> resetPassword(@Body ResetPasswordRequest request);

    @POST("auth/public/refresh")
    Call<BaseResponse<TokenResponse>> refreshToken(@Body TokenResponse request);

    // Content Module
    @GET("comics")
    Single<BaseResponse<List<ComicDTO>>> getComics(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("keyword") String keyword,
            @Query("type") String type,
            @Query("country") String country,
            @Query("genreId") Integer genreId,
            @Query("status") String status
    );

    @POST("search-history")
    Completable recordSearch(@Body SearchHistoryRequest request);

    @GET("search-history")
    Single<BaseResponse<List<SearchHistoryItemDTO>>> getSearchHistory(
            @Query("type") String type,
            @Query("limit") int limit
    );

    @GET("search-history/hot")
    Single<BaseResponse<List<HotSearchDTO>>> getHotSearches(
            @Query("type") String type,
            @Query("limit") int limit
    );

    @DELETE("search-history")
    Completable deleteSearchHistoryItem(
            @Query("keyword") String keyword,
            @Query("type") String type
    );

    @DELETE("search-history/all")
    Completable clearSearchHistory(@Query("type") String type);

    @GET("comics/{slug}")
    Single<BaseResponse<ComicDetailDTO>> getComicDetail(@Path("slug") String slug);

    @GET("comics/{slug}/chapters")
    Single<BaseResponse<List<ChapterItemDTO>>> getChapters(@Path("slug") String slug);

    @GET("comics/id/{id}/chapters")
    Single<BaseResponse<List<ChapterItemDTO>>> getChaptersById(@Path("id") int comicId);

    @GET("chapters/{id}")
    Single<BaseResponse<ChapterDetailDTO>> getChapterContent(@Path("id") int chapterId);

    @GET("comics/home")
    Single<BaseResponse<com.example.comicversev1.data.model.HomeDataResponse>> getHomeContent(
            @Query("type") String type
    );

    // User Action
    @POST("reading-history")
    Completable updateReadingHistory(@Body ReadingHistoryRequest request);

    @POST("reading-history/sync")
    Completable syncReadingHistory(@Body List<ReadingHistoryRequest> requests);

    @GET("reading-history")
    Single<BaseResponse<List<ReadingHistorySyncDTO>>> getReadingHistory();

    @GET("favorites")
    Single<BaseResponse<List<FavoriteDTO>>> getFavorites();

    @POST("favorites")
    Completable addFavorite(@Body FavoriteRequest request);

    @POST("favorites/sync")
    Completable syncFavorites(@Body List<FavoriteRequest> requests);

    @DELETE("favorites/{slug}")
    Completable removeFavorite(@Path("slug") String slug);

    // Reading History Info (batch fetch comic info for local reading history)
    @POST("comics/reading-history-info")
    Single<BaseResponse<List<ReadingHistoryInfoDTO>>> getReadingHistoryInfo(@Body ReadingHistoryInfoRequest request);

    @POST("tracking/view")
    Completable trackView(@Body ViewTrackingRequest request);

    // Comments
    @GET("chapters/{chapterId}/comments")
    Single<BaseResponse<com.example.comicversev1.data.model.PageResponse<com.example.comicversev1.data.model.CommentDTO>>> getComments(
            @Path("chapterId") int chapterId,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("comments/{commentId}/replies")
    Single<BaseResponse<com.example.comicversev1.data.model.PageResponse<com.example.comicversev1.data.model.CommentDTO>>> getReplies(
            @Path("commentId") int commentId,
            @Query("page") int page,
            @Query("size") int size
    );

    @POST("chapters/{chapterId}/comments")
    Single<BaseResponse<com.example.comicversev1.data.model.CommentDTO>> addComment(
            @Path("chapterId") int chapterId,
            @Body com.example.comicversev1.data.model.CommentRequest request
    );

    @GET("comics/{comicId}/comments")
    Single<BaseResponse<com.example.comicversev1.data.model.PageResponse<com.example.comicversev1.data.model.CommentDTO>>> getComicComments(
            @Path("comicId") int comicId,
            @Query("page") int page,
            @Query("size") int size
    );

    @POST("comics/{comicId}/comments")
    Single<BaseResponse<com.example.comicversev1.data.model.CommentDTO>> addComicComment(
            @Path("comicId") int comicId,
            @Body com.example.comicversev1.data.model.CommentRequest request
    );

    // Reports
    @POST("public/chapters/{chapterId}/reports")
    Single<BaseResponse<com.example.comicversev1.data.model.ChapterReportResponse>> reportChapter(
            @Path("chapterId") int chapterId,
            @Body com.example.comicversev1.data.model.ChapterReportRequest request
    );

    // Payment
    @POST("payment/create-vip-order")
    Single<com.example.comicversev1.data.model.PaymentResponse> createVipOrder(
            @Body com.example.comicversev1.data.model.PaymentRequest request
    );

    @POST("payment/confirm-vip-order")
    Single<com.example.comicversev1.data.model.PaymentResponse> confirmVipOrder(
            @Body com.example.comicversev1.data.model.PaymentConfirmRequest request
    );

    // Profile
    @GET("user/profile")
    Single<BaseResponse<com.example.comicversev1.data.model.UserProfileDTO>> getUserProfile();

    @retrofit2.http.PUT("user/profile")
    Single<BaseResponse<Object>> updateProfile(@Body com.example.comicversev1.data.model.UpdateProfileRequest request);

    @retrofit2.http.Multipart
    @retrofit2.http.POST("user/profile/avatar")
    Single<BaseResponse<String>> uploadAvatar(@retrofit2.http.Part okhttp3.MultipartBody.Part file);

    @retrofit2.http.PUT("user/password")
    Single<BaseResponse<Object>> changePassword(@Body com.example.comicversev1.data.model.ChangePasswordRequest request);

    // VIP Packages
    @GET("data/vip-packages")
    Single<BaseResponse<List<com.example.comicversev1.data.model.VipPackageDTO>>> getVipPackages();

    @GET("data/genres")
    Single<BaseResponse<List<GenreDTO>>> getGenres();

    // Notifications
    @GET("notifications")
    Single<BaseResponse<List<com.example.comicversev1.data.model.NotificationDTO>>> getNotifications();

    @GET("notifications/unread-count")
    Single<BaseResponse<Long>> getUnreadNotificationCount();

    @retrofit2.http.PUT("notifications/{id}/read")
    Completable markNotificationAsRead(@Path("id") int id);

    @retrofit2.http.PUT("notifications/read-all")
    Completable markAllNotificationsAsRead();

    // Recommendations
    @GET("comics/recommendations")
    Single<BaseResponse<java.util.List<ComicDTO>>> getRecommendations(
            @Query("type") String type
    );

    @GET("comics/{slug}/similar")
    Single<BaseResponse<List<ComicDTO>>> getSimilarComics(@Path("slug") String slug);
}
