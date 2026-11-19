package com.teumobile.dto.admin;

import com.teumobile.dto.AddressResponseDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAdminDetailResponseDto {

    private Long id;
    private String keycloakId;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String address;
    private String avatarUrl;
    private Boolean isActive;
    private List<String> roles;
    private List<AddressResponseDto> addresses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
