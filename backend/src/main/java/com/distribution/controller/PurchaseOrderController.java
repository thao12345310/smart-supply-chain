package com.distribution.controller;

import com.distribution.dto.PurchaseOrderDTO;
import com.distribution.dto.PurchaseOrderItemDTO;
import com.distribution.model.*;
import com.distribution.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PurchaseOrderController {

    private final PurchaseOrderRepository orderRepo;
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
    public ResponseEntity<PurchaseOrder> create(@RequestBody PurchaseOrderDTO dto) {
        Supplier supplier = supplierRepo.findById(dto.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        Warehouse warehouse = warehouseRepo.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        PurchaseOrder po = new PurchaseOrder();
        po.setCode("PO-" + System.currentTimeMillis());
        po.setSupplier(supplier);
        po.setWarehouse(warehouse);
        po.setCreatedDate(LocalDate.now());
        po.setOrderName(dto.getOrderName());
        po.setShippingCost(dto.getShippingCost() != null ? dto.getShippingCost() : BigDecimal.ZERO);
        po.setTaxType(dto.getTaxType());
        
        // Set deliveryDate
        if (dto.getAfterDate() != null) {
            po.setAfterDate(dto.getAfterDate());
        }

        if (dto.getBeforeDate() != null) {
            po.setBeforeDate(dto.getBeforeDate());
        }

        po.setStatus("Created");    

        // Tạo items từ DTO
        List<PurchaseOrderItem> items = new ArrayList<>();
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (PurchaseOrderItemDTO itemDto : dto.getItems()) {
                Product product = productRepo.findById(itemDto.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                
                PurchaseOrderItem item = PurchaseOrderItem.builder()
                        .product(product)
                        .purchaseOrder(po)
                        .unit(itemDto.getUnit())
                        .quantity(itemDto.getQuantity())
                        .unitPrice(itemDto.getUnitPrice())
                        .costBeforeTax(itemDto.getCostBeforeTax())
                        .amountBeforeTax(itemDto.getUnitPrice() != null && itemDto.getQuantity() != null
                                ? itemDto.getUnitPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()))
                                : BigDecimal.ZERO)
                        .build();
                items.add(item);
            }
        }
        po.setItems(items);

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
