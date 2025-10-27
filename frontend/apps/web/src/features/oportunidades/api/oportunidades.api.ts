/**
 * API: Oportunidades
 *
 * Servicio para interactuar con los endpoints de oportunidades del backend.
 */

import axios from 'axios';
import type {
  Oportunidad,
  OportunidadFormData,
  OportunidadListParams,
  Page,
} from '../types/oportunidad.types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:28080/api';
const OPORTUNIDADES_API = `${API_BASE_URL}/v1/oportunidades`;

const http = axios.create({
  baseURL: OPORTUNIDADES_API,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

export const oportunidadesApi = {
  list: async (params: OportunidadListParams = {}): Promise<Page<Oportunidad>> => {
    const { page = 0, size = 20, sort = 'createdAt', direction = 'DESC', clienteId, etapaId, propietarioId, q } = params;

    const searchParams = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sort: `${sort},${direction.toLowerCase()}`,
    });

    if (clienteId) {
      const response = await http.get<Page<Oportunidad>>(`/cliente/${clienteId}`, { params: searchParams });
      return response.data;
    }

    if (etapaId) {
      const response = await http.get<Page<Oportunidad>>(`/etapa/${etapaId}`, { params: searchParams });
      return response.data;
    }

    if (propietarioId) {
      const response = await http.get<Page<Oportunidad>>(`/propietario/${propietarioId}`, { params: searchParams });
      return response.data;
    }

    if (q) {
      searchParams.append('q', q);
      const response = await http.get<Page<Oportunidad>>('/search', { params: searchParams });
      return response.data;
    }

    const response = await http.get<Page<Oportunidad>>('/', { params: searchParams });
    return response.data;
  },

  getById: async (id: string): Promise<Oportunidad> => {
    const response = await http.get<Oportunidad>(`/${id}`);
    return response.data;
  },

  create: async (oportunidad: OportunidadFormData): Promise<Oportunidad> => {
    const response = await http.post<Oportunidad>('/', oportunidad);
    return response.data;
  },

  update: async (id: string, oportunidad: OportunidadFormData): Promise<Oportunidad> => {
    const response = await http.put<Oportunidad>(`/${id}`, oportunidad);
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await http.delete(`/${id}`);
  },

  moverAEtapa: async (id: string, etapaId: string, probabilidad: number): Promise<Oportunidad> => {
    const response = await http.put<Oportunidad>(`/${id}/mover-etapa`, { etapaId, probabilidad });
    return response.data;
  },

  marcarComoGanada: async (id: string, fechaCierre?: string): Promise<Oportunidad> => {
    const response = await http.put<Oportunidad>(`/${id}/marcar-ganada`, fechaCierre ? { fechaCierre } : {});
    return response.data;
  },

  marcarComoPerdida: async (id: string, motivo: string): Promise<Oportunidad> => {
    const response = await http.put<Oportunidad>(`/${id}/marcar-perdida`, { motivo });
    return response.data;
  },

  countByEtapa: async (etapaId: string): Promise<number> => {
    const response = await http.get<number>(`/count/etapa/${etapaId}`);
    return response.data;
  },
};
