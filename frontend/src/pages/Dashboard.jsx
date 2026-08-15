import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { toast } from 'react-toastify';
import { AlertCircle, PlusCircle, PackageCheck, Info, PackageSearch, MapPin, Search, Calendar, LogOut, Clock, Filter, ListFilter, User, LogIn, Trash2, Moon, Sun, ChevronDown } from 'lucide-react';
import { useDocumentTitle } from '../hooks/useDocumentTitle';
import Pagination from '../components/Pagination';

export default function Dashboard() {
  useDocumentTitle('Student Dashboard');
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('found'); // 'found' or 'my-activity'
  const [days, setDays] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [lostPage, setLostPage] = useState(0);
  const [foundPage, setFoundPage] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [lightboxImage, setLightboxImage] = useState(null);
  const [isDark, setIsDark] = useState(() => {
    return localStorage.getItem('theme') === 'dark';
  });
  const [isTimeDropdownOpen, setIsTimeDropdownOpen] = useState(false);

  const timeOptions = [
    { value: 0, label: 'All Time' },
    { value: 1, label: 'Today' },
    { value: 7, label: 'Last 7 Days' },
    { value: 30, label: 'Last 30 Days' }
  ];
  const navigate = useNavigate();

  const user = JSON.parse(localStorage.getItem('user'));

  useEffect(() => {
    // Clear items immediately to prevent old items from flashing in new tabs
    setItems([]);
    setLoading(true);

    // Simple debounce effect for search
    const delayDebounceFn = setTimeout(() => {
      fetchItems();
    }, 300);

    return () => clearTimeout(delayDebounceFn);
  }, [activeTab, searchQuery, days, currentPage]);

  // Reset pages when tab, filter, or search changes
  useEffect(() => {
    setCurrentPage(0);
    setLostPage(0);
    setFoundPage(0);
  }, [activeTab, days, searchQuery]);

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

      if (activeTab === 'my-activity') {
        const [lostRes, foundRes] = await Promise.all([
          axios.get(`/api/items/lost?days=${days}&size=1000`),
          axios.get(`/api/items/my-found?days=${days}&size=1000`)
        ]);

        const combined = [
          ...lostRes.data.items.map(item => ({ ...item, type: 'lost' })),
          ...foundRes.data.items.map(item => ({ ...item, type: 'found' }))
        ];

        combined.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        setItems(combined);
      } else {
        let url = `/api/items/${activeTab}?days=${days}&page=${currentPage}&size=10`;
        if (activeTab === 'found' && searchQuery.trim() !== '') {
          url += `&query=${encodeURIComponent(searchQuery)}`;
        }
        const response = await axios.get(url);
        setItems(response.data.items.map(item => ({ ...item, type: activeTab })));
        setTotalPages(response.data.totalPages);
      }
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

  const handleDelete = async (id, type) => {
    if (!window.confirm('Are you sure you want to delete this report?')) return;

    try {
      await axios.delete(`/api/items/${type}/${id}`);
      toast.success('Report deleted successfully');
      fetchItems();
    } catch (err) {
      toast.error('Failed to delete report');
    }
  };

  const renderItemCard = (item) => {
    const isMyActivity = activeTab === 'my-activity';
    const itemType = item.type || activeTab;

    return (
      <div
        key={item.id}
        className="bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-800 rounded-xl flex flex-col sm:flex-row group overflow-hidden transition-all hover:bg-slate-50 dark:hover:bg-slate-800/80 shadow-sm dark:shadow-lg h-auto"
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

              {/* Date and Time */}
              {item.createdAt && (
                <div className={`flex-1 w-full flex items-center ${isMyActivity ? 'sm:border-r border-slate-200 dark:border-slate-700' : ''}`}>
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

              {/* Status (Only in My Activity) */}
              {isMyActivity && (
                <div className="flex-1 w-full flex items-center sm:pl-6 mt-4 sm:mt-0">
                  <div className={`w-9 h-9 rounded-full flex items-center justify-center mr-3 shrink-0 ${itemType === 'found' ? (item.isApproved ? 'bg-green-600' : 'bg-amber-500') : 'bg-rose-600'}`}>
                    <Info className="w-5 h-5 text-white" />
                  </div>
                  <div className="flex flex-col">
                    <span className="text-sm font-bold text-slate-900 dark:text-white">
                      {itemType === 'found' ? (item.isApproved ? 'Approved' : 'Pending') : 'Active'}
                    </span>
                    <span className="text-xs text-slate-500">Status</span>
                  </div>
                </div>
              )}

            </div>

            {/* Actions */}
            <div className="flex items-center space-x-3 w-full sm:w-auto justify-end shrink-0 ml-auto mt-4 sm:mt-0">
              {isMyActivity && (
                <button
                  onClick={() => handleDelete(item.id, itemType)}
                  className="px-4 py-2 flex items-center border border-rose-200 dark:border-rose-900/50 bg-rose-50 dark:bg-rose-900/10 text-rose-600 dark:text-rose-500 text-sm font-semibold rounded-lg hover:bg-rose-100 dark:hover:bg-rose-900/30 transition-colors"
                  title="Delete My Report"
                >
                  <Trash2 className="w-4 h-4 mr-2" />
                  Delete
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    );
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
              <span className="text-sm font-medium text-white hidden sm:block">Welcome, {user?.name?.split(' ')[0]}</span>
              <button onClick={handleLogout} className="text-brand-100 hover:text-white transition-colors p-2" title="Logout">
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
            <h1 className="text-3xl font-bold text-slate-900 dark:text-white tracking-tight">Dashboard</h1>
            <p className="text-slate-500 dark:text-slate-400 mt-1">Manage and report lost and found items on campus.</p>
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

        {/* Action Bar (Tabs & Search & Filter) */}
        <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center mb-8 space-y-4 lg:space-y-0 lg:space-x-4">
          <div className="flex flex-col sm:flex-row space-y-4 sm:space-y-0 sm:space-x-4 w-full lg:w-auto">
            {/* Custom Tabs */}
            <div className="flex w-full sm:w-auto space-x-1 bg-white dark:bg-slate-800 p-1 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm shrink-0">
              <button
                onClick={() => {
                  if (activeTab !== 'found') {
                    setItems([]);
                    setLoading(true);
                    setActiveTab('found');
                  }
                }}
                className={`flex-1 sm:flex-none px-2 sm:px-6 py-2 rounded-lg text-sm font-semibold transition-all duration-200 ${activeTab === 'found'
                    ? 'bg-brand-50 dark:bg-brand-900/30 text-brand-700 dark:text-brand-400 shadow-sm'
                    : 'text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-700/50'
                  }`}
              >
                <PackageCheck className="w-4 h-4 inline mr-1 sm:mr-2 align-text-bottom" />
                <span className="truncate">Found<span className="hidden sm:inline"> Items</span></span>
              </button>
              <button
                onClick={() => {
                  if (activeTab !== 'my-activity') {
                    setItems([]);
                    setLoading(true);
                    setActiveTab('my-activity');
                  }
                }}
                className={`flex-1 sm:flex-none px-2 sm:px-6 py-2 rounded-lg text-sm font-semibold transition-all duration-200 ${activeTab === 'my-activity'
                    ? 'bg-brand-50 dark:bg-brand-900/30 text-brand-700 dark:text-brand-400 shadow-sm'
                    : 'text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-700/50'
                  }`}
              >
                <User className="w-4 h-4 inline mr-1 sm:mr-2 align-text-bottom" />
                <span className="truncate">Activity<span className="hidden sm:inline"> (My)</span></span>
              </button>
            </div>

            {/* Date Filter */}
            <div className="relative shrink-0">
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
                  <div className="absolute left-0 lg:left-auto lg:right-0 mt-2 w-48 bg-white dark:bg-slate-800 rounded-xl shadow-lg border border-slate-200 dark:border-slate-700 z-50 overflow-hidden py-1">
                    {timeOptions.map((option) => (
                      <button
                        key={option.value}
                        onClick={() => {
                          const newDays = option.value;
                          if (days !== newDays) {
                            setItems([]);
                            setLoading(true);
                            setDays(newDays);
                          }
                          setIsTimeDropdownOpen(false);
                        }}
                        className={`w-full text-left px-4 py-2.5 text-sm transition-colors ${days === option.value
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

          {/* Search Bar (Only for found items) */}
          {activeTab === 'found' && (
            <div className="relative w-full lg:w-96 shrink-0">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Search className="h-4 w-4 text-slate-400" />
              </div>
              <input
                type="text"
                placeholder="Search items by name or location..."
                className="input-field pl-10 bg-white"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          )}
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
        ) : activeTab === 'my-activity' ? (
          <div className="space-y-8">
            {items.some(i => i.type === 'lost') && (
              <div>
                <h2 className="text-xl font-bold text-slate-900 dark:text-white mb-4 flex items-center">
                  <AlertCircle className="w-5 h-5 mr-2 text-rose-500" />
                  Items I Lost
                </h2>
                <div className="flex flex-col space-y-4">
                  {items.filter(i => i.type === 'lost').slice(lostPage * 5, (lostPage + 1) * 5).map(item => renderItemCard(item))}
                </div>
                {items.filter(i => i.type === 'lost').length > 5 && (
                  <Pagination 
                    currentPage={lostPage}
                    totalPages={Math.ceil(items.filter(i => i.type === 'lost').length / 5)}
                    onPageChange={setLostPage}
                  />
                )}
              </div>
            )}
            {items.some(i => i.type === 'found') && (
              <div>
                <h2 className="text-xl font-bold text-slate-900 dark:text-white mb-4 mt-8 flex items-center">
                  <PackageCheck className="w-5 h-5 mr-2 text-brand-600" />
                  Items I Found
                </h2>
                <div className="flex flex-col space-y-4">
                  {items.filter(i => i.type === 'found').slice(foundPage * 5, (foundPage + 1) * 5).map(item => renderItemCard(item))}
                </div>
                {items.filter(i => i.type === 'found').length > 5 && (
                  <Pagination 
                    currentPage={foundPage}
                    totalPages={Math.ceil(items.filter(i => i.type === 'found').length / 5)}
                    onPageChange={setFoundPage}
                  />
                )}
              </div>
            )}
          </div>
        ) : (
          <>
            <div className="flex flex-col space-y-4">
              {items.map((item) => renderItemCard(item))}
            </div>
            {items.length > 0 && (
              <Pagination 
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={setCurrentPage}
              />
            )}
          </>
        )}
      </main>

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
