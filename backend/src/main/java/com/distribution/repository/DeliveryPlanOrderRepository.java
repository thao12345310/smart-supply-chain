package com.distribution.repository;

import com.distribution.model.DeliveryPlanOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryPlanOrderRepository extends JpaRepository<DeliveryPlanOrder, Long> {}
