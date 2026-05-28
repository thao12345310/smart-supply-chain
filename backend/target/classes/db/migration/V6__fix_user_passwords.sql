-- V6__fix_user_passwords.sql
-- Fix BCrypt password hashes that were using a non-functional example hash

-- Admin user password: admin123
UPDATE users SET password = '$2b$10$qVBH87sk.T2gRXfWFiX1y.8vt7QYbS/BfO9p5sP0yq3UtHbrt23KK'
WHERE username = 'admin';

-- All other sample users password: password123
UPDATE users SET password = '$2b$10$oapRaEqjSp3FFsp6I/GqSO4I2VJijZiCn.3/FzTd38WCXJpoojiLG'
WHERE username IN ('purchase_staff', 'purchase_manager', 'sales_staff', 'sales_manager',
                   'warehouse_staff', 'delivery_admin', 'shipper1', 'shipper2', 'accountant');
