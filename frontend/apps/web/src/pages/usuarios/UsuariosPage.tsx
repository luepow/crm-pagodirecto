/**
 * Page: UsuariosPage
 *
 * Página de gestión de usuarios del sistema
 */

import React, { useState, useEffect } from 'react';
import { UserPlus, Search, Edit, Lock, Unlock, Key, Trash2 } from 'lucide-react';
import { Card, CardContent } from '@shared-ui/components/Card';
import { Button } from '@shared-ui/components/Button';
import { toast } from 'sonner';
import { usuarioApi } from '../../features/usuarios/api/usuario.api';
import type { Usuario, UsuarioStatus } from '../../features/usuarios/types/usuario.types';
import { CreateUsuarioModal } from '../../features/usuarios/components/CreateUsuarioModal';
import { EditUsuarioModal } from '../../features/usuarios/components/EditUsuarioModal';

/**
 * UsuariosPage Component
 */
export const UsuariosPage: React.FC = () => {
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [selectedUsuario, setSelectedUsuario] = useState<Usuario | null>(null);

  useEffect(() => {
    loadUsuarios();
  }, []);

  const loadUsuarios = async () => {
    try {
      setIsLoading(true);
      const data = await usuarioApi.getAllUsuarios();
      setUsuarios(data);
    } catch (error) {
      console.error('Error cargando usuarios:', error);
      toast.error('Error al cargar los usuarios');
    } finally {
      setIsLoading(false);
    }
  };

  const handleEdit = (usuario: Usuario) => {
    setSelectedUsuario(usuario);
    setIsEditModalOpen(true);
  };

  const handleDelete = async (usuario: Usuario) => {
    if (!window.confirm(`¿Estás seguro de eliminar al usuario ${usuario.username}?`)) {
      return;
    }

    try {
      await usuarioApi.deleteUsuario(usuario.id);
      toast.success('Usuario eliminado exitosamente');
      loadUsuarios();
    } catch (error) {
      console.error('Error eliminando usuario:', error);
      toast.error('Error al eliminar el usuario');
    }
  };

  const handleToggleBlock = async (usuario: Usuario) => {
    try {
      if (usuario.status === 'LOCKED') {
        await usuarioApi.desbloquearUsuario(usuario.id);
        toast.success('Usuario desbloqueado exitosamente');
      } else {
        await usuarioApi.bloquearUsuario(usuario.id);
        toast.success('Usuario bloqueado exitosamente');
      }
      loadUsuarios();
    } catch (error) {
      console.error('Error al cambiar estado de bloqueo:', error);
      toast.error('Error al cambiar estado de bloqueo');
    }
  };

  const handleResetPassword = async (usuario: Usuario) => {
    const newPassword = window.prompt(
      `Ingrese la nueva contraseña para ${usuario.username} (mínimo 8 caracteres):`
    );

    if (!newPassword) {
      return;
    }

    if (newPassword.length < 8) {
      toast.error('La contraseña debe tener al menos 8 caracteres');
      return;
    }

    try {
      await usuarioApi.resetPassword(usuario.id, { newPassword });
      toast.success('Contraseña restablecida exitosamente');
    } catch (error) {
      console.error('Error restableciendo contraseña:', error);
      toast.error('Error al restablecer la contraseña');
    }
  };

  const handleCreateSuccess = () => {
    setIsCreateModalOpen(false);
    loadUsuarios();
  };

  const handleEditSuccess = () => {
    setIsEditModalOpen(false);
    setSelectedUsuario(null);
    loadUsuarios();
  };

  const filteredUsuarios = usuarios.filter(
    (usuario) =>
      usuario.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
      usuario.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (usuario.nombreCompleto?.toLowerCase().includes(searchTerm.toLowerCase()) ?? false)
  );

  const getStatusBadge = (status: UsuarioStatus) => {
    const styles = {
      ACTIVE: 'bg-green-100 text-green-800',
      INACTIVE: 'bg-gray-100 text-gray-800',
      LOCKED: 'bg-red-100 text-red-800',
      SUSPENDED: 'bg-yellow-100 text-yellow-800',
    };

    const labels = {
      ACTIVE: 'Activo',
      INACTIVE: 'Inactivo',
      LOCKED: 'Bloqueado',
      SUSPENDED: 'Suspendido',
    };

    return (
      <span className={`px-2 py-1 rounded-full text-xs font-medium ${styles[status]}`}>
        {labels[status]}
      </span>
    );
  };

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
          <p className="mt-4 text-text-secondary">Cargando usuarios...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-text-primary">Usuarios</h1>
          <p className="mt-1 text-text-secondary">Gestiona los usuarios del sistema</p>
        </div>

        <Button
          variant="primary"
          leftIcon={<UserPlus size={18} />}
          onClick={() => setIsCreateModalOpen(true)}
        >
          Nuevo Usuario
        </Button>
      </div>

      {/* Search bar */}
      <Card>
        <CardContent className="pt-6">
          <div className="relative">
            <Search
              className="absolute left-3 top-1/2 -translate-y-1/2 text-text-tertiary"
              size={20}
            />
            <input
              type="text"
              placeholder="Buscar por username, email o nombre..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full rounded-xl border border-gray-300 pl-10 pr-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
        </CardContent>
      </Card>

      {/* Users table */}
      <Card>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">
                    Usuario
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">
                    Email
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">
                    Cargo
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">
                    Roles
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">
                    Estado
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-text-secondary uppercase tracking-wider">
                    Acciones
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredUsuarios.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="px-6 py-12 text-center text-text-secondary">
                      {searchTerm
                        ? 'No se encontraron usuarios con ese criterio de búsqueda'
                        : 'No hay usuarios registrados'}
                    </td>
                  </tr>
                ) : (
                  filteredUsuarios.map((usuario) => (
                    <tr key={usuario.id} className="hover:bg-gray-50 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <div className="flex-shrink-0 h-10 w-10">
                            {usuario.photoUrl ? (
                              <img
                                className="h-10 w-10 rounded-full object-cover"
                                src={usuario.photoUrl}
                                alt={usuario.username}
                              />
                            ) : (
                              <div className="h-10 w-10 rounded-full bg-primary text-white flex items-center justify-center font-semibold">
                                {usuario.username.charAt(0).toUpperCase()}
                              </div>
                            )}
                          </div>
                          <div className="ml-4">
                            <div className="text-sm font-medium text-text-primary">
                              {usuario.username}
                            </div>
                            <div className="text-sm text-text-secondary">
                              {usuario.nombreCompleto || 'Sin nombre'}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-text-secondary">
                        {usuario.email}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-text-secondary">
                        {usuario.cargo || '-'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-text-secondary">
                        {usuario.roles.length > 0 ? (
                          <div className="flex flex-wrap gap-1">
                            {usuario.roles.slice(0, 2).map((role) => (
                              <span
                                key={role.id}
                                className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800"
                              >
                                {role.nombre}
                              </span>
                            ))}
                            {usuario.roles.length > 2 && (
                              <span className="text-xs text-text-tertiary">
                                +{usuario.roles.length - 2} más
                              </span>
                            )}
                          </div>
                        ) : (
                          <span className="text-text-tertiary">Sin roles</span>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {getStatusBadge(usuario.status)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <div className="flex justify-end gap-2">
                          <button
                            onClick={() => handleEdit(usuario)}
                            className="text-primary hover:text-primary-600 transition-colors"
                            title="Editar"
                          >
                            <Edit size={18} />
                          </button>
                          <button
                            onClick={() => handleToggleBlock(usuario)}
                            className={`transition-colors ${
                              usuario.status === 'LOCKED'
                                ? 'text-green-600 hover:text-green-700'
                                : 'text-yellow-600 hover:text-yellow-700'
                            }`}
                            title={usuario.status === 'LOCKED' ? 'Desbloquear' : 'Bloquear'}
                          >
                            {usuario.status === 'LOCKED' ? (
                              <Unlock size={18} />
                            ) : (
                              <Lock size={18} />
                            )}
                          </button>
                          <button
                            onClick={() => handleResetPassword(usuario)}
                            className="text-blue-600 hover:text-blue-700 transition-colors"
                            title="Restablecer contraseña"
                          >
                            <Key size={18} />
                          </button>
                          <button
                            onClick={() => handleDelete(usuario)}
                            className="text-red-600 hover:text-red-700 transition-colors"
                            title="Eliminar"
                          >
                            <Trash2 size={18} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>

      {/* Summary */}
      <div className="text-sm text-text-secondary">
        Mostrando {filteredUsuarios.length} de {usuarios.length} usuarios
      </div>

      {/* Create Modal */}
      {isCreateModalOpen && (
        <CreateUsuarioModal
          isOpen={isCreateModalOpen}
          onClose={() => setIsCreateModalOpen(false)}
          onSuccess={handleCreateSuccess}
        />
      )}

      {/* Edit Modal */}
      {isEditModalOpen && selectedUsuario && (
        <EditUsuarioModal
          isOpen={isEditModalOpen}
          usuario={selectedUsuario}
          onClose={() => {
            setIsEditModalOpen(false);
            setSelectedUsuario(null);
          }}
          onSuccess={handleEditSuccess}
        />
      )}
    </div>
  );
};
