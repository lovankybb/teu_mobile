package com.teumobile.service;

import com.teumobile.dto.AddressRequestDto;
import com.teumobile.dto.AddressResponseDto;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public interface UserAddressService {

    List<AddressResponseDto> getUserAddresses(Jwt jwt);

    AddressResponseDto getAddressById(Jwt jwt, Long id);

    AddressResponseDto createAddress(Jwt jwt, AddressRequestDto requestDto);

    AddressResponseDto updateAddress(Jwt jwt, Long id, AddressRequestDto requestDto);

    void deleteAddress(Jwt jwt, Long id);

    AddressResponseDto setDefaultAddress(Jwt jwt, Long id);
}
