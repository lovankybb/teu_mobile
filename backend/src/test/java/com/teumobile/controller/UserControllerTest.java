package com.teumobile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teumobile.config.KeycloakProperties;
import com.teumobile.config.SecurityConfig;
import com.teumobile.dto.UserProfileResponseDto;
import com.teumobile.dto.UserUpdateRequestDto;
import com.teumobile.exception.GlobalExceptionHandler;
import com.teumobile.security.KeycloakJwtAuthenticationConverter;
import com.teumobile.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserController.class, PublicController.class})
@Import({SecurityConfig.class, KeycloakJwtAuthenticationConverter.class, KeycloakProperties.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("Endpoint công khai /api/public/health truy cập được không cần Token")
    void testPublicHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/public/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    @DisplayName("Truy cập /api/users/me khi chưa đăng nhập (không có Bearer Token) trả về 401 Unauthorized")
    void testGetCurrentUserUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Truy cập /api/users/me với Token hợp lệ trả về 200 OK và Profile")
    void testGetCurrentUserAuthorized() throws Exception {
        UserProfileResponseDto profile = UserProfileResponseDto.builder()
                .id(1L)
                .keycloakId("sub-uuid-123")
                .username("customer_test")
                .email("customer@gmail.com")
                .fullName("Khách hàng Test")
                .isActive(true)
                .build();

        when(userService.getCurrentUserProfile(any())).thenReturn(profile);

        mockMvc.perform(get("/api/users/me")
                        .with(jwt().jwt(builder -> builder
                                .subject("sub-uuid-123")
                                .claim("preferred_username", "customer_test")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("customer_test"));
    }

    @Test
    @DisplayName("Cập nhật số điện thoại sai định dạng trả về 400 Bad Request kèm message lỗi từ custom validator")
    void testUpdateProfileWithInvalidPhoneNumber() throws Exception {
        UserUpdateRequestDto invalidDto = UserUpdateRequestDto.builder()
                .phoneNumber("123456") // Sai định dạng (ít hơn 10 chữ số)
                .build();

        mockMvc.perform(put("/api/users/me")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.phoneNumber").exists());
    }

    @Test
    @DisplayName("User với role CUSTOMER truy cập /api/users/{id} bị từ chối 403 Forbidden")
    void testAdminEndpointForbiddenForCustomer() throws Exception {
        mockMvc.perform(get("/api/users/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User với role ADMIN truy cập /api/users/{id} thành công 200 OK")
    void testAdminEndpointAllowedForAdmin() throws Exception {
        UserProfileResponseDto profile = UserProfileResponseDto.builder()
                .id(1L)
                .username("customer_test")
                .build();

        when(userService.getUserById(1L)).thenReturn(profile);

        mockMvc.perform(get("/api/users/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
