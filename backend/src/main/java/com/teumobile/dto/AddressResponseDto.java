package com.teumobile.dto;

import com.teumobile.entity.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponseDto {

    private Long id;
    private Long userId;
    private String recipientName;
    private String phoneNumber;
    private String province;
    private String district;
    private String ward;
    private String streetAddress;
    private String fullAddress;
    private AddressType addressType;
    private Boolean isDefault;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
