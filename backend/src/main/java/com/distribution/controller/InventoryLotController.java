package com.distribution.controller;

import com.distribution.dto.ApiResponse;
import com.distribution.dto.InventoryLotResponse;
import com.distribution.dto.LotDisposalRequest;
import com.distribution.dto.LotDisposalResponse;
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

    @PostMapping("/{lotId}/dispose")
    @Operation(summary = "Xuất hủy một lô",
        description = "Hủy toàn bộ tồn còn lại của lô (hết HSD/hư hỏng), trừ tồn kho tổng và ghi transaction DISPOSAL")
    public ResponseEntity<ApiResponse<LotDisposalResponse>> disposeLot(
            @PathVariable Long lotId,
            @RequestBody(required = false) LotDisposalRequest request) {
        LotDisposalResponse disposal = inventoryLotService.disposeLot(lotId, request);
        return ResponseEntity.ok(ApiResponse.success(disposal,
            "Đã xuất hủy " + disposal.getQuantity() + " đơn vị của lô " + disposal.getLotNumber()));
    }

    @PostMapping("/dispose-expired")
    @Operation(summary = "Xuất hủy tất cả lô hết HSD",
        description = "Hủy toàn bộ lô đã hết hạn sử dụng còn tồn kho (lọc theo kho nếu truyền warehouseId)")
    public ResponseEntity<ApiResponse<List<LotDisposalResponse>>> disposeExpired(
            @RequestParam(required = false) Long warehouseId,
            @RequestBody(required = false) LotDisposalRequest request) {
        List<LotDisposalResponse> disposals = inventoryLotService.disposeExpired(warehouseId, request);
        return ResponseEntity.ok(ApiResponse.success(disposals,
            "Đã xuất hủy " + disposals.size() + " lô hết hạn sử dụng"));
    }

    @GetMapping("/disposals")
    @Operation(summary = "Lịch sử xuất hủy", description = "Danh sách phiếu xuất hủy lô, mới nhất trước")
    public ResponseEntity<ApiResponse<List<LotDisposalResponse>>> getDisposals(
            @RequestParam(required = false) Long warehouseId) {
        List<LotDisposalResponse> disposals = inventoryLotService.getDisposals(warehouseId);
        return ResponseEntity.ok(ApiResponse.success(disposals,
            "Tìm thấy " + disposals.size() + " phiếu xuất hủy"));
    }
}
