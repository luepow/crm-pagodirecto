/**
 * Types: Profile
 *
 * Definiciones de tipos para el perfil de usuario
 */

export enum UsuarioStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  LOCKED = 'LOCKED',
  PENDING = 'PENDING',
}

export interface Role {
  id: string;
  nombre: string;
  descripcion?: string;
}

export interface Profile {
  id: string;
  username: string;
  email: string;
  nombreCompleto?: string;
  telefono?: string;
  cargo?: string;
  departamento?: string;
  photoUrl?: string;
  status: UsuarioStatus;
  mfaEnabled: boolean;
  ultimoAcceso?: string;
  roles: Role[];
  createdAt: string;
}

export interface UpdateProfileRequest {
  email: string;
  nombreCompleto?: string;
  telefono?: string;
  cargo?: string;
  departamento?: string;
  photoUrl?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface MFAResponse {
  message: string;
  secret?: string;
}
