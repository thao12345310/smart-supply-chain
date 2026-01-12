package com.distribution.service.impl;

import com.distribution.dto.ApprovalRequestDTO;
import com.distribution.dto.SalesOrderDTO;
import com.distribution.dto.SalesOrderItemDTO;
import com.distribution.exception.BusinessException;
import com.distribution.exception.ResourceNotFoundException;
import com.distribution.model.*;
import com.distribution.model.enums.SalesOrderStatus;
import com.distribution.model.enums.PaymentStatus;
import com.distribution.repository.*;
import com.distribution.service.SalesOrderService;
import com.distribution.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final CustomerRepository customerRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    @Override
    public SalesOrderDTO create(SalesOrderDTO dto) {
        log.info("Creating sales order for customer: {}", dto.getCustomerId());
        
        // Generate code if not provided
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            dto.setCode(SalesOrder.generateCode());
        }
        
        // Check for duplicate code
        if (salesOrderRepository.existsByCode(dto.getCode())) {
            throw new BusinessException("Sales Order with code " + dto.getCode() + " already exists");
        }
        
        // Validate customer
        Customer customer = customerRepository.findById(dto.getCustomerId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + dto.getCustomerId()));
        
        if (!customer.getActive()) {
            throw new BusinessException("Cannot create order for inactive customer");
        }
        
        // Build sales order
        SalesOrder salesOrder = SalesOrder.builder()
            .code(dto.getCode())
            .orderName(dto.getOrderName())
            .status(SalesOrderStatus.ORDER_OPEN)
            .orderDate(dto.getOrderDate() != null ? dto.getOrderDate() : LocalDate.now())
            .expectedDeliveryDate(dto.getExpectedDeliveryDate())
            .customer(customer)
            .discountAmount(dto.getDiscountAmount())
            .shippingCost(dto.getShippingCost())
            .notes(dto.getNotes())
            .createdBy(dto.getCreatedBy())
            .paymentStatus(PaymentStatus.UNPAID)
            .items(new ArrayList<>())
            .build();
        
        // Set delivery address
        if (dto.getDeliveryAddressId() != null) {
            DeliveryAddress address = deliveryAddressRepository.findById(dto.getDeliveryAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery address not found"));
            salesOrder.setDeliveryAddress(address);
        }
        
        // Set warehouse
        if (dto.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
            salesOrder.setWarehouse(warehouse);
        }
        
        // Add items
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (SalesOrderItemDTO itemDto : dto.getItems()) {
                SalesOrderItem item = createItem(itemDto);
                salesOrder.addItem(item);
            }
        }
        
        salesOrder.recalculateTotal();
        salesOrder = salesOrderRepository.save(salesOrder);
        
        log.info("Sales Order created with ID: {} and code: {}", salesOrder.getId(), salesOrder.getCode());
        return mapToDTO(salesOrder);
    }

    @Override
    public SalesOrderDTO update(Long id, SalesOrderDTO dto) {
        log.info("Updating sales order ID: {}", id);
        
        SalesOrder salesOrder = salesOrderRepository.findByIdWithItems(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with ID: " + id));
        
        // Only allow updates when status is OPEN
        if (salesOrder.getStatus() != SalesOrderStatus.ORDER_OPEN) {
            throw new BusinessException("Can only update Sales Order in OPEN status");
        }
        
        // Update basic fields
        salesOrder.setOrderName(dto.getOrderName());
        salesOrder.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        salesOrder.setDiscountAmount(dto.getDiscountAmount());
        salesOrder.setShippingCost(dto.getShippingCost());
        salesOrder.setNotes(dto.getNotes());
        
        // Update delivery address
        if (dto.getDeliveryAddressId() != null) {
            DeliveryAddress address = deliveryAddressRepository.findById(dto.getDeliveryAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery address not found"));
            salesOrder.setDeliveryAddress(address);
        }
        
        // Update warehouse
        if (dto.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
            salesOrder.setWarehouse(warehouse);
        }
        
        // Update items
        if (dto.getItems() != null) {
            salesOrder.getItems().clear();
            for (SalesOrderItemDTO itemDto : dto.getItems()) {
                SalesOrderItem item = createItem(itemDto);
                salesOrder.addItem(item);
            }
        }
        
        salesOrder.recalculateTotal();
        salesOrder = salesOrderRepository.save(salesOrder);
        
        return mapToDTO(salesOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOrderDTO getById(Long id) {
        SalesOrder salesOrder = salesOrderRepository.findByIdWithItemsAndGoodsIssues(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with ID: " + id));
        return mapToDTO(salesOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOrderDTO getByCode(String code) {
        SalesOrder salesOrder = salesOrderRepository.findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with code: " + code));
        return mapToDTO(salesOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrderDTO> getAll() {
        return salesOrderRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrderDTO> getByStatus(SalesOrderStatus status) {
        return salesOrderRepository.findByStatus(status).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrderDTO> getReadyForGoodsIssue() {
        return salesOrderRepository.findReadyForGoodsIssue().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrderDTO> getPendingApproval() {
        return salesOrderRepository.findPendingApproval().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrderDTO> getByCustomerId(Long customerId) {
        return salesOrderRepository.findByCustomerId(customerId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrderDTO> getByDateRange(LocalDate startDate, LocalDate endDate) {
        return salesOrderRepository.findByDateRange(startDate, endDate).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrderDTO> search(String query) {
        return salesOrderRepository.search(query).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting sales order ID: {}", id);
        
        SalesOrder salesOrder = salesOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with ID: " + id));
        
        if (salesOrder.getStatus() != SalesOrderStatus.ORDER_OPEN) {
            throw new BusinessException("Can only delete Sales Order in OPEN status");
        }
        
        salesOrderRepository.delete(salesOrder);
    }

    @Override
    public SalesOrderDTO processApproval(Long id, ApprovalRequestDTO approvalRequest) {
        if (approvalRequest.isApproval()) {
            return approve(id, approvalRequest.getApprovedBy());
        } else {
            return reject(id, approvalRequest.getApprovedBy(), approvalRequest.getReason());
        }
    }

    @Override
    public SalesOrderDTO approve(Long id, Long approvedBy) {
        log.info("Approving sales order ID: {}", id);
        
        SalesOrder salesOrder = salesOrderRepository.findByIdWithItems(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with ID: " + id));
        
        if (!salesOrder.getStatus().canApprove()) {
            throw new BusinessException("Sales Order cannot be approved from status: " + salesOrder.getStatus());
        }
        
        // Validate inventory availability
        Long warehouseId = salesOrder.getWarehouse() != null ? salesOrder.getWarehouse().getId() : null;
        for (SalesOrderItem item : salesOrder.getItems()) {
            if (warehouseId != null) {
                Integer available = inventoryService.getAvailableQuantity(item.getProduct().getId(), warehouseId);
                if (available < item.getQuantity()) {
                    throw new BusinessException("Insufficient inventory for product: " + item.getProduct().getName() + 
                        ". Available: " + available + ", Ordered: " + item.getQuantity());
                }
            }
        }
        
        // Reserve inventory
        for (SalesOrderItem item : salesOrder.getItems()) {
            if (warehouseId != null) {
                inventoryService.reserveInventory(item.getProduct().getId(), warehouseId, item.getQuantity());
            }
        }
        
        salesOrder.setStatus(SalesOrderStatus.ORDER_APPROVED);
        salesOrder.setApprovedDate(LocalDateTime.now());
        salesOrder.setApprovedBy(approvedBy);
        
        salesOrder = salesOrderRepository.save(salesOrder);
        log.info("Sales Order {} approved", salesOrder.getCode());
        
        return mapToDTO(salesOrder);
    }

    @Override
    public SalesOrderDTO reject(Long id, Long rejectedBy, String reason) {
        log.info("Rejecting sales order ID: {}", id);
        
        SalesOrder salesOrder = salesOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with ID: " + id));
        
        if (!salesOrder.getStatus().canApprove()) {
            throw new BusinessException("Sales Order cannot be rejected from status: " + salesOrder.getStatus());
        }
        
        salesOrder.setStatus(SalesOrderStatus.ORDER_CANCELLED);
        salesOrder.setRejectionReason(reason);
        salesOrder.setApprovedBy(rejectedBy);
        salesOrder.setApprovedDate(LocalDateTime.now());
        
        salesOrder = salesOrderRepository.save(salesOrder);
        log.info("Sales Order {} rejected", salesOrder.getCode());
        
        return mapToDTO(salesOrder);
    }

    @Override
    public SalesOrderDTO cancel(Long id, String reason) {
        log.info("Cancelling sales order ID: {}", id);
        
        SalesOrder salesOrder = salesOrderRepository.findByIdWithItems(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with ID: " + id));
        
        if (!salesOrder.getStatus().canCancel()) {
            throw new BusinessException("Sales Order cannot be cancelled from status: " + salesOrder.getStatus());
        }
        
        // Release reserved inventory if order was approved
        if (salesOrder.getStatus() == SalesOrderStatus.ORDER_APPROVED) {
            Long warehouseId = salesOrder.getWarehouse() != null ? salesOrder.getWarehouse().getId() : null;
            if (warehouseId != null) {
                for (SalesOrderItem item : salesOrder.getItems()) {
                    int remaining = item.getQuantity() - item.getDeliveredQuantity();
                    if (remaining > 0) {
                        inventoryService.releaseReservedInventory(item.getProduct().getId(), warehouseId, remaining);
                    }
                }
            }
        }
        
        salesOrder.setStatus(SalesOrderStatus.ORDER_CANCELLED);
        salesOrder.setRejectionReason(reason);
        
        salesOrder = salesOrderRepository.save(salesOrder);
        log.info("Sales Order {} cancelled", salesOrder.getCode());
        
        return mapToDTO(salesOrder);
    }

    @Override
    public void updateDeliveryStatus(Long soId) {
        log.info("Updating delivery status for SO ID: {}", soId);
        
        SalesOrder salesOrder = salesOrderRepository.findByIdWithItems(soId)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with ID: " + soId));
        
        if (salesOrder.isFullyDelivered()) {
            salesOrder.setStatus(SalesOrderStatus.ORDER_COMPLETED);
            salesOrder.setCompletedDate(LocalDateTime.now());
            log.info("Sales Order {} marked as COMPLETED", salesOrder.getCode());
        } else if (salesOrder.isPartiallyDelivered()) {
            salesOrder.setStatus(SalesOrderStatus.ORDER_PARTIALLY_DELIVERED);
            log.info("Sales Order {} marked as PARTIALLY_DELIVERED", salesOrder.getCode());
        }
        
        salesOrderRepository.save(salesOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOrderDTO getIssueSummary(Long soId) {
        SalesOrder salesOrder = salesOrderRepository.findByIdWithItems(soId)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with ID: " + soId));
        
        SalesOrderDTO dto = mapToDTO(salesOrder);
        
        // Compute remaining quantities for each item
        if (dto.getItems() != null) {
            for (SalesOrderItemDTO itemDto : dto.getItems()) {
                itemDto.computeFields();
                // Get available inventory
                Long warehouseId = salesOrder.getWarehouse() != null ? salesOrder.getWarehouse().getId() : null;
                if (warehouseId != null && itemDto.getProductId() != null) {
                    Integer available = inventoryService.getAvailableQuantity(itemDto.getProductId(), warehouseId);
                    itemDto.setAvailableQuantity(available);
                }
            }
        }
        
        return dto;
    }

    // Helper methods

    private SalesOrderItem createItem(SalesOrderItemDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + dto.getProductId()));
        
        return SalesOrderItem.builder()
            .product(product)
            .unit(dto.getUnit())
            .quantity(dto.getQuantity())
            .deliveredQuantity(0)
            .unitPrice(dto.getUnitPrice())
            .discountPercent(dto.getDiscountPercent())
            .taxPercent(dto.getTaxPercent())
            .notes(dto.getNotes())
            .build();
    }

    private SalesOrderDTO mapToDTO(SalesOrder salesOrder) {
        SalesOrderDTO dto = SalesOrderDTO.builder()
            .id(salesOrder.getId())
            .code(salesOrder.getCode())
            .orderName(salesOrder.getOrderName())
            .orderDate(salesOrder.getOrderDate())
            .expectedDeliveryDate(salesOrder.getExpectedDeliveryDate())
            .status(salesOrder.getStatus())
            .paymentStatus(salesOrder.getPaymentStatus())
            .totalAmount(salesOrder.getTotalAmount())
            .taxAmount(salesOrder.getTaxAmount())
            .discountAmount(salesOrder.getDiscountAmount())
            .shippingCost(salesOrder.getShippingCost())
            .grandTotal(salesOrder.getGrandTotal())
            .approvedDate(salesOrder.getApprovedDate())
            .completedDate(salesOrder.getCompletedDate())
            .notes(salesOrder.getNotes())
            .rejectionReason(salesOrder.getRejectionReason())
            .createdBy(salesOrder.getCreatedBy())
            .approvedBy(salesOrder.getApprovedBy())
            .createdAt(salesOrder.getCreatedAt())
            .build();
        
        // Customer info
        if (salesOrder.getCustomer() != null) {
            dto.setCustomerId(salesOrder.getCustomer().getId());
            dto.setCustomerName(salesOrder.getCustomer().getName());
            dto.setCustomerCode(salesOrder.getCustomer().getCode());
        }
        
        // Delivery address
        if (salesOrder.getDeliveryAddress() != null) {
            dto.setDeliveryAddressId(salesOrder.getDeliveryAddress().getId());
            dto.setDeliveryAddressText(salesOrder.getDeliveryAddress().getFullAddress());
        }
        
        // Warehouse
        if (salesOrder.getWarehouse() != null) {
            dto.setWarehouseId(salesOrder.getWarehouse().getId());
            dto.setWarehouseName(salesOrder.getWarehouse().getName());
            dto.setWarehouseCode(salesOrder.getWarehouse().getCode());
        }
        
        // Items
        if (salesOrder.getItems() != null) {
            dto.setItems(salesOrder.getItems().stream()
                .map(this::mapItemToDTO)
                .collect(Collectors.toList()));
        }
        
        dto.computeFields();
        return dto;
    }

    private SalesOrderItemDTO mapItemToDTO(SalesOrderItem item) {
        SalesOrderItemDTO dto = SalesOrderItemDTO.builder()
            .id(item.getId())
            .salesOrderId(item.getSalesOrder().getId())
            .productId(item.getProduct().getId())
            .productCode(item.getProduct().getCode())
            .productName(item.getProduct().getName())
            .unit(item.getUnit())
            .quantity(item.getQuantity())
            .deliveredQuantity(item.getDeliveredQuantity())
            .unitPrice(item.getUnitPrice())
            .discountPercent(item.getDiscountPercent())
            .taxPercent(item.getTaxPercent())
            .amountBeforeTax(item.getAmountBeforeTax())
            .taxAmount(item.getTaxAmount())
            .totalAmount(item.getTotalAmount())
            .notes(item.getNotes())
            .build();
        dto.computeFields();
        return dto;
    }
}
