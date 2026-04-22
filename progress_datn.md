# Cập nhật Tiến độ Dự án & Danh sách Tính năng Hoàn thiện

Tài liệu này tóm tắt tình trạng hiện tại của hệ thống **Quản lý Chuỗi cung ứng Thông minh (Smart Supply Chain Management)** và xác định danh sách tính năng cuối cùng cho đồ án tốt nghiệp (DATN).

## Tóm tắt Tiến độ Hiện tại

Dự án đã thiết lập được nền tảng vững chắc với các module cốt lõi cho **Mua hàng**, **Bán hàng** và **Kho vận**. Backend tuân thủ kiến trúc sạch (Spring Boot + JPA) và frontend là ứng dụng React có tính đáp ứng (responsive).

### [ĐÃ HOÀN THÀNH] Các Module Cốt lõi

#### 1. Hệ thống & Dữ liệu Danh mục (Master Data)
- [x] **Xác thực & RBAC**: Đăng nhập dựa trên JWT và phân quyền theo vai trò (Admin, Quản lý Mua hàng, Quản lý Bán hàng, Nhân viên Kho, Kế toán, Shipper).
- [x] **Quản lý Sản phẩm**: Theo dõi SKU, Danh mục, Đơn vị tính và Giá.
- [x] **Quản lý Đối tác**: Hồ sơ chi tiết cho Khách hàng và Nhà cung cấp.
- [x] **Quản lý Kho**: Hỗ trợ quản lý đa kho.

#### 2. Phân hệ Mua hàng (Purchase Module)
- [x] **Đơn mua hàng (PO)**: Tạo đơn, quy trình phê duyệt nhiều cấp và theo dõi trạng thái.
- [x] **Nhập kho (Goods Receipt - GR)**: Nhập kho một phần/toàn bộ từ PO, theo dõi số lô/hạn sử dụng và tự động tăng tồn kho.

#### 3. Phân hệ Bán hàng (Sales Module)
- [x] **Đơn bán hàng (SO)**: Đặt hàng, kiểm tra tồn kho khả dụng và quản lý trạng thái.
- [x] **Xuất kho (Goods Issue - GI)**: Trừ tồn kho, liên kết với Đơn bán hàng.
- [x] **Hóa đơn Bán hàng**: Ghi nhận doanh thu, theo dõi thanh toán đơn giản (Đã thanh toán/Chưa thanh toán/Thanh toán một phần).

#### 4. Phân hệ Kho vận (Inventory Module)
- [x] **Theo dõi Tồn kho Thời gian thực**: Mức tồn kho trên các kho khác nhau.
- [x] **Nhật ký Tồn kho**: Lưu vết toàn bộ các biến động kho (Nhập, Xuất, Điều chỉnh).

---

## Danh sách Tính năng Đề xuất Hoàn thiện

Dựa trên phạm vi dự án và tiến độ hiện tại, đây là danh sách các tính năng cuối cùng sẽ được bàn giao:

### Phân hệ 1: Quản lý Mua hàng (Purchase)
- Lập và quản lý đơn mua hàng (PO).
- Quy trình phê duyệt PO.
- Nhập kho (Goods Receipt) theo PO.
- Theo dõi lịch sử nhập hàng và công nợ nhà cung cấp.

### Phân hệ 2: Quản lý Bán hàng (Sales)
- Lập và quản lý đơn bán hàng (SO).
- Kiểm tra tồn kho khả dụng (ATP) khi đặt hàng.
- Xuất kho (Goods Issue) và Giao hàng.
- Hóa đơn bán hàng (Sales Invoice) và quản lý công nợ khách hàng (Sổ cái).

### Phân hệ 3: Quản lý Kho & Logistics (Inventory & Logistics)
- Quản lý tồn kho đa kho, đa vị trí.
- Quản lý lô hàng (Batch) và hạn sử dụng (Expiry Date).
- Lập đợt giao hàng (Delivery Plan) và phân chuyến (Delivery Trip).
- Theo dõi trạng thái giao hàng của Shipper.

### Phân hệ 4: Kế toán & Tài chính (Accounting - tạm thời dừng thực hiện -> bonus sau khi hoàn thành hết các phân hệ chính)
- **Bút toán tự động (Accounting Transactions)**: Tự động hạch toán khi Nhập/Xuất/Hóa đơn.
- **Quản lý Thanh toán (Payments)**: Quy trình thanh toán chi tiết, khớp lệnh hóa đơn.

### Phân hệ 5: Báo cáo & Dashboard (Reporting)
- Dashboard tổng quan doanh thu, tồn kho.
- Báo cáo chi tiết nhập-xuất-tồn.
- Báo cáo công nợ quá hạn.

---

## Kế hoạch Xác minh

### Kiểm thử Tự động (Automated Tests)
- Xác minh tính nhất quán của tồn kho sau khi Nhập/Xuất qua các Integration Tests.
- Kiểm thử bảo mật API (thực thi RBAC).

### Xác minh Thủ công (Manual Verification)
- Chạy thử toàn bộ luồng nghiệp vụ: `PO -> GR -> SO -> GI -> Invoice -> Payment`.
- Xác minh tính toán chi phí trong module kế toán.
