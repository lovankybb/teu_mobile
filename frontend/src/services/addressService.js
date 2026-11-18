import api from './api';

export const addressService = {
  /**
   * Lấy danh sách toàn bộ địa chỉ của người dùng đang đăng nhập
   */
  async getAddresses() {
    const res = await api.get('/users/addresses');
    return res.data?.data || [];
  },

  /**
   * Lấy thông tin chi tiết một địa chỉ
   */
  async getAddressById(id) {
    const res = await api.get(`/users/addresses/${id}`);
    return res.data?.data;
  },

  /**
   * Thêm mới địa chỉ nhận hàng
   */
  async createAddress(addressData) {
    const res = await api.post('/users/addresses', addressData);
    return res.data?.data;
  },

  /**
   * Cập nhật thông tin địa chỉ
   */
  async updateAddress(id, addressData) {
    const res = await api.put(`/users/addresses/${id}`, addressData);
    return res.data?.data;
  },

  /**
   * Xóa một địa chỉ khỏi sổ địa chỉ
   */
  async deleteAddress(id) {
    const res = await api.delete(`/users/addresses/${id}`);
    return res.data;
  },

  /**
   * Đặt làm địa chỉ giao hàng mặc định
   */
  async setDefaultAddress(id) {
    const res = await api.patch(`/users/addresses/${id}/default`);
    return res.data?.data;
  },
};

export default addressService;
