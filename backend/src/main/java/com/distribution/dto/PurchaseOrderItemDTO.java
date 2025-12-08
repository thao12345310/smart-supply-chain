package com.distribution.dto;

import java.math.BigDecimal;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String unit;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal costBeforeTax;
}
