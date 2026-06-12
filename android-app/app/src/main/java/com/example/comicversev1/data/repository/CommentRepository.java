package com.example.comicversev1.data.repository;

import com.example.comicversev1.data.model.CommentDTO;
import com.example.comicversev1.data.model.CommentRequest;
import com.example.comicversev1.data.model.PageResponse;

import io.reactivex.rxjava3.core.Single;

public interface CommentRepository {
    Single<PageResponse<CommentDTO>> getChapterComments(int chapterId, int page, int size);

    Single<PageResponse<CommentDTO>> getComicComments(int comicId, int page, int size);

    Single<PageResponse<CommentDTO>> getReplies(int commentId, int page, int size);

    Single<CommentDTO> addChapterComment(int chapterId, CommentRequest request);

    Single<CommentDTO> addComicComment(int comicId, CommentRequest request);
}
