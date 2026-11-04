package com.teumobile.dto;

import com.teumobile.validation.ValidPhoneNumber;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSyncRequestDto {

    @ValidPhoneNumber(required = false)
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phoneNumber;

    @Size(max = 300, message = "Địa chỉ không được vượt quá 300 ký tự")
    private String address;

    @Size(max = 500, message = "Avatar URL không được vượt quá 500 ký tự")
    private String avatarUrl;
}
