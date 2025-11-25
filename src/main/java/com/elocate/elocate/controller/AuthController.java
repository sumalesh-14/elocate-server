package com.elocate.elocate.controller;

import com.elocate.elocate.dto.LoginRequest;
import com.elocate.elocate.dto.LoginResponse;
import com.elocate.elocate.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.authenticateUser(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

//    @PostMapping("/register")
//    public ResponseEntity<Void> register(@RequestBody LoginRequest request) {
//        authService.create(request.email(), request.password());
//        return ResponseEntity.ok().build();
//    }

}