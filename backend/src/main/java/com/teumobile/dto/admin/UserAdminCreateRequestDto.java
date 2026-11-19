package com.teumobile.dto.admin;

import com.teumobile.validation.ValidEmail;
import com.teumobile.validation.ValidPhoneNumber;
import com.teumobile.validation.ValidUsername;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAdminCreateRequestDto {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @ValidUsername
    private String username;

    @NotBlank(message = "Email không được để trống")
    @ValidEmail
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự trở lên")
    private String password;

    private String fullName;

    @ValidPhoneNumber
    private String phoneNumber;

    private String address;

    @Builder.Default
    private String role = "USER"; // ADMIN hoặc USER
}
