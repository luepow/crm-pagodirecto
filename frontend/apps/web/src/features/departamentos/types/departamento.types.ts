/**
 * Types: Departamento
 *
 * Definiciones de tipos para el módulo de departamentos
 */

export interface Departamento {
  id: string;
  unidadNegocioId: string;
  codigo: string;
  nombre: string;
  descripcion?: string;
  parentId?: string;
  parentNombre?: string;
  nivel: number;
  path: string;
  jefeId?: string;
  jefeNombre?: string;
  emailDepartamento?: string;
  telefonoDepartamento?: string;
  ubicacion?: string;
  presupuestoAnual?: number;
  numeroEmpleados: number;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateDepartamentoRequest {
  unidadNegocioId: string;
  codigo: string;
  nombre: string;
  descripcion?: string;
  parentId?: string;
  jefeId?: string;
  emailDepartamento?: string;
  telefonoDepartamento?: string;
  ubicacion?: string;
  presupuestoAnual?: number;
  numeroEmpleados?: number;
  activo?: boolean;
}

export interface UpdateDepartamentoRequest {
  nombre: string;
  descripcion?: string;
  parentId?: string;
  jefeId?: string;
  emailDepartamento?: string;
  telefonoDepartamento?: string;
  ubicacion?: string;
  presupuestoAnual?: number;
  numeroEmpleados?: number;
  activo?: boolean;
}
