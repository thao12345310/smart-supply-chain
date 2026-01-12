package com.distribution.service.impl;

import com.distribution.dto.DeliveryTripRouteDTO;
import com.distribution.exception.ResourceNotFoundException;
import com.distribution.model.DeliveryPlan;
import com.distribution.model.DeliveryTripRoute;
import com.distribution.model.DeliveryTripRoute.TripStatus;
import com.distribution.model.User;
import com.distribution.repository.DeliveryPlanRepository;
import com.distribution.repository.DeliveryTripRouteRepository;
import com.distribution.repository.UserRepository;
import com.distribution.security.CustomUserDetails;
import com.distribution.security.SecurityUtils;
import com.distribution.service.DeliveryTripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of DeliveryTripService
 * 
 * Enforces RBAC rule: Shippers only see their assigned trips
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryTripServiceImpl implements DeliveryTripService {

    private final DeliveryTripRouteRepository tripRepository;
    private final UserRepository userRepository;
    private final DeliveryPlanRepository deliveryPlanRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryTripRouteDTO> getAllTrips() {
        return tripRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryTripRouteDTO> getTripsForCurrentUser() {
        Optional<CustomUserDetails> currentUser = SecurityUtils.getCurrentUser();
        
        if (currentUser.isEmpty()) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        CustomUserDetails user = currentUser.get();
        
        // If admin or delivery admin, return all trips
        if (user.hasAnyRole("ADMIN", "DELIVERY_ADMIN")) {
            log.debug("User {} has admin access, returning all trips", user.getUsername());
            return getAllTrips();
        }
        
        // If shipper, return only assigned trips
        if (user.hasRole("SHIPPER")) {
            log.debug("User {} is shipper, returning assigned trips only", user.getUsername());
            return getTripsByShipper(user.getId());
        }
        
        // Other roles: no access
        throw new AccessDeniedException("User does not have access to delivery trips");
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryTripRouteDTO> getTripsByShipper(Long shipperId) {
        return tripRepository.findByShipperUserId(shipperId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryTripRouteDTO getTripById(Long id) {
        DeliveryTripRoute trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryTripRoute", "id", id));
        
        // Check access for shipper
        if (!hasAccessToTrip(id)) {
            throw new AccessDeniedException("You do not have access to this trip");
        }
        
        return toDTO(trip);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryTripRouteDTO> getTripsByDeliveryPlan(Long deliveryPlanId) {
        return tripRepository.findByDeliveryPlanId(deliveryPlanId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryTripRouteDTO> getTripsByStatus(TripStatus status) {
        return tripRepository.findByStatus(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryTripRouteDTO> getActiveTripsForCurrentUser() {
        Optional<CustomUserDetails> currentUser = SecurityUtils.getCurrentUser();
        
        if (currentUser.isEmpty()) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        CustomUserDetails user = currentUser.get();
        
        if (user.hasAnyRole("ADMIN", "DELIVERY_ADMIN")) {
            return tripRepository.findActiveTrips().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        
        if (user.hasRole("SHIPPER")) {
            return tripRepository.findActiveTripsForShipper(user.getId()).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        
        throw new AccessDeniedException("User does not have access to delivery trips");
    }

    @Override
    @Transactional
    public DeliveryTripRouteDTO assignShipper(Long tripId, Long shipperId) {
        DeliveryTripRoute trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryTripRoute", "id", tripId));
        
        User shipper = userRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", shipperId));
        
        trip.setShipperUser(shipper);
        trip.setShipperName(shipper.getFullName());
        
        trip = tripRepository.save(trip);
        log.info("Assigned shipper {} to trip {}", shipper.getUsername(), trip.getCode());
        
        return toDTO(trip);
    }

    @Override
    @Transactional
    public DeliveryTripRouteDTO startTrip(Long tripId) {
        DeliveryTripRoute trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryTripRoute", "id", tripId));
        
        // Check access
        if (!hasAccessToTrip(tripId)) {
            throw new AccessDeniedException("You do not have access to this trip");
        }
        
        if (trip.getStatus() != TripStatus.CREATED) {
            throw new IllegalStateException("Trip can only be started from CREATED status");
        }
        
        trip.start();
        trip = tripRepository.save(trip);
        log.info("Trip {} started", trip.getCode());
        
        return toDTO(trip);
    }

    @Override
    @Transactional
    public DeliveryTripRouteDTO completeTrip(Long tripId) {
        DeliveryTripRoute trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryTripRoute", "id", tripId));
        
        // Check access
        if (!hasAccessToTrip(tripId)) {
            throw new AccessDeniedException("You do not have access to this trip");
        }
        
        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new IllegalStateException("Trip can only be completed from IN_PROGRESS status");
        }
        
        trip.complete();
        trip = tripRepository.save(trip);
        log.info("Trip {} completed", trip.getCode());
        
        return toDTO(trip);
    }

    @Override
    @Transactional
    public DeliveryTripRouteDTO cancelTrip(Long tripId, String reason) {
        DeliveryTripRoute trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryTripRoute", "id", tripId));
        
        if (trip.getStatus() == TripStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed trip");
        }
        
        trip.cancel(reason);
        trip = tripRepository.save(trip);
        log.info("Trip {} cancelled: {}", trip.getCode(), reason);
        
        return toDTO(trip);
    }

    @Override
    @Transactional
    public DeliveryTripRouteDTO updateTripStatus(Long tripId, TripStatus status) {
        DeliveryTripRoute trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryTripRoute", "id", tripId));
        
        // Check access for shipper
        if (!hasAccessToTrip(tripId)) {
            throw new AccessDeniedException("You do not have access to this trip");
        }
        
        trip.setStatus(status);
        trip = tripRepository.save(trip);
        log.info("Trip {} status updated to {}", trip.getCode(), status);
        
        return toDTO(trip);
    }

    @Override
    public boolean hasAccessToTrip(Long tripId) {
        Optional<CustomUserDetails> currentUser = SecurityUtils.getCurrentUser();
        
        if (currentUser.isEmpty()) {
            return false;
        }
        
        CustomUserDetails user = currentUser.get();
        
        // Admin and delivery admin have full access
        if (user.hasAnyRole("ADMIN", "DELIVERY_ADMIN")) {
            return true;
        }
        
        // Shipper can only access assigned trips
        if (user.hasRole("SHIPPER")) {
            return tripRepository.isTripAssignedToUser(tripId, user.getId());
        }
        
        return false;
    }

    /**
     * Convert entity to DTO
     */
    private DeliveryTripRouteDTO toDTO(DeliveryTripRoute trip) {
        DeliveryTripRouteDTO dto = DeliveryTripRouteDTO.builder()
                .id(trip.getId())
                .code(trip.getCode())
                .shipperName(trip.getShipperName())
                .status(trip.getStatus())
                .startedAt(trip.getStartedAt())
                .completedAt(trip.getCompletedAt())
                .notes(trip.getNotes())
                .build();
        
        if (trip.getShipperUser() != null) {
            dto.setShipperUserId(trip.getShipperUser().getId());
            dto.setShipperUsername(trip.getShipperUser().getUsername());
            if (trip.getShipperName() == null) {
                dto.setShipperName(trip.getShipperUser().getFullName());
            }
        }
        
        if (trip.getDeliveryPlan() != null) {
            dto.setDeliveryPlanId(trip.getDeliveryPlan().getId());
            dto.setDeliveryPlanDescription(trip.getDeliveryPlan().getDescription());
        }
        
        dto.computeFields();
        
        return dto;
    }
}
