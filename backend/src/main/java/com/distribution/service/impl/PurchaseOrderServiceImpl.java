package com.distribution.service.impl;

import com.distribution.dto.PurchaseOrderDTO;
import com.distribution.dto.PurchaseOrderItemDTO;
import com.distribution.model.*;
import com.distribution.repository.*;
import com.distribution.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository orderRepo;
    private final PurchaseOrderItemRepository itemRepo;
    private final SupplierRepository supplierRepo;
    private final ProductRepository productRepo;

    private PurchaseOrderDTO toDto(PurchaseOrder po) {
        List<PurchaseOrderItemDTO> itemDTOs = po.getItems() != null
                ? po.getItems().stream().map(i ->
                PurchaseOrderItemDTO.builder()
                        .id(i.getId())
                        .productId(i.getProduct() != null ? i.getProduct().getId() : null)
                        .productName(i.getProduct() != null ? i.getProduct().getName() : null)
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build()).collect(Collectors.toList())
                : null;

        return PurchaseOrderDTO.builder()
                .id(po.getId())
                .code(po.getCode())
                .orderDate(po.getExpectedDate())
                .status(po.getStatus())
                .supplierId(po.getSupplier() != null ? po.getSupplier().getId() : null)
                .supplierName(po.getSupplier() != null ? po.getSupplier().getName() : null)
                .items(itemDTOs)
                .build();
    }

    private PurchaseOrder toEntity(PurchaseOrderDTO dto) {
        Supplier supplier = supplierRepo.findById(dto.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        PurchaseOrder po = PurchaseOrder.builder()
                .id(dto.getId())
                .code(dto.getCode())
                .expectedDate(dto.getOrderDate())
                .status(dto.getStatus())
                .supplier(supplier)
                .build();

        if (dto.getItems() != null) {
            List<PurchaseOrderItem> items = dto.getItems().stream().map(i -> {
                Product p = productRepo.findById(i.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                return PurchaseOrderItem.builder()
                        .purchaseOrder(po)
                        .product(p)
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build();
            }).collect(Collectors.toList());
            po.setItems(items);
        }

        return po;
    }

    @Override
    public PurchaseOrderDTO create(PurchaseOrderDTO dto) {
        PurchaseOrder saved = orderRepo.save(toEntity(dto));
        return toDto(saved);
    }

    @Override
    public PurchaseOrderDTO update(Long id, PurchaseOrderDTO dto) {
        PurchaseOrder po = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));

        po.setCode(dto.getCode());
        po.setExpectedDate(dto.getOrderDate());
        po.setStatus(dto.getStatus());
        po.setSupplier(
                supplierRepo.findById(dto.getSupplierId())
                        .orElseThrow(() -> new RuntimeException("Supplier not found"))
        );

        // replace all items
        itemRepo.deleteAll(po.getItems());
        List<PurchaseOrderItem> items = dto.getItems().stream().map(i -> {
            Product p = productRepo.findById(i.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            return PurchaseOrderItem.builder()
                    .purchaseOrder(po)
                    .product(p)
                    .quantity(i.getQuantity())
                    .unitPrice(i.getUnitPrice())
                    .build();
        }).collect(Collectors.toList());
        po.setItems(items);

        return toDto(orderRepo.save(po));
    }

    @Override
    public PurchaseOrderDTO getById(Long id) {
        return orderRepo.findById(id).map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));
    }

    @Override
    public List<PurchaseOrderDTO> getAll() {
        return orderRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        orderRepo.deleteById(id);
    }
}
