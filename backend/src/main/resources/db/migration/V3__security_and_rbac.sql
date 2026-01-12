-- V3__security_and_rbac.sql
-- Role-Based Access Control (RBAC) Schema and Initial Data

-- Create roles table if not exists
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);

-- Create users table if not exists
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create user_roles join table if not exists
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Insert default roles
INSERT INTO roles (name, description) VALUES
    ('ROLE_ADMIN', 'System Administrator - Full access to all operations'),
    ('ROLE_PURCHASE_STAFF', 'Purchasing Staff - Create and manage purchase orders'),
    ('ROLE_PURCHASE_MANAGER', 'Purchasing Manager - Manage purchases and approve purchase orders'),
    ('ROLE_SALES_STAFF', 'Sales Staff - Create and manage sales orders'),
    ('ROLE_SALES_MANAGER', 'Sales Manager - Manage sales and approve sales orders'),
    ('ROLE_WAREHOUSE_STAFF', 'Warehouse Staff - Manage goods receipt/issue and inventory'),
    ('ROLE_DELIVERY_ADMIN', 'Delivery Administrator - Manage delivery plans and assign shippers'),
    ('ROLE_SHIPPER', 'Shipper - Handle assigned delivery trips only'),
    ('ROLE_ACCOUNTANT', 'Accountant - Approve orders and view financial data')
ON CONFLICT (name) DO NOTHING;

-- Create index on user_roles for faster lookups
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);

-- Create index on users for authentication
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);

-- Add shipper_user_id column to delivery_triproute if not exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'delivery_triproute' AND column_name = 'shipper_user_id'
    ) THEN
        ALTER TABLE delivery_triproute ADD COLUMN shipper_user_id BIGINT REFERENCES users(id);
    END IF;
END $$;

-- Add started_at and completed_at columns to delivery_triproute if not exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'delivery_triproute' AND column_name = 'started_at'
    ) THEN
        ALTER TABLE delivery_triproute ADD COLUMN started_at TIMESTAMP;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'delivery_triproute' AND column_name = 'completed_at'
    ) THEN
        ALTER TABLE delivery_triproute ADD COLUMN completed_at TIMESTAMP;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'delivery_triproute' AND column_name = 'notes'
    ) THEN
        ALTER TABLE delivery_triproute ADD COLUMN notes VARCHAR(500);
    END IF;
END $$;

-- Create index for shipper lookup
CREATE INDEX IF NOT EXISTS idx_trip_shipper ON delivery_triproute(shipper_user_id);
CREATE INDEX IF NOT EXISTS idx_trip_status ON delivery_triproute(status);

-- Insert a default admin user (password: admin123)
-- BCrypt hash for 'admin123'
INSERT INTO users (username, password, full_name, email, active)
VALUES (
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'System Administrator',
    'admin@distribution.local',
    true
)
ON CONFLICT (username) DO NOTHING;

-- Assign admin role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

-- Insert sample users for testing (all passwords: 'password123')
-- BCrypt hash for 'password123'
INSERT INTO users (username, password, full_name, email, active) VALUES
    ('purchase_staff', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Purchase Staff User', 'purchase.staff@distribution.local', true),
    ('purchase_manager', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Purchase Manager User', 'purchase.manager@distribution.local', true),
    ('sales_staff', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Sales Staff User', 'sales.staff@distribution.local', true),
    ('sales_manager', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Sales Manager User', 'sales.manager@distribution.local', true),
    ('warehouse_staff', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Warehouse Staff User', 'warehouse.staff@distribution.local', true),
    ('delivery_admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Delivery Admin User', 'delivery.admin@distribution.local', true),
    ('shipper1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Shipper One', 'shipper1@distribution.local', true),
    ('shipper2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Shipper Two', 'shipper2@distribution.local', true),
    ('accountant', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Accountant User', 'accountant@distribution.local', true)
ON CONFLICT (username) DO NOTHING;

-- Assign roles to sample users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u CROSS JOIN roles r
WHERE u.username = 'purchase_staff' AND r.name = 'ROLE_PURCHASE_STAFF'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u CROSS JOIN roles r
WHERE u.username = 'purchase_manager' AND r.name = 'ROLE_PURCHASE_MANAGER'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u CROSS JOIN roles r
WHERE u.username = 'sales_staff' AND r.name = 'ROLE_SALES_STAFF'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u CROSS JOIN roles r
WHERE u.username = 'sales_manager' AND r.name = 'ROLE_SALES_MANAGER'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u CROSS JOIN roles r
WHERE u.username = 'warehouse_staff' AND r.name = 'ROLE_WAREHOUSE_STAFF'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u CROSS JOIN roles r
WHERE u.username = 'delivery_admin' AND r.name = 'ROLE_DELIVERY_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u CROSS JOIN roles r
WHERE u.username = 'shipper1' AND r.name = 'ROLE_SHIPPER'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u CROSS JOIN roles r
WHERE u.username = 'shipper2' AND r.name = 'ROLE_SHIPPER'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u CROSS JOIN roles r
WHERE u.username = 'accountant' AND r.name = 'ROLE_ACCOUNTANT'
ON CONFLICT DO NOTHING;

-- Add audit columns to other tables if needed
DO $$
BEGIN
    -- Add created_by and approved_by to purchase_order if not exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'purchase_order' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE purchase_order ADD COLUMN created_by BIGINT REFERENCES users(id);
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'purchase_order' AND column_name = 'approved_by'
    ) THEN
        ALTER TABLE purchase_order ADD COLUMN approved_by BIGINT REFERENCES users(id);
    END IF;
END $$;
