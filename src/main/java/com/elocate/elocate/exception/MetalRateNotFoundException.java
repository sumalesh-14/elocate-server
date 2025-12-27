package com.elocate.elocate.exception;

import java.util.UUID;

public class MetalRateNotFoundException extends RuntimeException {
    
    public MetalRateNotFoundException(UUID id) {
        super("Metal rate not found with id: " + id);
    }
    
    public MetalRateNotFoundException(String message) {
        super(message);
    }
}
