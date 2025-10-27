/**
 * API: Clientes
 *
 * Servicio para interactuar con los endpoints de clientes del backend.
 */

import axios from 'axios';
import type {
  Cliente,
  ClienteFormData,
  ClienteListParams,
  ClienteStatus,
  ImportacionResult,
  Page,
} from '../types/cliente.types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:28080/api';
const CLIENTES_API = `${API_BASE_URL}/v1/clientes`;

/**
 * Cliente HTTP con configuración por defecto
 */
const http = axios.create({
  baseURL: CLIENTES_API,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

/**
 * Servicio de API para clientes
 */
export const clientesApi = {
  /**
   * Lista todos los clientes con paginación y filtros
   */
  list: async (params: ClienteListParams = {}): Promise<Page<Cliente>> => {
    const { page = 0, size = 20, sort = 'nombre', direction = 'ASC', status, q } = params;

    const searchParams = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sort: `${sort},${direction.toLowerCase()}`,
    });

    if (status) {
      const response = await http.get<Page<Cliente>>(`/status/${status}`, { params: searchParams });
      return response.data;
    }

    if (q) {
      searchParams.append('q', q);
      const response = await http.get<Page<Cliente>>('/search', { params: searchParams });
      return response.data;
    }

    const response = await http.get<Page<Cliente>>('', { params: searchParams });
    return response.data;
  },

  /**
   * Obtiene un cliente por ID
   */
  getById: async (id: string): Promise<Cliente> => {
    const response = await http.get<Cliente>(`/${id}`);
    return response.data;
  },

  /**
   * Obtiene un cliente por código
   */
  getByCodigo: async (codigo: string): Promise<Cliente> => {
    const response = await http.get<Cliente>(`/codigo/${codigo}`);
    return response.data;
  },

  /**
   * Crea un nuevo cliente
   */
  create: async (cliente: ClienteFormData): Promise<Cliente> => {
    const response = await http.post<Cliente>('', cliente);
    return response.data;
  },

  /**
   * Actualiza un cliente existente
   */
  update: async (id: string, cliente: ClienteFormData): Promise<Cliente> => {
    const response = await http.put<Cliente>(`/${id}`, cliente);
    return response.data;
  },

  /**
   * Elimina un cliente (soft delete)
   */
  delete: async (id: string): Promise<void> => {
    await http.delete(`/${id}`);
  },

  /**
   * Activa un cliente
   */
  activate: async (id: string): Promise<Cliente> => {
    const response = await http.put<Cliente>(`/${id}/activar`);
    return response.data;
  },

  /**
   * Desactiva un cliente
   */
  deactivate: async (id: string): Promise<Cliente> => {
    const response = await http.put<Cliente>(`/${id}/desactivar`);
    return response.data;
  },

  /**
   * Convierte un lead a prospecto
   */
  convertToProspect: async (id: string): Promise<Cliente> => {
    const response = await http.put<Cliente>(`/${id}/convertir-a-prospecto`);
    return response.data;
  },

  /**
   * Convierte un prospecto a cliente
   */
  convertToClient: async (id: string): Promise<Cliente> => {
    const response = await http.put<Cliente>(`/${id}/convertir-a-cliente`);
    return response.data;
  },

  /**
   * Agrega un cliente a la lista negra
   */
  addToBlacklist: async (id: string, motivo: string): Promise<Cliente> => {
    const response = await http.put<Cliente>(`/${id}/blacklist`, null, {
      params: { motivo },
    });
    return response.data;
  },

  /**
   * Cuenta clientes por status
   */
  countByStatus: async (status: ClienteStatus): Promise<number> => {
    const response = await http.get<number>(`/count/status/${status}`);
    return response.data;
  },

  /**
   * Importa clientes desde un archivo CSV
   */
  importFromCSV: async (file: File, unidadNegocioId: string): Promise<ImportacionResult> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('unidadNegocioId', unidadNegocioId);

    const response = await http.post<ImportacionResult>('/importar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    return response.data;
  },
};
