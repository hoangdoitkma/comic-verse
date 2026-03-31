package com.example.comicversev1.presentation.comments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.model.CommentDTO;
import com.example.comicversev1.data.model.CommentRequest;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class CommentViewModel extends ViewModel {
    private final CompositeDisposable disposable = new CompositeDisposable();
    private final ApiService apiService;

    @Inject
    public CommentViewModel(ApiService apiService) {
        this.apiService = apiService;
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
    private Integer activeComicId = null;
    private Integer activeChapterId = null;

    private final MutableLiveData<Integer> currentPageLiveData = new MutableLiveData<>(1);
    public LiveData<Integer> getCurrentPage() { return currentPageLiveData; }

    private final MutableLiveData<Integer> totalPagesLiveData = new MutableLiveData<>(1);
    public LiveData<Integer> getTotalPages() { return totalPagesLiveData; }

    // Since posting is immediate, we just have a livedata for post success
    private final MutableLiveData<CommentDTO> commentPostedLiveData = new MutableLiveData<>();
    public LiveData<CommentDTO> getCommentPosted() {
        return commentPostedLiveData;
    }

    public void loadComments(int chapterId, int page, int size) {
        currentPage = page;
        activeChapterId = chapterId;
        activeComicId = null;
        isLoading.setValue(true);
        disposable.add(apiService.getComments(chapterId, page, size)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    isLoading.setValue(false);
                    if (response.isSuccess()) {
                        totalPages = response.getData().getTotalPages();
                        if (totalPages == 0) totalPages = 1;
                        totalPagesLiveData.setValue(totalPages);
                        currentPageLiveData.setValue(currentPage + 1);
                        commentsLiveData.setValue(response.getData().getContent());
                    } else {
                        errorLiveData.setValue(response.getMessage());
                    }
                }, error -> {
                    isLoading.setValue(false);
                    errorLiveData.setValue(error.getMessage());
                }));
    }

    public void loadComicComments(int comicId, int page, int size) {
        currentPage = page;
        activeComicId = comicId;
        activeChapterId = null;
        isLoading.setValue(true);
        disposable.add(apiService.getComicComments(comicId, page, size)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    isLoading.setValue(false);
                    if (response.isSuccess()) {
                        totalPages = response.getData().getTotalPages();
                        if (totalPages == 0) totalPages = 1;
                        totalPagesLiveData.setValue(totalPages);
                        currentPageLiveData.setValue(currentPage + 1);
                        commentsLiveData.setValue(response.getData().getContent());
                    } else {
                        errorLiveData.setValue(response.getMessage());
                    }
                }, error -> {
                    isLoading.setValue(false);
                    errorLiveData.setValue(error.getMessage());
                }));
    }

    public void loadPage(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= totalPages) return;
        if (activeComicId != null) {
            loadComicComments(activeComicId, pageIndex, PAGE_SIZE);
        } else if (activeChapterId != null) {
            loadComments(activeChapterId, pageIndex, PAGE_SIZE);
        }
    }

    public void postComment(int chapterId, String content, Integer parentId) {
        CommentRequest request = new CommentRequest(parentId, content);
        disposable.add(apiService.addComment(chapterId, request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.isSuccess()) {
                        commentPostedLiveData.setValue(response.getData());
                    } else {
                        errorLiveData.setValue(response.getMessage());
                    }
                }, error -> {
                    errorLiveData.setValue(error.getMessage());
                }));
    }

    public void postComicComment(int comicId, String content, Integer parentId) {
        CommentRequest request = new CommentRequest(parentId, content);
        disposable.add(apiService.addComicComment(comicId, request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.isSuccess()) {
                        commentPostedLiveData.setValue(response.getData());
                    } else {
                        errorLiveData.setValue(response.getMessage());
                    }
                }, error -> {
                    errorLiveData.setValue(error.getMessage());
                }));
    }

    public void loadReplies(int commentId, int page, int size, ReplyCallback callback) {
        disposable.add(apiService.getReplies(commentId, page, size)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.isSuccess()) {
                        callback.onRepliesLoaded(response.getData().getContent());
                    } else {
                        callback.onError(response.getMessage());
                    }
                }, error -> {
                    callback.onError(error.getMessage());
                }));
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
