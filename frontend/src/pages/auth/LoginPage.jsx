import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../../components/layout/AuthLayout';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import Checkbox from '../../components/ui/Checkbox';
import { useAuth } from '../../context/AuthContext';
import { AlertCircle } from 'lucide-react';

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [formData, setFormData] = useState({
    usernameOrEmail: '',
    password: '',
    rememberMe: false,
  });

  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e) => {
    const { id, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [id]: type === 'checkbox' ? checked : value,
    }));
    // Xóa lỗi khi người dùng bắt đầu sửa
    if (errors[id]) {
      setErrors((prev) => ({ ...prev, [id]: '' }));
    }
    if (serverError) setServerError('');
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.usernameOrEmail.trim()) {
      newErrors.usernameOrEmail = 'Vui lòng nhập email hoặc tên đăng nhập';
    }
    if (!formData.password) {
      newErrors.password = 'Vui lòng nhập mật khẩu';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Mật khẩu phải có tối thiểu 6 ký tự';
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
      await login(formData.usernameOrEmail.trim(), formData.password);
      navigate('/');
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Đăng nhập thất bại. Vui lòng thử lại.';
      setServerError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthLayout>
      <div className="w-full">
        {/* Title & Subtitle chính xác theo bản vẽ Figma */}
        <div className="mb-8">
          <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight uppercase font-sans">
            WELCOME BACK
          </h1>
          <p className="text-slate-500 text-sm mt-1.5 font-normal">
            Welcome back! Please enter your details.
          </p>
        </div>

        {/* Thông báo lỗi từ Server */}
        {serverError && (
          <div className="mb-6 p-4 rounded-xl bg-red-50 border border-red-200 text-red-600 text-sm flex items-start gap-2.5">
            <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5 text-red-500" />
            <span>{serverError}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Email / Username field */}
          <Input
            id="usernameOrEmail"
            label="Email"
            type="text"
            placeholder="Enter your email"
            value={formData.usernameOrEmail}
            onChange={handleChange}
            error={errors.usernameOrEmail}
            autoComplete="username"
            required
          />

          {/* Password field */}
          <Input
            id="password"
            label="Password"
            type="password"
            placeholder="***********"
            value={formData.password}
            onChange={handleChange}
            error={errors.password}
            autoComplete="current-password"
            required
          />

          {/* Remember me & Forgot password row */}
          <div className="flex items-center justify-between pt-1">
            <Checkbox
              id="rememberMe"
              label="Remember me"
              checked={formData.rememberMe}
              onChange={handleChange}
            />

            <Link
              to="/forgot-password"
              className="text-xs font-semibold text-slate-900 hover:text-primary transition-colors"
            >
              Forgot password
            </Link>
          </div>

          {/* Primary CTA Button: Sign in */}
          <div className="pt-2">
            <Button
              type="submit"
              variant="primary"
              size="lg"
              className="w-full"
              isLoading={isLoading}
            >
              Sign in
            </Button>
          </div>

          {/* Secondary CTA: Sign in with Google */}
          <div>
            <Button
              type="button"
              variant="outline"
              size="lg"
              className="w-full relative"
              onClick={() => alert('Chức năng đăng nhập Google đang được tích hợp qua Keycloak!')}
            >
              {/* Google Multi-color Logo SVG */}
              <svg className="w-5 h-5 mr-1" viewBox="0 0 24 24">
                <path
                  fill="#4285F4"
                  d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                />
                <path
                  fill="#34A853"
                  d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                />
                <path
                  fill="#FBBC05"
                  d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
                />
                <path
                  fill="#EA4335"
                  d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
                />
              </svg>
              <span>Sign in with Google</span>
            </Button>
          </div>
        </form>

        {/* Footer chuyển sang Đăng ký */}
        <p className="mt-8 text-center text-xs text-slate-500 font-normal">
          Don't have an account?{' '}
          <Link
            to="/register"
            className="text-primary hover:underline font-semibold transition-colors"
          >
            Sign up for free!
          </Link>
        </p>
      </div>
    </AuthLayout>
  );
}
