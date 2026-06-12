package com.distribution.service.impl;

import com.distribution.dto.InventoryLotResponse;
import com.distribution.dto.LotDisposalRequest;
import com.distribution.dto.LotDisposalResponse;
import com.distribution.exception.BusinessException;
import com.distribution.exception.ResourceNotFoundException;
import com.distribution.model.InventoryLot;
import com.distribution.model.LotDisposal;
import com.distribution.repository.InventoryLotRepository;
import com.distribution.repository.LotDisposalRepository;
import com.distribution.repository.UserRepository;
import com.distribution.service.InventoryLotService;
import com.distribution.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InventoryLotServiceImpl implements InventoryLotService {

    private final InventoryLotRepository inventoryLotRepository;
    private final LotDisposalRepository lotDisposalRepository;
    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    @Override
    public List<InventoryLotResponse> getAll(Long productId, Long warehouseId) {
        return inventoryLotRepository.findAllFiltered(productId, warehouseId)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<InventoryLotResponse> getByProductAndWarehouse(Long productId, Long warehouseId) {
        return inventoryLotRepository.findByProductAndWarehouse(productId, warehouseId)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<InventoryLotResponse> getExpiringSoon(int days) {
        LocalDate threshold = LocalDate.now().plusDays(days);
        return inventoryLotRepository.findExpiringSoon(threshold)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<InventoryLotResponse> getExpired() {
        return inventoryLotRepository.findExpired()
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LotDisposalResponse disposeLot(Long lotId, LotDisposalRequest request) {
        InventoryLot lot = inventoryLotRepository.findById(lotId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lô hàng với ID: " + lotId));
        return doDispose(lot, request);
    }

    @Override
    @Transactional
    public List<LotDisposalResponse> disposeExpired(Long warehouseId, LotDisposalRequest request) {
        List<InventoryLot> expiredLots = inventoryLotRepository.findExpired().stream()
            .filter(l -> warehouseId == null
                || (l.getWarehouse() != null && warehouseId.equals(l.getWarehouse().getId())))
            .collect(Collectors.toList());

        if (expiredLots.isEmpty()) {
            throw new BusinessException("Không có lô hết hạn nào còn tồn kho để xuất hủy.");
        }

        List<LotDisposalResponse> results = new ArrayList<>();
        for (InventoryLot lot : expiredLots) {
            results.add(doDispose(lot, request));
        }
        return results;
    }

    @Override
    public List<LotDisposalResponse> getDisposals(Long warehouseId) {
        List<LotDisposal> disposals = warehouseId != null
            ? lotDisposalRepository.findByWarehouseIdOrderByDisposedAtDesc(warehouseId)
            : lotDisposalRepository.findAllByOrderByDisposedAtDesc();
        return disposals.stream().map(this::toDisposalResponse).collect(Collectors.toList());
    }

    private LotDisposalResponse doDispose(InventoryLot lot, LotDisposalRequest request) {
        BigDecimal quantity = lot.getQuantityRemaining();
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Lô " + lot.getLotNumber() + " không còn tồn kho để xuất hủy.");
        }

        boolean expired = lot.getExpiryDate() != null && lot.getExpiryDate().isBefore(LocalDate.now());
        String reason = (request != null && request.getReason() != null && !request.getReason().isBlank())
            ? request.getReason()
            : (expired ? "Hết hạn sử dụng" : "Xuất hủy hàng hỏng/kém chất lượng");
        Long disposedBy = request != null ? request.getDisposedBy() : null;

        LotDisposal disposal = lotDisposalRepository.save(LotDisposal.builder()
            .code(LotDisposal.generateCode())
            .lot(lot)
            .product(lot.getProduct())
            .warehouse(lot.getWarehouse())
            .lotNumber(lot.getLotNumber())
            .expiryDate(lot.getExpiryDate())
            .quantity(quantity)
            .unitCost(lot.getUnitCost())
            .reason(reason)
            .disposedBy(disposedBy)
            .build());

        // Trừ tồn kho tổng và ghi transaction DISPOSAL
        int intQuantity = quantity.setScale(0, RoundingMode.HALF_UP).intValue();
        inventoryService.disposeInventory(
            lot.getProduct().getId(),
            lot.getWarehouse().getId(),
            intQuantity,
            "LOT_DISPOSAL",
            disposal.getId(),
            disposal.getCode(),
            disposedBy,
            "Xuất hủy lô " + lot.getLotNumber() + ": " + reason
        );

        // Đưa tồn của lô về 0
        lot.setQuantityRemaining(BigDecimal.ZERO);
        inventoryLotRepository.save(lot);

        log.info("Đã xuất hủy lô {} (phiếu {}): {} đơn vị, lý do: {}",
            lot.getLotNumber(), disposal.getCode(), quantity, reason);
        return toDisposalResponse(disposal);
    }

    private LotDisposalResponse toDisposalResponse(LotDisposal d) {
        return LotDisposalResponse.builder()
            .id(d.getId())
            .code(d.getCode())
            .lotId(d.getLot() != null ? d.getLot().getId() : null)
            .lotNumber(d.getLotNumber())
            .productId(d.getProduct() != null ? d.getProduct().getId() : null)
            .productName(d.getProduct() != null ? d.getProduct().getName() : null)
            .productCode(d.getProduct() != null ? d.getProduct().getCode() : null)
            .warehouseId(d.getWarehouse() != null ? d.getWarehouse().getId() : null)
            .warehouseName(d.getWarehouse() != null ? d.getWarehouse().getName() : null)
            .expiryDate(d.getExpiryDate())
            .quantity(d.getQuantity())
            .unitCost(d.getUnitCost())
            .reason(d.getReason())
            .disposedBy(d.getDisposedBy())
            .disposedByName(d.getDisposedBy() != null
                ? userRepository.findById(d.getDisposedBy()).map(u -> u.getFullName()).orElse(null)
                : null)
            .disposedAt(d.getDisposedAt())
            .build();
    }

    private InventoryLotResponse toResponse(InventoryLot lot) {
        LocalDate today = LocalDate.now();
        Long daysUntilExpiry = null;
        String status = "FRESH";

        if (lot.getExpiryDate() != null) {
            daysUntilExpiry = ChronoUnit.DAYS.between(today, lot.getExpiryDate());
            if (daysUntilExpiry < 0) {
                status = "EXPIRED";
            } else if (daysUntilExpiry <= 30) {
                status = "EXPIRING_SOON";
            }
        }

        return InventoryLotResponse.builder()
            .id(lot.getId())
            .productId(lot.getProduct() != null ? lot.getProduct().getId() : null)
            .productName(lot.getProduct() != null ? lot.getProduct().getName() : null)
            .productCode(lot.getProduct() != null ? lot.getProduct().getCode() : null)
            .warehouseId(lot.getWarehouse() != null ? lot.getWarehouse().getId() : null)
            .warehouseName(lot.getWarehouse() != null ? lot.getWarehouse().getName() : null)
            .lotNumber(lot.getLotNumber())
            .manufactureDate(lot.getManufactureDate())
            .expiryDate(lot.getExpiryDate())
            .quantityReceived(lot.getQuantityReceived())
            .quantityRemaining(lot.getQuantityRemaining())
            .daysUntilExpiry(daysUntilExpiry)
            .status(status)
            .build();
    }
}
