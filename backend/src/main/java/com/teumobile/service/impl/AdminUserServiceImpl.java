package com.teumobile.service.impl;

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
import com.teumobile.service.AdminUserService;
import com.teumobile.specification.UserSpecification;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserMapper userMapper;
    private final UserAddressMapper userAddressMapper;
    private final KeycloakAuthFeignClient authFeignClient;
    private final KeycloakAdminFeignClient adminFeignClient;
    private final KeycloakProperties keycloakProperties;

    private static class ResolvedRole {
        boolean isClientRole;
        String clientUuid;
        KeycloakRoleRepresentation roleRepresentation;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<UserAdminResponseDto> getUsers(String search, Boolean isActive, Pageable pageable) {
        Specification<User> spec = UserSpecification.filterUsers(search, isActive);
        Page<User> userPage = userRepository.findAll(spec, pageable);

        String adminToken = null;
        try {
            adminToken = getAdminAccessToken();
        } catch (Exception ex) {
            log.warn("Không thể lấy admin token từ Keycloak: {}", ex.getMessage());
        }

        final String token = adminToken;
        Page<UserAdminResponseDto> dtoPage = userPage.map(user -> {
            UserAdminResponseDto dto = userMapper.toAdminDto(user);
            dto.setRoles(token != null ? fetchUserRoles(user.getKeycloakId(), token) : List.of("USER"));
            return dto;
        });

        return PageResponseDto.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public UserAdminDetailResponseDto getUserDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));

        UserAdminDetailResponseDto dto = userMapper.toAdminDetailDto(user);

        // Lấy sổ địa chỉ nhận hàng
        List<UserAddress> addresses = userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(id);
        List<AddressResponseDto> addressDtos = userAddressMapper.toDtoList(addresses);
        dto.setAddresses(addressDtos);

        // Lấy danh sách roles từ Keycloak
        try {
            String adminToken = getAdminAccessToken();
            dto.setRoles(fetchUserRoles(user.getKeycloakId(), adminToken));
        } catch (Exception ex) {
            log.warn("Không thể lấy roles từ Keycloak cho user {}: {}", user.getKeycloakId(), ex.getMessage());
            dto.setRoles(List.of("USER"));
        }

        return dto;
    }

    @Override
    @Transactional
    public UserAdminResponseDto updateUserStatus(Long id, UserStatusUpdateRequestDto request, Jwt jwt) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));

        // Kiểm tra an toàn: Ngăn chặn Admin tự khóa tài khoản của chính mình
        if (Boolean.FALSE.equals(request.getIsActive()) && isCurrentUser(user, jwt)) {
            throw new IllegalArgumentException("Bạn không thể tự khóa tài khoản của chính mình!");
        }

        user.setIsActive(request.getIsActive());
        User savedUser = userRepository.save(user);

        // Đồng bộ sang Keycloak
        String adminToken = null;
        try {
            adminToken = getAdminAccessToken();
            KeycloakUserRepresentation updatePayload = KeycloakUserRepresentation.builder()
                    .enabled(request.getIsActive())
                    .build();
            adminFeignClient.updateUser("Bearer " + adminToken, keycloakProperties.getRealm(), user.getKeycloakId(), updatePayload);
            log.info("Đã đồng bộ trạng thái enabled={} cho user {} trên Keycloak", request.getIsActive(), user.getUsername());
        } catch (Exception ex) {
            log.warn("Lỗi khi đồng bộ trạng thái sang Keycloak cho user {}: {}", user.getKeycloakId(), ex.getMessage());
        }

        UserAdminResponseDto dto = userMapper.toAdminDto(savedUser);
        dto.setRoles(adminToken != null ? fetchUserRoles(user.getKeycloakId(), adminToken) : List.of("USER"));
        return dto;
    }

    @Override
    @Transactional
    public UserAdminResponseDto updateUserRole(Long id, UserRoleUpdateRequestDto request, Jwt jwt) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));

        String targetRole = request.getRole().trim().replace("ROLE_", "");

        // Kiểm tra an toàn: Ngăn chặn Admin tự gỡ vai trò ADMIN của chính mình
        if (targetRole.equalsIgnoreCase("ADMIN") && Boolean.FALSE.equals(request.getAssign()) && isCurrentUser(user, jwt)) {
            throw new IllegalArgumentException("Bạn không thể tự hạ cấp hoặc gỡ quyền Quản trị viên (ADMIN) của chính mình!");
        }

        String adminToken = getAdminAccessToken();

        // Tìm role (hỗ trợ cả Realm Role và Client Role, không phân biệt chữ hoa/thường)
        ResolvedRole resolvedRole = resolveRole(adminToken, targetRole);

        try {
            if (resolvedRole.isClientRole) {
                // Gán / gỡ Client Role
                if (Boolean.TRUE.equals(request.getAssign())) {
                    adminFeignClient.addClientRolesToUser(
                            "Bearer " + adminToken,
                            keycloakProperties.getRealm(),
                            user.getKeycloakId(),
                            resolvedRole.clientUuid,
                            List.of(resolvedRole.roleRepresentation)
                    );
                    log.info("Đã gán Client Role {} cho user {}", resolvedRole.roleRepresentation.getName(), user.getUsername());
                } else {
                    adminFeignClient.removeClientRolesFromUser(
                            "Bearer " + adminToken,
                            keycloakProperties.getRealm(),
                            user.getKeycloakId(),
                            resolvedRole.clientUuid,
                            List.of(resolvedRole.roleRepresentation)
                    );
                    log.info("Đã gỡ Client Role {} khỏi user {}", resolvedRole.roleRepresentation.getName(), user.getUsername());
                }
            } else {
                // Gán / gỡ Realm Role
                if (Boolean.TRUE.equals(request.getAssign())) {
                    adminFeignClient.addRealmRolesToUser(
                            "Bearer " + adminToken,
                            keycloakProperties.getRealm(),
                            user.getKeycloakId(),
                            List.of(resolvedRole.roleRepresentation)
                    );
                    log.info("Đã gán Realm Role {} cho user {}", resolvedRole.roleRepresentation.getName(), user.getUsername());
                } else {
                    adminFeignClient.removeRealmRolesFromUser(
                            "Bearer " + adminToken,
                            keycloakProperties.getRealm(),
                            user.getKeycloakId(),
                            List.of(resolvedRole.roleRepresentation)
                    );
                    log.info("Đã gỡ Realm Role {} khỏi user {}", resolvedRole.roleRepresentation.getName(), user.getUsername());
                }
            }
        } catch (FeignException.Forbidden fe) {
            log.error("Keycloak từ chối quyền (403) khi cập nhật vai trò: {}", fe.getMessage());
            throw new IllegalArgumentException(
                    "Keycloak từ chối thao tác (403 Forbidden). Service Account của client '"
                    + keycloakProperties.getClientId() + "' cần được gán quyền 'realm-management' (manage-users, view-realm)."
            );
        } catch (Exception ex) {
            log.error("Lỗi khi cập nhật vai trò trên Keycloak: {}", ex.getMessage());
            throw new RuntimeException("Không thể cập nhật vai trò trên Keycloak: " + ex.getMessage(), ex);
        }

        UserAdminResponseDto dto = userMapper.toAdminDto(user);
        dto.setRoles(fetchUserRoles(user.getKeycloakId(), adminToken));
        return dto;
    }

    @Override
    @Transactional
    public UserAdminResponseDto createUser(UserAdminCreateRequestDto request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập '" + request.getUsername() + "' đã tồn tại");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' đã được sử dụng");
        }

        String adminToken = getAdminAccessToken();
        String[] nameParts = splitFullName(request.getFullName());

        KeycloakUserRepresentation keycloakUser = KeycloakUserRepresentation.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(nameParts[0])
                .lastName(nameParts[1])
                .enabled(true)
                .credentials(List.of(
                        KeycloakCredentialRepresentation.builder()
                                .type("password")
                                .value(request.getPassword())
                                .temporary(false)
                                .build()
                ))
                .build();

        String keycloakUserId;
        try {
            ResponseEntity<Void> response = adminFeignClient.createUser(
                    "Bearer " + adminToken,
                    keycloakProperties.getRealm(),
                    keycloakUser
            );

            URI location = response.getHeaders().getLocation();
            if (location != null) {
                String path = location.getPath();
                keycloakUserId = path.substring(path.lastIndexOf('/') + 1);
            } else {
                List<KeycloakUserRepresentation> users = adminFeignClient.searchUsersByUsername(
                        "Bearer " + adminToken,
                        keycloakProperties.getRealm(),
                        request.getUsername(),
                        true
                );
                keycloakUserId = users.isEmpty() ? UUID.randomUUID().toString() : users.get(0).getId();
            }
        } catch (FeignException.Conflict ex) {
            throw new IllegalArgumentException("Tài khoản đã tồn tại trên hệ thống xác thực");
        } catch (Exception ex) {
            log.error("Lỗi khi tạo user trên Keycloak: {}", ex.getMessage());
            throw new RuntimeException("Không thể tạo tài khoản trên Keycloak: " + ex.getMessage(), ex);
        }

        // Gán vai trò ban đầu nếu có chỉ định (USER hoặc ADMIN)
        if (request.getRole() != null && !request.getRole().isBlank()) {
            try {
                ResolvedRole resolved = resolveRole(adminToken, request.getRole());
                if (resolved.isClientRole) {
                    adminFeignClient.addClientRolesToUser(
                            "Bearer " + adminToken,
                            keycloakProperties.getRealm(),
                            keycloakUserId,
                            resolved.clientUuid,
                            List.of(resolved.roleRepresentation)
                    );
                } else {
                    adminFeignClient.addRealmRolesToUser(
                            "Bearer " + adminToken,
                            keycloakProperties.getRealm(),
                            keycloakUserId,
                            List.of(resolved.roleRepresentation)
                    );
                }
            } catch (Exception ex) {
                log.warn("Không thể gán vai trò ban đầu {} cho user {}: {}", request.getRole(), request.getUsername(), ex.getMessage());
            }
        }

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
        log.info("Admin tạo mới thành công người dùng: username={}, keycloakId={}", savedUser.getUsername(), keycloakUserId);

        UserAdminResponseDto dto = userMapper.toAdminDto(savedUser);
        dto.setRoles(fetchUserRoles(savedUser.getKeycloakId(), adminToken));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatsResponseDto getUserStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActive(true);
        long inactiveUsers = userRepository.countByIsActive(false);

        return UserStatsResponseDto.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .build();
    }

    /**
     * Tìm kiếm vai trò linh hoạt:
     * 1. Tìm trong Realm Roles (so khớp không phân biệt hoa/thường: ADMIN, admin...)
     * 2. Nếu không thấy, tìm tiếp trong Client Roles của client hiện tại (teu-mobile-client)
     * 3. Nếu không có, ném thông báo chi tiết danh sách vai trò hiện có trong Keycloak
     */
    private ResolvedRole resolveRole(String adminToken, String targetRoleName) {
        String authHeader = "Bearer " + adminToken;
        String realm = keycloakProperties.getRealm();

        List<String> realmRoleNames = new ArrayList<>();
        // 1. Kiểm tra Realm Roles
        try {
            List<KeycloakRoleRepresentation> realmRoles = adminFeignClient.getRealmRoles(authHeader, realm);
            if (realmRoles != null) {
                for (KeycloakRoleRepresentation r : realmRoles) {
                    if (r.getName() != null) {
                        realmRoleNames.add(r.getName());
                        if (r.getName().equalsIgnoreCase(targetRoleName)) {
                            ResolvedRole resolved = new ResolvedRole();
                            resolved.isClientRole = false;
                            resolved.roleRepresentation = r;
                            return resolved;
                        }
                    }
                }
            }
        } catch (FeignException.Forbidden fe) {
            log.error("Keycloak 403 Forbidden khi lấy Realm Roles: {}", fe.getMessage());
            throw new IllegalArgumentException(
                    "Keycloak từ chối quyền truy cập (403 Forbidden). Vui lòng đảm bảo Service Account của client '"
                    + keycloakProperties.getClientId() + "' có quyền 'realm-management' (manage-users, view-realm)."
            );
        } catch (Exception ex) {
            log.warn("Không thể lấy danh sách Realm Roles: {}", ex.getMessage());
        }

        // 2. Kiểm tra Client Roles dưới client hiện tại
        List<String> clientRoleNames = new ArrayList<>();
        try {
            List<Map<String, Object>> clients = adminFeignClient.getClientsByClientId(authHeader, realm, keycloakProperties.getClientId());
            if (clients != null && !clients.isEmpty()) {
                String clientUuid = (String) clients.get(0).get("id");
                List<KeycloakRoleRepresentation> clientRoles = adminFeignClient.getClientRoles(authHeader, realm, clientUuid);
                if (clientRoles != null) {
                    for (KeycloakRoleRepresentation cr : clientRoles) {
                        if (cr.getName() != null) {
                            clientRoleNames.add(cr.getName());
                            if (cr.getName().equalsIgnoreCase(targetRoleName)) {
                                ResolvedRole resolved = new ResolvedRole();
                                resolved.isClientRole = true;
                                resolved.clientUuid = clientUuid;
                                resolved.roleRepresentation = cr;
                                return resolved;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("Không thể lấy danh sách Client Roles: {}", ex.getMessage());
        }

        // 3. Không tìm thấy cả Realm lẫn Client Roles -> Báo lỗi chi tiết
        throw new IllegalArgumentException(
                "Vai trò '" + targetRoleName + "' không tồn tại trên Keycloak. "
                + "Các Realm Roles hiện có: " + realmRoleNames
                + (clientRoleNames.isEmpty() ? "" : ". Các Client Roles hiện có: " + clientRoleNames)
        );
    }

    private String getAdminAccessToken() {
        Map<String, String> formParams = new HashMap<>();
        formParams.put("grant_type", "client_credentials");
        formParams.put("client_id", keycloakProperties.getClientId());
        formParams.put("scope", "openid");
        if (keycloakProperties.getClientSecret() != null && !keycloakProperties.getClientSecret().isBlank()) {
            formParams.put("client_secret", keycloakProperties.getClientSecret());
        }

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

    private List<String> fetchUserRoles(String keycloakId, String adminToken) {
        if (keycloakId == null || keycloakId.isBlank()) {
            return List.of("USER");
        }

        List<String> rawRoles = new ArrayList<>();
        String authHeader = "Bearer " + adminToken;
        String realm = keycloakProperties.getRealm();

        // 1. Lấy Realm Roles
        try {
            List<KeycloakRoleRepresentation> realmRoles = adminFeignClient.getUserRealmRoles(
                    authHeader,
                    realm,
                    keycloakId
            );
            if (realmRoles != null) {
                realmRoles.stream()
                        .map(KeycloakRoleRepresentation::getName)
                        .filter(name -> !name.startsWith("default-roles") && !name.equals("offline_access") && !name.equals("uma_authorization"))
                        .forEach(rawRoles::add);
            }
        } catch (Exception ex) {
            log.debug("Không thể lấy Realm Roles cho user {}: {}", keycloakId, ex.getMessage());
        }

        // 2. Lấy Client Roles
        try {
            List<Map<String, Object>> clients = adminFeignClient.getClientsByClientId(authHeader, realm, keycloakProperties.getClientId());
            if (clients != null && !clients.isEmpty()) {
                String clientUuid = (String) clients.get(0).get("id");
                List<KeycloakRoleRepresentation> clientRoles = adminFeignClient.getUserClientRoles(
                        authHeader,
                        realm,
                        keycloakId,
                        clientUuid
                );
                if (clientRoles != null) {
                    clientRoles.stream()
                            .map(KeycloakRoleRepresentation::getName)
                            .forEach(rawRoles::add);
                }
            }
        } catch (Exception ex) {
            log.debug("Không thể lấy Client Roles cho user {}: {}", keycloakId, ex.getMessage());
        }

        List<String> normalized = rawRoles.stream()
                .map(String::toUpperCase)
                .distinct()
                .toList();

        return normalized.isEmpty() ? List.of("USER") : normalized;
    }

    private boolean isCurrentUser(User user, Jwt jwt) {
        if (jwt == null || user == null) {
            return false;
        }
        String currentKeycloakId = jwt.getSubject();
        if (currentKeycloakId != null && currentKeycloakId.equals(user.getKeycloakId())) {
            return true;
        }
        String currentUsername = jwt.getClaimAsString(keycloakProperties.getPrincipalAttribute());
        return currentUsername != null && currentUsername.equalsIgnoreCase(user.getUsername());
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
