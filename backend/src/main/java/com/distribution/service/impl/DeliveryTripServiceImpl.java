package com.distribution.service.impl;

import com.distribution.dto.DeliveryTripRouteDTO;
import com.distribution.exception.ResourceNotFoundException;
import com.distribution.model.DeliveryPlan;
import com.distribution.model.DeliveryPlanOrder;
import com.distribution.model.DeliveryPlanShipper;
import com.distribution.model.DeliveryTripRoute;
import com.distribution.model.DeliveryTripRoute.TripStatus;
import com.distribution.model.DeliveryTripRouteItem;
import com.distribution.model.User;
import com.distribution.model.DeliveryOrder;
import com.distribution.model.GoodsIssue;
import com.distribution.repository.DeliveryOrderRepository;
import com.distribution.repository.DeliveryPlanOrderRepository;
import com.distribution.repository.DeliveryPlanRepository;
import com.distribution.repository.DeliveryTripRouteItemRepository;
import com.distribution.repository.DeliveryTripRouteRepository;
import com.distribution.repository.GoodsIssueRepository;
import com.distribution.repository.UserRepository;
import com.distribution.security.CustomUserDetails;
import com.distribution.security.SecurityUtils;
import com.distribution.service.DeliveryTripService;
import com.distribution.service.GoodsIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final DeliveryPlanOrderRepository deliveryPlanOrderRepository;
    private final DeliveryTripRouteItemRepository tripItemRepository;
    private final GoodsIssueRepository goodsIssueRepository;
    private final DeliveryOrderRepository deliveryOrderRepository;
    private final GoodsIssueService goodsIssueService;

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryTripRouteDTO> getAllTrips() {
        return tripRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
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
    public List<DeliveryTripRouteDTO> generateTripsForPlan(Long planId) {
        DeliveryPlan plan = deliveryPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPlan", "id", planId));

        List<DeliveryPlanOrder> planOrders = deliveryPlanOrderRepository.findByDeliveryPlanId(planId);
        if (planOrders.isEmpty()) {
            throw new IllegalStateException("Đợt giao hàng chưa có vận đơn nào để tạo chuyến");
        }

        List<User> shippers = resolvePlanShippers(plan);
        if (shippers.isEmpty()) {
            throw new IllegalStateException("Đợt giao hàng chưa có nhân viên giao hàng nào để chia chuyến");
        }

        // Chỉ chia những vận đơn chưa được phân vào chuyến nào → không tạo trùng khi bấm lại
        List<Long> assignedOrderIds = tripItemRepository.findAssignedOrderIdsByPlanId(planId);
        List<DeliveryOrder> unassigned = planOrders.stream()
                .map(DeliveryPlanOrder::getDeliveryOrder)
                .filter(o -> !assignedOrderIds.contains(o.getId()))
                .collect(Collectors.toList());
        if (unassigned.isEmpty()) {
            throw new IllegalStateException("Tất cả vận đơn của đợt đã được phân chuyến");
        }

        // Chia đều vận đơn cho các shipper theo vòng (round-robin)
        List<List<DeliveryOrder>> buckets = new ArrayList<>();
        for (int i = 0; i < shippers.size(); i++) {
            buckets.add(new ArrayList<>());
        }
        int idx = 0;
        for (DeliveryOrder order : unassigned) {
            buckets.get(idx % shippers.size()).add(order);
            idx++;
        }

        List<DeliveryTripRouteDTO> result = new ArrayList<>();
        for (int i = 0; i < shippers.size(); i++) {
            if (buckets.get(i).isEmpty()) continue; // shipper nhiều hơn vận đơn → bỏ qua
            DeliveryTripRoute trip = createTrip(plan, shippers.get(i), buckets.get(i));
            result.add(toDTO(trip));
        }

        plan.setStatus("InProgress");
        deliveryPlanRepository.save(plan);

        log.info("Generated {} trip(s) for plan {} across {} shipper(s)", result.size(), planId, shippers.size());
        return result;
    }

    @Override
    @Transactional
    public DeliveryTripRouteDTO createTripManually(Long planId, Long shipperId, List<Long> orderIds) {
        DeliveryPlan plan = deliveryPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPlan", "id", planId));

        if (orderIds == null || orderIds.isEmpty()) {
            throw new IllegalStateException("Hãy chọn ít nhất một vận đơn cho chuyến");
        }
        User shipper = userRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", shipperId));

        List<DeliveryOrder> orders = deliveryPlanOrderRepository.findByDeliveryPlanId(planId).stream()
                .map(DeliveryPlanOrder::getDeliveryOrder)
                .filter(o -> orderIds.contains(o.getId()))
                .collect(Collectors.toList());
        if (orders.isEmpty()) {
            throw new IllegalStateException("Các vận đơn đã chọn không thuộc đợt giao hàng này");
        }

        // Một vận đơn chỉ được thuộc tối đa một chuyến → chặn nếu đã có trong chuyến khác
        List<Long> assignedOrderIds = tripItemRepository.findAssignedOrderIdsByPlanId(planId);
        List<String> alreadyAssigned = orders.stream()
                .filter(o -> assignedOrderIds.contains(o.getId()))
                .map(DeliveryOrder::getCode)
                .collect(Collectors.toList());
        if (!alreadyAssigned.isEmpty()) {
            throw new IllegalStateException(
                    "Các vận đơn sau đã được phân vào chuyến khác: " + String.join(", ", alreadyAssigned));
        }

        DeliveryTripRoute trip = createTrip(plan, shipper, orders);
        plan.setStatus("InProgress");
        deliveryPlanRepository.save(plan);
        return toDTO(trip);
    }

    @Override
    @Transactional
    public void deleteTrip(Long tripId) {
        DeliveryTripRoute trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryTripRoute", "id", tripId));
        tripRepository.delete(trip);
        log.info("Deleted trip {}", trip.getCode());
    }

    /**
     * Build the set of shipper users participating in a plan (from tab DS Shipper),
     * matched to real User accounts by full name / username.
     */
    private List<User> resolvePlanShippers(DeliveryPlan plan) {
        List<User> all = userRepository.findByRoleName("ROLE_SHIPPER");
        List<User> resolved = new ArrayList<>();
        if (plan.getShippers() != null) {
            for (DeliveryPlanShipper ps : plan.getShippers()) {
                // Ưu tiên định danh tài khoản; fallback khớp theo tên cho dữ liệu cũ
                User user = null;
                if (ps.getShipperUserId() != null) {
                    user = userRepository.findById(ps.getShipperUserId()).orElse(null);
                }
                if (user == null) {
                    user = all.stream()
                            .filter(u -> (u.getFullName() != null && u.getFullName().equalsIgnoreCase(ps.getShipperName()))
                                    || u.getUsername().equalsIgnoreCase(ps.getShipperName()))
                            .findFirst().orElse(null);
                }
                final User fu = user;
                if (fu != null && resolved.stream().noneMatch(r -> r.getId().equals(fu.getId()))) {
                    resolved.add(fu);
                }
            }
        }
        return resolved;
    }

    /**
     * Create one trip for a shipper containing an ordered sequence of delivery orders.
     */
    private DeliveryTripRoute createTrip(DeliveryPlan plan, User shipper, List<DeliveryOrder> orders) {
        DeliveryTripRoute trip = DeliveryTripRoute.builder()
                .code("TRIP-" + System.currentTimeMillis() + "-" + (shipper != null ? shipper.getId() : "x"))
                .status(TripStatus.CREATED)
                .deliveryPlan(plan)
                .shipperUser(shipper)
                .shipperName(shipper != null ? shipper.getFullName() : null)
                .build();
        trip = tripRepository.save(trip);

        int sequence = 1;
        for (DeliveryOrder order : orders) {
            tripItemRepository.save(DeliveryTripRouteItem.builder()
                    .tripRoute(trip)
                    .deliveryOrder(order)
                    .sequence(sequence++)
                    .status("Pending")
                    .build());
        }
        return trip;
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

        maybeCompletePlan(trip.getDeliveryPlan());

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
        if (status == TripStatus.COMPLETED && trip.getCompletedAt() == null) {
            trip.setCompletedAt(java.time.LocalDateTime.now());
        }
        trip = tripRepository.save(trip);
        log.info("Trip {} status updated to {}", trip.getCode(), status);

        if (status == TripStatus.COMPLETED) {
            maybeCompletePlan(trip.getDeliveryPlan());
        }

        return toDTO(trip);
    }

    @Override
    @Transactional
    public DeliveryTripRouteDTO updateTripItemStatus(Long tripId, Long itemId, String status) {
        DeliveryTripRoute trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryTripRoute", "id", tripId));

        if (!hasAccessToTrip(tripId)) {
            throw new AccessDeniedException("You do not have access to this trip");
        }
        if (trip.getStatus() == TripStatus.CREATED) {
            // Bắt đầu chuyến tự động khi shipper ghi nhận điểm giao đầu tiên
            trip.start();
            tripRepository.save(trip);
        }

        DeliveryTripRouteItem item = tripItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryTripRouteItem", "id", itemId));
        if (item.getTripRoute() == null || !item.getTripRoute().getId().equals(tripId)) {
            throw new IllegalStateException("Điểm giao không thuộc chuyến này");
        }

        String previousStatus = item.getStatus();
        item.setStatus(status); // "Delivered" / "Failed"
        tripItemRepository.save(item);
        log.info("Trip {} item {} marked {}", trip.getCode(), itemId, status);

        // Đồng bộ trạng thái vận đơn + bù trừ tồn kho/đơn bán theo kết quả giao.
        // Toàn bộ method ở trong 1 transaction nên nếu bù trừ lỗi thì thay đổi điểm giao cũng được rollback.
        syncDeliveryOrderOutcome(item.getDeliveryOrder(), previousStatus, status);

        // Khi tất cả điểm đã xử lý (Delivered/Failed) → hoàn thành chuyến
        List<DeliveryTripRouteItem> items = tripItemRepository.findByTripRouteIdOrderBySequenceAsc(tripId);
        boolean allResolved = !items.isEmpty() && items.stream()
                .allMatch(it -> "Delivered".equalsIgnoreCase(it.getStatus())
                        || "Failed".equalsIgnoreCase(it.getStatus()));
        if (allResolved && trip.getStatus() == TripStatus.IN_PROGRESS) {
            trip.complete();
            tripRepository.save(trip);
            maybeCompletePlan(trip.getDeliveryPlan());
        }

        return toDTO(tripRepository.findById(tripId).orElse(trip));
    }

    /**
     * Cập nhật trạng thái vận đơn theo kết quả điểm giao và bù trừ tồn kho/đơn bán:
     * - chuyển sang Failed: hoàn hàng về kho (restock) + giảm số lượng đã giao của đơn bán
     * - sửa từ Failed -> Delivered: trừ kho lại (reissue)
     * Tồn kho được trừ tại thời điểm xác nhận phiếu xuất, nên giao thành công không đụng tồn.
     */
    private void syncDeliveryOrderOutcome(DeliveryOrder order, String previousStatus, String newStatus) {
        if (order == null) return;

        boolean wasFailed = "Failed".equalsIgnoreCase(previousStatus);
        boolean nowFailed = "Failed".equalsIgnoreCase(newStatus);
        boolean nowDelivered = "Delivered".equalsIgnoreCase(newStatus);

        if (nowFailed && !wasFailed) {
            goodsIssueService.restockFromFailedDelivery(order.getCode());
            order.setStatus("Failed");
            deliveryOrderRepository.save(order);
        } else if (nowDelivered) {
            if (wasFailed) {
                goodsIssueService.reissueAfterCorrection(order.getCode());
            }
            order.setStatus("Delivered");
            deliveryOrderRepository.save(order);
        }
    }

    /**
     * If every trip of the plan is finished (COMPLETED/CANCELLED, with at least one COMPLETED),
     * mark the plan as Completed.
     */
    private void maybeCompletePlan(DeliveryPlan plan) {
        if (plan == null) return;
        List<DeliveryTripRoute> trips = tripRepository.findByDeliveryPlanId(plan.getId());
        if (trips.isEmpty()) return;
        boolean anyCompleted = trips.stream().anyMatch(t -> t.getStatus() == TripStatus.COMPLETED);
        boolean allDone = trips.stream().allMatch(t -> t.getStatus() == TripStatus.COMPLETED
                || t.getStatus() == TripStatus.CANCELLED);
        if (anyCompleted && allDone) {
            plan.setStatus("Completed");
            deliveryPlanRepository.save(plan);
            log.info("Plan {} marked Completed", plan.getId());
        }
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

        // Các điểm giao (vận đơn) trong chuyến, theo thứ tự
        List<DeliveryTripRouteItem> items = tripItemRepository.findByTripRouteIdOrderBySequenceAsc(trip.getId());
        List<DeliveryTripRouteDTO.DeliveryTripRouteItemDTO> itemDTOs = items.stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);
        dto.setTotalItems(itemDTOs.size());
        dto.setCompletedItems((int) items.stream()
                .filter(it -> "Delivered".equalsIgnoreCase(it.getStatus()))
                .count());

        dto.computeFields();

        return dto;
    }

    private DeliveryTripRouteDTO.DeliveryTripRouteItemDTO toItemDTO(DeliveryTripRouteItem item) {
        DeliveryOrder order = item.getDeliveryOrder();
        GoodsIssue gi = order != null ? goodsIssueRepository.findByCode(order.getCode()).orElse(null) : null;
        return DeliveryTripRouteDTO.DeliveryTripRouteItemDTO.builder()
                .id(item.getId())
                .deliveryOrderId(order != null ? order.getId() : null)
                .deliveryOrderCode(order != null ? order.getCode() : null)
                .customerName(customerName(gi))
                .deliveryAddress(deliveryAddress(gi, order))
                .products(productsSummary(gi))
                .sequence(item.getSequence())
                .status(item.getStatus())
                .build();
    }

    private String customerName(GoodsIssue gi) {
        if (gi != null && gi.getSalesOrder() != null && gi.getSalesOrder().getCustomer() != null) {
            return gi.getSalesOrder().getCustomer().getName();
        }
        return null;
    }

    private String deliveryAddress(GoodsIssue gi, DeliveryOrder order) {
        if (gi != null && gi.getDeliveryAddress() != null) {
            return gi.getDeliveryAddress().getFullAddress();
        }
        return order != null ? order.getDestinationAddress() : null;
    }

    private String productsSummary(GoodsIssue gi) {
        if (gi == null || gi.getItems() == null || gi.getItems().isEmpty()) {
            return null;
        }
        return gi.getItems().stream()
                .map(it -> {
                    String name = it.getProduct() != null ? it.getProduct().getName() : "SP";
                    String unit = it.getUnit() != null ? " " + it.getUnit() : "";
                    return name + " x" + it.getIssuedQuantity() + unit;
                })
                .collect(Collectors.joining(", "));
    }
}
