package com.distribution.service;

import com.distribution.dto.InventoryLotResponse;

import java.util.List;

public interface InventoryLotService {

    List<InventoryLotResponse> getAll(Long productId, Long warehouseId);

    List<InventoryLotResponse> getByProductAndWarehouse(Long productId, Long warehouseId);

    List<InventoryLotResponse> getExpiringSoon(int days);

    List<InventoryLotResponse> getExpired();
}
