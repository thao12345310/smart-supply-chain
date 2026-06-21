# 1-PAGER · Bảo mật — Authentication (JWT) · Authorization (RBAC) · BCrypt · CORS/CSRF

> Bổ trợ phần sâu cho **§5.5 (RBAC)** và phần bảo mật trong `ON_TAP_PHAN_BIEN_DATN.md` (PHẦN 8). Mục tiêu: **nói lại được, không nhìn note.**
> Code thật để dẫn chứng:
> `security/JwtTokenProvider.java` · `security/JwtAuthenticationFilter.java` · `security/CustomUserDetailsService.java` ·
> `config/SecurityConfig.java` · `service/AuthService.java` · `security/DataFilterAspect.java` · controllers có `@PreAuthorize`.

---

## ⏱ Trả lời 30 giây (học thuộc cái khung này)

> "Bảo mật của em tách **2 pha**. **Xác thực (authentication)**: lúc login, `AuthenticationManager` so mật khẩu bằng **BCrypt**; đúng thì phát một **JWT ký HS256** (khóa đối xứng). **Phân quyền (authorization)**: mỗi request sau, một `OncePerRequestFilter` đọc token ở header `Authorization: Bearer`, kiểm chữ ký, rồi **chỉ lấy username** và **nạp lại role từ DB** — KHÔNG tin role nằm trong token. Quyền được chặn ở **3 lớp**: endpoint (`SecurityConfig`), method (`@PreAuthorize` trên controller), và data (một AOP aspect lọc danh sách theo vai trò). Session **stateless** nên em **tắt CSRF** (an toàn vì token ở header, không phải cookie tự gửi), và bật **CORS** whitelist origin của frontend."

> ⚡ **Câu thần chú:** *Token để biết **bạn là AI** (authentication). Được làm **GÌ** (authorization) thì server luôn hỏi lại **DB + RBAC**.*

---

## 1) Hai pha tách biệt — đừng lẫn

| | **Authentication** (xác thực) | **Authorization** (phân quyền) |
|---|---|---|
| Trả lời | "Bạn là ai?" | "Bạn được làm gì?" |
| Khi nào | 1 lần lúc **login** | **mỗi** request |
| Bằng gì | BCrypt so mật khẩu → phát JWT | Token hợp lệ → nạp role DB → RBAC chặn |
| Code | `AuthService.login()` | `JwtAuthenticationFilter` + `SecurityConfig` |

---

## 2) JWT — ký bằng gì, trong token có gì

**Code:** `JwtTokenProvider.java`

- **HS256 = HMAC-SHA256**, khóa **đối xứng** (1 khóa bí mật vừa ký vừa kiểm):
```java
// dòng 25 — khóa bí mật (có giá trị MẶC ĐỊNH nếu env không set ⚠️)
@Value("${app.jwt.secret:distribution-logistics-secret-key-for-jwt-authentication-2024}")
private String jwtSecret;
// dòng 43 — dựng SecretKey cho HMAC
this.key = Keys.hmacShaKeyFor(paddedSecret.getBytes(StandardCharsets.UTF_8));
```
- **Phát token (dòng 65–73):**
```java
return Jwts.builder()
    .subject(userDetails.getUsername())     // ai
    .claim("userId", ...).claim("fullName", ...).claim("roles", roles)
    .issuedAt(now).expiration(expiryDate)   // hết hạn 24h (dòng 28)
    .signWith(key)                          // ký HS256
    .compact();
```
- Token = `header.payload.signature`. **Payload chỉ Base64, KHÔNG mã hóa** → ai cũng đọc được. Cái bảo vệ là **chữ ký**: sửa payload mà không có khóa → chữ ký sai.
- **Kiểm (dòng 132–151):** `parseSignedClaims` ném `ExpiredJwtException` / `SecurityException` (chữ ký sai) / `MalformedJwtException`… → `validateToken` trả `false`.

> 🎯 *"JWT ký HS256 bằng khóa đối xứng. Payload không bí mật, chỉ chống sửa nhờ chữ ký HMAC; hết hạn 24h."*

---

## 3) ⭐ Xác thực mỗi request — quyền lấy từ DB, KHÔNG từ token

**Đây là câu giám khảo hay gài.** Token CÓ claim `roles` → dễ trả lời nhầm "lấy từ token". **SAI.**

**Code:** `JwtAuthenticationFilter.doFilterInternal()`
```java
String jwt = getJwtFromRequest(request);                 // dòng 36: cắt "Bearer " (dòng 65–67)
if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) { // dòng 38: kiểm chữ ký
    String username = tokenProvider.getUsernameFromToken(jwt);      // dòng 39: CHỈ lấy username
    UserDetails userDetails = userDetailsService.loadUserByUsername(username); // dòng 41: NẠP TỪ DB
    var auth = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities());           // dòng 46: quyền TỪ DB
    SecurityContextHolder.getContext().setAuthentication(auth);     // dòng 50
}
```
`loadUserByUsername` (`CustomUserDetailsService` dòng 28) chạy `findByUsernameWithRoles` → **truy vấn DB mỗi request**, và chặn nếu `!user.isActive()` (dòng 32).

> 🎯 **Ăn điểm:** *"Em không tin role trong token. Filter chỉ lấy username rồi nạp role từ DB mỗi request → admin thu hồi quyền là có hiệu lực ngay, không đợi token hết hạn."*

**Endpoint công khai** không qua filter (`shouldNotFilter`, dòng 76–82): `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs`, `/actuator/**`.

---

## 4) BCrypt + salt — vì sao không lưu mật khẩu thường

**Code:** `SecurityConfig` dòng 288 `new BCryptPasswordEncoder()`; tạo user `AuthService` dòng 106 `passwordEncoder.encode(...)`; login `authenticationManager.authenticate(...)` (dòng 49) → `DaoAuthenticationProvider` (SecurityConfig dòng 253–258) **tự gọi BCrypt `.matches()`**.

3 ý phải thuộc:
1. **Hash một chiều** — không giải ngược ra mật khẩu.
2. **Salt tự sinh, nhúng trong chuỗi hash** → 2 người cùng mật khẩu `123456` ra **2 hash khác nhau** → chống **rainbow table**.
3. **Cố tình CHẬM** (work factor mặc định 10 = 2¹⁰ vòng) → chống **brute-force**.

Định dạng hash: `$2a$10$<22 ký tự salt><31 ký tự hash>` — salt nằm ngay trong chuỗi nên khi login chỉ cần lấy ra so.

> 🎯 *"Mật khẩu lưu BCrypt — hash một chiều, salt nhúng sẵn chống rainbow table, chậm có chủ đích chống dò vét."*

---

## 5) RBAC — 9 vai trò, chặn ở 3 LỚP

`@EnableWebSecurity` + `@EnableMethodSecurity(prePostEnabled=true)` (`SecurityConfig` dòng 49–50).

**Lớp 1 — Endpoint** (`SecurityConfig.authorizeHttpRequests`, dòng 72–243): chặn theo **URL + HTTP method**:
```java
.requestMatchers(HttpMethod.PUT, "/api/purchase-orders/*/approve")
    .hasAnyRole("PURCHASE_MANAGER", "ACCOUNTANT", "ADMIN")   // dòng 99–100
.requestMatchers("/api/users/**").hasRole("ADMIN")          // dòng 85
.anyRequest().authenticated()                               // mặc định: phải đăng nhập
```

**Lớp 2 — Method** (`@PreAuthorize` viết thẳng trên controller, **24 chỗ**: SalesOrder, PurchaseOrder, DeliveryTrip, Accounting, User, Payment, Metrics, Auth):
```java
// SalesOrderController — duyệt đơn bán
@PreAuthorize("hasAnyRole('SALES_MANAGER', 'ACCOUNTANT', 'ADMIN')")
public ... approveSalesOrder(...) { ... }
```

**Lớp 3 — Data (theo ngữ cảnh)** (`DataFilterAspect`, AOP `@Around`): lọc **từng dòng dữ liệu** theo vai trò — *nhân viên kho không thấy đơn CHƯA duyệt*, *shipper chỉ thấy chuyến được giao*:
```java
@Around("execution(java.util.List<...SalesOrderDTO> ...service.*.get*(..))")
public Object filterSalesOrdersForWarehouse(ProceedingJoinPoint jp) {
    List<SalesOrderDTO> orders = (List<SalesOrderDTO>) jp.proceed();
    if (SecurityUtils.hasRole("WAREHOUSE_STAFF") && !SecurityUtils.isAdmin())
        return orders.stream().filter(o -> isApprovedOrLater(o.getStatus()))...;
    return orders;
}
```

> **Vì sao cần 3 lớp?** Endpoint chặn *"vào được URL không"*; method chặn *"gọi được hàm không"*; data chặn *"thấy được dòng nào"*. Lớp 1–2 là **coarse-grained** (theo vai trò), lớp 3 là **fine-grained** (theo quan hệ sở hữu dữ liệu) — cái mà role đơn thuần không diễn đạt được.

> 🔴 **Tự nhận (trung thực):** file `SecurityAnnotations.java` định nghĩa 8 annotation tái dùng (`@RequireManager`, `@RequireWarehouseAccess`…) nhưng **chưa gắn lên method nào** (code chết) → method-level hiện dùng `@PreAuthorize` viết thẳng (lặp chuỗi role). *Hướng cải thiện: thay bằng các annotation tái dùng đó cho DRY.*

---

## 6) CORS — là gì, KHÔNG là gì

**Code:** `SecurityConfig.corsConfigurationSource()` dòng 266–284.
```java
List<String> origins = ... allowedOrigins.split(",");   // dòng 62: từ env, vd Vercel + localhost:5173
configuration.setAllowedOrigins(origins);
configuration.setAllowedMethods(GET/POST/PUT/DELETE/OPTIONS/PATCH);
configuration.setAllowCredentials(true);                // dòng 278
```
- CORS = quy tắc **trình duyệt** quyết định JS từ **origin lạ** có được đọc kết quả API của bạn không. Bạn **whitelist** origin của frontend.

> ⚠️ **Bẫy:** *"CORS có phải tính năng bảo mật của server không?"* → **KHÔNG.** CORS do **trình duyệt** thực thi; `curl`/Postman **bỏ qua** hoàn toàn. CORS chỉ chống trang web độc **trong trình duyệt nạn nhân** đọc lén API, **không thay** cho xác thực/phân quyền.

---

## 7) CSRF — vì sao TẮT mà vẫn an toàn

**Code:** `SecurityConfig` dòng 69 `.csrf(csrf -> csrf.disable())`; dòng 71 `SessionCreationPolicy.STATELESS`.

- **CSRF** = lừa trình duyệt nạn nhân tự động đính **cookie session** vào request giả mạo. Chỉ nguy hiểm khi xác thực dựa trên **cookie được trình duyệt tự gửi**.
- Hệ của bạn **không dùng cookie-session**: token nằm ở header `Authorization: Bearer` (`JwtAuthenticationFilter` dòng 65), do JS chủ động đính. Trình duyệt **không tự gắn** header này → kẻ tấn công không ép được → **không có bề mặt CSRF** → tắt là hợp lý.

> 🎯 *"Tắt CSRF được vì stateless + token ở header (không phải cookie tự gửi), nên không có đường tấn công CSRF."*

---

## 8) 🔴 Điểm yếu phải tự nhận (nhận trước thì ăn điểm)

1. **Secret JWT có default hardcode** — `JwtTokenProvider` dòng 25 fallback `distribution-...-2024` nếu env trống. → *"Nên bắt buộc qua biến môi trường, fail-fast nếu thiếu."*
2. **Token không thu hồi được** (stateless) — lộ token thì dùng được tới khi hết hạn (24h). → *"Cải thiện: blacklist / rút ngắn access token + refresh."*
3. **Token lưu `localStorage` ở FE** → dính **XSS** là mất token. → *"An toàn hơn: httpOnly cookie."*
4. **`@Require*` annotations là code chết** → method-level chưa DRY.

---

## 9) Câu "xoáy" hay gặp → trả lời gọn

| Hỏi | Đáp |
|---|---|
| Role lấy từ **token** hay **DB**? | **DB.** Filter chỉ lấy `username`, gọi `loadUserByUsername` mỗi request (dòng 41). Claim `roles` trong token **không dùng** để phân quyền. |
| HS256 vs RS256 khác gì? | HS256 **đối xứng** (1 khóa bí mật, đơn giản, hợp monolith). RS256 **bất đối xứng** (khóa riêng ký / khóa công kiểm) — hợp khi nhiều service cần verify mà không giữ khóa ký. |
| Đổi `roles` trong token rồi gọi API admin được không? | **Không.** (1) Sửa payload → chữ ký sai → `validateToken` chặn. (2) Kể cả chữ ký đúng, quyền vẫn nạp từ DB nên claim bị bỏ qua. |
| BCrypt khác MD5/SHA-256 chỗ nào? | MD5/SHA **nhanh** (tốt cho checksum, **tệ** cho mật khẩu) + không salt sẵn. BCrypt **chậm có chủ đích** + **salt nhúng** → chống brute-force & rainbow table. |
| Tắt CSRF không nguy hiểm à? | Không, vì không dùng cookie-session. CSRF chỉ khai thác được cookie **tự động gửi**; token ở header phải JS chủ động đính. |
| CORS chặn được Postman không? | **Không.** CORS chỉ trình duyệt thực thi. Bảo vệ thật nằm ở JWT + RBAC phía server. |
| Logout kiểu gì khi stateless? | FE **xóa token** khỏi localStorage. Server không giữ trạng thái nên không "hủy" được token đang lưu hành (đây là hạn chế đã nêu ở mục 8.2). |

---

**Liên quan:** §5.5 RBAC (`tab:rbac-matrix`) · `fig:ui-login` (màn đăng nhập) · 1-pager concurrency (cùng style).
**Tự vẽ được** luồng: `login → BCrypt match → phát JWT(HS256)` ‖ `request → Bearer → validateToken → loadUserByUsername(DB) → SecurityContext → [endpoint RBAC + @PreAuthorize + DataFilterAspect]`.

## ✅ Tự kiểm — trả lời KHÔNG nhìn (gõ cho tôi chấm)
1. JWT ký thuật toán gì, khóa đối xứng hay bất đối xứng?
2. Một request tới, server lấy **role** từ đâu? Code dòng nào?
3. Vì sao 2 user cùng mật khẩu `123456` lại có hash khác nhau?
4. `curl` có bị CORS chặn không? Vì sao?
5. Vì sao tắt CSRF mà vẫn an toàn?
6. Kể 3 lớp RBAC + mỗi lớp 1 ví dụ code.
