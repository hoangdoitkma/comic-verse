import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import DataTable from '../../components/DataTable';
import ActionModal from '../../components/ActionModal';
import adminService from '../../services/adminService';

export default function GenresPage() {
  const { addToast } = useOutletContext();

  const [genres, setGenres] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isError, setIsError] = useState(false);

  // Form modal
  const [modal, setModal] = useState({ open: false, mode: 'create', genre: null });
  const [formData, setFormData] = useState({ name: '', description: '' });
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Delete confirm
  const [deleteModal, setDeleteModal] = useState({ open: false, genre: null });

  const fetchGenres = async () => {
    setIsLoading(true);
    setIsError(false);
    try {
      const data = await adminService.getGenres();
      setGenres(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Error fetching genres:', err);
      setIsError(true);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchGenres();
  }, []);

  // ======= Create / Edit =======
  const openCreateModal = () => {
    setFormData({ name: '', description: '' });
    setModal({ open: true, mode: 'create', genre: null });
  };

  const openEditModal = (genre) => {
    setFormData({ name: genre.name, description: genre.description || '' });
    setModal({ open: true, mode: 'edit', genre });
  };

  const handleSubmit = async () => {
    if (!formData.name.trim()) {
      addToast('Vui lòng nhập tên thể loại.', 'error');
      return;
    }
    setIsSubmitting(true);
    try {
      if (modal.mode === 'create') {
        await adminService.createGenre(formData);
        addToast(`Đã tạo thể loại "${formData.name}".`, 'success');
      } else {
        await adminService.updateGenre(modal.genre.id, formData);
        addToast(`Đã cập nhật thể loại "${formData.name}".`, 'success');
      }
      setModal({ open: false, mode: 'create', genre: null });
      fetchGenres();
    } catch (err) {
      console.error('Submit error:', err);
      addToast(err.response?.data?.message || 'Lỗi khi lưu thể loại.', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  // ======= Delete =======
  const handleDelete = async () => {
    setIsSubmitting(true);
    try {
      await adminService.deleteGenre(deleteModal.genre.id);
      addToast(`Đã xoá thể loại "${deleteModal.genre.name}".`, 'success');
      setDeleteModal({ open: false, genre: null });
      fetchGenres();
    } catch (err) {
      console.error('Delete error:', err);
      addToast(err.response?.data?.message || 'Lỗi khi xoá thể loại. Có thể đang được sử dụng.', 'error');
    } finally {
      setIsSubmitting(false);
    }
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
      label: 'Tên thể loại',
      minWidth: '160px',
      render: (val) => <span className="font-medium text-white">{val}</span>,
    },
    {
      key: 'description',
      label: 'Mô tả',
      minWidth: '200px',
      render: (val) => (
        <span className="text-dark-400 text-sm line-clamp-2">{val || '—'}</span>
      ),
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
            onClick={() => setDeleteModal({ open: true, genre: row })}
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
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white">Quản lý Thể loại</h1>
          <p className="text-sm text-dark-400 mt-1">
            Thêm, sửa, xoá các thể loại truyện trong hệ thống
          </p>
        </div>
        <button
          onClick={openCreateModal}
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-lg text-sm font-semibold
            bg-emerald-600 text-white shadow-lg shadow-emerald-500/20
            hover:bg-emerald-500 transition-all cursor-pointer"
        >
          <Plus size={18} />
          Thêm thể loại
        </button>
      </div>

      {/* Table */}
      <DataTable
        columns={columns}
        data={genres}
        isLoading={isLoading}
        isError={isError}
        emptyMessage="Chưa có thể loại nào."
      />

      {/* ===== Create/Edit Modal ===== */}
      <ActionModal
        isOpen={modal.open}
        onClose={() => setModal({ open: false, mode: 'create', genre: null })}
        title={modal.mode === 'create' ? 'Thêm thể loại mới' : 'Chỉnh sửa thể loại'}
        onConfirm={handleSubmit}
        confirmText={modal.mode === 'create' ? 'Tạo mới' : 'Cập nhật'}
        isLoading={isSubmitting}
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-dark-300 mb-2">
              Tên thể loại <span className="text-red-400">*</span>
            </label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="Ví dụ: Action, Romance..."
              className="w-full px-4 py-2.5 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-dark-200
                placeholder:text-dark-500 focus:outline-none focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/20 transition-all"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-dark-300 mb-2">Mô tả</label>
            <textarea
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="Mô tả ngắn gọn về thể loại..."
              rows={3}
              className="w-full px-4 py-2.5 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-dark-200
                placeholder:text-dark-500 focus:outline-none focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/20 transition-all resize-none"
            />
          </div>
        </div>
      </ActionModal>

      {/* ===== Delete Confirm Modal ===== */}
      <ActionModal
        isOpen={deleteModal.open}
        onClose={() => setDeleteModal({ open: false, genre: null })}
        title="Xoá thể loại"
        onConfirm={handleDelete}
        confirmText="Xoá"
        confirmVariant="danger"
        isLoading={isSubmitting}
      >
        <p className="text-sm text-dark-300">
          Bạn có chắc muốn xoá thể loại{' '}
          <span className="font-semibold text-white">"{deleteModal.genre?.name}"</span>?
          Hành động này không thể hoàn tác.
        </p>
      </ActionModal>
    </div>
  );
}
