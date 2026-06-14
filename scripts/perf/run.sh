#!/usr/bin/env bash
# Đo response time các endpoint chính. Cần backend chạy ở localhost:8080.
# Dùng: BASE=http://localhost:8080 APP_USER=admin APP_PASS=admin123 N=50 ./scripts/perf/run.sh
# Lưu ý: dùng APP_USER chứ KHÔNG dùng USER — USER là biến môi trường có sẵn của shell (tên user OS).
set -euo pipefail
BASE="${BASE:-http://localhost:8080}"
APP_USER="${APP_USER:-admin}"
APP_PASS="${APP_PASS:-admin123}"
N="${N:-50}"

LOGIN=$(curl -s -X POST "$BASE/api/auth/login" -H "Content-Type: application/json" -d "{\"username\":\"$APP_USER\",\"password\":\"$APP_PASS\"}")
TOKEN=$(echo "$LOGIN" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
if [ -z "$TOKEN" ]; then echo "Login thất bại — kiểm tra APP_USER/APP_PASS. Response: $LOGIN"; exit 1; fi

ENDPOINTS=(
  "/api/purchase-orders"
  "/api/sales-orders"
  "/api/inventory"
  "/api/dashboard/summary"
  "/api/delivery-orders"
)

printf "%-32s %8s %10s %10s\n" "endpoint" "n" "avg(ms)" "max(ms)"
for ep in "${ENDPOINTS[@]}"; do
  total=0; max=0
  for i in $(seq 1 "$N"); do
    t=$(curl -s -o /dev/null -w "%{time_total}" "$BASE$ep" -H "Authorization: Bearer $TOKEN")
    ms=$(echo "$t * 1000" | bc -l)
    total=$(echo "$total + $ms" | bc -l)
    cmp=$(echo "$ms > $max" | bc -l); [ "$cmp" -eq 1 ] && max=$ms
  done
  avg=$(echo "scale=2; $total / $N" | bc -l)
  printf "%-32s %8s %10.2f %10.2f\n" "$ep" "$N" "$avg" "$max"
done
