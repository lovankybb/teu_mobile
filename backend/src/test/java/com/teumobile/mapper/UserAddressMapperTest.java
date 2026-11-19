package com.teumobile.mapper;

import com.teumobile.dto.AddressRequestDto;
import com.teumobile.dto.AddressResponseDto;
import com.teumobile.entity.AddressType;
import com.teumobile.entity.User;
import com.teumobile.entity.UserAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserAddressMapperTest {

    private final UserAddressMapper mapper = Mappers.getMapper(UserAddressMapper.class);

    @Test
    @DisplayName("Chuyển đổi UserAddress Entity sang AddressResponseDto chính xác đầy đủ các trường và userId")
    void testToDto() {
        User user = User.builder().id(77L).username("testuser").build();
        LocalDateTime now = LocalDateTime.now();

        UserAddress address = UserAddress.builder()
                .id(101L)
                .user(user)
                .recipientName("Nguyễn Văn A")
                .phoneNumber("0912345678")
                .province("Hà Nội")
                .district("Quận Cầu Giấy")
                .ward("Phường Dịch Vọng")
                .streetAddress("Số 10 Dịch Vọng")
                .fullAddress("Số 10 Dịch Vọng, Phường Dịch Vọng, Quận Cầu Giấy, Hà Nội")
                .addressType(AddressType.OFFICE)
                .isDefault(true)
                .notes("Giao giờ hành chính")
                .createdAt(now)
                .updatedAt(now)
                .build();

        AddressResponseDto dto = mapper.toDto(address);

        assertNotNull(dto);
        assertEquals(101L, dto.getId());
        assertEquals(77L, dto.getUserId());
        assertEquals("Nguyễn Văn A", dto.getRecipientName());
        assertEquals("0912345678", dto.getPhoneNumber());
        assertEquals("Hà Nội", dto.getProvince());
        assertEquals("Quận Cầu Giấy", dto.getDistrict());
        assertEquals("Phường Dịch Vọng", dto.getWard());
        assertEquals("Số 10 Dịch Vọng", dto.getStreetAddress());
        assertEquals("Số 10 Dịch Vọng, Phường Dịch Vọng, Quận Cầu Giấy, Hà Nội", dto.getFullAddress());
        assertEquals(AddressType.OFFICE, dto.getAddressType());
        assertTrue(dto.getIsDefault());
        assertEquals("Giao giờ hành chính", dto.getNotes());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Chuyển đổi danh sách UserAddress Entity sang danh sách AddressResponseDto")
    void testToDtoList() {
        User user = User.builder().id(1L).build();
        UserAddress addr1 = UserAddress.builder().id(1L).user(user).recipientName("A").build();
        UserAddress addr2 = UserAddress.builder().id(2L).user(user).recipientName("B").build();

        List<AddressResponseDto> dtoList = mapper.toDtoList(List.of(addr1, addr2));

        assertNotNull(dtoList);
        assertEquals(2, dtoList.size());
        assertEquals("A", dtoList.get(0).getRecipientName());
        assertEquals("B", dtoList.get(1).getRecipientName());
    }

    @Test
    @DisplayName("Chuyển đổi từ AddressRequestDto sang UserAddress Entity")
    void testToEntity() {
        AddressRequestDto requestDto = AddressRequestDto.builder()
                .recipientName("Trần Thị B")
                .phoneNumber("0987654321")
                .province("TP Hồ Chí Minh")
                .district("Quận 1")
                .ward("Phường Bến Nghé")
                .streetAddress("Số 1 Công Xã Paris")
                .addressType(AddressType.HOME)
                .isDefault(false)
                .notes("Gọi trước khi giao")
                .build();

        UserAddress entity = mapper.toEntity(requestDto);

        assertNotNull(entity);
        assertNull(entity.getId(), "Id phải được ignore khi map từ RequestDto");
        assertNull(entity.getUser(), "User phải được gán riêng biệt bởi Service");
        assertEquals("Trần Thị B", entity.getRecipientName());
        assertEquals("0987654321", entity.getPhoneNumber());
        assertEquals("TP Hồ Chí Minh", entity.getProvince());
        assertEquals("Quận 1", entity.getDistrict());
        assertEquals("Phường Bến Nghé", entity.getWard());
        assertEquals("Số 1 Công Xã Paris", entity.getStreetAddress());
        assertEquals(AddressType.HOME, entity.getAddressType());
        assertFalse(entity.getIsDefault());
        assertEquals("Gọi trước khi giao", entity.getNotes());
    }

    @Test
    @DisplayName("Cập nhật Entity từ AddressRequestDto bỏ qua các trường null và bảo toàn trường bất biến")
    void testUpdateEntityFromDto() {
        User user = User.builder().id(99L).build();
        UserAddress existing = UserAddress.builder()
                .id(200L)
                .user(user)
                .recipientName("Tên ban đầu")
                .phoneNumber("0911111111")
                .province("Hà Nội")
                .district("Cầu Giấy")
                .ward("Dịch Vọng")
                .streetAddress("Ngõ 1")
                .fullAddress("Ngõ 1, Dịch Vọng, Cầu Giấy, Hà Nội")
                .addressType(AddressType.HOME)
                .isDefault(true)
                .notes("Ghi chú cũ")
                .build();

        AddressRequestDto updateDto = AddressRequestDto.builder()
                .recipientName("Tên đã sửa")
                .phoneNumber("0922222222")
                .streetAddress("Ngõ 2 mới")
                .notes(null) // null phải được ignore theo nullValuePropertyMappingStrategy
                .build();

        mapper.updateEntityFromDto(updateDto, existing);

        assertEquals(200L, existing.getId(), "Id không được thay đổi");
        assertEquals(user, existing.getUser(), "User không được thay đổi");
        assertEquals("Tên đã sửa", existing.getRecipientName());
        assertEquals("0922222222", existing.getPhoneNumber());
        assertEquals("Ngõ 2 mới", existing.getStreetAddress());
        assertEquals("Hà Nội", existing.getProvince(), "Province giữ nguyên khi null trong dto");
        assertEquals("Ghi chú cũ", existing.getNotes(), "Notes không bị ghi đè thành null");
    }
}
