package com.distribution.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItemDTO {
    
    private Long id;
    
    @NotNull(message = "Product is required")
    private Long productId;
    private String productName;
    private String productCode;
    
    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    private Integer receivedQuantity;
    private Integer remainingQuantity;
    
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.00", message = "Unit price must be positive")
    private BigDecimal unitPrice;
    
    private BigDecimal costBeforeTax;
    private BigDecimal amountBeforeTax;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    
    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;
    
    // Computed fields
    private Boolean fullyReceived;
    private Boolean partiallyReceived;
    
    /**
     * Compute remaining quantity and receiving status
     */
    public void computeFields() {
        if (receivedQuantity == null) {
            receivedQuantity = 0;
        }
        this.remainingQuantity = (quantity != null ? quantity : 0) - receivedQuantity;
        this.fullyReceived = remainingQuantity <= 0;
        this.partiallyReceived = receivedQuantity > 0 && remainingQuantity > 0;
    }
}
