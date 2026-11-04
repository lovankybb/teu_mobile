package com.teumobile.service;

import com.teumobile.dto.UserProfileResponseDto;
import com.teumobile.dto.auth.LoginRequestDto;
import com.teumobile.dto.auth.RefreshTokenRequestDto;
import com.teumobile.dto.auth.RegisterRequestDto;
import com.teumobile.dto.auth.TokenResponseDto;

public interface AuthService {

    /**
     * Đăng nhập người dùng bằng OpenFeign gọi sang Keycloak
     */
    TokenResponseDto login(LoginRequestDto request);

    /**
     * Đăng ký tài khoản người dùng mới: Tạo trên Keycloak và lưu vào SQL Server
     */
    UserProfileResponseDto register(RegisterRequestDto request);

    /**
     * Làm mới token khi Access Token hết hạn
     */
    TokenResponseDto refreshToken(RefreshTokenRequestDto request);

    /**
     * Đăng xuất và thu hồi session trên Keycloak
     */
    void logout(RefreshTokenRequestDto request);
}
