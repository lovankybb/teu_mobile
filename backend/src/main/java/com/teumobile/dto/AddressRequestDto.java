package com.teumobile.dto;

import com.teumobile.entity.AddressType;
import com.teumobile.validation.ValidPhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequestDto {

    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(max = 150, message = "Tên người nhận không được vượt quá 150 ký tự")
    private String recipientName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @ValidPhoneNumber
    private String phoneNumber;

    @NotBlank(message = "Tỉnh / Thành phố không được để trống")
    @Size(max = 100, message = "Tỉnh / Thành phố không được vượt quá 100 ký tự")
    private String province;

    @NotBlank(message = "Quận / Huyện không được để trống")
    @Size(max = 100, message = "Quận / Huyện không được vượt quá 100 ký tự")
    private String district;

    @NotBlank(message = "Phường / Xã không được để trống")
    @Size(max = 100, message = "Phường / Xã không được vượt quá 100 ký tự")
    private String ward;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    @Size(max = 255, message = "Địa chỉ chi tiết không được vượt quá 255 ký tự")
    private String streetAddress;

    private AddressType addressType;

    private Boolean isDefault;

    @Size(max = 255, message = "Ghi chú không được vượt quá 255 ký tự")
    private String notes;
}
