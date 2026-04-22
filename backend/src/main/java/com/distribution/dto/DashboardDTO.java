package com.distribution.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Dashboard overview DTOs for Reporting module (Phân hệ 5)
 */
public class DashboardDTO {

    // ==================== Summary Cards ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OverviewSummary {
        // Revenue
        private BigDecimal totalRevenue;
        private BigDecimal totalRevenueThisMonth;
        private BigDecimal totalRevenueLastMonth;
        private Double revenueGrowthPercent;

        // Orders
        private Long totalSalesOrders;
        private Long pendingSalesOrders;
        private Long totalPurchaseOrders;
        private Long pendingPurchaseOrders;

        // Inventory
        private Long totalProducts;
        private Long lowStockItems;
        private Long outOfStockItems;
        private BigDecimal totalInventoryValue;

        // Receivables (Công nợ)
        private BigDecimal totalReceivables;
        private BigDecimal totalOverdueReceivables;
        private Long overdueInvoiceCount;

        // Delivery
        private Long pendingDeliveries;
        private Long completedDeliveriesToday;
    }

    // ==================== Revenue Chart ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RevenueChartData {
        private List<RevenuePoint> data;
        private BigDecimal total;
        private String period; // "daily", "monthly"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RevenuePoint {
        private String label; // e.g., "2024-01", "2024-01-15"
        private BigDecimal revenue;
        private BigDecimal cost;
        private Long orderCount;
    }

    // ==================== Inventory Report ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventoryReportDTO {
        private Long productId;
        private String productCode;
        private String productName;
        private String category;
        private String unit;

        // Stock levels
        private Integer openingStock;
        private Integer totalReceived;
        private Integer totalIssued;
        private Integer adjustments;
        private Integer closingStock;

        // Value
        private BigDecimal averageCost;
        private BigDecimal closingValue;

        // Locations
        private List<WarehouseStock> warehouseBreakdown;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WarehouseStock {
        private Long warehouseId;
        private String warehouseName;
        private Integer quantity;
        private Integer reserved;
        private Integer available;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventoryReportSummary {
        private BigDecimal totalInventoryValue;
        private Long totalProducts;
        private Long lowStockCount;
        private Long outOfStockCount;
        private Integer totalQuantityOnHand;
        private List<InventoryReportDTO> items;
    }

    // ==================== Receivables Report (Công nợ) ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReceivableDTO {
        private Long customerId;
        private String customerCode;
        private String customerName;
        private String phone;

        private BigDecimal totalInvoiceAmount;
        private BigDecimal totalPaidAmount;
        private BigDecimal outstandingAmount;

        private Long totalInvoices;
        private Long overdueInvoices;
        private Integer maxOverdueDays;

        private List<InvoiceSummary> invoices;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvoiceSummary {
        private Long invoiceId;
        private String invoiceCode;
        private String salesOrderCode;
        private LocalDate invoiceDate;
        private LocalDate dueDate;
        private String status;
        private BigDecimal totalAmount;
        private BigDecimal paidAmount;
        private BigDecimal remainingAmount;
        private Integer overdueDays;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReceivablesReportSummary {
        private BigDecimal totalOutstanding;
        private BigDecimal totalOverdue;
        private Long totalCustomersWithDebt;
        private Long totalOverdueInvoices;
        private List<ReceivableDTO> customers;
    }

    // ==================== Top Products ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopProductDTO {
        private Long productId;
        private String productCode;
        private String productName;
        private Integer totalQuantitySold;
        private BigDecimal totalRevenue;
    }
}
