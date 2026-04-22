package com.distribution.controller;

import com.distribution.dto.ApiResponse;
import com.distribution.dto.DashboardDTO;
import com.distribution.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Dashboard & Reporting", description = "Dashboard overview, revenue charts, stock reports, receivables")
public class DashboardController {

    private final DashboardService dashboardService;

    // ==================== Overview ====================

    @GetMapping("/summary")
    @Operation(summary = "Dashboard Summary", 
               description = "Get overview metrics: revenue, orders, inventory, receivables")
    public ResponseEntity<ApiResponse<DashboardDTO.OverviewSummary>> getSummary() {
        DashboardDTO.OverviewSummary summary = dashboardService.getOverviewSummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Dashboard summary loaded"));
    }

    // ==================== Revenue Chart ====================

    @GetMapping("/revenue-chart")
    @Operation(summary = "Revenue Chart Data", 
               description = "Revenue and order counts grouped by day or month")
    public ResponseEntity<ApiResponse<DashboardDTO.RevenueChartData>> getRevenueChart(
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Group by: daily or monthly")
            @RequestParam(defaultValue = "monthly") String groupBy) {
        DashboardDTO.RevenueChartData chart = dashboardService.getRevenueChart(startDate, endDate, groupBy);
        return ResponseEntity.ok(ApiResponse.success(chart, "Revenue chart data loaded"));
    }

    // ==================== Inventory Stock Report ====================

    @GetMapping("/inventory-report")
    @Operation(summary = "Inventory Stock Report (Nhập-Xuất-Tồn)", 
               description = "Opening stock, received, issued, and closing stock for each product")
    public ResponseEntity<ApiResponse<DashboardDTO.InventoryReportSummary>> getInventoryReport(
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Warehouse ID (optional, null = all)")
            @RequestParam(required = false) Long warehouseId) {
        DashboardDTO.InventoryReportSummary report = dashboardService.getInventoryReport(startDate, endDate, warehouseId);
        return ResponseEntity.ok(ApiResponse.success(report, "Inventory report generated"));
    }

    // ==================== Receivables Report ====================

    @GetMapping("/receivables-report")
    @Operation(summary = "Receivables Report (Công nợ)", 
               description = "Customer receivables and overdue invoice summary")
    public ResponseEntity<ApiResponse<DashboardDTO.ReceivablesReportSummary>> getReceivablesReport(
            @Parameter(description = "True = only overdue customers, False = all customers with outstanding invoices")
            @RequestParam(defaultValue = "false") boolean overdueOnly) {
        DashboardDTO.ReceivablesReportSummary report = dashboardService.getReceivablesReport(overdueOnly);
        return ResponseEntity.ok(ApiResponse.success(report, "Receivables report generated"));
    }

    // ==================== Top Selling Products ====================

    @GetMapping("/top-products")
    @Operation(summary = "Top Selling Products", 
               description = "Products ranked by revenue within a given date range")
    public ResponseEntity<ApiResponse<List<DashboardDTO.TopProductDTO>>> getTopProducts(
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Max number of products to return")
            @RequestParam(defaultValue = "10") int limit) {
        List<DashboardDTO.TopProductDTO> topProducts = dashboardService.getTopSellingProducts(startDate, endDate, limit);
        return ResponseEntity.ok(ApiResponse.success(topProducts, "Top products loaded"));
    }
}
