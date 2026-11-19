import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import api from "../../services/api";
import {
  Users,
  Package,
  ShoppingBag,
  TrendingUp,
  ArrowRight,
  ShieldCheck,
  Server,
  Database,
  KeyRound,
  CheckCircle2,
  Clock,
} from "lucide-react";
import Button from "../../components/ui/Button";

export default function AdminDashboardPage() {
  const { user } = useAuth();
  const [serverHealth, setServerHealth] = useState({ status: "checking", message: "Đang kiểm tra..." });

  useEffect(() => {
    let isMounted = true;
    api
      .get("/public/health")
      .then((res) => {
        if (isMounted) {
          setServerHealth({
            status: "online",
            message: res.data?.message || "Máy chủ REST API hoạt động bình thường",
          });
        }
      })
      .catch((err) => {
        if (isMounted) {
          setServerHealth({
            status: "offline",
            message: "Không thể kết nối đến Backend: " + (err.message || "Lỗi mạng"),
          });
        }
      });

    return () => {
      isMounted = false;
    };
  }, []);

  return (
    <div className="space-y-6 sm:space-y-8 animate-in fade-in duration-200">
      {/* ─── Hero Welcome Banner ────────────────────────────────────────── */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-slate-900 via-slate-800 to-slate-900 text-white p-6 sm:p-8 shadow-xl">
        <div className="relative z-10 max-w-2xl">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 backdrop-blur-md text-xs font-semibold text-red-200 mb-4 border border-white/10">
            <ShieldCheck className="w-4 h-4 text-primary" />
            <span>Khu vực Quản trị Cấp cao</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-black tracking-tight text-white mb-2">
            Xin chào, {user?.fullName || user?.username}! 👋
          </h1>
          <p className="text-sm text-slate-300 leading-relaxed">
            Hệ thống quản trị <strong>Teu Mobile</strong> đã được thiết lập thành công. Bắt đầu với phân hệ{" "}
            <strong>Quản trị Người dùng</strong> để quản lý tài khoản, vai trò và bảo mật.
          </p>
        </div>

        {/* Decorative background glow */}
        <div className="absolute right-0 top-0 -mt-10 -mr-10 w-72 h-72 rounded-full bg-primary/20 blur-3xl pointer-events-none" />
      </div>

      {/* ─── Quick Stats Grid ───────────────────────────────────────────── */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        {/* Card 1: Người dùng */}
        <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-sm hover:shadow-md transition-shadow">
          <div className="flex items-center justify-between mb-4">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">
              Khách hàng & User
            </span>
            <div className="w-10 h-10 rounded-xl bg-red-50 text-primary flex items-center justify-center">
              <Users className="w-5 h-5" />
            </div>
          </div>
          <div className="flex items-baseline justify-between">
            <div>
              <span className="text-2xl font-black text-slate-900">Module Sẵn Sàng</span>
              <p className="text-xs text-slate-500 mt-1">Đang triển khai chi tiết</p>
            </div>
          </div>
          <Link
            to="/admin/users"
            className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs font-bold text-primary hover:text-primary-dark transition-colors"
          >
            <span>Vào quản lý người dùng</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        {/* Card 2: Sản phẩm */}
        <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-sm opacity-80">
          <div className="flex items-center justify-between mb-4">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">
              Sản phẩm
            </span>
            <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center">
              <Package className="w-5 h-5" />
            </div>
          </div>
          <div className="flex items-baseline justify-between">
            <div>
              <span className="text-2xl font-black text-slate-900">Flagship & PK</span>
              <p className="text-xs text-slate-500 mt-1">Giai đoạn kế tiếp</p>
            </div>
          </div>
          <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs font-medium text-slate-400">
            <span>Sắp ra mắt</span>
            <Clock className="w-3.5 h-3.5" />
          </div>
        </div>

        {/* Card 3: Đơn hàng */}
        <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-sm opacity-80">
          <div className="flex items-center justify-between mb-4">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">
              Đơn hàng
            </span>
            <div className="w-10 h-10 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center">
              <ShoppingBag className="w-5 h-5" />
            </div>
          </div>
          <div className="flex items-baseline justify-between">
            <div>
              <span className="text-2xl font-black text-slate-900">Checkout & Order</span>
              <p className="text-xs text-slate-500 mt-1">Giai đoạn kế tiếp</p>
            </div>
          </div>
          <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs font-medium text-slate-400">
            <span>Sắp ra mắt</span>
            <Clock className="w-3.5 h-3.5" />
          </div>
        </div>

        {/* Card 4: Doanh thu */}
        <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-sm opacity-80">
          <div className="flex items-center justify-between mb-4">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">
              Doanh thu
            </span>
            <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <TrendingUp className="w-5 h-5" />
            </div>
          </div>
          <div className="flex items-baseline justify-between">
            <div>
              <span className="text-2xl font-black text-slate-900">Báo cáo & Thống kê</span>
              <p className="text-xs text-slate-500 mt-1">Tích hợp VNPay / COD</p>
            </div>
          </div>
          <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs font-medium text-slate-400">
            <span>Sắp ra mắt</span>
            <Clock className="w-3.5 h-3.5" />
          </div>
        </div>
      </div>

      {/* ─── Featured Action Banner: Quản trị Người Dùng ────────────────── */}
      <div className="bg-white rounded-2xl border border-slate-200/80 p-6 sm:p-8 shadow-sm flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
        <div className="max-w-xl">
          <div className="inline-flex items-center gap-2 px-2.5 py-1 rounded-lg bg-red-50 text-primary text-xs font-bold mb-3">
            <Users className="w-3.5 h-3.5" />
            <span>Mục tiêu trọng tâm: Module 1</span>
          </div>
          <h2 className="text-xl sm:text-2xl font-extrabold text-slate-900 mb-2">
            Quản trị Người Dùng & Sổ Địa Chỉ
          </h2>
          <p className="text-sm text-slate-600 leading-relaxed">
            Kiểm soát danh sách tài khoản khách hàng, khóa/mở khóa đăng nhập, phân quyền Quản trị viên
            và tra cứu địa chỉ nhận hàng đồng bộ trực tiếp với máy chủ Keycloak và SQL Server.
          </p>
        </div>

        <Link to="/admin/users" className="w-full md:w-auto">
          <Button variant="primary" size="lg" className="w-full md:w-auto justify-center font-bold">
            <Users className="w-4 h-4 mr-2" />
            Quản Lý Danh Sách Người Dùng
            <ArrowRight className="w-4 h-4 ml-2" />
          </Button>
        </Link>
      </div>

      {/* ─── System Infrastructure & Health Status ─────────────────────── */}
      <div className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-sm font-bold text-slate-900 uppercase tracking-wider flex items-center gap-2">
            <Server className="w-4 h-4 text-slate-500" />
            Trạng Thái Hạ Tầng Kỹ Thuật (Teu Mobile Stack)
          </h3>
          <div className="flex items-center gap-2">
            <div
              className={`w-2.5 h-2.5 rounded-full ${
                serverHealth.status === "online"
                  ? "bg-emerald-500 animate-pulse"
                  : serverHealth.status === "checking"
                  ? "bg-amber-500"
                  : "bg-red-500"
              }`}
            />
            <span
              className={`text-xs font-bold uppercase ${
                serverHealth.status === "online"
                  ? "text-emerald-600"
                  : serverHealth.status === "checking"
                  ? "text-amber-600"
                  : "text-red-600"
              }`}
            >
              {serverHealth.status}
            </span>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-2">
          <div className="p-4 rounded-xl bg-slate-50 border border-slate-100 flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-emerald-100 text-emerald-700 flex items-center justify-center flex-shrink-0">
              <CheckCircle2 className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs font-bold text-slate-800">Spring Boot 3.3.5</p>
              <p className="text-[11px] text-slate-500">REST API & OAuth2 Resource Server</p>
            </div>
          </div>

          <div className="p-4 rounded-xl bg-slate-50 border border-slate-100 flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-indigo-100 text-indigo-700 flex items-center justify-center flex-shrink-0">
              <KeyRound className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs font-bold text-slate-800">Keycloak 26 (Quarkus)</p>
              <p className="text-[11px] text-slate-500">Authorization Code PKCE & Roles</p>
            </div>
          </div>

          <div className="p-4 rounded-xl bg-slate-50 border border-slate-100 flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-blue-100 text-blue-700 flex items-center justify-center flex-shrink-0">
              <Database className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs font-bold text-slate-800">SQL Server 2022</p>
              <p className="text-[11px] text-slate-500">Flyway Migration (V1, V2 Schema)</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
