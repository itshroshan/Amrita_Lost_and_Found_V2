import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { toast } from 'react-toastify';
import { Mail, Lock, User, ArrowRight, ShieldCheck, FileKey, CheckCircle2, Key } from 'lucide-react';
import { useDocumentTitle } from '../hooks/useDocumentTitle';

export default function Register() {
  useDocumentTitle('Register');
  const [formData, setFormData] = useState({ name: '', email: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [step, setStep] = useState(1); // 1 for register, 2 for OTP
  const [otp, setOtp] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    document.documentElement.classList.remove('dark');
  }, []);

  const handleRegister = async (e) => {
    e.preventDefault();
    
    // Frontend Email Validation
    const isStudent = formData.email.endsWith('@bl.students.amrita.edu');
    const isFaculty = formData.email.endsWith('@blr.amrita.edu');
    
    if (!isStudent && !isFaculty) {
      toast.error('Please use a valid Amrita University email ID.');
      return;
    }

    setLoading(true);
    try {
      const response = await axios.post('/api/auth/register', formData);
      toast.success(response.data.message || 'OTP sent to your email!');
      setStep(2);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerify = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const response = await axios.post('/api/auth/verify-otp', {
        email: formData.email,
        otp
      });
      toast.success(response.data.message || 'Registration successful! You can now login.');
      navigate('/login');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Invalid OTP.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center relative overflow-hidden bg-slate-50 dark:bg-slate-900 px-4 sm:px-6">
      
      <div className="w-full max-w-md relative z-10">
        <div className="text-center mb-10">
          <div className="inline-flex items-center justify-center h-16 w-16 rounded-2xl bg-white dark:bg-slate-800 shadow-glow mb-6 transform rotate-x-[5deg] rotate-y-[10deg]">
            <ShieldCheck className="h-8 w-8 text-brand-600 dark:text-brand-400" />
          </div>
          <h1 className="text-4xl font-extrabold tracking-tight text-slate-900 dark:text-white mb-2">
            Create an Account
          </h1>
          <p className="text-slate-500 dark:text-slate-400 font-medium">Join the Amrita Lost & Found network</p>
        </div>

        <div className="card p-8 backdrop-blur-sm bg-white/90 dark:bg-slate-800/90 relative overflow-hidden">
          
          {/* Step indicator bar */}
          <div className="absolute top-0 left-0 w-full h-1 bg-slate-100 dark:bg-slate-700">
            <div 
              className="h-full bg-gradient-to-r from-brand-600 to-brand-400 transition-all duration-500 ease-out"
              style={{ width: step === 1 ? '50%' : '100%' }}
            ></div>
          </div>

          {step === 1 ? (
            <form onSubmit={handleRegister} className="space-y-5 pt-2">
              <div>
                <label className="label-text">Full Name</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <User className="h-5 w-5 text-slate-400" />
                  </div>
                  <input
                    type="text"
                    required
                    className="input-field pl-10"
                    placeholder="John Doe"
                    value={formData.name}
                    onChange={(e) => setFormData({...formData, name: e.target.value})}
                  />
                </div>
              </div>

              <div>
                <label className="label-text">University Email</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <Mail className="h-5 w-5 text-slate-400" />
                  </div>
                  <input
                    type="email"
                    required
                    className="input-field pl-10"
                    placeholder="name@bl.students.amrita.edu"
                    value={formData.email}
                    onChange={(e) => setFormData({...formData, email: e.target.value})}
                  />
                </div>
              </div>

              <div>
                <label className="label-text">Password</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <Lock className="h-5 w-5 text-slate-400" />
                  </div>
                  <input
                    type="password"
                    required
                    className="input-field pl-10"
                    placeholder="••••••••"
                    value={formData.password}
                    onChange={(e) => setFormData({...formData, password: e.target.value})}
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="btn-primary w-full flex items-center justify-center group mt-2"
              >
                {loading ? 'Sending OTP...' : 'Continue'}
                {!loading && <ArrowRight className="ml-2 h-4 w-4 transition-transform group-hover:translate-x-1" />}
              </button>
            </form>
          ) : (
            <form onSubmit={handleVerify} className="space-y-6 pt-2">
              <div className="text-center mb-6">
                <p className="text-sm text-slate-600 dark:text-slate-400">
                  We've sent a 6-digit verification code to <br/>
                  <span className="font-semibold text-slate-900 dark:text-white">{formData.email}</span>
                </p>
              </div>
              
              <div>
                <label className="label-text text-center">Enter Verification Code</label>
                <div className="relative mt-2">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <Key className="h-5 w-5 text-slate-400" />
                  </div>
                  <input
                    type="text"
                    required
                    maxLength={6}
                    className="input-field pl-10 text-center tracking-widest font-mono font-bold text-lg"
                    placeholder="000000"
                    value={otp}
                    onChange={(e) => setOtp(e.target.value)}
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="btn-primary w-full flex items-center justify-center group"
              >
                {loading ? 'Verifying...' : 'Verify & Register'}
              </button>
              
              <button
                type="button"
                onClick={() => setStep(1)}
                className="w-full text-sm font-medium text-slate-500 hover:text-slate-700 transition-colors"
              >
                Back to details
              </button>
            </form>
          )}

          <div className="mt-8 pt-6 border-t border-slate-100 dark:border-slate-700 text-center">
            <p className="text-sm text-slate-600 dark:text-slate-400">
              Already have an account?{' '}
              <Link to="/login" className="font-semibold text-brand-600 dark:text-brand-400 hover:text-brand-700 dark:hover:text-brand-300 transition-colors">
                Sign In
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
