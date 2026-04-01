import { createContext, useContext, useState, useEffect } from 'react';
import authService from '../services/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true); // Ngăn chặn FOUC mượt mà

  useEffect(() => {
    // Lắng nghe sự kiện 401 từ Axios Interceptor
    const handleUnauthorized = () => {
      authService.logout();
      setUser(null);
      setIsAuthenticated(false);
      alert('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!');
    };
    window.addEventListener('auth:unauthorized', handleUnauthorized);

    // Component Mount: Check Token
    const checkAuth = async () => {
      try {
        const token = localStorage.getItem('token');
        const userData = authService.getCurrentUser();
        
        if (token && userData) {
          setUser(userData);
          setIsAuthenticated(true);
        } else {
          setUser(null);
          setIsAuthenticated(false);
        }
      } catch (error) {
        console.error("Auth init error", error);
        setUser(null);
        setIsAuthenticated(false);
      } finally {
        // Tắt toàn màn hình loading
        setLoading(false);
      }
    };
    
    checkAuth();

    return () => {
      window.removeEventListener('auth:unauthorized', handleUnauthorized);
    };
  }, []);

  const login = async (email, password) => {
    const data = await authService.login(email, password);
    const userData = authService.getCurrentUser();
    setUser(userData);
    setIsAuthenticated(true);
    return data;
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    setIsAuthenticated(false);
  };

  const value = {
    user,
    isAuthenticated,
    loading,
    login,
    logout
  };

  if (loading) {
     return (
       <div className="min-h-screen bg-dark-950 flex flex-col items-center justify-center">
         <div className="w-12 h-12 border-4 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin"></div>
         <p className="text-emerald-400 mt-6 text-sm tracking-[0.2em] font-medium uppercase animate-pulse">
           Xác thực phiên...
         </p>
       </div>
     );
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
