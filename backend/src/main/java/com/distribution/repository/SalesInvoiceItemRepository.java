package com.distribution.repository;

import com.distribution.model.SalesInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesInvoiceItemRepository extends JpaRepository<SalesInvoiceItem, Long> {
    
    List<SalesInvoiceItem> findBySalesInvoiceId(Long salesInvoiceId);
    
    List<SalesInvoiceItem> findByProductId(Long productId);
    
    List<SalesInvoiceItem> findByGoodsIssueItemId(Long goodsIssueItemId);
}
