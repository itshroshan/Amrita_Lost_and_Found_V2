import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

const Pagination = ({ currentPage, totalPages, onPageChange }) => {
  if (totalPages <= 1) return null;

  const getPageNumbers = () => {
    const pages = [];
    // Show first page, last page, current page, and 1 page before and after current
    const maxVisible = 5;
    
    if (totalPages <= maxVisible + 2) {
      for (let i = 0; i < totalPages; i++) {
        pages.push(i);
      }
    } else {
      pages.push(0);
      
      let start = Math.max(1, currentPage - 1);
      let end = Math.min(totalPages - 2, currentPage + 1);
      
      if (currentPage <= 2) {
        end = 3;
      } else if (currentPage >= totalPages - 3) {
        start = totalPages - 4;
      }
      
      if (start > 1) {
        pages.push('...');
      }
      
      for (let i = start; i <= end; i++) {
        pages.push(i);
      }
      
      if (end < totalPages - 2) {
        pages.push('...');
      }
      
      pages.push(totalPages - 1);
    }
    return pages;
  };

  return (
    <div className="flex items-center justify-center space-x-2 mt-8 mb-4">
      <button
        onClick={() => onPageChange(Math.max(0, currentPage - 1))}
        disabled={currentPage === 0}
        className="flex items-center text-brand-700 dark:text-brand-400 font-medium disabled:opacity-50 disabled:cursor-not-allowed hover:text-brand-800 dark:hover:text-brand-300 px-2"
      >
        <ChevronLeft className="w-4 h-4 mr-1" /> prev
      </button>

      <div className="flex items-center space-x-2">
        {getPageNumbers().map((page, index) => {
          if (page === '...') {
            return (
              <span key={`ellipsis-${index}`} className="w-10 h-10 flex items-center justify-center text-brand-700 dark:text-brand-400 font-medium bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-slate-100 dark:border-slate-700">
                ...
              </span>
            );
          }

          const isActive = currentPage === page;

          return (
            <button
              key={page}
              onClick={() => onPageChange(page)}
              className={`w-10 h-10 flex items-center justify-center rounded-xl font-medium shadow-sm transition-colors duration-200 ${
                isActive
                  ? 'bg-brand-600 text-white border-transparent'
                  : 'bg-white dark:bg-slate-800 text-brand-700 dark:text-brand-400 border border-slate-100 dark:border-slate-700 hover:bg-brand-50 dark:hover:bg-brand-900/20'
              }`}
            >
              {page + 1}
            </button>
          );
        })}
      </div>

      <button
        onClick={() => onPageChange(Math.min(totalPages - 1, currentPage + 1))}
        disabled={currentPage === totalPages - 1}
        className="flex items-center text-brand-700 dark:text-brand-400 font-medium disabled:opacity-50 disabled:cursor-not-allowed hover:text-brand-800 dark:hover:text-brand-300 px-2"
      >
        next <ChevronRight className="w-4 h-4 ml-1" />
      </button>
    </div>
  );
};

export default Pagination;
