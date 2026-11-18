package com.teumobile.service.impl;

import com.teumobile.dto.AddressRequestDto;
import com.teumobile.dto.AddressResponseDto;
import com.teumobile.entity.AddressType;
import com.teumobile.entity.User;
import com.teumobile.entity.UserAddress;
import com.teumobile.exception.ResourceNotFoundException;
import com.teumobile.mapper.UserAddressMapper;
import com.teumobile.repository.UserAddressRepository;
import com.teumobile.repository.UserRepository;
import com.teumobile.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAddressServiceImpl implements UserAddressService {

    private static final int MAX_ADDRESSES_PER_USER = 20;

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserAddressMapper userAddressMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponseDto> getUserAddresses(Jwt jwt) {
        User user = getUserByJwt(jwt);
        List<UserAddress> addresses = userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId());
        return userAddressMapper.toDtoList(addresses);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponseDto getAddressById(Jwt jwt, Long id) {
        User user = getUserByJwt(jwt);
        UserAddress address = userAddressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ với ID: " + id));
        return userAddressMapper.toDto(address);
    }

    @Override
    @Transactional
    public AddressResponseDto createAddress(Jwt jwt, AddressRequestDto requestDto) {
        User user = getUserByJwt(jwt);

        long currentCount = userAddressRepository.countByUserId(user.getId());
        if (currentCount >= MAX_ADDRESSES_PER_USER) {
            throw new IllegalArgumentException("Bạn chỉ có thể lưu tối đa " + MAX_ADDRESSES_PER_USER + " địa chỉ nhận hàng");
        }

        boolean isFirst = (currentCount == 0);
        boolean shouldBeDefault = isFirst || Boolean.TRUE.equals(requestDto.getIsDefault());

        if (shouldBeDefault) {
            userAddressRepository.unsetDefaultAddressForUser(user.getId());
        }

        UserAddress address = userAddressMapper.toEntity(requestDto);
        address.setUser(user);
        address.setIsDefault(shouldBeDefault);
        if (address.getAddressType() == null) {
            address.setAddressType(AddressType.HOME);
        }
        address.setFullAddress(buildFullAddress(
                requestDto.getStreetAddress(),
                requestDto.getWard(),
                requestDto.getDistrict(),
                requestDto.getProvince()
        ));

        UserAddress saved = userAddressRepository.save(address);

        if (shouldBeDefault) {
            user.setAddress(saved.getFullAddress());
            userRepository.save(user);
        }

        log.info("Người dùng {} (id: {}) đã tạo mới địa chỉ: id={}, default={}",
                user.getUsername(), user.getId(), saved.getId(), shouldBeDefault);

        return userAddressMapper.toDto(saved);
    }

    @Override
    @Transactional
    public AddressResponseDto updateAddress(Jwt jwt, Long id, AddressRequestDto requestDto) {
        User user = getUserByJwt(jwt);
        UserAddress address = userAddressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ với ID: " + id));

        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        boolean wantDefault = Boolean.TRUE.equals(requestDto.getIsDefault());

        if (wantDefault && !wasDefault) {
            userAddressRepository.unsetDefaultAddressForUser(user.getId());
            address.setIsDefault(true);
        } else if (!wantDefault && wasDefault) {
            // Không cho phép tự bỏ mặc định nếu không chọn địa chỉ khác làm mặc định
            address.setIsDefault(true);
        } else if (requestDto.getIsDefault() != null) {
            address.setIsDefault(requestDto.getIsDefault());
        }

        userAddressMapper.updateEntityFromDto(requestDto, address);

        if (requestDto.getAddressType() != null) {
            address.setAddressType(requestDto.getAddressType());
        }

        address.setFullAddress(buildFullAddress(
                requestDto.getStreetAddress(),
                requestDto.getWard(),
                requestDto.getDistrict(),
                requestDto.getProvince()
        ));

        UserAddress saved = userAddressRepository.save(address);

        if (Boolean.TRUE.equals(saved.getIsDefault())) {
            user.setAddress(saved.getFullAddress());
            userRepository.save(user);
        }

        log.info("Người dùng {} đã cập nhật địa chỉ id: {}", user.getUsername(), saved.getId());
        return userAddressMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteAddress(Jwt jwt, Long id) {
        User user = getUserByJwt(jwt);
        UserAddress address = userAddressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ với ID: " + id));

        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());

        userAddressRepository.delete(address);
        userAddressRepository.flush();

        if (wasDefault) {
            Optional<UserAddress> fallbackOpt = userAddressRepository
                    .findFirstByUserIdAndIdNotOrderByUpdatedAtDesc(user.getId(), id);

            if (fallbackOpt.isPresent()) {
                UserAddress fallback = fallbackOpt.get();
                fallback.setIsDefault(true);
                userAddressRepository.save(fallback);
                user.setAddress(fallback.getFullAddress());
            } else {
                user.setAddress(null);
            }
            userRepository.save(user);
        }

        log.info("Người dùng {} đã xóa địa chỉ id: {}", user.getUsername(), id);
    }

    @Override
    @Transactional
    public AddressResponseDto setDefaultAddress(Jwt jwt, Long id) {
        User user = getUserByJwt(jwt);
        UserAddress address = userAddressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ với ID: " + id));

        userAddressRepository.unsetDefaultAddressForUser(user.getId());
        address.setIsDefault(true);
        UserAddress saved = userAddressRepository.save(address);

        user.setAddress(saved.getFullAddress());
        userRepository.save(user);

        log.info("Người dùng {} đã đặt địa chỉ id: {} làm mặc định", user.getUsername(), id);
        return userAddressMapper.toDto(saved);
    }

    private User getUserByJwt(Jwt jwt) {
        String keycloakId = jwt.getSubject();
        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản người dùng"));
    }

    private String buildFullAddress(String street, String ward, String district, String province) {
        return String.format("%s, %s, %s, %s",
                street != null ? street.trim() : "",
                ward != null ? ward.trim() : "",
                district != null ? district.trim() : "",
                province != null ? province.trim() : "");
    }
}
