package com.elocate.elocate.exception;

import java.util.UUID;

public class CategoryNotFoundException extends RuntimeException {
    
    public CategoryNotFoundException(UUID id) {
        super("Category not found with id: " + id);
    }
    
    public CategoryNotFoundException(String message) {
        super(message);
    }
}
