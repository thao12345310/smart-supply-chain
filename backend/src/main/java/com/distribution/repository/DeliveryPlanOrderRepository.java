package com.distribution.repository;

import com.distribution.model.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryPlanOrderRepository extends JpaRepository<DeliveryPlanOrder, Long> {

    List<DeliveryPlanOrder> findByDeliveryPlanId(Long deliveryPlanId);

    boolean existsByDeliveryPlanIdAndDeliveryOrderId(Long deliveryPlanId, Long deliveryOrderId);

    Optional<DeliveryPlanOrder> findByDeliveryPlanIdAndDeliveryOrderId(Long deliveryPlanId, Long deliveryOrderId);
}
