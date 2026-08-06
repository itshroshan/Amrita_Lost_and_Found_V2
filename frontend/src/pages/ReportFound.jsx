import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { toast } from 'react-toastify';
import { PackagePlus, MapPin, AlignLeft, Image as ImageIcon, ArrowLeft } from 'lucide-react';

export default function ReportFound() {
  const [formData, setFormData] = useState({
    itemName: '',
    description: '',
    location: '',
  });
  const [image, setImage] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user'));
  const dashboardRoute = user?.role === 'admin' ? '/admin' : '/dashboard';

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (!file.type.match('image.*')) {
        toast.error('Please upload an image file (JPG, PNG)');
        return;
      }
      if (file.size > 5 * 1024 * 1024) {
        toast.error('File size must be less than 5MB');
        return;
      }
      setImage(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    const data = new FormData();
    data.append('itemName', formData.itemName);
    data.append('description', formData.description);
    data.append('location', formData.location);
    if (image) {
      data.append('image', image);
    }

    try {
      await axios.post('/api/items/found', data, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      toast.success('Found item reported successfully!');
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
            <div className="h-12 w-12 rounded-xl bg-brand-50 dark:bg-brand-900/30 flex items-center justify-center mr-4">
              <PackagePlus className="h-6 w-6 text-brand-600 dark:text-brand-400" />
            </div>
            <div>
              <h1 className="text-xl font-bold text-slate-900 dark:text-white">Report Found Item</h1>
              <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">Help return a lost item to its owner.</p>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label-text">Item Name</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <PackagePlus className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  type="text"
                  required
                  className="input-field pl-10"
                  placeholder="e.g. Blue iPhone 13"
                  value={formData.itemName}
                  onChange={(e) => setFormData({...formData, itemName: e.target.value})}
                />
              </div>
            </div>

            <div>
              <label className="label-text">Where did you find it?</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <MapPin className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  type="text"
                  required
                  className="input-field pl-10"
                  placeholder="e.g. Main Library, 2nd Floor"
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
                  rows={2}
                  className="input-field pl-10 py-2"
                  placeholder="Provide identifying details like color, brand, or unique marks..."
                  value={formData.description}
                  onChange={(e) => setFormData({...formData, description: e.target.value})}
                />
              </div>
            </div>

            <div>
              <label className="label-text">Photo (Optional)</label>
              <div 
                className="mt-1 flex justify-center px-6 pt-4 pb-4 border-2 border-slate-200 dark:border-slate-700 border-dashed rounded-xl hover:border-brand-400 dark:hover:border-brand-500 transition-colors bg-white dark:bg-slate-900 cursor-pointer"
                onClick={() => document.getElementById('file-upload').click()}
              >
                <div className="space-y-1 text-center">
                  {image ? (
                    <div className="text-brand-600 font-medium">{image.name}</div>
                  ) : (
                    <ImageIcon className="mx-auto h-12 w-12 text-slate-300" />
                  )}
                  <div className="flex text-sm text-slate-600 mt-2">
                    <label className="relative cursor-pointer bg-transparent rounded-md font-medium text-brand-600 dark:text-brand-400 hover:text-brand-500 focus-within:outline-none focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-brand-500 px-2">
                      <span>Upload a file</span>
                      <input id="file-upload" type="file" className="sr-only" onChange={handleImageChange} accept="image/*" />
                    </label>
                    <p className="pl-1">or drag and drop</p>
                  </div>
                  <p className="text-xs text-slate-500">PNG, JPG up to 5MB</p>
                </div>
              </div>
            </div>

            <div className="pt-2">
              <button
                type="submit"
                disabled={loading}
                className="btn-primary w-full"
              >
                {loading ? 'Submitting...' : 'Submit Report'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
