# TÀI LIỆU ÔN TẬP PHẢN BIỆN ĐỒ ÁN TỐT NGHIỆP
## Hệ thống Quản lý Chuỗi cung ứng Thông minh (Smart Supply Chain Management)

> Tài liệu này tổng hợp TOÀN BỘ kiến thức bạn cần nắm để phản biện: bức tranh hệ thống, kiến thức nền về techstack, logic nghiệp vụ trong code, và ngân hàng câu hỏi dự kiến kèm gợi ý trả lời. Mọi mô tả đều đối chiếu trực tiếp với code thực tế trong repo.

---

# PHẦN 1 — BỨC TRANH TỔNG THỂ (nói được trong 60 giây)

**Elevator pitch khi mở đầu phản biện:**

> "Em xây dựng hệ thống quản lý phân phối/chuỗi cung ứng cho doanh nghiệp thương mại (nhà phân phối bánh kẹo), số hóa toàn bộ vòng đời hàng hóa: **Mua hàng (PO) → Nhập kho (GR) → Bán hàng (SO) → Xuất kho (GI) → Hóa đơn & công nợ → Giao vận (Delivery)**. Hệ thống là web app kiến trúc client-server: backend **Spring Boot 3 (Java 17)** cung cấp REST API, frontend **React 18 + Ant Design**, CSDL **PostgreSQL**. Điểm nhấn kỹ thuật: quản lý tồn kho **theo lô và hạn sử dụng với chiến lược xuất FEFO**, kiểm soát tranh chấp tồn kho bằng **pessimistic locking**, phân quyền **RBAC 9 vai trò** với JWT, và lọc dữ liệu theo vai trò bằng **AOP**."

## 1.1. Kiến trúc tổng thể

```
┌──────────────────┐     HTTP/JSON      ┌─────────────────────┐    JDBC     ┌──────────────┐
│  React 18 SPA    │ ◄────────────────► │  Spring Boot 3.1.4  │ ◄─────────► │ PostgreSQL   │
│  (Vite, AntD 5)  │   REST + JWT       │  REST API :8080     │  JPA/       │ distribution │
│  :5173           │   Bearer token     │  Layered Arch       │  Hibernate  │ _db          │
└──────────────────┘                    └─────────────────────┘             └──────────────┘
```

- **Mô hình:** Monolith 3 tầng (3-tier), tách frontend/backend hoàn toàn, giao tiếp qua REST API JSON.
- **Stateless:** Backend không lưu session; mỗi request mang JWT trong header `Authorization: Bearer <token>`.
- **Tài liệu API:** Swagger UI (springdoc-openapi) tại `/swagger-ui.html`.

## 1.2. Các phân hệ (module) chức năng

| Phân hệ | Chức năng chính | Trạng thái |
|---|---|---|
| Master Data | Sản phẩm (SKU, ĐVT, giá), Khách hàng, Nhà cung cấp, Đa kho | ✅ |
| Mua hàng | Đơn mua (PO), phê duyệt, Nhập kho (GR) một phần/toàn bộ, theo dõi lô/HSD | ✅ |
| Bán hàng | Đơn bán (SO), kiểm tra tồn khả dụng (ATP), Xuất kho (GI), Hóa đơn, công nợ | ✅ |
| Kho vận | Tồn kho realtime đa kho, nhật ký giao dịch kho, quản lý lô & HSD, xuất hủy lô hết hạn, gợi ý mua hàng (reorder) | ✅ |
| Giao vận | Đợt giao hàng (Delivery Plan), chuyến giao (Trip), phân công Shipper, cập nhật trạng thái giao | ✅ |
| Báo cáo | Dashboard doanh thu/tồn/công nợ, biểu đồ doanh thu, top SP bán chạy, báo cáo Nhập-Xuất-Tồn, công nợ quá hạn | ✅ |
| Kế toán (bút toán tự động) | — | 🔜 Ngoài phạm vi, đã ghi rõ trong báo cáo |

## 1.3. Luồng nghiệp vụ xương sống (PHẢI thuộc lòng — chắc chắn bị hỏi)

```
[Mua]  PO (ORDER_OPEN) → duyệt → (ORDER_APPROVED) → Nhập kho GR (DRAFT → CONFIRMED)
        → tạo InventoryLot (số lô + HSD) → TĂNG tồn kho → PO chuyển PARTIALLY_RECEIVED/COMPLETED

[Bán]  SO (ORDER_OPEN) → kiểm tra tồn khả dụng → duyệt → (ORDER_APPROVED)
        → Xuất kho GI (DRAFT → CONFIRMED) → trừ lô theo FEFO → GIẢM tồn kho
        → SO chuyển PARTIALLY_DELIVERED/COMPLETED
        → Hóa đơn (DRAFT → ISSUED → PAID/PARTIALLY_PAID; quá hạn → OVERDUE tự động)

[Giao] Delivery Plan → gom các đơn → chia Trip → gán Shipper → Shipper cập nhật trạng thái giao
```

**Nguyên tắc vàng của thiết kế:** tồn kho **chỉ thay đổi khi chứng từ kho được CONFIRM** (GR confirm → tăng; GI confirm → giảm). Đơn hàng (PO/SO) chỉ là cam kết, không trực tiếp đụng vào tồn kho. Mỗi biến động kho đều ghi một bản ghi `InventoryTransaction` để truy vết (audit trail).

---

# PHẦN 2 — KIẾN THỨC NỀN VỀ TECHSTACK (học để trả lời "X là gì, tại sao dùng")

## 2.1. Java 17 + Spring Boot 3.1.4

**Spring Boot là gì?** Framework dựng trên Spring, cung cấp:
- **Auto-configuration:** tự cấu hình bean dựa trên dependency có trên classpath (có `spring-boot-starter-data-jpa` + driver PostgreSQL → tự cấu hình DataSource, EntityManager).
- **Starter dependencies:** gói dependency theo chức năng (`starter-web`, `starter-security`...).
- **Embedded server:** Tomcat nhúng sẵn, chạy bằng `java -jar`, không cần deploy WAR.

**Các khái niệm Spring cốt lõi PHẢI hiểu:**

1. **IoC (Inversion of Control) / DI (Dependency Injection):** Thay vì tự `new` đối tượng, Spring container tạo và "tiêm" (inject) các bean vào nhau. Trong code của bạn: inject qua **constructor** (Lombok `@RequiredArgsConstructor` sinh constructor cho các field `final`). Constructor injection được khuyến nghị vì: bắt buộc dependency không null, dễ test, field có thể `final` (immutable).

2. **Bean & ApplicationContext:** Bean là đối tượng do Spring quản lý. Đăng ký bằng annotation `@Component`, `@Service`, `@Repository`, `@Controller/@RestController`, hoặc `@Bean` trong class `@Configuration`. Mặc định scope là **singleton**.

3. **Annotation hay bị hỏi:**
   - `@RestController` = `@Controller` + `@ResponseBody` (trả JSON thay vì view).
   - `@Service` / `@Repository`: đánh dấu tầng nghiệp vụ / truy cập dữ liệu. `@Repository` còn dịch exception JDBC sang `DataAccessException` của Spring.
   - `@Transactional`: bọc method trong transaction CSDL (xem 2.3).
   - `@Valid` + Bean Validation (`@NotNull`, `@Size`...): validate dữ liệu đầu vào ở tầng controller.

4. **Lombok:** thư viện sinh code lúc compile (`@Getter/@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`) — giảm boilerplate, không ảnh hưởng runtime.

**Tại sao chọn Spring Boot?** Hệ sinh thái doanh nghiệp trưởng thành (Security, Data JPA, Validation tích hợp sẵn), cộng đồng lớn, phù hợp hệ thống nghiệp vụ phức tạp cần transaction và phân quyền chặt; bản thân các hệ thống ERP/phân phối thực tế cũng hay dùng Java.

## 2.2. JPA / Hibernate (chắc chắn bị hỏi sâu)

- **JPA** = Jakarta Persistence API — **đặc tả** (interface chuẩn) về ORM của Java.
- **Hibernate** = **triển khai** phổ biến nhất của JPA (Spring Boot mặc định dùng Hibernate).
- **Spring Data JPA** = tầng trừu tượng trên JPA: chỉ cần khai báo interface kế thừa `JpaRepository<Entity, ID>` là có sẵn CRUD; tự sinh query từ tên method (`findByProductIdAndWarehouseId`); query phức tạp viết bằng `@Query` (JPQL).

**ORM là gì?** Object-Relational Mapping — ánh xạ class Java ↔ bảng CSDL, đối tượng ↔ dòng dữ liệu. Ưu: viết ít SQL, code hướng đối tượng, chống SQL injection (parameterized). Nhược: có overhead, dễ dính N+1 query nếu không hiểu.

**Khái niệm cần thuộc:**

1. **Entity lifecycle:** `transient` (mới new) → `managed` (được EntityManager quản lý, trong persistence context) → `detached` (hết transaction) → `removed`.
2. **Dirty checking:** trong transaction, sửa field của entity managed thì Hibernate TỰ phát hiện và sinh UPDATE khi commit — không cần gọi `save()` tường minh.
3. **FetchType LAZY vs EAGER:** LAZY chỉ load quan hệ khi truy cập (mặc định cho `@OneToMany`); EAGER load ngay (mặc định cho `@ManyToOne`). LAZY ngoài transaction → `LazyInitializationException`.
4. **N+1 problem:** load N đơn hàng rồi truy cập `order.getCustomer()` từng cái → 1 + N query. Khắc phục: `JOIN FETCH` trong JPQL, `@EntityGraph`, hoặc DTO projection.
5. **JPQL vs SQL:** JPQL truy vấn trên **entity và field** (`SELECT l FROM InventoryLot l WHERE l.product.id = :productId`), không phải tên bảng/cột; Hibernate dịch sang SQL của dialect PostgreSQL.
6. **`ddl-auto: validate`** (cấu hình trong `application.yml` của bạn): Hibernate KHÔNG tự sửa schema, chỉ kiểm tra entity khớp với schema — schema do file migration SQL quản lý. An toàn hơn `update` (vốn nguy hiểm cho production).

**Trong code của bạn:** ~25 entity (`Product`, `Inventory`, `InventoryLot`, `InventoryTransaction`, `PurchaseOrder/Item`, `SalesOrder/Item`, `GoodsReceipt/Item`, `GoodsIssue/Item`, `SalesInvoice/Item`, `DeliveryPlan/Trip/Order`, `User`, `Role`, `LotDisposal`...), mỗi entity một `Repository`.

## 2.3. Transaction & ACID (câu hỏi kinh điển)

**ACID:**
- **Atomicity:** tất cả hoặc không gì cả — confirm GI vừa trừ lô, vừa trừ tồn, vừa ghi nhật ký; lỗi giữa chừng → rollback hết.
- **Consistency:** dữ liệu luôn ở trạng thái hợp lệ (ràng buộc FK, check constraint).
- **Isolation:** các transaction song song không thấy dữ liệu dở dang của nhau.
- **Durability:** đã commit thì không mất (WAL của PostgreSQL).

**`@Transactional` trong code bạn:** đặt ở tầng **Service** (vd. `GoodsIssueServiceImpl` annotate cả class, method đọc dùng `@Transactional(readOnly = true)` để tối ưu). Mặc định: propagation `REQUIRED`, rollback khi gặp **unchecked exception** (RuntimeException). Các exception nghiệp vụ của bạn (`BusinessException`, `InventoryException`, `InvalidStatusTransitionException`) là RuntimeException → tự rollback.

**Isolation level PostgreSQL mặc định: READ COMMITTED** — không đọc dữ liệu chưa commit, nhưng có thể non-repeatable read. Bạn xử lý tranh chấp tồn kho bằng **lock tường minh** thay vì nâng isolation (xem 4.4).

## 2.4. PostgreSQL

- RDBMS mã nguồn mở, tuân thủ ACID, hỗ trợ transaction mạnh — phù hợp dữ liệu nghiệp vụ tài chính/kho (không được sai số).
- **Tại sao không dùng NoSQL (MongoDB)?** Dữ liệu của hệ thống **quan hệ chặt** (đơn hàng–chi tiết–sản phẩm–kho–lô), cần JOIN, FK, transaction đa bảng. NoSQL phù hợp dữ liệu phi cấu trúc/scale ngang — không phải bài toán này.
- **Tại sao không MySQL?** Cả hai đều đáp ứng; PostgreSQL chọn vì chuẩn SQL nghiêm ngặt hơn, MVCC tốt, kinh nghiệm cá nhân. (Câu trả lời an toàn: "cả hai đều phù hợp, em chọn PostgreSQL vì... và đã quen dùng").
- **Schema versioning:** schema định nghĩa bằng các file SQL `V1__purchasing_module.sql` → `V8__lot_disposal.sql` trong `backend/src/main/resources/db/migration` theo quy ước đặt tên của Flyway. ⚠️ **LƯU Ý QUAN TRỌNG:** `application.yml` có bật `spring.flyway.enabled: true` nhưng **`pom.xml` KHÔNG có dependency `flyway-core`** → Flyway thực tế KHÔNG chạy tự động; migration được apply thủ công bằng `psql`. Khi phản biện **đừng khẳng định "hệ thống dùng Flyway tự động"** — nói chính xác: "schema được quản lý phiên bản bằng các file migration SQL theo chuẩn Flyway, hiện apply thủ công; Hibernate ở chế độ `validate` để đảm bảo entity khớp schema."

## 2.5. Spring Security + JWT (trọng tâm hỏi sâu — học kỹ phần này)

### JWT là gì?
**JSON Web Token** — chuỗi 3 phần `header.payload.signature` (Base64URL):
- **Header:** thuật toán ký (HS256 — HMAC-SHA256) + type.
- **Payload (claims):** username (sub), roles, thời điểm phát hành (iat), hết hạn (exp).
- **Signature:** `HMAC-SHA256(base64(header) + "." + base64(payload), secret)` — đảm bảo token **không bị sửa** (integrity), KHÔNG mã hóa nội dung (payload ai cũng đọc được → không nhét dữ liệu nhạy cảm).

**Thông số trong hệ thống của bạn** (thư viện `jjwt 0.12.3`, cấu hình `application.yml`):
- Access token: **24 giờ** (`expiration-ms: 86400000`)
- Refresh token: **7 ngày** (`refresh-expiration-ms: 604800000`)
- Secret key HMAC đặt trong `application.yml`.

### Luồng xác thực (vẽ được sơ đồ này là ăn điểm)

```
1. POST /api/auth/login {username, password}
2. AuthenticationManager → DaoAuthenticationProvider
   → CustomUserDetailsService.loadUserByUsername() (đọc DB)
   → BCrypt so khớp password
3. Đúng → JwtTokenProvider sinh access token + refresh token → trả client
4. Client (React) lưu token vào localStorage
5. Mỗi request sau: axios interceptor gắn header "Authorization: Bearer <token>"
6. Backend: JwtAuthenticationFilter (chạy TRƯỚC UsernamePasswordAuthenticationFilter)
   → parse + verify chữ ký + check hạn → tạo Authentication
   → đặt vào SecurityContextHolder
7. Lớp authorize (SecurityConfig rules + @PreAuthorize) quyết định cho/không cho
8. Token hết hạn → client gọi POST /api/auth/refresh với refresh token → cấp access token mới
```

### Session-based vs Token-based (so sánh kinh điển)
| | Session | JWT |
|---|---|---|
| Trạng thái | Server lưu session (stateful) | Server không lưu gì (stateless) |
| Scale ngang | Cần sticky session / session store chung | Dễ — server nào verify cũng được |
| Thu hồi | Xóa session là xong | Khó — token còn hạn vẫn hợp lệ (nhược điểm phải thừa nhận) |
| Phù hợp | Web truyền thống server-render | SPA + REST API (trường hợp của bạn) |

**Tại sao chọn JWT?** Vì kiến trúc SPA tách rời frontend/backend qua REST API, backend stateless dễ mở rộng, không cần quản lý session.

### BCrypt (sẽ bị hỏi "mật khẩu lưu thế nào")
- Mật khẩu lưu dạng **hash BCrypt** (one-way), không bao giờ lưu plaintext.
- BCrypt > MD5/SHA-256 thuần vì: (1) có **salt ngẫu nhiên** tích hợp — hai user cùng mật khẩu cho hash khác nhau, chống rainbow table; (2) **cost factor** điều chỉnh được — cố tình chậm để chống brute-force.

### RBAC — phân quyền 3 lớp trong hệ thống (điểm mạnh nên chủ động khoe)

**9 vai trò:** ADMIN, PURCHASE_MANAGER, PURCHASE_STAFF, SALES_MANAGER, SALES_STAFF, WAREHOUSE_STAFF, DELIVERY_ADMIN, SHIPPER, ACCOUNTANT.

1. **Lớp URL (SecurityConfig):** rule theo endpoint + HTTP method. Vd: `POST /api/purchase-orders` chỉ cho PURCHASE_STAFF/PURCHASE_MANAGER/ADMIN; duyệt PO (`PUT .../approve`) chỉ MANAGER/ACCOUNTANT/ADMIN.
2. **Lớp method (`@EnableMethodSecurity` + `@PreAuthorize`):** chặn ở tầng service/controller theo logic mịn hơn.
3. **Lớp dữ liệu (AOP — `DataFilterAspect`):** aspect `@Around` bọc các method service trả `List<SalesOrderDTO>/List<PurchaseOrderDTO>`; nếu user là WAREHOUSE_STAFF (không phải admin) thì **lọc bỏ đơn chưa duyệt** — thủ kho chỉ thấy đơn đã APPROVED trở đi. Shipper chỉ thấy chuyến giao được phân công (lọc ở service).

**Business rule phân quyền đáng nói:** người tạo đơn không tự duyệt được đơn (tách vai trò STAFF/MANAGER) — nguyên tắc **separation of duties** trong kiểm soát nội bộ.

### CORS và CSRF (hai câu hỏi "gài" phổ biến)
- **CORS** (Cross-Origin Resource Sharing): trình duyệt chặn JS gọi API khác origin. Frontend chạy `localhost:5173`, API `localhost:8080` → khác origin → backend phải khai báo `allowed-origins` cho phép. Đã cấu hình allowlist cụ thể (không dùng `*`).
- **Tại sao tắt CSRF (`csrf.disable()`)?** CSRF khai thác việc trình duyệt **tự động gửi cookie/session**. Hệ thống dùng JWT đặt trong header `Authorization` do JS chủ động gắn — trình duyệt không tự gửi → bề mặt tấn công CSRF không tồn tại → tắt là chuẩn cho stateless API. (Đây là câu trả lời mẫu — học thuộc.)

## 2.6. Frontend: React 18 + Vite + Ant Design 5

- **React:** thư viện UI theo component, virtual DOM, one-way data flow. Bạn dùng **function component + hooks** (`useState` quản lý state, `useEffect` gọi API khi mount/đổi dependency).
- **SPA (Single Page Application):** tải 1 trang HTML, điều hướng bằng **react-router-dom v6** đổi component theo URL không reload trang; ~31 trang (List/Form/Detail cho từng nghiệp vụ).
- **Vite:** công cụ dev server + build. Dev nhanh nhờ native ES modules + esbuild (không bundle lại toàn bộ khi sửa code như Webpack); build production bằng Rollup. Trả lời "tại sao Vite không phải CRA": CRA đã ngừng phát triển, Vite khởi động và HMR nhanh hơn nhiều.
- **Ant Design 5:** thư viện UI component cho ứng dụng quản trị (Table, Form, Modal, DatePicker...) — chọn vì tối ưu cho admin dashboard, form và bảng dữ liệu dày đặc đúng tính chất hệ thống nghiệp vụ.
- **Axios:** HTTP client; điểm kỹ thuật đáng nói là **interceptor**: request interceptor tự gắn JWT từ localStorage vào header mọi request; response interceptor xử lý format `ApiResponse` chung và khi 401 thì xóa token + đá về trang login.
- **Recharts:** vẽ biểu đồ doanh thu trên Dashboard. **dayjs:** xử lý ngày giờ (nhẹ hơn moment).

**Virtual DOM (nếu bị hỏi):** React giữ bản sao DOM trong bộ nhớ; khi state đổi, so sánh (diffing) cây mới với cây cũ và chỉ cập nhật phần DOM thật thay đổi → nhanh hơn thao tác DOM trực tiếp toàn bộ.

---

# PHẦN 3 — KIẾN TRÚC BACKEND CHI TIẾT (trả lời "trình bày kiến trúc hệ thống")

## 3.1. Layered Architecture (kiến trúc phân tầng)

```
Request → [Controller] → [Service (interface) → ServiceImpl] → [Repository] → DB
              │                  │
           DTO ↔ (mapping) ↔ Entity
```

| Tầng | Vai trò | Ví dụ trong code |
|---|---|---|
| **Controller** | Nhận HTTP, validate input (`@Valid`), gọi service, trả `ApiResponse<T>` thống nhất | `PurchaseOrderController`, `InventoryController` |
| **Service** | TOÀN BỘ logic nghiệp vụ + ranh giới transaction (`@Transactional`) | `GoodsIssueServiceImpl` (FEFO), `DashboardServiceImpl` |
| **Repository** | Truy cập dữ liệu (Spring Data JPA) | `InventoryLotRepository` (query FEFO) |
| **Model/Entity** | Ánh xạ bảng CSDL | `InventoryLot`, `SalesOrder` |
| **DTO** | Hợp đồng dữ liệu vào/ra API | `SalesOrderDTO`, `DashboardDTO` |

**Tại sao tách Service interface + Impl?** Lập trình hướng interface: dễ thay thế triển khai, dễ mock khi test, giảm coupling.

**Tại sao cần DTO, không trả thẳng Entity?** (câu hỏi rất hay gặp)
1. **Bảo mật:** không lộ field nội bộ (password hash, audit field).
2. **Tránh lazy loading exception / vòng lặp JSON** khi serialize entity có quan hệ hai chiều.
3. **Tách hợp đồng API khỏi schema DB:** đổi cấu trúc bảng không vỡ API.
4. Gộp/định dạng dữ liệu theo nhu cầu màn hình (vd. DTO dashboard).

## 3.2. Xử lý lỗi tập trung

- `GlobalExceptionHandler` với `@RestControllerAdvice`: bắt exception toàn cục, trả JSON lỗi thống nhất + HTTP status đúng (404 cho `ResourceNotFoundException`, 400 cho `BusinessException`/validation, 403 cho `UnauthorizedOperationException`).
- Exception nghiệp vụ tự định nghĩa: `BusinessException`, `InventoryException` (thiếu tồn), `InvalidStatusTransitionException` (chuyển trạng thái sai), `ResourceNotFoundException`.
- **Lợi ích:** controller sạch (không try-catch lặp lại), client luôn nhận format lỗi nhất quán.

## 3.3. AOP (Aspect-Oriented Programming) — điểm cộng kỹ thuật

- **Khái niệm:** tách "mối quan tâm cắt ngang" (cross-cutting concern) ra khỏi logic chính. Thuật ngữ: **Aspect** (class chứa logic cắt ngang), **Advice** (`@Around` — code chạy quanh method), **Pointcut** (biểu thức chọn method nào bị áp dụng), **JoinPoint** (điểm thực thi cụ thể).
- **Trong hệ thống:** `DataFilterAspect` dùng pointcut theo **kiểu trả về** (`execution(java.util.List<SalesOrderDTO> com.distribution.service.*.get*(..))`) để lọc đơn chưa duyệt khỏi kết quả khi user là thủ kho — không phải sửa từng method service.
- Lưu ý nếu bị hỏi: bản thân `@Transactional` của Spring cũng hoạt động bằng AOP proxy — đây là lý do gọi method `@Transactional` từ chính class đó (self-invocation) sẽ không qua proxy, transaction không áp dụng.

## 3.4. Tác vụ định kỳ (Scheduling)

- `@EnableScheduling` + `OverdueInvoiceScheduler`: cron `0 0 1 * * *` (01:00 hằng ngày) quét hóa đơn ISSUED/PARTIALLY_PAID quá `dueDate` → bulk update sang **OVERDUE**. Cron override được qua property `app.overdue-check.cron`.

## 3.5. Chuẩn API & tài liệu

- REST theo resource: `/api/purchase-orders`, `/api/inventory`, `/api/delivery-trips`... HTTP method đúng ngữ nghĩa (GET đọc, POST tạo, PUT sửa/hành động, DELETE xóa). Hành động nghiệp vụ là sub-resource: `PUT /api/purchase-orders/{id}/approve`.
- Response bọc trong `ApiResponse<T>` (success, message, data) thống nhất.
- **Swagger/OpenAPI** (springdoc): tự sinh tài liệu API từ annotation, có UI test trực tiếp.

---

# PHẦN 4 — LOGIC NGHIỆP VỤ CỐT LÕI TRONG CODE (phần dễ bị "xoáy" nhất)

## 4.1. Mô hình tồn kho 2 cấp: Inventory + InventoryLot

- **`Inventory`** (cấp tổng hợp): mỗi cặp (product, warehouse) một dòng — `quantityOnHand` (tồn vật lý), `quantityAvailable` (tồn khả dụng), `reorderLevel` (ngưỡng đặt hàng lại).
- **`InventoryLot`** (cấp lô): mỗi lần nhập tạo lô với `lotNumber`, `expiryDate`, `quantityRemaining`, liên kết về `GoodsReceiptItem` nguồn (có check **idempotency** `existsBySourceReceiptItemId` — confirm 2 lần không tạo lô trùng).
- **`InventoryTransaction`** (nhật ký): mọi biến động Nhập/Xuất/Điều chỉnh đều ghi log — trả lời cho câu "làm sao truy vết tồn kho sai ở đâu".

**Phân biệt `quantityOnHand` vs `quantityAvailable` (hay bị hỏi):** OnHand là hàng vật lý trong kho; Available là phần có thể bán/hứa với khách (ATP — Available To Promise) sau khi trừ phần đã cam kết. Khi tạo SO, hệ thống kiểm tra Available, không phải OnHand.

## 4.2. FEFO — First Expired, First Out (điểm nhấn số 1 của đồ án)

**Khái niệm:** xuất lô có **hạn sử dụng gần nhất trước** — khác FIFO (nhập trước xuất trước, theo thời gian nhập) và LIFO. Với ngành hàng có HSD (bánh kẹo, thực phẩm, dược) FEFO là bắt buộc để giảm hàng hết hạn phải hủy.

**Triển khai thực tế** (`InventoryLotRepository.findAvailableLotsFEFO` — thuộc query này):

```sql
SELECT l FROM InventoryLot l
WHERE l.product.id = :productId AND l.warehouse.id = :warehouseId
  AND l.quantityRemaining > 0
  AND (l.expiryDate IS NULL OR l.expiryDate >= CURRENT_DATE)   -- loại lô đã hết hạn
ORDER BY (CASE WHEN l.expiryDate IS NULL THEN 1 ELSE 0 END),   -- lô không HSD xếp cuối
         l.expiryDate ASC, l.id ASC                            -- HSD gần nhất trước, tie-break theo id
```

**Luồng xuất kho theo FEFO** (`GoodsIssueServiceImpl.confirm`):
1. Validate trạng thái GI (DRAFT mới được confirm) và SO đã duyệt.
2. Lấy danh sách lô khả dụng theo thứ tự FEFO.
3. Duyệt từng lô, trừ dần `quantityRemaining` cho đến khi đủ số lượng xuất; thiếu → ném `InventoryException`, rollback toàn bộ.
4. Giảm `Inventory` tổng hợp, ghi `InventoryTransaction`, cập nhật trạng thái SO.

**Câu hỏi xoáy dự kiến:** "Nếu khách muốn xuất đích danh một lô thì sao?" → Hệ thống cho phép chỉ định lô trong chi tiết phiếu xuất; nếu không chỉ định thì tự động phân bổ FEFO (code có nhánh "có lot data → validate và trừ theo FEFO").

## 4.3. Quản lý hàng hết hạn & xuất hủy (Lot Disposal)

- Query riêng cho: lô **sắp hết hạn** trong N ngày (`findExpiringSoon`) → cảnh báo; lô **đã hết hạn còn tồn** (`findExpired`, `sumExpiredQuantity`) → hiển thị "tồn chờ hủy", **không tính vào tồn khả dụng** (query FEFO đã loại).
- **`LotDisposal`** (migration V8): nghiệp vụ xuất hủy lô hết hạn — ghi ai hủy, khi nào, số lượng → lịch sử hủy hàng phục vụ kiểm soát.
- Tinh tế đáng nói: phân biệt "sản phẩm không quản lý theo lô" với "chỉ còn lô hết hạn" (`hasLotsWithStock`).

## 4.4. Concurrency — chống tranh chấp tồn kho (câu hỏi khó nhất, chuẩn bị kỹ)

**Tình huống:** 2 nhân viên cùng confirm 2 phiếu xuất cho cùng sản phẩm, tồn chỉ đủ 1 phiếu → nếu cả 2 cùng đọc tồn cũ rồi cùng trừ → **tồn âm / oversell** (race condition kiểu lost update).

**Giải pháp trong code:** **Pessimistic Locking** —

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.warehouse.id = :warehouseId")
Optional<Inventory> findByProductIdAndWarehouseIdForUpdate(...);
```

→ Hibernate sinh `SELECT ... FOR UPDATE`: transaction A giữ row lock trên dòng tồn kho; transaction B phải **chờ** A commit/rollback rồi mới đọc được giá trị mới nhất → kiểm tra tồn và trừ tồn là nguyên tử.

**So sánh phải thuộc:**
| | Pessimistic Lock | Optimistic Lock (`@Version`) |
|---|---|---|
| Cơ chế | Khóa row ở DB (`SELECT FOR UPDATE`), ai đến sau phải chờ | Không khóa; check cột version khi update, lệch → ném `OptimisticLockException`, retry |
| Phù hợp | Tranh chấp **cao**, nghiệp vụ không được phép fail (trừ kho) | Tranh chấp thấp, chấp nhận retry |
| Nhược | Giảm throughput, nguy cơ deadlock nếu khóa nhiều row sai thứ tự | Phải xử lý retry ở tầng app |

**Tại sao chọn pessimistic?** Tồn kho là điểm nóng tranh chấp, sai = bán hàng không có thật; chấp nhận chờ vài ms để đảm bảo đúng tuyệt đối.

## 4.5. State machine — quản lý trạng thái chứng từ

Mỗi chứng từ có vòng đời trạng thái và **luật chuyển trạng thái được enforce trong enum** (method kiểu `canApprove()`, `canConfirm()`, `canCancel()`), vi phạm → `InvalidStatusTransitionException`:

- **PO/SO:** `ORDER_OPEN → ORDER_APPROVED → ORDER_PARTIALLY_RECEIVED/DELIVERED → ORDER_COMPLETED` (hoặc `ORDER_CANCELLED`). Nhập/xuất **một phần** được hỗ trợ: nhận thiếu → PARTIALLY_RECEIVED, đủ → COMPLETED.
- **GR/GI:** `DRAFT → CONFIRMED` (mới đụng tồn kho) hoặc `CANCELLED`.
- **Invoice:** `DRAFT → ISSUED → PAID / PARTIALLY_PAID / OVERDUE / CANCELLED` (OVERDUE do scheduler tự đánh).

**Ý nghĩa khi trả lời:** "trạng thái không phải chỉ là cái nhãn hiển thị — nó là **guard** đảm bảo quy trình nghiệp vụ đúng thứ tự: không thể nhập kho cho PO chưa duyệt, không thể xuất kho cho SO chưa duyệt, không thể sửa phiếu đã confirm."

## 4.6. Gợi ý mua hàng (Purchase Suggestions) — yếu tố "thông minh"

- Mỗi `Inventory` có `reorderLevel`; API `/api/inventory/needing-reorder` và `/api/inventory/purchase-suggestions` tìm mặt hàng tồn ≤ ngưỡng hoặc hết hàng, **gom theo nhà cung cấp** → gợi ý lập PO.
- Khi bị hỏi "thông minh ở đâu": trả lời thẳng thắn — "thông minh" ở mức **tự động hóa hỗ trợ ra quyết định dựa trên luật** (reorder point, FEFO, cảnh báo HSD, tự đánh OVERDUE, dashboard phân tích), chưa phải machine learning; hướng phát triển là dự báo nhu cầu (demand forecasting) từ dữ liệu bán hàng lịch sử.

## 4.7. Dashboard & Báo cáo

- `DashboardServiceImpl` tổng hợp: doanh thu (theo ngày/tháng, lọc khoảng thời gian), số đơn, giá trị tồn, công nợ, top sản phẩm bán chạy, **báo cáo Nhập-Xuất-Tồn** (tồn đầu kỳ + nhập − xuất = tồn cuối kỳ, tính từ `InventoryTransaction`), công nợ khách hàng + hóa đơn quá hạn.
- Frontend vẽ bằng Recharts, trang `DashboardPage` 3 tab.

---

# PHẦN 5 — NGÂN HÀNG CÂU HỎI PHẢN BIỆN + GỢI Ý TRẢ LỜI

## Nhóm A — Tổng quan & lý do lựa chọn

**A1. Vì sao chọn kiến trúc monolith mà không phải microservices?**
> Quy mô bài toán là một doanh nghiệp phân phối với các module nghiệp vụ liên kết chặt (đơn hàng ↔ kho ↔ hóa đơn cần transaction chung). Monolith cho phép đảm bảo tính nhất quán dữ liệu bằng transaction CSDL đơn giản, chi phí vận hành thấp, phù hợp team 1 người và thời gian đồ án. Microservices giải bài toán scale tổ chức/hệ thống lớn nhưng trả giá bằng distributed transaction (saga), eventual consistency, vận hành phức tạp — over-engineering cho phạm vi này. Tuy nhiên code đã phân tầng và chia module rõ (purchase/sales/inventory/delivery) nên tách service sau này khả thi.

**A2. Hệ thống khác gì Excel / phần mềm có sẵn (MISA, KiotViet)?**
> Khác Excel: dữ liệu tập trung, ràng buộc toàn vẹn, phân quyền, nhiều người dùng đồng thời không ghi đè nhau, truy vết. Khác phần mềm đóng gói: tùy biến quy trình theo đặc thù (FEFO theo lô, quy trình duyệt riêng, phân chuyến giao hàng), và mục tiêu đồ án là làm chủ công nghệ xây dựng hệ thống nghiệp vụ.

**A3. Đóng góp/điểm mới của đồ án là gì?**
> Tích hợp trọn vẹn chuỗi mua–bán–kho–giao vận trong một hệ thống; quản lý tồn kho theo lô + HSD với xuất FEFO tự động và nghiệp vụ xuất hủy hàng hết hạn; xử lý đồng thời an toàn bằng pessimistic locking; RBAC 9 vai trò 3 lớp (URL, method, data-level bằng AOP).

## Nhóm B — Backend & kiến trúc

**B1. Trình bày luồng một request từ client đến DB?**
> Axios gắn JWT → JwtAuthenticationFilter verify token, set SecurityContext → SecurityConfig kiểm tra quyền theo URL/method → Controller nhận DTO, `@Valid` → Service xử lý nghiệp vụ trong `@Transactional` → Repository (Spring Data JPA) sinh SQL qua Hibernate → PostgreSQL → kết quả map về DTO → bọc `ApiResponse` trả JSON.

**B2. `@Transactional` đặt ở đâu, tại sao? Khi nào rollback?**
> Đặt ở tầng Service vì một nghiệp vụ (confirm phiếu xuất) gồm nhiều thao tác repository phải nguyên tử. Mặc định rollback với unchecked exception; các exception nghiệp vụ của em kế thừa RuntimeException nên lỗi là rollback toàn bộ. Method chỉ đọc dùng `readOnly = true` để tối ưu (Hibernate bỏ dirty checking).

**B3. N+1 query là gì, gặp chưa, xử lý sao?**
> (Định nghĩa như mục 2.2.) Trong dự án, các màn danh sách map sang DTO; chỗ tổng hợp như dashboard dùng query JPQL aggregate (SUM/GROUP BY) trả thẳng kết quả thay vì load từng entity — ví dụ `sumExpiredQuantityGrouped()` gom tồn hết hạn theo (product, warehouse) trong 1 query thay vì N query.

**B4. Sao không dùng MapStruct/ModelMapper?**
> Mapping thủ công trong service — ưu điểm là tường minh, kiểm soát được; nhược là dài dòng. Với số lượng DTO hiện tại chấp nhận được; MapStruct là cải tiến hợp lý nếu mở rộng.

**B5. Validation nằm ở đâu?**
> 2 lớp: Bean Validation (`@Valid`, `@NotNull`...) ở DTO chặn dữ liệu sai định dạng ngay tại controller; validation nghiệp vụ (đủ tồn không, trạng thái có cho phép không, HSD hợp lệ không) ở service vì cần truy vấn DB. DB còn lớp ràng buộc cuối: FK, NOT NULL, UNIQUE.

## Nhóm C — Cơ sở dữ liệu

**C1. Thiết kế CSDL có bao nhiêu bảng, chuẩn hóa thế nào?**
> ~25 bảng chính theo mẫu header-detail (master-detail): `purchase_orders`–`purchase_order_items`, `sales_orders`–`sales_order_items`, `goods_receipts/issues`–items, `sales_invoices`–items, cùng master data (products, customers, suppliers, warehouses, users, roles) và kho (inventory, inventory_lots, inventory_transactions, lot_disposals), giao vận (delivery_plans/trips/orders). Đạt 3NF: thông tin sản phẩm không lặp trong chi tiết đơn (FK tới products); riêng **đơn giá tại thời điểm bán được snapshot vào item** — đây là denormalization có chủ đích vì giá thay đổi theo thời gian, hóa đơn phải giữ giá lịch sử.

**C2. Vì sao tách `inventory` và `inventory_lots`?**
> `inventory` cho truy vấn nhanh "tồn bao nhiêu" (1 dòng/sản phẩm/kho, có lock khi update); `inventory_lots` cho yêu cầu truy xuất nguồn gốc lô + HSD + FEFO. Gộp một bảng thì mọi truy vấn tồn phải SUM toàn bộ lô — chậm và khó lock.

**C3. Transaction isolation level dùng gì?**
> READ COMMITTED mặc định của PostgreSQL; bài toán lost update trên tồn kho xử lý bằng pessimistic lock (`SELECT FOR UPDATE`) thay vì nâng isolation lên SERIALIZABLE (giá quá đắt về throughput).

**C4. Index có những gì?** *(đi xem lại file migration V1–V8 trước buổi bảo vệ!)*
> PK/FK index mặc định; trả lời nguyên tắc: cột hay xuất hiện trong WHERE/JOIN/ORDER BY cần index — vd (product_id, warehouse_id) trên inventory_lots, status + ngày trên các bảng chứng từ. Nếu chưa có index nào đó, trả lời: "với khối lượng dữ liệu demo chưa nghẽn; đưa vào hướng phát triển khi dữ liệu lớn, kèm EXPLAIN ANALYZE để chọn index."

## Nhóm D — Bảo mật (hỏi dồn dập nhất)

**D1. JWT gồm những gì? Server làm sao biết token không bị giả mạo?** → mục 2.5 (chữ ký HMAC với secret chỉ server biết; sửa payload thì chữ ký không khớp).

**D2. Token bị đánh cắp thì sao? Logout thế nào khi stateless?**
> Thừa nhận đúng nhược điểm: JWT đã phát không thu hồi được cho đến khi hết hạn; giảm thiểu bằng thời hạn access token ngắn + refresh token, HTTPS khi triển khai. Logout phía client là xóa token khỏi localStorage. Giải pháp triệt để (hướng phát triển): token blacklist/Redis, hoặc rotate refresh token và lưu refresh token trong DB để thu hồi.

**D3. Lưu token ở localStorage có an toàn không?**
> Trung thực: localStorage tiện nhưng đọc được bằng JS → rủi ro nếu có XSS. Phương án an toàn hơn là httpOnly cookie (JS không đọc được) nhưng lại phải bật chống CSRF. Em chọn localStorage + JWT header cho phù hợp kiến trúc SPA, giảm thiểu XSS nhờ React tự escape output; đây là trade-off em nắm rõ.

**D4. SQL injection có phòng không?**
> Có — toàn bộ truy vấn qua JPA/JPQL với named parameter (`:productId`), Hibernate dùng PreparedStatement nên input luôn được bind tham số, không nối chuỗi SQL.

**D5. Phân quyền kiểm tra ở những đâu? Nếu chỉ ẩn nút trên frontend thì sao?**
> Frontend ẩn menu/nút chỉ là UX; **enforcement thật ở backend** 3 lớp (URL rules, method security, AOP data filter). Gọi thẳng API bằng curl không có quyền vẫn bị 403.

**D6. Mật khẩu lưu thế nào? Vì sao BCrypt?** → mục 2.5 (salt + cost factor, so với MD5/SHA).

## Nhóm E — Nghiệp vụ & concurrency

**E1. Hai người cùng xuất một sản phẩm, tồn chỉ đủ một người — chuyện gì xảy ra?** → trả lời theo mục 4.4, nói rõ `SELECT FOR UPDATE`, người sau chờ rồi đọc tồn mới → thiếu hàng → exception, rollback. Đây là câu ăn điểm nhất, tập trả lời trơn tru.

**E2. FEFO là gì, khác FIFO? Triển khai trong code thế nào?** → mục 4.2, đọc thuộc query.

**E3. Nhập kho một phần xử lý sao?**
> GR liên kết PO; mỗi item ghi số lượng thực nhận; tổng nhận < tổng đặt → PO sang PARTIALLY_RECEIVED, có thể tạo GR tiếp; đủ → COMPLETED. Tương tự cho xuất một phần phía bán.

**E4. Hủy phiếu xuất đã confirm thì tồn kho thế nào?**
> Theo state machine, phiếu đã CONFIRMED không sửa được; nghiệp vụ điều chỉnh đi theo chứng từ ngược (điều chỉnh kho/nhập trả) để giữ audit trail — không xóa/sửa lịch sử.

**E5. Sao biết được tồn kho hiện tại đúng?**
> Mọi biến động qua `InventoryTransaction`; tồn = tồn đầu + Σnhập − Σxuất, đối chiếu được bằng báo cáo Nhập-Xuất-Tồn; tồn tổng hợp và tổng lô được cập nhật trong cùng transaction nên không lệch nhau.

## Nhóm F — Frontend

**F1. State management dùng gì? Sao không Redux?**
> `useState`/`useEffect` + state cục bộ theo trang; dữ liệu server fetch qua axios mỗi trang. Quy mô state chia sẻ toàn cục nhỏ (user đăng nhập — đọc từ localStorage) nên Redux là không cần thiết; nếu app phình to sẽ cân nhắc React Query/Redux Toolkit.

**F2. Phân quyền hiển thị trên frontend làm sao?**
> Sau login lưu user + roles; menu/route/nút render có điều kiện theo role (`roleService.js`); shipper chỉ thấy trang chuyến giao được gán. Nhấn mạnh: đây chỉ là UX, backend mới là chốt chặn.

## Nhóm G — Kiểm thử & triển khai (điểm yếu — chuẩn bị câu trả lời khéo)

**G1. Đã kiểm thử thế nào?**
> Trung thực + chủ động: kiểm thử thủ công theo kịch bản end-to-end toàn luồng PO→GR→SO→GI→Invoice, các ca biên (xuất quá tồn, duyệt sai trạng thái, sai quyền — kỳ vọng 400/403), kiểm tra số liệu dashboard khớp dữ liệu gốc; API test qua Swagger. **Thừa nhận chưa có automated test** (unit/integration) và nêu là hạn chế + hướng phát triển (JUnit + MockMvc + Testcontainers cho PostgreSQL). Đừng để bị "bắt quả tang" nói có test mà repo không có thư mục test.

**G2. Triển khai production cần làm gì?**
> Build jar (`mvn package`) + `vite build` ra static files; những việc phải làm thêm: chuyển secret/password ra biến môi trường (hiện đang nằm trong `application.yml` — nhận là hạn chế), bật HTTPS, tắt `show-sql`, cấu hình CORS theo domain thật, Docker hóa, backup DB định kỳ.

---

# PHẦN 6 — ĐIỂM YẾU CỦA HỆ THỐNG: BIẾT TRƯỚC ĐỂ KHÔNG BỊ ĐỘNG

Nguyên tắc: **không giấu — chủ động nhận diện + nêu hướng khắc phục.** Hội đồng đánh giá cao sinh viên hiểu hạn chế của chính mình.

1. **Secret JWT & mật khẩu DB hardcode trong `application.yml`** → đúng ra dùng biến môi trường/Vault. Trả lời: "em để trong file cấu hình cho môi trường phát triển; production sẽ externalize qua env var."
2. **Không có automated tests** (repo không có `src/test`) → hạn chế đã nêu, hướng phát triển.
3. **Flyway không thực chạy** (yml bật nhưng pom thiếu `flyway-core`; migration apply tay bằng psql) → nói "schema quản lý phiên bản theo chuẩn Flyway, apply thủ công" — đừng demo câu "Flyway tự migrate khi khởi động".
4. **Token trong localStorage** → trade-off XSS vs CSRF, đã chuẩn bị câu trả lời (D3).
5. **JWT không thu hồi được trước hạn** → access 24h là khá dài; hướng cải thiện: rút ngắn còn 15–30 phút + refresh rotation/blacklist.
6. **Chưa có phân trang (pagination) nhất quán** cho các API danh sách (đa số trả List đầy đủ) → dữ liệu lớn sẽ chậm; hướng cải thiện: `Pageable` của Spring Data.
7. **Phân hệ kế toán (bút toán tự động) ngoài phạm vi** → đã ghi rõ trong phạm vi đồ án, không phải "làm thiếu".
8. **"Thông minh" chưa có ML** → định vị đúng: tự động hóa dựa trên luật; demand forecasting là hướng phát triển.

---

# PHẦN 7 — CHECKLIST ÔN TẬP 1 TUẦN TRƯỚC BẢO VỆ

**Phải làm:**
- [ ] Tự chạy lại toàn bộ luồng demo: login từng role → tạo PO → duyệt → GR (nhập lô + HSD) → tạo SO → duyệt → GI (xem FEFO chọn lô nào) → Invoice → Dashboard. Ghi lại mật khẩu các tài khoản demo.
- [ ] Mở và đọc lướt 5 file then chốt để "nói có sách": `SecurityConfig.java`, `JwtAuthenticationFilter.java`, `GoodsIssueServiceImpl.confirm()` (dòng ~233–300), `InventoryLotRepository.findAvailableLotsFEFO`, `DataFilterAspect.java`.
- [ ] Đọc lại migration V1–V8 để thuộc danh sách bảng + các index/constraint chính.
- [ ] Vẽ tay được 3 sơ đồ không nhìn tài liệu: (1) kiến trúc tổng thể, (2) luồng JWT login→request, (3) luồng PO→GR→SO→GI→Invoice kèm trạng thái.
- [ ] Tập trả lời to thành tiếng 5 câu: A1 (monolith), B1 (luồng request), D1 (JWT), E1 (concurrency), E2 (FEFO).
- [ ] Chuẩn bị phương án demo offline (video/screenshot) phòng khi máy lỗi.

**Thuộc lòng các con số:**
- Spring Boot **3.1.4**, Java **17**, jjwt **0.12.3**, React **18**, Vite **5**, Ant Design **5**, PostgreSQL, port BE **8080** / FE **5173**.
- JWT: access **24h**, refresh **7 ngày**, thuật toán **HS256**.
- **9 vai trò** RBAC; **8 file migration** (V1 purchasing, V2 sales, V3 security/RBAC, V4 sample data, V5 inventory lot, V6 fix passwords, V7 fix FK, V8 lot disposal).
- ~**157 file Java** backend, ~**31 trang** React.
- Scheduler quét hóa đơn quá hạn: **01:00 hằng ngày**.
