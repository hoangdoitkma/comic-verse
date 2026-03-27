# TÀI LIỆU THIẾT KẾ ỨNG DỤNG ANDROID: COMICVERSE (PHIÊN BẢN JAVA)

## 1. TỔNG QUAN ỨNG DỤNG
**ComicVerse** là một ứng dụng di động nền tảng Android hỗ trợ đọc truyện tranh (Comic) và truyện chữ (Novel). Ứng dụng cung cấp trải nghiệm đọc mượt mà, hỗ trợ tương tác cộng đồng, gợi ý nội dung bằng AI và hệ thống đọc truyện trả phí (VIP).
- **Nền tảng mục tiêu:** Android (API 24 trở lên)
- **Ngôn ngữ phát triển:** Java (Java 8/11/17)
- **Mô hình kiến trúc:** Clean Architecture kết hợp MVVM (Model-View-ViewModel).

---

## 2. CÔNG NGHỆ VÀ THƯ VIỆN SỬ DỤNG (TECH STACK)
Thay vì các công nghệ mới chủ yếu dành cho Kotlin (như Compose, Coroutines), hệ sinh thái Java sẽ tận dụng các thư viện trưởng thành và ổn định nhất:

- **UI Toolkit:** XML Layouts truyền thống. Khuyến nghị sử dụng **ViewBinding** (hoặc DataBinding) để tương tác với UI một cách an toàn (null-safe, type-safe).
- **Architecture Components:** ViewModel, LiveData (để quản lý trạng thái và chu kỳ sống), Navigation Component (quản lý điều hướng qua các Fragments).
- **Dependency Injection:** Dagger Hilt (hỗ trợ Java rất tốt, giảm boilerplate code của Dagger 2).
- **Network / API:** Retrofit 2, OkHttp 3, Gson (để parsing JSON).
- **Asynchronous Programming & Reactive:** **RxJava 3** và **RxAndroid** (Thay thế cho Coroutines. Xử lý đa luồng, gọi API bất đồng bộ và xử lý luồng dữ liệu mượt mà).
- **Local Storage / Caching:** 
  - Room Database (Kết hợp trả về `Single`, `Flowable` hoặc `LiveData` của RxJava): Lưu lại lịch sử đọc (Reading history), offline bookmarks, và cache nội dung.
  - SharedPreferences (hoặc EncryptedSharedPreferences): Lưu cấu hình ứng dụng (Dark/light mode) và Auth Token (JWT).
- **Image Loading:** **Glide** hoặc **Picasso** (Glide tối ưu bộ nhớ hơn cho danh sách ảnh cực dài như khi đọc truyện tranh).
- **Phân trang (Pagination):** Jetpack Paging 3 (kết hợp thư viện `RxPaging` để tải danh sách truyện, bình luận mượt mà).
- **Tích hợp bên thứ ba:** 
  - Firebase Cloud Messaging (FCM).
  - SDK VNPay (hoặc custom WebView): Cổng thanh toán.
  - Google / Facebook Login SDK.

---

## 3. KIẾN TRÚC ỨNG DỤNG (APP ARCHITECTURE)
Ứng dụng tuân thủ tiêu chuẩn **Clean Architecture** chia làm 3 layers chính, thiết kế theo hướng Reactive:

1. **Presentation Layer (UI):**
   - Chứa 1 Activity duy nhất (Single-Activity Architecture) và nhiều Fragments (`HomeFragment`, `ComicDetailFragment`...).
   - Quan sát (Observer) sự thay đổi dữ liệu từ ViewModel qua `LiveData`.
   - Bắt sự kiện người dùng (Click) và gọi các method của ViewModel.

2. **Domain Layer (Business Logic):**
   - Chứa các UseCases cụ thể (Dưới dạng các class Java tĩnh hoặc inject), trả về các kiểu dữ liệu của RxJava như `Single<T>` hoặc `Completable`. (Ví dụ: `Single<ComicDetail> getComicDetailUseCase(String slug)`).
   - Chứa Models (Entities) thuần Java (`POJO`).
   - Độc lập hoàn toàn với Android Framework API.

3. **Data Layer:**
   - Triển khai Remote Data Source: Retrofit Interface trả về `Single<Response<T>>`.
   - Triển khai Local Data Source: Room DAO.
   - Các Repository Classes quyết định việc map dữ liệu từ Network hoặc Local Database về cho Domain layer.

---

## 4. PHÂN TÍCH UI VÀ MODULE CHỨC NĂNG

### 4.1. Module Xác Thực (Auth Module)
- **Màn hình:** XML Fragments cho Login, Register.
- **Xử lý Logic:** 
  - Dùng Retrofit Interceptor (`Authenticator` của OkHttp) để tự động bắt lỗi `401 Unauthorized` hoặc `AUTH_002` (Token expired). Khi đó Interceptor tự động tạo request mới gọi `POST /auth/refresh`, lấy Token mới cập nhật vào `SharedPreferences` và thử lại request cũ một cách trong suốt (người dùng không hề hay biết).

### 4.2. Module Khám phá (Discover Module)
- **Màn hình:** `HomeFragment` (Sử dụng `RecyclerView` lồng nhau - Horizontal & Vertical).
- **Luồng chức năng:**
  - ViewPager2 hoặc RecyclerView nằm ngang cho danh sách `Trending`.
  - Infinite scrolling dọc sử dụng Paging 3 `PagingDataAdapter`.
  - Ô tìm kiếm: Dùng toán tử `debounce()` của **RxJava** (~500ms) áp dụng lên sự kiện TextChanged của EditText để tránh spam request API.

### 4.3. Module Chi Tiết Truyện (Comic Detail Module)
- **Màn hình:** `ComicDetailFragment`, Danh sách Chapter lồng trong `BottomSheetDialogFragment` để thiết kế giao diện nhận xét.
- **Luồng chức năng & Logic:**
  - Gọi `/comics/{id}/ai-summary` để lấy tóm tắt AI.
  - Danh sách Chapter có cờ: Bất c�� Chapter nào có `access_type = VIP`, Adapter sẽ vẽ icon Khóa.
  - Xử lý Click Chapter: Trước khi mở `ReaderFragment`, thực hiện check: Nếu `currentUser.getSubscriptionStatus() != ACTIVE`, hiển thị Dialog (`AlertDialog` hoặc `BottomSheet`) khuyên nâng cấp VIP.

### 4.4. Module Trình Đọc Truyện (Comic/Novel Viewer Module)
- **Màn hình:** 
  - Đọc Comic: Sử dụng `RecyclerView` thiết kế layout dọc/ngang với `Glide` load từng ảnh.
  - Đọc Novel: Sử dụng `ScrollView` và `TextView` tùy chỉnh HTML/Font.
- **Xử lý hiệu năng (RẤT QUAN TRỌNG):**
  - **Tối ưu Glide:** Cấu hình `DiskCacheStrategy.ALL` để lưu trữ ảnh tải về vào ổ cứng. Giới hạn `memory cache size` để tránh lỗi Out Of Memory (OOM) trong Java. Dùng `preload()` của Glide để fetch trước 1-2 trang ảnh tiếp theo.
  - **Lưu tiến trình (Resume reading):** override `onPause()` / `onStop()` của Fragment hoặc lắng nghe vòng đời qua `LifecycleObserver` để thực hiện gọi API `POST /reading-history` lên server.

### 4.5. Module VIP & Thanh toán (Monetization Module)
- **Xử lý Logic Thanh Toán VNPay:**
  1. Yêu cầu tạo thanh toán API `/payments/create`. Lấy về URL.
  2. Mở trình duyệt Web nằm trong App thông qua `CustomTabsIntent` (nhanh hơn WebView) để điều hướng tới VNPay.
  3. Khi xử lý thành công, VNPay redirect về một scheme định sẵn (Deep Link). Cấu hình `intent-filter` trong `AndroidManifest.xml` để đón Deep Link này \u0024\u0024 Gửi request check lại status và thông báo giao dịch thành công.

### 4.6. Module Hồ sơ & Thông báo (Profile & Notification)
- **Hệ thống thông báo:** Kế thừa `FirebaseMessagingService` (dùng thư viện FCM Firebase Java SDK), bắt tín hiệu và đẩy lên khay thông báo hệ thống qua `NotificationCompat.Builder`.

---

## 5. QUY TẮC NGHIỆP VỤ & BẢO MẬT (BUSINESS RULES)
- **Bảo mật mã thông báo:** Gói `SharedPreferences` bằng `EncryptedSharedPreferences` (thư viện AndroidX Security Crypto) để mã hóa file config XML trên thiết bị chứa Token, phòng chống máy giả lập/root đánh cắp JWT.
- **Age Restriction:** Dựa vào `age_rating` ở response truyện và ngày sinh `birthDate` lưu trong Profile user hiện tại để filter content.

## 6. SƠ ĐỒ CẤU TRÚC PACKAGE (JAVA)
```
com.comicverse.app
├── data                    
│   ├── api                 # Chứa các interface (Retrofit)
│   ├── local               # RoomDatabase, Daos, SharedPreferences util
│   ├── model               # DTOs cho Network
│   └── repository          # Implement các repository bằng RxJava
├── domain                  
│   ├── entity              # POJOs thuần túy
│   ├── repository          # Các interface Repository
│   └── usecase             # Business Logic (VD: GetComicsUseCase.java)
├── di                      # Hilt Modules (NetworkModule.java, DatabaseModule.java)
├── presentation            # UI Layer
│   ├── base                # BaseActivity, BaseFragment chứa boilerplate code
│   ├── ui                  
│   │   ├── auth            # LoginFragment, RegisterFragment
│   │   ├── home            # HomeFragment, HomeAdapter
│   │   ├── reader          # ReaderFragment (RecyclerView xử lý ảnh)
│   │   └── ...
│   ├── viewmodel           # Chứa các AndroidViewModel/ViewModel (LiveData)
│   └── common              # Custom Views (nếu cần)
└── utils                   # Helper classes (RxUtils, Constants.java)
```

