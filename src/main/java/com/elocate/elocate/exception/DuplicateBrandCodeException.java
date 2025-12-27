package com.elocate.elocate.exception;

public class DuplicateBrandCodeException extends RuntimeException {
    
    public DuplicateBrandCodeException(String code) {
        super("Brand with code '" + code + "' already exists");
    }
    
    public DuplicateBrandCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
