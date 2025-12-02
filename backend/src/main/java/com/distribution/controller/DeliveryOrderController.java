package com.distribution.controller;

import com.distribution.model.DeliveryOrder;
import com.distribution.repository.DeliveryOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/delivery-orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class DeliveryOrderController {
    private final DeliveryOrderRepository repo;

    @GetMapping public ResponseEntity<List<DeliveryOrder>> all(){ return ResponseEntity.ok(repo.findAll()); }
    @GetMapping("/{id}") public ResponseEntity<DeliveryOrder> get(@PathVariable Long id){ return ResponseEntity.ok(repo.findById(id).orElseThrow()); }
    @PostMapping public ResponseEntity<DeliveryOrder> create(@RequestBody DeliveryOrder d){ return ResponseEntity.ok(repo.save(d)); }
    @PutMapping("/{id}") public ResponseEntity<DeliveryOrder> update(@PathVariable Long id, @RequestBody DeliveryOrder d){
        DeliveryOrder ex = repo.findById(id).orElseThrow();
        ex.setCode(d.getCode()); ex.setStatus(d.getStatus()); ex.setDestinationAddress(d.getDestinationAddress());
        return ResponseEntity.ok(repo.save(ex));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){ repo.deleteById(id); return ResponseEntity.noContent().build(); }
}
