package com.teumobile.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleUpdateRequestDto {

    @NotBlank(message = "Tên vai trò (role) không được để trống")
    private String role; // Ví dụ: ADMIN hoặc CUSTOMER

    @NotNull(message = "Hành động (assign: true để gán, false để gỡ) không được để trống")
    private Boolean assign;
}
