package com.elocate.elocate.config;

import com.elocate.elocate.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpMethod;

/**
 * Spring Security Configuration
 * Configures JWT authentication and public/protected endpoints
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Public endpoints that don't require authentication
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/resend-otp",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/request-login-otp",
            "/api/v1/auth/verify-login-otp",
            "/api/v1/partner-auth/register",          // Partner self-registration
            "/api/v1/partner-auth/upload-document",   // Partner document upload (pre-registration)
            "/api/v1/test/**",
            "/api/v1/health",
            "/api/v1/ping",
            "/api/v1/facility",
            "/api/v1/facility/**",
            "/api/v1/recycle-requests/driver-action/**",
            "/api/v1/driver/pickup/**", // Driver pickup actions via email links (accept, reject, on-my-way)
            "/api/v1/contact-issues",   // Public contact form submission
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info",
            "/error",
            // Swagger UI endpoints
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (we're using JWT)
                .csrf(csrf -> csrf.disable())

                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless session (no session cookies)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure endpoint authorization
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/device-categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/device-brands/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/device-models/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/category-brands/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/contact-issues").permitAll()
                        .anyRequest().authenticated())

                // Add JWT filter before Spring Security's filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow all origins using patterns (for Swagger UI and external clients)
        configuration.setAllowedOriginPatterns(List.of("*"));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false); // Must be false when using wildcard origins
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
