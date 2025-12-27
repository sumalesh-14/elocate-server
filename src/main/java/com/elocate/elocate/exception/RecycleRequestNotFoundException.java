package com.elocate.elocate.exception;

import java.util.UUID;

public class RecycleRequestNotFoundException extends RuntimeException {
    
    public RecycleRequestNotFoundException(UUID id) {
        super("Recycle request not found with id: " + id);
    }
    
    public RecycleRequestNotFoundException(String message) {
        super(message);
    }
}
