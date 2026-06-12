package com.distribution.service.impl;

import com.distribution.dto.GoodsIssueDTO;
import com.distribution.dto.GoodsIssueItemDTO;
import com.distribution.dto.InventoryDTO;
import com.distribution.dto.SalesInvoiceDTO;
import com.distribution.exception.BusinessException;
import com.distribution.exception.ResourceNotFoundException;
import com.distribution.model.*;
import com.distribution.model.enums.GoodsIssueStatus;
import com.distribution.model.enums.SalesInvoiceStatus;
import com.distribution.repository.*;
import com.distribution.service.GoodsIssueService;
import com.distribution.service.InventoryService;
import com.distribution.service.SalesOrderService;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GoodsIssueServiceImpl implements GoodsIssueService {

    private final GoodsIssueRepository goodsIssueRepository;
    private final GoodsIssueItemRepository goodsIssueItemRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final InventoryService inventoryService;
    private final SalesOrderService salesOrderService;
    private final InventoryLotRepository inventoryLotRepository;

    @Override
    public GoodsIssueDTO create(GoodsIssueDTO dto) {
        log.info("Creating goods issue for sales order: {}", dto.getSalesOrderId());
        
        // Generate code if not provided
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            dto.setCode(GoodsIssue.generateCode());
        }
        
        // Check for duplicate code
        if (goodsIssueRepository.existsByCode(dto.getCode())) {
            throw new BusinessException("Goods Issue with code " + dto.getCode() + " already exists");
        }
        
        // Validate sales order
        SalesOrder salesOrder = salesOrderRepository.findByIdWithItems(dto.getSalesOrderId())
            .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with ID: " + dto.getSalesOrderId()));
        
        if (!salesOrder.getStatus().canIssueGoods()) {
            throw new BusinessException("Cannot create Goods Issue for Sales Order in status: " + salesOrder.getStatus());
        }
        
        // Build goods issue
        GoodsIssue goodsIssue = GoodsIssue.builder()
            .code(dto.getCode())
            .status(GoodsIssueStatus.DRAFT)
            .issueDate(dto.getIssueDate() != null ? dto.getIssueDate() : LocalDate.now())
            .salesOrder(salesOrder)
            .deliveryNoteNumber(dto.getDeliveryNoteNumber())
            .shippingMethod(dto.getShippingMethod())
            .trackingNumber(dto.getTrackingNumber())
            .carrierName(dto.getCarrierName())
            .notes(dto.getNotes())
            .createdBy(dto.getCreatedBy())
            .items(new ArrayList<>())
            .build();
        
        // Set warehouse (default to SO warehouse)
        if (dto.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
            goodsIssue.setWarehouse(warehouse);
        } else if (salesOrder.getWarehouse() != null) {
            goodsIssue.setWarehouse(salesOrder.getWarehouse());
        }
        
        // Set delivery address (default to SO delivery address)
        if (dto.getDeliveryAddressId() != null) {
            DeliveryAddress address = deliveryAddressRepository.findById(dto.getDeliveryAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery address not found"));
            goodsIssue.setDeliveryAddress(address);
        } else if (salesOrder.getDeliveryAddress() != null) {
            goodsIssue.setDeliveryAddress(salesOrder.getDeliveryAddress());
        }
        
        // Add items
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (GoodsIssueItemDTO itemDto : dto.getItems()) {
                GoodsIssueItem item = createItem(itemDto, salesOrder);
                goodsIssue.addItem(item);
            }
        }
        
        goodsIssue.recalculateTotal();
        goodsIssue = goodsIssueRepository.save(goodsIssue);
        
        log.info("Goods Issue created with ID: {} and code: {}", goodsIssue.getId(), goodsIssue.getCode());
        return mapToDTO(goodsIssue);
    }

    @Override
    public GoodsIssueDTO update(Long id, GoodsIssueDTO dto) {
        log.info("Updating goods issue ID: {}", id);
        
        GoodsIssue goodsIssue = goodsIssueRepository.findByIdWithItems(id)
            .orElseThrow(() -> new ResourceNotFoundException("Goods Issue not found with ID: " + id));
        
        if (goodsIssue.getStatus() != GoodsIssueStatus.DRAFT) {
            throw new BusinessException("Can only update Goods Issue in DRAFT status");
        }
        
        // Update fields
        goodsIssue.setIssueDate(dto.getIssueDate());
        goodsIssue.setDeliveryNoteNumber(dto.getDeliveryNoteNumber());
        goodsIssue.setShippingMethod(dto.getShippingMethod());
        goodsIssue.setTrackingNumber(dto.getTrackingNumber());
        goodsIssue.setCarrierName(dto.getCarrierName());
        goodsIssue.setNotes(dto.getNotes());
        
        // Update items
        if (dto.getItems() != null) {
            goodsIssue.getItems().clear();
            SalesOrder salesOrder = goodsIssue.getSalesOrder();
            for (GoodsIssueItemDTO itemDto : dto.getItems()) {
                GoodsIssueItem item = createItem(itemDto, salesOrder);
                goodsIssue.addItem(item);
            }
        }
        
        goodsIssue.recalculateTotal();
        goodsIssue = goodsIssueRepository.save(goodsIssue);
        
        return mapToDTO(goodsIssue);
    }

    @Override
    @Transactional(readOnly = true)
    public GoodsIssueDTO getById(Long id) {
        GoodsIssue goodsIssue = goodsIssueRepository.findByIdWithItemsAndInvoice(id)
            .orElseThrow(() -> new ResourceNotFoundException("Goods Issue not found with ID: " + id));
        return mapToDTO(goodsIssue);
    }

    @Override
    @Transactional(readOnly = true)
    public GoodsIssueDTO getByCode(String code) {
        GoodsIssue goodsIssue = goodsIssueRepository.findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Goods Issue not found with code: " + code));
        return mapToDTO(goodsIssue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsIssueDTO> getAll() {
        return goodsIssueRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsIssueDTO> getByStatus(GoodsIssueStatus status) {
        return goodsIssueRepository.findByStatus(status).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsIssueDTO> getBySalesOrderId(Long salesOrderId) {
        return goodsIssueRepository.findBySalesOrderId(salesOrderId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsIssueDTO> getByDateRange(LocalDate startDate, LocalDate endDate) {
        return goodsIssueRepository.findByDateRange(startDate, endDate).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsIssueDTO> getDraft() {
        return goodsIssueRepository.findDraft().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsIssueDTO> search(String query) {
        return goodsIssueRepository.search(query).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting goods issue ID: {}", id);
        
        GoodsIssue goodsIssue = goodsIssueRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Goods Issue not found with ID: " + id));
        
        if (goodsIssue.getStatus() != GoodsIssueStatus.DRAFT) {
            throw new BusinessException("Can only delete Goods Issue in DRAFT status");
        }
        
        goodsIssueRepository.delete(goodsIssue);
    }

    @Override
    public GoodsIssueDTO confirm(Long id, Long confirmedBy) {
        log.info("Confirming goods issue ID: {}", id);
        
        GoodsIssue goodsIssue = goodsIssueRepository.findByIdWithItems(id)
            .orElseThrow(() -> new ResourceNotFoundException("Goods Issue not found with ID: " + id));
        
        if (!goodsIssue.getStatus().canConfirm()) {
            throw new BusinessException("Goods Issue cannot be confirmed from status: " + goodsIssue.getStatus());
        }
        
        SalesOrder salesOrder = goodsIssue.getSalesOrder();
        Long warehouseId = goodsIssue.getWarehouse() != null ? goodsIssue.getWarehouse().getId() : null;
        
        // Validate inventory and issue each item
        for (GoodsIssueItem item : goodsIssue.getItems()) {
            // Check physical inventory on hand.
            // Lưu ý: KHÔNG dùng available (= onHand - reserved) vì hàng đã được giữ chỗ (reserve)
            // cho chính đơn bán này lúc duyệt đơn; dùng available sẽ trừ trùng và báo thiếu sai.
            if (warehouseId != null) {
                InventoryDTO inv = inventoryService.getByProductAndWarehouse(item.getProduct().getId(), warehouseId);
                int onHand = (inv != null && inv.getQuantityOnHand() != null) ? inv.getQuantityOnHand() : 0;
                if (onHand < item.getIssuedQuantity()) {
                    throw new BusinessException("Insufficient inventory for product: " + item.getProduct().getName() +
                        ". Available: " + onHand + ", Requested: " + item.getIssuedQuantity());
                }
            }
            
            // Validate against SO remaining quantity
            SalesOrderItem soItem = item.getSalesOrderItem();
            int remainingToDeliver = soItem.getQuantity() - soItem.getDeliveredQuantity();
            if (item.getIssuedQuantity() > remainingToDeliver) {
                throw new BusinessException("Issue quantity exceeds remaining quantity for product: " + 
                    item.getProduct().getName() + ". Remaining: " + remainingToDeliver);
            }
        }
        
        // Process inventory and update SO items
        for (GoodsIssueItem item : goodsIssue.getItems()) {
            // FEFO lot deduction (chỉ khi warehouse xác định)
            if (warehouseId != null) {
                List<InventoryLot> availableLots = inventoryLotRepository.findAvailableLotsFEFO(
                    item.getProduct().getId(), warehouseId);

                // Sản phẩm có quản lý lô nhưng toàn bộ lô còn tồn đã hết HSD → chặn xuất
                if (availableLots.isEmpty()
                        && inventoryLotRepository.hasLotsWithStock(item.getProduct().getId(), warehouseId)) {
                    throw new BusinessException(
                        "Sản phẩm " + item.getProduct().getName() +
                        ": tồn kho chỉ còn lô đã hết hạn sử dụng, không thể xuất.");
                }

                if (!availableLots.isEmpty()) {
                    // Có lot data → validate và trừ theo FEFO
                    BigDecimal totalLotAvailable = availableLots.stream()
                        .map(InventoryLot::getQuantityRemaining)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal issuedQty = BigDecimal.valueOf(item.getIssuedQuantity());

                    if (totalLotAvailable.compareTo(issuedQty) < 0) {
                        throw new BusinessException(
                            "Sản phẩm " + item.getProduct().getName() +
                            ": không đủ tồn kho theo lô. Cần: " + item.getIssuedQuantity() +
                            ", Còn: " + totalLotAvailable);
                    }

                    BigDecimal stillToIssue = issuedQty;
                    InventoryLot firstLot = null;

                    for (InventoryLot lot : availableLots) {
                        if (stillToIssue.compareTo(BigDecimal.ZERO) <= 0) break;
                        if (firstLot == null) firstLot = lot;

                        BigDecimal take = stillToIssue.min(lot.getQuantityRemaining());
                        lot.setQuantityRemaining(lot.getQuantityRemaining().subtract(take));
                        inventoryLotRepository.save(lot);
                        stillToIssue = stillToIssue.subtract(take);
                    }

                    // Ghi nhận lot đầu tiên (expiry sớm nhất) vào item để hiển thị
                    if (firstLot != null) {
                        item.setBatchNumber(firstLot.getLotNumber());
                        item.setExpiryDate(firstLot.getExpiryDate());
                    }
                    log.info("FEFO: đã trừ {} đơn vị sản phẩm {} từ {} lô tại kho {}",
                        item.getIssuedQuantity(), item.getProduct().getName(),
                        availableLots.size(), warehouseId);
                }
                // Nếu không có lot → fall through, chỉ dùng inventory cũ (backward compat)

                // Decrease inventory (legacy)
                inventoryService.decreaseInventory(
                    item.getProduct().getId(),
                    warehouseId,
                    item.getIssuedQuantity(),
                    "GOODS_ISSUE",
                    goodsIssue.getId(),
                    goodsIssue.getCode()
                );

                // Release reservation
                inventoryService.releaseReservedInventory(
                    item.getProduct().getId(),
                    warehouseId,
                    item.getIssuedQuantity()
                );
            }

            // Update SO item delivered quantity
            SalesOrderItem soItem = item.getSalesOrderItem();
            soItem.addDeliveredQuantity(item.getIssuedQuantity());
            salesOrderItemRepository.save(soItem);
        }
        
        // Update goods issue status
        goodsIssue.setStatus(GoodsIssueStatus.CONFIRMED);
        goodsIssue.setConfirmedDate(LocalDateTime.now());
        goodsIssue.setConfirmedBy(confirmedBy);
        
        goodsIssue = goodsIssueRepository.save(goodsIssue);
        
        // Create invoice
        createInvoice(goodsIssue);
        
        // Update SO status
        salesOrderService.updateDeliveryStatus(salesOrder.getId());
        
        log.info("Goods Issue {} confirmed", goodsIssue.getCode());
        return mapToDTO(goodsIssue);
    }

    @Override
    public GoodsIssueDTO cancel(Long id) {
        log.info("Cancelling goods issue ID: {}", id);
        
        GoodsIssue goodsIssue = goodsIssueRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Goods Issue not found with ID: " + id));
        
        if (!goodsIssue.getStatus().canCancel()) {
            throw new BusinessException("Goods Issue cannot be cancelled from status: " + goodsIssue.getStatus());
        }
        
        goodsIssue.setStatus(GoodsIssueStatus.CANCELLED);
        goodsIssue = goodsIssueRepository.save(goodsIssue);
        
        log.info("Goods Issue {} cancelled", goodsIssue.getCode());
        return mapToDTO(goodsIssue);
    }

    @Override
    public void restockFromFailedDelivery(String goodsIssueCode) {
        GoodsIssue gi = goodsIssueRepository.findByCode(goodsIssueCode).orElse(null);
        if (gi == null) {
            log.warn("Hoàn hàng giao thất bại: không tìm thấy phiếu xuất {}", goodsIssueCode);
            return;
        }
        Long warehouseId = gi.getWarehouse() != null ? gi.getWarehouse().getId() : null;
        for (GoodsIssueItem item : gi.getItems()) {
            int qty = item.getIssuedQuantity() != null ? item.getIssuedQuantity() : 0;
            if (qty <= 0) continue;

            if (warehouseId != null) {
                // Hoàn tồn on-hand (unitCost = null để không làm lệch giá vốn bình quân)
                inventoryService.addStock(item.getProduct().getId(), warehouseId, qty, null,
                    "DELIVERY_FAILED", gi.getId(), gi.getCode(), gi.getConfirmedBy());
                // Hoàn vào lô FEFO nếu sản phẩm quản lý theo lô
                restockLot(item, warehouseId, BigDecimal.valueOf(qty));
            }

            // Trả lại số lượng đã giao của đơn bán
            SalesOrderItem soItem = item.getSalesOrderItem();
            if (soItem != null) {
                int current = soItem.getDeliveredQuantity() != null ? soItem.getDeliveredQuantity() : 0;
                soItem.setDeliveredQuantity(Math.max(0, current - qty));
                salesOrderItemRepository.save(soItem);
            }
        }

        // Hủy hóa đơn gắn với phiếu xuất (1 GI = 1 hóa đơn) vì hàng không giao được.
        cancelInvoiceForFailedDelivery(gi);

        if (gi.getSalesOrder() != null) {
            salesOrderService.updateDeliveryStatus(gi.getSalesOrder().getId());
        }
        log.info("Đã hoàn hàng về kho cho phiếu xuất {} (giao thất bại)", gi.getCode());
    }

    /**
     * Hủy hóa đơn của phiếu xuất khi giao thất bại — chỉ khi hóa đơn chưa phát sinh thanh toán
     * và đang ở trạng thái hủy được (DRAFT/ISSUED). Nếu đã thu tiền thì KHÔNG tự hủy,
     * chỉ ghi cảnh báo để bộ phận kế toán xử lý hoàn tiền/điều chỉnh thủ công.
     */
    private void cancelInvoiceForFailedDelivery(GoodsIssue gi) {
        salesInvoiceRepository.findByGoodsIssueId(gi.getId()).ifPresent(inv -> {
            if (inv.getStatus() == SalesInvoiceStatus.CANCELLED) return;
            boolean hasPayment = inv.getPaidAmount() != null
                && inv.getPaidAmount().compareTo(BigDecimal.ZERO) > 0;
            if (!inv.getStatus().canCancel() || hasPayment) {
                log.warn("Giao thất bại GI {}: hóa đơn {} đang ở trạng thái {} (đã thu {}) — "
                        + "KHÔNG tự hủy, cần xử lý hoàn tiền/điều chỉnh thủ công",
                    gi.getCode(), inv.getCode(), inv.getStatus(), inv.getPaidAmount());
                return;
            }
            inv.setStatus(SalesInvoiceStatus.CANCELLED);
            inv.setNotes(appendNote(inv.getNotes(), "Hủy do vận đơn giao thất bại"));
            salesInvoiceRepository.save(inv);
            log.info("Đã hủy hóa đơn {} do giao thất bại GI {}", inv.getCode(), gi.getCode());
        });
    }

    private String appendNote(String existing, String note) {
        if (existing == null || existing.isBlank()) return note;
        return existing + " | " + note;
    }

    @Override
    public void reissueAfterCorrection(String goodsIssueCode) {
        GoodsIssue gi = goodsIssueRepository.findByCode(goodsIssueCode).orElse(null);
        if (gi == null) {
            log.warn("Ghi nhận lại giao thành công: không tìm thấy phiếu xuất {}", goodsIssueCode);
            return;
        }
        Long warehouseId = gi.getWarehouse() != null ? gi.getWarehouse().getId() : null;
        for (GoodsIssueItem item : gi.getItems()) {
            int qty = item.getIssuedQuantity() != null ? item.getIssuedQuantity() : 0;
            if (qty <= 0) continue;

            if (warehouseId != null) {
                // Trừ lại theo FEFO (nếu có lô)
                List<InventoryLot> lots = inventoryLotRepository.findAvailableLotsFEFO(
                    item.getProduct().getId(), warehouseId);
                if (!lots.isEmpty()) {
                    BigDecimal totalAvailable = lots.stream()
                        .map(InventoryLot::getQuantityRemaining)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    if (totalAvailable.compareTo(BigDecimal.valueOf(qty)) < 0) {
                        throw new BusinessException("Không đủ tồn để ghi nhận lại giao thành công cho "
                            + item.getProduct().getName() + ". Cần: " + qty + ", Còn: " + totalAvailable);
                    }
                    BigDecimal still = BigDecimal.valueOf(qty);
                    for (InventoryLot lot : lots) {
                        if (still.compareTo(BigDecimal.ZERO) <= 0) break;
                        BigDecimal take = still.min(lot.getQuantityRemaining());
                        lot.setQuantityRemaining(lot.getQuantityRemaining().subtract(take));
                        inventoryLotRepository.save(lot);
                        still = still.subtract(take);
                    }
                }
                inventoryService.decreaseInventory(item.getProduct().getId(), warehouseId, qty,
                    "DELIVERY_REDELIVERED", gi.getId(), gi.getCode());
            }

            SalesOrderItem soItem = item.getSalesOrderItem();
            if (soItem != null) {
                soItem.addDeliveredQuantity(qty);
                salesOrderItemRepository.save(soItem);
            }
        }

        // Khôi phục hóa đơn đã bị hủy lúc giao thất bại về DRAFT để có thể phát hành lại.
        salesInvoiceRepository.findByGoodsIssueId(gi.getId()).ifPresent(inv -> {
            if (inv.getStatus() == SalesInvoiceStatus.CANCELLED) {
                inv.setStatus(SalesInvoiceStatus.DRAFT);
                inv.setNotes(appendNote(inv.getNotes(), "Khôi phục do giao lại thành công"));
                salesInvoiceRepository.save(inv);
                log.info("Đã khôi phục hóa đơn {} về DRAFT do giao lại thành công GI {}",
                    inv.getCode(), gi.getCode());
            }
        });

        if (gi.getSalesOrder() != null) {
            salesOrderService.updateDeliveryStatus(gi.getSalesOrder().getId());
        }
        log.info("Đã trừ kho lại cho phiếu xuất {} (sửa giao thất bại -> thành công)", gi.getCode());
    }

    /**
     * Hoàn số lượng vào một lô tồn kho. Ưu tiên lô đúng số lô đã ghi trên dòng phiếu xuất,
     * nếu không có thì cộng vào lô FEFO sớm nhất. Bỏ qua nếu sản phẩm không quản lý theo lô.
     */
    private void restockLot(GoodsIssueItem item, Long warehouseId, BigDecimal qty) {
        List<InventoryLot> lots = inventoryLotRepository.findByProductAndWarehouse(
            item.getProduct().getId(), warehouseId);
        if (lots.isEmpty()) return;
        InventoryLot target = null;
        if (item.getBatchNumber() != null) {
            target = lots.stream()
                .filter(l -> item.getBatchNumber().equals(l.getLotNumber()))
                .findFirst().orElse(null);
        }
        if (target == null) target = lots.get(0); // lô FEFO sớm nhất
        target.setQuantityRemaining(target.getQuantityRemaining().add(qty));
        inventoryLotRepository.save(target);
    }

    // Helper methods

    private GoodsIssueItem createItem(GoodsIssueItemDTO dto, SalesOrder salesOrder) {
        // Find the SO item
        SalesOrderItem soItem = salesOrderItemRepository.findById(dto.getSalesOrderItemId())
            .orElseThrow(() -> new ResourceNotFoundException("Sales Order Item not found with ID: " + dto.getSalesOrderItemId()));
        
        // Validate quantity
        int remainingToDeliver = soItem.getQuantity() - soItem.getDeliveredQuantity();
        if (dto.getIssuedQuantity() > remainingToDeliver) {
            throw new BusinessException("Issue quantity (" + dto.getIssuedQuantity() + 
                ") exceeds remaining quantity (" + remainingToDeliver + ") for product: " + soItem.getProduct().getName());
        }
        
        // Tính sẵn totalAmount để parent.recalculateTotal() có giá trị thực để cộng.
        // Nếu để null (chỉ tính trong @PrePersist của item lúc flush), tổng phiếu xuất sẽ = 0.
        BigDecimal lineTotal = BigDecimal.ZERO;
        if (soItem.getUnitPrice() != null && dto.getIssuedQuantity() != null) {
            lineTotal = soItem.getUnitPrice().multiply(BigDecimal.valueOf(dto.getIssuedQuantity()));
        }

        return GoodsIssueItem.builder()
            .salesOrderItem(soItem)
            .product(soItem.getProduct())
            .orderedQuantity(soItem.getQuantity())
            .issuedQuantity(dto.getIssuedQuantity())
            .unitPrice(soItem.getUnitPrice())
            .totalAmount(lineTotal)
            .unit(soItem.getUnit())
            .batchNumber(dto.getBatchNumber())
            .expiryDate(dto.getExpiryDate())
            .notes(dto.getNotes())
            .build();
    }

    private void createInvoice(GoodsIssue goodsIssue) {
        log.info("Creating invoice for goods issue: {}", goodsIssue.getCode());
        
        SalesOrder salesOrder = goodsIssue.getSalesOrder();
        
        SalesInvoice invoice = SalesInvoice.builder()
            .code(SalesInvoice.generateCode())
            .status(SalesInvoiceStatus.DRAFT)
            .invoiceDate(LocalDate.now())
            .dueDate(LocalDate.now().plusDays(salesOrder.getCustomer().getPaymentTerms()))
            .salesOrder(salesOrder)
            .goodsIssue(goodsIssue)
            .customer(salesOrder.getCustomer())
            .subtotal(goodsIssue.getTotalAmount())
            .totalAmount(goodsIssue.getTotalAmount())
            .paidAmount(BigDecimal.ZERO)
            .remainingAmount(goodsIssue.getTotalAmount())
            .createdBy(goodsIssue.getConfirmedBy())
            .items(new ArrayList<>())
            .build();
        
        // Create invoice items from GI items
        for (GoodsIssueItem giItem : goodsIssue.getItems()) {
            SalesInvoiceItem invItem = SalesInvoiceItem.builder()
                .product(giItem.getProduct())
                .goodsIssueItem(giItem)
                .description(giItem.getProduct().getName())
                .quantity(giItem.getIssuedQuantity())
                .unit(giItem.getUnit())
                .unitPrice(giItem.getUnitPrice())
                .totalAmount(giItem.getTotalAmount())
                .build();
            invoice.addItem(invItem);
        }
        
        invoice.calculateTotal();
        salesInvoiceRepository.save(invoice);
        
        log.info("Invoice created with code: {}", invoice.getCode());
    }

    private GoodsIssueDTO mapToDTO(GoodsIssue goodsIssue) {
        GoodsIssueDTO dto = GoodsIssueDTO.builder()
            .id(goodsIssue.getId())
            .code(goodsIssue.getCode())
            .status(goodsIssue.getStatus())
            .issueDate(goodsIssue.getIssueDate())
            .confirmedDate(goodsIssue.getConfirmedDate())
            .deliveryNoteNumber(goodsIssue.getDeliveryNoteNumber())
            .totalAmount(goodsIssue.getTotalAmount())
            .shippingMethod(goodsIssue.getShippingMethod())
            .trackingNumber(goodsIssue.getTrackingNumber())
            .carrierName(goodsIssue.getCarrierName())
            .notes(goodsIssue.getNotes())
            .createdBy(goodsIssue.getCreatedBy())
            .confirmedBy(goodsIssue.getConfirmedBy())
            .createdAt(goodsIssue.getCreatedAt())
            .build();
        
        // Sales Order info
        if (goodsIssue.getSalesOrder() != null) {
            dto.setSalesOrderId(goodsIssue.getSalesOrder().getId());
            dto.setSalesOrderCode(goodsIssue.getSalesOrder().getCode());
            dto.setCustomerId(goodsIssue.getSalesOrder().getCustomer().getId());
            dto.setCustomerName(goodsIssue.getSalesOrder().getCustomer().getName());
        }
        
        // Warehouse
        if (goodsIssue.getWarehouse() != null) {
            dto.setWarehouseId(goodsIssue.getWarehouse().getId());
            dto.setWarehouseName(goodsIssue.getWarehouse().getName());
            dto.setWarehouseCode(goodsIssue.getWarehouse().getCode());
        }
        
        // Delivery address
        if (goodsIssue.getDeliveryAddress() != null) {
            dto.setDeliveryAddressId(goodsIssue.getDeliveryAddress().getId());
            dto.setDeliveryAddressText(goodsIssue.getDeliveryAddress().getFullAddress());
        }
        
        // Items
        if (goodsIssue.getItems() != null) {
            dto.setItems(goodsIssue.getItems().stream()
                .map(this::mapItemToDTO)
                .collect(Collectors.toList()));

            // Fallback: nếu totalAmount lưu trong DB = 0/null (record cũ bị lỗi tính tổng)
            // thì tính lại từ các dòng để hiển thị đúng "Tổng cộng".
            if (dto.getTotalAmount() == null || dto.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal recomputed = dto.getItems().stream()
                    .map(GoodsIssueItemDTO::getTotalAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                dto.setTotalAmount(recomputed);
            }
        }

        dto.computeFields();
        return dto;
    }

    private GoodsIssueItemDTO mapItemToDTO(GoodsIssueItem item) {
        GoodsIssueItemDTO dto = GoodsIssueItemDTO.builder()
            .id(item.getId())
            .goodsIssueId(item.getGoodsIssue().getId())
            .salesOrderItemId(item.getSalesOrderItem().getId())
            .productId(item.getProduct().getId())
            .productCode(item.getProduct().getCode())
            .productName(item.getProduct().getName())
            .orderedQuantity(item.getOrderedQuantity())
            .issuedQuantity(item.getIssuedQuantity())
            .unitPrice(item.getUnitPrice())
            .totalAmount(item.getTotalAmount())
            .unit(item.getUnit())
            .batchNumber(item.getBatchNumber())
            .expiryDate(item.getExpiryDate())
            .notes(item.getNotes())
            .build();
        
        // Calculate remaining
        SalesOrderItem soItem = item.getSalesOrderItem();
        dto.setPreviouslyDeliveredQuantity(soItem.getDeliveredQuantity() - item.getIssuedQuantity());
        dto.setRemainingQuantity(soItem.getQuantity() - soItem.getDeliveredQuantity());
        dto.setMaxAllowedQuantity(soItem.getQuantity() - (soItem.getDeliveredQuantity() - item.getIssuedQuantity()));
        
        return dto;
    }
}
