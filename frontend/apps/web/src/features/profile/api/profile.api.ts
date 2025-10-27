/**
 * API: Profile
 *
 * Servicios de API para gestión del perfil de usuario
 */

import axios from 'axios';
import type { Profile, UpdateProfileRequest, ChangePasswordRequest, MFAResponse } from '../types/profile.types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:28080/api';

const http = axios.create({
  baseURL: `${API_BASE_URL}/v1/profile`,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

export const profileApi = {
  /**
   * Obtiene el perfil del usuario actual
   */
  getMyProfile: async (): Promise<Profile> => {
    const response = await http.get<Profile>('/');
    return response.data;
  },

  /**
   * Actualiza el perfil del usuario actual
   */
  updateMyProfile: async (data: UpdateProfileRequest): Promise<Profile> => {
    const response = await http.put<Profile>('/', data);
    return response.data;
  },

  /**
   * Cambia la contraseña del usuario actual
   */
  changePassword: async (data: ChangePasswordRequest): Promise<void> => {
    await http.put('/change-password', data);
  },

  /**
   * Habilita MFA para el usuario actual
   */
  enableMFA: async (): Promise<MFAResponse> => {
    const response = await http.post<MFAResponse>('/mfa/enable');
    return response.data;
  },

  /**
   * Deshabilita MFA para el usuario actual
   */
  disableMFA: async (): Promise<MFAResponse> => {
    const response = await http.post<MFAResponse>('/mfa/disable');
    return response.data;
  },
};
