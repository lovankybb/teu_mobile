import React, { useState, useEffect } from 'react';
import { X, MapPin, Home, Briefcase, AlertCircle, Loader2 } from 'lucide-react';
import Button from '../ui/Button';
import Input from '../ui/Input';
import Checkbox from '../ui/Checkbox';
import locationService from '../../services/locationService';

export default function AddressModal({
  isOpen,
  onClose,
  onSubmit,
  initialData = null,
  isSubmitting = false,
}) {
  const isEdit = Boolean(initialData?.id);

  const [formData, setFormData] = useState({
    recipientName: '',
    phoneNumber: '',
    province: '',
    district: '',
    ward: '',
    streetAddress: '',
    addressType: 'HOME',
    isDefault: false,
    notes: '',
  });

  const [provinces, setProvinces] = useState([]);
  const [districts, setDistricts] = useState([]);
  const [wards, setWards] = useState([]);

  const [selectedProvinceCode, setSelectedProvinceCode] = useState('');
  const [selectedDistrictCode, setSelectedDistrictCode] = useState('');

  const [isLoadingLocations, setIsLoadingLocations] = useState(false);
  const [formError, setFormError] = useState('');

  // 1. Tải danh mục Tỉnh / Thành phố khi mở modal
  useEffect(() => {
    if (!isOpen) return;

    let isMounted = true;
    const fetchProvinces = async () => {
      setIsLoadingLocations(true);
      try {
        const list = await locationService.getProvinces();
        if (isMounted) setProvinces(list);
      } catch (err) {
        console.error('Không thể tải danh sách Tỉnh/Thành:', err);
      } finally {
        if (isMounted) setIsLoadingLocations(false);
      }
    };

    fetchProvinces();
    return () => { isMounted = false; };
  }, [isOpen]);

  // 2. Điền form data khi mở modal (tạo mới hoặc chỉnh sửa)
  useEffect(() => {
    if (!isOpen) return;

    if (initialData) {
      setFormData({
        recipientName: initialData.recipientName || '',
        phoneNumber: initialData.phoneNumber || '',
        province: initialData.province || '',
        district: initialData.district || '',
        ward: initialData.ward || '',
        streetAddress: initialData.streetAddress || '',
        addressType: initialData.addressType || 'HOME',
        isDefault: Boolean(initialData.isDefault),
        notes: initialData.notes || '',
      });
    } else {
      setFormData({
        recipientName: '',
        phoneNumber: '',
        province: '',
        district: '',
        ward: '',
        streetAddress: '',
        addressType: 'HOME',
        isDefault: false,
        notes: '',
      });
      setSelectedProvinceCode('');
      setSelectedDistrictCode('');
      setDistricts([]);
      setWards([]);
    }
    setFormError('');
  }, [isOpen, initialData]);

  // 3. Khi Tỉnh/Thành thay đổi -> Tải Quận/Huyện
  const handleProvinceChange = async (e) => {
    const provinceName = e.target.value;
    const provinceObj = provinces.find(p => p.name === provinceName);
    const code = provinceObj?.code || '';

    setSelectedProvinceCode(code);
    setSelectedDistrictCode('');
    setDistricts([]);
    setWards([]);

    setFormData(prev => ({
      ...prev,
      province: provinceName,
      district: '',
      ward: '',
    }));

    if (code) {
      const dList = await locationService.getDistricts(code);
      setDistricts(dList);
    }
  };

  // 4. Khi Quận/Huyện thay đổi -> Tải Phường/Xã
  const handleDistrictChange = async (e) => {
    const districtName = e.target.value;
    const districtObj = districts.find(d => d.name === districtName);
    const code = districtObj?.code || '';

    setSelectedDistrictCode(code);
    setWards([]);

    setFormData(prev => ({
      ...prev,
      district: districtName,
      ward: '',
    }));

    if (code) {
      const wList = await locationService.getWards(code);
      setWards(wList);
    }
  };

  const handleWardChange = (e) => {
    setFormData(prev => ({ ...prev, ward: e.target.value }));
  };

  const handleChange = (e) => {
    const { id, value } = e.target;
    setFormData(prev => ({ ...prev, [id]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    setFormError('');

    // Validation cơ bản phía client
    if (!formData.recipientName.trim()) {
      setFormError('Vui lòng nhập họ và tên người nhận');
      return;
    }
    if (!formData.phoneNumber.trim()) {
      setFormError('Vui lòng nhập số điện thoại');
      return;
    }
    if (!formData.province) {
      setFormError('Vui lòng chọn Tỉnh / Thành phố');
      return;
    }
    if (!formData.district) {
      setFormError('Vui lòng chọn Quận / Huyện');
      return;
    }
    if (!formData.ward) {
      setFormError('Vui lòng chọn Phường / Xã');
      return;
    }
    if (!formData.streetAddress.trim()) {
      setFormError('Vui lòng nhập địa chỉ cụ thể (số nhà, tên đường)');
      return;
    }

    onSubmit(formData);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 overflow-y-auto bg-slate-900/60 backdrop-blur-sm animate-fadeIn">
      <div className="relative w-full max-w-lg bg-white rounded-3xl shadow-2xl border border-slate-100 overflow-hidden my-8">
        {/* Header Modal */}
        <div className="px-6 py-5 border-b border-slate-100 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-red-50 text-primary flex items-center justify-center font-bold">
              <MapPin className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-slate-900 tracking-tight">
                {isEdit ? 'Cập nhật địa chỉ nhận hàng' : 'Thêm địa chỉ nhận hàng mới'}
              </h3>
              <p className="text-xs text-slate-400">
                Địa chỉ dùng để tính phí ship và giao hàng nhanh chóng
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            className="p-1.5 rounded-xl text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {formError && (
            <div className="p-3.5 rounded-xl bg-red-50 border border-red-200 text-red-600 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>{formError}</span>
            </div>
          )}

          {/* Hàng 1: Tên & SĐT */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              id="recipientName"
              label="Họ và tên người nhận"
              value={formData.recipientName}
              onChange={handleChange}
              placeholder="Nguyễn Văn A"
              required
            />

            <Input
              id="phoneNumber"
              label="Số điện thoại"
              type="tel"
              value={formData.phoneNumber}
              onChange={handleChange}
              placeholder="0912 345 678"
              required
            />
          </div>

          {/* Hàng 2: Tỉnh / Thành phố & Quận / Huyện */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-semibold text-slate-800 mb-1.5">
                Tỉnh / Thành phố <span className="text-primary">*</span>
              </label>
              <select
                value={formData.province}
                onChange={handleProvinceChange}
                disabled={isLoadingLocations || isSubmitting}
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all cursor-pointer"
                required
              >
                <option value="">-- Chọn Tỉnh / Thành --</option>
                {provinces.map(p => (
                  <option key={p.code} value={p.name}>{p.name}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-semibold text-slate-800 mb-1.5">
                Quận / Huyện <span className="text-primary">*</span>
              </label>
              {districts.length > 0 ? (
                <select
                  value={formData.district}
                  onChange={handleDistrictChange}
                  disabled={isSubmitting || !formData.province}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all cursor-pointer"
                  required
                >
                  <option value="">-- Chọn Quận / Huyện --</option>
                  {districts.map(d => (
                    <option key={d.code} value={d.name}>{d.name}</option>
                  ))}
                </select>
              ) : (
                <input
                  type="text"
                  id="district"
                  value={formData.district}
                  onChange={handleChange}
                  placeholder="Nhập Quận / Huyện"
                  disabled={isSubmitting}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                  required
                />
              )}
            </div>
          </div>

          {/* Hàng 3: Phường / Xã */}
          <div>
            <label className="block text-sm font-semibold text-slate-800 mb-1.5">
              Phường / Xã / Thị trấn <span className="text-primary">*</span>
            </label>
            {wards.length > 0 ? (
              <select
                value={formData.ward}
                onChange={handleWardChange}
                disabled={isSubmitting || !formData.district}
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all cursor-pointer"
                required
              >
                <option value="">-- Chọn Phường / Xã --</option>
                {wards.map(w => (
                  <option key={w.code} value={w.name}>{w.name}</option>
                ))}
              </select>
            ) : (
              <input
                type="text"
                id="ward"
                value={formData.ward}
                onChange={handleChange}
                placeholder="Nhập Phường / Xã"
                disabled={isSubmitting}
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                required
              />
            )}
          </div>

          {/* Hàng 4: Địa chỉ cụ thể */}
          <Input
            id="streetAddress"
            label="Địa chỉ cụ thể (Số nhà, tên ngõ/đường, tòa nhà)"
            value={formData.streetAddress}
            onChange={handleChange}
            placeholder="Số 10, Ngõ 45, Đường Trần Hưng Đạo"
            required
          />

          {/* Hàng 5: Loại địa chỉ (Nhà riêng vs Văn phòng) */}
          <div>
            <label className="block text-sm font-semibold text-slate-800 mb-1.5">
              Loại địa chỉ
            </label>
            <div className="grid grid-cols-2 gap-3">
              <button
                type="button"
                onClick={() => setFormData(prev => ({ ...prev, addressType: 'HOME' }))}
                className={`flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl text-xs sm:text-sm font-semibold border transition-all ${
                  formData.addressType === 'HOME'
                    ? 'border-primary bg-red-50 text-primary shadow-sm'
                    : 'border-slate-200 text-slate-600 hover:bg-slate-50'
                }`}
              >
                <Home className="w-4 h-4" />
                Nhà riêng
              </button>

              <button
                type="button"
                onClick={() => setFormData(prev => ({ ...prev, addressType: 'OFFICE' }))}
                className={`flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl text-xs sm:text-sm font-semibold border transition-all ${
                  formData.addressType === 'OFFICE'
                    ? 'border-primary bg-red-50 text-primary shadow-sm'
                    : 'border-slate-200 text-slate-600 hover:bg-slate-50'
                }`}
              >
                <Briefcase className="w-4 h-4" />
                Văn phòng
              </button>
            </div>
          </div>

          {/* Hàng 6: Ghi chú giao hàng */}
          <Input
            id="notes"
            label="Ghi chú giao hàng (Tùy chọn)"
            value={formData.notes}
            onChange={handleChange}
            placeholder="Ví dụ: Gọi trước khi giao, gửi lễ tân..."
          />

          {/* Hàng 7: Checkbox Đặt làm mặc định */}
          <div className="pt-1">
            <Checkbox
              id="isDefault"
              label="Đặt làm địa chỉ nhận hàng mặc định"
              checked={formData.isDefault}
              onChange={(e) => setFormData(prev => ({ ...prev, isDefault: e.target.checked }))}
            />
          </div>

          {/* Footer Modal Actions */}
          <div className="pt-4 flex items-center justify-end gap-3 border-t border-slate-100">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="px-5 py-2.5 rounded-xl text-sm font-semibold text-slate-600 hover:text-slate-800 hover:bg-slate-100 transition-colors"
            >
              Hủy
            </button>

            <Button
              type="submit"
              variant="primary"
              size="md"
              isLoading={isSubmitting}
            >
              {isEdit ? 'Lưu cập nhật' : 'Thêm địa chỉ'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
