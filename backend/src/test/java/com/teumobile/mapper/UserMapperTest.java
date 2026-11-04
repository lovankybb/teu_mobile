package com.teumobile.mapper;

import com.teumobile.dto.UserProfileResponseDto;
import com.teumobile.dto.UserUpdateRequestDto;
import com.teumobile.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    @DisplayName("Chuyển đổi từ User Entity sang UserProfileResponseDto chính xác")
    void testToDto() {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .id(10L)
                .keycloakId("sub-uuid-abc")
                .username("lequang")
                .email("lequang@gmail.com")
                .fullName("Lê Quang")
                .phoneNumber("0987654321")
                .address("Đà Nẵng")
                .avatarUrl("https://example.com/avatar.jpg")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        UserProfileResponseDto dto = userMapper.toDto(user);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("sub-uuid-abc", dto.getKeycloakId());
        assertEquals("lequang", dto.getUsername());
        assertEquals("lequang@gmail.com", dto.getEmail());
        assertEquals("0987654321", dto.getPhoneNumber());
        assertEquals("Đà Nẵng", dto.getAddress());
        assertTrue(dto.getIsActive());
    }

    @Test
    @DisplayName("Cập nhật Entity từ DTO bỏ qua các trường null và bảo toàn các trường bất biến")
    void testUpdateEntityFromDto() {
        User user = User.builder()
                .id(10L)
                .keycloakId("sub-uuid-abc")
                .username("lequang")
                .email("lequang@gmail.com")
                .fullName("Tên Cũ")
                .phoneNumber("0900000000")
                .address("Địa Chỉ Cũ")
                .build();

        UserUpdateRequestDto updateDto = UserUpdateRequestDto.builder()
                .fullName("Tên Mới Đã Đổi")
                .address("Địa Chỉ Mới")
                .phoneNumber(null) // Giữ nguyên số điện thoại cũ
                .build();

        userMapper.updateEntityFromDto(updateDto, user);

        assertEquals(10L, user.getId());
        assertEquals("sub-uuid-abc", user.getKeycloakId());
        assertEquals("lequang", user.getUsername());
        assertEquals("lequang@gmail.com", user.getEmail());
        assertEquals("Tên Mới Đã Đổi", user.getFullName());
        assertEquals("Địa Chỉ Mới", user.getAddress());
        assertEquals("0900000000", user.getPhoneNumber()); // Không bị ghi đè bởi null
    }
}
