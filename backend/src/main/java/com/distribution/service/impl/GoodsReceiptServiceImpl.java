package com.distribution.service.impl;

import com.distribution.dto.GoodsReceiptDTO;
import com.distribution.dto.GoodsReceiptItemDTO;
import com.distribution.exception.BusinessException;
import com.distribution.exception.InvalidStatusTransitionException;
import com.distribution.exception.InventoryException;
import com.distribution.exception.ResourceNotFoundException;
import com.distribution.model.*;
import com.distribution.model.enums.GoodsReceiptStatus;
import com.distribution.model.enums.PurchaseOrderStatus;
import com.distribution.repository.*;
import com.distribution.service.GoodsReceiptService;
import com.distribution.service.InventoryService;
import com.distribution.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of GoodsReceiptService
 * 
 * Business Rules:
 * - GR can only be created for APPROVED or PARTIALLY_RECEIVED POs
 * - Cannot receive more than remaining ordered quantity
 * - Confirmation updates PO item received quantities and inventory
 * - Auto-updates PO status based on receiving completion
 */
@Service
@RequiredArgsConstructor
@Transactional
public class GoodsReceiptServiceImpl implements GoodsReceiptService {

    private static final Logger logger = LoggerFactory.getLogger(GoodsReceiptServiceImpl.class);

    private final GoodsReceiptRepository grRepo;
    private final GoodsReceiptItemRepository grItemRepo;
    private final PurchaseOrderRepository poRepo;
    private final PurchaseOrderItemRepository poItemRepo;
    private final ProductRepository productRepo;
    private final WarehouseRepository warehouseRepo;
    private final InventoryService inventoryService;
    private final PurchaseOrderService purchaseOrderService;
    private final InventoryLotRepository inventoryLotRepo;

    @Override
    public GoodsReceiptDTO create(GoodsReceiptDTO dto) {
        logger.info("Creating Goods Receipt for PO: {}", dto.getPurchaseOrderId());
        
        // Get and validate PO
        PurchaseOrder po = poRepo.findByIdWithItems(dto.getPurchaseOrderId())
            .orElseThrow(() -> new ResourceNotFoundException("Purchase Order", dto.getPurchaseOrderId()));
        
        // Validate PO status allows goods receipt
        if (!po.getStatus().canReceiveGoods()) {
            throw new InvalidStatusTransitionException(
                po.getStatus().getDisplayName(), "receive goods");
        }
        
        // Get warehouse (use PO warehouse if not specified)
        Warehouse warehouse;
        if (dto.getWarehouseId() != null) {
            warehouse = warehouseRepo.findById(dto.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", dto.getWarehouseId()));
        } else if (po.getWarehouse() != null) {
            warehouse = po.getWarehouse();
        } else {
            throw new BusinessException("Warehouse is required for goods receipt");
        }
        
        // Create GR entity
        GoodsReceipt gr = GoodsReceipt.builder()
            .code(generateGRCode())
            .status(GoodsReceiptStatus.DRAFT)
            .receiptDate(dto.getReceiptDate() != null ? dto.getReceiptDate() : LocalDate.now())
            .deliveryNoteNumber(dto.getDeliveryNoteNumber())
            .invoiceNumber(dto.getInvoiceNumber())
            .notes(dto.getNotes())
            .purchaseOrder(po)
            .warehouse(warehouse)
            .createdBy(dto.getCreatedBy())
            .createdAt(LocalDateTime.now())
            .items(new ArrayList<>())
            .build();
        
        // Validate and add items
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("At least one item is required for goods receipt");
        }
        
        for (GoodsReceiptItemDTO itemDto : dto.getItems()) {
            // Get PO item
            PurchaseOrderItem poItem = poItemRepo.findById(itemDto.getPurchaseOrderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order Item", itemDto.getPurchaseOrderItemId()));
            
            // Validate PO item belongs to this PO
            if (!poItem.getPurchaseOrder().getId().equals(po.getId())) {
                throw new BusinessException("PO Item does not belong to the specified Purchase Order");
            }
            
            // Validate receiving quantity
            int remainingQty = poItem.getRemainingQuantity();
            if (itemDto.getReceivedQuantity() > remainingQty) {
                throw InventoryException.exceedsOrderedQuantity(
                    poItem.getProduct().getName(),
                    poItem.getQuantity(),
                    poItem.getReceivedQuantity() + itemDto.getReceivedQuantity()
                );
            }
            
            // Create GR item
            GoodsReceiptItem grItem = GoodsReceiptItem.builder()
                .goodsReceipt(gr)
                .purchaseOrderItem(poItem)
                .product(poItem.getProduct())
                .orderedQuantity(poItem.getQuantity())
                .receivedQuantity(itemDto.getReceivedQuantity())
                .acceptedQuantity(itemDto.getReceivedQuantity() - (itemDto.getRejectedQuantity() != null ? itemDto.getRejectedQuantity() : 0))
                .rejectedQuantity(itemDto.getRejectedQuantity() != null ? itemDto.getRejectedQuantity() : 0)
                .unitPrice(poItem.getUnitPrice())
                .unit(poItem.getUnit())
                .batchNumber(itemDto.getBatchNumber())
                .expiryDate(itemDto.getExpiryDate())
                .rejectionReason(itemDto.getRejectionReason())
                .notes(itemDto.getNotes())
                .build();
            
            gr.getItems().add(grItem);
        }
        
        // Calculate total
        gr.recalculateTotal();
        
        // Save
        GoodsReceipt saved = grRepo.save(gr);
        logger.info("Created Goods Receipt: {}", saved.getCode());
        
        return toDto(saved);
    }

    @Override
    public GoodsReceiptDTO update(Long id, GoodsReceiptDTO dto) {
        logger.info("Updating Goods Receipt: {}", id);
        
        GoodsReceipt gr = grRepo.findByIdWithItems(id)
            .orElseThrow(() -> new ResourceNotFoundException("Goods Receipt", id));
        
        // Only allow updates when status is DRAFT
        if (gr.getStatus() != GoodsReceiptStatus.DRAFT) {
            throw new InvalidStatusTransitionException(
                gr.getStatus().getDisplayName(), "update");
        }
        
        // Update basic fields
        gr.setReceiptDate(dto.getReceiptDate());
        gr.setDeliveryNoteNumber(dto.getDeliveryNoteNumber());
        gr.setInvoiceNumber(dto.getInvoiceNumber());
        gr.setNotes(dto.getNotes());
        
        // Update items
        if (dto.getItems() != null) {
            gr.getItems().clear();
            
            for (GoodsReceiptItemDTO itemDto : dto.getItems()) {
                PurchaseOrderItem poItem = poItemRepo.findById(itemDto.getPurchaseOrderItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Purchase Order Item", itemDto.getPurchaseOrderItemId()));
                
                // Validate receiving quantity
                int remainingQty = poItem.getRemainingQuantity();
                if (itemDto.getReceivedQuantity() > remainingQty) {
                    throw InventoryException.exceedsOrderedQuantity(
                        poItem.getProduct().getName(),
                        poItem.getQuantity(),
                        poItem.getReceivedQuantity() + itemDto.getReceivedQuantity()
                    );
                }
                
                GoodsReceiptItem grItem = GoodsReceiptItem.builder()
                    .goodsReceipt(gr)
                    .purchaseOrderItem(poItem)
                    .product(poItem.getProduct())
                    .orderedQuantity(poItem.getQuantity())
                    .receivedQuantity(itemDto.getReceivedQuantity())
                    .acceptedQuantity(itemDto.getReceivedQuantity() - (itemDto.getRejectedQuantity() != null ? itemDto.getRejectedQuantity() : 0))
                    .rejectedQuantity(itemDto.getRejectedQuantity() != null ? itemDto.getRejectedQuantity() : 0)
                    .unitPrice(poItem.getUnitPrice())
                    .unit(poItem.getUnit())
                    .batchNumber(itemDto.getBatchNumber())
                    .expiryDate(itemDto.getExpiryDate())
                    .rejectionReason(itemDto.getRejectionReason())
                    .notes(itemDto.getNotes())
                    .build();
                
                gr.getItems().add(grItem);
            }
        }
        
        gr.recalculateTotal();
        
        GoodsReceipt saved = grRepo.save(gr);
        logger.info("Updated Goods Receipt: {}", saved.getCode());
        
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public GoodsReceiptDTO getById(Long id) {
        GoodsReceipt gr = grRepo.findByIdWithItems(id)
            .orElseThrow(() -> new ResourceNotFoundException("Goods Receipt", id));
        return toDto(gr);
    }

    @Override
    @Transactional(readOnly = true)
    public GoodsReceiptDTO getByCode(String code) {
        GoodsReceipt gr = grRepo.findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Goods Receipt", code));
        return toDto(gr);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsReceiptDTO> getAll() {
        return grRepo.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsReceiptDTO> getByStatus(GoodsReceiptStatus status) {
        return grRepo.findByStatus(status).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsReceiptDTO> getByPurchaseOrderId(Long purchaseOrderId) {
        return grRepo.findByPurchaseOrderId(purchaseOrderId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsReceiptDTO> getByWarehouseId(Long warehouseId) {
        return grRepo.findByWarehouseId(warehouseId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsReceiptDTO> getByDateRange(LocalDate startDate, LocalDate endDate) {
        return grRepo.findByReceiptDateBetween(startDate, endDate).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsReceiptDTO> getPendingConfirmation() {
        return grRepo.findPendingConfirmation().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public GoodsReceiptDTO confirm(Long id, Long confirmedBy) {
        logger.info("Confirming Goods Receipt: {} by user: {}", id, confirmedBy);
        
        GoodsReceipt gr = grRepo.findByIdWithItems(id)
            .orElseThrow(() -> new ResourceNotFoundException("Goods Receipt", id));
        
        // Validate status
        if (!gr.getStatus().canConfirm()) {
            throw new InvalidStatusTransitionException(
                gr.getStatus().getDisplayName(), "confirm");
        }
        
        PurchaseOrder po = gr.getPurchaseOrder();
        Long warehouseId = gr.getWarehouse().getId();
        
        // Process each item
        for (GoodsReceiptItem grItem : gr.getItems()) {
            PurchaseOrderItem poItem = grItem.getPurchaseOrderItem();
            
            // Final validation of quantities
            int remaining = poItem.getRemainingQuantity();
            if (grItem.getReceivedQuantity() > remaining) {
                throw InventoryException.exceedsOrderedQuantity(
                    poItem.getProduct().getName(),
                    poItem.getQuantity(),
                    poItem.getReceivedQuantity() + grItem.getReceivedQuantity()
                );
            }
            
            // Update PO item received quantity
            poItem.addReceivedQuantity(grItem.getAcceptedQuantity());
            poItemRepo.save(poItem);
            
            // Update inventory (only for accepted quantity)
            if (grItem.getAcceptedQuantity() > 0) {
                inventoryService.addStock(
                    grItem.getProduct().getId(),
                    warehouseId,
                    grItem.getAcceptedQuantity(),
                    grItem.getUnitPrice(),
                    "GOODS_RECEIPT",
                    gr.getId(),
                    gr.getCode(),
                    confirmedBy
                );

                // Tạo lot record theo FEFO (idempotency: skip nếu đã tồn tại)
                if (inventoryLotRepo.existsBySourceReceiptItemId(grItem.getId())) {
                    logger.warn("Lot đã tồn tại cho GoodsReceiptItem id={}, bỏ qua", grItem.getId());
                } else {
                    String batchNum = grItem.getBatchNumber();
                    if (batchNum == null || batchNum.isBlank()) {
                        throw new BusinessException(
                            "Sản phẩm " + grItem.getProduct().getName() + ": thiếu số lô (batch number)");
                    }
                    InventoryLot lot = InventoryLot.builder()
                        .product(grItem.getProduct())
                        .warehouse(gr.getWarehouse())
                        .lotNumber(batchNum)
                        .manufactureDate(null) // GoodsReceiptItem chưa có trường manufactureDate
                        .expiryDate(grItem.getExpiryDate())
                        .quantityReceived(BigDecimal.valueOf(grItem.getAcceptedQuantity()))
                        .quantityRemaining(BigDecimal.valueOf(grItem.getAcceptedQuantity()))
                        .unitCost(grItem.getUnitPrice())
                        .sourceReceipt(gr)
                        .sourceReceiptItem(grItem)
                        .build();
                    inventoryLotRepo.save(lot);
                    logger.info("Đã tạo lot {} cho sản phẩm {} tại kho {}",
                        batchNum, grItem.getProduct().getName(), gr.getWarehouse().getName());
                }
            }
        }

        // Update GR status
        gr.setStatus(GoodsReceiptStatus.CONFIRMED);
        gr.setConfirmedDate(LocalDateTime.now());
        gr.setConfirmedBy(confirmedBy);
        
        GoodsReceipt saved = grRepo.save(gr);
        
        // Update PO status based on receiving progress
        purchaseOrderService.updateReceivingStatus(po.getId());
        
        logger.info("Confirmed Goods Receipt: {}. Inventory updated.", saved.getCode());
        
        return toDto(saved);
    }

    @Override
    public GoodsReceiptDTO cancel(Long id) {
        logger.info("Cancelling Goods Receipt: {}", id);
        
        GoodsReceipt gr = grRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Goods Receipt", id));
        
        // Validate status
        if (!gr.getStatus().canCancel()) {
            throw new InvalidStatusTransitionException(
                gr.getStatus().getDisplayName(), "cancel");
        }
        
        gr.setStatus(GoodsReceiptStatus.CANCELLED);
        
        GoodsReceipt saved = grRepo.save(gr);
        logger.info("Cancelled Goods Receipt: {}", saved.getCode());
        
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        GoodsReceipt gr = grRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Goods Receipt", id));
        
        // Only allow delete when status is DRAFT
        if (gr.getStatus() != GoodsReceiptStatus.DRAFT) {
            throw new InvalidStatusTransitionException(
                gr.getStatus().getDisplayName(), "delete");
        }
        
        logger.info("Deleting Goods Receipt: {}", gr.getCode());
        grRepo.delete(gr);
    }

    @Override
    @Transactional(readOnly = true)
    public GoodsReceiptDTO getReceivingSummary(Long purchaseOrderId) {
        PurchaseOrder po = poRepo.findByIdWithItems(purchaseOrderId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase Order", purchaseOrderId));
        
        List<GoodsReceiptItemDTO> summaryItems = po.getItems().stream()
            .map(poItem -> {
                GoodsReceiptItemDTO itemDto = GoodsReceiptItemDTO.builder()
                    .purchaseOrderItemId(poItem.getId())
                    .productId(poItem.getProduct().getId())
                    .productName(poItem.getProduct().getName())
                    .productCode(poItem.getProduct().getCode())
                    .orderedQuantity(poItem.getQuantity())
                    .previouslyReceivedQuantity(poItem.getReceivedQuantity())
                    .remainingQuantity(poItem.getRemainingQuantity())
                    .receivedQuantity(0) // To be filled by user
                    .unitPrice(poItem.getUnitPrice())
                    .unit(poItem.getUnit())
                    .build();
                return itemDto;
            })
            .collect(Collectors.toList());
        
        return GoodsReceiptDTO.builder()
            .purchaseOrderId(po.getId())
            .purchaseOrderCode(po.getCode())
            .warehouseId(po.getWarehouse() != null ? po.getWarehouse().getId() : null)
            .warehouseName(po.getWarehouse() != null ? po.getWarehouse().getName() : null)
            .items(summaryItems)
            .build();
    }

    // Helper methods
    private String generateGRCode() {
        return "GR-" + System.currentTimeMillis();
    }

    private GoodsReceiptDTO toDto(GoodsReceipt gr) {
        List<GoodsReceiptItemDTO> itemDTOs = gr.getItems() != null
            ? gr.getItems().stream().map(this::toItemDto).collect(Collectors.toList())
            : new ArrayList<>();

        GoodsReceiptDTO dto = GoodsReceiptDTO.builder()
            .id(gr.getId())
            .code(gr.getCode())
            .purchaseOrderId(gr.getPurchaseOrder() != null ? gr.getPurchaseOrder().getId() : null)
            .purchaseOrderCode(gr.getPurchaseOrder() != null ? gr.getPurchaseOrder().getCode() : null)
            .warehouseId(gr.getWarehouse() != null ? gr.getWarehouse().getId() : null)
            .warehouseName(gr.getWarehouse() != null ? gr.getWarehouse().getName() : null)
            .status(gr.getStatus())
            .statusDisplayName(gr.getStatus() != null ? gr.getStatus().getDisplayName() : null)
            .receiptDate(gr.getReceiptDate())
            .confirmedDate(gr.getConfirmedDate())
            .deliveryNoteNumber(gr.getDeliveryNoteNumber())
            .invoiceNumber(gr.getInvoiceNumber())
            .totalAmount(gr.getTotalAmount())
            .notes(gr.getNotes())
            .createdBy(gr.getCreatedBy())
            .confirmedBy(gr.getConfirmedBy())
            .createdAt(gr.getCreatedAt())
            .items(itemDTOs)
            .build();

        dto.computeFields();
        return dto;
    }

    private GoodsReceiptItemDTO toItemDto(GoodsReceiptItem item) {
        return GoodsReceiptItemDTO.builder()
            .id(item.getId())
            .purchaseOrderItemId(item.getPurchaseOrderItem() != null ? item.getPurchaseOrderItem().getId() : null)
            .productId(item.getProduct() != null ? item.getProduct().getId() : null)
            .productName(item.getProduct() != null ? item.getProduct().getName() : null)
            .productCode(item.getProduct() != null ? item.getProduct().getCode() : null)
            .orderedQuantity(item.getOrderedQuantity())
            .receivedQuantity(item.getReceivedQuantity())
            .acceptedQuantity(item.getAcceptedQuantity())
            .rejectedQuantity(item.getRejectedQuantity())
            .unitPrice(item.getUnitPrice())
            .totalAmount(item.getTotalAmount())
            .unit(item.getUnit())
            .batchNumber(item.getBatchNumber())
            .expiryDate(item.getExpiryDate())
            .rejectionReason(item.getRejectionReason())
            .notes(item.getNotes())
            .build();
    }
}
