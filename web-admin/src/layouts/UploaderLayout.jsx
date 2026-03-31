import { useState, useRef, useEffect } from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  BookOpen,
  LogOut,
  Menu,
  X,
  ChevronRight,
  User,
  Lock,
  ChevronDown,
  AlertOctagon
} from 'lucide-react';
import authService from '../services/authService';
import ProfileModal from '../components/ProfileModal';
import { ToastContainer, useToast } from '../components/Toast';
import NotificationBell from '../components/NotificationBell';

const menuItems = [
  {
    label: 'Bảng điều khiển',
    path: '/uploader',
    icon: LayoutDashboard,
    exact: true,
  },
  {
    label: 'Quản lý Truyện',
    path: '/uploader/comics',
    icon: BookOpen,
  },
  {
    label: 'Báo cáo lỗi',
    path: '/uploader/reports',
    icon: AlertOctagon,
  },
];

export default function UploaderLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const user = authService.getCurrentUser();
  const dropdownRef = useRef(null);
  const { toasts, addToast, dismissToast } = useToast();

  const displayName = user?.displayName || user?.email?.split('@')[0] || 'Uploader';
  const avatarLetter = displayName.charAt(0).toUpperCase();

  const isActive = (item) => {
    if (item.exact) return location.pathname === item.path;
    return location.pathname.startsWith(item.path);
  };

  const handleLogout = () => {
    authService.logout();
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

  const handleProfileSave = (data) => {
    addToast('Hồ sơ đã được cập nhật thành công!', 'success');
  };

  return (
    <div className="min-h-screen bg-dark-950 flex">
      {/* ===== Toast Notifications ===== */}
      <ToastContainer toasts={toasts} dismissToast={dismissToast} />

      {/* ===== Profile Modal ===== */}
      <ProfileModal
        isOpen={profileOpen}
        onClose={() => setProfileOpen(false)}
        onSave={handleProfileSave}
      />

      {/* ===== Mobile Overlay ===== */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black/60 backdrop-blur-sm z-40 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* ===== Sidebar ===== */}
      <aside
        className={`
          fixed top-0 left-0 z-50 h-full w-64 bg-dark-900 border-r border-dark-700/50
          flex flex-col transition-transform duration-300 ease-in-out
          lg:translate-x-0 lg:static lg:z-auto
          ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}
        `}
      >
        {/* Logo */}
        <div className="h-16 flex items-center gap-3 px-6 border-b border-dark-700/50">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center">
            <BookOpen size={18} className="text-white" />
          </div>
          <span className="text-lg font-bold text-white tracking-tight">
            Comic<span className="text-primary-400">Hub</span>
          </span>
          {/* Close button on mobile */}
          <button
            onClick={() => setSidebarOpen(false)}
            className="ml-auto lg:hidden text-dark-400 hover:text-white transition-colors"
          >
            <X size={20} />
          </button>
        </div>

        {/* Menu Navigation */}
        <nav className="flex-1 py-4 px-3 space-y-1">
          <p className="px-3 mb-3 text-[11px] font-semibold uppercase tracking-widest text-dark-500">
            Menu
          </p>
          {menuItems.map((item) => {
            const Icon = item.icon;
            const active = isActive(item);
            return (
              <Link
                key={item.path}
                to={item.path}
                onClick={() => setSidebarOpen(false)}
                className={`
                  group flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium
                  transition-all duration-200
                  ${
                    active
                      ? 'bg-primary-600/15 text-primary-400 shadow-sm shadow-primary-500/10'
                      : 'text-dark-400 hover:bg-dark-800 hover:text-dark-200'
                  }
                `}
              >
                <Icon
                  size={18}
                  className={active ? 'text-primary-400' : 'text-dark-500 group-hover:text-dark-300'}
                />
                <span className="flex-1">{item.label}</span>
                {active && (
                  <ChevronRight size={14} className="text-primary-500/60" />
                )}
              </Link>
            );
          })}
        </nav>

        {/* Logout Button */}
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
        {/* Header */}
        <header className="h-16 bg-dark-900/80 backdrop-blur-md border-b border-dark-700/50 flex items-center justify-between px-4 lg:px-6 sticky top-0 z-30">
          {/* Mobile menu toggle */}
          <button
            onClick={() => setSidebarOpen(true)}
            className="lg:hidden text-dark-400 hover:text-white transition-colors p-1"
          >
            <Menu size={22} />
          </button>

          {/* Page breadcrumb (desktop) */}
          <div className="hidden lg:flex items-center gap-2 text-sm text-dark-400">
            <span>Uploader Portal</span>
            <ChevronRight size={14} className="text-dark-600" />
            <span className="text-dark-200 font-medium">
              {menuItems.find((item) => isActive(item))?.label || 'Trang chủ'}
            </span>
          </div>

          {/* Notifications and User */}
          <div className="flex items-center gap-2 sm:gap-4 ml-auto">
            <NotificationBell addToast={addToast} />

            <div className="relative" ref={dropdownRef}>
              <button
              onClick={() => setDropdownOpen(!dropdownOpen)}
              className="flex items-center gap-3 cursor-pointer group"
            >
              <div className="text-right hidden sm:block">
                <p className="text-sm font-medium text-dark-200 leading-tight group-hover:text-white transition-colors">
                  {displayName}
                </p>
                <p className="text-[11px] text-dark-500">Uploader</p>
              </div>
              <div className="w-9 h-9 rounded-full bg-gradient-to-br from-primary-400 to-primary-600 flex items-center justify-center shadow-lg shadow-primary-500/20 ring-2 ring-transparent group-hover:ring-primary-500/30 transition-all">
                <span className="text-sm font-bold text-white">{avatarLetter}</span>
              </div>
              <ChevronDown
                size={14}
                className={`text-dark-500 transition-transform duration-200 hidden sm:block ${dropdownOpen ? 'rotate-180' : ''}`}
              />
            </button>

            {/* Dropdown Menu */}
            {dropdownOpen && (
              <div className="absolute right-0 top-full mt-2 w-56 bg-dark-800 border border-dark-700/50 rounded-xl shadow-2xl shadow-black/40 py-1.5 animate-scale-in z-50">
                {/* User info header */}
                <div className="px-4 py-3 border-b border-dark-700/40">
                  <p className="text-sm font-medium text-white truncate">{displayName}</p>
                  <p className="text-xs text-dark-500 truncate">{user?.email || ''}</p>
                </div>

                {/* Menu items */}
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

                {/* Divider + Logout */}
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

        {/* Content */}
        <main className="flex-1 p-4 lg:p-6 overflow-auto">
          <Outlet context={{ addToast }} />
        </main>
      </div>
    </div>
  );
}
