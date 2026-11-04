import React from 'react';
import { Link } from 'react-router-dom';
import { Smartphone, Zap, ShieldCheck } from 'lucide-react';

export default function AuthLayout({ children }) {
  return (
    <div className="min-h-screen w-full flex bg-white font-sans antialiased">
      {/* CỘT TRÁI: FORM PANEL (Chiếm 45% - 50% trên Desktop, 100% trên Mobile) */}
      <div className="w-full lg:w-1/2 flex flex-col justify-between px-6 sm:px-12 lg:px-20 py-8 lg:py-12 overflow-y-auto">
        {/* Brand Header */}
        <div className="flex items-center justify-between mb-8">
          <Link to="/" className="flex items-center gap-2 group">
            <div className="w-9 h-9 rounded-xl bg-primary text-white flex items-center justify-center font-black shadow-md shadow-red-500/25 group-hover:scale-105 transition-transform">
              <Smartphone className="w-5 h-5" />
            </div>
            <span className="text-xl font-extrabold tracking-tight text-slate-900">
              TEU<span className="text-primary">.MOBILE</span>
            </span>
          </Link>
          <Link
            to="/"
            className="text-xs font-semibold text-slate-500 hover:text-primary transition-colors"
          >
            Về trang chủ →
          </Link>
        </div>

        {/* Nội dung Form chính */}
        <div className="w-full max-w-[420px] mx-auto my-auto">
          {children}
        </div>

        {/* Footer bảo đảm */}
        <div className="mt-8 pt-6 border-t border-slate-100 flex items-center justify-between text-xs text-slate-400">
          <div className="flex items-center gap-1.5">
            <ShieldCheck className="w-4 h-4 text-emerald-500" />
            <span>Bảo mật chuẩn Keycloak OIDC</span>
          </div>
          <div className="flex items-center gap-1.5">
            <Zap className="w-4 h-4 text-amber-500" />
            <span>Đồng bộ tức thì</span>
          </div>
        </div>
      </div>

      {/* CỘT PHẢI: HERO VISUAL PANEL (Tái hiện Artwork thể thao & công nghệ từ Figma) */}
      <div className="hidden lg:flex lg:w-1/2 relative bg-gradient-to-br from-slate-100 via-slate-200 to-slate-300 items-center justify-center overflow-hidden p-8">
        {/* Họa tiết khói nghệ thuật nền SVG (Dynamic Smoke Waves) */}
        <svg
          className="absolute inset-0 w-full h-full object-cover opacity-80 pointer-events-none"
          viewBox="0 0 800 800"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <g filter="url(#smokeFilter)">
            <path
              d="M100 200 C 250 80, 450 150, 600 80 C 750 20, 850 160, 780 320 C 700 480, 750 650, 620 720 C 480 800, 320 680, 180 720 C 50 760, -20 600, 20 450 C 60 300, -50 320, 100 200 Z"
              fill="white"
              fillOpacity="0.45"
            />
            <path
              d="M200 300 C 350 180, 520 250, 680 180 C 820 120, 900 280, 840 440 C 780 600, 720 720, 560 760 C 400 800, 250 720, 160 620 C 80 500, 50 420, 200 300 Z"
              fill="white"
              fillOpacity="0.35"
            />
          </g>
          <defs>
            <filter id="smokeFilter" x="-50" y="-50" width="900" height="900" filterUnits="userSpaceOnUse">
              <feGaussianBlur stdDeviation="30" />
            </filter>
          </defs>
        </svg>

        {/* Minh họa nhân vật thể thao / công nghệ cao với tone Đen - Trắng - Đỏ Coral */}
        <div className="relative z-10 w-full max-w-lg flex flex-col items-center">
          <div className="relative group">
            {/* Hiệu ứng hào quang đỏ san hô phía sau */}
            <div className="absolute -inset-4 bg-primary/20 rounded-full blur-3xl group-hover:bg-primary/30 transition-all duration-500"></div>

            {/* Khung minh họa chính */}
            <div className="relative bg-white/70 backdrop-blur-md border border-white/80 p-8 rounded-3xl shadow-2xl flex flex-col items-center text-center max-w-md">
              {/* Badge thương hiệu công nghệ */}
              <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-slate-900 text-white text-xs font-bold uppercase tracking-wider mb-6">
                <span className="w-2 h-2 rounded-full bg-primary animate-pulse"></span>
                Next-Gen Experience
              </div>

              {/* Artwork SVG minh họa giày thể thao công nghệ & chuyển động dứt khoát */}
              <div className="w-56 h-56 relative my-2 flex items-center justify-center">
                <svg viewBox="0 0 200 200" className="w-full h-full drop-shadow-xl" fill="none">
                  {/* Đường nét khói comic năng động */}
                  <path d="M20 150 Q 50 120 70 140 T 130 130 T 180 160" stroke="#0F172A" strokeWidth="4" strokeLinecap="round" />
                  <path d="M40 165 Q 80 140 110 155 T 170 145" stroke="#EA384D" strokeWidth="3" strokeLinecap="round" />
                  
                  {/* Hình khối giày thể thao công nghệ phong cách line-art */}
                  <path
                    d="M30 140 C 30 110, 60 70, 95 65 C 120 60, 135 75, 145 95 C 160 100, 180 120, 175 145 C 170 155, 150 160, 120 160 C 80 160, 30 155, 30 140 Z"
                    fill="#0F172A"
                  />
                  {/* Đế giày trắng phân khúc như trong ảnh Figma */}
                  <path
                    d="M30 140 L 175 145 L 170 160 L 30 158 Z"
                    fill="#FFFFFF"
                    stroke="#0F172A"
                    strokeWidth="3"
                  />
                  {/* Chi tiết sọc đỏ Coral đặc trưng */}
                  <path d="M140 100 L 150 125 L 165 120 Z" fill="#EA384D" />
                  <rect x="150" y="145" width="15" height="15" rx="3" fill="#EA384D" />
                  <rect x="130" y="145" width="12" height="15" rx="2" fill="#FFFFFF" stroke="#0F172A" strokeWidth="2" />
                  
                  {/* Swoosh / Logo điểm nhấn màu trắng */}
                  <path
                    d="M60 120 Q 95 125 130 105 Q 90 145 60 120 Z"
                    fill="#FFFFFF"
                  />
                </svg>
              </div>

              {/* Slogan định hình phong cách */}
              <h3 className="text-2xl font-black uppercase tracking-tight text-slate-900 mt-4">
                POWER YOUR <span className="text-primary">SPEED</span>
              </h3>
              <p className="text-slate-500 text-sm mt-2 leading-relaxed">
                Đỉnh cao công nghệ di động và phụ kiện đỉnh cao. Trải nghiệm mua sắm mượt mà cùng Teu Mobile.
              </p>

              {/* Thông số ấn tượng */}
              <div className="grid grid-cols-3 gap-4 w-full mt-6 pt-6 border-t border-slate-200/80">
                <div>
                  <div className="text-lg font-bold text-slate-900">100%</div>
                  <div className="text-[11px] text-slate-400 font-medium">Chính hãng</div>
                </div>
                <div>
                  <div className="text-lg font-bold text-primary">24/7</div>
                  <div className="text-[11px] text-slate-400 font-medium">Hỗ trợ</div>
                </div>
                <div>
                  <div className="text-lg font-bold text-slate-900">0%</div>
                  <div className="text-[11px] text-slate-400 font-medium">Trả góp</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
