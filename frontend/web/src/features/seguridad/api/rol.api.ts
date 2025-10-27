/**
 * API Service para gestión de Roles
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */

import axios from 'axios';
import { RolWithPermisosDTO, CreateRolRequest, UpdateRolRequest } from '../types/rol.types';

const API_BASE_URL = 'http://localhost:8080/api/v1/roles';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const rolApi = {
  /**
   * Obtiene todos los roles
   */
  getAllRoles: async (): Promise<RolWithPermisosDTO[]> => {
    const response = await axiosInstance.get<RolWithPermisosDTO[]>('');
    return response.data;
  },

  /**
   * Obtiene roles por unidad de negocio
   */
  getRolesByUnidadNegocio: async (unidadNegocioId: string): Promise<RolWithPermisosDTO[]> => {
    const response = await axiosInstance.get<RolWithPermisosDTO[]>(`/unidad-negocio/${unidadNegocioId}`);
    return response.data;
  },

  /**
   * Obtiene roles por departamento
   */
  getRolesByDepartamento: async (departamento: string): Promise<RolWithPermisosDTO[]> => {
    const response = await axiosInstance.get<RolWithPermisosDTO[]>(`/departamento/${departamento}`);
    return response.data;
  },

  /**
   * Obtiene un rol por su ID
   */
  getRolById: async (id: string): Promise<RolWithPermisosDTO> => {
    const response = await axiosInstance.get<RolWithPermisosDTO>(`/${id}`);
    return response.data;
  },

  /**
   * Crea un nuevo rol
   */
  createRol: async (data: CreateRolRequest): Promise<RolWithPermisosDTO> => {
    const response = await axiosInstance.post<RolWithPermisosDTO>('', data);
    return response.data;
  },

  /**
   * Actualiza un rol existente
   */
  updateRol: async (id: string, data: UpdateRolRequest): Promise<RolWithPermisosDTO> => {
    const response = await axiosInstance.put<RolWithPermisosDTO>(`/${id}`, data);
    return response.data;
  },

  /**
   * Elimina un rol (soft delete)
   */
  deleteRol: async (id: string): Promise<void> => {
    await axiosInstance.delete(`/${id}`);
  },

  /**
   * Asigna permisos a un rol
   */
  assignPermisos: async (id: string, permisoIds: string[]): Promise<void> => {
    await axiosInstance.post(`/${id}/permisos`, permisoIds);
  },

  /**
   * Remueve permisos de un rol
   */
  removePermisos: async (id: string, permisoIds: string[]): Promise<void> => {
    await axiosInstance.delete(`/${id}/permisos`, { data: permisoIds });
  },
};
