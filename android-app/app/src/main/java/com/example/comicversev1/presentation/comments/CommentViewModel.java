package com.example.comicversev1.presentation.comments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.comicversev1.data.model.CommentDTO;
import com.example.comicversev1.data.model.CommentRequest;
import com.example.comicversev1.data.model.PageResponse;
import com.example.comicversev1.data.repository.CommentRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class CommentViewModel extends ViewModel {
    private final CompositeDisposable disposable = new CompositeDisposable();
    private final CommentRepository commentRepository;

    @Inject
    public CommentViewModel(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    private final MutableLiveData<List<CommentDTO>> commentsLiveData = new MutableLiveData<>();
    public LiveData<List<CommentDTO>> getComments() {
        return commentsLiveData;
    }

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    public LiveData<String> getError() {
        return errorLiveData;
    }

    // Pagination states
    private int currentPage = 0;
    private final int PAGE_SIZE = 5;
    private int totalPages = 1;
    private Integer activeTargetId = null;
    private CommentTargetType activeTargetType = null;

    private final MutableLiveData<Integer> currentPageLiveData = new MutableLiveData<>(1);
    public LiveData<Integer> getCurrentPage() { return currentPageLiveData; }

    private final MutableLiveData<Integer> totalPagesLiveData = new MutableLiveData<>(1);
    public LiveData<Integer> getTotalPages() { return totalPagesLiveData; }

    private final MutableLiveData<Long> totalElementsLiveData = new MutableLiveData<>(0L);
    public LiveData<Long> getTotalElements() { return totalElementsLiveData; }

    private final MutableLiveData<Boolean> isPosting = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsPosting() { return isPosting; }

    // Since posting is immediate, we just have a livedata for post success
    private final MutableLiveData<CommentDTO> commentPostedLiveData = new MutableLiveData<>();
    public LiveData<CommentDTO> getCommentPosted() {
        return commentPostedLiveData;
    }

    public void loadComments(int chapterId, int page, int size) {
        loadComments(CommentTargetType.CHAPTER, chapterId, page, size);
    }

    public void loadComicComments(int comicId, int page, int size) {
        loadComments(CommentTargetType.COMIC, comicId, page, size);
    }

    public void loadComments(CommentTargetType targetType, int targetId, int page, int size) {
        currentPage = page;
        activeTargetType = targetType;
        activeTargetId = targetId;
        isLoading.setValue(true);

        io.reactivex.rxjava3.core.Single<PageResponse<CommentDTO>> source =
                targetType == CommentTargetType.COMIC
                        ? commentRepository.getComicComments(targetId, page, size)
                        : commentRepository.getChapterComments(targetId, page, size);

        disposable.add(source
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onCommentsLoaded, error -> {
                    isLoading.setValue(false);
                    errorLiveData.setValue(toUserMessage(error));
                }));
    }

    public void loadPage(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= totalPages) return;
        if (activeTargetType != null && activeTargetId != null) {
            loadComments(activeTargetType, activeTargetId, pageIndex, PAGE_SIZE);
        }
    }

    public void postComment(int chapterId, String content, Integer parentId) {
        activeTargetType = CommentTargetType.CHAPTER;
        activeTargetId = chapterId;
        postComment(content, parentId);
    }

    public void postComicComment(int comicId, String content, Integer parentId) {
        activeTargetType = CommentTargetType.COMIC;
        activeTargetId = comicId;
        postComment(content, parentId);
    }

    public void postComment(String content, Integer parentId) {
        if (activeTargetType == null || activeTargetId == null) {
            errorLiveData.setValue("Chưa xác định được nơi đăng bình luận");
            return;
        }

        CommentRequest request = new CommentRequest(parentId, content);
        io.reactivex.rxjava3.core.Single<CommentDTO> source =
                activeTargetType == CommentTargetType.COMIC
                        ? commentRepository.addComicComment(activeTargetId, request)
                        : commentRepository.addChapterComment(activeTargetId, request);

        isPosting.setValue(true);
        disposable.add(source
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comment -> {
                    isPosting.setValue(false);
                    applyPostedComment(comment, parentId);
                    commentPostedLiveData.setValue(comment);
                }, error -> {
                    isPosting.setValue(false);
                    errorLiveData.setValue(toUserMessage(error));
                }));
    }

    public void loadReplies(int commentId, int page, int size, ReplyCallback callback) {
        disposable.add(commentRepository.getReplies(commentId, page, size)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> callback.onRepliesLoaded(response.getContent()), error -> {
                    callback.onError(toUserMessage(error));
                }));
    }

    private String toUserMessage(Throwable error) {
        String message = error != null ? error.getMessage() : null;
        if (message == null || message.trim().isEmpty()) {
            return "Khong the tai binh luan. Vui long thu lai.";
        }
        String lower = message.toLowerCase();
        if (message.length() > 140
                || message.contains("{")
                || lower.contains("payload")
                || lower.contains("jwt")
                || lower.contains("token")) {
            return "Phien dang nhap hoac du lieu binh luan khong hop le. Vui long thu lai.";
        }
        return message;
    }

    private void onCommentsLoaded(PageResponse<CommentDTO> pageResponse) {
        isLoading.setValue(false);
        totalPages = pageResponse.getTotalPages();
        if (totalPages == 0) totalPages = 1;
        totalPagesLiveData.setValue(totalPages);
        totalElementsLiveData.setValue(pageResponse.getTotalElements());
        currentPageLiveData.setValue(currentPage + 1);
        commentsLiveData.setValue(pageResponse.getContent());
    }

    private void applyPostedComment(CommentDTO comment, Integer parentId) {
        List<CommentDTO> current = commentsLiveData.getValue();
        List<CommentDTO> updated = current == null ? new ArrayList<>() : new ArrayList<>(current);

        if (parentId == null) {
            updated.add(0, comment);
            commentsLiveData.setValue(updated);
            Long total = totalElementsLiveData.getValue();
            totalElementsLiveData.setValue((total == null ? 0L : total) + 1L);
            return;
        }

        for (CommentDTO parent : updated) {
            if (parent.getId() != null && parent.getId().equals(parentId)) {
                int replyCount = parent.getReplyCount() == null ? 0 : parent.getReplyCount();
                parent.setReplyCount(replyCount + 1);

                if (parent.isRepliesLoaded() && parent.getReplies() != null) {
                    List<CommentDTO> replies = new ArrayList<>(parent.getReplies());
                    replies.add(0, comment);
                    parent.setReplies(replies);
                }
                break;
            }
        }
        commentsLiveData.setValue(updated);
    }

    public interface ReplyCallback {
        void onRepliesLoaded(List<CommentDTO> replies);
        void onError(String message);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
