package com.distribution.repository;

import com.distribution.model.GoodsIssueItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoodsIssueItemRepository extends JpaRepository<GoodsIssueItem, Long> {
    
    List<GoodsIssueItem> findByGoodsIssueId(Long goodsIssueId);
    
    List<GoodsIssueItem> findBySalesOrderItemId(Long salesOrderItemId);
    
    List<GoodsIssueItem> findByProductId(Long productId);
    
    @Query("SELECT SUM(gii.issuedQuantity) FROM GoodsIssueItem gii " +
           "WHERE gii.salesOrderItem.id = :salesOrderItemId " +
           "AND gii.goodsIssue.status = 'CONFIRMED'")
    Integer getTotalIssuedQuantityBySalesOrderItemId(Long salesOrderItemId);
    
    @Query("SELECT SUM(gii.issuedQuantity) FROM GoodsIssueItem gii WHERE gii.goodsIssue.id = :goodsIssueId")
    Integer getTotalIssuedQuantityByGoodsIssueId(Long goodsIssueId);
}
