package com.teumobile.service;

import com.teumobile.dto.UserProfileResponseDto;
import com.teumobile.dto.UserSyncRequestDto;
import com.teumobile.dto.UserUpdateRequestDto;
import org.springframework.security.oauth2.jwt.Jwt;

public interface UserService {

    /**
     * Đồng bộ tài khoản từ Token JWT của Keycloak vào SQL Server
     */
    UserProfileResponseDto syncUserFromJwt(Jwt jwt, UserSyncRequestDto additionalInfo);

    /**
     * Lấy thông tin hồ sơ của user hiện tại đang đăng nhập
     */
    UserProfileResponseDto getCurrentUserProfile(Jwt jwt);

    /**
     * Cập nhật thông tin cá nhân (họ tên, số điện thoại, địa chỉ, avatar)
     */
    UserProfileResponseDto updateProfile(Jwt jwt, UserUpdateRequestDto updateDto);

    /**
     * Lấy thông tin user theo ID (cho Admin hoặc quản trị)
     */
    UserProfileResponseDto getUserById(Long id);
}
