import React, { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';

export default function Input({
  label,
  id,
  type = 'text',
  placeholder = '',
  value,
  onChange,
  error,
  helperText,
  required = false,
  className = '',
  disabled = false,
  ...props
}) {
  const [showPassword, setShowPassword] = useState(false);
  const isPassword = type === 'password';
  const inputType = isPassword ? (showPassword ? 'text' : 'password') : type;

  return (
    <div className={`w-full ${className}`}>
      {label && (
        <label
          htmlFor={id}
          className="block text-sm font-semibold text-slate-800 mb-1.5"
        >
          {label} {required && <span className="text-primary">*</span>}
        </label>
      )}

      <div className="relative">
        <input
          id={id}
          type={inputType}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          disabled={disabled}
          className={`w-full px-4 py-3 bg-white text-slate-900 placeholder:text-slate-400 text-sm rounded-xl border transition-all duration-200 outline-none
            ${
              error
                ? 'border-red-500 focus:ring-2 focus:ring-red-500/20'
                : 'border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20'
            }
            ${isPassword ? 'pr-11' : ''}
            disabled:bg-slate-50 disabled:text-slate-400 disabled:cursor-not-allowed
          `}
          {...props}
        />

        {isPassword && (
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-slate-400 hover:text-slate-600 focus:outline-none transition-colors"
            tabIndex={-1}
          >
            {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
          </button>
        )}
      </div>

      {error ? (
        <p className="mt-1.5 text-xs text-red-500 font-medium">{error}</p>
      ) : helperText ? (
        <p className="mt-1.5 text-xs text-slate-500">{helperText}</p>
      ) : null}
    </div>
  );
}
