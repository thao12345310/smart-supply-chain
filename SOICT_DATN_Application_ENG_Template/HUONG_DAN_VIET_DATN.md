# Hướng dẫn viết Đồ án — Smart Supply Chain Management System

> Tài liệu này hướng dẫn **viết gì, chuẩn bị gì, vẽ sơ đồ gì**. Quyển đồ án viết bằng **tiếng Anh**.
> Template gốc (`main.tex`, `Chapter/*`) được **giữ nguyên** làm tài liệu tham chiếu hướng dẫn.
> Bạn **viết nội dung thật vào thư mục `NoiDung/`** và build bằng `main_datn.tex`.

---

## 0. Quy trình làm việc (setup đã sẵn sàng)

**Bạn viết vào:** `NoiDung/01_introduction.tex` … `06_conclusion.tex` (+ `00_abstract`, `00_acknowledgment`, `07_appendix`).
Mỗi file có sẵn khung mục + dòng `% GUIDE:` nói rõ chỗ đó viết gì cho hệ thống này, và các `[TODO]` để bạn thay.

**Build quyển đồ án:**
```bash
cd SOICT_DATN_Application_ENG_Template
latexmk -pdf -outdir=build main_datn.tex      # ra build/main_datn.pdf
```
Hoặc trong VS Code: mở `main_datn.tex` → `Cmd+S` (LaTeX Workshop tự build).

**Render lại sơ đồ sau khi sửa:**
```bash
bash Figure/diagrams/render.sh                 # src/*.puml -> Figure/diagrams/*.png
```

**Bìa & thông tin cá nhân:** sửa `Cover.tex`, `Cover2.tex` (tên, MSSV, GVHD, ngành, kỳ).
Ngành trên bìa phải đúng quy định (xem `Chapter/Appendix_A.tex` mục A.2).

**Từ viết tắt / thuật ngữ:** thêm vào `glossary.tex` (hiện ra mục LIST OF ABBREVIATIONS tự động).

---

## 1. Bản đồ nội dung từng chương (viết gì cho hệ thống của bạn)

### Chương 1 — Introduction (3–6 trang)
- **1.1 Motivation:** Nhà phân phối vừa & nhỏ vẫn quản lý mua hàng, kho, bán hàng, giao hàng bằng Excel/giấy rời rạc → sai lệch tồn kho, bán vượt, hàng cận/hết hạn (không có FEFO), không truy vết, duyệt chậm. Nêu **vấn đề**, chưa nêu giải pháp.
- **1.2 Objectives & Scope:** Tổng quan sản phẩm có sẵn (Odoo, SAP Business One, KiotViet, Sapo, MISA) → hạn chế cho nhà phân phối nhỏ → đồ án xây hệ thống web tích hợp: mua hàng, **tồn kho có FEFO theo lô**, bán hàng + hóa đơn, giao hàng, phân quyền 9 vai trò. Chỉ liệt kê **chức năng chính**.
- **1.3 Tentative Solution:** Nêu tên hướng tiếp cận (Spring Boot REST + React SPA, PostgreSQL, JWT/RBAC, thuật toán cấp phát FEFO), mô tả 1–2 câu, nêu đóng góp chính.
- **1.4 Thesis Organization:** đã viết sẵn đoạn văn mẫu (chỉ tả chương 2–6, không tả chương 1).

### Chương 2 — Requirement Survey and Analysis (9–11 trang)
- **2.1 Status Survey:** khảo sát 3 nguồn (người dùng, hệ thống hiện có, ứng dụng tương tự); **bảng so sánh** Odoo/KiotViet/Sapo/đồ án (đã có khung bảng).
- **2.2.1 General Use Case** → `usecase_general.png` ✅
- **2.2.2 Detailed Use Case** → 4 sơ đồ con (purchasing/sales/warehouse/delivery) ✅
- **2.2.3 Business Process** → `activity_procure_to_stock`, `activity_order_to_cash` ✅
- **2.3 Functional Description:** đặc tả 4–7 use case quan trọng (đã gợi ý: Confirm GI/FEFO, Approve SO, Confirm GR, Delivery Trip).
- **2.4 Non-Functional Requirements:** hiệu năng, bảo mật (JWT), độ tin cậy, kỹ thuật (PostgreSQL, REST).

### Chương 3 — Theoretical Background and Technologies (≤ 10 trang)
Với **mỗi công nghệ**: giải quyết yêu cầu nào (ở Ch.2) → các lựa chọn thay thế → lý do chọn. **Phải trích dẫn nguồn**.
Nội dung: lý thuyết tồn kho & **FEFO vs FIFO/LIFO**; Spring Boot/JPA-Hibernate/Spring Security-JWT/Flyway; React/Vite/Ant Design; PostgreSQL; JWT + RBAC.

### Chương 4 — Design, Implementation, Evaluation (chương dài nhất)
- **4.1.1** kiến trúc 3 tầng → `architecture.png` ✅
- **4.1.2** package diagram → `package_diagram.png` ✅
- **4.1.3** class diagram (chỉ tên + quan hệ) → `class_inbound/outbound/delivery.png` ✅
- **4.2.1 UI Design:** chuẩn hiển thị + **mockup** (bạn tự vẽ Figma/draw.io — KHÔNG dùng screenshot thật ở đây).
- **4.2.2 Layer Design:** class chi tiết (thuộc tính+phương thức) → `class_detail_key.png` ✅; sequence → `seq_so_gi_fefo/seq_po_gr/seq_delivery_trip.png` ✅
- **4.2.3 Database Design:** ER → `er_diagram.png` ✅
- **4.3.1 Tools:** bảng công cụ+phiên bản (**đã điền sẵn** từ pom.xml/package.json — kiểm tra lại).
- **4.3.2 Achievement:** thống kê mã nguồn (số liệu thật bên dưới mục 3).
- **4.3.3 Main Functions:** **screenshot app thật** (phải chạy app, xem mục 4).
- **4.4 Testing:** thiết kế test case cho 2–3 chức năng (đã có khung bảng).
- **4.5 Deployment:** mô hình triển khai → `deployment.png` ✅

### Chương 5 — Solution and Contribution (≥ 5 trang) — **quan trọng nhất khi chấm**
Mỗi đóng góp = (i) vấn đề, (ii) giải pháp, (iii) kết quả. Đã dựng sẵn 4 đóng góp:
1. **Cơ chế cấp phát FEFO theo lô** → `flowchart_fefo.png` ✅ (điểm nhấn của đồ án)
2. **Giữ chỗ tồn kho (reservation) khi duyệt đơn** — chống bán vượt
3. **Khôi phục khi giao thất bại** (`restockFromFailedDelivery`)
4. **Phân quyền theo vai trò** (RBAC 3 mức)
⚠️ Không lặp lại nguyên văn chi tiết đã có ở chương trước — chương trước chỉ tham chiếu xuống đây.

### Chương 6 — Conclusion and Future Work
- **6.1** so sánh kết quả với sản phẩm tương tự, làm được/chưa được, đóng góp chính.
- **6.2** hoàn thiện chức năng hiện có + hướng mới (tối ưu lộ trình giao hàng, dự báo nhu cầu, quét QR/barcode, app shipper mobile, chuyển kho, BI/báo cáo).

---

## 2. Danh sách sơ đồ — TẤT CẢ đã được tạo sẵn (PlantUML)

21 ảnh đã render trong `Figure/diagrams/`, nguồn ở `Figure/diagrams/src/*.puml`. Sửa nguồn rồi chạy `render.sh`.

| Sơ đồ | Mục | Trạng thái |
|---|---|---|
| Use case tổng quát | 2.2.1 | ✅ |
| Use case chi tiết (4 phân hệ) | 2.2.2 | ✅ |
| Activity procure-to-stock / order-to-cash | 2.2.3 | ✅ |
| Kiến trúc hệ thống | 4.1.1 | ✅ |
| Package diagram | 4.1.2 | ✅ |
| Class diagram (inbound/outbound/delivery) | 4.1.3 | ✅ |
| Class chi tiết (thuộc tính+phương thức) | 4.2.2 | ✅ |
| Sequence (FEFO / PO-GR / delivery) | 4.2.2 | ✅ |
| ER diagram | 4.2.3 | ✅ |
| State machine (PO / SO) | hỗ trợ | ✅ |
| Flowchart FEFO | Ch.5 | ✅ |
| Deployment | 4.5 | ✅ |

**Bạn tự làm (không phải PlantUML):**
- **UI mockup** (4.2.1) — Figma hoặc draw.io.
- **Bảng ma trận phân quyền RBAC** (Ch.5) — bảng LaTeX, không phải sơ đồ.

> Lưu ý: các sơ đồ được sinh từ mã nguồn nên **đúng cấu trúc**, nhưng bạn nên đọc lại & tinh chỉnh tên use case/nhãn cho khớp văn bản của mình.

---

## 3. Tài liệu / dữ liệu cần chuẩn bị (checklist)

- [ ] **Khảo sát đối thủ** (Odoo, SAP B1, KiotViet, Sapo, MISA): bảng tính năng vs hạn chế → mục 1.2 & 2.1.
- [ ] **15–25 tài liệu tham khảo** chuẩn IEEE (sách/bài báo về quản lý tồn kho, FEFO/FIFO, supply chain, Spring/JWT/React). **Không** dùng Wikipedia/slide bài giảng (quy định A.6). Thêm vào `reference.bib`, trích dẫn bằng `\cite{key}`.
- [ ] **Screenshot app thật** (mục 4.3.3): login, dashboard, tạo/duyệt PO & SO, goods issue (thể hiện FEFO), danh sách lô tồn kho + cảnh báo hết hạn, kế hoạch giao hàng, hóa đơn.
- [ ] **UI mockup** (mục 4.2.1) cho vài màn hình quan trọng.
- [ ] **Thống kê mã nguồn** (mục 4.3.2) — số liệu thật của dự án bạn:
  - Backend Java: **~157 file, ~15.461 dòng, 12 package** (số liệu 06/2026).
  - Frontend (jsx/js): **~34 file, ~10.991 dòng**.
  - Muốn bảng đẹp tách theo ngôn ngữ: `brew install cloc && cloc backend/src frontend/src`.
- [ ] **Test case** (mục 4.4) cho 3 chức năng: Confirm GI (FEFO), Approve SO (reservation), kiểm tra hạn mức tín dụng.
- [ ] **Thông số triển khai** (mục 4.5): cấu hình server, port (8080/5432/5173).
- [ ] **Từ viết tắt & thuật ngữ** → `glossary.tex`.
- [ ] **Acknowledgment** (100–150 từ) & **Abstract** (200–350 từ, 4 phần, văn xuôi liền mạch).

---

## 4. Chạy app để chụp màn hình

Backend Spring Boot (cần PostgreSQL chạy + DB `distribution_db`):
```bash
cd backend && ./mvnw spring-boot:run        # http://localhost:8080  (Swagger: /swagger-ui.html)
```
Frontend:
```bash
cd frontend && npm install && npm run dev    # http://localhost:5173
```
Đăng nhập từng vai trò để chụp các luồng. Lưu ảnh vào `Figure/` rồi chèn bằng `\includegraphics`.

---

## 5. Thứ tự viết khuyến nghị

1. **Ch.3 (Background)** — dễ viết, định hình thuật ngữ/công nghệ.
2. **Ch.2 (Survey)** — chốt yêu cầu & use case (sơ đồ đã có).
3. **Ch.4 (Design/Impl)** — phần lớn nhất; vừa viết vừa chụp screenshot, làm test.
4. **Ch.5 (Contribution)** — chắt lọc đóng góp (FEFO là điểm nhấn).
5. **Ch.1 (Introduction)** — viết sau khi đã rõ toàn bộ.
6. **Ch.6 (Conclusion)** → **Abstract** → **Acknowledgment** (viết cuối).

---

## 6. Quy tắc chất lượng (rút từ Phụ lục A — bắt buộc)

- Viết **đoạn văn hoàn chỉnh**, KHÔNG gạch đầu dòng kiểu slide. Mỗi đoạn một ý chính + phân tích.
- **Mọi hình/bảng/công thức phải được tham chiếu và giải thích** ít nhất 1 lần trong văn bản (dùng `\ref{}`). Caption hình đặt **dưới** hình, caption bảng đặt **trên** bảng.
- **Cấm đạo văn.** Trích dẫn nguồn cho mọi thứ không tự làm (hình, bảng, ý). Bị phát hiện → không được bảo vệ.
- **Văn phong khoa học, khách quan.** Tránh từ cảm tính ("tuyệt vời", "cực hay").
- Khi cần liệt kê: dùng kiểu khoa học (i), (ii), (iii) và nhất quán toàn bài.
- Nội dung các chương phải **liền mạch**: mỗi chương có đoạn mở (liên kết chương trước) và đoạn kết (tóm tắt + dẫn sang chương sau) — các file `NoiDung/` đã chừa sẵn chỗ.
- Giữ **định dạng template** — đừng đổi font/lề; đừng đổi recipe sang XeLaTeX (template dùng pdfLaTeX).

---

## 7. Ngân sách trang (tham khảo)

| Chương | Số trang |
|---|---|
| 1. Introduction | 3–6 |
| 2. Survey & Analysis | 9–11 |
| 3. Background & Technologies | ≤ 10 |
| 4. Design, Implementation, Evaluation | nhiều nhất (15–25) |
| 5. Solution & Contribution | ≥ 5 |
| 6. Conclusion & Future Work | 2–4 |

---

## 8. Chèn code vào đồ án (đã cấu hình sẵn)

`lstlisting.tex` đã cấu hình tô màu Java/SQL/JSON tiếng Việt. Ví dụ:
```latex
\begin{lstlisting}[language=Java, caption={FEFO allocation}, label={lst:fefo}]
List<InventoryLot> lots = inventoryLotRepository.findAvailableLotsFEFO(productId, warehouseId);
\end{lstlisting}
```
Hoặc thuật toán bằng `algorithm2e` (đã nạp) cho phần FEFO ở Chương 5.
