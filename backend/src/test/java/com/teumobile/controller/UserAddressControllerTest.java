package com.teumobile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teumobile.config.KeycloakProperties;
import com.teumobile.config.SecurityConfig;
import com.teumobile.dto.AddressRequestDto;
import com.teumobile.dto.AddressResponseDto;
import com.teumobile.entity.AddressType;
import com.teumobile.exception.GlobalExceptionHandler;
import com.teumobile.exception.ResourceNotFoundException;
import com.teumobile.security.KeycloakJwtAuthenticationConverter;
import com.teumobile.service.UserAddressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserAddressController.class)
@Import({SecurityConfig.class, KeycloakJwtAuthenticationConverter.class, KeycloakProperties.class, GlobalExceptionHandler.class})
class UserAddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserAddressService userAddressService;

    @Test
    @DisplayName("Truy cập /api/users/addresses không có Bearer Token trả về 401 Unauthorized")
    void testGetAddressesUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/addresses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Lấy danh sách địa chỉ của người dùng thành công khi có Token")
    void testGetAddressesSuccess() throws Exception {
        AddressResponseDto addressDto = AddressResponseDto.builder()
                .id(1L)
                .userId(10L)
                .recipientName("Trần Văn B")
                .phoneNumber("0988776655")
                .province("Hà Nội")
                .district("Cầu Giấy")
                .ward("Dịch Vọng")
                .streetAddress("Số 1 Cầu Giấy")
                .fullAddress("Số 1 Cầu Giấy, Dịch Vọng, Cầu Giấy, Hà Nội")
                .addressType(AddressType.HOME)
                .isDefault(true)
                .build();

        when(userAddressService.getUserAddresses(any())).thenReturn(List.of(addressDto));

        mockMvc.perform(get("/api/users/addresses")
                        .with(jwt().jwt(builder -> builder.subject("keycloak-sub-123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].recipientName").value("Trần Văn B"))
                .andExpect(jsonPath("$.data[0].isDefault").value(true));
    }

    @Test
    @DisplayName("Thêm địa chỉ mới thành công trả về 201 Created")
    void testCreateAddressSuccess() throws Exception {
        AddressRequestDto requestDto = AddressRequestDto.builder()
                .recipientName("Trần Văn B")
                .phoneNumber("0988776655")
                .province("Hà Nội")
                .district("Cầu Giấy")
                .ward("Dịch Vọng")
                .streetAddress("Số 1 Cầu Giấy")
                .addressType(AddressType.HOME)
                .isDefault(true)
                .build();

        AddressResponseDto responseDto = AddressResponseDto.builder()
                .id(1L)
                .recipientName("Trần Văn B")
                .phoneNumber("0988776655")
                .fullAddress("Số 1 Cầu Giấy, Dịch Vọng, Cầu Giấy, Hà Nội")
                .isDefault(true)
                .build();

        when(userAddressService.createAddress(any(), any(AddressRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/users/addresses")
                        .with(jwt().jwt(builder -> builder.subject("keycloak-sub-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @DisplayName("Thêm địa chỉ thất bại trả về 400 Bad Request khi số điện thoại không hợp lệ")
    void testCreateAddressInvalidPhone() throws Exception {
        AddressRequestDto invalidDto = AddressRequestDto.builder()
                .recipientName("Trần Văn B")
                .phoneNumber("12345") // SĐT không hợp lệ
                .province("Hà Nội")
                .district("Cầu Giấy")
                .ward("Dịch Vọng")
                .streetAddress("Số 1 Cầu Giấy")
                .build();

        mockMvc.perform(post("/api/users/addresses")
                        .with(jwt().jwt(builder -> builder.subject("keycloak-sub-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.phoneNumber").exists());
    }

    @Test
    @DisplayName("Cập nhật địa chỉ thành công trả về 200 OK")
    void testUpdateAddressSuccess() throws Exception {
        AddressRequestDto updateDto = AddressRequestDto.builder()
                .recipientName("Nguyễn Văn C")
                .phoneNumber("0912345678")
                .province("TP Hồ Chí Minh")
                .district("Quận 1")
                .ward("Bến Nghé")
                .streetAddress("Đường Lê Duẩn")
                .addressType(AddressType.OFFICE)
                .isDefault(false)
                .build();

        AddressResponseDto updatedResponse = AddressResponseDto.builder()
                .id(1L)
                .recipientName("Nguyễn Văn C")
                .addressType(AddressType.OFFICE)
                .build();

        when(userAddressService.updateAddress(any(), eq(1L), any(AddressRequestDto.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/users/addresses/1")
                        .with(jwt().jwt(builder -> builder.subject("keycloak-sub-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recipientName").value("Nguyễn Văn C"));
    }

    @Test
    @DisplayName("Xóa địa chỉ thành công trả về 200 OK")
    void testDeleteAddressSuccess() throws Exception {
        doNothing().when(userAddressService).deleteAddress(any(), eq(1L));

        mockMvc.perform(delete("/api/users/addresses/1")
                        .with(jwt().jwt(builder -> builder.subject("keycloak-sub-123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Đặt làm địa chỉ mặc định thành công trả về 200 OK")
    void testSetDefaultAddressSuccess() throws Exception {
        AddressResponseDto defaultResponse = AddressResponseDto.builder()
                .id(2L)
                .isDefault(true)
                .build();

        when(userAddressService.setDefaultAddress(any(), eq(2L))).thenReturn(defaultResponse);

        mockMvc.perform(patch("/api/users/addresses/2/default")
                        .with(jwt().jwt(builder -> builder.subject("keycloak-sub-123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isDefault").value(true));
    }

    @Test
    @DisplayName("Lấy chi tiết một địa chỉ theo ID thành công trả về 200 OK")
    void testGetAddressByIdSuccess() throws Exception {
        AddressResponseDto responseDto = AddressResponseDto.builder()
                .id(100L)
                .recipientName("Trần Văn B")
                .phoneNumber("0988776655")
                .fullAddress("Số 1 Cầu Giấy, Dịch Vọng, Cầu Giấy, Hà Nội")
                .build();

        when(userAddressService.getAddressById(any(), eq(100L))).thenReturn(responseDto);

        mockMvc.perform(get("/api/users/addresses/100")
                        .with(jwt().jwt(builder -> builder.subject("keycloak-sub-123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100L))
                .andExpect(jsonPath("$.data.recipientName").value("Trần Văn B"));
    }

    @Test
    @DisplayName("Lấy chi tiết địa chỉ khi ID không tồn tại trả về 404 Not Found")
    void testGetAddressByIdNotFound() throws Exception {
        when(userAddressService.getAddressById(any(), eq(999L)))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy địa chỉ với ID: 999"));

        mockMvc.perform(get("/api/users/addresses/999")
                        .with(jwt().jwt(builder -> builder.subject("keycloak-sub-123"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Không tìm thấy địa chỉ với ID: 999"));
    }

    @Test
    @DisplayName("Thêm địa chỉ khi đã đạt tối đa 20 địa chỉ trả về 400 Bad Request")
    void testCreateAddressLimitExceeded() throws Exception {
        AddressRequestDto requestDto = AddressRequestDto.builder()
                .recipientName("Trần Văn B")
                .phoneNumber("0988776655")
                .province("Hà Nội")
                .district("Cầu Giấy")
                .ward("Dịch Vọng")
                .streetAddress("Số 1 Cầu Giấy")
                .build();

        when(userAddressService.createAddress(any(), any(AddressRequestDto.class)))
                .thenThrow(new IllegalArgumentException("Bạn chỉ có thể lưu tối đa 20 địa chỉ nhận hàng"));

        mockMvc.perform(post("/api/users/addresses")
                        .with(jwt().jwt(builder -> builder.subject("keycloak-sub-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Bạn chỉ có thể lưu tối đa 20 địa chỉ nhận hàng"));
    }

    @Test
    @DisplayName("Xóa địa chỉ khi ID không tồn tại trả về 404 Not Found")
    void testDeleteAddressNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Không tìm thấy địa chỉ với ID: 999"))
                .when(userAddressService).deleteAddress(any(), eq(999L));

        mockMvc.perform(delete("/api/users/addresses/999")
                        .with(jwt().jwt(builder -> builder.subject("keycloak-sub-123"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
