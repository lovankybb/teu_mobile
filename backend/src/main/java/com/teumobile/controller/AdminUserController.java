package com.teumobile.controller;

import com.teumobile.dto.ApiResponse;
import com.teumobile.dto.PageResponseDto;
import com.teumobile.dto.admin.*;
import com.teumobile.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * Lấy danh sách người dùng phân trang và lọc (tìm kiếm, trạng thái hoạt động)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<UserAdminResponseDto>>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = Sort.by(
                sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy
        );
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponseDto<UserAdminResponseDto> result = adminUserService.getUsers(search, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách người dùng thành công"));
    }

    /**
     * Thống kê số lượng người dùng hệ thống
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStatsResponseDto>> getUserStats() {
        UserStatsResponseDto stats = adminUserService.getUserStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Lấy thống kê người dùng thành công"));
    }

    /**
     * Xem thông tin chi tiết một người dùng kèm danh sách địa chỉ nhận hàng
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserAdminDetailResponseDto>> getUserDetail(
            @PathVariable Long id
    ) {
        UserAdminDetailResponseDto detail = adminUserService.getUserDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Lấy thông tin chi tiết người dùng thành công"));
    }

    /**
     * Khóa hoặc mở khóa tài khoản người dùng
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserAdminResponseDto>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequestDto request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserAdminResponseDto updatedUser = adminUserService.updateUserStatus(id, request, jwt);
        String action = Boolean.TRUE.equals(request.getIsActive()) ? "Mở khóa" : "Khóa";
        return ResponseEntity.ok(ApiResponse.success(updatedUser, action + " tài khoản thành công"));
    }

    /**
     * Gán hoặc gỡ vai trò (role) của người dùng trên Keycloak
     */
    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserAdminResponseDto>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateRequestDto request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserAdminResponseDto updatedUser = adminUserService.updateUserRole(id, request, jwt);
        String action = Boolean.TRUE.equals(request.getAssign()) ? "Gán" : "Gỡ";
        return ResponseEntity.ok(ApiResponse.success(updatedUser, action + " vai trò thành công"));
    }

    /**
     * Tạo tài khoản người dùng mới từ trang quản trị
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserAdminResponseDto>> createUser(
            @Valid @RequestBody UserAdminCreateRequestDto request
    ) {
        UserAdminResponseDto createdUser = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdUser, "Tạo tài khoản người dùng thành công"));
    }
}
