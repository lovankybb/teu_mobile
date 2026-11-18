/**
 * locationService.js
 * Quản lý danh mục Đơn vị hành chính 3 cấp Việt Nam (Tỉnh/Thành -> Quận/Huyện -> Phường/Xã)
 * Sử dụng Open API (provinces.open-api.vn) kết hợp bộ nhớ cache và fallback dự phòng.
 */
import axios from 'axios';

const VIETNAM_PROVINCES = [
  'Hà Nội', 'TP. Hồ Chí Minh', 'Đà Nẵng', 'Hải Phòng', 'Cần Thơ',
  'An Giang', 'Bà Rịa - Vũng Tàu', 'Bắc Giang', 'Bắc Kạn', 'Bạc Liêu',
  'Bắc Ninh', 'Bến Tre', 'Bình Định', 'Bình Dương', 'Bình Phước',
  'Bình Thuận', 'Cà Mau', 'Cao Bằng', 'Đắk Lắk', 'Đắk Nông',
  'Điện Biên', 'Đồng Nai', 'Đồng Tháp', 'Gia Lai', 'Hà Giang',
  'Hà Nam', 'Hà Tĩnh', 'Hải Dương', 'Hậu Giang', 'Hòa Bình',
  'Hưng Yên', 'Khánh Hòa', 'Kiên Giang', 'Kon Tum', 'Lai Châu',
  'Lâm Đồng', 'Lạng Sơn', 'Lào Cai', 'Long An', 'Nam Định',
  'Nghệ An', 'Ninh Bình', 'Ninh Thuận', 'Phú Thọ', 'Phú Yên',
  'Quảng Bình', 'Quảng Nam', 'Quảng Ngãi', 'Quảng Ninh', 'Quảng Trị',
  'Sóc Trăng', 'Sơn La', 'Tây Ninh', 'Thái Bình', 'Thái Nguyên',
  'Thanh Hóa', 'Thừa Thiên Huế', 'Tiền Giang', 'Trà Vinh', 'Tuyên Quang',
  'Vĩnh Long', 'Vĩnh Phúc', 'Yên Bái'
];

// In-memory cache để tránh gọi lặp lại API
const cache = {
  provinces: null,
  districts: {},
  wards: {},
};

export const locationService = {
  /**
   * Lấy danh sách 63 Tỉnh / Thành phố
   */
  async getProvinces() {
    if (cache.provinces) return cache.provinces;

    try {
      const res = await axios.get('https://provinces.open-api.vn/api/?depth=1', { timeout: 4000 });
      if (res.data && Array.isArray(res.data)) {
        cache.provinces = res.data.map(p => ({
          code: p.code,
          name: p.name,
        }));
        return cache.provinces;
      }
    } catch {
      // Fallback về danh sách tĩnh nếu không kết nối được API bên ngoài
    }

    cache.provinces = VIETNAM_PROVINCES.map((name, index) => ({
      code: index + 1,
      name,
    }));
    return cache.provinces;
  },

  /**
   * Lấy danh sách Quận / Huyện theo mã Tỉnh / Thành
   */
  async getDistricts(provinceCode) {
    if (!provinceCode) return [];
    if (cache.districts[provinceCode]) return cache.districts[provinceCode];

    try {
      const res = await axios.get(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`, { timeout: 4000 });
      if (res.data && res.data.districts) {
        cache.districts[provinceCode] = res.data.districts.map(d => ({
          code: d.code,
          name: d.name,
        }));
        return cache.districts[provinceCode];
      }
    } catch {
      // Bỏ qua lỗi mạng
    }

    return [];
  },

  /**
   * Lấy danh sách Phường / Xã theo mã Quận / Huyện
   */
  async getWards(districtCode) {
    if (!districtCode) return [];
    if (cache.wards[districtCode]) return cache.wards[districtCode];

    try {
      const res = await axios.get(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`, { timeout: 4000 });
      if (res.data && res.data.wards) {
        cache.wards[districtCode] = res.data.wards.map(w => ({
          code: w.code,
          name: w.name,
        }));
        return cache.wards[districtCode];
      }
    } catch {
      // Bỏ qua lỗi mạng
    }

    return [];
  },
};

export default locationService;
