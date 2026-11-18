import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../../components/layout/AuthLayout';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import { useAuth } from '../../context/AuthContext';
import { AlertCircle } from 'lucide-react';

export default function RegisterPage() {
  const navigate = useNavigate();
  const { register } = useAuth();

  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    fullName: '',
    phoneNumber: '',
    address: '',
  });

  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e) => {
    const { id, value } = e.target;
    setFormData((prev) => ({ ...prev, [id]: value }));
    if (errors[id]) {
      setErrors((prev) => ({ ...prev, [id]: '' }));
    }
    if (serverError) setServerError('');
  };

  const validate = () => {
    const newErrors = {};

    // Validate Username: 3-30 ký tự, không dấu cách
    if (!formData.username.trim()) {
      newErrors.username = 'Vui lòng nhập tên đăng nhập';
    } else if (!/^[a-zA-Z0-9][a-zA-Z0-9._-]{2,29}$/.test(formData.username.trim())) {
      newErrors.username = 'Tên đăng nhập từ 3-30 ký tự, không chứa khoảng trắng hoặc ký tự đặc biệt';
    }

    // Validate Email
    if (!formData.email.trim()) {
      newErrors.email = 'Vui lòng nhập địa chỉ email';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email.trim())) {
      newErrors.email = 'Định dạng email không hợp lệ (ví dụ: user@example.com)';
    }

    // Validate Password
    if (!formData.password) {
      newErrors.password = 'Vui lòng nhập mật khẩu';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Mật khẩu phải có ít nhất 6 ký tự';
    }

    // Validate Full Name
    if (!formData.fullName.trim()) {
      newErrors.fullName = 'Vui lòng nhập họ và tên';
    }

    // Validate Phone Number (Optional, nhưng nếu nhập thì phải chuẩn 10 số VN)
    if (formData.phoneNumber.trim()) {
      const cleanPhone = formData.phoneNumber.replace(/[\s-]+/g, '');
      if (!/^(?:\+84|0)(?:3[2-9]|5[25689]|7[06-9]|8[1-9]|9[0-9])[0-9]{7}$/.test(cleanPhone)) {
        newErrors.phoneNumber = 'Số điện thoại không hợp lệ (yêu cầu số di động 10 số, ví dụ: 0912345678)';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setIsLoading(true);
    setServerError('');

    try {
      await register({
        username: formData.username.trim(),
        email: formData.email.trim(),
        password: formData.password,
        fullName: formData.fullName.trim(),
        phoneNumber: formData.phoneNumber.trim() || null,
        address: formData.address.trim() || null,
      });
      navigate('/');
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Đăng ký thất bại. Vui lòng thử lại.';
      setServerError(msg);
      // Nếu có field errors từ Spring Boot Validation
      if (err.response?.data?.data && typeof err.response.data.data === 'object') {
        setErrors(err.response.data.data);
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthLayout>
      <div className="w-full">
        {/* Title & Subtitle cùng phong cách */}
        <div className="mb-6">
          <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight uppercase font-sans">
            CREATE ACCOUNT
          </h1>
          <p className="text-slate-500 text-sm mt-1.5 font-normal">
            Bắt đầu trải nghiệm mua sắm công nghệ đỉnh cao cùng Teu Mobile.
          </p>
        </div>

        {/* Thông báo lỗi */}
        {serverError && (
          <div className="mb-5 p-4 rounded-xl bg-red-50 border border-red-200 text-red-600 text-sm flex items-start gap-2.5">
            <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5 text-red-500" />
            <span>{serverError}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            id="fullName"
            label="Họ và tên"
            type="text"
            placeholder="Ví dụ: Nguyễn Văn A"
            value={formData.fullName}
            onChange={handleChange}
            error={errors.fullName}
            required
          />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
            <Input
              id="username"
              label="Tên đăng nhập"
              type="text"
              placeholder="nguyenvana"
              value={formData.username}
              onChange={handleChange}
              error={errors.username}
              required
            />

            <Input
              id="phoneNumber"
              label="Số điện thoại"
              type="tel"
              placeholder="0912 345 678"
              value={formData.phoneNumber}
              onChange={handleChange}
              error={errors.phoneNumber}
            />
          </div>

          <Input
            id="email"
            label="Email"
            type="email"
            placeholder="Enter your email"
            value={formData.email}
            onChange={handleChange}
            error={errors.email}
            required
          />

          <Input
            id="password"
            label="Mật khẩu"
            type="password"
            placeholder="Tối thiểu 6 ký tự"
            value={formData.password}
            onChange={handleChange}
            error={errors.password}
            required
          />

          {/* Nút hành động chính */}
          <div className="pt-2">
            <Button
              type="submit"
              variant="primary"
              size="lg"
              className="w-full"
              isLoading={isLoading}
            >
              Tạo tài khoản
            </Button>
          </div>
        </form>

        {/* Footer chuyển sang Đăng nhập */}
        <p className="mt-6 text-center text-xs text-slate-500 font-normal">
          Already have an account?{' '}
          <Link
            to="/login"
            className="text-primary hover:underline font-semibold transition-colors"
          >
            Sign in here!
          </Link>
        </p>
      </div>
    </AuthLayout>
  );
}
