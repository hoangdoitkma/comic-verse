import { useState, useEffect, useMemo } from 'react';
import { useOutletContext, useNavigate } from 'react-router-dom';
import { Search, Calendar, Check, XCircle, RefreshCw, Eye } from 'lucide-react';
import DataTable from '../../components/DataTable';
import ActionModal from '../../components/ActionModal';
import adminService from '../../services/adminService';

export default function ApprovalQueuePage() {
  const { addToast } = useOutletContext();
  const navigate = useNavigate();

  // Data state
  const [logs, setLogs] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isError, setIsError] = useState(false);

  // Filter state
  const [searchTerm, setSearchTerm] = useState('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');

  const [rejectModal, setRejectModal] = useState({ open: false, log: null });
  const [rejectReason, setRejectReason] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // ======= Fetch Data =======
  const fetchLogs = async () => {
    setIsLoading(true);
    setIsError(false);
    try {
      const data = await adminService.getPendingLogs();
      setLogs(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Error fetching pending logs:', err);
      setIsError(true);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  // ======= Filtered & Grouped Data =======
  const groupedComics = useMemo(() => {
    let result = logs;

    // 1. Filter
    if (searchTerm.trim()) {
      const term = searchTerm.toLowerCase();
      result = result.filter(
        (log) =>
          log.comicTitle?.toLowerCase().includes(term) ||
          log.chapterTitle?.toLowerCase().includes(term)
      );
    }

    if (dateFrom) {
      const from = new Date(dateFrom);
      result = result.filter((log) => new Date(log.createdAt) >= from);
    }
    if (dateTo) {
      const to = new Date(dateTo);
      to.setHours(23, 59, 59, 999);
      result = result.filter((log) => new Date(log.createdAt) <= to);
    }

    // 2. Group by comicId
    const groups = {};
    result.forEach((log) => {
      if (!groups[log.comicId]) {
        groups[log.comicId] = {
          comicId: log.comicId,
          comicTitle: log.comicTitle,
          uploaderName: log.uploaderName,
          pendingCount: 0,
          latestUpload: log.createdAt,
          logs: [],
        };
      }
      groups[log.comicId].logs.push(log);
      groups[log.comicId].pendingCount += 1;
      
      if (new Date(log.createdAt) > new Date(groups[log.comicId].latestUpload)) {
        groups[log.comicId].latestUpload = log.createdAt;
      }
    });

    return Object.values(groups).sort(
      (a, b) => new Date(b.latestUpload) - new Date(a.latestUpload)
    );
  }, [logs, searchTerm, dateFrom, dateTo]);

  // ======= Actions =======
  const handleApprove = async (log) => {
    try {
      await adminService.reviewLog(log.id, { status: 'APPROVED' });
      addToast(`Đã duyệt chương "${log.chapterTitle}" thành công!`, 'success');
      fetchLogs(); // refresh logs
    } catch (err) {
      console.error('Approve error:', err);
      addToast(
        err.response?.data?.message || 'Lỗi khi duyệt. Vui lòng thử lại.',
        'error'
      );
    }
  };

  const openRejectModal = (log) => {
    setRejectModal({ open: true, log });
    setRejectReason('');
  };

  const handleReject = async () => {
    if (!rejectReason.trim()) {
      addToast('Vui lòng nhập lý do từ chối.', 'error');
      return;
    }
    setIsSubmitting(true);
    try {
      await adminService.reviewLog(rejectModal.log.id, {
        status: 'REJECTED',
        reason: rejectReason.trim(),
      });
      addToast(`Đã từ chối chương "${rejectModal.log.chapterTitle}".`, 'success');
      setRejectModal({ open: false, log: null });
      fetchLogs(); // refresh logs
    } catch (err) {
      console.error('Reject error:', err);
      addToast(
        err.response?.data?.message || 'Lỗi khi từ chối. Vui lòng thử lại.',
        'error'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  // ======= Columns for Comic Group =======
  const columns = [
    {
      key: 'comicTitle',
      label: 'Tên truyện',
      minWidth: '200px',
      render: (val) => <span className="font-medium text-white">{val || '—'}</span>,
    },
    {
      key: 'uploaderName',
      label: 'Người tải',
      minWidth: '140px',
      render: (val) => <span className="text-dark-300">{val || '—'}</span>,
    },
    {
      key: 'pendingCount',
      label: 'Số chương chờ duyệt',
      align: 'center',
      minWidth: '160px',
      render: (val) => (
        <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-500/15 text-amber-400 border border-amber-500/20">
          {val} chương
        </span>
      ),
    },
    {
      key: 'latestUpload',
      label: 'Tải lên gần nhất',
      minWidth: '160px',
      render: (val) => {
        if (!val) return '—';
        const d = new Date(val);
        return (
          <span className="text-dark-400 text-xs tabular-nums">
            {d.toLocaleDateString('vi-VN')} {d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}
          </span>
        );
      },
    },
    {
      key: 'actions',
      label: 'Hành động',
      align: 'center',
      minWidth: '120px',
      render: (_, row) => (
        <button
          onClick={() => navigate(`/admin/approval-queue/comics/${row.comicId}`)}
          className="inline-flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold
            bg-primary-600/15 text-primary-400 border border-primary-500/20
            hover:bg-primary-600/25 hover:border-primary-500/40
            transition-all cursor-pointer"
        >
          <Eye size={14} />
          Xem chi tiết
        </button>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white">Kiểm duyệt truyện</h1>
          <p className="text-sm text-dark-400 mt-1">
            Gom nhóm các chương truyện đang chờ phê duyệt theo từng truyện
          </p>
        </div>
        <button
          onClick={fetchLogs}
          disabled={isLoading}
          className="inline-flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium
            bg-dark-800 text-dark-300 border border-dark-700/50
            hover:bg-dark-700/50 hover:text-white transition-all disabled:opacity-50 cursor-pointer"
        >
          <RefreshCw size={16} className={isLoading ? 'animate-spin' : ''} />
          Làm mới
        </button>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1 max-w-md">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-dark-500" />
          <input
            type="text"
            placeholder="Tìm theo tên truyện hoặc chương..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-9 pr-4 py-2.5 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-dark-200
              placeholder:text-dark-500 focus:outline-none focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/20 transition-all"
          />
        </div>
        <div className="relative">
          <Calendar size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-dark-500 pointer-events-none" />
          <input
            type="date"
            value={dateFrom}
            onChange={(e) => setDateFrom(e.target.value)}
            className="pl-9 pr-3 py-2.5 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-dark-200
              focus:outline-none focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/20 transition-all
              [color-scheme:dark]"
          />
        </div>
        <div className="relative">
          <Calendar size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-dark-500 pointer-events-none" />
          <input
            type="date"
            value={dateTo}
            onChange={(e) => setDateTo(e.target.value)}
            className="pl-9 pr-3 py-2.5 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-dark-200
              focus:outline-none focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/20 transition-all
              [color-scheme:dark]"
          />
        </div>
        {(searchTerm || dateFrom || dateTo) && (
          <button
            onClick={() => {
              setSearchTerm('');
              setDateFrom('');
              setDateTo('');
            }}
            className="px-3 py-2.5 rounded-lg text-xs font-medium text-dark-400 hover:text-white
              bg-dark-800 border border-dark-700/50 hover:bg-dark-700/50 transition-all cursor-pointer whitespace-nowrap"
          >
            Xoá bộ lọc
          </button>
        )}
      </div>

      {!isLoading && !isError && (
        <div className="text-xs text-dark-500">
          Hiển thị {groupedComics.length} đầu truyện có chương cần duyệt
        </div>
      )}

      {/* Main Table */}
      <DataTable
        columns={columns}
        data={groupedComics}
        isLoading={isLoading}
        isError={isError}
        emptyMessage="Không có truyện nào đang chờ duyệt 🎉"
        skeletonRows={5}
      />

      {/* ===== Reject Modal ===== */}
      <ActionModal
        isOpen={rejectModal.open}
        onClose={() => setRejectModal({ open: false, log: null })}
        title="Từ chối chương"
        onConfirm={handleReject}
        confirmText="Xác nhận từ chối"
        confirmVariant="danger"
        isLoading={isSubmitting}
        // Để `z-index` cao hơn nếu hai modal cùng mở
      >
        <div className="space-y-4">
          {rejectModal.log && (
            <div className="bg-dark-800/50 rounded-lg p-3 border border-dark-700/30">
              <p className="text-xs text-dark-500 mb-1">Nội dung từ chối</p>
              <p className="text-sm text-white font-medium">
                {rejectModal.log.comicTitle} — {rejectModal.log.chapterTitle}
              </p>
            </div>
          )}
          <div>
            <label className="block text-sm font-medium text-dark-300 mb-2">
              Lý do từ chối <span className="text-red-400">*</span>
            </label>
            <textarea
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="Nhập lý do từ chối chương này..."
              rows={4}
              className="w-full px-4 py-3 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-dark-200
                placeholder:text-dark-500 focus:outline-none focus:border-red-500/50 focus:ring-1 focus:ring-red-500/20
                transition-all resize-none z-50 relative"
            />
          </div>
        </div>
      </ActionModal>
    </div>
  );
}
