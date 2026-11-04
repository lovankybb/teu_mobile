package com.teumobile.dto.keycloak;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeycloakCredentialRepresentation {

    @Builder.Default
    private String type = "password";

    private String value;

    @Builder.Default
    private Boolean temporary = false;
}
