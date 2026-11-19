package com.teumobile.service;

import com.teumobile.dto.PageResponseDto;
import com.teumobile.dto.admin.*;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

public interface AdminUserService {

    /**
     * Lấy danh sách người dùng phân trang và lọc theo tiêu chí (search, isActive)
     */
    PageResponseDto<UserAdminResponseDto> getUsers(String search, Boolean isActive, Pageable pageable);

    /**
     * Lấy thông tin chi tiết một người dùng kèm danh sách địa chỉ nhận hàng
     */
    UserAdminDetailResponseDto getUserDetail(Long id);

    /**
     * Cập nhật trạng thái khóa/mở khóa người dùng (đồng bộ sang Keycloak)
     * Ngăn chặn Quản trị viên tự khóa tài khoản của chính mình.
     */
    UserAdminResponseDto updateUserStatus(Long id, UserStatusUpdateRequestDto request, Jwt jwt);

    /**
     * Gán hoặc gỡ vai trò (role) của người dùng trên Keycloak
     * Ngăn chặn Quản trị viên tự hạ cấp vai trò ADMIN của chính mình.
     */
    UserAdminResponseDto updateUserRole(Long id, UserRoleUpdateRequestDto request, Jwt jwt);

    /**
     * Tạo tài khoản người dùng mới từ trang quản trị
     */
    UserAdminResponseDto createUser(UserAdminCreateRequestDto request);

    /**
     * Thống kê tổng số lượng người dùng, hoạt động, bị khóa
     */
    UserStatsResponseDto getUserStats();
}
