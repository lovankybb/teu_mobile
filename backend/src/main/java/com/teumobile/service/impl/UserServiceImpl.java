package com.teumobile.service.impl;

import com.teumobile.dto.UserProfileResponseDto;
import com.teumobile.dto.UserSyncRequestDto;
import com.teumobile.dto.UserUpdateRequestDto;
import com.teumobile.entity.User;
import com.teumobile.exception.ResourceNotFoundException;
import com.teumobile.mapper.UserMapper;
import com.teumobile.repository.UserRepository;
import com.teumobile.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserProfileResponseDto syncUserFromJwt(Jwt jwt, UserSyncRequestDto additionalInfo) {
        String keycloakId = jwt.getSubject();
        String rawUsername = jwt.getClaimAsString("preferred_username");
        String rawEmail = jwt.getClaimAsString("email");
        String fullName = jwt.getClaimAsString("name");

        final String resolvedUsername = (rawUsername != null && !rawUsername.isBlank())
                ? rawUsername
                : ((rawEmail != null && !rawEmail.isBlank()) ? rawEmail.split("@")[0] : keycloakId);

        final String resolvedEmail = (rawEmail != null && !rawEmail.isBlank())
                ? rawEmail
                : (resolvedUsername + "@teumobile.local");

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> User.builder()
                        .keycloakId(keycloakId)
                        .username(resolvedUsername)
                        .email(resolvedEmail)
                        .fullName(fullName)
                        .isActive(true)
                        .build());

        // Cập nhật các thông tin cơ bản từ token
        user.setUsername(resolvedUsername);
        user.setEmail(resolvedEmail);
        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }

        // Bổ sung thông tin nếu client truyền kèm
        if (additionalInfo != null) {
            if (additionalInfo.getPhoneNumber() != null && !additionalInfo.getPhoneNumber().isBlank()) {
                user.setPhoneNumber(additionalInfo.getPhoneNumber());
            }
            if (additionalInfo.getAddress() != null && !additionalInfo.getAddress().isBlank()) {
                user.setAddress(additionalInfo.getAddress());
            }
            if (additionalInfo.getAvatarUrl() != null && !additionalInfo.getAvatarUrl().isBlank()) {
                user.setAvatarUrl(additionalInfo.getAvatarUrl());
            }
        }

        User savedUser = userRepository.save(user);
        log.info("Đã đồng bộ thành công hồ sơ user keycloakId: {}", keycloakId);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserProfileResponseDto getCurrentUserProfile(Jwt jwt) {
        String keycloakId = jwt.getSubject();
        return userRepository.findByKeycloakId(keycloakId)
                .map(userMapper::toDto)
                .orElseGet(() -> syncUserFromJwt(jwt, null));
    }

    @Override
    @Transactional
    public UserProfileResponseDto updateProfile(Jwt jwt, UserUpdateRequestDto updateDto) {
        String keycloakId = jwt.getSubject();
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> {
                    syncUserFromJwt(jwt, null);
                    return userRepository.findByKeycloakId(keycloakId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "keycloakId", keycloakId));
                });

        userMapper.updateEntityFromDto(updateDto, user);
        User updated = userRepository.save(user);
        log.info("Đã cập nhật thông tin cá nhân cho user keycloakId: {}", keycloakId);
        return userMapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}
