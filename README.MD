# 📚 ComicVerse - Hệ thống Đọc Truyện Tranh Trực Tuyến

Chào mừng bạn đến với mã nguồn của Đồ án Tốt nghiệp: **Hệ thống Quản lý và Đọc truyện tranh ComicVerse**. 
Dự án được xây dựng theo mô hình Client-Server với đầy đủ các nền tảng dành cho Người dùng (Mobile App) và Quản trị viên (Web Admin).

---

## 🗂 Cấu trúc thư mục (Monorepo)

Dự án được tổ chức thành 3 module chính nằm trong thư mục gốc:
- 📱 `android-app/`: Ứng dụng Mobile (Dành cho độc giả). Ngôn ngữ: Java/Kotlin, Kiến trúc: MVVM.
- ⚙️ `backend-springboot/`: Máy chủ API RESTful. Framework: Java Spring Boot 3.x.
- 💻 `web-admin/`: Giao diện Website Quản trị (Dành cho Admin/Uploader). Framework: Node.js / React / Vue.

---

## 🛠 Yêu cầu hệ thống (Prerequisites)

Để cài đặt và chạy thử nghiệm toàn bộ hệ thống ở môi trường Local, máy tính của bạn cần cài đặt sẵn:
1. **Java JDK 17** (hoặc mới hơn).
2. **Node.js** (Phiên bản 18.x trở lên) & NPM/Yarn.
3. **Android Studio** (Bản mới nhất) & Máy ảo Android (Emulator) hoặc thiết bị điện thoại thật.
4. **MySQL Server** (Phiên bản 8.x) hoặc phần mềm XAMPP / DBeaver / MySQL Workbench để quản lý Cơ sở dữ liệu.

---

## 🚀 Hướng dẫn Cài đặt & Khởi chạy

Vui lòng thực hiện tuần tự theo các bước dưới đây để khởi chạy hệ thống tránh gặp lỗi kết nối:

### Bước 1: Khởi tạo Cơ sở dữ liệu (Database)
1. Mở MySQL Client, DBeaver hoặc phpMyAdmin.
2. Tạo một database mới với tên: `comic_verse_db` (Character set: `utf8mb4`).
3. Tìm file `database_export.sql` (nằm trong thư mục gốc hoặc thư mục `docs/`) và Import (phục hồi) vào database vừa tạo.

### Bước 2: Khởi chạy Backend (Java Spring Boot)
1. Mở thư mục `backend-springboot` bằng phần mềm **IntelliJ IDEA** (hoặc Eclipse).
2. Mở file cấu hình `src/main/resources/application.properties` (hoặc `application.yml`) và cập nhật lại thông tin kết nối Database của máy bạn:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/comic_verse_db
   spring.datasource.username=root
   spring.datasource.password=mat_khau_cua_ban
   ```
3. Chờ Maven đồng bộ các thư viện (Dependencies).
4. Nhấn **Run** class `ComicVerseApplication.java` để khởi động Server. 
   *(Mặc định server sẽ chạy ở địa chỉ: `http://localhost:8080`)*

### Bước 3: Khởi chạy Web Admin
1. Mở Terminal/Command Prompt, trỏ vào thư mục `web-admin`:
   ```bash
   cd web-admin
   ```
2. Cài đặt các gói thư viện cần thiết:
   ```bash
   npm install
   ```
3. Khởi chạy giao diện Web:
   ```bash
   npm run dev
   ```
   *(Truy cập Web Admin tại địa chỉ: `http://localhost:3000` hoặc cổng được cấp trên Terminal)*

### Bước 4: Khởi chạy App Android
1. Mở thư mục `android-app` bằng **Android Studio**.
2. Đợi Gradle đồng bộ (Sync) xong toàn bộ thư viện.
3. **Lưu ý quan trọng:** Cập nhật lại `BASE_URL` trỏ về Backend cục bộ. Mở file cấu hình mạng (VD: `RetrofitClient.java`, `NetworkModule.kt` hoặc `Constants.kt`) và đổi thành:
   - Nếu chạy trên máy ảo của Android Studio (Emulator): `http://10.0.2.2:8080/`
   - Nếu chạy trên điện thoại thật (cắm cáp USB): `http://<IP_LAN_Của_Máy_Tính>:8080/` (Ví dụ: `http://192.168.1.5:8080/`)
4. Nhấn **Run (Shift + F10)** để cài đặt ứng dụng lên thiết bị.

---

## 🔑 Tài khoản Thử nghiệm (Test Accounts)

Để thuận tiện cho việc đánh giá các chức năng của hệ thống, vui lòng sử dụng các tài khoản được cung cấp sẵn dưới đây:

- **Tài khoản Web Quản trị (Admin):** - Email: `admin@comicverse.com` 
  - Mật khẩu: `123456`
  
- **Tài khoản App Mobile (User):** - Hệ thống hỗ trợ tính năng **Đọc ẩn danh (Guest)** không cần đăng nhập, lưu tiến độ đọc trực tiếp trên thiết bị (Room Database). 
  - Để test luồng có tài khoản và đồng bộ dữ liệu, vui lòng sử dụng:
    - Email: `user@comicverse.com` 
    - Mật khẩu: `123456`

---
*Lưu ý: Dự án có sử dụng dịch vụ AWS S3 để lưu trữ ảnh truyện và AWS RDS. Trong bản chạy Local này, cấu hình đã được điều chỉnh để đọc URL ảnh trực tiếp qua Internet. Vui lòng đảm bảo thiết bị có kết nối mạng ổn định để trải nghiệm ứng dụng.*