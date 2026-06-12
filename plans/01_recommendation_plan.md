# Kế hoạch triển khai (Hoàn thiện): Đề xuất truyện dựa trên thói quen đọc

Tính năng tự động đề xuất ưu tiên các truyện phù hợp với sở thích của người dùng dựa trên `ReadingHistory` (lịch sử đọc). 
Bản kế hoạch này đã được thống nhất:
1. **Tính toán Real-time**: Logic sẽ chạy ngay khi gọi API để người dùng có thể thấy kết quả ngay lập tức phục vụ việc test.
2. **Logic cho Guest**: Người dùng chưa đăng nhập sẽ nhận danh sách 10 truyện có lượt xem cao nhất toàn hệ thống.

## Proposed Changes

---

### Backend (Spring Boot)

#### [MODIFY] `backend-springboot/src/main/java/com/datn/backend/repository/ComicRepository.java`
- Thêm các Query JPA hỗ trợ thuật toán:
  - Lấy Top 10 truyện nhiều View nhất toàn hệ thống (dành cho Guest).
  - Tìm các truyện thuộc một danh sách Thể loại (Genre IDs) cụ thể, bỏ qua các truyện nằm trong list ID mà User đã đọc, và sắp xếp theo độ trùng khớp/lượt xem.

#### [NEW] `backend-springboot/src/main/java/com/datn/backend/repository/RecommendationRepository.java`
- Tạo Repository cho `Recommendation` Entity để có thể lưu trữ kết quả Đề xuất (Cache) nhằm phục vụ mở rộng về sau, hoặc lịch sử truy vết đề xuất.

#### [NEW] `backend-springboot/src/main/java/com/datn/backend/service/RecommendationService.java`
- Chứa logic cốt lõi:
  1. Kiểm tra User có đăng nhập không. Nếu `null` -> Lấy Top 10 truyện Views cao nhất báo về.
  2. Nếu User đã đăng nhập: Lấy toàn bộ `ReadingHistory` của User.
  3. Lọc ra danh sách các `Comic` đã đọc, rút trích 3-5 `Genre` được đọc nhiều nhất.
  4. Query Database lấy 10 bộ truyện thuộc các Thể loại này mà chưa nằm trong danh sách đã đọc.
  5. Xóa các bản ghi `Recommendation` cũ của User này trong Database và Insert bản ghi mới (mục đích để tận dụng bảng Recommendation có sẵn, minh bạch kết quả).
  6. Return danh sách truyện.

#### [MODIFY] `backend-springboot/src/main/java/com/datn/backend/controller/public_api/PublicComicController.java`
- Tạo API mới: `GET /api/v1/public/comics/recommended`.
- Endpoint sẽ kiểm tra Authentication Token (có hoặc không có đều cho phép pass qua, do Spring Security cấu hình permitAll cho chuỗi `/public`).
- Trả về JSON theo định dạng `BaseResponse<List<ComicDto>>`.

---

### Android App

#### [MODIFY] `android-app/app/src/main/java/com/datn/comicverse/data/remote/ApiService.kt`
- Khai báo endpoint: `@GET("api/v1/public/comics/recommended")` cấu hình trả list truyện.

#### [MODIFY] `android-app/app/src/main/java/com/datn/comicverse/ui/home/HomeFragment` & `HomeViewModel`
- Bổ sung `getRecommendedComics()` vào file Repository của màn hình Home.
- Tại `HomeViewModel`, gọi hàm này cùng lúc khi tải màn hình Home.
- Tại XML và `HomeFragment`, thêm một danh sách RecyclerView "Dành cho bạn" (hoặc Tùy chọn đề xuất) nằm ngay dưới mục Banner hoặc Truyện Mới.
