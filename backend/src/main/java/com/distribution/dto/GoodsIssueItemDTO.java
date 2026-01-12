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
public class GoodsIssueItemDTO {
    
    private Long id;
    
    private Long goodsIssueId;
    
    @NotNull(message = "Sales Order Item is required")
    private Long salesOrderItemId;
    
    @NotNull(message = "Product is required")
    private Long productId;
    private String productCode;
    private String productName;
    
    @NotNull(message = "Ordered quantity is required")
    private Integer orderedQuantity;
    
    @NotNull(message = "Issued quantity is required")
    @Min(value = 1, message = "Issued quantity must be at least 1")
    private Integer issuedQuantity;
    
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    
    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;
    
    @Size(max = 100, message = "Batch number must not exceed 100 characters")
    private String batchNumber;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
    
    // Additional info for UI
    private Integer previouslyDeliveredQuantity;
    private Integer remainingQuantity;
    private Integer maxAllowedQuantity;
}
