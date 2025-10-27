/**
 * Authentication API
 *
 * API functions for authentication and user management.
 */

import { apiClient } from './client';
import type { LoginRequest, LoginResponse, User } from './types';

/**
 * Login user
 */
export const login = async (credentials: LoginRequest): Promise<LoginResponse> => {
  // Backend mock expects email field
  const loginData = {
    email: credentials.email,
    password: credentials.password
  };
  const response = await apiClient.post<LoginResponse>('/v1/auth/login', loginData);
  return response.data;
};

/**
 * Logout user
 */
export const logout = async (): Promise<void> => {
  await apiClient.post('/auth/logout');
};

/**
 * Get current user
 */
export const getCurrentUser = async (): Promise<User> => {
  const response = await apiClient.get<User>('/v1/auth/me');
  return response.data;
};

/**
 * Register new user (admin only)
 */
export const register = async (data: {
  email: string;
  password: string;
  nombre: string;
  apellido: string;
  rol: string;
}): Promise<User> => {
  const response = await apiClient.post<User>('/auth/register', data);
  return response.data;
};

/**
 * Request password reset
 */
export const requestPasswordReset = async (email: string): Promise<void> => {
  await apiClient.post('/auth/forgot-password', { email });
};

/**
 * Reset password with token
 */
export const resetPassword = async (token: string, newPassword: string): Promise<void> => {
  await apiClient.post('/auth/reset-password', { token, newPassword });
};
