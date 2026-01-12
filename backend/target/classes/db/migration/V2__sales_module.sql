-- =====================================================
-- Distribution Management System - Sales Order Module
-- Database Schema for PostgreSQL
-- =====================================================

-- Enum types (for reference, actual enums are in Java)
-- SalesOrderStatus: ORDER_OPEN, ORDER_APPROVED, ORDER_PARTIALLY_DELIVERED, ORDER_COMPLETED, ORDER_CANCELLED
-- GoodsIssueStatus: DRAFT, CONFIRMED, CANCELLED
-- SalesInvoiceStatus: DRAFT, ISSUED, PAID, CANCELLED

-- =====================================================
-- Sales Module Roles
-- =====================================================

INSERT INTO roles (name, description) VALUES 
    ('ROLE_SALES_STAFF', 'Sales Staff'),
    ('ROLE_SALES_MANAGER', 'Sales Manager'),
    ('ROLE_CUSTOMER', 'Customer')
ON CONFLICT (name) DO NOTHING;

-- =====================================================
-- Customer Table
-- =====================================================

CREATE TABLE IF NOT EXISTS customer (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(255),
    tax_code VARCHAR(50),
    credit_limit DECIMAL(15,2) DEFAULT 0,
    current_balance DECIMAL(15,2) DEFAULT 0,
    payment_terms INTEGER DEFAULT 30,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for customer
CREATE INDEX IF NOT EXISTS idx_customer_code ON customer(code);
CREATE INDEX IF NOT EXISTS idx_customer_name ON customer(name);
CREATE INDEX IF NOT EXISTS idx_customer_email ON customer(email);

-- =====================================================
-- Delivery Address Table (Customer can have multiple addresses)
-- =====================================================

CREATE TABLE IF NOT EXISTS delivery_address (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE CASCADE,
    address_name VARCHAR(255),
    recipient_name VARCHAR(255),
    phone VARCHAR(50),
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) DEFAULT 'Vietnam',
    is_default BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for delivery_address
CREATE INDEX IF NOT EXISTS idx_da_customer ON delivery_address(customer_id);

-- =====================================================
-- Sales Order Tables
-- =====================================================

-- Sales Order header
CREATE TABLE IF NOT EXISTS sales_order (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    order_name VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ORDER_OPEN',
    order_date DATE NOT NULL DEFAULT CURRENT_DATE,
    expected_delivery_date DATE,
    approved_date TIMESTAMP,
    completed_date TIMESTAMP,
    total_amount DECIMAL(15,2),
    tax_amount DECIMAL(15,2),
    discount_amount DECIMAL(15,2) DEFAULT 0,
    shipping_cost DECIMAL(15,2) DEFAULT 0,
    grand_total DECIMAL(15,2),
    payment_status VARCHAR(50) DEFAULT 'UNPAID',
    notes TEXT,
    rejection_reason TEXT,
    created_by BIGINT REFERENCES users(id),
    approved_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    delivery_address_id BIGINT REFERENCES delivery_address(id),
    warehouse_id BIGINT REFERENCES warehouse(id)
);

-- Indexes for sales_order
CREATE INDEX IF NOT EXISTS idx_so_code ON sales_order(code);
CREATE INDEX IF NOT EXISTS idx_so_status ON sales_order(status);
CREATE INDEX IF NOT EXISTS idx_so_customer ON sales_order(customer_id);
CREATE INDEX IF NOT EXISTS idx_so_order_date ON sales_order(order_date);
CREATE INDEX IF NOT EXISTS idx_so_payment_status ON sales_order(payment_status);

-- Sales Order items
CREATE TABLE IF NOT EXISTS sales_order_item (
    id BIGSERIAL PRIMARY KEY,
    sales_order_id BIGINT NOT NULL REFERENCES sales_order(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES product(id),
    unit VARCHAR(50),
    quantity INTEGER NOT NULL,
    delivered_quantity INTEGER NOT NULL DEFAULT 0,
    unit_price DECIMAL(15,2) NOT NULL,
    discount_percent DECIMAL(5,2) DEFAULT 0,
    tax_percent DECIMAL(5,2) DEFAULT 0,
    amount_before_tax DECIMAL(15,2),
    tax_amount DECIMAL(15,2),
    total_amount DECIMAL(15,2),
    notes TEXT
);

-- Indexes for sales_order_item
CREATE INDEX IF NOT EXISTS idx_soi_sales_order ON sales_order_item(sales_order_id);
CREATE INDEX IF NOT EXISTS idx_soi_product ON sales_order_item(product_id);

-- =====================================================
-- Goods Issue Tables (Outbound delivery)
-- =====================================================

-- Goods Issue header
CREATE TABLE IF NOT EXISTS goods_issue (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    issue_date DATE NOT NULL DEFAULT CURRENT_DATE,
    confirmed_date TIMESTAMP,
    delivery_note_number VARCHAR(100),
    total_amount DECIMAL(15,2),
    shipping_method VARCHAR(100),
    tracking_number VARCHAR(100),
    carrier_name VARCHAR(100),
    notes TEXT,
    created_by BIGINT REFERENCES users(id),
    confirmed_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    sales_order_id BIGINT NOT NULL REFERENCES sales_order(id),
    warehouse_id BIGINT REFERENCES warehouse(id),
    delivery_address_id BIGINT REFERENCES delivery_address(id)
);

-- Indexes for goods_issue
CREATE INDEX IF NOT EXISTS idx_gi_code ON goods_issue(code);
CREATE INDEX IF NOT EXISTS idx_gi_status ON goods_issue(status);
CREATE INDEX IF NOT EXISTS idx_gi_sales_order ON goods_issue(sales_order_id);
CREATE INDEX IF NOT EXISTS idx_gi_issue_date ON goods_issue(issue_date);

-- Goods Issue items
CREATE TABLE IF NOT EXISTS goods_issue_item (
    id BIGSERIAL PRIMARY KEY,
    goods_issue_id BIGINT NOT NULL REFERENCES goods_issue(id) ON DELETE CASCADE,
    sales_order_item_id BIGINT NOT NULL REFERENCES sales_order_item(id),
    product_id BIGINT NOT NULL REFERENCES product(id),
    ordered_quantity INTEGER NOT NULL,
    issued_quantity INTEGER NOT NULL,
    unit_price DECIMAL(15,2),
    total_amount DECIMAL(15,2),
    unit VARCHAR(50),
    batch_number VARCHAR(100),
    expiry_date DATE,
    notes TEXT
);

-- Indexes for goods_issue_item
CREATE INDEX IF NOT EXISTS idx_gii_goods_issue ON goods_issue_item(goods_issue_id);
CREATE INDEX IF NOT EXISTS idx_gii_so_item ON goods_issue_item(sales_order_item_id);
CREATE INDEX IF NOT EXISTS idx_gii_product ON goods_issue_item(product_id);

-- =====================================================
-- Sales Invoice Tables
-- =====================================================

-- Sales Invoice header
CREATE TABLE IF NOT EXISTS sales_invoice (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    invoice_date DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date DATE,
    issued_date TIMESTAMP,
    paid_date TIMESTAMP,
    subtotal DECIMAL(15,2),
    tax_amount DECIMAL(15,2),
    discount_amount DECIMAL(15,2) DEFAULT 0,
    shipping_cost DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(15,2),
    paid_amount DECIMAL(15,2) DEFAULT 0,
    remaining_amount DECIMAL(15,2),
    payment_method VARCHAR(50),
    payment_reference VARCHAR(100),
    notes TEXT,
    created_by BIGINT REFERENCES users(id),
    issued_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    sales_order_id BIGINT NOT NULL REFERENCES sales_order(id),
    goods_issue_id BIGINT NOT NULL REFERENCES goods_issue(id),
    customer_id BIGINT NOT NULL REFERENCES customer(id)
);

-- Indexes for sales_invoice
CREATE INDEX IF NOT EXISTS idx_si_code ON sales_invoice(code);
CREATE INDEX IF NOT EXISTS idx_si_status ON sales_invoice(status);
CREATE INDEX IF NOT EXISTS idx_si_sales_order ON sales_invoice(sales_order_id);
CREATE INDEX IF NOT EXISTS idx_si_goods_issue ON sales_invoice(goods_issue_id);
CREATE INDEX IF NOT EXISTS idx_si_customer ON sales_invoice(customer_id);
CREATE INDEX IF NOT EXISTS idx_si_invoice_date ON sales_invoice(invoice_date);
CREATE INDEX IF NOT EXISTS idx_si_due_date ON sales_invoice(due_date);

-- Sales Invoice items (line items)
CREATE TABLE IF NOT EXISTS sales_invoice_item (
    id BIGSERIAL PRIMARY KEY,
    sales_invoice_id BIGINT NOT NULL REFERENCES sales_invoice(id) ON DELETE CASCADE,
    goods_issue_item_id BIGINT REFERENCES goods_issue_item(id),
    product_id BIGINT NOT NULL REFERENCES product(id),
    description VARCHAR(500),
    quantity INTEGER NOT NULL,
    unit VARCHAR(50),
    unit_price DECIMAL(15,2),
    discount_percent DECIMAL(5,2) DEFAULT 0,
    tax_percent DECIMAL(5,2) DEFAULT 0,
    amount_before_tax DECIMAL(15,2),
    tax_amount DECIMAL(15,2),
    total_amount DECIMAL(15,2)
);

-- Indexes for sales_invoice_item
CREATE INDEX IF NOT EXISTS idx_sii_invoice ON sales_invoice_item(sales_invoice_id);
CREATE INDEX IF NOT EXISTS idx_sii_product ON sales_invoice_item(product_id);

-- =====================================================
-- Inventory Reservation Table (prevents overselling)
-- =====================================================

CREATE TABLE IF NOT EXISTS inventory_reservation (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouse(id),
    sales_order_id BIGINT NOT NULL REFERENCES sales_order(id),
    sales_order_item_id BIGINT NOT NULL REFERENCES sales_order_item(id),
    reserved_quantity INTEGER NOT NULL,
    fulfilled_quantity INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'RESERVED',
    reserved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    released_at TIMESTAMP,
    notes TEXT,
    UNIQUE(sales_order_item_id)
);

-- Indexes for inventory_reservation
CREATE INDEX IF NOT EXISTS idx_ir_product ON inventory_reservation(product_id);
CREATE INDEX IF NOT EXISTS idx_ir_warehouse ON inventory_reservation(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_ir_sales_order ON inventory_reservation(sales_order_id);
CREATE INDEX IF NOT EXISTS idx_ir_status ON inventory_reservation(status);

-- =====================================================
-- Sample Data for Testing
-- =====================================================

-- Insert sample customers
INSERT INTO customer (code, name, contact_name, phone, email, tax_code, credit_limit, payment_terms) VALUES 
    ('CUST001', 'Acme Corporation', 'John Doe', '111-222-3333', 'john@acme.com', 'TAX001', 100000.00, 30),
    ('CUST002', 'Tech Solutions Inc', 'Jane Smith', '222-333-4444', 'jane@techsol.com', 'TAX002', 150000.00, 45),
    ('CUST003', 'Global Trading Co', 'Bob Wilson', '333-444-5555', 'bob@globaltrading.com', 'TAX003', 75000.00, 30),
    ('CUST004', 'Smart Electronics', 'Alice Brown', '444-555-6666', 'alice@smartelec.com', 'TAX004', 200000.00, 60),
    ('CUST005', 'Metro Distributors', 'Charlie Davis', '555-666-7777', 'charlie@metrodist.com', 'TAX005', 50000.00, 15)
ON CONFLICT (code) DO NOTHING;

-- Insert sample delivery addresses
INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, is_default) 
SELECT c.id, 'Main Office', c.contact_name, c.phone, '123 Main Street', 'Ho Chi Minh City', 'HCMC', '70000', true
FROM customer c WHERE c.code = 'CUST001'
ON CONFLICT DO NOTHING;

INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, is_default) 
SELECT c.id, 'Warehouse', 'Warehouse Manager', '111-222-3334', '456 Industrial Zone', 'Binh Duong', 'BD', '75000', false
FROM customer c WHERE c.code = 'CUST001'
ON CONFLICT DO NOTHING;

INSERT INTO delivery_address (customer_id, address_name, recipient_name, phone, address_line1, city, state, postal_code, is_default) 
SELECT c.id, 'Head Office', c.contact_name, c.phone, '789 Tech Park', 'Ha Noi', 'HN', '10000', true
FROM customer c WHERE c.code = 'CUST002'
ON CONFLICT DO NOTHING;

-- Insert sample sales staff users
INSERT INTO users (username, password, full_name, email, active) VALUES 
    ('sarah.sales', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqDgFl0JQYxP9W8q1w.VfANfGkqNGqO', 'Sarah Sales', 'sarah@example.com', true),
    ('mike.manager', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqDgFl0JQYxP9W8q1w.VfANfGkqNGqO', 'Mike Manager', 'mike@example.com', true)
ON CONFLICT (username) DO NOTHING;

-- Assign sales roles to users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'sarah.sales' AND r.name = 'ROLE_SALES_STAFF'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'mike.manager' AND r.name = 'ROLE_SALES_MANAGER'
ON CONFLICT DO NOTHING;
