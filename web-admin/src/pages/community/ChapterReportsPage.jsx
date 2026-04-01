import { useState, useEffect } from 'react';
import { useOutletContext, useLocation } from 'react-router-dom';
import { AlertOctagon, CheckCircle, XCircle, Search, RefreshCw, Eye } from 'lucide-react';
import chapterReportService from '../../services/chapterReportService';
import authService from '../../services/authService';
import ActionModal from '../../components/ActionModal';

// Badge map
const typeStatusConfig = {
  IMAGE_NOT_LOADING: 'Lỗi tải ảnh',
  WRONG_CONTENT: 'Sai nội dung chương',
  TYPO_ERROR: 'Lỗi chính tả',
  DUPLICATE_CHAPTER: 'Trùng chương',
  OTHER: 'Khác',
};

const statusConfig = {
  PENDING: { label: 'Chờ xử lý', bg: 'bg-amber-500/15', text: 'text-amber-400', dot: 'bg-amber-400' },
  RESOLVED: { label: 'Đã xử lý', bg: 'bg-emerald-500/15', text: 'text-emerald-400', dot: 'bg-emerald-400' },
  REJECTED: { label: 'Từ chối', bg: 'bg-red-500/15', text: 'text-red-400', dot: 'bg-red-400' },
};

function StatusBadge({ status }) {
  const config = statusConfig[status] || statusConfig.PENDING;
  return (
    <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${config.bg} ${config.text}`}>
      <span className={`w-1.5 h-1.5 rounded-full ${config.dot}`} />
      {config.label}
    </span>
  );
}

function SkeletonRow() {
  return (
    <tr className="border-b border-dark-700/30 animate-pulse">
      <td className="px-4 py-4 w-1/4"><div className="h-4 w-3/4 rounded bg-dark-700" /></td>
      <td className="px-4 py-4"><div className="h-4 w-20 rounded bg-dark-700" /></td>
      <td className="px-4 py-4"><div className="h-4 w-1/2 rounded bg-dark-700" /></td>
      <td className="px-4 py-4"><div className="h-6 w-24 rounded-full bg-dark-700" /></td>
      <td className="px-4 py-4"><div className="h-4 w-24 rounded bg-dark-700" /></td>
      <td className="px-4 py-4"><div className="h-8 w-16 rounded bg-dark-700" /></td>
    </tr>
  );
}

export default function ChapterReportsPage() {
  const location = useLocation();
  const isAdmin = location.pathname.startsWith('/admin');
  const baseColorClass = isAdmin ? 'emerald' : 'primary';

  const { addToast } = useOutletContext();
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('PENDING'); // PENDING or RESOLVED
  
  // Pagination
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');

  // Modals
  const [resolveModal, setResolveModal] = useState({ open: false, type: 'resolve', report: null });
  const [resolveNote, setResolveNote] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    fetchReports(0);
  }, [activeTab]);

  const fetchReports = async (page) => {
    setLoading(true);
    try {
      let data;
      if (activeTab === 'PENDING') {
        data = await chapterReportService.getPendingReports(isAdmin, page, 10);
      } else {
        data = await chapterReportService.getResolvedReports(isAdmin, page, 10);
      }
      setReports(data.content || []);
      setTotalPages(data.totalPages || 0);
      setCurrentPage(page);
    } catch (err) {
      console.error('Lỗi lấy báo cáo:', err);
      addToast(err.response?.data?.message || 'Có lỗi xảy ra khi tải báo cáo.', 'error');
      setReports([]);
    } finally {
      setLoading(false);
    }
  };

  const handleResolveOpen = (report, type) => {
    setResolveModal({ open: true, type, report });
    setResolveNote('');
  };

  const handleResolveSubmit = async () => {
    setIsSubmitting(true);
    try {
      const data = { adminNotes: resolveNote };
      if (resolveModal.type === 'resolve') {
        await chapterReportService.resolveReport(isAdmin, resolveModal.report.id, data);
        addToast('Đã đánh dấu báo cáo là Đã Xử Lý', 'success');
      } else {
        await chapterReportService.rejectReport(isAdmin, resolveModal.report.id, data);
        addToast('Đã đánh dấu báo cáo là Bị Từ Chối', 'success');
      }
      setResolveModal({ open: false, type: '', report: null });
      fetchReports(0);
    } catch (err) {
      console.error('Lỗi duyệt báo cáo:', err);
      addToast(err.response?.data?.message || 'Không thể xử lý báo cáo lúc này.', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const filteredReports = reports.filter(r => 
    (r.chapterTitle && r.chapterTitle.toLowerCase().includes(searchTerm.toLowerCase())) ||
    (r.comicTitle && r.comicTitle.toLowerCase().includes(searchTerm.toLowerCase())) ||
    (r.reporterName && r.reporterName.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white">Quản lý Báo cáo</h1>
          <p className={`text-sm text-dark-400 mt-1`}>
            Khắc phục lỗi chương hiển thị mà người dùng đã báo cáo
          </p>
        </div>
        <button
          onClick={() => fetchReports(0)}
          className={`inline-flex items-center gap-2 px-4 py-2 rounded-lg text-sm bg-dark-800 text-dark-200 hover:text-white border border-dark-700 transition cursor-pointer`}
        >
          <RefreshCw size={16} />
          Làm mới
        </button>
      </div>

      {/* Tabs */}
      <div className="flex items-center gap-4 border-b border-dark-700/50">
        <button
          onClick={() => setActiveTab('PENDING')}
          className={`px-4 py-3 text-sm font-medium border-b-2 transition-colors cursor-pointer ${
            activeTab === 'PENDING'
              ? `border-${baseColorClass}-500 text-${baseColorClass}-400`
              : 'border-transparent text-dark-400 hover:text-dark-200 hover:border-dark-600'
          }`}
        >
          Chờ xử lý
        </button>
        <button
          onClick={() => setActiveTab('RESOLVED')}
          className={`px-4 py-3 text-sm font-medium border-b-2 transition-colors cursor-pointer ${
            activeTab === 'RESOLVED'
              ? `border-${baseColorClass}-500 text-${baseColorClass}-400`
              : 'border-transparent text-dark-400 hover:text-dark-200 hover:border-dark-600'
          }`}
        >
          Lịch sử xử lý
        </button>
      </div>

      <div className="bg-dark-900/60 border border-dark-700/40 rounded-2xl overflow-hidden">
        {/* Search */}
        <div className="p-4 border-b border-dark-700/40">
          <div className="relative max-w-sm">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-dark-500" />
            <input
              type="text"
              placeholder="Tìm kiếm truyện/báo cáo..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className={`w-full pl-9 pr-4 py-2 rounded-lg bg-dark-800 border border-dark-700/50 text-sm text-dark-200 placeholder:text-dark-500 focus:outline-none focus:border-${baseColorClass}-500/50 focus:ring-1 focus:ring-${baseColorClass}-500/20 transition-all`}
            />
          </div>
        </div>

        {/* Table */}
        <div className="overflow-x-auto min-h-[300px]">
          <table className="w-full">
            <thead>
              <tr className="border-b border-dark-700/50 text-left">
                <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase">Truyện & Chương</th>
                <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase">Người dùng</th>
                <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase">Báo cáo</th>
                <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase">Trạng thái</th>
                <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase">Ngày tạo</th>
                <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase text-center">Xử lý</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                [...Array(5)].map((_, i) => <SkeletonRow key={i} />)
              ) : filteredReports.length === 0 ? (
                <tr>
                  <td colSpan="6" className="py-20 text-center text-dark-400">
                    Không có báo cáo nào.
                  </td>
                </tr>
              ) : (
                filteredReports.map((report) => (
                  <tr key={report.id} className="border-b border-dark-700/20 hover:bg-dark-800/50 transition-colors">
                    <td className="px-4 py-3">
                      <p className="font-medium text-dark-200">{report.comicTitle || 'N/A'}</p>
                      <p className="text-xs text-dark-500">{report.chapterTitle || `Chương ${report.chapterId}`}</p>
                    </td>
                    <td className="px-4 py-3 text-sm text-dark-300">
                      {report.reporterName || 'N/A'}
                    </td>
                    <td className="px-4 py-3">
                      <p className="text-sm text-white font-medium">{report.typeDescription || typeStatusConfig[report.type] || report.type}</p>
                      <p className="text-xs text-dark-400 mt-1 max-w-[250px] truncate" title={report.reason}>
                        {report.reason || 'Không có mô tả thêm'}
                      </p>
                      {activeTab !== 'PENDING' && report.adminNotes && (
                        <p className="text-xs text-dark-500 mt-1"><span className="text-dark-300">Ghi chú:</span> {report.adminNotes}</p>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      <StatusBadge status={report.status} />
                    </td>
                    <td className="px-4 py-3 text-sm text-dark-400 tabular-nums">
                      {new Date(report.createdAt).toLocaleString('vi-VN', {
                        hour: '2-digit', minute: '2-digit',
                        day: '2-digit', month: '2-digit', year: 'numeric'
                      })}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-center gap-2">
                        {report.status === 'PENDING' ? (
                          <>
                            <button
                              onClick={() => handleResolveOpen(report, 'resolve')}
                              className={`p-2 rounded hover:bg-emerald-500/20 text-emerald-400 transition cursor-pointer`}
                              title="Xử lý (đã sửa)"
                            >
                              <CheckCircle size={18} />
                            </button>
                            <button
                              onClick={() => handleResolveOpen(report, 'reject')}
                              className={`p-2 rounded hover:bg-red-500/20 text-red-400 transition cursor-pointer`}
                              title="Bỏ qua báo cáo"
                            >
                              <XCircle size={18} />
                            </button>
                          </>
                        ) : (
                          <span className="text-dark-500 text-xs">Không có</span>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {!loading && totalPages > 1 && (
          <div className="p-4 border-t border-dark-700/40 flex items-center justify-between">
            <button
              disabled={currentPage === 0}
              onClick={() => fetchReports(currentPage - 1)}
              className="px-3 py-1.5 text-sm bg-dark-800 disabled:opacity-50 hover:bg-dark-700 transition rounded"
            >
              Trang trước
            </button>
            <span className="text-sm text-dark-400">
              Trang <span className="text-white">{currentPage + 1}</span> / {totalPages}
            </span>
            <button
              disabled={currentPage >= totalPages - 1}
              onClick={() => fetchReports(currentPage + 1)}
              className="px-3 py-1.5 text-sm bg-dark-800 disabled:opacity-50 hover:bg-dark-700 transition rounded"
            >
              Trang sau
            </button>
          </div>
        )}
      </div>

      <ActionModal
        isOpen={resolveModal.open}
        onClose={() => setResolveModal({ open: false, type: '', report: null })}
        title={resolveModal.type === 'resolve' ? 'Duyệt báo cáo lỗi' : 'Từ chối báo cáo'}
        onConfirm={handleResolveSubmit}
        confirmText={resolveModal.type === 'resolve' ? 'Ghi nhận sửa lỗi' : 'Từ chối'}
        confirmVariant={resolveModal.type === 'resolve' ? 'success' : 'danger'}
        isLoading={isSubmitting}
      >
        <div className="space-y-3">
          <p className="text-sm text-dark-300">
            Bạn đang thao tác với báo cáo lỗi <strong>{typeStatusConfig[resolveModal.report?.reportType]}</strong> ở chương {resolveModal.report?.chapterTitle}.
          </p>
          <div>
            <label className="text-sm text-dark-400 block mb-1">Ghi chú quản trị (không bắt buộc):</label>
            <textarea
              className="w-full bg-dark-800 border border-dark-700 rounded-lg p-2 text-dark-200 text-sm focus:outline-none"
              rows={3}
              value={resolveNote}
              onChange={(e) => setResolveNote(e.target.value)}
              placeholder="VD: Đã up lại ảnh, không bị lỗi gì..."
            />
          </div>
        </div>
      </ActionModal>
    </div>
  );
}
