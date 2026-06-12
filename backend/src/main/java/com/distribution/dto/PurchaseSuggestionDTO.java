package com.distribution.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Đề xuất mua hàng: các mặt hàng dưới mức cảnh báo / hết hàng,
 * gom nhóm theo nhà cung cấp mặc định của sản phẩm.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseSuggestionDTO {

    /** null nếu sản phẩm chưa gán nhà cung cấp */
    private Long supplierId;
    private String supplierName;

    private Integer itemCount;
    private BigDecimal totalEstimated;

    private List<Item> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        private Long productId;
        private String productCode;
        private String productName;

        private Long warehouseId;
        private String warehouseName;

        private Integer quantityOnHand;
        /** Khả dụng thực = tồn - giữ chỗ - hết hạn chờ hủy */
        private Integer quantityAvailable;
        private Integer quantityExpired;

        private Integer reorderLevel;
        private Integer reorderQuantity;
        private Integer suggestedQuantity;

        private BigDecimal unitPrice;
        private BigDecimal estimatedAmount;
    }
}
