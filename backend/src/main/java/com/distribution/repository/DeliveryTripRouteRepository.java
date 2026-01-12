package com.distribution.repository;

import com.distribution.model.DeliveryTripRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryTripRouteRepository extends JpaRepository<DeliveryTripRoute, Long> {

    /**
     * Find trips by delivery plan ID
     */
    @Query("SELECT dtr FROM DeliveryTripRoute dtr WHERE dtr.deliveryPlan.id = :deliveryPlanId")
    List<DeliveryTripRoute> findByDeliveryPlanId(@Param("deliveryPlanId") Long id);

    /**
     * Find trips assigned to a specific shipper (user ID)
     * Used for RBAC: Shippers only see their assigned trips
     */
    @Query("SELECT dtr FROM DeliveryTripRoute dtr WHERE dtr.shipperUser.id = :shipperId")
    List<DeliveryTripRoute> findByShipperUserId(@Param("shipperId") Long shipperId);

    /**
     * Find trips assigned to a shipper with a specific status
     */
    @Query("SELECT dtr FROM DeliveryTripRoute dtr WHERE dtr.shipperUser.id = :shipperId AND dtr.status = :status")
    List<DeliveryTripRoute> findByShipperUserIdAndStatus(
            @Param("shipperId") Long shipperId, 
            @Param("status") DeliveryTripRoute.TripStatus status);

    /**
     * Find trip by code
     */
    Optional<DeliveryTripRoute> findByCode(String code);

    /**
     * Find trips by status
     */
    List<DeliveryTripRoute> findByStatus(DeliveryTripRoute.TripStatus status);

    /**
     * Check if a trip is assigned to a specific user
     */
    @Query("SELECT CASE WHEN COUNT(dtr) > 0 THEN true ELSE false END FROM DeliveryTripRoute dtr WHERE dtr.id = :tripId AND dtr.shipperUser.id = :userId")
    boolean isTripAssignedToUser(@Param("tripId") Long tripId, @Param("userId") Long userId);

    /**
     * Find all active trips (not completed or cancelled)
     */
    @Query("SELECT dtr FROM DeliveryTripRoute dtr WHERE dtr.status IN ('CREATED', 'IN_PROGRESS')")
    List<DeliveryTripRoute> findActiveTrips();

    /**
     * Find active trips for a specific shipper
     */
    @Query("SELECT dtr FROM DeliveryTripRoute dtr WHERE dtr.shipperUser.id = :shipperId AND dtr.status IN ('CREATED', 'IN_PROGRESS')")
    List<DeliveryTripRoute> findActiveTripsForShipper(@Param("shipperId") Long shipperId);
}
