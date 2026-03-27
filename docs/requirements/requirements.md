# Product Requirements Document (PRD)
**Project:** Comic & Novel Digital Platform  
**Version:** 1.0.0  
**Status:** Approved  

## 1. Tổng quan hệ thống (System Overview)
[cite_start]Hệ thống được xây dựng nhằm cung cấp một nền tảng đọc truyện tranh trực tuyến cho phép người dùng truy cập, theo dõi và tương tác với các bộ truyện thông qua ứng dụng[cite: 38]. 
[cite_start]Bên cạnh đó, hệ thống cung cấp một hệ thống quản trị nội dung (Content Management System - CMS) cho phép nhân viên nội dung đăng tải, quản lý truyện tranh và quản trị viên quản lý dữ liệu vận hành[cite: 40].

## 2. Nhóm người dùng (User Roles)
[cite_start]Hệ thống được thiết kế với 3 nhóm người dùng chính[cite: 41]:

| Nhóm người dùng | Khái niệm | Mô tả |
| :--- | :--- | :--- |
| **Guest / User** | Độc giả | [cite_start]Sử dụng ứng dụng để đọc truyện, theo dõi, nhận thông báo, bình luận và thực hiện giao dịch mở khóa nội dung[cite: 39, 42]. |
| **Uploader** | Nhân viên nội dung | [cite_start]Chịu trách nhiệm đăng tải và quản lý nội dung truyện[cite: 42, 121]. |
| **System Admin** | Quản trị viên | [cite_start]Chịu trách nhiệm quản lý toàn bộ hệ thống, cấu hình dữ liệu, tài khoản và giám sát hoạt động[cite: 42, 44]. |

---

## 3. Yêu cầu chức năng (Functional Requirements)

### 3.1. Phân hệ Quản trị hệ thống (System Admin)
* [cite_start]**Quản lý Master Data:** * Thiết lập và quản lý Thể loại truyện (Action, Romance, v.v.), cho phép tạo mới, chỉnh sửa, xóa [cite: 45, 47-57].
    * [cite_start]Quản lý Tags (School Life, Comedy, v.v.) để hỗ trợ tìm kiếm và đề xuất [cite: 58-65].
    * [cite_start]Khai báo thông tin Tác giả / Studio (Tên, studio, quốc gia) [cite: 66-70].
    * [cite_start]Thiết lập Nhãn độ tuổi (All Ages, 13+, 16+, 18+) hiển thị trong hồ sơ truyện [cite: 72-76].
* [cite_start]**Cấu hình hệ thống:** Tùy chỉnh tỷ lệ quy đổi tiền sang Coin, thời gian chờ quảng cáo, số chương miễn phí, thời gian mở khóa VIP qua quảng cáo [cite: 77-82].
* [cite_start]**Quản lý Tài khoản & Phân quyền:** Tạo/sửa/khóa tài khoản Uploader [cite: 84-88]. [cite_start]Xem danh sách, trạng thái, lịch sử hoạt động và khóa tài khoản người dùng vi phạm [cite: 89-94].
* [cite_start]**Kiểm duyệt nội dung:** Xem danh sách truyện/chương, xóa nội dung vi phạm, xử lý báo cáo từ cộng đồng [cite: 95-100].
* [cite_start]**Báo cáo & Thống kê (Analytics):** Xem thống kê người dùng (tổng số, active hàng ngày), thống kê truyện (top đọc, theo dõi, đánh giá), thống kê doanh thu (từ Coin, mua VIP, quảng cáo) [cite: 101-113].
* [cite_start]**Truy cập & Cá nhân:** Đăng nhập Web Admin, chỉnh sửa hồ sơ, đổi/quên mật khẩu [cite: 114-119].

### 3.2. Phân hệ Quản lý nội dung (Uploader)
* [cite_start]**Quản lý Hồ sơ truyện:** Tạo và chỉnh sửa Tên truyện, Synopsis, Tác giả, Thể loại, Tags, Trạng thái (Ongoing/Completed), Nhãn độ tuổi, Ảnh bìa [cite: 122-132]. [cite_start]Hệ thống tự động lưu tổng số chương, lượt xem, điểm đánh giá [cite: 133-136].
* [cite_start]**Quản lý Chương truyện:** Tải lên hình ảnh từng trang, đặt tiêu đề, số chương [cite: 139-143]. [cite_start]Hình ảnh lưu trữ trên Cloud Storage[cite: 144].
* [cite_start]**Phân quyền truy cập nội dung:** Thiết lập chương Miễn phí (đọc trực tiếp) hoặc chương VIP (yêu cầu Coin hoặc xem quảng cáo) [cite: 145-152].
* [cite_start]**Theo dõi hiệu suất:** Đánh giá độ phổ biến qua lượt đọc, lượt theo dõi, lượt mua VIP, điểm đánh giá [cite: 153-159].
* [cite_start]**Quản lý tương tác:** Xem, phản hồi độc giả và xóa bình luận vi phạm [cite: 160-164].
* [cite_start]**Truy cập & Cá nhân:** Đăng nhập Web Admin, chỉnh sửa hồ sơ, đổi mật khẩu [cite: 165-169].

### 3.3. Phân hệ Độc giả (Guest / User)
* [cite_start]**Định danh & Tài khoản:** Hỗ trợ Guest (chưa đăng nhập) và User (đã đăng nhập) [cite: 172-173]. [cite_start]Đăng ký, đăng nhập (thường & mạng xã hội), quên/đổi mật khẩu [cite: 174-180].
* [cite_start]**Tủ truyện cá nhân (Dashboard):** Quản lý truyện theo dõi, truyện yêu thích, tự động lưu lịch sử đọc (chương, vị trí trang) [cite: 181-191].
* [cite_start]**Khám phá & Gợi ý:** Tìm kiếm (tên, từ khóa), lọc theo thể loại, sắp xếp (mới, phổ biến, đánh giá cao) [cite: 192-204]. [cite_start]Gợi ý truyện phổ biến, mới, hoặc theo lịch sử đọc [cite: 205-209].
* [cite_start]**Trình đọc truyện (Reader):** Hỗ trợ chế độ cuộn dọc và lật trang ngang [cite: 210-216].
* [cite_start]**Giao dịch & Mở khóa VIP:** Nạp Coin qua ví điện tử (Momo, ZaloPay), xem lịch sử giao dịch [cite: 223-232]. [cite_start]Sử dụng Coin mua vĩnh viễn hoặc xem quảng cáo mở khóa tạm thời [cite: 217-222].
* [cite_start]**Tương tác cộng đồng:** Đánh giá sao, bình luận, trả lời bình luận, báo cáo vi phạm [cite: 233-238]. [cite_start]Hệ thống tự tính điểm trung bình truyện[cite: 239].

---

## 4. Yêu cầu phi chức năng (Non-Functional Requirements)

### 4.1. Tích hợp hệ thống ngoài (3rd Party Integrations)
| Dịch vụ | Chức năng đáp ứng |
| :--- | :--- |
| **Firebase Authentication** | [cite_start]Quản lý định danh và đăng nhập[cite: 242]. |
| **Firebase Cloud Messaging** | [cite_start]Gửi thông báo đẩy (Push Notifications)[cite: 242]. |
| **Payment Gateway** | [cite_start]Xử lý thanh toán ví điện tử (Momo, ZaloPay)[cite: 242]. |
| **Ad Network** | [cite_start]Hiển thị quảng cáo mở khóa nội dung[cite: 242]. |
| **Cloud Storage** | [cite_start]Lưu trữ tối ưu hình ảnh truyện (AWS S3)[cite: 242]. |

### 4.2. Yêu cầu bảo mật nội dung (Security Requirements)
Hệ thống phải áp dụng các biện pháp kỹ thuật khắt khe để bảo vệ bản quyền:
1.  [cite_start]Hạn chế sao chép nội dung trái phép[cite: 245].
2.  [cite_start]Chèn Watermark tự động vào hình ảnh truyện[cite: 246].
3.  [cite_start]Áp dụng cơ chế hạn chế chụp màn hình (Screen capture restriction) trên ứng dụng[cite: 247].
4.  [cite_start]Kiểm soát truy cập chặt chẽ đối với nội dung VIP (Server-side validation)[cite: 248].