-- Thêm DISPOSAL vào check constraint loại giao dịch tồn kho (constraint cũ do Hibernate sinh)
ALTER TABLE inventory_transaction DROP CONSTRAINT IF EXISTS inventory_transaction_transaction_type_check;
ALTER TABLE inventory_transaction ADD CONSTRAINT inventory_transaction_transaction_type_check
    CHECK (transaction_type IN ('RECEIPT', 'ISSUE', 'TRANSFER_IN', 'TRANSFER_OUT',
        'ADJUSTMENT_PLUS', 'ADJUSTMENT_MINUS', 'RETURN_IN', 'RETURN_OUT', 'DISPOSAL'));

-- Bảng lot_disposal: phiếu xuất hủy lô hàng (hết hạn sử dụng / hư hỏng)
CREATE TABLE IF NOT EXISTS lot_disposal (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    lot_id BIGINT NOT NULL REFERENCES inventory_lot(id),
    product_id BIGINT NOT NULL REFERENCES product(id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouse(id),
    lot_number VARCHAR(64) NOT NULL,
    expiry_date DATE,
    quantity NUMERIC(18,3) NOT NULL CHECK (quantity > 0),
    unit_cost NUMERIC(18,2),
    reason VARCHAR(255),
    disposed_by BIGINT,
    disposed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_lot_disposal_lot ON lot_disposal(lot_id);
CREATE INDEX IF NOT EXISTS idx_lot_disposal_warehouse ON lot_disposal(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_lot_disposal_date ON lot_disposal(disposed_at);

COMMENT ON TABLE lot_disposal IS 'Phiếu xuất hủy lô (hết HSD/hư hỏng); mỗi phiếu hủy toàn bộ tồn còn lại của 1 lô';
COMMENT ON COLUMN lot_disposal.quantity IS 'SL đã hủy = quantity_remaining của lô tại thời điểm hủy';
