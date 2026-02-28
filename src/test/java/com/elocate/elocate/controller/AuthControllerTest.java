package com.elocate.elocate.controller;

import com.elocate.elocate.context.UserContext;
import com.elocate.elocate.context.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testValidateToken_Success() throws Exception {
        // Setup UserContext manually since WithMockUser only sets SecurityContext
        UUID userId = UUID.randomUUID();
        UserContext context = UserContext.builder()
                .userId(userId)
                .email("test@example.com")
                .fullName("Test User")
                .build();
        UserContextHolder.setContext(context);

        mockMvc.perform(get("/api/v1/auth/validate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    public void testValidateToken_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // Filter should block it
    }
}
