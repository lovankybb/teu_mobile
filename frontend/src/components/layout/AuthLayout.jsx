import React from "react";
import { Link } from "react-router-dom";
import { ShieldCheck, Zap, Smartphone } from "lucide-react";
import logo from "../../assets/logo.png";

// ─── Animated smoke wave background (SVG) ────────────────────────────────────
function SmokeBackground() {
  return (
    <svg
      className="absolute inset-0 w-full h-full object-cover pointer-events-none"
      viewBox="0 0 800 800"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      <defs>
        <filter id="smoke" x="-50" y="-50" width="900" height="900" filterUnits="userSpaceOnUse">
          <feGaussianBlur stdDeviation="28" />
        </filter>
      </defs>
      <g filter="url(#smoke)">
        <path
          d="M100 200 C 250 80,450 150,600 80 C750 20,850 160,780 320 C700 480,750 650,620 720 C480 800,320 680,180 720 C50 760,-20 600,20 450 C60 300,-50 320,100 200 Z"
          fill="white"
          fillOpacity="0.5"
        />
        <path
          d="M200 300 C 350 180,520 250,680 180 C820 120,900 280,840 440 C780 600,720 720,560 760 C400 800,250 720,160 620 C80 500,50 420,200 300 Z"
          fill="white"
          fillOpacity="0.35"
        />
        <path
          d="M350 50 C 500 -30,650 100,720 220 C800 360,780 520,680 620 C580 720,420 700,300 640 C180 580,80 460,100 320 C120 180,200 130,350 50 Z"
          fill="#EA384D"
          fillOpacity="0.07"
        />
      </g>
    </svg>
  );
}

// ─── Line-art Smartphone Illustration (SVG) ───────────────────────────────────
function PhoneIllustration() {
  return (
    <svg viewBox="0 0 200 320" className="w-full h-full drop-shadow-2xl" fill="none" aria-hidden="true">
      {/* Phone body */}
      <rect x="30" y="10" width="140" height="300" rx="22" ry="22" fill="#0F172A" />
      {/* Screen */}
      <rect x="38" y="30" width="124" height="250" rx="12" ry="12" fill="#1E293B" />
      {/* Status bar */}
      <rect x="70" y="18" width="60" height="8" rx="4" fill="#334155" />
      {/* Screen content lines (simulated UI) */}
      <rect x="50" y="52" width="100" height="8"  rx="4" fill="#EA384D" fillOpacity="0.9" />
      <rect x="50" y="68" width="70"  height="5"  rx="2.5" fill="#475569" />
      <rect x="50" y="80" width="85"  height="5"  rx="2.5" fill="#475569" />
      {/* Product card mock */}
      <rect x="50" y="98" width="100" height="80" rx="10" fill="#0F172A" stroke="#334155" strokeWidth="1.5" />
      <rect x="58" y="106" width="40" height="40" rx="6" fill="#1E293B" />
      <rect x="58" y="106" width="40" height="40" rx="6" fill="url(#phoneGrad)" fillOpacity="0.6" />
      <rect x="106" y="110" width="36" height="5" rx="2.5" fill="#94A3B8" />
      <rect x="106" y="120" width="28" height="4" rx="2"   fill="#64748B" />
      <rect x="106" y="130" width="32" height="5" rx="2.5" fill="#EA384D" />
      {/* Add to cart button */}
      <rect x="58" y="154" width="84" height="16" rx="8" fill="#EA384D" />
      <rect x="80" y="159" width="40" height="4"  rx="2" fill="white" fillOpacity="0.9" />
      {/* Second card (smaller) */}
      <rect x="50" y="188" width="45" height="52" rx="8" fill="#0F172A" stroke="#334155" strokeWidth="1.5" />
      <rect x="55" y="193" width="35" height="30" rx="5" fill="#1E293B" />
      <rect x="55" y="228" width="25" height="4" rx="2" fill="#EA384D" />
      {/* Third card */}
      <rect x="105" y="188" width="45" height="52" rx="8" fill="#0F172A" stroke="#334155" strokeWidth="1.5" />
      <rect x="110" y="193" width="35" height="30" rx="5" fill="#1E293B" />
      <rect x="110" y="228" width="25" height="4" rx="2" fill="#EA384D" />
      {/* Home indicator */}
      <rect x="80" y="295" width="40" height="4" rx="2" fill="#475569" />
      {/* Dynamic speed lines - Coral accent */}
      <path d="M5 160 L 25 155" stroke="#EA384D" strokeWidth="3" strokeLinecap="round" />
      <path d="M5 173 L 20 169" stroke="#EA384D" strokeWidth="2" strokeLinecap="round" />
      <path d="M5 186 L 18 183" stroke="#EA384D" strokeWidth="1.5" strokeLinecap="round" />
      <path d="M175 140 L 195 135" stroke="#EA384D" strokeWidth="3" strokeLinecap="round" />
      <path d="M178 153 L 195 150" stroke="#EA384D" strokeWidth="2" strokeLinecap="round" />
      <defs>
        <linearGradient id="phoneGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%"   stopColor="#EA384D" />
          <stop offset="100%" stopColor="#0F172A" />
        </linearGradient>
      </defs>
    </svg>
  );
}

// ─────────────────────────────────────────────────────────────────────────────
export default function AuthLayout({ children }) {
  return (
    <div className="min-h-screen w-full flex bg-white font-sans antialiased">

      {/* ════════════════════════════════════════════════════════════════════
          CỘT TRÁI: FORM PANEL
          45-50% desktop | 100% mobile
      ════════════════════════════════════════════════════════════════════ */}
      <div className="w-full lg:w-[48%] flex flex-col justify-between px-6 sm:px-12 lg:px-16 py-8 lg:py-12 overflow-y-auto">

        {/* Brand Header */}
        <div className="flex items-center justify-between mb-8">
          <Link to="/" className="flex items-center gap-2 group" aria-label="TEU Mobile – Trang chủ">
            <img
              src={logo}
              alt="TEU Mobile logo"
              className="h-14 w-auto object-contain group-hover:scale-105 transition-transform duration-200"
            />
            <div className="mt-3">
              <span className="font-extrabold text-3xl text-primary tracking-tight">TẾU</span>
              <span className="font-light text-xl text-slate-700 ml-1.5">Mobile</span>
            </div>
          </Link>

          <Link
            to="/"
            className="hidden sm:inline-flex items-center gap-1 text-xs font-semibold text-slate-500 hover:text-primary transition-colors"
          >
            Về trang chủ
            <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </Link>
        </div>

        {/* Form Content */}
        <div className="w-full max-w-[420px] mx-auto my-auto">
          {children}
        </div>

        {/* Footer Trust Badges */}
        <div className="mt-8 pt-6 border-t border-slate-100 flex flex-wrap items-center justify-between gap-3 text-xs text-slate-400">
          <div className="flex items-center gap-1.5">
            <ShieldCheck className="w-4 h-4 text-emerald-500 flex-shrink-0" />
            <span>Bảo mật chuẩn Keycloak OIDC</span>
          </div>
          <div className="flex items-center gap-1.5">
            <Zap className="w-4 h-4 text-amber-500 flex-shrink-0" />
            <span>Đồng bộ tức thì</span>
          </div>
          <div className="flex items-center gap-1.5">
            <Smartphone className="w-4 h-4 text-primary flex-shrink-0" />
            <span>Hàng chính hãng 100%</span>
          </div>
        </div>
      </div>

      {/* ════════════════════════════════════════════════════════════════════
          CỘT PHẢI: HERO VISUAL SHOWCASE PANEL
          50-55% desktop | ẩn trên mobile
      ════════════════════════════════════════════════════════════════════ */}
      <div
        className="hidden lg:flex lg:w-[52%] relative items-center justify-center overflow-hidden p-10"
        style={{ background: 'linear-gradient(135deg, #F1F5F9 0%, #E2E8F0 60%, #CBD5E1 100%)' }}
        aria-hidden="true"
      >
        {/* Animated smoke / blob background */}
        <SmokeBackground />

        {/* Coral glow halo */}
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 rounded-full bg-primary/10 blur-3xl pointer-events-none" />

        {/* Main Showcase Card */}
        <div className="relative z-10 w-full max-w-sm flex flex-col items-center">

          {/* Glassmorphism card */}
          <div className="w-full bg-white/70 backdrop-blur-md border border-white/80 rounded-3xl shadow-2xl p-8 flex flex-col items-center text-center">

            {/* Tech Badge */}
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-slate-900 text-white text-[11px] font-bold uppercase tracking-widest mb-6">
              <span className="w-2 h-2 rounded-full bg-primary animate-pulse flex-shrink-0" />
              Next-Gen Experience
            </div>

            {/* Phone Illustration */}
            <div className="w-44 h-72 relative mb-4">
              <PhoneIllustration />
            </div>

            {/* Headline */}
            <h2 className="text-2xl font-black uppercase tracking-tight text-slate-900 leading-tight">
              MUA SẮM <span className="text-primary">ĐỈNH CAO</span>
            </h2>
            <p className="text-slate-500 text-sm mt-2.5 leading-relaxed max-w-xs">
              Điện thoại &amp; phụ kiện công nghệ chính hãng. Trải nghiệm mua sắm mượt mà, giao hàng tốc thần.
            </p>

            {/* Stats Row */}
            <div className="grid grid-cols-3 gap-4 w-full mt-6 pt-5 border-t border-slate-200/80">
              <div>
                <div className="text-lg font-bold text-slate-900">100%</div>
                <div className="text-[11px] text-slate-400 font-medium mt-0.5">Chính hãng</div>
              </div>
              <div>
                <div className="text-lg font-bold text-primary">24/7</div>
                <div className="text-[11px] text-slate-400 font-medium mt-0.5">Hỗ trợ</div>
              </div>
              <div>
                <div className="text-lg font-bold text-slate-900">0%</div>
                <div className="text-[11px] text-slate-400 font-medium mt-0.5">Trả góp</div>
              </div>
            </div>
          </div>

          {/* Floating tags – Cyberpunk line-art accent */}
          <div className="absolute -top-5 -right-5 bg-slate-900 text-white text-[10px] font-bold px-3 py-1.5 rounded-full uppercase tracking-wider shadow-lg">
            Hot Deal 🔥
          </div>
          <div className="absolute -bottom-4 -left-4 bg-primary text-white text-[10px] font-bold px-3 py-1.5 rounded-full uppercase tracking-wider shadow-lg">
            Freeship toàn quốc
          </div>
        </div>
      </div>

    </div>
  );
}
