package com.distribution.service.impl;

import com.distribution.dto.InventoryLotResponse;
import com.distribution.model.InventoryLot;
import com.distribution.repository.InventoryLotRepository;
import com.distribution.service.InventoryLotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryLotServiceImpl implements InventoryLotService {

    private final InventoryLotRepository inventoryLotRepository;

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
