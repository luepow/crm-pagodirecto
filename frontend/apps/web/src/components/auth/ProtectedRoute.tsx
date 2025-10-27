/**
 * Protected Route Component
 *
 * Wrapper component for routes that require authentication.
 * Redirects to login if user is not authenticated.
 */

import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../../lib/stores/authStore';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

/**
 * Protected route component
 */
export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated } = useAuthStore();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};
