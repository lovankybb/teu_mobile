import React from "react";
import { Link } from "react-router-dom";
import logo from "../../assets/logo.png";
import heroArtwork from "../../assets/hero-artwork.png";

export default function AuthLayout({ children }) {
  return (
    <div className="min-h-screen w-full flex bg-white font-sans antialiased">
      {/* ════════════════════════════════════════════════════════════════════
          CỘT TRÁI: FORM PANEL (50% desktop | 100% mobile)
          ════════════════════════════════════════════════════════════════════ */}
      <div className="w-full lg:w-1/2 flex flex-col justify-between px-6 py-6 sm:px-10 sm:py-8 lg:px-14 lg:py-10 min-h-screen overflow-y-auto">
        {/* Brand Header */}
        <div className="flex items-center justify-between flex-shrink-0 mb-6">
          <Link to="/" className="flex items-center gap-2 group" aria-label="TEU Mobile">
            <img src={logo} alt="TEU Mobile" className="h-8 w-auto object-contain" />
            <div className="flex items-baseline">
              <span className="font-extrabold text-xl text-primary tracking-tight">TẾU</span>
              <span className="font-medium text-slate-700 text-sm ml-1">Mobile</span>
            </div>
          </Link>

          <Link
            to="/"
            className="inline-flex items-center gap-1 text-xs font-semibold text-slate-500 hover:text-primary transition-colors"
          >
            Về trang chủ
            <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </Link>
        </div>

        {/* Form Content */}
        <div className="w-full max-w-[400px] mx-auto my-auto py-2">
          {children}
        </div>

        {/* Spacer for bottom alignment */}
        <div className="h-5 flex-shrink-0" />
      </div>

      {/* ════════════════════════════════════════════════════════════════════
          CỘT PHẢI: HERO VISUAL SHOWCASE PANEL (Matching Figma Design)
          ════════════════════════════════════════════════════════════════════ */}
      <div
        className="hidden lg:flex lg:w-1/2 h-screen relative items-center justify-center overflow-hidden bg-slate-100"
        aria-hidden="true"
      >
        <img
          src={heroArtwork}
          alt="Hero Artwork"
          className="w-full h-full object-cover object-center block"
        />
      </div>
    </div>
  );
}
