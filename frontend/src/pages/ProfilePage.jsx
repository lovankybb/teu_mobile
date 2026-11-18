import React, { useState, useEffect } from 'react';
import Navbar from '../components/layout/Navbar';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import { useAuth } from '../context/AuthContext';
import { User, Mail, Phone, MapPin, CheckCircle, AlertCircle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function ProfilePage() {
  const { user, isAuthenticated, loading, updateProfile } = useAuth();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    fullName: '',
    phoneNumber: '',
    address: '',
  });

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [isUpdating, setIsUpdating] = useState(false);

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      navigate('/login');
    }
    if (user) {
      setFormData({
        fullName: user.fullName || '',
        phoneNumber: user.phoneNumber || '',
        address: user.address || '',
      });
    }
  }, [user, isAuthenticated, loading, navigate]);

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
      await updateProfile(formData);
      setMessage('Cập nhật thông tin thành công!');
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Cập nhật thất bại');
    } finally {
      setIsUpdating(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center">
        <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />

      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="bg-white rounded-3xl border border-slate-200/80 shadow-sm p-6 sm:p-10">
          {/* Header Profile */}
          <div className="flex items-center gap-4 pb-8 border-b border-slate-100">
            <div className="w-16 h-16 rounded-2xl bg-primary text-white text-2xl font-black flex items-center justify-center shadow-lg shadow-red-500/20">
              {user?.fullName ? user.fullName[0].toUpperCase() : 'U'}
            </div>
            <div>
              <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">
                {user?.fullName || user?.username}
              </h1>
              <p className="text-slate-400 text-sm mt-0.5">
                Tài khoản đồng bộ bảo mật Keycloak: <span className="font-mono text-slate-600 text-xs">@{user?.username}</span>
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
