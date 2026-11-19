import React from "react";
import { Link, Outlet } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { ShieldAlert, ShieldCheck, LogIn, ArrowLeft, RefreshCw } from "lucide-react";
import Button from "../ui/Button";

/**
 * AdminRoute
 * HOC bảo vệ các route dành riêng cho Quản Trị Viên (ROLE_ADMIN).
 * Kiểm tra trạng thái xác thực và quyền hạn từ Keycloak.
 */
export default function AdminRoute() {
  const { isAuthenticated, isAdmin, loading, login, logout, user, roles } = useAuth();

  // 1. Trạng thái đang khởi tạo Keycloak
  if (loading) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-slate-50 px-4">
        <div className="relative flex items-center justify-center mb-4">
          <div className="w-16 h-16 rounded-2xl bg-red-100 animate-ping absolute opacity-75" />
          <div className="w-16 h-16 rounded-2xl bg-white shadow-lg border border-slate-100 flex items-center justify-center relative">
            <RefreshCw className="w-8 h-8 text-primary animate-spin" />
          </div>
        </div>
        <h3 className="text-base font-bold text-slate-800 mb-1">Đang kiểm tra quyền truy cập...</h3>
        <p className="text-xs text-slate-500">Hệ thống đang xác thực danh tính từ Keycloak</p>
      </div>
    );
  }

  // 2. Chưa đăng nhập
  if (!isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50 p-4">
        <div className="max-w-md w-full bg-white rounded-2xl shadow-xl border border-slate-100 p-8 text-center animate-in fade-in duration-200">
          <div className="w-16 h-16 mx-auto rounded-2xl bg-red-50 text-primary flex items-center justify-center mb-6">
            <ShieldCheck className="w-8 h-8" />
          </div>
          <h2 className="text-xl font-extrabold text-slate-900 mb-2">Yêu Cầu Đăng Nhập</h2>
          <p className="text-sm text-slate-600 mb-6 leading-relaxed">
            Khu vực Quản trị (Admin Portal) chỉ dành cho người dùng được cấp quyền. Vui lòng đăng nhập với tài khoản có quyền Quản trị viên để tiếp tục.
          </p>
          <div className="space-y-3">
            <Button
              variant="primary"
              className="w-full justify-center py-2.5"
              onClick={() => login(window.location.pathname)}
            >
              <LogIn className="w-4 h-4 mr-2" />
              Đăng nhập Quản trị viên
            </Button>
            <Link to="/" className="block">
              <Button variant="outline" className="w-full justify-center py-2.5">
                <ArrowLeft className="w-4 h-4 mr-2" />
                Quay về Trang chủ
              </Button>
            </Link>
          </div>
        </div>
      </div>
    );
  }

  // 3. Đã đăng nhập nhưng không có quyền ADMIN (403 Forbidden)
  if (!isAdmin) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50 p-4">
        <div className="max-w-lg w-full bg-white rounded-2xl shadow-xl border border-slate-100 p-8 text-center animate-in fade-in duration-200">
          <div className="w-16 h-16 mx-auto rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center mb-6">
            <ShieldAlert className="w-8 h-8" />
          </div>
          <span className="inline-block px-3 py-1 bg-amber-100 text-amber-700 text-xs font-bold rounded-full mb-3">
            403 - ACCESS FORBIDDEN
          </span>
          <h2 className="text-2xl font-black text-slate-900 mb-2">Quyền Truy Cập Bị Từ Chối</h2>
          <p className="text-sm text-slate-600 mb-4 leading-relaxed">
            Tài khoản <span className="font-semibold text-slate-900">{user?.fullName || user?.username}</span> (
            <span className="text-slate-700 font-mono text-xs">{user?.email}</span>) không có quyền <strong>Quản trị viên (ROLE_ADMIN)</strong> để truy cập khu vực này.
          </p>

          {roles && roles.length > 0 && (
            <div className="mb-6 p-3 bg-slate-50 rounded-xl border border-slate-100 text-left">
              <p className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider mb-1.5">
                Các vai trò hiện tại của bạn:
              </p>
              <div className="flex flex-wrap gap-1.5">
                {roles.map((role) => (
                  <span
                    key={role}
                    className="px-2 py-0.5 bg-slate-200/80 text-slate-700 font-mono text-[11px] rounded-md"
                  >
                    {role}
                  </span>
                ))}
              </div>
            </div>
          )}

          <div className="flex flex-col sm:flex-row items-center gap-3">
            <Link to="/" className="w-full sm:w-1/2">
              <Button variant="outline" className="w-full justify-center">
                <ArrowLeft className="w-4 h-4 mr-2" />
                Về Trang chủ
              </Button>
            </Link>
            <Button
              variant="primary"
              className="w-full sm:w-1/2 justify-center"
              onClick={() => logout()}
            >
              Đổi tài khoản khác
            </Button>
          </div>
        </div>
      </div>
    );
  }

  // 4. Có quyền ADMIN -> Render nội dung bên trong
  return <Outlet />;
}
