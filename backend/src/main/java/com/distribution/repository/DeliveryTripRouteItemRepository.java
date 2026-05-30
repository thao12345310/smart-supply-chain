package com.distribution.repository;

import com.distribution.model.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryTripRouteItemRepository extends JpaRepository<DeliveryTripRouteItem, Long> {

    long countByTripRouteId(Long tripRouteId);

    List<DeliveryTripRouteItem> findByTripRouteIdOrderBySequenceAsc(Long tripRouteId);
}
