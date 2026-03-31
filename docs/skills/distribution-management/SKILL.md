---
name: distribution-management
description: >
  Hệ thống quản lý phân phối (Distribution Management) gồm 4 phân hệ: Mua hàng, Bán hàng,
  Kho vận, và Kế toán. Dùng skill này bất cứ khi nào người dùng hỏi về nghiệp vụ, thiết kế
  màn hình, viết code, thiết kế database, hoặc phân tích luồng của hệ thống phân phối này.
  Trigger khi thấy các từ khóa: PO, SO, phiếu nhập, phiếu xuất, kho vận, vận đơn, đợt giao
  hàng, chuyến giao, shipper, hóa đơn mua/bán, thanh toán, khớp lệnh, phân bổ chi phí,
  acctg_trans, shipment, delivery_plan, order_header, asset, invoice, payment.
---

# Distribution Management — Skill

## Tổng quan hệ thống

Hệ thống quản lý phân phối kết nối **Nhà cung cấp (Supplier) → Nhà phân phối (Distributor) → Nhà bán lẻ (Retailer) → Khách hàng**.

Có 4 phân hệ nghiệp vụ chính:

| Phân hệ | Mô tả |
|---|---|
| **Mua hàng** | Mua từ NCC → nhập kho tạo tồn kho |
| **Bán hàng** | Bán cho đại lý, cửa hàng, siêu thị |
| **Kho vận** | Quản lý tồn kho, vận chuyển giao hàng |
| **Kế toán** | Công nợ, hạch toán doanh thu/chi phí |

---

## Entities (Bảng dữ liệu chính)

### Mua hàng & Bán hàng
- `order_header` — Header đơn hàng mua (PO) hoặc bán (SO)
- `order_item` — Từng dòng sản phẩm trong đơn hàng

### Kho vận
- `shipment` — Phiếu nhập / phiếu xuất kho
- `shipment_item` — Từng dòng sản phẩm trong phiếu
- `asset` — Lot hàng trong kho (tồn kho theo lô)
- `asset_detail` — Chi tiết lot (số lô, ngày SX, HSD, số lượng)
- `delivery_order` — Vận đơn (sinh từ phiếu xuất)
- `delivery_plan` — Đợt giao hàng
- `delivery_plan_order` — Vận đơn nào thuộc đợt giao hàng nào
- `delivery_plan_shipper` — Shipper nào tham gia đợt giao hàng
- `delivery_triproute` — Chuyến giao hàng (1 shipper thực hiện)
- `delivery_triproute_item` — Sequence các vận đơn trong 1 chuyến

### Kế toán
- `invoice` — Hóa đơn (mua/bán/chi phí)
- `invoice_item` — Từng dòng thành phần hóa đơn
- `payment` — Thanh toán (1 hóa đơn có thể nhiều payment)
- `payment_application` — Lần thực hiện thanh toán (khớp lệnh)
- `acctg_trans` — Bút toán kế toán
- `acctg_trans_entry` — Từng định khoản trong bút toán

---

## Luồng nghiệp vụ

> Xem chi tiết từng luồng tại: `references/workflows.md`

### Mua hàng (Purchase Order Flow)
```
NV mua tạo PO → Duyệt PO (OrderApproved) → NV kho tạo Phiếu nhập
→ Nhập hàng (tạo asset/asset_detail + invoice + acctg_trans)
→ KT tạo Payment → Khớp lệnh (PaymentApplication)
```

### Bán hàng (Sales Order Flow)
```
NV bán tạo SO → Duyệt SO → NV kho tạo Phiếu xuất
→ Xuất hàng (tạo invoice + acctg_trans; asset giảm sau khi giao thành công)
→ KT tạo Payment → Khớp lệnh
```

### Kho vận - Giao hàng (Delivery Flow)
```
Phiếu xuất → Tạo Vận đơn (delivery_order)
→ Tạo Đợt giao hàng (delivery_plan) + chọn Vận đơn + chọn Shipper
→ Tự động tạo Chuyến (delivery_triproute) cho từng shipper
→ Shipper thực hiện giao, ghi nhận kết quả từng điểm
```

### Kế toán - Phân bổ chi phí
```
KT tạo Hóa đơn "Chi phí mua hàng" → Duyệt → Vào Phiếu nhập
→ TAB Phân bổ chi phí → Chọn hóa đơn chi phí
→ Hệ thống tự phân bổ theo tỷ lệ (đơn giá × số lượng)
```
Công thức: `chi_phi_don_vi = tong_chi_phi / sum(don_gia_i × so_luong_i)`

---

## Trạng thái (Status)

| Đối tượng | Status | Mô tả |
|---|---|---|
| PO / SO | `OrderOpen` | Mới tạo, chưa duyệt |
| PO / SO | `OrderApproved` | Đã duyệt, NV kho nhìn thấy |
| Phiếu nhập/xuất | `Khởi tạo` | Mới tạo |
| Phiếu nhập/xuất | `Đã giao hàng` | Đã thực hiện nhập/xuất |
| Hóa đơn | `InvoiceInProcess` | Đang xử lý |
| Hóa đơn | `Đã duyệt` | Đã duyệt, sinh acctg_trans |
| Hóa đơn | `Đã hoàn thành` | Đã thanh toán xong |
| Payment | `Cam kết sẽ thanh toán` | Chờ khớp lệnh |
| Payment | `Đã thực hiện thanh toán` | Đã khớp lệnh |

---

## Actors (Tác nhân)

| Tác nhân | Quyền |
|---|---|
| NV mua sắm | Tạo PO |
| QL mua / Kế toán | Duyệt hoặc từ chối PO |
| NV kho | Nhìn thấy PO đã duyệt, tạo và thực hiện phiếu nhập/xuất |
| NV bán hàng | Tạo SO |
| QL bán hàng / Kế toán | Duyệt hoặc từ chối SO |
| Delivery Admin | Tạo delivery_plan, phân công chuyến cho shipper |
| Shipper | Xem chuyến được phân công, ghi nhận giao hàng |
| NV kế toán | Tạo payment, khớp lệnh, phân bổ chi phí |

---

## Màn hình quan trọng

Xem mô tả đầy đủ các màn hình tại: `references/screens.md`

**Tóm tắt nhanh:**
- **Tạo PO**: Chọn NCC, kho, sản phẩm, số lượng, đơn giá
- **Chi tiết PO**: Tabs: Tổng quan / Thông tin chung / Sản phẩm / Phiếu nhập / Thanh toán / Lịch sử / Trả lại
- **Chi tiết Phiếu nhập**: Nhập SL thực nhận, số lô, ngày SX, HSD → bấm "Nhập hàng"
- **Chi tiết SO**: Tabs: Tổng quan / Thông tin chung / Thanh toán / Sản phẩm
- **Chi tiết Phiếu xuất**: Nhập SL thực xuất → bấm "Xuất hàng"
- **Đợt giao hàng**: Tabs: Thông tin chung / DS Vận đơn / DS Shipper / DS Chuyến
- **Chi tiết Hóa đơn**: Tabs: Tổng quan / Giao dịch / Thanh toán / Đơn hàng / Phiếu nhập(xuất)
- **Chi tiết Phiếu nhập (KT)**: Tabs: Tổng quan / Phân bổ chi phí

---

## Quy tắc nghiệp vụ quan trọng

1. **1 PO/SO → nhiều phiếu** (nhập/xuất nhiều lần)
2. **1 phiếu xuất → 1 hoặc nhiều vận đơn** (nếu quá lớn thì tách)
3. **Phân bổ chi phí**: Kho trong phiếu nhập phải **khớp** với kho trong hóa đơn chi phí
4. **acctg_trans tạo ra khi**:
   - Nhập hàng: khi bấm "Nhập hàng" (tạo cả invoice lẫn trans)
   - Xuất hàng: khi bấm "Xuất hàng" (tạo invoice + trans ngay; asset giảm sau khi giao xong)
   - Duyệt hóa đơn chi phí: mới sinh trans
   - Khớp lệnh payment: sinh trans thanh toán
5. **NV kho chỉ thấy PO/SO ở trạng thái `OrderApproved`**
6. **Một Payment có thể khớp lệnh nhiều lần** (PaymentApplication)

---

## Hướng dẫn sử dụng skill này

Khi trả lời câu hỏi liên quan đến hệ thống này:
- Dùng **đúng tên entity** (order_header, shipment, acctg_trans...)
- Dùng **đúng tên status** (OrderApproved, InvoiceInProcess...)
- Nếu thiết kế DB: tham chiếu các bảng đã có trong hệ thống
- Nếu viết code UI: tham chiếu cấu trúc màn hình trong `references/screens.md`
- Nếu câu hỏi về luồng phức tạp: đọc `references/workflows.md` để có đủ chi tiết