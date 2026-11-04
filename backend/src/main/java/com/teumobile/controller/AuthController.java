package com.teumobile.controller;

import com.teumobile.dto.ApiResponse;
import com.teumobile.dto.UserProfileResponseDto;
import com.teumobile.dto.auth.LoginRequestDto;
import com.teumobile.dto.auth.RefreshTokenRequestDto;
import com.teumobile.dto.auth.RegisterRequestDto;
import com.teumobile.dto.auth.TokenResponseDto;
import com.teumobile.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Đăng ký tài khoản người dùng mới
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> register(
            @Valid @RequestBody RegisterRequestDto request
    ) {
        UserProfileResponseDto profile = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(profile, "Đăng ký tài khoản thành công"));
    }

    /**
     * Đăng nhập lấy JWT Access Token và Refresh Token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        TokenResponseDto tokens = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(tokens, "Đăng nhập thành công"));
    }

    /**
     * Làm mới Access Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponseDto>> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDto request
    ) {
        TokenResponseDto tokens = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(tokens, "Làm mới token thành công"));
    }

    /**
     * Đăng xuất và hủy phiên làm việc
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequestDto request
    ) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Đăng xuất thành công"));
    }
}
