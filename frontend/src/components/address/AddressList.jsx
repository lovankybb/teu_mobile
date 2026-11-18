import React, { useState, useEffect, useCallback } from 'react';
import { Plus, MapPin, AlertCircle, CheckCircle2, Loader2 } from 'lucide-react';
import AddressCard from './AddressCard';
import AddressModal from './AddressModal';
import Button from '../ui/Button';
import addressService from '../../services/addressService';

export default function AddressList() {
  const [addresses, setAddresses] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isProcessing, setIsProcessing] = useState(false);

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  // Trạng thái modal Thêm/Sửa
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingAddress, setEditingAddress] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Tải danh sách địa chỉ từ server
  const loadAddresses = useCallback(async () => {
    try {
      setIsLoading(true);
      setError('');
      const list = await addressService.getAddresses();
      setAddresses(list);
    } catch (err) {
      console.error('Lỗi tải danh sách địa chỉ:', err);
      setError(err.response?.data?.message || err.message || 'Không thể tải sổ địa chỉ');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAddresses();
  }, [loadAddresses]);

  // Mở modal thêm mới
  const handleOpenCreateModal = () => {
    if (addresses.length >= 20) {
      setError('Bạn đã đạt giới hạn tối đa 20 địa chỉ nhận hàng');
      return;
    }
    setEditingAddress(null);
    setIsModalOpen(true);
  };

  // Mở modal cập nhật
  const handleOpenEditModal = (address) => {
    setEditingAddress(address);
    setIsModalOpen(true);
  };

  // Đóng modal
  const handleCloseModal = () => {
    if (!isSubmitting) {
      setIsModalOpen(false);
      setEditingAddress(null);
    }
  };

  // Submit form thêm mới hoặc cập nhật
  const handleFormSubmit = async (formData) => {
    setIsSubmitting(true);
    setMessage('');
    setError('');

    try {
      if (editingAddress?.id) {
        await addressService.updateAddress(editingAddress.id, formData);
        setMessage('Cập nhật địa chỉ nhận hàng thành công!');
      } else {
        await addressService.createAddress(formData);
        setMessage('Thêm địa chỉ mới thành công!');
      }
      setIsModalOpen(false);
      setEditingAddress(null);
      await loadAddresses();
    } catch (err) {
      console.error('Lỗi lưu địa chỉ:', err);
      setError(err.response?.data?.message || err.message || 'Lưu địa chỉ thất bại');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Thiết lập địa chỉ mặc định
  const handleSetDefault = async (addressId) => {
    setIsProcessing(true);
    setMessage('');
    setError('');

    try {
      await addressService.setDefaultAddress(addressId);
      setMessage('Đã thiết lập làm địa chỉ mặc định!');
      await loadAddresses();
    } catch (err) {
      console.error('Lỗi đặt địa chỉ mặc định:', err);
      setError(err.response?.data?.message || err.message || 'Không thể đặt làm địa chỉ mặc định');
    } finally {
      setIsProcessing(false);
    }
  };

  // Xóa địa chỉ
  const handleDelete = async (addressId) => {
    const isConfirm = window.confirm('Bạn có chắc chắn muốn xóa địa chỉ nhận hàng này?');
    if (!isConfirm) return;

    setIsProcessing(true);
    setMessage('');
    setError('');

    try {
      await addressService.deleteAddress(addressId);
      setMessage('Đã xóa địa chỉ thành công!');
      await loadAddresses();
    } catch (err) {
      console.error('Lỗi xóa địa chỉ:', err);
      setError(err.response?.data?.message || err.message || 'Không thể xóa địa chỉ');
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Header của Section Sổ địa chỉ */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-4 border-b border-slate-100">
        <div>
          <h2 className="text-xl font-bold text-slate-900 tracking-tight">
            Sổ địa chỉ nhận hàng
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Quản lý danh sách địa chỉ nhận hàng cho các đơn mua tại Teu Mobile (tối đa 20 địa chỉ)
          </p>
        </div>

        <Button
          type="button"
          variant="primary"
          size="sm"
          onClick={handleOpenCreateModal}
          disabled={isLoading || addresses.length >= 20}
        >
          <Plus className="w-4 h-4 mr-1.5" />
          Thêm địa chỉ mới
        </Button>
      </div>

      {/* Thông báo Alert */}
      {message && (
        <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm flex items-center gap-2 animate-fadeIn">
          <CheckCircle2 className="w-5 h-5 text-emerald-500 flex-shrink-0" />
          <span>{message}</span>
        </div>
      )}

      {error && (
        <div className="p-4 rounded-xl bg-red-50 border border-red-200 text-red-600 text-sm flex items-center gap-2 animate-fadeIn">
          <AlertCircle className="w-5 h-5 text-red-500 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Danh sách địa chỉ hoặc Trạng thái rỗng */}
      {isLoading ? (
        <div className="py-16 flex flex-col items-center justify-center text-slate-400">
          <Loader2 className="w-8 h-8 animate-spin text-primary mb-3" />
          <p className="text-sm font-medium">Đang tải danh sách địa chỉ...</p>
        </div>
      ) : addresses.length === 0 ? (
        /* Empty State */
        <div className="py-16 px-4 text-center rounded-3xl border-2 border-dashed border-slate-200 bg-slate-50/50 flex flex-col items-center justify-center">
          <div className="w-16 h-16 rounded-2xl bg-white shadow-sm border border-slate-100 flex items-center justify-center text-slate-300 mb-4">
            <MapPin className="w-8 h-8 text-slate-400" />
          </div>
          <h3 className="text-base font-bold text-slate-800">
            Chưa có địa chỉ nhận hàng nào
          </h3>
          <p className="text-xs text-slate-500 max-w-sm mt-1 mb-5">
            Lưu địa chỉ để việc mua sắm và thanh toán đơn hàng diễn ra nhanh chóng hơn.
          </p>
          <Button
            type="button"
            variant="primary"
            size="md"
            onClick={handleOpenCreateModal}
          >
            <Plus className="w-4 h-4 mr-1.5" />
            Thêm địa chỉ đầu tiên
          </Button>
        </div>
      ) : (
        /* Grid danh sách thẻ địa chỉ */
        <div className="grid grid-cols-1 gap-4">
          {addresses.map((address) => (
            <AddressCard
              key={address.id}
              address={address}
              onEdit={handleOpenEditModal}
              onDelete={handleDelete}
              onSetDefault={handleSetDefault}
              isProcessing={isProcessing}
            />
          ))}
        </div>
      )}

      {/* Modal Thêm mới / Cập nhật */}
      <AddressModal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        onSubmit={handleFormSubmit}
        initialData={editingAddress}
        isSubmitting={isSubmitting}
      />
    </div>
  );
}
