/**
 * Tipos TypeScript para el módulo de Roles
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */

import { PermisoDTO } from './permiso.types';

export interface RolWithPermisosDTO {
  id: string;
  unidadNegocioId: string;
  nombre: string;
  descripcion: string;
  departamento: string;
  nivelJerarquico: number;
  permisos: PermisoDTO[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateRolRequest {
  unidadNegocioId: string;
  nombre: string;
  descripcion: string;
  departamento: string;
  nivelJerarquico?: number;
  permisoIds?: string[];
}

export interface UpdateRolRequest {
  nombre?: string;
  descripcion?: string;
  departamento?: string;
  nivelJerarquico?: number;
}

export interface AssignPermisosRequest {
  permisoIds: string[];
}
