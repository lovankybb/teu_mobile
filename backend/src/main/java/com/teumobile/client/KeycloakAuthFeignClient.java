package com.teumobile.client;

import com.teumobile.config.FeignFormConfiguration;
import com.teumobile.dto.keycloak.KeycloakTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@FeignClient(
        name = "keycloakAuthFeignClient",
        url = "${app.keycloak.base-url}",
        configuration = FeignFormConfiguration.class
)
public interface KeycloakAuthFeignClient {

    /**
     * Gọi Keycloak Token Endpoint để cấp phát token (password grant, refresh_token grant, client_credentials)
     */
    @PostMapping(
            value = "/realms/{realm}/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    KeycloakTokenResponse getToken(
            @PathVariable("realm") String realm,
            Map<String, ?> formParams
    );

    /**
     * Gọi Keycloak Logout Endpoint để thu hồi phiên
     */
    @PostMapping(
            value = "/realms/{realm}/protocol/openid-connect/logout",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    void logout(
            @PathVariable("realm") String realm,
            Map<String, ?> formParams
    );
}
