import React from 'react';

export default function Checkbox({
  label,
  id,
  checked,
  onChange,
  className = '',
  disabled = false,
  ...props
}) {
  return (
    <label
      htmlFor={id}
      className={`inline-flex items-center gap-2 cursor-pointer select-none text-sm text-slate-700 ${className} ${
        disabled ? 'cursor-not-allowed opacity-60' : ''
      }`}
    >
      <input
        type="checkbox"
        id={id}
        checked={checked}
        onChange={onChange}
        disabled={disabled}
        className="w-4 h-4 rounded border-slate-300 text-primary focus:ring-primary/25 focus:ring-2 cursor-pointer transition-colors accent-primary"
        {...props}
      />
      {label && <span>{label}</span>}
    </label>
  );
}
