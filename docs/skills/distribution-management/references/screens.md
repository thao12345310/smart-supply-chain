# Mô tả màn hình hệ thống

## Phân hệ Mua hàng

### Màn hình Tạo đơn hàng mua (PO)
**Route**: Mua hàng → Đơn hàng mua → Tạo mới

**Fields**:
| Field | Type | Bắt buộc |
|---|---|---|
| Nhà cung cấp | Select (search) | ✓ |
| Kho hàng | Select | ✓ |
| Chi phí vận chuyển | Number | |
| Loại thuế chi phí vận chuyển | Select | |
| Thêm thuế | Toggle | |
| Số hóa đơn (TC) | Text | |
| Giao sau ngày | DateTime | |
| Giao trước ngày | DateTime | |
| Tên đơn hàng | Textarea | |
| DS Sản phẩm | Table | ✓ |

**Cột bảng sản phẩm**: Mã SP, Tên SP, Giá mua gần nhất, Giá bán (sau thuế), SL tồn, ĐVT, SL, ĐG trước thuế, ĐG chi phí (trước thuế), Thành tiền

---

### Màn hình Chi tiết đơn hàng mua
**Route**: Click vào mã đơn trong danh sách PO

**Tabs**:
- **Tổng quan**: Thông tin tóm tắt, các nút hành động (Chỉnh sửa / Duyệt / Sao chép / Hủy bỏ / In)
- **Thông tin chung**: Các field chi tiết
- **Sản phẩm**: DS sản phẩm với giá, số lượng
- **Phiếu nhập**: DS phiếu nhập liên quan + nút "+ Tạo phiếu"
- **Thanh toán**: DS thanh toán
- **Lịch sử chỉnh sửa**: Log thay đổi
- **Trả lại**: Hàng trả NCC

**Fields hiển thị**: Mã đơn hàng, Trạng thái, Ngày tạo đơn, Nhà cung cấp, Số hóa đơn TC, Người tạo, Nhập về kho, Thời gian giao hàng, Tên đơn hàng

**Actions buttons**: Chỉnh sửa | Duyệt | Sao chép | Hủy bỏ | In

---

### Màn hình TAB Phiếu nhập (trong chi tiết PO)
**Columns**: Mã phiếu | Trạng thái | Loại phiếu | Ngày tạo | Giao trước ngày | Kho hàng | Địa chỉ nhận

**Button**: `+ Tạo phiếu` — mở dialog chọn kho

---

### Màn hình Chi tiết Phiếu nhập (NV kho)
**Header**: Mã phiếu, Nhà cung cấp, Kho nhập, Địa chỉ kho, Ghi chú

**Bảng sản phẩm columns**: Mã SP | Tên SP | Đơn vị | SL yêu cầu | **SL thực nhận** (input) | Số lô (input) | Ngày SX (date) | Hạn sử dụng (date)

**Actions**: `✓ Nhập hàng` | `← Quay lại`

---

## Phân hệ Bán hàng

### Màn hình Tạo đơn hàng bán (SO)
**Route**: Bán hàng → Đơn hàng bán → Tạo mới

**Fields**:
| Field | Type | Bắt buộc |
|---|---|---|
| Kênh bán hàng | Select | ✓ |
| Mã kho hàng | Select | |
| Khách hàng | Select (search) | ✓ |
| Giao sau ngày | Date | ✓ |
| Giao trước ngày | Date | |
| Số hóa đơn TC | Text | |
| Địa chỉ nhận | Select | |
| SĐT nhận | Select | |
| Phương thức vận chuyển | Select | |
| Đơn vị vận chuyển | Select | |
| Mục đích đơn hàng bán | Select | Default: Bán hàng |
| Ghi chú | Textarea | |
| Loại chiết khấu | Select | |
| Giá trị chiết khấu | Number | |
| Bảng giá | Select | |
| Xuất hoá đơn VAT | Checkbox | |

**Cột bảng SP**: STT | Mã SP | Tên SP | Đơn vị | Giá sàn | Giá bán lẻ | Đơn giá | Giá bao gồm thuế | Số lượng | Thành tiền

---

### Màn hình Chi tiết đơn hàng bán
**Tabs**: Tổng quan | Thông tin chung | Thanh toán | Sản phẩm

**Actions**: `✓ Duyệt` | `🗑 Hủy bỏ` | `✏ Chỉnh sửa` | `+ Chiết khấu`

**Fields**: Mã đơn hàng, Số hóa đơn TC, Mục đích, Trạng thái, Kênh bán hàng, Kho hàng, Khách hàng, Nhân viên bán hàng, Địa chỉ, SĐT, Độ ưu tiên, Ngày giao hàng, Ngày tạo đơn, Ghi chú

---

### Màn hình Chi tiết Phiếu xuất
**Header**: Mã đơn hàng, Khách hàng, SĐT, Địa chỉ KH, Ngày giao hàng, Ghi chú | Mã phiếu, Loại phiếu, Trạng thái, Kho xuất, Địa chỉ kho, Ngày tạo, Ngày xuất kho

**Bảng SP columns**: Mã SP | Tên SP | Đơn vị | SL yêu cầu | SL thực xuất | Đơn giá | Thành tiền | Số lô | Ngày SX | HSD

**Actions**: `✏ Chỉnh sửa` | `⬆ Xuất hàng` | `✗ Hủy bỏ` | `🖨 Phiếu giao`

---

## Phân hệ Kho vận - Giao hàng

### Màn hình Danh sách Đợt giao hàng
**Route**: Menu "Giao hàng theo đợt"

**Columns**: Mã đợt GH | Ngày tạo | Mô tả | Trạng thái

**Statuses**: Mới tạo | Hoàn thành

**Button**: `Thêm mới`

---

### Màn hình Chi tiết Đợt giao hàng
**4 Tabs**:

#### Tab 1: Thông tin chung
Mã đợt, Ngày tạo, Mô tả, Trạng thái

#### Tab 2: DS Vận đơn
| Column | Note |
|---|---|
| Mã phiếu xuất | Link đến phiếu xuất |
| Người tạo | |
| [remove button] | Xóa khỏi đợt |

Button: `Thêm` — chọn vận đơn từ danh sách

#### Tab 3: DS Shipper
| Column | Note |
|---|---|
| Mã NHVG | Mã nhân viên giao hàng |
| Tên | |
| [remove button] | |

Button: `Thêm`

#### Tab 4: DS Chuyến
| Column | Note |
|---|---|
| Mã chuyến | Link đến chuyến |
| NHVG | Shipper phụ trách |
| Số vận đơn | Số lượng vận đơn trong chuyến |
| [remove button] | |

Buttons: `Tự động tạo chuyến` | `Thêm`

---

## Phân hệ Kế toán

### Màn hình Chi tiết Hóa đơn
**Tabs**: Tổng quan | Giao dịch | Thanh toán | Đơn hàng | Phiếu nhập/xuất

**Tổng quan fields**: Mã HĐ, Loại HĐ, Trạng thái, Thành tiền, Ngày chứng từ, Ngày TT, Hạn TT, Đơn vị bán, Đơn vị mua, Kênh bán hàng/cửa hàng, Kho hàng, Mã tham chiếu, Mô tả

**Giao dịch (acctg_trans)**:
| Column | |
|---|---|
| Mã bút toán | Link |
| Loại bút toán | |
| Năm kế toán | |
| Ngày ghi nhận | |
| Đã ghi sổ | Y/N |

Khi expand 1 bút toán hiện **acctg_trans_entry**:
| Mã định khoản | Mã SP | Loại tài khoản | Tên tài khoản | Mã tài khoản | Nợ/Có | Số tiền | Mô tả |

**Thanh toán**:
- Giá trị đã khớp lệnh / Giá trị chưa khớp lệnh
- DS Thanh toán đã khớp: Mã TT | Loại TT | Phương thức | Giá trị khớp lệnh
- DS Thanh toán của hóa đơn: Mã TT | Ngày hiệu lực | Loại | Phương thức | Số tiền | Đã khớp | Chưa khớp | Trạng thái

**Actions**: `Nhận` | `Duyệt` | `Hủy` (hoặc `Chỉnh sửa / Sao chép / Hủy bỏ / In` tùy trạng thái)

---

### Màn hình Tạo mới Thanh toán (Payment)
**Fields**:
| Field | Note |
|---|---|
| Đơn vị thanh toán | Auto: công ty |
| Đơn vị nhận TT | Auto: NCC / KH |
| Ngày có hiệu lực | Date, bắt buộc |
| Hạn thanh toán | Date |
| Ngày thanh toán | Date |
| Số tiền | Number, bắt buộc |
| Loại thanh toán | Select (Thanh toán hóa đơn...) |
| Phương thức TT | Select (Tiền mặt / CK...) |
| Mô tả | Textarea |

---

### Màn hình Chi tiết Thanh toán (Payment)
**Tabs**: Tổng quan | Giao dịch | Khớp lệnh | Hóa đơn

**Actions**: `✏ Chỉnh sửa` | `Cho phép` | `Chuyển tiền` | `Từ chối` | `Hủy`

---

### Màn hình Phiếu nhập (Kế toán phải trả)
**Route**: Menu Kế toán → Kế toán phải trả → Phiếu nhập

**Tabs**: Tổng quan | Phân bổ chi phí

**TAB Tổng quan**: Thông tin chi tiết phiếu nhập, danh sách SP với SL yêu cầu, SL thực nhận, số lô

**TAB Phân bổ chi phí**:
- DS phân bổ: Mã phân bổ | Ngày tạo | Trạng thái
- Button: `+ Thêm mới` → Mở dialog "Danh sách hóa đơn chi phí phải trả"
- Dialog columns: Mã HĐ | Loại HĐ | Đơn vị bán | Ngày chứng từ | Trạng thái | Thành tiền

---

### Màn hình Tạo Hóa đơn Chi phí (3 bước)
**Bước 1 - Thông tin chung**:
- Loại hóa đơn (chọn "Chi phí mua hàng")
- Ngày chứng từ
- Hạn thanh toán
- Đã thanh toán (toggle)
- Loại TT / Công cụ TT / Phương thức TT / Ngày hiệu lực
- Đơn vị bán (NCC)
- Đơn vị mua (auto: công ty)
- Kênh bán hàng/cửa hàng
- **Kho hàng** (quan trọng: phải khớp với kho phiếu nhập)
- Mã tham chiếu / Mô tả / Thêm thuế

**Bước 2 - Thành phần hóa đơn**:
- DS thành phần: Loại TPHĐ | Sản phẩm | Số lượng | Đơn giá | Mô tả
- Dialog Thêm mới TPHĐ: Loại TPHĐ (Vận chuyển, bốc xếp: 151) | Sản phẩm | SL | Đơn giá | Mô tả

**Bước 3 - Xác nhận**: Review và xác nhận