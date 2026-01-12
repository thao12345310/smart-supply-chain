package com.distribution.controller;

import com.distribution.dto.ApiResponse;
import com.distribution.dto.GoodsIssueDTO;
import com.distribution.model.enums.GoodsIssueStatus;
import com.distribution.service.GoodsIssueService;
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
 * REST Controller for Goods Issue (outbound delivery) operations
 * 
 * Role Requirements (to be enforced by security layer):
 * - Create/Update/Delete: ROLE_WAREHOUSE_STAFF, ROLE_ADMIN
 * - Confirm: ROLE_WAREHOUSE_STAFF, ROLE_ADMIN
 * - View: All authenticated users
 */
@RestController
@RequestMapping("/api/goods-issues")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Tag(name = "Goods Issue", description = "Goods Issue (Outbound Delivery) Management APIs")
public class GoodsIssueController {

    private final GoodsIssueService goodsIssueService;

    // ==================== CRUD Operations ====================

    @GetMapping
    @Operation(summary = "Get all Goods Issues", description = "Retrieve all goods issues")
    public ResponseEntity<ApiResponse<List<GoodsIssueDTO>>> getAll() {
        List<GoodsIssueDTO> issues = goodsIssueService.getAll();
        return ResponseEntity.ok(ApiResponse.success(issues, "Retrieved " + issues.size() + " goods issues"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Goods Issue by ID", description = "Retrieve a specific goods issue with all items")
    public ResponseEntity<ApiResponse<GoodsIssueDTO>> getById(
            @Parameter(description = "Goods Issue ID") @PathVariable Long id) {
        GoodsIssueDTO issue = goodsIssueService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(issue));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get Goods Issue by Code", description = "Retrieve a goods issue by its code")
    public ResponseEntity<ApiResponse<GoodsIssueDTO>> getByCode(
            @Parameter(description = "Goods Issue Code") @PathVariable String code) {
        GoodsIssueDTO issue = goodsIssueService.getByCode(code);
        return ResponseEntity.ok(ApiResponse.success(issue));
    }

    @PostMapping
    @Operation(summary = "Create Goods Issue", 
               description = "Create a new goods issue. Validates quantities don't exceed remaining SO quantities")
    public ResponseEntity<ApiResponse<GoodsIssueDTO>> create(
            @Valid @RequestBody GoodsIssueDTO dto) {
        GoodsIssueDTO created = goodsIssueService.create(dto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(created, "Goods Issue created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Goods Issue", 
               description = "Update an existing goods issue. Only allowed when status is DRAFT")
    public ResponseEntity<ApiResponse<GoodsIssueDTO>> update(
            @Parameter(description = "Goods Issue ID") @PathVariable Long id,
            @Valid @RequestBody GoodsIssueDTO dto) {
        GoodsIssueDTO updated = goodsIssueService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Goods Issue updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Goods Issue", 
               description = "Delete a goods issue. Only allowed when status is DRAFT")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Goods Issue ID") @PathVariable Long id) {
        goodsIssueService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Goods Issue deleted successfully"));
    }

    // ==================== Workflow Operations ====================

    @PutMapping("/{id}/confirm")
    @Operation(summary = "Confirm Goods Issue", 
               description = "Confirm goods issue. Decreases inventory, updates SO, and creates invoice")
    public ResponseEntity<ApiResponse<GoodsIssueDTO>> confirm(
            @Parameter(description = "Goods Issue ID") @PathVariable Long id,
            @RequestParam(required = false) Long confirmedBy) {
        GoodsIssueDTO result = goodsIssueService.confirm(id, confirmedBy);
        return ResponseEntity.ok(ApiResponse.success(result, "Goods Issue confirmed. Invoice created."));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel Goods Issue", 
               description = "Cancel a goods issue. Only allowed when status is DRAFT")
    public ResponseEntity<ApiResponse<GoodsIssueDTO>> cancel(
            @Parameter(description = "Goods Issue ID") @PathVariable Long id) {
        GoodsIssueDTO result = goodsIssueService.cancel(id);
        return ResponseEntity.ok(ApiResponse.success(result, "Goods Issue cancelled"));
    }

    // ==================== Query Operations ====================

    @GetMapping("/status/{status}")
    @Operation(summary = "Get Goods Issues by Status", 
               description = "Retrieve all goods issues with the specified status")
    public ResponseEntity<ApiResponse<List<GoodsIssueDTO>>> getByStatus(
            @Parameter(description = "Goods Issue Status") @PathVariable GoodsIssueStatus status) {
        List<GoodsIssueDTO> issues = goodsIssueService.getByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(issues, "Retrieved " + issues.size() + " goods issues"));
    }

    @GetMapping("/sales-order/{salesOrderId}")
    @Operation(summary = "Get Goods Issues by Sales Order", 
               description = "Retrieve all goods issues for a specific sales order")
    public ResponseEntity<ApiResponse<List<GoodsIssueDTO>>> getBySalesOrderId(
            @Parameter(description = "Sales Order ID") @PathVariable Long salesOrderId) {
        List<GoodsIssueDTO> issues = goodsIssueService.getBySalesOrderId(salesOrderId);
        return ResponseEntity.ok(ApiResponse.success(issues, "Retrieved " + issues.size() + " goods issues"));
    }

    @GetMapping("/draft")
    @Operation(summary = "Get Draft Goods Issues", 
               description = "Retrieve all goods issues in DRAFT status")
    public ResponseEntity<ApiResponse<List<GoodsIssueDTO>>> getDraft() {
        List<GoodsIssueDTO> issues = goodsIssueService.getDraft();
        return ResponseEntity.ok(ApiResponse.success(issues, "Retrieved " + issues.size() + " draft goods issues"));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get Goods Issues by Date Range", 
               description = "Retrieve all goods issues within the specified date range")
    public ResponseEntity<ApiResponse<List<GoodsIssueDTO>>> getByDateRange(
            @Parameter(description = "Start date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<GoodsIssueDTO> issues = goodsIssueService.getByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(issues, "Retrieved " + issues.size() + " goods issues"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search Goods Issues", 
               description = "Search goods issues by code, SO code, or tracking number")
    public ResponseEntity<ApiResponse<List<GoodsIssueDTO>>> search(
            @Parameter(description = "Search query") @RequestParam String q) {
        List<GoodsIssueDTO> issues = goodsIssueService.search(q);
        return ResponseEntity.ok(ApiResponse.success(issues, "Found " + issues.size() + " goods issues"));
    }
}
