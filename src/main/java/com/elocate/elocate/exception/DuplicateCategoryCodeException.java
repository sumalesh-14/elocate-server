package com.elocate.elocate.exception;

public class DuplicateCategoryCodeException extends RuntimeException {
    
    public DuplicateCategoryCodeException(String code) {
        super("Category with code '" + code + "' already exists");
    }
    
    public DuplicateCategoryCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
