package com.distribution.exception;

/**
 * Exception for invalid status transition attempts
 */
public class InvalidStatusTransitionException extends BusinessException {
    
    public InvalidStatusTransitionException(String currentStatus, String attemptedAction) {
        super(String.format("Cannot %s from status: %s", attemptedAction, currentStatus), "INVALID_STATUS_TRANSITION");
    }
    
    public InvalidStatusTransitionException(String message) {
        super(message, "INVALID_STATUS_TRANSITION");
    }
}
