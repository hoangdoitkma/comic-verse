import { useState, useRef, useEffect, useMemo } from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import {
  ShieldCheck,
  Users,
  Layers,
  Pen,
  Crown,
  LogOut,
  Menu,
  X,
  ChevronRight,
  User,
  Lock,
  ChevronDown,
  BookOpen,
  LayoutDashboard,
  AlertOctagon,
  DollarSign,
  CreditCard
} from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import adminService from '../services/adminService';
import ProfileModal from '../components/ProfileModal';
import { ToastContainer, useToast } from '../components/Toast';
import NotificationBell from '../components/NotificationBell';

const MENU_ITEMS = [
  {
    label: 'Dashboard',
    path: '/dashboard',
    icon: LayoutDashboard,
    allowedRoles: ['ROLE_ADMIN']
  },
  {
    label: 'Quản lý Truyện',
    path: '/content/comics',
    icon: BookOpen,
    allowedRoles: ['ROLE_ADMIN', 'ROLE_UPLOADER']
  },
  {
    label: 'Kiểm duyệt truyện',
    path: '/content/approval-queue',
    icon: ShieldCheck,
    allowedRoles: ['ROLE_ADMIN']
  },
  {
    label: 'Thể loại',
    path: '/content/genres',
    icon: Layers,
    allowedRoles: ['ROLE_ADMIN']
  },
  {
    label: 'Tác giả',
    path: '/content/authors',
    icon: Pen,
    allowedRoles: ['ROLE_ADMIN']
  },
  {
    label: 'Doanh thu',
    path: '/revenue',
    icon: DollarSign,
    allowedRoles: ['ROLE_ADMIN']
  },
  {
    label: 'Gói VIP',
    path: '/monetization/vip-packages',
    icon: Crown,
    allowedRoles: ['ROLE_ADMIN']
  },
  {
    label: 'Giao dịch',
    path: '/monetization/transactions',
    icon: CreditCard,
    allowedRoles: ['ROLE_ADMIN']
  },
  {
    label: 'Người dùng',
    path: '/community/users',
    icon: Users,
    allowedRoles: ['ROLE_ADMIN']
  },
  {
    label: 'Báo cáo lỗi',
    path: '/community/reports',
    icon: AlertOctagon,
    allowedRoles: ['ROLE_ADMIN', 'ROLE_UPLOADER']
  }
];

export default function MainLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const location = useLocation();
  const { user, logout } = useAuth();
  const dropdownRef = useRef(null);
  const { toasts, addToast, dismissToast } = useToast();

  const userRoles = user?.roles || [];
  const isGlobalAdmin = userRoles.includes('ROLE_ADMIN');

  const visibleMenus = useMemo(() => {
    return MENU_ITEMS.filter(item => 
      item.allowedRoles.some(role => userRoles.includes(role))
    );
  }, [userRoles]);

  const displayName = user?.displayName || user?.email?.split('@')[0] || 'User';
  const avatarLetter = displayName.charAt(0).toUpperCase();

  const isActive = (item) => {
    if (item.path === '/dashboard') {
      return location.pathname === '/dashboard';
    }
    return location.pathname === item.path || location.pathname.startsWith(`${item.path}/`);
  };

  const handleLogout = () => {
    logout();
  };

  // Close dropdown on outside click
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setDropdownOpen(false);
      }
    };
    if (dropdownOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [dropdownOpen]);

  // Close dropdown on ESC
  useEffect(() => {
    const handleEsc = (e) => {
      if (e.key === 'Escape') setDropdownOpen(false);
    };
    if (dropdownOpen) {
      document.addEventListener('keydown', handleEsc);
    }
    return () => document.removeEventListener('keydown', handleEsc);
  }, [dropdownOpen]);

  // Polling transactions ONLY for admins
  const [lastTxCount, setLastTxCount] = useState(0);
  useEffect(() => {
    if (!isGlobalAdmin) return;
    let intervalId;
    const pollTransactions = async () => {
      try {
        const res = await adminService.getTransactions({ page: 0, size: 1 });
        if (res.success && res.data && res.data.totalElements !== undefined) {
          const currentCount = res.data.totalElements;
          setLastTxCount((prev) => {
            if (prev > 0 && currentCount > prev) {
              addToast('Có giao dịch thanh toán thành công VIP mới!', 'success');
            }
            return currentCount;
          });
        }
      } catch (e) {
        // silent fail
      }
    };
    
    pollTransactions();
    intervalId = setInterval(pollTransactions, 15000); // 15s

    return () => clearInterval(intervalId);
  }, [addToast, isGlobalAdmin]);

  const handleProfileSave = () => {
    addToast('Hồ sơ đã được cập nhật thành công!', 'success');
  };

  const currentPageLabel = visibleMenus.find((item) => isActive(item))?.label || 'Bảng điều khiển';

  return (
    <div className="min-h-screen bg-dark-950 flex">
      <ToastContainer toasts={toasts} dismissToast={dismissToast} />
      <ProfileModal
        isOpen={profileOpen}
        onClose={() => setProfileOpen(false)}
        onSave={handleProfileSave}
      />

      {/* Mobile Overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black/60 backdrop-blur-sm z-40 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* ===== Sidebar ===== */}
      <aside
        className={`fixed top-0 left-0 z-50 h-full w-64 bg-dark-900 border-r border-dark-700/50
          flex flex-col transition-transform duration-300 ease-in-out
          lg:translate-x-0 lg:static lg:z-auto
          ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}`}
      >
        <div className="h-16 flex items-center gap-3 px-6 border-b border-dark-700/50">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-emerald-500 to-emerald-700 flex items-center justify-center">
            <ShieldCheck size={18} className="text-white" />
          </div>
          <span className="text-lg font-bold text-white tracking-tight">
            Comic<span className="text-emerald-400">Hub</span>
          </span>
          <span className="ml-1 px-1.5 py-0.5 text-[10px] font-bold uppercase bg-emerald-500/15 text-emerald-400 rounded">
            Panel
          </span>
          <button
            onClick={() => setSidebarOpen(false)}
            className="ml-auto lg:hidden text-dark-400 hover:text-white transition-colors"
          >
            <X size={20} />
          </button>
        </div>

        <nav className="flex-1 py-4 px-3 space-y-1 overflow-y-auto custom-scrollbar">
          <p className="px-3 mb-3 text-[11px] font-semibold uppercase tracking-widest text-dark-500">
            Menu Chức năng
          </p>
          {visibleMenus.map((item) => {
            const Icon = item.icon;
            const active = isActive(item);
            return (
              <Link
                key={item.path}
                to={item.path}
                onClick={() => setSidebarOpen(false)}
                className={`group flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200
                  ${active
                      ? 'bg-emerald-600/15 text-emerald-400 shadow-sm shadow-emerald-500/10'
                      : 'text-dark-400 hover:bg-dark-800 hover:text-dark-200'
                  }`}
              >
                <Icon
                  size={18}
                  className={active ? 'text-emerald-400' : 'text-dark-500 group-hover:text-dark-300'}
                />
                <span className="flex-1">{item.label}</span>
                {active && (
                  <ChevronRight size={14} className="text-emerald-500/60" />
                )}
              </Link>
            );
          })}
        </nav>

        <div className="p-3 border-t border-dark-700/50">
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium
              text-dark-400 hover:bg-red-500/10 hover:text-red-400 transition-all duration-200 cursor-pointer"
          >
            <LogOut size={18} />
            <span>Đăng xuất</span>
          </button>
        </div>
      </aside>

      {/* ===== Main Area ===== */}
      <div className="flex-1 flex flex-col min-w-0">
        <header className="h-16 bg-dark-900/80 backdrop-blur-md border-b border-dark-700/50 flex items-center justify-between px-4 lg:px-6 sticky top-0 z-30">
          <button
            onClick={() => setSidebarOpen(true)}
            className="lg:hidden text-dark-400 hover:text-white transition-colors p-1"
          >
            <Menu size={22} />
          </button>

          <div className="hidden lg:flex items-center gap-2 text-sm text-dark-400">
            <span>Admin Panel</span>
            <ChevronRight size={14} className="text-dark-600" />
            <span className="text-dark-200 font-medium">{currentPageLabel}</span>
          </div>

          <div className="flex items-center gap-2 sm:gap-4 ml-auto">
            {isGlobalAdmin && <NotificationBell addToast={addToast} />}
            
            <div className="relative" ref={dropdownRef}>
              <button
              onClick={() => setDropdownOpen(!dropdownOpen)}
              className="flex items-center gap-3 cursor-pointer group"
            >
              <div className="text-right hidden sm:block">
                <p className="text-sm font-medium text-dark-200 leading-tight group-hover:text-white transition-colors">
                  {displayName}
                </p>
                <p className="text-[11px] text-dark-500">{isGlobalAdmin ? 'Admin' : 'Uploader'}</p>
              </div>
              <div className="w-9 h-9 rounded-full bg-gradient-to-br from-emerald-400 to-emerald-600 flex items-center justify-center shadow-lg shadow-emerald-500/20 ring-2 ring-transparent group-hover:ring-emerald-500/30 transition-all">
                <span className="text-sm font-bold text-white">{avatarLetter}</span>
              </div>
              <ChevronDown
                size={14}
                className={`text-dark-500 transition-transform duration-200 hidden sm:block ${dropdownOpen ? 'rotate-180' : ''}`}
              />
            </button>

            {dropdownOpen && (
              <div className="absolute right-0 top-full mt-2 w-56 bg-dark-800 border border-dark-700/50 rounded-xl shadow-2xl shadow-black/40 py-1.5 animate-scale-in z-50">
                <div className="px-4 py-3 border-b border-dark-700/40">
                  <p className="text-sm font-medium text-white truncate">{displayName}</p>
                  <p className="text-xs text-dark-500 truncate">{user?.email || ''}</p>
                </div>
                <div className="py-1.5">
                  <button
                    onClick={() => {
                      setDropdownOpen(false);
                      setProfileOpen(true);
                    }}
                    className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-dark-300 hover:bg-dark-700/50 hover:text-white transition-all cursor-pointer"
                  >
                    <User size={16} className="text-dark-500" />
                    Hồ sơ của tôi
                  </button>
                  <button
                    onClick={() => {
                      setDropdownOpen(false);
                      addToast('Tính năng đổi mật khẩu đang phát triển', 'info');
                    }}
                    className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-dark-300 hover:bg-dark-700/50 hover:text-white transition-all cursor-pointer"
                  >
                    <Lock size={16} className="text-dark-500" />
                    Đổi mật khẩu
                  </button>
                </div>
                <div className="border-t border-dark-700/40 pt-1.5">
                  <button
                    onClick={handleLogout}
                    className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-red-400 hover:bg-red-500/10 transition-all cursor-pointer"
                  >
                    <LogOut size={16} />
                    Đăng xuất
                  </button>
                </div>
              </div>
            )}
          </div>
          </div>
        </header>

        <main className="flex-1 p-4 lg:p-6 overflow-auto">
          <Outlet context={{ addToast }} />
        </main>
      </div>
    </div>
  );
}
