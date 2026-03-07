package com.elocate.elocate.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * User context holding authenticated user information
 * Available throughout the request lifecycle
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {
    private UUID userId;
    private String fullName;
    private String email;
    private String role;
}
