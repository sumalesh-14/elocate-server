package com.elocate.elocate.service;

import com.elocate.elocate.dto.FirebaseRefreshResponse;
import com.elocate.elocate.dto.FirebaseSignInRequest;
import com.elocate.elocate.dto.FirebaseSignInResponse;
import com.elocate.elocate.dto.LoginResponse;
import com.elocate.elocate.exception.InvalidLoginCredentialsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class AuthService {

    private static final String INVALID_CREDENTIALS_ERROR = "INVALID_LOGIN_CREDENTIALS";
    private static final String SIGN_IN_URL =
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword";
    private static final String REFRESH_TOKEN_URL =
            "https://securetoken.googleapis.com/v1/token";

    @Value("${firebase.web-api-key}")
    private String webApiKey;

    private final RestClient restClient = RestClient.builder().build();

    public FirebaseSignInResponse login(String emailId, String password) {
        FirebaseSignInRequest requestBody = new FirebaseSignInRequest(emailId, password, true);
        return sendSignInRequest(requestBody);
    }

    public FirebaseRefreshResponse refreshToken(String refreshToken) {
        return restClient.post()
                .uri(REFRESH_TOKEN_URL + "?key=" + webApiKey)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=refresh_token&refresh_token=" + refreshToken)
                .retrieve()
                .body(FirebaseRefreshResponse.class);
    }

    public LoginResponse authenticateUser(String email, String password) {
        FirebaseSignInResponse firebaseResponse = login(email, password);
        
        // Mock user data - replace with actual user data retrieval
        return new LoginResponse(
            "user123", // id
            email,
            "John Doe", // fullname
            "+1234567890", // phoneNumber
            "johndoe", // username
            firebaseResponse.getIdToken() // token
        );
    }

    private FirebaseSignInResponse sendSignInRequest(FirebaseSignInRequest request) {
        log.info("Attempting Firebase login for: {}", request.getEmail());
        log.info("Using API key: {}", (webApiKey != null ? "Present" : "Missing"));
        
        try {
            return restClient.post()
                    .uri(SIGN_IN_URL + "?key=" + webApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(FirebaseSignInResponse.class);

        } catch (HttpClientErrorException ex) {
            String responseBody = ex.getResponseBodyAsString();
            log.error("Firebase error response: {}", responseBody);
            log.error("HTTP Status: {}", ex.getStatusCode());
            
            if (responseBody.contains(INVALID_CREDENTIALS_ERROR) || 
                responseBody.contains("INVALID_PASSWORD") ||
                responseBody.contains("EMAIL_NOT_FOUND") ||
                responseBody.contains("USER_DISABLED") ||
                responseBody.contains("TOO_MANY_ATTEMPTS_TRY_LATER")) {
                throw new InvalidLoginCredentialsException("Invalid login credentials provided");
            }
            throw ex;
        }
    }


}
