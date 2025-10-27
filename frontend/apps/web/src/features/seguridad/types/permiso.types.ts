/**
 * Tipos TypeScript para el módulo de Permisos
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */

export interface PermisoDTO {
  id: string;
  recurso: string;
  accion: string;
  scope: string;
  descripcion: string;
  createdAt: string;
}

export interface CreatePermisoRequest {
  recurso: string;
  accion: 'CREATE' | 'READ' | 'UPDATE' | 'DELETE' | 'EXECUTE' | 'ADMIN';
  scope: string;
  descripcion: string;
}

export interface UpdatePermisoRequest {
  scope?: string;
  descripcion?: string;
}

export type AccionType = 'CREATE' | 'READ' | 'UPDATE' | 'DELETE' | 'EXECUTE' | 'ADMIN';
