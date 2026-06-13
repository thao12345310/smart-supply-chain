package com.distribution.service;

import com.distribution.dto.DashboardDTO;

import java.time.LocalDate;

/**
 * Service interface for Dashboard & Reporting operations (Phân hệ 5)
 * 
 * Provides aggregated data for:
 * - Dashboard overview (summary statistics)
 * - Revenue charts
 * - Inventory in-out-stock reports
 * - Overdue receivables reports
 * - Top selling products
 */
public interface DashboardService {

    /**
     * Get overview summary for dashboard cards
     * Includes revenue, order counts, inventory status, receivables
     */
    DashboardDTO.OverviewSummary getOverviewSummary();

    /**
     * Get revenue chart data for a date range
     * @param startDate start of range
     * @param endDate end of range
     * @param groupBy "daily" or "monthly"
     */
    DashboardDTO.RevenueChartData getRevenueChart(LocalDate startDate, LocalDate endDate, String groupBy);

    /**
     * Get inventory stock report (báo cáo nhập-xuất-tồn)
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @param warehouseId optional warehouse filter (null = all warehouses)
     */
    DashboardDTO.InventoryReportSummary getInventoryReport(LocalDate startDate, LocalDate endDate, Long warehouseId);

    /**
     * Get receivables report (báo cáo công nợ quá hạn)
     * @param overdueOnly if true, only show customers with overdue invoices
     */
    DashboardDTO.ReceivablesReportSummary getReceivablesReport(boolean overdueOnly);

    /**
     * Get top selling products
     * @param startDate start of period
     * @param endDate end of period
     * @param limit max products to return
     */
    java.util.List<DashboardDTO.TopProductDTO> getTopSellingProducts(LocalDate startDate, LocalDate endDate, int limit);

    // ==================== Per-Cluster Dashboards ====================

    DashboardDTO.PurchaseDashboard getPurchaseDashboard();
    DashboardDTO.SalesDashboard getSalesDashboard();
    DashboardDTO.InventoryDashboard getInventoryDashboard();
    DashboardDTO.DeliveryDashboard getDeliveryDashboard();
    DashboardDTO.AccountingDashboard getAccountingDashboard();
}
