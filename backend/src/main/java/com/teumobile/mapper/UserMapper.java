package com.teumobile.mapper;

import com.teumobile.dto.UserProfileResponseDto;
import com.teumobile.dto.UserUpdateRequestDto;
import com.teumobile.dto.admin.UserAdminDetailResponseDto;
import com.teumobile.dto.admin.UserAdminResponseDto;
import com.teumobile.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    /**
     * Chuyển đổi từ Entity User sang UserProfileResponseDto
     */
    UserProfileResponseDto toDto(User user);

    /**
     * Chuyển đổi danh sách User sang danh sách DTO
     */
    List<UserProfileResponseDto> toDtoList(List<User> users);

    /**
     * Chuyển đổi từ Entity User sang UserAdminResponseDto (dành cho Admin)
     */
    @Mapping(target = "roles", ignore = true)
    UserAdminResponseDto toAdminDto(User user);

    /**
     * Chuyển đổi từ Entity User sang UserAdminDetailResponseDto (kèm chi tiết)
     */
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    UserAdminDetailResponseDto toAdminDetailDto(User user);

    /**
     * Cập nhật các trường không null từ UserUpdateRequestDto vào Entity User hiện có
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakId", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UserUpdateRequestDto dto, @MappingTarget User user);
}
