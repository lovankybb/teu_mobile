import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import AuthLayout from '../../components/layout/AuthLayout';
import Button from '../../components/ui/Button';
import { useAuth } from '../../context/AuthContext';
import { ArrowRight, ShieldCheck } from 'lucide-react';

// ─── Google Icon (multi-color SVG) ───────────────────────────────────────────
function GoogleIcon() {
  return (
    <svg className="w-5 h-5 flex-shrink-0" viewBox="0 0 24 24" aria-hidden="true">
      <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
      <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
      <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" />
      <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" />
    </svg>
  );
}

// ─── Divider ─────────────────────────────────────────────────────────────────
function OrDivider() {
  return (
    <div className="relative my-1">
      <div className="absolute inset-0 flex items-center">
        <div className="w-full border-t border-slate-200" />
      </div>
      <div className="relative flex justify-center">
        <span className="bg-white px-3 text-xs text-slate-400 font-medium tracking-wide">
          HOẶC
        </span>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────────────────────
export default function LoginPage() {
  const { login } = useAuth();
  const [isLoading, setIsLoading] = useState(false);

  // Trigger redirect sang trang login Keycloak (Auth Code + PKCE)
  const handleLogin = () => {
    setIsLoading(true);
    login(); // redirect — trang này sẽ unmount, không cần setIsLoading(false)
  };

  // Đăng nhập Google qua Keycloak Identity Broker
  const handleGoogleLogin = () => {
    setIsLoading(true);
    login(); // Keycloak sẽ hiển thị option Google nếu đã cấu hình IdP
  };

  // ─── Render ───────────────────────────────────────────────────────────────
  return (
    <AuthLayout>
      <div className="w-full">

        {/* ── Title & Subtitle ── */}
        <div className="mb-8">
          <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight uppercase font-sans">
            WELCOME BACK
          </h1>
          <p className="text-slate-500 text-sm mt-2 font-normal leading-relaxed">
            Chào mừng trở lại! Nhấn đăng nhập để tiếp tục.
          </p>
        </div>

        {/* ── Security Badge ── */}
        <div className="mb-6 p-3.5 rounded-xl bg-emerald-50 border border-emerald-100 flex items-center gap-2.5">
          <ShieldCheck className="w-5 h-5 text-emerald-500 flex-shrink-0" />
          <p className="text-xs text-emerald-700 font-medium">
            Đăng nhập bảo mật qua <span className="font-bold">Keycloak</span> với mã hóa PKCE — thông tin của bạn được bảo vệ tuyệt đối.
          </p>
        </div>

        <div className="space-y-3">
          {/* ── Primary CTA — redirect Keycloak ── */}
          <Button
            type="button"
            variant="primary"
            size="lg"
            className="w-full group"
            isLoading={isLoading}
            onClick={handleLogin}
          >
            {!isLoading && (
              <>
                Đăng nhập
                <ArrowRight className="w-4 h-4 ml-1 group-hover:translate-x-0.5 transition-transform" />
              </>
            )}
          </Button>

          {/* ── Divider ── */}
          <OrDivider />

          {/* ── Google Sign-in (Keycloak Identity Broker) ── */}
          <Button
            type="button"
            variant="outline"
            size="lg"
            className="w-full"
            onClick={handleGoogleLogin}
            disabled={isLoading}
          >
            <GoogleIcon />
            <span>Tiếp tục với Google</span>
          </Button>
        </div>

        {/* ── Footer: link tới Register ── */}
        <p className="mt-8 text-center text-xs text-slate-500 font-normal">
          Chưa có tài khoản?{' '}
          <Link
            to="/register"
            className="text-primary hover:underline font-semibold transition-colors"
          >
            Đăng ký miễn phí!
          </Link>
        </p>
      </div>
    </AuthLayout>
  );
}
