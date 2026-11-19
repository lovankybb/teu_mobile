package com.teumobile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teumobile.config.KeycloakProperties;
import com.teumobile.config.SecurityConfig;
import com.teumobile.dto.AvatarUpdateRequestDto;
import com.teumobile.dto.CloudinarySignatureResponseDto;
import com.teumobile.dto.UserProfileResponseDto;
import com.teumobile.dto.UserSyncRequestDto;
import com.teumobile.dto.UserUpdateRequestDto;
import com.teumobile.exception.GlobalExceptionHandler;
import com.teumobile.exception.ResourceNotFoundException;
import com.teumobile.security.KeycloakJwtAuthenticationConverter;
import com.teumobile.service.CloudinaryService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({SecurityConfig.class, KeycloakJwtAuthenticationConverter.class, KeycloakProperties.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private CloudinaryService cloudinaryService;

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
    @DisplayName("Cập nhật thông tin cá nhân với dữ liệu hợp lệ thành công trả về 200 OK")
    void testUpdateProfileSuccess() throws Exception {
        UserUpdateRequestDto validDto = UserUpdateRequestDto.builder()
                .fullName("Nguyễn Văn A Mới")
                .phoneNumber("0912345678")
                .address("Hà Nội, Việt Nam")
                .build();

        UserProfileResponseDto updatedProfile = UserProfileResponseDto.builder()
                .id(1L)
                .username("customer_test")
                .fullName("Nguyễn Văn A Mới")
                .phoneNumber("0912345678")
                .address("Hà Nội, Việt Nam")
                .build();

        when(userService.updateProfile(any(), any(UserUpdateRequestDto.class))).thenReturn(updatedProfile);

        mockMvc.perform(put("/api/users/me")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Nguyễn Văn A Mới"))
                .andExpect(jsonPath("$.data.phoneNumber").value("0912345678"));
    }

    @Test
    @DisplayName("Đồng bộ User từ JWT thành công khi có Bearer Token trả về 200 OK")
    void testSyncUserAuthorized() throws Exception {
        UserSyncRequestDto syncDto = UserSyncRequestDto.builder()
                .phoneNumber("0912345678")
                .address("Hà Nội")
                .build();

        UserProfileResponseDto syncedProfile = UserProfileResponseDto.builder()
                .id(1L)
                .username("customer_test")
                .email("customer@gmail.com")
                .phoneNumber("0912345678")
                .build();

        when(userService.syncUserFromJwt(any(), any())).thenReturn(syncedProfile);

        mockMvc.perform(post("/api/users/sync")
                        .with(jwt().jwt(builder -> builder
                                .subject("sub-uuid-123")
                                .claim("preferred_username", "customer_test")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("customer_test"));
    }

    @Test
    @DisplayName("Đồng bộ User khi chưa đăng nhập trả về 401 Unauthorized")
    void testSyncUserUnauthorized() throws Exception {
        mockMvc.perform(post("/api/users/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Admin tìm User theo ID không tồn tại trả về 404 Not Found")
    void testGetUserByIdNotFoundForAdmin() throws Exception {
        when(userService.getUserById(999L)).thenThrow(new ResourceNotFoundException("User", "id", 999L));

        mockMvc.perform(get("/api/users/999")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
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

    @Test
    @DisplayName("Xin chữ ký Cloudinary upload khi chưa đăng nhập trả về 401 Unauthorized")
    void testGetAvatarSignatureUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/avatar/signature"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Xin chữ ký Cloudinary upload với Token hợp lệ trả về 200 OK kèm signature và timestamp")
    void testGetAvatarSignatureAuthorized() throws Exception {
        CloudinarySignatureResponseDto signatureDto = CloudinarySignatureResponseDto.builder()
                .signature("sample-sha1-signature-abc123")
                .timestamp(1731920000L)
                .apiKey("sample-api-key")
                .cloudName("sample-cloud")
                .folder("teu-mobile/avatars")
                .build();

        when(cloudinaryService.generateAvatarUploadSignature()).thenReturn(signatureDto);

        mockMvc.perform(get("/api/users/avatar/signature")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.signature").value("sample-sha1-signature-abc123"))
                .andExpect(jsonPath("$.data.timestamp").value(1731920000L))
                .andExpect(jsonPath("$.data.apiKey").value("sample-api-key"))
                .andExpect(jsonPath("$.data.cloudName").value("sample-cloud"))
                .andExpect(jsonPath("$.data.folder").value("teu-mobile/avatars"));
    }

    @Test
    @DisplayName("Cập nhật avatar với URL hợp lệ thành công trả về 200 OK")
    void testUpdateAvatarSuccess() throws Exception {
        String validUrl = "https://res.cloudinary.com/sample-cloud/image/upload/v123/avatar.jpg";
        AvatarUpdateRequestDto requestDto = AvatarUpdateRequestDto.builder()
                .avatarUrl(validUrl)
                .build();

        UserProfileResponseDto responseDto = UserProfileResponseDto.builder()
                .id(1L)
                .username("customer_test")
                .avatarUrl(validUrl)
                .build();

        when(userService.updateAvatar(any(), eq(validUrl))).thenReturn(responseDto);

        mockMvc.perform(patch("/api/users/me/avatar")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.avatarUrl").value(validUrl));
    }

    @Test
    @DisplayName("Cập nhật avatar với URL không hợp lệ trả về 400 Bad Request")
    void testUpdateAvatarInvalidUrl() throws Exception {
        AvatarUpdateRequestDto invalidDto = AvatarUpdateRequestDto.builder()
                .avatarUrl("not-a-valid-url")
                .build();

        mockMvc.perform(patch("/api/users/me/avatar")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.avatarUrl").exists());
    }
}
