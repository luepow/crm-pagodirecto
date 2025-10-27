/**
 * Feature: Ventas - Type Definitions
 */

export enum PedidoStatus {
  PENDIENTE = 'PENDIENTE',
  CONFIRMADO = 'CONFIRMADO',
  EN_PROCESO = 'EN_PROCESO',
  ENVIADO = 'ENVIADO',
  ENTREGADO = 'ENTREGADO',
  CANCELADO = 'CANCELADO',
  DEVUELTO = 'DEVUELTO',
}

export interface Pedido {
  id?: string;
  unidadNegocioId: string;
  cotizacionId?: string;
  clienteId: string;
  clienteNombre?: string;
  numero: string;
  fecha: string;
  fechaEntregaEstimada?: string;
  fechaEntregaReal?: string;
  status: PedidoStatus;
  subtotal: number;
  descuentoGlobal?: number;
  impuestos: number;
  total: number;
  moneda: string;
  metodoPago?: string;
  terminosPago?: string;
  notas?: string;
  propietarioId: string;
  propietarioNombre?: string;
  cantidadItems?: number;
  createdAt?: string;
  createdBy?: string;
  createdByNombre?: string;
  updatedAt?: string;
  updatedBy?: string;
}

export interface PedidoFormData {
  unidadNegocioId: string;
  clienteId: string;
  numero: string;
  fecha: string;
  fechaEntregaEstimada?: string;
  status: PedidoStatus;
  subtotal: number;
  descuentoGlobal?: number;
  impuestos: number;
  total: number;
  moneda: string;
  metodoPago?: string;
  terminosPago?: string;
  notas?: string;
  propietarioId: string;
}

export interface PedidoListParams {
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'ASC' | 'DESC';
  clienteId?: string;
  status?: PedidoStatus;
  fechaInicio?: string;
  fechaFin?: string;
  propietarioId?: string;
  q?: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
