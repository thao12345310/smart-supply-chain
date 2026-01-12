package com.distribution.service;

import com.distribution.dto.DeliveryTripRouteDTO;
import com.distribution.model.DeliveryTripRoute;
import com.distribution.model.DeliveryTripRoute.TripStatus;

import java.util.List;

/**
 * Service interface for Delivery Trip Route operations
 * 
 * RBAC Rule: Shippers only see their assigned trips
 */
public interface DeliveryTripService {

    /**
     * Get all trips (for admin/delivery admin)
     */
    List<DeliveryTripRouteDTO> getAllTrips();

    /**
     * Get trips visible to current user
     * - Admin/DeliveryAdmin: all trips
     * - Shipper: only assigned trips
     */
    List<DeliveryTripRouteDTO> getTripsForCurrentUser();

    /**
     * Get trips assigned to a specific shipper
     */
    List<DeliveryTripRouteDTO> getTripsByShipper(Long shipperId);

    /**
     * Get trip by ID (checks access if shipper)
     */
    DeliveryTripRouteDTO getTripById(Long id);

    /**
     * Get trips by delivery plan
     */
    List<DeliveryTripRouteDTO> getTripsByDeliveryPlan(Long deliveryPlanId);

    /**
     * Get trips by status
     */
    List<DeliveryTripRouteDTO> getTripsByStatus(TripStatus status);

    /**
     * Get active trips for current user
     */
    List<DeliveryTripRouteDTO> getActiveTripsForCurrentUser();

    /**
     * Assign shipper to a trip
     */
    DeliveryTripRouteDTO assignShipper(Long tripId, Long shipperId);

    /**
     * Start a trip (shipper action)
     */
    DeliveryTripRouteDTO startTrip(Long tripId);

    /**
     * Complete a trip (shipper action)
     */
    DeliveryTripRouteDTO completeTrip(Long tripId);

    /**
     * Cancel a trip
     */
    DeliveryTripRouteDTO cancelTrip(Long tripId, String reason);

    /**
     * Update trip status
     */
    DeliveryTripRouteDTO updateTripStatus(Long tripId, TripStatus status);

    /**
     * Check if current user has access to a trip
     */
    boolean hasAccessToTrip(Long tripId);
}
