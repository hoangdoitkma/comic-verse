package com.example.comicversev1.data.repository;

import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.model.BaseResponse;
import com.example.comicversev1.data.model.CommentDTO;
import com.example.comicversev1.data.model.CommentRequest;
import com.example.comicversev1.data.model.PageResponse;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;

@Singleton
public class CommentRepositoryImpl implements CommentRepository {

    private final ApiService apiService;

    @Inject
    public CommentRepositoryImpl(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public Single<PageResponse<CommentDTO>> getChapterComments(int chapterId, int page, int size) {
        return apiService.getComments(chapterId, page, size)
                .map(this::extractPage);
    }

    @Override
    public Single<PageResponse<CommentDTO>> getComicComments(int comicId, int page, int size) {
        return apiService.getComicComments(comicId, page, size)
                .map(this::extractPage);
    }

    @Override
    public Single<PageResponse<CommentDTO>> getReplies(int commentId, int page, int size) {
        return apiService.getReplies(commentId, page, size)
                .map(this::extractPage);
    }

    @Override
    public Single<CommentDTO> addChapterComment(int chapterId, CommentRequest request) {
        return apiService.addComment(chapterId, request)
                .map(this::extractComment);
    }

    @Override
    public Single<CommentDTO> addComicComment(int comicId, CommentRequest request) {
        return apiService.addComicComment(comicId, request)
                .map(this::extractComment);
    }

    private PageResponse<CommentDTO> extractPage(BaseResponse<PageResponse<CommentDTO>> response) throws Exception {
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new Exception(response.getMessage() != null ? response.getMessage() : "Khong the tai binh luan");
    }

    private CommentDTO extractComment(BaseResponse<CommentDTO> response) throws Exception {
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new Exception(response.getMessage() != null ? response.getMessage() : "Khong the gui binh luan");
    }

    public static PageResponse<CommentDTO> emptyPage() {
        PageResponse<CommentDTO> page = new PageResponse<>();
        page.setContent(new ArrayList<>());
        page.setTotalPages(1);
        return page;
    }
}
