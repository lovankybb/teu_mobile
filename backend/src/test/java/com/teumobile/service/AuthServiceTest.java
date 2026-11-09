package com.teumobile.service;

import com.teumobile.client.KeycloakAdminFeignClient;
import com.teumobile.client.KeycloakAuthFeignClient;
import com.teumobile.config.KeycloakProperties;
import com.teumobile.dto.UserProfileResponseDto;
import com.teumobile.dto.auth.LoginRequestDto;
import com.teumobile.dto.auth.RefreshTokenRequestDto;
import com.teumobile.dto.auth.RegisterRequestDto;
import com.teumobile.dto.auth.TokenResponseDto;
import com.teumobile.dto.keycloak.KeycloakTokenResponse;
import com.teumobile.dto.keycloak.KeycloakUserRepresentation;
import com.teumobile.entity.User;
import com.teumobile.mapper.UserMapper;
import com.teumobile.repository.UserRepository;
import com.teumobile.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private KeycloakAuthFeignClient authFeignClient;

    @Mock
    private KeycloakAdminFeignClient adminFeignClient;

    @Mock
    private KeycloakProperties keycloakProperties;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        lenient().when(keycloakProperties.getClientId()).thenReturn("teu-mobile-client");
        lenient().when(keycloakProperties.getRealm()).thenReturn("teu-mobile-realm");
        lenient().when(keycloakProperties.getClientSecret()).thenReturn("mock-client-secret");
    }

    @Test
    @DisplayName("Đăng nhập thành công trả về TokenResponseDto")
    void testLoginSuccess() {
        LoginRequestDto request = LoginRequestDto.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .build();

        KeycloakTokenResponse tokenResponse = KeycloakTokenResponse.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .expiresIn(300L)
                .refreshExpiresIn(1800L)
                .tokenType("Bearer")
                .build();

        when(authFeignClient.getToken(eq("teu-mobile-realm"), any(Map.class))).thenReturn(tokenResponse);

        TokenResponseDto result = authService.login(request);

        assertNotNull(result);
        assertEquals("mock-access-token", result.getAccessToken());
        assertEquals("mock-refresh-token", result.getRefreshToken());
        assertEquals(300L, result.getExpiresIn());
    }

    @Test
    @DisplayName("Đăng ký người dùng mới thành công và lưu vào SQL Server")
    void testRegisterSuccess() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("newcustomer")
                .email("customer@gmail.com")
                .password("password123")
                .fullName("Nguyễn Khách Hàng")
                .phoneNumber("0912345678")
                .address("Hà Nội")
                .build();

        when(userRepository.existsByUsername("newcustomer")).thenReturn(false);
        when(userRepository.existsByEmail("customer@gmail.com")).thenReturn(false);

        // Giả lập lấy token client_credentials
        KeycloakTokenResponse adminTokenResponse = KeycloakTokenResponse.builder()
                .accessToken("mock-admin-token")
                .build();
        when(authFeignClient.getToken(eq("teu-mobile-realm"), any(Map.class))).thenReturn(adminTokenResponse);

        // Giả lập response từ Keycloak Admin createUser có Location Header
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("http://localhost:8180/admin/realms/teu-mobile-realm/users/keycloak-user-uuid-888"));
        ResponseEntity<Void> createResponse = ResponseEntity.created(headers.getLocation()).build();

        when(adminFeignClient.createUser(eq("Bearer mock-admin-token"), eq("teu-mobile-realm"), any(KeycloakUserRepresentation.class)))
                .thenReturn(createResponse);

        User savedUser = User.builder()
                .id(1L)
                .keycloakId("keycloak-user-uuid-888")
                .username("newcustomer")
                .email("customer@gmail.com")
                .fullName("Nguyễn Khách Hàng")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserProfileResponseDto expectedDto = UserProfileResponseDto.builder()
                .id(1L)
                .keycloakId("keycloak-user-uuid-888")
                .username("newcustomer")
                .email("customer@gmail.com")
                .fullName("Nguyễn Khách Hàng")
                .build();
        when(userMapper.toDto(savedUser)).thenReturn(expectedDto);

        UserProfileResponseDto result = authService.register(request);

        assertNotNull(result);
        assertEquals("keycloak-user-uuid-888", result.getKeycloakId());
        assertEquals("newcustomer", result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));

        // Kiểm tra Feign Client được gọi với đúng grant_type client_credentials
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, ?>> formCaptor = ArgumentCaptor.forClass(Map.class);
        verify(authFeignClient, times(1)).getToken(eq("teu-mobile-realm"), formCaptor.capture());
        Map<String, ?> capturedParams = formCaptor.getValue();
        assertEquals("client_credentials", capturedParams.get("grant_type"));
        assertEquals("teu-mobile-client", capturedParams.get("client_id"));
        assertEquals("mock-client-secret", capturedParams.get("client_secret"));
    }

    @Test
    @DisplayName("Đăng ký trùng username ném ra IllegalArgumentException")
    void testRegisterDuplicateUsername() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("existinguser")
                .email("new@gmail.com")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        verify(adminFeignClient, never()).createUser(any(), any(), any());
    }

    @Test
    @DisplayName("Làm mới token thành công")
    void testRefreshTokenSuccess() {
        RefreshTokenRequestDto request = RefreshTokenRequestDto.builder()
                .refreshToken("old-refresh-token")
                .build();

        KeycloakTokenResponse tokenResponse = KeycloakTokenResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .expiresIn(300L)
                .build();

        when(authFeignClient.getToken(eq("teu-mobile-realm"), any(Map.class))).thenReturn(tokenResponse);

        TokenResponseDto result = authService.refreshToken(request);

        assertNotNull(result);
        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
    }
}
