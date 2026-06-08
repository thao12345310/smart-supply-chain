-- V7: Sửa liên kết delivery_order.sales_order_id
--
-- Trước đây entity DeliveryOrder khai báo nhầm quan hệ tới PurchaseOrder, nên ràng buộc
-- khóa ngoại trên cột sales_order_id (do Hibernate sinh ở thời ddl-auto=update) trỏ sang
-- bảng purchase_order. Cột này luôn NULL nên chưa gây lỗi, nhưng khi bắt đầu gán đúng
-- đơn bán hàng (SalesOrder) thì FK cũ sẽ chặn. Migration này gỡ mọi FK đang gắn trên cột
-- sales_order_id của delivery_order rồi tạo lại FK trỏ đúng sang sales_order.
--
-- An toàn cả khi chạy lại (idempotent) và khi bảng delivery_order chưa tồn tại.

DO $$
DECLARE
    fk_name text;
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_name = 'delivery_order'
    ) THEN
        -- Gỡ mọi khóa ngoại đang gắn trên cột sales_order_id
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

        -- Tạo lại FK trỏ đúng sang sales_order (nếu chưa có)
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.table_constraints
            WHERE table_name = 'delivery_order'
              AND constraint_name = 'fk_delivery_order_sales_order'
        ) THEN
            ALTER TABLE delivery_order
                ADD CONSTRAINT fk_delivery_order_sales_order
                FOREIGN KEY (sales_order_id) REFERENCES sales_order(id);
        END IF;
    END IF;
END $$;
