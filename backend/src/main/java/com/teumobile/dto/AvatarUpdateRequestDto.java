package com.teumobile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvatarUpdateRequestDto {

    @NotBlank(message = "Avatar URL không được để trống")
    @Size(max = 500, message = "Avatar URL không được vượt quá 500 ký tự")
    @Pattern(regexp = "^(https?://).+", message = "Avatar URL phải là một đường dẫn URL hợp lệ")
    private String avatarUrl;
}
