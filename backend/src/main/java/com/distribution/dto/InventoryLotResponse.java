package com.distribution.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLotResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productCode;
    private Long warehouseId;
    private String warehouseName;
    private String lotNumber;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private BigDecimal quantityReceived;
    private BigDecimal quantityRemaining;
    // null nếu expiryDate null; âm nếu đã hết hạn
    private Long daysUntilExpiry;
    // FRESH | EXPIRING_SOON (≤30 ngày) | EXPIRED
    private String status;
}
