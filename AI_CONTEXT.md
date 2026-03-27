# BỘ QUY TẮC VÀ NGỮ CẢNH DỰ ÁN DÀNH CHO AI AGENT (AI CONTEXT)

## 1. TỔNG QUAN DỰ ÁN (PROJECT CONTEXT)
- **Tên dự án:** ComicVerse (Ứng dụng đọc truyện tranh trực tuyến có tích hợp thanh toán).
- **Mô hình:** Monorepo chứa 3 module chính:
  1. `/android-app`: Ứng dụng Mobile cho độc giả.
  2. `/backend-springboot`: API Server cho toàn bộ hệ thống.
  3. `/web-admin`: Giao diện quản trị cho Admin/Uploader.

## 2. KIẾN TRÚC & CÔNG NGHỆ CHÍNH (TECH STACK)
- **Android App:** Java/Kotlin, MVVM Architecture, Room Database (Local Storage), Retrofit + OkHttp, RxJava3 / Coroutines, Glide/Coil (Load ảnh).
- **Backend:** Java 17, Spring Boot 3.x, Spring Data JPA, Spring Security (JWT).
- **Database & Cloud:** AWS RDS (MySQL 8), AWS S3 (Lưu trữ ảnh truyện).
- **Web Admin:** Node.js, React/Vue (Frontend quản trị).

## 3. LUỒNG DỮ LIỆU CỐT LÕI (CORE DATA FLOWS)
- **Quản lý file tĩnh (Images):** Mọi ảnh truyện được upload lên AWS S3. Backend không trả về file vật lý, chỉ lưu và trả về **URL tuyệt đối** của S3 qua JSON DTO.
- **Cơ chế đọc truyện của Guest (Guest Mode):** App cho phép người dùng chưa đăng nhập đọc truyện. Tiến độ đọc (Reading History) của Guest BẮT BUỘC lưu ở Local bằng **Room Database**. Không gọi API lưu lịch sử lên Backend nếu không có JWT Token.
- **Thiết kế API (RESTful):** API cho Client (App) và API cho Admin (Web) phải tách biệt hoàn toàn. (VD: `/api/v1/public/comics` cho App và `/api/v1/admin/comics` cho Web). Mọi response phải bọc trong BaseResponse `{ status, message, data, timestamp }`.

## 4. QUY TẮC CODE BẮT BUỘC (STRICT CODING RULES)
Khi được yêu cầu viết code mới hoặc mở rộng tính năng, bạn BẮT BUỘC phải tuân thủ:
1. **Không Null (Null Safety):** Trên Android, mọi trường dữ liệu DTO có khả năng null phải được đánh dấu `?` (Kotlin) hoặc `@Nullable`. Backend phải có cơ chế fallback giá trị (VD: viewCount mặc định là 0, không được trả về null).
2. **Luôn dùng DTO (Data Transfer Object):** Backend tuyệt đối không trả về nguyên bản Entity của DB ra API. Phải map qua DTO để giấu thông tin nhạy cảm.
3. **Bảo toàn code cũ:** Không tự ý xóa hoặc sửa các cấu hình hạ tầng hiện có (`build.gradle`, `pom.xml`, `application.properties`) nếu không được người dùng yêu cầu rõ ràng.
4. **Viết Log & Handle Exception:** Mọi luồng call API trên Android phải xử lý đủ 3 trạng thái UI: Loading, Success, Error.

## 5. LỘ TRÌNH MỞ RỘNG (ROADMAP)
Các tính năng ưu tiên tiếp theo cần AI hỗ trợ triển khai:
- [ ] Tích hợp Room Database lưu lịch sử đọc cho Guest trên Android.
- [ ] Tích hợp API phân trang (Pagination) cho danh sách truyện.
- [ ] Tích hợp cổng thanh toán (Payment Gateway) để mua chapter khóa.