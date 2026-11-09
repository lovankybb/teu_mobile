package com.teumobile.service.impl;

import com.teumobile.client.KeycloakAdminFeignClient;
import com.teumobile.client.KeycloakAuthFeignClient;
import com.teumobile.config.KeycloakProperties;
import com.teumobile.dto.UserProfileResponseDto;
import com.teumobile.dto.auth.LoginRequestDto;
import com.teumobile.dto.auth.RefreshTokenRequestDto;
import com.teumobile.dto.auth.RegisterRequestDto;
import com.teumobile.dto.auth.TokenResponseDto;
import com.teumobile.dto.keycloak.KeycloakCredentialRepresentation;
import com.teumobile.dto.keycloak.KeycloakTokenResponse;
import com.teumobile.dto.keycloak.KeycloakUserRepresentation;
import com.teumobile.entity.User;
import com.teumobile.mapper.UserMapper;
import com.teumobile.repository.UserRepository;
import com.teumobile.service.AuthService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final KeycloakAuthFeignClient authFeignClient;
    private final KeycloakAdminFeignClient adminFeignClient;
    private final KeycloakProperties keycloakProperties;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public TokenResponseDto login(LoginRequestDto request) {
        Map<String, String> formParams = new HashMap<>();
        formParams.put("grant_type", "password");
        formParams.put("client_id", keycloakProperties.getClientId());
        if (keycloakProperties.getClientSecret() != null && !keycloakProperties.getClientSecret().isBlank()) {
            formParams.put("client_secret", keycloakProperties.getClientSecret());
        }
        formParams.put("username", request.getUsernameOrEmail());
        formParams.put("password", request.getPassword());
        formParams.put("scope", "openid profile email");

        try {
            KeycloakTokenResponse tokenResponse = authFeignClient.getToken(keycloakProperties.getRealm(), formParams);
            return mapToTokenResponseDto(tokenResponse);
        } catch (FeignException.Unauthorized | FeignException.BadRequest ex) {
            log.warn("Đăng nhập thất bại cho user {}: {}", request.getUsernameOrEmail(), ex.getMessage());
            throw new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không chính xác");
        } catch (Exception ex) {
            log.error("Lỗi khi kết nối tới máy chủ Keycloak: {}", ex.getMessage());
            throw new RuntimeException("Lỗi kết nối tới hệ thống xác thực Keycloak: " + ex.getMessage());
        }
    }

    @Override
    @Transactional
    public UserProfileResponseDto register(RegisterRequestDto request) {
        // 1. Kiểm tra tồn tại trong SQL Server
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập '" + request.getUsername() + "' đã tồn tại");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' đã được sử dụng");
        }

        // 2. Lấy Access Token của Keycloak bằng client_credentials
        String adminToken = getAdminAccessToken();

        // 3. Chuẩn bị payload tạo User trên Keycloak
        String[] nameParts = splitFullName(request.getFullName());
        KeycloakUserRepresentation keycloakUser = KeycloakUserRepresentation.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(nameParts[0])
                .lastName(nameParts[1])
                .enabled(true)
                .emailVerified(true)
                .credentials(List.of(
                        KeycloakCredentialRepresentation.builder()
                                .type("password")
                                .value(request.getPassword())
                                .temporary(false)
                                .build()
                ))
                .build();

        String keycloakUserId = null;
        try {
            ResponseEntity<Void> response = adminFeignClient.createUser(
                    "Bearer " + adminToken,
                    keycloakProperties.getRealm(),
                    keycloakUser
            );

            // 4. Trích xuất Keycloak User ID từ header Location (ví dụ: .../users/UUID)
            URI location = response.getHeaders().getLocation();
            if (location != null) {
                String path = location.getPath();
                keycloakUserId = path.substring(path.lastIndexOf('/') + 1);
            }
        } catch (FeignException.Conflict ex) {
            throw new IllegalArgumentException("Tài khoản hoặc email đã tồn tại trên máy chủ xác thực Keycloak");
        } catch (Exception ex) {
            log.error("Lỗi khi tạo user trên Keycloak qua Admin API: {}", ex.getMessage());
            throw new RuntimeException("Không thể đăng ký người dùng trên Keycloak: " + ex.getMessage());
        }

        // Nếu header Location không trả về UUID thì tra cứu lại qua search API
        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            List<KeycloakUserRepresentation> users = adminFeignClient.searchUsersByUsername(
                    "Bearer " + adminToken,
                    keycloakProperties.getRealm(),
                    request.getUsername(),
                    true
            );
            if (!users.isEmpty()) {
                keycloakUserId = users.get(0).getId();
            } else {
                keycloakUserId = UUID.randomUUID().toString();
            }
        }

        // 5. Lưu thông tin người dùng vào SQL Server (teu_mobile_db)
        User localUser = User.builder()
                .keycloakId(keycloakUserId)
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(localUser);
        log.info("Đăng ký thành công người dùng: username={}, keycloakId={}", savedUser.getUsername(), keycloakUserId);
        return userMapper.toDto(savedUser);
    }

    @Override
    public TokenResponseDto refreshToken(RefreshTokenRequestDto request) {
        Map<String, String> formParams = new HashMap<>();
        formParams.put("grant_type", "refresh_token");
        formParams.put("client_id", keycloakProperties.getClientId());
        if (keycloakProperties.getClientSecret() != null && !keycloakProperties.getClientSecret().isBlank()) {
            formParams.put("client_secret", keycloakProperties.getClientSecret());
        }
        formParams.put("refresh_token", request.getRefreshToken());

        try {
            KeycloakTokenResponse tokenResponse = authFeignClient.getToken(keycloakProperties.getRealm(), formParams);
            return mapToTokenResponseDto(tokenResponse);
        } catch (Exception ex) {
            log.warn("Làm mới token thất bại: {}", ex.getMessage());
            throw new IllegalArgumentException("Refresh token không hợp lệ hoặc đã hết hạn");
        }
    }

    @Override
    public void logout(RefreshTokenRequestDto request) {
        Map<String, String> formParams = new HashMap<>();
        formParams.put("client_id", keycloakProperties.getClientId());
        if (keycloakProperties.getClientSecret() != null && !keycloakProperties.getClientSecret().isBlank()) {
            formParams.put("client_secret", keycloakProperties.getClientSecret());
        }
        formParams.put("refresh_token", request.getRefreshToken());

        try {
            authFeignClient.logout(keycloakProperties.getRealm(), formParams);
            log.info("Đã đăng xuất phiên thành công");
        } catch (Exception ex) {
            log.warn("Đăng xuất Keycloak gặp cảnh báo: {}", ex.getMessage());
        }
    }

    /**
     * Lấy Access Token bằng grant_type client_credentials từ Realm của ứng dụng để gọi Keycloak Admin API
     */
    private String getAdminAccessToken() {
        Map<String, String> formParams = new HashMap<>();
        formParams.put("grant_type", "client_credentials");
        formParams.put("client_id", keycloakProperties.getClientId());
        formParams.put("scope", "openid");
        if (keycloakProperties.getClientSecret() != null && !keycloakProperties.getClientSecret().isBlank()) {
            formParams.put("client_secret", keycloakProperties.getClientSecret());
        }

        log.info(keycloakProperties.getClientId());
        log.info(keycloakProperties.getRealm());
        log.info("Secret: " + keycloakProperties.getClientSecret());


        try {
            KeycloakTokenResponse tokenResponse = authFeignClient.getToken(
                    keycloakProperties.getRealm(),
                    formParams
            );
            return tokenResponse.getAccessToken();
        } catch (Exception ex) {
            log.error("Lỗi khi kết nối Keycloak bằng client_credentials: {}", ex.getMessage());
            throw new RuntimeException("Không thể xác thực với Keycloak bằng client_credentials: " + ex.getMessage(), ex);
        }
    }

    private TokenResponseDto mapToTokenResponseDto(KeycloakTokenResponse response) {
        return TokenResponseDto.builder()
                .accessToken(response.getAccessToken())
                .refreshToken(response.getRefreshToken())
                .expiresIn(response.getExpiresIn())
                .refreshExpiresIn(response.getRefreshExpiresIn())
                .tokenType(response.getTokenType() != null ? response.getTokenType() : "Bearer")
                .build();
    }

    private String[] splitFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{"", ""};
        }
        String trimmed = fullName.trim();
        int lastSpaceIndex = trimmed.lastIndexOf(' ');
        if (lastSpaceIndex > 0) {
            return new String[]{trimmed.substring(0, lastSpaceIndex), trimmed.substring(lastSpaceIndex + 1)};
        }
        return new String[]{trimmed, ""};
    }
}
