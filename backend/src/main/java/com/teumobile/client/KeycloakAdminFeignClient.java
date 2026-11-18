package com.teumobile.client;

import com.teumobile.dto.keycloak.KeycloakUserRepresentation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
