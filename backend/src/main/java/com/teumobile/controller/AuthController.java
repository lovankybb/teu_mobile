package com.teumobile.controller;

import com.teumobile.dto.ApiResponse;
import com.teumobile.dto.UserProfileResponseDto;
import com.teumobile.dto.auth.RegisterRequestDto;
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
}
