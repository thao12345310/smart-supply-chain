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

    // ==================== Per-Cluster Dashboards ====================

    @Getter @Setter @Builder
    public static class ClusterChartPoint {
        private String label;
        private java.math.BigDecimal value;
    }

    @Getter @Setter @Builder
    public static class PurchaseDashboard {
        private long totalPO;
        private long pendingApproval;
        private long pendingReceipt;
        private java.math.BigDecimal purchaseValueThisMonth;
        private java.util.List<ClusterChartPoint> poByStatus;     // label=status name, value=count
        private java.util.List<ClusterChartPoint> topSuppliers;   // label=supplier name, value=amount
    }

    @Getter @Setter @Builder
    public static class SalesDashboard {
        private long totalSO;
        private java.math.BigDecimal revenueThisMonth;
        private java.util.List<ClusterChartPoint> soByStatus;
        private java.util.List<ClusterChartPoint> topCustomers;
    }

    @Getter @Setter @Builder
    public static class InventoryDashboard {
        private java.math.BigDecimal totalStockValue;
        private long lowStockCount;
        private long expiringSoonCount;
        private long expiredCount;
        private java.util.List<ClusterChartPoint> stockByWarehouse;
    }

    @Getter @Setter @Builder
    public static class DeliveryDashboard {
        private long totalTrips;
        private long completedTrips;
        private double successRate;
        private java.util.List<ClusterChartPoint> tripsByStatus;
        private java.util.List<ClusterChartPoint> ordersByShipper;
    }

    @Getter @Setter @Builder
    public static class AccountingDashboard {
        private java.math.BigDecimal totalReceivable;
        private java.math.BigDecimal totalPayable;
        private java.math.BigDecimal cashIn;
        private java.math.BigDecimal cashOut;
        private long overdueInvoices;
        private java.util.List<ClusterChartPoint> cashFlowByMonth;
    }
}
