package com.elocate.elocate.repository;

import com.elocate.elocate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Find user by mobile number
     * 
     * @param mobileNumber Mobile number
     * @return Optional containing user if found
     */
    Optional<User> findByMobileNumber(String mobileNumber);
    
    /**
     * Find user by email
     * 
     * @param email Email address
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if email already exists
     * 
     * @param email Email to check
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if mobile number already exists
     * 
     * @param mobileNumber Mobile number to check
     * @return true if exists, false otherwise
     */
    boolean existsByMobileNumber(String mobileNumber);
}
