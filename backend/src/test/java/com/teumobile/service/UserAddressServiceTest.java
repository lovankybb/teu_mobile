package com.teumobile.service;

import com.teumobile.dto.AddressRequestDto;
import com.teumobile.dto.AddressResponseDto;
import com.teumobile.entity.AddressType;
import com.teumobile.entity.User;
import com.teumobile.entity.UserAddress;
import com.teumobile.exception.ResourceNotFoundException;
import com.teumobile.mapper.UserAddressMapper;
import com.teumobile.repository.UserAddressRepository;
import com.teumobile.repository.UserRepository;
import com.teumobile.service.impl.UserAddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAddressRepository userAddressRepository;

    @Mock
    private UserAddressMapper userAddressMapper;

    @InjectMocks
    private UserAddressServiceImpl userAddressService;

    private Jwt sampleJwt;
    private User sampleUser;
    private UserAddress sampleAddress;
    private AddressResponseDto sampleResponseDto;
    private AddressRequestDto sampleRequestDto;

    @BeforeEach
    void setUp() {
        sampleJwt = Jwt.withTokenValue("mock-jwt-token")
                .header("alg", "RS256")
                .subject("keycloak-user-uuid-123")
                .claim("preferred_username", "nguyenvana")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        sampleUser = User.builder()
                .id(1L)
                .keycloakId("keycloak-user-uuid-123")
                .username("nguyenvana")
                .email("nguyenvana@gmail.com")
                .build();

        sampleAddress = UserAddress.builder()
                .id(100L)
                .user(sampleUser)
                .recipientName("Nguyễn Văn A")
                .phoneNumber("0912345678")
                .province("Hà Nội")
                .district("Quận Cầu Giấy")
                .ward("Phường Dịch Vọng Hậu")
                .streetAddress("Số 12 Duy Tân")
                .fullAddress("Số 12 Duy Tân, Phường Dịch Vọng Hậu, Quận Cầu Giấy, Hà Nội")
                .addressType(AddressType.HOME)
                .isDefault(true)
                .build();

        sampleResponseDto = AddressResponseDto.builder()
                .id(100L)
                .userId(1L)
                .recipientName("Nguyễn Văn A")
                .phoneNumber("0912345678")
                .province("Hà Nội")
                .district("Quận Cầu Giấy")
                .ward("Phường Dịch Vọng Hậu")
                .streetAddress("Số 12 Duy Tân")
                .fullAddress("Số 12 Duy Tân, Phường Dịch Vọng Hậu, Quận Cầu Giấy, Hà Nội")
                .addressType(AddressType.HOME)
                .isDefault(true)
                .build();

        sampleRequestDto = AddressRequestDto.builder()
                .recipientName("Nguyễn Văn A")
                .phoneNumber("0912345678")
                .province("Hà Nội")
                .district("Quận Cầu Giấy")
                .ward("Phường Dịch Vọng Hậu")
                .streetAddress("Số 12 Duy Tân")
                .addressType(AddressType.HOME)
                .isDefault(true)
                .build();
    }

    @Test
    @DisplayName("Lấy danh sách địa chỉ của người dùng thành công")
    void testGetUserAddressesSuccess() {
        when(userRepository.findByKeycloakId("keycloak-user-uuid-123")).thenReturn(Optional.of(sampleUser));
        when(userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L)).thenReturn(List.of(sampleAddress));
        when(userAddressMapper.toDtoList(any())).thenReturn(List.of(sampleResponseDto));

        List<AddressResponseDto> result = userAddressService.getUserAddresses(sampleJwt);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Nguyễn Văn A", result.get(0).getRecipientName());
        verify(userAddressRepository).findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("Lấy chi tiết một địa chỉ theo ID thành công")
    void testGetAddressByIdSuccess() {
        when(userRepository.findByKeycloakId("keycloak-user-uuid-123")).thenReturn(Optional.of(sampleUser));
        when(userAddressRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleAddress));
        when(userAddressMapper.toDto(sampleAddress)).thenReturn(sampleResponseDto);

        AddressResponseDto result = userAddressService.getAddressById(sampleJwt, 100L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    @Test
    @DisplayName("Lấy chi tiết địa chỉ thất bại nếu ID không tồn tại hoặc không thuộc quyền sở hữu")
    void testGetAddressByIdNotFound() {
        when(userRepository.findByKeycloakId("keycloak-user-uuid-123")).thenReturn(Optional.of(sampleUser));
        when(userAddressRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userAddressService.getAddressById(sampleJwt, 999L));
    }

    @Test
    @DisplayName("Tạo địa chỉ đầu tiên tự động đặt làm mặc định")
    void testCreateFirstAddressSetsDefaultAutomatically() {
        when(userRepository.findByKeycloakId("keycloak-user-uuid-123")).thenReturn(Optional.of(sampleUser));
        when(userAddressRepository.countByUserId(1L)).thenReturn(0L);
        when(userAddressMapper.toEntity(sampleRequestDto)).thenReturn(sampleAddress);
        when(userAddressRepository.save(any(UserAddress.class))).thenReturn(sampleAddress);
        when(userAddressMapper.toDto(sampleAddress)).thenReturn(sampleResponseDto);

        AddressResponseDto result = userAddressService.createAddress(sampleJwt, sampleRequestDto);

        assertNotNull(result);
        assertTrue(result.getIsDefault());
        verify(userAddressRepository).save(any(UserAddress.class));
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Tạo địa chỉ ném lỗi IllegalArgumentException khi đã đạt tối đa 20 địa chỉ")
    void testCreateAddressExceedsLimit() {
        when(userRepository.findByKeycloakId("keycloak-user-uuid-123")).thenReturn(Optional.of(sampleUser));
        when(userAddressRepository.countByUserId(1L)).thenReturn(20L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userAddressService.createAddress(sampleJwt, sampleRequestDto));

        assertTrue(ex.getMessage().contains("tối đa 20"));
    }

    @Test
    @DisplayName("Xóa địa chỉ mặc định sẽ tự động chọn một địa chỉ còn lại làm mặc định mới")
    void testDeleteDefaultAddressReassignsFallback() {
        when(userRepository.findByKeycloakId("keycloak-user-uuid-123")).thenReturn(Optional.of(sampleUser));
        when(userAddressRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleAddress));

        UserAddress remainingAddress = UserAddress.builder()
                .id(101L)
                .user(sampleUser)
                .recipientName("Người nhận thứ 2")
                .phoneNumber("0987654321")
                .fullAddress("Địa chỉ 2")
                .isDefault(false)
                .build();

        when(userAddressRepository.findFirstByUserIdAndIdNotOrderByUpdatedAtDesc(1L, 100L))
                .thenReturn(Optional.of(remainingAddress));

        userAddressService.deleteAddress(sampleJwt, 100L);

        verify(userAddressRepository).delete(sampleAddress);
        assertTrue(remainingAddress.getIsDefault());
        verify(userAddressRepository).save(remainingAddress);
        assertEquals("Địa chỉ 2", sampleUser.getAddress());
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Thiết lập một địa chỉ làm mặc định thành công")
    void testSetDefaultAddressSuccess() {
        when(userRepository.findByKeycloakId("keycloak-user-uuid-123")).thenReturn(Optional.of(sampleUser));
        sampleAddress.setIsDefault(false);
        when(userAddressRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleAddress));
        when(userAddressRepository.save(sampleAddress)).thenReturn(sampleAddress);
        when(userAddressMapper.toDto(sampleAddress)).thenReturn(sampleResponseDto);

        AddressResponseDto result = userAddressService.setDefaultAddress(sampleJwt, 100L);

        assertNotNull(result);
        verify(userAddressRepository).unsetDefaultAddressForUser(1L);
        assertTrue(sampleAddress.getIsDefault());
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Tạo địa chỉ thứ hai với isDefault=false không làm thay đổi địa chỉ mặc định cũ")
    void testCreateSecondAddressAsNonDefault() {
        when(userRepository.findByKeycloakId("keycloak-user-uuid-123")).thenReturn(Optional.of(sampleUser));
        when(userAddressRepository.countByUserId(1L)).thenReturn(1L);

        AddressRequestDto secondReq = AddressRequestDto.builder()
                .recipientName("Người nhận phụ")
                .phoneNumber("0912345678")
                .province("Hà Nội")
                .district("Đống Đa")
                .ward("Láng Thượng")
                .streetAddress("Số 5 Chùa Láng")
                .isDefault(false)
                .build();

        UserAddress secondAddr = UserAddress.builder()
                .id(102L)
                .user(sampleUser)
                .recipientName("Người nhận phụ")
                .isDefault(false)
                .build();

        AddressResponseDto secondResp = AddressResponseDto.builder()
                .id(102L)
                .recipientName("Người nhận phụ")
                .isDefault(false)
                .build();

        when(userAddressMapper.toEntity(secondReq)).thenReturn(secondAddr);
        when(userAddressRepository.save(any(UserAddress.class))).thenReturn(secondAddr);
        when(userAddressMapper.toDto(secondAddr)).thenReturn(secondResp);

        AddressResponseDto result = userAddressService.createAddress(sampleJwt, secondReq);

        assertNotNull(result);
        assertFalse(result.getIsDefault());
        verify(userAddressRepository, never()).unsetDefaultAddressForUser(anyLong());
        verify(userRepository, never()).save(sampleUser);
    }

    @Test
    @DisplayName("Cập nhật thông tin địa chỉ thông thường thành công (giữ nguyên isDefault)")
    void testUpdateAddressBasicInfo() {
        when(userRepository.findByKeycloakId("keycloak-user-uuid-123")).thenReturn(Optional.of(sampleUser));
        sampleAddress.setIsDefault(false);
        when(userAddressRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleAddress));
        when(userAddressRepository.save(sampleAddress)).thenReturn(sampleAddress);
        when(userAddressMapper.toDto(sampleAddress)).thenReturn(sampleResponseDto);

        AddressRequestDto updateDto = AddressRequestDto.builder()
                .recipientName("Nguyễn Văn A (Mới)")
                .phoneNumber("0999888777")
                .province("Hà Nội")
                .district("Quận Cầu Giấy")
                .ward("Phường Dịch Vọng Hậu")
                .streetAddress("Số 99 Duy Tân")
                .addressType(AddressType.OFFICE)
                .isDefault(false)
                .build();

        AddressResponseDto result = userAddressService.updateAddress(sampleJwt, 100L, updateDto);

        assertNotNull(result);
        verify(userAddressMapper).updateEntityFromDto(updateDto, sampleAddress);
        verify(userAddressRepository, never()).unsetDefaultAddressForUser(anyLong());
        verify(userAddressRepository).save(sampleAddress);
    }

    @Test
    @DisplayName("Cập nhật chuyển địa chỉ thành mặc định (wasDefault=false -> wantDefault=true)")
    void testUpdateAddressChangeToDefault() {
        when(userRepository.findByKeycloakId("keycloak-user-uuid-123")).thenReturn(Optional.of(sampleUser));
        sampleAddress.setIsDefault(false);
        when(userAddressRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleAddress));
        when(userAddressRepository.save(sampleAddress)).thenReturn(sampleAddress);
        when(userAddressMapper.toDto(sampleAddress)).thenReturn(sampleResponseDto);

        AddressRequestDto updateDto = AddressRequestDto.builder()
                .recipientName("Nguyễn Văn A")
                .phoneNumber("0912345678")
                .province("Hà Nội")
                .district("Quận Cầu Giấy")
                .ward("Phường Dịch Vọng Hậu")
                .streetAddress("Số 12 Duy Tân")
                .isDefault(true)
                .build();

        AddressResponseDto result = userAddressService.updateAddress(sampleJwt, 100L, updateDto);

        assertNotNull(result);
        verify(userAddressRepository).unsetDefaultAddressForUser(1L);
        assertTrue(sampleAddress.getIsDefault());
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Cập nhật uncheck mặc định trên địa chỉ đang mặc định thì hệ thống vẫn giữ nguyên isDefault=true")
    void testUpdateAddressCannotUnsetDefaultDirectly() {
        when(userRepository.findByKeycloakId("keycloak-user-uuid-123")).thenReturn(Optional.of(sampleUser));
        sampleAddress.setIsDefault(true);
        when(userAddressRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleAddress));
        when(userAddressRepository.save(sampleAddress)).thenReturn(sampleAddress);
        when(userAddressMapper.toDto(sampleAddress)).thenReturn(sampleResponseDto);

        AddressRequestDto updateDto = AddressRequestDto.builder()
                .recipientName("Nguyễn Văn A")
                .phoneNumber("0912345678")
                .province("Hà Nội")
                .district("Quận Cầu Giấy")
                .ward("Phường Dịch Vọng Hậu")
                .streetAddress("Số 12 Duy Tân")
                .isDefault(false) // Cố tình uncheck mặc định
                .build();

        userAddressService.updateAddress(sampleJwt, 100L, updateDto);

        assertTrue(sampleAddress.getIsDefault(), "Phải giữ nguyên isDefault=true để tránh trường hợp không có địa chỉ mặc định");
        verify(userAddressRepository, never()).unsetDefaultAddressForUser(anyLong());
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Cập nhật địa chỉ ném ResourceNotFoundException khi ID không tồn tại hoặc không thuộc user")
    void testUpdateAddressNotFound() {
        when(userRepository.findByKeycloakId("keycloak-user-uuid-123")).thenReturn(Optional.of(sampleUser));
        when(userAddressRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userAddressService.updateAddress(sampleJwt, 999L, sampleRequestDto));
    }

    @Test
    @DisplayName("Xóa địa chỉ không phải mặc định thì không kích hoạt gán fallback hay đổi địa chỉ user")
    void testDeleteNonDefaultAddress() {
        when(userRepository.findByKeycloakId("keycloak-user-uuid-123")).thenReturn(Optional.of(sampleUser));
        sampleAddress.setIsDefault(false);
        when(userAddressRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleAddress));

        userAddressService.deleteAddress(sampleJwt, 100L);

        verify(userAddressRepository).delete(sampleAddress);
        verify(userAddressRepository, never()).findFirstByUserIdAndIdNotOrderByUpdatedAtDesc(anyLong(), anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Xóa địa chỉ mặc định khi đó là địa chỉ duy nhất thì xóa địa chỉ trong User (setAddress null)")
    void testDeleteDefaultAddressWhenOnlyAddress() {
        when(userRepository.findByKeycloakId("keycloak-user-uuid-123")).thenReturn(Optional.of(sampleUser));
        sampleAddress.setIsDefault(true);
        when(userAddressRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleAddress));
        when(userAddressRepository.findFirstByUserIdAndIdNotOrderByUpdatedAtDesc(1L, 100L))
                .thenReturn(Optional.empty());

        userAddressService.deleteAddress(sampleJwt, 100L);

        verify(userAddressRepository).delete(sampleAddress);
        assertNull(sampleUser.getAddress());
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Xóa địa chỉ ném ResourceNotFoundException khi ID không tồn tại hoặc không thuộc user")
    void testDeleteAddressNotFound() {
        when(userRepository.findByKeycloakId("keycloak-user-uuid-123")).thenReturn(Optional.of(sampleUser));
        when(userAddressRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userAddressService.deleteAddress(sampleJwt, 999L));
    }
}
