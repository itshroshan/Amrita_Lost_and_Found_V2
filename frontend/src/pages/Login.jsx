import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { toast } from 'react-toastify';
import { Mail, Lock, ArrowRight, ShieldCheck } from 'lucide-react';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const response = await axios.post('/api/auth/login', {
        email,
        password
      });
      
      const { name, role, email: userEmail } = response.data;
      // Note: Token is now automatically managed by the browser as an HttpOnly cookie!
      localStorage.setItem('user', JSON.stringify({ name, email: userEmail, role }));
      localStorage.removeItem('token'); // Clean up any stale token from v1
      
      toast.success('Welcome back, ' + name + '!');
      
      if (role === 'admin') {
        navigate('/admin');
      } else {
        navigate('/dashboard');
      }
      
    } catch (err) {
      toast.error(err.response?.data?.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-900 flex items-center justify-center p-4 relative overflow-hidden">
      
      <div className="card w-full max-w-md p-8 bg-white/80 dark:bg-slate-800/80 backdrop-blur-sm relative z-10">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-brand-50 dark:bg-brand-900/30 mb-4 shadow-sm">
            <ShieldCheck className="h-8 w-8 text-brand-600 dark:text-brand-400" />
          </div>
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white">Welcome Back</h2>
          <p className="text-slate-500 dark:text-slate-400 mt-2 text-sm">Sign in to your Amrita account</p>
        </div>

        <form onSubmit={handleLogin} className="space-y-6">
          <div>
            <label className="label-text dark:text-slate-300">University Email</label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Mail className="h-5 w-5 text-slate-400" />
              </div>
              <input
                type="email"
                required
                className="input-field pl-10"
                placeholder="name@bl.students.amrita.edu"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
          </div>

          <div>
            <div className="flex justify-between items-center mb-1.5">
              <label className="label-text !mb-0 dark:text-slate-300">Password</label>
              <Link to="/forgot-password" className="text-sm font-medium text-brand-600 dark:text-brand-400 hover:text-brand-700 transition-colors">
                Forgot password?
              </Link>
            </div>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Lock className="h-5 w-5 text-slate-400" />
              </div>
              <input
                type="password"
                required
                className="input-field pl-10"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="btn-primary w-full flex items-center justify-center group"
          >
            {loading ? 'Authenticating...' : 'Sign In'}
            {!loading && <ArrowRight className="ml-2 h-4 w-4 transition-transform group-hover:translate-x-1" />}
          </button>
        </form>

        <div className="mt-6 relative flex items-center justify-center">
          <div className="absolute inset-x-0 h-px bg-slate-200 dark:bg-slate-700"></div>
          <span className="relative bg-white dark:bg-[#1a2332] px-4 text-xs font-medium text-slate-400 uppercase tracking-wider">Or</span>
        </div>

        <button
          type="button"
          onClick={() => {}}
          className="mt-6 w-full flex items-center justify-center px-4 py-2.5 border border-slate-300 dark:border-slate-700 rounded-lg shadow-sm bg-white dark:bg-[#1a2332] text-slate-700 dark:text-slate-300 font-medium hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors"
        >
          <svg className="w-5 h-5 mr-3" viewBox="0 0 21 21" xmlns="http://www.w3.org/2000/svg">
            <path fill="#f25022" d="M1 1h9v9H1z"/>
            <path fill="#00a4ef" d="M1 11h9v9H1z"/>
            <path fill="#7fba00" d="M11 1h9v9h-9z"/>
            <path fill="#ffb900" d="M11 11h9v9h-9z"/>
          </svg>
          Login with O365
        </button>

        <div className="mt-6 text-center text-sm text-slate-600 dark:text-slate-400">
          Don't have an account?{' '}
          <Link to="/register" className="text-brand-600 dark:text-brand-400 font-semibold hover:text-brand-700 dark:hover:text-brand-300 transition-colors">
            Create Account
          </Link>
        </div>
      </div>
    </div>
  );
}
