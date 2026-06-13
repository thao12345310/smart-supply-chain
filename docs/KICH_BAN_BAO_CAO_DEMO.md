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
mvn spring-boot:run        # hoặc: mvn spring-boot:run

# Terminal 2 – Frontend (React + Vite)
cd frontend
npm run dev      # mở http://localhost:5173
```

- Kiểm tra DB PostgreSQL đã chạy, các migration **V1→V7** đã apply (Flyway tự chạy khi khởi động backend).
- Mở sẵn trình duyệt 2 cửa sổ ở chế độ ẩn danh để demo song song nhiều vai trò (vd. NV bán hàng và Quản lý).

### 0.2. Tài khoản demo (đã có sẵn trong DB qua seed data)


| Vai trò        | Username                | Password      | Dùng để demo                                  |
| -------------- | ----------------------- | ------------- | --------------------------------------------- |
| Quản trị viên  | `admin`                 | `admin123`    | Quản lý người dùng, phân quyền                |
| NV Mua hàng    | `purchase_staff`        | `password123` | Tạo PO                                        |
| QL Mua hàng    | `purchase_manager`      | `password123` | Duyệt PO                                      |
| NV Bán hàng    | `sales_staff`           | `password123` | Tạo SO                                        |
| QL Bán hàng    | `sales_manager`         | `password123` | Duyệt SO                                      |
| NV Kho         | `warehouse_staff`       | `password123` | Nhập/Xuất kho                                 |
| Admin Giao vận | `delivery_admin`        | `password123` | Lập đợt/chuyến giao hàng                      |
| Shipper        | `shipper1` / `shipper2` | `password123` | Xem chuyến được giao, xác nhận giao từng điểm |
| Kế toán        | `accountant`            | `password123` | Xem công nợ, duyệt đơn                        |


### 0.3. Checklist "an toàn demo"

- Chuẩn bị sẵn **1 sản phẩm có hạn sử dụng** (để demo FEFO) và **tồn kho > 0**.
- Có sẵn ít nhất 1 Khách hàng, 1 Nhà cung cấp, 1 Kho.
- Backup DB trước khi demo (đã có `backup_before_tip02.sql`) – nếu demo lỗi có thể restore.
- Tắt thông báo, phóng to trình duyệt, zoom 110–125% cho dễ nhìn trên máy chiếu.
- Mở sẵn tab Dashboard để "chốt hạ" cuối buổi.

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
│  Ant Design   │    JSON over HTTPS   │  (Service + RBAC)  │  Hibernate │ (Flyway V1-V7)│
└──────────────┘                       └────────────────────┘            └──────────────┘
```

**Điểm nhấn kỹ thuật cần nói:**

- **Backend**: Spring Boot, kiến trúc phân lớp Controller → Service → Repository (JPA/Hibernate). Bảo mật bằng **JWT + Role-Based Access Control (9 vai trò)**.
- **Frontend**: React + Vite + Ant Design, responsive, gọi API qua lớp `services/api.js` tập trung.
- **CSDL**: PostgreSQL, quản lý phiên bản schema bằng **Flyway migration** (V1 Mua hàng → V7), seed data sẵn cho demo. Hibernate để `ddl-auto=validate` (chỉ đối chiếu entity với schema, **không tự sửa DB** — toàn bộ schema do Flyway kiểm soát).
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
- Khi nhập số lượng, ngay dưới dòng sản phẩm hệ thống hiển thị **"Tồn khả dụng: X"**; nếu nhập vượt tồn sẽ chuyển **chữ đỏ "— không đủ hàng"** (cảnh báo realtime ở frontend).
- Lưu → SO trạng thái **Chờ duyệt**.
  > *Lưu ý khi demo:* cảnh báo ở bước nhập là **cảnh báo mềm** (vẫn lưu nháp được). **Chốt chặn cứng** nằm ở bước **Duyệt** (`approve`): hệ thống kiểm tra lại tồn khả dụng và **từ chối duyệt** nếu không đủ — đây là điểm chống bán âm kho thực sự. Có thể demo bằng cách tạo SO vượt tồn rồi cho QL bấm Duyệt để thấy hệ thống chặn.

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

### CASE 3 – Luồng GIAO HÀNG: Lập đợt → Gom vận đơn → Chia chuyến → Shipper xác nhận từng điểm (2–3 phút)

**B1. Admin Giao vận lập Đợt giao hàng (Delivery Plan)** — `delivery_admin`

- Menu **Giao hàng → Kế hoạch giao hàng** (`/delivery-plans`) → **Thêm mới**: tạo `DeliveryPlan`.
- Mở chi tiết đợt — màn hình có 4 tab và thẻ thống kê (Số vận đơn / Số NV giao hàng / Số chuyến xe / Ngày tạo):
  - **Tab Vận đơn → Thêm vận đơn**: chọn vận đơn cần giao. *Lưu ý:* danh sách vận đơn được sinh **tự động từ các Phiếu xuất kho đã xác nhận ở Case 2** và **chỉ hiện những vận đơn chưa thuộc đợt giao nào** (đã giao/đã phân chuyến sẽ bị loại khỏi danh sách). Mỗi dòng hiển thị rõ **mã vận đơn (mã đơn bán) — khách hàng** — minh chứng dữ liệu liên thông từ đơn đã bán sang giao hàng, không phải chọn đơn bất kỳ.
  - **Tab Nhân viên GH → Thêm nhân viên**: thêm `shipper1` (và `shipper2` nếu muốn cho thấy việc chia nhiều chuyến).
  - **Tab Chuyến xe**: bấm **Tự động tạo chuyến** — hệ thống tự chia các vận đơn cho từng shipper của đợt (mỗi shipper một chuyến). *(Hoặc dùng **Thêm thủ công** để chọn shipper + vận đơn cụ thể.)*
    > *Nói:* "Admin chỉ cần gom vận đơn và chọn shipper, hệ thống tự chia chuyến — giảm thao tác thủ công."

**B2. Shipper xác nhận giao theo từng điểm** — `shipper1`

- Menu **Giao hàng → Chuyến giao hàng** (`/assigned-trips`): shipper thấy thẻ thống kê (Tổng chuyến / Đang giao / Đã hoàn thành) và danh sách chuyến được phân công.
- Mở rộng một chuyến → bảng **các điểm giao** (số thứ tự, mã vận đơn, khách hàng, địa chỉ, hàng hóa, tình trạng).
- Với mỗi điểm, bấm **Thành công** (hoặc **Thất bại**) → **thanh tiến độ** của chuyến cập nhật (vd. 1/2 → 2/2), khi giao đủ chuyến tự chuyển trạng thái **Hoàn thành**.
  > **Câu chốt:** "Chặng giao hàng cuối được số hóa tới từng điểm giao — admin biết chính xác đơn nào đã giao, đơn nào thất bại và tiến độ của từng chuyến theo thời gian thực."

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

- **Phân hệ Kế toán – Tài chính**: hiện đã làm phần *kế toán bán hàng / công nợ phải thu*, còn phần *kế toán tổng hợp* sẽ phát triển tiếp (chi tiết ở mục 4.1 bên dưới).
- Bổ sung **dự báo nhu cầu (demand forecasting)** và **gợi ý đặt hàng tự động** bằng AI — đúng tinh thần "thông minh".
- Tối ưu **định tuyến giao hàng** cho shipper.
- Ứng dụng **mobile** cho shipper.

### 4.1. Phân hệ Kế toán – Tài chính: hiện trạng & lộ trình phát triển

> *Cách trình bày trước hội đồng:* "Em không bỏ trống phần kế toán — em đã hoàn thiện **trục phải thu (Accounts Receivable)** vì nó gắn trực tiếp với luồng bán hàng, và đã thiết kế dữ liệu (giá vốn theo lô) làm **nền cho kế toán tổng hợp** ở giai đoạn sau."

**A. Hiện tại đã có (đang chạy được, có thể demo):**

- **Hóa đơn bán hàng (`SalesInvoice`)** sinh theo đơn bán/phiếu xuất, có đủ: tạm tính, **thuế GTGT đầu ra**, chiết khấu, phí vận chuyển, tổng tiền.
- **Theo dõi công nợ phải thu**: mỗi hóa đơn có `paid_amount` / `remaining_amount`; hàm `recordPayment()` cho phép **thu tiền nhiều lần (trả góp/một phần)** → trạng thái tự chuyển *Chưa thanh toán → Một phần → Đã thanh toán*.
- **Tự động gắn cờ quá hạn**: một **scheduler chạy 01:00 mỗi ngày** quét hóa đơn quá `due_date` chưa trả đủ → đánh dấu **OVERDUE**.
- **Báo cáo công nợ** trên Dashboard (tab 3): tổng phải thu, hóa đơn quá hạn, chi tiết theo khách hàng.
- **Giá vốn theo lô** (`unit_cost` ở `inventory_lot`) → tính được **giá trị tồn kho** và giá vốn bình quân — dữ liệu nền để dựng bút toán giá vốn sau này.

**B. Còn thiếu / sẽ phát triển (đây là phần "tạm hoãn"):**


| Hạng mục                                                 | Hiện trạng                                                         | Hướng phát triển                                                                                             |
| -------------------------------------------------------- | ------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------ |
| **Công nợ phải trả (Accounts Payable)**                  | Mới có phía bán; chưa có hóa đơn mua & theo dõi nợ nhà cung cấp    | Thêm `PurchaseInvoice` + thanh toán cho NCC, đối xứng với phía bán                                           |
| **Bút toán kép & Sổ cái (Double-entry, General Ledger)** | Chưa có hệ thống tài khoản (chart of accounts), chưa sinh bút toán | Mỗi nghiệp vụ (nhập, xuất, bán, thu/chi) **tự sinh cặp bút toán Nợ/Có** vào sổ cái → đảm bảo cân đối kế toán |
| **Bút toán giá vốn (COGS)**                              | Đã có dữ liệu `unit_cost` theo lô nhưng chưa hạch toán             | Khi xuất bán, tự ghi *Nợ Giá vốn / Có Hàng tồn kho* theo đúng lô FEFO đã trừ                                 |
| **Giao dịch thanh toán & khớp lệnh**                     | Mới lưu `paid_amount`, chưa có bảng giao dịch riêng                | Bảng `Payment` độc lập, **đối soát (reconciliation)** với sao kê ngân hàng, hỗ trợ nhiều phương thức         |
| **Thuế**                                                 | Mới lưu thuế đầu ra trên hóa đơn                                   | Quản lý **thuế GTGT đầu vào/đầu ra**, kết xuất **tờ khai thuế**                                              |
| **Báo cáo tài chính**                                    | Mới có báo cáo công nợ & tồn kho                                   | **Kết quả kinh doanh (P&L), Cân đối kế toán, Lưu chuyển tiền tệ** sinh tự động từ sổ cái                     |


> *Câu chốt nếu bị hỏi "sao chưa làm kế toán đầy đủ?":* "Kế toán tổng hợp (sổ cái, bút toán kép, báo cáo tài chính) là một phân hệ lớn và đòi hỏi chuẩn mực kế toán riêng. Trong phạm vi đồ án, em ưu tiên **số hóa thông suốt chuỗi cung ứng và phần công nợ gắn trực tiếp với bán hàng** trước; đồng thời đã chuẩn bị sẵn dữ liệu giá vốn theo lô để khi mở rộng có thể sinh bút toán tự động mà không phải sửa lại mô hình dữ liệu."

---

## PHẦN 5 – DỰ PHÒNG CÂU HỎI HỘI ĐỒNG (Q&A)


| Câu hỏi có thể gặp                                            | Hướng trả lời                                                                                                                                                                                                                                                                                                                                                                                            |
| ------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| "FEFO được triển khai thế nào?"                               | Bảng `inventory_lot` lưu từng lô + HSD; có index `idx_inv_lot_fefo (product, warehouse, expiry_date ASC)`. Khi xuất kho, service truy vấn lô còn hàng sắp xếp theo HSD tăng dần và trừ dần.                                                                                                                                                                                                              |
| "Đảm bảo tồn kho không bị âm / sai khi nhiều người thao tác?" | Kiểm tra tồn khả dụng (ATP) khi tạo SO; cập nhật tồn trong transaction; mọi biến động ghi vào `InventoryTransaction` để truy vết & đối soát.                                                                                                                                                                                                                                                             |
| "Phân quyền chặn ở đâu, chỉ ở giao diện?"                     | Chặn cả 2 lớp: ẩn menu ở frontend **và** kiểm tra vai trò ở API backend (Spring Security), nên không thể gọi thẳng API để vượt quyền.                                                                                                                                                                                                                                                                    |
| "Vì sao tách Đơn mua và Phiếu nhập?"                          | Một PO có thể nhập nhiều lần (nhập thiếu rồi bổ sung); tách giúp theo dõi tiến độ nhận hàng thực tế và xử lý nhập một phần.                                                                                                                                                                                                                                                                              |
| "Dữ liệu báo cáo lấy real-time hay tính trước?"               | Tính trực tiếp từ DB qua `DashboardService` mỗi lần truy vấn, đảm bảo phản ánh đúng số liệu hiện tại.                                                                                                                                                                                                                                                                                                    |
| "Vận đơn trong đợt giao hàng lấy từ đâu?"                     | Sinh tự động từ các **Phiếu xuất kho đã xác nhận** (`DeliveryOrderService` đọc Goods Issue) — tức các **đơn đã thực sự bán & xuất kho**, mỗi vận đơn liên kết tới đúng đơn bán hàng (SalesOrder) và hiển thị kèm mã đơn bán. Danh sách **tự loại các vận đơn đã thuộc đợt giao khác**, nên admin chỉ thấy đơn còn cần giao chứ không phải chọn đơn bất kỳ — dữ liệu nối liền từ bán hàng sang giao hàng. |
| "Chia chuyến cho shipper thế nào?"                            | Admin thêm shipper vào đợt rồi bấm **Tự động tạo chuyến** (chia đều vận đơn, mỗi shipper một chuyến), hoặc tạo thủ công chọn shipper + vận đơn. Shipper xác nhận **từng điểm giao**; tiến độ và trạng thái chuyến tự cập nhật theo số điểm đã giao.                                                                                                                                                      |
| "Công nghệ này có mở rộng được không?"                        | Kiến trúc phân lớp + REST API tách biệt FE/BE, schema quản lý bằng Flyway → dễ thêm phân hệ, dễ scale backend độc lập.                                                                                                                                                                                                                                                                                   |


### 5.1. Câu hỏi KỸ THUẬT chuyên sâu (hội đồng thường "khoan" ở buổi cuối)


| Câu hỏi có thể gặp                                                        | Hướng trả lời                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| ------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| "Migration tới V mấy rồi? V7 fix gì?"                                     | Hiện có **V1→V7**. V7 sửa một **khóa ngoại sai** của `delivery_order.sales_order_id`: giai đoạn đầu em lỡ để Hibernate `ddl-auto=update` nên nó sinh FK trỏ nhầm sang `purchase_order`. Em đã (1) viết migration V7 gỡ FK sai, tạo lại FK đúng trỏ sang `sales_order`, và (2) chuyển `ddl-auto` về `**validate`** để từ nay **chỉ Flyway** được phép đổi schema, Hibernate chỉ đối chiếu. Migration viết **idempotent** (chạy lại nhiều lần vẫn an toàn). |
| "Vì sao dùng cả Flyway lẫn Hibernate? Có mâu thuẫn không?"                | Không. Flyway là **nguồn sự thật duy nhất** của schema (versioned, có thể rollback theo version). `ddl-auto=validate` chỉ để Hibernate **báo lỗi sớm** nếu entity Java lệch với bảng thật — như một lớp kiểm tra, không tự ý sửa DB.                                                                                                                                                                                                                      |
| "Tồn kho có bị sai khi 2 người cùng xuất một lúc không? (race condition)" | (1) Bảng `inventory` có cột `**@Version` (optimistic locking)** — nếu 2 giao dịch sửa cùng bản ghi, giao dịch sau bị `OptimisticLockException` và phải thử lại, không ghi đè mù. (2) Mỗi thao tác chạy trong `**@Transactional`**, có **kiểm tra ràng buộc `CHECK (quantity_remaining >= 0)`** ở DB nên không thể âm kho. (3) Mọi biến động ghi `InventoryTransaction` để đối soát.                                                                       |
| "FEFO xử lý lô **không có hạn sử dụng** (NULL) thế nào?"                  | Query FEFO sắp xếp `CASE WHEN expiryDate IS NULL THEN 1 ELSE 0 END, expiryDate ASC, id ASC` — tức **lô có HSD gần nhất xuất trước**, lô không HSD **đẩy xuống cuối**. Tránh giữ lại hàng sắp hết hạn.                                                                                                                                                                                                                                                     |
| "Reserve (giữ tồn) khác Deduct (trừ tồn) thế nào?"                        | Khi **duyệt SO**, hệ thống *reserve* (giữ chỗ) phần tồn để không ai bán trùng. Tồn vật lý chỉ thực sự **giảm khi Phiếu xuất kho (Goods Issue) được xác nhận** — lúc đó mới trừ vào `inventory` và `inventory_lot` theo FEFO. Tách rõ "đã hứa bán" và "đã xuất thật".                                                                                                                                                                                      |
| "Nếu xuất kho rồi mà hủy/hoàn phiếu thì tồn lô có về đúng không?"         | Có. Logic hoàn (`GoodsIssueServiceImpl`) **cộng trả lại đúng lô đã trừ** nếu lô còn tồn tại; nếu không khớp được lô gốc thì cộng vào **lô FEFO sớm nhất**. Tồn tổng và tồn lô luôn cân.                                                                                                                                                                                                                                                                   |
| "Mật khẩu lưu thế nào?"                                                   | Hash **BCrypt** (cost 10), không lưu plaintext. JWT ký phía server, FE đính kèm token ở header mỗi request.                                                                                                                                                                                                                                                                                                                                               |
| "Phân quyền ở backend cụ thể ra sao, không chỉ ẩn menu?"                  | Mỗi vai trò là `RoleType` (9 vai trò) với các method kiểm tra quyền (`canApprovePurchaseOrder`, `canManageWarehouse`…). Spring Security chặn ở tầng API; gọi thẳng API không đúng quyền sẽ bị **403**, không vượt được bằng cách ẩn/hiện menu.                                                                                                                                                                                                            |


### 5.2. Câu hỏi về phần GIAO HÀNG & BÁO CÁO (phần mới demo lần này)


| Câu hỏi có thể gặp                                           | Hướng trả lời                                                                                                                                                                                                                                  |
| ------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| "Một vận đơn ứng với cái gì? Tại sao không phải là đơn bán?" | Mỗi vận đơn được **vật chất hóa 1–1 từ một Phiếu xuất kho đã xác nhận** (keyed theo mã Goods Issue), và liên kết ngược về đúng `SalesOrder`. Nghĩa là **chỉ hàng đã thực sự xuất kho mới được đem đi giao**, không phải đơn còn nằm trên giấy. |
| "Tự động chia chuyến theo thuật toán gì?"                    | Bản hiện tại chia **đều theo round-robin: mỗi shipper trong đợt một chuyến**, gom các vận đơn cho shipper đó. Em **chưa** tối ưu quãng đường — đó là hướng phát triển (định tuyến VRP) em ghi ở phần kết luận.                                 |
| "Trạng thái chuyến cập nhật thế nào?"                        | Shipper xác nhận **từng điểm giao** (Thành công/Thất bại). Hệ thống đếm số điểm đã xử lý → cập nhật **thanh tiến độ** (vd 1/2 → 2/2); khi mọi điểm đã giao xong, chuyến tự chuyển **Hoàn thành**.                                              |
| "Số liệu Dashboard tính lúc nào — có cache không?"           | Tính **trực tiếp từ DB** qua `DashboardService` mỗi lần mở (không cache), nên luôn khớp dữ liệu vừa thao tác. Với dữ liệu lớn có thể thêm cache/materialized view sau — là đánh đổi hiệu năng/độ tươi.                                         |
| "Giá trị tồn kho và giá vốn tính theo phương pháp nào?"      | Mỗi lô lưu `unit_cost`; giá trị tồn = Σ(tồn lô × giá vốn lô), giá vốn TB là bình quân theo lô. Đây cũng là nền cho bút toán giá vốn ở phân hệ kế toán (hướng phát triển).                                                                      |
| "Vì sao Kế toán (`accountant`) lại duyệt được cả PO và SO?"  | Theo thiết kế quyền, Kế toán có quyền **phê duyệt đơn và xem dữ liệu tài chính** (`canApprove()=true`) — mô phỏng vai trò kiểm soát chi/thu ở doanh nghiệp nhỏ. Có thể tách mịn hơn nếu cần.                                                   |


### 5.3. Câu hỏi "ĐÀO" về phạm vi & hạn chế (chuẩn bị để không bị động)


| Câu hỏi có thể gặp                               | Hướng trả lời                                                                                                                                                                                                                                                                                                                              |
| ------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| "Chữ 'thông minh' (smart) thể hiện ở đâu?"       | Hiện tại nằm ở **FEFO tự động chọn lô cận hạn** (giảm thất thoát do hết hạn) và **kiểm soát tồn/công nợ real-time**. Phần AI (dự báo nhu cầu, gợi ý đặt hàng, tối ưu tuyến giao) là **hướng phát triển** em đã định hình rõ — em xin trình bày là phạm vi đồ án tập trung số hóa & thông suốt quy trình trước, làm nền dữ liệu cho AI sau. |
| "Phân hệ Kế toán dừng ở đâu?"                    | Đã có **hóa đơn bán hàng + theo dõi công nợ (đã/chưa/một phần thanh toán)** và **giá vốn theo lô**. Phần **bút toán tự động & khớp lệnh thanh toán** đang tạm hoãn — em ghi rõ ở hạn chế.                                                                                                                                                  |
| "Đã test chưa? Đảm bảo đúng bằng cách nào?"      | Em kiểm thử thủ công theo đúng **vòng đời end-to-end** này, đối chiếu số liệu Dashboard với phiếu nhập/xuất. (Nếu có viết unit/integration test thì nói thêm; nếu chưa, nói thẳng đây là hạn chế và là việc bổ sung tiếp.)                                                                                                                 |
| "Nếu mất kết nối lúc shipper đang giao thì sao?" | Hiện là web app cần mạng; thao tác xác nhận chỉ ghi nhận khi gọi API thành công. **App mobile cho shipper có offline-sync** là hướng phát triển em đã nêu.                                                                                                                                                                                 |
| "Triển khai thật cho doanh nghiệp cần gì thêm?"  | Tách FE/BE giúp deploy độc lập (Docker), thêm HTTPS, sao lưu định kỳ, phân quyền mịn hơn và audit log. Kiến trúc hiện tại **đã sẵn sàng để mở rộng** theo hướng đó.                                                                                                                                                                        |


---

## PHỤ LỤC – TRÌNH TỰ DEMO RÚT GỌN (in ra cầm tay)

1. **Login** `sales_staff` (thấy ít menu) → `admin` (thấy đủ) — *RBAC* ✅
2. `purchase_staff`: tạo **PO** → `purchase_manager`: **duyệt** → `warehouse_staff`: **nhập kho + lô/HSD** → check **Tồn kho tăng** ✅
3. `sales_staff`: tạo **SO** (thử vượt tồn → cảnh báo) → `sales_manager`: **duyệt** → `warehouse_staff`: **xuất kho (FEFO trừ lô cận date)** → **Hóa đơn** ✅
4. `delivery_admin`: lập **đợt** → **thêm vận đơn** (từ phiếu xuất) → **thêm shipper** → **Tự động tạo chuyến** → `shipper1`: **Chuyến giao hàng** → **xác nhận từng điểm** (Thành công/Thất bại, xem tiến độ) ✅
5. `admin`: **Dashboard** 3 tab (Tổng quan / Nhập-Xuất-Tồn / Công nợ) — số liệu khớp các bước trên ✅

