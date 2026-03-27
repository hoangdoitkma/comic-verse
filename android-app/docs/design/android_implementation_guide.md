# TÀI LIỆU THIẾT KẾ IMPLEMENTATION ỨNG DỤNG ANDROID: COMICVERSE (JAVA)

*Đây là tài liệu phần tiếp nối ("Bản vẽ thi công") của bản Tổng quan Kiến Trúc, cung cấp các đoạn code mẫu và cấu trúc chi tiết dành riêng cho việc lập trình module.*

---

## 1. CHI TIẾT API CLIENT (RETROFIT INTERFACE TƯƠNG TÁC)

Dựa trên tài liệu `api.md`, các Endpoint cần được map vào Java object thông qua Model DTO (Data Transfer Objects).

### 1.1 Base Response & Models
```java
// Base Response bao bọc mọi response từ API
public class BaseResponse<T> {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;
    
    @SerializedName("error_code")
    private String errorCode;

    // Getters and Setters...
}

// Đối tượng Truyện
public class ComicDTO {
    @SerializedName("id")
    private int id;

    @SerializedName("slug")
    private String slug;

    @SerializedName("title")
    private String title;
    
    @SerializedName("cover_image")
    private String coverImage;
    
    // Các fields khác
}
```

### 1.2 Retrofit Interface (Ví dụ Component Comic)
```java
public interface ApiService {
    
    // Auth Module
    @POST("/auth/login")
    Single<BaseResponse<LoginResponse>> login(@Body LoginRequest request);

    @POST("/auth/refresh")
    Call<BaseResponse<TokenResponse>> refreshToken(@Body RefreshRequest request); // Call đồng bộ cho Interceptor

    // Content Module
    @GET("/comics")
    Single<BaseResponse<List<ComicDTO>>> getComics(
        @Query("page") int page,
        @Query("limit") int limit
    );

    @GET("/comics/{slug}")
    Single<BaseResponse<ComicDTO>> getComicDetail(@Path("slug") String slug);

    @GET("/chapters/{id}")
    Single<BaseResponse<ChapterDetailDTO>> getChapterContent(@Path("id") int chapterId);
    
    // User Action
    @POST("/reading-history")
    Completable updateReadingHistory(@Body ReadingHistoryRequest request);
}
```

---

## 2. THIẾT KẾ DATABASE NỘI BỘ (ROOM DATABASE)

Room xử lý bộ đệm Offline cho người dùng, kết hợp RxJava.

### 2.1 Entity: Lịch sử đọc (`reading_history`)
Để có thể "Resume" sau khi thoát ứng dụng.

```java
@Entity(tableName = "reading_history",
        indices = {@Index(value = "comic_id", unique = true)}) // Mỗi truyện 1 lịch sử gần nhất
public class ReadingHistoryEntity {
    
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "comic_id")
    private int comicId;

    @ColumnInfo(name = "chapter_id")
    private int chapterId;

    @ColumnInfo(name = "page_index")
    private int pageIndex; // Vị trí ảnh đang đọc dở

    @ColumnInfo(name = "read_at")
    private long readAt; // Timestamp

    // Getters, Setters, Constructor...
}
```

### 2.2 Entity: Danh sách truyện offline (`comics_cache`)
Cần cache để khi không có mạng, người dùng vẫn thấy danh sách màn hình Home.

### 2.3 DAO (Data Access Object)
```java
@Dao
public interface ReadingHistoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertOrUpdate(ReadingHistoryEntity history);

    @Query("SELECT * FROM reading_history ORDER BY read_at DESC LIMIT 20")
    Flowable<List<ReadingHistoryEntity>> getRecentHistory(); // Lắng nghe realtime
    
    @Query("SELECT * FROM reading_history WHERE comic_id = :comicId LIMIT 1")
    Single<ReadingHistoryEntity> getHistoryForComic(int comicId);
}
```

---

## 3. CẤU TRÚC ĐIỀU HƯỚNG CỤ THỂ (NAVIGATION FLOW)

Sử dụng Jetpack Navigation Component với Graph Navigation (file `nav_graph.xml`).

| Nguồn Mở | Màn Hình Đích | Tham Số (SafeArgs) Yêu Cầu |
| :--- | :--- | :--- |
| `HomeFragment` | `ComicDetailFragment` | `slug` (String) |
| `ComicDetailFragment` | `ReaderFragment` | `chapterId` (Int), `comicId` (Int) |
| `ComicDetailFragment` | `BottomSheetComments` | `chapterId` (Int) (Nếu mở cmnt của chuơng) |
| Bất kỳ màn nào | `VipSubscriptionActivity` | Call Intent, ko cần argument truyền qua, fetch status từ token |
| Thanh Toán VNPay | Bắt scheme `comicverse://vnpay-callback` | DeepLink bắt từ System Intent -> gọi màn KQ Giao Dịch |

---

## 4. THIẾT KẾ CÂY LAYOUT XML (VIEW HIERARCHY)

Thiết kế cấu trúc view cốt lõi cho một số màn hình quan trọng nhất.

### 4.1. Màn hình Đọc Comic (`fragment_reader.xml`)
Cần hỗ tr��� "Immersive Mode" (Toàn màn hình, che mất Status Bar). Mặc định Ẩn thanh công cụ.

```xml
<androidx.constraintlayout.widget.ConstraintLayout>
    
    <!-- Lớp Dưới Cùng: Chứa toàn bộ hình ảnh truyện -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvChapterPages"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" />

    <!-- Lớp Ở Giữa: Nút tiến tới chương mới / Lùi chương cũ (Ẩn/Hiện) -->
    
    <!-- Lớp Trên Cùng (Overlay): Thanh trạng thái trên & dưới. Mặc định: visibility="gone" -->
    <com.google.android.material.appbar.AppBarLayout
        android:id="@+id/topToolbar"
        ...>
        <!-- Có nút Back, Tên Chapter -->
    </com.google.android.material.appbar.AppBarLayout>

    <LinearLayout
        android:id="@+id/bottomMenu"
        android:layout_gravity="bottom"
        ...>
        <!-- SeekBar Tua trang, Nút Bình luận, Nút Cài đặt (Night mode) -->
    </LinearLayout>

</androidx.constraintlayout.widget.ConstraintLayout>
```

### 4.2. Màn hình Trang Chủ (`fragment_home.xml`)
Sử dụng Nested RecyclerView:
```xml
<androidx.core.widget.NestedScrollView>
   <LinearLayout android:orientation="vertical">
      
       <!-- Slider Truyện Nổi Bật -->
       <androidx.viewpager2.widget.ViewPager2 
            android:id="@+id/vpTrending" />
            
       <TextView android:text="Mới Cập Nhật" />
       
       <!-- Danh sách truyện mới (Grid) -->
       <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/rvLatest"
            app:layoutManager="androidx.recyclerview.widget.GridLayoutManager"
            app:spanCount="3" />
            
   </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

---

## 5. XỬ LÝ LOGIC CỤ THỂ (USE CASES & VIEWMODEL FLOW)

Luồng lấy dữ liệu chuẩn (Ví dụ: `HomeViewModel` lấy danh sách truyện sử dụng RxJava).

### Bước 1: UseCase (Nằm ở Domain Layer)
Chỉ định nhiệm vụ xử lý nghiệp vụ, chuyển đổi kiểu.
```java
public class GetLatestComicsUseCase {
    private final ComicRepository repository;

    @Inject
    public GetLatestComicsUseCase(ComicRepository repository) {
        this.repository = repository;
    }

    // Trả về Single List của Entity, không phải DTO
    public Single<List<ComicEntity>> execute(int page) {
        return repository.getLatestComics(page);
    }
}
```

### Bước 2: ViewModel (Nằm ở Presentation Layer)
Tiếp nhận và đẩy data ra `LiveData` cho Fragment vẽ.
```java
@HiltViewModel
public class HomeViewModel extends ViewModel {
    
    private final GetLatestComicsUseCase getLatestComicsUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();

    // LiveData công khai chỉ đọc cho View
    private final MutableLiveData<List<ComicEntity>> _latestComics = new MutableLiveData<>();
    public LiveData<List<ComicEntity>> getLatestComics() { return _latestComics; }

    // LiveData Trạng thái Loading / Error
    private final MutableLiveData<String> _errorInfo = new MutableLiveData<>();
    public LiveData<String> getErrorInfo() { return _errorInfo; }

    @Inject
    public HomeViewModel(GetLatestComicsUseCase getLatestComicsUseCase) {
        this.getLatestComicsUseCase = getLatestComicsUseCase;
    }

    public void fetchLatestComics() {
        disposables.add(
            getLatestComicsUseCase.execute(1)
                .subscribeOn(Schedulers.io()) // Chạy trên luồng background (Network)
                .observeOn(AndroidSchedulers.mainThread()) // Cập nhật UI trên Main Thread
                .subscribe(
                    comics -> _latestComics.setValue(comics), // OnSuccess
                    error -> _errorInfo.setValue(error.getMessage()) // OnError (Exception)
                )
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear(); // Hủy mọi request nếu Fragment chết để tránh Leak Memory
    }
}
```

### Bước 3: Fragment (Presentation Layer)
Binding dữ liệu lên Layout.
```java
@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private FragmentHomeBinding binding;
    private LatestComicAdapter adapter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Khởi tạo Adapter cho RecyclerView
        adapter = new LatestComicAdapter();
        binding.rvLatest.setAdapter(adapter);

        // Lắng nghe (Observe) LiveData
        viewModel.getLatestComics().observe(getViewLifecycleOwner(), comics -> {
            // Dữ liệu API đã về thành công, cập nhật Adapter
            adapter.submitList(comics); 
        });

        viewModel.getErrorInfo().observe(getViewLifecycleOwner(), errorMessage -> {
             Toast.makeText(requireContext(), "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
        });

        // Kích hoạt lấy dữ liệu
        viewModel.fetchLatestComics();
    }
}
```

