package com.distribution.controller;

import com.distribution.model.Product;
import com.distribution.model.Supplier;
import com.distribution.repository.ProductRepository;
import com.distribution.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {

    private final ProductRepository repo;
    private final SupplierRepository supplierRepo;

    @GetMapping
    public ResponseEntity<List<Product>> all() {
        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> get(@PathVariable Long id) {
        return ResponseEntity.ok(repo.findById(id).orElseThrow());
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        if (product.getSupplier() != null && product.getSupplier().getId() != null) {
            Supplier supplier = supplierRepo.findById(product.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            product.setSupplier(supplier);
        }
        return ResponseEntity.ok(repo.save(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product product) {
        Product existing = repo.findById(id).orElseThrow();

        existing.setCode(product.getCode());
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setQuantity(product.getQuantity());
        existing.setPrice(product.getPrice());

        if (product.getSupplier() != null && product.getSupplier().getId() != null) {
            Supplier supplier = supplierRepo.findById(product.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            existing.setSupplier(supplier);
        }

        return ResponseEntity.ok(repo.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
