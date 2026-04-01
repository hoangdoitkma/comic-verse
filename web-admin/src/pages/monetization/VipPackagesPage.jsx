import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import DataTable from '../../components/DataTable';
import ActionModal from '../../components/ActionModal';
import adminService from '../../services/adminService';

export default function VipPackagesPage() {
  const { addToast } = useOutletContext();

  const [packages, setPackages] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isError, setIsError] = useState(false);

  // Form modal
  const [modal, setModal] = useState({ open: false, mode: 'create', pkg: null });
  const [formData, setFormData] = useState({
    name: '',
    durationMonth: 1,
    price: 0,
    currency: 'VND',
    isActive: true,
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Delete confirm
  const [deleteModal, setDeleteModal] = useState({ open: false, pkg: null });

  const fetchPackages = async () => {
    setIsLoading(true);
    setIsError(false);
    try {
      const data = await adminService.getVipPackages();
      setPackages(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Error fetching VIP packages:', err);
      setIsError(true);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchPackages();
  }, []);

  const openCreateModal = () => {
    setFormData({ name: '', durationMonth: 1, price: 0, currency: 'VND', isActive: true });
    setModal({ open: true, mode: 'create', pkg: null });
  };

  const openEditModal = (pkg) => {
    setFormData({
      name: pkg.name || '',
      durationMonth: pkg.durationMonth || 1,
      price: pkg.price || 0,
      currency: pkg.currency || 'VND',
      isActive: pkg.isActive ?? pkg.active ?? true,
    });
    setModal({ open: true, mode: 'edit', pkg });
  };

  const handleSubmit = async () => {
    if (!formData.name.trim()) {
      addToast('Vui lòng nhập tên gói VIP.', 'error');
      return;
    }
    if (formData.price <= 0) {
      addToast('Giá gói phải lớn hơn 0.', 'error');
      return;
    }
    setIsSubmitting(true);
    try {
      if (modal.mode === 'create') {
        await adminService.createVipPackage(formData);
        addToast(`Đã tạo gói VIP "${formData.name}".`, 'success');
      } else {
        await adminService.updateVipPackage(modal.pkg.id, formData);
        addToast(`Đã cập nhật gói VIP "${formData.name}".`, 'success');
      }
      setModal({ open: false, mode: 'create', pkg: null });
      fetchPackages();
    } catch (err) {
      console.error('Submit error:', err);
      addToast(err.response?.data?.message || 'Lỗi khi lưu gói VIP.', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    setIsSubmitting(true);
    try {
      await adminService.deleteVipPackage(deleteModal.pkg.id);
      addToast(`Đã xoá gói VIP "${deleteModal.pkg.name}".`, 'success');
      setDeleteModal({ open: false, pkg: null });
      fetchPackages();
    } catch (err) {
      console.error('Delete error:', err);
      addToast(err.response?.data?.message || 'Lỗi khi xoá gói VIP.', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const formatPrice = (price, currency) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: currency || 'VND',
    }).format(price || 0);
  };

  const columns = [
    {
      key: 'id',
      label: 'ID',
      minWidth: '60px',
      render: (val) => <span className="text-dark-500 tabular-nums">#{val}</span>,
    },
    {
      key: 'name',
      label: 'Tên gói',
      minWidth: '160px',
      render: (val) => <span className="font-medium text-white">{val}</span>,
    },
    {
      key: 'durationMonth',
      label: 'Thời hạn',
      align: 'center',
      render: (val) => (
        <span className="text-dark-300">{val} tháng</span>
      ),
    },
    {
      key: 'price',
      label: 'Giá',
      align: 'right',
      minWidth: '120px',
      render: (val, row) => (
        <span className="font-semibold text-emerald-400 tabular-nums">
          {formatPrice(val, row.currency)}
        </span>
      ),
    },
    {
      key: 'isActive',
      label: 'Trạng thái',
      align: 'center',
      render: (val, row) => {
        const active = val ?? row.active;
        return (
          <span
            className={`inline-flex px-2.5 py-1 rounded-full text-xs font-semibold border ${
              active
                ? 'bg-emerald-500/15 text-emerald-400 border-emerald-500/20'
                : 'bg-dark-700/50 text-dark-400 border-dark-600/30'
            }`}
          >
            {active ? 'Đang hoạt động' : 'Tạm ngưng'}
          </span>
        );
      },
    },
    {
      key: 'actions',
      label: 'Hành động',
      align: 'center',
      minWidth: '140px',
      render: (_, row) => (
        <div className="flex items-center justify-center gap-2">
          <button
            onClick={() => openEditModal(row)}
            className="p-2 rounded-lg text-dark-400 hover:bg-primary-600/15 hover:text-primary-400 transition-all cursor-pointer"
            title="Sửa"
          >
            <Pencil size={15} />
          </button>
          <button
            onClick={() => setDeleteModal({ open: true, pkg: row })}
            className="p-2 rounded-lg text-dark-400 hover:bg-red-600/15 hover:text-red-400 transition-all cursor-pointer"
            title="Xoá"
          >
            <Trash2 size={15} />
          </button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white">Quản lý Gói VIP</h1>
          <p className="text-sm text-dark-400 mt-1">
            Quản lý các gói VIP dành cho người dùng
          </p>
        </div>
        <button
          onClick={openCreateModal}
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-lg text-sm font-semibold
            bg-emerald-600 text-white shadow-lg shadow-emerald-500/20
            hover:bg-emerald-500 transition-all cursor-pointer"
        >
          <Plus size={18} />
          Thêm gói VIP
        </button>
      </div>

      <DataTable
        columns={columns}
        data={packages}
        isLoading={isLoading}
        isError={isError}
        emptyMessage="Chưa có gói VIP nào."
      />

      {/* Create/Edit Modal */}
      <ActionModal
        isOpen={modal.open}
        onClose={() => setModal({ open: false, mode: 'create', pkg: null })}
        title={modal.mode === 'create' ? 'Thêm gói VIP mới' : 'Chỉnh sửa gói VIP'}
        onConfirm={handleSubmit}
        confirmText={modal.mode === 'create' ? 'Tạo mới' : 'Cập nhật'}
        isLoading={isSubmitting}
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-dark-300 mb-2">
              Tên gói <span className="text-red-400">*</span>
            </label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="Ví dụ: Gói 1 Tháng..."
              className="w-full px-4 py-2.5 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-dark-200
                placeholder:text-dark-500 focus:outline-none focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/20 transition-all"
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-dark-300 mb-2">
                Thời hạn (tháng) <span className="text-red-400">*</span>
              </label>
              <input
                type="number"
                min="1"
                value={formData.durationMonth}
                onChange={(e) => setFormData({ ...formData, durationMonth: parseInt(e.target.value) || 1 })}
                className="w-full px-4 py-2.5 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-dark-200
                  focus:outline-none focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/20 transition-all"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-dark-300 mb-2">
                Đơn vị tiền
              </label>
              <select
                value={formData.currency}
                onChange={(e) => setFormData({ ...formData, currency: e.target.value })}
                className="w-full px-4 py-2.5 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-dark-200
                  focus:outline-none focus:border-emerald-500/50 appearance-none cursor-pointer"
              >
                <option value="VND">VND</option>
                <option value="USD">USD</option>
              </select>
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-dark-300 mb-2">
              Giá <span className="text-red-400">*</span>
            </label>
            <input
              type="number"
              min="0"
              step="1000"
              value={formData.price}
              onChange={(e) => setFormData({ ...formData, price: parseFloat(e.target.value) || 0 })}
              placeholder="50000"
              className="w-full px-4 py-2.5 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-dark-200
                placeholder:text-dark-500 focus:outline-none focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/20 transition-all"
            />
          </div>
          <div className="flex items-center gap-3">
            <label className="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                checked={formData.isActive}
                onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
                className="sr-only peer"
              />
              <div className="w-10 h-5.5 bg-dark-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full
                after:content-[''] after:absolute after:top-[3px] after:left-[3px] after:bg-dark-400
                after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-emerald-600 peer-checked:after:bg-white" />
            </label>
            <span className="text-sm text-dark-300">Đang hoạt động</span>
          </div>
        </div>
      </ActionModal>

      {/* Delete Modal */}
      <ActionModal
        isOpen={deleteModal.open}
        onClose={() => setDeleteModal({ open: false, pkg: null })}
        title="Xoá gói VIP"
        onConfirm={handleDelete}
        confirmText="Xoá"
        confirmVariant="danger"
        isLoading={isSubmitting}
      >
        <p className="text-sm text-dark-300">
          Bạn có chắc muốn xoá gói VIP{' '}
          <span className="font-semibold text-white">"{deleteModal.pkg?.name}"</span>?
          Hành động này không thể hoàn tác.
        </p>
      </ActionModal>
    </div>
  );
}
