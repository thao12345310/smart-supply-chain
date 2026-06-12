#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Sinh dữ liệu demo cho NPP bánh kẹo "Ngọt Việt" — doanh nghiệp phân phối bánh kẹo
quy mô vừa, kiểm soát hạn sử dụng theo lô (FEFO).

Kịch bản: hoạt động từ giữa tháng 12/2025 đến nay (12/06/2026), gồm:
- 12 nhà cung cấp (các hãng bánh kẹo VN), ~59 SKU có hạn sử dụng 4-24 tháng
- 48 khách hàng (đại lý, tạp hóa, minimart, căng tin) ở HCM / HN / ĐN
- Mua hàng theo tháng (cao điểm Tết Bính Ngọ 17/02/2026), nhập kho theo lô
  (số lô + NSX + HSD), xuất kho FEFO, hóa đơn, công nợ, giao vận
- Tồn kho cuối kỳ có lô đã hết hạn (mứt Tết), lô cận hạn (bánh bông lan ôm
  hàng Tết), phục vụ demo cảnh báo hạn sử dụng

Kết quả ghi đè vào: backend/src/main/resources/db/migration/V4__sample_data.sql
(V4 chưa từng được Flyway ghi vào schema history nên thay nội dung an toàn;
file tự DELETE dữ liệu cũ trước khi INSERT nên chạy lại được trên DB hiện có.)
"""
import random
from datetime import date, datetime, timedelta
from pathlib import Path

random.seed(20260612)

TODAY = date(2026, 6, 12)
TET = date(2026, 2, 17)                  # Tết Bính Ngọ
TET_BREAK = (date(2026, 2, 14), date(2026, 2, 22))  # nghỉ Tết, không phát sinh chứng từ

OUT = Path(__file__).resolve().parent.parent / "src/main/resources/db/migration/V4__sample_data.sql"

def esc(s):
    return s.replace("'", "''")

def q(s):
    return "NULL" if s is None else f"'{esc(str(s))}'"

def d(dt):
    return f"'{dt.isoformat()}'" if dt else "NULL"

def ts(dt):
    return f"'{dt.strftime('%Y-%m-%d %H:%M:%S')}'" if dt else "NULL"

def U(username):
    return f"(SELECT id FROM users WHERE username='{username}')"

def rt(day, h1=8, h2=17):
    """timestamp ngẫu nhiên trong giờ làm việc của một ngày"""
    return datetime(day.year, day.month, day.day,
                    random.randint(h1, h2), random.randint(0, 59), random.randint(0, 59))

def add_months(day, months):
    m = day.month - 1 + months
    y = day.year + m // 12
    m = m % 12 + 1
    dd = min(day.day, [31, 29 if y % 4 == 0 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][m - 1])
    return date(y, m, dd)

def money(x):
    return f"{x:.2f}"

# ============================================================================
# 1. MASTER DATA
# ============================================================================
WAREHOUSES = [
    (1, "WH001", "Kho Tổng TP.HCM", "Lô C2-5 KCN Tân Bình, Q. Tân Phú, TP.HCM", "Kho trung tâm, phục vụ khu vực TP.HCM và miền Nam"),
    (2, "WH002", "Kho Miền Bắc - Hà Nội", "Số 8 đường Nguyễn Văn Linh, Long Biên, Hà Nội", "Kho chi nhánh phục vụ khu vực miền Bắc"),
    (3, "WH003", "Kho Miền Trung - Đà Nẵng", "Lô 12 KCN Hòa Khánh, Liên Chiểu, Đà Nẵng", "Kho chi nhánh phục vụ khu vực miền Trung"),
]
WH_SHARE = {1: 0.60, 2: 0.28, 3: 0.12}

SUPPLIERS = [
    (1, "SUP001", "Công ty CP Bibica", "443 Lý Thường Kiệt, Q. Tân Bình, TP.HCM", "Trần Quang Huy", "huy.tran@bibica.com.vn", "028-3971-7920", "BBC"),
    (2, "SUP002", "Công ty CP Bánh kẹo Hải Hà", "25-27 Trương Định, Hai Bà Trưng, Hà Nội", "Nguyễn Thị Hồng", "hong.nguyen@haihaco.com.vn", "024-3863-2956", "HHC"),
    (3, "SUP003", "Công ty TNHH Mondelez Kinh Đô Việt Nam", "Số 26 VSIP, Thuận An, Bình Dương", "Lê Hoàng Phúc", "phuc.le@mdlz.com", "0274-3754-9999", "KDO"),
    (4, "SUP004", "Công ty CP Thực phẩm Hữu Nghị", "122 Định Công, Hoàng Mai, Hà Nội", "Phạm Văn Thắng", "thang.pham@huunghi.com.vn", "024-3664-9451", "HNG"),
    (5, "SUP005", "Công ty TNHH Thực phẩm Orion Vina", "KCN Mỹ Phước 2, Bến Cát, Bình Dương", "Kim Min-jun", "minjun.kim@orion.vn", "0274-3556-7100", "ORI"),
    (6, "SUP006", "Công ty TNHH Perfetti Van Melle Việt Nam", "KCN Sóng Thần 1, Dĩ An, Bình Dương", "Đỗ Thanh Bình", "binh.do@vn.pvmgrp.com", "0274-3742-880", "PVM"),
    (7, "SUP007", "Công ty TNHH URC Việt Nam", "KCN Việt Nam - Singapore, Thuận An, Bình Dương", "Maria Santos", "maria.santos@urc.com.vn", "0274-3743-666", "URC"),
    (8, "SUP008", "Công ty TNHH Liwayway Sài Gòn (Oishi)", "KCN Tân Bình, Q. Tân Phú, TP.HCM", "Vũ Đình Khang", "khang.vu@oishi.com.vn", "028-3815-4972", "OIS"),
    (9, "SUP009", "Công ty CP Chế biến Thực phẩm Phạm Nguyên", "613 Trần Đại Nghĩa, Bình Tân, TP.HCM", "Phạm Ngọc Châu", "chau.pham@phamnguyen.vn", "028-3754-2168", "PNG"),
    (10, "SUP010", "Công ty TNHH Tân Tân", "32C Đại lộ Độc Lập, KCN Sóng Thần, Bình Dương", "Trần Văn Đậu", "dau.tran@tantan.com.vn", "0274-3790-786", "TTN"),
    (11, "SUP011", "Công ty TNHH Lotte Việt Nam", "Tầng 9, Diamond Plaza, 34 Lê Duẩn, Q.1, TP.HCM", "Park Ji-sung", "jisung.park@lotte.vn", "028-3822-7799", "LTE"),
    (12, "SUP012", "Công ty TNHH Nestlé Việt Nam", "KCN Biên Hòa 2, Đồng Nai", "Hoàng Thu Trang", "trang.hoang@vn.nestle.com", "0251-3836-505", "NES"),
]

# (name, quy_cách, giá bán/thùng, HSD tháng, supplier_id, weight, flags)
# flags: 'tet' = hàng Tết (chỉ nhập T12-T1, bán đến 10/02); 'over' = bánh HSD ngắn ôm hàng Tết
PRODUCTS_RAW = [
    ("Bánh bông lan kem Hura Deli 168g", "Thùng 24 hộp x 168g", 510_000, 6, 1, 9, "over"),
    ("Bánh bông lan cuộn Hura Swissroll 360g", "Thùng 12 hộp x 360g", 455_000, 6, 1, 7, "over"),
    ("Bánh quy bơ Goody hộp thiếc 454g", "Thùng 12 hộp thiếc x 454g", 1_260_000, 12, 1, 5, ""),
    ("Bánh Choco Chip Goody 144g", "Thùng 24 hộp x 144g", 470_000, 9, 1, 5, ""),
    ("Kẹo cứng trái cây Migita 700g", "Thùng 12 gói x 700g", 535_000, 18, 1, 6, ""),
    ("Kẹo mềm sữa Sumika 350g", "Thùng 24 gói x 350g", 430_000, 12, 1, 5, ""),
    ("Bánh trứng nướng Quasure Light 140g", "Thùng 20 hộp x 140g", 495_000, 8, 1, 4, ""),
    ("Hộp quà Tết Lạc Việt 650g", "Thùng 8 hộp x 650g", 1_150_000, 9, 1, 6, "tet"),
    ("Kẹo Chew Hải Hà nho đen 105g", "Thùng 40 gói x 105g", 365_000, 12, 2, 8, ""),
    ("Kẹo Jelly Chip Hải Hà 175g", "Thùng 30 gói x 175g", 425_000, 12, 2, 6, ""),
    ("Bánh quy dừa Hải Hà 178g", "Thùng 24 gói x 178g", 355_000, 9, 2, 5, ""),
    ("Kẹo cứng nhân socola Hải Hà 105g", "Thùng 40 gói x 105g", 325_000, 18, 2, 5, ""),
    ("Bánh kem xốp Hải Hà 175g", "Thùng 30 gói x 175g", 390_000, 9, 2, 6, ""),
    ("Kẹo hộp Tết Hải Hà 400g", "Thùng 20 hộp x 400g", 790_000, 6, 2, 6, "tet"),
    ("Bánh quy Cosy Marie 320g", "Thùng 12 gói x 320g", 340_000, 9, 3, 10, ""),
    ("Bánh quy Cosy Double Choc 304g", "Thùng 12 gói x 304g", 385_000, 9, 3, 7, ""),
    ("Bánh bông lan Solite cuộn kem bơ sữa 360g", "Thùng 12 hộp x 360g", 545_000, 6, 3, 8, "over"),
    ("Bánh mặn AFC vị rau cải 200g", "Thùng 24 hộp x 200g", 385_000, 9, 3, 8, ""),
    ("Bánh Oreo kem vani 303g", "Thùng 12 gói x 303g", 360_000, 12, 3, 9, ""),
    ("Bánh Ritz phô mai 243g", "Thùng 12 hộp x 243g", 420_000, 9, 3, 5, ""),
    ("Bánh quy hộp thiếc Cosy quà Tết 378g", "Thùng 12 hộp thiếc x 378g", 725_000, 9, 3, 6, "tet"),
    ("Bánh trứng Tipo 220g", "Thùng 12 hộp x 220g", 365_000, 9, 4, 9, ""),
    ("Bánh bông lan Staff kem bơ 300g", "Thùng 12 hộp x 300g", 420_000, 6, 4, 6, "over"),
    ("Bánh quy dừa Arita 145g", "Thùng 24 gói x 145g", 290_000, 9, 4, 5, ""),
    ("Mứt Tết thập cẩm Hữu Nghị hộp 300g", "Thùng 20 hộp x 300g", 1_090_000, 4, 4, 7, "tet"),
    ("Bánh trứng nướng Salsa 176g", "Thùng 24 hộp x 176g", 430_000, 8, 4, 4, ""),
    ("Bánh Orion Choco Pie hộp 12 chiếc 396g", "Thùng 8 hộp x 396g", 415_000, 9, 5, 10, ""),
    ("Bánh trứng Custas hộp 12 chiếc 282g", "Thùng 8 hộp x 282g", 470_000, 6, 5, 8, "over"),
    ("Snack Toonies vị phô mai 35g", "Thùng 40 gói x 35g", 215_000, 6, 5, 7, ""),
    ("Snack O'Star khoai tây vị tự nhiên 36g", "Thùng 40 gói x 36g", 235_000, 6, 5, 7, ""),
    ("Bánh gạo An nướng vị tảo biển 111g", "Thùng 20 gói x 111g", 335_000, 9, 5, 8, ""),
    ("Bánh quy mè Goute 288g", "Thùng 12 gói x 288g", 415_000, 9, 5, 7, ""),
    ("Kẹo sữa Alpenliebe caramen 329g", "Thùng 24 gói x 329g", 705_000, 18, 6, 10, ""),
    ("Kẹo Alpenliebe 2Chew dâu 115g", "Thùng 24 gói x 115g", 520_000, 12, 6, 6, ""),
    ("Kẹo Mentos bạc hà lốc 16 cuộn", "Thùng 6 lốc x 16 cuộn", 655_000, 24, 6, 7, ""),
    ("Kẹo mút Chupa Chups túi 50 cây", "Thùng 12 túi x 50 cây", 950_000, 24, 6, 6, ""),
    ("Kẹo Golia Active hương bạc hà 116g", "Thùng 30 gói x 116g", 475_000, 18, 6, 5, ""),
    ("Kẹo gum Happydent White hũ 56 viên", "Thùng 24 hũ x 56 viên", 830_000, 24, 6, 5, ""),
    ("Bánh Cream-O kem socola 85g", "Thùng 48 gói x 85g", 375_000, 9, 7, 7, ""),
    ("Kẹo Dynamite bạc hà nhân socola 330g", "Thùng 14 gói x 330g", 445_000, 18, 7, 7, ""),
    ("Bánh cracker Magic phô mai 290g", "Thùng 12 hộp x 290g", 395_000, 9, 7, 5, ""),
    ("Snack tôm cay Oishi 42g", "Thùng 80 gói x 42g", 355_000, 6, 8, 10, ""),
    ("Snack bắp ngọt Oishi 40g", "Thùng 80 gói x 40g", 355_000, 6, 8, 8, ""),
    ("Snack cua Oishi 32g", "Thùng 80 gói x 32g", 355_000, 6, 8, 7, ""),
    ("Bánh Pillows socola Oishi 38g", "Thùng 60 gói x 38g", 295_000, 6, 8, 6, ""),
    ("Bánh Phaner Pie hộp 12 cái 360g", "Thùng 8 hộp x 360g", 340_000, 9, 9, 7, ""),
    ("Bánh bông lan Solo 264g", "Thùng 12 gói x 264g", 330_000, 6, 9, 6, "over"),
    ("Bánh quy socola Choco P&N 216g", "Thùng 24 hộp x 216g", 340_000, 9, 9, 4, ""),
    ("Bánh quy mè Oscar 145g", "Thùng 24 gói x 145g", 285_000, 9, 9, 4, ""),
    ("Đậu phộng nước cốt dừa Tân Tân 100g", "Thùng 50 gói x 100g", 445_000, 9, 10, 7, ""),
    ("Đậu phộng muối Tân Tân 100g", "Thùng 50 gói x 100g", 405_000, 9, 10, 5, ""),
    ("Đậu Hà Lan wasabi Tân Tân 100g", "Thùng 50 gói x 100g", 495_000, 9, 10, 4, ""),
    ("Hạt điều rang muối Tân Tân 200g", "Thùng 24 hộp x 200g", 835_000, 6, 10, 4, ""),
    ("Kẹo gum Lotte Xylitol Lime Mint hũ 145g", "Thùng 12 hũ x 145g", 810_000, 24, 11, 6, ""),
    ("Bánh gấu Koala's March nhân socola 37g", "Thùng 24 hộp x 37g", 365_000, 12, 11, 7, ""),
    ("Bánh que Pepero socola hạnh nhân 36g", "Thùng 32 hộp x 36g", 690_000, 12, 11, 6, ""),
    ("Bánh Lotte Choco Pie hộp 12 chiếc 336g", "Thùng 8 hộp x 336g", 390_000, 9, 11, 6, ""),
    ("Bánh xốp phủ socola KitKat 4F 35g", "Thùng 48 thanh x 35g", 470_000, 12, 12, 8, ""),
    ("Socola sữa Milo Nuggets 25g", "Thùng 48 gói x 25g", 430_000, 9, 12, 5, ""),
]

PRODUCTS = []
for i, (name, spec, price, shelf, sup, w, flags) in enumerate(PRODUCTS_RAW, start=1):
    PRODUCTS.append({
        "id": i, "code": f"PRD{i:03d}", "name": name,
        "desc": f"{spec} - HSD {shelf} tháng",
        "price": price, "cost": round(price * random.uniform(0.80, 0.88) / 1000) * 1000,
        "shelf": shelf, "sup": sup, "w": w,
        "tet": flags == "tet", "over": flags == "over",
    })
PROD_BY_SUP = {}
for p in PRODUCTS:
    PROD_BY_SUP.setdefault(p["sup"], []).append(p)

# --- Khách hàng -------------------------------------------------------------
HCM_ADDR = [("12 Hậu Giang", "Q.6"), ("231 Lê Văn Sỹ", "Q.3"), ("57 Nguyễn Trãi", "Q.5"),
            ("889 Cách Mạng Tháng Tám", "Q.10"), ("145 Lũy Bán Bích", "Q. Tân Phú"),
            ("76 Quang Trung", "Q. Gò Vấp"), ("303 Phan Văn Trị", "Q. Bình Thạnh"),
            ("18 Âu Cơ", "Q. Tân Bình"), ("420 Trường Chinh", "Q.12"), ("66 Lê Đức Thọ", "Q. Gò Vấp"),
            ("214 Kinh Dương Vương", "Q. Bình Tân"), ("35 Nguyễn Sơn", "Q. Tân Phú"),
            ("502 Võ Văn Kiệt", "Q.1"), ("128 Tháp Mười", "Q.6"), ("91 Bàu Cát", "Q. Tân Bình"),
            ("250 Lê Văn Khương", "Q.12"), ("44 Phạm Thế Hiển", "Q.8"), ("167 Lê Văn Việt", "TP. Thủ Đức"),
            ("23 Nguyễn Ảnh Thủ", "H. Hóc Môn"), ("310 Tỉnh lộ 10", "Q. Bình Tân")]
HN_ADDR = [("88 Bạch Mai", "Q. Hai Bà Trưng"), ("215 Tôn Đức Thắng", "Q. Đống Đa"),
           ("139 Cầu Giấy", "Q. Cầu Giấy"), ("52 Ngọc Lâm", "Q. Long Biên"),
           ("371 Giải Phóng", "Q. Hoàng Mai"), ("29 Hồ Tùng Mậu", "Q. Nam Từ Liêm"),
           ("404 Nguyễn Trãi", "Q. Thanh Xuân"), ("17 Quang Trung", "Q. Hà Đông"),
           ("66 Trương Định", "Q. Hai Bà Trưng"), ("203 Nguyễn Văn Cừ", "Q. Long Biên")]
DN_ADDR = [("125 Hùng Vương", "Q. Hải Châu"), ("310 Điện Biên Phủ", "Q. Thanh Khê"),
           ("47 Ông Ích Khiêm", "Q. Hải Châu"), ("582 Nguyễn Văn Linh", "Q. Cẩm Lệ"),
           ("90 Ngô Quyền", "Q. Sơn Trà"), ("233 Tôn Đức Thắng", "Q. Liên Chiểu")]

FIRST = ["Minh", "Hồng", "Tuấn", "Lan", "Hùng", "Thảo", "Quang", "Ngọc", "Đức", "Trang",
         "Phong", "Hương", "Khoa", "Yến", "Sơn", "Nhung", "Bình", "Loan", "Việt", "Hà"]
LAST = ["Nguyễn Văn", "Trần Thị", "Lê Văn", "Phạm Thị", "Hoàng Văn", "Vũ Thị", "Đặng Văn",
        "Bùi Thị", "Đỗ Văn", "Ngô Thị", "Dương Văn", "Lý Thị"]

def person():
    return f"{random.choice(LAST)} {random.choice(FIRST)}"

# (tên, vùng kho, hạng) hạng: L lớn / M vừa / S nhỏ
CUSTOMERS_RAW = [
    ("Hệ thống minimart An Tâm", 1, "L"), ("Chuỗi cửa hàng tiện lợi SaiGon Mart", 1, "L"),
    ("Đại lý bánh kẹo Phước Lộc - Chợ Bình Tây", 1, "L"), ("Siêu thị mini Gia Phúc", 1, "L"),
    ("Đại lý Thu Thảo", 1, "M"), ("Đại lý Hồng Phát", 1, "M"), ("Tạp hóa Cô Hằng", 1, "M"),
    ("Tạp hóa Dì Sáu Gò Vấp", 1, "M"), ("Cửa hàng Vy Vy", 1, "M"), ("Bách hóa Minh Trí", 1, "M"),
    ("Đại lý Quang Vinh", 1, "M"), ("Tạp hóa Chú Tư", 1, "M"), ("Minimart Phúc An", 1, "M"),
    ("Cửa hàng bánh kẹo Ngọc Lan", 1, "M"), ("Đại lý Tân Thành", 1, "M"),
    ("Bách hóa tổng hợp Kim Yến", 1, "M"), ("Minimart 79", 1, "M"), ("Cửa hàng Hải Yến", 1, "M"),
    ("Đại lý Song Long", 1, "M"), ("Tạp hóa Bà Năm Chợ Lớn", 1, "M"),
    ("Tạp hóa Anh Khoa", 1, "S"), ("Tạp hóa Hoa Mai", 1, "S"),
    ("Căng tin Trường THPT Nguyễn Hữu Huân", 1, "S"), ("Căng tin Trường THCS Lê Quý Đôn", 1, "S"),
    ("Tạp hóa Út Nhỏ", 1, "S"), ("Cửa hàng Tuấn Kiệt", 1, "S"), ("Tạp hóa Mỹ Lệ", 1, "S"),
    ("Quầy bánh kẹo chợ Tân Định - Cô Tư", 1, "S"), ("Tạp hóa Thanh Bình", 1, "S"),
    ("Nhà sách Tri Thức - quầy bánh kẹo", 1, "S"),
    ("Đại lý bánh kẹo Hà Thành - Chợ Đồng Xuân", 2, "L"), ("Hệ thống minimart Thăng Long", 2, "L"),
    ("Đại lý Minh Đức", 2, "M"), ("Tạp hóa Cô Lan Bạch Mai", 2, "M"), ("Cửa hàng Bảo An", 2, "M"),
    ("Minimart Hoa Hồng", 2, "M"), ("Đại lý Hùng Cường", 2, "M"), ("Bách hóa Phương Nga", 2, "M"),
    ("Tạp hóa Ông Bình", 2, "S"), ("Căng tin Trường THPT Trần Phú", 2, "S"),
    ("Tạp hóa Thu Hương", 2, "S"), ("Cửa hàng Đức Anh", 2, "S"),
    ("Đại lý bánh kẹo Sông Hàn", 3, "L"), ("Minimart Mỹ Khê", 3, "M"),
    ("Tạp hóa Chị Bảy Chợ Cồn", 3, "M"), ("Cửa hàng Hòa Xuân", 3, "M"),
    ("Tạp hóa Cẩm Tú", 3, "S"), ("Căng tin Trường THPT Phan Châu Trinh", 3, "S"),
]
TIER = {"L": dict(mult=2.6, weight=8, credit=(600, 1500), terms=[30, 45]),
        "M": dict(mult=1.3, weight=4, credit=(150, 400), terms=[15, 30]),
        "S": dict(mult=0.6, weight=2, credit=(30, 100), terms=[0, 15])}
CITY = {1: "TP.HCM", 2: "Hà Nội", 3: "Đà Nẵng"}

CUSTOMERS, ADDRESSES = [], []
addr_pool = {1: list(HCM_ADDR), 2: list(HN_ADDR), 3: list(DN_ADDR)}
for pool in addr_pool.values():
    random.shuffle(pool)
aid = 0
for cid, (name, region, tier) in enumerate(CUSTOMERS_RAW, start=1):
    t = TIER[tier]
    pool = addr_pool[region]
    street, dist = pool[cid % len(pool)]
    no = random.randint(2, 480)
    line1 = f"{no} {street.split(' ', 1)[1]}" if street.split(" ")[0].isdigit() else street
    phone = f"09{random.randint(10000000, 99999999)}"
    cust = {
        "id": cid, "code": f"KH{cid:03d}", "name": name, "contact": person(),
        "phone": phone, "email": f"lienhe.kh{cid:03d}@gmail.com",
        "tax": f"0{random.randint(300000000, 319999999)}",
        "credit": random.randint(*t["credit"]) * 1_000_000,
        "terms": random.choice(t["terms"]), "region": region,
        "mult": t["mult"], "weight": t["weight"], "tier": tier,
        "slow": False,
    }
    CUSTOMERS.append(cust)
    n_addr = 2 if tier == "L" else 1
    for k in range(n_addr):
        aid += 1
        st2, di2 = pool[(cid + k * 3) % len(pool)]
        street_name = st2.split(" ", 1)[1] if st2.split(" ")[0].isdigit() else st2
        addr = {
            "id": aid, "cust": cid,
            "name": "Kho nhận hàng chính" if k == 0 else f"Cửa hàng chi nhánh {di2}",
            "line1": f"{random.randint(2, 460)} {street_name}", "city": f"{di2}, {CITY[region]}",
            "recipient": cust["contact"] if k == 0 else person(), "phone": phone,
            "default": k == 0,
        }
        ADDRESSES.append(addr)
        if k == 0:
            cust["addr_id"] = aid
            cust["addr_text"] = f"{addr['line1']}, {addr['city']}"
# 3 khách thanh toán chậm (sinh hóa đơn quá hạn)
for c in random.sample([c for c in CUSTOMERS if c["tier"] == "M"], 3):
    c["slow"] = True

# ============================================================================
# 2. SIMULATION
# ============================================================================
purchase_orders, po_items = [], []
goods_receipts, gr_items = [], []
lots = []                      # lot runtime + xuất SQL
sales_orders, so_items = [], []
goods_issues, gi_items = [], []
invoices, inv_items = [], []
reservations = []
tx_events = []                 # (ts, type, product, wh, qty, cost, ref_type, ref_code, ref_id, note)
delivery_orders = []

ids = dict(po=0, poi=0, gr=0, gri=0, lot=0, so=0, soi=0, gi=0, gii=0, si=0, sii=0, res=0, do_=0)
seq = {}
def code(prefix, year):
    key = (prefix, year)
    seq[key] = seq.get(key, 0) + 1
    return f"{prefix}-{year}-{seq[key]:03d}"

lots_by_pw = {}                # (product, wh) -> [lot dict]
reserved = {}                  # (product, wh) -> qty đang giữ chỗ

def monthly_demand(p, wh):
    return p["w"] * 9 * WH_SHARE[wh]

PO_MONTHS = [  # (year, month, hệ số nhập)
    (2025, 12, 1.25), (2026, 1, 1.55), (2026, 2, 0.55),
    (2026, 3, 1.0), (2026, 4, 1.0), (2026, 5, 1.05), (2026, 6, 1.0),
]

def make_po(sup_id, wh, created, factor, month, status_plan):
    """status_plan: completed | partial | approved | open | cancelled | draft_gr"""
    global ids
    sup = SUPPLIERS[sup_id - 1]
    items = []
    for p in PROD_BY_SUP[sup_id]:
        if p["tet"] and month not in (12, 1):
            continue
        if not p["tet"] and random.random() > 0.85:
            continue
        f = factor
        if p["tet"]:
            f *= 2.2
        if p["over"] and month == 1:
            f *= 1.6
        qty = max(10, int(round(monthly_demand(p, wh) * f * random.uniform(1.0, 1.35) / 5)) * 5)
        items.append((p, qty))
    if not items:
        return
    ids["po"] += 1
    po_id = ids["po"]
    po_code = code("PO", created.year)
    approved = None if status_plan in ("open", "cancelled") else created + timedelta(days=random.randint(1, 2))
    completed = None
    receipt_day = None
    if status_plan in ("completed", "partial", "draft_gr"):
        receipt_day = approved + timedelta(days=random.randint(4, 8))
        if receipt_day > TODAY:
            # hàng chưa về kịp -> đơn còn ở trạng thái chờ nhận hàng
            status_plan = "approved"
            receipt_day = None

    total = 0
    first_names = []
    for p, qty in items:
        ids["poi"] += 1
        cost = p["cost"]
        amount = qty * cost
        tax = round(amount * 0.08)
        recv = qty if status_plan in ("completed",) else (int(qty * 0.6) if status_plan == "partial" else 0)
        po_items.append(dict(id=ids["poi"], po=po_id, product=p["id"], qty=qty, recv=recv,
                             unit="Thùng", price=cost, amount=amount, tax=tax, total=amount + tax))
        total += amount + tax
        first_names.append(p["name"])

    status = {"completed": "ORDER_COMPLETED", "partial": "ORDER_PARTIALLY_RECEIVED",
              "approved": "ORDER_APPROVED", "draft_gr": "ORDER_APPROVED",
              "open": "ORDER_OPEN", "cancelled": "ORDER_CANCELLED"}[status_plan]
    tet_note = " - hàng Tết" if month in (12, 1) else ""
    purchase_orders.append(dict(
        id=po_id, code=po_code, name=f"Mua hàng {sup[2].replace('Công ty ', '')} T{month:02d}{tet_note}",
        sup=sup_id, wh=wh, created=created, approved=approved, status=status,
        completed=(rt(receipt_day) if status == "ORDER_COMPLETED" else None),
        delivery=(approved + timedelta(days=random.randint(5, 9))) if approved else None,
        total=total,
        reject=("Giá chào cao hơn hợp đồng nguyên tắc, yêu cầu NCC báo giá lại" if status_plan == "cancelled" else None),
    ))

    # Phiếu nhập kho
    if status_plan in ("completed", "partial", "draft_gr"):
        ids["gr"] += 1
        gr_id = ids["gr"]
        gr_code = code("GR", receipt_day.year)
        gr_status = "DRAFT" if status_plan == "draft_gr" else "CONFIRMED"
        confirmed_ts = rt(receipt_day, 9, 16) if gr_status == "CONFIRMED" else None
        gr_total = 0
        for it in [x for x in po_items if x["po"] == po_id]:
            p = PRODUCTS[it["product"] - 1]
            recv = it["qty"] if status_plan != "partial" else it["recv"]
            if status_plan == "partial" and recv == 0:
                continue
            rejected = 0
            reason = None
            if status_plan == "completed" and random.random() < 0.05:
                rejected = max(1, int(recv * random.uniform(0.02, 0.06)))
                reason = "Thùng bị móp/ướt khi vận chuyển, trả lại NCC"
            accepted = recv - rejected
            if p["tet"]:
                mfg = receipt_day - timedelta(days=random.randint(5, 15))
            else:
                mfg = receipt_day - timedelta(days=random.randint(10, 40))
            expiry = add_months(mfg, p["shelf"])
            batch = f"{SUPPLIERS[p['sup'] - 1][7]}{mfg.strftime('%y%m%d')}-{random.randint(1, 9)}"
            ids["gri"] += 1
            gr_items.append(dict(id=ids["gri"], gr=gr_id, product=p["id"], poi=it["id"],
                                 ordered=it["qty"], received=recv, rejected=rejected, accepted=accepted,
                                 reason=reason, batch=batch, mfg=mfg, expiry=expiry,
                                 unit="Thùng", price=it["price"], total=accepted * it["price"]))
            gr_total += accepted * it["price"]
            if gr_status == "CONFIRMED":
                ids["lot"] += 1
                lot = dict(id=ids["lot"], product=p["id"], wh=wh, batch=batch, mfg=mfg, expiry=expiry,
                           received=accepted, remaining=accepted, cost=it["price"],
                           gr=gr_id, gri=ids["gri"], created=confirmed_ts)
                lots.append(lot)
                lots_by_pw.setdefault((p["id"], wh), []).append(lot)
                tx_events.append((confirmed_ts, "RECEIPT", p["id"], wh, accepted, it["price"],
                                  "GOODS_RECEIPT", gr_code, gr_id, f"Nhập kho theo phiếu {gr_code} (lô {batch})"))
        goods_receipts.append(dict(id=gr_id, code=gr_code, po=po_id, wh=wh, day=receipt_day,
                                   status=gr_status, confirmed=confirmed_ts, total=gr_total,
                                   note=f"Nhập hàng theo đơn {po_code}"))

# --- Sinh đơn mua theo tháng -------------------------------------------------
for (y, m, factor) in PO_MONTHS:
    for sup_id in range(1, 13):
        plans = []
        if y == 2025:
            plans.append((1, date(2025, 12, random.randint(10, 14))))
        else:
            start = 2 if m == 1 else 1
            plans.append((1, date(y, m, random.randint(start, min(5, 28)))))
        if random.random() < 0.75:
            plans.append((2, plans[0][1] + timedelta(days=random.randint(0, 3))))
        if random.random() < 0.40:
            plans.append((3, plans[0][1] + timedelta(days=random.randint(0, 3))))
        for wh, created in plans:
            if y == 2026 and m == 6:
                make_po(sup_id, wh, created, factor, m, "completed")
            else:
                make_po(sup_id, wh, created, factor, m, "completed")

# Vài đơn đặc biệt cho hiện trạng "đang vận hành"
make_po(3, 1, date(2026, 2, 24), 0.5, 2, "cancelled")
make_po(6, 2, date(2026, 4, 6), 0.6, 4, "cancelled")
make_po(5, 1, date(2026, 5, 25), 0.5, 5, "partial")
make_po(8, 1, date(2026, 5, 27), 0.4, 5, "partial")
make_po(1, 1, date(2026, 6, 9), 0.6, 6, "approved")
make_po(3, 2, date(2026, 6, 9), 0.5, 6, "approved")
make_po(5, 1, date(2026, 6, 10), 0.5, 6, "approved")
make_po(2, 1, date(2026, 6, 10), 0.4, 6, "open")
make_po(8, 3, date(2026, 6, 11), 0.4, 6, "open")
make_po(7, 1, date(2026, 6, 8), 0.4, 6, "draft_gr")

# --- Bán hàng theo ngày ------------------------------------------------------
SALES_MONTH_TARGET = {(2025, 12): 22, (2026, 1): 58, (2026, 2): 40, (2026, 3): 45,
                      (2026, 4): 47, (2026, 5): 50, (2026, 6): 22}
SALES_FACTOR = {(2025, 12): 1.1, (2026, 1): 1.5, (2026, 2): 0.85, (2026, 3): 0.95,
                (2026, 4): 1.0, (2026, 5): 1.0, (2026, 6): 1.0}

def available(p_id, wh, day):
    total = 0
    for lot in lots_by_pw.get((p_id, wh), []):
        if lot["created"].date() <= day and lot["expiry"] > day + timedelta(days=10):
            total += lot["remaining"]
    return total - reserved.get((p_id, wh), 0)

def allocate(p_id, wh, day, qty):
    slices = []
    cand = [l for l in lots_by_pw.get((p_id, wh), [])
            if l["created"].date() <= day and l["expiry"] > day + timedelta(days=10) and l["remaining"] > 0]
    cand.sort(key=lambda l: (l["expiry"], l["id"]))
    need = qty
    for lot in cand:
        if need <= 0:
            break
        take = min(lot["remaining"], need)
        lot["remaining"] -= take
        slices.append((lot, take))
        need -= take
    return slices

def pick_products(cust, day):
    cands = []
    for p in PRODUCTS:
        w = p["w"]
        if p["tet"]:
            if not (date(2025, 12, 20) <= day <= date(2026, 2, 10)):
                continue
            w *= 2
        if p["over"] and day > TET:
            w *= 0.5
        cands.append((p, w))
    n = random.randint(2, 6)
    chosen, seen = [], set()
    for _ in range(n * 4):
        if len(chosen) >= n:
            break
        p, w = random.choices(cands, weights=[c[1] for c in cands])[0]
        if p["id"] in seen:
            continue
        seen.add(p["id"])
        chosen.append(p)
    return chosen

# lịch các ngày bán hàng
sim_days = []
day = date(2025, 12, 20)
while day <= TODAY:
    if day.weekday() != 6 and not (TET_BREAK[0] <= day <= TET_BREAK[1]):
        sim_days.append(day)
    day += timedelta(days=1)

days_by_month = {}
for dd in sim_days:
    days_by_month.setdefault((dd.year, dd.month), []).append(dd)

so_schedule = []   # (day, so thứ tự trong ngày)
for key, target in SALES_MONTH_TARGET.items():
    dlist = days_by_month.get(key, [])
    for _ in range(target):
        so_schedule.append(random.choice(dlist))
so_schedule.sort()

cust_weights = [c["weight"] for c in CUSTOMERS]

for day in so_schedule:
    cust = random.choices(CUSTOMERS, weights=cust_weights)[0]
    wh = cust["region"]
    age = (TODAY - day).days

    # trạng thái theo "tuổi" đơn
    r = random.random()
    if r < 0.03:
        plan = "cancelled"
    elif age <= 1:
        plan = random.choice(["open", "open", "approved_pending"])
    elif age <= 4:
        plan = random.choices(["completed", "approved_pending", "approved_draft_gi", "open"],
                              weights=[5, 3, 2, 1])[0]
    else:
        plan = "completed"

    items = []
    for p in pick_products(cust, day):
        f = SALES_FACTOR[(day.year, day.month)]
        desired = max(2, int(random.uniform(4, 14) * cust["mult"] * f))
        avail = available(p["id"], wh, day)
        if plan in ("completed", "approved_pending", "approves_draft_gi", "approved_draft_gi"):
            if avail < 3:
                continue
            qty = min(desired, avail)
        else:
            qty = desired
        items.append((p, qty))
    if not items:
        continue

    ids["so"] += 1
    so_id = ids["so"]
    so_code = code("SO", day.year)
    created_ts = rt(day)
    approved_ts = None if plan in ("open", "cancelled") else created_ts + timedelta(hours=random.randint(2, 20))
    tet_label = " (hàng Tết)" if day < TET and day >= date(2026, 1, 5) else ""
    status = {"completed": "ORDER_COMPLETED", "approved_pending": "ORDER_APPROVED",
              "approved_draft_gi": "ORDER_APPROVED", "open": "ORDER_OPEN",
              "cancelled": "ORDER_CANCELLED"}[plan]

    total = tax_total = 0
    so_item_rows = []
    for p, qty in items:
        ids["soi"] += 1
        disc = random.choice([0, 0, 0, 0, 2, 3, 5]) if cust["tier"] == "L" else 0
        amount = round(qty * p["price"] * (1 - disc / 100))
        tax = round(amount * 0.08)
        delivered = qty if plan == "completed" else 0
        row = dict(id=ids["soi"], so=so_id, product=p["id"], qty=qty, delivered=delivered,
                   disc=disc, unit="Thùng", price=p["price"], amount=amount, tax=tax,
                   total=amount + tax)
        so_item_rows.append(row)
        so_items.append(row)
        total += amount
        tax_total += tax

    sales_orders.append(dict(
        id=so_id, code=so_code, name=f"Đơn bánh kẹo {cust['name']}{tet_label}",
        cust=cust["id"], wh=wh, addr=cust["addr_id"], day=day, created=created_ts,
        approved=approved_ts, status=status, total=total, tax=tax_total,
        grand=total + tax_total, expected=day + timedelta(days=random.randint(1, 3)),
        completed=None, pay="UNPAID",
        reject=("Khách báo hủy do thay đổi kế hoạch nhập hàng" if plan == "cancelled" else None),
    ))
    so = sales_orders[-1]

    if plan == "approved_pending":
        for row in so_item_rows:
            ids["res"] += 1
            reservations.append(dict(id=ids["res"], product=row["product"], wh=wh, so=so_id,
                                     soi=row["id"], qty=row["qty"], at=approved_ts))
            reserved[(row["product"], wh)] = reserved.get((row["product"], wh), 0) + row["qty"]
        continue
    if plan in ("open", "cancelled"):
        continue

    # ---- Tạo phiếu xuất ----
    issue_day = (approved_ts + timedelta(days=random.randint(0, 2))).date()
    issue_day = min(issue_day, TODAY)
    ids["gi"] += 1
    gi_id = ids["gi"]
    gi_code = code("GI", issue_day.year)
    gi_status = "DRAFT" if plan == "approved_draft_gi" else "CONFIRMED"
    confirmed_ts = rt(issue_day, 9, 16) if gi_status == "CONFIRMED" else None
    gi_total = 0
    if gi_status == "CONFIRMED":
        for row in so_item_rows:
            slices = allocate(row["product"], wh, issue_day, row["qty"])
            for lot, take in slices:
                ids["gii"] += 1
                amt = round(take * row["price"] * (1 - row["disc"] / 100) * 1.08)
                gi_items.append(dict(id=ids["gii"], gi=gi_id, product=row["product"], soi=row["id"],
                                     ordered=row["qty"], issued=take, unit="Thùng",
                                     price=row["price"], total=amt,
                                     batch=lot["batch"], expiry=lot["expiry"]))
                gi_total += amt
                tx_events.append((confirmed_ts, "ISSUE", row["product"], wh, take, lot["cost"],
                                  "GOODS_ISSUE", gi_code, gi_id,
                                  f"Xuất kho theo phiếu {gi_code} (lô {lot['batch']})"))
        so["status"] = "ORDER_COMPLETED"
        so["completed"] = confirmed_ts
    else:
        for row in so_item_rows:
            ids["res"] += 1
            reservations.append(dict(id=ids["res"], product=row["product"], wh=wh, so=so_id,
                                     soi=row["id"], qty=row["qty"], at=approved_ts))
            reserved[(row["product"], wh)] = reserved.get((row["product"], wh), 0) + row["qty"]
        for row in so_item_rows:
            ids["gii"] += 1
            gi_items.append(dict(id=ids["gii"], gi=gi_id, product=row["product"], soi=row["id"],
                                 ordered=row["qty"], issued=0, unit="Thùng",
                                 price=row["price"], total=0, batch=None, expiry=None))

    goods_issues.append(dict(id=gi_id, code=gi_code, so=so_id, wh=wh, addr=so["addr"],
                             day=issue_day, status=gi_status, confirmed=confirmed_ts,
                             total=gi_total, cust=cust["id"],
                             carrier="Đội xe NPP Ngọt Việt", method="Xe tải"))

    if gi_status != "CONFIRMED":
        continue

    # ---- Hóa đơn ----
    ids["si"] += 1
    si_id = ids["si"]
    si_code = code("INV", issue_day.year)
    inv_day = issue_day
    due = inv_day + timedelta(days=cust["terms"])
    subtotal = sum(r["amount"] for r in so_item_rows)
    tax_amt = sum(r["tax"] for r in so_item_rows)
    total_amt = subtotal + tax_amt

    if (TODAY - issue_day).days <= 1 and random.random() < 0.5:
        st, paid, paid_day = "DRAFT", 0, None
    elif cust["terms"] == 0:
        st, paid, paid_day = "PAID", total_amt, inv_day
    elif due < TODAY:
        if cust["slow"] and random.random() < 0.5:
            if random.random() < 0.4:
                st, paid, paid_day = "PARTIALLY_PAID", round(total_amt * random.uniform(0.3, 0.7)), due - timedelta(days=2)
            else:
                st, paid, paid_day = "OVERDUE", 0, None
        else:
            st, paid = "PAID", total_amt
            paid_day = inv_day + timedelta(days=random.randint(2, max(3, cust["terms"])))
    else:
        if random.random() < 0.45:
            st, paid, paid_day = "PAID", total_amt, inv_day + timedelta(days=random.randint(1, 3))
        else:
            st, paid, paid_day = "ISSUED", 0, None

    if paid_day:
        paid_day = min(paid_day, TODAY)
    method = "Tiền mặt" if (cust["terms"] == 0 and random.random() < 0.7) else "Chuyển khoản"
    invoices.append(dict(id=si_id, code=si_code, so=so_id, gi=gi_id, cust=cust["id"],
                         day=inv_day, due=due, status=st, subtotal=subtotal, tax=tax_amt,
                         total=total_amt, paid=paid, paid_day=paid_day, method=method))
    so["pay"] = {"PAID": "PAID", "PARTIALLY_PAID": "PARTIALLY_PAID"}.get(st, "UNPAID")

    for gii in [g for g in gi_items if g["gi"] == gi_id]:
        p = PRODUCTS[gii["product"] - 1]
        soi = next(r for r in so_item_rows if r["id"] == gii["soi"])
        amount = round(gii["issued"] * gii["price"] * (1 - soi["disc"] / 100))
        tax = round(amount * 0.08)
        ids["sii"] += 1
        inv_items.append(dict(id=ids["sii"], si=si_id, product=p["id"], gii=gii["id"],
                              desc=p["name"], qty=gii["issued"], unit="Thùng",
                              price=gii["price"], disc=soi["disc"], amount=amount,
                              tax=tax, total=amount + tax))

    # ---- Đơn giao hàng ----
    ids["do_"] += 1
    delivery_orders.append(dict(id=ids["do_"], code=gi_code, so=so_id, gi_day=issue_day,
                                dest=CUSTOMERS[cust["id"] - 1]["addr_text"],
                                status="Delivered" if issue_day < date(2026, 6, 8) else "Pending"))

# ============================================================================
# 3. KẾ HOẠCH GIAO VẬN (tháng 5-6/2026)
# ============================================================================
plans_out, plan_orders_out, plan_shippers_out, trips_out, trip_items_out = [], [], [], [], []
PLAN_WEEKS = [date(2026, 5, 4), date(2026, 5, 11), date(2026, 5, 18), date(2026, 5, 25),
              date(2026, 6, 1), date(2026, 6, 8), date(2026, 6, 12)]
SHIPPERS = [("shipper1", "Phạm Minh Tuấn", "0903441257", 13),
            ("shipper2", "Võ Thành Long", "0907728193", 14)]
pid = tid = tii = poi2 = psi = 0
do_used = set()
for w_i, pday in enumerate(PLAN_WEEKS):
    week_dos = [o for o in delivery_orders
                if o["id"] not in do_used and pday - timedelta(days=7) <= o["gi_day"] < pday]
    week_dos = week_dos[:14]
    if not week_dos:
        continue
    pid += 1
    if pday == date(2026, 6, 8):
        status = "InProgress"
    elif pday == date(2026, 6, 12):
        status = "Created"
    else:
        status = "Completed"
    plans_out.append(dict(id=pid, code=f"DP-2026-{pid:03d}", day=pday, status=status,
                          desc=f"Kế hoạch giao hàng tuần {pday.strftime('%d/%m/%Y')} - khu vực TP.HCM"))
    for s in SHIPPERS:
        psi += 1
        plan_shippers_out.append(dict(id=psi, plan=pid, name=s[1], phone=s[2], uname=s[0]))
    for o in week_dos:
        poi2 += 1
        plan_orders_out.append(dict(id=poi2, plan=pid, do=o["id"]))
        do_used.add(o["id"])
    if status == "Created":
        continue
    half = (len(week_dos) + 1) // 2
    groups = [week_dos[:half], week_dos[half:]]
    for g_i, (uname, sname, sphone, legacy_uid) in enumerate(SHIPPERS):
        group = groups[g_i]
        if not group:
            continue
        tid += 1
        started = datetime(pday.year, pday.month, pday.day, 8, random.randint(0, 25))
        epoch_ms = int(started.timestamp() * 1000) + tid
        if status == "Completed":
            t_status, completed = "COMPLETED", datetime(pday.year, pday.month, pday.day, 16, random.randint(5, 55))
        else:
            t_status, completed = ("COMPLETED", datetime(pday.year, pday.month, pday.day, 15, 40)) if g_i == 0 \
                else ("IN_PROGRESS", None)
        trips_out.append(dict(id=tid, code=f"TRIP-{epoch_ms}-{legacy_uid}", plan=pid,
                              shipper=sname, uname=uname, status=t_status,
                              started=started, completed=completed))
        for s_i, o in enumerate(group, start=1):
            tii += 1
            if t_status == "COMPLETED":
                it_status = "Delivered"
                if pday == date(2026, 5, 18) and s_i == 3:
                    it_status = "Failed"   # khách hẹn giao lại tuần sau
            else:
                it_status = "Delivered" if s_i <= len(group) // 2 else "Pending"
            trip_items_out.append(dict(id=tii, trip=tid, do=o["id"], seq=s_i, status=it_status))
            for do_ in delivery_orders:
                if do_["id"] == o["id"]:
                    do_["status"] = {"Delivered": "Delivered", "Failed": "Failed", "Pending": "Pending"}[it_status]

# ============================================================================
# 4. TỒN KHO & THẺ KHO
# ============================================================================
tx_events.sort(key=lambda e: e[0])
running = {}
tx_rows = []
for i, (t, typ, p_id, wh, qty, cost, rtype, rcode, rid, note) in enumerate(tx_events, start=1):
    before = running.get((p_id, wh), 0)
    after = before + qty if typ == "RECEIPT" else before - qty
    running[(p_id, wh)] = after
    tx_rows.append(dict(id=i, t=t, typ=typ, product=p_id, wh=wh, qty=qty, before=before,
                        after=after, cost=cost, rtype=rtype, rcode=rcode, rid=rid, note=note))

inventory_rows = []
inv_id = 0
last_recv, last_issue = {}, {}
avg_cost_num, avg_cost_den = {}, {}
for r in tx_rows:
    key = (r["product"], r["wh"])
    if r["typ"] == "RECEIPT":
        last_recv[key] = r["t"]
        avg_cost_num[key] = avg_cost_num.get(key, 0) + r["qty"] * r["cost"]
        avg_cost_den[key] = avg_cost_den.get(key, 0) + r["qty"]
    else:
        last_issue[key] = r["t"]
for (p_id, wh), on_hand in sorted(running.items()):
    inv_id += 1
    res = min(reserved.get((p_id, wh), 0), on_hand)
    p = PRODUCTS[p_id - 1]
    md = monthly_demand(p, wh)
    inventory_rows.append(dict(id=inv_id, product=p_id, wh=wh, on_hand=on_hand, reserved=res,
                               available=on_hand - res,
                               avg=round(avg_cost_num.get((p_id, wh), 0) / max(1, avg_cost_den.get((p_id, wh), 1)), 2),
                               reorder=max(10, int(md * 0.4)), reorder_qty=max(20, int(md * 1.2)),
                               recv=last_recv.get((p_id, wh)), iss=last_issue.get((p_id, wh))))

# ============================================================================
# 5. EMIT SQL
# ============================================================================
L = []
A = L.append
A("-- =========================================================================")
A("-- V4: Dữ liệu demo - NPP bánh kẹo Ngọt Việt (doanh nghiệp phân phối bánh kẹo")
A("--     quy mô vừa, quản lý hạn sử dụng theo lô, xuất kho FEFO)")
A("-- Sinh tự động bởi backend/scripts/generate_demo_data.py - KHÔNG sửa tay.")
A(f"-- Mốc dữ liệu: 15/12/2025 -> {TODAY.strftime('%d/%m/%Y')} (Tết Bính Ngọ: 17/02/2026)")
A("-- =========================================================================")
A("")
A("-- Bảng do Hibernate tạo trong môi trường cũ: tạo nếu chưa có để file chạy được trên DB mới")
A("""CREATE TABLE IF NOT EXISTS inventory_lot (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouse(id),
    lot_number VARCHAR(64) NOT NULL,
    manufacture_date DATE,
    expiry_date DATE,
    quantity_received NUMERIC(18,3) NOT NULL CHECK (quantity_received > 0),
    quantity_remaining NUMERIC(18,3) NOT NULL CHECK (quantity_remaining >= 0),
    unit_cost NUMERIC(18,2),
    source_receipt_id BIGINT REFERENCES goods_receipt(id),
    source_receipt_item_id BIGINT REFERENCES goods_receipt_item(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_remaining_lte_received CHECK (quantity_remaining <= quantity_received)
);
CREATE TABLE IF NOT EXISTS delivery_order (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(255),
    destination_address VARCHAR(255),
    status VARCHAR(255),
    sales_order_id BIGINT
);
CREATE TABLE IF NOT EXISTS delivery_plan (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(255),
    created_date DATE,
    planned_date DATE,
    description VARCHAR(255),
    notes VARCHAR(255),
    status VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS delivery_plan_order (
    id BIGSERIAL PRIMARY KEY,
    delivery_order_id BIGINT,
    delivery_plan_id BIGINT
);
CREATE TABLE IF NOT EXISTS delivery_plan_shipper (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(255),
    shipper_name VARCHAR(255),
    shipper_user_id BIGINT,
    delivery_plan_id BIGINT
);
-- Cột entity mới thêm sau thời điểm DB được tạo bằng ddl-auto=update
ALTER TABLE delivery_plan ADD COLUMN IF NOT EXISTS planned_date DATE;
ALTER TABLE delivery_plan ADD COLUMN IF NOT EXISTS notes VARCHAR(255);
ALTER TABLE delivery_plan_shipper ADD COLUMN IF NOT EXISTS shipper_user_id BIGINT;
CREATE TABLE IF NOT EXISTS delivery_triproute (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(255) UNIQUE,
    completed_at TIMESTAMP,
    notes VARCHAR(500),
    shipper_name VARCHAR(255),
    started_at TIMESTAMP,
    status VARCHAR(50),
    delivery_plan_id BIGINT,
    shipper_user_id BIGINT
);
CREATE TABLE IF NOT EXISTS delivery_triproute_item (
    id BIGSERIAL PRIMARY KEY,
    sequence INTEGER,
    status VARCHAR(255),
    delivery_order_id BIGINT,
    triproute_id BIGINT
);

-- FK delivery_order.sales_order_id từng bị Hibernate sinh nhầm trỏ sang purchase_order
-- (V7 sửa nhưng chạy sau V4) -> sửa ngay tại đây, idempotent y hệt V7.
DO $$
DECLARE
    fk_name text;
BEGIN
    FOR fk_name IN
        SELECT tc.constraint_name
        FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu
          ON tc.constraint_name = kcu.constraint_name
         AND tc.table_schema = kcu.table_schema
        WHERE tc.constraint_type = 'FOREIGN KEY'
          AND tc.table_name = 'delivery_order'
          AND kcu.column_name = 'sales_order_id'
    LOOP
        EXECUTE format('ALTER TABLE delivery_order DROP CONSTRAINT %I', fk_name);
    END LOOP;
    ALTER TABLE delivery_order
        ADD CONSTRAINT fk_delivery_order_sales_order
        FOREIGN KEY (sales_order_id) REFERENCES sales_order(id);
END $$;""")
A("")
A("-- ===== Nhân sự: Việt hóa tên hiển thị (giữ nguyên username/mật khẩu demo) =====")
USER_NAMES = [
    ("admin", "Dương Phương Thảo", "thao.duong@ngotviet.vn"),
    ("purchase_staff", "Nguyễn Thị Thu Hà", "ha.nguyen@ngotviet.vn"),
    ("purchase_manager", "Đặng Hữu Nghĩa", "nghia.dang@ngotviet.vn"),
    ("sales_staff", "Trần Quốc Bảo", "bao.tran@ngotviet.vn"),
    ("sales_manager", "Vũ Thị Lan Anh", "lananh.vu@ngotviet.vn"),
    ("warehouse_staff", "Lê Văn Tâm", "tam.le@ngotviet.vn"),
    ("delivery_admin", "Hoàng Văn Dũng", "dung.hoang@ngotviet.vn"),
    ("shipper1", "Phạm Minh Tuấn", "tuan.pham@ngotviet.vn"),
    ("shipper2", "Võ Thành Long", "long.vo@ngotviet.vn"),
    ("accountant", "Ngô Thị Kim Yến", "yen.ngo@ngotviet.vn"),
    ("john.buyer", "Bùi Đức Long", "long.bui@ngotviet.vn"),
    ("jane.manager", "Lý Thị Mai", "mai.ly@ngotviet.vn"),
    ("bob.warehouse", "Trương Văn Phú", "phu.truong@ngotviet.vn"),
    ("alice.accountant", "Đỗ Thị Hồng Nhung", "nhung.do@ngotviet.vn"),
    ("sarah.sales", "Mai Thị Ngọc Trâm", "tram.mai@ngotviet.vn"),
    ("mike.manager", "Đinh Công Thành", "thanh.dinh@ngotviet.vn"),
    ("admin_real", "Phan Quốc Khánh", "khanh.phan@ngotviet.vn"),
    ("WAREHOUSE_STAFF", "Hồ Văn Sang", "sang.ho@ngotviet.vn"),
    ("PURCHASE_STAFF", "Lương Thị Bích Phượng", "phuong.luong@ngotviet.vn"),
    ("PURCHASE_MANAGER", "Tạ Quang Hiếu", "hieu.ta@ngotviet.vn"),
    ("SALES_STAFF", "Châu Mỹ Duyên", "duyen.chau@ngotviet.vn"),
    ("SALES_MANAGER", "La Văn Quyết", "quyet.la@ngotviet.vn"),
    ("DELIVERY_ADMIN", "Tô Hoài Nam", "nam.to@ngotviet.vn"),
    ("SHIPPER", "Ông Văn Tài", "tai.ong@ngotviet.vn"),
    ("ACCOUNTANT", "Thái Thị Thanh Thúy", "thuy.thai@ngotviet.vn"),
    ("testuser2", "Cao Xuân Trường", "truong.cao@ngotviet.vn"),
]
for uname, full, email in USER_NAMES:
    A(f"UPDATE users SET full_name={q(full)}, email={q(email)} WHERE username={q(uname)};")
A("")
A("-- ===== Xóa dữ liệu nghiệp vụ cũ (giữ users/roles) =====")
for t in ["sales_invoice_item", "sales_invoice", "delivery_triproute_item", "delivery_triproute",
          "delivery_plan_order", "delivery_plan_shipper", "delivery_plan", "delivery_order",
          "goods_issue_item", "goods_issue", "inventory_reservation", "inventory_transaction",
          "inventory_lot", "inventory", "goods_receipt_item", "goods_receipt",
          "sales_order_item", "sales_order", "purchase_order_item", "purchase_order",
          "delivery_address", "customer", "invoice", "product", "supplier", "warehouse"]:
    A(f"DELETE FROM {t};")
A("")

def emit(table, cols, rows, batch=150):
    if not rows:
        return
    A(f"-- {table}: {len(rows)} dòng")
    for i in range(0, len(rows), batch):
        chunk = rows[i:i + batch]
        A(f"INSERT INTO {table} ({', '.join(cols)}) VALUES")
        A(",\n".join("(" + ", ".join(r) + ")" for r in chunk) + ";")
    A("")

emit("warehouse", ["id", "code", "name", "location", "description"],
     [[str(i), q(c), q(n), q(l), q(de)] for i, c, n, l, de in WAREHOUSES])

emit("supplier", ["id", "code", "name", "address", "contact_name", "email", "phone"],
     [[str(s[0]), q(s[1]), q(s[2]), q(s[3]), q(s[4]), q(s[5]), q(s[6])] for s in SUPPLIERS])

emit("product", ["id", "code", "name", "description", "price", "quantity", "supplier_id"],
     [[str(p["id"]), q(p["code"]), q(p["name"]), q(p["desc"]), str(p["price"]), "0", str(p["sup"])]
      for p in PRODUCTS])

emit("customer", ["id", "code", "name", "contact_name", "phone", "email", "tax_code",
                  "credit_limit", "current_balance", "payment_terms", "active", "created_at"],
     [[str(c["id"]), q(c["code"]), q(c["name"]), q(c["contact"]), q(c["phone"]), q(c["email"]),
       q(c["tax"]), money(c["credit"]), "0.00", str(c["terms"]), "TRUE", ts(datetime(2025, 12, 10, 9, 0))]
      for c in CUSTOMERS])

emit("delivery_address", ["id", "customer_id", "address_name", "address_line1", "city", "country",
                          "recipient_name", "phone", "is_default", "created_at"],
     [[str(a["id"]), str(a["cust"]), q(a["name"]), q(a["line1"]), q(a["city"]), q("Việt Nam"),
       q(a["recipient"]), q(a["phone"]), "TRUE" if a["default"] else "FALSE",
       ts(datetime(2025, 12, 10, 9, 30))] for a in ADDRESSES])

emit("purchase_order",
     ["id", "code", "order_name", "supplier_id", "warehouse_id", "status", "created_by",
      "approved_by", "created_date", "approved_date", "delivery_date", "completed_date",
      "total_amount", "shipping_cost", "rejection_reason", "updated_at"],
     [[str(p["id"]), q(p["code"]), q(p["name"]), str(p["sup"]), str(p["wh"]), q(p["status"]),
       U("purchase_staff"), U("purchase_manager") if p["approved"] else "NULL",
       d(p["created"]), ts(rt(p["approved"])) if p["approved"] else "NULL",
       ts(rt(p["delivery"])) if p["delivery"] else "NULL",
       ts(p["completed"]) if p["completed"] else "NULL",
       money(p["total"]), "0.00", q(p["reject"]),
       ts(p["completed"] or rt(p["created"]))] for p in purchase_orders])

emit("purchase_order_item",
     ["id", "purchase_order_id", "product_id", "quantity", "received_quantity", "unit",
      "unit_price", "cost_before_tax", "amount_before_tax", "tax_amount", "total_amount"],
     [[str(i["id"]), str(i["po"]), str(i["product"]), str(i["qty"]), str(i["recv"]), q(i["unit"]),
       money(i["price"]), money(i["price"]), money(i["amount"]), money(i["tax"]), money(i["total"])]
      for i in po_items])

emit("goods_receipt",
     ["id", "code", "purchase_order_id", "warehouse_id", "receipt_date", "status", "created_by",
      "confirmed_by", "confirmed_date", "total_amount", "notes", "created_at", "updated_at"],
     [[str(g["id"]), q(g["code"]), str(g["po"]), str(g["wh"]), d(g["day"]), q(g["status"]),
       U("warehouse_staff"), U("warehouse_staff") if g["confirmed"] else "NULL",
       ts(g["confirmed"]), money(g["total"]), q(g["note"]),
       ts(g["confirmed"] or rt(g["day"])), ts(g["confirmed"] or rt(g["day"]))]
      for g in goods_receipts])

emit("goods_receipt_item",
     ["id", "goods_receipt_id", "purchase_order_item_id", "product_id", "ordered_quantity",
      "received_quantity", "accepted_quantity", "rejected_quantity", "rejection_reason",
      "batch_number", "expiry_date", "unit", "unit_price", "total_amount", "notes"],
     [[str(i["id"]), str(i["gr"]), str(i["poi"]), str(i["product"]), str(i["ordered"]),
       str(i["received"]), str(i["accepted"]), str(i["rejected"]), q(i["reason"]),
       q(i["batch"]), d(i["expiry"]), q(i["unit"]), money(i["price"]), money(i["total"]),
       q(f"NSX {i['mfg'].strftime('%d/%m/%Y')}")] for i in gr_items])

emit("inventory_lot",
     ["id", "product_id", "warehouse_id", "lot_number", "manufacture_date", "expiry_date",
      "quantity_received", "quantity_remaining", "unit_cost", "source_receipt_id",
      "source_receipt_item_id", "created_at", "updated_at"],
     [[str(l["id"]), str(l["product"]), str(l["wh"]), q(l["batch"]), d(l["mfg"]), d(l["expiry"]),
       str(l["received"]), str(l["remaining"]), money(l["cost"]), str(l["gr"]), str(l["gri"]),
       ts(l["created"]), ts(l["created"])] for l in lots])

emit("sales_order",
     ["id", "code", "order_name", "customer_id", "warehouse_id", "delivery_address_id", "status",
      "payment_status", "created_by", "approved_by", "order_date", "approved_date",
      "expected_delivery_date", "completed_date", "total_amount", "tax_amount", "discount_amount",
      "shipping_cost", "grand_total", "rejection_reason", "created_at", "updated_at"],
     [[str(s["id"]), q(s["code"]), q(s["name"]), str(s["cust"]), str(s["wh"]), str(s["addr"]),
       q(s["status"]), q(s["pay"]), U("sales_staff"),
       U("sales_manager") if s["approved"] else "NULL",
       d(s["day"]), ts(s["approved"]), d(s["expected"]), ts(s["completed"]),
       money(s["total"]), money(s["tax"]), "0.00", "0.00", money(s["grand"]),
       q(s["reject"]), ts(s["created"]), ts(s["completed"] or s["approved"] or s["created"])]
      for s in sales_orders])

emit("sales_order_item",
     ["id", "sales_order_id", "product_id", "quantity", "delivered_quantity", "unit",
      "unit_price", "discount_percent", "amount_before_tax", "tax_percent", "tax_amount",
      "total_amount"],
     [[str(i["id"]), str(i["so"]), str(i["product"]), str(i["qty"]), str(i["delivered"]),
       q(i["unit"]), money(i["price"]), money(i["disc"]), money(i["amount"]), "8.00",
       money(i["tax"]), money(i["total"])] for i in so_items])

emit("inventory_reservation",
     ["id", "product_id", "warehouse_id", "sales_order_id", "sales_order_item_id",
      "reserved_quantity", "fulfilled_quantity", "status", "reserved_at", "expires_at"],
     [[str(r["id"]), str(r["product"]), str(r["wh"]), str(r["so"]), str(r["soi"]),
       str(r["qty"]), "0", q("ACTIVE"), ts(r["at"]), ts(r["at"] + timedelta(days=7))]
      for r in reservations])

emit("goods_issue",
     ["id", "code", "sales_order_id", "warehouse_id", "delivery_address_id", "issue_date",
      "status", "created_by", "confirmed_by", "confirmed_date", "total_amount",
      "shipping_method", "carrier_name", "delivery_note_number", "notes", "created_at", "updated_at"],
     [[str(g["id"]), q(g["code"]), str(g["so"]), str(g["wh"]), str(g["addr"]), d(g["day"]),
       q(g["status"]), U("warehouse_staff"), U("warehouse_staff") if g["confirmed"] else "NULL",
       ts(g["confirmed"]), money(g["total"]), q(g["method"]), q(g["carrier"]),
       q(f"PGH-{g['code'][3:]}"), q("Xuất kho theo nguyên tắc FEFO"),
       ts(g["confirmed"] or rt(g["day"])), ts(g["confirmed"] or rt(g["day"]))]
      for g in goods_issues])

emit("goods_issue_item",
     ["id", "goods_issue_id", "sales_order_item_id", "product_id", "ordered_quantity",
      "issued_quantity", "unit", "unit_price", "total_amount", "batch_number", "expiry_date"],
     [[str(i["id"]), str(i["gi"]), str(i["soi"]), str(i["product"]), str(i["ordered"]),
       str(i["issued"]), q(i["unit"]), money(i["price"]), money(i["total"]),
       q(i["batch"]), d(i["expiry"])] for i in gi_items])

emit("sales_invoice",
     ["id", "code", "sales_order_id", "goods_issue_id", "customer_id", "invoice_date", "due_date",
      "status", "subtotal", "tax_amount", "discount_amount", "shipping_cost", "total_amount",
      "paid_amount", "remaining_amount", "paid_date", "payment_method", "created_by", "issued_by",
      "issued_date", "created_at", "updated_at"],
     [[str(v["id"]), q(v["code"]), str(v["so"]), str(v["gi"]), str(v["cust"]), d(v["day"]),
       d(v["due"]), q(v["status"]), money(v["subtotal"]), money(v["tax"]), "0.00", "0.00",
       money(v["total"]), money(v["paid"]), money(v["total"] - v["paid"]),
       ts(rt(v["paid_day"])) if v["paid_day"] else "NULL", q(v["method"]),
       U("accountant"), U("accountant") if v["status"] != "DRAFT" else "NULL",
       ts(rt(v["day"])) if v["status"] != "DRAFT" else "NULL",
       ts(rt(v["day"])), ts(rt(v["paid_day"] or v["day"]))] for v in invoices])

emit("sales_invoice_item",
     ["id", "sales_invoice_id", "goods_issue_item_id", "product_id", "description", "quantity",
      "unit", "unit_price", "discount_percent", "amount_before_tax", "tax_percent", "tax_amount",
      "total_amount"],
     [[str(i["id"]), str(i["si"]), str(i["gii"]), str(i["product"]), q(i["desc"]), str(i["qty"]),
       q(i["unit"]), money(i["price"]), money(i["disc"]), money(i["amount"]), "8.00",
       money(i["tax"]), money(i["total"])] for i in inv_items])

emit("inventory_transaction",
     ["id", "transaction_type", "product_id", "warehouse_id", "quantity", "quantity_before",
      "quantity_after", "unit_cost", "total_cost", "reference_type", "reference_id",
      "reference_code", "transaction_date", "created_by", "notes"],
     [[str(r["id"]), q(r["typ"]), str(r["product"]), str(r["wh"]), str(r["qty"]),
       str(r["before"]), str(r["after"]), money(r["cost"]), money(r["qty"] * r["cost"]),
       q(r["rtype"]), str(r["rid"]), q(r["rcode"]), ts(r["t"]), U("warehouse_staff"),
       q(r["note"])] for r in tx_rows])

emit("inventory",
     ["id", "product_id", "warehouse_id", "quantity_on_hand", "quantity_reserved",
      "quantity_available", "average_cost", "reorder_level", "reorder_quantity",
      "last_received_date", "last_issued_date", "version", "updated_at"],
     [[str(v["id"]), str(v["product"]), str(v["wh"]), str(v["on_hand"]), str(v["reserved"]),
       str(v["available"]), money(v["avg"]), str(v["reorder"]), str(v["reorder_qty"]),
       ts(v["recv"]), ts(v["iss"]), "0", ts(v["iss"] or v["recv"])] for v in inventory_rows])

emit("delivery_order", ["id", "code", "destination_address", "status", "sales_order_id"],
     [[str(o["id"]), q(o["code"]), q(o["dest"]), q(o["status"]), str(o["so"])]
      for o in delivery_orders])

emit("delivery_plan", ["id", "code", "created_date", "planned_date", "description", "notes", "status"],
     [[str(p["id"]), q(p["code"]), d(p["day"]), d(p["day"]), q(p["desc"]), "NULL", q(p["status"])]
      for p in plans_out])

emit("delivery_plan_order", ["id", "delivery_plan_id", "delivery_order_id"],
     [[str(p["id"]), str(p["plan"]), str(p["do"])] for p in plan_orders_out])

emit("delivery_plan_shipper", ["id", "delivery_plan_id", "shipper_name", "phone", "shipper_user_id"],
     [[str(p["id"]), str(p["plan"]), q(p["name"]), q(p["phone"]), U(p["uname"])]
      for p in plan_shippers_out])

emit("delivery_triproute",
     ["id", "code", "delivery_plan_id", "shipper_user_id", "shipper_name", "status",
      "started_at", "completed_at", "notes"],
     [[str(t["id"]), q(t["code"]), str(t["plan"]), U(t["uname"]), q(t["shipper"]), q(t["status"]),
       ts(t["started"]), ts(t["completed"]), "NULL"] for t in trips_out])

emit("delivery_triproute_item", ["id", "triproute_id", "delivery_order_id", "sequence", "status"],
     [[str(i["id"]), str(i["trip"]), str(i["do"]), str(i["seq"]), q(i["status"])]
      for i in trip_items_out])

A("-- ===== Đồng bộ sequence =====")
for t in ["warehouse", "supplier", "product", "customer", "delivery_address", "purchase_order",
          "purchase_order_item", "goods_receipt", "goods_receipt_item", "inventory_lot",
          "sales_order", "sales_order_item", "inventory_reservation", "goods_issue",
          "goods_issue_item", "sales_invoice", "sales_invoice_item", "inventory_transaction",
          "inventory", "delivery_order", "delivery_plan", "delivery_plan_order",
          "delivery_plan_shipper", "delivery_triproute", "delivery_triproute_item"]:
    A(f"SELECT setval(pg_get_serial_sequence('{t}','id'), GREATEST((SELECT COALESCE(MAX(id),0) FROM {t}), 1));")

OUT.write_text("\n".join(L) + "\n", encoding="utf-8")

# ============================================================================
# 6. SUMMARY
# ============================================================================
expired = [l for l in lots if l["remaining"] > 0 and l["expiry"] <= TODAY]
near = [l for l in lots if l["remaining"] > 0 and TODAY < l["expiry"] <= TODAY + timedelta(days=30)]
soon = [l for l in lots if l["remaining"] > 0 and TODAY + timedelta(days=30) < l["expiry"] <= TODAY + timedelta(days=60)]
rev = {}
for v in invoices:
    rev[(v["day"].year, v["day"].month)] = rev.get((v["day"].year, v["day"].month), 0) + v["total"]
print(f"File: {OUT} ({OUT.stat().st_size/1024:.0f} KB)")
print(f"PO {len(purchase_orders)} | POI {len(po_items)} | GR {len(goods_receipts)} | GRI {len(gr_items)} | lot {len(lots)}")
print(f"SO {len(sales_orders)} | SOI {len(so_items)} | GI {len(goods_issues)} | GII {len(gi_items)}")
print(f"INV {len(invoices)} | tx {len(tx_rows)} | inventory {len(inventory_rows)} | res {len(reservations)}")
print(f"DO {len(delivery_orders)} | plan {len(plans_out)} | trip {len(trips_out)} | trip items {len(trip_items_out)}")
print(f"Lô hết hạn còn tồn: {len(expired)} | cận hạn <=30 ngày: {len(near)} | 31-60 ngày: {len(soon)}")
for l in expired[:8]:
    print(f"  EXPIRED  {PRODUCTS[l['product']-1]['name'][:40]:42s} lô {l['batch']} HSD {l['expiry']} còn {l['remaining']}")
for l in near[:8]:
    print(f"  NEAR     {PRODUCTS[l['product']-1]['name'][:40]:42s} lô {l['batch']} HSD {l['expiry']} còn {l['remaining']}")
print("Doanh thu theo tháng (hóa đơn):")
for k in sorted(rev):
    print(f"  {k[0]}-{k[1]:02d}: {rev[k]/1e9:.2f} tỷ")
inv_status = {}
for v in invoices:
    inv_status[v["status"]] = inv_status.get(v["status"], 0) + 1
print("Hóa đơn:", inv_status)
so_status = {}
for s in sales_orders:
    so_status[s["status"]] = so_status.get(s["status"], 0) + 1
print("SO:", so_status)
po_status = {}
for p in purchase_orders:
    po_status[p["status"]] = po_status.get(p["status"], 0) + 1
print("PO:", po_status)
