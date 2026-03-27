# API Specification Document

## [cite_start]1. Cấu trúc Response Mẫu [cite: 19]
[cite_start]Để API đồng nhất, cấu trúc JSON được đề xuất như sau[cite: 20]:

[cite_start]**Success Response:** [cite: 21, 22]
* [cite_start]"success": true, [cite: 23, 24]
* [cite_start]"data": { ... }, [cite: 25]
* [cite_start]"message": "Request processed successfully" [cite: 26, 27]

[cite_start]**Error Response:** [cite: 28, 29]
* [cite_start]"success": false, [cite: 30, 31]
* [cite_start]"error": { [cite: 32]
    * [cite_start]"code": "AGE_RESTRICTED", [cite: 33]
    * [cite_start]"message": "Bạn chưa đủ 18 tuổi để xem nội dung này" [cite: 34, 35]
* [cite_start]} [cite: 36]

---

## 2. Danh sách API theo Module

### 2.1. [cite_start]MODULE: AUTHENTICATION & USER (UC04, UC05, UC25) [cite: 1]
[cite_start]Quản lý định danh, đăng ký và phân quyền người dùng[cite: 3].

| Method | Endpoint | Actor | Mô tả |
| :--- | :--- | :--- | :--- |
| POST | `/auth/register` | Guest | [cite_start]Đăng ký tài khoản mới [cite: 2] |
| POST | `/auth/login` | Guest | [cite_start]Đăng nhập và nhận JWT Token [cite: 2] |
| GET | `/user/profile` | User | [cite_start]Lấy thông tin cá nhân & ngày sinh [cite: 2] |
| PUT | `/user/profile` | User | [cite_start]Cập nhật hồ sơ (Display name, Avatar) [cite: 2] |
| GET | `/admin/users` | Admin | [cite_start]Liệt kê & Quản lý người dùng (UC25) [cite: 2] |
| PATCH | `/admin/users/{id}/status` | Admin | [cite_start]Khóa/Mở tài khoản (ACTIVE/BANNED) [cite: 2] |

### 2.2. [cite_start]MODULE: CONTENT DELIVERY (UC01, UC02, UC03, UC06) [cite: 4]
[cite_start]Lấy dữ liệu truyện (Comic/Novel) cho độc giả[cite: 5].

| Method | Endpoint | Actor | Mô tả |
| :--- | :--- | :--- | :--- |
| GET | `/comics` | Guest | [cite_start]Danh sách truyện (Hỗ trợ Filter genre, tag, type) [cite: 6] |
| GET | `/comics/search` | Guest | [cite_start]Tìm kiếm truyện theo từ khóa (UC02) [cite: 6] |
| GET | `/comics/{slug_or_id}` | Guest | [cite_start]Chi tiết truyện, Tác giả, Age Rating (UC03) [cite: 6] |
| GET | `/comics/{id}/chapters` | Guest | [cite_start]Danh sách chương (Sắp xếp theo sort_order) [cite: 6] |
| GET | `/chapters/{id}` | User | [cite_start]Nội dung chương (Text cho Novel hoặc Metadata cho Comic) [cite: 6] |
| GET | `/chapters/{id}/pages` | User | [cite_start]Danh sách URL hình ảnh truyện tranh (UC06) [cite: 6] |

### 2.3. [cite_start]MODULE: SOCIAL & INTERACTION (UC07, UC09, UC10, UC11, UC12) [cite: 7]
[cite_start]Xử lý tương tác giữa người dùng và nội dung[cite: 8].

| Method | Endpoint | Actor | Mô tả |
| :--- | :--- | :--- | :--- |
| POST | `/comics/{id}/follow` | User | [cite_start]Theo dõi/Bỏ theo dõi truyện (UC07) [cite: 9] |
| POST | `/comics/{id}/rate` | User | [cite_start]Gửi đánh giá sao (1-5) (UC12) [cite: 9] |
| GET | `/chapters/{id}/comments` | Guest | [cite_start]Lấy bình luận tầng sâu (Nested) [cite: 9] |
| POST | `/chapters/{id}/comments` | User | [cite_start]Gửi bình luận mới hoặc Reply (UC09, UC10) [cite: 9] |
| POST | `/comments/{id}/like` | User | [cite_start]Like/Unlike bình luận (Cập nhật like_count) [cite: 9] |
| POST | `/reports` | User | [cite_start]Báo cáo vi phạm bình luận/nội dung [cite: 9] |

### 2.4. [cite_start]MODULE: VIP & TRANSACTION (UC16, UC17, UC18) [cite: 10]
[cite_start]Quản lý gói cước và thanh toán[cite: 11].

| Method | Endpoint | Actor | Mô tả |
| :--- | :--- | :--- | :--- |
| GET | `/vip/packages` | Guest | [cite_start]Lấy danh sách các gói VIP hiện có [cite: 12] |
| POST | `/vip/subscribe` | User | [cite_start]Khởi tạo thanh toán mua gói (Momo/ZaloPay) [cite: 12] |
| GET | `/vip/transactions` | User | [cite_start]Xem lịch sử nạp tiền (UC18) [cite: 12] |
| GET | `/user/subscriptions` | User | [cite_start]Kiểm tra trạng thái VIP hiện tại [cite: 12] |

### 2.5. [cite_start]MODULE: READING & AI (UC08, UC13, UC14) [cite: 13]
[cite_start]Cá nhân hóa trải nghiệm qua lịch sử đọc và AI[cite: 14].

| Method | Endpoint | Actor | Mô tả |
| :--- | :--- | :--- | :--- |
| GET | `/reading/history` | User | [cite_start]Lấy lịch sử truyện đã đọc (UC14) [cite: 15] |
| POST | `/reading/history` | User | [cite_start]Cập nhật tiến trình đọc (last_page, chapter_id) [cite: 15] |
| GET | `/reading/bookmarks` | User | [cite_start]Lấy danh sách các trang đã đánh dấu (UC08) [cite: 15] |
| GET | `/ai/summaries/comic/{id}` | User | [cite_start]Lấy bản tóm tắt truyện do AI tạo (UC13) [cite: 15] |
| GET | `/ai/recommendations` | User | [cite_start]Lấy danh sách truyện gợi ý theo sở thích [cite: 15] |

### 2.6. [cite_start]MODULE: CMS & ADMINISTRATION (UC19 -> UC32) [cite: 16]
[cite_start]Dành cho Uploader đăng nội dung và Admin kiểm duyệt[cite: 17].

| Method | Endpoint | Actor | Mô tả |
| :--- | :--- | :--- | :--- |
| POST | `/cms/comics` | Uploader | [cite_start]Tạo hồ sơ truyện mới (UC19) [cite: 18] |
| POST | `/cms/chapters` | Uploader | [cite_start]Đăng chương mới (PENDING) (UC20, UC21) [cite: 18] |
| GET | `/admin/upload-logs` | Admin | [cite_start]Danh sách chương chờ duyệt (UC26) [cite: 18] |
| PATCH | `/admin/chapters/{id}/status` | Admin | [cite_start]Duyệt/Từ chối chương (APPROVED/REJECTED) [cite: 18] |
| GET | `/analytics/daily-stats` | Admin/Up | [cite_start]Lấy thống kê lượt xem, doanh thu (UC31, UC23) [cite: 18] |
| PUT | `/admin/configs` | Admin | [cite_start]Cập nhật cấu hình hệ thống (UC32) [cite: 18] |