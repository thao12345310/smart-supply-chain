package com.distribution.controller;

import com.distribution.model.*;
import com.distribution.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/delivery-plans")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class DeliveryPlanController {

    private final DeliveryPlanRepository repo;
    private final DeliveryOrderRepository orderRepo;
    private final DeliveryPlanShipperRepository shipperRepo;
    private final DeliveryTripRouteRepository tripRepo;

    // 🟢 1. Lấy toàn bộ danh sách đợt giao hàng
    @GetMapping
    public ResponseEntity<List<DeliveryPlan>> all() {
        return ResponseEntity.ok(repo.findAll());
    }

    // 🟢 2. Tạo mới đợt giao hàng
    @PostMapping
    public ResponseEntity<DeliveryPlan> create(@RequestBody DeliveryPlan plan) {
        plan.setCreatedDate(LocalDate.now());
        plan.setStatus("Created");
        return ResponseEntity.ok(repo.save(plan));
    }

    // 🟢 3. Cập nhật đợt giao hàng
    @PutMapping("/{id}")
    public ResponseEntity<DeliveryPlan> update(@PathVariable Long id, @RequestBody DeliveryPlan plan) {
        DeliveryPlan existing = repo.findById(id).orElseThrow();
        existing.setDescription(plan.getDescription());
        existing.setStatus(plan.getStatus());
        return ResponseEntity.ok(repo.save(existing));
    }

    // 🟢 4. Xoá đợt giao hàng
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // 🟢 5. Lấy chi tiết 1 đợt giao hàng
    @GetMapping("/{id}")
    public ResponseEntity<DeliveryPlan> getById(@PathVariable Long id) {
        return ResponseEntity.of(repo.findById(id));
    }

    // 🟢 6. Lấy danh sách vận đơn của đợt
    @GetMapping("/{id}/orders")
    public ResponseEntity<List<DeliveryOrder>> getOrders(@PathVariable Long id) {
        List<DeliveryOrder> orders = orderRepo.findByDeliveryPlanId(id);
        return ResponseEntity.ok(orders);
    }

    // 🟢 7. Lấy danh sách shipper của đợt
    @GetMapping("/{id}/shippers")
    public ResponseEntity<List<DeliveryPlanShipper>> getShippers(@PathVariable Long id) {
        List<DeliveryPlanShipper> shippers = shipperRepo.findByDeliveryPlanId(id);
        return ResponseEntity.ok(shippers);
    }

    // 🟢 8. Lấy danh sách chuyến giao hàng của đợt
    @GetMapping("/{id}/trips")
    public ResponseEntity<List<DeliveryTripRoute>> getTrips(@PathVariable Long id) {
        List<DeliveryTripRoute> trips = tripRepo.findByDeliveryPlanId(id);
        return ResponseEntity.ok(trips);
    }

    // 🟢 9. Thêm shipper vào đợt
    @PostMapping("/{id}/shippers")
    public ResponseEntity<DeliveryPlanShipper> addShipper(@PathVariable Long id, @RequestBody DeliveryPlanShipper shipper) {
        DeliveryPlan plan = repo.findById(id).orElseThrow();
        shipper.setDeliveryPlan(plan);
        return ResponseEntity.ok(shipperRepo.save(shipper));
    }

    // 🟢 10. Tự động tạo chuyến giao hàng (simple version)
    @PostMapping("/{id}/generate-trips")
    public ResponseEntity<String> generateTrips(@PathVariable Long id) {
        DeliveryPlan plan = repo.findById(id).orElseThrow();

        DeliveryTripRoute trip = DeliveryTripRoute.builder()
                .code("TRIP-" + System.currentTimeMillis())
                .shipperName("AutoShipper")
                .status(DeliveryTripRoute.TripStatus.CREATED)
                .deliveryPlan(plan)
                .build();
        tripRepo.save(trip);

        return ResponseEntity.ok("Generated trip successfully");
    }
}
