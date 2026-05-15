import React, { useState, useEffect } from 'react';
import { 
  Eye,
  ArrowUp,
  ArrowDown,
  DollarSign,
  BookOpen,
  Crown,
  Users,
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

// --- Helpers ---
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

const getRelativeTime = (dateString) => {
  const date = new Date(dateString);
  const now = new Date();
  const diffInSeconds = Math.floor((now - date) / 1000);
  if (diffInSeconds < 60) return `${diffInSeconds}s trước`;
  if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}p trước`;
  if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}h trước`;
  return `${Math.floor(diffInSeconds / 86400)}d trước`;
};

// --- Custom Dark Tooltip ---
const DarkTooltip = ({ active, payload, label, formatter }) => {
  if (!active || !payload || !payload.length) return null;
  return (
    <div className="bg-dark-800 border border-dark-700/50 rounded-lg px-3 py-2 shadow-xl">
      {label && <p className="text-xs text-dark-400 mb-1">{label}</p>}
      {payload.map((entry, i) => (
        <p key={i} className="text-sm font-medium" style={{ color: entry.color || '#e2e8f0' }}>
          {entry.name}: {formatter ? formatter(entry.value) : entry.value}
        </p>
      ))}
    </div>
  );
};

// --- KPI Card (Dark) ---
const KpiCard = ({ title, primaryValue, icon: Icon, iconBgClass, iconColorClass, trendValue, trendDirection = 'up', footerItems = [] }) => {
  const isUp = trendDirection === 'up';
  const TrendIcon = isUp ? ArrowUp : ArrowDown;
  const trendColorClass = isUp ? 'text-emerald-400 bg-emerald-500/15' : 'text-red-400 bg-red-500/15';

  return (
    <div className="bg-dark-900 border border-dark-700/50 rounded-xl p-4 flex flex-col justify-between min-h-[120px]">
      <div className="flex justify-between items-start">
        <p className="text-xs font-medium text-dark-400 tracking-wide">{title}</p>
        <div className={`p-2 rounded-lg ${iconBgClass} ${iconColorClass} shrink-0`}>
          <Icon size={16} strokeWidth={2.2} />
        </div>
      </div>
      <div className="flex items-end gap-2 mt-2">
        <h3 className="text-xl font-bold text-white leading-none">{primaryValue}</h3>
        {trendValue && (
          <span className={`inline-flex items-center gap-0.5 text-[10px] font-semibold px-1.5 py-0.5 rounded-full ${trendColorClass}`}>
            <TrendIcon size={10} strokeWidth={2.5} />
            {isUp ? '+' : ''}{trendValue}%
          </span>
        )}
      </div>
      {footerItems.length > 0 && (
        <div className="flex items-center flex-wrap gap-x-3 gap-y-1 mt-2 pt-2 border-t border-dark-700/50">
          {footerItems.map((item, idx) => (
            <React.Fragment key={idx}>
              {idx > 0 && <span className="text-dark-700">|</span>}
              <span className="inline-flex items-center text-[11px] text-dark-500">
                {item.dot && (
                  <span className="relative mr-1.5 flex h-2 w-2">
                    <span className={`animate-ping absolute inline-flex h-full w-full rounded-full opacity-75 ${item.dotColor || 'bg-emerald-400'}`}></span>
                    <span className={`relative inline-flex rounded-full h-2 w-2 ${item.dotColor || 'bg-emerald-500'}`}></span>
                  </span>
                )}
                {item.text}
                {item.highlight && (
                  <span className={`ml-1 font-bold ${item.highlightColor || 'text-amber-400'}`}>
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

// --- Status config ---
const comicStatusConfig = {
  PENDING: { label: 'Chờ duyệt', className: 'bg-amber-500/15 text-amber-400 border-amber-500/20' },
  APPROVED: { label: 'Đã duyệt', className: 'bg-emerald-500/15 text-emerald-400 border-emerald-500/20' },
  ONGOING: { label: 'Đang ra', className: 'bg-blue-500/15 text-blue-400 border-blue-500/20' },
  COMPLETED: { label: 'Hoàn thành', className: 'bg-dark-700/50 text-dark-300 border-dark-600/30' },
  REJECTED: { label: 'Từ chối', className: 'bg-red-500/15 text-red-400 border-red-500/20' },
};

// --- Main Page ---
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
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="flex flex-col items-center">
          <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-emerald-500 mb-3"></div>
          <p className="text-dark-400 text-sm">Đang tải dữ liệu...</p>
        </div>
      </div>
    );
  }

  if (!data) {
    return (
      <div className="flex items-center justify-center min-h-[400px] text-red-400">
        Không thể tải dữ liệu Dashboard.
      </div>
    );
  }

  const topComics = (data.topComics || []).slice(0, 5);
  const recentComics = (data.recentComics || []).slice(0, 5);

  return (
    <div className="space-y-4">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-white">Dashboard</h1>
        <p className="text-sm text-dark-400 mt-1">Tổng quan hoạt động hệ thống ComicHub</p>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3">
        <KpiCard 
          title="Doanh thu tháng này"
          primaryValue={formatVnd(data.totalRevenue)}
          icon={DollarSign}
          iconBgClass="bg-emerald-500/15"
          iconColorClass="text-emerald-400"
          trendValue={12}
          trendDirection="up"
          footerItems={[
            { text: `Tổng: ${formatCompact(data.totalAllTimeRevenue)}` }
          ]}
        />
        <KpiCard 
          title="Quản lý Nội dung"
          primaryValue={`${(data.totalComics || 0).toLocaleString()} truyện`}
          icon={BookOpen}
          iconBgClass="bg-blue-500/15"
          iconColorClass="text-blue-400"
          footerItems={[
            { text: `Tổng: ${(data.totalComics || 0).toLocaleString()}` },
            { 
              text: 'Chờ duyệt:', 
              highlight: `${data.pendingApprovals || 0}`, 
              highlightColor: (data.pendingApprovals > 0) ? 'text-amber-400' : 'text-dark-500' 
            }
          ]}
        />
        <KpiCard 
          title="Cộng đồng"
          primaryValue={`${(data.totalUsers || 0).toLocaleString()} người`}
          icon={Users}
          iconBgClass="bg-purple-500/15"
          iconColorClass="text-purple-400"
          trendValue={data.totalNewUsers > 0 ? Math.round((data.totalNewUsers / Math.max(data.totalUsers - data.totalNewUsers, 1)) * 100) : null}
          trendDirection="up"
          footerItems={[
            { text: `Mới: +${(data.totalNewUsers || 0).toLocaleString()}` },
            { text: `${data.onlineUsers || 0} online`, dot: true, dotColor: 'bg-emerald-500' }
          ]}
        />
        <KpiCard 
          title="Thống kê VIP"
          primaryValue={`${(data.activeVipUsers || 0).toLocaleString()} VIP`}
          icon={Crown}
          iconBgClass="bg-amber-500/15"
          iconColorClass="text-amber-400"
          footerItems={[
            { text: `Gói bán tháng này:`, highlight: `${data.vipSoldThisMonth || 0}`, highlightColor: 'text-primary-400' }
          ]}
        />
      </div>

      {/* Row 2: Charts (2) + Top Comics (1) — all in one row */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-3">
        {/* Revenue Chart */}
        <div className="lg:col-span-4 bg-dark-900 border border-dark-700/50 rounded-xl p-4">
          <div className="mb-2">
            <h3 className="text-sm font-semibold text-white">Doanh thu 12 tháng</h3>
          </div>
          <div className="h-[180px]">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={data.revenueChart || []} margin={{ top: 5, right: 5, left: 0, bottom: 0 }}>
                <defs>
                  <linearGradient id="colorRevenueDark" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#10b981" stopOpacity={0.3}/>
                    <stop offset="95%" stopColor="#10b981" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="rgba(255,255,255,0.06)" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 10, fill: '#6b7280' }} />
                <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 10, fill: '#6b7280' }} tickFormatter={(v) => v >= 1000000 ? `${v/1000000}M` : formatCompact(v)} width={35} />
                <RechartsTooltip content={<DarkTooltip formatter={formatVnd} />} />
                <Area type="monotone" dataKey="revenue" stroke="#10b981" strokeWidth={2} fillOpacity={1} fill="url(#colorRevenueDark)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Upload Activity Chart */}
        <div className="lg:col-span-4 bg-dark-900 border border-dark-700/50 rounded-xl p-4">
          <div className="mb-2">
            <h3 className="text-sm font-semibold text-white">Upload 7 ngày qua</h3>
          </div>
          <div className="h-[180px]">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={data.uploadActivity || []} margin={{ top: 5, right: 5, left: 0, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="rgba(255,255,255,0.06)" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 10, fill: '#6b7280' }} />
                <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 10, fill: '#6b7280' }} width={20} />
                <RechartsTooltip content={<DarkTooltip />} />
                <Line type="monotone" dataKey="chapters" name="Chương mới" stroke="#38bdf8" strokeWidth={2} dot={{ r: 3 }} activeDot={{ r: 5 }} />
                <Line type="monotone" dataKey="comics" name="Truyện mới" stroke="#f43f5e" strokeWidth={2} dot={{ r: 3 }} activeDot={{ r: 5 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Top Comics */}
        <div className="lg:col-span-4 bg-dark-900 border border-dark-700/50 rounded-xl p-4">
          <h3 className="text-sm font-semibold text-white mb-2">Top Truyện Nổi Bật</h3>
          <div className="space-y-1.5">
            {topComics.map((comic, index) => (
              <div
                key={comic.id}
                onClick={() => setSelectedComicId(comic.id)}
                className="flex items-center p-2 hover:bg-dark-800/60 rounded-lg transition-colors cursor-pointer group"
              >
                <span className="text-xs font-bold text-dark-500 w-5 text-center shrink-0">{index + 1}</span>
                <img 
                  src={comic.thumbnail || 'https://via.placeholder.com/40x56'} 
                  alt={comic.name} 
                  className="w-8 h-11 rounded object-cover ml-2 mr-3 shrink-0" 
                />
                <div className="flex-1 min-w-0">
                  <h4 className="text-xs font-medium text-white truncate group-hover:text-primary-400 transition-colors">{comic.name}</h4>
                  <div className="flex items-center text-[10px] text-dark-500 mt-0.5">
                    <Eye size={10} className="mr-1" />
                    {new Intl.NumberFormat('vi-VN').format(comic.views)}
                  </div>
                </div>
                {comic.revenue > 0 && (
                  <span className="text-[10px] font-semibold text-emerald-400 ml-2 shrink-0">
                    {formatCompact(comic.revenue)}
                  </span>
                )}
              </div>
            ))}
            {topComics.length === 0 && (
              <p className="text-xs text-dark-500 text-center py-4">Chưa có dữ liệu</p>
            )}
          </div>
        </div>
      </div>

      {/* Row 3: Recent Comics */}
      <div className="bg-dark-900 border border-dark-700/50 rounded-xl p-4">
        <h3 className="text-sm font-semibold text-white mb-3">Truyện Mới Cập Nhật</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-2">
          {recentComics.map((comic) => {
            const statusInfo = comicStatusConfig[comic.status] || comicStatusConfig.ONGOING;
            return (
              <div
                key={comic.id}
                onClick={() => setSelectedComicId(comic.id)}
                className="flex items-center p-2.5 hover:bg-dark-800/60 rounded-lg transition-colors cursor-pointer group"
              >
                <img 
                  src={comic.thumbnail || 'https://via.placeholder.com/40x56'} 
                  alt={comic.title} 
                  className="w-8 h-11 rounded object-cover mr-3 shrink-0" 
                />
                <div className="flex-1 min-w-0">
                  <h4 className="text-xs font-medium text-white truncate group-hover:text-primary-400 transition-colors">
                    {comic.title}
                  </h4>
                  <div className="flex items-center gap-2 mt-1">
                    {comic.status === 'PENDING' ? (
                      <span className={`text-[10px] px-1.5 py-0.5 rounded-full border font-medium ${statusInfo.className}`}>
                        {statusInfo.label}
                      </span>
                    ) : (
                      <span className="inline-flex items-center text-[10px] text-dark-500">
                        <Clock size={10} className="mr-1" />
                        {comic.updatedAt ? getRelativeTime(comic.updatedAt) : '—'}
                      </span>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
          {recentComics.length === 0 && (
            <p className="text-xs text-dark-500 text-center py-4 col-span-5">Chưa có dữ liệu</p>
          )}
        </div>
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
