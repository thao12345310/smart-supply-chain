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
     * Auto-generate trips for a plan: splits the plan's vận đơn across the plan's shippers
     * (one trip per shipper). Returns the created trips.
     */
    List<DeliveryTripRouteDTO> generateTripsForPlan(Long planId);

    /**
     * Manually create a trip: assign a chosen shipper and a chosen subset of the plan's vận đơn.
     */
    DeliveryTripRouteDTO createTripManually(Long planId, Long shipperId, List<Long> orderIds);

    /**
     * Delete a trip (and its items).
     */
    void deleteTrip(Long tripId);

    /**
     * Record delivery outcome at one point (vận đơn): status = "Delivered" or "Failed".
     * Auto-completes the trip when all points are resolved.
     */
    DeliveryTripRouteDTO updateTripItemStatus(Long tripId, Long itemId, String status);

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
