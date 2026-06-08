package com.distribution.repository;

import com.distribution.model.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryTripRouteItemRepository extends JpaRepository<DeliveryTripRouteItem, Long> {

    long countByTripRouteId(Long tripRouteId);

    List<DeliveryTripRouteItem> findByTripRouteIdOrderBySequenceAsc(Long tripRouteId);

    /**
     * Id các vận đơn đã được phân vào một chuyến (chưa bị hủy) trong một đợt giao hàng.
     * Dùng để chặn việc gán cùng một vận đơn cho nhiều chuyến.
     */
    @Query("select i.deliveryOrder.id from DeliveryTripRouteItem i "
            + "where i.tripRoute.deliveryPlan.id = :planId "
            + "and i.tripRoute.status <> CANCELLED")
    List<Long> findAssignedOrderIdsByPlanId(@Param("planId") Long planId);
}
