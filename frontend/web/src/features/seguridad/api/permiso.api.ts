/**
 * API Service para gestión de Permisos
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */

import axios from 'axios';
import { PermisoDTO, CreatePermisoRequest, UpdatePermisoRequest } from '../types/permiso.types';

const API_BASE_URL = 'http://localhost:8080/api/v1/permisos';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const permisoApi = {
  /**
   * Obtiene todos los permisos
   */
  getAllPermisos: async (): Promise<PermisoDTO[]> => {
    const response = await axiosInstance.get<PermisoDTO[]>('');
    return response.data;
  },

  /**
   * Obtiene permisos por recurso
   */
  getPermisosByRecurso: async (recurso: string): Promise<PermisoDTO[]> => {
    const response = await axiosInstance.get<PermisoDTO[]>(`/recurso/${recurso}`);
    return response.data;
  },

  /**
   * Obtiene permisos por acción
   */
  getPermisosByAccion: async (accion: string): Promise<PermisoDTO[]> => {
    const response = await axiosInstance.get<PermisoDTO[]>(`/accion/${accion}`);
    return response.data;
  },

  /**
   * Obtiene un permiso por su ID
   */
  getPermisoById: async (id: string): Promise<PermisoDTO> => {
    const response = await axiosInstance.get<PermisoDTO>(`/${id}`);
    return response.data;
  },

  /**
   * Crea un nuevo permiso
   */
  createPermiso: async (data: CreatePermisoRequest): Promise<PermisoDTO> => {
    const response = await axiosInstance.post<PermisoDTO>('', data);
    return response.data;
  },

  /**
   * Actualiza un permiso existente
   */
  updatePermiso: async (id: string, data: UpdatePermisoRequest): Promise<PermisoDTO> => {
    const response = await axiosInstance.put<PermisoDTO>(`/${id}`, data);
    return response.data;
  },

  /**
   * Elimina un permiso (soft delete)
   */
  deletePermiso: async (id: string): Promise<void> => {
    await axiosInstance.delete(`/${id}`);
  },
};
