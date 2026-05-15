import { useState, useEffect } from "react";
import { useOutletContext } from "react-router-dom";
import { CreditCard } from "lucide-react";
import DataTable from "../../components/DataTable";
import adminService from "../../services/adminService";

const statusStyles = {
  SUCCESS: "bg-emerald-500/15 text-emerald-400 border-emerald-500/20",
  PENDING: "bg-amber-500/15 text-amber-400 border-amber-500/20",
  FAILED: "bg-red-500/15 text-red-400 border-red-500/20",
};

const formatVnd = (value) => {
  if (value == null) return "0 đ";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
};

export default function TransactionsPage() {
  const { addToast } = useOutletContext();

  const [transactions, setTransactions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isError, setIsError] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const fetchTransactions = async () => {
    setIsLoading(true);
    setIsError(false);
    try {
      const res = await adminService.getTransactions({ page, size: 6 });
      setTransactions(res.content || []);
      setTotalPages(res.totalPages || 0);
      setTotalElements(res.totalElements || 0);
    } catch (err) {
      console.error("Error fetching transactions:", err);
      setIsError(true);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions();
  }, [page]);

  const columns = [
    {
      key: "orderCode",
      label: "Mã đơn hàng",
      minWidth: "130px",
      render: (val) => (
        <span className="font-medium text-white tabular-nums">{val}</span>
      ),
    },
    {
      key: "userDisplayName",
      label: "Người dùng",
      minWidth: "180px",
      render: (val, row) => (
        <div className="min-w-0">
          <p className="text-sm font-medium text-white truncate">
            {val || "Unknown"}
          </p>
          <p className="text-xs text-dark-500 truncate">{row.userEmail}</p>
        </div>
      ),
    },
    {
      key: "packageName",
      label: "Gói VIP",
      minWidth: "120px",
      render: (val) => (
        <span className="text-dark-300">{val || "N/A"}</span>
      ),
    },
    {
      key: "amount",
      label: "Số tiền",
      minWidth: "120px",
      render: (val) => (
        <span className="font-medium text-white tabular-nums">
          {formatVnd(val)}
        </span>
      ),
    },
    {
      key: "status",
      label: "Trạng thái",
      align: "center",
      render: (val) => (
        <span
          className={`inline-flex px-2.5 py-1 rounded-full text-xs font-semibold border ${
            statusStyles[val] || statusStyles.PENDING
          }`}
        >
          {val || "UNKNOWN"}
        </span>
      ),
    },
    {
      key: "createdAt",
      label: "Ngày tạo",
      minWidth: "140px",
      render: (val) => {
        if (!val) return "—";
        return (
          <span className="text-dark-400 text-xs tabular-nums">
            {new Date(val).toLocaleString("vi-VN", {
              dateStyle: "short",
              timeStyle: "short",
            })}
          </span>
        );
      },
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <div className="p-3 bg-primary-500/15 rounded-lg">
          <CreditCard size={24} className="text-primary-400" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-white">
            Lịch sử giao dịch VIP
          </h1>
          <p className="text-sm text-dark-400 mt-1">
            Quản lý và theo dõi các khoản thanh toán đơn hàng gói VIP
          </p>
        </div>
      </div>

      {/* Table */}
      <DataTable
        columns={columns}
        data={transactions}
        isLoading={isLoading}
        isError={isError}
        emptyMessage="Không có giao dịch nào."
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
