package com.teumobile.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app.keycloak")
public class KeycloakProperties {

    /**
     * Keycloak Server Base URL, ví dụ: http://localhost:8180
     */
    private String baseUrl = "http://localhost:8180";

    /**
     * Tên Realm quản lý người dùng, ví dụ: teu-mobile-realm
     */
    private String realm = "teu-mobile-realm";

    /**
     * Client ID đăng ký trong Keycloak, ví dụ: teu-mobile-client
     */
    private String clientId = "teu-mobile-client";

    /**
     * Claim name dùng làm Principal name (mặc định preferred_username hoặc sub)
     */
    private String principalAttribute = "preferred_username";

    /**
     * Client Secret cho confidential client (dùng cho client_credentials kết nối Keycloak)
     */
    private String clientSecret;

    public String getIssuerUri() {
        return baseUrl + "/realms/" + realm;
    }

    public String getJwkSetUri() {
        return baseUrl + "/realms/" + realm + "/protocol/openid-connect/certs";
    }
}
