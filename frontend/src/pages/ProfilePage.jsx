import React, { useState, useEffect, useRef } from 'react';
import Navbar from '../components/layout/Navbar';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import { useAuth } from '../context/AuthContext';
import { User, Mail, Phone, MapPin, CheckCircle, AlertCircle, Camera, Loader2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function ProfilePage() {
  const { user, isAuthenticated, loading, login, updateProfile, uploadAvatar } = useAuth();
  const navigate = useNavigate();
  const fileInputRef = useRef(null);

  const [formData, setFormData] = useState({
    fullName: '',
    phoneNumber: '',
    address: '',
  });

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [isUpdating, setIsUpdating] = useState(false);

  // Trạng thái upload avatar trực tiếp lên Cloudinary
  const [isUploadingAvatar, setIsUploadingAvatar] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [avatarPreview, setAvatarPreview] = useState(null);

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      login(window.location.href);
    }
    if (user) {
      setFormData({
        fullName: user.fullName || '',
        phoneNumber: user.phoneNumber || '',
        address: user.address || '',
      });
    }
  }, [user, isAuthenticated, loading, login]);

  const handleChange = (e) => {
    const { id, value } = e.target;
    setFormData((prev) => ({ ...prev, [id]: value }));
    if (message) setMessage('');
    if (error) setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsUpdating(true);
    setMessage('');
    setError('');

    try {
      await updateProfile({
        ...formData,
        avatarUrl: user?.avatarUrl || null,
      });
      setMessage('Cập nhật thông tin thành công!');
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Cập nhật thất bại');
    } finally {
      setIsUpdating(false);
    }
  };

  /**
   * Xử lý khi người dùng chọn file ảnh đại diện:
   * 1. Validate file (định dạng, kích thước < 5MB)
   * 2. Preview ngay lập tức trên UI
   * 3. Gọi uploadAvatar (xin chữ ký -> upload thẳng Cloudinary -> lưu URL vào DB)
   */
  const handleAvatarFileChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Reset input để người dùng có thể chọn lại cùng file nếu muốn
    e.target.value = '';

    // Kiểm tra định dạng ảnh
    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
    if (!allowedTypes.includes(file.type)) {
      setError('Chỉ chấp nhận file ảnh định dạng JPG, PNG, WEBP hoặc GIF');
      return;
    }

    // Kiểm tra kích thước (< 5MB)
    if (file.size > 5 * 1024 * 1024) {
      setError('Kích thước ảnh không được vượt quá 5MB');
      return;
    }

    // Tạo preview tạm thời trong lúc upload
    const localPreviewUrl = URL.createObjectURL(file);
    setAvatarPreview(localPreviewUrl);
    setIsUploadingAvatar(true);
    setUploadProgress(0);
    setMessage('');
    setError('');

    try {
      await uploadAvatar(file, (progress) => {
        setUploadProgress(progress);
      });
      setAvatarPreview(null); // Thành công: Xóa preview tạm, dùng URL chính thức từ database
      setMessage('Cập nhật ảnh đại diện thành công!');
    } catch (err) {
      console.error('Lỗi upload avatar:', err);
      const errMsg = err.response?.data?.error?.message
        || err.response?.data?.message
        || err.message
        || 'Tải ảnh đại diện lên thất bại';
      setError(errMsg);
      setAvatarPreview(null); // Hoàn tác preview nếu upload thất bại
    } finally {
      setIsUploadingAvatar(false);
    }
  };

  const handleAvatarClick = () => {
    if (!isUploadingAvatar) {
      fileInputRef.current?.click();
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center">
        <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  const currentAvatarSrc = avatarPreview || user?.avatarUrl;

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />

      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="bg-white rounded-3xl border border-slate-200/80 shadow-sm p-6 sm:p-10">
          {/* Header Profile & Avatar Upload */}
          <div className="flex flex-col sm:flex-row items-center sm:items-start gap-6 pb-8 border-b border-slate-100">
            {/* Vùng Avatar có nút đổi ảnh */}
            <div className="relative group flex-shrink-0">
              <input
                ref={fileInputRef}
                type="file"
                accept="image/png,image/jpeg,image/webp,image/gif"
                onChange={handleAvatarFileChange}
                className="hidden"
                disabled={isUploadingAvatar}
              />

              <div
                onClick={handleAvatarClick}
                className={`w-24 h-24 sm:w-28 sm:h-28 rounded-3xl overflow-hidden relative cursor-pointer shadow-md transition-all duration-200 ${
                  isUploadingAvatar ? 'ring-4 ring-primary/30 cursor-wait' : 'hover:ring-4 hover:ring-primary/20 hover:scale-105'
                }`}
              >
                {currentAvatarSrc ? (
                  <img
                    src={currentAvatarSrc}
                    alt={user?.fullName || 'Avatar'}
                    referrerPolicy="no-referrer"
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div className="w-full h-full bg-gradient-to-br from-red-500 to-rose-600 text-white text-3xl font-black flex items-center justify-center">
                    {user?.fullName ? user.fullName[0].toUpperCase() : 'U'}
                  </div>
                )}

                {/* Overlay khi đang tải lên */}
                {isUploadingAvatar ? (
                  <div className="absolute inset-0 bg-black/60 backdrop-blur-sm flex flex-col items-center justify-center text-white p-2">
                    <Loader2 className="w-6 h-6 animate-spin text-white mb-1" />
                    <span className="text-[11px] font-bold">
                      {uploadProgress > 0 ? `${uploadProgress}%` : 'Đang tải...'}
                    </span>
                  </div>
                ) : (
                  /* Overlay hover gợi ý đổi ảnh */
                  <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex flex-col items-center justify-center text-white">
                    <Camera className="w-6 h-6 mb-1 text-white" />
                    <span className="text-[10px] font-bold tracking-tight">Đổi ảnh</span>
                  </div>
                )}
              </div>

              {/* Nút badge camera góc dưới phải */}
              <button
                type="button"
                onClick={handleAvatarClick}
                disabled={isUploadingAvatar}
                title="Thay đổi ảnh đại diện"
                className="absolute -bottom-1.5 -right-1.5 p-2 rounded-xl bg-white text-slate-700 shadow-md border border-slate-200 hover:text-primary hover:border-primary/40 transition-all hover:scale-110 active:scale-95"
              >
                <Camera className="w-4 h-4" />
              </button>
            </div>

            {/* Thông tin chào mừng & định danh */}
            <div className="text-center sm:text-left flex-1">
              <div className="flex flex-wrap items-center justify-center sm:justify-start gap-2">
                <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">
                  {user?.fullName || user?.username}
                </h1>
                <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                  Đã xác thực
                </span>
              </div>
              <p className="text-slate-400 text-sm mt-1">
                Tài khoản đồng bộ bảo mật Keycloak: <span className="font-mono text-slate-600 text-xs font-semibold">@{user?.username}</span>
              </p>
              <p className="text-slate-500 text-xs mt-2">
                Nhấp vào ảnh đại diện hoặc biểu tượng máy ảnh để tải ảnh mới lên (JPG, PNG, WEBP, tối đa 5MB).
              </p>
            </div>
          </div>

          {/* Alert Messages */}
          {message && (
            <div className="mt-6 p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm flex items-center gap-2">
              <CheckCircle className="w-5 h-5 text-emerald-500 flex-shrink-0" />
              <span>{message}</span>
            </div>
          )}

          {error && (
            <div className="mt-6 p-4 rounded-xl bg-red-50 border border-red-200 text-red-600 text-sm flex items-center gap-2">
              <AlertCircle className="w-5 h-5 text-red-500 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Form Thông tin người dùng */}
          <form onSubmit={handleSubmit} className="mt-8 space-y-6">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
              {/* Tên đăng nhập (Readonly) */}
              <Input
                label="Tên đăng nhập"
                value={user?.username || ''}
                disabled
                helperText="Tên đăng nhập được quản lý bởi Keycloak"
              />

              {/* Email (Readonly) */}
              <Input
                label="Địa chỉ Email"
                value={user?.email || ''}
                disabled
                helperText="Email định danh bảo mật OIDC"
              />
            </div>

            <Input
              id="fullName"
              label="Họ và tên hiển thị"
              value={formData.fullName}
              onChange={handleChange}
              placeholder="Nguyễn Văn A"
              required
            />

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
              <Input
                id="phoneNumber"
                label="Số điện thoại nhận hàng"
                type="tel"
                value={formData.phoneNumber}
                onChange={handleChange}
                placeholder="0912 345 678"
                helperText="Định dạng số điện thoại Việt Nam 10 chữ số"
              />

              <Input
                id="address"
                label="Địa chỉ giao hàng mặc định"
                value={formData.address}
                onChange={handleChange}
                placeholder="Số nhà, đường, quận/huyện, tỉnh thành"
              />
            </div>

            <div className="pt-4 flex justify-end">
              <Button
                type="submit"
                variant="primary"
                size="md"
                isLoading={isUpdating}
              >
                Lưu thay đổi
              </Button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}
