# System Map — Smart Supply Chain (bản đồ hệ thống để ôn phản biện)

> 1-pager để nắm nhanh toàn hệ thống: kiến trúc → module → các luồng kỹ thuật đáng giá → file phải biết.
> Mọi mục đều trỏ tới **file/method thật**. Backend gốc: `backend/src/main/java/com/distribution/`.

## 1. Kiến trúc & stack
- **3 lớp:** Controller (REST) → Service (`service/` + `service/impl/`) → Repository (Spring Data JPA) → PostgreSQL.
- **Backend:** Spring Boot 3.1.4, Java 17, Spring Security + JWT (HS256), JPA/Hibernate, Flyway (V1–V10, *apply tay bằng psql* vì pom thiếu `flyway-core`).
- **Frontend:** React 18 + Vite 5 + Ant Design 5 + Axios; deploy Vercel (FE) + Render (BE+DB).
- **Test:** H2 in-memory, profile `test` (`@ActiveProfiles("test")` + `application-test.yml`). 4 test: `AccountingServiceTest`, `RbacSecurityTest`, `CoreFlowTest` (context-load), `ConcurrencyLotTest` (mới).

## 2. 9 module (class lõi → file)
| Module | Class chính | Vai trò |
|---|---|---|
| Master data | `Product`, `Supplier`, `Customer`, `Warehouse` (`model/`) | Dữ liệu nền |
| Mua hàng | `PurchaseOrder(Item)`, `service/impl/PurchaseOrderServiceImpl` | Đặt hàng NCC |
| Nhập kho | `GoodsReceipt`, `service/impl/GoodsReceiptServiceImpl` | Nhận hàng → tạo `InventoryLot` |
| **Tồn kho (lõi)** | `Inventory`, `InventoryLot`, `service/impl/InventoryServiceImpl`, `repository/InventoryLotRepository` | Tồn 2 mức (on-hand/reserved) + tồn theo lô |
| Bán hàng | `SalesOrder`, `service/impl/SalesOrderServiceImpl` | Duyệt đơn → giữ chỗ (reserve) |
| **Xuất kho** | `GoodsIssue`, `service/impl/GoodsIssueServiceImpl` | FEFO + khóa concurrency + hoàn hàng giao lỗi |
| Giao hàng | `DeliveryTripRoute`, `service/impl/DeliveryTripServiceImpl`, `DeliveryOrderService` | Chia chuyến (round-robin), waybill |
| Kế toán | `AccountingTransaction`, `Payment`, `service/impl/AccountingServiceImpl` | Ghi sổ kép, sổ cái, công nợ |
| Dashboard | `service/impl/DashboardServiceImpl` | Tổng hợp theo từng cụm |
| Bảo mật (xuyên suốt) | `config/SecurityConfig`, `security/JwtAuthenticationFilter`, `aspect/DataFilterAspect` | RBAC 3 tầng |

## 3. 7 luồng kỹ thuật đáng giá (file:method → cách hoạt động)
1. **FEFO xuất theo lô** — `GoodsIssueServiceImpl.confirm()` (vòng lô ~dòng 288–356) + `InventoryLotRepository.findAvailableLotsFEFO` (`ORDER BY expiryDate ASC NULLS LAST`). Trừ tham lam từ lô hết hạn sớm nhất; ghi lô đầu tiên lên dòng phiếu. Chặn xuất nếu chỉ còn lô hết HSD (`hasLotsWithStock`).
2. **Khóa chống lost-update cấp lô** — `GoodsIssueServiceImpl.confirm()` (dòng 246–262) gọi `InventoryServiceImpl.lockInventoryForUpdate` → `InventoryRepository.findByProductIdAndWarehouseIdForUpdate` `@Lock(PESSIMISTIC_WRITE)`. Khóa DÒNG Inventory = "cổng" serialize cho vòng trừ lô (lô không có @Version). Khóa theo `productId` tăng dần để tránh deadlock. **Bằng chứng:** `ConcurrencyLotTest`.
3. **Giữ chỗ tồn khi duyệt đơn bán** — `SalesOrderServiceImpl.approve()` → `inventoryService.reserveInventory` → `Inventory.reserveStock`. `available = onHand − reserved`; duyệt đơn chỉ giữ chỗ, lúc xuất mới trừ on-hand thật → chống bán quá.
4. **Hoàn hàng khi giao thất bại** — `DeliveryTripServiceImpl.syncDeliveryOrderOutcome` → `GoodsIssueServiceImpl.restockFromFailedDelivery`: hoàn on-hand (`addStock`) + hoàn lô (`restockLot`) + trừ lại `deliveredQuantity` + `cancelInvoiceForFailedDelivery` (chỉ hủy hóa đơn nếu **chưa thu tiền**, đã thu thì cảnh báo cho kế toán).
5. **RBAC 3 tầng** — endpoint (`SecurityConfig` rule URL theo role) + method (`@PreAuthorize`) + data (`DataFilterAspect` AOP + service check, vd shipper chỉ thấy chuyến của mình). Quyền nạp từ DB mỗi request (`loadUserByUsername`), KHÔNG đọc từ claim token → mất quyền có hiệu lực ngay.
6. **Sửa N+1 (2380ms → 47ms)** — `DeliveryOrderService.listAvailable` (commit `20d711f0`): nạp sẵn map/set + `GoodsIssueRepository.findConfirmedWithDetails` dùng `JOIN FETCH` thay vì query lặp.
7. **Ghi sổ kép tự động** — `AccountingServiceImpl.post()` (Nợ/Có) tự ghi khi GR/Invoice/Payment; `getLedger()` tính số dư lũy kế theo tài khoản.

## 4. File "phải biết" cho buổi phản biện
- `config/SecurityConfig.java` — rule URL, CORS, tắt CSRF (vì JWT stateless).
- `security/JwtAuthenticationFilter.java` — verify token → set SecurityContext mỗi request.
- `service/impl/GoodsIssueServiceImpl.java` → `confirm()` — FEFO + khóa + tạo hóa đơn.
- `repository/InventoryLotRepository.java` → `findAvailableLotsFEFO`.
- `service/impl/InventoryServiceImpl.java` → `lockInventoryForUpdate`, `decreaseInventory`, `reserveStock`.
- `service/impl/AccountingServiceImpl.java` → `post()`, `getLedger()`.
- `aspect/DataFilterAspect.java` — lọc dữ liệu theo role.
- `backend/src/test/java/com/distribution/integration/ConcurrencyLotTest.java` — test khóa cấp lô.

## 5. Chạy & test
```bash
# Backend (cần PostgreSQL): mvn -f backend/pom.xml spring-boot:run   → http://localhost:8080
# Frontend:                  (trong frontend/) npm install && npm run dev → http://localhost:5173
# Test (H2, không cần DB):   mvn -f backend/pom.xml test
# Một test:                  mvn -f backend/pom.xml -Dtest=ConcurrencyLotTest test
```
