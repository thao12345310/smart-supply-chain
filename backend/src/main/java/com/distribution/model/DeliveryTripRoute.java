package com.distribution.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DeliveryTripRoute Entity
 * Represents a delivery trip assigned to a shipper
 * 
 * RBAC Rule: Shipper can only see their assigned trips
 */
@Entity
@Table(name = "delivery_triproute", indexes = {
    @Index(name = "idx_trip_shipper", columnList = "shipper_user_id"),
    @Index(name = "idx_trip_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryTripRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;
    
    // Legacy field - kept for backward compatibility
    private String shipperName;
    
    // New: Link to User entity for RBAC
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_user_id")
    private User shipperUser;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @Builder.Default
    private TripStatus status = TripStatus.CREATED;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_plan_id")
    private DeliveryPlan deliveryPlan;

    @OneToMany(mappedBy = "tripRoute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryTripRouteItem> items;
    
    /**
     * Trip Status Enum
     */
    public enum TripStatus {
        CREATED("Created", "Trip created, not yet started"),
        IN_PROGRESS("In Progress", "Trip in progress"),
        COMPLETED("Completed", "Trip completed successfully"),
        CANCELLED("Cancelled", "Trip cancelled");
        
        private final String displayName;
        private final String description;
        
        TripStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Check if trip is assigned to a specific user
     */
    public boolean isAssignedTo(Long userId) {
        return shipperUser != null && shipperUser.getId().equals(userId);
    }
    
    /**
     * Start the trip
     */
    public void start() {
        this.status = TripStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }
    
    /**
     * Complete the trip
     */
    public void complete() {
        this.status = TripStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * Cancel the trip
     */
    public void cancel(String reason) {
        this.status = TripStatus.CANCELLED;
        this.notes = reason;
    }
}
