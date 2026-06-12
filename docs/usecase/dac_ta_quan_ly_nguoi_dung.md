# Đặc tả Use Case: Quản lý Người dùng

Tài liệu này cung cấp đặc tả chi tiết cho các Use Case thuộc phân hệ **Quản lý Người dùng**, dựa trên biểu đồ phân rã `phan_ra_quan_ly_nguoi_dung.puml`.

---

## 1. Use Case: Xem danh sách người dùng (UC_View)

*   **Tác nhân (Actor):** Quản trị viên (Admin)
*   **Mục đích:** Cho phép Admin xem danh sách toàn bộ người dùng trong hệ thống để quản lý và theo dõi thông tin.
*   **Tiền điều kiện:** Admin đã đăng nhập thành công vào trang quản trị (Admin Panel).
*   **Hậu điều kiện:** Giao diện hiển thị danh sách người dùng với các thông tin cơ bản.

**Luồng sự kiện chính (Main Flow):**
1.  Admin chọn mục "Quản lý Người dùng" (hoặc "Danh sách người dùng") trên menu điều hướng của hệ thống.
2.  Hệ thống gửi yêu cầu lấy dữ liệu người dùng đến máy chủ.
3.  Hệ thống truy xuất cơ sở dữ liệu, lấy thông tin người dùng bao gồm: ID, Tên hiển thị (Username/Full name), Email, Vai trò (Role), Ngày tạo, Trạng thái (Hoạt động/Bị khóa).
4.  Hệ thống hiển thị dữ liệu lên giao diện dưới dạng bảng. Nếu số lượng người dùng lớn, hệ thống sẽ tự động phân trang (pagination).

**Luồng sự kiện thay thế / Ngoại lệ (Alternative Flows):**
*   **A1 - Không có dữ liệu:** Tại bước 3, nếu hệ thống chưa có người dùng nào (ngoài bản thân Admin), hệ thống sẽ hiển thị thông báo "Không tìm thấy dữ liệu người dùng" trên màn hình.
*   **A2 - Lỗi kết nối:** Nếu quá trình tải dữ liệu gặp lỗi (lỗi mạng, server không phản hồi), hệ thống hiển thị thông báo "Không thể tải danh sách người dùng lúc này. Vui lòng thử lại sau".
*   **A3 - Tìm kiếm / Lọc:** Admin có thể nhập từ khóa vào ô tìm kiếm (theo tên, email) hoặc sử dụng bộ lọc (theo Role, Trạng thái) để thu hẹp kết quả. Hệ thống sẽ tự động gửi yêu cầu và cập nhật lại danh sách theo tiêu chí đã chọn.

---

## 2. Use Case: Khóa / Mở khóa tài khoản (UC_Ban)

*   **Tác nhân (Actor):** Quản trị viên (Admin)
*   **Mục đích:** Cho phép Admin xử lý các tài khoản vi phạm chính sách bằng cách khóa (Ban) hoặc khôi phục hoạt động cho tài khoản bị khóa trước đó.
*   **Tiền điều kiện:** Admin đang ở màn hình xem danh sách người dùng.
*   **Hậu điều kiện:** Trạng thái tài khoản của người dùng được cập nhật (Active <-> Banned) trong cơ sở dữ liệu.

**Luồng sự kiện chính (Main Flow):**
1.  Tại danh sách người dùng, Admin xác định người dùng cần xử lý.
2.  Admin nhấn vào nút/biểu tượng hành động "Khóa" (nếu tài khoản đang hoạt động) hoặc "Mở khóa" (nếu tài khoản đang bị khóa).
3.  Hệ thống hiển thị hộp thoại xác nhận: "Bạn có chắc chắn muốn [khóa/mở khóa] tài khoản này không?". (Có thể kèm theo ô nhập lý do khóa).
4.  Admin nhấn "Đồng ý" (hoặc "Xác nhận").
5.  Hệ thống cập nhật trạng thái của người dùng tương ứng trong cơ sở dữ liệu.
6.  Hệ thống hiển thị thông báo "Cập nhật trạng thái thành công", đồng thời cập nhật lại trạng thái hiển thị của người dùng đó trên bảng danh sách.

**Luồng sự kiện thay thế / Ngoại lệ (Alternative Flows):**
*   **A1 - Hủy bỏ thao tác:** Tại bước 3, nếu Admin chọn "Hủy", hệ thống sẽ đóng hộp thoại và không có thay đổi nào được thực hiện.
*   **A2 - Khóa tài khoản Admin khác:** Nếu Admin cố gắng khóa một tài khoản cũng có cấp bậc Admin ngang hàng hoặc cao hơn (tùy theo logic phân quyền hệ thống), hệ thống sẽ từ chối và hiển thị lỗi: "Bạn không có quyền khóa tài khoản Quản trị viên khác".
*   **A3 - Lỗi hệ thống:** Tại bước 5, nếu việc cập nhật cơ sở dữ liệu thất bại, hệ thống thông báo lỗi "Cập nhật trạng thái thất bại. Vui lòng thử lại".

---

## 3. Use Case: Phân quyền - Cấp Role (UC_Role)

*   **Tác nhân (Actor):** Quản trị viên (Admin)
*   **Mục đích:** Cho phép Admin thay đổi vai trò của người dùng trong hệ thống (Ví dụ: nâng cấp người dùng thường thành Uploader, hạ quyền Uploader về User, hoặc cấp quyền Admin).
*   **Tiền điều kiện:** Admin đang ở màn hình danh sách người dùng (hoặc trong trang chi tiết người dùng).
*   **Hậu điều kiện:** Vai trò (Role) của người dùng được cập nhật trong hệ thống, ảnh hưởng trực tiếp đến quyền hạn của họ ở lần đăng nhập tiếp theo hoặc ngay lập tức.

**Luồng sự kiện chính (Main Flow):**
1.  Admin nhấn vào nút "Phân quyền" (hoặc nút Chỉnh sửa thông tin) trên dòng dữ liệu của người dùng cần thay đổi.
2.  Hệ thống hiển thị một hộp thoại (hoặc chuyển sang trang chi tiết) cho phép chọn Vai trò hiện tại.
3.  Hệ thống hiển thị danh sách các vai trò khả dụng dưới dạng Dropdown list (vd: User, Uploader, Admin).
4.  Admin chọn một vai trò mới từ danh sách.
5.  Admin nhấn "Lưu thay đổi" (hoặc "Cập nhật").
6.  Hệ thống hiển thị hộp thoại yêu cầu xác nhận việc thay đổi quyền hạn.
7.  Admin nhấn "Xác nhận".
8.  Hệ thống cập nhật vai trò mới vào cơ sở dữ liệu cho ID người dùng tương ứng.
9.  Hệ thống thông báo "Cập nhật quyền thành công" và làm mới lại giao diện hiển thị để phản ánh vai trò mới.

**Luồng sự kiện thay thế / Ngoại lệ (Alternative Flows):**
*   **A1 - Không thay đổi quyền:** Nếu Admin mở hộp thoại nhưng chọn lại quyền cũ (quyền hiện tại) và nhấn Lưu, hệ thống có thể bỏ qua quá trình cập nhật database và chỉ đóng hộp thoại hoặc thông báo "Không có thay đổi nào".
*   **A2 - Hủy bỏ thao tác:** Tại bước 6, nếu Admin chọn "Hủy", hệ thống đóng hộp thoại xác nhận.
*   **A3 - Lỗi hệ thống:** Tại bước 8, nếu việc ghi nhận vào DB bị lỗi, hệ thống hiển thị thông báo "Có lỗi xảy ra khi phân quyền. Vui lòng thử lại".
*   **A4 - Thay đổi quyền bản thân (Self-demotion):** Nếu Admin tự hạ quyền của chính mình, hệ thống có thể hiện một cảnh báo đặc biệt: "Bạn đang hạ quyền của chính mình. Sau khi lưu, bạn sẽ mất quyền truy cập Admin". Nếu tiếp tục, Admin sẽ bị đăng xuất hoặc bị chuyển hướng ra khỏi Admin Panel.
