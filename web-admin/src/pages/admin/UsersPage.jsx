import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Search, Filter, ShieldAlert, ShieldCheck } from 'lucide-react';
import DataTable from '../../components/DataTable';
import adminService from '../../services/adminService';

const ROLE_OPTIONS = ['', 'USER', 'UPLOADER', 'ADMIN'];
const STATUS_OPTIONS = ['', 'ACTIVE', 'BANNED', 'SUSPENDED'];

const statusStyles = {
  ACTIVE: 'bg-emerald-500/15 text-emerald-400 border-emerald-500/20',
  BANNED: 'bg-red-500/15 text-red-400 border-red-500/20',
  SUSPENDED: 'bg-amber-500/15 text-amber-400 border-amber-500/20',
};

const roleStyles = {
  ADMIN: 'bg-purple-500/15 text-purple-400 border-purple-500/20',
  UPLOADER: 'bg-blue-500/15 text-blue-400 border-blue-500/20',
  USER: 'bg-dark-700/50 text-dark-300 border-dark-600/30',
};

export default function UsersPage() {
  const { addToast } = useOutletContext();

  const [users, setUsers] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isError, setIsError] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Filters
  const [roleFilter, setRoleFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const fetchUsers = async () => {
    setIsLoading(true);
    setIsError(false);
    try {
      const params = { page, size: 6 };
      if (roleFilter) params.role = roleFilter;
      if (statusFilter) params.status = statusFilter;
      const data = await adminService.getUsers(params);
      setUsers(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (err) {
      console.error('Error fetching users:', err);
      setIsError(true);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [page, roleFilter, statusFilter]);

  // Reset page when filters change
  useEffect(() => {
    setPage(0);
  }, [roleFilter, statusFilter]);

  const handleToggleStatus = async (user) => {
    const newStatus = user.status === 'ACTIVE' ? 'BANNED' : 'ACTIVE';
    try {
      await adminService.updateUserStatus(user.id, { status: newStatus });
      addToast(
        newStatus === 'BANNED'
          ? `Đã khoá tài khoản "${user.displayName || user.email}".`
          : `Đã mở khoá tài khoản "${user.displayName || user.email}".`,
        'success'
      );
      fetchUsers();
    } catch (err) {
      console.error('Toggle status error:', err);
      addToast(err.response?.data?.message || 'Lỗi khi cập nhật trạng thái.', 'error');
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
      key: 'displayName',
      label: 'Tên hiển thị',
      minWidth: '160px',
      render: (val, row) => (
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-gradient-to-br from-primary-400 to-primary-600 flex items-center justify-center flex-shrink-0">
            <span className="text-xs font-bold text-white">
              {(val || row.email || '?').charAt(0).toUpperCase()}
            </span>
          </div>
          <div className="min-w-0">
            <p className="text-sm font-medium text-white truncate">{val || '—'}</p>
            <p className="text-xs text-dark-500 truncate">{row.email}</p>
          </div>
        </div>
      ),
    },
    {
      key: 'role',
      label: 'Vai trò',
      align: 'center',
      render: (val) => (
        <span
          className={`inline-flex px-2.5 py-1 rounded-full text-xs font-semibold border ${roleStyles[val] || roleStyles.USER}`}
        >
          {val}
        </span>
      ),
    },
    {
      key: 'status',
      label: 'Trạng thái',
      align: 'center',
      render: (val) => (
        <span
          className={`inline-flex px-2.5 py-1 rounded-full text-xs font-semibold border ${statusStyles[val] || statusStyles.ACTIVE}`}
        >
          {val}
        </span>
      ),
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
      minWidth: '130px',
      render: (_, row) => {
        if (row.role === 'ADMIN') return <span className="text-xs text-dark-600">—</span>;
        const isBanned = row.status === 'BANNED';
        return (
          <button
            onClick={() => handleToggleStatus(row)}
            className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
              isBanned
                ? 'bg-emerald-600/15 text-emerald-400 border border-emerald-500/20 hover:bg-emerald-600/25'
                : 'bg-red-600/15 text-red-400 border border-red-500/20 hover:bg-red-600/25'
            }`}
          >
            {isBanned ? (
              <>
                <ShieldCheck size={14} /> Mở khoá
              </>
            ) : (
              <>
                <ShieldAlert size={14} /> Khoá
              </>
            )}
          </button>
        );
      },
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-white">Quản lý Users</h1>
        <p className="text-sm text-dark-400 mt-1">
          Quản lý tài khoản người dùng trong hệ thống
        </p>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative">
          <Filter size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-dark-500 pointer-events-none" />
          <select
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
            className="pl-9 pr-8 py-2.5 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-dark-200
              focus:outline-none focus:border-emerald-500/50 appearance-none cursor-pointer"
          >
            <option value="">Tất cả vai trò</option>
            {ROLE_OPTIONS.filter(Boolean).map((r) => (
              <option key={r} value={r}>{r}</option>
            ))}
          </select>
        </div>
        <div className="relative">
          <Filter size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-dark-500 pointer-events-none" />
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="pl-9 pr-8 py-2.5 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-dark-200
              focus:outline-none focus:border-emerald-500/50 appearance-none cursor-pointer"
          >
            <option value="">Tất cả trạng thái</option>
            {STATUS_OPTIONS.filter(Boolean).map((s) => (
              <option key={s} value={s}>{s}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Table */}
      <DataTable
        columns={columns}
        data={users}
        isLoading={isLoading}
        isError={isError}
        emptyMessage="Không tìm thấy người dùng nào."
        pagination={{
          page,
          totalPages,
          totalElements,
          onPageChange: setPage,
        }}
      />
    </div>
  );
}
