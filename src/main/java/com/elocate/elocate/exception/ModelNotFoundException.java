package com.elocate.elocate.exception;

import java.util.UUID;

public class ModelNotFoundException extends RuntimeException {
    
    public ModelNotFoundException(UUID id) {
        super("Device model not found with id: " + id);
    }
    
    public ModelNotFoundException(String message) {
        super(message);
    }
}
