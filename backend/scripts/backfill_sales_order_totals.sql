-- =====================================================
-- Backfill totals cho các đơn bán hàng cũ
-- =====================================================
-- Vấn đề: Trước khi fix, SalesOrder.totalAmount và grand_total bị lưu = 0
-- vì recalculateTotal() chạy trước khi @PrePersist tính item.total_amount.
--
-- Script này tính lại:
--   1. sales_order_item.amount_before_tax, tax_amount, total_amount
--      (theo đúng logic SalesOrderItem.calculateAmounts())
--   2. sales_order.total_amount, tax_amount, grand_total
--      (theo đúng logic SalesOrder.recalculateTotal())
--
-- Cách dùng:
--   psql -d <database> -f backfill_sales_order_totals.sql
--
-- Hoặc paste vào tool DB (DBeaver / pgAdmin). Nên chạy trong transaction
-- để xem kết quả trước khi COMMIT.
-- =====================================================

BEGIN;

-- ---------------------------------------------------------------
-- Bước 1: Backfill các cột tính toán ở cấp item
-- Công thức (khớp với SalesOrderItem.calculateAmounts):
--   gross              = unit_price * quantity
--   amount_before_tax  = gross * (1 - discount_percent / 100)
--   tax_amount         = amount_before_tax * tax_percent / 100
--   total_amount       = amount_before_tax + tax_amount
-- ---------------------------------------------------------------
UPDATE sales_order_item
SET
    amount_before_tax = (unit_price * quantity)
                        * (1 - COALESCE(discount_percent, 0) / 100.0),
    tax_amount        = (unit_price * quantity)
                        * (1 - COALESCE(discount_percent, 0) / 100.0)
                        * COALESCE(tax_percent, 0) / 100.0,
    total_amount      = (unit_price * quantity)
                        * (1 - COALESCE(discount_percent, 0) / 100.0)
                        * (1 + COALESCE(tax_percent, 0) / 100.0)
WHERE total_amount IS NULL
   OR total_amount = 0;

-- ---------------------------------------------------------------
-- Bước 2: Tính lại total_amount / tax_amount / grand_total cấp order
-- Công thức (khớp với SalesOrder.recalculateTotal sau fix double-tax):
--   order.total_amount = SUM(item.total_amount)   -- đã gồm thuế
--   order.tax_amount   = SUM(item.tax_amount)
--   order.grand_total  = total_amount - discount + shipping
-- (KHÔNG cộng tax lần nữa vì total_amount đã gồm thuế)
-- ---------------------------------------------------------------
WITH item_totals AS (
    SELECT
        sales_order_id,
        SUM(COALESCE(total_amount, 0)) AS sum_total,
        SUM(COALESCE(tax_amount,   0)) AS sum_tax
    FROM sales_order_item
    GROUP BY sales_order_id
)
UPDATE sales_order so
SET
    total_amount = it.sum_total,
    tax_amount   = it.sum_tax,
    grand_total  = it.sum_total
                 - COALESCE(so.discount_amount, 0)
                 + COALESCE(so.shipping_cost,   0)
FROM item_totals it
WHERE so.id = it.sales_order_id
  AND (so.total_amount IS NULL
       OR so.total_amount = 0
       OR so.grand_total  IS NULL
       OR so.grand_total  = 0);

-- ---------------------------------------------------------------
-- Bước 3: Kiểm tra kết quả trước khi commit
-- ---------------------------------------------------------------
SELECT
    so.id,
    so.code,
    so.total_amount,
    so.tax_amount,
    so.discount_amount,
    so.shipping_cost,
    so.grand_total,
    (SELECT COUNT(*) FROM sales_order_item WHERE sales_order_id = so.id) AS item_count
FROM sales_order so
ORDER BY so.id DESC
LIMIT 50;

-- Nếu kết quả ổn, chạy:   COMMIT;
-- Nếu sai, chạy:           ROLLBACK;
