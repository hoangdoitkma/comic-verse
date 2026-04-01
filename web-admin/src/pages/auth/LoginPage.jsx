import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Mail, Lock, Eye, EyeOff, BookOpen, LogIn, Loader2 } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';

const LoginPage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { login } = useAuth();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    if (error) setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // 1. Kiểm tra xem nút có gọi được hàm này không, và formData có dữ liệu không?
    console.log("👉 1. ĐÃ BẤM NÚT ĐĂNG NHẬP!", formData);

    setLoading(true);
    setError('');

    try {
      console.log("👉 2. CHUẨN BỊ GỌI API...");
      const data = await login(formData.email, formData.password);

      console.log("👉 3. API TRẢ VỀ THÀNH CÔNG:", data);

      if (data.roles?.includes('ROLE_ADMIN')) {
        navigate('/dashboard', { replace: true });
      } else if (data.roles?.includes('ROLE_UPLOADER')) {
        navigate('/content/comics', { replace: true });
      } else {
        navigate('/', { replace: true });
      }
    } catch (err) {
      // 4. Bắt chính xác lỗi gì đang xảy ra
      console.error("👉 4. LỖI RỒI:", err);
      const message =
        err.response?.data?.message ||
        'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-dark-950 relative overflow-hidden">
      {/* Animated background blobs */}
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-primary-500/20 rounded-full blur-3xl animate-pulse" />
        <div className="absolute -bottom-40 -left-40 w-96 h-96 bg-primary-700/15 rounded-full blur-3xl animate-pulse [animation-delay:1s]" />
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-primary-600/5 rounded-full blur-3xl" />
      </div>

      {/* Grid pattern overlay */}
      <div
        className="absolute inset-0 opacity-[0.03]"
        style={{
          backgroundImage:
            'linear-gradient(rgba(255,255,255,.1) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,.1) 1px, transparent 1px)',
          backgroundSize: '60px 60px',
        }}
      />

      <div className="relative z-10 w-full max-w-md mx-4">
        {/* Logo & Header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-primary-500 to-primary-700 shadow-lg shadow-primary-500/25 mb-4">
            <BookOpen className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-3xl font-bold text-white tracking-tight">
            Comic<span className="text-primary-400">Hub</span>
          </h1>
          <p className="text-dark-400 mt-2 text-sm">
            Đăng nhập vào hệ thống quản lý
          </p>
        </div>

        {/* Login Card */}
        <div className="bg-dark-900/80 backdrop-blur-xl border border-dark-700/50 rounded-2xl p-8 shadow-2xl shadow-black/20">
          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Error Alert */}
            {error && (
              <div className="flex items-start gap-3 p-4 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-sm animate-[fadeIn_0.3s_ease-out]">
                <div className="w-5 h-5 rounded-full bg-red-500/20 flex items-center justify-center flex-shrink-0 mt-0.5">
                  <span className="text-xs font-bold">!</span>
                </div>
                <span>{error}</span>
              </div>
            )}

            {/* Email Field */}
            <div className="space-y-2">
              <label
                htmlFor="email"
                className="block text-sm font-medium text-dark-300"
              >
                Email
              </label>
              <div className="relative group">
                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                  <Mail className="w-4 h-4 text-dark-500 group-focus-within:text-primary-400 transition-colors" />
                </div>
                <input
                  id="email"
                  name="email"
                  type="email"
                  required
                  autoComplete="email"
                  placeholder="admin@example.com"
                  value={formData.email}
                  onChange={handleChange}
                  className="w-full pl-11 pr-4 py-3 bg-dark-800/50 border border-dark-600/50 rounded-xl text-white placeholder-dark-500 text-sm
                    focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500/50
                    hover:border-dark-500/50 transition-all duration-200"
                />
              </div>
            </div>

            {/* Password Field */}
            <div className="space-y-2">
              <label
                htmlFor="password"
                className="block text-sm font-medium text-dark-300"
              >
                Mật khẩu
              </label>
              <div className="relative group">
                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                  <Lock className="w-4 h-4 text-dark-500 group-focus-within:text-primary-400 transition-colors" />
                </div>
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  required
                  autoComplete="current-password"
                  placeholder="••••••••"
                  value={formData.password}
                  onChange={handleChange}
                  className="w-full pl-11 pr-12 py-3 bg-dark-800/50 border border-dark-600/50 rounded-xl text-white placeholder-dark-500 text-sm
                    focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500/50
                    hover:border-dark-500/50 transition-all duration-200"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute inset-y-0 right-0 pr-4 flex items-center text-dark-500 hover:text-dark-300 transition-colors"
                >
                  {showPassword ? (
                    <EyeOff className="w-4 h-4" />
                  ) : (
                    <Eye className="w-4 h-4" />
                  )}
                </button>
              </div>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={loading}
              className="w-full flex items-center justify-center gap-2 py-3 px-4 bg-gradient-to-r from-primary-600 to-primary-500 
                hover:from-primary-500 hover:to-primary-400 text-white font-semibold rounded-xl text-sm
                shadow-lg shadow-primary-500/25 hover:shadow-primary-500/40
                focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:ring-offset-2 focus:ring-offset-dark-900
                disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:shadow-primary-500/25
                transform hover:scale-[1.01] active:scale-[0.99] transition-all duration-200"
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Đang xử lý...</span>
                </>
              ) : (
                <>
                  <LogIn className="w-4 h-4" />
                  <span>Đăng nhập</span>
                </>
              )}
            </button>
          </form>
        </div>

        {/* Footer */}
        <p className="text-center text-dark-600 text-xs mt-6">
          © 2024 ComicHub. Admin & Uploader Portal.
        </p>
      </div>

      {/* Keyframe for fadeIn animation */}
      <style>{`
        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(-8px); }
          to { opacity: 1; transform: translateY(0); }
        }
      `}</style>
    </div>
  );
};

export default LoginPage;
