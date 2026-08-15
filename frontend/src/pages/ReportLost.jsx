import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { toast } from 'react-toastify';
import { PackageMinus, MapPin, AlignLeft, Image as ImageIcon, ArrowLeft, AlertCircle } from 'lucide-react';
import { useDocumentTitle } from '../hooks/useDocumentTitle';
import { Upload, FileImage, Camera, Loader2 } from 'lucide-react';

export default function ReportLost() {
  useDocumentTitle('Report Lost Item');
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
      await axios.post('/api/items/lost', data, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
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
                  onChange={(e) => setFormData({ ...formData, itemName: e.target.value })}
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
                  onChange={(e) => setFormData({ ...formData, location: e.target.value })}
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
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                />
              </div>
            </div>

            <div>
              <label className="label-text flex items-center justify-between">
                <span>Upload Image</span>
                <span className="text-xs text-slate-400 font-normal">Optional (Max 5MB)</span>
              </label>

              <div className="mt-2">
                <div
                  className={`
                    border-2 border-dashed rounded-xl p-4 transition-colors
                    ${image ? 'border-brand-300 bg-brand-50/50 dark:border-brand-800 dark:bg-brand-900/10' : 'border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 hover:border-brand-400 hover:bg-slate-50 dark:hover:border-brand-500'}
                  `}
                >
                  {image ? (
                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-3 overflow-hidden">
                        <div className="h-10 w-10 shrink-0 bg-brand-100 dark:bg-brand-900/50 rounded-lg flex items-center justify-center">
                          <FileImage className="h-5 w-5 text-brand-600 dark:text-brand-400" />
                        </div>
                        <div className="truncate">
                          <p className="text-sm font-medium text-slate-700 dark:text-slate-300 truncate">
                            {image.name}
                          </p>
                          <p className="text-xs text-slate-500">
                            {(image.size / (1024 * 1024)).toFixed(2)} MB
                          </p>
                        </div>
                      </div>
                      <button
                        type="button"
                        onClick={(e) => { e.stopPropagation(); setImage(null); }}
                        className="text-sm text-red-500 hover:text-red-600 font-medium px-2 py-1"
                      >
                        Remove
                      </button>
                    </div>
                  ) : (
                    <>
                      {/* Mobile View: Explicit Buttons */}
                      <div className="sm:hidden flex flex-col space-y-3 w-full">
                        <label className="flex items-center justify-center cursor-pointer w-full py-3 px-4 border border-slate-300 dark:border-slate-700 rounded-lg shadow-sm bg-white dark:bg-slate-800 hover:bg-slate-50 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 font-medium transition-colors">
                          <Camera className="w-5 h-5 mr-2 text-brand-500" />
                          Take Photo
                          <input
                            type="file"
                            className="sr-only"
                            accept="image/jpeg,image/png,image/jpg"
                            capture="environment"
                            onChange={handleImageChange}
                          />
                        </label>
                        <label className="flex items-center justify-center cursor-pointer w-full py-3 px-4 border border-slate-300 dark:border-slate-700 rounded-lg shadow-sm bg-white dark:bg-slate-800 hover:bg-slate-50 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 font-medium transition-colors">
                          <FileImage className="w-5 h-5 mr-2 text-brand-500" />
                          Choose from Gallery
                          <input
                            type="file"
                            className="sr-only"
                            accept="image/jpeg,image/png,image/jpg"
                            onChange={handleImageChange}
                          />
                        </label>
                        <p className="text-xs text-center text-slate-500 mt-1">PNG, JPG up to 5MB</p>
                      </div>

                      {/* Desktop View: Drag and Drop */}
                      <label
                        htmlFor="file-upload"
                        className="hidden sm:block text-center cursor-pointer w-full h-full"
                      >
                        <Camera className="mx-auto h-8 w-8 text-slate-400" />
                        <div className="mt-2 flex text-sm leading-6 justify-center">
                          <span className="relative font-semibold text-brand-600 focus-within:outline-none hover:text-brand-500">
                            Upload a file
                            <input
                              id="file-upload"
                              name="file-upload"
                              type="file"
                              className="sr-only"
                              accept="image/jpeg,image/png,image/jpg"
                              onChange={handleImageChange}
                            />
                          </span>
                          <p className="pl-1 text-slate-500">or drag and drop</p>
                        </div>
                        <p className="text-xs leading-5 text-slate-500">PNG, JPG up to 5MB</p>
                      </label>
                    </>
                  )}
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
