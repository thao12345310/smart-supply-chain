package com.distribution.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderDTO {
    private Long id;
    private String code;
    private LocalDateTime orderDate;
    private Long supplierId;
    private String supplierName;
    private String status;
    private List<PurchaseOrderItemDTO> items;
}
