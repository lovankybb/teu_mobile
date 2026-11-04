import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Smartphone, Search, ShoppingBag, User, LogOut, Settings } from 'lucide-react';
import Button from '../ui/Button';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    setDropdownOpen(false);
    navigate('/login');
  };

  return (
    <header className="sticky top-0 z-50 bg-white/90 backdrop-blur-md border-b border-slate-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16 sm:h-20 gap-4">
          {/* Logo Brand */}
          <Link to="/" className="flex items-center gap-2 group flex-shrink-0">
            <div className="w-10 h-10 rounded-xl bg-primary text-white flex items-center justify-center font-black shadow-md shadow-red-500/25 group-hover:scale-105 transition-transform">
              <Smartphone className="w-5 h-5" />
            </div>
            <span className="text-xl sm:text-2xl font-extrabold tracking-tight text-slate-900">
              TEU<span className="text-primary">.MOBILE</span>
            </span>
          </Link>

          {/* Search bar */}
          <div className="hidden md:flex flex-1 max-w-md mx-4">
            <div className="relative w-full">
              <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
              <input
                type="text"
                placeholder="Tìm iPhone 16 Pro Max, Samsung S24 Ultra, tai nghe..."
                className="w-full pl-10 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-800 placeholder:text-slate-400 focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all"
              />
            </div>
          </div>

          {/* Navigation Links */}
          <nav className="hidden lg:flex items-center gap-6 text-sm font-semibold text-slate-700">
            <Link to="/" className="hover:text-primary transition-colors">Điện thoại</Link>
            <Link to="/" className="hover:text-primary transition-colors">Phụ kiện</Link>
            <Link to="/" className="hover:text-primary transition-colors">Khuyến mãi <span className="px-1.5 py-0.5 rounded-full text-[10px] bg-red-100 text-primary font-bold">HOT</span></Link>
          </nav>

          {/* Right Action Icons & User Profile */}
          <div className="flex items-center gap-3">
            {/* Giỏ hàng */}
            <button className="relative p-2.5 rounded-xl text-slate-700 hover:bg-slate-100 transition-colors">
              <ShoppingBag className="w-5 h-5" />
              <span className="absolute top-1 right-1 w-4 h-4 rounded-full bg-primary text-white text-[10px] font-bold flex items-center justify-center">
                0
              </span>
            </button>

            {/* Trạng thái xác thực */}
            {isAuthenticated ? (
              <div className="relative">
                <button
                  onClick={() => setDropdownOpen(!dropdownOpen)}
                  className="flex items-center gap-2 p-1.5 pl-2.5 rounded-xl border border-slate-200 hover:border-slate-300 transition-colors"
                >
                  <span className="text-xs font-bold text-slate-800 max-w-[100px] truncate hidden sm:inline">
                    {user?.fullName || user?.username}
                  </span>
                  <div className="w-7 h-7 rounded-lg bg-primary/10 text-primary font-bold text-xs flex items-center justify-center">
                    {user?.fullName ? user.fullName[0].toUpperCase() : 'U'}
                  </div>
                </button>

                {/* Dropdown User Menu */}
                {dropdownOpen && (
                  <div className="absolute right-0 mt-2 w-56 rounded-2xl bg-white border border-slate-100 shadow-xl py-2 z-50 animate-in fade-in slide-in-from-top-2 duration-150">
                    <div className="px-4 py-2 border-b border-slate-100">
                      <p className="text-xs font-medium text-slate-400">Đã đăng nhập với</p>
                      <p className="text-sm font-bold text-slate-800 truncate">{user?.email}</p>
                    </div>

                    <Link
                      to="/profile"
                      onClick={() => setDropdownOpen(false)}
                      className="flex items-center gap-2.5 px-4 py-2.5 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition-colors"
                    >
                      <User className="w-4 h-4 text-slate-400" />
                      Hồ sơ cá nhân
                    </Link>

                    <button
                      onClick={handleLogout}
                      className="w-full flex items-center gap-2.5 px-4 py-2.5 text-xs font-semibold text-red-600 hover:bg-red-50 transition-colors text-left"
                    >
                      <LogOut className="w-4 h-4 text-red-500" />
                      Đăng xuất
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link to="/login">
                  <Button variant="ghost" size="sm">
                    Đăng nhập
                  </Button>
                </Link>
                <Link to="/register">
                  <Button variant="primary" size="sm">
                    Đăng ký
                  </Button>
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}
