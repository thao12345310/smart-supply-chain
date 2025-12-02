package com.distribution.repository;

import com.distribution.model.DeliveryPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryPlanRepository extends JpaRepository<DeliveryPlan, Long> {}
