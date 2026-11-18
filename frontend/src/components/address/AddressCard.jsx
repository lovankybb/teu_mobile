import React from 'react';
import { Home, Briefcase, MapPin, CheckCircle, Trash2, Edit3, ShieldCheck } from 'lucide-react';
import Button from '../ui/Button';

export default function AddressCard({
  address,
  onEdit,
  onDelete,
  onSetDefault,
  isProcessing = false,
}) {
  const isHome = address.addressType === 'HOME';

  return (
    <div
      className={`relative rounded-2xl p-5 sm:p-6 transition-all duration-200 bg-white border ${
        address.isDefault
          ? 'border-red-300 ring-2 ring-primary/10 shadow-sm'
          : 'border-slate-200/90 hover:border-slate-300 shadow-none'
      }`}
    >
      {/* Header: Thông tin người nhận & Badges */}
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="flex flex-wrap items-center gap-2.5">
          <h4 className="text-base font-bold text-slate-900 tracking-tight">
            {address.recipientName}
          </h4>
          <span className="text-slate-300">|</span>
          <span className="text-sm font-mono text-slate-600 font-medium">
            {address.phoneNumber}
          </span>
        </div>

        {/* Badges */}
        <div className="flex items-center gap-2">
          {address.isDefault && (
            <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-red-50 text-primary border border-red-200">
              <ShieldCheck className="w-3.5 h-3.5" />
              Mặc định
            </span>
          )}

          <span
            className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold border ${
              isHome
                ? 'bg-blue-50 text-blue-700 border-blue-200'
                : 'bg-amber-50 text-amber-700 border-amber-200'
            }`}
          >
            {isHome ? <Home className="w-3 h-3" /> : <Briefcase className="w-3 h-3" />}
            {isHome ? 'Nhà riêng' : 'Văn phòng'}
          </span>
        </div>
      </div>

      {/* Body: Địa chỉ chi tiết */}
      <div className="mt-3 text-sm text-slate-700 space-y-1">
        <p className="font-medium text-slate-900 flex items-start gap-1.5">
          <MapPin className="w-4 h-4 text-slate-400 flex-shrink-0 mt-0.5" />
          <span>{address.streetAddress}</span>
        </p>
        <p className="text-slate-500 pl-5 text-xs sm:text-sm">
          {address.ward}, {address.district}, {address.province}
        </p>

        {address.notes && (
          <p className="text-xs text-slate-500 italic pl-5 pt-1">
            Ghi chú: {address.notes}
          </p>
        )}
      </div>

      {/* Footer: Nút tác vụ (Sửa, Xóa, Thiết lập mặc định) */}
      <div className="mt-5 pt-4 border-t border-slate-100 flex flex-wrap items-center justify-between gap-3">
        <div>
          {!address.isDefault && (
            <button
              type="button"
              onClick={() => onSetDefault(address.id)}
              disabled={isProcessing}
              className="text-xs font-semibold text-slate-600 hover:text-primary transition-colors py-1 px-2.5 rounded-lg border border-slate-200 hover:border-primary/40 disabled:opacity-50"
            >
              Thiết lập mặc định
            </button>
          )}
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => onEdit(address)}
            disabled={isProcessing}
            className="inline-flex items-center gap-1 text-xs font-semibold text-slate-600 hover:text-primary transition-colors py-1.5 px-3 rounded-lg hover:bg-slate-50"
          >
            <Edit3 className="w-3.5 h-3.5" />
            Cập nhật
          </button>

          <button
            type="button"
            onClick={() => onDelete(address.id)}
            disabled={isProcessing}
            className="inline-flex items-center gap-1 text-xs font-semibold text-slate-400 hover:text-red-600 transition-colors py-1.5 px-3 rounded-lg hover:bg-red-50 disabled:opacity-50"
          >
            <Trash2 className="w-3.5 h-3.5" />
            Xóa
          </button>
        </div>
      </div>
    </div>
  );
}
