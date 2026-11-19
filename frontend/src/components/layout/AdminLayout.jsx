import React, { useState } from "react";
import { Link, NavLink, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import {
  LayoutDashboard,
  Users,
  Package,
  ShoppingBag,
  Settings,
  Store,
  LogOut,
  Menu,
  X,
  ChevronRight,
  ShieldCheck,
  Bell,
  Search,
} from "lucide-react";
import logo from "../../assets/logo.png";

export default function AdminLayout() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const navigationItems = [
    {
      name: "Tổng quan",
      path: "/admin/dashboard",
      icon: LayoutDashboard,
      badge: null,
    },
    {
      name: "Quản lý Người dùng",
      path: "/admin/users",
      icon: Users,
      badge: "Ưu tiên",
      badgeColor: "bg-red-100 text-primary",
    },
    {
      name: "Quản lý Sản phẩm",
      path: "/admin/products",
      icon: Package,
      badge: "Sắp có",
      badgeColor: "bg-slate-100 text-slate-500",
      disabled: true,
    },
    {
      name: "Quản lý Đơn hàng",
      path: "/admin/orders",
      icon: ShoppingBag,
      badge: "Sắp có",
      badgeColor: "bg-slate-100 text-slate-500",
      disabled: true,
    },
    {
      name: "Cài đặt Hệ thống",
      path: "/admin/settings",
      icon: Settings,
      badge: "Sắp có",
      badgeColor: "bg-slate-100 text-slate-500",
      disabled: true,
    },
  ];

  // Helper để lấy tên trang hiện tại hiển thị trên Breadcrumb
  const getCurrentPageTitle = () => {
    if (location.pathname.startsWith("/admin/users")) return "Quản lý Người dùng";
    if (location.pathname.startsWith("/admin/products")) return "Quản lý Sản phẩm";
    if (location.pathname.startsWith("/admin/orders")) return "Quản lý Đơn hàng";
    if (location.pathname.startsWith("/admin/settings")) return "Cài đặt Hệ thống";
    return "Bảng Điều Khiển Tổng Quan";
  };

  return (
    <div className="min-h-screen bg-slate-50 flex">
      {/* ─── Backdrop cho Mobile Drawer ──────────────────────────────── */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/40 backdrop-blur-sm lg:hidden transition-opacity"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* ─── Left Sidebar Navigation ──────────────────────────────────── */}
      <aside
        className={`fixed inset-y-0 left-0 z-50 w-72 bg-white border-r border-slate-200/80 flex flex-col transition-transform duration-300 ease-in-out lg:static lg:translate-x-0 ${
          sidebarOpen ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        {/* Brand Header */}
        <div className="h-20 px-6 flex items-center justify-between border-b border-slate-100">
          <Link to="/admin/dashboard" className="flex items-center gap-3 group">
            <img
              src={logo}
              alt="TEU Mobile"
              className="h-10 w-auto object-contain group-hover:scale-105 transition-transform"
            />
            <div className="flex flex-col">
              <div className="flex items-center gap-1.5">
                <span className="font-extrabold text-xl text-primary tracking-tight">TẾU</span>
                <span className="font-light text-slate-700 text-lg">Mobile</span>
              </div>
              <span className="text-[10px] font-bold uppercase tracking-wider text-primary bg-red-50 px-1.5 py-0.5 rounded-md self-start mt-0.5">
                Admin Portal
              </span>
            </div>
          </Link>

          <button
            onClick={() => setSidebarOpen(false)}
            className="p-1.5 text-slate-400 hover:text-slate-600 rounded-lg lg:hidden"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation Links */}
        <div className="flex-1 overflow-y-auto px-4 py-6 space-y-1.5">
          <p className="px-3 text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">
            Quản trị hệ thống
          </p>

          {navigationItems.map((item) => {
            const Icon = item.icon;
            if (item.disabled) {
              return (
                <div
                  key={item.name}
                  className="flex items-center justify-between px-3.5 py-2.5 rounded-xl text-sm font-medium text-slate-400 opacity-60 cursor-not-allowed select-none"
                >
                  <div className="flex items-center gap-3">
                    <Icon className="w-4 h-4 text-slate-400" />
                    <span>{item.name}</span>
                  </div>
                  {item.badge && (
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${item.badgeColor}`}>
                      {item.badge}
                    </span>
                  )}
                </div>
              );
            }

            return (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={() => setSidebarOpen(false)}
                className={({ isActive }) =>
                  `flex items-center justify-between px-3.5 py-2.5 rounded-xl text-sm font-semibold transition-all duration-150 ${
                    isActive
                      ? "bg-primary text-white shadow-sm shadow-primary/25"
                      : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"
                  }`
                }
              >
                {({ isActive }) => (
                  <>
                    <div className="flex items-center gap-3">
                      <Icon className={`w-4 h-4 ${isActive ? "text-white" : "text-slate-500"}`} />
                      <span>{item.name}</span>
                    </div>
                    {item.badge && !isActive && (
                      <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${item.badgeColor}`}>
                        {item.badge}
                      </span>
                    )}
                  </>
                )}
              </NavLink>
            );
          })}
        </div>

        {/* Sidebar Footer */}
        <div className="p-4 border-t border-slate-100 space-y-2">
          <Link
            to="/"
            className="flex items-center gap-2.5 px-3.5 py-2.5 rounded-xl text-xs font-semibold text-slate-600 hover:bg-slate-100 hover:text-slate-900 transition-colors"
          >
            <Store className="w-4 h-4 text-slate-500" />
            <span>Xem trang cửa hàng</span>
          </Link>

          <button
            onClick={() => logout()}
            className="w-full flex items-center gap-2.5 px-3.5 py-2.5 rounded-xl text-xs font-semibold text-red-600 hover:bg-red-50 transition-colors"
          >
            <LogOut className="w-4 h-4 text-red-500" />
            <span>Đăng xuất</span>
          </button>
        </div>
      </aside>

      {/* ─── Main Content Canvas ──────────────────────────────────────── */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Top Header */}
        <header className="h-20 bg-white border-b border-slate-200/80 sticky top-0 z-30 px-4 sm:px-6 lg:px-8 flex items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <button
              onClick={() => setSidebarOpen(true)}
              className="p-2 -ml-2 text-slate-600 hover:bg-slate-100 rounded-xl lg:hidden"
            >
              <Menu className="w-5 h-5" />
            </button>

            {/* Breadcrumbs */}
            <div className="hidden sm:flex items-center gap-2 text-xs font-medium text-slate-400">
              <Link to="/admin/dashboard" className="hover:text-slate-700 transition-colors">
                Admin
              </Link>
              <ChevronRight className="w-3.5 h-3.5" />
              <span className="text-slate-800 font-semibold">{getCurrentPageTitle()}</span>
            </div>
          </div>

          {/* Right Header: Search & Admin User Chip */}
          <div className="flex items-center gap-3 sm:gap-4">
            <Link
              to="/"
              className="hidden md:inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold text-slate-600 bg-slate-100 hover:bg-slate-200/80 transition-colors"
            >
              <Store className="w-3.5 h-3.5 text-slate-500" />
              <span>Cửa hàng</span>
            </Link>

            {/* Admin Profile Pill */}
            <div className="flex items-center gap-3 pl-3 border-l border-slate-200">
              <div className="text-right hidden sm:block">
                <p className="text-xs font-bold text-slate-900 leading-tight">
                  {user?.fullName || user?.username}
                </p>
                <div className="flex items-center justify-end gap-1 mt-0.5">
                  <ShieldCheck className="w-3 h-3 text-primary" />
                  <span className="text-[10px] font-bold text-primary uppercase">
                    Quản trị viên
                  </span>
                </div>
              </div>

              {user?.avatarUrl ? (
                <img
                  src={user.avatarUrl}
                  alt={user?.fullName || "Admin"}
                  referrerPolicy="no-referrer"
                  className="w-9 h-9 rounded-xl object-cover ring-2 ring-primary/20"
                />
              ) : (
                <div className="w-9 h-9 rounded-xl bg-primary text-white font-black text-xs flex items-center justify-center shadow-sm">
                  {user?.fullName ? user.fullName[0].toUpperCase() : "A"}
                </div>
              )}
            </div>
          </div>
        </header>

        {/* Page Content Body */}
        <main className="flex-1 p-4 sm:p-6 lg:p-8 max-w-7xl mx-auto w-full">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
