package com.distribution.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesInvoiceItemDTO {
    
    private Long id;
    
    private Long salesInvoiceId;
    private Long goodsIssueItemId;
    
    @NotNull(message = "Product is required")
    private Long productId;
    private String productCode;
    private String productName;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;
    
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
}
