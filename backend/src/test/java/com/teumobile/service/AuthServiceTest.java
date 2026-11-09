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
}
