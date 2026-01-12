package com.distribution.service;

import com.distribution.dto.SalesInvoiceDTO;
import com.distribution.model.enums.SalesInvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Sales Invoice operations
 */
public interface SalesInvoiceService {
    
    /**
     * Get Sales Invoice by ID
     */
    SalesInvoiceDTO getById(Long id);
    
    /**
     * Get Sales Invoice by code
     */
    SalesInvoiceDTO getByCode(String code);
    
    /**
     * Get all Sales Invoices
     */
    List<SalesInvoiceDTO> getAll();
    
    /**
     * Get Sales Invoices by status
     */
    List<SalesInvoiceDTO> getByStatus(SalesInvoiceStatus status);
    
    /**
     * Get Sales Invoices by Sales Order
     */
    List<SalesInvoiceDTO> getBySalesOrderId(Long salesOrderId);
    
    /**
     * Get Sales Invoice by Goods Issue
     */
    SalesInvoiceDTO getByGoodsIssueId(Long goodsIssueId);
    
    /**
     * Get Sales Invoices by customer
     */
    List<SalesInvoiceDTO> getByCustomerId(Long customerId);
    
    /**
     * Get Sales Invoices by date range
     */
    List<SalesInvoiceDTO> getByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get overdue invoices
     */
    List<SalesInvoiceDTO> getOverdue();
    
    /**
     * Get unpaid invoices
     */
    List<SalesInvoiceDTO> getUnpaid();
    
    /**
     * Search Sales Invoices
     */
    List<SalesInvoiceDTO> search(String query);
    
    /**
     * Issue invoice to customer
     */
    SalesInvoiceDTO issue(Long id, Long issuedBy);
    
    /**
     * Record payment
     */
    SalesInvoiceDTO recordPayment(Long id, BigDecimal amount, String paymentMethod, String paymentReference);
    
    /**
     * Cancel invoice (only when DRAFT or ISSUED with no payments)
     */
    SalesInvoiceDTO cancel(Long id);
    
    /**
     * Get total outstanding amount for customer
     */
    BigDecimal getTotalOutstanding(Long customerId);
}
