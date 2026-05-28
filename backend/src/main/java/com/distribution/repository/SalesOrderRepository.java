package com.distribution.repository;

import com.distribution.model.SalesOrder;
import com.distribution.model.enums.SalesOrderStatus;
import com.distribution.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    
    Optional<SalesOrder> findByCode(String code);
    
    @Query("SELECT so FROM SalesOrder so WHERE so.status = :status ORDER BY so.id DESC")
    List<SalesOrder> findByStatus(@Param("status") SalesOrderStatus status);

    @Query("SELECT so FROM SalesOrder so WHERE so.paymentStatus = :paymentStatus ORDER BY so.id DESC")
    List<SalesOrder> findByPaymentStatus(@Param("paymentStatus") PaymentStatus paymentStatus);

    @Query("SELECT so FROM SalesOrder so WHERE so.customer.id = :customerId ORDER BY so.id DESC")
    List<SalesOrder> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT so FROM SalesOrder so WHERE so.warehouse.id = :warehouseId ORDER BY so.id DESC")
    List<SalesOrder> findByWarehouseId(@Param("warehouseId") Long warehouseId);
    
    @Query("SELECT so FROM SalesOrder so WHERE so.orderDate BETWEEN :startDate AND :endDate ORDER BY so.orderDate DESC")
    List<SalesOrder> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT so FROM SalesOrder so WHERE so.status = 'ORDER_OPEN' ORDER BY so.createdAt DESC")
    List<SalesOrder> findPendingApproval();
    
    @Query("SELECT so FROM SalesOrder so WHERE so.status IN ('ORDER_APPROVED', 'ORDER_PARTIALLY_DELIVERED') ORDER BY so.createdAt DESC")
    List<SalesOrder> findReadyForGoodsIssue();
    
    @Query("SELECT so FROM SalesOrder so WHERE so.customer.id = :customerId AND so.status IN ('ORDER_APPROVED', 'ORDER_PARTIALLY_DELIVERED') ORDER BY so.orderDate DESC")
    List<SalesOrder> findActiveByCustomer(@Param("customerId") Long customerId);
    
    @Query("SELECT so FROM SalesOrder so LEFT JOIN FETCH so.items WHERE so.id = :id")
    Optional<SalesOrder> findByIdWithItems(@Param("id") Long id);
    
    @Query("SELECT so FROM SalesOrder so LEFT JOIN FETCH so.items LEFT JOIN FETCH so.goodsIssues WHERE so.id = :id")
    Optional<SalesOrder> findByIdWithItemsAndGoodsIssues(@Param("id") Long id);
    
    @Query("SELECT COUNT(so) FROM SalesOrder so WHERE so.status = :status")
    long countByStatus(@Param("status") SalesOrderStatus status);
    
    @Query("SELECT so FROM SalesOrder so WHERE " +
           "LOWER(so.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(so.orderName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(so.customer.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "ORDER BY so.id DESC")
    List<SalesOrder> search(@Param("search") String search);
    
    boolean existsByCode(String code);
}
