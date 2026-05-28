package com.distribution.service.impl;

import com.distribution.dto.ApprovalRequestDTO;
import com.distribution.dto.PurchaseOrderDTO;
import com.distribution.dto.PurchaseOrderItemDTO;
import com.distribution.exception.BusinessException;
import com.distribution.exception.InvalidStatusTransitionException;
import com.distribution.exception.ResourceNotFoundException;
import com.distribution.model.*;
import com.distribution.model.enums.PurchaseOrderStatus;
import com.distribution.repository.*;
import com.distribution.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of PurchaseOrderService
 * 
 * Business Rules:
 * - PO created with ORDER_OPEN status
 * - Only ORDER_OPEN POs can be edited or deleted
 * - Only Manager/Accountant can approve (enforced at controller level)
 * - PO status updates automatically based on receiving progress
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderServiceImpl.class);

    private final PurchaseOrderRepository orderRepo;
    private final PurchaseOrderItemRepository itemRepo;
    private final SupplierRepository supplierRepo;
    private final WarehouseRepository warehouseRepo;
    private final ProductRepository productRepo;

    @Override
    public PurchaseOrderDTO create(PurchaseOrderDTO dto) {
        logger.info("Creating new Purchase Order");
        
        // Validate supplier exists
        Supplier supplier = supplierRepo.findById(dto.getSupplierId())
            .orElseThrow(() -> new ResourceNotFoundException("Supplier", dto.getSupplierId()));
        
        // Get warehouse if provided
        Warehouse warehouse = null;
        if (dto.getWarehouseId() != null) {
            warehouse = warehouseRepo.findById(dto.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", dto.getWarehouseId()));
        }
        
        // Create PO entity
        PurchaseOrder po = PurchaseOrder.builder()
            .code(generatePOCode())
            .orderName(dto.getOrderName())
            .deliveryDate(dto.getDeliveryDate())
            .status(PurchaseOrderStatus.ORDER_OPEN)
            .supplier(supplier)
            .warehouse(warehouse)
            .taxType(dto.getTaxType())
            .shippingCost(dto.getShippingCost())
            .notes(dto.getNotes())
            .createdDate(LocalDate.now())
            .createdBy(dto.getCreatedBy())
            .items(new ArrayList<>())
            .build();
        
        // Add items
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (PurchaseOrderItemDTO itemDto : dto.getItems()) {
                Product product = productRepo.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemDto.getProductId()));
                
                PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(po)
                    .product(product)
                    .unit(itemDto.getUnit())
                    .quantity(itemDto.getQuantity())
                    .receivedQuantity(0)
                    .unitPrice(itemDto.getUnitPrice())
                    .costBeforeTax(itemDto.getCostBeforeTax())
                    .notes(itemDto.getNotes())
                    .build();
                
                po.getItems().add(item);
            }
        }
        
        // Calculate total
        po.recalculateTotal();
        
        // Save and return
        PurchaseOrder saved = orderRepo.save(po);
        logger.info("Created Purchase Order: {}", saved.getCode());
        
        return toDto(saved);
    }

    @Override
    public PurchaseOrderDTO update(Long id, PurchaseOrderDTO dto) {
        logger.info("Updating Purchase Order: {}", id);
        
        PurchaseOrder po = orderRepo.findByIdWithItems(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase Order", id));
        
        // Only allow updates when status is OPEN
        if (po.getStatus() != PurchaseOrderStatus.ORDER_OPEN) {
            throw new InvalidStatusTransitionException(
                po.getStatus().getDisplayName(), "update");
        }
        
        // Update basic fields
        po.setOrderName(dto.getOrderName());
        po.setDeliveryDate(dto.getDeliveryDate());
        po.setTaxType(dto.getTaxType());
        po.setShippingCost(dto.getShippingCost());
        po.setNotes(dto.getNotes());
        
        // Update supplier if changed
        if (dto.getSupplierId() != null && 
            (po.getSupplier() == null || !po.getSupplier().getId().equals(dto.getSupplierId()))) {
            Supplier supplier = supplierRepo.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", dto.getSupplierId()));
            po.setSupplier(supplier);
        }
        
        // Update warehouse if changed
        if (dto.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepo.findById(dto.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", dto.getWarehouseId()));
            po.setWarehouse(warehouse);
        }
        
        // Replace items
        if (dto.getItems() != null) {
            // Clear existing items
            po.getItems().clear();
            
            // Add new items
            for (PurchaseOrderItemDTO itemDto : dto.getItems()) {
                Product product = productRepo.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemDto.getProductId()));
                
                PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(po)
                    .product(product)
                    .unit(itemDto.getUnit())
                    .quantity(itemDto.getQuantity())
                    .receivedQuantity(0)
                    .unitPrice(itemDto.getUnitPrice())
                    .costBeforeTax(itemDto.getCostBeforeTax())
                    .notes(itemDto.getNotes())
                    .build();
                
                po.getItems().add(item);
            }
        }
        
        // Recalculate total
        po.recalculateTotal();
        
        PurchaseOrder saved = orderRepo.save(po);
        logger.info("Updated Purchase Order: {}", saved.getCode());
        
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDTO getById(Long id) {
        PurchaseOrder po = orderRepo.findByIdWithItems(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase Order", id));
        return toDto(po);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDTO getByCode(String code) {
        PurchaseOrder po = orderRepo.findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase Order", code));
        return toDto(po);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDTO> getAll() {
        return orderRepo.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDTO> getByStatus(PurchaseOrderStatus status) {
        return orderRepo.findByStatus(status).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDTO> getReadyForGoodsReceipt() {
        return orderRepo.findReadyForGoodsReceipt().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDTO> getPendingApproval() {
        return orderRepo.findPendingApproval().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDTO> getBySupplierId(Long supplierId) {
        return orderRepo.findBySupplierId(supplierId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDTO> getByDateRange(LocalDate startDate, LocalDate endDate) {
        return orderRepo.findByCreatedDateBetween(startDate, endDate).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        PurchaseOrder po = orderRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase Order", id));
        
        // Only allow delete when status is OPEN
        if (po.getStatus() != PurchaseOrderStatus.ORDER_OPEN) {
            throw new InvalidStatusTransitionException(
                po.getStatus().getDisplayName(), "delete");
        }
        
        logger.info("Deleting Purchase Order: {}", po.getCode());
        orderRepo.delete(po);
    }

    @Override
    public PurchaseOrderDTO processApproval(Long id, ApprovalRequestDTO approvalRequest) {
        if (approvalRequest.isApproval()) {
            return approve(id, approvalRequest.getApprovedBy());
        } else if (approvalRequest.isRejection()) {
            return reject(id, approvalRequest.getApprovedBy(), approvalRequest.getReason());
        } else {
            throw new BusinessException("Invalid approval action: " + approvalRequest.getAction());
        }
    }

    @Override
    public PurchaseOrderDTO approve(Long id, Long approvedBy) {
        logger.info("Approving Purchase Order: {} by user: {}", id, approvedBy);
        
        PurchaseOrder po = orderRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase Order", id));
        
        // Validate status allows approval
        if (!po.getStatus().canApprove()) {
            throw new InvalidStatusTransitionException(
                po.getStatus().getDisplayName(), "approve");
        }
        
        // Update status
        po.setStatus(PurchaseOrderStatus.ORDER_APPROVED);
        po.setApprovedDate(LocalDateTime.now());
        po.setApprovedBy(approvedBy);
        
        PurchaseOrder saved = orderRepo.save(po);
        logger.info("Approved Purchase Order: {}", saved.getCode());
        
        return toDto(saved);
    }

    @Override
    public PurchaseOrderDTO reject(Long id, Long rejectedBy, String reason) {
        logger.info("Rejecting Purchase Order: {} by user: {}", id, rejectedBy);
        
        PurchaseOrder po = orderRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase Order", id));
        
        // Validate status allows rejection
        if (!po.getStatus().canApprove()) {
            throw new InvalidStatusTransitionException(
                po.getStatus().getDisplayName(), "reject");
        }
        
        // Update status
        po.setStatus(PurchaseOrderStatus.ORDER_CANCELLED);
        po.setRejectionReason(reason);
        po.setApprovedBy(rejectedBy);
        po.setApprovedDate(LocalDateTime.now());
        
        PurchaseOrder saved = orderRepo.save(po);
        logger.info("Rejected Purchase Order: {}", saved.getCode());
        
        return toDto(saved);
    }

    @Override
    public PurchaseOrderDTO cancel(Long id, String reason) {
        logger.info("Cancelling Purchase Order: {}", id);
        
        PurchaseOrder po = orderRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase Order", id));
        
        // Validate status allows cancellation
        if (!po.getStatus().canCancel()) {
            throw new InvalidStatusTransitionException(
                po.getStatus().getDisplayName(), "cancel");
        }
        
        // Update status
        po.setStatus(PurchaseOrderStatus.ORDER_CANCELLED);
        po.setRejectionReason(reason);
        
        PurchaseOrder saved = orderRepo.save(po);
        logger.info("Cancelled Purchase Order: {}", saved.getCode());
        
        return toDto(saved);
    }

    @Override
    public PurchaseOrderDTO save(PurchaseOrderDTO dto) {
        PurchaseOrder po = orderRepo.findById(dto.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Purchase Order", dto.getId()));
        
        po.setStatus(dto.getStatus());
        
        return toDto(orderRepo.save(po));
    }

    @Override
    public void updateReceivingStatus(Long poId) {
        logger.info("Updating receiving status for PO: {}", poId);
        
        PurchaseOrder po = orderRepo.findByIdWithItems(poId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase Order", poId));
        
        // Check if fully received
        if (po.isFullyReceived()) {
            po.setStatus(PurchaseOrderStatus.ORDER_COMPLETED);
            po.setCompletedDate(LocalDateTime.now());
            logger.info("PO {} fully received, status updated to COMPLETED", po.getCode());
        } else if (po.isPartiallyReceived()) {
            po.setStatus(PurchaseOrderStatus.ORDER_PARTIALLY_RECEIVED);
            logger.info("PO {} partially received", po.getCode());
        }
        
        orderRepo.save(po);
    }

    // Helper methods
    private String generatePOCode() {
        return "PO-" + System.currentTimeMillis();
    }

    private PurchaseOrderDTO toDto(PurchaseOrder po) {
        List<PurchaseOrderItemDTO> itemDTOs = po.getItems() != null
            ? po.getItems().stream().map(this::toItemDto).collect(Collectors.toList())
            : new ArrayList<>();

        PurchaseOrderDTO dto = PurchaseOrderDTO.builder()
            .id(po.getId())
            .code(po.getCode())
            .orderName(po.getOrderName())
            .deliveryDate(po.getDeliveryDate())
            .status(po.getStatus())
            .statusDisplayName(po.getStatus() != null ? po.getStatus().getDisplayName() : null)
            .supplierId(po.getSupplier() != null ? po.getSupplier().getId() : null)
            .supplierName(po.getSupplier() != null ? po.getSupplier().getName() : null)
            .supplierCode(po.getSupplier() != null ? po.getSupplier().getCode() : null)
            .warehouseId(po.getWarehouse() != null ? po.getWarehouse().getId() : null)
            .warehouseName(po.getWarehouse() != null ? po.getWarehouse().getName() : null)
            .warehouseCode(po.getWarehouse() != null ? po.getWarehouse().getCode() : null)
            .taxType(po.getTaxType())
            .shippingCost(po.getShippingCost())
            .totalAmount(po.getTotalAmount())
            .createdDate(po.getCreatedDate())
            .approvedDate(po.getApprovedDate())
            .completedDate(po.getCompletedDate())
            .notes(po.getNotes())
            .rejectionReason(po.getRejectionReason())
            .createdBy(po.getCreatedBy())
            .approvedBy(po.getApprovedBy())
            .items(itemDTOs)
            .build();

        dto.computeFields();
        return dto;
    }

    private PurchaseOrderItemDTO toItemDto(PurchaseOrderItem item) {
        PurchaseOrderItemDTO dto = PurchaseOrderItemDTO.builder()
            .id(item.getId())
            .productId(item.getProduct() != null ? item.getProduct().getId() : null)
            .productName(item.getProduct() != null ? item.getProduct().getName() : null)
            .productCode(item.getProduct() != null ? item.getProduct().getCode() : null)
            .unit(item.getUnit())
            .quantity(item.getQuantity())
            .receivedQuantity(item.getReceivedQuantity())
            .unitPrice(item.getUnitPrice())
            .costBeforeTax(item.getCostBeforeTax())
            .amountBeforeTax(item.getAmountBeforeTax())
            .taxAmount(item.getTaxAmount())
            .totalAmount(item.getTotalAmount())
            .notes(item.getNotes())
            .build();

        dto.computeFields();
        return dto;
    }
}
