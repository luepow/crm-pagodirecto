/**
 * API: Pedidos (Ventas)
 */

import axios from 'axios';
import type { Pedido, PedidoFormData, PedidoListParams, Page, PedidoStatus } from '../types/pedido.types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:28080/api';
const PEDIDOS_API = `${API_BASE_URL}/v1/ventas/pedidos`;

const http = axios.create({
  baseURL: PEDIDOS_API,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

export const pedidosApi = {
  list: async (params: PedidoListParams = {}): Promise<Page<Pedido>> => {
    const { page = 0, size = 20, sort = 'fecha', direction = 'DESC', clienteId, status, fechaInicio, fechaFin, propietarioId, q } = params;
    const searchParams = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sort: `${sort},${direction.toLowerCase()}`,
    });

    if (clienteId) {
      const response = await http.get<Page<Pedido>>(`/cliente/${clienteId}`, { params: searchParams });
      return response.data;
    }
    if (status) {
      const response = await http.get<Page<Pedido>>(`/status/${status}`, { params: searchParams });
      return response.data;
    }
    if (fechaInicio && fechaFin) {
      searchParams.append('fechaInicio', fechaInicio);
      searchParams.append('fechaFin', fechaFin);
      const response = await http.get<Page<Pedido>>('/fecha-range', { params: searchParams });
      return response.data;
    }
    if (propietarioId) {
      const response = await http.get<Page<Pedido>>(`/propietario/${propietarioId}`, { params: searchParams });
      return response.data;
    }
    if (q) {
      searchParams.append('q', q);
      const response = await http.get<Page<Pedido>>('/search', { params: searchParams });
      return response.data;
    }
    const response = await http.get<Page<Pedido>>('/', { params: searchParams });
    return response.data;
  },

  getById: async (id: string): Promise<Pedido> => {
    const response = await http.get<Pedido>(`/${id}`);
    return response.data;
  },

  create: async (pedido: PedidoFormData): Promise<Pedido> => {
    const response = await http.post<Pedido>('/', pedido);
    return response.data;
  },

  update: async (id: string, pedido: PedidoFormData): Promise<Pedido> => {
    const response = await http.put<Pedido>(`/${id}`, pedido);
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await http.delete(`/${id}`);
  },

  confirmar: async (id: string): Promise<Pedido> => {
    const response = await http.put<Pedido>(`/${id}/confirmar`);
    return response.data;
  },

  marcarEnProceso: async (id: string): Promise<Pedido> => {
    const response = await http.put<Pedido>(`/${id}/en-proceso`);
    return response.data;
  },

  marcarEnviado: async (id: string): Promise<Pedido> => {
    const response = await http.put<Pedido>(`/${id}/enviado`);
    return response.data;
  },

  marcarEntregado: async (id: string, fechaEntrega: string): Promise<Pedido> => {
    const response = await http.put<Pedido>(`/${id}/entregado`, { fechaEntrega });
    return response.data;
  },

  cancelar: async (id: string): Promise<Pedido> => {
    const response = await http.put<Pedido>(`/${id}/cancelar`);
    return response.data;
  },

  contarPorStatus: async (status: PedidoStatus): Promise<number> => {
    const response = await http.get<number>(`/count/status/${status}`);
    return response.data;
  },

  calcularVentasTotales: async (fechaInicio: string, fechaFin: string): Promise<number> => {
    const response = await http.get<number>('/ventas-totales', {
      params: { fechaInicio, fechaFin },
    });
    return response.data;
  },
};
