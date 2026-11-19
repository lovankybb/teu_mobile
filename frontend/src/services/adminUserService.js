/**
 * adminUserService.js
 * Service tương tác với các REST API quản trị người dùng (/api/admin/users)
 */
import api from "./api";

export const adminUserService = {
  /**
   * Lấy danh sách người dùng phân trang và lọc
   */
  async getUsers({
    search = "",
    isActive = null,
    page = 0,
    size = 10,
    sortBy = "createdAt",
    sortDir = "desc",
  } = {}) {
    const params = { page, size, sortBy, sortDir };
    if (search && search.trim()) {
      params.search = search.trim();
    }
    if (isActive !== null && isActive !== undefined && isActive !== "") {
      params.isActive = isActive;
    }

    const response = await api.get("/admin/users", { params });
    return response.data;
  },

  /**
   * Lấy thống kê số lượng người dùng
   */
  async getUserStats() {
    const response = await api.get("/admin/users/stats");
    return response.data;
  },

  /**
   * Lấy chi tiết hồ sơ người dùng kèm danh sách địa chỉ nhận hàng
   */
  async getUserDetail(id) {
    const response = await api.get(`/admin/users/${id}`);
    return response.data;
  },

  /**
   * Khóa hoặc mở khóa tài khoản người dùng
   */
  async updateUserStatus(id, isActive) {
    const response = await api.patch(`/admin/users/${id}/status`, { isActive });
    return response.data;
  },

  /**
   * Gán hoặc gỡ vai trò (role) của người dùng trên Keycloak
   */
  async updateUserRole(id, role, assign) {
    const response = await api.patch(`/admin/users/${id}/role`, { role, assign });
    return response.data;
  },

  /**
   * Tạo tài khoản người dùng mới từ trang quản trị
   */
  async createUser(userData) {
    const response = await api.post("/admin/users", userData);
    return response.data;
  },
};

export default adminUserService;
