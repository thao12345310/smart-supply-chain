package com.distribution.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.distribution.model.enums.SalesOrderStatus;
import com.distribution.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Sales Order Entity
 * 
 * Business Rules:
 * - SO is created by Sales Staff with status ORDER_OPEN
 * - Manager or Accountant can approve/reject SO
 * - Approved SO can have multiple Goods Issues (partial delivery)
 * - SO transitions to COMPLETED when all items are fully delivered
 * - Inventory is reserved when SO is approved
 * - Inventory is decreased only after Goods Issue confirmation
 */
@Entity
@Table(name = "sales_order", indexes = {
    @Index(name = "idx_so_code", columnList = "code"),
    @Index(name = "idx_so_status", columnList = "status"),
    @Index(name = "idx_so_customer", columnList = "customer_id"),
    @Index(name = "idx_so_order_date", columnList = "order_date"),
    @Index(name = "idx_so_payment_status", columnList = "payment_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SalesOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "order_name", length = 255)
    private String orderName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SalesOrderStatus status;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "shipping_cost", precision = 15, scale = 2)
    private BigDecimal shippingCost;

    @Column(name = "grand_total", precision = 15, scale = 2)
    private BigDecimal grandTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 50)
    private PaymentStatus paymentStatus;

    @Column(length = 500)
    private String notes;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    // Audit fields
    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "salesOrders", "deliveryAddresses"})
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_address_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "customer"})
    private DeliveryAddress deliveryAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Warehouse warehouse;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<SalesOrderItem> items = new HashSet<>();

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<GoodsIssue> goodsIssues = new HashSet<>();

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<SalesInvoice> invoices = new HashSet<>();

    // Helper methods
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = SalesOrderStatus.ORDER_OPEN;
        }
        if (orderDate == null) {
            orderDate = LocalDate.now();
        }
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.UNPAID;
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Add item to sales order
     */
    public void addItem(SalesOrderItem item) {
        items.add(item);
        item.setSalesOrder(this);
        recalculateTotal();
    }

    /**
     * Remove item from sales order
     */
    public void removeItem(SalesOrderItem item) {
        items.remove(item);
        item.setSalesOrder(null);
        recalculateTotal();
    }

    /**
     * Recalculate total amount from items
     */
    public void recalculateTotal() {
        // item.totalAmount đã bao gồm thuế (amount_before_tax + tax_amount)
        this.totalAmount = items.stream()
            .map(SalesOrderItem::getTotalAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.taxAmount = items.stream()
            .map(SalesOrderItem::getTaxAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        BigDecimal shipping = shippingCost != null ? shippingCost : BigDecimal.ZERO;
        BigDecimal total = totalAmount != null ? totalAmount : BigDecimal.ZERO;

        this.grandTotal = total.subtract(discount).add(shipping);
    }

    /**
     * Check if all items have been fully delivered
     */
    public boolean isFullyDelivered() {
        return items.stream().allMatch(item -> 
            item.getDeliveredQuantity() >= item.getQuantity()
        );
    }

    /**
     * Check if any items have been partially delivered
     */
    public boolean isPartiallyDelivered() {
        return items.stream().anyMatch(item -> 
            item.getDeliveredQuantity() > 0 && item.getDeliveredQuantity() < item.getQuantity()
        );
    }

    /**
     * Calculate total ordered quantity
     */
    public int getTotalOrderedQuantity() {
        return items.stream()
            .mapToInt(SalesOrderItem::getQuantity)
            .sum();
    }

    /**
     * Calculate total delivered quantity
     */
    public int getTotalDeliveredQuantity() {
        return items.stream()
            .mapToInt(SalesOrderItem::getDeliveredQuantity)
            .sum();
    }

    /**
     * Get remaining quantity to deliver
     */
    public int getRemainingQuantity() {
        return getTotalOrderedQuantity() - getTotalDeliveredQuantity();
    }

    /**
     * Generate SO code
     */
    public static String generateCode() {
        return "SO-" + System.currentTimeMillis();
    }
}
