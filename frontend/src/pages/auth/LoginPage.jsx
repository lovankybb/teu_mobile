import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Loader2 } from 'lucide-react';

/**
 * LoginPage: Tự động chuyển hướng (Auto-redirect) sang trang đăng nhập của Keycloak.
 * - Nếu người dùng đã đăng nhập: Chuyển về trang chủ.
 * - Nếu chưa đăng nhập: Gọi keycloak.login() ngay lập tức, loại bỏ thao tác bấm nút trung gian.
 */
export default function LoginPage() {
  const { isAuthenticated, loading, login } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (loading) return;

    if (isAuthenticated) {
      navigate('/', { replace: true });
    } else {
      login();
    }
  }, [isAuthenticated, loading, login, navigate]);

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col items-center justify-center p-4">
      <div className="flex flex-col items-center gap-4 text-center">
        <Loader2 className="w-10 h-10 text-primary animate-spin" />
        <p className="text-slate-600 text-sm font-medium">
          Đang chuyển hướng đến trang đăng nhập bảo mật...
        </p>
      </div>
    </div>
  );
}
