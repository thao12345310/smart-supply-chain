# KỊCH BẢN BÁO CÁO & DEMO
## Hệ thống Quản lý Chuỗi cung ứng Thông minh (Smart Supply Chain Management)
**Đồ án tốt nghiệp – Dương Phương Thảo**

> Tổng thời lượng đề xuất: **20–25 phút** (15' thuyết trình + 8' demo + Q&A).
> Mục tiêu: Hội đồng thấy được (1) bài toán thực tế, (2) kiến trúc & công nghệ, (3) nghiệp vụ chạy thông suốt end-to-end qua demo trực tiếp.

---

## PHẦN 0 – CHUẨN BỊ TRƯỚC KHI DEMO (làm trước buổi báo cáo)

### 0.1. Khởi động hệ thống
```bash
# Terminal 1 – Backend (Spring Boot)
cd backend
./mvnw spring-boot:run        # hoặc: mvn spring-boot:run

# Terminal 2 – Frontend (React + Vite)
cd frontend
npm install      # nếu chưa cài
npm run dev      # mở http://localhost:5173
```
- Kiểm tra DB PostgreSQL đã chạy, các migration V1→V6 đã apply (Flyway tự chạy khi khởi động backend).
- Mở sẵn trình duyệt 2 cửa sổ ở chế độ ẩn danh để demo song song nhiều vai trò (vd. NV bán hàng và Quản lý).

### 0.2. Tài khoản demo (đã có sẵn trong DB qua seed data)
| Vai trò | Username | Password | Dùng để demo |
|---|---|---|---|
| Quản trị viên | `admin` | `admin123` | Quản lý người dùng, phân quyền |
| NV Mua hàng | `purchase_staff` | `password123` | Tạo PO |
| QL Mua hàng | `purchase_manager` | `password123` | Duyệt PO |
| NV Bán hàng | `sales_staff` | `password123` | Tạo SO |
| QL Bán hàng | `sales_manager` | `password123` | Duyệt SO |
| NV Kho | `warehouse_staff` | `password123` | Nhập/Xuất kho |
| Admin Giao vận | `delivery_admin` | `password123` | Lập đợt/chuyến giao hàng |
| Shipper | `shipper1` / `shipper2` | `password123` | Xác nhận giao hàng |
| Kế toán | `accountant` | `password123` | Xem công nợ, duyệt đơn |

### 0.3. Checklist "an toàn demo"
- [ ] Chuẩn bị sẵn **1 sản phẩm có hạn sử dụng** (để demo FEFO) và **tồn kho > 0**.
- [ ] Có sẵn ít nhất 1 Khách hàng, 1 Nhà cung cấp, 1 Kho.
- [ ] Backup DB trước khi demo (đã có `backup_before_tip02.sql`) – nếu demo lỗi có thể restore.
- [ ] Tắt thông báo, phóng to trình duyệt, zoom 110–125% cho dễ nhìn trên máy chiếu.
- [ ] Mở sẵn tab Dashboard để "chốt hạ" cuối buổi.

---

## PHẦN 1 – MỞ ĐẦU (2–3 phút)

**Lời mở đầu (đọc/diễn đạt):**

> "Kính thưa hội đồng, em là Dương Phương Thảo. Hôm nay em xin trình bày đồ án **Hệ thống Quản lý Chuỗi cung ứng Thông minh**.
>
> Trong các doanh nghiệp thương mại – phân phối, ba dòng chảy *mua hàng – tồn kho – bán hàng* thường bị quản lý rời rạc trên Excel hoặc nhiều phần mềm khác nhau, dẫn đến sai lệch tồn kho, khó kiểm soát công nợ và không truy vết được lô hàng/hạn sử dụng. Đồ án của em xây dựng một hệ thống **tập trung, phân quyền theo vai trò**, số hóa toàn bộ quy trình từ **Đặt mua → Nhập kho → Bán hàng → Xuất kho → Giao hàng → Báo cáo**."

**Nội dung trình bày trong phần mở đầu:**
1. **Bài toán & tính cấp thiết** – quản lý chuỗi cung ứng phân tán, sai lệch tồn kho, thất thoát do hết hạn.
2. **Mục tiêu đồ án** – một nền tảng thống nhất, real-time, có kiểm soát phân quyền và truy vết lô/hạn sử dụng (FEFO).
3. **Phạm vi** – 6 phân hệ: Danh mục, Mua hàng, Bán hàng, Kho vận, Giao hàng, Báo cáo.

---

## PHẦN 2 – KIẾN TRÚC & CÔNG NGHỆ (3–4 phút)

**Kiến trúc tổng thể (vẽ/slide):**
```
┌──────────────┐    REST API / JWT    ┌────────────────────┐    JPA     ┌──────────────┐
│  React + Vite │ ◄──────────────────► │  Spring Boot       │ ◄────────► │ PostgreSQL    │
│  Ant Design   │    JSON over HTTPS   │  (Service + RBAC)  │  Hibernate │ (Flyway V1-V6)│
└──────────────┘                       └────────────────────┘            └──────────────┘
```

**Điểm nhấn kỹ thuật cần nói:**
- **Backend**: Spring Boot, kiến trúc phân lớp Controller → Service → Repository (JPA/Hibernate). Bảo mật bằng **JWT + Role-Based Access Control (9 vai trò)**.
- **Frontend**: React + Vite + Ant Design, responsive, gọi API qua lớp `services/api.js` tập trung.
- **CSDL**: PostgreSQL, quản lý phiên bản schema bằng **Flyway migration** (V1 Mua hàng → V6), seed data sẵn cho demo.
- **Điểm khác biệt – Quản lý lô & FEFO** (TIP-02): bảng `inventory_lot` lưu từng lô theo `lot_number`, `expiry_date`, có index FEFO để **xuất hàng theo nguyên tắc cận date xuất trước** (First-Expired-First-Out).

**Các thực thể nghiệp vụ chính** (nói nhanh, chỉ vào sơ đồ):
`PurchaseOrder → GoodsReceipt → InventoryLot/Inventory → SalesOrder → GoodsIssue → SalesInvoice → DeliveryPlan/DeliveryTrip`.

---

## PHẦN 3 – DEMO NGHIỆP VỤ (8–10 phút) — PHẦN QUAN TRỌNG NHẤT

> **Thông điệp xuyên suốt:** "Em sẽ chạy một vòng đời đơn hàng hoàn chỉnh để cho thấy dữ liệu chảy liên tục giữa các phân hệ và tồn kho được cập nhật chính xác theo thời gian thực."

### CASE 0 – Đăng nhập & Phân quyền (1 phút) ⭐ *chứng minh bảo mật RBAC*
1. Đăng nhập `sales_staff` → chỉ vào màn hình tài liệu được dùng cho menu này nó **không thấy** menu Quản lý người dùng / Mua hàng.
2. Đăng xuất, đăng nhập `admin` → thấy **đầy đủ** menu, vào **Quản lý người dùng** (`/admin/users`) xem danh sách user & vai trò.
> **Câu chốt:** "Mỗi vai trò chỉ nhìn thấy và thao tác đúng phần việc của mình — đây là kiểm soát phân quyền ở cả frontend lẫn API."

---

### CASE 1 – Luồng MUA HÀNG: Đặt mua → Duyệt → Nhập kho (3 phút) ⭐ *luồng lõi*

**B1. NV Mua hàng tạo Đơn mua (PO)** — đăng nhập `purchase_staff`
- Vào **Đơn mua hàng → Tạo mới** (`/purchase-orders/new`).
- Chọn **Nhà cung cấp**, **Kho nhập**, thêm dòng sản phẩm: chọn SP, nhập **số lượng**, **đơn giá**.
- Lưu → PO ở trạng thái **Chờ duyệt (Open/Draft)**.
> *Nói:* "Ở bước này NV kho chưa nhìn thấy đơn — phải được duyệt trước."

**B2. QL Mua hàng duyệt PO** — đăng nhập `purchase_manager`
- Vào chi tiết PO vừa tạo → bấm **Duyệt** → trạng thái chuyển **Đã duyệt (Approved)**.

**B3. NV Kho nhập hàng (Goods Receipt) + nhập LÔ/HSD** — đăng nhập `warehouse_staff`
- Từ PO đã duyệt → **Tạo phiếu nhập** (`/purchase-orders/:id/receive`).
- Nhập **số lượng thực nhận**, **số lô (lot_number)**, **hạn sử dụng (expiry_date)**.
- Xác nhận nhập → hệ thống:
  - Tăng **tồn kho** (`Inventory`) của sản phẩm tại kho.
  - Tạo **lô hàng** trong `inventory_lot` với HSD tương ứng.
  - Ghi **nhật ký tồn kho** (`InventoryTransaction`) loại "Nhập".
> **Câu chốt:** "Tồn kho tự động tăng và lô hàng được ghi nhận kèm hạn sử dụng — nền tảng cho FEFO ở phần bán hàng."

**B4. Kiểm chứng** → vào **Tồn kho** (`/inventory`) chỉ rõ số lượng vừa tăng.

---

### CASE 2 – Luồng BÁN HÀNG: Đặt bán → Duyệt → Xuất kho theo FEFO → Hóa đơn (3–4 phút) ⭐⭐ *điểm nhấn FEFO*

**Chuẩn bị (tăng sức thuyết phục):** đảm bảo sản phẩm demo có **2 lô khác hạn sử dụng** (vd. Lô A HSD 30/06, Lô B HSD 31/12). Nếu chưa có, nhập thêm 1 phiếu ở Case 1 với HSD khác.

**B1. NV Bán hàng tạo Đơn bán (SO) + kiểm tra tồn khả dụng (ATP)** — `sales_staff`
- **Đơn bán hàng → Tạo mới** (`/sales-orders/new`): chọn **Khách hàng**, **Kho xuất**, thêm sản phẩm, **số lượng**, **đơn giá**.
- Khi nhập số lượng, hệ thống **kiểm tra tồn kho khả dụng** — thử nhập số lượng lớn hơn tồn để cho thấy hệ thống cảnh báo.
- Lưu → SO trạng thái **Chờ duyệt**.

**B2. QL Bán hàng duyệt SO** — `sales_manager` → bấm **Duyệt**.

**B3. NV Kho xuất hàng (Goods Issue) theo FEFO** — `warehouse_staff`
- Từ SO đã duyệt → **Tạo phiếu xuất** → xác nhận **Xuất hàng**.
- **Điểm nhấn:** hệ thống tự động trừ **lô cận date trước (Lô A HSD 30/06)** trước, đúng nguyên tắc **FEFO**.
- Kết quả: giảm tồn kho, giảm `quantity_remaining` của lô A trước, ghi `InventoryTransaction` loại "Xuất".
> **Câu chốt:** "Hệ thống tự chọn lô hết hạn sớm nhất để xuất, giúp giảm thất thoát do hàng hết hạn — đây là giá trị 'thông minh' của hệ thống."

**B4. Hóa đơn bán hàng (Sales Invoice)** — ghi nhận doanh thu
- Vào **Hóa đơn bán hàng** (`/sales-invoices`) → mở hóa đơn tương ứng SO.
- Chỉ ra trạng thái thanh toán: **Chưa thanh toán / Một phần / Đã thanh toán**.

---

### CASE 3 – Luồng GIAO HÀNG: Lập đợt → Chia chuyến → Shipper xác nhận (2 phút)

**B1. Admin Giao vận lập Đợt giao hàng** — `delivery_admin`
- **Giao hàng theo đợt → Thêm mới** (`/delivery-plans/new`): tạo `DeliveryPlan`.
- Trong chi tiết đợt: **thêm vận đơn** (từ phiếu xuất), **thêm shipper** (`shipper1`), **tạo chuyến giao hàng** (Delivery Trip).

**B2. Shipper xác nhận giao** — `shipper1`
- Shipper xem chuyến được phân công → cập nhật trạng thái **Giao thành công**.
> **Câu chốt:** "Toàn bộ chặng giao hàng cuối được số hóa, admin biết chính xác đơn nào đang ở đâu, ai giao."

---

### CASE 4 – BÁO CÁO & DASHBOARD (2 phút) ⭐ *chốt hạ bằng số liệu*

Đăng nhập `admin` hoặc `accountant` → vào **Dashboard** (`/`), demo lần lượt 3 tab:

1. **Tab Tổng quan**: thẻ thống kê **Doanh thu / Số đơn / Giá trị tồn kho / Công nợ**; **biểu đồ doanh thu** theo tháng; **Top sản phẩm bán chạy**.
   > *Nói:* "Các con số này được tính trực tiếp từ dữ liệu vừa thao tác ở các case trên — minh chứng dữ liệu liên thông."
2. **Tab Nhập–Xuất–Tồn**: chỉ ra **tồn đầu kỳ, nhập, xuất, tồn cuối kỳ** và **giá trị tồn kho** — khớp với phiếu nhập (Case 1) và phiếu xuất (Case 2).
3. **Tab Công nợ Khách hàng**: tổng công nợ, **hóa đơn quá hạn**, chi tiết từng khách hàng.

> **Câu chốt cả phần demo:** "Như hội đồng thấy, một đơn hàng đi từ lúc mua vào đến lúc giao cho khách, tồn kho và công nợ được cập nhật chính xác và phản ánh tức thời lên báo cáo — đó là mục tiêu cốt lõi mà đồ án đạt được."

---

## PHẦN 4 – KẾT LUẬN & HƯỚNG PHÁT TRIỂN (2 phút)

**Kết quả đạt được:**
- Hoàn thiện **6 phân hệ** vận hành thông suốt end-to-end (Mua → Nhập → Bán → Xuất → Giao → Báo cáo).
- **Phân quyền RBAC** 9 vai trò, bảo mật JWT.
- **Quản lý lô & FEFO** — điểm khác biệt so với các hệ thống quản lý kho cơ bản.
- Dashboard & báo cáo real-time.

**Hạn chế / Hướng phát triển:**
- Phân hệ **Kế toán – Tài chính** (bút toán tự động, khớp lệnh thanh toán) đang tạm hoãn → hoàn thiện tiếp.
- Bổ sung **dự báo nhu cầu (demand forecasting)** và **gợi ý đặt hàng tự động** bằng AI — đúng tinh thần "thông minh".
- Tối ưu **định tuyến giao hàng** cho shipper.
- Ứng dụng **mobile** cho shipper.

---

## PHẦN 5 – DỰ PHÒNG CÂU HỎI HỘI ĐỒNG (Q&A)

| Câu hỏi có thể gặp | Hướng trả lời |
|---|---|
| "FEFO được triển khai thế nào?" | Bảng `inventory_lot` lưu từng lô + HSD; có index `idx_inv_lot_fefo (product, warehouse, expiry_date ASC)`. Khi xuất kho, service truy vấn lô còn hàng sắp xếp theo HSD tăng dần và trừ dần. |
| "Đảm bảo tồn kho không bị âm / sai khi nhiều người thao tác?" | Kiểm tra tồn khả dụng (ATP) khi tạo SO; cập nhật tồn trong transaction; mọi biến động ghi vào `InventoryTransaction` để truy vết & đối soát. |
| "Phân quyền chặn ở đâu, chỉ ở giao diện?" | Chặn cả 2 lớp: ẩn menu ở frontend **và** kiểm tra vai trò ở API backend (Spring Security), nên không thể gọi thẳng API để vượt quyền. |
| "Vì sao tách Đơn mua và Phiếu nhập?" | Một PO có thể nhập nhiều lần (nhập thiếu rồi bổ sung); tách giúp theo dõi tiến độ nhận hàng thực tế và xử lý nhập một phần. |
| "Dữ liệu báo cáo lấy real-time hay tính trước?" | Tính trực tiếp từ DB qua `DashboardService` mỗi lần truy vấn, đảm bảo phản ánh đúng số liệu hiện tại. |
| "Công nghệ này có mở rộng được không?" | Kiến trúc phân lớp + REST API tách biệt FE/BE, schema quản lý bằng Flyway → dễ thêm phân hệ, dễ scale backend độc lập. |

---

## PHỤ LỤC – TRÌNH TỰ DEMO RÚT GỌN (in ra cầm tay)

1. **Login** `sales_staff` (thấy ít menu) → `admin` (thấy đủ) — *RBAC* ✅
2. `purchase_staff`: tạo **PO** → `purchase_manager`: **duyệt** → `warehouse_staff`: **nhập kho + lô/HSD** → check **Tồn kho tăng** ✅
3. `sales_staff`: tạo **SO** (thử vượt tồn → cảnh báo) → `sales_manager`: **duyệt** → `warehouse_staff`: **xuất kho (FEFO trừ lô cận date)** → **Hóa đơn** ✅
4. `delivery_admin`: lập **đợt + chuyến** → `shipper1`: **xác nhận giao** ✅
5. `admin`: **Dashboard** 3 tab (Tổng quan / Nhập-Xuất-Tồn / Công nợ) — số liệu khớp các bước trên ✅
