import React, { useState, useEffect } from 'react';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, Legend, ResponsiveContainer,
  LineChart, Line, PieChart, Pie, Cell
} from 'recharts';
import api from '../../utils/axiosClient';
import { DollarSign, TrendingUp } from 'lucide-react';

const formatVnd = (value) => {
  if (value == null) return '0 đ';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const PIE_COLORS = {
  'Thành công': '#10b981',
  'Thất bại': '#ef4444',
  'Đang chờ': '#f59e0b',
};
const PIE_FALLBACK = '#6366f1';

const CustomTooltip = ({ active, payload, label, formatter }) => {
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

export default function RevenuePage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        setLoading(true);
        const response = await api.get('/admin/revenue/stats');
        setData(response);
      } catch (error) {
        console.error('Failed to load revenue stats', error);
        setError(error.response?.data?.message || error.message || 'Lỗi không xác định');
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
      </div>
    );
  }

  if (!data) return <div className="p-6 text-red-400">Lỗi tải dữ liệu: {error || 'Không có phản hồi từ server'}</div>;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-white">Báo cáo Doanh thu</h1>
        <p className="text-sm text-dark-400 mt-1">Thống kê doanh thu, gói VIP và tỉ lệ giao dịch thành công qua PayOS</p>
      </div>

      {/* Stat Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-dark-900 border border-dark-700/50 rounded-xl p-5 flex items-center gap-4">
          <div className="p-3 bg-emerald-500/15 rounded-lg">
            <DollarSign size={24} className="text-emerald-400" />
          </div>
          <div>
            <p className="text-sm font-medium text-dark-400">Tổng doanh thu</p>
            <h3 className="text-xl font-bold text-white">{formatVnd(data.totalRevenue)}</h3>
          </div>
        </div>

        <div className="bg-dark-900 border border-dark-700/50 rounded-xl p-5 flex items-center gap-4">
          <div className="p-3 bg-blue-500/15 rounded-lg">
            <TrendingUp size={24} className="text-blue-400" />
          </div>
          <div>
            <p className="text-sm font-medium text-dark-400">Tháng này</p>
            <h3 className="text-xl font-bold text-white">{formatVnd(data.thisMonthRevenue)}</h3>
          </div>
        </div>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Biểu đồ doanh thu 30 ngày */}
        <div className="bg-dark-900 border border-dark-700/50 rounded-xl p-4">
          <h3 className="text-sm font-semibold text-white mb-3">Doanh thu 30 ngày gần đây</h3>
          <div className="h-[220px]">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={data.revenueByDate}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="rgba(255,255,255,0.06)" />
                <XAxis dataKey="date" tick={{ fontSize: 10, fill: '#6b7280' }} axisLine={{ stroke: 'rgba(255,255,255,0.1)' }} tickLine={false} />
                <YAxis tickFormatter={(val) => val / 1000 + 'k'} tick={{ fontSize: 10, fill: '#6b7280' }} axisLine={false} tickLine={false} width={35} />
                <RechartsTooltip content={<CustomTooltip formatter={formatVnd} />} />
                <Line type="monotone" dataKey="revenue" name="Doanh thu" stroke="#10b981" strokeWidth={2} dot={{ r: 3, fill: '#10b981', stroke: '#064e3b', strokeWidth: 2 }} activeDot={{ r: 5, fill: '#10b981' }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Biểu đồ gói VIP */}
        <div className="bg-dark-900 border border-dark-700/50 rounded-xl p-4">
          <h3 className="text-sm font-semibold text-white mb-3">Số lượng mua Gói VIP</h3>
          <div className="h-[220px]">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={data.vipPackageSales}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="rgba(255,255,255,0.06)" />
                <XAxis dataKey="name" tick={{ fontSize: 10, fill: '#6b7280' }} axisLine={{ stroke: 'rgba(255,255,255,0.1)' }} tickLine={false} />
                <YAxis tick={{ fontSize: 10, fill: '#6b7280' }} axisLine={false} tickLine={false} width={25} />
                <RechartsTooltip content={<CustomTooltip />} />
                <Bar dataKey="count" name="Số lượt mua" fill="#6366f1" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Trạng thái giao dịch */}
        <div className="bg-dark-900 border border-dark-700/50 rounded-xl p-4">
          <h3 className="text-sm font-semibold text-white mb-3">Tỉ lệ trạng thái giao dịch</h3>
          <div className="h-[220px]">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie 
                  data={data.transactionRates} 
                  dataKey="value" 
                  nameKey="name" 
                  cx="50%" 
                  cy="45%" 
                  outerRadius={65}
                  innerRadius={32}
                  strokeWidth={0}
                >
                  {data.transactionRates.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={PIE_COLORS[entry.name] || PIE_FALLBACK} />
                  ))}
                </Pie>
                <RechartsTooltip content={<CustomTooltip />} />
                <Legend 
                  wrapperStyle={{ fontSize: '12px' }}
                  formatter={(value, entry) => {
                    const item = data.transactionRates.find(r => r.name === value);
                    return <span className="text-dark-300 text-xs">{value} ({item?.value || 0})</span>;
                  }}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
}
