package com.distribution.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inventory Entity - Tracks product stock levels at warehouse level
 * 
 * Business Rules:
 * - Stock is updated when Goods Receipt is confirmed
 * - Supports multiple warehouses per product
 * - Tracks both available and reserved quantities
 */
@Entity
@Table(name = "inventory", uniqueConstraints = @UniqueConstraint(columnNames = { "product_id",
        "warehouse_id" }), indexes = {
                @Index(name = "idx_inv_product", columnList = "product_id"),
                @Index(name = "idx_inv_warehouse", columnList = "warehouse_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Warehouse warehouse;

    @Column(name = "quantity_on_hand", nullable = false)
    @Builder.Default
    private Integer quantityOnHand = 0;

    @Column(name = "quantity_reserved", nullable = false)
    @Builder.Default
    private Integer quantityReserved = 0;

    @Column(name = "quantity_available", nullable = false)
    @Builder.Default
    private Integer quantityAvailable = 0;

    @Column(name = "reorder_level")
    private Integer reorderLevel;

    @Column(name = "reorder_quantity")
    private Integer reorderQuantity;

    @Column(name = "average_cost", precision = 15, scale = 2)
    private BigDecimal averageCost;

    @Column(name = "last_received_date")
    private LocalDateTime lastReceivedDate;

    @Column(name = "last_issued_date")
    private LocalDateTime lastIssuedDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version = 0L; // Optimistic locking for concurrent updates

    // Helper methods
    @PrePersist
    @PreUpdate
    protected void onSave() {
        updatedAt = LocalDateTime.now();
        calculateAvailable();
    }

    /**
     * Calculate available quantity
     */
    public void calculateAvailable() {
        this.quantityAvailable = (quantityOnHand != null ? quantityOnHand : 0)
                - (quantityReserved != null ? quantityReserved : 0);
    }

    /**
     * Add stock (e.g., from goods receipt)
     */
    public void addStock(int quantity, BigDecimal unitCost) {
        // Update weighted average cost
        if (unitCost != null && quantity > 0) {
            if (this.averageCost == null || this.quantityOnHand == null || this.quantityOnHand == 0) {
                this.averageCost = unitCost;
            } else {
                BigDecimal totalValue = this.averageCost.multiply(BigDecimal.valueOf(this.quantityOnHand))
                        .add(unitCost.multiply(BigDecimal.valueOf(quantity)));
                int newQuantity = this.quantityOnHand + quantity;
                this.averageCost = totalValue.divide(BigDecimal.valueOf(newQuantity), 2,
                        java.math.RoundingMode.HALF_UP);
            }
        }

        this.quantityOnHand = (this.quantityOnHand != null ? this.quantityOnHand : 0) + quantity;
        this.lastReceivedDate = LocalDateTime.now();
        calculateAvailable();
    }

    /**
     * Remove stock (e.g., for sales or transfers)
     * 
     * @throws IllegalArgumentException if insufficient available quantity
     */
    public void removeStock(int quantity) {
        if (quantity > this.quantityAvailable) {
            throw new IllegalArgumentException(
                    String.format("Insufficient stock. Available: %d, Requested: %d",
                            this.quantityAvailable, quantity));
        }
        this.quantityOnHand = this.quantityOnHand - quantity;
        this.lastIssuedDate = LocalDateTime.now();
        calculateAvailable();
    }

    /**
     * Reserve stock for pending orders
     */
    public void reserveStock(int quantity) {
        if (quantity > this.quantityAvailable) {
            throw new IllegalArgumentException(
                    String.format("Insufficient available stock to reserve. Available: %d, Requested: %d",
                            this.quantityAvailable, quantity));
        }
        this.quantityReserved = (this.quantityReserved != null ? this.quantityReserved : 0) + quantity;
        calculateAvailable();
    }

    /**
     * Release reserved stock
     */
    public void releaseReservedStock(int quantity) {
        this.quantityReserved = Math.max(0, (this.quantityReserved != null ? this.quantityReserved : 0) - quantity);
        calculateAvailable();
    }

    /**
     * Check if reorder is needed
     */
    public boolean needsReorder() {
        return reorderLevel != null && quantityAvailable != null && quantityAvailable <= reorderLevel;
    }
}
