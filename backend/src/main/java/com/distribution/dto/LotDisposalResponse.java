package com.distribution.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotDisposalResponse {
    private Long id;
    private String code;
    private Long lotId;
    private String lotNumber;
    private Long productId;
    private String productName;
    private String productCode;
    private Long warehouseId;
    private String warehouseName;
    private LocalDate expiryDate;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private String reason;
    private Long disposedBy;
    private String disposedByName;
    private LocalDateTime disposedAt;
}
