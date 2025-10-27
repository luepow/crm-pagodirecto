/**
 * API: Dashboard
 */

import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:28080/api';

const http = axios.create({
  baseURL: `${API_BASE_URL}/v1`,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

export interface DashboardStats {
  totalClientes: number;
  clientesNuevosEsteMes: number;
  clientesCambioMensual: number;
  oportunidadesActivas: number;
  oportunidadesGanadas: number;
  oportunidadesCambioMensual: number;
  valorTotalOportunidades: number;
  tareasPendientes: number;
  tareasCompletadas: number;
  tareasVencidas: number;
  tareasCambioMensual: number;
  totalPedidos: number;
  pedidosEsteMes: number;
  ventasTotalesEsteMes: number;
  ventasMesAnterior: number;
  ventasCambioMensual: number;
  totalProductos: number;
  productosStockBajo: number;
}

export const dashboardApi = {
  getStats: async (): Promise<DashboardStats> => {
    const response = await http.get<DashboardStats>('/dashboard/stats');
    return response.data;
  },
};
