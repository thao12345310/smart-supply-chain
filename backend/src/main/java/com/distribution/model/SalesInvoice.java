package com.distribution.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.distribution.model.enums.SalesInvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sales Invoice Entity
 * 
 * Business Rules:
 * - Invoice is created after Goods Issue confirmation
 * - One Goods Issue = One Invoice
 * - Invoice tracks payment status
 */
@Entity
@Table(name = "sales_invoice", indexes = {
    @Index(name = "idx_si_code", columnList = "code"),
    @Index(name = "idx_si_status", columnList = "status"),
    @Index(name = "idx_si_sales_order", columnList = "sales_order_id"),
    @Index(name = "idx_si_goods_issue", columnList = "goods_issue_id"),
    @Index(name = "idx_si_customer", columnList = "customer_id"),
    @Index(name = "idx_si_invoice_date", columnList = "invoice_date"),
    @Index(name = "idx_si_due_date", columnList = "due_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SalesInvoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SalesInvoiceStatus status;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "issued_date")
    private LocalDateTime issuedDate;

    @Column(name = "paid_date")
    private LocalDateTime paidDate;

    @Column(precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "shipping_cost", precision = 15, scale = 2)
    private BigDecimal shippingCost;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", precision = 15, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "remaining_amount", precision = 15, scale = 2)
    private BigDecimal remainingAmount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(length = 500)
    private String notes;

    // Audit fields
    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "issued_by")
    private Long issuedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "items", "goodsIssues", "invoices"})
    private SalesOrder salesOrder;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_issue_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "items", "invoice"})
    private GoodsIssue goodsIssue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "salesOrders", "deliveryAddresses"})
    private Customer customer;

    @OneToMany(mappedBy = "salesInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesInvoiceItem> items = new ArrayList<>();

    // Helper methods
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = SalesInvoiceStatus.DRAFT;
        }
        if (invoiceDate == null) {
            invoiceDate = LocalDate.now();
        }
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
        updatedAt = LocalDateTime.now();
        calculateRemainingAmount();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateRemainingAmount();
    }

    /**
     * Calculate remaining amount
     */
    public void calculateRemainingAmount() {
        BigDecimal total = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        BigDecimal paid = paidAmount != null ? paidAmount : BigDecimal.ZERO;
        this.remainingAmount = total.subtract(paid);
    }

    /**
     * Calculate total from items
     */
    public void calculateTotal() {
        this.subtotal = items.stream()
            .map(SalesInvoiceItem::getTotalAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        BigDecimal shipping = shippingCost != null ? shippingCost : BigDecimal.ZERO;
        BigDecimal tax = taxAmount != null ? taxAmount : BigDecimal.ZERO;

        this.totalAmount = subtotal.add(tax).subtract(discount).add(shipping);
        calculateRemainingAmount();
    }

    /**
     * Add item to invoice
     */
    public void addItem(SalesInvoiceItem item) {
        items.add(item);
        item.setSalesInvoice(this);
        calculateTotal();
    }

    /**
     * Check if invoice is fully paid
     */
    public boolean isFullyPaid() {
        return remainingAmount != null && remainingAmount.compareTo(BigDecimal.ZERO) <= 0;
    }

    /**
     * Check if invoice is overdue
     */
    public boolean isOverdue() {
        return dueDate != null &&
               LocalDate.now().isAfter(dueDate) &&
               (status == SalesInvoiceStatus.ISSUED || status == SalesInvoiceStatus.PARTIALLY_PAID
                || status == SalesInvoiceStatus.OVERDUE);
    }

    /**
     * Generate invoice code
     */
    public static String generateCode() {
        return "INV-" + System.currentTimeMillis();
    }
}
