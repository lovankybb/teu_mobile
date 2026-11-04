package com.teumobile.security;

import com.teumobile.config.KeycloakProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Bộ chuyển đổi JWT của Keycloak thành Spring Security Authentication Token.
 * Trích xuất roles từ claim `realm_access.roles` và `resource_access.{clientId}.roles`,
 * chuẩn hóa thành tiền tố `ROLE_...` (ví dụ: ROLE_CUSTOMER, ROLE_ADMIN).
 */
@Component
@RequiredArgsConstructor
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final KeycloakProperties keycloakProperties;

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Set<GrantedAuthority> authorities = Stream.concat(
                extractRealmRoles(jwt).stream(),
                extractResourceRoles(jwt).stream()
        ).collect(Collectors.toSet());

        String principalClaimName = keycloakProperties.getPrincipalAttribute();
        String principalName = jwt.getClaimAsString(principalClaimName);
        if (principalName == null || principalName.isBlank()) {
            principalName = jwt.getSubject();
        }

        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return Collections.emptyList();
        }

        Object rolesObj = realmAccess.get("roles");
        if (!(rolesObj instanceof List<?> rawList)) {
            return Collections.emptyList();
        }

        return rawList.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess == null) {
            return Collections.emptyList();
        }

        String clientId = keycloakProperties.getClientId();
        Map<String, Object> clientResource = (Map<String, Object>) resourceAccess.get(clientId);
        if (clientResource == null || !clientResource.containsKey("roles")) {
            return Collections.emptyList();
        }

        Object rolesObj = clientResource.get("roles");
        if (!(rolesObj instanceof List<?> rawList)) {
            return Collections.emptyList();
        }

        return rawList.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
    }
}
