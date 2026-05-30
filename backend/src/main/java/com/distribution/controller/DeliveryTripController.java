package com.distribution.controller;

import com.distribution.dto.ApiResponse;
import com.distribution.dto.DeliveryTripRouteDTO;
import com.distribution.model.DeliveryTripRoute.TripStatus;
import com.distribution.service.DeliveryTripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Delivery Trip operations
 * 
 * RBAC Rules:
 * - GET endpoints: Shipper sees only assigned trips, Admin/DeliveryAdmin sees all
 * - Assign/Cancel: DeliveryAdmin and Admin only
 * - Start/Complete: Shipper (for assigned trips), DeliveryAdmin, Admin
 */
@RestController
@RequestMapping("/api/delivery-trips")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Tag(name = "Delivery Trips", description = "Delivery Trip Management APIs")
public class DeliveryTripController {

    private final DeliveryTripService deliveryTripService;

    // ==================== Query Operations ====================

    @GetMapping
    @Operation(summary = "Get trips for current user", 
               description = "Shippers see only assigned trips, Admin/DeliveryAdmin see all trips")
    public ResponseEntity<ApiResponse<List<DeliveryTripRouteDTO>>> getTripsForCurrentUser() {
        List<DeliveryTripRouteDTO> trips = deliveryTripService.getTripsForCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(trips, "Retrieved " + trips.size() + " trips"));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'DELIVERY_ADMIN')")
    @Operation(summary = "Get all trips (Admin only)", description = "Retrieve all delivery trips")
    public ResponseEntity<ApiResponse<List<DeliveryTripRouteDTO>>> getAllTrips() {
        List<DeliveryTripRouteDTO> trips = deliveryTripService.getAllTrips();
        return ResponseEntity.ok(ApiResponse.success(trips, "Retrieved " + trips.size() + " trips"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trip by ID", description = "Retrieve a specific trip (access checked based on role)")
    public ResponseEntity<ApiResponse<DeliveryTripRouteDTO>> getTripById(
            @Parameter(description = "Trip ID") @PathVariable Long id) {
        DeliveryTripRouteDTO trip = deliveryTripService.getTripById(id);
        return ResponseEntity.ok(ApiResponse.success(trip));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active trips for current user", 
               description = "Get trips that are not completed or cancelled")
    public ResponseEntity<ApiResponse<List<DeliveryTripRouteDTO>>> getActiveTrips() {
        List<DeliveryTripRouteDTO> trips = deliveryTripService.getActiveTripsForCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(trips, "Retrieved " + trips.size() + " active trips"));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DELIVERY_ADMIN')")
    @Operation(summary = "Get trips by status (Admin only)", description = "Retrieve trips with specific status")
    public ResponseEntity<ApiResponse<List<DeliveryTripRouteDTO>>> getTripsByStatus(
            @Parameter(description = "Trip status") @PathVariable TripStatus status) {
        List<DeliveryTripRouteDTO> trips = deliveryTripService.getTripsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(trips, "Retrieved " + trips.size() + " trips"));
    }

    @GetMapping("/delivery-plan/{deliveryPlanId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DELIVERY_ADMIN')")
    @Operation(summary = "Get trips by delivery plan (Admin only)", 
               description = "Retrieve all trips for a delivery plan")
    public ResponseEntity<ApiResponse<List<DeliveryTripRouteDTO>>> getTripsByDeliveryPlan(
            @Parameter(description = "Delivery Plan ID") @PathVariable Long deliveryPlanId) {
        List<DeliveryTripRouteDTO> trips = deliveryTripService.getTripsByDeliveryPlan(deliveryPlanId);
        return ResponseEntity.ok(ApiResponse.success(trips, "Retrieved " + trips.size() + " trips"));
    }

    @GetMapping("/shipper/{shipperId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DELIVERY_ADMIN')")
    @Operation(summary = "Get trips by shipper (Admin only)", 
               description = "Retrieve all trips assigned to a specific shipper")
    public ResponseEntity<ApiResponse<List<DeliveryTripRouteDTO>>> getTripsByShipper(
            @Parameter(description = "Shipper User ID") @PathVariable Long shipperId) {
        List<DeliveryTripRouteDTO> trips = deliveryTripService.getTripsByShipper(shipperId);
        return ResponseEntity.ok(ApiResponse.success(trips, "Retrieved " + trips.size() + " trips"));
    }

    // ==================== Shipper Actions ====================

    @PutMapping("/{id}/start")
    @Operation(summary = "Start trip", description = "Start a delivery trip (shipper or admin)")
    public ResponseEntity<ApiResponse<DeliveryTripRouteDTO>> startTrip(
            @Parameter(description = "Trip ID") @PathVariable Long id) {
        DeliveryTripRouteDTO trip = deliveryTripService.startTrip(id);
        return ResponseEntity.ok(ApiResponse.success(trip, "Trip started successfully"));
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Complete trip", description = "Complete a delivery trip (shipper or admin)")
    public ResponseEntity<ApiResponse<DeliveryTripRouteDTO>> completeTrip(
            @Parameter(description = "Trip ID") @PathVariable Long id) {
        DeliveryTripRouteDTO trip = deliveryTripService.completeTrip(id);
        return ResponseEntity.ok(ApiResponse.success(trip, "Trip completed successfully"));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update trip status", description = "Update the status of a delivery trip")
    public ResponseEntity<ApiResponse<DeliveryTripRouteDTO>> updateTripStatus(
            @Parameter(description = "Trip ID") @PathVariable Long id,
            @RequestParam TripStatus status) {
        DeliveryTripRouteDTO trip = deliveryTripService.updateTripStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(trip, "Trip status updated successfully"));
    }

    @PutMapping("/{id}/items/{itemId}/status")
    @Operation(summary = "Record delivery outcome at a point",
               description = "Mark a delivery order (point) in the trip as Delivered or Failed")
    public ResponseEntity<ApiResponse<DeliveryTripRouteDTO>> updateItemStatus(
            @Parameter(description = "Trip ID") @PathVariable Long id,
            @Parameter(description = "Trip item ID") @PathVariable Long itemId,
            @RequestParam String status) {
        DeliveryTripRouteDTO trip = deliveryTripService.updateTripItemStatus(id, itemId, status);
        return ResponseEntity.ok(ApiResponse.success(trip, "Delivery point updated"));
    }

    // ==================== Admin Actions ====================

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'DELIVERY_ADMIN')")
    @Operation(summary = "Assign shipper to trip (Admin only)", 
               description = "Assign a shipper to a delivery trip")
    public ResponseEntity<ApiResponse<DeliveryTripRouteDTO>> assignShipper(
            @Parameter(description = "Trip ID") @PathVariable Long id,
            @Parameter(description = "Shipper User ID") @RequestParam Long shipperId) {
        DeliveryTripRouteDTO trip = deliveryTripService.assignShipper(id, shipperId);
        return ResponseEntity.ok(ApiResponse.success(trip, "Shipper assigned successfully"));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'DELIVERY_ADMIN')")
    @Operation(summary = "Cancel trip (Admin only)", description = "Cancel a delivery trip")
    public ResponseEntity<ApiResponse<DeliveryTripRouteDTO>> cancelTrip(
            @Parameter(description = "Trip ID") @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        DeliveryTripRouteDTO trip = deliveryTripService.cancelTrip(id, reason);
        return ResponseEntity.ok(ApiResponse.success(trip, "Trip cancelled"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DELIVERY_ADMIN')")
    @Operation(summary = "Delete trip (Admin only)", description = "Delete a delivery trip and its items")
    public ResponseEntity<ApiResponse<Void>> deleteTrip(
            @Parameter(description = "Trip ID") @PathVariable Long id) {
        deliveryTripService.deleteTrip(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Trip deleted"));
    }
}
