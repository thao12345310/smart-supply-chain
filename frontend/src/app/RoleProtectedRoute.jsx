import { Navigate } from "react-router-dom";
import { hasAnyRole } from "@/services/roleService";

/**
 * Role-Based Protected Route
 * Checks whether the current user holds any of the required roles.
 */
export default function RoleProtectedRoute({ roles = [], children }) {
  const isAuthorized = hasAnyRole(roles);

  if (!isAuthorized) {
    // Silently redirect to home/dashboard instead of showing 403
    return <Navigate to="/" replace />;
  }

  return children;
}
