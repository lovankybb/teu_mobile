package com.teumobile.controller;

import com.teumobile.dto.ApiResponse;
import com.teumobile.dto.UserProfileResponseDto;
import com.teumobile.dto.UserSyncRequestDto;
import com.teumobile.dto.UserUpdateRequestDto;
import com.teumobile.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Lấy thông tin hồ sơ của tài khoản đang đăng nhập
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> getCurrentUser(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserProfileResponseDto profile = userService.getCurrentUserProfile(jwt);
        return ResponseEntity.ok(ApiResponse.success(profile, "Lấy thông tin tài khoản thành công"));
    }

    /**
     * Đồng bộ tài khoản từ Token Keycloak vào SQL Server
     */
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> syncUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody(required = false) UserSyncRequestDto requestDto
    ) {
        UserProfileResponseDto syncedProfile = userService.syncUserFromJwt(jwt, requestDto);
        return ResponseEntity.ok(ApiResponse.success(syncedProfile, "Đồng bộ tài khoản thành công"));
    }

    /**
     * Cập nhật thông tin cá nhân (họ tên, số điện thoại, địa chỉ, avatar)
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserUpdateRequestDto updateDto
    ) {
        UserProfileResponseDto updatedProfile = userService.updateProfile(jwt, updateDto);
        return ResponseEntity.ok(ApiResponse.success(updatedProfile, "Cập nhật thông tin cá nhân thành công"));
    }

    /**
     * Xem thông tin người dùng theo ID (Dành cho quản trị viên ADMIN)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> getUserById(
            @PathVariable Long id
    ) {
        UserProfileResponseDto profile = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(profile, "Lấy thông tin người dùng thành công"));
    }
}
