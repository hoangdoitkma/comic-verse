# BỘ QUY TẮC VÀ NGỮ CẢNH DỰ ÁN DÀNH CHO AI AGENT (AI CONTEXT)

## 1. TỔNG QUAN DỰ ÁN (PROJECT CONTEXT)
- **Tên dự án:** ComicVerse (Ứng dụng đọc truyện tranh trực tuyến có tích hợp thanh toán).
- **Mô hình:** Monorepo chứa 3 module chính:
  1. `/android-app`: Ứng dụng Mobile cho độc giả.
  2. `/backend-springboot`: API Server cho toàn bộ hệ thống.
  3. `/web-admin`: Giao diện quản trị cho Admin/Uploader.

## 2. KIẾN TRÚC & CÔNG NGHỆ CHÍNH (TECH STACK)
- **Android App (`/android-app`):**
  - Ngôn ngữ: Java (chính), cấu hình JVM Target 11. Hỗ trợ Kotlin Coroutines ở mức cơ bản.
  - Kiến trúc: MVVM (Model-View-ViewModel).
  - Dependency Injection: Hilt.
  - Local Storage: Room Database (tích hợp RxJava3).
  - Networking: Retrofit2, OkHttp3 (có Logging Interceptor).
  - Bất đồng bộ: RxJava3 (chính), Kotlin Coroutines (cho Timer/Task nhẹ).
  - UI/Image Loading: Glide, ViewBinding, SwipeRefreshLayout, Lottie (Animations), Paging3.
  - Cấu hình mạng: BASE_URL trỏ về server AWS (`http://3.1.207.156:8080/api/`).
- **Backend (`/backend-springboot`):**
  - Nền tảng: Java 17, Spring Boot 3.2.x.
  - Data Access: Spring Data JPA.
  - Database: MySQL (chính) & Redis (Caching/Session).
  - Bảo mật: Spring Security, JWT (io.jsonwebtoken bản 0.11.5).
  - Lưu trữ Object (Cloud): AWS SDK for S3 (Lưu trữ ảnh, file tĩnh).
  - Utilities: Lombok, Validation.
- **Web Admin (`/web-admin`):**
  - Frontend Framework: React 19, Vite.
  - Styling: Tailwind CSS v4.
  - Routing & State: React Router DOM.
  - HTTP Client: Axios.
  - UI Components: Recharts (biểu đồ), Lucide React (Icons).

## 3. LUỒNG DỮ LIỆU CỐT LÕI (CORE DATA FLOWS)
- **Quản lý file tĩnh (Images):** Mọi ảnh truyện được upload lên AWS S3. Backend không trả về file vật lý, chỉ lưu và trả về **URL tuyệt đối** của S3 qua JSON DTO.
- **Cơ chế đọc truyện của Guest (Guest Mode):** App cho phép người dùng chưa đăng nhập đọc truyện. Tiến độ đọc (Reading History) của Guest BẮT BUỘC lưu ở Local bằng **Room Database**. Không gọi API lưu lịch sử lên Backend nếu không có JWT Token. Khi user đăng nhập, có thể đồng bộ (Sync) lịch sử từ local lên server.
- **Thiết kế API (RESTful):** API cho Client (App) và API cho Admin (Web) phải tách biệt hoàn toàn. (VD: `/api/v1/public/...` hoặc `/api/v1/user/...` cho App và `/api/v1/admin/...` cho Web). Mọi response phải bọc trong BaseResponse `{ status, message, data, timestamp }`.

## 4. QUY TẮC CODE BẮT BUỘC (STRICT CODING RULES)
Khi được yêu cầu viết code mới hoặc mở rộng tính năng, BẮT BUỘC phải tuân thủ:
1. **Không Null (Null Safety):** Trên Android, mọi trường dữ liệu DTO có khả năng null phải được đánh dấu `@Nullable` (Java) cẩn thận. Backend phải có cơ chế fallback giá trị (VD: viewCount mặc định là 0, không được trả về null).
2. **Luôn dùng DTO (Data Transfer Object):** Backend tuyệt đối không trả về nguyên bản Entity của DB ra API. Phải map qua DTO để bảo vệ thông tin hệ thống và tối ưu băng thông.
3. **Bảo toàn code cũ & Config:** Không tự ý xóa hoặc sửa các cấu hình hạ tầng hiện có (`build.gradle`, `pom.xml`, `package.json`, `application.properties`) nếu không được người dùng yêu cầu rõ ràng. Nếu thêm thư viện, phải check version tương thích.
4. **Viết Log & Handle Exception:** Mọi luồng call API trên Android phải xử lý đủ 3 trạng thái UI: Loading, Success, Error (có thông báo lỗi thân thiện cho user).

## 5. LỘ TRÌNH MỞ RỘNG (ROADMAP)
Các tính năng đang và sẽ ưu tiên triển khai:
- [ ] Tích hợp Room Database lưu lịch sử đọc cho Guest trên Android.
- [ ] Tích hợp API phân trang (Paging3) cho danh sách truyện trên App.
- [ ] Tích hợp cổng thanh toán (Payment Gateway) để mua chapter khóa.
- [ ] Hoàn thiện Dashboard thống kê trên Web Admin với Recharts.