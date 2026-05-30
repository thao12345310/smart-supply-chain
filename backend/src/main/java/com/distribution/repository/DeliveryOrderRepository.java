package com.distribution.repository;

import com.distribution.model.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, Long> {

    @Query("SELECT dpo.deliveryOrder FROM DeliveryPlanOrder dpo WHERE dpo.deliveryPlan.id = :deliveryPlanId")
    List<DeliveryOrder> findByDeliveryPlanId(@Param("deliveryPlanId") Long id);

    Optional<DeliveryOrder> findByCode(String code);
}

