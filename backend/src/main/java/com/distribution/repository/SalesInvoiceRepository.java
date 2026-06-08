package com.distribution.repository;

import com.distribution.model.SalesInvoice;
import com.distribution.model.enums.SalesInvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    List<SalesInvoice> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT si FROM SalesInvoice si WHERE si.dueDate < :today AND si.status IN ('ISSUED', 'PARTIALLY_PAID', 'OVERDUE') ORDER BY si.dueDate")
    List<SalesInvoice> findOverdue(@Param("today") LocalDate today);

    @Query("SELECT si FROM SalesInvoice si WHERE si.status = 'DRAFT' ORDER BY si.createdAt DESC")
    List<SalesInvoice> findDraft();

    @Query("SELECT si FROM SalesInvoice si WHERE si.status IN ('ISSUED', 'PARTIALLY_PAID', 'OVERDUE') ORDER BY si.dueDate")
    List<SalesInvoice> findUnpaid();

    /**
     * Bulk-mark invoices whose due date has passed and are still unpaid as OVERDUE.
     * Run periodically by the scheduled job. Returns the number of rows updated.
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE SalesInvoice si SET si.status = 'OVERDUE', si.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE si.dueDate < :today AND si.status IN ('ISSUED', 'PARTIALLY_PAID')")
    int markOverdue(@Param("today") LocalDate today);
    
    @Query("SELECT si FROM SalesInvoice si LEFT JOIN FETCH si.items WHERE si.id = :id")
    Optional<SalesInvoice> findByIdWithItems(@Param("id") Long id);
    
    @Query("SELECT COUNT(si) FROM SalesInvoice si WHERE si.status = :status")
    long countByStatus(@Param("status") SalesInvoiceStatus status);
    
    @Query("SELECT SUM(si.remainingAmount) FROM SalesInvoice si WHERE si.customer.id = :customerId AND si.status IN ('ISSUED', 'PARTIALLY_PAID', 'OVERDUE')")
    java.math.BigDecimal getTotalOutstandingByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT si FROM SalesInvoice si WHERE " +
           "LOWER(si.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(si.salesOrder.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(si.customer.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<SalesInvoice> search(@Param("search") String search);
    
    boolean existsByCode(String code);
}
