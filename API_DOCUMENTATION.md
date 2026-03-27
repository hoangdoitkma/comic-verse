# Tài Liệu API Hệ Thống DATN

Tài liệu này mô tả toàn bộ các API endpoint, method, payload gửi lên và cấu trúc JSON trả về của 3 luồng chức năng chính: **Auth** (Xác thực), **Uploader** (Đăng truyện) và **Admin** (Quản trị).

Cấu trúc trả về chung (Wrapper) của tất cả API thường có dạng `ApiResponse<T>`:
```json
{
  "status": 200,
  "message": "Nội dung thông báo (nếu có)",
  "data": { ... dứa liệu trả về tuỳ từng API ... },
  "timestamp": "2024-03-25T10:00:00.000"
}
```
*Lưu ý:* Các mục `data` bên dưới mô tả phần bên trong object `data` của response wrapper.

---

## 1. Luồng Xác thực (Auth)
**Base URL:** `/api/auth`

### 1.1 Đăng nhập (Login)
- **Endpoint:** `POST /login`
- **Chức năng:** Xác thực và lấy JWT Token.
- **Request Body (JSON):**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
- **Response (Data):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "user@example.com",
  "roles": ["ROLE_ADMIN"]
}
```

### 1.2 Đăng ký (Register)
- **Endpoint:** `POST /register`
- **Chức năng:** Tạo tài khoản người dùng mới (Mặc định Role: `USER`).
- **Request Body (JSON):**
```json
{
  "email": "newuser@example.com",
  "password": "password123",
  "displayName": "New User"
}
```
- **Response (Data):** `null` (Thông báo thành công trong `message`).

---

## 2. Luồng Uploader (Quản lý nội dung đăng tải)
**Base URL:** `/api/uploader`
**Yêu cầu:** Token có quyền `ROLE_UPLOADER` hoặc `ROLE_ADMIN`.

### 2.1 Truyện (Comics)
#### 2.1.1 Thêm truyện mới
- **Endpoint:** `POST /comics`
- **Content-Type:** `multipart/form-data`
- **Request Data:**
  - `title` (String): Tiêu đề truyện
  - `synopsis` (String): Tóm tắt nội dung
  - `authorId` (Integer): Đi kèm ID tác giả
  - `ageRatingId` (Integer): Giới hạn độ tuổi
  - `contentType` (String): Loại nội dung (`COMIC`, `NOVEL`,...)
  - `comicFormat` (String): Định dạng
  - `thumbnail` (File): Ảnh bìa truyện
- **Response (Data):** Trả về `ComicResponse` (Xem phần 2.1.2).

#### 2.1.2 Lấy danh sách truyện đã đăng của Uploader
- **Endpoint:** `GET /comics`
- **Request Body:** Không có
- **Response (Data) - List:**
```json
[
  {
    "id": 1,
    "title": "Tên Truyện",
    "synopsis": "Tóm tắt...",
    "thumbnailUrl": "https://s3.../image.jpg",
    "contentType": "COMIC",
    "comicFormat": "COLORED",
    "status": "APPROVED",
    "totalChapters": 10,
    "viewCount": 1500,
    "createdAt": "2024-03-25T10:00:00.000",
    "updatedAt": "2024-03-25T10:00:00.000"
  }
]
```

#### 2.1.3 Cập nhật truyện
- **Endpoint:** `PUT /comics/{comicId}`
- **Request Body (JSON):**
```json
{
  "title": "Tiêu đề mới",
  "synopsis": "Tóm tắt mới",
  "authorId": 2,
  "ageRatingId": 1,
  "contentType": "COMIC",
  "comicFormat": "ORIGINAL"
}
```
- **Response (Data):** Trả về `ComicResponse` đã cập nhật.

### 2.2 Chương truyện (Chapters)
#### 2.2.1 Tạo chương tranh mĩ thuật (Comic Chapter)
- **Endpoint:** `POST /chapters/{comicId}/comic`
- **Content-Type:** `multipart/form-data`
- **Request Data:**
  - `chapterNumber` (BigDecimal): Số chương (ví dụ 1.5)
  - `title` (String): Tên chương
  - `accessType` (String): Loại quyền truy cập (`FREE`, `VIP`, v.v.)
  - `pages` (File[]): Mảng các file ảnh (các trang truyện)
- **Response (Data):** `null` (Trả status 200 message thành công).

#### 2.2.2 Tạo chương tiểu thuyết (Novel Chapter)
- **Endpoint:** `POST /chapters/{comicId}/novel`
- **Request Body (JSON):** Thay vì file, ta gửi nội dung dạng String.
```json
{
  "chapterNumber": 2,
  "title": "Tiêu đề chương tiểu thuyết",
  "accessType": "FREE",
  "content": "Nội dung văn bản tiểu thuyết..."
}
```
- **Response (Data):** `null` (Trả status 200 message thành công).

#### 2.2.3 Lấy danh sách chương của một truyện
- **Endpoint:** `GET /chapters/{comicId}`
- **Request Body:** Không có
- **Response (Data):** Trả về danh sách `ChapterRequest` detail (mượn tạm class request làm model trả về).
```json
[
  {
    "chapterNumber": 1.0,
    "title": "Chương 1",
    "accessType": "FREE",
    "content": null
  }
]
```

---

## 3. Luồng Quản trị (Admin)
**Yêu cầu:** Token có quyền `ROLE_ADMIN`.

### 3.1 Dữ liệu Danh mục (Master Data)
**Base URL:** `/api/admin/data`

#### 3.1.1 Thể loại (Genres)
- `GET /genres`: Trả về danh sách `GenreResponse`.
- `GET /genres/{id}`: Trả về 1 `GenreResponse`.
- `POST /genres`: Tạo thể loại mới.
  - **Body (GenreRequest):** `{ "name": "Action", "description": "Hành động" }`
- `PUT /genres/{id}`: Cập nhật thể loại.
  - **Body (GenreRequest):** Như POST.
- `DELETE /genres/{id}`: Xoá thể loại.
- **Response (GenreResponse):** `{ "id": 1, "name": "Action", "description": "Hành động" }`

#### 3.1.2 Tác giả (Authors)
- `GET /authors`: Trả về danh sách `AuthorResponse`.
- `GET /authors/{id}`: Trả về một `AuthorResponse`.
- `POST /authors`: Tạo mới.
  - **Body (AuthorRequest):** `{ "name": "Tác Giả A", "studio": "Studio B", "country": "VN" }`
- `PUT /authors/{id}`: Cập nhật (Body tương tự POST).
- `DELETE /authors/{id}`: Xóa.
- **Response (AuthorResponse):** `{ "id": 1, "name": "Tác Giả A", "studio": "Studio B", "country": "VN", "createdAt": "..." }`

#### 3.1.3 Gói VIP (VIP Packages)
- `GET /vip-packages`: Trả về danh sách `VipPackageResponse`.
- `GET /vip-packages/{id}`: Trả về `VipPackageResponse`.
- `POST /vip-packages`: Tạo mới.
  - **Body (VipPackageRequest):** `{ "name": "Gói 1 Tháng", "durationMonth": 1, "price": 50000, "currency": "VND", "isActive": true }`
- `PUT /vip-packages/{id}`: Cập nhật (Body tương tự POST).
- `DELETE /vip-packages/{id}`: Xóa.

### 3.2 Kiểm duyệt (Moderation)
**Base URL:** `/api/admin/moderation`

#### 3.2.1 Lấy danh sách chờ duyệt (Logs Pending)
- **Endpoint:** `GET /logs/pending`
- **Response (Data):** Dạng danh sách (`List<UploadLogResponse>`).
```json
[
  {
    "id": 1,
    "status": "PENDING",
    "uploaderId": 5,
    "uploaderName": "User 5",
    "comicId": 12,
    "comicTitle": "Truyện cần duyệt",
    "chapterId": 34,
    "chapterTitle": "Chương 1",
    "reviewAt": null,
    "reviewerId": null,
    "createdAt": "2024-03-25T10:00:00"
  }
]
```

#### 3.2.2 Duyệt hoặc Từ chối Upload Log
- **Endpoint:** `PUT /logs/{logId}`
- **Request Body (ReviewRequest):**
```json
{
  "status": "APPROVED", 
  "reason": "Nội dung bạo lực quá mức" // (Hoặc REJECTED kèm lý do)
}
```
- **Response:** Trả về `UploadLogResponse` đã nhận xét/duyệt.

### 3.3 Bảng điều khiển (Dashboard)
**Base URL:** `/api/admin/dashboard`

- **Endpoint:** `GET /summary`
- **Chức năng:** Trả về các số liệu báo cáo tổng quan.
- **Response (Data - DashboardStatsResponse):**
```json
{
  "totalNewUsers": 150,
  "totalNewComics": 20,
  "totalRevenue": 5000000.00
}
```

### 3.4 Báo cáo người dùng (Reports)
**Base URL:** `/api/admin/reports`

- **Endpoint:** `GET ?page=0&size=10`
- **Response (Data):** Trả về Spring `Page<ReportResponse>`.
```json
{
  "content": [
    {
      "id": 1,
      "reporterId": 2,
      "reporterName": "Nguyen Van A",
      "commentId": 4,
      "commentContent": "Nội dung comment vi phạm",
      "reason": "Chửi bới, xúc phạm",
      "status": "PENDING",
      "createdAt": "2024-03-25T10:00:00"
    }
  ],
  "pageable": { ... },
  "totalPages": 1,
  "totalElements": 1
}
```

- **Endpoint:** `PUT /{reportId}`
- **Chức năng:** Xử lý báo cáo.
- **Request Body (HandleReportRequest):**
```json
{
  "action": "RESOLVED" // Hoặc "REJECTED"
}
```

### 3.5 Quản lý Người dùng (Admin Users)
**Base URL:** `/api/admin/users`

- **Endpoint:** `GET ?role=&status=&page=0&size=10`
- **Chức năng:** Tìm kiếm/lọc danh sách user.
- **Response (Data):** Trả về `Page<UserResponse>`.
```json
{
  "content": [
    {
      "id": 2,
      "email": "user2@example.com",
      "displayName": "User 2",
      "avatarUrl": "https://...",
      "role": "USER",
      "status": "ACTIVE",
      "createdAt": "2024-03-20..."
    }
  ],
  "totalPages": 5,
  "totalElements": 50
}
```

- **Endpoint:** `PUT /{userId}/status`
- **Chức năng:** Khoá/Mở khoá tài khoản, v.v.
- **Request Body (UpdateUserStatusRequest):**
```json
{
  "status": "SUSPENDED" // Hoặc "ACTIVE", "BANNED"
}
```
