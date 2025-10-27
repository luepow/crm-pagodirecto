/**
 * API: Tareas
 *
 * Servicio para interactuar con los endpoints de tareas del backend.
 */

import axios from 'axios';
import type {
  Tarea,
  TareaFormData,
  TareaListParams,
  Page,
  StatusTarea,
} from '../types/tarea.types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:28080/api';
const TAREAS_API = `${API_BASE_URL}/v1/tareas`;

const http = axios.create({
  baseURL: TAREAS_API,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

export const tareasApi = {
  /**
   * Lista tareas con filtros y paginación
   */
  list: async (params: TareaListParams = {}): Promise<Page<Tarea>> => {
    const {
      page = 0,
      size = 20,
      sort = 'createdAt',
      direction = 'DESC',
      asignadoA,
      status,
      relacionadoTipo,
      relacionadoId,
      q,
    } = params;

    const searchParams = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sort: `${sort},${direction.toLowerCase()}`,
    });

    // Ruta específica según el filtro
    if (asignadoA) {
      const response = await http.get<Page<Tarea>>(`/asignado/${asignadoA}`, { params: searchParams });
      return response.data;
    }

    if (status) {
      const response = await http.get<Page<Tarea>>(`/status/${status}`, { params: searchParams });
      return response.data;
    }

    if (relacionadoTipo && relacionadoId) {
      const response = await http.get<Page<Tarea>>(`/relacionado/${relacionadoTipo}/${relacionadoId}`, {
        params: searchParams,
      });
      return response.data;
    }

    if (q) {
      searchParams.append('q', q);
      const response = await http.get<Page<Tarea>>('/search', { params: searchParams });
      return response.data;
    }

    const response = await http.get<Page<Tarea>>('/', { params: searchParams });
    return response.data;
  },

  /**
   * Obtiene una tarea por ID
   */
  getById: async (id: string): Promise<Tarea> => {
    const response = await http.get<Tarea>(`/${id}`);
    return response.data;
  },

  /**
   * Crea una nueva tarea
   */
  create: async (tarea: TareaFormData): Promise<Tarea> => {
    const response = await http.post<Tarea>('/', tarea);
    return response.data;
  },

  /**
   * Actualiza una tarea existente
   */
  update: async (id: string, tarea: TareaFormData): Promise<Tarea> => {
    const response = await http.put<Tarea>(`/${id}`, tarea);
    return response.data;
  },

  /**
   * Elimina una tarea (soft delete)
   */
  delete: async (id: string): Promise<void> => {
    await http.delete(`/${id}`);
  },

  /**
   * Marca una tarea como completada
   */
  completar: async (id: string): Promise<Tarea> => {
    const response = await http.put<Tarea>(`/${id}/completar`);
    return response.data;
  },

  /**
   * Cancela una tarea
   */
  cancelar: async (id: string): Promise<Tarea> => {
    const response = await http.put<Tarea>(`/${id}/cancelar`);
    return response.data;
  },

  /**
   * Reasigna una tarea a otro usuario
   */
  reasignar: async (id: string, asignadoA: string): Promise<Tarea> => {
    const response = await http.put<Tarea>(`/${id}/reasignar`, { asignadoA });
    return response.data;
  },

  /**
   * Lista tareas vencidas
   */
  listarVencidas: async (page = 0, size = 20): Promise<Page<Tarea>> => {
    const response = await http.get<Page<Tarea>>('/vencidas', {
      params: { page, size, sort: 'fechaVencimiento,asc' },
    });
    return response.data;
  },

  /**
   * Lista tareas por vencer en los próximos N días
   */
  listarPorVencer: async (dias = 7, page = 0, size = 20): Promise<Page<Tarea>> => {
    const response = await http.get<Page<Tarea>>('/por-vencer', {
      params: { dias, page, size, sort: 'fechaVencimiento,asc' },
    });
    return response.data;
  },

  /**
   * Cuenta tareas por usuario y status
   */
  contarPorAsignadoYStatus: async (usuarioId: string, status: StatusTarea): Promise<number> => {
    const response = await http.get<number>(`/count/asignado/${usuarioId}/status/${status}`);
    return response.data;
  },

  /**
   * Encuentra tareas inactivas (sin actualizar en X días)
   */
  encontrarInactivas: async (dias = 30): Promise<Tarea[]> => {
    const response = await http.get<Tarea[]>('/inactivas', { params: { dias } });
    return response.data;
  },
};
