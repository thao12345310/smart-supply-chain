import axios from 'axios';

// Base URL lấy từ biến môi trường khi build (Vercel: VITE_API_BASE_URL).
// Khi dev local không set biến thì mặc định trỏ về backend localhost:8080.
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// Create axios instance with base configuration
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to attach JWT token to every request
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle API response format
api.interceptors.response.use(
  (response) => {
    // Check if response has the new ApiResponse format
    if (response.data && typeof response.data === 'object' && 'success' in response.data) {
      if (!response.data.success) {
        return Promise.reject(new Error(response.data.message || 'Operation failed'));
      }
      // Return the data field from ApiResponse
      return { ...response, data: response.data.data };
    }
    return response;
  },
  (error) => {
    // Handle error responses
    if (error.response?.status === 401 || error.response?.status === 403) {
      // Token might be invalid or expired
      if (window.location.pathname !== '/login') {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        // Redirect to login only if not already there
        // Actually, we should probably let the component handle the redirect if using a React context
      }
    }

    if (error.response?.data?.message) {
      error.message = error.response.data.message;
    }
    return Promise.reject(error);
  }
);

export default api;
