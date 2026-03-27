import React, { useState, useEffect } from 'react';
import { 
  TrendingUp, 
  AlertCircle, 
  Users, 
  Eye,
  ArrowUpRight,
  ArrowUp,
  ArrowDown,
  DollarSign,
  BookOpen,
  Crown,
  Clock
} from 'lucide-react';
import { 
  AreaChart, 
  Area, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip as RechartsTooltip, 
  ResponsiveContainer,
  LineChart,
  Line
} from 'recharts';

import adminService from '../../services/adminService';
import ComicDetailModal from '../../components/ComicDetailModal';

// --- Helper: Format VNĐ ---
const formatVnd = (value) => {
  if (value == null) return '0 đ';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const formatCompact = (value) => {
  if (value == null) return '0';
  if (value >= 1_000_000_000) return `${(value / 1_000_000_000).toFixed(1)}B`;
  if (value >= 1_000_000) return `${(value / 1_000_000).toFixed(0)}M`;
  if (value >= 1_000) return `${(value / 1_000).toFixed(1)}K`;
  return new Intl.NumberFormat('vi-VN').format(value);
};

// --- Các Component Con ---

const KpiCard = ({ 
  title, 
  primaryValue, 
  icon: Icon, 
  iconBgClass = 'bg-indigo-50', 
  iconColorClass = 'text-indigo-600',
  trendValue, 
  trendDirection = 'up', // 'up' | 'down'
  footerItems = [] // [{ text, highlight, highlightColor, dot, dotColor }]
}) => {
  const isUp = trendDirection === 'up';
  const TrendIcon = isUp ? ArrowUp : ArrowDown;
  const trendColorClass = isUp ? 'text-emerald-600 bg-emerald-50' : 'text-red-600 bg-red-50';

  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-100 p-5 flex flex-col justify-between min-h-[160px] hover:shadow-md transition-shadow duration-200">
      {/* Tầng 1: Header */}
      <div className="flex justify-between items-start">
        <p className="text-sm font-medium text-slate-400 tracking-wide">{title}</p>
        <div className={`p-2.5 rounded-xl ${iconBgClass} ${iconColorClass} shrink-0`}>
          <Icon size={20} strokeWidth={2.2} />
        </div>
      </div>

      {/* Tầng 2: Body - Primary Metric + Trend */}
      <div className="flex items-end gap-2.5 mt-3">
        <h3 className="text-2xl font-extrabold text-slate-800 leading-none tracking-tight">
          {primaryValue}
        </h3>
        {trendValue && (
          <span className={`inline-flex items-center gap-0.5 text-xs font-semibold px-2 py-0.5 rounded-full ${trendColorClass}`}>
            <TrendIcon size={12} strokeWidth={2.5} />
            {isUp ? '+' : ''}{trendValue}%
          </span>
        )}
      </div>

      {/* Tầng 3: Footer - Secondary Metrics */}
      {footerItems.length > 0 && (
        <div className="flex items-center flex-wrap gap-x-3 gap-y-1 mt-3 pt-3 border-t border-slate-100">
          {footerItems.map((item, idx) => (
            <React.Fragment key={idx}>
              {idx > 0 && <span className="text-slate-200">|</span>}
              <span className="inline-flex items-center text-xs text-slate-500">
                {item.dot && (
                  <span className="relative mr-1.5 flex h-2.5 w-2.5">
                    <span className={`animate-ping absolute inline-flex h-full w-full rounded-full opacity-75 ${item.dotColor || 'bg-emerald-400'}`}></span>
                    <span className={`relative inline-flex rounded-full h-2.5 w-2.5 ${item.dotColor || 'bg-emerald-500'}`}></span>
                  </span>
                )}
                {item.text}
                {item.highlight && (
                  <span className={`ml-1 font-bold ${item.highlightColor || 'text-amber-600'}`}>
                    {item.highlight}
                  </span>
                )}
              </span>
            </React.Fragment>
          ))}
        </div>
      )}
    </div>
  );
};

const RevenueChart = ({ data }) => {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-100 p-6 h-full">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h3 className="text-lg font-bold text-slate-800">Tổng quan Doanh thu</h3>
          <p className="text-sm text-slate-500">Doanh thu 12 tháng gần nhất (VNĐ)</p>
        </div>
      </div>
      <div className="h-72 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={data} margin={{ top: 10, right: 10, left: 10, bottom: 0 }}>
            <defs>
              <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#4f46e5" stopOpacity={0.3}/>
                <stop offset="95%" stopColor="#4f46e5" stopOpacity={0}/>
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
            <XAxis 
              dataKey="name" 
              axisLine={false} 
              tickLine={false} 
              tick={{ fontSize: 12, fill: '#64748b' }} 
              dy={10}
            />
            <YAxis 
              axisLine={false} 
              tickLine={false} 
              tick={{ fontSize: 12, fill: '#64748b' }}
              tickFormatter={(value) => `${value / 1000000}M`}
              dx={-10}
            />
            <RechartsTooltip 
              formatter={(value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)}
              contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
            />
            <Area 
              type="monotone" 
              dataKey="revenue" 
              stroke="#4f46e5" 
              strokeWidth={3}
              fillOpacity={1} 
              fill="url(#colorRevenue)" 
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

const UploadActivityChart = ({ data }) => {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-100 p-6 h-full">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h3 className="text-lg font-bold text-slate-800">Hoạt động Upload</h3>
          <p className="text-sm text-slate-500">Truyện và chương mới 7 ngày qua</p>
        </div>
      </div>
      <div className="h-72 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={data} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
            <XAxis 
              dataKey="name" 
              axisLine={false} 
              tickLine={false} 
              tick={{ fontSize: 12, fill: '#64748b' }}
              dy={10} 
            />
            <YAxis 
              axisLine={false} 
              tickLine={false} 
              tick={{ fontSize: 12, fill: '#64748b' }}
            />
            <RechartsTooltip 
              contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
            />
            <Line type="monotone" dataKey="chapters" name="Chương mới" stroke="#0ea5e9" strokeWidth={3} dot={{ r: 4 }} activeDot={{ r: 6 }} />
            <Line type="monotone" dataKey="comics" name="Truyện mới" stroke="#f43f5e" strokeWidth={3} dot={{ r: 4 }} activeDot={{ r: 6 }} />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

const TopComicsList = ({ comics, onComicClick }) => {
  if (!comics || comics.length === 0) return null;

  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-100 p-6 h-full">
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-lg font-bold text-slate-800">Top Truyện Nổi Bật</h3>
        <button className="text-sm font-medium text-indigo-600 hover:text-indigo-700 flex items-center">
          Xem tất cả <ArrowUpRight size={16} className="ml-1" />
        </button>
      </div>
      <div className="space-y-4">
        {comics.map((comic, index) => (
          <div
            key={comic.id}
            onClick={() => onComicClick?.(comic.id)}
            className="flex items-center p-3 hover:bg-indigo-50/60 rounded-lg transition-colors border border-transparent hover:border-indigo-100 cursor-pointer group"
          >
            <div className="font-bold text-slate-400 w-6 text-center">{index + 1}</div>
            <img 
              src={comic.thumbnail || 'https://via.placeholder.com/50x70'} 
              alt={comic.name} 
              className="w-10 h-14 rounded object-cover ml-2 mr-4 shadow-sm" 
            />
            <div className="flex-1 min-w-0">
              <h4 className="text-sm font-bold text-slate-800 truncate group-hover:text-indigo-700 transition-colors">{comic.name}</h4>
              <div className="flex items-center text-xs text-slate-500 mt-1">
                <Eye size={12} className="mr-1" />
                {new Intl.NumberFormat('vi-VN').format(comic.views)} lượt xem
              </div>
            </div>
            <div className="text-right ml-4">
              <div className="text-sm font-bold text-emerald-600">
                {comic.revenue ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(comic.revenue) : '-'}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

// Helper để tính toán relative time (10 phút trước, 2 giờ trước)
const getRelativeTime = (dateString) => {
  const date = new Date(dateString);
  const now = new Date();
  const diffInSeconds = Math.floor((now - date) / 1000);
  
  if (diffInSeconds < 60) return `${diffInSeconds} giây trước`;
  if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} phút trước`;
  if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)} giờ trước`;
  return `${Math.floor(diffInSeconds / 86400)} ngày trước`;
};

// Status badge config cho RecentComicsList
const comicStatusConfig = {
  PENDING: { label: 'Chờ duyệt', className: 'bg-amber-100 text-amber-700 border-amber-200' },
  APPROVED: { label: 'Đã duyệt', className: 'bg-emerald-100 text-emerald-700 border-emerald-200' },
  ONGOING: { label: 'Đang ra', className: 'bg-blue-100 text-blue-700 border-blue-200' },
  COMPLETED: { label: 'Hoàn thành', className: 'bg-slate-100 text-slate-700 border-slate-200' },
  REJECTED: { label: 'Từ chối', className: 'bg-red-100 text-red-700 border-red-200' },
};

const RecentComicsList = ({ comics, onComicClick }) => {
  if (!comics || comics.length === 0) return null;

  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-100 p-6 h-full flex flex-col">
      <div className="flex justify-between items-center mb-5 shrink-0">
        <h3 className="text-lg font-bold text-slate-800">Truyện Mới Cập Nhật</h3>
        <button className="text-sm font-medium text-indigo-600 hover:text-indigo-700 flex items-center">
          Xem tất cả <ArrowUpRight size={16} className="ml-1" />
        </button>
      </div>
      <div className="overflow-y-auto h-[400px] scrollbar-auto-hide -mr-2 pr-2">
        <div className="space-y-1">
          {comics.map((comic) => {
            const statusInfo = comicStatusConfig[comic.status] || comicStatusConfig.ONGOING;
            return (
              <div
                key={comic.id}
                onClick={() => onComicClick?.(comic.id)}
                className="flex items-center p-3 hover:bg-indigo-50/60 rounded-lg transition-colors border border-transparent hover:border-indigo-100 cursor-pointer group"
              >
                <img 
                  src={comic.thumbnail || 'https://via.placeholder.com/50x70'} 
                  alt={comic.title} 
                  className="w-10 h-14 rounded object-cover mr-3 shadow-sm shrink-0" 
                />
                <div className="flex-1 min-w-0">
                  <h4 className="text-sm font-bold text-slate-800 truncate group-hover:text-indigo-700 transition-colors">
                    {comic.title}
                  </h4>
                  <p className="text-xs text-slate-500 mt-0.5 truncate">
                    {comic.uploaderName || 'Unknown'}
                  </p>
                </div>
                <div className="text-right ml-3 shrink-0 flex flex-col items-end gap-1">
                  {comic.status === 'PENDING' ? (
                    <span className={`text-[10px] px-2 py-0.5 rounded-full border font-medium ${statusInfo.className}`}>
                      {statusInfo.label}
                    </span>
                  ) : (
                    <span className="inline-flex items-center text-xs text-slate-400">
                      <Clock size={11} className="mr-1" />
                      {comic.updatedAt ? getRelativeTime(comic.updatedAt) : '—'}
                    </span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

const RecentTransactions = ({ transactions }) => {
  if (!transactions || transactions.length === 0) return null;

  const getBadgeStyle = (type) => {
    switch(type) {
      case 'Diamond': return 'bg-cyan-100 text-cyan-700 border-cyan-200';
      case 'Gold': return 'bg-amber-100 text-amber-700 border-amber-200';
      case 'Silver': return 'bg-slate-100 text-slate-700 border-slate-200';
      default: return 'bg-indigo-100 text-indigo-700 border-indigo-200';
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-100 p-6 h-full">
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-lg font-bold text-slate-800">Giao dịch VIP Gần Nhất</h3>
        <button className="text-sm font-medium text-indigo-600 hover:text-indigo-700 flex items-center">
          Xem tất cả <ArrowUpRight size={16} className="ml-1" />
        </button>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full text-sm text-left">
          <thead className="text-xs text-slate-500 uppercase bg-slate-50 rounded-lg">
            <tr>
              <th className="px-4 py-3 rounded-l-lg">Người dùng</th>
              <th className="px-4 py-3">Gói VIP</th>
              <th className="px-4 py-3">Thời gian</th>
              <th className="px-4 py-3 text-right rounded-r-lg">Số tiền</th>
            </tr>
          </thead>
          <tbody>
            {transactions.map((trx) => (
              <tr key={trx.id} className="border-b border-slate-100 last:border-0 hover:bg-slate-50/50 transition-colors">
                <td className="px-4 py-3 font-medium text-slate-800">@{trx.user}</td>
                <td className="px-4 py-3">
                  <span className={`text-xs px-2.5 py-1 rounded-full border font-medium ${getBadgeStyle(trx.type)}`}>
                    {trx.packageName}
                  </span>
                </td>
                <td className="px-4 py-3 text-slate-500">{getRelativeTime(trx.time)}</td>
                <td className="px-4 py-3 text-right font-bold text-slate-800">
                  {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(trx.amount)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

// --- Main Page Component ---

const DashboardPage = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedComicId, setSelectedComicId] = useState(null);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        const stats = await adminService.getDashboardSummary();
        setData(stats);
      } catch (error) {
        console.error('Lỗi khi tải dữ liệu dashboard:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) {
    return (
      <div className="p-6 max-w-7xl mx-auto space-y-6 flex items-center justify-center min-h-[500px]">
        <div className="flex flex-col items-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mb-4"></div>
          <p className="text-slate-500">Đang tải dữ liệu, vui lòng đợi...</p>
        </div>
      </div>
    );
  }

  if (!data) {
    return (
      <div className="p-6 max-w-7xl mx-auto text-center text-red-500 min-h-[500px] flex items-center justify-center">
        Không thể tải dữ liệu Dashboard. Vui lòng kiểm tra lại kết nối.
      </div>
    );
  }
  
  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-slate-800">Dashboard</h1>
        <p className="text-slate-500 mt-1">Tổng quan hoạt động hệ thống ComicHub</p>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
        {/* Thẻ 1: Doanh thu tháng này */}
        <KpiCard 
          title="Doanh thu tháng này"
          primaryValue={formatVnd(data.totalRevenue)}
          icon={DollarSign}
          iconBgClass="bg-emerald-50"
          iconColorClass="text-emerald-600"
          trendValue={12}
          trendDirection="up"
          footerItems={[
            { text: `Tổng doanh thu: ${formatCompact(data.totalAllTimeRevenue)}` }
          ]}
        />

        {/* Thẻ 2: Quản lý Nội dung */}
        <KpiCard 
          title="Quản lý Nội dung"
          primaryValue={`${(data.totalComics || 0).toLocaleString()} truyện`}
          icon={BookOpen}
          iconBgClass="bg-blue-50"
          iconColorClass="text-blue-600"
          footerItems={[
            { text: `Tổng: ${(data.totalComics || 0).toLocaleString()} truyện` },
            { 
              text: 'Chờ duyệt:', 
              highlight: `${data.pendingApprovals || 0}`, 
              highlightColor: (data.pendingApprovals > 0) ? 'text-amber-600' : 'text-slate-600' 
            }
          ]}
        />

        {/* Thẻ 3: Cộng đồng */}
        <KpiCard 
          title="Cộng đồng"
          primaryValue={`${(data.totalUsers || 0).toLocaleString()} người`}
          icon={Users}
          iconBgClass="bg-violet-50"
          iconColorClass="text-violet-600"
          trendValue={data.totalNewUsers > 0 ? Math.round((data.totalNewUsers / Math.max(data.totalUsers - data.totalNewUsers, 1)) * 100) : null}
          trendDirection="up"
          footerItems={[
            { text: `Mới: +${(data.totalNewUsers || 0).toLocaleString()} tháng này` },
            { text: `${data.onlineUsers || 0} online`, dot: true, dotColor: 'bg-emerald-500' }
          ]}
        />

        {/* Thẻ 4: Thống kê VIP */}
        <KpiCard 
          title="Thống kê VIP"
          primaryValue={`${(data.activeVipUsers || 0).toLocaleString()} VIP`}
          icon={Crown}
          iconBgClass="bg-amber-50"
          iconColorClass="text-amber-600"
          footerItems={[
            { text: `Gói bán tháng này:`, highlight: `${data.vipSoldThisMonth || 0}`, highlightColor: 'text-indigo-600' }
          ]}
        />
      </div>

      {/* Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        <div className="lg:col-span-7">
          <RevenueChart data={data.revenueChart || []} />
        </div>
        <div className="lg:col-span-5">
          <UploadActivityChart data={data.uploadActivity || []} />
        </div>
      </div>

      {/* Bottom Lists Grid: Top Comics + Recent Comics */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 pb-8">
        <TopComicsList comics={data.topComics || []} onComicClick={setSelectedComicId} />
        <RecentComicsList comics={data.recentComics || []} onComicClick={setSelectedComicId} />
      </div>

      {/* Comic Detail Modal */}
      <ComicDetailModal
        isOpen={!!selectedComicId}
        onClose={() => setSelectedComicId(null)}
        comicId={selectedComicId}
      />
    </div>
  );
};

export default DashboardPage;
