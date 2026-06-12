package com.distribution.controller;

import com.distribution.dto.ApiResponse;
import com.distribution.dto.InventoryDTO;
import com.distribution.dto.PurchaseSuggestionDTO;
import com.distribution.model.InventoryTransaction;
import com.distribution.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Inventory operations
 * 
 * Provides read-only access to inventory data.
 * Stock updates are handled through GoodsReceiptService.
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Tag(name = "Inventory", description = "Inventory Management APIs")
public class InventoryController {

    private final InventoryService inventoryService;

    // ==================== Query Operations ====================

    @GetMapping
    @Operation(summary = "Get all Inventory", description = "Retrieve all inventory records")
    public ResponseEntity<ApiResponse<List<InventoryDTO>>> getAll() {
        List<InventoryDTO> inventory = inventoryService.getAll();
        return ResponseEntity.ok(ApiResponse.success(inventory, "Retrieved " + inventory.size() + " inventory records"));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get Inventory by Product", 
               description = "Retrieve inventory for a product across all warehouses")
    public ResponseEntity<ApiResponse<List<InventoryDTO>>> getByProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        List<InventoryDTO> inventory = inventoryService.getByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(inventory));
    }

    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Get Inventory by Warehouse", 
               description = "Retrieve all inventory at a specific warehouse")
    public ResponseEntity<ApiResponse<List<InventoryDTO>>> getByWarehouse(
            @Parameter(description = "Warehouse ID") @PathVariable Long warehouseId) {
        List<InventoryDTO> inventory = inventoryService.getByWarehouse(warehouseId);
        return ResponseEntity.ok(ApiResponse.success(inventory));
    }

    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    @Operation(summary = "Get Inventory by Product and Warehouse", 
               description = "Retrieve inventory for a specific product at a specific warehouse")
    public ResponseEntity<ApiResponse<InventoryDTO>> getByProductAndWarehouse(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Warehouse ID") @PathVariable Long warehouseId) {
        InventoryDTO inventory = inventoryService.getByProductAndWarehouse(productId, warehouseId);
        if (inventory == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "No inventory found for this product at this warehouse"));
        }
        return ResponseEntity.ok(ApiResponse.success(inventory));
    }

    @GetMapping("/product/{productId}/total-available")
    @Operation(summary = "Get Total Available Quantity", 
               description = "Get total available quantity for a product across all warehouses")
    public ResponseEntity<ApiResponse<Integer>> getTotalAvailable(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        Integer total = inventoryService.getTotalAvailableQuantity(productId);
        return ResponseEntity.ok(ApiResponse.success(total, "Total available quantity: " + total));
    }

    // ==================== Alerts & Reports ====================

    @GetMapping("/low-stock")
    @Operation(summary = "Get Low Stock Items", 
               description = "Retrieve inventory items below the specified threshold")
    public ResponseEntity<ApiResponse<List<InventoryDTO>>> getLowStock(
            @Parameter(description = "Stock threshold (default 10)") 
            @RequestParam(defaultValue = "10") Integer threshold) {
        List<InventoryDTO> inventory = inventoryService.getLowStock(threshold);
        return ResponseEntity.ok(ApiResponse.success(inventory, 
            "Found " + inventory.size() + " items with stock below " + threshold));
    }

    @GetMapping("/needing-reorder")
    @Operation(summary = "Get Items Needing Reorder", 
               description = "Retrieve inventory items at or below their reorder level")
    public ResponseEntity<ApiResponse<List<InventoryDTO>>> getNeedingReorder() {
        List<InventoryDTO> inventory = inventoryService.getNeedingReorder();
        return ResponseEntity.ok(ApiResponse.success(inventory,
            "Found " + inventory.size() + " items needing reorder"));
    }

    @GetMapping("/purchase-suggestions")
    @Operation(summary = "Get Purchase Suggestions",
               description = "Items at/below reorder level or out of stock, grouped by supplier")
    public ResponseEntity<ApiResponse<List<PurchaseSuggestionDTO>>> getPurchaseSuggestions(
            @Parameter(description = "Optional warehouse filter")
            @RequestParam(required = false) Long warehouseId) {
        List<PurchaseSuggestionDTO> suggestions = inventoryService.getPurchaseSuggestions(warehouseId);
        return ResponseEntity.ok(ApiResponse.success(suggestions,
            "Found " + suggestions.size() + " supplier groups needing purchase"));
    }

    @PatchMapping("/{id}/reorder-level")
    @Operation(summary = "Update Reorder Level",
               description = "Set the reorder (low-stock alert) level for an inventory record")
    public ResponseEntity<ApiResponse<InventoryDTO>> updateReorderLevel(
            @Parameter(description = "Inventory ID") @PathVariable Long id,
            @RequestBody java.util.Map<String, Integer> body) {
        InventoryDTO updated = inventoryService.updateReorderLevel(
            id, body.get("reorderLevel"), body.get("reorderQuantity"));
        return ResponseEntity.ok(ApiResponse.success(updated, "Reorder level updated"));
    }

    // ==================== Transaction History ====================

    @GetMapping("/transactions/product/{productId}")
    @Operation(summary = "Get Transactions by Product", 
               description = "Retrieve all inventory transactions for a product")
    public ResponseEntity<ApiResponse<List<InventoryTransaction>>> getTransactionsByProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        List<InventoryTransaction> transactions = inventoryService.getTransactionsByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/transactions/warehouse/{warehouseId}")
    @Operation(summary = "Get Transactions by Warehouse", 
               description = "Retrieve all inventory transactions for a warehouse")
    public ResponseEntity<ApiResponse<List<InventoryTransaction>>> getTransactionsByWarehouse(
            @Parameter(description = "Warehouse ID") @PathVariable Long warehouseId) {
        List<InventoryTransaction> transactions = inventoryService.getTransactionsByWarehouse(warehouseId);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/transactions/reference")
    @Operation(summary = "Get Transactions by Reference", 
               description = "Retrieve all inventory transactions for a specific reference (e.g., GR, SO)")
    public ResponseEntity<ApiResponse<List<InventoryTransaction>>> getTransactionsByReference(
            @Parameter(description = "Reference type (e.g., GOODS_RECEIPT, SALES_ORDER)") 
            @RequestParam String referenceType,
            @Parameter(description = "Reference ID") 
            @RequestParam Long referenceId) {
        List<InventoryTransaction> transactions = inventoryService.getTransactionsByReference(referenceType, referenceId);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }
}
