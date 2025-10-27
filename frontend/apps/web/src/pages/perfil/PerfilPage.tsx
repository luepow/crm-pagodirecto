/**
 * Perfil Page - User profile and settings
 */

import React from 'react';
import { User, Mail, Phone, Briefcase, Key, Upload, Save } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@shared-ui/components/Card';
import { Button } from '@shared-ui/components/Button';
import { Badge } from '@shared-ui/components/Badge';
import { usuariosApi } from '../../features/usuarios';
import type { PerfilUsuario, CambiarPasswordData } from '../../features/usuarios';
import { toast } from 'sonner';

export const PerfilPage: React.FC = () => {
  const [perfil, setPerfil] = React.useState<PerfilUsuario | null>(null);
  const [isLoading, setIsLoading] = React.useState(true);
  const [isEditing, setIsEditing] = React.useState(false);
  const [editForm, setEditForm] = React.useState<Partial<PerfilUsuario>>({});
  const [showPasswordModal, setShowPasswordModal] = React.useState(false);
  const [passwordForm, setPasswordForm] = React.useState<CambiarPasswordData>({
    passwordActual: '',
    passwordNueva: '',
    passwordNuevaConfirm: '',
  });

  React.useEffect(() => {
    loadPerfil();
  }, []);

  const loadPerfil = async () => {
    try {
      setIsLoading(true);
      const data = await usuariosApi.getPerfil();
      setPerfil(data);
      setEditForm(data);
    } catch (error) {
      console.error('Error cargando perfil:', error);
      toast.error('Error al cargar el perfil');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSaveProfile = async () => {
    try {
      await usuariosApi.updatePerfil(editForm);
      toast.success('Perfil actualizado exitosamente');
      setIsEditing(false);
      await loadPerfil();
    } catch (error) {
      toast.error('Error al actualizar el perfil');
    }
  };

  const handleChangePassword = async () => {
    if (passwordForm.passwordNueva !== passwordForm.passwordNuevaConfirm) {
      toast.error('Las contraseñas no coinciden');
      return;
    }
    try {
      await usuariosApi.cambiarPassword(passwordForm);
      toast.success('Contraseña cambiada exitosamente');
      setShowPasswordModal(false);
      setPasswordForm({ passwordActual: '', passwordNueva: '', passwordNuevaConfirm: '' });
    } catch (error) {
      toast.error('Error al cambiar la contraseña');
    }
  };

  const handleAvatarUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      toast.success('Avatar actualizado');
      await loadPerfil();
    } catch (error) {
      toast.error('Error al subir el avatar');
    }
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('es-VE', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(date);
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin h-12 w-12 border-4 border-blue-500 border-t-transparent rounded-full" />
      </div>
    );
  }

  if (!perfil) {
    return <div className="text-center py-12">No se pudo cargar el perfil</div>;
  }

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <div>
        <h1 className="text-3xl font-bold text-text-primary">Mi Perfil</h1>
        <p className="mt-1 text-text-secondary">Gestiona tu información personal y configuración</p>
      </div>

      {/* Avatar and Basic Info */}
      <Card>
        <CardContent padding="lg">
          <div className="flex items-start gap-6">
            <div className="relative">
              <div className="w-32 h-32 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-white text-4xl font-bold">
                {(perfil.nombre?.[0] || perfil.username?.[0] || '?').toUpperCase()}
              </div>
              <label className="absolute bottom-0 right-0 bg-white rounded-full p-2 shadow-lg cursor-pointer hover:bg-gray-50">
                <Upload size={20} className="text-gray-600" />
                <input type="file" accept="image/*" onChange={handleAvatarUpload} className="hidden" />
              </label>
            </div>
            <div className="flex-1">
              <h2 className="text-2xl font-bold text-gray-900">
                {perfil.nombre} {perfil.apellido}
              </h2>
              <p className="text-gray-600 mt-1">@{perfil.username}</p>
              <div className="mt-4 flex flex-wrap gap-2">
                {perfil.roles?.map((role) => (
                  <Badge key={role.id} variant="info">
                    {role.nombre}
                  </Badge>
                ))}
              </div>
              <p className="mt-4 text-sm text-gray-600">
                <strong>Último acceso:</strong> {formatDate(perfil.ultimoAcceso)}
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Contact Information */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Información de Contacto</CardTitle>
            {!isEditing ? (
              <Button variant="outline" size="sm" onClick={() => setIsEditing(true)}>
                Editar
              </Button>
            ) : (
              <div className="flex gap-2">
                <Button variant="outline" size="sm" onClick={() => setIsEditing(false)}>
                  Cancelar
                </Button>
                <Button variant="primary" size="sm" leftIcon={<Save size={16} />} onClick={handleSaveProfile}>
                  Guardar
                </Button>
              </div>
            )}
          </div>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="flex items-center gap-2 text-sm font-medium text-gray-700 mb-2">
                <User size={18} /> Nombre
              </label>
              {isEditing ? (
                <input
                  type="text"
                  value={editForm.nombre || ''}
                  onChange={(e) => setEditForm({ ...editForm, nombre: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                />
              ) : (
                <p className="text-gray-900">{perfil.nombre || '-'}</p>
              )}
            </div>

            <div>
              <label className="flex items-center gap-2 text-sm font-medium text-gray-700 mb-2">
                <User size={18} /> Apellido
              </label>
              {isEditing ? (
                <input
                  type="text"
                  value={editForm.apellido || ''}
                  onChange={(e) => setEditForm({ ...editForm, apellido: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                />
              ) : (
                <p className="text-gray-900">{perfil.apellido || '-'}</p>
              )}
            </div>

            <div>
              <label className="flex items-center gap-2 text-sm font-medium text-gray-700 mb-2">
                <Mail size={18} /> Email
              </label>
              <p className="text-gray-900">{perfil.email}</p>
            </div>

            <div>
              <label className="flex items-center gap-2 text-sm font-medium text-gray-700 mb-2">
                <Phone size={18} /> Teléfono
              </label>
              {isEditing ? (
                <input
                  type="tel"
                  value={editForm.telefono || ''}
                  onChange={(e) => setEditForm({ ...editForm, telefono: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                />
              ) : (
                <p className="text-gray-900">{perfil.telefono || '-'}</p>
              )}
            </div>

            <div>
              <label className="flex items-center gap-2 text-sm font-medium text-gray-700 mb-2">
                <Briefcase size={18} /> Departamento
              </label>
              <p className="text-gray-900">{perfil.departamentoNombre || '-'}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Security */}
      <Card>
        <CardHeader>
          <CardTitle>Seguridad</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-sm font-medium text-gray-900">Contraseña</h3>
              <p className="text-sm text-gray-600">Cambia tu contraseña regularmente para mantener tu cuenta segura</p>
            </div>
            <Button variant="outline" leftIcon={<Key size={18} />} onClick={() => setShowPasswordModal(true)}>
              Cambiar Contraseña
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Password Modal */}
      {showPasswordModal && (
        <div className="fixed inset-0 z-50 overflow-y-auto">
          <div className="fixed inset-0 bg-black bg-opacity-50" onClick={() => setShowPasswordModal(false)} />
          <div className="flex min-h-full items-center justify-center p-4">
            <div className="relative w-full max-w-md bg-white rounded-lg shadow-xl p-6">
              <h2 className="text-xl font-semibold mb-4">Cambiar Contraseña</h2>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Contraseña Actual</label>
                  <input
                    type="password"
                    value={passwordForm.passwordActual}
                    onChange={(e) => setPasswordForm({ ...passwordForm, passwordActual: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Nueva Contraseña</label>
                  <input
                    type="password"
                    value={passwordForm.passwordNueva}
                    onChange={(e) => setPasswordForm({ ...passwordForm, passwordNueva: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Confirmar Nueva Contraseña</label>
                  <input
                    type="password"
                    value={passwordForm.passwordNuevaConfirm}
                    onChange={(e) => setPasswordForm({ ...passwordForm, passwordNuevaConfirm: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                  />
                </div>
              </div>
              <div className="flex justify-end gap-3 mt-6">
                <Button variant="outline" onClick={() => setShowPasswordModal(false)}>
                  Cancelar
                </Button>
                <Button variant="primary" onClick={handleChangePassword}>
                  Cambiar Contraseña
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
