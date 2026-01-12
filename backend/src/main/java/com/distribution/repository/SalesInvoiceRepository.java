package com.distribution.repository;

import com.distribution.model.SalesInvoice;
import com.distribution.model.enums.SalesInvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {
    
    Optional<SalesInvoice> findByCode(String code);
    
    List<SalesInvoice> findByStatus(SalesInvoiceStatus status);
    
    List<SalesInvoice> findBySalesOrderId(Long salesOrderId);
    
    Optional<SalesInvoice> findByGoodsIssueId(Long goodsIssueId);
    
    List<SalesInvoice> findByCustomerId(Long customerId);
    
    @Query("SELECT si FROM SalesInvoice si WHERE si.invoiceDate BETWEEN :startDate AND :endDate ORDER BY si.invoiceDate DESC")
    List<SalesInvoice> findByDateRange(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT si FROM SalesInvoice si WHERE si.dueDate < :today AND si.status IN ('ISSUED', 'PARTIALLY_PAID') ORDER BY si.dueDate")
    List<SalesInvoice> findOverdue(LocalDate today);
    
    @Query("SELECT si FROM SalesInvoice si WHERE si.status = 'DRAFT' ORDER BY si.createdAt DESC")
    List<SalesInvoice> findDraft();
    
    @Query("SELECT si FROM SalesInvoice si WHERE si.status IN ('ISSUED', 'PARTIALLY_PAID') ORDER BY si.dueDate")
    List<SalesInvoice> findUnpaid();
    
    @Query("SELECT si FROM SalesInvoice si LEFT JOIN FETCH si.items WHERE si.id = :id")
    Optional<SalesInvoice> findByIdWithItems(Long id);
    
    @Query("SELECT COUNT(si) FROM SalesInvoice si WHERE si.status = :status")
    long countByStatus(SalesInvoiceStatus status);
    
    @Query("SELECT SUM(si.remainingAmount) FROM SalesInvoice si WHERE si.customer.id = :customerId AND si.status IN ('ISSUED', 'PARTIALLY_PAID', 'OVERDUE')")
    java.math.BigDecimal getTotalOutstandingByCustomerId(Long customerId);
    
    @Query("SELECT si FROM SalesInvoice si WHERE " +
           "LOWER(si.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(si.salesOrder.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(si.customer.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<SalesInvoice> search(String search);
    
    boolean existsByCode(String code);
}
