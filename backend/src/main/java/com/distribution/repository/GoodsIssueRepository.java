package com.distribution.repository;

import com.distribution.model.GoodsIssue;
import com.distribution.model.enums.GoodsIssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoodsIssueRepository extends JpaRepository<GoodsIssue, Long> {
    
    Optional<GoodsIssue> findByCode(String code);
    
    List<GoodsIssue> findByStatus(GoodsIssueStatus status);
    
    List<GoodsIssue> findBySalesOrderId(Long salesOrderId);
    
    List<GoodsIssue> findByWarehouseId(Long warehouseId);
    
    @Query("SELECT gi FROM GoodsIssue gi WHERE gi.issueDate BETWEEN :startDate AND :endDate ORDER BY gi.issueDate DESC")
    List<GoodsIssue> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT gi FROM GoodsIssue gi WHERE gi.status = 'DRAFT' ORDER BY gi.createdAt DESC")
    List<GoodsIssue> findDraft();
    
    @Query("SELECT gi FROM GoodsIssue gi WHERE gi.status = 'CONFIRMED' ORDER BY gi.confirmedDate DESC")
    List<GoodsIssue> findConfirmed();

    /**
     * Như findConfirmed nhưng fetch sẵn salesOrder + customer + deliveryAddress để tránh N+1
     * khi build danh sách vận đơn (listAvailable).
     */
    @Query("SELECT gi FROM GoodsIssue gi " +
           "LEFT JOIN FETCH gi.salesOrder so " +
           "LEFT JOIN FETCH so.customer " +
           "LEFT JOIN FETCH gi.deliveryAddress " +
           "WHERE gi.status = 'CONFIRMED' ORDER BY gi.confirmedDate DESC")
    List<GoodsIssue> findConfirmedWithDetails();

    @Query("SELECT gi FROM GoodsIssue gi LEFT JOIN FETCH gi.items WHERE gi.id = :id")
    Optional<GoodsIssue> findByIdWithItems(@Param("id") Long id);
    
    @Query("SELECT gi FROM GoodsIssue gi LEFT JOIN FETCH gi.items LEFT JOIN FETCH gi.invoice WHERE gi.id = :id")
    Optional<GoodsIssue> findByIdWithItemsAndInvoice(@Param("id") Long id);
    
    @Query("SELECT COUNT(gi) FROM GoodsIssue gi WHERE gi.status = :status")
    long countByStatus(@Param("status") GoodsIssueStatus status);
    
    @Query("SELECT gi FROM GoodsIssue gi WHERE " +
           "LOWER(gi.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(gi.salesOrder.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(gi.trackingNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<GoodsIssue> search(@Param("search") String search);
    
    boolean existsByCode(String code);
}
