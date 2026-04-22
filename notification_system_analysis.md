# Phân tích Hệ thống Thông báo (Notification System)

Dựa trên việc đọc mã nguồn Spring Boot của hệ thống, dưới đây là tiến độ và tình trạng hiện tại của luồng thông báo giữa các nhóm người dùng: Admin, Uploader và User.

## 1. Hệ thống dữ liệu nền tảng
- **Thực thể (Entity)**: Đã có sẵn `Notification` và bảng lưu trữ trong cơ sở dữ liệu với các trường (người nhận, tiêu đề, nội dung, đường dẫn điều hướng, trạng thái đã đọc).
- **Phân loại (Enums)**: Đã định nghĩa các loại thông báo (`NotificationType`): `NEW_CHAPTER`, `COMMENT_REPLY`, `SYSTEM`, `PROMOTION`, `UPDATE`, `APPROVED`, `REJECTED`.
- **Dịch vụ cốt lõi**: `NotificationService` đã cung cấp sẵn các hàm cốt lõi như:
  - `sendSystemNotification`: Gửi cho 1 cá nhân ẩn danh (hệ thống gửi).
  - `sendBroadcastNotification`: Gửi cho toàn bộ người dùng.
  - Xử lý trạng thái đọc thông báo.

---

## 2. Chi tiết các luồng thông báo

### 2.1. Admin -> Uploader (✅ Đã có sẵn 🟢)
- **Hệ thống tự động**: Khi Admin thực hiện Duyệt (Approve) hoặc Từ chối (Reject) truyện/chương mới do Uploader đăng tải (thông qua `AdminModerationServiceImpl`), hệ thống tự động bắn 1 thông báo cho Uploader kèm lý do (nếu bị từ chối). 
- **Gửi thủ công (Manual)**: Admin có thể gửi trực tiếp từ công cụ quản trị (trong `AdminNotificationController`).

### 2.2. Admin -> User (✅ Đã có sẵn 🟢)
- **Gửi thủ công (Manual / Broadcast)**: Admin có thể phát thông báo hệ thống (như bảo trì, nâng cấp, sự kiện - `PROMOTION`, `UPDATE`) tới **tất cả người dùng** (Broadcast) hoặc tới đích danh một ID người dùng nào đó.
- *Lưu ý*: Các hệ thống quà tặng (Giftcode), xác thực tài khoản có thể tận dụng luồng này.

### 2.3. Uploader -> Admin (✅ Đã có sẵn 🟢)
- **Yêu cầu duyệt Truyện**: Khi Uploader tạo một bộ truyện mới (`ComicServiceImpl`), hệ thống tự sinh thông báo loại `SYSTEM` tới tài khoản Admin: *"Có truyện mới được tạo"*.
- **Yêu cầu duyệt Chương**: Khi Uploader vượt quá số lượng/tạo thêm chương mới cần phê duyệt (`ChapterServiceImpl`), hệ thống tự động báo cho Admin: *"Có chương mới chờ duyệt"* (loại `NEW_CHAPTER`).

### 2.4. Uploader -> User (❌ Còn thiếu / Chưa hoàn thiện 🔴)
- Mặc dù hệ thống có Enum `NEW_CHAPTER` nhưng **chưa có logic code tự động kích hoạt** thông báo này tới nhóm *người theo dõi (Followers / Favorites)* khi một chương của bộ truyện họ yêu thích vừa được duyệt / xuất bản thành công.
- **Tính năng có thể bổ sung**: 
  - Kích hoạt sự kiện gửi thông báo (Push/In-app notification) cho danh sách người Follow truyện đó sau khi Admin thao tác `UPLOAD_STATUS = APPROVED` hoặc khi Uploader tự xuất bản chương.
  - Cho phép Uploader phát thông báo Broadcast tới những người đã Follow mình (ví dụ: thông báo hoãn lịch đăng chap).

### 2.5. User -> Admin (❌ Còn thiếu 🔴)
- Hiện hệ thống chưa có tính năng để người dùng giao tiếp/báo cáo về cho Admin.
- **Tính năng có thể bổ sung**: 
  - Gửi thông báo khi người dùng **Report** (báo cáo lỗi chương truyện/hình ảnh hỏng/bình luận độc hại).
  - Gửi thông báo khi người dùng gửi yêu cầu Yêu cầu lên quyền (Upgrade to Uploader).
  - Yêu cầu nạp điểm/xử lý thanh toán lỗi.

### 2.6. User -> Uploader (❌ Còn thiếu 🔴)
- Enum `COMMENT_REPLY` có tồn tại nhưng trong logic nghiệp vụ chưa thấy gửi thông báo.
- **Tính năng có thể bổ sung**:
  - Gửi thông báo cho Uploader khi có độc giả **bình luận mới** trên truyện của họ.
  - Gửi thông báo cho độc giả/Uploader khi có người dùng khác **Trả lời bình luận (Reply)** của họ.
  - Gửi thông báo Đánh giá/Rating truyện.
  - Độc giả báo lỗi chương truyện trực tiếp cho Uploader xử lý trước thay vì báo cho Admin.

---

## 3. Tổng kết & Đề xuất hành động tiếp theo
1. **Khẩn cấp (Nên làm ngay cho App)**: Viết thêm API hoặc xử lý Event Listener để kích hoạt tính năng: **Báo cho User khi truyện đang Follow có Chapter mới** (Uploader -> Admin duyệt -> Báo lại cho User). Đây là tính năng cốt lõi giúp giữ chân người đọc.
2. **Trung hạn**: Hoàn thiện tính năng thảo luận, sử dụng `NotificationType.COMMENT_REPLY` để báo người dùng và Uploader khi có bình luận mới.
3. **Quản trị rủi ro**: Xây dựng module Report (User -> Admin) để nâng cao chất lượng content do các Uploader đăng lên.
