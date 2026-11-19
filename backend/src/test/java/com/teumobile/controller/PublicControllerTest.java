package com.teumobile.controller;

import com.teumobile.config.KeycloakProperties;
import com.teumobile.config.SecurityConfig;
import com.teumobile.exception.GlobalExceptionHandler;
import com.teumobile.security.KeycloakJwtAuthenticationConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicController.class)
@Import({SecurityConfig.class, KeycloakJwtAuthenticationConverter.class, KeycloakProperties.class, GlobalExceptionHandler.class})
class PublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/public/health cho phép truy cập công khai không cần Bearer Token và trả về 200 OK")
    void testPublicHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/public/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("teu-mobile-backend"))
                .andExpect(jsonPath("$.message").value("Hệ thống đang hoạt động bình thường"));
    }
}
