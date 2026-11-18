package com.teumobile.dto.keycloak;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakUserRepresentation {

    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    private Boolean emailVerified = true;

    private List<KeycloakCredentialRepresentation> credentials;
    private Map<String, List<String>> attributes;
}
