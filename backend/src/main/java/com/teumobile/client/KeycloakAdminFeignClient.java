package com.teumobile.client;

import com.teumobile.dto.keycloak.KeycloakRoleRepresentation;
import com.teumobile.dto.keycloak.KeycloakUserRepresentation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "keycloakAdminFeignClient",
        url = "${app.keycloak.base-url}"
)
public interface KeycloakAdminFeignClient {

    /**
     * Tạo mới người dùng trong Realm qua Keycloak Admin REST API
     */
    @PostMapping(
            value = "/admin/realms/{realm}/users",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Void> createUser(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("realm") String realm,
            @RequestBody KeycloakUserRepresentation user
    );

    /**
     * Lấy thông tin user theo Keycloak UUID
     */
    @GetMapping(
            value = "/admin/realms/{realm}/users/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    KeycloakUserRepresentation getUserById(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("realm") String realm,
            @PathVariable("id") String id
    );

    /**
     * Cập nhật thông tin user trên Keycloak (ví dụ: enabled)
     */
    @PutMapping(
            value = "/admin/realms/{realm}/users/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Void> updateUser(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("realm") String realm,
            @PathVariable("id") String id,
            @RequestBody KeycloakUserRepresentation user
    );

    /**
     * Tìm kiếm người dùng theo username
     */
    @GetMapping(
            value = "/admin/realms/{realm}/users",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<KeycloakUserRepresentation> searchUsersByUsername(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("realm") String realm,
            @RequestParam("username") String username,
            @RequestParam(value = "exact", defaultValue = "true") boolean exact
    );

    /**
     * Tìm kiếm người dùng theo email
     */
    @GetMapping(
            value = "/admin/realms/{realm}/users",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<KeycloakUserRepresentation> searchUsersByEmail(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("realm") String realm,
            @RequestParam("email") String email,
            @RequestParam(value = "exact", defaultValue = "true") boolean exact
    );

    /**
     * Lấy danh sách toàn bộ Realm Roles
     */
    @GetMapping(
            value = "/admin/realms/{realm}/roles",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<KeycloakRoleRepresentation> getRealmRoles(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("realm") String realm
    );

    /**
     * Lấy danh sách Realm Roles đã gán cho user
     */
    @GetMapping(
            value = "/admin/realms/{realm}/users/{id}/role-mappings/realm",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<KeycloakRoleRepresentation> getUserRealmRoles(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("realm") String realm,
            @PathVariable("id") String id
    );

    /**
     * Lấy thông tin role theo tên
     */
    @GetMapping(
            value = "/admin/realms/{realm}/roles/{roleName}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    KeycloakRoleRepresentation getRealmRoleByName(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("realm") String realm,
            @PathVariable("roleName") String roleName
    );

    /**
     * Gán realm roles cho user
     */
    @PostMapping(
            value = "/admin/realms/{realm}/users/{id}/role-mappings/realm",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Void> addRealmRolesToUser(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("realm") String realm,
            @PathVariable("id") String id,
            @RequestBody List<KeycloakRoleRepresentation> roles
    );

    /**
     * Gỡ realm roles khỏi user
     */
    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/admin/realms/{realm}/users/{id}/role-mappings/realm",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Void> removeRealmRolesFromUser(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("realm") String realm,
            @PathVariable("id") String id,
            @RequestBody List<KeycloakRoleRepresentation> roles
    );

    /**
     * Lấy Client theo clientId (để lấy client internal UUID)
     */
    @GetMapping(
            value = "/admin/realms/{realm}/clients",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Map<String, Object>> getClientsByClientId(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("realm") String realm,
            @RequestParam("clientId") String clientId
    );

    /**
     * Lấy danh sách Client Roles của một Client
     */
    @GetMapping(
            value = "/admin/realms/{realm}/clients/{clientUuid}/roles",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<KeycloakRoleRepresentation> getClientRoles(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("realm") String realm,
            @PathVariable("clientUuid") String clientUuid
    );

    /**
     * Lấy danh sách Client Roles đã gán cho user
     */
    @GetMapping(
            value = "/admin/realms/{realm}/users/{userId}/role-mappings/clients/{clientUuid}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<KeycloakRoleRepresentation> getUserClientRoles(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("realm") String realm,
            @PathVariable("userId") String userId,
            @PathVariable("clientUuid") String clientUuid
    );

    /**
     * Gán Client Roles cho user
     */
    @PostMapping(
            value = "/admin/realms/{realm}/users/{userId}/role-mappings/clients/{clientUuid}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Void> addClientRolesToUser(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("realm") String realm,
            @PathVariable("userId") String userId,
            @PathVariable("clientUuid") String clientUuid,
            @RequestBody List<KeycloakRoleRepresentation> roles
    );

    /**
     * Gỡ Client Roles khỏi user
     */
    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/admin/realms/{realm}/users/{userId}/role-mappings/clients/{clientUuid}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Void> removeClientRolesFromUser(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("realm") String realm,
            @PathVariable("userId") String userId,
            @PathVariable("clientUuid") String clientUuid,
            @RequestBody List<KeycloakRoleRepresentation> roles
    );
}
