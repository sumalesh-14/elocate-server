package com.elocate.elocate.exception;

import java.util.UUID;

public class BrandNotFoundException extends RuntimeException {
    
    public BrandNotFoundException(UUID id) {
        super("Brand not found with id: " + id);
    }
    
    public BrandNotFoundException(String message) {
        super(message);
    }
}
