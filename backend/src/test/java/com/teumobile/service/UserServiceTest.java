package com.teumobile.service;

import com.teumobile.dto.UserProfileResponseDto;
import com.teumobile.dto.UserSyncRequestDto;
import com.teumobile.dto.UserUpdateRequestDto;
import com.teumobile.entity.User;
import com.teumobile.exception.ResourceNotFoundException;
import com.teumobile.mapper.UserMapper;
import com.teumobile.repository.UserRepository;
import com.teumobile.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private UserServiceImpl userService;

    private Jwt sampleJwt;
    private User sampleUser;
    private UserProfileResponseDto sampleDto;

    @BeforeEach
    void setUp() {
        sampleJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .subject("keycloak-sub-123")
                .claim("preferred_username", "nguyenvana")
                .claim("email", "vana@gmail.com")
                .claim("name", "Nguyễn Văn A")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        sampleUser = User.builder()
                .id(1L)
                .keycloakId("keycloak-sub-123")
                .username("nguyenvana")
                .email("vana@gmail.com")
                .fullName("Nguyễn Văn A")
                .isActive(true)
                .build();

        sampleDto = UserProfileResponseDto.builder()
                .id(1L)
                .keycloakId("keycloak-sub-123")
                .username("nguyenvana")
                .email("vana@gmail.com")
                .fullName("Nguyễn Văn A")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Đồng bộ User mới từ JWT chưa có trong cơ sở dữ liệu")
    void testSyncNewUserFromJwt() {
        when(userRepository.findByKeycloakId("keycloak-sub-123")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(userMapper.toDto(sampleUser)).thenReturn(sampleDto);

        UserSyncRequestDto extra = UserSyncRequestDto.builder()
                .phoneNumber("0912345678")
                .address("Hà Nội")
                .build();

        UserProfileResponseDto result = userService.syncUserFromJwt(sampleJwt, extra);

        assertNotNull(result);
        assertEquals("keycloak-sub-123", result.getKeycloakId());
        assertEquals("nguyenvana", result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Lấy hồ sơ người dùng hiện tại thành công")
    void testGetCurrentUserProfileSuccess() {
        when(userRepository.findByKeycloakId("keycloak-sub-123")).thenReturn(Optional.of(sampleUser));
        when(userMapper.toDto(sampleUser)).thenReturn(sampleDto);

        UserProfileResponseDto result = userService.getCurrentUserProfile(sampleJwt);

        assertNotNull(result);
        assertEquals("nguyenvana", result.getUsername());
    }

    @Test
    @DisplayName("Lấy hồ sơ người dùng hiện tại tự động lưu Google picture từ Access Token vào CSDL")
    void testGetCurrentUserProfileWithGooglePictureInAccessToken() {
        String googlePicture = "https://lh3.googleusercontent.com/a/google_avatar_at.jpg";
        Jwt jwtWithPicture = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .subject("keycloak-sub-123")
                .claim("preferred_username", "nguyenvana")
                .claim("email", "vana@gmail.com")
                .claim("picture", googlePicture)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        sampleUser.setAvatarUrl(null);
        when(userRepository.findByKeycloakId("keycloak-sub-123")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(sampleUser)).thenReturn(sampleUser);
        when(userMapper.toDto(sampleUser)).thenReturn(sampleDto);

        UserProfileResponseDto result = userService.getCurrentUserProfile(jwtWithPicture);

        assertNotNull(result);
        assertEquals(googlePicture, sampleUser.getAvatarUrl());
        verify(userRepository, atLeastOnce()).save(sampleUser);
    }

    @Test
    @DisplayName("Cập nhật thông tin người dùng thành công")
    void testUpdateProfileSuccess() {
        when(userRepository.findByKeycloakId("keycloak-sub-123")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(sampleUser)).thenReturn(sampleUser);
        when(userMapper.toDto(sampleUser)).thenReturn(sampleDto);

        UserUpdateRequestDto updateDto = UserUpdateRequestDto.builder()
                .fullName("Nguyễn Văn A (Cập nhật)")
                .phoneNumber("0988888888")
                .address("TP Hồ Chí Minh")
                .build();

        UserProfileResponseDto result = userService.updateProfile(sampleJwt, updateDto);

        assertNotNull(result);
        verify(userMapper, times(1)).updateEntityFromDto(updateDto, sampleUser);
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    @DisplayName("Cập nhật URL ảnh đại diện thành công và xóa avatar cũ trên Cloudinary")
    void testUpdateAvatarSuccess() {
        String oldAvatar = "https://res.cloudinary.com/teu-cloud/image/upload/v111/teu-mobile/avatars/old_pic.jpg";
        sampleUser.setAvatarUrl(oldAvatar);

        when(userRepository.findByKeycloakId("keycloak-sub-123")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(sampleUser)).thenReturn(sampleUser);
        when(userMapper.toDto(sampleUser)).thenReturn(sampleDto);
        when(cloudinaryService.extractPublicId(oldAvatar)).thenReturn("teu-mobile/avatars/old_pic");

        String newAvatarUrl = "https://res.cloudinary.com/teu-cloud/image/upload/v12345/teu-mobile/avatars/new_pic.jpg";
        UserProfileResponseDto result = userService.updateAvatar(sampleJwt, newAvatarUrl);

        assertNotNull(result);
        assertEquals(newAvatarUrl, sampleUser.getAvatarUrl());
        verify(userRepository, times(1)).save(sampleUser);
        verify(cloudinaryService, times(1)).deleteImage("teu-mobile/avatars/old_pic");
    }

    @Test
    @DisplayName("Lưu trực tiếp URL ảnh đại diện từ Google token vào CSDL khi đăng nhập mà không qua Cloudinary")
    void testSyncUserFromJwtWithGooglePicture() {
        String googlePicture = "https://lh3.googleusercontent.com/a/google_avatar.jpg";
        Jwt googleJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .subject("google-sub-456")
                .claim("preferred_username", "googleuser")
                .claim("email", "googleuser@gmail.com")
                .claim("name", "Google User")
                .claim("picture", googlePicture)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(userRepository.findByKeycloakId("google-sub-456")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toDto(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            return UserProfileResponseDto.builder()
                    .username(saved.getUsername())
                    .avatarUrl(saved.getAvatarUrl())
                    .build();
        });

        UserProfileResponseDto result = userService.syncUserFromJwt(googleJwt, null);

        assertNotNull(result);
        assertEquals(googlePicture, result.getAvatarUrl());
        verify(cloudinaryService, never()).uploadFromUrl(anyString());
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    @Test
    @DisplayName("Lấy User theo ID không tồn tại ném ra ResourceNotFoundException")
    void testGetUserByIdNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    }
}
