import { Navigate, useLocation } from "react-router-dom";

/**
 * Protected Route Component
 * Redirects to login if user is not authenticated.
 */
export default function ProtectedRoute({ children }) {
  const token = localStorage.getItem('token');
  const location = useLocation();

  if (!token) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
}
