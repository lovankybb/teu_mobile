package com.teumobile.dto.auth;

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
public class RegisterRequestDto {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @ValidUsername
    private String username;

    @NotBlank(message = "Email không được để trống")
    @ValidEmail
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có tối thiểu 6 ký tự")
    private String password;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 150, message = "Họ và tên không được vượt quá 150 ký tự")
    private String fullName;

    @ValidPhoneNumber(required = false)
    private String phoneNumber;

    @Size(max = 300, message = "Địa chỉ không được vượt quá 300 ký tự")
    private String address;
}
