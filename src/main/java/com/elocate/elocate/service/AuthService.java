package com.elocate.elocate.service;

import com.elocate.elocate.dto.FirebaseSignInRequest;
import com.elocate.elocate.dto.FirebaseSignInResponse;
import com.elocate.elocate.dto.LoginResponse;
import com.elocate.elocate.exception.InvalidLoginCredentialsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS_ERROR = "INVALID_LOGIN_CREDENTIALS";
    private static final String SIGN_IN_URL =
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword";

    @Value("${firebase.web-api-key}")
    private String webApiKey;

    private final RestClient restClient = RestClient.builder().build();

    public FirebaseSignInResponse login(String emailId, String password) {
        FirebaseSignInRequest requestBody = new FirebaseSignInRequest(emailId, password, true);
        return sendSignInRequest(requestBody);
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
        try {
            return restClient.post()
                    .uri(SIGN_IN_URL + "?key=" + webApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(FirebaseSignInResponse.class);

        } catch (HttpClientErrorException ex) {
            if (ex.getResponseBodyAsString().contains(INVALID_CREDENTIALS_ERROR)) {
                throw new InvalidLoginCredentialsException("Invalid login credentials provided");
            }
            throw ex;
        }
    }


}
