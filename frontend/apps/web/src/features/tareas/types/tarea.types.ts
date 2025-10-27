/**
 * Feature: Tareas - Type Definitions
 *
 * Definiciones de tipos TypeScript para el módulo de tareas.
 */

export enum TipoTarea {
  LLAMADA = 'LLAMADA',
  EMAIL = 'EMAIL',
  REUNION = 'REUNION',
  SEGUIMIENTO = 'SEGUIMIENTO',
  ADMINISTRATIVA = 'ADMINISTRATIVA',
  TECNICA = 'TECNICA',
  OTRA = 'OTRA',
}

export enum PrioridadTarea {
  BAJA = 'BAJA',
  MEDIA = 'MEDIA',
  ALTA = 'ALTA',
  URGENTE = 'URGENTE',
}

export enum StatusTarea {
  PENDIENTE = 'PENDIENTE',
  EN_PROGRESO = 'EN_PROGRESO',
  COMPLETADA = 'COMPLETADA',
  CANCELADA = 'CANCELADA',
  BLOQUEADA = 'BLOQUEADA',
}

export interface Tarea {
  id?: string;
  unidadNegocioId: string;
  titulo: string;
  descripcion?: string;
  tipo: TipoTarea;
  prioridad: PrioridadTarea;
  status: StatusTarea;
  fechaVencimiento?: string;
  fechaCompletada?: string;
  asignadoA: string;
  asignadoNombre?: string;
  relacionadoTipo?: string;
  relacionadoId?: string;
  relacionadoNombre?: string;
  vencida?: boolean;
  createdAt?: string;
  createdBy?: string;
  createdByNombre?: string;
  updatedAt?: string;
  updatedBy?: string;
}

export interface TareaFormData {
  unidadNegocioId: string;
  titulo: string;
  descripcion?: string;
  tipo: TipoTarea;
  prioridad: PrioridadTarea;
  status: StatusTarea;
  fechaVencimiento?: string;
  asignadoA: string;
  relacionadoTipo?: string;
  relacionadoId?: string;
}

export interface TareaListParams {
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'ASC' | 'DESC';
  asignadoA?: string;
  status?: StatusTarea;
  relacionadoTipo?: string;
  relacionadoId?: string;
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

export interface TareaStats {
  pendientes: number;
  enProgreso: number;
  completadas: number;
  vencidas: number;
}
