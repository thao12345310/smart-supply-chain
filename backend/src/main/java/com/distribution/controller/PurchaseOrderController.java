package com.distribution.controller;

import com.distribution.dto.ApiResponse;
import com.distribution.dto.ApprovalRequestDTO;
import com.distribution.dto.PurchaseOrderDTO;
import com.distribution.model.enums.PurchaseOrderStatus;
import com.distribution.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Purchase Order operations
 * 
 * Endpoints:
 * - CRUD operations for Purchase Orders
 * - Approval/Rejection workflow
 * - Status-based queries
 * 
 * Role Requirements (to be enforced by security layer):
 * - Create/Update/Delete: ROLE_PURCHASING_STAFF, ROLE_PURCHASING_MANAGER, ROLE_ADMIN
 * - Approve/Reject: ROLE_PURCHASING_MANAGER, ROLE_ACCOUNTANT, ROLE_ADMIN
 * - View: All authenticated users
 */
@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Tag(name = "Purchase Order", description = "Purchase Order Management APIs")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    // ==================== CRUD Operations ====================

    @GetMapping
    @Operation(summary = "Get all Purchase Orders", description = "Retrieve all purchase orders")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDTO>>> getAll() {
        List<PurchaseOrderDTO> orders = purchaseOrderService.getAll();
        return ResponseEntity.ok(ApiResponse.success(orders, "Retrieved " + orders.size() + " purchase orders"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Purchase Order by ID", description = "Retrieve a specific purchase order with all items")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> getById(
            @Parameter(description = "Purchase Order ID") @PathVariable Long id) {
        PurchaseOrderDTO order = purchaseOrderService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get Purchase Order by Code", description = "Retrieve a purchase order by its code")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> getByCode(
            @Parameter(description = "Purchase Order Code") @PathVariable String code) {
        PurchaseOrderDTO order = purchaseOrderService.getByCode(code);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PURCHASE_STAFF', 'PURCHASE_MANAGER', 'ADMIN')")
    @Operation(summary = "Create Purchase Order", 
               description = "Create a new purchase order. Initial status will be ORDER_OPEN")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> create(
            @Valid @RequestBody PurchaseOrderDTO dto) {
        PurchaseOrderDTO created = purchaseOrderService.create(dto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(created, "Purchase Order created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PURCHASE_STAFF', 'PURCHASE_MANAGER', 'ADMIN')")
    @Operation(summary = "Update Purchase Order", 
               description = "Update an existing purchase order. Only allowed when status is ORDER_OPEN")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> update(
            @Parameter(description = "Purchase Order ID") @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderDTO dto) {
        PurchaseOrderDTO updated = purchaseOrderService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Purchase Order updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PURCHASE_MANAGER', 'ADMIN')")
    @Operation(summary = "Delete Purchase Order", 
               description = "Delete a purchase order. Only allowed when status is ORDER_OPEN")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Purchase Order ID") @PathVariable Long id) {
        purchaseOrderService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Purchase Order deleted successfully"));
    }

    // ==================== Approval Workflow ====================

    @PostMapping("/{id}/approval")
    @PreAuthorize("hasAnyRole('PURCHASE_MANAGER', 'ACCOUNTANT', 'ADMIN')")
    @Operation(summary = "Process Approval", 
               description = "Approve or reject a purchase order. Requires PURCHASING_MANAGER or ACCOUNTANT role")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> processApproval(
            @Parameter(description = "Purchase Order ID") @PathVariable Long id,
            @Valid @RequestBody ApprovalRequestDTO approvalRequest) {
        PurchaseOrderDTO result = purchaseOrderService.processApproval(id, approvalRequest);
        String message = approvalRequest.isApproval() 
            ? "Purchase Order approved successfully" 
            : "Purchase Order rejected";
        return ResponseEntity.ok(ApiResponse.success(result, message));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('PURCHASE_MANAGER', 'ACCOUNTANT', 'ADMIN')")
    @Operation(summary = "Approve Purchase Order", 
               description = "Approve a purchase order. Requires PURCHASING_MANAGER or ACCOUNTANT role")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> approve(
            @Parameter(description = "Purchase Order ID") @PathVariable Long id,
            @RequestParam(required = false) Long approvedBy) {
        PurchaseOrderDTO result = purchaseOrderService.approve(id, approvedBy);
        return ResponseEntity.ok(ApiResponse.success(result, "Purchase Order approved successfully"));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('PURCHASE_MANAGER', 'ACCOUNTANT', 'ADMIN')")
    @Operation(summary = "Reject Purchase Order", 
               description = "Reject a purchase order. Requires PURCHASING_MANAGER or ACCOUNTANT role")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> reject(
            @Parameter(description = "Purchase Order ID") @PathVariable Long id,
            @RequestParam(required = false) Long rejectedBy,
            @RequestParam(required = false) String reason) {
        PurchaseOrderDTO result = purchaseOrderService.reject(id, rejectedBy, reason);
        return ResponseEntity.ok(ApiResponse.success(result, "Purchase Order rejected"));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel Purchase Order", 
               description = "Cancel a purchase order. Only allowed for OPEN or APPROVED status")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> cancel(
            @Parameter(description = "Purchase Order ID") @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        PurchaseOrderDTO result = purchaseOrderService.cancel(id, reason);
        return ResponseEntity.ok(ApiResponse.success(result, "Purchase Order cancelled"));
    }

    // ==================== Query Operations ====================

    @GetMapping("/status/{status}")
    @Operation(summary = "Get Purchase Orders by Status", 
               description = "Retrieve all purchase orders with the specified status")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDTO>>> getByStatus(
            @Parameter(description = "Purchase Order Status") @PathVariable PurchaseOrderStatus status) {
        List<PurchaseOrderDTO> orders = purchaseOrderService.getByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(orders, "Retrieved " + orders.size() + " purchase orders"));
    }

    @GetMapping("/pending-approval")
    @Operation(summary = "Get Pending Approval", 
               description = "Retrieve all purchase orders pending approval (ORDER_OPEN status)")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDTO>>> getPendingApproval() {
        List<PurchaseOrderDTO> orders = purchaseOrderService.getPendingApproval();
        return ResponseEntity.ok(ApiResponse.success(orders, "Retrieved " + orders.size() + " pending approvals"));
    }

    @GetMapping("/ready-for-receipt")
    @Operation(summary = "Get Ready for Goods Receipt", 
               description = "Retrieve all purchase orders ready for goods receipt (APPROVED or PARTIALLY_RECEIVED)")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDTO>>> getReadyForGoodsReceipt() {
        List<PurchaseOrderDTO> orders = purchaseOrderService.getReadyForGoodsReceipt();
        return ResponseEntity.ok(ApiResponse.success(orders, "Retrieved " + orders.size() + " orders ready for receipt"));
    }

    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "Get Purchase Orders by Supplier", 
               description = "Retrieve all purchase orders for a specific supplier")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDTO>>> getBySupplierId(
            @Parameter(description = "Supplier ID") @PathVariable Long supplierId) {
        List<PurchaseOrderDTO> orders = purchaseOrderService.getBySupplierId(supplierId);
        return ResponseEntity.ok(ApiResponse.success(orders, "Retrieved " + orders.size() + " purchase orders"));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get Purchase Orders by Date Range", 
               description = "Retrieve all purchase orders created within the specified date range")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDTO>>> getByDateRange(
            @Parameter(description = "Start date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PurchaseOrderDTO> orders = purchaseOrderService.getByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(orders, "Retrieved " + orders.size() + " purchase orders"));
    }
}
