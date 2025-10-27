/**
 * Component: CreateDepartamentoModal
 *
 * Modal para crear nuevos departamentos
 */

import React, { useState } from 'react';
import { X } from 'lucide-react';
import { Button } from '@shared-ui/components/Button';
import { toast } from 'sonner';
import { departamentoApi } from '../api/departamento.api';
import type { CreateDepartamentoRequest, Departamento } from '../types/departamento.types';

interface CreateDepartamentoModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  departamentos: Departamento[];
}

export const CreateDepartamentoModal: React.FC<CreateDepartamentoModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  departamentos,
}) => {
  const [formData, setFormData] = useState<CreateDepartamentoRequest>({
    unidadNegocioId: '00000000-0000-0000-0000-000000000001',
    codigo: '',
    nombre: '',
    descripcion: '',
    parentId: undefined,
    emailDepartamento: '',
    telefonoDepartamento: '',
    ubicacion: '',
    presupuestoAnual: undefined,
    numeroEmpleados: 0,
    activo: true,
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      setIsSubmitting(true);
      await departamentoApi.createDepartamento(formData);
      toast.success('Departamento creado exitosamente');
      onSuccess();
    } catch (error: any) {
      console.error('Error creando departamento:', error);
      const message = error.response?.data?.message || 'Error al crear el departamento';
      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-3xl max-h-[90vh] overflow-y-auto mx-4">
        <div className="flex items-center justify-between p-6 border-b">
          <h2 className="text-2xl font-bold text-text-primary">Crear Departamento</h2>
          <button
            onClick={onClose}
            className="text-text-secondary hover:text-text-primary transition-colors"
          >
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">
                Código *
              </label>
              <input
                type="text"
                required
                value={formData.codigo}
                onChange={(e) => setFormData({ ...formData, codigo: e.target.value.toUpperCase() })}
                placeholder="ADM-001"
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">
                Nombre *
              </label>
              <input
                type="text"
                required
                value={formData.nombre}
                onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-text-primary mb-1">
                Descripción
              </label>
              <textarea
                value={formData.descripcion}
                onChange={(e) => setFormData({ ...formData, descripcion: e.target.value })}
                rows={2}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-text-primary mb-1">
                Departamento Padre (opcional)
              </label>
              <select
                value={formData.parentId || ''}
                onChange={(e) => setFormData({ ...formData, parentId: e.target.value || undefined })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              >
                <option value="">Sin padre (Raíz)</option>
                {departamentos
                  .filter(d => d.nivel < 5)
                  .map((dep) => (
                    <option key={dep.id} value={dep.id}>
                      {'  '.repeat(dep.nivel)}
                      {dep.path}
                    </option>
                  ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">
                Email
              </label>
              <input
                type="email"
                value={formData.emailDepartamento}
                onChange={(e) => setFormData({ ...formData, emailDepartamento: e.target.value })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">
                Teléfono
              </label>
              <input
                type="tel"
                value={formData.telefonoDepartamento}
                onChange={(e) => setFormData({ ...formData, telefonoDepartamento: e.target.value })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">
                Ubicación
              </label>
              <input
                type="text"
                value={formData.ubicacion}
                onChange={(e) => setFormData({ ...formData, ubicacion: e.target.value })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">
                Presupuesto Anual
              </label>
              <input
                type="number"
                step="0.01"
                min="0"
                value={formData.presupuestoAnual || ''}
                onChange={(e) => setFormData({ ...formData, presupuestoAnual: e.target.value ? parseFloat(e.target.value) : undefined })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">
                Número de Empleados
              </label>
              <input
                type="number"
                min="0"
                value={formData.numeroEmpleados}
                onChange={(e) => setFormData({ ...formData, numeroEmpleados: parseInt(e.target.value) || 0 })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div className="md:col-span-2">
              <label className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={formData.activo}
                  onChange={(e) => setFormData({ ...formData, activo: e.target.checked })}
                  className="rounded border-gray-300 text-primary focus:ring-primary"
                />
                <span className="text-sm text-text-primary">Departamento activo</span>
              </label>
            </div>
          </div>

          <div className="flex justify-end gap-3 pt-4 border-t">
            <Button variant="outline" onClick={onClose} type="button" disabled={isSubmitting}>
              Cancelar
            </Button>
            <Button variant="primary" type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Creando...' : 'Crear Departamento'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
