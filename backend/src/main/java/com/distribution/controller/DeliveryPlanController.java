package com.distribution.controller;

import com.distribution.dto.DeliveryOrderDTO;
import com.distribution.dto.DeliveryTripRouteDTO;
import com.distribution.model.*;
import com.distribution.repository.*;
import com.distribution.service.DeliveryOrderService;
import com.distribution.service.DeliveryTripService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/delivery-plans")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class DeliveryPlanController {

    private final DeliveryPlanRepository repo;
    private final DeliveryOrderRepository orderRepo;
    private final DeliveryPlanOrderRepository planOrderRepo;
    private final DeliveryPlanShipperRepository shipperRepo;
    private final DeliveryTripRouteRepository tripRepo;
    private final DeliveryTripRouteItemRepository tripItemRepo;
    private final UserRepository userRepository;
    private final DeliveryOrderService deliveryOrderService;
    private final DeliveryTripService deliveryTripService;

    // 🟢 1. Lấy toàn bộ danh sách đợt giao hàng
    @GetMapping
    public ResponseEntity<List<DeliveryPlan>> all() {
        List<DeliveryPlan> plans = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        plans.forEach(p -> p.setOrderCount(planOrderRepo.countByDeliveryPlanId(p.getId())));
        return ResponseEntity.ok(plans);
    }

    // 🟢 2. Tạo mới đợt giao hàng
    @PostMapping
    public ResponseEntity<DeliveryPlan> create(@RequestBody DeliveryPlan plan) {
        plan.setCreatedDate(LocalDate.now());
        plan.setStatus("Created");
        DeliveryPlan saved = repo.save(plan);
        // Sinh mã đợt dạng DP-<năm>-<id 3 chữ số> nếu chưa có, để có định danh hiển thị/tra cứu
        if (saved.getCode() == null || saved.getCode().isBlank()) {
            saved.setCode(String.format("DP-%d-%03d", saved.getCreatedDate().getYear(), saved.getId()));
            saved = repo.save(saved);
        }
        return ResponseEntity.ok(saved);
    }

    // 🟢 3. Cập nhật đợt giao hàng
    @PutMapping("/{id}")
    public ResponseEntity<DeliveryPlan> update(@PathVariable Long id, @RequestBody DeliveryPlan plan) {
        DeliveryPlan existing = repo.findById(id).orElseThrow();
        existing.setDescription(plan.getDescription());
        existing.setStatus(plan.getStatus());
        if (plan.getPlannedDate() != null) existing.setPlannedDate(plan.getPlannedDate());
        if (plan.getNotes() != null) existing.setNotes(plan.getNotes());
        return ResponseEntity.ok(repo.save(existing));
    }

    // 🟢 4. Xoá đợt giao hàng
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // 🟢 5. Lấy danh sách shipper user (để gán "ai giao")
    @GetMapping("/shipper-users")
    public ResponseEntity<List<Map<String, Object>>> shipperUsers() {
        List<Map<String, Object>> list = userRepository.findByRoleName("ROLE_SHIPPER").stream()
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("name", u.getFullName() != null ? u.getFullName() : u.getUsername());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // 🟢 6. Lấy chi tiết 1 đợt giao hàng
    @GetMapping("/{id}")
    public ResponseEntity<DeliveryPlan> getById(@PathVariable Long id) {
        return ResponseEntity.of(repo.findById(id));
    }

    // 🟢 7. Lấy danh sách vận đơn của đợt
    @GetMapping("/{id}/orders")
    public ResponseEntity<List<DeliveryOrderDTO>> getOrders(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryOrderService.listByPlan(id));
    }

    // 🟢 8. Thêm vận đơn vào đợt (từ phiếu xuất)
    @PostMapping("/{id}/orders")
    public ResponseEntity<Void> addOrders(@PathVariable Long id, @RequestBody List<Long> orderIds) {
        DeliveryPlan plan = repo.findById(id).orElseThrow();
        if (orderIds != null) {
            for (Long orderId : orderIds) {
                if (orderId == null) continue;
                if (planOrderRepo.existsByDeliveryPlanIdAndDeliveryOrderId(id, orderId)) continue;
                orderRepo.findById(orderId).ifPresent(order ->
                        planOrderRepo.save(DeliveryPlanOrder.builder()
                                .deliveryPlan(plan)
                                .deliveryOrder(order)
                                .build()));
            }
        }
        return ResponseEntity.ok().build();
    }

    // 🟢 9. Xoá vận đơn khỏi đợt
    @DeleteMapping("/{id}/orders/{orderId}")
    public ResponseEntity<Void> removeOrder(@PathVariable Long id, @PathVariable Long orderId) {
        // Không cho gỡ vận đơn đang nằm trong một chuyến (chưa hủy) để tránh điểm giao mồ côi
        if (tripItemRepo.findAssignedOrderIdsByPlanId(id).contains(orderId)) {
            throw new IllegalStateException("Vận đơn đang thuộc một chuyến giao, hãy hủy/gỡ khỏi chuyến trước khi xóa khỏi đợt");
        }
        planOrderRepo.findByDeliveryPlanIdAndDeliveryOrderId(id, orderId)
                .ifPresent(planOrderRepo::delete);
        return ResponseEntity.noContent().build();
    }

    // 🟢 10. Lấy danh sách shipper của đợt
    @GetMapping("/{id}/shippers")
    public ResponseEntity<List<DeliveryPlanShipper>> getShippers(@PathVariable Long id) {
        return ResponseEntity.ok(shipperRepo.findByDeliveryPlanId(id));
    }

    // 🟢 11. Thêm shipper vào đợt
    @PostMapping("/{id}/shippers")
    public ResponseEntity<DeliveryPlanShipper> addShipper(@PathVariable Long id, @RequestBody DeliveryPlanShipper shipper) {
        DeliveryPlan plan = repo.findById(id).orElseThrow();
        shipper.setDeliveryPlan(plan);
        // Khi chọn tài xế theo tài khoản, lấy tên chuẩn từ user và chặn trùng tài xế trong cùng đợt
        if (shipper.getShipperUserId() != null) {
            if (shipperRepo.existsByDeliveryPlanIdAndShipperUserId(id, shipper.getShipperUserId())) {
                throw new IllegalStateException("Nhân viên giao hàng này đã có trong đợt");
            }
            userRepository.findById(shipper.getShipperUserId()).ifPresent(u ->
                    shipper.setShipperName(u.getFullName() != null ? u.getFullName() : u.getUsername()));
        }
        return ResponseEntity.ok(shipperRepo.save(shipper));
    }

    // 🟢 12. Xoá shipper khỏi đợt
    @DeleteMapping("/{id}/shippers/{shipperId}")
    public ResponseEntity<Void> removeShipper(@PathVariable Long id, @PathVariable Long shipperId) {
        shipperRepo.deleteById(shipperId);
        return ResponseEntity.noContent().build();
    }

    // 🟢 13. Lấy danh sách chuyến giao hàng của đợt
    @GetMapping("/{id}/trips")
    public ResponseEntity<List<Map<String, Object>>> getTrips(@PathVariable Long id) {
        List<Map<String, Object>> trips = tripRepo.findByDeliveryPlanId(id).stream()
                .map(t -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", t.getId());
                    m.put("code", t.getCode());
                    m.put("shipperName", t.getShipperName());
                    m.put("status", t.getStatus());
                    m.put("orderCount", tripItemRepo.countByTripRouteId(t.getId()));
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(trips);
    }

    // 🟢 14. Tự động tạo chuyến: chia vận đơn của đợt cho các shipper của đợt
    @PostMapping("/{id}/generate-trips")
    public ResponseEntity<List<DeliveryTripRouteDTO>> generateTrips(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryTripService.generateTripsForPlan(id));
    }

    // 🟢 15. Tạo chuyến thủ công: chọn shipper + danh sách vận đơn
    @PostMapping("/{id}/trips")
    public ResponseEntity<DeliveryTripRouteDTO> createTrip(@PathVariable Long id,
                                                           @RequestBody CreateTripRequest body) {
        return ResponseEntity.ok(
                deliveryTripService.createTripManually(id, body.getShipperId(), body.getOrderIds()));
    }

    @lombok.Data
    public static class CreateTripRequest {
        private Long shipperId;
        private List<Long> orderIds;
    }
}
