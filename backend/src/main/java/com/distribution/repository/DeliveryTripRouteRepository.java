package com.distribution.repository;

import com.distribution.model.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryTripRouteRepository extends JpaRepository<DeliveryTripRoute, Long> {

    @Query("SELECT dtr FROM DeliveryTripRoute dtr WHERE dtr.deliveryPlan.id = :deliveryPlanId")
    List<DeliveryTripRoute> findByDeliveryPlanId(@Param("deliveryPlanId") Long id);
}
