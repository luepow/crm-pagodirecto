/**
 * API: Departamento
 *
 * Servicios de API para gestión de departamentos
 */

import axios from 'axios';
import type {
  Departamento,
  CreateDepartamentoRequest,
  UpdateDepartamentoRequest,
} from '../types/departamento.types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:28080/api';

const http = axios.create({
  baseURL: `${API_BASE_URL}/v1/departamentos`,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

export const departamentoApi = {
  /**
   * Obtiene todos los departamentos
   */
  getAllDepartamentos: async (): Promise<Departamento[]> => {
    const response = await http.get<Departamento[]>('');
    return response.data;
  },

  /**
   * Obtiene departamentos por unidad de negocio
   */
  getDepartamentosByUnidadNegocio: async (unidadNegocioId: string): Promise<Departamento[]> => {
    const response = await http.get<Departamento[]>(`/unidad-negocio/${unidadNegocioId}`);
    return response.data;
  },

  /**
   * Obtiene departamentos activos por unidad de negocio
   */
  getDepartamentosActivosByUnidadNegocio: async (unidadNegocioId: string): Promise<Departamento[]> => {
    const response = await http.get<Departamento[]>(`/unidad-negocio/${unidadNegocioId}/activos`);
    return response.data;
  },

  /**
   * Obtiene departamentos raíz (sin padre)
   */
  getDepartamentosRaiz: async (): Promise<Departamento[]> => {
    const response = await http.get<Departamento[]>('/raiz');
    return response.data;
  },

  /**
   * Obtiene sub-departamentos de un departamento
   */
  getSubDepartamentos: async (parentId: string): Promise<Departamento[]> => {
    const response = await http.get<Departamento[]>(`/${parentId}/sub-departamentos`);
    return response.data;
  },

  /**
   * Obtiene un departamento por su ID
   */
  getDepartamentoById: async (id: string): Promise<Departamento> => {
    const response = await http.get<Departamento>(`/${id}`);
    return response.data;
  },

  /**
   * Obtiene un departamento por su código
   */
  getDepartamentoByCodigo: async (codigo: string): Promise<Departamento> => {
    const response = await http.get<Departamento>(`/codigo/${codigo}`);
    return response.data;
  },

  /**
   * Crea un nuevo departamento
   */
  createDepartamento: async (data: CreateDepartamentoRequest): Promise<Departamento> => {
    const response = await http.post<Departamento>('', data);
    return response.data;
  },

  /**
   * Actualiza un departamento existente
   */
  updateDepartamento: async (id: string, data: UpdateDepartamentoRequest): Promise<Departamento> => {
    const response = await http.put<Departamento>(`/${id}`, data);
    return response.data;
  },

  /**
   * Elimina un departamento (soft delete)
   */
  deleteDepartamento: async (id: string): Promise<void> => {
    await http.delete(`/${id}`);
  },

  /**
   * Activa/desactiva un departamento
   */
  toggleActivoDepartamento: async (id: string): Promise<void> => {
    await http.post(`/${id}/toggle-activo`);
  },
};
