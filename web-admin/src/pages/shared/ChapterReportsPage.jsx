import { useCallback, useEffect, useState } from 'react';
import { useLocation, useOutletContext } from 'react-router-dom';
import { AlertOctagon, CheckCircle, Eye, RefreshCw, Search, XCircle } from 'lucide-react';
import ActionModal from '../../components/ActionModal';
import ReportChapterDetailModal from '../../components/ReportChapterDetailModal';
import chapterReportService from '../../services/chapterReportService';

const reportTypeLabels = {
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

const themeByRole = {
  admin: {
    activeTab: 'border-emerald-500 text-emerald-400',
    focus: 'focus:border-emerald-500/50 focus:ring-emerald-500/20',
  },
  uploader: {
    activeTab: 'border-primary-500 text-primary-400',
    focus: 'focus:border-primary-500/50 focus:ring-primary-500/20',
  },
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
      <td className="px-4 py-4"><div className="h-4 w-48 rounded bg-dark-700" /></td>
      <td className="px-4 py-4"><div className="h-4 w-24 rounded bg-dark-700" /></td>
      <td className="px-4 py-4"><div className="h-4 w-56 rounded bg-dark-700" /></td>
      <td className="px-4 py-4"><div className="h-6 w-24 rounded-full bg-dark-700" /></td>
      <td className="px-4 py-4"><div className="h-4 w-24 rounded bg-dark-700" /></td>
      <td className="px-4 py-4"><div className="h-8 w-20 rounded bg-dark-700" /></td>
    </tr>
  );
}

function formatDate(value) {
  if (!value) return '-';
  return new Date(value).toLocaleString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}

function getReportLocation(report) {
  if (report.pageNumber) return `Trang ${report.pageNumber}`;
  if (typeof report.paragraphIndex === 'number') return `Đoạn ${report.paragraphIndex + 1}`;
  return 'Chưa có vị trí';
}

export default function ChapterReportsPage() {
  const location = useLocation();
  const isAdmin = location.pathname.startsWith('/admin');
  const theme = themeByRole[isAdmin ? 'admin' : 'uploader'];
  const { addToast } = useOutletContext();

  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('PENDING');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [detailReport, setDetailReport] = useState(null);
  const [resolveModal, setResolveModal] = useState({ open: false, type: 'resolve', report: null });
  const [resolveNote, setResolveNote] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchReports = useCallback(async (page) => {
    setLoading(true);
    try {
      const data = activeTab === 'PENDING'
        ? await chapterReportService.getPendingReports(isAdmin, page, 10)
        : await chapterReportService.getHandledReports(isAdmin, page, 10);
      setReports(data.content || []);
      setTotalPages(data.totalPages || 0);
      setCurrentPage(page);
    } catch (err) {
      console.error('Report list error:', err);
      addToast(err.response?.data?.message || 'Không thể tải danh sách báo cáo.', 'error');
      setReports([]);
    } finally {
      setLoading(false);
    }
  }, [activeTab, addToast, isAdmin]);

  useEffect(() => {
    fetchReports(0);
  }, [fetchReports]);

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
        addToast('Đã ghi nhận báo cáo là đã xử lý.', 'success');
      } else {
        await chapterReportService.rejectReport(isAdmin, resolveModal.report.id, data);
        addToast('Đã từ chối báo cáo.', 'success');
      }
      setResolveModal({ open: false, type: 'resolve', report: null });
      fetchReports(0);
    } catch (err) {
      console.error('Handle report error:', err);
      addToast(err.response?.data?.message || 'Không thể cập nhật báo cáo lúc này.', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const filteredReports = reports.filter((report) => {
    const keyword = searchTerm.trim().toLowerCase();
    if (!keyword) return true;
    return [report.chapterTitle, report.comicTitle, report.reporterName, report.reason]
      .filter(Boolean)
      .some((value) => value.toLowerCase().includes(keyword));
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white">Quản lý báo cáo lỗi chương</h1>
          <p className="text-sm text-dark-400 mt-1">
            Xem vị trí người dùng báo lỗi, đọc lại chương và cập nhật trạng thái xử lý.
          </p>
        </div>
        <button
          onClick={() => fetchReports(0)}
          className="inline-flex items-center gap-2 px-4 py-2 rounded-lg text-sm bg-dark-800 text-dark-200 hover:text-white border border-dark-700 transition cursor-pointer"
        >
          <RefreshCw size={16} />
          Làm mới
        </button>
      </div>

      <div className="flex items-center gap-4 border-b border-dark-700/50">
        <button
          onClick={() => setActiveTab('PENDING')}
          className={`px-4 py-3 text-sm font-medium border-b-2 transition-colors cursor-pointer ${
            activeTab === 'PENDING'
              ? theme.activeTab
              : 'border-transparent text-dark-400 hover:text-dark-200 hover:border-dark-600'
          }`}
        >
          Chờ xử lý
        </button>
        <button
          onClick={() => setActiveTab('HANDLED')}
          className={`px-4 py-3 text-sm font-medium border-b-2 transition-colors cursor-pointer ${
            activeTab === 'HANDLED'
              ? theme.activeTab
              : 'border-transparent text-dark-400 hover:text-dark-200 hover:border-dark-600'
          }`}
        >
          Lịch sử xử lý
        </button>
      </div>

      <div className="bg-dark-900/60 border border-dark-700/40 rounded-2xl overflow-hidden">
        <div className="p-4 border-b border-dark-700/40">
          <div className="relative max-w-sm">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-dark-500" />
            <input
              type="text"
              placeholder="Tìm truyện, chương, người báo cáo..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className={`w-full pl-9 pr-4 py-2 rounded-lg bg-dark-800 border border-dark-700/50 text-sm text-dark-200 placeholder:text-dark-500 focus:outline-none focus:ring-1 transition-all ${theme.focus}`}
            />
          </div>
        </div>

        <div className="overflow-x-auto min-h-[320px]">
          <table className="w-full">
            <thead>
              <tr className="border-b border-dark-700/50 text-left">
                <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase">Truyện & chương</th>
                <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase">Người dùng</th>
                <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase">Báo cáo</th>
                <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase">Trạng thái</th>
                <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase">Ngày tạo</th>
                <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase text-center">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                [...Array(5)].map((_, index) => <SkeletonRow key={index} />)
              ) : filteredReports.length === 0 ? (
                <tr>
                  <td colSpan="6" className="py-20 text-center text-dark-400">
                    Không có báo cáo nào.
                  </td>
                </tr>
              ) : (
                filteredReports.map((report) => (
                  <tr key={report.id} className="border-b border-dark-700/20 hover:bg-dark-800/50 transition-colors">
                    <td className="px-4 py-3 min-w-[220px]">
                      <p className="font-medium text-dark-100">{report.comicTitle || 'Không rõ truyện'}</p>
                      <p className="text-xs text-dark-500 mt-1">
                        {report.chapterTitle || `Chương ${report.chapterNumber || report.chapterId}`}
                        <span className="mx-1.5">•</span>
                        {getReportLocation(report)}
                      </p>
                    </td>
                    <td className="px-4 py-3 text-sm text-dark-300">
                      {report.reporterName || 'Không rõ'}
                    </td>
                    <td className="px-4 py-3 min-w-[260px]">
                      <p className="text-sm text-white font-medium">
                        {report.typeDescription || reportTypeLabels[report.type] || report.type}
                      </p>
                      <p className="text-xs text-dark-400 mt-1 max-w-[280px] truncate" title={report.reason || ''}>
                        {report.reason || 'Không có mô tả thêm'}
                      </p>
                      {activeTab !== 'PENDING' && report.adminNotes && (
                        <p className="text-xs text-dark-500 mt-1 max-w-[280px] truncate">
                          <span className="text-dark-300">Ghi chú:</span> {report.adminNotes}
                        </p>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      <StatusBadge status={report.status} />
                    </td>
                    <td className="px-4 py-3 text-sm text-dark-400 tabular-nums">
                      {formatDate(report.createdAt)}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-center gap-2">
                        <button
                          onClick={() => setDetailReport(report)}
                          className="p-2 rounded hover:bg-primary-500/20 text-primary-300 transition cursor-pointer"
                          title="Xem chi tiết và đọc lại chương"
                        >
                          <Eye size={18} />
                        </button>
                        {report.status === 'PENDING' ? (
                          <>
                            <button
                              onClick={() => handleResolveOpen(report, 'resolve')}
                              className="p-2 rounded hover:bg-emerald-500/20 text-emerald-400 transition cursor-pointer"
                              title="Ghi nhận đã xử lý"
                            >
                              <CheckCircle size={18} />
                            </button>
                            <button
                              onClick={() => handleResolveOpen(report, 'reject')}
                              className="p-2 rounded hover:bg-red-500/20 text-red-400 transition cursor-pointer"
                              title="Từ chối báo cáo"
                            >
                              <XCircle size={18} />
                            </button>
                          </>
                        ) : null}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

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
        onClose={() => setResolveModal({ open: false, type: 'resolve', report: null })}
        title={resolveModal.type === 'resolve' ? 'Ghi nhận đã xử lý' : 'Từ chối báo cáo'}
        onConfirm={handleResolveSubmit}
        confirmText={resolveModal.type === 'resolve' ? 'Đã xử lý' : 'Từ chối'}
        confirmVariant={resolveModal.type === 'resolve' ? 'primary' : 'danger'}
        isLoading={isSubmitting}
      >
        <div className="space-y-3">
          <div className="flex items-start gap-3 rounded-lg border border-dark-700/60 bg-dark-800/50 p-3">
            <AlertOctagon size={18} className="text-amber-300 mt-0.5 shrink-0" />
            <p className="text-sm text-dark-300">
              Báo cáo <span className="text-white">{reportTypeLabels[resolveModal.report?.type] || resolveModal.report?.type}</span>
              {' '}tại {resolveModal.report ? getReportLocation(resolveModal.report) : 'vị trí chưa rõ'} của chương{' '}
              <span className="text-white">{resolveModal.report?.chapterTitle || resolveModal.report?.chapterId}</span>.
            </p>
          </div>
          <div>
            <label className="text-sm text-dark-400 block mb-1">Ghi chú xử lý</label>
            <textarea
              className="w-full bg-dark-800 border border-dark-700 rounded-lg p-2 text-dark-200 text-sm focus:outline-none focus:border-primary-500/50"
              rows={3}
              value={resolveNote}
              onChange={(e) => setResolveNote(e.target.value)}
              placeholder="Ví dụ: đã tải lại ảnh trang 8, nội dung chương đã đúng..."
            />
          </div>
        </div>
      </ActionModal>

      <ReportChapterDetailModal
        key={detailReport?.id || 'closed'}
        isOpen={!!detailReport}
        report={detailReport}
        onClose={() => setDetailReport(null)}
      />
    </div>
  );
}
