package com.distribution.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryDTO {
    
    private Long id;
    
    private Long productId;
    private String productName;
    private String productCode;
    
    private Long warehouseId;
    private String warehouseName;
    private String warehouseCode;
    
    private Integer quantityOnHand;
    private Integer quantityReserved;
    private Integer quantityAvailable;
    
    private Integer reorderLevel;
    private Integer reorderQuantity;
    
    private BigDecimal averageCost;
    private BigDecimal totalValue;
    
    private LocalDateTime lastReceivedDate;
    private LocalDateTime lastIssuedDate;
    
    private Boolean needsReorder;
    private Boolean lowStock;
    
    /**
     * Compute calculated fields
     */
    public void computeFields() {
        this.quantityAvailable = (quantityOnHand != null ? quantityOnHand : 0) 
            - (quantityReserved != null ? quantityReserved : 0);
        
        if (averageCost != null && quantityOnHand != null) {
            this.totalValue = averageCost.multiply(BigDecimal.valueOf(quantityOnHand));
        }
        
        this.needsReorder = reorderLevel != null && quantityAvailable != null 
            && quantityAvailable <= reorderLevel;
        
        this.lowStock = quantityAvailable != null && quantityAvailable <= 10;
    }
}
