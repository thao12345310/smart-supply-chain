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

    // Chi tiết vận đơn (detail endpoint)
    private String recipientName;
    private String recipientPhone;
    private java.time.LocalDate plannedDate;
    private Long goodsIssueId;
    private String goodsIssueCode;
    private java.util.List<Item> items;

    @lombok.Getter
    @lombok.Setter
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class Item {
        private String productCode;
        private String productName;
        private Integer quantity;
        private String unit;
    }
}
