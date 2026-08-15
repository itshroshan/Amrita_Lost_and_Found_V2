import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { toast } from 'react-toastify';
import { AlertCircle, PlusCircle, PackageCheck, CheckCircle, FileSignature, Search, PackageSearch, MapPin, Trash2, Clock, LogOut, Loader2, KeyRound, Calendar, Tag, User, Moon, Sun, ChevronDown } from 'lucide-react';
import { useDocumentTitle } from '../hooks/useDocumentTitle';

export default function AdminDashboard() {
  useDocumentTitle('Admin Dashboard');
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('found'); // 'found', 'lost', or 'claimed'
  const [days, setDays] = useState(0);
  const [claimModalOpen, setClaimModalOpen] = useState(false);
  const [claimStep, setClaimStep] = useState(1);
  const [isTimeDropdownOpen, setIsTimeDropdownOpen] = useState(false);

  const timeOptions = [
    { value: 0, label: 'All Time' },
    { value: 1, label: 'Today' },
    { value: 7, label: 'Last 7 Days' },
    { value: 30, label: 'Last 30 Days' }
  ];
  const [claimData, setClaimData] = useState({ student_name: '', registration_number: '', otp: '' });
  const [selectedItemId, setSelectedItemId] = useState(null);
  const [lightboxImage, setLightboxImage] = useState(null);
  const [isDark, setIsDark] = useState(() => {
    return localStorage.getItem('theme') === 'dark';
  });
  const navigate = useNavigate();
  
  const user = JSON.parse(localStorage.getItem('user'));

  useEffect(() => {
    fetchItems();
  }, [activeTab, days]);

  useEffect(() => {
    if (isDark) {
      document.documentElement.classList.add('dark');
      localStorage.setItem('theme', 'dark');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('theme', 'light');
    }
  }, [isDark]);

  const fetchItems = async () => {
    setLoading(true);
    try {
      const response = await axios.get(`/api/items/${activeTab}?days=${days}`);
      setItems(response.data.items);
    } catch (err) {
      toast.error('Failed to load items.');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = async () => {
    try {
      await axios.post('/api/auth/logout');
    } catch (e) {
      console.error("Logout error", e);
    }
    localStorage.removeItem('user');
    localStorage.removeItem('token'); // Clean up any stale token from v1
    toast.success('Logged out successfully');
    navigate('/login');
  };

  const handleDelete = async (id) => {
    if (!window.confirm(`Are you sure you want to delete this ${activeTab} item?`)) return;
    
    try {
      const deleteTab = activeTab === 'pending' ? 'found' : activeTab;
      await axios.delete(`/api/items/${deleteTab}/${id}`);
      toast.success('Item deleted successfully');
      fetchItems();
    } catch (err) {
      toast.error('Failed to delete item');
    }
  };

  const handleApprove = async (id) => {
    try {
      await axios.post(`/api/items/approve/${id}`, {});
      toast.success('Item approved successfully');
      fetchItems();
    } catch (err) {
      toast.error('Failed to approve item');
    }
  };

  const openClaimModal = (id) => {
    setSelectedItemId(id);
    setClaimData({ student_name: '', registration_number: '', otp: '' });
    setClaimStep(1);
    setClaimModalOpen(true);
  };

  const handleInitiateClaim = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post(`/api/items/claim/initiate/${selectedItemId}`, claimData);
      toast.success(response.data.message || 'OTP sent successfully!');
      setClaimStep(2);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to initiate claim');
    }
  };

  const handleClaimSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post(`/api/items/claim/${selectedItemId}`, claimData);
      toast.success('Item successfully marked as claimed!');
      setClaimModalOpen(false);
      fetchItems();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to claim item or invalid OTP');
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-900 relative">
      {/* Navigation */}
      <nav className="bg-brand-600 dark:bg-brand-800 sticky top-0 z-50 shadow-md">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center min-w-0">
              <img 
                src="/amrita-logo.jpg" 
                alt="Amrita Logo" 
                className="h-8 sm:h-10 w-auto max-w-[120px] sm:max-w-none bg-white p-1 rounded-md mr-2 sm:mr-3 object-contain shrink-0" 
              />
              <span className="text-lg sm:text-xl font-bold text-white truncate">
                Amrita<span className="hidden sm:inline"> Lost & Found</span>
                <span className="sm:hidden"> L&F</span>
              </span>
            </div>
            <div className="flex items-center space-x-4">
              <button 
                onClick={() => setIsDark(!isDark)} 
                className="text-brand-100 hover:text-white transition-colors p-2"
                title="Toggle Theme"
              >
                {isDark ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
              </button>
              <span className="text-sm font-medium text-white hidden sm:block">Admin Console</span>
              <button onClick={handleLogout} className="text-brand-100 hover:text-white transition-colors p-2">
                <LogOut className="h-5 w-5" />
              </button>
            </div>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 relative z-10">
        
        {/* Header Section with Actions */}
        <div className="flex flex-col md:flex-row md:items-center justify-between mb-8 space-y-4 md:space-y-0">
          <div>
            <h1 className="text-3xl font-bold text-slate-900 dark:text-white tracking-tight">Admin Dashboard</h1>
            <p className="text-slate-500 dark:text-slate-400 mt-1">Manage and resolve all items reported on campus.</p>
          </div>
          <div className="flex flex-col sm:flex-row gap-3 w-full md:w-auto">
            <button 
              onClick={() => navigate('/report-lost')}
              className="btn-secondary flex items-center justify-center group w-full sm:w-auto"
            >
              <AlertCircle className="w-4 h-4 mr-2 text-slate-400 group-hover:text-slate-600 transition-colors" />
              Report Lost
            </button>
            <button 
              onClick={() => navigate('/report-found')}
              className="btn-primary flex items-center justify-center w-full sm:w-auto"
            >
              <PlusCircle className="w-4 h-4 mr-2" />
              Report Found
            </button>
          </div>
        </div>

        {/* Action Bar (Tabs & Filters) */}
        <div className="flex flex-col xl:flex-row justify-between items-start xl:items-center mb-8 space-y-4 xl:space-y-0">
          <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4 w-full">
            {/* Tabs */}
            <div className="flex w-full sm:w-auto overflow-x-auto sm:overflow-visible space-x-1 bg-white dark:bg-slate-800 p-1 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm shrink-0">
              <button
                onClick={() => setActiveTab('found')}
                className={`flex-1 sm:flex-none px-3 sm:px-6 py-2 rounded-lg text-sm font-semibold transition-all duration-200 whitespace-nowrap ${
                    activeTab === 'found' 
                      ? 'bg-brand-50 dark:bg-brand-900/30 text-brand-700 dark:text-brand-400 shadow-sm' 
                      : 'text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-700/50'
                }`}
              >
                <PackageCheck className="w-4 h-4 inline mr-1 sm:mr-2 align-text-bottom" />
                <span className="truncate">Found<span className="hidden sm:inline"> Items</span></span>
              </button>
              <button
                onClick={() => setActiveTab('lost')}
                className={`flex-1 sm:flex-none px-3 sm:px-6 py-2 rounded-lg text-sm font-semibold transition-all duration-200 whitespace-nowrap ${
                    activeTab === 'lost' 
                      ? 'bg-brand-50 dark:bg-brand-900/30 text-brand-700 dark:text-brand-400 shadow-sm' 
                      : 'text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-700/50'
                }`}
              >
                <AlertCircle className="w-4 h-4 inline mr-1 sm:mr-2 align-text-bottom" />
                <span className="truncate">Lost<span className="hidden sm:inline"> Items</span></span>
              </button>
              <button
                onClick={() => setActiveTab('claimed')}
                className={`flex-1 sm:flex-none px-3 sm:px-6 py-2 rounded-lg text-sm font-semibold transition-all duration-200 whitespace-nowrap ${
                    activeTab === 'claimed' 
                      ? 'bg-brand-50 dark:bg-brand-900/30 text-brand-700 dark:text-brand-400 shadow-sm' 
                      : 'text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-700/50'
                }`}
              >
                <CheckCircle className="w-4 h-4 inline mr-1 sm:mr-2 align-text-bottom" />
                <span className="truncate">Claimed<span className="hidden sm:inline"> Items</span></span>
              </button>
              <button
                onClick={() => setActiveTab('pending')}
                className={`flex-1 sm:flex-none px-3 sm:px-6 py-2 rounded-lg text-sm font-semibold transition-all duration-200 whitespace-nowrap ${
                    activeTab === 'pending' 
                      ? 'bg-brand-50 dark:bg-brand-900/30 text-brand-700 dark:text-brand-400 shadow-sm' 
                      : 'text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-700/50'
                }`}
              >
                <FileSignature className="w-4 h-4 inline mr-1 sm:mr-2 align-text-bottom" />
                <span className="truncate">Approve<span className="hidden sm:inline"> Items</span></span>
              </button>
            </div>

            {/* Date Filter */}
            <div className="relative shrink-0 self-start lg:self-auto">
              <button 
                onClick={() => setIsTimeDropdownOpen(!isTimeDropdownOpen)}
                className="flex items-center space-x-2 bg-white dark:bg-slate-800 px-4 py-2.5 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors"
              >
                <Calendar className="w-4 h-4 text-slate-500" />
                <span className="text-sm font-semibold text-slate-700 dark:text-slate-300">
                  {timeOptions.find(opt => opt.value === days)?.label || 'All Time'}
                </span>
                <ChevronDown className={`w-4 h-4 text-slate-400 transition-transform duration-200 ${isTimeDropdownOpen ? 'rotate-180' : ''}`} />
              </button>

              {isTimeDropdownOpen && (
                <>
                  <div 
                    className="fixed inset-0 z-40" 
                    onClick={() => setIsTimeDropdownOpen(false)}
                  ></div>
                  <div className="absolute right-0 mt-2 w-48 bg-white dark:bg-slate-800 rounded-xl shadow-lg border border-slate-200 dark:border-slate-700 z-50 overflow-hidden py-1">
                    {timeOptions.map((option) => (
                      <button
                        key={option.value}
                        onClick={() => {
                          setDays(option.value);
                          setIsTimeDropdownOpen(false);
                        }}
                        className={`w-full text-left px-4 py-2.5 text-sm transition-colors ${
                          days === option.value 
                            ? 'bg-brand-50 dark:bg-brand-900/30 text-brand-600 dark:text-brand-400 font-semibold' 
                            : 'text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700/50'
                        }`}
                      >
                        {option.label}
                      </button>
                    ))}
                  </div>
                </>
              )}
            </div>
          </div>
        </div>

        {/* Item Grid */}
        {loading ? (
          <div className="flex flex-col space-y-4">
            {[1, 2, 3, 4].map(n => (
              <div key={n} className="card h-40 animate-pulse bg-white p-4 flex flex-row">
                <div className="w-40 h-32 bg-slate-100 rounded-lg mr-4 shrink-0"></div>
                <div className="flex-1 py-2">
                  <div className="h-4 bg-slate-100 rounded w-1/3 mb-4"></div>
                  <div className="h-3 bg-slate-50 rounded w-1/2 mb-2"></div>
                  <div className="h-3 bg-slate-50 rounded w-1/4"></div>
                </div>
              </div>
            ))}
          </div>
        ) : items.length === 0 ? (
          <div className="text-center py-20 card bg-white/50 backdrop-blur-sm border-dashed border-2 border-slate-200">
            <Search className="mx-auto h-12 w-12 text-slate-300 mb-3" />
            <h3 className="text-lg font-medium text-slate-900">No {activeTab} items found</h3>
            <p className="mt-1 text-slate-500">There are currently no items in this category.</p>
          </div>
        ) : (
          <div className="flex flex-col space-y-4">
            {items.map((item) => (
              <div 
                key={item.id}
                className="bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-800 rounded-xl flex flex-col sm:flex-row group overflow-hidden transition-all hover:bg-slate-50 dark:hover:bg-slate-800/80 shadow-sm dark:shadow-lg h-auto sm:min-h-[12rem]"
              >
                {item.image ? (
                  <div className="w-full h-48 sm:h-auto sm:w-64 flex-shrink-0 relative">
                    <div className="absolute inset-3" onClick={() => setLightboxImage(item.image)}>
                      <img 
                        src={item.image} 
                        alt={item.itemName} 
                        className="w-full h-full object-cover rounded-lg cursor-pointer transition-transform duration-500 group-hover:scale-[1.02]" 
                      />
                    </div>
                  </div>
                ) : (
                  <div className="w-full h-48 sm:h-auto sm:w-64 flex-shrink-0 relative">
                    <div className="absolute inset-3 rounded-lg bg-slate-100 dark:bg-slate-800 flex items-center justify-center border border-slate-200 dark:border-slate-700">
                      <PackageSearch className="h-12 w-12 text-slate-400 dark:text-slate-600" />
                    </div>
                  </div>
                )}
                
                <div className="p-5 flex-1 flex flex-col justify-between overflow-hidden">
                  <div>
                    <h3 className="text-xl font-bold text-slate-900 dark:text-white mb-1 line-clamp-1">{item.itemName}</h3>
                    
                    <div className="flex items-center text-base font-medium text-slate-700 dark:text-slate-300 mb-2">
                      <MapPin className="w-5 h-5 mr-1 text-rose-500" />
                      <span className="truncate">{item.location}</span>
                    </div>

                    <p className="text-slate-500 dark:text-slate-400 text-sm mb-2 max-w-3xl break-words whitespace-pre-wrap">{item.description}</p>
                  </div>
                  
                  <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mt-auto pt-4 border-t border-slate-200 dark:border-slate-800 gap-4 sm:gap-0">
                    
                    {/* Detail Box */}
                    <div className="flex flex-col sm:flex-row items-start sm:items-center flex-1 w-full gap-y-4 sm:gap-y-0 mr-0 sm:mr-8">
                      
                      {activeTab === 'claimed' && (
                        <>
                          {/* Date and Time */}
                          {item.claimedAt && (
                            <div className="flex-1 w-full flex items-center sm:border-r border-slate-200 dark:border-slate-700">
                              <Calendar className="w-5 h-5 text-slate-400 dark:text-slate-500 mr-3 shrink-0" />
                              <div className="flex flex-col">
                                <span className="text-sm font-bold text-slate-900 dark:text-white">
                                  {new Date(item.claimedAt).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })}
                                </span>
                                <span className="text-xs text-slate-500">
                                  {new Date(item.claimedAt).toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' })}
                                </span>
                              </div>
                            </div>
                          )}

                          {/* Registration Number */}
                          <div className="flex-1 w-full flex items-center sm:border-r border-slate-200 dark:border-slate-700 sm:pl-6">
                            <Tag className="w-5 h-5 text-slate-400 dark:text-slate-500 mr-3 shrink-0" />
                            <div className="flex flex-col">
                              <span className="text-sm font-bold text-slate-900 dark:text-white uppercase">{item.registrationNumber}</span>
                              <span className="text-xs text-slate-500">Registration No.</span>
                            </div>
                          </div>

                          {/* Claimed By */}
                          <div className="flex-1 w-full flex items-center sm:pl-6">
                            <div className="w-9 h-9 rounded-full bg-rose-600 flex items-center justify-center mr-3 shrink-0">
                              <User className="w-5 h-5 text-white" />
                            </div>
                            <div className="flex flex-col">
                              <span className="text-sm font-bold text-slate-900 dark:text-white">{item.studentName}</span>
                              <span className="text-xs text-slate-500">Claimed by</span>
                            </div>
                          </div>
                        </>
                      )}

                      {activeTab === 'lost' && (
                        <>
                          {/* Date and Time */}
                          {item.createdAt && (
                            <div className="flex-1 w-full flex items-center sm:border-r border-slate-200 dark:border-slate-700">
                              <Calendar className="w-5 h-5 text-slate-400 dark:text-slate-500 mr-3 shrink-0" />
                              <div className="flex flex-col">
                                <span className="text-sm font-bold text-slate-900 dark:text-white">
                                  {new Date(item.createdAt).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })}
                                </span>
                                <span className="text-xs text-slate-500">
                                  {new Date(item.createdAt).toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' })}
                                </span>
                              </div>
                            </div>
                          )}

                          {/* Registration Number (Only if student) */}
                          {item.studentEmail && item.studentEmail.endsWith('@bl.students.amrita.edu') && (
                            <div className="flex-1 w-full flex items-center sm:border-r border-slate-200 dark:border-slate-700 sm:pl-6">
                              <Tag className="w-5 h-5 text-slate-400 dark:text-slate-500 mr-3 shrink-0" />
                              <div className="flex flex-col">
                                <span className="text-sm font-bold text-slate-900 dark:text-white uppercase">
                                  {item.studentEmail.split('@')[0]}
                                </span>
                                <span className="text-xs text-slate-500">Registration No.</span>
                              </div>
                            </div>
                          )}

                          {/* Reported By */}
                          <div className="flex-1 w-full flex items-center sm:pl-6">
                            <div className="w-9 h-9 rounded-full bg-rose-600 flex items-center justify-center mr-3 shrink-0">
                              <User className="w-5 h-5 text-white" />
                            </div>
                            <div className="flex flex-col">
                              <span className="text-sm font-bold text-slate-900 dark:text-white truncate max-w-[120px]">
                                {item.reporterName || (item.studentEmail ? item.studentEmail.split('@')[0] : 'Admin')}
                              </span>
                              <span className="text-xs text-slate-500">Reported by</span>
                            </div>
                          </div>
                        </>
                      )}

                      {(activeTab === 'found' || activeTab === 'pending') && (
                        <>
                          {/* Date and Time */}
                          {item.createdAt && (
                            <div className="flex-1 w-full flex items-center sm:border-r border-slate-200 dark:border-slate-700">
                              <Calendar className="w-5 h-5 text-slate-400 dark:text-slate-500 mr-3 shrink-0" />
                              <div className="flex flex-col">
                                <span className="text-sm font-bold text-slate-900 dark:text-white">
                                  {new Date(item.createdAt).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })}
                                </span>
                                <span className="text-xs text-slate-500">
                                  {new Date(item.createdAt).toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' })}
                                </span>
                              </div>
                            </div>
                          )}

                          {/* Reported By */}
                          <div className="flex-1 w-full flex items-center sm:pl-6">
                            <div className="w-9 h-9 rounded-full bg-rose-600 flex items-center justify-center mr-3 shrink-0">
                              <User className="w-5 h-5 text-white" />
                            </div>
                            <div className="flex flex-col">
                              <span className="text-sm font-bold text-slate-900 dark:text-white truncate max-w-[120px]">
                                {item.reporterName || (item.reportedBy ? item.reportedBy.split('@')[0] : 'Admin')}
                              </span>
                              <span className="text-xs text-slate-500">Reported by</span>
                            </div>
                          </div>
                        </>
                      )}

                    </div>

                    {/* Actions */}
                    <div className="flex items-center space-x-3 w-full sm:w-auto justify-end shrink-0 ml-auto mt-4 sm:mt-0">
                      {activeTab === 'found' && (
                        <button 
                          onClick={() => openClaimModal(item.id)}
                          className="px-4 py-2 flex items-center border border-green-600 text-green-600 dark:border-green-500 dark:text-green-500 text-sm font-semibold rounded-lg hover:bg-green-50 dark:hover:bg-green-900/30 transition-colors"
                          title="Claim Item"
                        >
                          <CheckCircle className="w-4 h-4 mr-2" />
                          Claim
                        </button>
                      )}
                      {activeTab === 'pending' && (
                        <button 
                          onClick={() => handleApprove(item.id)}
                          className="px-4 py-2 flex items-center border border-amber-500 text-amber-600 dark:text-amber-400 text-sm font-semibold rounded-lg hover:bg-amber-50 dark:hover:bg-amber-900/30 transition-colors"
                          title="Approve Item"
                        >
                          <CheckCircle className="w-4 h-4 mr-2" />
                          Approve
                        </button>
                      )}
                      <button 
                        onClick={() => handleDelete(item.id)}
                        className="px-4 py-2 flex items-center border border-rose-200 dark:border-rose-900/50 bg-rose-50 dark:bg-rose-900/10 text-rose-600 dark:text-rose-500 text-sm font-semibold rounded-lg hover:bg-rose-100 dark:hover:bg-rose-900/30 transition-colors"
                        title="Delete Item"
                      >
                        <Trash2 className="w-4 h-4 mr-2" />
                        Delete
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      {/* Claim Modal */}
      {claimModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
          <div className="bg-white dark:bg-slate-900 border border-transparent dark:border-slate-800 rounded-2xl shadow-xl w-full max-w-md overflow-hidden">
            <div className="p-6">
              <div className="flex items-center justify-center w-12 h-12 rounded-full bg-brand-50 dark:bg-brand-900/30 mb-4">
                <FileSignature className="w-6 h-6 text-brand-600 dark:text-brand-500" />
              </div>
              <h3 className="text-xl font-bold text-slate-900 dark:text-white mb-1">Approve Claim</h3>
              <p className="text-sm text-slate-500 dark:text-slate-400 mb-6">Enter the details of the student claiming this item.</p>
              
              {claimStep === 1 ? (
                <form onSubmit={handleInitiateClaim} className="space-y-4">
                  <div>
                    <label className="label-text">Student Name</label>
                    <input
                      type="text"
                      required
                      className="input-field"
                      placeholder="e.g. John Doe"
                      value={claimData.student_name}
                      onChange={(e) => setClaimData({...claimData, student_name: e.target.value})}
                    />
                  </div>
                  <div>
                    <label className="label-text">Registration Number</label>
                    <input
                      type="text"
                      required
                      className="input-field uppercase"
                      placeholder="e.g. CB.EN.U4CSE20000"
                      value={claimData.registration_number}
                      onChange={(e) => setClaimData({...claimData, registration_number: e.target.value})}
                    />
                  </div>
                  <div className="flex space-x-3 pt-4">
                    <button
                      type="button"
                      onClick={() => setClaimModalOpen(false)}
                      className="btn-secondary flex-1"
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      className="btn-primary flex-1"
                    >
                      Send OTP
                    </button>
                  </div>
                </form>
              ) : (
                <form onSubmit={handleClaimSubmit} className="space-y-4">
                  <div className="bg-blue-50 dark:bg-blue-900/20 text-blue-800 dark:text-blue-300 p-3 rounded-lg text-sm mb-4">
                    An OTP has been sent to <strong>{claimData.registration_number.toLowerCase()}@bl.students.amrita.edu</strong>.
                  </div>
                  <div>
                    <label className="label-text">Enter OTP</label>
                    <input
                      type="text"
                      required
                      className="input-field tracking-widest text-lg font-semibold"
                      placeholder="123456"
                      value={claimData.otp}
                      onChange={(e) => setClaimData({...claimData, otp: e.target.value.replace(/\D/g, '').slice(0, 6)})}
                    />
                  </div>
                  <div className="flex space-x-3 pt-4">
                    <button
                      type="button"
                      onClick={() => setClaimStep(1)}
                      className="btn-secondary flex-1"
                    >
                      Back
                    </button>
                    <button
                      type="submit"
                      disabled={claimData.otp.length !== 6}
                      className="btn-primary flex-1 bg-green-600 hover:bg-green-700 focus:ring-green-500"
                    >
                      Verify & Claim
                    </button>
                  </div>
                </form>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Lightbox Modal */}
      {lightboxImage && (
        <div 
          className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-slate-900/90 backdrop-blur-sm"
          onClick={() => setLightboxImage(null)}
        >
          <div className="relative max-w-5xl max-h-[90vh] w-full h-full flex justify-center items-center">
            <button 
              className="absolute top-4 right-4 p-2 bg-white/10 hover:bg-white/20 rounded-full text-white backdrop-blur-md transition-colors"
              onClick={() => setLightboxImage(null)}
            >
              <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
            <img 
              src={lightboxImage} 
              alt="Enlarged view" 
              className="max-w-full max-h-[90vh] object-contain rounded-lg shadow-2xl" 
              onClick={(e) => e.stopPropagation()} // Prevent closing when clicking the image itself
            />
          </div>
        </div>
      )}
    </div>
  );
}
