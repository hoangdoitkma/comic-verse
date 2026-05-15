import { useState, useEffect, useMemo } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import DataTable from '../../components/DataTable';
import ActionModal from '../../components/ActionModal';
import adminService from '../../services/adminService';

const PAGE_SIZE = 6;

export default function AuthorsPage() {
  const { addToast } = useOutletContext();

  const [authors, setAuthors] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isError, setIsError] = useState(false);
  const [page, setPage] = useState(0);

  // Form modal
  const [modal, setModal] = useState({ open: false, mode: 'create', author: null });
  const [formData, setFormData] = useState({ name: '', studio: '', country: '' });
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Delete confirm
  const [deleteModal, setDeleteModal] = useState({ open: false, author: null });

  const fetchAuthors = async () => {
    setIsLoading(true);
    setIsError(false);
    try {
      const data = await adminService.getAuthors();
      setAuthors(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Error fetching authors:', err);
      setIsError(true);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchAuthors();
  }, []);

  const openCreateModal = () => {
    setFormData({ name: '', studio: '', country: '' });
    setModal({ open: true, mode: 'create', author: null });
  };

  const openEditModal = (author) => {
    setFormData({
      name: author.name || '',
      studio: author.studio || '',
      country: author.country || '',
    });
    setModal({ open: true, mode: 'edit', author });
  };

  const handleSubmit = async () => {
    if (!formData.name.trim()) {
      addToast('Vui lòng nhập tên tác giả.', 'error');
      return;
    }
    setIsSubmitting(true);
    try {
      if (modal.mode === 'create') {
        await adminService.createAuthor(formData);
        addToast(`Đã tạo tác giả "${formData.name}".`, 'success');
      } else {
        await adminService.updateAuthor(modal.author.id, formData);
        addToast(`Đã cập nhật tác giả "${formData.name}".`, 'success');
      }
      setModal({ open: false, mode: 'create', author: null });
      fetchAuthors();
    } catch (err) {
      console.error('Submit error:', err);
      addToast(err.response?.data?.message || 'Lỗi khi lưu tác giả.', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    setIsSubmitting(true);
    try {
      await adminService.deleteAuthor(deleteModal.author.id);
      addToast(`Đã xoá tác giả "${deleteModal.author.name}".`, 'success');
      setDeleteModal({ open: false, author: null });
      fetchAuthors();
    } catch (err) {
      console.error('Delete error:', err);
      addToast(err.response?.data?.message || 'Lỗi khi xoá tác giả. Có thể đang được sử dụng.', 'error');
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
      label: 'Tên tác giả',
      minWidth: '160px',
      render: (val) => <span className="font-medium text-white">{val}</span>,
    },
    {
      key: 'studio',
      label: 'Studio',
      minWidth: '140px',
      render: (val) => <span className="text-dark-300">{val || '—'}</span>,
    },
    {
      key: 'country',
      label: 'Quốc gia',
      minWidth: '100px',
      render: (val) => <span className="text-dark-300">{val || '—'}</span>,
    },
    {
      key: 'createdAt',
      label: 'Ngày tạo',
      minWidth: '120px',
      render: (val) => {
        if (!val) return '—';
        return (
          <span className="text-dark-400 text-xs tabular-nums">
            {new Date(val).toLocaleDateString('vi-VN')}
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
            onClick={() => setDeleteModal({ open: true, author: row })}
            className="p-2 rounded-lg text-dark-400 hover:bg-red-600/15 hover:text-red-400 transition-all cursor-pointer"
            title="Xoá"
          >
            <Trash2 size={15} />
          </button>
        </div>
      ),
    },
  ];

  // Client-side pagination
  const totalPages = Math.ceil(authors.length / PAGE_SIZE);
  const paginatedAuthors = useMemo(() => {
    const start = page * PAGE_SIZE;
    return authors.slice(start, start + PAGE_SIZE);
  }, [authors, page]);

  // Reset page nếu vượt quá totalPages (ví dụ sau khi xoá)
  useEffect(() => {
    if (page > 0 && page >= totalPages) {
      setPage(Math.max(0, totalPages - 1));
    }
  }, [authors, page, totalPages]);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white">Quản lý Tác giả</h1>
          <p className="text-sm text-dark-400 mt-1">
            Thêm, sửa, xoá thông tin tác giả
          </p>
        </div>
        <button
          onClick={openCreateModal}
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-lg text-sm font-semibold
            bg-emerald-600 text-white shadow-lg shadow-emerald-500/20
            hover:bg-emerald-500 transition-all cursor-pointer"
        >
          <Plus size={18} />
          Thêm tác giả
        </button>
      </div>

      <DataTable
        columns={columns}
        data={paginatedAuthors}
        isLoading={isLoading}
        isError={isError}
        emptyMessage="Chưa có tác giả nào."
        pagination={{
          page,
          totalPages,
          totalElements: authors.length,
          onPageChange: setPage,
        }}
      />

      {/* Create/Edit Modal */}
      <ActionModal
        isOpen={modal.open}
        onClose={() => setModal({ open: false, mode: 'create', author: null })}
        title={modal.mode === 'create' ? 'Thêm tác giả mới' : 'Chỉnh sửa tác giả'}
        onConfirm={handleSubmit}
        confirmText={modal.mode === 'create' ? 'Tạo mới' : 'Cập nhật'}
        isLoading={isSubmitting}
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-dark-300 mb-2">
              Tên tác giả <span className="text-red-400">*</span>
            </label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="Nhập tên tác giả..."
              className="w-full px-4 py-2.5 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-dark-200
                placeholder:text-dark-500 focus:outline-none focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/20 transition-all"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-dark-300 mb-2">Studio</label>
            <input
              type="text"
              value={formData.studio}
              onChange={(e) => setFormData({ ...formData, studio: e.target.value })}
              placeholder="Ví dụ: Studio Ghibli..."
              className="w-full px-4 py-2.5 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-dark-200
                placeholder:text-dark-500 focus:outline-none focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/20 transition-all"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-dark-300 mb-2">Quốc gia</label>
            <input
              type="text"
              value={formData.country}
              onChange={(e) => setFormData({ ...formData, country: e.target.value })}
              placeholder="Ví dụ: JP, KR, VN..."
              className="w-full px-4 py-2.5 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-dark-200
                placeholder:text-dark-500 focus:outline-none focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/20 transition-all"
            />
          </div>
        </div>
      </ActionModal>

      {/* Delete Modal */}
      <ActionModal
        isOpen={deleteModal.open}
        onClose={() => setDeleteModal({ open: false, author: null })}
        title="Xoá tác giả"
        onConfirm={handleDelete}
        confirmText="Xoá"
        confirmVariant="danger"
        isLoading={isSubmitting}
      >
        <p className="text-sm text-dark-300">
          Bạn có chắc muốn xoá tác giả{' '}
          <span className="font-semibold text-white">"{deleteModal.author?.name}"</span>?
          Hành động này không thể hoàn tác.
        </p>
      </ActionModal>
    </div>
  );
}
