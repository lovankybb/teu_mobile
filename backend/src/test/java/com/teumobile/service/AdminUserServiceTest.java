package com.teumobile.service;

import com.teumobile.client.KeycloakAdminFeignClient;
import com.teumobile.client.KeycloakAuthFeignClient;
import com.teumobile.config.KeycloakProperties;
import com.teumobile.dto.AddressResponseDto;
import com.teumobile.dto.PageResponseDto;
import com.teumobile.dto.admin.*;
import com.teumobile.dto.keycloak.*;
import com.teumobile.entity.User;
import com.teumobile.entity.UserAddress;
import com.teumobile.exception.ResourceNotFoundException;
import com.teumobile.mapper.UserAddressMapper;
import com.teumobile.mapper.UserMapper;
import com.teumobile.repository.UserAddressRepository;
import com.teumobile.repository.UserRepository;
import com.teumobile.service.impl.AdminUserServiceImpl;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.net.URI;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAddressRepository userAddressRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserAddressMapper userAddressMapper;

    @Mock
    private KeycloakAuthFeignClient authFeignClient;

    @Mock
    private KeycloakAdminFeignClient adminFeignClient;

    @Mock
    private KeycloakProperties keycloakProperties;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User sampleUser;
    private UserAdminResponseDto sampleAdminDto;
    private KeycloakTokenResponse sampleTokenResponse;
    private Jwt selfJwt;
    private Jwt otherJwt;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .keycloakId("uuid-keycloak-1")
                .username("testuser")
                .email("test@teumobile.com")
                .fullName("Nguyễn Văn Test")
                .phoneNumber("0987654321")
                .isActive(true)
                .build();

        sampleAdminDto = UserAdminResponseDto.builder()
                .id(1L)
                .keycloakId("uuid-keycloak-1")
                .username("testuser")
                .email("test@teumobile.com")
                .fullName("Nguyễn Văn Test")
                .phoneNumber("0987654321")
                .isActive(true)
                .roles(List.of("USER"))
                .build();

        sampleTokenResponse = new KeycloakTokenResponse();
        sampleTokenResponse.setAccessToken("admin-token-xyz");
        sampleTokenResponse.setExpiresIn(300L);

        selfJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .subject("uuid-keycloak-1")
                .claim("preferred_username", "testuser")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        otherJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .subject("uuid-other-999")
                .claim("preferred_username", "otheradmin")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        lenient().when(keycloakProperties.getClientId()).thenReturn("teu-client");
        lenient().when(keycloakProperties.getRealm()).thenReturn("teu-realm");
        lenient().when(keycloakProperties.getClientSecret()).thenReturn("secret");
        lenient().when(keycloakProperties.getPrincipalAttribute()).thenReturn("preferred_username");
    }

    @Test
    @DisplayName("Lấy danh sách người dùng phân trang thành công kèm roles từ Keycloak")
    void testGetUsersSuccess() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(sampleUser), pageable, 1L);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(authFeignClient.getToken(anyString(), anyMap())).thenReturn(sampleTokenResponse);
        when(userMapper.toAdminDto(sampleUser)).thenReturn(sampleAdminDto);

        KeycloakRoleRepresentation roleAdmin = KeycloakRoleRepresentation.builder().name("ADMIN").build();
        when(adminFeignClient.getUserRealmRoles(anyString(), anyString(), eq("uuid-keycloak-1")))
                .thenReturn(List.of(roleAdmin));

        PageResponseDto<UserAdminResponseDto> result = adminUserService.getUsers("test", true, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("testuser", result.getContent().get(0).getUsername());
        assertTrue(result.getContent().get(0).getRoles().contains("ADMIN"));
    }

    @Test
    @DisplayName("Lấy danh sách người dùng khi không kết nối được Keycloak fallback sang vai trò USER")
    void testGetUsersKeycloakErrorFallback() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(sampleUser), pageable, 1L);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(authFeignClient.getToken(anyString(), anyMap())).thenThrow(new RuntimeException("Keycloak unavailable"));
        when(userMapper.toAdminDto(sampleUser)).thenReturn(sampleAdminDto);

        PageResponseDto<UserAdminResponseDto> result = adminUserService.getUsers(null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(List.of("USER"), result.getContent().get(0).getRoles());
    }

    @Test
    @DisplayName("Lấy chi tiết người dùng thành công kèm sổ địa chỉ và vai trò")
    void testGetUserDetailSuccess() {
        UserAddress address = UserAddress.builder().id(10L).streetAddress("123 Phố Huế").isDefault(true).build();
        AddressResponseDto addressDto = AddressResponseDto.builder().id(10L).streetAddress("123 Phố Huế").build();

        UserAdminDetailResponseDto detailDto = UserAdminDetailResponseDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@teumobile.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userMapper.toAdminDetailDto(sampleUser)).thenReturn(detailDto);
        when(userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L)).thenReturn(List.of(address));
        when(userAddressMapper.toDtoList(List.of(address))).thenReturn(List.of(addressDto));
        when(authFeignClient.getToken(anyString(), anyMap())).thenReturn(sampleTokenResponse);

        UserAdminDetailResponseDto result = adminUserService.getUserDetail(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(1, result.getAddresses().size());
    }

    @Test
    @DisplayName("Lấy chi tiết người dùng ném ResourceNotFoundException khi ID không tồn tại")
    void testGetUserDetailNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminUserService.getUserDetail(999L));
    }

    @Test
    @DisplayName("Khóa/Mở khóa người dùng thành công và đồng bộ sang Keycloak khi thao tác với user khác")
    void testUpdateUserStatusSuccess() {
        UserStatusUpdateRequestDto request = new UserStatusUpdateRequestDto(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(authFeignClient.getToken(anyString(), anyMap())).thenReturn(sampleTokenResponse);
        when(adminFeignClient.updateUser(anyString(), anyString(), anyString(), any(KeycloakUserRepresentation.class)))
                .thenReturn(ResponseEntity.noContent().build());

        UserAdminResponseDto updatedDto = UserAdminResponseDto.builder()
                .id(1L)
                .username("testuser")
                .isActive(false)
                .build();
        when(userMapper.toAdminDto(sampleUser)).thenReturn(updatedDto);

        UserAdminResponseDto result = adminUserService.updateUserStatus(1L, request, otherJwt);

        assertNotNull(result);
        assertFalse(sampleUser.getIsActive());
        verify(adminFeignClient, times(1)).updateUser(anyString(), anyString(), eq("uuid-keycloak-1"), any());
    }

    @Test
    @DisplayName("Admin tự khóa tài khoản của chính mình bị chặn ném IllegalArgumentException")
    void testUpdateUserStatusSelfLockThrowsException() {
        UserStatusUpdateRequestDto request = new UserStatusUpdateRequestDto(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> adminUserService.updateUserStatus(1L, request, selfJwt)
        );

        assertEquals("Bạn không thể tự khóa tài khoản của chính mình!", ex.getMessage());
        verify(userRepository, never()).save(any());
        verify(adminFeignClient, never()).updateUser(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Gán vai trò ADMIN cho người dùng trên Keycloak thành công (tìm kiếm không phân biệt hoa/thường)")
    void testUpdateUserRoleAssignSuccess() {
        UserRoleUpdateRequestDto request = new UserRoleUpdateRequestDto("ADMIN", true);
        KeycloakRoleRepresentation roleRep = KeycloakRoleRepresentation.builder().name("admin").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(authFeignClient.getToken(anyString(), anyMap())).thenReturn(sampleTokenResponse);
        when(adminFeignClient.getRealmRoles(anyString(), anyString())).thenReturn(List.of(roleRep));
        when(adminFeignClient.addRealmRolesToUser(anyString(), anyString(), eq("uuid-keycloak-1"), anyList()))
                .thenReturn(ResponseEntity.noContent().build());
        when(adminFeignClient.getUserRealmRoles(anyString(), anyString(), eq("uuid-keycloak-1")))
                .thenReturn(List.of(roleRep));
        when(userMapper.toAdminDto(sampleUser)).thenReturn(sampleAdminDto);

        UserAdminResponseDto result = adminUserService.updateUserRole(1L, request, otherJwt);

        assertNotNull(result);
        verify(adminFeignClient, times(1)).addRealmRolesToUser(anyString(), anyString(), eq("uuid-keycloak-1"), anyList());
    }

    @Test
    @DisplayName("Gỡ vai trò ADMIN khỏi người dùng trên Keycloak thành công khi thao tác với user khác")
    void testUpdateUserRoleRevokeSuccess() {
        UserRoleUpdateRequestDto request = new UserRoleUpdateRequestDto("ADMIN", false);
        KeycloakRoleRepresentation roleRep = KeycloakRoleRepresentation.builder().name("ADMIN").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(authFeignClient.getToken(anyString(), anyMap())).thenReturn(sampleTokenResponse);
        when(adminFeignClient.getRealmRoles(anyString(), anyString())).thenReturn(List.of(roleRep));
        when(adminFeignClient.removeRealmRolesFromUser(anyString(), anyString(), eq("uuid-keycloak-1"), anyList()))
                .thenReturn(ResponseEntity.noContent().build());
        when(userMapper.toAdminDto(sampleUser)).thenReturn(sampleAdminDto);

        UserAdminResponseDto result = adminUserService.updateUserRole(1L, request, otherJwt);

        assertNotNull(result);
        verify(adminFeignClient, times(1)).removeRealmRolesFromUser(anyString(), anyString(), eq("uuid-keycloak-1"), anyList());
    }

    @Test
    @DisplayName("Admin tự gỡ vai trò ADMIN của chính mình bị chặn ném IllegalArgumentException")
    void testUpdateUserRoleSelfDemoteThrowsException() {
        UserRoleUpdateRequestDto request = new UserRoleUpdateRequestDto("ADMIN", false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> adminUserService.updateUserRole(1L, request, selfJwt)
        );

        assertEquals("Bạn không thể tự hạ cấp hoặc gỡ quyền Quản trị viên (ADMIN) của chính mình!", ex.getMessage());
        verify(adminFeignClient, never()).removeRealmRolesFromUser(anyString(), anyString(), anyString(), anyList());
    }

    @Test
    @DisplayName("Gán vai trò Client Role khi không có trong Realm Role thành công")
    void testUpdateUserRoleClientRoleSuccess() {
        UserRoleUpdateRequestDto request = new UserRoleUpdateRequestDto("ADMIN", true);
        KeycloakRoleRepresentation clientRoleRep = KeycloakRoleRepresentation.builder().name("ADMIN").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(authFeignClient.getToken(anyString(), anyMap())).thenReturn(sampleTokenResponse);
        when(adminFeignClient.getRealmRoles(anyString(), anyString())).thenReturn(List.of()); // Không có trong Realm Roles
        when(adminFeignClient.getClientsByClientId(anyString(), anyString(), anyString()))
                .thenReturn(List.of(Map.of("id", "client-uuid-123")));
        when(adminFeignClient.getClientRoles(anyString(), anyString(), eq("client-uuid-123")))
                .thenReturn(List.of(clientRoleRep)); // Có trong Client Roles
        when(adminFeignClient.addClientRolesToUser(anyString(), anyString(), eq("uuid-keycloak-1"), eq("client-uuid-123"), anyList()))
                .thenReturn(ResponseEntity.noContent().build());
        when(userMapper.toAdminDto(sampleUser)).thenReturn(sampleAdminDto);

        UserAdminResponseDto result = adminUserService.updateUserRole(1L, request, otherJwt);

        assertNotNull(result);
        verify(adminFeignClient, times(1)).addClientRolesToUser(anyString(), anyString(), eq("uuid-keycloak-1"), eq("client-uuid-123"), anyList());
    }

    @Test
    @DisplayName("Gán vai trò không tồn tại trên cả Realm lẫn Client Roles ném IllegalArgumentException")
    void testUpdateUserRoleNotFoundOnKeycloak() {
        UserRoleUpdateRequestDto request = new UserRoleUpdateRequestDto("SUPER_VIP", true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(authFeignClient.getToken(anyString(), anyMap())).thenReturn(sampleTokenResponse);
        when(adminFeignClient.getRealmRoles(anyString(), anyString())).thenReturn(List.of());
        when(adminFeignClient.getClientsByClientId(anyString(), anyString(), anyString())).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> adminUserService.updateUserRole(1L, request, otherJwt));
    }

    @Test
    @DisplayName("Admin tạo mới người dùng thành công kèm gán vai trò ban đầu")
    void testCreateUserSuccess() {
        UserAdminCreateRequestDto request = UserAdminCreateRequestDto.builder()
                .username("newadmin")
                .email("admin@teumobile.com")
                .password("Admin@123")
                .fullName("Trần Quản Trị")
                .phoneNumber("0912345678")
                .role("ADMIN")
                .build();

        when(userRepository.existsByUsername("newadmin")).thenReturn(false);
        when(userRepository.existsByEmail("admin@teumobile.com")).thenReturn(false);
        when(authFeignClient.getToken(anyString(), anyMap())).thenReturn(sampleTokenResponse);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("http://keycloak/admin/realms/teu/users/new-uuid-999"));
        when(adminFeignClient.createUser(anyString(), anyString(), any(KeycloakUserRepresentation.class)))
                .thenReturn(ResponseEntity.created(URI.create("http://keycloak/users/new-uuid-999")).headers(headers).build());

        KeycloakRoleRepresentation adminRole = KeycloakRoleRepresentation.builder().name("ADMIN").build();
        when(adminFeignClient.getRealmRoles(anyString(), anyString())).thenReturn(List.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(userMapper.toAdminDto(any(User.class))).thenReturn(sampleAdminDto);

        UserAdminResponseDto result = adminUserService.createUser(request);

        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
        verify(adminFeignClient, times(1)).addRealmRolesToUser(anyString(), anyString(), eq("new-uuid-999"), anyList());
    }

    @Test
    @DisplayName("Admin tạo người dùng thất bại khi tên đăng nhập đã tồn tại")
    void testCreateUserDuplicateUsername() {
        UserAdminCreateRequestDto request = UserAdminCreateRequestDto.builder()
                .username("existinguser")
                .email("test@teumobile.com")
                .password("123456")
                .build();

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> adminUserService.createUser(request));
    }

    @Test
    @DisplayName("Admin tạo người dùng thất bại khi email đã tồn tại")
    void testCreateUserDuplicateEmail() {
        UserAdminCreateRequestDto request = UserAdminCreateRequestDto.builder()
                .username("newuser")
                .email("duplicate@teumobile.com")
                .password("123456")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("duplicate@teumobile.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> adminUserService.createUser(request));
    }

    @Test
    @DisplayName("Thống kê số lượng người dùng thành công")
    void testGetUserStats() {
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByIsActive(true)).thenReturn(90L);
        when(userRepository.countByIsActive(false)).thenReturn(10L);

        UserStatsResponseDto stats = adminUserService.getUserStats();

        assertNotNull(stats);
        assertEquals(100L, stats.getTotalUsers());
        assertEquals(90L, stats.getActiveUsers());
        assertEquals(10L, stats.getInactiveUsers());
    }
}
