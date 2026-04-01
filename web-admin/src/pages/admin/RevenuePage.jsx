import React, { useState, useEffect } from 'react';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, Legend, ResponsiveContainer,
  LineChart, Line, PieChart, Pie, Cell
} from 'recharts';
import api from '../../utils/axiosClient';
import { DollarSign, TrendingUp, AlertCircle, ShoppingBag } from 'lucide-react';

const formatVnd = (value) => {
  if (value == null) return '0 đ';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8'];

export default function RevenuePage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        setLoading(true);
        const response = await api.get('/admin/revenue/stats');
        console.log('Revenue stats response:', response);
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

  if (!data) return <div className="p-6 text-red-500">Lỗi tải dữ liệu: {error || 'Không có phản hồi từ server'}</div>;

  return (
    <div className="p-6 space-y-6 bg-slate-50 min-h-screen">
      <div>
        <h1 className="text-2xl font-bold text-slate-800">Báo cáo Doanh thu</h1>
        <p className="text-slate-500 text-sm mt-1">Thống kê doanh thu, gói VIP và tỉ lệ giao dịch thành công qua PayOS</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="bg-white p-5 rounded-xl shadow-sm border border-slate-100 flex items-center gap-4">
          <div className="p-3 bg-emerald-100 text-emerald-600 rounded-lg">
            <DollarSign size={24} />
          </div>
          <div>
            <p className="text-sm font-medium text-slate-500">Tổng doanh thu</p>
            <h3 className="text-xl font-bold text-slate-800">{formatVnd(data.totalRevenue)}</h3>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl shadow-sm border border-slate-100 flex items-center gap-4">
          <div className="p-3 bg-blue-100 text-blue-600 rounded-lg">
            <TrendingUp size={24} />
          </div>
          <div>
            <p className="text-sm font-medium text-slate-500">Tháng này</p>
            <h3 className="text-xl font-bold text-slate-800">{formatVnd(data.thisMonthRevenue)}</h3>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">
        {/* Biểu đồ doanh thu 30 ngày qua */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-100">
          <h3 className="text-lg font-bold text-slate-800 mb-4">Doanh thu 30 ngày gần đây</h3>
          <div className="h-[300px]">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={data.revenueByDate}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="date" tick={{fontSize: 12}} />
                <YAxis tickFormatter={(val) => val / 1000 + 'k'} tick={{fontSize: 12}} />
                <RechartsTooltip formatter={(val) => formatVnd(val)} />
                <Line type="monotone" dataKey="revenue" name="Doanh thu" stroke="#10b981" strokeWidth={3} dot={{r:4}} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Biểu đồ tỷ lệ bán gói VIP */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-100">
          <h3 className="text-lg font-bold text-slate-800 mb-4">Số lượng mua Gói VIP</h3>
          <div className="h-[300px]">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={data.vipPackageSales}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="name" tick={{fontSize: 12}} />
                <YAxis tick={{fontSize: 12}} />
                <RechartsTooltip />
                <Bar dataKey="count" name="Số lượt mua" fill="#3b82f6" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Trạng thái giao dịch */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-100 lg:col-span-2">
          <h3 className="text-lg font-bold text-slate-800 mb-4">Tỉ lệ trạng thái giao dịch (Tất cả)</h3>
          <div className="h-[300px]">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie 
                  data={data.transactionRates} 
                  dataKey="value" 
                  nameKey="name" 
                  cx="50%" 
                  cy="50%" 
                  outerRadius={100} 
                  label={(entry) => `${entry.name} (${entry.value})`}
                >
                  {data.transactionRates.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.name === 'Thành công' ? '#10b981' : (entry.name === 'Thất bại' ? '#ef4444' : '#f59e0b')} />
                  ))}
                </Pie>
                <RechartsTooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
}
