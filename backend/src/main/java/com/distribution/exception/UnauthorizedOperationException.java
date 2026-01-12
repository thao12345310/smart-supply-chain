package com.distribution.exception;

/**
 * Exception for insufficient permission/authorization errors
 */
public class UnauthorizedOperationException extends BusinessException {
    
    public UnauthorizedOperationException(String operation) {
        super(String.format("User is not authorized to perform: %s", operation), "UNAUTHORIZED");
    }
    
    public UnauthorizedOperationException(String operation, String requiredRole) {
        super(String.format("User is not authorized to perform: %s. Required role: %s", operation, requiredRole), "UNAUTHORIZED");
    }
}
