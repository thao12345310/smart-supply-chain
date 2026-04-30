# X-RAY REPORT — Distribution Management ĐATN

**Ngày scan**: 2026-04-30 (giờ local)
**Branch hiện tại**: main
**Commit gần nhất**: de080033 refactor: update backend compiled classes to reflect recent model, service, and controller modifications
**Tổng số file (loại trừ node_modules/target/.git/dist/build)**: 190

---

## 1. CẤU TRÚC THƯ MỤC

```
smart-supply-chain/
├── backend/
│   └── src/
│       └── main/
│           ├── java/com/distribution/
│           │   ├── config/
│           │   ├── controller/
│           │   ├── dto/
│           │   ├── exception/
│           │   ├── model/
│           │   │   └── enums/
│           │   ├── repository/
│           │   ├── security/
│           │   └── service/
│           │       └── impl/
│           └── resources/
│               └── db/migration/
├── docs/
│   └── skills/distribution-management/references/
├── frontend/
│   └── src/
│       ├── pages/      (27 files)
│       └── services/
└── XRAY_REPORT.md
```

**Kiểu repo**: Monorepo (backend + frontend trong cùng một repo)
**Đường dẫn backend**: `./backend`
**Đường dẫn frontend**: `./frontend`

---

## 2. BACKEND

### 2.1 Tech Stack
- Java version: 17
- Spring Boot version: 3.1.4
- Build tool: Maven
- Dependencies chính:
  - Spring Security: 3.1.4 (managed by Spring Boot BOM)
  - Spring Data JPA: 3.1.4 (managed by Spring Boot BOM)
  - PostgreSQL driver: 3.1.4 BOM (42.x runtime)
  - JWT library: io.jsonwebtoken jjwt 0.12.3
  - Lombok: managed by Spring Boot BOM
  - SpringDoc OpenAPI (Swagger): 2.1.0
  - Spring Boot Validation: 3.1.4 BOM
  - Spring Boot Actuator: 3.1.4 BOM

### 2.2 Cấu trúc package
```
com.distribution
├── config/          (SecurityConfig, CorsConfig, …)
├── controller/      (14 @RestController)
├── dto/             (Request/Response DTOs)
├── exception/       (GlobalExceptionHandler)
├── model/           (26 @Entity classes)
│   └── enums/       (7 enums)
├── repository/      (25 @Repository interfaces)
├── security/        (JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService)
└── service/
    └── impl/        (14 @Service implementations)
```

### 2.3 Entities (26 entities)

| Tên Entity | File | Quan hệ chính | Note |
|---|---|---|---|
| User | model/User.java | @ManyToMany Role | Auth entity |
| Role | model/Role.java | @ManyToMany User | 9 RoleType enum values |
| Product | model/Product.java | @ManyToOne Supplier | Master data |
| Supplier | model/Supplier.java | — | Master data |
| Customer | model/Customer.java | @OneToMany DeliveryAddress, @OneToMany SalesOrder | |
| DeliveryAddress | model/DeliveryAddress.java | @ManyToOne Customer, @OneToMany SalesOrder | |
| Warehouse | model/Warehouse.java | — | Master data |
| Inventory | model/Inventory.java | @ManyToOne Product, @ManyToOne Warehouse | @Version (optimistic locking); quantity-based, NO lot/expiry at entity level |
| InventoryTransaction | model/InventoryTransaction.java | @ManyToOne Product, @ManyToOne Warehouse | Audit trail only |
| PurchaseOrder | model/PurchaseOrder.java | @ManyToOne Supplier, @OneToMany PurchaseOrderItem, @OneToMany GoodsReceipt | Status enum |
| PurchaseOrderItem | model/PurchaseOrderItem.java | @ManyToOne PurchaseOrder, @ManyToOne Product | |
| GoodsReceipt | model/GoodsReceipt.java | @ManyToOne PurchaseOrder, @ManyToOne Warehouse, @OneToMany GoodsReceiptItem | |
| GoodsReceiptItem | model/GoodsReceiptItem.java | @ManyToOne GoodsReceipt, @ManyToOne Product | Has lotNumber + expiryDate fields |
| SalesOrder | model/SalesOrder.java | @ManyToOne Customer, @OneToMany SalesOrderItem, @OneToMany GoodsIssue, @OneToMany SalesInvoice | Status enum |
| SalesOrderItem | model/SalesOrderItem.java | @ManyToOne SalesOrder, @ManyToOne Product | Has deliveredQuantity |
| GoodsIssue | model/GoodsIssue.java | @ManyToOne SalesOrder, @ManyToOne Warehouse, @OneToMany GoodsIssueItem, @OneToOne SalesInvoice | |
| GoodsIssueItem | model/GoodsIssueItem.java | @ManyToOne GoodsIssue, @ManyToOne Product | Has lotNumber + expiryDate fields |
| SalesInvoice | model/SalesInvoice.java | @ManyToOne SalesOrder, @OneToOne GoodsIssue, @OneToMany SalesInvoiceItem | Status: DRAFT/FINALIZED/PAID |
| SalesInvoiceItem | model/SalesInvoiceItem.java | @ManyToOne SalesInvoice, @ManyToOne Product | |
| Invoice | model/Invoice.java | — | Legacy/reference entity (unused hoặc placeholder) |
| DeliveryPlan | model/DeliveryPlan.java | @OneToMany DeliveryPlanOrder, @OneToMany DeliveryPlanShipper | |
| DeliveryPlanOrder | model/DeliveryPlanOrder.java | @ManyToOne DeliveryPlan | |
| DeliveryPlanShipper | model/DeliveryPlanShipper.java | @ManyToOne DeliveryPlan | |
| DeliveryOrder | model/DeliveryOrder.java | — | Basic shipment info |
| DeliveryTripRoute | model/DeliveryTripRoute.java | @OneToMany DeliveryTripRouteItem | |
| DeliveryTripRouteItem | model/DeliveryTripRouteItem.java | @ManyToOne DeliveryTripRoute | |

### 2.4 REST Endpoints (75+ endpoints)

| Method | Path | Controller | Có @PreAuthorize? |
|---|---|---|---|
| POST | /api/auth/login | AuthController | No (public) |
| POST | /api/auth/register | AuthController | No (public) |
| POST | /api/auth/refresh | AuthController | No (public) |
| GET | /api/auth/me | AuthController | Yes (authenticated) |
| POST | /api/auth/logout | AuthController | Yes (authenticated) |
| POST | /api/auth/admin/register | AuthController | Yes (ADMIN) |
| GET | /api/purchase-orders | PurchaseOrderController | Yes |
| GET | /api/purchase-orders/{id} | PurchaseOrderController | Yes |
| GET | /api/purchase-orders/code/{code} | PurchaseOrderController | Yes |
| POST | /api/purchase-orders | PurchaseOrderController | Yes |
| PUT | /api/purchase-orders/{id} | PurchaseOrderController | Yes |
| DELETE | /api/purchase-orders/{id} | PurchaseOrderController | Yes |
| POST | /api/purchase-orders/{id}/approval | PurchaseOrderController | Yes |
| PUT | /api/purchase-orders/{id}/approve | PurchaseOrderController | Yes (MANAGER/ACCOUNTANT/ADMIN) |
| PUT | /api/purchase-orders/{id}/reject | PurchaseOrderController | Yes |
| PUT | /api/purchase-orders/{id}/cancel | PurchaseOrderController | Yes |
| GET | /api/purchase-orders/status/{status} | PurchaseOrderController | Yes |
| GET | /api/purchase-orders/pending-approval | PurchaseOrderController | Yes |
| GET | /api/purchase-orders/ready-for-receipt | PurchaseOrderController | Yes |
| GET | /api/purchase-orders/supplier/{id} | PurchaseOrderController | Yes |
| GET | /api/purchase-orders/date-range | PurchaseOrderController | Yes |
| GET | /api/goods-receipts | GoodsReceiptController | Yes |
| GET | /api/goods-receipts/{id} | GoodsReceiptController | Yes |
| GET | /api/goods-receipts/code/{code} | GoodsReceiptController | Yes |
| POST | /api/goods-receipts | GoodsReceiptController | Yes |
| PUT | /api/goods-receipts/{id} | GoodsReceiptController | Yes |
| DELETE | /api/goods-receipts/{id} | GoodsReceiptController | Yes |
| PUT | /api/goods-receipts/{id}/confirm | GoodsReceiptController | Yes (WAREHOUSE_STAFF/ADMIN) |
| PUT | /api/goods-receipts/{id}/cancel | GoodsReceiptController | Yes |
| GET | /api/goods-receipts/status/{status} | GoodsReceiptController | Yes |
| GET | /api/goods-receipts/purchase-order/{id} | GoodsReceiptController | Yes |
| GET | /api/goods-receipts/warehouse/{id} | GoodsReceiptController | Yes |
| GET | /api/goods-receipts/pending-confirmation | GoodsReceiptController | Yes |
| GET | /api/goods-receipts/date-range | GoodsReceiptController | Yes |
| GET | /api/goods-receipts/receiving-summary/{poId} | GoodsReceiptController | Yes |
| GET | /api/sales-orders | SalesOrderController | Yes |
| GET | /api/sales-orders/{id} | SalesOrderController | Yes |
| POST | /api/sales-orders | SalesOrderController | Yes |
| PUT | /api/sales-orders/{id} | SalesOrderController | Yes |
| DELETE | /api/sales-orders/{id} | SalesOrderController | Yes |
| POST | /api/sales-orders/{id}/approval | SalesOrderController | Yes |
| PUT | /api/sales-orders/{id}/approve | SalesOrderController | Yes (MANAGER/ACCOUNTANT/ADMIN) |
| PUT | /api/sales-orders/{id}/reject | SalesOrderController | Yes |
| PUT | /api/sales-orders/{id}/cancel | SalesOrderController | Yes |
| GET | /api/sales-orders/ready-for-issue | SalesOrderController | Yes |
| GET | /api/sales-orders/search | SalesOrderController | Yes |
| GET | /api/goods-issues | GoodsIssueController | Yes |
| GET | /api/goods-issues/{id} | GoodsIssueController | Yes |
| POST | /api/goods-issues | GoodsIssueController | Yes |
| PUT | /api/goods-issues/{id} | GoodsIssueController | Yes |
| DELETE | /api/goods-issues/{id} | GoodsIssueController | Yes |
| PUT | /api/goods-issues/{id}/confirm | GoodsIssueController | Yes (WAREHOUSE_STAFF/ADMIN) |
| PUT | /api/goods-issues/{id}/cancel | GoodsIssueController | Yes |
| GET | /api/inventory | InventoryController | Yes |
| GET | /api/inventory/product/{id} | InventoryController | Yes |
| GET | /api/inventory/warehouse/{id} | InventoryController | Yes |
| GET | /api/inventory/low-stock | InventoryController | Yes |
| GET | /api/inventory/needing-reorder | InventoryController | Yes |
| GET | /api/inventory/transactions/product/{id} | InventoryController | Yes |
| GET | /api/customers | CustomerController | Yes |
| GET | /api/customers/{id} | CustomerController | Yes |
| POST | /api/customers | CustomerController | Yes |
| PUT | /api/customers/{id} | CustomerController | Yes |
| DELETE | /api/customers/{id} | CustomerController | Yes |
| GET | /api/suppliers | SupplierController | Yes |
| POST | /api/suppliers | SupplierController | Yes |
| PUT | /api/suppliers/{id} | SupplierController | Yes |
| DELETE | /api/suppliers/{id} | SupplierController | Yes |
| GET | /api/products | ProductController | Yes |
| POST | /api/products | ProductController | Yes |
| PUT | /api/products/{id} | ProductController | Yes |
| DELETE | /api/products/{id} | ProductController | Yes |
| GET | /api/warehouses | WarehouseController | Yes |
| POST | /api/warehouses | WarehouseController | Yes |
| PUT | /api/warehouses/{id} | WarehouseController | Yes |
| DELETE | /api/warehouses/{id} | WarehouseController | Yes |
| GET | /api/sales-invoices | SalesInvoiceController | Yes |
| GET | /api/sales-invoices/{id} | SalesInvoiceController | Yes |
| POST | /api/sales-invoices | SalesInvoiceController | Yes |
| PUT | /api/sales-invoices/{id} | SalesInvoiceController | Yes |
| DELETE | /api/sales-invoices/{id} | SalesInvoiceController | Yes |
| GET | /api/sales-invoices/customer/{id}/outstanding | SalesInvoiceController | Yes |
| GET | /api/sales-invoices/overdue | SalesInvoiceController | Yes |
| GET | /api/delivery-plans | DeliveryPlanController | Yes |
| GET | /api/delivery-plans/{id} | DeliveryPlanController | Yes |
| POST | /api/delivery-plans | DeliveryPlanController | Yes |
| PUT | /api/delivery-plans/{id} | DeliveryPlanController | Yes |
| DELETE | /api/delivery-plans/{id} | DeliveryPlanController | Yes |
| GET | /api/delivery-plans/{id}/orders | DeliveryPlanController | Yes |
| GET | /api/delivery-plans/{id}/shippers | DeliveryPlanController | Yes |
| GET | /api/delivery-plans/{id}/trips | DeliveryPlanController | Yes |
| POST | /api/delivery-plans/{id}/shippers | DeliveryPlanController | Yes (DELIVERY_ADMIN/ADMIN) |
| POST | /api/delivery-plans/{id}/generate-trips | DeliveryPlanController | Yes (DELIVERY_ADMIN/ADMIN) |
| GET | /api/delivery-trips | DeliveryTripController | Yes |
| GET | /api/delivery-trips/{id} | DeliveryTripController | Yes |
| GET | /api/delivery-trips/shipper/{id} | DeliveryTripController | Yes (SHIPPER view) |
| PUT | /api/delivery-trips/{id}/start | DeliveryTripController | Yes |
| PUT | /api/delivery-trips/{id}/complete | DeliveryTripController | Yes |
| PUT | /api/delivery-trips/{id}/assign | DeliveryTripController | Yes (DELIVERY_ADMIN/ADMIN) |
| PUT | /api/delivery-trips/{id}/cancel | DeliveryTripController | Yes |
| GET | /api/dashboard/summary | DashboardController | Yes |
| GET | /api/dashboard/revenue-chart | DashboardController | Yes |
| GET | /api/dashboard/inventory-report | DashboardController | Yes |
| GET | /api/dashboard/receivables-report | DashboardController | Yes |
| GET | /api/dashboard/top-products | DashboardController | Yes |

### 2.5 Security
- Spring Security có cấu hình không? **Yes** — `config/SecurityConfig.java`
- JWT filter có không? **Yes** — `security/JwtAuthenticationFilter.java` (extends OncePerRequestFilter)
- RBAC: **Yes** — @PreAuthorize annotations trên các method service/controller

**Các role string xuất hiện trong code (9 roles):**
- `ROLE_ADMIN`
- `ROLE_PURCHASE_STAFF`
- `ROLE_PURCHASE_MANAGER`
- `ROLE_SALES_STAFF`
- `ROLE_SALES_MANAGER`
- `ROLE_WAREHOUSE_STAFF`
- `ROLE_DELIVERY_ADMIN`
- `ROLE_SHIPPER`
- `ROLE_ACCOUNTANT`

**Endpoint công khai (không cần JWT):** `/api/auth/login`, `/api/auth/register`, `/api/auth/refresh`, `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/**`

### 2.6 Database Migration
- Có Flyway/Liquibase? **Yes — Flyway**
- Số migration files: **4**
- Danh sách file migration:
  - `V1__purchasing_module.sql`
  - `V2__sales_module.sql`
  - `V3__security_and_rbac.sql`
  - `V4__sample_data.sql`

---

## 3. FRONTEND

### 3.1 Tech Stack
- React version: 18.2.0
- Build tool: Vite 5.0.0
- UI library: Ant Design 5.10.0
- State management: Local state (useState/useEffect) + localStorage cho auth token
- HTTP client: Axios 1.4.0 (với custom interceptors)
- Routing: React Router DOM 6.11.1
- Form library: Ant Design Form (built-in)
- Chart library: Không có thư viện chart riêng (Ant Design Statistic/Card cho dashboard)
- Total package count: ~20 direct dependencies

### 3.2 Cấu trúc thư mục frontend
```
frontend/src/
├── App.jsx          (routing + MainLayout + RBAC guards)
├── main.jsx         (entry point)
├── pages/           (27 page components)
└── services/
    ├── api.js       (axios instance + 14 API service objects)
    └── roleService.js (ROLES constants + hasAnyRole helper)
```

### 3.3 Routes (24 routes)

| Path | Component | Có Auth Guard? |
|---|---|---|
| /login | LoginPage | No (public) |
| / | DashboardPage | Yes (ProtectedRoute) |
| /reports | DashboardPage | Yes + ADMIN/SALES_MANAGER/PURCHASE_MANAGER/ACCOUNTANT |
| /sales-orders | SalesOrderList | Yes + ADMIN/SALES_STAFF/SALES_MANAGER/ACCOUNTANT/WAREHOUSE_STAFF |
| /sales-orders/new | SalesOrderForm | Yes + ADMIN/SALES_STAFF |
| /sales-orders/:id | SalesOrderDetail | Yes + ADMIN/SALES_STAFF/SALES_MANAGER/ACCOUNTANT/WAREHOUSE_STAFF |
| /sales-orders/:id/edit | SalesOrderForm | Yes + ADMIN/SALES_STAFF |
| /goods-issues | GoodsIssueList | Yes + ADMIN/WAREHOUSE_STAFF/SALES_MANAGER |
| /goods-issues/new | GoodsIssueForm | Yes + ADMIN/WAREHOUSE_STAFF |
| /goods-issues/:id | GoodsIssueDetail | Yes + ADMIN/WAREHOUSE_STAFF/SALES_MANAGER |
| /goods-issues/:id/edit | GoodsIssueForm | Yes + ADMIN/WAREHOUSE_STAFF |
| /sales-invoices | SalesInvoiceList | Yes + ADMIN/ACCOUNTANT/SALES_MANAGER |
| /sales-invoices/:id | SalesInvoiceDetail | Yes + ADMIN/ACCOUNTANT/SALES_MANAGER |
| /customers | CustomerList | Yes + ADMIN/SALES_STAFF/SALES_MANAGER |
| /purchase-orders | PurchaseOrderList | Yes + ADMIN/PURCHASE_STAFF/PURCHASE_MANAGER/ACCOUNTANT/WAREHOUSE_STAFF |
| /purchase-orders/:id | PurchaseOrderDetail | Yes + ADMIN/PURCHASE_STAFF/PURCHASE_MANAGER/ACCOUNTANT/WAREHOUSE_STAFF |
| /goods-receipts | GoodsReceiptList | Yes + ADMIN/WAREHOUSE_STAFF/PURCHASE_MANAGER |
| /goods-receipts/:id | GoodsReceiptDetail | Yes + ADMIN/WAREHOUSE_STAFF/PURCHASE_MANAGER |
| /inventory | InventoryList | Yes + ADMIN/WAREHOUSE_STAFF/PURCHASE_STAFF/SALES_STAFF |
| /warehouses | WarehouseList | Yes + ADMIN/WAREHOUSE_STAFF |
| /products | ProductList | Yes + ADMIN/PURCHASE_STAFF/SALES_STAFF |
| /suppliers | SupplierList | Yes + ADMIN/PURCHASE_STAFF |
| /delivery-plans | DeliveryPlanList | Yes + ADMIN/DELIVERY_ADMIN/SHIPPER |
| /delivery-plans/:id | DeliveryPlanDetail | Yes + ADMIN/DELIVERY_ADMIN/SHIPPER |

### 3.4 Pages/Screens (27 pages)

| Tên page | File | Module | Status |
|---|---|---|---|
| LoginPage | pages/LoginPage.jsx | Auth | Complete |
| DashboardPage | pages/DashboardPage.jsx | Cross-cutting | Partial (dùng cho cả / và /reports) |
| PurchaseOrderList | pages/PurchaseOrderList.jsx | Mua hàng | Complete |
| PurchaseOrderDetail | pages/PurchaseOrderDetail.jsx | Mua hàng | Partial |
| PurchaseOrderForm | pages/PurchaseOrderForm.jsx | Mua hàng | Stub (không có route trong App.jsx) |
| GoodsReceiptList | pages/GoodsReceiptList.jsx | Mua hàng | Complete |
| GoodsReceiptDetail | pages/GoodsReceiptDetail.jsx | Mua hàng | Partial |
| GoodsReceiptForm | pages/GoodsReceiptForm.jsx | Mua hàng | Stub (không có route trong App.jsx) |
| InventoryList | pages/InventoryList.jsx | Kho hàng | Partial |
| WarehouseList | pages/WarehouseList.jsx | Kho hàng | Partial (không có form) |
| CustomerList | pages/CustomerList.jsx | Bán hàng | Partial (không có form riêng) |
| SalesOrderList | pages/SalesOrderList.jsx | Bán hàng | Complete |
| SalesOrderForm | pages/SalesOrderForm.jsx | Bán hàng | Complete |
| SalesOrderDetail | pages/SalesOrderDetail.jsx | Bán hàng | Complete |
| GoodsIssueList | pages/GoodsIssueList.jsx | Bán hàng | Complete |
| GoodsIssueForm | pages/GoodsIssueForm.jsx | Bán hàng | Complete |
| GoodsIssueDetail | pages/GoodsIssueDetail.jsx | Bán hàng | Complete |
| SalesInvoiceList | pages/SalesInvoiceList.jsx | Kế toán | Partial |
| SalesInvoiceDetail | pages/SalesInvoiceDetail.jsx | Kế toán | Partial (thiếu payment flow) |
| DeliveryPlanList | pages/DeliveryPlanList.jsx | Kho vận | Complete |
| DeliveryPlanDetail | pages/DeliveryPlanDetail.jsx | Kho vận | Partial (4-tab UI chưa rõ đủ) |
| DeliveryPlanForm | pages/DeliveryPlanForm.jsx | Kho vận | Stub (không có route trong App.jsx) |
| ShipmentForm | pages/ShipmentForm.jsx | Kho vận | Stub (không có route, không import trong App.jsx) |
| ProductList | pages/ProductList.jsx | Danh mục | Partial |
| ProductForm | pages/ProductForm.jsx | Danh mục | Stub (không có route riêng) |
| SupplierList | pages/SupplierList.jsx | Danh mục | Partial |
| SupplierForm | pages/SupplierForm.jsx | Danh mục | Stub (không có route riêng) |

### 3.5 Environment variables
- Không tìm thấy file `.env` hoặc `.env.example` trong frontend/
- Backend URL hiện được hardcode trong `frontend/src/services/api.js`: `http://localhost:8080/api`
- Biến cần cấu hình:
  - `VITE_API_BASE_URL` (hiện chưa có, đang hardcode)

---

## 4. DATABASE SCHEMA

### 4.1 Bảng (suy ra từ JPA Entity + Flyway migration)

| Bảng | Số cột (ước tính) | FK đến |
|---|---|---|
| users | ~8 | roles (many-to-many qua user_roles) |
| roles | ~3 | — |
| user_roles | 2 | users, roles |
| products | ~10 | suppliers |
| suppliers | ~8 | — |
| customers | ~8 | — |
| delivery_addresses | ~6 | customers |
| warehouses | ~6 | — |
| inventory | ~8 | products, warehouses |
| inventory_transactions | ~10 | products, warehouses |
| purchase_orders | ~12 | suppliers |
| purchase_order_items | ~7 | purchase_orders, products |
| goods_receipts | ~10 | purchase_orders, warehouses |
| goods_receipt_items | ~9 | goods_receipts, products (+ lot_number, expiry_date) |
| sales_orders | ~14 | customers, delivery_addresses |
| sales_order_items | ~8 | sales_orders, products |
| goods_issues | ~10 | sales_orders, warehouses |
| goods_issue_items | ~9 | goods_issues, products (+ lot_number, expiry_date) |
| sales_invoices | ~12 | sales_orders, goods_issues |
| sales_invoice_items | ~7 | sales_invoices, products |
| invoices | ~? | Không rõ (legacy entity) |
| delivery_plans | ~8 | — |
| delivery_plan_orders | ~5 | delivery_plans |
| delivery_plan_shippers | ~5 | delivery_plans |
| delivery_orders | ~8 | — |
| delivery_trip_routes | ~8 | — |
| delivery_trip_route_items | ~6 | delivery_trip_routes |

### 4.2 So khớp Entity vs Bảng kỳ vọng theo domain Distribution Management

| Entity kỳ vọng | Có trong code? | Note |
|---|---|---|
| order_header | Tên khác: PurchaseOrder + SalesOrder | Tách thành 2 entity riêng, không dùng type discriminator |
| order_item | Tên khác: PurchaseOrderItem + SalesOrderItem | Tương tự trên |
| shipment | Tên khác: GoodsReceipt (GR) + GoodsIssue (GI) | Tách thành 2 entity riêng |
| shipment_item | Tên khác: GoodsReceiptItem + GoodsIssueItem | Có lot_number + expiry_date fields |
| asset | No — thay bằng Inventory | Chỉ lưu quantity tổng, không lot-based |
| asset_detail | No — thay bằng InventoryTransaction | Chỉ là audit trail, không phải lot record |
| delivery_order | Yes — DeliveryOrder | Có entity, thiếu liên kết rõ ràng với SalesOrder |
| delivery_plan | Yes — DeliveryPlan | Đầy đủ |
| delivery_plan_order | Yes — DeliveryPlanOrder | Có |
| delivery_plan_shipper | Yes — DeliveryPlanShipper | Có (thêm ngoài spec) |
| delivery_triproute | Yes — DeliveryTripRoute | Có |
| delivery_triproute_item | Yes — DeliveryTripRouteItem | Có |
| invoice | Yes — SalesInvoice (+ legacy Invoice.java) | Purchase invoice chưa có |
| invoice_item | Yes — SalesInvoiceItem | Chỉ cho sales, không có purchase |
| payment | No | Chỉ có PaymentStatus enum; không có entity Payment |
| payment_application | No | Không tìm thấy |
| acctg_trans | No | Không tìm thấy |
| acctg_trans_entry | No | Không tìm thấy |
| product | Yes — Product | Đầy đủ |
| customer | Yes — Customer | Đầy đủ |
| supplier | Yes — Supplier | Đầy đủ |
| warehouse | Yes — Warehouse | Đầy đủ |
| user/account | Yes — User | Đầy đủ |
| role | Yes — Role | 9 roles (chi tiết hơn spec 5 roles) |

---

## 5. ĐÁNH GIÁ HOÀN THIỆN PHÂN HỆ

### 5.1 Mua hàng — 60%
**Đã có**:
- Entity PurchaseOrder + PurchaseOrderItem với đầy đủ status workflow
- CRUD PO + filter by status/supplier/date range
- Flow Approve PO: request-approval → approve/reject/cancel (endpoints + frontend)
- Phiếu nhập kho (GoodsReceipt + GoodsReceiptItem)
- Action "Nhập hàng" xác nhận (confirm) → cập nhật Inventory quantity
- GoodsReceiptItem có fields `lot_number` + `expiry_date` (data model sẵn sàng cho FEFO)
- Frontend: PurchaseOrderList, PurchaseOrderDetail, GoodsReceiptList, GoodsReceiptDetail

**Còn thiếu**:
- `PurchaseOrderForm` tồn tại nhưng KHÔNG có route trong App.jsx (không thể tạo PO mới từ UI)
- `GoodsReceiptForm` tồn tại nhưng KHÔNG có route trong App.jsx
- Không có Purchase Invoice entity (chỉ có SalesInvoice)
- Không có acctg_trans integration khi xác nhận nhập hàng
- Không có Phân bổ chi phí mua hàng (landed cost allocation)

**Evidence**: `backend/src/main/java/com/distribution/controller/PurchaseOrderController.java`, `model/PurchaseOrder.java`, `model/GoodsReceipt.java`, `frontend/src/pages/PurchaseOrderList.jsx`

---

### 5.2 Bán hàng — 70%
**Đã có**:
- Entity SalesOrder + SalesOrderItem với đầy đủ status workflow
- CRUD SO + filter + search
- Flow Approve SO: request-approval → approve/reject/cancel
- Phiếu xuất kho (GoodsIssue + GoodsIssueItem) + confirm/cancel
- SalesInvoice + SalesInvoiceItem với status DRAFT/FINALIZED/PAID
- Frontend: SalesOrderList, SalesOrderForm, SalesOrderDetail, GoodsIssueList, GoodsIssueForm, GoodsIssueDetail, SalesInvoiceList, SalesInvoiceDetail — bộ đầy đủ nhất trong codebase

**Còn thiếu**:
- Không có Payment entity (chỉ PaymentStatus enum trên SalesInvoice)
- Không có payment_application (matching/offset payment)
- Không có acctg_trans integration khi finalize invoice
- CustomerList không có form tạo/sửa (chỉ có danh sách)
- Flow thanh toán thực sự (ghi nhận tiền thu) chưa implement

**Evidence**: `model/SalesOrder.java`, `model/GoodsIssue.java`, `model/SalesInvoice.java`, `frontend/src/pages/SalesOrderForm.jsx`, `frontend/src/pages/SalesInvoiceDetail.jsx`

---

### 5.3 Kho vận — 55%
**Đã có**:
- Entity Inventory (multi-warehouse, optimistic locking) + InventoryTransaction (audit)
- Entity DeliveryPlan + DeliveryPlanOrder + DeliveryPlanShipper
- Entity DeliveryTripRoute + DeliveryTripRouteItem
- Entity DeliveryOrder
- API generate-trips, assign-shipper, start/complete/cancel trip
- Shipper role có endpoint GET /api/delivery-trips/shipper/{id}
- Frontend: DeliveryPlanList, DeliveryPlanDetail (với trip info), InventoryList, WarehouseList

**Còn thiếu**:
- Không có asset/asset_detail (lot-based tracking) — Inventory chỉ theo quantity tổng
- FEFO pick logic chưa implement trong GoodsIssueServiceImpl (fields có nhưng service không sort/filter theo expiry)
- DeliveryPlanForm tồn tại nhưng không có route (không tạo delivery plan từ UI được)
- Shipper không có trang riêng (App.jsx comment: "// Reusing or adding a trip page later")
- ShipmentForm.jsx có trong pages/ nhưng không được import hay route
- WarehouseList không có form tạo/sửa kho

**Evidence**: `model/Inventory.java`, `model/DeliveryPlan.java`, `model/DeliveryTripRoute.java`, `controller/DeliveryTripController.java`, `frontend/src/App.jsx:206` (TODO shipper view)

---

### 5.4 Kế toán — 15%
**Đã có**:
- SalesInvoice với trạng thái DRAFT/FINALIZED/PAID
- ACCOUNTANT role với quyền xem orders, approve, view invoices
- DashboardController: receivables-report, revenue-chart (aggregation queries)
- CustomerController: outstanding balance, overdue payment queries

**Còn thiếu**:
- Không có Payment entity (chỉ PaymentStatus enum — không ghi nhận được giao dịch thanh toán)
- Không có payment_application (không có khớp lệnh)
- Không có acctg_trans / acctg_trans_entry (không có sổ cái)
- Không có "Duyệt hóa đơn" sinh journal entry
- Không có Phân bổ chi phí mua hàng (purchase accounting)
- Không có Purchase Invoice entity
- Phân hệ kế toán thực chất chỉ là view-only hiện tại

**Evidence**: `model/enums/PaymentStatus.java`, `model/SalesInvoice.java`, `controller/SalesInvoiceController.java`, `controller/DashboardController.java`

---

### 5.5 Cross-cutting — 70%
- Authentication (JWT): **Done** — JwtAuthenticationFilter + JwtTokenProvider + access/refresh token
- RBAC (9 vai trò thay vì 5 trong spec): **Done** — 9 roles chi tiết hơn, RBAC guards ở cả backend và frontend
- Dashboard: **Partial** — Có summary stats, revenue chart, inventory report, receivables. Chưa có chart library thực sự (chỉ Ant Design Statistic)
- Audit log: **Missing** — Chỉ có `createdAt`/`updatedAt` timestamps, không có history table
- Multi-warehouse: **Done** — Inventory tracking per warehouse
- Lot/Expiry (FEFO): **Partial (data model only)** — `lot_number` + `expiry_date` fields có trên GoodsReceiptItem/GoodsIssueItem, nhưng service không implement FEFO sort logic
- Phân bổ chi phí: **Missing** — Không có landed cost allocation

---

## 6. CODE QUALITY FLAGS

### 6.1 TODO / FIXME / HACK trong code
```bash
grep -r "TODO\|FIXME\|HACK" --include="*.java" --include="*.jsx" -l (excluding node_modules)
```
- Tổng số file có TODO/FIXME trong source code (Java + JSX): **0 file**
- Tuy nhiên có comment inline trong App.jsx:
  - `frontend/src/App.jsx:207`: `// Reusing or adding a trip page later` — shipper trip page chưa implement

### 6.2 Test coverage
- Có thư mục `src/test/`? **No** — Không tìm thấy thư mục test trong backend
- Số file test: **0**
- Có test integration không? **No**

### 6.3 Linting/Format
- Có `.eslintrc`? **No** — Không tìm thấy
- Có `.prettierrc`? **No** — Không tìm thấy
- Có `checkstyle.xml` cho Java? **No** — Không tìm thấy

### 6.4 Git status
```bash
git status --short
```
- Số file uncommitted: **0** (working tree clean)
- Có file lớn untracked không? **Không** — clean

---

## 7. DEPLOYMENT & DEVOPS

- Có `Dockerfile`? **No** — Không tìm thấy
- Có `docker-compose.yml`? **No** — Không tìm thấy
- Có CI/CD config (GitHub Actions, GitLab CI)? **No** — Không có `.github/workflows/`
- Có README.md hướng dẫn run local? **No** — Không có README.md ở root
- Đã deploy lên Railway chưa? **Không rõ** — Không tìm thấy Railway config, Dockerfile, hay reference trong code. Backend URL còn hardcode localhost:8080

**⚠ Backend URL hardcode**: `frontend/src/services/api.js` hardcode `http://localhost:8080/api`, không dùng environment variable — sẽ gặp vấn đề khi deploy production.

---

## 8. ĐÁNH GIÁ TỔNG QUÁT

**Điểm mạnh**:
- Kiến trúc backend rõ ràng, layered pattern nhất quán (Controller → Service → Repository → Entity)
- Flyway migrations đảm bảo schema versioning
- RBAC chi tiết với 9 roles, được enforce ở cả frontend (route guards + menu visibility) và backend (SecurityConfig)
- Phân hệ Bán hàng là hoàn thiện nhất — có đầy đủ CRUD, workflow, và frontend pages
- Data model GoodsReceiptItem/GoodsIssueItem đã chuẩn bị sẵn `lot_number` + `expiry_date` cho FEFO

**Rủi ro/Nợ kỹ thuật**:
- **Phân hệ Kế toán gần như chưa làm** (15%): thiếu Payment entity, acctg_trans, journal entries — đây là gap lớn nhất
- **4 form pages bị "orphaned"**: `PurchaseOrderForm`, `GoodsReceiptForm`, `DeliveryPlanForm`, `ShipmentForm` tồn tại trong code nhưng không có route → không dùng được từ UI
- **Không có test nào**: 0 unit test, 0 integration test — rủi ro regression khi mở rộng
- **Backend URL hardcode**: `http://localhost:8080/api` trong api.js — chặn deployment
- **Không có Dockerfile/docker-compose**: deploy local phức tạp, không có môi trường chuẩn hóa

**% Hoàn thành ước tính tổng thể**: **~54%**

| Phân hệ | Ước tính |
|---|---|
| Mua hàng | 60% |
| Bán hàng | 70% |
| Kho vận | 55% |
| Kế toán | 15% |
| Cross-cutting | 70% |
| **Tổng** | **~54%** |

---

## 9. KẾT THÚC

Báo cáo này được sinh tự động bởi Thợ thi công theo TIP-XRAY.
Các quyết định về roadmap tiếp theo sẽ do Chủ thầu đưa ra dựa trên báo cáo này.
