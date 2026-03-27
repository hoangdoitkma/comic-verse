import { Navigate, Outlet } from 'react-router-dom';
import authService from '../services/authService';

/**
 * PrivateRoute - Component bảo vệ route
 * @param {string[]} allowedRoles - Danh sách role được phép truy cập
 */
const PrivateRoute = ({ allowedRoles }) => {
  const isAuthenticated = authService.isAuthenticated();
  const user = authService.getCurrentUser();

  // Chưa đăng nhập -> Redirect về /login
  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  // Kiểm tra role nếu có yêu cầu
  if (allowedRoles && allowedRoles.length > 0) {
    const hasPermission = user.roles?.some((role) =>
      allowedRoles.includes(role)
    );

    if (!hasPermission) {
      // Không đủ quyền -> Redirect về trang phù hợp với role
      const primaryRole = authService.getPrimaryRole();
      if (primaryRole === 'ROLE_ADMIN') {
        return <Navigate to="/admin" replace />;
      }
      if (primaryRole === 'ROLE_UPLOADER') {
        return <Navigate to="/uploader" replace />;
      }
      return <Navigate to="/login" replace />;
    }
  }

  return <Outlet />;
};

export default PrivateRoute;
