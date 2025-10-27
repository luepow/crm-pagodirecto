/**
 * Page: ProfilePage
 *
 * Página de perfil de usuario con edición de información personal,
 * cambio de contraseña y configuración de MFA
 */

import React, { useState, useEffect } from 'react';
import { User, Lock, Shield, Mail, Phone, Briefcase, Building2, Camera } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@shared-ui/components/Card';
import { Button } from '@shared-ui/components/Button';
import { Badge } from '@shared-ui/components/Badge';
import { toast } from 'sonner';
import { profileApi } from '../../features/profile/api/profile.api';
import type { Profile, UpdateProfileRequest, ChangePasswordRequest } from '../../features/profile/types/profile.types';

/**
 * ProfilePage Component
 */
export const ProfilePage: React.FC = () => {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'info' | 'security'>('info');

  // Form states
  const [formData, setFormData] = useState<UpdateProfileRequest>({
    email: '',
    nombreCompleto: '',
    telefono: '',
    cargo: '',
    departamento: '',
    photoUrl: '',
  });

  const [passwordData, setPasswordData] = useState<ChangePasswordRequest>({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const [isSaving, setIsSaving] = useState(false);

  // Load profile on mount
  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      setIsLoading(true);
      const data = await profileApi.getMyProfile();
      setProfile(data);
      setFormData({
        email: data.email,
        nombreCompleto: data.nombreCompleto || '',
        telefono: data.telefono || '',
        cargo: data.cargo || '',
        departamento: data.departamento || '',
        photoUrl: data.photoUrl || '',
      });
    } catch (error) {
      console.error('Error cargando perfil:', error);
      toast.error('Error al cargar el perfil');
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setIsSaving(true);
      const updatedProfile = await profileApi.updateMyProfile(formData);
      setProfile(updatedProfile);
      toast.success('Perfil actualizado exitosamente');
    } catch (error) {
      console.error('Error actualizando perfil:', error);
      toast.error('Error al actualizar el perfil');
    } finally {
      setIsSaving(false);
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();

    if (passwordData.newPassword !== passwordData.confirmPassword) {
      toast.error('Las contraseñas no coinciden');
      return;
    }

    if (passwordData.newPassword.length < 8) {
      toast.error('La contraseña debe tener al menos 8 caracteres');
      return;
    }

    try {
      setIsSaving(true);
      await profileApi.changePassword(passwordData);
      toast.success('Contraseña actualizada exitosamente');
      setPasswordData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
    } catch (error: any) {
      console.error('Error cambiando contraseña:', error);
      toast.error(error.response?.data?.message || 'Error al cambiar la contraseña');
    } finally {
      setIsSaving(false);
    }
  };

  const handleToggleMFA = async () => {
    try {
      if (profile?.mfaEnabled) {
        await profileApi.disableMFA();
        toast.success('MFA deshabilitado exitosamente');
      } else {
        const response = await profileApi.enableMFA();
        toast.success(`MFA habilitado. Secret: ${response.secret}`);
      }
      await loadProfile();
    } catch (error) {
      console.error('Error gestionando MFA:', error);
      toast.error('Error al gestionar MFA');
    }
  };

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
          <p className="mt-4 text-text-secondary">Cargando perfil...</p>
        </div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <p className="text-text-secondary">No se pudo cargar el perfil</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-text-primary">Mi Perfil</h1>
          <p className="mt-1 text-text-secondary">
            Gestiona tu información personal y configuración de seguridad
          </p>
        </div>
      </div>

      {/* Profile header card */}
      <Card>
        <CardContent padding="lg">
          <div className="flex items-start gap-6">
            {/* Avatar */}
            <div className="relative">
              <div className="flex h-24 w-24 items-center justify-center rounded-full bg-gradient-to-br from-primary to-primary-600 text-white">
                {profile.photoUrl ? (
                  <img
                    src={profile.photoUrl}
                    alt={profile.nombreCompleto || profile.username}
                    className="h-24 w-24 rounded-full object-cover"
                  />
                ) : (
                  <User size={48} />
                )}
              </div>
              <button className="absolute bottom-0 right-0 flex h-8 w-8 items-center justify-center rounded-full bg-white shadow-md hover:shadow-lg transition-shadow">
                <Camera size={16} className="text-gray-600" />
              </button>
            </div>

            {/* User info */}
            <div className="flex-1">
              <h2 className="text-2xl font-bold text-text-primary">
                {profile.nombreCompleto || profile.username}
              </h2>
              <p className="mt-1 text-text-secondary">{profile.email}</p>
              <div className="mt-3 flex flex-wrap gap-2">
                <Badge variant={profile.status === 'ACTIVE' ? 'success' : 'error'}>
                  {profile.status}
                </Badge>
                {profile.mfaEnabled && (
                  <Badge variant="info">
                    <Shield size={14} className="mr-1" />
                    MFA Habilitado
                  </Badge>
                )}
                {profile.roles.map((role) => (
                  <Badge key={role.id} variant="primary">
                    {role.nombre}
                  </Badge>
                ))}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Tabs */}
      <div className="flex gap-2 border-b border-gray-200">
        <button
          onClick={() => setActiveTab('info')}
          className={`px-4 py-2 font-medium transition-colors ${
            activeTab === 'info'
              ? 'border-b-2 border-primary text-primary'
              : 'text-text-secondary hover:text-text-primary'
          }`}
        >
          <User size={18} className="inline-block mr-2" />
          Información Personal
        </button>
        <button
          onClick={() => setActiveTab('security')}
          className={`px-4 py-2 font-medium transition-colors ${
            activeTab === 'security'
              ? 'border-b-2 border-primary text-primary'
              : 'text-text-secondary hover:text-text-primary'
          }`}
        >
          <Lock size={18} className="inline-block mr-2" />
          Seguridad
        </button>
      </div>

      {/* Tab content */}
      {activeTab === 'info' && (
        <Card>
          <CardHeader>
            <CardTitle>Información Personal</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleUpdateProfile} className="space-y-4">
              <div className="grid gap-4 md:grid-cols-2">
                <div>
                  <label className="block text-sm font-medium text-text-primary mb-1">
                    <Mail size={16} className="inline-block mr-1" />
                    Correo Electrónico
                  </label>
                  <input
                    type="email"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-text-primary mb-1">
                    <User size={16} className="inline-block mr-1" />
                    Nombre Completo
                  </label>
                  <input
                    type="text"
                    value={formData.nombreCompleto}
                    onChange={(e) => setFormData({ ...formData, nombreCompleto: e.target.value })}
                    className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-text-primary mb-1">
                    <Phone size={16} className="inline-block mr-1" />
                    Teléfono
                  </label>
                  <input
                    type="tel"
                    value={formData.telefono}
                    onChange={(e) => setFormData({ ...formData, telefono: e.target.value })}
                    className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-text-primary mb-1">
                    <Briefcase size={16} className="inline-block mr-1" />
                    Cargo
                  </label>
                  <input
                    type="text"
                    value={formData.cargo}
                    onChange={(e) => setFormData({ ...formData, cargo: e.target.value })}
                    className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-text-primary mb-1">
                    <Building2 size={16} className="inline-block mr-1" />
                    Departamento
                  </label>
                  <input
                    type="text"
                    value={formData.departamento}
                    onChange={(e) => setFormData({ ...formData, departamento: e.target.value })}
                    className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                  />
                </div>
              </div>

              <div className="flex justify-end gap-2">
                <Button type="button" variant="outline" onClick={loadProfile}>
                  Cancelar
                </Button>
                <Button type="submit" variant="primary" disabled={isSaving}>
                  {isSaving ? 'Guardando...' : 'Guardar Cambios'}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      {activeTab === 'security' && (
        <div className="space-y-6">
          {/* Change password */}
          <Card>
            <CardHeader>
              <CardTitle>
                <Lock size={20} className="inline-block mr-2" />
                Cambiar Contraseña
              </CardTitle>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleChangePassword} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-text-primary mb-1">
                    Contraseña Actual
                  </label>
                  <input
                    type="password"
                    value={passwordData.currentPassword}
                    onChange={(e) =>
                      setPasswordData({ ...passwordData, currentPassword: e.target.value })
                    }
                    className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-text-primary mb-1">
                    Nueva Contraseña
                  </label>
                  <input
                    type="password"
                    value={passwordData.newPassword}
                    onChange={(e) =>
                      setPasswordData({ ...passwordData, newPassword: e.target.value })
                    }
                    className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                    required
                    minLength={8}
                  />
                  <p className="mt-1 text-xs text-text-tertiary">
                    Mínimo 8 caracteres. Incluye mayúsculas, minúsculas, números y símbolos.
                  </p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-text-primary mb-1">
                    Confirmar Nueva Contraseña
                  </label>
                  <input
                    type="password"
                    value={passwordData.confirmPassword}
                    onChange={(e) =>
                      setPasswordData({ ...passwordData, confirmPassword: e.target.value })
                    }
                    className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                    required
                  />
                </div>

                <div className="flex justify-end">
                  <Button type="submit" variant="primary" disabled={isSaving}>
                    {isSaving ? 'Actualizando...' : 'Actualizar Contraseña'}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>

          {/* MFA settings */}
          <Card>
            <CardHeader>
              <CardTitle>
                <Shield size={20} className="inline-block mr-2" />
                Autenticación Multi-Factor (MFA)
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <p className="text-text-secondary">
                  La autenticación multi-factor añade una capa adicional de seguridad a tu cuenta.
                  Cuando está habilitada, necesitarás ingresar un código de tu aplicación
                  autenticadora además de tu contraseña.
                </p>

                <div className="flex items-center justify-between rounded-xl border border-gray-200 p-4">
                  <div className="flex items-center gap-3">
                    <Shield
                      size={24}
                      className={profile.mfaEnabled ? 'text-success' : 'text-gray-400'}
                    />
                    <div>
                      <p className="font-medium text-text-primary">
                        MFA {profile.mfaEnabled ? 'Habilitado' : 'Deshabilitado'}
                      </p>
                      <p className="text-sm text-text-secondary">
                        {profile.mfaEnabled
                          ? 'Tu cuenta está protegida con MFA'
                          : 'Habilita MFA para mayor seguridad'}
                      </p>
                    </div>
                  </div>

                  <Button
                    variant={profile.mfaEnabled ? 'outline' : 'primary'}
                    onClick={handleToggleMFA}
                  >
                    {profile.mfaEnabled ? 'Deshabilitar' : 'Habilitar'}
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
};
