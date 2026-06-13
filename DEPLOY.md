# Hướng dẫn Deploy

Kiến trúc deploy (đều dùng free tier):

```
Frontend (React + Vite)  ──►  Vercel        (URL công khai, HTTPS)
        │  gọi API
        ▼
Backend (Spring Boot)    ──►  Render         (Docker, chạy JVM liên tục)
        │
        ▼
PostgreSQL               ──►  Render         (managed Postgres free)
```

> Vì sao tách đôi: Vercel chỉ host static site / serverless, **không chạy được** server Spring Boot hay PostgreSQL. Nên backend + DB đặt ở Render.

---

## Phần A — Deploy Backend + Database lên Render

### 1. Đẩy code lên GitHub
Render và Vercel đều deploy từ GitHub. Đảm bảo repo đã có các file mới:
`render.yaml`, `backend/Dockerfile`, `frontend/vercel.json`.

```bash
git add -A
git commit -m "chore: cấu hình deploy Render + Vercel"
git push
```

### 2. Tạo dịch vụ bằng Blueprint
1. Vào https://dashboard.render.com → **New +** → **Blueprint**.
2. Kết nối tài khoản GitHub, chọn repo `smart-supply-chain`.
3. Render tự đọc file `render.yaml` → hiện ra **1 Database** + **1 Web Service**. Bấm **Apply**.
4. Chờ Render:
   - tạo PostgreSQL `distribution-db` (free),
   - build Docker image backend từ `backend/Dockerfile` (~3–5 phút lần đầu),
   - tự chạy Flyway migration V1→V8 → **tạo schema + seed sẵn dữ liệu mẫu và tài khoản**.

### 3. Lấy URL backend
Sau khi service xanh (Live), URL có dạng:
```
https://distribution-backend.onrender.com
```
Kiểm tra health:
```
https://distribution-backend.onrender.com/actuator/health   →  {"status":"UP"}
```
Swagger (xem toàn bộ API):
```
https://distribution-backend.onrender.com/swagger-ui.html
```

> ⚠️ Free tier Render **ngủ sau ~15 phút không có request**. Lần gọi đầu sau khi ngủ sẽ chậm ~30–50s rồi tỉnh lại. Khi demo bảo vệ, nên mở trang trước vài phút cho nó “warm-up”.

---

## Phần B — Deploy Frontend lên Vercel

### 1. Import project
1. Vào https://vercel.com → **Add New** → **Project** → chọn repo `smart-supply-chain`.
2. Ở phần cấu hình:
   - **Root Directory**: chọn `frontend`  ← quan trọng, vì frontend nằm trong thư mục con.
   - Framework Preset: **Vite** (tự nhận).
   - Build Command / Output: để mặc định (`npm run build` / `dist`) — đã khai trong `vercel.json`.

### 2. Khai biến môi trường
Trong bước import (hoặc **Settings → Environment Variables**), thêm:

| Key | Value |
|-----|-------|
| `VITE_API_BASE_URL` | `https://distribution-backend.onrender.com/api` |

> Nhớ có `/api` ở cuối. Đây là URL backend Render ở Phần A.

Bấm **Deploy**. Sau ~1 phút có URL frontend, ví dụ:
```
https://smart-supply-chain.vercel.app
```

---

## Phần C — Nối hai bên qua CORS (bắt buộc)

Backend chỉ chấp nhận request từ origin được khai báo. Sau khi có URL Vercel:

1. Vào Render → service `distribution-backend` → tab **Environment**.
2. Sửa biến `CORS_ALLOWED_ORIGINS` thành URL Vercel (không có dấu `/` ở cuối):
   ```
   https://smart-supply-chain.vercel.app
   ```
   (muốn cho cả nhiều origin thì ngăn cách bằng dấu phẩy, vd:
   `https://smart-supply-chain.vercel.app,http://localhost:5173`)
3. **Save** → Render tự restart backend.

Nếu bỏ qua bước này, frontend mở được nhưng mọi lời gọi API sẽ bị chặn (lỗi CORS), trang trắng / không đăng nhập được.

---

## Phần D — Kiểm tra & đăng nhập

1. Mở URL Vercel.
2. Đăng nhập bằng tài khoản đã seed sẵn qua Flyway (xem `backend/src/main/resources/db/migration/V3__security_and_rbac.sql` và `V6__fix_user_passwords.sql` để biết username/mật khẩu). Tài khoản ADMIN thường là `admin`.
3. Nếu login lỗi mạng → kiểm tra lại:
   - `VITE_API_BASE_URL` trên Vercel có đúng `/api` không;
   - `CORS_ALLOWED_ORIGINS` trên Render có đúng URL Vercel không;
   - backend Render có đang Live không (mở `/actuator/health`).

---

## Tóm tắt biến môi trường

**Render (backend)** — phần lớn được `render.yaml` tự set, chỉ cần điền tay `CORS_ALLOWED_ORIGINS`:

| Biến | Nguồn |
|------|-------|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | tự lấy từ database |
| `JWT_SECRET` | Render tự sinh ngẫu nhiên |
| `CORS_ALLOWED_ORIGINS` | **điền tay** = URL Vercel |
| `PORT` | Render tự cấp |

**Vercel (frontend)**:

| Biến | Giá trị |
|------|---------|
| `VITE_API_BASE_URL` | `https://<backend>.onrender.com/api` |

---

## Cập nhật về sau
Cả Render và Vercel đều **auto-deploy mỗi khi push lên nhánh `main`**. Chỉ cần `git push`, hai bên tự build lại.
