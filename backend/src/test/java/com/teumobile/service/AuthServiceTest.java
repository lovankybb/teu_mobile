package com.teumobile.service;

import com.teumobile.client.KeycloakAdminFeignClient;
import com.teumobile.client.KeycloakAuthFeignClient;
import com.teumobile.config.KeycloakProperties;
import com.teumobile.dto.UserProfileResponseDto;
import com.teumobile.dto.auth.RegisterRequestDto;
import com.teumobile.dto.keycloak.KeycloakTokenResponse;
import com.teumobile.dto.keycloak.KeycloakUserRepresentation;
import com.teumobile.entity.User;
import com.teumobile.mapper.UserMapper;
import com.teumobile.repository.UserRepository;
import com.teumobile.service.impl.AuthServiceImpl;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

        UserProfileResponseDto expectedDto = UserProfileResponseDto.builder()
                .id(1L)
                .username("newcustomer")
                .email("customer@gmail.com")
                .fullName("Nguyễn Khách Hàng")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(expectedDto);

        UserProfileResponseDto result = authService.register(request);

        assertNotNull(result);
        assertEquals("newcustomer", result.getUsername());
        assertEquals("customer@gmail.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Đăng ký thất bại khi username đã tồn tại")
    void testRegisterFailsWhenUsernameExists() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("existinguser")
                .email("user@gmail.com")
                .password("password123")
                .fullName("Người Dùng")
                .build();

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đăng ký thất bại khi email đã tồn tại")
    void testRegisterFailsWhenEmailExists() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("newuser")
                .email("existing@gmail.com")
                .password("password123")
                .fullName("Người Dùng")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@gmail.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đăng ký fallback tìm kiếm username khi Keycloak Admin không trả về Location Header")
    void testRegisterFallbackWhenLocationHeaderNull() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("newcustomer")
                .email("customer@gmail.com")
                .password("password123")
                .fullName("Nguyễn Khách Hàng")
                .build();

        when(userRepository.existsByUsername("newcustomer")).thenReturn(false);
        when(userRepository.existsByEmail("customer@gmail.com")).thenReturn(false);

        KeycloakTokenResponse adminTokenResponse = KeycloakTokenResponse.builder()
                .accessToken("mock-admin-token")
                .build();
        when(authFeignClient.getToken(eq("teu-mobile-realm"), any(Map.class))).thenReturn(adminTokenResponse);

        // Location Header bị null
        ResponseEntity<Void> createResponse = ResponseEntity.ok().build();
        when(adminFeignClient.createUser(eq("Bearer mock-admin-token"), eq("teu-mobile-realm"), any(KeycloakUserRepresentation.class)))
                .thenReturn(createResponse);

        // Fallback: search user by username
        KeycloakUserRepresentation foundUser = KeycloakUserRepresentation.builder()
                .id("fallback-user-uuid-999")
                .username("newcustomer")
                .build();
        when(adminFeignClient.searchUsersByUsername("Bearer mock-admin-token", "teu-mobile-realm", "newcustomer", true))
                .thenReturn(List.of(foundUser));

        User savedUser = User.builder()
                .id(1L)
                .keycloakId("fallback-user-uuid-999")
                .username("newcustomer")
                .email("customer@gmail.com")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(UserProfileResponseDto.builder()
                .id(1L)
                .keycloakId("fallback-user-uuid-999")
                .username("newcustomer")
                .build());

        UserProfileResponseDto result = authService.register(request);

        assertNotNull(result);
        assertEquals("fallback-user-uuid-999", result.getKeycloakId());
        verify(adminFeignClient).searchUsersByUsername("Bearer mock-admin-token", "teu-mobile-realm", "newcustomer", true);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Đăng ký thất bại khi Keycloak ném FeignException.Conflict (tài khoản đã tồn tại trên IdP)")
    void testRegisterFailsOnKeycloakConflict() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("existingidp")
                .email("idp@gmail.com")
                .password("password123")
                .fullName("Người Dùng")
                .build();

        when(userRepository.existsByUsername("existingidp")).thenReturn(false);
        when(userRepository.existsByEmail("idp@gmail.com")).thenReturn(false);

        KeycloakTokenResponse adminTokenResponse = KeycloakTokenResponse.builder()
                .accessToken("mock-admin-token")
                .build();
        when(authFeignClient.getToken(eq("teu-mobile-realm"), any(Map.class))).thenReturn(adminTokenResponse);

        FeignException.Conflict conflictEx = mock(FeignException.Conflict.class);
        when(adminFeignClient.createUser(eq("Bearer mock-admin-token"), eq("teu-mobile-realm"), any(KeycloakUserRepresentation.class)))
                .thenThrow(conflictEx);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        assertTrue(ex.getMessage().contains("Tài khoản đã tồn tại trên hệ thống xác thực"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đăng ký thất bại khi Keycloak gặp lỗi hệ thống khác")
    void testRegisterFailsOnKeycloakGenericError() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("anyuser")
                .email("any@gmail.com")
                .password("password123")
                .fullName("Người Dùng")
                .build();

        when(userRepository.existsByUsername("anyuser")).thenReturn(false);
        when(userRepository.existsByEmail("any@gmail.com")).thenReturn(false);

        KeycloakTokenResponse adminTokenResponse = KeycloakTokenResponse.builder()
                .accessToken("mock-admin-token")
                .build();
        when(authFeignClient.getToken(eq("teu-mobile-realm"), any(Map.class))).thenReturn(adminTokenResponse);

        when(adminFeignClient.createUser(eq("Bearer mock-admin-token"), eq("teu-mobile-realm"), any(KeycloakUserRepresentation.class)))
                .thenThrow(new RuntimeException("Kết nối Keycloak bị ngắt"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(request));
        assertTrue(ex.getMessage().contains("Không thể tạo tài khoản trên hệ thống xác thực"));
        verify(userRepository, never()).save(any());
    }
}
