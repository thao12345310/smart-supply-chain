package com.distribution.dto;

import lombok.*;

/**
 * DTO for a "vận đơn" (delivery order) derived from a confirmed Goods Issue (phiếu xuất).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryOrderDTO {
    private Long id;
    private String code;
    private String status;
    private String customerName;
    private String deliveryAddress;
}
