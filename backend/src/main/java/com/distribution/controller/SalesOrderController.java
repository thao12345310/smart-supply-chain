package com.distribution.controller;

import com.distribution.dto.ApiResponse;
import com.distribution.dto.ApprovalRequestDTO;
import com.distribution.dto.SalesOrderDTO;
import com.distribution.model.enums.SalesOrderStatus;
import com.distribution.service.SalesOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Sales Order operations
 * 
 * Endpoints:
 * - CRUD operations for Sales Orders
 * - Approval/Rejection workflow
 * - Status-based queries
 * 
 * Role Requirements (to be enforced by security layer):
 * - Create/Update/Delete: ROLE_SALES_STAFF, ROLE_SALES_MANAGER, ROLE_ADMIN
 * - Approve/Reject: ROLE_SALES_MANAGER, ROLE_ACCOUNTANT, ROLE_ADMIN
 * - View: All authenticated users
 */
@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Tag(name = "Sales Order", description = "Sales Order Management APIs")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    // ==================== CRUD Operations ====================

    @GetMapping
    @Operation(summary = "Get all Sales Orders", description = "Retrieve all sales orders")
    public ResponseEntity<ApiResponse<List<SalesOrderDTO>>> getAll() {
        List<SalesOrderDTO> orders = salesOrderService.getAll();
        return ResponseEntity.ok(ApiResponse.success(orders, "Retrieved " + orders.size() + " sales orders"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Sales Order by ID", description = "Retrieve a specific sales order with all items")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> getById(
            @Parameter(description = "Sales Order ID") @PathVariable Long id) {
        SalesOrderDTO order = salesOrderService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get Sales Order by Code", description = "Retrieve a sales order by its code")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> getByCode(
            @Parameter(description = "Sales Order Code") @PathVariable String code) {
        SalesOrderDTO order = salesOrderService.getByCode(code);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping
    @Operation(summary = "Create Sales Order", 
               description = "Create a new sales order. Initial status will be ORDER_OPEN")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> create(
            @Valid @RequestBody SalesOrderDTO dto) {
        SalesOrderDTO created = salesOrderService.create(dto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(created, "Sales Order created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Sales Order", 
               description = "Update an existing sales order. Only allowed when status is ORDER_OPEN")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> update(
            @Parameter(description = "Sales Order ID") @PathVariable Long id,
            @Valid @RequestBody SalesOrderDTO dto) {
        SalesOrderDTO updated = salesOrderService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Sales Order updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Sales Order", 
               description = "Delete a sales order. Only allowed when status is ORDER_OPEN")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Sales Order ID") @PathVariable Long id) {
        salesOrderService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Sales Order deleted successfully"));
    }

    // ==================== Approval Workflow ====================

    @PostMapping("/{id}/approval")
    @Operation(summary = "Process Approval", 
               description = "Approve or reject a sales order. Requires SALES_MANAGER or ACCOUNTANT role")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> processApproval(
            @Parameter(description = "Sales Order ID") @PathVariable Long id,
            @Valid @RequestBody ApprovalRequestDTO approvalRequest) {
        SalesOrderDTO result = salesOrderService.processApproval(id, approvalRequest);
        String message = approvalRequest.isApproval() 
            ? "Sales Order approved successfully" 
            : "Sales Order rejected";
        return ResponseEntity.ok(ApiResponse.success(result, message));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve Sales Order", 
               description = "Approve a sales order. Reserves inventory. Requires SALES_MANAGER or ACCOUNTANT role")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> approve(
            @Parameter(description = "Sales Order ID") @PathVariable Long id,
            @RequestParam(required = false) Long approvedBy) {
        SalesOrderDTO result = salesOrderService.approve(id, approvedBy);
        return ResponseEntity.ok(ApiResponse.success(result, "Sales Order approved successfully"));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject Sales Order", 
               description = "Reject a sales order. Requires SALES_MANAGER or ACCOUNTANT role")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> reject(
            @Parameter(description = "Sales Order ID") @PathVariable Long id,
            @RequestParam(required = false) Long rejectedBy,
            @RequestParam(required = false) String reason) {
        SalesOrderDTO result = salesOrderService.reject(id, rejectedBy, reason);
        return ResponseEntity.ok(ApiResponse.success(result, "Sales Order rejected"));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel Sales Order", 
               description = "Cancel a sales order. Only allowed for OPEN or APPROVED status")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> cancel(
            @Parameter(description = "Sales Order ID") @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        SalesOrderDTO result = salesOrderService.cancel(id, reason);
        return ResponseEntity.ok(ApiResponse.success(result, "Sales Order cancelled"));
    }

    // ==================== Query Operations ====================

    @GetMapping("/status/{status}")
    @Operation(summary = "Get Sales Orders by Status", 
               description = "Retrieve all sales orders with the specified status")
    public ResponseEntity<ApiResponse<List<SalesOrderDTO>>> getByStatus(
            @Parameter(description = "Sales Order Status") @PathVariable SalesOrderStatus status) {
        List<SalesOrderDTO> orders = salesOrderService.getByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(orders, "Retrieved " + orders.size() + " sales orders"));
    }

    @GetMapping("/pending-approval")
    @Operation(summary = "Get Pending Approval", 
               description = "Retrieve all sales orders pending approval (ORDER_OPEN status)")
    public ResponseEntity<ApiResponse<List<SalesOrderDTO>>> getPendingApproval() {
        List<SalesOrderDTO> orders = salesOrderService.getPendingApproval();
        return ResponseEntity.ok(ApiResponse.success(orders, "Retrieved " + orders.size() + " pending approvals"));
    }

    @GetMapping("/ready-for-issue")
    @Operation(summary = "Get Ready for Goods Issue", 
               description = "Retrieve all sales orders ready for goods issue (APPROVED or PARTIALLY_DELIVERED)")
    public ResponseEntity<ApiResponse<List<SalesOrderDTO>>> getReadyForGoodsIssue() {
        List<SalesOrderDTO> orders = salesOrderService.getReadyForGoodsIssue();
        return ResponseEntity.ok(ApiResponse.success(orders, "Retrieved " + orders.size() + " orders ready for issue"));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get Sales Orders by Customer", 
               description = "Retrieve all sales orders for a specific customer")
    public ResponseEntity<ApiResponse<List<SalesOrderDTO>>> getByCustomerId(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {
        List<SalesOrderDTO> orders = salesOrderService.getByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(orders, "Retrieved " + orders.size() + " sales orders"));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get Sales Orders by Date Range", 
               description = "Retrieve all sales orders created within the specified date range")
    public ResponseEntity<ApiResponse<List<SalesOrderDTO>>> getByDateRange(
            @Parameter(description = "Start date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<SalesOrderDTO> orders = salesOrderService.getByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(orders, "Retrieved " + orders.size() + " sales orders"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search Sales Orders", 
               description = "Search sales orders by code, name, or customer name")
    public ResponseEntity<ApiResponse<List<SalesOrderDTO>>> search(
            @Parameter(description = "Search query") @RequestParam String q) {
        List<SalesOrderDTO> orders = salesOrderService.search(q);
        return ResponseEntity.ok(ApiResponse.success(orders, "Found " + orders.size() + " sales orders"));
    }

    @GetMapping("/{id}/issue-summary")
    @Operation(summary = "Get Issue Summary", 
               description = "Get remaining quantities to issue for each item in the SO")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> getIssueSummary(
            @Parameter(description = "Sales Order ID") @PathVariable Long id) {
        SalesOrderDTO summary = salesOrderService.getIssueSummary(id);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
