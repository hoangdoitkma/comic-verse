import { useState, useRef, useEffect } from 'react';
import { Bell, Check, Circle, Info } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import notificationService from '../services/notificationService';

function timeAgo(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString);
  const now = new Date();
  const diffInSeconds = Math.floor((now - date) / 1000);

  if (diffInSeconds < 60) return `Vài giây trước`;
  const diffInMinutes = Math.floor(diffInSeconds / 60);
  if (diffInMinutes < 60) return `${diffInMinutes} phút trước`;
  const diffInHours = Math.floor(diffInMinutes / 60);
  if (diffInHours < 24) return `${diffInHours} giờ trước`;
  const diffInDays = Math.floor(diffInHours / 24);
  if (diffInDays < 30) return `${diffInDays} ngày trước`;
  const diffInMonths = Math.floor(diffInDays / 30);
  if (diffInMonths < 12) return `${diffInMonths} tháng trước`;
  const diffInYears = Math.floor(diffInDays / 365);
  return `${diffInYears} năm trước`;
}

export default function NotificationBell({ addToast }) {
  const [isOpen, setIsOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const dropdownRef = useRef(null);
  const navigate = useNavigate();

  const fetchNotifications = async () => {
    try {
      setLoading(true);
      const res = await notificationService.getUserNotifications();
      if (res.status === 200) {
        setNotifications(res.data);
        const unread = res.data.filter(n => !n.isRead).length;
        setUnreadCount(unread);
      }
    } catch (error) {
      console.error('Failed to fetch notifications', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
    
    // Auto refresh unread count every 30s
    const target = setInterval(async () => {
      try {
        const res = await notificationService.getUnreadCount();
        if (res.status === 200) {
          setUnreadCount(res.data);
        }
      } catch (e) {
        /* ignore hidden polling error */
      }
    }, 30000);

    return () => clearInterval(target);
  }, []);

  // Close when clicking outside
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isOpen]);

  const toggleDropdown = () => {
    if (!isOpen) {
      fetchNotifications();
    }
    setIsOpen(!isOpen);
  };

  const handleMarkAsRead = async (e, id) => {
    e.stopPropagation();
    try {
      await notificationService.markAsRead(id);
      setNotifications(prev => prev.map(n => n.id === id ? { ...n, isRead: true } : n));
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch (error) {
      addToast && addToast('Không thể đánh dấu đã đọc', 'error');
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await notificationService.markAllAsRead();
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
      setUnreadCount(0);
      addToast && addToast('Đã đánh dấu tất cả đã đọc', 'success');
    } catch (error) {
      addToast && addToast('Có lỗi xảy ra', 'error');
    }
  };

  const handleNotificationClick = async (notif) => {
    if (!notif.isRead) {
      await handleMarkAsRead({ stopPropagation: () => {} }, notif.id);
    }
    setIsOpen(false);
    if (notif.redirectUrl) {
      navigate(notif.redirectUrl);
    }
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={toggleDropdown}
        className="w-10 h-10 rounded-full flex items-center justify-center text-dark-400 hover:text-white hover:bg-dark-800 transition-colors relative"
      >
        <Bell size={20} />
        {unreadCount > 0 && (
          <span className="absolute top-1.5 right-1.5 w-4 h-4 bg-red-500 rounded-full flex items-center justify-center text-[10px] font-bold text-white shadow-sm ring-2 ring-dark-900 border-none">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 top-full mt-2 w-80 sm:w-96 bg-dark-800 border border-dark-700/50 rounded-xl shadow-2xl shadow-black/40 overflow-hidden z-50 animate-scale-in flex flex-col max-h-[85vh]">
          {/* Header */}
          <div className="px-4 py-3 border-b border-dark-700/50 flex items-center justify-between bg-dark-800/80 backdrop-blur-md sticky top-0 z-10">
            <h3 className="text-sm font-semibold text-white">Thông báo</h3>
            {unreadCount > 0 && (
              <button
                onClick={handleMarkAllAsRead}
                className="text-xs text-primary-400 hover:text-primary-300 transition-colors flex items-center gap-1 font-medium"
              >
                <Check size={14} />
                Đánh dấu tất cả đã đọc
              </button>
            )}
          </div>

          {/* List */}
          <div className="flex-1 overflow-y-auto custom-scrollbar">
            {loading && notifications.length === 0 ? (
              <div className="py-8 text-center text-dark-500">
                <div className="animate-spin w-5 h-5 border-2 border-primary-500 border-t-transparent rounded-full mx-auto mb-2"></div>
                <p className="text-sm">Đang tải...</p>
              </div>
            ) : notifications.length > 0 ? (
              <div className="divide-y divide-dark-700/30">
                {notifications.map((notif) => (
                  <div
                    key={notif.id}
                    onClick={() => handleNotificationClick(notif)}
                    className={`
                      p-4 flex gap-3 cursor-pointer transition-all duration-200
                      ${notif.isRead ? 'opacity-70 hover:opacity-100 hover:bg-dark-700/30' : 'bg-primary-900/10 hover:bg-primary-900/20'}
                    `}
                  >
                    <div className="mt-0.5">
                      {notif.isRead ? (
                        <div className="w-2 h-2 rounded-full bg-transparent mt-1.5" />
                      ) : (
                        <div className="w-2 h-2 rounded-full bg-primary-500 mt-1.5 shadow-[0_0_8px_rgba(var(--color-primary-500),0.8)]" />
                      )}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className={`text-sm mb-0.5 ${notif.isRead ? 'text-dark-200 font-medium' : 'text-white font-semibold'}`}>
                        {notif.title}
                      </p>
                      <p className="text-xs text-dark-400 line-clamp-2 leading-relaxed">
                        {notif.message}
                      </p>
                      <p className="text-[11px] text-dark-500 mt-2 flex items-center gap-1.5 font-medium">
                        {timeAgo(notif.createdAt)}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="py-12 flex flex-col items-center justify-center text-dark-500">
                <Bell size={32} className="mb-3 text-dark-600 opacity-50" />
                <p className="text-sm font-medium">Chưa có thông báo nào</p>
                <p className="text-xs text-dark-500 mt-1">Khi có thông báo mới, chúng sẽ hiển thị ở đây</p>
              </div>
            )}
          </div>
          
          {/* Footer (Optional Link to view all history if needed) */}
        </div>
      )}
    </div>
  );
}
