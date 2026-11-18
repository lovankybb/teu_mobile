package com.teumobile.controller;

import com.teumobile.dto.AddressRequestDto;
import com.teumobile.dto.AddressResponseDto;
import com.teumobile.dto.ApiResponse;
import com.teumobile.service.UserAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    /**
     * Lấy danh sách toàn bộ sổ địa chỉ của người dùng đang đăng nhập
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponseDto>>> getUserAddresses(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<AddressResponseDto> addresses = userAddressService.getUserAddresses(jwt);
        return ResponseEntity.ok(ApiResponse.success(addresses, "Lấy danh sách địa chỉ thành công"));
    }

    /**
     * Lấy thông tin chi tiết một địa chỉ theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponseDto>> getAddressById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        AddressResponseDto address = userAddressService.getAddressById(jwt, id);
        return ResponseEntity.ok(ApiResponse.success(address, "Lấy thông tin địa chỉ thành công"));
    }

    /**
     * Thêm mới địa chỉ nhận hàng vào sổ địa chỉ
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponseDto>> createAddress(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddressRequestDto requestDto
    ) {
        AddressResponseDto createdAddress = userAddressService.createAddress(jwt, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdAddress, "Thêm địa chỉ mới thành công"));
    }

    /**
     * Cập nhật thông tin địa chỉ nhận hàng
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponseDto>> updateAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequestDto requestDto
    ) {
        AddressResponseDto updatedAddress = userAddressService.updateAddress(jwt, id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(updatedAddress, "Cập nhật địa chỉ thành công"));
    }

    /**
     * Xóa một địa chỉ nhận hàng khỏi sổ địa chỉ
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        userAddressService.deleteAddress(jwt, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa địa chỉ thành công"));
    }

    /**
     * Thiết lập một địa chỉ làm địa chỉ mặc định
     */
    @PatchMapping("/{id}/default")
    public ResponseEntity<ApiResponse<AddressResponseDto>> setDefaultAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        AddressResponseDto defaultAddress = userAddressService.setDefaultAddress(jwt, id);
        return ResponseEntity.ok(ApiResponse.success(defaultAddress, "Đặt làm địa chỉ mặc định thành công"));
    }
}
