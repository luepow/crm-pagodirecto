/**
 * Component: EditUsuarioModal
 *
 * Modal para editar usuarios existentes
 */

import React, { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import { Button } from '@shared-ui/components/Button';
import { toast } from 'sonner';
import { usuarioApi } from '../api/usuario.api';
import type { Usuario, UpdateUsuarioRequest, UsuarioStatus } from '../types/usuario.types';

interface EditUsuarioModalProps {
  isOpen: boolean;
  usuario: Usuario;
  onClose: () => void;
  onSuccess: () => void;
}

export const EditUsuarioModal: React.FC<EditUsuarioModalProps> = ({
  isOpen,
  usuario,
  onClose,
  onSuccess,
}) => {
  const [formData, setFormData] = useState<UpdateUsuarioRequest>({
    email: usuario.email,
    nombreCompleto: usuario.nombreCompleto || '',
    telefono: usuario.telefono || '',
    cargo: usuario.cargo || '',
    departamento: usuario.departamento || '',
    photoUrl: usuario.photoUrl || '',
    status: usuario.status,
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    setFormData({
      email: usuario.email,
      nombreCompleto: usuario.nombreCompleto || '',
      telefono: usuario.telefono || '',
      cargo: usuario.cargo || '',
      departamento: usuario.departamento || '',
      photoUrl: usuario.photoUrl || '',
      status: usuario.status,
    });
  }, [usuario]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      setIsSubmitting(true);
      await usuarioApi.updateUsuario(usuario.id, formData);
      toast.success('Usuario actualizado exitosamente');
      onSuccess();
    } catch (error) {
      console.error('Error actualizando usuario:', error);
      toast.error('Error al actualizar el usuario');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto mx-4">
        <div className="flex items-center justify-between p-6 border-b">
          <h2 className="text-2xl font-bold text-text-primary">Editar Usuario</h2>
          <button
            onClick={onClose}
            className="text-text-secondary hover:text-text-primary transition-colors"
          >
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div className="bg-gray-50 p-4 rounded-xl mb-4">
            <p className="text-sm text-text-secondary">
              <span className="font-semibold">Username:</span> {usuario.username}
            </p>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-text-primary mb-1">
                Email *
              </label>
              <input
                type="email"
                required
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-text-primary mb-1">
                Nombre Completo
              </label>
              <input
                type="text"
                value={formData.nombreCompleto}
                onChange={(e) => setFormData({ ...formData, nombreCompleto: e.target.value })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">
                Teléfono
              </label>
              <input
                type="tel"
                value={formData.telefono}
                onChange={(e) => setFormData({ ...formData, telefono: e.target.value })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">
                Cargo
              </label>
              <input
                type="text"
                value={formData.cargo}
                onChange={(e) => setFormData({ ...formData, cargo: e.target.value })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">
                Departamento
              </label>
              <input
                type="text"
                value={formData.departamento}
                onChange={(e) => setFormData({ ...formData, departamento: e.target.value })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">
                URL de Foto
              </label>
              <input
                type="url"
                value={formData.photoUrl}
                onChange={(e) => setFormData({ ...formData, photoUrl: e.target.value })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-text-primary mb-1">
                Estado
              </label>
              <select
                value={formData.status}
                onChange={(e) => setFormData({ ...formData, status: e.target.value as UsuarioStatus })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              >
                <option value="ACTIVE">Activo</option>
                <option value="INACTIVE">Inactivo</option>
                <option value="LOCKED">Bloqueado</option>
                <option value="SUSPENDED">Suspendido</option>
              </select>
            </div>
          </div>

          <div className="flex justify-end gap-3 pt-4 border-t">
            <Button variant="outline" onClick={onClose} type="button" disabled={isSubmitting}>
              Cancelar
            </Button>
            <Button variant="primary" type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Guardando...' : 'Guardar Cambios'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
