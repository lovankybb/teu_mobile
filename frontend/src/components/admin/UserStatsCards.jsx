import React from "react";
import { Users, UserCheck, UserX } from "lucide-react";

export default function UserStatsCards({ stats, loading }) {
  if (loading) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        {[1, 2, 3].map((i) => (
          <div
            key={i}
            className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-sm animate-pulse"
          >
            <div className="flex items-center justify-between mb-3">
              <div className="w-24 h-4 bg-slate-200 rounded" />
              <div className="w-10 h-10 rounded-xl bg-slate-100" />
            </div>
            <div className="w-16 h-7 bg-slate-200 rounded mt-2" />
          </div>
        ))}
      </div>
    );
  }

  const cards = [
    {
      title: "Tổng Người Dùng",
      value: stats?.totalUsers ?? 0,
      icon: Users,
      color: "text-blue-600 bg-blue-50 border-blue-100",
      description: "Tất cả tài khoản trong hệ thống",
    },
    {
      title: "Đang Hoạt Động",
      value: stats?.activeUsers ?? 0,
      icon: UserCheck,
      color: "text-emerald-600 bg-emerald-50 border-emerald-100",
      description: "Tài khoản có quyền đăng nhập bình thường",
    },
    {
      title: "Đã Bị Khóa",
      value: stats?.inactiveUsers ?? 0,
      icon: UserX,
      color: "text-red-600 bg-red-50 border-red-100",
      description: "Tài khoản bị vô hiệu hóa Keycloak & DB",
    },
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
      {cards.map((card) => {
        const Icon = card.icon;
        return (
          <div
            key={card.title}
            className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-sm hover:shadow-md transition-shadow"
          >
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">
                {card.title}
              </span>
              <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${card.color}`}>
                <Icon className="w-5 h-5" />
              </div>
            </div>
            <div className="flex items-baseline justify-between">
              <span className="text-2xl sm:text-3xl font-black text-slate-900 tracking-tight">
                {card.value.toLocaleString()}
              </span>
            </div>
            <p className="text-[11px] text-slate-400 mt-1 font-medium">{card.description}</p>
          </div>
        );
      })}
    </div>
  );
}
