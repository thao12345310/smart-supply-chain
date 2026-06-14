# Đo hiệu năng (response time)

## Cách 1 — script curl
1. Chạy backend: `cd backend && mvn spring-boot:run`
2. `chmod +x scripts/perf/run.sh`
3. `N=50 ./scripts/perf/run.sh` → in bảng endpoint | n | avg(ms) | max(ms). Copy vào báo cáo (mục WP5).

## Cách 2 — đọc Micrometer
Sau khi đã tạo tải (chạy script hoặc dùng app), gọi (token ADMIN):
`curl -s http://localhost:8080/api/metrics/summary -H "Authorization: Bearer <TOKEN>"`
Trả về count/avgMs/maxMs theo từng endpoint từ Timer "http.api.timing".
