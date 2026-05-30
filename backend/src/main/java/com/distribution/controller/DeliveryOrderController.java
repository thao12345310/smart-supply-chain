package com.distribution.controller;

import com.distribution.dto.DeliveryOrderDTO;
import com.distribution.service.DeliveryOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes the list of "vận đơn" (delivery orders) derived from confirmed phiếu xuất,
 * used by the Delivery Admin when building a delivery plan.
 */
@RestController
@RequestMapping("/api/delivery-orders")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class DeliveryOrderController {

    private final DeliveryOrderService deliveryOrderService;

    @GetMapping
    public ResponseEntity<List<DeliveryOrderDTO>> all() {
        return ResponseEntity.ok(deliveryOrderService.listAvailable());
    }
}
