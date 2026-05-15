# BÁO CÁO ĐỒ ÁN TỐT NGHIỆP
**Đề tài: Xây dựng Hệ thống Quản lý và Đọc truyện tranh trực tuyến (ComicVerse)**

---

## LỜI MỞ ĐẦU
Trong thời đại công nghệ số phát triển mạnh mẽ, nhu cầu giải trí và tiếp cận thông tin qua các nền tảng trực tuyến ngày càng tăng cao. Truyện tranh, một hình thức giải trí phổ biến, cũng không nằm ngoài xu hướng này. Việc xây dựng một nền tảng đọc truyện tranh trực tuyến không chỉ đáp ứng nhu cầu của độc giả mà còn tạo ra một môi trường quản lý nội dung hiệu quả cho các nhà xuất bản và nhóm dịch thuật. 

Dự án **ComicVerse** được phát triển nhằm mục đích cung cấp một giải pháp toàn diện bao gồm: một ứng dụng di động dành cho độc giả với trải nghiệm mượt mà, và một trang web quản trị giúp việc quản lý, đăng tải nội dung trở nên dễ dàng, hiệu quả.

---

## CHƯƠNG 1: GIỚI THIỆU TỔNG QUAN

### 1.1. Mục tiêu dự án
- **Đối với người dùng (Độc giả):** Cung cấp ứng dụng di động (Android) thân thiện, dễ sử dụng, tốc độ tải trang nhanh chóng, hỗ trợ đọc trực tuyến và lưu trữ tiến độ đọc (cả khi có tài khoản và không có tài khoản - Guest mode).
- **Đối với Quản trị viên (Admin/Uploader):** Cung cấp hệ thống quản trị Web (Web Admin) giúp dễ dàng quản lý thông tin truyện, quản lý người dùng, đăng tải các chương truyện mới và theo dõi hoạt động của hệ thống.
- **Về mặt kỹ thuật:** Xây dựng một hệ thống phân tán theo mô hình Client-Server, có tính mở rộng cao, chịu tải tốt, áp dụng các công nghệ điện toán đám mây hiện đại (AWS S3, AWS RDS).

### 1.2. Đối tượng sử dụng
- **Độc giả:** Những người có nhu cầu tìm kiếm và đọc truyện tranh trực tuyến trên thiết bị di động.
- **Uploader/Admin:** Những cá nhân hoặc nhóm chuyên dịch thuật, đăng tải nội dung và những người quản trị toàn bộ hệ thống.

---

## CHƯƠNG 2: KIẾN TRÚC VÀ CÔNG NGHỆ SỬ DỤNG

### 2.1. Kiến trúc hệ thống
Hệ thống được thiết kế theo mô hình phân tán Client-Server, bao gồm 3 phân hệ chính:
1. **Android App (`/android-app`)**: Ứng dụng client dành cho người đọc. Giao tiếp với backend thông qua RESTful API.
2. **Web Admin (`/web-admin`)**: Giao diện quản trị dành cho Admin và Uploader.
3. **Backend Service (`/backend-springboot`)**: Server ứng dụng tập trung xử lý logic nghiệp vụ, xác thực người dùng và tương tác với cơ sở dữ liệu.

```mermaid
flowchart TD
    subgraph Clients["Tầng Client/Frontend"]
        App["📱 Android App\n(Kotlin/Java, Retrofit, Room)"]
        Web["💻 Web Admin\n(React/Vue, Axios)"]
    end

    subgraph Compute["Cloud Server (AWS EC2)"]
        Backend["🚀 Backend Spring Boot\n(RESTful API, JWT)"]
    end

    subgraph Storage["Cloud Storage & Database (AWS)"]
        RDS[("🗄️ AWS RDS\n(MySQL 8)")]
        S3[("☁️ AWS S3\n(Object Storage)") ]
    end

    App -- "HTTP/REST API (JSON)" --> Backend
    Web -- "HTTP/REST API (JSON)" --> Backend

    Backend -- "JPA/Hibernate" --> RDS
    Backend -- "AWS SDK (Upload File)" --> S3
    
    S3 -. "Trả Image URLs trực tiếp" .-> App
    S3 -. "Preview Thumbnail" .-> Web
```

### 2.2. Công nghệ sử dụng
- **Phía Backend:**
  - **Framework:** Java Spring Boot 3.x.
  - **Bảo mật:** Spring Security, JSON Web Token (JWT) để xác thực và phân quyền (Authentication/Authorization).
  - **ORM:** Spring Data JPA / Hibernate để tương tác cơ sở dữ liệu.
- **Phía Client (Android):**
  - **Ngôn ngữ & Kiến trúc:** Java/Kotlin, kiến trúc MVVM (Model-View-ViewModel).
  - **Network:** Retrofit + OkHttp để gọi API bất đồng bộ (sử dụng RxJava/Coroutines).
  - **Lưu trữ cục bộ:** Room Database (Lưu tiến độ đọc ẩn danh, đồng bộ sau khi đăng nhập).
  - **Render ảnh:** Glide (Hiển thị ảnh mượt mà, tối ưu bộ nhớ).
- **Phía Web Admin:**
  - **Framework:** Node.js, React.
  - **Network:** Axios với Interceptor để quản lý JWT Token tự động.
- **Cơ sở hạ tầng & Lưu trữ (AWS Cloud):**
  - **Compute:** AWS EC2 để host backend.
  - **Database:** AWS RDS (MySQL 8) lưu trữ dữ liệu cấu trúc (User, Truyện, Lịch sử đọc,...).
  - **Media Storage:** AWS S3 dùng để lưu trữ toàn bộ ảnh truyện. App/Web lấy ảnh trực tiếp từ S3 giúp giảm thiểu tải băng thông cho Backend.

---

## CHƯƠNG 3: CÁC CHỨC NĂNG CHÍNH

### 3.1. Chức năng dành cho Độc giả (Android App)
- **Duyệt và tìm kiếm truyện:** Xem danh sách truyện mới cập nhật, truyện hot, lọc theo thể loại.
- **Đọc truyện tranh:** Trình đọc truyện mượt mà, tải ảnh nhanh nhờ lấy link trực tiếp từ CDN/S3.
- **Chế độ đọc ẩn danh (Guest Mode):** Đọc không cần tài khoản, hệ thống sử dụng Room DB lưu trữ tiến độ trên thiết bị.
- **Quản lý tài khoản:** Đăng nhập, đăng ký, đổi mật khẩu, đồng bộ lịch sử đọc từ thiết bị lên Server khi người dùng chuyển từ Guest sang User chính thức.
- **Đánh dấu và theo dõi:** Thêm truyện vào danh sách yêu thích, xem lịch sử đọc.
- **Gợi ý truyện:** Gợi ý truyện (Recommendation) liên quan dựa trên thể loại hoặc tác giả.

### 3.2. Chức năng dành cho Quản trị viên (Web Admin)
- **Quản lý nội dung (Truyện/Chương):** Đăng tải (Upload) truyện mới, quản lý chapter (chương). Tệp hình ảnh dung lượng lớn được tự động đẩy lên AWS S3.
- **Quản lý danh mục/thể loại:** Thêm, sửa, xóa các thể loại truyện (Action, Romance, Sci-fi,...).
- **Quản lý người dùng:** Kiểm soát danh sách độc giả, cấp quyền.
- **Hệ thống thông báo (Notification):** Gửi thông báo broadcast/từng cá nhân đến người dùng khi có sự kiện hoặc chương mới.

---

## CHƯƠNG 4: KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

### 4.1. Kết quả đạt được
- Xây dựng thành công hệ thống đọc và quản lý truyện tranh hoàn chỉnh bao gồm App Mobile và Web Admin.
- Tích hợp thành công các dịch vụ Cloud (AWS RDS, S3) đảm bảo tính mở rộng và tốc độ tải trang nhanh nhờ cơ chế đọc ảnh trực tiếp không qua backend trung gian.
- Xây dựng API bảo mật với JWT, tối ưu hóa quá trình đồng bộ dữ liệu Offline-Online một cách mượt mà.

### 4.2. Hạn chế và Khó khăn
- Cần tối ưu thêm chi phí duy trì các dịch vụ đám mây (AWS EC2, S3, RDS) nếu hệ thống có lượng người truy cập lớn đồng thời.
- Hệ thống gợi ý (Recommendation) còn ở mức cơ bản, cần nghiên cứu đưa các mô hình trí tuệ nhân tạo (AI/ML) vào phân tích sâu hơn sở thích đọc.

### 4.3. Hướng phát triển tương lai
- Phát triển thêm tính năng **Tải xuống Offline** (Download chapter) phục vụ người dùng đọc khi không có mạng.
- Tích hợp cổng thanh toán để triển khai hệ thống **Gói Đăng ký VIP (VIP Subscription)** như đã thiết kế.
- Tối ưu bộ nhớ Cache bằng **Redis** để giảm tải cho cơ sở dữ liệu đối với các truy vấn đọc nhiều (Trang chủ, chi tiết truyện hot).
- Mở rộng thêm nền tảng Web Client cho phép độc giả đọc truyện trực tiếp trên trình duyệt máy tính.

---
*Tài liệu này được tổng hợp dựa trên kiến trúc hệ thống và mã nguồn thực tế của dự án ComicVerse.*
