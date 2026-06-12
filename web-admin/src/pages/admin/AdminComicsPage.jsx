import { useState, useEffect, useCallback } from 'react';
import { useOutletContext } from 'react-router-dom';
import {
  BookOpen,
  Search,
  Plus,
  RefreshCw,
  MoreVertical,
  Image as ImageIcon,
  CheckCircle,
  Trash2,
  Eye,
  Pencil,
  RotateCcw
} from 'lucide-react';
import adminService from '../../services/adminService';
import AdminComicModal from '../../components/AdminComicModal';
import DataTable from '../../components/DataTable';

export default function AdminComicsPage() {
  const { addToast } = useOutletContext();

  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const limit = 6;

  // Search state
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');

  // Dropdown
  const [openDropdownId, setOpenDropdownId] = useState(null);

  // Modal
  const [modalOpen, setModalOpen] = useState(false);
  const [editingComic, setEditingComic] = useState(null);

  const fetchComics = useCallback(async () => {
    try {
      setLoading(true);
      setError(false);
      const res = await adminService.getAllComicsForAdmin({ page, size: limit });
      if (res && res.content) {
        setItems(res.content || []);
        setTotalPages(res.totalPages || 1);
        setTotalElements(res.totalElements || 0);
      } else {
        setItems([]);
        setError(true);
      }
    } catch (err) {
      console.error(err);
      setError(true);
      setItems([]);
    } finally {
      setLoading(false);
    }
  }, [page, limit]);

  useEffect(() => {
    fetchComics();
  }, [fetchComics]);

  // Handle Search Debounce
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(searchTerm);
      setPage(0);
    }, 500);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  const toggleDropdown = (id) => {
    setOpenDropdownId(openDropdownId === id ? null : id);
  };

  const handleOpenAdd = () => {
    setEditingComic(null);
    setModalOpen(true);
  };

  const handleOpenEdit = (comic) => {
    setEditingComic(comic);
    setOpenDropdownId(null);
    setModalOpen(true);
  };

  const handleDelete = async (comic) => {
    if (!window.confirm(`Bạn có chắc muốn XÓA truyện "${comic.title}"? \nTruyện sẽ không bị xóa vĩnh viễn mà chỉ bị ẩn.`)) return;
    try {
      await adminService.deleteComic(comic.id);
      addToast('Đã xóa truyện', 'success');
      fetchComics();
    } catch (e) {
      addToast(e.response?.data?.message || 'Xóa thất bại', 'error');
    } finally {
      setOpenDropdownId(null);
    }
  };

  const handleRestore = async (comic) => {
    try {
      await adminService.restoreComic(comic.id);
      addToast('Khôi phục truyện thành công', 'success');
      fetchComics();
    } catch (e) {
      addToast(e.response?.data?.message || 'Khôi phục thất bại', 'error');
    } finally {
      setOpenDropdownId(null);
    }
  };

  const handleModalSuccess = () => {
    addToast(editingComic ? 'Sửa thông tin truyện thành công' : 'Đã thêm truyện mới', 'success');
    fetchComics();
  };

  // Local filter for search term
  const displayedItems = items.filter(c =>
    c.title.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
    c.slug.toLowerCase().includes(debouncedSearch.toLowerCase())
  );

  const columns = [
    {
      key: 'id',
      label: 'ID',
      minWidth: '50px',
      render: (val) => <span className="text-dark-500 tabular-nums text-xs">#{val}</span>,
    },
    {
      key: 'title',
      label: 'Truyện',
      minWidth: '200px',
      render: (val, row) => (
        <div className="flex items-center gap-2.5">
          <div className="relative shrink-0">
            {row.thumbnailUrl ? (
              <img
                src={row.thumbnailUrl}
                alt="thumbnail"
                className={`w-9 h-12 object-cover rounded border border-dark-700/50 ${row.isDeleted ? 'opacity-50 grayscale' : ''}`}
              />
            ) : (
              <div className="w-9 h-12 rounded bg-dark-800 flex items-center justify-center text-dark-600 border border-dark-700/50">
                <ImageIcon size={14} />
              </div>
            )}
          </div>
          <div className="min-w-0">
            <p className={`font-semibold text-sm leading-tight ${row.isDeleted ? 'text-dark-400 line-through' : 'text-dark-100'} truncate`}>
              {val}
            </p>
            <span className="text-[10px] font-mono text-dark-500 bg-dark-800 px-1 py-0.5 rounded mt-0.5 inline-block truncate max-w-[140px]">
              {row.slug}
            </span>
          </div>
        </div>
      ),
    },
    {
      key: 'viewCount',
      label: 'Thông số',
      minWidth: '110px',
      render: (val, row) => (
        <div className="space-y-0.5">
          <div className="flex items-center gap-1.5 text-dark-300 text-xs">
            <Eye size={12} className="text-dark-500" />
            <span>{val ? val.toLocaleString() : 0} lượt xem</span>
          </div>
          <div className="flex items-center gap-1.5 text-dark-300 text-xs">
            <BookOpen size={12} className="text-dark-500" />
            <span>{row.totalChapters || 0} chương</span>
          </div>
          <div className="mt-1">
            {row.accessType === 'VIP' ? (
              <span className="px-1.5 py-0.5 rounded bg-yellow-500/10 text-yellow-500 font-semibold text-[10px] border border-yellow-500/20">VIP</span>
            ) : (
              <span className="px-1.5 py-0.5 rounded bg-emerald-500/10 text-emerald-400 font-semibold text-[10px] border border-emerald-500/20">MIỄN PHÍ</span>
            )}
          </div>
        </div>
      ),
    },
    {
      key: 'updatedAt',
      label: 'Cập nhật',
      minWidth: '110px',
      render: (val) => (
        <span className="text-dark-400 text-xs tabular-nums">
          {val ? new Date(val).toLocaleString('vi-VN') : '—'}
        </span>
      ),
    },
    {
      key: 'status',
      label: 'Trạng thái',
      align: 'center',
      minWidth: '100px',
      render: (val) => {
        if (val === 'ONGOING') {
          return (
            <span className="inline-flex px-2 py-0.5 text-[11px] font-medium bg-blue-500/10 text-blue-400 border border-blue-500/20 rounded-full whitespace-nowrap">
              Đang tiến hành
            </span>
          );
        } else if (val === 'COMPLETED') {
          return (
            <span className="inline-flex px-2 py-0.5 text-[11px] font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded-full whitespace-nowrap">
              Hoàn thành
            </span>
          );
        }
        return (
          <span className="inline-flex px-2 py-0.5 text-[11px] font-medium bg-red-500/10 text-red-400 border border-red-500/20 rounded-full whitespace-nowrap">
            Tạm ngưng
          </span>
        );
      },
    },
    {
      key: 'isDeleted',
      label: 'Xóa mềm',
      align: 'center',
      minWidth: '80px',
      render: (val) => {
        if (val) {
          return (
            <span className="inline-flex items-center justify-center px-1.5 py-0.5 rounded bg-red-500/10 text-red-400 text-[10px] font-semibold border border-red-500/20">
              <Trash2 size={10} className="mr-0.5" /> XÓA
            </span>
          );
        }
        return (
          <span className="inline-flex items-center justify-center w-5 h-5 rounded-full bg-emerald-500/10 text-emerald-400">
            <CheckCircle size={12} />
          </span>
        );
      },
    },
    {
      key: 'actions',
      label: 'Thao tác',
      align: 'right',
      minWidth: '70px',
      render: (_, row) => (
        <div className="relative inline-block text-left">
          <button
            onClick={() => toggleDropdown(row.id)}
            className="p-1 rounded-lg text-dark-400 hover:text-white hover:bg-dark-700 transition-colors cursor-pointer"
          >
            <MoreVertical size={16} />
          </button>

          {openDropdownId === row.id && (
            <>
              <div
                className="fixed inset-0 z-40"
                onClick={() => setOpenDropdownId(null)}
              />
              <div className="absolute right-0 mt-1 w-36 bg-dark-800 border border-dark-700/50 rounded-xl shadow-xl z-50 py-1 overflow-hidden animate-scale-in">
                <button
                  onClick={() => handleOpenEdit(row)}
                  className="w-full flex items-center gap-2 px-3 py-1.5 text-xs text-dark-200 hover:bg-dark-700 hover:text-white transition-colors cursor-pointer"
                >
                  <Pencil size={12} className="text-blue-400" />
                  Chỉnh sửa
                </button>

                {!row.isDeleted ? (
                  <button
                    onClick={() => handleDelete(row)}
                    className="w-full flex items-center gap-2 px-3 py-1.5 text-xs text-red-400 hover:bg-red-500/10 transition-colors cursor-pointer border-t border-dark-700/50"
                  >
                    <Trash2 size={12} className="text-red-400" />
                    Xóa mềm
                  </button>
                ) : (
                  <button
                    onClick={() => handleRestore(row)}
                    className="w-full flex items-center gap-2 px-3 py-1.5 text-xs text-emerald-400 hover:bg-emerald-500/10 transition-colors cursor-pointer border-t border-dark-700/50"
                  >
                    <RotateCcw size={12} className="text-emerald-400" />
                    Khôi phục
                  </button>
                )}
              </div>
            </>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row gap-3 items-start sm:items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">
            Quản lý Truyện
          </h1>
          <p className="text-sm text-dark-400 mt-1">
            Xem, thêm, sửa, xóa tất cả các truyện trên hệ thống
          </p>
        </div>

        <button
          onClick={handleOpenAdd}
          className="inline-flex items-center justify-center gap-1.5 px-4 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white text-sm font-medium transition-all shadow-lg shadow-emerald-500/20 active:scale-95 cursor-pointer"
        >
          <Plus size={16} />
          <span>Thêm truyện</span>
        </button>
      </div>

      {/* Toolbar */}
      <div className="flex flex-col sm:flex-row gap-3 items-center">
        <div className="relative w-full sm:w-80">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-dark-500" />
          <input
            type="text"
            placeholder="Tìm theo tiêu đề hoặc slug..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-9 pr-4 py-2 rounded-lg bg-dark-800 border border-dark-700/50 text-sm text-white placeholder:text-dark-500 focus:outline-none focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/20 transition-all font-medium"
          />
        </div>

        <button
          onClick={fetchComics}
          disabled={loading}
          className="p-2 rounded-lg bg-dark-800 border border-dark-700/50 hover:bg-dark-700 text-dark-300 hover:text-white transition-all cursor-pointer group"
          title="Tải lại dữ liệu"
        >
          <RefreshCw size={16} className={loading ? 'animate-spin text-emerald-400' : 'group-hover:text-emerald-400 transition-colors'} />
        </button>
      </div>

      {/* Table */}
      <DataTable
        columns={columns}
        data={debouncedSearch ? displayedItems : items}
        isLoading={loading}
        isError={error}
        errorMessage="Không thể tải danh sách truyện."
        emptyMessage="Chưa có truyện nào phù hợp."
        skeletonRows={6}
        pagination={!debouncedSearch ? {
          page,
          totalPages,
          totalElements,
          onPageChange: setPage,
        } : undefined}
      />

      <AdminComicModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        initialData={editingComic}
        onSuccess={handleModalSuccess}
      />
    </div>
  );
}
