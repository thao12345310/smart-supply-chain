package com.distribution.repository;

import com.distribution.model.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryPlanShipperRepository extends JpaRepository<DeliveryPlanShipper, Long> {

    @Query("SELECT dps FROM DeliveryPlanShipper dps WHERE dps.deliveryPlan.id = :deliveryPlanId")
    List<DeliveryPlanShipper> findByDeliveryPlanId(@Param("deliveryPlanId") Long id);

    boolean existsByDeliveryPlanIdAndShipperUserId(Long deliveryPlanId, Long shipperUserId);
}

