/**
 * Types: Cliente
 *
 * Definiciones de tipos TypeScript para el dominio de clientes.
 * Sincronizado con los DTOs del backend.
 */

export enum ClienteTipo {
  PERSONA = 'PERSONA',
  EMPRESA = 'EMPRESA',
}

export enum ClienteStatus {
  LEAD = 'LEAD',
  PROSPECT = 'PROSPECT',
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  BLACKLIST = 'BLACKLIST',
}

export interface Cliente {
  id?: string;
  unidadNegocioId: string;
  codigo: string;
  nombre: string;
  email?: string;
  telefono?: string;
  tipo: ClienteTipo;
  rfc?: string;
  razonSocial?: string;
  status: ClienteStatus;
  segmento?: string;
  fuente?: string;
  propietarioId?: string;
  notas?: string;
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
}

export interface ClienteFormData extends Omit<Cliente, 'id' | 'codigo' | 'createdAt' | 'createdBy' | 'updatedAt' | 'updatedBy'> {
  codigo?: string;
}

export interface ImportacionResult {
  totalRegistros: number;
  registrosExitosos: number;
  registrosConErrores: number;
  errores: string[];
  clientesCreados: Cliente[];
  mensaje: string;
  tasaExito: number;
  exitoCompleto: boolean;
}

export interface ClienteListParams {
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'ASC' | 'DESC';
  status?: ClienteStatus;
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
  empty: boolean;
}
