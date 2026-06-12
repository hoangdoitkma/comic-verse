import { useState, useEffect, useMemo } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Send, Clock } from 'lucide-react';
import DataTable from '../../components/DataTable';
import notificationService from '../../services/notificationService';

const COLUMNS = [
  {
    key: 'id',
    label: 'ID',
    minWidth: '60px',
    render: (val) => <span className="text-dark-500 tabular-nums">#{val}</span>,
  },
  {
    key: 'title',
    label: 'Tiêu đề',
    minWidth: '200px',
    render: (val, row) => (
      <div>
        <p className="font-medium text-white">{val}</p>
        <p className="text-xs text-dark-400 line-clamp-1 mt-0.5">{row.message}</p>
      </div>
    ),
  },
  {
    key: 'type',
    label: 'Loại',
    minWidth: '120px',
    render: (val) => {
      const colors = {
        SYSTEM: 'bg-emerald-500/15 text-emerald-400',
        PROMOTION: 'bg-purple-500/15 text-purple-400',
        UPDATE: 'bg-blue-500/15 text-blue-400',
      };
      const defaultColor = 'bg-primary-500/15 text-primary-400';
      return (
        <span className={`px-2 py-1 rounded text-xs font-medium ${colors[val] || defaultColor}`}>
          {val}
        </span>
      );
    },
  },
  {
    key: 'targetUserName',
    label: 'Người nhận',
    minWidth: '140px',
    render: (val) => (
      <span className={val === 'Broadcast' ? 'text-primary-400 font-medium' : 'text-dark-200'}>
        {val || 'Broadcast'}
      </span>
    ),
  },
  {
    key: 'createdAt',
    label: 'Thời gian gửi',
    minWidth: '160px',
    render: (val) => <span className="text-sm text-dark-300">{new Date(val).toLocaleString()}</span>,
  },
];

export default function NotificationsPage() {
  const { addToast } = useOutletContext();
  const [history, setHistory] = useState([]);
  const [isLoadingHistory, setIsLoadingHistory] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [pagination, setPagination] = useState({
    page: 0,
    totalPages: 1,
    totalElements: 0,
    size: 6
  });
  const [filters, setFilters] = useState({
    type: '',
    isBroadcast: ''
  });

  const [formData, setFormData] = useState({
    title: '',
    message: '',
    type: 'SYSTEM',
    isBroadcast: true,
    targetUserId: '',
  });

  const fetchHistory = async (page = 0) => {
    setIsLoadingHistory(true);
    try {
      const params = {
        page: page,
        size: pagination.size,
      };
      if (filters.type) params.type = filters.type;
      if (filters.isBroadcast !== '') params.isBroadcast = filters.isBroadcast;

      const res = await notificationService.getNotificationHistory(params);
      const pageData = res.data;
      setHistory(pageData.content || []);
      setPagination(prev => ({
        ...prev,
        page: pageData.number || 0,
        totalPages: pageData.totalPages || 1,
        totalElements: pageData.totalElements || 0
      }));
    } catch (err) {
      addToast('Lỗi khi tải lịch sử thông báo', 'error');
    } finally {
      setIsLoadingHistory(false);
    }
  };

  useEffect(() => {
    fetchHistory(0);
  }, [filters]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.title.trim() || !formData.message.trim()) {
      addToast('Vui lòng nhập đầy đủ tiêu đề và nội dung', 'error');
      return;
    }
    if (!formData.isBroadcast && !formData.targetUserId) {
      addToast('Vui lòng nhập ID người nhận', 'error');
      return;
    }

    setIsSubmitting(true);
    try {
      const payload = {
        title: formData.title,
        message: formData.message,
        type: formData.type,
        isBroadcast: formData.isBroadcast,
        targetUserId: formData.isBroadcast ? null : parseInt(formData.targetUserId, 10),
      };

      await notificationService.sendNotification(payload);
      addToast('Gửi thông báo thành công', 'success');
      setFormData({
        title: '',
        message: '',
        type: 'SYSTEM',
        isBroadcast: true,
        targetUserId: '',
      });
      fetchHistory(0);
    } catch (err) {
      addToast(err.response?.data?.message || 'Có lỗi xảy ra khi gửi thông báo', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const memoizedTable = useMemo(() => (
    <DataTable
      columns={COLUMNS}
      data={history}
      isLoading={isLoadingHistory}
      emptyMessage="Chưa có thông báo nào được gửi."
      pagination={{
        page: pagination.page,
        totalPages: pagination.totalPages,
        totalElements: pagination.totalElements,
        onPageChange: (newPage) => fetchHistory(newPage)
      }}
    />
  ), [history, isLoadingHistory, pagination]);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-white">Quản lý Thông báo</h1>
        <p className="text-sm text-dark-400 mt-1">
          Gửi thông báo hệ thống hoặc xem lịch sử thông báo đã gửi
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Send Form */}
        <div className="lg:col-span-1">
          <div className="bg-dark-900 border border-dark-700/50 rounded-xl p-5 sticky top-24">
            <h2 className="text-base font-semibold text-white mb-4 flex items-center gap-2">
              <Send size={18} className="text-emerald-400" />
              Tạo thông báo mới
            </h2>

            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Type & Target */}
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-dark-300 mb-2">Loại thông báo</label>
                  <select
                    value={formData.type}
                    onChange={(e) => setFormData({ ...formData, type: e.target.value })}
                    className="w-full px-4 py-2.5 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-white focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/20 transition-all outline-none"
                  >
                    <option value="SYSTEM">Hệ thống (SYSTEM)</option>
                    <option value="PROMOTION">Khuyến mãi (PROMOTION)</option>
                    <option value="UPDATE">Cập nhật (UPDATE)</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-dark-300 mb-2">Đối tượng nhận</label>
                  <div className="flex gap-4">
                    <label className="flex items-center gap-2 cursor-pointer">
                      <input
                        type="radio"
                        name="target"
                        checked={formData.isBroadcast}
                        onChange={() => setFormData({ ...formData, isBroadcast: true, targetUserId: '' })}
                        className="w-4 h-4 text-emerald-500 bg-dark-800 border-dark-600 focus:ring-emerald-500 outline-none"
                      />
                      <span className="text-sm text-dark-200">Gửi tất cả (Broadcast)</span>
                    </label>
                    <label className="flex items-center gap-2 cursor-pointer">
                      <input
                        type="radio"
                        name="target"
                        checked={!formData.isBroadcast}
                        onChange={() => setFormData({ ...formData, isBroadcast: false })}
                        className="w-4 h-4 text-emerald-500 bg-dark-800 border-dark-600 focus:ring-emerald-500 outline-none"
                      />
                      <span className="text-sm text-dark-200">Cá nhân</span>
                    </label>
                  </div>
                </div>

                {!formData.isBroadcast && (
                  <div className="animate-fade-in">
                    <label className="block text-sm font-medium text-dark-300 mb-2">User ID người nhận</label>
                    <input
                      type="number"
                      value={formData.targetUserId}
                      onChange={(e) => setFormData({ ...formData, targetUserId: e.target.value })}
                      placeholder="Nhập ID (VD: 12)"
                      className="w-full px-4 py-2.5 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-white placeholder:text-dark-500 focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/20 transition-all outline-none"
                    />
                  </div>
                )}
              </div>

              {/* Title & Message */}
              <div>
                <label className="block text-sm font-medium text-dark-300 mb-2">Tiêu đề</label>
                <input
                  type="text"
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                  placeholder="Tiêu đề thông báo..."
                  className="w-full px-4 py-2.5 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-white placeholder:text-dark-500 focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/20 transition-all outline-none"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-dark-300 mb-2">Nội dung</label>
                <textarea
                  value={formData.message}
                  onChange={(e) => setFormData({ ...formData, message: e.target.value })}
                  placeholder="Nội dung chi tiết..."
                  rows={4}
                  className="w-full px-4 py-2.5 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-white placeholder:text-dark-500 focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/20 transition-all outline-none resize-none"
                />
              </div>

              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full inline-flex items-center justify-center gap-2 px-4 py-2.5 mt-2 rounded-lg text-sm font-semibold text-white bg-emerald-600 hover:bg-emerald-500 transition-all shadow-lg shadow-emerald-500/20 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isSubmitting ? (
                  <div className="animate-spin w-4 h-4 border-2 border-white/80 border-t-transparent rounded-full" />
                ) : (
                  <Send size={16} />
                )}
                Gửi thông báo
              </button>
            </form>
          </div>
        </div>

        {/* History Table */}
        <div className="lg:col-span-2 space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div className="flex items-center gap-2">
              <Clock size={18} className="text-dark-400" />
              <h2 className="text-base font-semibold text-white">Lịch sử đã gửi</h2>
            </div>
            
            {/* Table Filters */}
            <div className="flex items-center gap-3">
              <select
                value={filters.type}
                onChange={(e) => setFilters({ ...filters, type: e.target.value })}
                className="px-3 py-2 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-dark-200 outline-none"
              >
                <option value="">Tất cả loại</option>
                <option value="SYSTEM">Hệ thống</option>
                <option value="PROMOTION">Khuyến mãi</option>
                <option value="UPDATE">Cập nhật</option>
              </select>
              <select
                value={filters.isBroadcast}
                onChange={(e) => setFilters({ ...filters, isBroadcast: e.target.value })}
                className="px-3 py-2 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-dark-200 outline-none"
              >
                <option value="">Mọi đối tượng</option>
                <option value="true">Gửi tất cả (Broadcast)</option>
                <option value="false">Cá nhân</option>
              </select>
            </div>
          </div>
          {memoizedTable}
        </div>
      </div>
    </div>
  );
}
