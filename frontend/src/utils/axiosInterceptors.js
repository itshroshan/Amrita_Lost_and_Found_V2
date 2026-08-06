import axios from 'axios';
import { toast } from 'react-toastify';

export const setupAxiosInterceptors = () => {
  // Set the base URL for all API requests
  axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081';
  
  // Ensure cookies (like the HttpOnly JWT) are sent with every request automatically
  axios.defaults.withCredentials = true;

  axios.interceptors.response.use(
    (response) => {
      // If the response is successful, just return it
      return response;
    },
    (error) => {
      // Check if the error is 401 Unauthorized
      if (error.response && error.response.status === 401) {
        // Clear local storage completely
        // We no longer have a token in localStorage, the browser handles it.
        // We just clear the user state.
        localStorage.removeItem('user');

        // Only show toast if we were previously logged in (avoids toast spam on pure unauthenticated requests)
        // Wait, if it's 401, they probably had an invalid/expired token.
        toast.error('Session expired. Please log in again.');

        // Redirect to login page
        // Using window.location.href ensures a hard reload and clears memory state
        if (window.location.pathname !== '/login' && window.location.pathname !== '/' && window.location.pathname !== '/register') {
           window.location.href = '/login';
        }
      }
      return Promise.reject(error);
    }
  );
};
