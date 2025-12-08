package com.distribution.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderDTO {
    private Long id;
    private String code;
    private String orderName;   
    private LocalDateTime afterDate;
    private LocalDateTime beforeDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime deliveryDate;
    
    private Long supplierId;
    private String supplierName;
    private Long warehouseId;
    private String warehouseName;
    private String status;
    private String taxType;
    private BigDecimal shippingCost;
    private List<PurchaseOrderItemDTO> items;
}
