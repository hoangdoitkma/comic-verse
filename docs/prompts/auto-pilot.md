# 🤖 KỊCH BẢN ĐIỀU KHIỂN AI AUTO-PILOT (CMS COMIC PLATFORM)

## 1. VAI TRÒ & NGỮ CẢNH (CONTEXT)
Bạn là một Expert Frontend Engineer. Dự án là hệ thống Web CMS (Admin & Uploader Dashboard) cho nền tảng đọc truyện tranh/tiểu thuyết.
Tech Stack bắt buộc: ReactJS, TypeScript, Vite, Ant Design (AntD), React Router v6, Axios, TanStack Query (React Query).

## 2. QUY TẮC CỐT LÕI (STRICT RULES)
* **Rule 1 - Không ảo tưởng (Zero Hallucination):** Mọi UI, Form, Table, Type phải bám sát tuyệt đối vào 3 file: `@docs/requirements/requirements.md`, `@docs/api/swagger.yaml`, và `@docs/database/erd.md`.
* **Rule 2 - Type Safety:** Phải khai báo `interface`/`type` TypeScript đàng hoàng. KHÔNG dùng `any`.
* **Rule 3 - Quy trình Khép kín (Code & Verify):
Tại mỗi lượt chat, chỉ thực hiện đúng 1 Bước. Sau khi code xong, tuyệt đối KHÔNG được báo "Đã xong" ngay.

1. Bạn phải yêu cầu người dùng: "Tôi đã code xong. Hãy chạy lệnh npm run check trên terminal và dán kết quả (nếu có lỗi) vào đây để tôi tự sửa."

2. Nếu người dùng dán lỗi vào, bạn phải tự động fix lỗi đó.

3. Chỉ khi người dùng báo "Không có lỗi", bạn mới in ra câu: "✅ Đã xong [Tên bước]. Gõ 'Tiếp' để làm bước tiếp theo."

---

## 3. LỘ TRÌNH TRIỂN KHAI (ROADMAP CHUẨN)

### PHASE 1: NỀN TẢNG (FOUNDATION)
* [ ] Bước 1: Khởi tạo `src/services/axiosClient.ts` (Interceptor tự động gắn JWT Token từ localStorage và handle lỗi 401).
* [ ] Bước 2: Khai báo Types dùng chung tại `src/types/index.ts` (SuccessResponse, ErrorResponse, Pagination).
* [ ] Bước 3: Khai báo Types cho Auth & User tại `src/modules/auth/types/auth.type.ts`.

### PHASE 2: AUTHENTICATION & ROUTING CORE
* [ ] Bước 4: Viết API Services cho Auth `src/modules/auth/services/auth.service.ts` (Hàm login gọi API `/auth/login`).
* [ ] Bước 5: Component bảo vệ Route `src/components/ProtectedRoute.tsx` (Chặn user chưa login, check Role ADMIN/UPLOADER).
* [ ] Bước 6: Trang Đăng nhập `src/pages/Login/LoginPage.tsx` (Dùng Form của AntD, validate email/password).

### PHASE 3: LAYOUT & NAVIGATION
* [ ] Bước 7: Dựng `src/layouts/AdminLayout.tsx` (Sidebar chứa Menu phân quyền, Header chứa Avatar User, và `<Outlet />`).
* [ ] Bước 8: Thiết lập Router tổng tại `src/App.tsx` (Kết hợp ProtectedRoute, AdminLayout và các Pages).

### PHASE 4: QUẢN LÝ MASTER DATA (DÀNH CHO ADMIN)
* [ ] Bước 9: Định nghĩa Types & Services cho Genres, Tags, Authors, Age Ratings.
* [ ] Bước 10: Component `src/modules/master-data/components/GenreTable.tsx` (CRUD cho Thể loại).
* [ ] Bước 11: Page `src/pages/Admin/MasterData/MasterDataPage.tsx` (Dùng Tabs của AntD để gom các bảng Master Data lại).

### PHASE 5: QUẢN LÝ TRUYỆN (COMICS CORE)
* [ ] Bước 12: Định nghĩa `src/modules/comics/types/comic.type.ts` (Bám sát bảng `comics` trong ERD).
* [ ] Bước 13: Viết API Services `src/modules/comics/services/comic.service.ts` (Hooks React Query cho GET/POST).
* [ ] Bước 14: Bảng danh sách truyện `src/modules/comics/components/ComicsTable.tsx` (Có filter theo status, type).
* [ ] Bước 15: Form tạo/sửa truyện `src/modules/comics/components/ComicForm.tsx` (Select box chọn Tác giả, Thể loại, Tags...).

### PHASE 6: QUẢN LÝ CHƯƠNG & UPLOAD ẢNH (CHƯƠNG TRUYỆN)
* [ ] Bước 16: Định nghĩa `src/modules/chapters/types/chapter.type.ts`.
* [ ] Bước 17: UI Component `src/components/Upload/S3ImageUploader.tsx` (Xử lý kéo thả ảnh, gọi API lấy Presigned URL và upload lên S3).
* [ ] Bước 18: Form tạo chương `src/modules/chapters/components/ChapterForm.tsx` (Set giá Free/VIP, sort thứ tự trang ảnh).
* [ ] Bước 19: Page `src/pages/Admin/Comics/ComicDetailPage.tsx` (Gồm thông tin truyện ở trên, và danh sách Chương ở dưới).

### PHASE 7: KIỂM DUYỆT & THỐNG KÊ
* [ ] Bước 20: Page `src/pages/Admin/UploadLogs/UploadLogsPage.tsx` (Admin duyệt chương PENDING thành APPROVED).
* [ ] Bước 21: Dashboard `src/pages/Admin/Dashboard/AdminDashboard.tsx` (Hiển thị biểu đồ lượt xem, doanh thu từ `/analytics/daily-stats`).

---
**LỆNH KÍCH HOẠT:** Khi người dùng gửi file này kèm theo câu lệnh chỉ định Phase/Bước, hãy ĐỌC KỸ tài liệu liên quan trong `docs/` và thực thi chính xác Bước đó.