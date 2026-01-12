package com.distribution.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderItemDTO {
    
    private Long id;
    
    private Long salesOrderId;
    
    @NotNull(message = "Product is required")
    private Long productId;
    private String productCode;
    private String productName;
    
    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    private Integer deliveredQuantity;
    private Integer remainingQuantity;
    
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be positive")
    private BigDecimal unitPrice;
    
    @DecimalMin(value = "0.00", message = "Discount percent must be positive")
    @DecimalMax(value = "100.00", message = "Discount percent cannot exceed 100")
    private BigDecimal discountPercent;
    
    @DecimalMin(value = "0.00", message = "Tax percent must be positive")
    private BigDecimal taxPercent;
    
    private BigDecimal amountBeforeTax;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
    
    // Computed fields
    private Boolean fullyDelivered;
    
    // Inventory info
    private Integer availableQuantity;
    
    /**
     * Compute fields
     */
    public void computeFields() {
        int delivered = deliveredQuantity != null ? deliveredQuantity : 0;
        this.remainingQuantity = quantity != null ? quantity - delivered : 0;
        this.fullyDelivered = remainingQuantity <= 0;
    }
}
