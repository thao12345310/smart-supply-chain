-- =====================================================
-- V4__sample_data.sql
-- Dữ liệu mô phỏng 3 tháng (Jan–Mar 2026)
-- Quy mô: Doanh nghiệp vừa-lớn
-- =====================================================

-- =====================================================
-- SUPPLIERS (10 nhà cung cấp)
-- =====================================================
INSERT INTO supplier (code, name, contact_name, phone, email, address) VALUES
    ('SUP001', 'Công ty TNHH Linh Kiện Điện Tử Miền Nam', 'Nguyễn Văn An', '028-3823-1100', 'an.nguyen@linhkienmn.vn', '45 Đinh Tiên Hoàng, Q.1, TP.HCM'),
    ('SUP002', 'Công ty CP Vật Liệu Xây Dựng Hoàng Phát', 'Trần Thị Bích', '028-3945-2200', 'bich.tran@hoangphat.vn', '123 Nguyễn Tất Thành, Q.4, TP.HCM'),
    ('SUP003', 'Công ty TNHH Thiết Bị Công Nghiệp Đại Việt', 'Lê Minh Cường', '024-3556-3300', 'cuong.le@daiviet.vn', '88 Trần Duy Hưng, Cầu Giấy, Hà Nội'),
    ('SUP004', 'Công ty CP Hóa Chất Sài Gòn', 'Phạm Thị Dung', '028-3721-4400', 'dung.pham@hoachatsaigon.vn', '67 Lê Đại Hành, Q.11, TP.HCM'),
    ('SUP005', 'Công ty TNHH Bao Bì Nhựa Tiến Phát', 'Hoàng Văn Em', '0251-382-5500', 'em.hoang@tienphatapc.vn', '12 KCN Biên Hòa 2, Đồng Nai'),
    ('SUP006', 'Công ty CP Thép Miền Nam', 'Vũ Thị Hoa', '028-3836-6600', 'hoa.vu@thepmiennam.vn', '156 Nguyễn Văn Linh, Q.7, TP.HCM'),
    ('SUP007', 'Công ty TNHH Inox & Kim Loại Ánh Kim', 'Đặng Minh Hiếu', '028-3864-7700', 'hieu.dang@anhkim.vn', '34 Hùng Vương, Q.5, TP.HCM'),
    ('SUP008', 'Công ty CP Điện Công Nghiệp Việt Nam', 'Bùi Thị Lan', '024-3766-8800', 'lan.bui@diencnvn.vn', '200 Trường Chinh, Đống Đa, Hà Nội'),
    ('SUP009', 'Công ty TNHH Cao Su Kỹ Thuật Đông Nam', 'Ngô Văn Minh', '0274-382-9900', 'minh.ngo@caosudn.vn', '78 KCN Mỹ Phước 3, Bình Dương'),
    ('SUP010', 'Công ty CP Linh Phụ Kiện Cơ Khí Hà Nội', 'Đinh Thị Nga', '024-3645-1010', 'nga.dinh@linhuakien.vn', '15 Giải Phóng, Hoàng Mai, Hà Nội')
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- WAREHOUSES (5 kho)
-- =====================================================
INSERT INTO warehouse (code, name, location, description) VALUES
    ('WH001', 'Kho Tổng TP.HCM', 'Lô A, KCN Tân Bình, TP.HCM', 'Kho trung tâm, tổng diện tích 5000m2'),
    ('WH002', 'Kho Miền Nam - Bình Dương', 'KCN VSIP II, Bình Dương', 'Kho vệ tinh khu vực miền Nam, 2500m2'),
    ('WH003', 'Kho Miền Bắc - Hà Nội', 'KCN Thăng Long, Hà Nội', 'Kho khu vực miền Bắc, 3000m2'),
    ('WH004', 'Kho Miền Trung - Đà Nẵng', 'KCN Hòa Khánh, Đà Nẵng', 'Kho khu vực miền Trung, 1500m2'),
    ('WH005', 'Kho Xuất Khẩu - Cảng Cát Lái', 'Cảng Cát Lái, Q.2, TP.HCM', 'Kho chuyên hàng xuất khẩu, 2000m2')
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- PRODUCTS (25 sản phẩm)
-- =====================================================
INSERT INTO product (code, name, description, quantity, price, supplier_id) VALUES
    ('PRD001', 'Board mạch điều khiển PLC S7-300', 'PLC Siemens S7-300, CPU 314C-2 DP', 0, 8500000.00, (SELECT id FROM supplier WHERE code='SUP001')),
    ('PRD002', 'Cảm biến nhiệt độ PT100', 'Cảm biến PT100, dải đo -50 đến 200°C, IP67', 0, 450000.00, (SELECT id FROM supplier WHERE code='SUP001')),
    ('PRD003', 'Biến tần ABB ACS550 5.5kW', 'Biến tần 3 pha 380V, 5.5kW, IP21', 0, 12000000.00, (SELECT id FROM supplier WHERE code='SUP001')),
    ('PRD004', 'Xi măng Hà Tiên 50kg', 'Xi măng Portland PC40, bao 50kg', 0, 95000.00, (SELECT id FROM supplier WHERE code='SUP002')),
    ('PRD005', 'Gạch ceramic 60x60 cao cấp', 'Gạch ceramic men bóng, kích thước 60x60cm', 0, 180000.00, (SELECT id FROM supplier WHERE code='SUP002')),
    ('PRD006', 'Máy bơm công nghiệp GRUNDFOS 15kW', 'Máy bơm ly tâm, lưu lượng 120 m3/h, cột áp 30m', 0, 45000000.00, (SELECT id FROM supplier WHERE code='SUP003')),
    ('PRD007', 'Van bi inox 304 DN50', 'Van bi inox 304, DN50, PN16, tay vặn', 0, 320000.00, (SELECT id FROM supplier WHERE code='SUP003')),
    ('PRD008', 'Hóa chất xử lý nước NaOH 99%', 'Natri Hydroxide công nghiệp, bao 25kg', 0, 280000.00, (SELECT id FROM supplier WHERE code='SUP004')),
    ('PRD009', 'Acid Sulfuric 98% công nghiệp', 'H2SO4 98%, can 35kg', 0, 350000.00, (SELECT id FROM supplier WHERE code='SUP004')),
    ('PRD010', 'Thùng nhựa HDPE 200L', 'Thùng nhựa HDPE trắng, dung tích 200L, có nắp', 0, 650000.00, (SELECT id FROM supplier WHERE code='SUP005')),
    ('PRD011', 'Pallet nhựa 1200x1000', 'Pallet nhựa HDPE, tải trọng 1500kg, 4 chiều vào', 0, 1200000.00, (SELECT id FROM supplier WHERE code='SUP005')),
    ('PRD012', 'Thép hộp 50x50x2mm', 'Thép hộp mạ kẽm, 50x50x2mm, cây 6m', 0, 280000.00, (SELECT id FROM supplier WHERE code='SUP006')),
    ('PRD013', 'Thép tấm SS400 3mm', 'Thép tấm cán nóng SS400, dày 3mm, 1220x2440mm', 0, 1850000.00, (SELECT id FROM supplier WHERE code='SUP006')),
    ('PRD014', 'Ống inox 304 phi 76 x 2mm', 'Ống inox 304, phi 76, dày 2mm, cây 6m', 0, 980000.00, (SELECT id FROM supplier WHERE code='SUP007')),
    ('PRD015', 'Tấm inox 304 1.5mm', 'Tấm inox 304 2B, dày 1.5mm, 1000x2000mm', 0, 2200000.00, (SELECT id FROM supplier WHERE code='SUP007')),
    ('PRD016', 'Cáp điện CADIVI 3x50mm2', 'Cáp Cu/PVC/PVC hạ thế, 3x50mm2, cuộn 100m', 0, 18500000.00, (SELECT id FROM supplier WHERE code='SUP008')),
    ('PRD017', 'Aptomat MCB CHINT 3P 100A', 'MCB 3 pha, 100A, 6kA, DIN rail', 0, 850000.00, (SELECT id FROM supplier WHERE code='SUP008')),
    ('PRD018', 'Dây curoa cao su Type B70', 'Dây curoa hình thang, tiết diện B, dài 70 inch', 0, 95000.00, (SELECT id FROM supplier WHERE code='SUP009')),
    ('PRD019', 'Gioăng cao su chịu nhiệt EPDM DN100', 'Gioăng EPDM, DN100, chịu nhiệt 150°C', 0, 45000.00, (SELECT id FROM supplier WHERE code='SUP009')),
    ('PRD020', 'Vòng bi SKF 6205-2RS', 'Vòng bi cầu 1 dãy, che kín 2 mặt, 25x52x15mm', 0, 85000.00, (SELECT id FROM supplier WHERE code='SUP010')),
    ('PRD021', 'Vít me bi THK BNF1520', 'Vít me bi THK BNF1520-8, bước 20mm, dài 1500mm', 0, 4500000.00, (SELECT id FROM supplier WHERE code='SUP010')),
    ('PRD022', 'Khớp nối đàn hồi GE28', 'Khớp nối đàn hồi GE28, lỗ 14-19mm', 0, 180000.00, (SELECT id FROM supplier WHERE code='SUP010')),
    ('PRD023', 'Encoder Autonics E40S6', 'Encoder tăng lượng 600P/R, trục 6mm, 5-24VDC', 0, 1200000.00, (SELECT id FROM supplier WHERE code='SUP001')),
    ('PRD024', 'Relay nhiệt LS MT-32 28-40A', 'Relay nhiệt, dải 28-40A, reset tay/tự động', 0, 380000.00, (SELECT id FROM supplier WHERE code='SUP008')),
    ('PRD025', 'Đồng hồ áp suất Wise P110 0-10 bar', 'Đồng hồ áp suất mặt 100mm, 0-10 bar, kết nối 1/2"', 0, 320000.00, (SELECT id FROM supplier WHERE code='SUP003'))
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- CUSTOMERS (15 khách hàng)
-- =====================================================
INSERT INTO customer (code, name, contact_name, phone, email, tax_code, credit_limit, payment_terms, active) VALUES
    ('CUST001', 'Công ty CP Chế Tạo Máy Bình Dương', 'Trần Hữu Phúc', '0274-395-1111', 'phuc.tran@ctmbd.vn', '3701234567', 500000000.00, 30, true),
    ('CUST002', 'Công ty TNHH SX Nhựa Kỹ Thuật Đông Á', 'Lê Thị Mỹ Hạnh', '028-3755-2222', 'hanh.le@donganhua.vn', '0312345678', 300000000.00, 45, true),
    ('CUST003', 'Tổng Công ty Xây Dựng Số 1 - TNHH MTV', 'Nguyễn Công Trí', '024-3566-3333', 'tri.nguyen@xaydung1.vn', '0100234567', 800000000.00, 60, true),
    ('CUST004', 'Công ty CP Thực Phẩm Sài Gòn Food', 'Phạm Ngọc Hân', '028-3844-4444', 'han.pham@saigonfood.vn', '0300345678', 400000000.00, 30, true),
    ('CUST005', 'Công ty TNHH Điện Tử Samsung Vina', 'Kim Jae-won', '0222-396-5555', 'jaewon.kim@samsungvina.vn', '2300456789', 2000000000.00, 45, true),
    ('CUST006', 'Công ty CP Nước Sạch Môi Trường Xanh', 'Vũ Đình Khoa', '028-3910-6666', 'khoa.vu@moitruongxanh.vn', '0303567890', 250000000.00, 30, true),
    ('CUST007', 'Công ty TNHH Cơ Khí Chính Xác Tân Thành', 'Đỗ Văn Long', '0274-382-7777', 'long.do@tanthanh.vn', '3700678901', 350000000.00, 45, true),
    ('CUST008', 'Công ty CP Dược Phẩm Imexpharm', 'Trương Thị Mai', '0292-389-8888', 'mai.truong@imexpharm.vn', '1800789012', 600000000.00, 60, true),
    ('CUST009', 'Công ty TNHH Dệt May Việt Tiến', 'Nguyễn Thanh Nam', '028-3864-9999', 'nam.nguyen@viettien.vn', '0301890123', 700000000.00, 30, true),
    ('CUST010', 'Công ty CP Giấy Bãi Bằng', 'Hoàng Phi Yến', '0210-388-0101', 'yen.hoang@giaybaibang.vn', '2600901234', 450000000.00, 45, true),
    ('CUST011', 'Công ty TNHH Ô Tô Trường Hải - THACO', 'Bùi Ngọc Anh', '0235-385-0202', 'anh.bui@thaco.vn', '5100012345', 1500000000.00, 60, true),
    ('CUST012', 'Công ty CP Xi Măng Hà Tiên 2', 'Đinh Quốc Bảo', '028-3751-0303', 'bao.dinh@hatien2.vn', '0303123456', 900000000.00, 45, true),
    ('CUST013', 'Công ty TNHH Thép Hòa Phát Miền Nam', 'Lý Thiên Cương', '028-3613-0404', 'cuong.ly@hoaphat.vn', '0312234567', 1200000000.00, 30, true),
    ('CUST014', 'Công ty CP Nhựa Đà Nẵng', 'Phan Thị Diễm', '0236-395-0505', 'diem.phan@nhuadanang.vn', '0400345678', 200000000.00, 30, true),
    ('CUST015', 'Công ty TNHH KCN Long Hậu', 'Mai Thanh Gia', '028-3718-0606', 'gia.mai@longhau.vn', '0311456789', 600000000.00, 45, true)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- DELIVERY ADDRESSES
-- =====================================================
INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, country, is_default)
SELECT c.id, 'Trụ sở chính', c.contact_name, c.phone, '100 Bình Dương Avenue', 'Bình Dương', 'Bình Dương', '75000', 'Vietnam', true FROM customer c WHERE c.code = 'CUST001' ON CONFLICT DO NOTHING;
INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, country, is_default)
SELECT c.id, 'Nhà máy KCN Sóng Thần', 'Quản lý kho', '0274-395-1112', 'Lô B12 KCN Sóng Thần 2', 'Bình Dương', 'Bình Dương', '75000', 'Vietnam', false FROM customer c WHERE c.code = 'CUST001' ON CONFLICT DO NOTHING;

INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, country, is_default)
SELECT c.id, 'Văn phòng chính', c.contact_name, c.phone, '45 Đinh Bộ Lĩnh, Q.Bình Thạnh', 'TP.HCM', 'TP.HCM', '70000', 'Vietnam', true FROM customer c WHERE c.code = 'CUST002' ON CONFLICT DO NOTHING;

INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, country, is_default)
SELECT c.id, 'Ban điều hành', c.contact_name, c.phone, '37 Lê Đại Hành, Hai Bà Trưng', 'Hà Nội', 'Hà Nội', '10000', 'Vietnam', true FROM customer c WHERE c.code = 'CUST003' ON CONFLICT DO NOTHING;
INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, country, is_default)
SELECT c.id, 'Công trình Ecopark', 'Chỉ huy trưởng', '024-3566-3334', 'Khu đô thị Ecopark, Hưng Yên', 'Hưng Yên', 'Hưng Yên', '17000', 'Vietnam', false FROM customer c WHERE c.code = 'CUST003' ON CONFLICT DO NOTHING;

INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, country, is_default)
SELECT c.id, 'Nhà máy Bình Chánh', c.contact_name, c.phone, 'Lô 23 KCN Lê Minh Xuân, Bình Chánh', 'TP.HCM', 'TP.HCM', '70000', 'Vietnam', true FROM customer c WHERE c.code = 'CUST004' ON CONFLICT DO NOTHING;

INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, country, is_default)
SELECT c.id, 'Samsung Complex', c.contact_name, c.phone, 'KCN Yên Phong, Bắc Ninh', 'Bắc Ninh', 'Bắc Ninh', '16000', 'Vietnam', true FROM customer c WHERE c.code = 'CUST005' ON CONFLICT DO NOTHING;

INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, country, is_default)
SELECT c.id, 'Nhà máy xử lý nước', c.contact_name, c.phone, '233 Nguyễn Trãi, Q.Thanh Xuân', 'Hà Nội', 'Hà Nội', '10000', 'Vietnam', true FROM customer c WHERE c.code = 'CUST006' ON CONFLICT DO NOTHING;

INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, country, is_default)
SELECT c.id, 'Xưởng sản xuất', c.contact_name, c.phone, 'Lô CN-07 KCN Đồng An, Bình Dương', 'Bình Dương', 'Bình Dương', '75000', 'Vietnam', true FROM customer c WHERE c.code = 'CUST007' ON CONFLICT DO NOTHING;

INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, country, is_default)
SELECT c.id, 'Nhà máy sản xuất', c.contact_name, c.phone, '04 Nguyễn Thị Minh Khai, Sa Đéc', 'Đồng Tháp', 'Đồng Tháp', '81000', 'Vietnam', true FROM customer c WHERE c.code = 'CUST008' ON CONFLICT DO NOTHING;

INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, country, is_default)
SELECT c.id, 'Xưởng may chính', c.contact_name, c.phone, 'Lô B3 KCN Tân Bình, TP.HCM', 'TP.HCM', 'TP.HCM', '70000', 'Vietnam', true FROM customer c WHERE c.code = 'CUST009' ON CONFLICT DO NOTHING;

INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, country, is_default)
SELECT c.id, 'Nhà máy giấy', c.contact_name, c.phone, 'KCN Phú Thọ, Phú Thọ', 'Phú Thọ', 'Phú Thọ', '29000', 'Vietnam', true FROM customer c WHERE c.code = 'CUST010' ON CONFLICT DO NOTHING;

INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, country, is_default)
SELECT c.id, 'Tổ hợp THACO Chu Lai', c.contact_name, c.phone, 'KKT mở Chu Lai, Núi Thành', 'Quảng Nam', 'Quảng Nam', '51000', 'Vietnam', true FROM customer c WHERE c.code = 'CUST011' ON CONFLICT DO NOTHING;

INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, country, is_default)
SELECT c.id, 'Nhà máy xi măng', c.contact_name, c.phone, 'Lô A5, KCN Hiệp Phước, Nhà Bè', 'TP.HCM', 'TP.HCM', '70000', 'Vietnam', true FROM customer c WHERE c.code = 'CUST012' ON CONFLICT DO NOTHING;

INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, country, is_default)
SELECT c.id, 'Văn phòng & Kho HCM', c.contact_name, c.phone, '288 Lý Thường Kiệt, Q.10, TP.HCM', 'TP.HCM', 'TP.HCM', '70000', 'Vietnam', true FROM customer c WHERE c.code = 'CUST013' ON CONFLICT DO NOTHING;

INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, country, is_default)
SELECT c.id, 'Nhà máy Đà Nẵng', c.contact_name, c.phone, 'Lô D7, KCN Hòa Khánh, Liên Chiểu', 'Đà Nẵng', 'Đà Nẵng', '55000', 'Vietnam', true FROM customer c WHERE c.code = 'CUST014' ON CONFLICT DO NOTHING;

INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, country, is_default)
SELECT c.id, 'KCN Long Hậu văn phòng', c.contact_name, c.phone, 'KCN Long Hậu, Long An', 'Long An', 'Long An', '82000', 'Vietnam', true FROM customer c WHERE c.code = 'CUST015' ON CONFLICT DO NOTHING;

-- =====================================================
-- PURCHASE ORDERS - 3 tháng (30 đơn mua hàng)
-- =====================================================
DO $$
DECLARE
    v_ps_id   BIGINT; -- purchase_staff user id
    v_pm_id   BIGINT; -- purchase_manager user id
    v_wh1     BIGINT; -- WH001
    v_wh2     BIGINT; -- WH002
    v_wh3     BIGINT; -- WH003
    v_s1 BIGINT; v_s2 BIGINT; v_s3 BIGINT; v_s4 BIGINT; v_s5 BIGINT;
    v_s6 BIGINT; v_s7 BIGINT; v_s8 BIGINT; v_s9 BIGINT; v_s10 BIGINT;
    v_p1  BIGINT; v_p2  BIGINT; v_p3  BIGINT; v_p4  BIGINT; v_p5  BIGINT;
    v_p6  BIGINT; v_p7  BIGINT; v_p8  BIGINT; v_p9  BIGINT; v_p10 BIGINT;
    v_p11 BIGINT; v_p12 BIGINT; v_p13 BIGINT; v_p14 BIGINT; v_p15 BIGINT;
    v_p16 BIGINT; v_p17 BIGINT; v_p18 BIGINT; v_p19 BIGINT; v_p20 BIGINT;
    v_p21 BIGINT; v_p22 BIGINT; v_p23 BIGINT; v_p24 BIGINT; v_p25 BIGINT;
    v_po_id BIGINT;
BEGIN
    SELECT id INTO v_ps_id FROM users WHERE username='purchase_staff';
    SELECT id INTO v_pm_id FROM users WHERE username='purchase_manager';
    SELECT id INTO v_wh1 FROM warehouse WHERE code='WH001';
    SELECT id INTO v_wh2 FROM warehouse WHERE code='WH002';
    SELECT id INTO v_wh3 FROM warehouse WHERE code='WH003';
    SELECT id INTO v_s1  FROM supplier WHERE code='SUP001';
    SELECT id INTO v_s2  FROM supplier WHERE code='SUP002';
    SELECT id INTO v_s3  FROM supplier WHERE code='SUP003';
    SELECT id INTO v_s4  FROM supplier WHERE code='SUP004';
    SELECT id INTO v_s5  FROM supplier WHERE code='SUP005';
    SELECT id INTO v_s6  FROM supplier WHERE code='SUP006';
    SELECT id INTO v_s7  FROM supplier WHERE code='SUP007';
    SELECT id INTO v_s8  FROM supplier WHERE code='SUP008';
    SELECT id INTO v_s9  FROM supplier WHERE code='SUP009';
    SELECT id INTO v_s10 FROM supplier WHERE code='SUP010';
    SELECT id INTO v_p1  FROM product WHERE code='PRD001';
    SELECT id INTO v_p2  FROM product WHERE code='PRD002';
    SELECT id INTO v_p3  FROM product WHERE code='PRD003';
    SELECT id INTO v_p4  FROM product WHERE code='PRD004';
    SELECT id INTO v_p5  FROM product WHERE code='PRD005';
    SELECT id INTO v_p6  FROM product WHERE code='PRD006';
    SELECT id INTO v_p7  FROM product WHERE code='PRD007';
    SELECT id INTO v_p8  FROM product WHERE code='PRD008';
    SELECT id INTO v_p9  FROM product WHERE code='PRD009';
    SELECT id INTO v_p10 FROM product WHERE code='PRD010';
    SELECT id INTO v_p11 FROM product WHERE code='PRD011';
    SELECT id INTO v_p12 FROM product WHERE code='PRD012';
    SELECT id INTO v_p13 FROM product WHERE code='PRD013';
    SELECT id INTO v_p14 FROM product WHERE code='PRD014';
    SELECT id INTO v_p15 FROM product WHERE code='PRD015';
    SELECT id INTO v_p16 FROM product WHERE code='PRD016';
    SELECT id INTO v_p17 FROM product WHERE code='PRD017';
    SELECT id INTO v_p18 FROM product WHERE code='PRD018';
    SELECT id INTO v_p19 FROM product WHERE code='PRD019';
    SELECT id INTO v_p20 FROM product WHERE code='PRD020';
    SELECT id INTO v_p21 FROM product WHERE code='PRD021';
    SELECT id INTO v_p22 FROM product WHERE code='PRD022';
    SELECT id INTO v_p23 FROM product WHERE code='PRD023';
    SELECT id INTO v_p24 FROM product WHERE code='PRD024';
    SELECT id INTO v_p25 FROM product WHERE code='PRD025';

    -- == THÁNG 1/2026 ==
    -- PO001: Mua linh kiện điện tử lô lớn - COMPLETED
    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-001', 'Mua linh kiện Q1/2026 - Lô 1', 'ORDER_COMPLETED',
        2500000, 158250000, '2026-01-15', '2026-01-03', '2026-01-04 09:00:00', '2026-01-15 16:30:00',
        v_ps_id, v_pm_id, v_s1, v_wh1, 'Ưu tiên giao hàng đúng hạn')
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p1, 'Cái', 5, 5, 8500000, 8500000, 42500000, 4250000, 46750000),
           (v_po_id, v_p2, 'Cái', 50, 50, 450000, 450000, 22500000, 2250000, 24750000),
           (v_po_id, v_p23,'Cái', 10, 10, 1200000, 1200000, 12000000, 1200000, 13200000),
           (v_po_id, v_p24,'Cái', 30, 30, 380000, 380000, 11400000, 1140000, 12540000),
           (v_po_id, v_p3, 'Cái', 2, 2, 12000000, 12000000, 24000000, 2400000, 26400000);

    -- PO002: Mua vật liệu xây dựng - COMPLETED
    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-002', 'Mua VLXD tháng 1/2026', 'ORDER_COMPLETED',
        5000000, 215000000, '2026-01-20', '2026-01-05', '2026-01-06 10:30:00', '2026-01-20 14:00:00',
        v_ps_id, v_pm_id, v_s2, v_wh1, 'Giao theo 2 chuyến')
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p4, 'Bao', 1000, 1000, 95000, 95000, 95000000, 9500000, 104500000),
           (v_po_id, v_p5, 'Tấm', 500, 500, 180000, 180000, 90000000, 9000000, 99000000);

    -- PO003: Mua thiết bị công nghiệp - COMPLETED
    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-003', 'Mua thiết bị bơm & van T1/2026', 'ORDER_COMPLETED',
        3000000, 180500000, '2026-01-25', '2026-01-07', '2026-01-08 08:00:00', '2026-01-25 11:00:00',
        v_ps_id, v_pm_id, v_s3, v_wh2, 'Hàng nhập khẩu, kiểm tra kỹ trước nhận')
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p6, 'Cái', 3, 3, 45000000, 45000000, 135000000, 13500000, 148500000),
           (v_po_id, v_p7, 'Cái', 80, 80, 320000, 320000, 25600000, 2560000, 28160000),
           (v_po_id, v_p25,'Cái', 20, 20, 320000, 320000, 6400000, 640000, 7040000);

    -- PO004: Mua hóa chất tháng 1 - COMPLETED
    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-004', 'Mua hóa chất công nghiệp T1/2026', 'ORDER_COMPLETED',
        1500000, 95700000, '2026-01-28', '2026-01-08', '2026-01-09 09:00:00', '2026-01-28 15:00:00',
        v_ps_id, v_pm_id, v_s4, v_wh1, 'Lưu kho hóa chất chuyên dụng')
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p8, 'Bao', 150, 150, 280000, 280000, 42000000, 4200000, 46200000),
           (v_po_id, v_p9, 'Can', 100, 100, 350000, 350000, 35000000, 3500000, 38500000);

    -- PO005: Mua bao bì nhựa - COMPLETED
    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-005', 'Mua bao bì nhựa tháng 1/2026', 'ORDER_COMPLETED',
        2000000, 182050000, '2026-01-30', '2026-01-10', '2026-01-11 10:00:00', '2026-01-30 16:00:00',
        v_ps_id, v_pm_id, v_s5, v_wh2, NULL)
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p10, 'Cái', 200, 200, 650000, 650000, 130000000, 13000000, 143000000),
           (v_po_id, v_p11, 'Cái', 100, 100, 1200000, 1200000, 120000000, 12000000, 132000000);

    -- PO006: Mua vật liệu thép - COMPLETED
    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-006', 'Mua thép kết cấu T1/2026', 'ORDER_COMPLETED',
        4000000, 297000000, '2026-01-31', '2026-01-12', '2026-01-13 08:30:00', '2026-01-31 17:00:00',
        v_ps_id, v_pm_id, v_s6, v_wh1, 'Thép nhập từ Formosa')
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p12, 'Cây', 500, 500, 280000, 280000, 140000000, 14000000, 154000000),
           (v_po_id, v_p13, 'Tấm', 60, 60, 1850000, 1850000, 111000000, 11100000, 122100000);

    -- PO007: Mua inox - COMPLETED
    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-007', 'Mua inox 304 tháng 1/2026', 'ORDER_COMPLETED',
        2500000, 158950000, '2026-02-05', '2026-01-15', '2026-01-16 09:00:00', '2026-02-05 14:30:00',
        v_ps_id, v_pm_id, v_s7, v_wh1, NULL)
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p14, 'Cây', 80, 80, 980000, 980000, 78400000, 7840000, 86240000),
           (v_po_id, v_p15, 'Tấm', 30, 30, 2200000, 2200000, 66000000, 6600000, 72600000);

    -- PO008: Mua cáp điện - COMPLETED
    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-008', 'Mua thiết bị điện T1/2026', 'ORDER_COMPLETED',
        1800000, 125100000, '2026-02-08', '2026-01-18', '2026-01-19 10:00:00', '2026-02-08 16:00:00',
        v_ps_id, v_pm_id, v_s8, v_wh3, 'Giao kho Hà Nội')
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p16, 'Cuộn', 4, 4, 18500000, 18500000, 74000000, 7400000, 81400000),
           (v_po_id, v_p17, 'Cái', 40, 40, 850000, 850000, 34000000, 3400000, 37400000);

    -- PO009: Mua cao su kỹ thuật - COMPLETED
    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-009', 'Mua sản phẩm cao su T1/2026', 'ORDER_COMPLETED',
        800000, 45650000, '2026-02-10', '2026-01-20', '2026-01-21 08:00:00', '2026-02-10 11:00:00',
        v_ps_id, v_pm_id, v_s9, v_wh2, NULL)
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p18, 'Cái', 200, 200, 95000, 95000, 19000000, 1900000, 20900000),
           (v_po_id, v_p19, 'Cái', 500, 500, 45000, 45000, 22500000, 2250000, 24750000);

    -- PO010: Mua linh phụ kiện cơ khí - COMPLETED
    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-010', 'Mua linh phụ kiện cơ khí T1/2026', 'ORDER_COMPLETED',
        1200000, 121200000, '2026-02-12', '2026-01-22', '2026-01-23 09:30:00', '2026-02-12 15:00:00',
        v_ps_id, v_pm_id, v_s10, v_wh1, NULL)
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p20, 'Cái', 300, 300, 85000, 85000, 25500000, 2550000, 28050000),
           (v_po_id, v_p21, 'Cái', 10, 10, 4500000, 4500000, 45000000, 4500000, 49500000),
           (v_po_id, v_p22, 'Cái', 100, 100, 180000, 180000, 18000000, 1800000, 19800000);

    -- == THÁNG 2/2026 ==
    -- PO011 đến PO020

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-011', 'Mua linh kiện điện tử T2/2026 - Lô 1', 'ORDER_COMPLETED',
        3000000, 210650000, '2026-02-18', '2026-02-03', '2026-02-04 09:00:00', '2026-02-18 16:00:00',
        v_ps_id, v_pm_id, v_s1, v_wh1, NULL)
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p1, 'Cái', 8, 8, 8500000, 8500000, 68000000, 6800000, 74800000),
           (v_po_id, v_p2, 'Cái', 80, 80, 450000, 450000, 36000000, 3600000, 39600000),
           (v_po_id, v_p3, 'Cái', 3, 3, 12000000, 12000000, 36000000, 3600000, 39600000),
           (v_po_id, v_p23,'Cái', 15, 15, 1200000, 1200000, 18000000, 1800000, 19800000);

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-012', 'Mua thép tháng 2/2026', 'ORDER_COMPLETED',
        5000000, 396000000, '2026-02-22', '2026-02-05', '2026-02-06 10:00:00', '2026-02-22 15:30:00',
        v_ps_id, v_pm_id, v_s6, v_wh1, 'Lô thép lớn cho dự án mở rộng')
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p12, 'Cây', 700, 700, 280000, 280000, 196000000, 19600000, 215600000),
           (v_po_id, v_p13, 'Tấm', 80, 80, 1850000, 1850000, 148000000, 14800000, 162800000);

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-013', 'Mua hóa chất T2/2026', 'ORDER_COMPLETED',
        2000000, 143000000, '2026-02-25', '2026-02-07', '2026-02-08 08:30:00', '2026-02-25 14:00:00',
        v_ps_id, v_pm_id, v_s4, v_wh1, NULL)
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p8, 'Bao', 200, 200, 280000, 280000, 56000000, 5600000, 61600000),
           (v_po_id, v_p9, 'Can', 150, 150, 350000, 350000, 52500000, 5250000, 57750000);

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-014', 'Mua VLXD T2/2026', 'ORDER_COMPLETED',
        4500000, 279400000, '2026-02-26', '2026-02-10', '2026-02-11 09:00:00', '2026-02-26 16:30:00',
        v_ps_id, v_pm_id, v_s2, v_wh2, NULL)
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p4, 'Bao', 1200, 1200, 95000, 95000, 114000000, 11400000, 125400000),
           (v_po_id, v_p5, 'Tấm', 600, 600, 180000, 180000, 108000000, 10800000, 118800000);

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-015', 'Mua bao bì T2/2026', 'ORDER_COMPLETED',
        2200000, 233200000, '2026-02-28', '2026-02-12', '2026-02-13 10:30:00', '2026-02-28 15:00:00',
        v_ps_id, v_pm_id, v_s5, v_wh2, NULL)
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p10, 'Cái', 250, 250, 650000, 650000, 162500000, 16250000, 178750000),
           (v_po_id, v_p11, 'Cái', 120, 120, 1200000, 1200000, 144000000, 0, 144000000);

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-016', 'Mua inox T2/2026', 'ORDER_COMPLETED',
        3000000, 187000000, '2026-03-03', '2026-02-14', '2026-02-15 09:00:00', '2026-03-03 14:00:00',
        v_ps_id, v_pm_id, v_s7, v_wh1, NULL)
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p14, 'Cây', 100, 100, 980000, 980000, 98000000, 9800000, 107800000),
           (v_po_id, v_p15, 'Tấm', 35, 35, 2200000, 2200000, 77000000, 7700000, 84700000);

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-017', 'Mua thiết bị điện T2/2026', 'ORDER_COMPLETED',
        2000000, 163800000, '2026-03-05', '2026-02-16', '2026-02-17 08:00:00', '2026-03-05 16:00:00',
        v_ps_id, v_pm_id, v_s8, v_wh3, 'Bổ sung cho kho Hà Nội')
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p16, 'Cuộn', 5, 5, 18500000, 18500000, 92500000, 9250000, 101750000),
           (v_po_id, v_p17, 'Cái', 60, 60, 850000, 850000, 51000000, 5100000, 56100000);

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-018', 'Mua linh phụ kiện cơ khí T2/2026', 'ORDER_COMPLETED',
        1500000, 165900000, '2026-03-06', '2026-02-18', '2026-02-19 09:30:00', '2026-03-06 15:00:00',
        v_ps_id, v_pm_id, v_s10, v_wh1, NULL)
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p20, 'Cái', 500, 500, 85000, 85000, 42500000, 4250000, 46750000),
           (v_po_id, v_p21, 'Cái', 15, 15, 4500000, 4500000, 67500000, 6750000, 74250000),
           (v_po_id, v_p22, 'Cái', 150, 150, 180000, 180000, 27000000, 2700000, 29700000);

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-019', 'Mua cao su T2/2026', 'ORDER_COMPLETED',
        1000000, 60500000, '2026-03-07', '2026-02-20', '2026-02-21 08:30:00', '2026-03-07 11:00:00',
        v_ps_id, v_pm_id, v_s9, v_wh2, NULL)
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p18, 'Cái', 300, 300, 95000, 95000, 28500000, 2850000, 31350000),
           (v_po_id, v_p19, 'Cái', 600, 600, 45000, 45000, 27000000, 2700000, 29700000);

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-020', 'Mua thiết bị bơm T2/2026', 'ORDER_COMPLETED',
        3500000, 220000000, '2026-03-10', '2026-02-22', '2026-02-23 10:00:00', '2026-03-10 16:00:00',
        v_ps_id, v_pm_id, v_s3, v_wh2, 'Dự án mở rộng nhà máy Bình Dương')
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p6, 'Cái', 4, 4, 45000000, 45000000, 180000000, 18000000, 198000000),
           (v_po_id, v_p7, 'Cái', 50, 50, 320000, 320000, 16000000, 1600000, 17600000);

    -- == THÁNG 3/2026 ==
    -- PO021 đến PO030

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-021', 'Mua linh kiện điện tử T3/2026', 'ORDER_COMPLETED',
        2800000, 196350000, '2026-03-15', '2026-03-03', '2026-03-04 09:00:00', '2026-03-15 16:00:00',
        v_ps_id, v_pm_id, v_s1, v_wh1, NULL)
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p1, 'Cái', 10, 10, 8500000, 8500000, 85000000, 8500000, 93500000),
           (v_po_id, v_p2, 'Cái', 100, 100, 450000, 450000, 45000000, 4500000, 49500000),
           (v_po_id, v_p24,'Cái', 50, 50, 380000, 380000, 19000000, 1900000, 20900000);

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-022', 'Mua thép T3/2026 - Lô lớn', 'ORDER_APPROVED',
        6000000, 506000000, '2026-03-28', '2026-03-05', '2026-03-06 10:00:00', NULL,
        v_ps_id, v_pm_id, v_s6, v_wh1, 'Cần hàng gấp cho dự án')
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p12, 'Cây', 1000, 0, 280000, 280000, 280000000, 28000000, 308000000),
           (v_po_id, v_p13, 'Tấm', 100, 0, 1850000, 1850000, 185000000, 18500000, 203500000);

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-023', 'Mua hóa chất T3/2026', 'ORDER_APPROVED',
        2500000, 165000000, '2026-03-30', '2026-03-07', '2026-03-08 09:00:00', NULL,
        v_ps_id, v_pm_id, v_s4, v_wh1, NULL)
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p8, 'Bao', 250, 0, 280000, 280000, 70000000, 7000000, 77000000),
           (v_po_id, v_p9, 'Can', 200, 0, 350000, 350000, 70000000, 7000000, 77000000);

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-024', 'Mua inox T3/2026', 'ORDER_APPROVED',
        3200000, 220000000, '2026-03-31', '2026-03-08', '2026-03-09 10:00:00', NULL,
        v_ps_id, v_pm_id, v_s7, v_wh1, NULL)
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p14, 'Cây', 120, 0, 980000, 980000, 117600000, 11760000, 129360000),
           (v_po_id, v_p15, 'Tấm', 40, 0, 2200000, 2200000, 88000000, 8800000, 96800000);

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-025', 'Mua VLXD T3/2026', 'ORDER_APPROVED',
        5000000, 330000000, '2026-04-02', '2026-03-10', '2026-03-11 09:00:00', NULL,
        v_ps_id, v_pm_id, v_s2, v_wh2, 'Cung cấp cho dự án nhà ở xã hội')
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p4, 'Bao', 1500, 0, 95000, 95000, 142500000, 14250000, 156750000),
           (v_po_id, v_p5, 'Tấm', 800, 0, 180000, 180000, 144000000, 14400000, 158400000);

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-026', 'Mua bao bì T3/2026', 'ORDER_APPROVED',
        2500000, 286000000, '2026-04-03', '2026-03-11', '2026-03-12 10:00:00', NULL,
        v_ps_id, v_pm_id, v_s5, v_wh2, NULL)
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p10, 'Cái', 300, 0, 650000, 650000, 195000000, 19500000, 214500000),
           (v_po_id, v_p11, 'Cái', 150, 0, 1200000, 1200000, 180000000, 0, 180000000);

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-027', 'Mua thiết bị điện T3/2026', 'ORDER_OPEN',
        2000000, 190000000, '2026-04-05', '2026-03-13', NULL, NULL,
        v_ps_id, NULL, v_s8, v_wh1, 'Đang chờ duyệt')
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p16, 'Cuộn', 6, 0, 18500000, 18500000, 111000000, 11100000, 122100000),
           (v_po_id, v_p17, 'Cái', 70, 0, 850000, 850000, 59500000, 5950000, 65450000);

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-028', 'Mua cao su T3/2026', 'ORDER_OPEN',
        1200000, 79200000, '2026-04-06', '2026-03-14', NULL, NULL,
        v_ps_id, NULL, v_s9, v_wh2, NULL)
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p18, 'Cái', 400, 0, 95000, 95000, 38000000, 3800000, 41800000),
           (v_po_id, v_p19, 'Cái', 800, 0, 45000, 45000, 36000000, 3600000, 39600000);

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-029', 'Mua linh phụ kiện T3/2026', 'ORDER_OPEN',
        1800000, 195000000, '2026-04-08', '2026-03-15', NULL, NULL,
        v_ps_id, NULL, v_s10, v_wh1, NULL)
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p20, 'Cái', 600, 0, 85000, 85000, 51000000, 5100000, 56100000),
           (v_po_id, v_p21, 'Cái', 20, 0, 4500000, 4500000, 90000000, 9000000, 99000000),
           (v_po_id, v_p22, 'Cái', 200, 0, 180000, 180000, 36000000, 3600000, 39600000);

    INSERT INTO purchase_order (code, order_name, status, shipping_cost, total_amount,
        delivery_date, created_date, approved_date, completed_date,
        created_by, approved_by, supplier_id, warehouse_id, notes)
    VALUES ('PO-2026-030', 'Mua thiết bị bơm T3/2026', 'ORDER_OPEN',
        4000000, 270000000, '2026-04-10', '2026-03-16', NULL, NULL,
        v_ps_id, NULL, v_s3, v_wh1, 'Yêu cầu báo giá lại nếu cần')
    RETURNING id INTO v_po_id;
    INSERT INTO purchase_order_item (purchase_order_id, product_id, unit, quantity, received_quantity, unit_price, cost_before_tax, amount_before_tax, tax_amount, total_amount)
    VALUES (v_po_id, v_p6, 'Cái', 5, 0, 45000000, 45000000, 225000000, 22500000, 247500000),
           (v_po_id, v_p7, 'Cái', 60, 0, 320000, 320000, 19200000, 1920000, 21120000);

END $$;

-- =====================================================
-- GOODS RECEIPTS (phiếu nhập kho - cho các PO đã COMPLETED)
-- =====================================================
DO $$
DECLARE
    v_ws_id BIGINT; -- warehouse_staff
    v_pm_id BIGINT; -- purchase_manager
    v_gr_id BIGINT;
    v_po_id BIGINT;
    v_poi_id BIGINT;
    v_pid   BIGINT;
BEGIN
    SELECT id INTO v_ws_id FROM users WHERE username='warehouse_staff';
    SELECT id INTO v_pm_id FROM users WHERE username='purchase_manager';

    -- GR cho PO-2026-001
    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-001';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-001', 'CONFIRMED', '2026-01-15', '2026-01-15 16:30:00', 'DN-SUP001-2601', poi_sum.total, 'Nhận đủ hàng, kiểm tra OK',
           v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po,
         (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id
    RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2601-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    -- GR cho PO-2026-002
    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-002';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-002', 'CONFIRMED', '2026-01-20', '2026-01-20 14:00:00', 'DN-SUP002-2601', poi_sum.total, 'Giao 2 chuyến, nhận đủ',
           v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2602-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    -- GR cho PO-2026-003
    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-003';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-003', 'CONFIRMED', '2026-01-25', '2026-01-25 11:00:00', 'DN-SUP003-2601', poi_sum.total, 'Kiểm tra chất lượng trước khi nhận',
           v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2603-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    -- GR cho PO-2026-004
    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-004';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-004', 'CONFIRMED', '2026-01-28', '2026-01-28 15:00:00', 'DN-SUP004-2601', poi_sum.total, 'Lưu kho hóa chất đúng quy định',
           v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2604-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    -- GR cho PO-2026-005
    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-005';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-005', 'CONFIRMED', '2026-01-30', '2026-01-30 16:00:00', 'DN-SUP005-2601', poi_sum.total, NULL,
           v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2605-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    -- GR cho PO-2026-006
    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-006';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-006', 'CONFIRMED', '2026-01-31', '2026-01-31 17:00:00', 'DN-SUP006-2601', poi_sum.total, 'Thép nhập từ Formosa, chứng nhận CQ đầy đủ',
           v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2606-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    -- GR cho PO-2026-007
    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-007';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-007', 'CONFIRMED', '2026-02-05', '2026-02-05 14:30:00', 'DN-SUP007-2602', poi_sum.total, NULL,
           v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2607-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    -- GR cho PO-2026-008
    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-008';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-008', 'CONFIRMED', '2026-02-08', '2026-02-08 16:00:00', 'DN-SUP008-2602', poi_sum.total, 'Giao kho Hà Nội',
           v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2608-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    -- GR cho PO-2026-009
    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-009';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-009', 'CONFIRMED', '2026-02-10', '2026-02-10 11:00:00', 'DN-SUP009-2602', poi_sum.total, NULL,
           v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2609-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    -- GR cho PO-2026-010
    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-010';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-010', 'CONFIRMED', '2026-02-12', '2026-02-12 15:00:00', 'DN-SUP010-2602', poi_sum.total, NULL,
           v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2610-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    -- GR cho PO-2026-011 đến PO-2026-021 (tháng 2-3)
    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-011';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-011', 'CONFIRMED', '2026-02-18', '2026-02-18 16:00:00', 'DN-SUP001-2602', poi_sum.total, NULL, v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2611-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-012';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-012', 'CONFIRMED', '2026-02-22', '2026-02-22 15:30:00', 'DN-SUP006-2602', poi_sum.total, NULL, v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2612-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-013';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-013', 'CONFIRMED', '2026-02-25', '2026-02-25 14:00:00', 'DN-SUP004-2602', poi_sum.total, NULL, v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2613-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-014';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-014', 'CONFIRMED', '2026-02-26', '2026-02-26 16:30:00', 'DN-SUP002-2602', poi_sum.total, NULL, v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2614-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-015';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-015', 'CONFIRMED', '2026-02-28', '2026-02-28 15:00:00', 'DN-SUP005-2602', poi_sum.total, NULL, v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2615-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-016';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-016', 'CONFIRMED', '2026-03-03', '2026-03-03 14:00:00', 'DN-SUP007-2603', poi_sum.total, NULL, v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2616-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-017';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-017', 'CONFIRMED', '2026-03-05', '2026-03-05 16:00:00', 'DN-SUP008-2603', poi_sum.total, NULL, v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2617-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-018';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-018', 'CONFIRMED', '2026-03-06', '2026-03-06 15:00:00', 'DN-SUP010-2603', poi_sum.total, NULL, v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2618-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-019';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-019', 'CONFIRMED', '2026-03-07', '2026-03-07 11:00:00', 'DN-SUP009-2603', poi_sum.total, NULL, v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2619-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-020';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-020', 'CONFIRMED', '2026-03-10', '2026-03-10 16:00:00', 'DN-SUP003-2603', poi_sum.total, NULL, v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2620-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

    SELECT id INTO v_po_id FROM purchase_order WHERE code='PO-2026-021';
    INSERT INTO goods_receipt (code, status, receipt_date, confirmed_date, delivery_note_number, total_amount, notes, created_by, confirmed_by, purchase_order_id, warehouse_id)
    SELECT 'GR-2026-021', 'CONFIRMED', '2026-03-15', '2026-03-15 16:00:00', 'DN-SUP001-2603', poi_sum.total, NULL, v_ws_id, v_pm_id, v_po_id, po.warehouse_id
    FROM purchase_order po, (SELECT SUM(total_amount) total FROM purchase_order_item WHERE purchase_order_id=v_po_id) poi_sum
    WHERE po.id=v_po_id RETURNING id INTO v_gr_id;
    INSERT INTO goods_receipt_item (goods_receipt_id, purchase_order_item_id, product_id, ordered_quantity, received_quantity, accepted_quantity, rejected_quantity, unit_price, total_amount, unit, batch_number)
    SELECT v_gr_id, poi.id, poi.product_id, poi.quantity, poi.quantity, poi.quantity, 0, poi.unit_price, poi.total_amount, poi.unit, 'BATCH-2621-'||poi.product_id
    FROM purchase_order_item poi WHERE poi.purchase_order_id=v_po_id;

END $$;

-- =====================================================
-- INVENTORY (tồn kho sau khi nhập)
-- =====================================================
DO $$
DECLARE
    v_wh1 BIGINT; v_wh2 BIGINT; v_wh3 BIGINT;
BEGIN
    SELECT id INTO v_wh1 FROM warehouse WHERE code='WH001';
    SELECT id INTO v_wh2 FROM warehouse WHERE code='WH002';
    SELECT id INTO v_wh3 FROM warehouse WHERE code='WH003';

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh1, 18, 5, 13, 3, 10, 8500000, '2026-03-15', NOW()
    FROM product p WHERE p.code='PRD001' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=18, quantity_available=13, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh1, 180, 30, 150, 50, 100, 450000, '2026-03-15', NOW()
    FROM product p WHERE p.code='PRD002' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=180, quantity_available=150, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh1, 5, 2, 3, 2, 5, 12000000, '2026-03-15', NOW()
    FROM product p WHERE p.code='PRD003' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=5, quantity_available=3, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh1, 2200, 300, 1900, 500, 1000, 95000, '2026-02-26', NOW()
    FROM product p WHERE p.code='PRD004' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=2200, quantity_available=1900, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh1, 1000, 200, 800, 200, 500, 180000, '2026-02-26', NOW()
    FROM product p WHERE p.code='PRD005' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=1000, quantity_available=800, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh2, 6, 2, 4, 2, 5, 45000000, '2026-03-10', NOW()
    FROM product p WHERE p.code='PRD006' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=6, quantity_available=4, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh2, 110, 20, 90, 30, 100, 320000, '2026-03-10', NOW()
    FROM product p WHERE p.code='PRD007' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=110, quantity_available=90, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh1, 380, 50, 330, 100, 200, 280000, '2026-02-25', NOW()
    FROM product p WHERE p.code='PRD008' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=380, quantity_available=330, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh1, 220, 30, 190, 80, 150, 350000, '2026-02-25', NOW()
    FROM product p WHERE p.code='PRD009' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=220, quantity_available=190, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh2, 420, 80, 340, 100, 200, 650000, '2026-02-28', NOW()
    FROM product p WHERE p.code='PRD010' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=420, quantity_available=340, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh2, 190, 40, 150, 50, 100, 1200000, '2026-02-28', NOW()
    FROM product p WHERE p.code='PRD011' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=190, quantity_available=150, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh1, 1100, 200, 900, 300, 500, 280000, '2026-02-22', NOW()
    FROM product p WHERE p.code='PRD012' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=1100, quantity_available=900, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh1, 110, 20, 90, 30, 80, 1850000, '2026-02-22', NOW()
    FROM product p WHERE p.code='PRD013' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=110, quantity_available=90, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh1, 160, 30, 130, 40, 100, 980000, '2026-03-03', NOW()
    FROM product p WHERE p.code='PRD014' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=160, quantity_available=130, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh1, 58, 10, 48, 15, 30, 2200000, '2026-03-03', NOW()
    FROM product p WHERE p.code='PRD015' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=58, quantity_available=48, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh3, 8, 2, 6, 3, 5, 18500000, '2026-03-05', NOW()
    FROM product p WHERE p.code='PRD016' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=8, quantity_available=6, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh3, 85, 15, 70, 20, 50, 850000, '2026-03-05', NOW()
    FROM product p WHERE p.code='PRD017' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=85, quantity_available=70, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh2, 450, 80, 370, 100, 200, 95000, '2026-03-07', NOW()
    FROM product p WHERE p.code='PRD018' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=450, quantity_available=370, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh2, 1000, 150, 850, 200, 500, 45000, '2026-03-07', NOW()
    FROM product p WHERE p.code='PRD019' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=1000, quantity_available=850, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh1, 760, 100, 660, 200, 500, 85000, '2026-03-06', NOW()
    FROM product p WHERE p.code='PRD020' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=760, quantity_available=660, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh1, 24, 5, 19, 5, 15, 4500000, '2026-03-06', NOW()
    FROM product p WHERE p.code='PRD021' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=24, quantity_available=19, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh1, 230, 40, 190, 50, 150, 180000, '2026-03-06', NOW()
    FROM product p WHERE p.code='PRD022' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=230, quantity_available=190, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh1, 25, 5, 20, 5, 20, 1200000, '2026-03-15', NOW()
    FROM product p WHERE p.code='PRD023' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=25, quantity_available=20, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh1, 70, 10, 60, 20, 50, 380000, '2026-03-15', NOW()
    FROM product p WHERE p.code='PRD024' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=70, quantity_available=60, updated_at=NOW();

    INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_level, reorder_quantity, average_cost, last_received_date, updated_at)
    SELECT p.id, v_wh2, 18, 3, 15, 5, 20, 320000, '2026-01-25', NOW()
    FROM product p WHERE p.code='PRD025' ON CONFLICT (product_id, warehouse_id) DO UPDATE SET quantity_on_hand=18, quantity_available=15, updated_at=NOW();

END $$;

-- =====================================================
-- SALES ORDERS (30 đơn bán hàng 3 tháng)
-- =====================================================
DO $$
DECLARE
    v_ss_id BIGINT; v_sm_id BIGINT;
    v_wh1 BIGINT; v_wh2 BIGINT;
    v_c1 BIGINT; v_c2 BIGINT; v_c3 BIGINT; v_c4 BIGINT; v_c5 BIGINT;
    v_c6 BIGINT; v_c7 BIGINT; v_c8 BIGINT; v_c9 BIGINT; v_c10 BIGINT;
    v_c11 BIGINT; v_c12 BIGINT; v_c13 BIGINT; v_c14 BIGINT; v_c15 BIGINT;
    v_da1 BIGINT; v_da2 BIGINT; v_da3 BIGINT; v_da4 BIGINT; v_da5 BIGINT;
    v_da6 BIGINT; v_da7 BIGINT; v_da8 BIGINT; v_da9 BIGINT; v_da10 BIGINT;
    v_da11 BIGINT; v_da12 BIGINT; v_da13 BIGINT; v_da14 BIGINT; v_da15 BIGINT;
    v_p1 BIGINT; v_p2 BIGINT; v_p3 BIGINT; v_p4 BIGINT; v_p5 BIGINT;
    v_p6 BIGINT; v_p7 BIGINT; v_p8 BIGINT; v_p9 BIGINT; v_p10 BIGINT;
    v_p12 BIGINT; v_p13 BIGINT; v_p14 BIGINT; v_p16 BIGINT; v_p17 BIGINT;
    v_p18 BIGINT; v_p20 BIGINT; v_p23 BIGINT; v_p24 BIGINT; v_p25 BIGINT;
    v_so_id BIGINT;
BEGIN
    SELECT id INTO v_ss_id FROM users WHERE username='sales_staff';
    SELECT id INTO v_sm_id FROM users WHERE username='sales_manager';
    SELECT id INTO v_wh1 FROM warehouse WHERE code='WH001';
    SELECT id INTO v_wh2 FROM warehouse WHERE code='WH002';
    SELECT id INTO v_c1  FROM customer WHERE code='CUST001';
    SELECT id INTO v_c2  FROM customer WHERE code='CUST002';
    SELECT id INTO v_c3  FROM customer WHERE code='CUST003';
    SELECT id INTO v_c4  FROM customer WHERE code='CUST004';
    SELECT id INTO v_c5  FROM customer WHERE code='CUST005';
    SELECT id INTO v_c6  FROM customer WHERE code='CUST006';
    SELECT id INTO v_c7  FROM customer WHERE code='CUST007';
    SELECT id INTO v_c8  FROM customer WHERE code='CUST008';
    SELECT id INTO v_c9  FROM customer WHERE code='CUST009';
    SELECT id INTO v_c10 FROM customer WHERE code='CUST010';
    SELECT id INTO v_c11 FROM customer WHERE code='CUST011';
    SELECT id INTO v_c12 FROM customer WHERE code='CUST012';
    SELECT id INTO v_c13 FROM customer WHERE code='CUST013';
    SELECT id INTO v_c14 FROM customer WHERE code='CUST014';
    SELECT id INTO v_c15 FROM customer WHERE code='CUST015';
    SELECT id INTO v_da1  FROM delivery_address WHERE customer_id=v_c1  AND is_default=true LIMIT 1;
    SELECT id INTO v_da2  FROM delivery_address WHERE customer_id=v_c2  AND is_default=true LIMIT 1;
    SELECT id INTO v_da3  FROM delivery_address WHERE customer_id=v_c3  AND is_default=true LIMIT 1;
    SELECT id INTO v_da4  FROM delivery_address WHERE customer_id=v_c4  AND is_default=true LIMIT 1;
    SELECT id INTO v_da5  FROM delivery_address WHERE customer_id=v_c5  AND is_default=true LIMIT 1;
    SELECT id INTO v_da6  FROM delivery_address WHERE customer_id=v_c6  AND is_default=true LIMIT 1;
    SELECT id INTO v_da7  FROM delivery_address WHERE customer_id=v_c7  AND is_default=true LIMIT 1;
    SELECT id INTO v_da8  FROM delivery_address WHERE customer_id=v_c8  AND is_default=true LIMIT 1;
    SELECT id INTO v_da9  FROM delivery_address WHERE customer_id=v_c9  AND is_default=true LIMIT 1;
    SELECT id INTO v_da10 FROM delivery_address WHERE customer_id=v_c10 AND is_default=true LIMIT 1;
    SELECT id INTO v_da11 FROM delivery_address WHERE customer_id=v_c11 AND is_default=true LIMIT 1;
    SELECT id INTO v_da12 FROM delivery_address WHERE customer_id=v_c12 AND is_default=true LIMIT 1;
    SELECT id INTO v_da13 FROM delivery_address WHERE customer_id=v_c13 AND is_default=true LIMIT 1;
    SELECT id INTO v_da14 FROM delivery_address WHERE customer_id=v_c14 AND is_default=true LIMIT 1;
    SELECT id INTO v_da15 FROM delivery_address WHERE customer_id=v_c15 AND is_default=true LIMIT 1;
    SELECT id INTO v_p1  FROM product WHERE code='PRD001';
    SELECT id INTO v_p2  FROM product WHERE code='PRD002';
    SELECT id INTO v_p3  FROM product WHERE code='PRD003';
    SELECT id INTO v_p4  FROM product WHERE code='PRD004';
    SELECT id INTO v_p5  FROM product WHERE code='PRD005';
    SELECT id INTO v_p6  FROM product WHERE code='PRD006';
    SELECT id INTO v_p7  FROM product WHERE code='PRD007';
    SELECT id INTO v_p8  FROM product WHERE code='PRD008';
    SELECT id INTO v_p9  FROM product WHERE code='PRD009';
    SELECT id INTO v_p10 FROM product WHERE code='PRD010';
    SELECT id INTO v_p12 FROM product WHERE code='PRD012';
    SELECT id INTO v_p13 FROM product WHERE code='PRD013';
    SELECT id INTO v_p14 FROM product WHERE code='PRD014';
    SELECT id INTO v_p16 FROM product WHERE code='PRD016';
    SELECT id INTO v_p17 FROM product WHERE code='PRD017';
    SELECT id INTO v_p18 FROM product WHERE code='PRD018';
    SELECT id INTO v_p20 FROM product WHERE code='PRD020';
    SELECT id INTO v_p23 FROM product WHERE code='PRD023';
    SELECT id INTO v_p24 FROM product WHERE code='PRD024';
    SELECT id INTO v_p25 FROM product WHERE code='PRD025';

    -- == THÁNG 1/2026 - 10 đơn ==

    -- SO001: CUST001 - Máy CNC mua linh kiện điện tử - COMPLETED
    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-001','Cung cấp linh kiện Q1 lần 1','ORDER_COMPLETED','2026-01-05','2026-01-20','2026-01-06 10:00:00','2026-01-22 15:00:00', 85500000, 8550000, 94050000,'PAID', v_ss_id, v_sm_id, v_c1, v_da1, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p1,  'Cái', 3, 3, 9500000, 10, 28500000, 2850000, 31350000),
           (v_so_id, v_p2,  'Cái', 30, 30, 500000, 10, 15000000, 1500000, 16500000),
           (v_so_id, v_p23, 'Cái', 8, 8, 1350000, 10, 10800000, 1080000, 11880000),
           (v_so_id, v_p24, 'Cái', 20, 20, 420000, 10, 8400000, 840000, 9240000);

    -- SO002: CUST005 - Samsung - linh kiện PLC - COMPLETED
    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-002','PLC & Biến tần batch 1','ORDER_COMPLETED','2026-01-06','2026-01-22','2026-01-07 09:00:00','2026-01-23 16:00:00', 180000000, 18000000, 198000000,'PAID', v_ss_id, v_sm_id, v_c5, v_da5, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p1, 'Cái', 5, 5, 9500000, 10, 47500000, 4750000, 52250000),
           (v_so_id, v_p3, 'Cái', 3, 3, 13500000, 10, 40500000, 4050000, 44550000),
           (v_so_id, v_p2, 'Cái', 50, 50, 500000, 10, 25000000, 2500000, 27500000);

    -- SO003: CUST007 - Cơ khí chính xác - thép & inox - COMPLETED
    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-003','Vật liệu thép & inox tháng 1','ORDER_COMPLETED','2026-01-07','2026-01-25','2026-01-08 10:30:00','2026-01-26 14:00:00', 145000000, 14500000, 159500000,'PAID', v_ss_id, v_sm_id, v_c7, v_da7, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p12, 'Cây', 200, 200, 320000, 10, 64000000, 6400000, 70400000),
           (v_so_id, v_p13, 'Tấm', 25, 25, 2100000, 10, 52500000, 5250000, 57750000),
           (v_so_id, v_p14, 'Cây', 20, 20, 1100000, 10, 22000000, 2200000, 24200000);

    -- SO004: CUST004 - Thực phẩm - hóa chất & bao bì - COMPLETED
    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-004','Hóa chất & bao bì T1/2026','ORDER_COMPLETED','2026-01-08','2026-01-26','2026-01-09 09:00:00','2026-01-27 16:00:00', 92000000, 9200000, 101200000,'PAID', v_ss_id, v_sm_id, v_c4, v_da4, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p8,  'Bao', 100, 100, 310000, 10, 31000000, 3100000, 34100000),
           (v_so_id, v_p10, 'Cái', 50, 50, 720000, 10, 36000000, 3600000, 39600000),
           (v_so_id, v_p9,  'Can', 50, 50, 390000, 10, 19500000, 1950000, 21450000);

    -- SO005: CUST006 - Xử lý nước - máy bơm & van - COMPLETED
    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-005','Thiết bị bơm & van cho trạm xử lý nước','ORDER_COMPLETED','2026-01-10','2026-01-28','2026-01-11 09:00:00','2026-01-28 17:00:00', 148000000, 14800000, 162800000,'PAID', v_ss_id, v_sm_id, v_c6, v_da6, v_wh2)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p6,  'Cái', 2, 2, 50000000, 10, 100000000, 10000000, 110000000),
           (v_so_id, v_p7,  'Cái', 60, 60, 360000, 10, 21600000, 2160000, 23760000),
           (v_so_id, v_p25, 'Cái', 15, 15, 360000, 10, 5400000, 540000, 5940000);

    -- SO006: CUST003 - Xây dựng - xi măng gạch - COMPLETED
    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-006','VLXD dự án chung cư Ecopark','ORDER_COMPLETED','2026-01-11','2026-01-29','2026-01-12 10:00:00','2026-01-29 16:00:00', 165000000, 16500000, 181500000,'PAID', v_ss_id, v_sm_id, v_c3, v_da3, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p4, 'Bao', 800, 800, 105000, 10, 84000000, 8400000, 92400000),
           (v_so_id, v_p5, 'Tấm', 400, 400, 200000, 10, 80000000, 8000000, 88000000);

    -- SO007: CUST011 - THACO - thép & cao su - COMPLETED
    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-007','Vật tư cơ khí cho dây chuyền lắp ráp','ORDER_COMPLETED','2026-01-12','2026-01-30','2026-01-13 09:00:00','2026-01-30 15:00:00', 98000000, 9800000, 107800000,'PAID', v_ss_id, v_sm_id, v_c11, v_da11, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p20, 'Cái', 200, 200, 95000, 10, 19000000, 1900000, 20900000),
           (v_so_id, v_p18, 'Cái', 150, 150, 105000, 10, 15750000, 1575000, 17325000),
           (v_so_id, v_p12, 'Cây', 120, 120, 320000, 10, 38400000, 3840000, 42240000),
           (v_so_id, v_p13, 'Tấm', 10, 10, 2100000, 10, 21000000, 2100000, 23100000);

    -- SO008: CUST013 - Hòa Phát - thép lớn - COMPLETED
    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-008','Thép hộp & thép tấm tháng 1','ORDER_COMPLETED','2026-01-13','2026-01-31','2026-01-14 09:30:00','2026-01-31 17:00:00', 220000000, 22000000, 242000000,'PAID', v_ss_id, v_sm_id, v_c13, v_da13, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p12, 'Cây', 400, 400, 320000, 10, 128000000, 12800000, 140800000),
           (v_so_id, v_p13, 'Tấm', 40, 40, 2100000, 10, 84000000, 8400000, 92400000);

    -- SO009: CUST009 - Việt Tiến - dây cao su & curoa - COMPLETED
    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-009','Vật tư kỹ thuật T1/2026','ORDER_COMPLETED','2026-01-14','2026-02-01','2026-01-15 10:00:00','2026-02-02 15:00:00', 52000000, 5200000, 57200000,'PAID', v_ss_id, v_sm_id, v_c9, v_da9, v_wh2)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p18, 'Cái', 150, 150, 105000, 10, 15750000, 1575000, 17325000),
           (v_so_id, v_p20, 'Cái', 100, 100, 95000, 10, 9500000, 950000, 10450000),
           (v_so_id, v_p24, 'Cái', 30, 30, 420000, 10, 12600000, 1260000, 13860000),
           (v_so_id, v_p25, 'Cái', 10, 10, 360000, 10, 3600000, 360000, 3960000);

    -- SO010: CUST008 - Dược - thiết bị điện & đồng hồ áp - COMPLETED
    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-010','Thiết bị điện & đo lường cho nhà máy dược','ORDER_COMPLETED','2026-01-15','2026-02-03','2026-01-16 09:00:00','2026-02-04 16:00:00', 78000000, 7800000, 85800000,'PAID', v_ss_id, v_sm_id, v_c8, v_da8, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p16, 'Cuộn', 2, 2, 20500000, 10, 41000000, 4100000, 45100000),
           (v_so_id, v_p17, 'Cái', 25, 25, 950000, 10, 23750000, 2375000, 26125000),
           (v_so_id, v_p25, 'Cái', 8, 8, 360000, 10, 2880000, 288000, 3168000);

    -- == THÁNG 2/2026 - 10 đơn ==

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-011','Linh kiện điện tử T2 batch 1','ORDER_COMPLETED','2026-02-03','2026-02-20','2026-02-04 09:00:00','2026-02-21 16:00:00', 120000000, 12000000, 132000000,'PAID', v_ss_id, v_sm_id, v_c5, v_da5, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p1, 'Cái', 4, 4, 9500000, 10, 38000000, 3800000, 41800000),
           (v_so_id, v_p3, 'Cái', 2, 2, 13500000, 10, 27000000, 2700000, 29700000),
           (v_so_id, v_p23,'Cái', 10, 10, 1350000, 10, 13500000, 1350000, 14850000);

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-012','VLXD dự án T2/2026','ORDER_COMPLETED','2026-02-05','2026-02-23','2026-02-06 10:00:00','2026-02-24 14:00:00', 198000000, 19800000, 217800000,'PAID', v_ss_id, v_sm_id, v_c3, v_da3, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p4, 'Bao', 1000, 1000, 105000, 10, 105000000, 10500000, 115500000),
           (v_so_id, v_p5, 'Tấm', 500, 500, 200000, 10, 100000000, 10000000, 110000000);

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-013','Thép kết cấu T2/2026','ORDER_COMPLETED','2026-02-07','2026-02-25','2026-02-08 09:00:00','2026-02-26 15:00:00', 264000000, 26400000, 290400000,'PAID', v_ss_id, v_sm_id, v_c13, v_da13, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p12, 'Cây', 450, 450, 320000, 10, 144000000, 14400000, 158400000),
           (v_so_id, v_p13, 'Tấm', 50, 50, 2100000, 10, 105000000, 10500000, 115500000);

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-014','Thiết bị điện T2/2026 - Hà Nội','ORDER_COMPLETED','2026-02-08','2026-02-26','2026-02-09 08:30:00','2026-02-27 16:00:00', 95000000, 9500000, 104500000,'PAID', v_ss_id, v_sm_id, v_c8, v_da8, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p16, 'Cuộn', 3, 3, 20500000, 10, 61500000, 6150000, 67650000),
           (v_so_id, v_p17, 'Cái', 30, 30, 950000, 10, 28500000, 2850000, 31350000);

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-015','Hóa chất xử lý nước T2/2026','ORDER_COMPLETED','2026-02-10','2026-02-28','2026-02-11 09:00:00','2026-03-01 15:00:00', 75000000, 7500000, 82500000,'PAID', v_ss_id, v_sm_id, v_c6, v_da6, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p8, 'Bao', 120, 120, 310000, 10, 37200000, 3720000, 40920000),
           (v_so_id, v_p9, 'Can', 80, 80, 390000, 10, 31200000, 3120000, 34320000);

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-016','Inox & kim loại T2/2026','ORDER_COMPLETED','2026-02-11','2026-03-01','2026-02-12 10:00:00','2026-03-02 14:00:00', 130000000, 13000000, 143000000,'PAID', v_ss_id, v_sm_id, v_c7, v_da7, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p14, 'Cây', 60, 60, 1100000, 10, 66000000, 6600000, 72600000),
           (v_so_id, v_p2,  'Tấm', 25, 25, 2500000, 10, 62500000, 6250000, 68750000);

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-017','Bao bì & pallet T2/2026','ORDER_COMPLETED','2026-02-12','2026-03-02','2026-02-13 09:00:00','2026-03-03 16:00:00', 110000000, 11000000, 121000000,'PAID', v_ss_id, v_sm_id, v_c4, v_da4, v_wh2)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p10, 'Cái', 80, 80, 720000, 10, 57600000, 5760000, 63360000),
           (v_so_id, v_p7,  'Cái', 60, 60, 360000, 10, 21600000, 2160000, 23760000));

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-018','Linh kiện cơ khí T2/2026','ORDER_COMPLETED','2026-02-14','2026-03-04','2026-02-15 09:30:00','2026-03-05 15:00:00', 88000000, 8800000, 96800000,'PAID', v_ss_id, v_sm_id, v_c1, v_da1, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p20, 'Cái', 300, 300, 95000, 10, 28500000, 2850000, 31350000),
           (v_so_id, v_p18, 'Cái', 200, 200, 105000, 10, 21000000, 2100000, 23100000),
           (v_so_id, v_p24, 'Cái', 30, 30, 420000, 10, 12600000, 1260000, 13860000),
           (v_so_id, v_p25, 'Cái', 10, 10, 360000, 10, 3600000, 360000, 3960000);

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-019','Thiết bị bơm T2/2026','ORDER_COMPLETED','2026-02-16','2026-03-06','2026-02-17 10:00:00','2026-03-07 16:00:00', 225000000, 22500000, 247500000,'PAID', v_ss_id, v_sm_id, v_c12, v_da12, v_wh2)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p6, 'Cái', 3, 3, 50000000, 10, 150000000, 15000000, 165000000),
           (v_so_id, v_p7, 'Cái', 50, 50, 360000, 10, 18000000, 1800000, 19800000),
           (v_so_id, v_p25,'Cái', 10, 10, 360000, 10, 3600000, 360000, 3960000));

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-020','Nhựa Đà Nẵng - vật tư T2/2026','ORDER_COMPLETED','2026-02-18','2026-03-08','2026-02-19 09:00:00','2026-03-09 14:00:00', 65000000, 6500000, 71500000,'PAID', v_ss_id, v_sm_id, v_c14, v_da14, v_wh2)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p10, 'Cái', 50, 50, 720000, 10, 36000000, 3600000, 39600000),
           (v_so_id, v_p8,  'Bao', 60, 60, 310000, 10, 18600000, 1860000, 20460000));

    -- == THÁNG 3/2026 - 10 đơn ==

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-021','Samsung - PLC T3/2026','ORDER_COMPLETED','2026-03-03','2026-03-20','2026-03-04 09:00:00','2026-03-21 16:00:00', 200000000, 20000000, 220000000,'PAID', v_ss_id, v_sm_id, v_c5, v_da5, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p1, 'Cái', 6, 6, 9500000, 10, 57000000, 5700000, 62700000),
           (v_so_id, v_p3, 'Cái', 4, 4, 13500000, 10, 54000000, 5400000, 59400000),
           (v_so_id, v_p2, 'Cái', 60, 60, 500000, 10, 30000000, 3000000, 33000000);

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-022','Thép T3/2026 - Hòa Phát','ORDER_APPROVED','2026-03-05','2026-03-25','2026-03-06 10:00:00', NULL, 280000000, 28000000, 308000000,'UNPAID', v_ss_id, v_sm_id, v_c13, v_da13, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p12, 'Cây', 500, 0, 320000, 10, 160000000, 16000000, 176000000),
           (v_so_id, v_p13, 'Tấm', 60, 0, 2100000, 10, 126000000, 12600000, 138600000);

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-023','VLXD T3/2026 - Xây dựng số 1','ORDER_APPROVED','2026-03-06','2026-03-28','2026-03-07 09:00:00', NULL, 220000000, 22000000, 242000000,'UNPAID', v_ss_id, v_sm_id, v_c3, v_da3, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p4, 'Bao', 1200, 0, 105000, 10, 126000000, 12600000, 138600000),
           (v_so_id, v_p5, 'Tấm', 400, 0, 200000, 10, 80000000, 8000000, 88000000);

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-024','Thiết bị xử lý nước T3/2026','ORDER_APPROVED','2026-03-07','2026-03-28','2026-03-08 09:00:00', NULL, 178000000, 17800000, 195800000,'UNPAID', v_ss_id, v_sm_id, v_c6, v_da6, v_wh2)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p6, 'Cái', 2, 0, 50000000, 10, 100000000, 10000000, 110000000),
           (v_so_id, v_p8, 'Bao', 150, 0, 310000, 10, 46500000, 4650000, 51150000),
           (v_so_id, v_p9, 'Can', 30, 0, 390000, 10, 11700000, 1170000, 12870000));

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-025','Inox T3/2026 - Cơ khí Tân Thành','ORDER_APPROVED','2026-03-08','2026-03-28','2026-03-09 10:00:00', NULL, 155000000, 15500000, 170500000,'UNPAID', v_ss_id, v_sm_id, v_c7, v_da7, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p14, 'Cây', 80, 0, 1100000, 10, 88000000, 8800000, 96800000),
           (v_so_id, v_p7,  'Cái', 30, 0, 360000, 10, 10800000, 1080000, 11880000),
           (v_so_id, v_p20, 'Cái', 100, 0, 95000, 10, 9500000, 950000, 10450000));

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-026','Dây điện T3/2026 - Long Hậu','ORDER_APPROVED','2026-03-09','2026-03-29','2026-03-10 09:00:00', NULL, 102500000, 10250000, 112750000,'UNPAID', v_ss_id, v_sm_id, v_c15, v_da15, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p16, 'Cuộn', 3, 0, 20500000, 10, 61500000, 6150000, 67650000),
           (v_so_id, v_p17, 'Cái', 40, 0, 950000, 10, 38000000, 3800000, 41800000));

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-027','Vật tư THACO T3/2026','ORDER_APPROVED','2026-03-10','2026-03-28','2026-03-11 09:30:00', NULL, 118000000, 11800000, 129800000,'UNPAID', v_ss_id, v_sm_id, v_c11, v_da11, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p20, 'Cái', 250, 0, 95000, 10, 23750000, 2375000, 26125000),
           (v_so_id, v_p18, 'Cái', 200, 0, 105000, 10, 21000000, 2100000, 23100000),
           (v_so_id, v_p12, 'Cây', 150, 0, 320000, 10, 48000000, 4800000, 52800000));

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-028','Bao bì giấy T3/2026','ORDER_OPEN','2026-03-12','2026-04-01', NULL, NULL, 95000000, 9500000, 104500000,'UNPAID', v_ss_id, NULL, v_c10, v_da10, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p10, 'Cái', 80, 0, 720000, 10, 57600000, 5760000, 63360000),
           (v_so_id, v_p8,  'Bao', 80, 0, 310000, 10, 24800000, 2480000, 27280000));

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-029','Imexpharm dược - thiết bị T3/2026','ORDER_OPEN','2026-03-13','2026-04-02', NULL, NULL, 85000000, 8500000, 93500000,'UNPAID', v_ss_id, NULL, v_c8, v_da8, v_wh1)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p16, 'Cuộn', 2, 0, 20500000, 10, 41000000, 4100000, 45100000),
           (v_so_id, v_p17, 'Cái', 20, 0, 950000, 10, 19000000, 1900000, 20900000),
           (v_so_id, v_p25, 'Cái', 12, 0, 360000, 10, 4320000, 432000, 4752000));

    INSERT INTO sales_order (code, order_name, status, order_date, expected_delivery_date, approved_date, completed_date, total_amount, tax_amount, grand_total, payment_status, created_by, approved_by, customer_id, delivery_address_id, warehouse_id)
    VALUES ('SO-2026-030','Vật tư tổng hợp Nhựa ĐN T3/2026','ORDER_OPEN','2026-03-14','2026-04-03', NULL, NULL, 72000000, 7200000, 79200000,'UNPAID', v_ss_id, NULL, v_c2, v_da2, v_wh2)
    RETURNING id INTO v_so_id;
    INSERT INTO sales_order_item (sales_order_id, product_id, unit, quantity, delivered_quantity, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
    VALUES (v_so_id, v_p10, 'Cái', 60, 0, 720000, 10, 43200000, 4320000, 47520000),
           (v_so_id, v_p9,  'Can', 40, 0, 390000, 10, 15600000, 1560000, 17160000));

END $$;

-- =====================================================
-- GOODS ISSUES & SALES INVOICES (cho các SO đã COMPLETED)
-- =====================================================
DO $$
DECLARE
    v_ws_id BIGINT; v_sm_id BIGINT; v_ac_id BIGINT;
    v_gi_id BIGINT; v_so_id BIGINT; v_si_id BIGINT;
    v_cust_id BIGINT; v_da_id BIGINT; v_wh_id BIGINT;
    v_total DECIMAL(15,2); v_tax DECIMAL(15,2); v_grand DECIMAL(15,2);
    rec RECORD;
BEGIN
    SELECT id INTO v_ws_id FROM users WHERE username='warehouse_staff';
    SELECT id INTO v_sm_id FROM users WHERE username='sales_manager';
    SELECT id INTO v_ac_id FROM users WHERE username='accountant';

    -- Tạo Goods Issue & Invoice cho các SO COMPLETED (SO001-SO021)
    FOR rec IN
        SELECT so.id so_id, so.code so_code, so.customer_id, so.delivery_address_id,
               so.warehouse_id, so.total_amount, so.tax_amount, so.grand_total,
               so.completed_date, so.order_date,
               ROW_NUMBER() OVER (ORDER BY so.code) rn
        FROM sales_order so
        WHERE so.status = 'ORDER_COMPLETED'
        ORDER BY so.code
    LOOP
        -- Goods Issue
        INSERT INTO goods_issue (code, status, issue_date, confirmed_date, delivery_note_number,
            total_amount, shipping_method, carrier_name, notes, created_by, confirmed_by,
            sales_order_id, warehouse_id, delivery_address_id)
        VALUES (
            'GI-2026-' || LPAD(rec.rn::TEXT, 3, '0'),
            'CONFIRMED',
            (rec.completed_date::DATE - INTERVAL '2 days')::DATE,
            rec.completed_date,
            'GI-DN-' || LPAD(rec.rn::TEXT, 3, '0'),
            rec.grand_total,
            'Xe tải', 'Giao Hàng Nhanh', NULL,
            v_ws_id, v_sm_id,
            rec.so_id, rec.warehouse_id, rec.delivery_address_id
        )
        RETURNING id INTO v_gi_id;

        -- Goods Issue Items (copy từ sales_order_item)
        INSERT INTO goods_issue_item (goods_issue_id, sales_order_item_id, product_id,
            ordered_quantity, issued_quantity, unit_price, total_amount, unit, batch_number)
        SELECT v_gi_id, soi.id, soi.product_id,
               soi.quantity, soi.quantity, soi.unit_price, soi.total_amount, soi.unit,
               'BATCH-ISSUE-' || LPAD(rec.rn::TEXT, 3, '0') || '-' || soi.product_id
        FROM sales_order_item soi WHERE soi.sales_order_id = rec.so_id;

        -- Sales Invoice
        INSERT INTO sales_invoice (code, status, invoice_date, due_date, issued_date, paid_date,
            subtotal, tax_amount, total_amount, paid_amount, remaining_amount,
            payment_method, notes, created_by, issued_by,
            sales_order_id, goods_issue_id, customer_id)
        VALUES (
            'INV-2026-' || LPAD(rec.rn::TEXT, 3, '0'),
            'PAID',
            (rec.completed_date::DATE)::DATE,
            (rec.completed_date::DATE + INTERVAL '30 days')::DATE,
            rec.completed_date,
            (rec.completed_date + INTERVAL '5 days')::TIMESTAMP,
            rec.total_amount,
            rec.tax_amount,
            rec.grand_total,
            rec.grand_total,
            0,
            'Chuyển khoản',
            'Thanh toán đúng hạn',
            v_sm_id, v_ac_id,
            rec.so_id, v_gi_id, rec.customer_id
        )
        RETURNING id INTO v_si_id;

        -- Sales Invoice Items
        INSERT INTO sales_invoice_item (sales_invoice_id, goods_issue_item_id, product_id,
            description, quantity, unit, unit_price, tax_percent, amount_before_tax, tax_amount, total_amount)
        SELECT v_si_id, gii.id, gii.product_id,
               p.name, gii.issued_quantity, gii.unit, gii.unit_price,
               10, gii.total_amount * 10.0/11.0, gii.total_amount/11.0, gii.total_amount
        FROM goods_issue_item gii
        JOIN product p ON p.id = gii.product_id
        WHERE gii.goods_issue_id = v_gi_id;

    END LOOP;
END $$;

-- =====================================================
-- INVENTORY TRANSACTIONS (lịch sử nhập/xuất kho)
-- =====================================================
DO $$
DECLARE
    v_ws_id BIGINT; v_ps_id BIGINT;
    v_wh1 BIGINT;
BEGIN
    SELECT id INTO v_ws_id FROM users WHERE username='warehouse_staff';
    SELECT id INTO v_ps_id FROM users WHERE username='purchase_staff';
    SELECT id INTO v_wh1 FROM warehouse WHERE code='WH001';

    INSERT INTO inventory_transaction (product_id, warehouse_id, transaction_type, quantity, unit_cost, total_cost,
        quantity_before, quantity_after, reference_type, reference_code, transaction_date, created_by, notes)
    SELECT
        gri.product_id,
        gr.warehouse_id,
        'RECEIPT',
        gri.accepted_quantity,
        gri.unit_price,
        gri.total_amount,
        0,
        gri.accepted_quantity,
        'GOODS_RECEIPT',
        gr.code,
        gr.confirmed_date,
        v_ws_id,
        'Nhập kho theo phiếu ' || gr.code
    FROM goods_receipt_item gri
    JOIN goods_receipt gr ON gr.id = gri.goods_receipt_id
    WHERE gr.status = 'CONFIRMED';

    INSERT INTO inventory_transaction (product_id, warehouse_id, transaction_type, quantity, unit_cost, total_cost,
        quantity_before, quantity_after, reference_type, reference_code, transaction_date, created_by, notes)
    SELECT
        gii.product_id,
        gi.warehouse_id,
        'ISSUE',
        gii.issued_quantity,
        gii.unit_price,
        gii.total_amount,
        gii.issued_quantity,
        0,
        'GOODS_ISSUE',
        gi.code,
        gi.confirmed_date,
        v_ws_id,
        'Xuất kho theo phiếu ' || gi.code
    FROM goods_issue_item gii
    JOIN goods_issue gi ON gi.id = gii.goods_issue_id
    WHERE gi.status = 'CONFIRMED';

END $$;
