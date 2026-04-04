package com.elocate.elocate.service;

import com.auth0.client.auth.AuthAPI;
import com.auth0.json.auth.CreatedUser;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.Request;
import com.auth0.net.BaseRequest;
import com.auth0.exception.Auth0Exception;
import com.elocate.elocate.exception.InvalidCredentialsException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class Auth0Service {

    private final AuthAPI authAPI;
    private final String domain;
    private final String clientId;
    private final String clientSecret;
    private final String connection;

    public Auth0Service(@Value("${auth0.domain}") String domain,
                        @Value("${auth0.clientId}") String clientId,
                        @Value("${auth0.clientSecret}") String clientSecret,
                        @Value("${auth0.connection:Username-Password-Authentication}") String connection) {
        log.info("Auth0Service initialized with domain: {}, clientId: {}, connection: {}", 
                domain, clientId, connection);
        this.domain = domain;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authAPI = new AuthAPI(domain, clientId, clientSecret);
        this.connection = connection;
    }
    public TokenHolder login(String email, String password) {
        try {
            log.info("Attempting Auth0 login for user: {}", email);

            String tokenUrl = "https://" + domain + "/oauth/token";

            // Use the standard password grant
            // NOTE: Make sure you've set Default Directory in Auth0 Tenant Settings
            // to "Username-Password-Authentication"
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("client_id", clientId);
            requestBody.put("client_secret", clientSecret);
            requestBody.put("grant_type", "password");
            requestBody.put("username", email);
            requestBody.put("password", password);
            requestBody.put("scope", "openid profile email offline_access");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            try {
                ResponseEntity<TokenHolder> response = restTemplate.exchange(
                        tokenUrl,
                        HttpMethod.POST,
                        entity,
                        TokenHolder.class
                );

                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("Auth0 login successful for user: {}", email);
                    return response.getBody();
                } else {
                    throw new RuntimeException("Auth0 login failed with status: " + response.getStatusCode());
                }
            } catch (HttpClientErrorException e) {
                String errorBody = e.getResponseBodyAsString();
                log.error("Auth0 API error - Status: {}, Body: {}", e.getStatusCode(), errorBody);

                // Parse the error response
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode errorNode = mapper.readTree(errorBody);
                    String error = errorNode.get("error").asText();
                    String errorDescription = errorNode.get("error_description").asText();
                    
                    // Check for invalid credentials
                    if ("invalid_grant".equals(error) && errorDescription.toLowerCase().contains("wrong email or password")) {
                        throw new InvalidCredentialsException("Wrong email or password");
                    }
                    
                    throw new RuntimeException("Auth0 Error: " + errorDescription);
                } catch (InvalidCredentialsException ice) {
                    throw ice; // Re-throw InvalidCredentialsException
                } catch (Exception parseError) {
                    throw new RuntimeException("Authentication failed: " + errorBody);
                }
            }

        } catch (InvalidCredentialsException e) {
            // Re-throw InvalidCredentialsException without wrapping
            throw e;
        } catch (Exception e) {
            log.error("Auth0 login failed: {}", e.getMessage(), e);
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    public TokenHolder refreshToken(String refreshToken) {
        try {
            Request<TokenHolder> request = authAPI.renewAuth(refreshToken);
            return request.execute().getBody();
        } catch (Auth0Exception e) {
            log.error("Auth0 token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Token refresh failed: " + e.getMessage());
        }
    }
    
    public void resetPassword(String email) {
        try {
            authAPI.resetPassword(email, connection).execute();
        } catch (Auth0Exception e) {
            log.error("Auth0 password reset failed: {}", e.getMessage());
            throw new RuntimeException("Failed to send password reset email: " + e.getMessage());
        }
    }

    public String createUser(String email, String password, String username) {
        try {
            Request<CreatedUser> request = authAPI.signUp(email, username, password.toCharArray(), connection);
            CreatedUser user = request.execute().getBody();
            return "auth0|" + user.getUserId();
        } catch (Auth0Exception e) {
            log.error("Auth0 registration failed: {}", e.getMessage());
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Update a user's email in Auth0 via the Management API.
     * Requires the app to have the "update:users" permission in Auth0 Management API.
     * Uses client_credentials grant to obtain a management token first.
     */
    public void updateUserEmail(String auth0UserId, String newEmail) {
        try {
            // 1. Get management API token via client_credentials
            RestTemplate rest = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> tokenReq = new HashMap<>();
            tokenReq.put("client_id", clientId);
            tokenReq.put("client_secret", clientSecret);
            tokenReq.put("audience", "https://" + domain + "/api/v2/");
            tokenReq.put("grant_type", "client_credentials");

            ResponseEntity<Map> tokenRes = rest.exchange(
                "https://" + domain + "/oauth/token",
                HttpMethod.POST,
                new HttpEntity<>(tokenReq, headers),
                Map.class
            );

            String mgmtToken = (String) tokenRes.getBody().get("access_token");
            if (mgmtToken == null) throw new RuntimeException("Failed to obtain Auth0 management token");

            // 2. PATCH /api/v2/users/{id} with new email
            HttpHeaders patchHeaders = new HttpHeaders();
            patchHeaders.setContentType(MediaType.APPLICATION_JSON);
            patchHeaders.setBearerAuth(mgmtToken);

            Map<String, Object> patchBody = new HashMap<>();
            patchBody.put("email", newEmail);
            patchBody.put("email_verified", true);
            patchBody.put("connection", connection);

            rest.exchange(
                "https://" + domain + "/api/v2/users/" + auth0UserId,
                HttpMethod.PATCH,
                new HttpEntity<>(patchBody, patchHeaders),
                Map.class
            );

            log.info("Auth0 email updated for user {} to {}", auth0UserId, newEmail);
        } catch (Exception e) {
            log.error("Failed to update Auth0 email: {}", e.getMessage());
            throw new RuntimeException("Failed to update email in Auth0: " + e.getMessage());
        }
    }
}
