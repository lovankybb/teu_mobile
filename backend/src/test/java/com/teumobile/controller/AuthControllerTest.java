package com.teumobile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teumobile.config.KeycloakProperties;
import com.teumobile.config.SecurityConfig;
import com.teumobile.dto.UserProfileResponseDto;
import com.teumobile.dto.auth.RegisterRequestDto;
import com.teumobile.exception.GlobalExceptionHandler;
import com.teumobile.security.KeycloakJwtAuthenticationConverter;
import com.teumobile.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, KeycloakJwtAuthenticationConverter.class, KeycloakProperties.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/register thành công trả về 201 Created")
    void testRegisterSuccess() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("newuser2026")
                .email("user@gmail.com")
                .password("password123")
                .fullName("Khách hàng mới")
                .phoneNumber("0912345678")
                .build();

        UserProfileResponseDto responseDto = UserProfileResponseDto.builder()
                .id(1L)
                .username("newuser2026")
                .email("user@gmail.com")
                .fullName("Khách hàng mới")
                .build();

        when(authService.register(any(RegisterRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("newuser2026"));
    }

    @Test
    @DisplayName("POST /api/auth/register dữ liệu không hợp lệ trả về 400 Bad Request")
    void testRegisterInvalidPayload() throws Exception {
        RegisterRequestDto invalid = RegisterRequestDto.builder()
                .username("ab") // Quá ngắn
                .email("invalid-email") // Sai định dạng email
                .password("123") // Mật khẩu dưới 6 ký tự
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
