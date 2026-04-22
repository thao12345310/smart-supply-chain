package com.distribution.controller;

import com.distribution.dto.ApiResponse;
import com.distribution.dto.DashboardDTO;
import com.distribution.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Dashboard & Reporting (Phân hệ 5)
 * 
 * Provides aggregated dashboards, revenue charts,
 * inventory stock reports, and receivables reports.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class DashboardController {

    private final DashboardService dashboardService;

    // ==================== Overview ====================

    /**
     * Dashboard Summary - Overview metrics: revenue, orders, inventory, receivables
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardDTO.OverviewSummary>> getSummary() {
        DashboardDTO.OverviewSummary summary = dashboardService.getOverviewSummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Dashboard summary loaded"));
    }

    // ==================== Revenue Chart ====================

    /**
     * Revenue Chart Data - Revenue and order counts grouped by day or month
     */
    @GetMapping("/revenue-chart")
    public ResponseEntity<ApiResponse<DashboardDTO.RevenueChartData>> getRevenueChart(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "monthly") String groupBy) {
        DashboardDTO.RevenueChartData chart = dashboardService.getRevenueChart(startDate, endDate, groupBy);
        return ResponseEntity.ok(ApiResponse.success(chart, "Revenue chart data loaded"));
    }

    // ==================== Inventory Stock Report ====================

    /**
     * Inventory Stock Report (Nhập-Xuất-Tồn)
     * Opening stock, received, issued, and closing stock for each product
     */
    @GetMapping("/inventory-report")
    public ResponseEntity<ApiResponse<DashboardDTO.InventoryReportSummary>> getInventoryReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long warehouseId) {
        DashboardDTO.InventoryReportSummary report = dashboardService.getInventoryReport(startDate, endDate, warehouseId);
        return ResponseEntity.ok(ApiResponse.success(report, "Inventory report generated"));
    }

    // ==================== Receivables Report ====================

    /**
     * Receivables Report (Công nợ)
     * Customer receivables and overdue invoice summary
     */
    @GetMapping("/receivables-report")
    public ResponseEntity<ApiResponse<DashboardDTO.ReceivablesReportSummary>> getReceivablesReport(
            @RequestParam(defaultValue = "false") boolean overdueOnly) {
        DashboardDTO.ReceivablesReportSummary report = dashboardService.getReceivablesReport(overdueOnly);
        return ResponseEntity.ok(ApiResponse.success(report, "Receivables report generated"));
    }

    // ==================== Top Selling Products ====================

    /**
     * Top Selling Products - Products ranked by revenue within a given date range
     */
    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse<List<DashboardDTO.TopProductDTO>>> getTopProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        List<DashboardDTO.TopProductDTO> topProducts = dashboardService.getTopSellingProducts(startDate, endDate, limit);
        return ResponseEntity.ok(ApiResponse.success(topProducts, "Top products loaded"));
    }
}
