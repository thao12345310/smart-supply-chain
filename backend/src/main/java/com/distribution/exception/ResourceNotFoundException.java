package com.distribution.exception;

/**
 * Exception for resource not found errors
 */
public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s not found with id: %d", resourceName, id), "NOT_FOUND");
    }
    
    public ResourceNotFoundException(String resourceName, String identifier) {
        super(String.format("%s not found: %s", resourceName, identifier), "NOT_FOUND");
    }
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue), "NOT_FOUND");
    }
    
    public ResourceNotFoundException(String message) {
        super(message, "NOT_FOUND");
    }
}
