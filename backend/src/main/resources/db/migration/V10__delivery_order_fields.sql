-- V10: bổ sung thông tin người nhận / ngày giao / liên kết phiếu xuất cho vận đơn
ALTER TABLE delivery_order ADD COLUMN IF NOT EXISTS recipient_name  VARCHAR(255);
ALTER TABLE delivery_order ADD COLUMN IF NOT EXISTS recipient_phone VARCHAR(50);
ALTER TABLE delivery_order ADD COLUMN IF NOT EXISTS planned_date    DATE;
ALTER TABLE delivery_order ADD COLUMN IF NOT EXISTS goods_issue_id  BIGINT;
