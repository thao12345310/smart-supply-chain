package com.distribution.controller;

import com.distribution.dto.ApiResponse;
import com.distribution.dto.GoodsReceiptDTO;
import com.distribution.model.enums.GoodsReceiptStatus;
import com.distribution.service.GoodsReceiptService;
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
 * REST Controller for Goods Receipt operations
 * 
 * Endpoints:
 * - CRUD operations for Goods Receipts
 * - Confirmation workflow (updates inventory)
 * - Receiving summary for POs
 * 
 * Role Requirements (to be enforced by security layer):
 * - All operations: ROLE_WAREHOUSE_STAFF, ROLE_ADMIN
 */
@RestController
@RequestMapping("/api/goods-receipts")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Tag(name = "Goods Receipt", description = "Goods Receipt Management APIs")
public class GoodsReceiptController {

    private final GoodsReceiptService goodsReceiptService;

    // ==================== CRUD Operations ====================

    @GetMapping
    @Operation(summary = "Get all Goods Receipts", description = "Retrieve all goods receipts")
    public ResponseEntity<ApiResponse<List<GoodsReceiptDTO>>> getAll() {
        List<GoodsReceiptDTO> receipts = goodsReceiptService.getAll();
        return ResponseEntity.ok(ApiResponse.success(receipts, "Retrieved " + receipts.size() + " goods receipts"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Goods Receipt by ID", description = "Retrieve a specific goods receipt with all items")
    public ResponseEntity<ApiResponse<GoodsReceiptDTO>> getById(
            @Parameter(description = "Goods Receipt ID") @PathVariable Long id) {
        GoodsReceiptDTO receipt = goodsReceiptService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(receipt));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get Goods Receipt by Code", description = "Retrieve a goods receipt by its code")
    public ResponseEntity<ApiResponse<GoodsReceiptDTO>> getByCode(
            @Parameter(description = "Goods Receipt Code") @PathVariable String code) {
        GoodsReceiptDTO receipt = goodsReceiptService.getByCode(code);
        return ResponseEntity.ok(ApiResponse.success(receipt));
    }

    @PostMapping
    @Operation(summary = "Create Goods Receipt", 
               description = "Create a new goods receipt for an approved PO. Initial status will be DRAFT")
    public ResponseEntity<ApiResponse<GoodsReceiptDTO>> create(
            @Valid @RequestBody GoodsReceiptDTO dto) {
        GoodsReceiptDTO created = goodsReceiptService.create(dto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(created, "Goods Receipt created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Goods Receipt", 
               description = "Update an existing goods receipt. Only allowed when status is DRAFT")
    public ResponseEntity<ApiResponse<GoodsReceiptDTO>> update(
            @Parameter(description = "Goods Receipt ID") @PathVariable Long id,
            @Valid @RequestBody GoodsReceiptDTO dto) {
        GoodsReceiptDTO updated = goodsReceiptService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Goods Receipt updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Goods Receipt", 
               description = "Delete a goods receipt. Only allowed when status is DRAFT")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Goods Receipt ID") @PathVariable Long id) {
        goodsReceiptService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Goods Receipt deleted successfully"));
    }

    // ==================== Confirmation Workflow ====================

    @PutMapping("/{id}/confirm")
    @Operation(summary = "Confirm Goods Receipt", 
               description = "Confirm a goods receipt. This will update PO received quantities and inventory levels")
    public ResponseEntity<ApiResponse<GoodsReceiptDTO>> confirm(
            @Parameter(description = "Goods Receipt ID") @PathVariable Long id,
            @RequestParam(required = false) Long confirmedBy) {
        GoodsReceiptDTO result = goodsReceiptService.confirm(id, confirmedBy);
        return ResponseEntity.ok(ApiResponse.success(result, 
            "Goods Receipt confirmed successfully. Inventory has been updated."));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel Goods Receipt", 
               description = "Cancel a goods receipt. Only allowed for DRAFT status")
    public ResponseEntity<ApiResponse<GoodsReceiptDTO>> cancel(
            @Parameter(description = "Goods Receipt ID") @PathVariable Long id) {
        GoodsReceiptDTO result = goodsReceiptService.cancel(id);
        return ResponseEntity.ok(ApiResponse.success(result, "Goods Receipt cancelled"));
    }

    // ==================== Query Operations ====================

    @GetMapping("/status/{status}")
    @Operation(summary = "Get Goods Receipts by Status", 
               description = "Retrieve all goods receipts with the specified status")
    public ResponseEntity<ApiResponse<List<GoodsReceiptDTO>>> getByStatus(
            @Parameter(description = "Goods Receipt Status") @PathVariable GoodsReceiptStatus status) {
        List<GoodsReceiptDTO> receipts = goodsReceiptService.getByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(receipts, "Retrieved " + receipts.size() + " goods receipts"));
    }

    @GetMapping("/purchase-order/{poId}")
    @Operation(summary = "Get Goods Receipts by Purchase Order", 
               description = "Retrieve all goods receipts for a specific purchase order")
    public ResponseEntity<ApiResponse<List<GoodsReceiptDTO>>> getByPurchaseOrderId(
            @Parameter(description = "Purchase Order ID") @PathVariable Long poId) {
        List<GoodsReceiptDTO> receipts = goodsReceiptService.getByPurchaseOrderId(poId);
        return ResponseEntity.ok(ApiResponse.success(receipts, "Retrieved " + receipts.size() + " goods receipts"));
    }

    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Get Goods Receipts by Warehouse", 
               description = "Retrieve all goods receipts for a specific warehouse")
    public ResponseEntity<ApiResponse<List<GoodsReceiptDTO>>> getByWarehouseId(
            @Parameter(description = "Warehouse ID") @PathVariable Long warehouseId) {
        List<GoodsReceiptDTO> receipts = goodsReceiptService.getByWarehouseId(warehouseId);
        return ResponseEntity.ok(ApiResponse.success(receipts, "Retrieved " + receipts.size() + " goods receipts"));
    }

    @GetMapping("/pending-confirmation")
    @Operation(summary = "Get Pending Confirmation", 
               description = "Retrieve all goods receipts pending confirmation (DRAFT status)")
    public ResponseEntity<ApiResponse<List<GoodsReceiptDTO>>> getPendingConfirmation() {
        List<GoodsReceiptDTO> receipts = goodsReceiptService.getPendingConfirmation();
        return ResponseEntity.ok(ApiResponse.success(receipts, "Retrieved " + receipts.size() + " pending confirmations"));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get Goods Receipts by Date Range", 
               description = "Retrieve all goods receipts within the specified date range")
    public ResponseEntity<ApiResponse<List<GoodsReceiptDTO>>> getByDateRange(
            @Parameter(description = "Start date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<GoodsReceiptDTO> receipts = goodsReceiptService.getByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(receipts, "Retrieved " + receipts.size() + " goods receipts"));
    }

    // ==================== Receiving Helper ====================

    @GetMapping("/receiving-summary/{poId}")
    @Operation(summary = "Get Receiving Summary for PO", 
               description = "Get ordered vs received quantities for each item in a PO. " +
                            "Use this to prepare a new goods receipt")
    public ResponseEntity<ApiResponse<GoodsReceiptDTO>> getReceivingSummary(
            @Parameter(description = "Purchase Order ID") @PathVariable Long poId) {
        GoodsReceiptDTO summary = goodsReceiptService.getReceivingSummary(poId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Receiving summary generated"));
    }
}
