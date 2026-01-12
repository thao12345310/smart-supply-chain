package com.distribution.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceiptItemDTO {
    
    private Long id;
    
    @NotNull(message = "Purchase Order Item is required")
    private Long purchaseOrderItemId;
    
    @NotNull(message = "Product is required")
    private Long productId;
    private String productName;
    private String productCode;
    
    private Integer orderedQuantity;
    private Integer previouslyReceivedQuantity;
    private Integer remainingQuantity;
    
    @NotNull(message = "Received quantity is required")
    @Min(value = 0, message = "Received quantity must be non-negative")
    private Integer receivedQuantity;
    
    private Integer acceptedQuantity;
    
    @Min(value = 0, message = "Rejected quantity must be non-negative")
    private Integer rejectedQuantity;
    
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    
    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;
    
    @Size(max = 100, message = "Batch number must not exceed 100 characters")
    private String batchNumber;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    
    @Size(max = 255, message = "Rejection reason must not exceed 255 characters")
    private String rejectionReason;
    
    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;
    
    /**
     * Compute fields before processing
     */
    public void computeFields() {
        if (rejectedQuantity == null) {
            rejectedQuantity = 0;
        }
        if (receivedQuantity != null) {
            this.acceptedQuantity = receivedQuantity - rejectedQuantity;
        }
        if (unitPrice != null && acceptedQuantity != null) {
            this.totalAmount = unitPrice.multiply(BigDecimal.valueOf(acceptedQuantity));
        }
    }
    
    /**
     * Validate that received quantity doesn't exceed remaining
     */
    public void validateQuantity() {
        if (remainingQuantity != null && receivedQuantity != null && receivedQuantity > remainingQuantity) {
            throw new IllegalArgumentException(
                String.format("Received quantity (%d) exceeds remaining quantity (%d) for product %s",
                    receivedQuantity, remainingQuantity, productName)
            );
        }
    }
}
