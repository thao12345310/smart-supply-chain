-- Bảng inventory_lot: lưu chi tiết từng lô hàng tồn kho
CREATE TABLE IF NOT EXISTS inventory_lot (
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

CREATE INDEX IF NOT EXISTS idx_inv_lot_product_warehouse ON inventory_lot(product_id, warehouse_id);
CREATE INDEX IF NOT EXISTS idx_inv_lot_fefo ON inventory_lot(product_id, warehouse_id, expiry_date ASC)
    WHERE quantity_remaining > 0;
CREATE INDEX IF NOT EXISTS idx_inv_lot_expiry ON inventory_lot(expiry_date) WHERE quantity_remaining > 0;
CREATE UNIQUE INDEX IF NOT EXISTS idx_inv_lot_natural_key ON inventory_lot(
    product_id, warehouse_id, lot_number, source_receipt_item_id
);

COMMENT ON TABLE inventory_lot IS 'Chi tiết tồn kho theo lô (lot-based inventory)';
COMMENT ON COLUMN inventory_lot.quantity_remaining IS 'SL còn lại sau khi đã xuất một phần';
COMMENT ON COLUMN inventory_lot.unit_cost IS 'Giá vốn đơn vị (sẽ dùng ở TIP-04 cho bút toán)';