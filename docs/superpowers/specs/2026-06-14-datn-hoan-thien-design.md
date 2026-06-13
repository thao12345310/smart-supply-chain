# Spec: Hoàn thiện DATN Quản lý phân phối — 4 nhiệm vụ + Testing

- **Ngày:** 2026-06-14
- **Đề tài:** Building a software for distribution management (HUST – ITE7-02-K67, Dương Phương Thảo, 20226001)
- **Mục tiêu:** Hoàn thành đầy đủ các hạng mục trong Phiếu giao nhiệm vụ (PGNV), cụ thể bổ sung Vận đơn, Dashboard từng cụm, Đo hiệu năng, Module Kế toán (tối thiểu) và bộ Test đại diện (WP5).
- **Stack:** Spring Boot + Spring Data JPA + Spring Security (JWT/RBAC) + PostgreSQL (backend); React 18 + Vite + Ant Design 5 + Recharts + axios + react-router (frontend). **Không Flyway** — migration apply bằng `psql`.

## Bối cảnh hiện trạng

Đã có đầy đủ: Product, Customer, Supplier, Warehouse, PO → GR (nhập kho), SO → GI (xuất kho) → SalesInvoice, Inventory + InventoryLot (lô/HSD), 1 trang Dashboard tổng quan (3 tab). Giao hàng đã có `DeliveryPlan` (đợt) → `DeliveryTripRoute` (chuyến shipper) → `DeliveryOrder` (vận đơn, hiện sinh transient từ phiếu xuất, model mỏng).

Thiếu so với PGNV:
- **Kế toán** — đang "Tạm hoãn"; chỉ có stub `Invoice.java` + enum `PaymentStatus`.
- **Testing (WP5)** — không có thư mục test.
- **Đo hiệu năng** — có `spring-boot-starter-actuator` nhưng chưa cấu hình metrics.
- **Vận đơn** — chưa có trang quản lý riêng / chi tiết mặt hàng / in.

## Thứ tự triển khai (rủi ro giảm dần)

1. Module Kế toán tối thiểu (lớn nhất, đụng GR/GI/Invoice → làm trước, test kỹ)
2. Vận đơn (vừa, độc lập)
3. Dashboard từng cụm (gồm dashboard Kế toán → sau Kế toán)
4. Đo response time (làm cuối khi API ổn định)
5. Bộ test đại diện (xuyên suốt, chốt ở cuối cho WP5)

---

## Task 4 — Module Kế toán (mức tối thiểu)

**Phạm vi:** bút toán tự động + quản lý Payment + sổ cái cơ bản. KHÔNG làm chuẩn mực kế toán đầy đủ (không kỳ kế toán, không khóa sổ, không báo cáo tài chính chuẩn).

### Backend — thực thể mới
- `Account` (danh mục tài khoản) — **cố định, seed sẵn**, không cho người dùng tạo. Tập tối giản:
  - `CASH` — Tiền mặt/Ngân hàng
  - `AR` — Phải thu khách hàng
  - `AP` — Phải trả nhà cung cấp
  - `REVENUE` — Doanh thu
  - `INVENTORY` — Hàng tồn kho
  - `EXPENSE` — Chi phí / Giá vốn
- `AccountingTransaction` (bút toán) — `id, date, description, sourceType (GR|INVOICE|PAYMENT), sourceId, debitAccount, creditAccount, amount`. Mô hình **2 dòng Nợ/Có đơn giản** (1 transaction = 1 cặp Nợ/Có). Tạo tự động, không cho sửa tay.
- `Payment` — `id, code, type (RECEIPT|DISBURSEMENT), amount, paymentDate, method, salesInvoiceId (nếu RECEIPT), supplierInvoiceId/purchaseOrderId (nếu DISBURSEMENT), note`.

### Quy tắc bút toán tự động
| Sự kiện | Nợ | Có |
|---|---|---|
| GR xác nhận | INVENTORY | AP |
| GI + SalesInvoice phát hành | AR | REVENUE |
| Payment RECEIPT (thu KH) | CASH | AR |
| Payment DISBURSEMENT (chi NCC) | AP | CASH |

### Tích hợp & chính sách giao dịch
- Móc bằng cách gọi `AccountingService.post(...)` **ở cuối** các hàm xác nhận hiện có: `GoodsReceiptService` (confirm GR), `SalesInvoiceService` (phát hành invoice / hoặc `GoodsIssueService` khi GI gắn invoice), và khi tạo `Payment`.
- **Chính sách:** bút toán nằm **trong cùng transaction** với nghiệp vụ gốc → nếu post bút toán lỗi thì rollback cả nghiệp vụ (đảm bảo sổ sách luôn khớp). Ghi log rõ ràng khi post.
- Tạo `Payment` → cập nhật `PaymentStatus` của hóa đơn liên quan (PAID / PARTIALLY_PAID / UNPAID dựa trên tổng đã thu so với tổng hóa đơn).

### API
- `GET/POST /api/payments` (+ lọc theo type/ngày/hóa đơn)
- `GET /api/accounting/transactions` (lọc theo account/ngày/sourceType)
- `GET /api/accounting/ledger?account=` (sổ cái 1 tài khoản: dòng phát sinh + số dư lũy kế)

### Frontend
- `PaymentList.jsx` — danh sách + form tạo phiếu thu/chi, chọn hóa đơn cần thanh toán.
- `LedgerPage.jsx` — danh sách bút toán (Nhật ký) + xem sổ cái theo tài khoản, lọc account/ngày.
- Thêm menu nhóm **Kế toán**.

### Migration
- SQL tạo bảng `account`, `accounting_transaction`, `payment` + seed 6 account. Apply bằng `psql`.

---

## Task 1 — Vận đơn trong giao hàng

**Vấn đề:** `DeliveryOrder` sinh transient, không chi tiết mặt hàng, không trang riêng, không in.

### Backend
- Persist `DeliveryOrder` (bảng đã có) + bổ sung field: `recipientName, recipientPhone, plannedDate`, liên kết `goodsIssueId / salesOrderId / customerId` (mở rộng từ liên kết `salesOrder` hiện có).
- **Mặt hàng vận đơn lấy từ `GoodsIssueItem`** của phiếu xuất liên kết — KHÔNG tạo bảng item trùng lặp; trả qua API chi tiết.
- `GET /api/delivery-orders/{id}` — chi tiết: thông tin vận đơn + người nhận + danh sách mặt hàng (product, qty, unit).
- Mở rộng `GET /api/delivery-orders` — lọc theo `status / fromDate / toDate / customerId`.

### Frontend
- `DeliveryOrderList.jsx` — danh sách + bộ lọc; gắn vào menu **Giao hàng**.
- `DeliveryOrderDetail.jsx` — thông tin + bảng mặt hàng + timeline trạng thái (PENDING → SHIPPED → DELIVERED/FAILED, dùng `deliveryStatus.js`).
- **Phiếu in vận đơn** — component có print CSS riêng, nút "In" gọi `window.print()` (xuất PDF qua hộp thoại in trình duyệt). Không thêm dependency.

---

## Task 2 — Dashboard riêng cho từng cụm

Giữ `DashboardPage` tổng quan; thêm **5 dashboard cụm** (Recharts + Ant Design, đồng bộ style hiện tại).

| Cụm | KPI / biểu đồ |
|---|---|
| Mua hàng | PO theo trạng thái, giá trị mua theo thời gian, top NCC, GR đang chờ |
| Bán hàng | SO theo trạng thái, doanh thu theo thời gian, top KH, top sản phẩm |
| Kho & Logistics | Giá trị tồn, hàng sắp hết, lô sắp/đã hết hạn, biến động nhập-xuất |
| Giao hàng | Chuyến theo trạng thái, tỉ lệ giao thành công, vận đơn theo shipper |
| Kế toán | Phải thu/phải trả, thu-chi theo thời gian, hóa đơn quá hạn |

### Backend
- Thêm endpoint tổng hợp trong `DashboardService` / `DashboardController`:
  `/api/dashboard/purchase`, `/sales`, `/inventory`, `/delivery`, `/accounting`.
- Tận dụng repository hiện có; thêm query tổng hợp khi cần.

### Frontend
- Mỗi cụm 1 page riêng (`PurchaseDashboard.jsx`, `SalesDashboard.jsx`, `InventoryDashboard.jsx`, `DeliveryDashboard.jsx`, `AccountingDashboard.jsx`), KPI cards + charts.
- Gắn menu cạnh module tương ứng.

---

## Task 3 — Đo response time (Evaluation cho báo cáo)

### Backend
- Thêm `HandlerInterceptor` (hoặc `OncePerRequestFilter`) ghi thời gian xử lý mỗi request vào **Micrometer Timer** (tag theo URI pattern + method).
- Bật expose actuator: `management.endpoints.web.exposure.include=health,metrics` (+ cấu hình `application.properties`).
- `GET /api/metrics/summary` — trả **avg / p95 / max / count** theo từng endpoint (đọc từ MeterRegistry) để lấy số liệu vào báo cáo.

### Script tải & bảng số liệu
- 1 script (`scripts/perf/run.sh` dùng `curl`, hoặc k6 nếu đã cài) gọi các API chính N lần (login → list PO/SO/Inventory/Dashboard...).
- Xuất **bảng tổng hợp** (CSV/markdown) dán vào báo cáo WP5.

---

## Testing (WP5) — bộ đại diện, mức tối thiểu

Thêm thư mục test (`backend/src/test`):
- **Integration test luồng cốt lõi:** `PO → GR → SO → GI → Invoice → Payment`, kiểm chứng tồn kho và bút toán phát sinh đúng.
- **Security/RBAC test:** vai trò không đủ quyền gọi API bị 403.
- **Unit test:** 1–2 test cho logic tồn kho (tăng/giảm sau GR/GI) và post bút toán.

Không đặt mục tiêu coverage cao — chỉ minh chứng chất lượng cho WP5.

---

## Out of scope (YAGNI)

- Chuẩn mực kế toán đầy đủ (kỳ kế toán, khóa sổ, BCTC chuẩn VAS/IFRS).
- Bút toán nhiều dòng (>2), phân bổ, thuế GTGT chi tiết.
- Sửa/xóa bút toán thủ công.
- Thư viện PDF server-side (dùng print trình duyệt).
- Test coverage toàn diện.

## Tiêu chí hoàn thành

- [ ] Kế toán: tạo Payment thu/chi cập nhật đúng `PaymentStatus`; bút toán tự động phát sinh đúng cho GR/Invoice/Payment; sổ cái khớp.
- [ ] Vận đơn: trang danh sách + chi tiết (kèm mặt hàng) + in được phiếu.
- [ ] 5 dashboard cụm hiển thị số liệu đúng với dữ liệu thực.
- [ ] `/api/metrics/summary` trả bảng số liệu + script tải chạy được, có bảng kết quả cho báo cáo.
- [ ] Bộ test đại diện chạy xanh.
