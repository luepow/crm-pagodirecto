/**
 * Types: Oportunidad
 *
 * Definiciones de tipos TypeScript para el dominio de oportunidades.
 */

export interface Oportunidad {
  id?: string;
  unidadNegocioId: string;
  clienteId: string;
  titulo: string;
  descripcion?: string;
  valorEstimado: number;
  moneda: string;
  probabilidad: number;
  etapaId: string;
  fechaCierreEstimada?: string;
  fechaCierreReal?: string;
  propietarioId: string;
  fuente?: string;
  motivoPerdida?: string;
  valorPonderado?: number;
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
  // Información desnormalizada
  clienteNombre?: string;
  etapaNombre?: string;
  propietarioNombre?: string;
}

export interface OportunidadFormData
  extends Omit<
    Oportunidad,
    'id' | 'valorPonderado' | 'createdAt' | 'createdBy' | 'updatedAt' | 'updatedBy' | 'clienteNombre' | 'etapaNombre' | 'propietarioNombre'
  > {}

export interface OportunidadListParams {
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'ASC' | 'DESC';
  clienteId?: string;
  etapaId?: string;
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
  empty: boolean;
}
