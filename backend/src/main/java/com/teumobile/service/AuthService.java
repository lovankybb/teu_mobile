package com.teumobile.service;

import com.teumobile.dto.UserProfileResponseDto;
import com.teumobile.dto.auth.RegisterRequestDto;

public interface AuthService {

    /**
     * Đăng ký tài khoản người dùng mới: Tạo trên Keycloak và lưu vào SQL Server
     */
    UserProfileResponseDto register(RegisterRequestDto request);
}
