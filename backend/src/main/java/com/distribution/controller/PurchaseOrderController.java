package com.distribution.controller;

import com.distribution.model.*;
import com.distribution.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PurchaseOrderController {

    private final PurchaseOrderRepository orderRepo;
    private final PurchaseOrderItemRepository itemRepo;
    private final SupplierRepository supplierRepo;
    private final WarehouseRepository warehouseRepo;
    private final ProductRepository productRepo;

    // 🔹 Lấy toàn bộ danh sách PO
    @GetMapping
    public ResponseEntity<List<PurchaseOrder>> all() {
        return ResponseEntity.ok(orderRepo.findAll());
    }

    // 🔹 Lấy 1 PO cụ thể (kèm items)
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrder> get(@PathVariable Long id) {
        return ResponseEntity.of(orderRepo.findById(id));
    }

    // 🔹 Tạo mới đơn hàng (header + items)
    @PostMapping
    public ResponseEntity<PurchaseOrder> create(@RequestBody PurchaseOrder payload) {
        Supplier supplier = supplierRepo.findById(payload.getSupplier().getId())
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        Warehouse warehouse = warehouseRepo.findById(payload.getWarehouse().getId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        PurchaseOrder po = new PurchaseOrder();
        po.setCode("PO-" + System.currentTimeMillis());
        po.setSupplier(supplier);
        po.setWarehouse(warehouse);
        po.setCreatedDate(LocalDate.now());
        po.setOrderName(payload.getOrderName());
        po.setShippingCost(payload.getShippingCost());
        po.setTaxType(payload.getTaxType());
        po.setDeliveryDate(payload.getDeliveryDate() != null ? payload.getDeliveryDate() : LocalDateTime.now());
        po.setStatus("Created");

        // Gán items (2 chiều)
        if (payload.getItems() != null && !payload.getItems().isEmpty()) {
            for (PurchaseOrderItem i : payload.getItems()) {
                Product product = productRepo.findById(i.getProduct().getId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                i.setProduct(product);
                i.setPurchaseOrder(po);
            }
            po.setItems(payload.getItems());
        }

        PurchaseOrder saved = orderRepo.save(po);
        return ResponseEntity.ok(saved);
    }

    // 🔹 Cập nhật trạng thái PO
    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrder> update(@PathVariable Long id, @RequestBody PurchaseOrder payload) {
        PurchaseOrder po = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));

        po.setStatus(payload.getStatus());
        po.setOrderName(payload.getOrderName());
        po.setShippingCost(payload.getShippingCost());
        po.setTaxType(payload.getTaxType());

        PurchaseOrder saved = orderRepo.save(po);
        return ResponseEntity.ok(saved);
    }

    // 🔹 Xóa đơn hàng
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
