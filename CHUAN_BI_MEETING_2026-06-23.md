# Chuẩn bị họp online về quyển đồ án — 22h, T3 23/06/2026

> Mục tiêu buổi họp (giả định): GVHD review bản thảo + chốt việc còn lại trước khi nộp/bảo vệ.
> Mở sẵn `SOICT_DATN_Application_ENG_Template/build/main_datn.pdf` (71 trang) trong khi call.

---

## 0. Chốt số liệu để báo cáo (đọc thuộc, nói trong 30s)

- **71 trang**, 6 chương + Abstract + Appendix. Build sạch, không lỗi reference.
- **20 tài liệu tham khảo** chuẩn IEEE (đạt mốc 15–25). Không Wikipedia/slide.
- **21 sơ đồ PlantUML** + **10 screenshot app thật** (§4.3.3), đều có `\ref` + giải thích trong văn.
- **Code thật:** backend 176 class Java / **11.821 dòng** / 12 package; frontend **10.575 dòng** (đo bằng `cloc`, loại blank/comment).
- 6 đóng góp ở Chương 5, mỗi đóng góp gắn class/method thật trong repo.

## 1. Mở đầu (script ~30 giây)

> "Em báo cáo tiến độ quyển. Hiện quyển đã đủ 6 chương, 71 trang, build sạch. Nội dung bám sát code thật: mỗi claim về hệ thống đều trỏ tới class/method cụ thể. Trọng tâm là Chương 5 với 6 đóng góp, điểm nhấn là cơ chế cấp phát FEFO theo lô và phần tối ưu hiệu năng (N+1, 2380ms → 47ms). Em xin trình bày trạng thái và xin ý kiến thầy về vài điểm còn lại."

## 2. 6 đóng góp — pitch 1 câu mỗi cái (Chương 5, trang 45–53)

| # | Đóng góp | Pitch | Trang |
|---|----------|-------|-------|
| 1 | **FEFO lot allocation** | Mở rộng tồn kho tổng → `InventoryLot`; goods issue cấp phát lô hết hạn sớm nhất trước (ORDER BY expiry ASC), greedy, ghi lô/HSD lên phiếu xuất. **Điểm nhấn quyển.** | 46 |
| 2 | **Concurrency control** | Lock pessimistic (SELECT…FOR UPDATE) trên hàng Inventory tổng trước khi đọc lô, khóa theo thứ tự productId tăng dần chống deadlock → chống lost-update ở mức lô. Có test 2 luồng `ConcurrencyLotTest`. | 48 |
| 3 | **Inventory reservation** | Duyệt SO thì giữ chỗ (reserved↑, available↓), xuất kho mới trừ on-hand → chống bán vượt. | 50 |
| 4 | **Failed-delivery recovery** | `restockFromFailedDelivery` hoàn kho + lô, hủy hóa đơn (cảnh báo nếu đã thu tiền), revert trạng thái SO. | 50 |
| 5 | **RBAC 3 mức** | Endpoint (Spring Security URL) + method (`@PreAuthorize`) + data (shipper chỉ thấy trip của mình). 9 vai trò, có bảng ma trận. | 51 |
| 6 | **Khử N+1 (50×)** | Thay query/row bằng 3 bulk query + JOIN FETCH → **2380ms → 47ms** trên cùng dataset. | 52 |

## 3. ⚠️ Điểm yếu — CHỦ ĐỘNG nêu trước khi thầy hỏi (lấy từ Conclusion §6.1)

Nêu trước = chủ động, kiểm soát. Bị hỏi mới lòi ra = bị động.

> ✅ **VỪA HOÀN THIỆN (23/6):** Hạn mức công nợ (credit limit) **đã được enforce tại bước duyệt SO** —
> `SalesOrderServiceImpl.approve()` chặn đơn nếu (công nợ hiện tại + giá trị đơn) > hạn mức, trước khi giữ chỗ tồn kho.
> Có test `SalesOrderApprovalCreditTest` (2 ca: chặn đơn vượt + không reserve). Toàn bộ 15 test backend pass.
> Quyển đã cập nhật: bảng test §4.4 + bỏ mục này khỏi Future Work. → **Không còn phải nói "chưa làm".**

1. **Vẫn là prototype.** Accounting có ghi sổ kép đơn giản (mỗi bút toán 1 nợ/1 có) tự động từ chứng từ + theo dõi công nợ, **nhưng chưa có** báo cáo tài chính (BS/IS), đóng kỳ, tích hợp phần mềm kế toán ngoài.
2. **Failed-delivery recovery chưa idempotent ở mức DB** — gọi 2 lần có thể hoàn kho 2 lần; hướng khắc phục: thêm cờ recovery trên goods issue/delivery.
3. **Một số kiểm thử mới ở mức manual e2e** (goods-receipt confirm, SO cancel, failed-delivery) — kế hoạch bổ sung test tự động.
4. **Chưa có** tối ưu lộ trình giao hàng, quét barcode/QR, dự báo nhu cầu (đều đã ghi rõ ở Future Work).

## 4. Câu hỏi phản biện dự kiến + trả lời ngắn

- **FEFO khác FIFO/LIFO chỗ nào, sao chọn FEFO?** → Hàng có HSD (thực phẩm/dược/mỹ phẩm) phải xuất theo *hạn dùng sớm nhất* để giảm hủy do hết hạn, không phải theo thứ tự *nhập*. Có cite (silver1998inventory, richards2021warehouse).
- **Concurrency: sao lock hàng Inventory tổng mà không lock lô?** → Lô không có version/row-lock; hàng tổng là record chung duy nhất của mọi goods issue cùng product → khóa nó là khóa được toàn bộ read-modify-write của lô, không phải đổi schema. Đây là pessimistic offline lock (Fowler PoEAA).
- **N+1 đo thế nào, môi trường nào?** → Cùng dataset dev, cùng máy; endpoint list delivery-order khả dụng; trước 2380ms (1 query/row + lazy load), sau 3 bulk query + JOIN FETCH còn 47ms; dữ liệu trả về không đổi.
- **RBAC "3 mức" cụ thể là gì?** → endpoint (URL/HTTP method qua Spring Security), method (`@PreAuthorize` trên controller), data (service kiểm tra ngữ cảnh: warehouse không thấy đơn chưa duyệt, shipper chỉ thấy trip được giao).
- **Khác Odoo/KiotViet/SAP B1 chỗ nào?** → Hẹp hơn ERP nhưng tích hợp đúng workflow nhà phân phối (duyệt mua → nhập → reservation → FEFO xuất → giao → hóa đơn → recovery); POS bán lẻ không khớp luồng này.
- **Accounting ghi sổ kép hoạt động sao?** → Tự sinh bút toán (1 nợ/1 có) từ chứng từ nguồn + theo dõi thanh toán khách; chưa có báo cáo tài chính đầy đủ (đã nêu là hạn chế).
- **Duyệt đơn có kiểm tra công nợ khách không?** → **Có.** `approve()` kiểm tra `hasAvailableCredit(grandTotal)`: nếu công nợ hiện tại + giá trị đơn > hạn mức thì từ chối, chưa giữ chỗ tồn kho. `currentBalance` được cập nhật từ AR khi xuất/thu hóa đơn. Test: `SalesOrderApprovalCreditTest`.

## 5. Việc cần CHỐT với GVHD tối nay (chủ động hỏi)

- [ ] **Deadline nộp quyển** và **lịch bảo vệ** chính xác?
- [ ] 20 tài liệu tham khảo đã đủ chưa, có cần thêm mảng nào (FEFO/supply chain/JWT)?
- [x] ~~Credit-limit — wire vào luồng duyệt SO~~ **ĐÃ XONG 23/6** (có test, quyển đã cập nhật).
- [ ] **Phạm vi accounting** tới đâu là đủ cho quyển (giữ ở mức ghi sổ kép + công nợ, hay cần thêm)?
- [ ] Có cần bổ sung/cắt mục nào không? (Ch4 dài nhất p.21–44; Ch5 trọng tâm p.45–53.)
- [ ] Có cần làm **UI mockup §4.2.1** (Figma) tách khỏi screenshot thật không?

## 6. Checklist trước khi vào call (22h)

- [ ] Mở sẵn `build/main_datn.pdf` (rebuild lại cho chắc: `cd SOICT_DATN_Application_ENG_Template && latexmk -pdf -outdir=build main_datn.tex`).
- [ ] Bật sẵn trang: Ch5 p.45 (đóng góp), §4.3.3 p.34 (screenshot), bảng RBAC p.51.
- [ ] Mở repo để trỏ code nếu thầy hỏi: `GoodsIssueServiceImpl` (FEFO + lock), `SalesOrderServiceImpl` (reservation), `DeliveryTripServiceImpl` (recovery), commit `20d711f0` (N+1).
- [ ] Mở sẵn `ON_TAP_PHAN_BIEN_DATN.md`, `1PAGER_D1_CONCURRENCY_LO.md`, `1PAGER_SECURITY.md` để tra nhanh.
- [ ] Giấy bút ghi việc thầy giao.

## 7. Bản đồ trang nhanh (để nhảy nhanh khi call)

Ch1 p.1 · Ch2 p.4 · Ch3 p.17 · Ch4 p.21 (screenshots p.34, testing p.41, deployment p.43) · Ch5 p.45 · Ch6 p.54 · References+Appendix sau p.55.
