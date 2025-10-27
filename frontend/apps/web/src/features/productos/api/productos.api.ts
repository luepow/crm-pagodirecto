/**
 * API: Productos
 */

import axios from 'axios';
import type { Producto, ProductoFormData, ProductoListParams, Page, ProductoStatus } from '../types/producto.types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:28080/api';
const PRODUCTOS_API = `${API_BASE_URL}/v1/productos`;

const http = axios.create({
  baseURL: PRODUCTOS_API,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

export const productosApi = {
  list: async (params: ProductoListParams = {}): Promise<Page<Producto>> => {
    const { page = 0, size = 20, sort = 'nombre', direction = 'ASC', status, categoriaId, q } = params;
    const searchParams = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sort: `${sort},${direction.toLowerCase()}`,
    });

    if (status) {
      const response = await http.get<Page<Producto>>(`/status/${status}`, { params: searchParams });
      return response.data;
    }
    if (categoriaId) {
      const response = await http.get<Page<Producto>>(`/categoria/${categoriaId}`, { params: searchParams });
      return response.data;
    }
    if (q) {
      searchParams.append('q', q);
      const response = await http.get<Page<Producto>>('/search', { params: searchParams });
      return response.data;
    }
    const response = await http.get<Page<Producto>>('', { params: searchParams });
    return response.data;
  },

  getById: async (id: string): Promise<Producto> => {
    const response = await http.get<Producto>(`/${id}`);
    return response.data;
  },

  create: async (producto: ProductoFormData): Promise<Producto> => {
    const response = await http.post<Producto>('', producto);
    return response.data;
  },

  update: async (id: string, producto: ProductoFormData): Promise<Producto> => {
    const response = await http.put<Producto>(`/${id}`, producto);
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await http.delete(`/${id}`);
  },

  actualizarStock: async (id: string, cantidad: number): Promise<Producto> => {
    const response = await http.put<Producto>(`/${id}/stock`, { cantidad });
    return response.data;
  },

  activar: async (id: string): Promise<Producto> => {
    const response = await http.put<Producto>(`/${id}/activar`);
    return response.data;
  },

  desactivar: async (id: string): Promise<Producto> => {
    const response = await http.put<Producto>(`/${id}/desactivar`);
    return response.data;
  },

  listarParaReabastecer: async (): Promise<Producto[]> => {
    const response = await http.get<Producto[]>('/reabastecer');
    return response.data;
  },

  contarPorStatus: async (status: ProductoStatus): Promise<number> => {
    const response = await http.get<number>(`/count/status/${status}`);
    return response.data;
  },
};
