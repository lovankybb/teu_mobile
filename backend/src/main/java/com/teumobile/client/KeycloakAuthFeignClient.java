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
     * Gọi Keycloak Token Endpoint để cấp phát token (client_credentials grant)
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
}
