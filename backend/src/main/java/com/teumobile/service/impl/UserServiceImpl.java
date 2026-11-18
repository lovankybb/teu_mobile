package com.teumobile.service.impl;

import com.teumobile.dto.UserProfileResponseDto;
import com.teumobile.dto.UserSyncRequestDto;
import com.teumobile.dto.UserUpdateRequestDto;
import com.teumobile.entity.User;
import com.teumobile.exception.ResourceNotFoundException;
import com.teumobile.mapper.UserMapper;
import com.teumobile.repository.UserRepository;
import com.teumobile.service.CloudinaryService;
import com.teumobile.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;

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

        // Tự động tải và đồng bộ ảnh Google của người dùng lên Cloudinary (nếu chưa có ảnh tùy biến)
        syncGoogleAvatarIfEligible(user, jwt);

        // Bổ sung thông tin nếu client truyền kèm
        if (additionalInfo != null) {
            if (additionalInfo.getPhoneNumber() != null && !additionalInfo.getPhoneNumber().isBlank()) {
                user.setPhoneNumber(additionalInfo.getPhoneNumber());
            }
            if (additionalInfo.getAddress() != null && !additionalInfo.getAddress().isBlank()) {
                user.setAddress(additionalInfo.getAddress());
            }
            if (additionalInfo.getAvatarUrl() != null && !additionalInfo.getAvatarUrl().isBlank()) {
                String oldAvatar = user.getAvatarUrl();
                user.setAvatarUrl(additionalInfo.getAvatarUrl());
                try {
                    deleteOldAvatarIfPresent(oldAvatar, additionalInfo.getAvatarUrl());
                } catch (Exception e) {
                    log.warn("Không thể xóa ảnh cũ trên Cloudinary khi đồng bộ user: {}", e.getMessage());
                }
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
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> {
                    syncUserFromJwt(jwt, null);
                    return userRepository.findByKeycloakId(keycloakId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "keycloakId", keycloakId));
                });

        // Nếu user đã có trong DB nhưng chưa có ảnh tùy biến trên Cloudinary mà Token Google có ảnh -> đồng bộ ngay
        String oldAvatar = user.getAvatarUrl();
        syncGoogleAvatarIfEligible(user, jwt);
        if (!Objects.equals(oldAvatar, user.getAvatarUrl())) {
            user = userRepository.save(user);
        }

        return userMapper.toDto(user);
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

        String oldAvatarUrl = user.getAvatarUrl();
        userMapper.updateEntityFromDto(updateDto, user);
        User updated = userRepository.save(user);

        // Xóa ảnh cũ trên Cloudinary nếu người dùng cập nhật ảnh mới
        if (updateDto.getAvatarUrl() != null && !updateDto.getAvatarUrl().isBlank()) {
            try {
                deleteOldAvatarIfPresent(oldAvatarUrl, user.getAvatarUrl());
            } catch (Exception e) {
                log.warn("Không thể xóa ảnh cũ trên Cloudinary khi cập nhật profile: {}", e.getMessage());
            }
        }

        log.info("Đã cập nhật thông tin cá nhân cho user keycloakId: {}", keycloakId);
        return userMapper.toDto(updated);
    }

    @Override
    @Transactional
    public UserProfileResponseDto updateAvatar(Jwt jwt, String avatarUrl) {
        String keycloakId = jwt.getSubject();
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> {
                    syncUserFromJwt(jwt, null);
                    return userRepository.findByKeycloakId(keycloakId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "keycloakId", keycloakId));
                });

        String oldAvatarUrl = user.getAvatarUrl();
        user.setAvatarUrl(avatarUrl);
        User updated = userRepository.save(user);

        // Xóa ảnh avatar cũ trên Cloudinary để tiết kiệm dung lượng lưu trữ
        try {
            deleteOldAvatarIfPresent(oldAvatarUrl, avatarUrl);
        } catch (Exception e) {
            log.warn("Không thể xóa avatar cũ trên Cloudinary (url: {}): {}", oldAvatarUrl, e.getMessage());
        }

        log.info("Đã cập nhật avatar cho user keycloakId: {}", keycloakId);
        return userMapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /**
     * Tự động đồng bộ ảnh đại diện từ Google (claim 'picture' hoặc 'avatar_url') vào hồ sơ người dùng.
     * Lưu trực tiếp link ảnh của Google vào DB, không tốn dung lượng Cloudinary và không gây trễ mạng.
     * Nếu người dùng đã tự upload ảnh tùy biến trên Cloudinary thì bảo lưu ảnh đó, không ghi đè link Google.
     */
    private void syncGoogleAvatarIfEligible(User user, Jwt jwt) {
        if (user == null || jwt == null) {
            return;
        }

        String remotePictureUrl = extractPictureFromJwt(jwt);
        if (remotePictureUrl == null || remotePictureUrl.isBlank()) {
            return;
        }

        // Nếu người dùng đã tự upload avatar tùy biến trên Cloudinary -> không ghi đè link Google
        if (user.getAvatarUrl() != null && user.getAvatarUrl().contains("res.cloudinary.com")) {
            return;
        }

        // Nếu chưa có avatar hoặc URL Google trong token thay đổi -> cập nhật trực tiếp link mới
        if (user.getAvatarUrl() == null || !user.getAvatarUrl().equals(remotePictureUrl)) {
            user.setAvatarUrl(remotePictureUrl);
            log.info("Đã lưu trực tiếp đường dẫn avatar Google cho user: {}", user.getUsername());
        }
    }

    /**
     * Xóa ảnh cũ trên Cloudinary bằng public_id khi người dùng đổi ảnh mới.
     */
    private void deleteOldAvatarIfPresent(String oldAvatarUrl, String newAvatarUrl) {
        if (oldAvatarUrl == null || oldAvatarUrl.isBlank() || oldAvatarUrl.equals(newAvatarUrl)) {
            return;
        }

        try {
            String oldPublicId = cloudinaryService.extractPublicId(oldAvatarUrl);
            if (oldPublicId != null && !oldPublicId.isBlank()) {
                log.info("Bắt đầu xóa avatar cũ trên Cloudinary (public_id: {})", oldPublicId);
                cloudinaryService.deleteImage(oldPublicId);
            }
        } catch (Exception e) {
            log.warn("Lỗi khi xóa avatar cũ trên Cloudinary: {}", e.getMessage());
        }
    }

    /**
     * Trích xuất đường dẫn ảnh đại diện từ các claim tiêu chuẩn OIDC của Keycloak / Google
     */
    private String extractPictureFromJwt(Jwt jwt) {
        if (jwt == null) {
            return null;
        }

        String[] claimKeys = {"picture", "avatar_url", "avatar", "profile_picture", "image"};
        for (String key : claimKeys) {
            String val = jwt.getClaimAsString(key);
            if (val != null && !val.isBlank()) {
                return val;
            }
        }

        return null;
    }
}
