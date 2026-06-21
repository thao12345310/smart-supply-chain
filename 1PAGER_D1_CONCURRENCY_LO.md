# 1-PAGER · D1 — Câu khó nhất: chống lost-update khi xuất kho song song (cấp LÔ)

> Bổ trợ phần sâu cho **§4.4** trong `ON_TAP_PHAN_BIEN_DATN.md`. Mục tiêu: **nói lại được, không nhìn note.**
> Code thật để dẫn chứng:
> `GoodsIssueServiceImpl.confirm()` · `InventoryRepository.findByProductIdAndWarehouseIdForUpdate` (dòng 23, `@Lock`) ·
> `InventoryServiceImpl.lockInventoryForUpdate()` (dòng 340) + `decreaseInventory()` (dòng 351) ·
> `InventoryLotRepository.findAvailableLotsFEFO` · test `integration/ConcurrencyLotTest.java`.

---

## ⏱ Trả lời 30 giây (học thuộc cái khung này)

> "Khi hai phiếu xuất cho **cùng (sản phẩm, kho)** chạy song song, cả hai có thể cùng đọc tồn của một lô, cùng thấy đủ, rồi cùng ghi đè `quantityRemaining` → **mất cập nhật (lost update)**, oversell. Bản thân `InventoryLot` **không có khóa riêng**. Em xử lý: ngay đầu `confirm()`, **khóa PESSIMISTIC_WRITE dòng `Inventory` (sản phẩm, kho)** — dòng này thành **cổng serialize** cho toàn bộ vòng trừ lô. Khóa giữ tới hết transaction nên phiếu thứ hai phải **chờ**; khi tới lượt nó đọc lại tồn đã giảm → thấy thiếu → bị chặn, không oversell. Nếu một phiếu nhiều mặt hàng, em khóa **theo productId tăng dần** để không khóa chéo thứ tự → **không deadlock**. Có integration test chứng minh: 2 thread cùng xuất 6 từ tồn 10 → đúng **1 thành công**, lô còn **4** chứ không phải −2."

---

## 1) Lost-update là gì — kịch bản cụ thể

Một lô tồn = **10**. Hai phiếu cùng xuất **6** (6 + 6 = 12 > 10, chỉ 1 phiếu được phép).

```
         Phiếu A                     Phiếu B
T1   đọc lô: còn 10
T2                              đọc lô: còn 10        ← cùng đọc giá trị cũ
T3   10 ≥ 6 ✔ → ghi 10−6 = 4
T4                              10 ≥ 6 ✔ → ghi 10−6 = 4   ← ĐÈ mất trừ của A
```

- **Không khóa:** sổ ghi lô = 4, nhưng **CẢ HAI phiếu đều xuất** → 12 cái rời kho trong khi chỉ trừ như 6 → mất trừ của A, **oversell** (thực tế phải là −2 / phải chặn 1 phiếu).
- Gốc rễ: thao tác **read‑modify‑write** trên `quantityRemaining` **không nguyên tử**.

## 2) Vì sao `InventoryLot` dễ dính

- Tồn kho **2 cấp**: `Inventory` (tồn tổng, **1 dòng / sp / kho**) + `InventoryLot` (tồn theo **lô**, phục vụ FEFO & HSD).
- `InventoryLot.quantityRemaining` **KHÔNG có `@Version`** (optimistic) **cũng không `@Lock`** riêng → tự nó không chống được race.
- Vòng trừ FEFO lặp qua từng lô: `lot.setQuantityRemaining(remaining − take)` rồi `save` → **đúng điểm chèn** của lost‑update.

## 3) Bản vá: khóa dòng `Inventory` làm **"CỔNG" serialize**

`GoodsIssueServiceImpl.confirm()` — **trước** khi đọc/trừ lô:

```java
goodsIssue.getItems().stream()
    .map(i -> i.getProduct().getId())
    .distinct().sorted()                              // ← productId TĂNG DẦN
    .forEach(pid -> inventoryService.lockInventoryForUpdate(pid, warehouseId));
```

`lockInventoryForUpdate` → `findByProductIdAndWarehouseIdForUpdate`:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)                 // Hibernate sinh: SELECT ... FOR UPDATE
Optional<Inventory> findByProductIdAndWarehouseIdForUpdate(productId, warehouseId);
```

- Khóa đặt trên **dòng `Inventory`**, **giữ tới hết transaction** (`@Transactional` ở Service).
- Hai phiếu cùng (sp, kho): phiếu thứ hai **chờ ngay ở bước khóa**, không vào được vòng trừ lô cùng lúc ⇒ các lô bị trừ **tuần tự** **dù bản thân lô không bị khóa**. Đó chính là nghĩa **"cổng serialize"**.
- `decreaseInventory()` (trừ tồn tổng, dòng 351) cũng gọi lại `...ForUpdate` ⇒ **tái dùng đúng khóa đang giữ**, không khóa thêm lần nữa.
- Hệ quả: phiếu B tới lượt đọc lại tồn đã giảm → ném `BusinessException` "không đủ tồn" → **1 thành công, 1 bị chặn**.

## 4) Vì sao khóa theo **productId TĂNG DẦN** (chống deadlock)

Một phiếu có thể gồm **nhiều mặt hàng** ⇒ phải khóa nhiều dòng `Inventory`. Nếu mỗi phiếu khóa theo thứ tự tùy ý:

```
Phiếu A: giữ P1, xin P2
Phiếu B: giữ P2, xin P1     →  A chờ B, B chờ A  =  DEADLOCK
```

Bắt **mọi phiếu khóa theo cùng một thứ tự (productId tăng dần)** ⇒ không thể tạo **vòng chờ** ⇒ **không bao giờ deadlock**. Đây là kỹ thuật chuẩn **lock ordering** (đánh số tài nguyên, luôn chiếm theo thứ tự tăng).

## 5) Bằng chứng — `ConcurrencyLotTest` (integration, bean thật)

- Dựng tồn **10** (1 lô), `ExecutorService` 2 thread, `CountDownLatch` để **bắn cùng lúc** cùng xuất **6**.
- Khẳng định: **đúng 1 thành công, 1 thất bại**; `lot.quantityRemaining == 4` (không phải −2); on‑hand == 4.
- Javadoc test ghi thẳng: **bỏ lời gọi `lockInventoryForUpdate` → race tái xuất hiện, test HỎNG** — đó là bằng chứng khóa là cần thiết.

## 6) Câu "xoáy" hay gặp → trả lời gọn

| Hỏi | Đáp |
|---|---|
| Sao không **optimistic** (`@Version`)? | Tồn kho tranh chấp cao + nghiệp vụ không nên fail‑retry; nhiều lô ⇒ retry phức tạp. Pessimistic đơn giản, vùng tới hạn ngắn (đọc lô + trừ vài dòng). |
| Sao khóa **`Inventory`** mà không khóa từng **lô**? | `Inventory(sp,kho)` là **điểm hội tụ tự nhiên** của mọi thao tác tồn ⇒ **1 cổng** thay vì khóa N lô (đỡ phức tạp, khỏi phải sắp thứ tự lô). Lô được bảo vệ **gián tiếp** qua cổng. |
| Pessimistic có **chậm/nghẽn**? | Chỉ serialize các phiếu **trùng (sp, kho)** — hiếm khi đụng nhau; isolation vẫn **READ COMMITTED**, không cần SERIALIZABLE (quá đắt). |
| Phiếu B **chờ bao lâu**? | Tới khi A commit/rollback; xong B đọc giá trị mới rồi quyết định đúng. `FOR UPDATE` = **chờ**, không phải fail ngay. |
| Khác gì `synchronized` của Java? | `synchronized` chỉ trong **1 JVM**; khóa ở **PostgreSQL** vẫn đúng khi chạy **nhiều instance**. |

---

**Liên quan:** D2 (reservation/ATP lúc duyệt SO) · §4.2 FEFO · §4.4 (bản khái niệm). **Tự vẽ được** sequence: `confirm() → lock Inventory(sp,kho) → findAvailableLotsFEFO → vòng trừ lô → decreaseInventory (cùng khóa) → commit`.
