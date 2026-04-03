package com.example.comicversev1.presentation.novel.reader;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.local.dao.ReadingHistoryDao;
import com.example.comicversev1.data.local.entity.ReadingHistoryEntity;
import com.example.comicversev1.data.model.ViewTrackingRequest;
import com.example.comicversev1.domain.entity.ChapterEntity;
import com.example.comicversev1.domain.usecase.GetChapterDetailUseCase;
import com.example.comicversev1.domain.entity.ChapterItem;
import com.example.comicversev1.data.local.dao.ComicCacheDao;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@HiltViewModel
public class TextNovelViewModel extends ViewModel {

    private static final String TAG = "TextNovelViewModel";

    private final GetChapterDetailUseCase getChapterDetailUseCase;
    private final ReadingHistoryDao readingHistoryDao;
    private final ComicCacheDao comicCacheDao;
    private final ApiService apiService;
    private final CompositeDisposable disposables = new CompositeDisposable();
    
    // Manual singleton client to avoid Hilt injection issues if not provided in Module
    private final OkHttpClient httpClient = new OkHttpClient();
    private final Gson gson = new Gson();

    private final int comicId;
    private final int initialChapterId;
    
    // Cached variables config UI for history
    private String cachedComicTitle = "Truyện Chữ";
    private String cachedCoverUrl = "";
    private String cachedSlug = "";
    private String cachedAuthorName = "Unknown";
    private long cachedViewCount = 0;

    // Trạng thái load cho UI
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<String> _errorEvent = new MutableLiveData<>();
    public LiveData<String> errorEvent() { return _errorEvent; }

    // Event append dải items khi lấy thành công JSON
    private final MutableLiveData<List<TextNovelItem>> _appendItemsEvent = new MutableLiveData<>();
    public LiveData<List<TextNovelItem>> appendItemsEvent() { return _appendItemsEvent; }

    private final MutableLiveData<Boolean> _clearItemsEvent = new MutableLiveData<>();
    public LiveData<Boolean> clearItemsEvent() { return _clearItemsEvent; }

    private final MutableLiveData<Boolean> _reportSuccessEvent = new MutableLiveData<>();
    public LiveData<Boolean> reportSuccessEvent() { return _reportSuccessEvent; }

    private final MutableLiveData<ChapterEntity> _currentChapterEvent = new MutableLiveData<>();
    public LiveData<ChapterEntity> currentChapterEvent() { return _currentChapterEvent; }

    private final MutableLiveData<List<ChapterItem>> _chapterListEvent = new MutableLiveData<>();
    public LiveData<List<ChapterItem>> chapterListEvent() { return _chapterListEvent; }
    private boolean isChapterListLoaded = false;

    private final Set<Integer> loadedChapterIds = new HashSet<>();
    private final java.util.Map<Integer, String> chapterTitleCache = new java.util.concurrent.ConcurrentHashMap<>();
    private Integer pendingNextChapterId = null;
    private Integer activePrevChapterId = null;
    private boolean isLoadingMore = false;
    
    // Dữ liệu db
    private int savedChapterId = -1;
    private Integer pendingScrollToRelativeParagraph = null;

    private final PublishSubject<ReadingHistoryEntity> saveProgressSubject = PublishSubject.create();

    @Inject
    public TextNovelViewModel(GetChapterDetailUseCase getChapterDetailUseCase,
                              ReadingHistoryDao readingHistoryDao,
                              ComicCacheDao comicCacheDao,
                              SavedStateHandle handle,
                              ApiService apiService) {
        this.getChapterDetailUseCase = getChapterDetailUseCase;
        this.readingHistoryDao = readingHistoryDao;
        this.comicCacheDao = comicCacheDao;
        this.apiService = apiService;
        
        // Nhận param truyền từ màn chi tiết
        this.initialChapterId = handle.get("chapterId");
        this.comicId = handle.get("comicId");

        // Load cached properties for historic saves
        disposables.add(comicCacheDao.getComicById(this.comicId)
                .subscribeOn(Schedulers.io())
                .subscribe(cached -> {
                    this.cachedComicTitle = cached.title != null ? cached.title : "Truyện Chữ";
                    this.cachedCoverUrl = cached.coverImage != null ? cached.coverImage : "";
                    this.cachedSlug = cached.slug != null ? cached.slug : "";
                    this.cachedAuthorName = cached.author != null ? cached.author : "Unknown";
                    this.cachedViewCount = cached.viewCount;
                }, throwable -> {
                    Log.e(TAG, "Not found in comicCache: " + throwable.getMessage());
                })
        );

        setupSaveDebouncer();
        loadSavedProgressAndStart();
    }

    private void setupSaveDebouncer() {
        disposables.add(
                saveProgressSubject
                        .throttleLatest(1000, TimeUnit.MILLISECONDS)
                        .observeOn(Schedulers.io())
                        .flatMapCompletable(entity -> readingHistoryDao.insertOrUpdate(entity))
                        .subscribe(
                                () -> Log.d(TAG, ">>> Lịch sử Novel lưu OK"),
                                throwable -> Log.e(TAG, ">>> Lỗi lưu Novel history: " + throwable.getMessage())
                        )
        );
    }

    private void loadSavedProgressAndStart() {
        disposables.add(
                readingHistoryDao.getHistoryForComic(comicId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(history -> {
                            savedChapterId = history.chapterId;
                            Log.d(TAG, "Khôi phục lịch sử chapter=" + savedChapterId + ", paragraph=" + history.pageIndex);
                            if (savedChapterId == initialChapterId && history.pageIndex > 0) {
                                pendingScrollToRelativeParagraph = history.pageIndex;
                            }
                            loadChapter(initialChapterId, true);
                        }, throwable -> {
                            Log.d(TAG, "Chưa từng đọc, bắt đầu từ: " + initialChapterId);
                            loadChapter(initialChapterId, true);
                        })
        );
    }

    private void loadChapter(int chapterId, boolean isInitial) {
        if (loadedChapterIds.contains(chapterId)) return;

        if (isInitial) {
            _isLoading.setValue(true);
        } else {
            isLoadingMore = true;
            // Append a loading item indicator at bottom
            List<TextNovelItem> loadItem = new ArrayList<>();
            loadItem.add(new TextNovelItem.LoadingItem());
            _appendItemsEvent.setValue(loadItem);
        }

        disposables.add(
                getChapterDetailUseCase.execute(chapterId)
                        .flatMap(chapter -> {
                            loadedChapterIds.add(chapter.getId());
                            pendingNextChapterId = chapter.getNextChapterId();
                            
                            if (isInitial) {
                                activePrevChapterId = chapter.getPrevChapterId();
                                _currentChapterEvent.postValue(chapter);
                            }
                            
                            // Ưu tiên load từ thuộc tính content của RDS
                            if (chapter.getContent() != null && !chapter.getContent().isEmpty()) {
                                return parseContent(chapter);
                            }
                            
                            // Giả định: nếu BE trả về rỗng content và rỗng ảnh thì có nghĩa là chương báo lỗi hoặc VIP
                            if (chapter.getImages() == null || chapter.getImages().isEmpty()) {
                                // Xử lý khóa Paywall hiển thị item đặc biệt
                                return Single.just(createPaywallItem(chapter));
                            }
                            
                            // Có URL S3. Ta proceed tải file JSON (Dự phòng ngập ngừng)
                            String jsonUrl = chapter.getImages().get(0);
                            return fetchAndParseJson(jsonUrl, chapter);
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(items -> {
                            isLoadingMore = false;
                            if (isInitial) {
                                _isLoading.setValue(false);
                            }
                            _appendItemsEvent.setValue(items);
                        }, throwable -> {
                            isLoadingMore = false;
                            if (isInitial) {
                                _isLoading.setValue(false);
                                _errorEvent.setValue("Lỗi tải S3 JSON hoặc API: " + throwable.getMessage());
                            } else {
                                // Nên remove cái Spinner ở đây nếu là Load More (Fragment/Adapter tự lo bằng method removeLoadingItem)
                                _errorEvent.setValue("Chưa tải được chương tiếp theo!");
                            }
                        })
        );
    }

    private List<TextNovelItem> createPaywallItem(ChapterEntity chapter) {
        List<TextNovelItem> items = new ArrayList<>();
        items.add(new TextNovelItem.DividerItem());
        items.add(new TextNovelItem.PaywallItem(chapter.getId()));
        return items;
    }

    private Single<List<TextNovelItem>> parseContent(ChapterEntity chapter) {
        return Single.fromCallable(() -> {
            chapterTitleCache.put(chapter.getId(), chapter.getTitle());
            
            List<TextNovelItem> items = new ArrayList<>();
            if (loadedChapterIds.size() > 1) {
                items.add(new TextNovelItem.DividerItem());
            }
            items.add(new TextNovelItem.TitleItem(chapter.getTitle()));
            
            String content = chapter.getContent();
            if (content != null && !content.isEmpty()) {
                String[] paragraphs = content.split("\\n+");
                int validIndex = 0;
                for (String p : paragraphs) {
                    String pTrimmed = p.trim();
                    if (!pTrimmed.isEmpty()) {
                        if (validIndex == 0) {
                            // Compare using normalized strings to catch variations (e.g. "Chương 1" vs "Chương 1:")
                            String normP = pTrimmed.toLowerCase().replaceAll("[^\\p{L}\\p{Nd}]+", "");
                            String normTitle = chapter.getTitle().toLowerCase().replaceAll("[^\\p{L}\\p{Nd}]+", "");
                            if (!normP.isEmpty() && !normTitle.isEmpty() && (normP.contains(normTitle) || normTitle.contains(normP))) {
                                continue; // Skip redundant title paragraph
                            }
                        }
                        items.add(new TextNovelItem.ParagraphItem(chapter.getId(), validIndex++, pTrimmed));
                    }
                }
            }
            return items;
        });
    }

    // Call http sync request to AWS S3 & return list items
    private Single<List<TextNovelItem>> fetchAndParseJson(String url, ChapterEntity chapter) {
        return Single.fromCallable(() -> {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    throw new RuntimeException("S3 Failed code: " + response.code());
                }
                String jsonStr = response.body().string();
                NovelChapterData data = gson.fromJson(jsonStr, NovelChapterData.class);
                
                List<TextNovelItem> items = new ArrayList<>();
                // Nếu có chapter trc đó, add khoảng trống divider
                if (loadedChapterIds.size() > 1) {
                    items.add(new TextNovelItem.DividerItem());
                }
                
                // Tiêu đề
                String fetchedTitle = data.getChapterTitle() != null ? data.getChapterTitle() : chapter.getTitle();
                chapterTitleCache.put(chapter.getId(), fetchedTitle);
                items.add(new TextNovelItem.TitleItem(fetchedTitle));
                
                // Danh sách paragraphs
                List<String> paragraphs = data.getContent();
                if (paragraphs != null) {
                    int validIndex = 0;
                    String mainTitle = data.getChapterTitle() != null ? data.getChapterTitle() : chapter.getTitle();
                    for (String p : paragraphs) {
                        String pTrimmed = p.trim();
                        if (!pTrimmed.isEmpty()) {
                            if (validIndex == 0) {
                                String normP = pTrimmed.toLowerCase().replaceAll("[^\\p{L}\\p{Nd}]+", "");
                                String normTitle = mainTitle.toLowerCase().replaceAll("[^\\p{L}\\p{Nd}]+", "");
                                if (!normP.isEmpty() && !normTitle.isEmpty() && (normP.contains(normTitle) || normTitle.contains(normP))) {
                                    continue;
                                }
                            }
                            items.add(new TextNovelItem.ParagraphItem(chapter.getId(), validIndex++, pTrimmed));
                        }
                    }
                }
                return items;
            }
        });
    }

    public void saveReadingProgress(int chapterId, int paragraphIndex) {
        if (comicId <= 0 || chapterId <= 0 || paragraphIndex < 0) return;

        ReadingHistoryEntity entity = new ReadingHistoryEntity();
        entity.comicId = this.comicId;
        entity.chapterId = chapterId;
        entity.pageIndex = paragraphIndex;
        entity.readAt = System.currentTimeMillis();
        
        entity.comicTitle = this.cachedComicTitle;
        entity.coverUrl = this.cachedCoverUrl;
        entity.slug = this.cachedSlug;
        entity.comicType = "NOVEL";
        entity.authorName = this.cachedAuthorName;
        entity.viewCount = this.cachedViewCount;
        
        String chTitle = chapterTitleCache.get(chapterId);
        entity.chapterTitle = chTitle != null ? chTitle : "Chương " + chapterId;

        // Percent tính phức tạp hơn với List flat, ta set tạm 0 hoặc số trang ước lượng
        entity.percent = 0; 
        
        saveProgressSubject.onNext(entity);
    }

    public void loadNextChapterIfNeeded() {
        if (isLoadingMore || pendingNextChapterId == null || pendingNextChapterId <= 0) return;
        loadChapter(pendingNextChapterId, false);
    }

    public void loadSpecificChapter(int chapterId) {
        if (chapterId <= 0) return;
        loadedChapterIds.clear();
        pendingNextChapterId = null;
        activePrevChapterId = null;
        _clearItemsEvent.setValue(true);
        // Ngừng hiển thị spinner cục bộ vì đây là load mới hoàn toàn
        loadChapter(chapterId, true);
    }

    public void fetchChapterList() {
        if (isChapterListLoaded) return;
        
        disposables.add(
            apiService.getChaptersById(comicId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response != null && response.getData() != null) {
                        List<ChapterItem> list = new ArrayList<>();
                        for (com.example.comicversev1.data.model.ChapterItemDTO dto : response.getData()) {
                            list.add(new ChapterItem(dto.getId(), dto.getTitle(), dto.getAccessType()));
                        }
                        _chapterListEvent.setValue(list);
                        isChapterListLoaded = true;
                    }
                }, throwable -> {
                    _errorEvent.setValue("Lỗi tải danh sách chương: " + throwable.getMessage());
                })
        );
    }

    public int getComicId() {
        return comicId;
    }

    public Integer getActivePrevChapterId() {
        return activePrevChapterId;
    }

    public Integer getPendingNextChapterId() {
        return pendingNextChapterId;
    }

    public String getChapterTitleCache(int chapterId) {
        return chapterTitleCache.get(chapterId);
    }

    public void trackChapterView(int chapterId) {
        if (comicId <= 0 || chapterId <= 0) return;
        disposables.add(
                apiService.trackView(new ViewTrackingRequest(comicId, chapterId))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> Log.d(TAG, "Tính View novel thành công cho chapter: " + chapterId),
                                throwable -> Log.e(TAG, "Lỗi fetch View Novel: " + throwable.getMessage())
                        )
        );
    }

    public Integer consumePendingScrollPosition() {
        Integer val = pendingScrollToRelativeParagraph;
        pendingScrollToRelativeParagraph = null;
        return val;
    }

    public void reportChapter(int chapterId, com.example.comicversev1.data.model.ChapterReportRequest request) {
        disposables.add(
                apiService.reportChapter(chapterId, request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    _reportSuccessEvent.setValue(true);
                                },
                                throwable -> {
                                    Log.e(TAG, "Lỗi report chapter: " + throwable.getMessage());
                                    _errorEvent.setValue("Gửi báo cáo thất bại");
                                }
                        )
        );
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
