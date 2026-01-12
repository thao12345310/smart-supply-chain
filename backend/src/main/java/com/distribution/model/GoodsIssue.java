package com.distribution.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.distribution.model.enums.GoodsIssueStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Goods Issue (GI) Entity - Represents an outbound delivery for a Sales Order
 * 
 * Business Rules:
 * - One SO can have multiple Goods Issues (partial delivery)
 * - GI is created by Warehouse Staff
 * - Confirmation decreases inventory and updates SO delivered quantities
 * - Cannot issue more than ordered quantity minus already delivered
 * - Each GI generates an Invoice
 */
@Entity
@Table(name = "goods_issue", indexes = {
    @Index(name = "idx_gi_code", columnList = "code"),
    @Index(name = "idx_gi_status", columnList = "status"),
    @Index(name = "idx_gi_sales_order", columnList = "sales_order_id"),
    @Index(name = "idx_gi_issue_date", columnList = "issue_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GoodsIssue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GoodsIssueStatus status;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "confirmed_date")
    private LocalDateTime confirmedDate;

    @Column(name = "delivery_note_number", length = 100)
    private String deliveryNoteNumber;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "shipping_method", length = 100)
    private String shippingMethod;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "carrier_name", length = 100)
    private String carrierName;

    @Column(length = 500)
    private String notes;

    // Audit fields
    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "confirmed_by")
    private Long confirmedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "items", "goodsIssues", "invoices"})
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_address_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "customer"})
    private DeliveryAddress deliveryAddress;

    @OneToMany(mappedBy = "goodsIssue", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GoodsIssueItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "goodsIssue", cascade = CascadeType.ALL)
    private SalesInvoice invoice;

    // Helper methods
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = GoodsIssueStatus.DRAFT;
        }
        if (issueDate == null) {
            issueDate = LocalDate.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Add item to goods issue
     */
    public void addItem(GoodsIssueItem item) {
        items.add(item);
        item.setGoodsIssue(this);
        recalculateTotal();
    }

    /**
     * Remove item from goods issue
     */
    public void removeItem(GoodsIssueItem item) {
        items.remove(item);
        item.setGoodsIssue(null);
        recalculateTotal();
    }

    /**
     * Recalculate total amount from items
     */
    public void recalculateTotal() {
        this.totalAmount = items.stream()
            .map(GoodsIssueItem::getTotalAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Generate GI code
     */
    public static String generateCode() {
        return "GI-" + System.currentTimeMillis();
    }
}
