/**
 * API: Usuarios y Seguridad
 */

import axios from 'axios';
import type { Usuario, UsuarioFormData, PerfilUsuario, CambiarPasswordData, Page } from '../types/usuario.types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:28080/api';

const http = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

export const usuariosApi = {
  // Gestión de usuarios (admin)
  list: async (page = 0, size = 20): Promise<Page<Usuario>> => {
    const response = await http.get<Page<Usuario>>('/v1/usuarios', {
      params: { page, size },
    });
    return response.data;
  },

  getById: async (id: string): Promise<Usuario> => {
    const response = await http.get<Usuario>(`/v1/usuarios/${id}`);
    return response.data;
  },

  create: async (usuario: UsuarioFormData): Promise<Usuario> => {
    const response = await http.post<Usuario>('/v1/usuarios', usuario);
    return response.data;
  },

  update: async (id: string, usuario: Partial<UsuarioFormData>): Promise<Usuario> => {
    const response = await http.put<Usuario>(`/v1/usuarios/${id}`, usuario);
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await http.delete(`/v1/usuarios/${id}`);
  },

  activar: async (id: string): Promise<Usuario> => {
    const response = await http.put<Usuario>(`/v1/usuarios/${id}/activar`);
    return response.data;
  },

  desactivar: async (id: string): Promise<Usuario> => {
    const response = await http.put<Usuario>(`/v1/usuarios/${id}/desactivar`);
    return response.data;
  },

  resetPassword: async (id: string): Promise<{ temporaryPassword: string }> => {
    const response = await http.post<{ temporaryPassword: string }>(`/v1/usuarios/${id}/reset-password`);
    return response.data;
  },

  // Perfil del usuario actual
  getPerfil: async (): Promise<PerfilUsuario> => {
    const response = await http.get<PerfilUsuario>('/v1/perfil');
    return response.data;
  },

  updatePerfil: async (data: Partial<PerfilUsuario>): Promise<PerfilUsuario> => {
    const response = await http.put<PerfilUsuario>('/v1/perfil', data);
    return response.data;
  },

  cambiarPassword: async (data: CambiarPasswordData): Promise<void> => {
    await http.post('/v1/perfil/cambiar-password', data);
  },

  uploadAvatar: async (file: File): Promise<{ avatarUrl: string }> => {
    const formData = new FormData();
    formData.append('avatar', file);
    const response = await http.post<{ avatarUrl: string }>('/v1/perfil/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },
};
