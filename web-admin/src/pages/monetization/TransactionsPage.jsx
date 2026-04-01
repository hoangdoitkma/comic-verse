import React, { useState, useEffect } from "react";
import { CreditCard, Search, ArrowUpDown } from "lucide-react";
import adminService from "../../services/adminService";

export default function TransactionsPage() {
  const [transactions, setTransactions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchTransactions = async (pageIndex = 0) => {
    try {
      setIsLoading(true);
      const res = await adminService.getTransactions({ page: pageIndex, size: 10 });
      // adminService đã unwrap 2 lần: axios(response.data) → adminService(response.data)
      // res = Page object trực tiếp: { content: [...], totalPages, number }
      console.log('Transactions response:', res);
      if (res && res.content) {
        setTransactions(res.content);
        setTotalPages(res.totalPages || 0);
        setPage(res.number || 0);
      } else {
        setError("Không có dữ liệu");
      }
    } catch (err) {
      setError(err.message || "Failed to load transactions.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions(0);
  }, []);

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      fetchTransactions(newPage);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case "SUCCESS": return "bg-green-500/10 text-green-500";
      case "PENDING": return "bg-yellow-500/10 text-yellow-500";
      case "FAILED": return "bg-red-500/10 text-red-500";
      default: return "bg-gray-500/10 text-gray-500";
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center bg-gray-900 border border-gray-800 p-6 rounded-xl">
        <div className="flex items-center gap-4">
          <div className="p-3 bg-indigo-500/10 rounded-xl">
            <CreditCard className="w-8 h-8 text-indigo-500" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-white">Lịch sử giao dịch VIP</h1>
            <p className="text-gray-400 mt-1">Quản lý và theo dõi các khoản thanh toán đơn hàng gói VIP</p>
          </div>
        </div>
      </div>

      <div className="bg-gray-900 border border-gray-800 rounded-xl max-w-full overflow-x-auto">
        <div className="p-4 border-b border-gray-800 flex justify-between items-center">
          <h2 className="text-lg font-semibold text-white">Danh sách Giao dịch</h2>
          <button onClick={() => fetchTransactions(page)} className="text-sm bg-gray-800 hover:bg-gray-700 text-white px-4 py-2 rounded-lg transition-colors">
            Làm mới
          </button>
        </div>

        {isLoading ? (
          <div className="flex justify-center items-center h-48">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
          </div>
        ) : error ? (
          <div className="text-red-500 text-center p-6 border border-red-500/20 rounded-xl m-4 bg-red-500/5">
            {error}
          </div>
        ) : (
          <div className="w-full">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-gray-800 text-gray-400 text-sm">
                  <th className="p-4 font-semibold w-[150px]">Mã đơn hàng</th>
                  <th className="p-4 font-semibold w-[200px]">Người dùng</th>
                  <th className="p-4 font-semibold w-[180px]">Gói VIP</th>
                  <th className="p-4 font-semibold w-[150px]">Số tiền</th>
                  <th className="p-4 font-semibold w-[150px]">Trạng thái</th>
                  <th className="p-4 font-semibold w-[180px]">Ngày tạo</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-800 text-sm">
                {transactions.map((tx) => (
                  <tr key={tx.id} className="hover:bg-gray-800/50 transition-colors">
                    <td className="p-4 font-medium text-white">{tx.orderCode}</td>
                    <td className="p-4 text-gray-300">
                      <div>{tx.userDisplayName || "Unknown"}</div>
                      <div className="text-xs text-gray-500">{tx.userEmail}</div>
                    </td>
                    <td className="p-4 text-gray-300">{tx.packageName || "N/A"}</td>
                    <td className="p-4 font-medium text-white">
                      {new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(tx.amount)}
                    </td>
                    <td className="p-4">
                      <span className={`px-2.5 py-1 text-xs font-semibold rounded-full ${getStatusColor(tx.status)}`}>
                        {tx.status || "UNKNOWN"}
                      </span>
                    </td>
                    <td className="p-4 text-gray-400">
                      {new Date(tx.createdAt).toLocaleString("vi-VN", { dateStyle: "short", timeStyle: "short" })}
                    </td>
                  </tr>
                ))}
                {transactions.length === 0 && (
                  <tr>
                    <td colSpan="6" className="p-8 text-center text-gray-500">
                      Không có giao dịch nào.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination */ }
        <div className="p-4 border-t border-gray-800 flex items-center justify-between">
          <div className="text-sm text-gray-400">
            Trang {page + 1} / {totalPages == 0 ? 1 : totalPages}
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => handlePageChange(page - 1)}
              disabled={page === 0}
              className="px-4 py-2 bg-gray-800 text-white rounded-lg hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              Trước
            </button>
            <button
              onClick={() => handlePageChange(page + 1)}
              disabled={page >= totalPages - 1}
              className="px-4 py-2 bg-gray-800 text-white rounded-lg hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              Sau
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
