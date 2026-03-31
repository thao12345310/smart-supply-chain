# Luồng nghiệp vụ chi tiết

## 1. Luồng Mua hàng (Purchase Order)

### Bước 1: Tạo đơn mua (PO)
- **Actor**: NV mua sắm
- **Action**: Điền form Tạo đơn hàng mua
- **Input fields**:
  - Nhà cung cấp (bắt buộc)
  - Kho hàng (bắt buộc)
  - Chi phí vận chuyển, Loại thuế vận chuyển
  - Thêm thuế (toggle)
  - Số hóa đơn TC
  - Giao sau ngày / Giao trước ngày
  - Tên đơn hàng (ghi chú)
  - Danh sách sản phẩm: mã SP, tên SP, ĐVT, SL, ĐG trước thuế, ĐG chi phí
- **Output**: Bản ghi `order_header` (status=OrderOpen) + nhiều `order_item`
- **Note**: NV kho chưa nhìn thấy đơn ở bước này

### Bước 2: Duyệt PO
- **Actor**: QL mua sắm hoặc Kế toán (có quyền duyệt)
- **Action**: Bấm nút "Duyệt" trên màn hình chi tiết PO
- **Output**: `order_header.status` = `OrderApproved`
- **Note**: Sau khi duyệt, NV kho mới nhìn thấy PO trong danh sách

### Bước 3: Tạo phiếu nhập
- **Actor**: NV kho
- **Action**: Vào TAB "Phiếu nhập" trong chi tiết PO → bấm "+ Tạo phiếu" → chọn kho nhập
- **Output**: Bản ghi `shipment` (loại: Phiếu nhập hàng mua, status: Khởi tạo) + `shipment_item` cho mỗi SP
- **Note**: 1 PO có thể tạo nhiều phiếu nhập (nhập nhiều lần, nhập thiếu rồi bổ sung)

### Bước 4: Nhập hàng
- **Actor**: NV kho
- **Action**: Vào chi tiết phiếu nhập → nhập SL thực nhận, số lô, ngày SX, HSD cho từng SP → bấm "Nhập hàng"
- **Output tạo ra**:
  - `asset` + `asset_detail` — lot hàng, tăng tồn kho
  - `invoice` + `invoice_item` — hóa đơn mua hàng (status: InvoiceInProcess)
  - `acctg_trans` + `acctg_trans_entry` — bút toán hàng hóa

### Bước 5: Kế toán tạo Payment
- **Actor**: Kế toán
- **Action**: Vào chi tiết hóa đơn → TAB Thanh toán → "+ Tạo mới"
- **Input**: Số tiền, loại thanh toán, phương thức (tiền mặt / chuyển khoản...), ngày có hiệu lực
- **Output**: `payment` (status: Cam kết sẽ thanh toán)

### Bước 6: Khớp lệnh (Payment Application)
- **Actor**: Kế toán
- **Action**: Vào chi tiết Payment → bấm "Chuyển tiền" (xác nhận đã nhận/trả tiền)
- **Output**: `payment_application`, `acctg_trans` + `acctg_trans_entry` thanh toán

---

## 2. Luồng Bán hàng (Sales Order)

### Bước 1: Tạo đơn bán (SO)
- **Actor**: NV bán hàng
- **Action**: Điền form Tạo đơn hàng bán
- **Input fields**:
  - Kênh bán hàng (bắt buộc)
  - Mã kho hàng
  - Khách hàng (bắt buộc)
  - Giao sau ngày (bắt buộc)
  - Giao trước ngày
  - Số hóa đơn TC
  - Địa chỉ nhận, SĐT nhận
  - Phương thức vận chuyển, Đơn vị vận chuyển
  - Loại chiết khấu, Giá trị chiết khấu, Bảng giá
  - Xuất hóa đơn VAT (checkbox)
  - Danh sách SP: mã SP, tên SP, ĐVT, giá sàn, giá bán lẻ, đơn giá, SL
- **Output**: `order_header` (SO, status=OrderOpen) + `order_item`

### Bước 2: Duyệt SO
- **Actor**: QL bán hàng hoặc Kế toán
- **Action**: Bấm "Duyệt" trên chi tiết SO
- **Output**: `order_header.status` = `OrderApproved`

### Bước 3: Tạo phiếu xuất
- **Actor**: NV kho
- **Action**: Vào TAB "Phiếu xuất" trong chi tiết SO → bấm "+ Tạo phiếu"
- **Output**: `shipment` (loại: Phiếu xuất hàng bán, status: Khởi tạo) + `shipment_item`

### Bước 4: Xuất hàng
- **Actor**: NV kho
- **Action**: Vào chi tiết phiếu xuất → nhập SL thực xuất cho từng SP → bấm "Xuất hàng"
- **Output tạo ra**:
  - `invoice` + `invoice_item` — hóa đơn bán hàng
  - `acctg_trans` + `acctg_trans_entry` — bút toán doanh thu (ghi nhận ngay)
  - **Lưu ý**: `asset` / `asset_detail` (giảm tồn kho) **chưa được cập nhật** ở bước này, chỉ cập nhật sau khi shipper xác nhận giao thành công

### Bước 5 & 6: Kế toán tạo Payment và Khớp lệnh
- Tương tự luồng Mua hàng (bước 5, 6)
- Loại bút toán thanh toán: Nợ TK 111 (Tiền mặt), Có TK 131 (Phải thu KH)

---

## 3. Luồng Kho vận - Giao hàng

### Bước 1: Từ phiếu xuất → Vận đơn
- **Actor**: Delivery Admin
- **Logic**:
  - Nếu phiếu xuất nhỏ (SL ≤ khả năng 1 chuyến): 1 phiếu xuất = 1 vận đơn
  - Nếu phiếu xuất lớn: Delivery Admin chia thành nhiều vận đơn
- **Output**: `delivery_order` (mỗi vận đơn liên kết với 1 phiếu xuất)

### Bước 2: Tạo Đợt giao hàng
- **Actor**: Delivery Admin
- **Action**: Menu "Giao hàng theo đợt" → "+ Thêm mới"
- **Output**: `delivery_plan`
- **Màn hình chi tiết gồm 4 tabs**:
  1. **Thông tin chung**: Mã đợt, ngày tạo, mô tả, trạng thái
  2. **DS Vận đơn**: Thêm/xóa các `delivery_order` vào đợt → `delivery_plan_order`
  3. **DS Shipper**: Thêm/xóa shipper tham gia → `delivery_plan_shipper`
  4. **DS Chuyến**: Danh sách `delivery_triproute`, nút "Tự động tạo chuyến" + "Thêm"

### Bước 3: Tạo Chuyến giao hàng
- **Actor**: Delivery Admin
- **Action**: Bấm "Tự động tạo chuyến" hoặc "Thêm" thủ công trong TAB DS Chuyến
- **Output**:
  - `delivery_triproute` (1 chuyến = 1 shipper)
  - `delivery_triproute_item` (sequence các vận đơn trong chuyến)

### Bước 4: Shipper thực hiện giao hàng
- **Actor**: Shipper
- **Action**: Xem chuyến được phân công → đi giao → ghi nhận kết quả tại từng điểm
- **Output**: Cập nhật trạng thái giao (thành công / thất bại) cho từng `delivery_triproute_item`
- **Sau khi giao thành công**: `asset` / `asset_detail` được cập nhật (giảm tồn kho)

---

## 4. Luồng Kế toán - Phân bổ chi phí vào Phiếu nhập

### Bước 1: Tạo Hóa đơn chi phí mua hàng
- **Actor**: Kế toán
- **Action**: Kế toán phải trả → Hóa đơn → Tạo mới, chọn loại "Chi phí mua hàng"
- **Input**: Kho hàng (phải trùng kho trong phiếu nhập cần phân bổ), ngày chứng từ, đơn vị bán, thành phần hóa đơn
- **Thành phần hóa đơn**: Loại "Vận chuyển, bốc xếp: 151", nhập số tiền, mô tả
- **Output**: `invoice` (loại: Chi phí mua hàng, status: Đang được gửi đến)

### Bước 2: Duyệt Hóa đơn chi phí
- **Actor**: Kế toán
- **Action**: Bấm "Duyệt" trong chi tiết hóa đơn
- **Output**: `acctg_trans` + `acctg_trans_entry` được tạo
  - Nợ TK 151 (Hàng mua đang đi đường) — theo từng khoản chi phí
  - Có TK 331 (Phải trả cho người bán)

### Bước 3: Phân bổ chi phí vào Phiếu nhập
- **Actor**: Kế toán
- **Action**: Vào Phiếu nhập (menu KT phải trả → Phiếu nhập) → TAB "Phân bổ chi phí" → Thêm → Chọn hóa đơn chi phí
- **Điều kiện**: Kho trong phiếu nhập **phải trùng** với kho trong hóa đơn chi phí
- **Công thức phân bổ**:
  ```
  chi_phi_don_vi = tong_chi_phi / sum(don_gia_i × so_luong_i)
  chi_phi_sp_i = chi_phi_don_vi × don_gia_i × so_luong_i
  ```
- **Output**: Cập nhật giá vốn từng SP trong phiếu nhập

---

## 5. Luồng Kế toán - Định khoản tự động

### Khi nhập hàng (PO):
- Nợ TK hàng hóa (ví dụ 156) — giá trị hàng nhập
- Có TK 331 (Phải trả cho người bán)

### Khi xuất hàng (SO):
- Có TK 5111 (Doanh thu bán hàng hóa) — theo từng SP
- Nợ TK 131 (Phải thu của khách hàng) — tổng hóa đơn

### Khi khớp lệnh thu (SO Payment):
- Có TK 131 (Phải thu của khách hàng)
- Nợ TK 111 (Tiền mặt) hoặc TK 112 (Tiền gửi ngân hàng)

### Khi chi phí mua hàng được duyệt:
- Nợ TK 151 (Hàng mua đang đi đường)
- Có TK 331 (Phải trả cho người bán)