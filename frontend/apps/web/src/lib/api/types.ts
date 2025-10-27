/**
 * API Types and Interfaces
 *
 * Shared type definitions for API requests and responses.
 */

/**
 * User entity
 */
export interface User {
  id: string;
  email: string;
  nombre: string;
  apellido: string;
  rol: 'ADMIN' | 'MANAGER' | 'SALES_REP' | 'USER';
  unidadNegocioId: string;
  avatarUrl?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Authentication types
 */
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: {
    id: string;
    email: string;
    nombre: string;
    apellido: string;
    rol: string;
  };
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
}

/**
 * Pagination types
 */
export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

/**
 * Cliente (Customer) entity
 */
export interface Cliente {
  id: string;
  nombre: string;
  apellido?: string;
  email: string;
  telefono?: string;
  empresa?: string;
  cargo?: string;
  tipo: 'PERSONA' | 'EMPRESA';
  status: 'ACTIVO' | 'INACTIVO' | 'PROSPECTO';
  unidadNegocioId: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Oportunidad (Opportunity) entity
 */
export interface Oportunidad {
  id: string;
  titulo: string;
  descripcion?: string;
  clienteId: string;
  cliente?: Cliente;
  valor: number;
  etapa: 'PROSPECTO' | 'CALIFICACION' | 'PROPUESTA' | 'NEGOCIACION' | 'GANADA' | 'PERDIDA';
  probabilidad: number;
  fechaCierre?: string;
  unidadNegocioId: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Producto (Product) entity
 */
export interface Producto {
  id: string;
  nombre: string;
  descripcion?: string;
  sku: string;
  precio: number;
  categoria: string;
  stock: number;
  unidadNegocioId: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Tarea (Task) entity
 */
export interface Tarea {
  id: string;
  titulo: string;
  descripcion?: string;
  tipo: 'LLAMADA' | 'EMAIL' | 'REUNION' | 'SEGUIMIENTO' | 'OTRO';
  prioridad: 'BAJA' | 'MEDIA' | 'ALTA' | 'URGENTE';
  status: 'PENDIENTE' | 'EN_PROGRESO' | 'COMPLETADA' | 'CANCELADA';
  fechaVencimiento?: string;
  clienteId?: string;
  oportunidadId?: string;
  asignadoA?: string;
  unidadNegocioId: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Dashboard KPI types
 */
export interface DashboardKPIs {
  clientesNuevos: number;
  clientesNuevosChange: number;
  oportunidadesActivas: number;
  oportunidadesActivasChange: number;
  tareasPendientes: number;
  tareasPendientesChange: number;
  forecastVentas: number;
  forecastVentasChange: number;
}

export interface VentasPorMes {
  mes: string;
  ventas: number;
}

export interface PipelinePorEtapa {
  etapa: string;
  cantidad: number;
  valor: number;
}

export interface ActividadReciente {
  id: string;
  tipo: 'CLIENTE_CREADO' | 'OPORTUNIDAD_CREADA' | 'TAREA_COMPLETADA' | 'VENTA_CERRADA';
  descripcion: string;
  usuario: string;
  fecha: string;
}
