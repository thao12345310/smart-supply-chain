-- =====================================================
-- Distribution Management System - Purchasing Module
-- Database Schema for PostgreSQL
-- =====================================================

-- Enum types (for reference, actual enums are in Java)
-- PurchaseOrderStatus: ORDER_OPEN, ORDER_APPROVED, ORDER_PARTIALLY_RECEIVED, ORDER_COMPLETED, ORDER_CANCELLED
-- GoodsReceiptStatus: DRAFT, CONFIRMED, CANCELLED

-- =====================================================
-- Core Tables
-- =====================================================

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);

-- Insert default roles
INSERT INTO roles (name, description) VALUES 
    ('ROLE_ADMIN', 'System Administrator'),
    ('ROLE_PURCHASING_STAFF', 'Purchasing Staff'),
    ('ROLE_PURCHASING_MANAGER', 'Purchasing Manager'),
    ('ROLE_ACCOUNTANT', 'Accountant'),
    ('ROLE_WAREHOUSE_STAFF', 'Warehouse Staff'),
    ('ROLE_SUPPLIER', 'Supplier')
ON CONFLICT (name) DO NOTHING;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255),
    full_name VARCHAR(255),
    email VARCHAR(255),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User-Role relationship
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

-- Supplier table
CREATE TABLE IF NOT EXISTS supplier (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(255),
    address TEXT
);

-- Warehouse table
CREATE TABLE IF NOT EXISTS warehouse (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    location TEXT,
    description TEXT
);

-- Product table
CREATE TABLE IF NOT EXISTS product (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    quantity INTEGER,
    price DECIMAL(15,2),
    supplier_id BIGINT REFERENCES supplier(id)
);

-- =====================================================
-- Purchase Order Tables
-- =====================================================

-- Purchase Order header
CREATE TABLE IF NOT EXISTS purchase_order (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    order_name VARCHAR(255),
    invoice_number VARCHAR(100),
    tax_type VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'ORDER_OPEN',
    shipping_cost DECIMAL(15,2),
    total_amount DECIMAL(15,2),
    delivery_date TIMESTAMP,
    created_date DATE NOT NULL DEFAULT CURRENT_DATE,
    approved_date TIMESTAMP,
    completed_date TIMESTAMP,
    notes TEXT,
    rejection_reason TEXT,
    created_by BIGINT REFERENCES users(id),
    approved_by BIGINT REFERENCES users(id),
    updated_at TIMESTAMP,
    supplier_id BIGINT NOT NULL REFERENCES supplier(id),
    warehouse_id BIGINT REFERENCES warehouse(id)
);

-- Indexes for purchase_order
CREATE INDEX IF NOT EXISTS idx_po_code ON purchase_order(code);
CREATE INDEX IF NOT EXISTS idx_po_status ON purchase_order(status);
CREATE INDEX IF NOT EXISTS idx_po_supplier ON purchase_order(supplier_id);
CREATE INDEX IF NOT EXISTS idx_po_created_date ON purchase_order(created_date);

-- Purchase Order items
CREATE TABLE IF NOT EXISTS purchase_order_item (
    id BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL REFERENCES purchase_order(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES product(id),
    unit VARCHAR(50),
    quantity INTEGER NOT NULL,
    received_quantity INTEGER NOT NULL DEFAULT 0,
    unit_price DECIMAL(15,2) NOT NULL,
    cost_before_tax DECIMAL(15,2),
    amount_before_tax DECIMAL(15,2),
    tax_amount DECIMAL(15,2),
    total_amount DECIMAL(15,2),
    notes TEXT
);

-- Indexes for purchase_order_item
CREATE INDEX IF NOT EXISTS idx_poi_purchase_order ON purchase_order_item(purchase_order_id);
CREATE INDEX IF NOT EXISTS idx_poi_product ON purchase_order_item(product_id);

-- =====================================================
-- Goods Receipt Tables
-- =====================================================

-- Goods Receipt header
CREATE TABLE IF NOT EXISTS goods_receipt (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    receipt_date DATE NOT NULL DEFAULT CURRENT_DATE,
    confirmed_date TIMESTAMP,
    delivery_note_number VARCHAR(100),
    invoice_number VARCHAR(100),
    total_amount DECIMAL(15,2),
    notes TEXT,
    created_by BIGINT REFERENCES users(id),
    confirmed_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    purchase_order_id BIGINT NOT NULL REFERENCES purchase_order(id),
    warehouse_id BIGINT REFERENCES warehouse(id)
);

-- Indexes for goods_receipt
CREATE INDEX IF NOT EXISTS idx_gr_code ON goods_receipt(code);
CREATE INDEX IF NOT EXISTS idx_gr_status ON goods_receipt(status);
CREATE INDEX IF NOT EXISTS idx_gr_purchase_order ON goods_receipt(purchase_order_id);
CREATE INDEX IF NOT EXISTS idx_gr_receipt_date ON goods_receipt(receipt_date);

-- Goods Receipt items
CREATE TABLE IF NOT EXISTS goods_receipt_item (
    id BIGSERIAL PRIMARY KEY,
    goods_receipt_id BIGINT NOT NULL REFERENCES goods_receipt(id) ON DELETE CASCADE,
    purchase_order_item_id BIGINT NOT NULL REFERENCES purchase_order_item(id),
    product_id BIGINT NOT NULL REFERENCES product(id),
    ordered_quantity INTEGER NOT NULL,
    received_quantity INTEGER NOT NULL,
    accepted_quantity INTEGER NOT NULL,
    rejected_quantity INTEGER DEFAULT 0,
    unit_price DECIMAL(15,2),
    total_amount DECIMAL(15,2),
    unit VARCHAR(50),
    batch_number VARCHAR(100),
    expiry_date DATE,
    rejection_reason VARCHAR(255),
    notes TEXT
);

-- Indexes for goods_receipt_item
CREATE INDEX IF NOT EXISTS idx_gri_goods_receipt ON goods_receipt_item(goods_receipt_id);
CREATE INDEX IF NOT EXISTS idx_gri_po_item ON goods_receipt_item(purchase_order_item_id);
CREATE INDEX IF NOT EXISTS idx_gri_product ON goods_receipt_item(product_id);

-- =====================================================
-- Inventory Tables
-- =====================================================

-- Inventory (stock levels per product per warehouse)
CREATE TABLE IF NOT EXISTS inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouse(id),
    quantity_on_hand INTEGER NOT NULL DEFAULT 0,
    quantity_reserved INTEGER NOT NULL DEFAULT 0,
    quantity_available INTEGER NOT NULL DEFAULT 0,
    reorder_level INTEGER,
    reorder_quantity INTEGER,
    average_cost DECIMAL(15,2),
    last_received_date TIMESTAMP,
    last_issued_date TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    UNIQUE(product_id, warehouse_id)
);

-- Indexes for inventory
CREATE INDEX IF NOT EXISTS idx_inv_product ON inventory(product_id);
CREATE INDEX IF NOT EXISTS idx_inv_warehouse ON inventory(warehouse_id);

-- Inventory transactions (audit trail)
CREATE TABLE IF NOT EXISTS inventory_transaction (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouse(id),
    transaction_type VARCHAR(30) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_cost DECIMAL(15,2),
    total_cost DECIMAL(15,2),
    quantity_before INTEGER,
    quantity_after INTEGER,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    reference_code VARCHAR(50),
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    notes TEXT
);

-- Indexes for inventory_transaction
CREATE INDEX IF NOT EXISTS idx_inv_tx_product ON inventory_transaction(product_id);
CREATE INDEX IF NOT EXISTS idx_inv_tx_warehouse ON inventory_transaction(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_inv_tx_type ON inventory_transaction(transaction_type);
CREATE INDEX IF NOT EXISTS idx_inv_tx_date ON inventory_transaction(transaction_date);
CREATE INDEX IF NOT EXISTS idx_inv_tx_ref ON inventory_transaction(reference_type, reference_id);

-- =====================================================
-- Sample Data for Testing
-- =====================================================

-- Insert sample users
INSERT INTO users (username, password, full_name, email, active) VALUES 
    ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqDgFl0JQYxP9W8q1w.VfANfGkqNGqO', 'System Admin', 'admin@example.com', true),
    ('john.buyer', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqDgFl0JQYxP9W8q1w.VfANfGkqNGqO', 'John Buyer', 'john@example.com', true),
    ('jane.manager', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqDgFl0JQYxP9W8q1w.VfANfGkqNGqO', 'Jane Manager', 'jane@example.com', true),
    ('bob.warehouse', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqDgFl0JQYxP9W8q1w.VfANfGkqNGqO', 'Bob Warehouse', 'bob@example.com', true),
    ('alice.accountant', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqDgFl0JQYxP9W8q1w.VfANfGkqNGqO', 'Alice Accountant', 'alice@example.com', true)
ON CONFLICT (username) DO NOTHING;

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'john.buyer' AND r.name = 'ROLE_PURCHASING_STAFF'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'jane.manager' AND r.name = 'ROLE_PURCHASING_MANAGER'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'bob.warehouse' AND r.name = 'ROLE_WAREHOUSE_STAFF'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'alice.accountant' AND r.name = 'ROLE_ACCOUNTANT'
ON CONFLICT DO NOTHING;

-- Insert sample suppliers
INSERT INTO supplier (code, name, contact_name, phone, email, address) VALUES 
    ('SUP001', 'ABC Electronics', 'Tom Smith', '123-456-7890', 'tom@abc.com', '123 Tech Street'),
    ('SUP002', 'XYZ Materials', 'Mary Johnson', '234-567-8901', 'mary@xyz.com', '456 Industrial Ave'),
    ('SUP003', 'Global Parts', 'David Brown', '345-678-9012', 'david@global.com', '789 Commerce Blvd')
ON CONFLICT (code) DO NOTHING;

-- Insert sample warehouses
INSERT INTO warehouse (code, name, location, description) VALUES 
    ('WH001', 'Main Warehouse', 'Building A', 'Primary storage facility'),
    ('WH002', 'Secondary Warehouse', 'Building B', 'Overflow storage'),
    ('WH003', 'Distribution Center', 'Building C', 'Shipping and receiving')
ON CONFLICT (code) DO NOTHING;

-- Insert sample products
INSERT INTO product (code, name, description, quantity, price, supplier_id) VALUES 
    ('PROD001', 'Widget A', 'Standard widget', 100, 25.00, 1),
    ('PROD002', 'Widget B', 'Premium widget', 50, 45.00, 1),
    ('PROD003', 'Component X', 'Essential component', 200, 12.50, 2),
    ('PROD004', 'Component Y', 'Advanced component', 75, 35.00, 2),
    ('PROD005', 'Part Z', 'Replacement part', 150, 8.00, 3)
ON CONFLICT (code) DO NOTHING;
