package com.distribution.controller;

import com.distribution.model.DeliveryPlan;
import com.distribution.repository.DeliveryPlanRepository;
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

    @GetMapping public ResponseEntity<List<DeliveryPlan>> all(){ return ResponseEntity.ok(repo.findAll()); }

    @GetMapping("/{id}") public ResponseEntity<DeliveryPlan> get(@PathVariable Long id){ return ResponseEntity.ok(repo.findById(id).orElseThrow()); }

    @PostMapping public ResponseEntity<DeliveryPlan> create(@RequestBody DeliveryPlan plan){
        plan.setCreatedDate(LocalDate.now());
        if(plan.getStatus()==null) plan.setStatus("Created");
        return ResponseEntity.ok(repo.save(plan));
    }

    @PutMapping("/{id}") public ResponseEntity<DeliveryPlan> update(@PathVariable Long id, @RequestBody DeliveryPlan plan){
        DeliveryPlan ex = repo.findById(id).orElseThrow();
        ex.setDescription(plan.getDescription());
        ex.setStatus(plan.getStatus());
        ex.setCode(plan.getCode());
        return ResponseEntity.ok(repo.save(ex));
    }

    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){ repo.deleteById(id); return ResponseEntity.noContent().build(); }
}
