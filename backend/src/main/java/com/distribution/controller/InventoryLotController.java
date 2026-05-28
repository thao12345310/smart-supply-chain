package com.distribution.controller;

import com.distribution.dto.ApiResponse;
import com.distribution.dto.InventoryLotResponse;
import com.distribution.service.InventoryLotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory-lots")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Tag(name = "Inventory Lot", description = "Quản lý tồn kho theo lô (FEFO)")
public class InventoryLotController {

    private final InventoryLotService inventoryLotService;

    @GetMapping
    @Operation(summary = "Danh sách lô hàng", description = "Lấy danh sách tất cả lô, filter theo sản phẩm/kho")
    public ResponseEntity<ApiResponse<List<InventoryLotResponse>>> getAll(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId) {
        List<InventoryLotResponse> lots = inventoryLotService.getAll(productId, warehouseId);
        return ResponseEntity.ok(ApiResponse.success(lots, "Tìm thấy " + lots.size() + " lô hàng"));
    }

    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    @Operation(summary = "Lô hàng theo sản phẩm và kho", description = "Lấy danh sách lô của 1 (sản phẩm, kho), sort FEFO")
    public ResponseEntity<ApiResponse<List<InventoryLotResponse>>> getByProductAndWarehouse(
            @PathVariable Long productId,
            @PathVariable Long warehouseId) {
        List<InventoryLotResponse> lots = inventoryLotService.getByProductAndWarehouse(productId, warehouseId);
        return ResponseEntity.ok(ApiResponse.success(lots));
    }

    @GetMapping("/expiring-soon")
    @Operation(summary = "Lô sắp hết HSD", description = "Lô sẽ hết hạn sử dụng trong N ngày tới (mặc định 30 ngày)")
    public ResponseEntity<ApiResponse<List<InventoryLotResponse>>> getExpiringSoon(
            @RequestParam(defaultValue = "30") int days) {
        List<InventoryLotResponse> lots = inventoryLotService.getExpiringSoon(days);
        return ResponseEntity.ok(ApiResponse.success(lots,
            "Tìm thấy " + lots.size() + " lô sắp hết hạn trong " + days + " ngày tới"));
    }

    @GetMapping("/expired")
    @Operation(summary = "Lô đã hết HSD", description = "Lô đã hết hạn sử dụng nhưng còn tồn kho")
    public ResponseEntity<ApiResponse<List<InventoryLotResponse>>> getExpired() {
        List<InventoryLotResponse> lots = inventoryLotService.getExpired();
        return ResponseEntity.ok(ApiResponse.success(lots,
            "Tìm thấy " + lots.size() + " lô đã hết hạn sử dụng"));
    }
}
