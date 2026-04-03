# Kế hoạch cải thiện luồng Upload Truyện (Gắn Thể Loại, Thêm Meta & Vá Lỗi UX)

Kế hoạch này sẽ giải quyết dứt điểm sự thiếu sót dữ liệu của hệ thống, vá lỗ hổng trong UX (Trải nghiệm Uploader), và cung cấp đầy đủ thông tin để thuật toán Recommend hoạt động.

## Chi tiết kế hoạch (Proposed Changes)

---

### Quản trị Cơ Sở Dữ Liệu (Backend)

#### [NEW] `backend-springboot/src/main/java/com/datn/backend/entity/enums/PublishStatus.java`
- Tạo Enum định nghĩa Trạng thái phát hành (ONGOING - Đang tiến hành, COMPLETED - Đã hoàn thành, HIATUS - Tạm ngưng).

#### [NEW] `backend-springboot/src/main/java/com/datn/backend/entity/enums/OriginCountry.java`
- Tạo Enum định nghĩa Nguồn gốc xuất xứ (JAPAN - Manga, KOREA - Manhwa, CHINA - Manhua, VIETNAM - Truyện Việt, GLOBAL).

#### [MODIFY] `backend-springboot/src/main/java/com/datn/backend/entity/Comic.java`
- Thêm cột `publishStatus` (EnumType.STRING).
- Thêm cột `originCountry` (EnumType.STRING).

#### [MODIFY] `backend-springboot/src/main/java/com/datn/backend/dto/request/ComicRequest.java`
- Bổ sung biến `List<Integer> genreIds` để đón list thể loại.
- Bổ sung biến `PublishStatus publishStatus` & `OriginCountry originCountry`.
- **[Vá Lỗi UX]**: Gỡ bỏ ràng buộc `@NotNull` của biến `comicFormat`.

#### [MODIFY] `backend-springboot/src/main/java/com/datn/backend/service/ComicService.java`
- Load cấu trúc bảng Entity `Genre` qua `genreIds` và map vào `comic_genres`.
- Xử lý lưu các Enum mới. Đồng thời xử lý logic: Nếu Request đưa lên là NOVEL, tự động null hóa (`null`) trường `comicFormat` bất kể đầu vào.

#### [MODIFY] `backend-springboot/src/main/java/com/datn/backend/dto/response/ComicResponse.java`
- Nối mảng `genres` và 2 trường `publishStatus`, `originCountry` qua đường JSON.

---

### Web Admin (React)

#### [MODIFY] `web-admin/src/pages/uploader/CreateComicModal.jsx`
- Gọi API danh sách `Thể loại`. Dựng giao diện Multi-Select checkbox cho thể loại.
- Bổ sung Select Menu cho 2 trường mới: `Tiến độ` & `Nguồn gốc`.
- **[Vá Lỗi UX]**: Tạo hàm lắng nghe (useEffect hoặc condition rendering). Nếu người dùng chọn `Loại nội dung` (contentType) là **NOVEL (Tiểu thuyết)** -> Ẩn hoặc Auto-disable khối tùy chọn `Định dạng (comicFormat)` (Màu / Đen trắng) vì không có tác dụng. Nếu `COMIC` thì mới cho chọn lại.

#### [MODIFY] `web-admin/src/pages/uploader/ComicDetailPage.jsx`
- Vẽ UI Card info mới bổ sung 2 Badge tròn hiển thị Trạng thái Tiến độ và Quốc gia.
- Cập nhật thêm khu vực liệt kê các Tag Thể Loại.

## Kế hoạch kiểm thử (Verification Plan)
1. Truy cập Web Admin -> Upload -> Thêm Truyện Mới.
2. Kiểm tra thao tác: Thử chọn Novel -> Đảm bảo mục Định dạng (Format) biến mất. Chọn lại Comic -> Hiện ra Màu/Trắng Đen.
3. Chỉnh sửa và Lưu vào Hệ Thống -> Xem hiển thị ngoài Màn Detal.
