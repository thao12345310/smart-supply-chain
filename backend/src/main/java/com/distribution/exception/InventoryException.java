package com.distribution.exception;

/**
 * Exception for inventory-related errors
 */
public class InventoryException extends BusinessException {
    
    public InventoryException(String message) {
        super(message, "INVENTORY_ERROR");
    }
    
    public InventoryException(String message, String errorCode) {
        super(message, errorCode);
    }
    
    public static InventoryException insufficientStock(String productName, int available, int requested) {
        return new InventoryException(
            String.format("Insufficient stock for %s. Available: %d, Requested: %d", 
                productName, available, requested),
            "INSUFFICIENT_STOCK"
        );
    }
    
    public static InventoryException exceedsOrderedQuantity(String productName, int ordered, int receiving) {
        return new InventoryException(
            String.format("Receiving quantity (%d) exceeds ordered quantity (%d) for %s", 
                receiving, ordered, productName),
            "EXCEEDS_ORDERED_QUANTITY"
        );
    }
}
