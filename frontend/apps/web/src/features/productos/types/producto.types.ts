/**
 * Feature: Productos - Type Definitions
 */

export enum ProductoTipo {
  PRODUCTO = 'PRODUCTO',
  SERVICIO = 'SERVICIO',
  COMBO = 'COMBO',
}

export enum ProductoStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  DISCONTINUED = 'DISCONTINUED',
}

export interface Producto {
  id?: string;
  unidadNegocioId: string;
  codigo: string;
  nombre: string;
  descripcion?: string;
  categoriaId?: string;
  categoriaNombre?: string;
  tipo: ProductoTipo;
  precioBase: number;
  moneda: string;
  costoUnitario?: number;
  status: ProductoStatus;
  stockActual?: number;
  stockMinimo?: number;
  unidadMedida?: string;
  pesoKg?: number;
  sku?: string;
  codigoBarras?: string;
  imagenUrl?: string;
  requiereReabastecimiento?: boolean;
  margenBruto?: number;
  createdAt?: string;
  createdBy?: string;
  createdByNombre?: string;
  updatedAt?: string;
  updatedBy?: string;
}

export interface ProductoFormData {
  unidadNegocioId: string;
  codigo: string;
  nombre: string;
  descripcion?: string;
  categoriaId?: string;
  tipo: ProductoTipo;
  precioBase: number;
  moneda: string;
  costoUnitario?: number;
  status: ProductoStatus;
  stockActual?: number;
  stockMinimo?: number;
  unidadMedida?: string;
  pesoKg?: number;
  sku?: string;
  codigoBarras?: string;
  imagenUrl?: string;
}

export interface ProductoListParams {
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'ASC' | 'DESC';
  status?: ProductoStatus;
  categoriaId?: string;
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
