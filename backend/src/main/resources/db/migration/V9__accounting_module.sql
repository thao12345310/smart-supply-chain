-- V9: Module kế toán tối thiểu — account, accounting_transaction, payment

CREATE TABLE account (
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL
);

INSERT INTO account (code, name) VALUES
    ('CASH',      'Tiền mặt/Ngân hàng'),
    ('AR',        'Phải thu khách hàng'),
    ('AP',        'Phải trả nhà cung cấp'),
    ('REVENUE',   'Doanh thu'),
    ('INVENTORY', 'Hàng tồn kho'),
    ('EXPENSE',   'Chi phí / Giá vốn');

CREATE TABLE accounting_transaction (
    id             BIGSERIAL PRIMARY KEY,
    tx_date        TIMESTAMP NOT NULL,
    description    VARCHAR(255) NOT NULL,
    source_type    VARCHAR(20),
    source_id      BIGINT,
    debit_account  VARCHAR(20) NOT NULL,
    credit_account VARCHAR(20) NOT NULL,
    amount         NUMERIC(18,2) NOT NULL
);
CREATE INDEX idx_acctx_date   ON accounting_transaction(tx_date);
CREATE INDEX idx_acctx_source ON accounting_transaction(source_type, source_id);

CREATE TABLE payment (
    id                BIGSERIAL PRIMARY KEY,
    code              VARCHAR(255) NOT NULL UNIQUE,
    type              VARCHAR(20) NOT NULL,
    amount            NUMERIC(18,2) NOT NULL,
    payment_date      DATE NOT NULL,
    method            VARCHAR(50),
    sales_invoice_id  BIGINT,
    purchase_order_id BIGINT,
    note              VARCHAR(500)
);
CREATE INDEX idx_payment_type ON payment(type);
CREATE INDEX idx_payment_date ON payment(payment_date);

COMMENT ON TABLE accounting_transaction IS 'Bút toán Nợ/Có đơn giản, sinh tự động từ GR/Invoice/Payment';
COMMENT ON TABLE payment IS 'Phiếu thu (RECEIPT) / phiếu chi (DISBURSEMENT)';
