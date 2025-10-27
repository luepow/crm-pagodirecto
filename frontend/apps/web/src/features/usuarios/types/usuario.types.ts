/**
 * Types: Usuario
 *
 * Definiciones de tipos para el módulo de usuarios
 */

export type UsuarioStatus = 'ACTIVE' | 'INACTIVE' | 'LOCKED' | 'SUSPENDED';

export interface Role {
  id: string;
  nombre: string;
  descripcion: string;
  departamento: string;
  nivelJerarquico: number;
  createdAt: string;
}

export interface Usuario {
  id: string;
  unidadNegocioId: string;
  username: string;
  email: string;
  nombre?: string;
  apellido?: string;
  nombreCompleto?: string;
  telefono?: string;
  cargo?: string;
  departamento?: string;
  departamentoNombre?: string;
  photoUrl?: string;
  mfaEnabled: boolean;
  status: UsuarioStatus;
  ultimoAcceso?: string;
  intentosFallidos: number;
  bloqueadoHasta?: string;
  roles: Role[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateUsuarioRequest {
  unidadNegocioId: string;
  username: string;
  email: string;
  password: string;
  nombreCompleto?: string;
  telefono?: string;
  cargo?: string;
  departamento?: string;
  photoUrl?: string;
  status?: UsuarioStatus;
  roleIds?: string[];
}

export interface UpdateUsuarioRequest {
  email: string;
  nombreCompleto?: string;
  telefono?: string;
  cargo?: string;
  departamento?: string;
  photoUrl?: string;
  status?: UsuarioStatus;
  roleIds?: string[];
}

export interface ResetPasswordRequest {
  newPassword: string;
}

export interface BloquearUsuarioRequest {
  duracionSegundos?: number;
}

export interface UsuarioFormData extends CreateUsuarioRequest {}

export interface PerfilUsuario extends Usuario {}

export interface CambiarPasswordData {
  passwordActual: string;
  passwordNueva: string;
  passwordNuevaConfirm: string;
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
