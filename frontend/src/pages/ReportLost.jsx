import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { toast } from 'react-toastify';
import { AlertCircle, MapPin, AlignLeft, ArrowLeft } from 'lucide-react';

export default function ReportLost() {
  const [formData, setFormData] = useState({
    itemName: '',
    description: '',
    location: '',
  });
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user'));
  const dashboardRoute = user?.role === 'admin' ? '/admin' : '/dashboard';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      await axios.post('/api/items/lost', formData);
      toast.success('Lost item reported successfully!');
      navigate(dashboardRoute);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to report item');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-900 py-6 px-4 sm:px-6 relative flex flex-col justify-center">
      <div className="max-w-3xl w-full mx-auto relative z-10">
        
        <button 
          onClick={() => navigate(dashboardRoute)}
          className="flex items-center text-slate-500 dark:text-slate-400 hover:text-brand-600 dark:hover:text-brand-400 transition-colors mb-4 font-medium"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Dashboard
        </button>

        <div className="card p-6">
          <div className="flex items-center mb-4 pb-4 border-b border-slate-100 dark:border-slate-700">
            <div className="h-12 w-12 rounded-xl bg-orange-50 dark:bg-orange-900/30 flex items-center justify-center mr-4">
              <AlertCircle className="h-6 w-6 text-orange-500 dark:text-orange-400" />
            </div>
            <div>
              <h1 className="text-xl font-bold text-slate-900 dark:text-white">Report Lost Item</h1>
              <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">Post a notice for an item you have lost.</p>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label-text">Item Name</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <AlertCircle className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  type="text"
                  required
                  className="input-field pl-10"
                  placeholder="e.g. Black Dell Laptop"
                  value={formData.itemName}
                  onChange={(e) => setFormData({...formData, itemName: e.target.value})}
                />
              </div>
            </div>

            <div>
              <label className="label-text">Where did you last see it?</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <MapPin className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  type="text"
                  required
                  className="input-field pl-10"
                  placeholder="e.g. Amriteshwari Hall, 3rd Bench"
                  value={formData.location}
                  onChange={(e) => setFormData({...formData, location: e.target.value})}
                />
              </div>
            </div>

            <div>
              <label className="label-text">Description</label>
              <div className="relative">
                <div className="absolute top-3 left-0 pl-3 pointer-events-none">
                  <AlignLeft className="h-5 w-5 text-slate-400" />
                </div>
                <textarea
                  required
                  rows={3}
                  className="input-field pl-10 py-2"
                  placeholder="Provide identifying details to help someone recognize it..."
                  value={formData.description}
                  onChange={(e) => setFormData({...formData, description: e.target.value})}
                />
              </div>
            </div>

            <div className="pt-2">
              <button
                type="submit"
                disabled={loading}
                className="btn-primary w-full bg-gradient-to-r from-orange-500 to-orange-400 shadow-[0_4px_14px_0_rgba(249,115,22,0.3)] hover:shadow-[0_6px_20px_0_rgba(249,115,22,0.4)] hover:-translate-y-0.5 focus-visible:ring-orange-500"
              >
                {loading ? 'Submitting...' : 'Post Notice'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
