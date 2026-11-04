package com.teumobile.security;

import com.teumobile.config.KeycloakProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KeycloakJwtAuthenticationConverterTest {

    private KeycloakJwtAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        KeycloakProperties properties = new KeycloakProperties();
        properties.setClientId("teu-mobile-web");
        properties.setPrincipalAttribute("preferred_username");
        converter = new KeycloakJwtAuthenticationConverter(properties);
    }

    @Test
    @DisplayName("Trích xuất Realm Roles và Client Roles chuẩn hóa thành ROLE_...")
    void testConvertJwtWithRoles() {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .subject("keycloak-user-uuid-123")
                .claim("preferred_username", "nguyenvana")
                .claim("email", "vana@gmail.com")
                .claim("realm_access", Map.of("roles", List.of("customer", "admin")))
                .claim("resource_access", Map.of("teu-mobile-web", Map.of("roles", List.of("manager"))))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        AbstractAuthenticationToken auth = converter.convert(jwt);

        assertNotNull(auth);
        assertEquals("nguyenvana", auth.getName());

        List<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertTrue(authorities.contains("ROLE_CUSTOMER"));
        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertTrue(authorities.contains("ROLE_MANAGER"));
    }

    @Test
    @DisplayName("Fallback về subject khi không có preferred_username")
    void testFallbackToSubjectWhenUsernameMissing() {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .subject("keycloak-user-uuid-999")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        AbstractAuthenticationToken auth = converter.convert(jwt);

        assertNotNull(auth);
        assertEquals("keycloak-user-uuid-999", auth.getName());
        assertTrue(auth.getAuthorities().isEmpty());
    }
}
