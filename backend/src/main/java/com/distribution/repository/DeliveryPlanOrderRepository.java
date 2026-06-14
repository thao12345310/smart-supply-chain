package com.distribution.repository;

import com.distribution.model.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DeliveryPlanOrderRepository extends JpaRepository<DeliveryPlanOrder, Long> {

    /** Tất cả id vận đơn đã được gom vào (bất kỳ) đợt giao hàng — 1 query để lọc theo lô. */
    @Query("SELECT dpo.deliveryOrder.id FROM DeliveryPlanOrder dpo")
    List<Long> findAllPlannedDeliveryOrderIds();

    List<DeliveryPlanOrder> findByDeliveryPlanId(Long deliveryPlanId);

    long countByDeliveryPlanId(Long deliveryPlanId);

    boolean existsByDeliveryPlanIdAndDeliveryOrderId(Long deliveryPlanId, Long deliveryOrderId);

    /** Vận đơn này đã được gom vào một đợt giao hàng nào đó hay chưa. */
    boolean existsByDeliveryOrderId(Long deliveryOrderId);

    Optional<DeliveryPlanOrder> findByDeliveryPlanIdAndDeliveryOrderId(Long deliveryPlanId, Long deliveryOrderId);
}
