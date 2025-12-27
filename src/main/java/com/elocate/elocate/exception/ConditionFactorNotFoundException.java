package com.elocate.elocate.exception;

public class ConditionFactorNotFoundException extends RuntimeException {
    
    public ConditionFactorNotFoundException(String conditionCode) {
        super("Condition factor not found for code: " + conditionCode);
    }
    
    public ConditionFactorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
