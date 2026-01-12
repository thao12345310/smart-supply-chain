package com.distribution.controller;

import com.distribution.dto.ApiResponse;
import com.distribution.dto.SalesInvoiceDTO;
import com.distribution.model.enums.SalesInvoiceStatus;
import com.distribution.service.SalesInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Sales Invoice operations
 * 
 * Role Requirements (to be enforced by security layer):
 * - Issue/Record Payment: ROLE_ACCOUNTANT, ROLE_ADMIN
 * - View: All authenticated users
 */
@RestController
@RequestMapping("/api/sales-invoices")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Tag(name = "Sales Invoice", description = "Sales Invoice Management APIs")
public class SalesInvoiceController {

    private final SalesInvoiceService salesInvoiceService;

    // ==================== Read Operations ====================

    @GetMapping
    @Operation(summary = "Get all Sales Invoices", description = "Retrieve all sales invoices")
    public ResponseEntity<ApiResponse<List<SalesInvoiceDTO>>> getAll() {
        List<SalesInvoiceDTO> invoices = salesInvoiceService.getAll();
        return ResponseEntity.ok(ApiResponse.success(invoices, "Retrieved " + invoices.size() + " sales invoices"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Sales Invoice by ID", description = "Retrieve a specific sales invoice with all items")
    public ResponseEntity<ApiResponse<SalesInvoiceDTO>> getById(
            @Parameter(description = "Sales Invoice ID") @PathVariable Long id) {
        SalesInvoiceDTO invoice = salesInvoiceService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get Sales Invoice by Code", description = "Retrieve a sales invoice by its code")
    public ResponseEntity<ApiResponse<SalesInvoiceDTO>> getByCode(
            @Parameter(description = "Sales Invoice Code") @PathVariable String code) {
        SalesInvoiceDTO invoice = salesInvoiceService.getByCode(code);
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }

    // ==================== Workflow Operations ====================

    @PutMapping("/{id}/issue")
    @Operation(summary = "Issue Invoice", 
               description = "Issue invoice to customer. Changes status from DRAFT to ISSUED")
    public ResponseEntity<ApiResponse<SalesInvoiceDTO>> issue(
            @Parameter(description = "Sales Invoice ID") @PathVariable Long id,
            @RequestParam(required = false) Long issuedBy) {
        SalesInvoiceDTO result = salesInvoiceService.issue(id, issuedBy);
        return ResponseEntity.ok(ApiResponse.success(result, "Invoice issued successfully"));
    }

    @PostMapping("/{id}/payment")
    @Operation(summary = "Record Payment", 
               description = "Record a payment for the invoice")
    public ResponseEntity<ApiResponse<SalesInvoiceDTO>> recordPayment(
            @Parameter(description = "Sales Invoice ID") @PathVariable Long id,
            @Parameter(description = "Payment amount") @RequestParam BigDecimal amount,
            @Parameter(description = "Payment method") @RequestParam(required = false) String paymentMethod,
            @Parameter(description = "Payment reference") @RequestParam(required = false) String paymentReference) {
        SalesInvoiceDTO result = salesInvoiceService.recordPayment(id, amount, paymentMethod, paymentReference);
        return ResponseEntity.ok(ApiResponse.success(result, "Payment recorded successfully"));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel Invoice", 
               description = "Cancel an invoice. Only allowed for DRAFT or ISSUED status with no payments")
    public ResponseEntity<ApiResponse<SalesInvoiceDTO>> cancel(
            @Parameter(description = "Sales Invoice ID") @PathVariable Long id) {
        SalesInvoiceDTO result = salesInvoiceService.cancel(id);
        return ResponseEntity.ok(ApiResponse.success(result, "Invoice cancelled"));
    }

    // ==================== Query Operations ====================

    @GetMapping("/status/{status}")
    @Operation(summary = "Get Sales Invoices by Status", 
               description = "Retrieve all sales invoices with the specified status")
    public ResponseEntity<ApiResponse<List<SalesInvoiceDTO>>> getByStatus(
            @Parameter(description = "Sales Invoice Status") @PathVariable SalesInvoiceStatus status) {
        List<SalesInvoiceDTO> invoices = salesInvoiceService.getByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(invoices, "Retrieved " + invoices.size() + " sales invoices"));
    }

    @GetMapping("/sales-order/{salesOrderId}")
    @Operation(summary = "Get Sales Invoices by Sales Order", 
               description = "Retrieve all sales invoices for a specific sales order")
    public ResponseEntity<ApiResponse<List<SalesInvoiceDTO>>> getBySalesOrderId(
            @Parameter(description = "Sales Order ID") @PathVariable Long salesOrderId) {
        List<SalesInvoiceDTO> invoices = salesInvoiceService.getBySalesOrderId(salesOrderId);
        return ResponseEntity.ok(ApiResponse.success(invoices, "Retrieved " + invoices.size() + " sales invoices"));
    }

    @GetMapping("/goods-issue/{goodsIssueId}")
    @Operation(summary = "Get Sales Invoice by Goods Issue", 
               description = "Retrieve the sales invoice for a specific goods issue")
    public ResponseEntity<ApiResponse<SalesInvoiceDTO>> getByGoodsIssueId(
            @Parameter(description = "Goods Issue ID") @PathVariable Long goodsIssueId) {
        SalesInvoiceDTO invoice = salesInvoiceService.getByGoodsIssueId(goodsIssueId);
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get Sales Invoices by Customer", 
               description = "Retrieve all sales invoices for a specific customer")
    public ResponseEntity<ApiResponse<List<SalesInvoiceDTO>>> getByCustomerId(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {
        List<SalesInvoiceDTO> invoices = salesInvoiceService.getByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(invoices, "Retrieved " + invoices.size() + " sales invoices"));
    }

    @GetMapping("/customer/{customerId}/outstanding")
    @Operation(summary = "Get Customer Outstanding Amount", 
               description = "Get total outstanding amount for a customer")
    public ResponseEntity<ApiResponse<BigDecimal>> getCustomerOutstanding(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {
        BigDecimal outstanding = salesInvoiceService.getTotalOutstanding(customerId);
        return ResponseEntity.ok(ApiResponse.success(outstanding, "Outstanding amount: " + outstanding));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get Overdue Invoices", 
               description = "Retrieve all overdue invoices")
    public ResponseEntity<ApiResponse<List<SalesInvoiceDTO>>> getOverdue() {
        List<SalesInvoiceDTO> invoices = salesInvoiceService.getOverdue();
        return ResponseEntity.ok(ApiResponse.success(invoices, "Retrieved " + invoices.size() + " overdue invoices"));
    }

    @GetMapping("/unpaid")
    @Operation(summary = "Get Unpaid Invoices", 
               description = "Retrieve all unpaid invoices (ISSUED or PARTIALLY_PAID)")
    public ResponseEntity<ApiResponse<List<SalesInvoiceDTO>>> getUnpaid() {
        List<SalesInvoiceDTO> invoices = salesInvoiceService.getUnpaid();
        return ResponseEntity.ok(ApiResponse.success(invoices, "Retrieved " + invoices.size() + " unpaid invoices"));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get Sales Invoices by Date Range", 
               description = "Retrieve all sales invoices within the specified date range")
    public ResponseEntity<ApiResponse<List<SalesInvoiceDTO>>> getByDateRange(
            @Parameter(description = "Start date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<SalesInvoiceDTO> invoices = salesInvoiceService.getByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(invoices, "Retrieved " + invoices.size() + " sales invoices"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search Sales Invoices", 
               description = "Search sales invoices by code, SO code, or customer name")
    public ResponseEntity<ApiResponse<List<SalesInvoiceDTO>>> search(
            @Parameter(description = "Search query") @RequestParam String q) {
        List<SalesInvoiceDTO> invoices = salesInvoiceService.search(q);
        return ResponseEntity.ok(ApiResponse.success(invoices, "Found " + invoices.size() + " sales invoices"));
    }
}
