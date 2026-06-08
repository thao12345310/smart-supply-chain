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
    private String salesOrderCode; // mã đơn bán hàng nguồn (để biết vận đơn thuộc đơn đã bán nào)
    private String status;
    private String customerName;
    private String deliveryAddress;
    private boolean assignedToTrip; // vận đơn đã được phân vào một chuyến trong đợt chưa
}
