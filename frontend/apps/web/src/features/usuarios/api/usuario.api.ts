/**
 * API: Usuario
 *
 * Servicios de API para gestión de usuarios
 */

import axios from 'axios';
import type {
  Usuario,
  CreateUsuarioRequest,
  UpdateUsuarioRequest,
  ResetPasswordRequest,
  BloquearUsuarioRequest,
} from '../types/usuario.types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:28080/api';

const http = axios.create({
  baseURL: `${API_BASE_URL}/v1/usuarios`,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

export const usuarioApi = {
  /**
   * Obtiene todos los usuarios
   */
  getAllUsuarios: async (): Promise<Usuario[]> => {
    const response = await http.get<Usuario[]>('');
    return response.data;
  },

  /**
   * Obtiene usuarios por unidad de negocio
   */
  getUsuariosByUnidadNegocio: async (unidadNegocioId: string): Promise<Usuario[]> => {
    const response = await http.get<Usuario[]>(`/unidad-negocio/${unidadNegocioId}`);
    return response.data;
  },

  /**
   * Obtiene un usuario por su ID
   */
  getUsuarioById: async (id: string): Promise<Usuario> => {
    const response = await http.get<Usuario>(`/${id}`);
    return response.data;
  },

  /**
   * Crea un nuevo usuario
   */
  createUsuario: async (data: CreateUsuarioRequest): Promise<Usuario> => {
    const response = await http.post<Usuario>('', data);
    return response.data;
  },

  /**
   * Actualiza un usuario existente
   */
  updateUsuario: async (id: string, data: UpdateUsuarioRequest): Promise<Usuario> => {
    const response = await http.put<Usuario>(`/${id}`, data);
    return response.data;
  },

  /**
   * Elimina un usuario (soft delete)
   */
  deleteUsuario: async (id: string): Promise<void> => {
    await http.delete(`/${id}`);
  },

  /**
   * Bloquea un usuario
   */
  bloquearUsuario: async (id: string, data?: BloquearUsuarioRequest): Promise<void> => {
    await http.post(`/${id}/bloquear`, data);
  },

  /**
   * Desbloquea un usuario
   */
  desbloquearUsuario: async (id: string): Promise<void> => {
    await http.post(`/${id}/desbloquear`);
  },

  /**
   * Restablece la contraseña de un usuario
   */
  resetPassword: async (id: string, data: ResetPasswordRequest): Promise<void> => {
    await http.post(`/${id}/reset-password`, data);
  },
};
