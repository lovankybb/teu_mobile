package com.teumobile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teumobile.config.KeycloakProperties;
import com.teumobile.config.SecurityConfig;
import com.teumobile.dto.PageResponseDto;
import com.teumobile.dto.admin.*;
import com.teumobile.exception.GlobalExceptionHandler;
import com.teumobile.security.KeycloakJwtAuthenticationConverter;
import com.teumobile.service.AdminUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminUserController.class)
@Import({SecurityConfig.class, KeycloakJwtAuthenticationConverter.class, KeycloakProperties.class, GlobalExceptionHandler.class})
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminUserService adminUserService;

    @Test
    @DisplayName("Truy cập /api/admin/users khi chưa đăng nhập trả về 401 Unauthorized")
    void testGetUsersUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Truy cập /api/admin/users với quyền ROLE_USER bị từ chối 403 Forbidden")
    void testGetUsersForbiddenForUser() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Lấy danh sách người dùng với quyền ROLE_ADMIN thành công trả về 200 OK")
    void testGetUsersSuccess() throws Exception {
        UserAdminResponseDto userDto = UserAdminResponseDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@teumobile.com")
                .isActive(true)
                .roles(List.of("USER"))
                .build();

        PageResponseDto<UserAdminResponseDto> pageResponse = PageResponseDto.<UserAdminResponseDto>builder()
                .content(List.of(userDto))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(adminUserService.getUsers(any(), any(), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/admin/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("page", "0")
                        .param("size", "10")
                        .param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].username").value("testuser"));
    }

    @Test
    @DisplayName("Lấy thống kê người dùng với quyền ROLE_ADMIN trả về 200 OK")
    void testGetUserStatsSuccess() throws Exception {
        UserStatsResponseDto stats = new UserStatsResponseDto(50L, 45L, 5L);
        when(adminUserService.getUserStats()).thenReturn(stats);

        mockMvc.perform(get("/api/admin/users/stats")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalUsers").value(50))
                .andExpect(jsonPath("$.data.activeUsers").value(45));
    }

    @Test
    @DisplayName("Xem chi tiết người dùng theo ID với quyền ROLE_ADMIN trả về 200 OK")
    void testGetUserDetailSuccess() throws Exception {
        UserAdminDetailResponseDto detail = UserAdminDetailResponseDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@teumobile.com")
                .addresses(List.of())
                .roles(List.of("USER"))
                .build();

        when(adminUserService.getUserDetail(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/admin/users/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("Cập nhật trạng thái người dùng (Khóa/Mở) trả về 200 OK")
    void testUpdateUserStatusSuccess() throws Exception {
        UserStatusUpdateRequestDto request = new UserStatusUpdateRequestDto(false);
        UserAdminResponseDto updatedDto = UserAdminResponseDto.builder()
                .id(1L)
                .username("testuser")
                .isActive(false)
                .build();

        when(adminUserService.updateUserStatus(eq(1L), any(UserStatusUpdateRequestDto.class), any()))
                .thenReturn(updatedDto);

        mockMvc.perform(patch("/api/admin/users/1/status")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    @DisplayName("Cập nhật trạng thái người dùng thất bại khi payload rỗng trả về 400 Bad Request")
    void testUpdateUserStatusValidationFail() throws Exception {
        mockMvc.perform(patch("/api/admin/users/1/status")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Gán vai trò cho người dùng trả về 200 OK")
    void testUpdateUserRoleSuccess() throws Exception {
        UserRoleUpdateRequestDto request = new UserRoleUpdateRequestDto("ADMIN", true);
        UserAdminResponseDto updatedDto = UserAdminResponseDto.builder()
                .id(1L)
                .username("testuser")
                .roles(List.of("USER", "ADMIN"))
                .build();

        when(adminUserService.updateUserRole(eq(1L), any(UserRoleUpdateRequestDto.class), any()))
                .thenReturn(updatedDto);

        mockMvc.perform(patch("/api/admin/users/1/role")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roles[1]").value("ADMIN"));
    }

    @Test
    @DisplayName("Tạo người dùng mới từ trang quản trị trả về 201 Created")
    void testCreateUserSuccess() throws Exception {
        UserAdminCreateRequestDto request = UserAdminCreateRequestDto.builder()
                .username("newadmin")
                .email("admin@teumobile.com")
                .password("Password@123")
                .fullName("Quản trị mới")
                .phoneNumber("0987654321")
                .role("ADMIN")
                .build();

        UserAdminResponseDto createdDto = UserAdminResponseDto.builder()
                .id(2L)
                .username("newadmin")
                .email("admin@teumobile.com")
                .roles(List.of("ADMIN"))
                .build();

        when(adminUserService.createUser(any(UserAdminCreateRequestDto.class))).thenReturn(createdDto);

        mockMvc.perform(post("/api/admin/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("newadmin"));
    }

    @Test
    @DisplayName("Tạo người dùng thất bại khi thiếu thông tin bắt buộc trả về 400 Bad Request")
    void testCreateUserValidationFail() throws Exception {
        UserAdminCreateRequestDto invalidRequest = UserAdminCreateRequestDto.builder()
                .username("")
                .email("not-an-email")
                .password("123") // < 6 chars
                .build();

        mockMvc.perform(post("/api/admin/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
