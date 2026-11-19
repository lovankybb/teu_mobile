package com.teumobile.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusUpdateRequestDto {

    @NotNull(message = "Trạng thái hoạt động (isActive) không được để trống")
    private Boolean isActive;
}
