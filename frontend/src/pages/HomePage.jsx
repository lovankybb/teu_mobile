import React from 'react';
import Navbar from '../components/layout/Navbar';
import Button from '../components/ui/Button';
import { Smartphone, Zap, Shield, Truck, ShoppingCart, Star } from 'lucide-react';
import { Link } from 'react-router-dom';

const FEATURED_PRODUCTS = [
  {
    id: 1,
    name: 'iPhone 16 Pro Max 256GB',
    category: 'Điện thoại',
    price: 34990000,
    originalPrice: 36990000,
    tag: 'Mới ra mắt',
    image: 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=600&auto=format&fit=crop&q=80',
    rating: 4.9,
    reviews: 128,
  },
  {
    id: 2,
    name: 'Samsung Galaxy S24 Ultra 5G',
    category: 'Điện thoại',
    price: 29990000,
    originalPrice: 33990000,
    tag: 'Giảm 4 Triệu',
    image: 'https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=600&auto=format&fit=crop&q=80',
    rating: 4.8,
    reviews: 95,
  },
  {
    id: 3,
    name: 'Tai nghe Apple AirPods Pro 2 USB-C',
    category: 'Phụ kiện',
    price: 5490000,
    originalPrice: 6190000,
    tag: 'Bán chạy',
    image: 'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=600&auto=format&fit=crop&q=80',
    rating: 5.0,
    reviews: 210,
  },
  {
    id: 4,
    name: 'Củ sạc nhanh Anker GaNPrime 65W',
    category: 'Phụ kiện',
    price: 890000,
    originalPrice: 1200000,
    tag: 'Giảm 25%',
    image: 'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=600&auto=format&fit=crop&q=80',
    rating: 4.9,
    reviews: 340,
  },
];

export default function HomePage() {
  const formatVND = (amount) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  return (
    <div className="min-h-screen bg-white">
      <Navbar />

      {/* HERO BANNER SECTION */}
      <section className="relative overflow-hidden bg-gradient-to-br from-slate-900 via-slate-800 to-slate-950 text-white py-16 lg:py-24">
        {/* Họa tiết ánh sáng đỏ Coral thương hiệu */}
        <div className="absolute top-0 right-1/4 w-96 h-96 bg-primary/20 rounded-full blur-3xl pointer-events-none"></div>

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
            <div>
              <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white/10 backdrop-blur-md text-xs font-bold uppercase tracking-wider text-primary-light mb-6 border border-white/10">
                <span className="w-2 h-2 rounded-full bg-primary animate-ping"></span>
                Flagship Phone 2026
              </div>

              <h1 className="text-4xl sm:text-5xl lg:text-6xl font-black uppercase tracking-tight leading-tight">
                ĐỈNH CAO <span className="text-primary">CÔNG NGHỆ</span> TRONG TẦM TAY
              </h1>

              <p className="mt-5 text-slate-300 text-base sm:text-lg max-w-lg leading-relaxed">
                Khám phá các dòng điện thoại cao cấp và phụ kiện sạc nhanh chính hãng với giá tốt nhất thị trường cùng Teu Mobile.
              </p>

              <div className="mt-8 flex flex-wrap items-center gap-4">
                <Link to="/login">
                  <Button variant="primary" size="lg">
                    Khám phá ngay
                  </Button>
                </Link>
                <Button variant="secondary" size="lg" className="bg-transparent text-white border-white/20 hover:bg-white/10">
                  Xem khuyến mãi
                </Button>
              </div>

              {/* USP Badges */}
              <div className="mt-12 grid grid-cols-3 gap-6 pt-8 border-t border-white/10">
                <div className="flex items-center gap-2.5">
                  <Truck className="w-5 h-5 text-primary" />
                  <span className="text-xs font-semibold text-slate-300">Giao nhanh 2h</span>
                </div>
                <div className="flex items-center gap-2.5">
                  <Shield className="w-5 h-5 text-primary" />
                  <span className="text-xs font-semibold text-slate-300">Bảo hành 12T</span>
                </div>
                <div className="flex items-center gap-2.5">
                  <Zap className="w-5 h-5 text-primary" />
                  <span className="text-xs font-semibold text-slate-300">Đổi mới 30 ngày</span>
                </div>
              </div>
            </div>

            {/* Hero Image Showcase */}
            <div className="relative flex items-center justify-center">
              <div className="relative w-full max-w-md aspect-square rounded-3xl bg-gradient-to-tr from-white/5 to-white/10 backdrop-blur-md border border-white/10 p-6 flex items-center justify-center shadow-2xl">
                <img
                  src="https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=600&auto=format&fit=crop&q=80"
                  alt="Flagship Smartphone"
                  className="w-4/5 h-4/5 object-contain drop-shadow-2xl hover:scale-105 transition-transform duration-500"
                />
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* FEATURED PRODUCTS SECTION */}
      <section className="py-16 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between mb-10">
          <div>
            <h2 className="text-2xl sm:text-3xl font-extrabold uppercase tracking-tight text-slate-900">
              SẢN PHẨM <span className="text-primary">NỔI BẬT</span>
            </h2>
            <p className="text-slate-500 text-sm mt-1">Các sản phẩm điện thoại và phụ kiện bán chạy nhất tuần qua</p>
          </div>
          <button className="text-sm font-bold text-primary hover:underline">
            Xem tất cả →
          </button>
        </div>

        {/* Product Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {FEATURED_PRODUCTS.map((prod) => (
            <div
              key={prod.id}
              className="group bg-white rounded-2xl border border-slate-100 hover:border-slate-200 shadow-sm hover:shadow-xl transition-all duration-300 p-4 flex flex-col justify-between"
            >
              <div>
                {/* Product Image & Tag */}
                <div className="relative aspect-square rounded-xl bg-slate-50 overflow-hidden mb-4 flex items-center justify-center p-4">
                  <span className="absolute top-2.5 left-2.5 px-2 py-0.5 rounded-md bg-primary text-white text-[10px] font-bold uppercase tracking-wider">
                    {prod.tag}
                  </span>
                  <img
                    src={prod.image}
                    alt={prod.name}
                    className="w-full h-full object-contain group-hover:scale-105 transition-transform duration-300"
                  />
                </div>

                <div className="text-xs text-slate-400 font-medium mb-1">{prod.category}</div>
                <h3 className="text-sm font-bold text-slate-800 line-clamp-2 group-hover:text-primary transition-colors">
                  {prod.name}
                </h3>

                {/* Rating */}
                <div className="flex items-center gap-1.5 mt-2">
                  <div className="flex text-amber-400">
                    <Star className="w-3.5 h-3.5 fill-current" />
                  </div>
                  <span className="text-xs font-bold text-slate-700">{prod.rating}</span>
                  <span className="text-[11px] text-slate-400">({prod.reviews})</span>
                </div>
              </div>

              {/* Price & Buy Button */}
              <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between">
                <div>
                  <div className="text-base font-extrabold text-primary">
                    {formatVND(prod.price)}
                  </div>
                  <div className="text-xs text-slate-400 line-through">
                    {formatVND(prod.originalPrice)}
                  </div>
                </div>

                <button className="w-9 h-9 rounded-xl bg-slate-100 group-hover:bg-primary group-hover:text-white text-slate-700 flex items-center justify-center transition-all duration-200 shadow-sm">
                  <ShoppingCart className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
