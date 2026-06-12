package com.distribution.service;

import com.distribution.dto.InventoryLotResponse;
import com.distribution.dto.LotDisposalRequest;
import com.distribution.dto.LotDisposalResponse;

import java.util.List;

public interface InventoryLotService {

    List<InventoryLotResponse> getAll(Long productId, Long warehouseId);

    List<InventoryLotResponse> getByProductAndWarehouse(Long productId, Long warehouseId);

    List<InventoryLotResponse> getExpiringSoon(int days);

    List<InventoryLotResponse> getExpired();

    /** Xuất hủy toàn bộ tồn còn lại của một lô */
    LotDisposalResponse disposeLot(Long lotId, LotDisposalRequest request);

    /** Xuất hủy tất cả lô đã hết HSD còn tồn (lọc theo kho nếu có) */
    List<LotDisposalResponse> disposeExpired(Long warehouseId, LotDisposalRequest request);

    /** Lịch sử phiếu xuất hủy */
    List<LotDisposalResponse> getDisposals(Long warehouseId);
}
