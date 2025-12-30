package com.elocate.elocate.model.enums;

/**
 * OTP type enum for different OTP use cases
 */
public enum OtpType {
    /**
     * Email verification during registration
     */
    EMAIL_VERIFICATION,
    
    /**
     * Password reset request
     */
    PASSWORD_RESET,
    
    /**
     * Email change verification
     */
    EMAIL_CHANGE
}
