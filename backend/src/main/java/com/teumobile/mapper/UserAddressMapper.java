package com.teumobile.mapper;

import com.teumobile.dto.AddressRequestDto;
import com.teumobile.dto.AddressResponseDto;
import com.teumobile.entity.UserAddress;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserAddressMapper {

    @Mapping(target = "userId", source = "user.id")
    AddressResponseDto toDto(UserAddress address);

    List<AddressResponseDto> toDtoList(List<UserAddress> addresses);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "fullAddress", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserAddress toEntity(AddressRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "fullAddress", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(AddressRequestDto dto, @MappingTarget UserAddress address);
}
