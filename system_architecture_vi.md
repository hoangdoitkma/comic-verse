# Tài Liệu Thiết Kế Hệ Thống - ComicVerse

Tài liệu này mô tả chi tiết kiến trúc tổng thể của dự án ComicVerse, bao gồm 3 phân hệ chính: ứng dụng Android, giao diện quản trị Web (Admin/Uploader) và Backend Spring Boot, cùng với cơ sở hạ tầng đám mây lưu trữ dữ liệu trên AWS.

---

## 1. Tổng Quan Hệ Thống

Dự án ComicVerse áp dụng mô hình phân tán (Client - Server) xoay quanh backend được triển khai tập trung, phục vụ thiết bị di động và các trình duyệt web bảo trì. Hệ thống bao gồm:

1. **Android App (`/android-app`)**: Ứng dụng client dành cho người đọc. Giao tiếp với backend thông qua REST API chuyên biệt cho phía "Public".
2. **Web Admin (`/web-admin`)**: Giao diện quản trị/dashboard cho Admin và Uploader (quản lý người dùng, đăng tải truyện tranh, quản lý danh mục, v.v.). Tương tác qua nhóm API riêng biệt cho "Admin".
3. **Backend Service (`/backend-springboot`)**: Server ứng dụng viết bằng Java / Spring Boot. Được triển khai trên máy chủ đám mây (AWS EC2 - Endpoint hiện tại: `http://3.1.207.156:8080`).

---

## 2. Sơ Đồ Kiến Trúc Hệ Thống (Architecture Diagram)

Dưới đây là sơ đồ luồng dữ liệu và phương thức giao tiếp giữa các thành phần.

```mermaid
flowchart TD
    %% Khai báo Client
    subgraph Clients["Tầng Client/Frontend"]
        App["📱 Android App\n(Kotlin/Java, Retrofit, Room)"]
        Web["💻 Web Admin\n(React/Vue, Axios)"]
    end

    %% Khai báo Compute Cloud
    subgraph Compute["AWS EC2"]
        Backend["🚀 Backend Spring Boot\n(Port 8080, RESTful API, JWT)"]
    end

    %% Khai báo Backend Storage Cloud
    subgraph Storage["AWS Cloud Dịch vụ Quản lý"]
        RDS[("🗄️ AWS RDS\n(MySQL 8)")]
        S3[("☁️ AWS S3\n(Object Storage)") ]
    end

    %% Định nghĩa giao tiếp C2B
    App -- "HTTP/REST API\nJSON (Retrofit)" --> Backend
    Web -- "HTTP/REST API\nJSON (Axios)" --> Backend

    %% Định nghĩa giao tiếp B2D
    Backend -- "JPA/Hibernate\n(Mã hóa, TCP 3306)" --> RDS
    Backend -- "AWS SDK (SigV4)\nQuản lý file/URL" --> S3
    
    %% Trả ảnh trực tiếp từ S3 xuống Client (CDN/URL trần)
    S3 -. "Truy cập public resource\n(Image URLs)" .-> App
    S3 -. "Preview Thumbnail" .-> Web
```

---

## 3. Cách Thức Các Thành Phần Giao Tiếp (Component Communication)

### 3.1 Giao thức & Bảo mật
Toàn bộ dữ liệu giao tiếp giữa các trình khách (Client) và máy chủ (Backend) sử dụng **HTTP/RESTful API** với định dạng **JSON**.
- **Xác thực và phân quyền (Authentication/Authorization)**: Ứng dụng sử dụng **JWT (JSON Web Token)** để bảo mật.
  - Sau khi đăng nhập (`/api/auth/login` cho Admin, hoặc `auth/public/login` cho App), hệ thống phát hành Access Token và Refresh Token.
  - Các request đòi hỏi quyền truy cập phải có chuỗi `Authorization: Bearer <token>` trên tiêu đề (Header).

### 3.2 Tương tác từ Android App
- Máy khách Android xây dựng với **MVVM** pattern. Lớp mạng (Network Layer) sử dụng **Retrofit + OkHttp** (được định nghĩa rõ tại `ApiService.java`).
- Dữ liệu API được xử lý bất đồng bộ nhờ **RxJava/Coroutines**.
- Có chế độ khách (**Guest Mode**). Tiến trình đọc khi không có mạng hoặc chưa đăng nhập được lưu tại cơ sở dữ liệu nội bộ trên thiết bị (**Room Database**). Đồng bộ lên Server (vào MySQL) khi người dùng Online và Đăng nhập.

### 3.3 Tương tác từ Web Admin
- Được đóng gói độc lập và tương tác máy chủ thông qua Client Axios (`web-admin/src/utils/axiosClient.js`). Được cấu hình `baseURL`: `http://3.1.207.156:8080/api`.
- Request Interceptor của Axios sẽ tự động lấy và gắn JWT Token có sẵn ở `localStorage` vào các lệnh điều khiển quản trị (thêm Chapter, xóa truyện) trước khi gọi xuống Backend.
- Nếu Backend trả về 401, Interceptor sẽ điều hướng tự động về trang Login.

---

## 4. Cách Gọi Và Lưu Dữ Liệu Các Dịch Vụ AWS

Dữ liệu của hệ thống không lưu trữ trực tiếp trên Local File System của EC2 để đảm bảo khả năng thu phóng (Scalability) và chịu lỗi. Hệ thống dùng hai luồng xử lý độc lập cho *Dữ Liệu Quan Hệ* và *Tệp Đa Phương Tiện*.

### 4.1. Cơ Sở Dữ Liệu Quan Hệ (AWS RDS - MySQL 8)

- **Vai trò**: Lưu trữ dữ liệu cấu trúc của hệ thống, bao gồm User Profile, Roles, thẻ Comic (Tên, Thể loại, Tác giả), Chapter, Lịch sử đọc (Reading History) và Logging views.
- **Cấu hình Backend**:
  Spring Boot tương tác qua Hibernate. Chỉnh định trong `application.yml` (`datasource.url`):
  ```yaml
  datasource:
    url: jdbc:mysql://comic-verse-db.cniko6wq6psw.ap-southeast-1.rds.amazonaws.com:3306/comicverse
    username: admin
  ```
- **Luồng hoạt động**:
  1. Backend ánh xạ Data Models (ví dụ: `@Entity Comic`, `@Entity Chapter`) vào các bảng trong MySQL thông qua Spring Data JPA.
  2. Các lời gọi cấp Repository sẽ được Hibernate chuyển đổi thành Native SQL tương thích (tối ưu hóa qua Connection Pool - HikariCP).

### 4.2. Lưu Trữ File Đa Phương Tiện (AWS S3)

- **Vai trò**: Cloud Object Storage để lưu toàn bộ tệp tĩnh, chủ yếu là ảnh Cover (Bìa) truyện và Danh sách trang truyện của từng Chapter.
- **Thông tin Storage**: Bucket: `comicverse-storage` - Region: `ap-southeast-1` (Singapore).
- **Cách Backend tương tác với S3**:
  1. Sử dụng thư viện `AWS SDK for Java`.
  2. Backend khởi tạo xác thực (Credentials) thông qua `access-key` và `secret-key` ẩn trong `application.yml`.
  3. **Quy trình Upload (Luồng Uploader từ Web)**:
     - Admin tải file ảnh (`multipart/form-data`). Hệ thống gửi file dung lượng lớn này lên Backend EC2.
     - Backend sẽ nhận Buffered Image này, băm tên chống trùng lặp và đẩy trực tiếp (Streaming) vào AWS S3 thông qua `PutObjectRequest`.
     - S3 trả lại **URL tuyệt đối** trỏ tới Object trên Cloud. Backend lưu cấu trúc URI file này vào bảng Database trên RDS và cấp tín hiệu thành công.
  4. **Quy trình Phân Phối (Luồng Đọc từ App/Web)**:
     - Ứng dụng/Trình duyệt gửi API kết xuất đọc 1 chương. Backend tra cứu MySQL và trả mảng chứa các URL tuyệt đối kết nối trực tiếp với S3.
     - Giao diện người dùng nhận các liên kết ảnh trực tiếp (Image URLs) này, giao cho thư viện Render Ảnh nội bộ (`Glide` trên Android hoặc thẻ DOM `<img src="...">` trên Web) gọi HTTP request trực tiếp tải tệp tĩnh về thiết bị. 
     - Nhờ thiết kế này, **Lưu lượng mạng Media tải về người dùng hoàn toàn bypass (vượt qua) Backend** giảm được 90% băng thông cho ứng dụng trung tâm.
