package com.distribution.repository;

import com.distribution.model.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryPlanOrderRepository extends JpaRepository<DeliveryPlanOrder, Long> {

    List<DeliveryPlanOrder> findByDeliveryPlanId(Long deliveryPlanId);

    long countByDeliveryPlanId(Long deliveryPlanId);

    boolean existsByDeliveryPlanIdAndDeliveryOrderId(Long deliveryPlanId, Long deliveryOrderId);

    /** Vận đơn này đã được gom vào một đợt giao hàng nào đó hay chưa. */
    boolean existsByDeliveryOrderId(Long deliveryOrderId);

    Optional<DeliveryPlanOrder> findByDeliveryPlanIdAndDeliveryOrderId(Long deliveryPlanId, Long deliveryOrderId);
}
