package com.elocate.elocate.context;

/**
 * ThreadLocal holder for UserContext
 * Provides access to authenticated user info throughout the request
 */
public class UserContextHolder {
    
    private static final ThreadLocal<UserContext> contextHolder = new ThreadLocal<>();
    
    /**
     * Set the current user context
     */
    public static void setContext(UserContext context) {
        contextHolder.set(context);
    }
    
    /**
     * Get the current user context
     */
    public static UserContext getContext() {
        return contextHolder.get();
    }
    
    /**
     * Clear the current user context
     * Should be called after request completes
     */
    public static void clear() {
        contextHolder.remove();
    }
}
