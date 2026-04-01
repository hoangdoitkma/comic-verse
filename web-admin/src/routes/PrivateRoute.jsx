import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

/**
 * PrivateRoute - Component bảo vệ route
 * @param {string[]} allowedRoles - Danh sách role được phép truy cập
 */
const PrivateRoute = ({ allowedRoles }) => {
  const { isAuthenticated, user, loading } = useAuth();
  const location = useLocation();

  // Đang check auth (dù đã check ở Context nhưng an toàn)
  if (loading) return null;

  // Chưa đăng nhập -> Redirect về /login
  if (!isAuthenticated || !user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Kiểm tra role nếu có yêu cầu
  if (allowedRoles && allowedRoles.length > 0) {
    const hasPermission = user.roles?.some((role) =>
      allowedRoles.includes(role)
    );

    if (!hasPermission) {
      // Không đủ quyền -> Redirect về trang phù hợp với role
      if (user.roles?.includes('ROLE_ADMIN')) {
        return <Navigate to="/dashboard" replace />;
      }
      if (user.roles?.includes('ROLE_UPLOADER')) {
        return <Navigate to="/content/comics" replace />;
      }
      return <Navigate to="/login" replace />;
    }
  }

  return <Outlet />;
};

export default PrivateRoute;
