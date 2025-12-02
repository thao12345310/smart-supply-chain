package com.distribution.controller;

import com.distribution.model.*;
import com.distribution.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/delivery-plans/{planId}")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class DeliveryPlanManagementController {

    private final DeliveryPlanRepository planRepo;
    private final DeliveryOrderRepository orderRepo;
    private final DeliveryPlanOrderRepository planOrderRepo;
    private final DeliveryPlanShipperRepository shipperRepo;
    private final DeliveryTripRouteRepository tripRepo;
    private final DeliveryTripRouteItemRepository tripItemRepo;

    // --- ORDERS ---
    @GetMapping("/orders")
    public ResponseEntity<List<DeliveryPlanOrder>> listOrders(@PathVariable Long planId){
        DeliveryPlan plan = planRepo.findById(planId).orElseThrow();
        return ResponseEntity.ok(plan.getOrders());
    }

    @PostMapping("/orders")
    public ResponseEntity<List<DeliveryPlanOrder>> addOrders(@PathVariable Long planId, @RequestBody List<Long> deliveryOrderIds){
        DeliveryPlan plan = planRepo.findById(planId).orElseThrow();
        List<DeliveryPlanOrder> links = deliveryOrderIds.stream().map(id -> {
            DeliveryOrder od = orderRepo.findById(id).orElseThrow();
            DeliveryPlanOrder link = DeliveryPlanOrder.builder().deliveryPlan(plan).deliveryOrder(od).build();
            return planOrderRepo.save(link);
        }).collect(Collectors.toList());
        return ResponseEntity.ok(links);
    }

    @DeleteMapping("/orders/{planOrderId}")
    public ResponseEntity<Void> removeOrder(@PathVariable Long planId, @PathVariable Long planOrderId){
        planOrderRepo.deleteById(planOrderId);
        return ResponseEntity.noContent().build();
    }

    // --- SHIPPERS ---
    @GetMapping("/shippers")
    public ResponseEntity<List<DeliveryPlanShipper>> listShippers(@PathVariable Long planId){
        DeliveryPlan plan = planRepo.findById(planId).orElseThrow();
        return ResponseEntity.ok(plan.getShippers());
    }

    @PostMapping("/shippers")
    public ResponseEntity<DeliveryPlanShipper> addShipper(@PathVariable Long planId, @RequestBody DeliveryPlanShipper s){
        DeliveryPlan plan = planRepo.findById(planId).orElseThrow();
        s.setDeliveryPlan(plan);
        return ResponseEntity.ok(shipperRepo.save(s));
    }

    @DeleteMapping("/shippers/{planShipperId}")
    public ResponseEntity<Void> removeShipper(@PathVariable Long planId, @PathVariable Long planShipperId){
        shipperRepo.deleteById(planShipperId);
        return ResponseEntity.noContent().build();
    }

    // --- TRIPS ---
    @GetMapping("/trips")
    public ResponseEntity<List<DeliveryTripRoute>> listTrips(@PathVariable Long planId){
        DeliveryPlan plan = planRepo.findById(planId).orElseThrow();
        return ResponseEntity.ok(plan.getTrips());
    }

    @PostMapping("/generate-trips")
    public ResponseEntity<List<DeliveryTripRoute>> generateTrips(@PathVariable Long planId){
        DeliveryPlan plan = planRepo.findById(planId).orElseThrow();
        List<DeliveryPlanOrder> planOrders = plan.getOrders();
        List<DeliveryPlanShipper> shippers = plan.getShippers();
        if(shippers==null || shippers.isEmpty()) throw new RuntimeException("No shippers in plan");

        // simple round-robin assignment of delivery orders to shippers
        List<DeliveryOrder> orders = planOrders.stream().map(DeliveryPlanOrder::getDeliveryOrder).collect(Collectors.toList());
        List<DeliveryTripRoute> trips = new ArrayList<>();
        for (int i=0;i<shippers.size();i++){
            DeliveryPlanShipper s = shippers.get(i);
            DeliveryTripRoute trip = DeliveryTripRoute.builder()
                    .code(plan.getCode() + "-TRIP-" + (i+1))
                    .shipperName(s.getShipperName())
                    .status("Created")
                    .deliveryPlan(plan)
                    .build();
            trip = tripRepo.save(trip);
            trips.add(trip);
        }
        // assign orders
        int idx = 0;
        for (int i=0;i<orders.size();i++){
            DeliveryTripRoute trip = trips.get(idx % trips.size());
            DeliveryOrder od = orders.get(i);
            DeliveryTripRouteItem item = DeliveryTripRouteItem.builder()
                    .tripRoute(trip)
                    .deliveryOrder(od)
                    .sequenceNo(i+1)
                    .status("Pending")
                    .build();
            tripItemRepo.save(item);
            idx++;
        }
        return ResponseEntity.ok(tripRepo.findAll().stream().filter(t -> t.getDeliveryPlan().getId().equals(planId)).collect(Collectors.toList()));
    }
}
