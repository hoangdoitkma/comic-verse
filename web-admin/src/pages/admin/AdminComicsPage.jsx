import { useState, useEffect, useCallback } from 'react';
import { useOutletContext, Link } from 'react-router-dom';
import {
  BookOpen,
  Search,
  Plus,
  RefreshCw,
  MoreVertical,
  Link as LinkIcon,
  Trash2,
  Image as ImageIcon,
  CheckCircle,
  XCircle,
  Eye,
  Settings,
  Pencil,
  RotateCcw
} from 'lucide-react';
import adminService from '../../services/adminService';
import AdminComicModal from '../../components/AdminComicModal';
import Pagination from '../../components/Pagination';

export default function AdminComicsPage() {
  const { addToast } = useOutletContext();

  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const limit = 10;
  
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
      setError('');
      // In a real system you'd pass the search term to backend. Here we just fetch all and possibly filter frontend, or pass query if backed supports it.
      // Assuming backend does not support search by title on this endpoint, we will just fetch.
      const res = await adminService.getAllComicsForAdmin({ page, size: limit });
      if (res && res.content) {
        setItems(res.content || []);
        setTotalPages(res.totalPages || 1);
      } else {
        setItems([]);
        setError('Không thể lấy dữ liệu.');
      }
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || 'Lỗi kết nối server');
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

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold border-b-2 border-emerald-500 pb-1 inline-block text-white">
            Quản lý Truyện
          </h1>
          <p className="text-sm text-dark-400 mt-2">
            Xem, thêm, sửa, xóa tất cả các truyện trên hệ thống
          </p>
        </div>

        <button
          onClick={handleOpenAdd}
          className="inline-flex items-center justify-center gap-2 px-5 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-medium transition-all shadow-lg shadow-emerald-500/20 active:scale-95 cursor-pointer"
        >
          <Plus size={18} />
          <span>Thêm truyện</span>
        </button>
      </div>

      {/* Toolbar */}
      <div className="bg-dark-900 border border-dark-700/50 rounded-2xl p-4 flex flex-col md:flex-row gap-4 justify-between items-center shadow-xl shadow-black/10">
        <div className="relative w-full md:w-96">
          <Search size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-dark-500" />
          <input
            type="text"
            placeholder="Tìm theo tiêu đề hoặc slug..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-11 pr-4 py-2.5 rounded-xl bg-dark-800 border border-dark-700/50 text-sm text-white placeholder:text-dark-500 focus:outline-none focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/20 transition-all font-medium"
          />
        </div>

        <button
          onClick={fetchComics}
          disabled={loading}
          className="w-full md:w-auto p-2.5 rounded-xl bg-dark-800 border border-dark-700/50 hover:bg-dark-700 text-dark-300 hover:text-white transition-all cursor-pointer group"
          title="Tải lại dữ liệu"
        >
          <RefreshCw size={18} className={loading ? 'animate-spin text-emerald-400' : 'group-hover:text-emerald-400 transition-colors'} />
        </button>
      </div>

      {/* Table */}
      <div className="bg-dark-900 border border-dark-700/50 rounded-2xl shadow-xl shadow-black/10 overflow-hidden">
        <div className="overflow-x-auto custom-scrollbar">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-dark-800/50 text-dark-400 text-xs uppercase font-medium tracking-wider border-b border-dark-700/50">
                <th className="px-6 py-4">ID</th>
                <th className="px-6 py-4">Truyện</th>
                <th className="px-6 py-4">Thông số</th>
                <th className="px-6 py-4">Cập nhật</th>
                <th className="px-6 py-4">Trạng thái</th>
                <th className="px-6 py-4 text-center">Xóa (Mềm)</th>
                <th className="px-6 py-4 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-dark-700/50 text-sm text-dark-200">
              {loading ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-dark-400">
                    <RefreshCw size={24} className="animate-spin mx-auto mb-3 text-emerald-500/50" />
                    <p>Đang tải dữ liệu...</p>
                  </td>
                </tr>
              ) : error ? (
                <tr>
                  <td colSpan={6} className="px-6 py-8 text-center text-red-400 bg-red-500/5">
                    {error}
                  </td>
                </tr>
              ) : displayedItems.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-dark-400">
                    <BookOpen size={32} className="mx-auto mb-3 text-dark-600" />
                    <p>Chưa có truyện nào phù hợp</p>
                  </td>
                </tr>
              ) : (
                displayedItems.map((item) => (
                  <tr key={item.id} className="hover:bg-dark-800/30 transition-colors group">
                    <td className="px-6 py-4 font-mono text-dark-400">#{item.id}</td>
                    
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="relative shrink-0">
                          {item.thumbnailUrl ? (
                            <img
                              src={item.thumbnailUrl}
                              alt="thumbnail"
                              className={`w-10 h-14 object-cover rounded-md border border-dark-700/50 ${item.isDeleted ? 'opacity-50 grayscale' : ''}`}
                            />
                          ) : (
                            <div className="w-10 h-14 rounded-md bg-dark-800 flex items-center justify-center text-dark-600 border border-dark-700/50">
                              <ImageIcon size={18} />
                            </div>
                          )}
                        </div>
                        <div>
                          <p className={`font-semibold text-base ${item.isDeleted ? 'text-dark-400 line-through' : 'text-dark-100'} group-hover:text-emerald-400 transition-colors cursor-pointer limit-lines-1`}>
                            {item.title}
                          </p>
                          <div className="flex items-center gap-2 text-xs mt-1 text-dark-500">
                            <span className="font-mono bg-dark-800 px-1.5 py-0.5 rounded text-dark-400">
                                {item.slug}
                            </span>
                          </div>
                        </div>
                      </div>
                    </td>

                    <td className="px-6 py-4">
                      <div className="space-y-1 text-xs">
                        <div className="flex items-center gap-2 text-dark-300">
                          <Eye size={14} className="text-dark-500" />
                          <span>{item.viewCount ? item.viewCount.toLocaleString() : 0} lượt xem</span>
                        </div>
                        <div className="flex items-center gap-2 text-dark-300">
                          <BookOpen size={14} className="text-dark-500" />
                          <span>{item.totalChapters ? item.totalChapters : 0} chương</span>
                        </div>
                        <div className="flex items-center gap-2 mt-2">
                          {item.accessType === 'VIP' ? (
                            <span className="px-1.5 py-0.5 rounded bg-yellow-500/10 text-yellow-500 font-semibold text-[10px] border border-yellow-500/20">VIP</span>
                          ) : (
                            <span className="px-1.5 py-0.5 rounded bg-emerald-500/10 text-emerald-400 font-semibold text-[10px] border border-emerald-500/20">MIỄN PHÍ</span>
                          )}
                        </div>
                      </div>
                    </td>

                    <td className="px-6 py-4 whitespace-nowrap text-dark-400 text-xs">
                      {new Date(item.updatedAt).toLocaleString('vi-VN')}
                    </td>
                    
                    <td className="px-6 py-4">
                      {item.status === 'ONGOING' ? (
                        <span className="px-2.5 py-1 text-xs font-medium bg-blue-500/10 text-blue-400 border border-blue-500/20 rounded-full whitespace-nowrap">
                          Đang tiến hành
                        </span>
                      ) : item.status === 'COMPLETED' ? (
                        <span className="px-2.5 py-1 text-xs font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded-full whitespace-nowrap">
                          Hoàn thành
                        </span>
                      ) : (
                        <span className="px-2.5 py-1 text-xs font-medium bg-red-500/10 text-red-400 border border-red-500/20 rounded-full whitespace-nowrap">
                          Tạm ngưng
                        </span>
                      )}
                    </td>

                    <td className="px-6 py-4 text-center">
                      {item.isDeleted ? (
                        <span className="inline-flex items-center justify-center px-2 py-1 rounded bg-red-500/10 text-red-400 text-xs font-semibold border border-red-500/20">
                          <Trash2 size={12} className="mr-1"/> BỊ XÓA
                        </span>
                      ) : (
                        <span className="inline-flex items-center justify-center w-6 h-6 rounded-full bg-emerald-500/10 text-emerald-400">
                          <CheckCircle size={14} />
                        </span>
                      )}
                    </td>

                    <td className="px-6 py-4 text-right">
                      <div className="relative inline-block text-left">
                        <button
                          onClick={() => toggleDropdown(item.id)}
                          className="p-1.5 rounded-lg text-dark-400 hover:text-white hover:bg-dark-700 transition-colors cursor-pointer"
                        >
                          <MoreVertical size={18} />
                        </button>

                        {/* Dropdown Menu */}
                        {openDropdownId === item.id && (
                          <>
                            <div
                              className="fixed inset-0 z-40"
                              onClick={() => setOpenDropdownId(null)}
                            />
                            <div className="absolute right-0 mt-1 w-40 bg-dark-800 border border-dark-700/50 rounded-xl shadow-xl z-50 py-1 overflow-hidden animate-scale-in">
                              
                              <button
                                onClick={() => handleOpenEdit(item)}
                                className="w-full flex items-center gap-2 px-3 py-2 text-sm text-dark-200 hover:bg-dark-700 hover:text-white transition-colors cursor-pointer"
                              >
                                <Pencil size={14} className="text-blue-400" />
                                Edit Info
                              </button>

                              {!item.isDeleted ? (
                                <button
                                    onClick={() => handleDelete(item)}
                                    className="w-full flex items-center gap-2 px-3 py-2 text-sm text-red-400 hover:bg-red-500/10 transition-colors cursor-pointer border-t border-dark-700/50"
                                >
                                    <Trash2 size={14} className="text-red-400" />
                                    Soft Delete
                                </button>
                              ) : (
                                <button
                                    onClick={() => handleRestore(item)}
                                    className="w-full flex items-center gap-2 px-3 py-2 text-sm text-emerald-400 hover:bg-emerald-500/10 transition-colors cursor-pointer border-t border-dark-700/50"
                                >
                                    <RotateCcw size={14} className="text-emerald-400" />
                                    Khôi phục
                                </button>
                              )}
                            </div>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
        
        {!loading && !error && displayedItems.length > 0 && searchTerm === '' && (
          <div className="px-6 py-4 border-t border-dark-700/50 bg-dark-900/50">
            <Pagination
              currentPage={page}
              totalPages={totalPages}
              onPageChange={(p) => setPage(p)}
            />
          </div>
        )}
      </div>

      <AdminComicModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        initialData={editingComic}
        onSuccess={handleModalSuccess}
      />
    </div>
  );
}
