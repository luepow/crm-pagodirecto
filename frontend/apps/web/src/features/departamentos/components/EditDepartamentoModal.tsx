/**
 * Component: EditDepartamentoModal
 *
 * Modal para editar departamentos existentes
 */

import React, { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import { Button } from '@shared-ui/components/Button';
import { toast } from 'sonner';
import { departamentoApi } from '../api/departamento.api';
import type { Departamento, UpdateDepartamentoRequest } from '../types/departamento.types';

interface EditDepartamentoModalProps {
  isOpen: boolean;
  departamento: Departamento;
  departamentos: Departamento[];
  onClose: () => void;
  onSuccess: () => void;
}

export const EditDepartamentoModal: React.FC<EditDepartamentoModalProps> = ({
  isOpen,
  departamento,
  departamentos,
  onClose,
  onSuccess,
}) => {
  const [formData, setFormData] = useState<UpdateDepartamentoRequest>({
    nombre: departamento.nombre,
    descripcion: departamento.descripcion || '',
    parentId: departamento.parentId,
    emailDepartamento: departamento.emailDepartamento || '',
    telefonoDepartamento: departamento.telefonoDepartamento || '',
    ubicacion: departamento.ubicacion || '',
    presupuestoAnual: departamento.presupuestoAnual,
    numeroEmpleados: departamento.numeroEmpleados,
    activo: departamento.activo,
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    setFormData({
      nombre: departamento.nombre,
      descripcion: departamento.descripcion || '',
      parentId: departamento.parentId,
      emailDepartamento: departamento.emailDepartamento || '',
      telefonoDepartamento: departamento.telefonoDepartamento || '',
      ubicacion: departamento.ubicacion || '',
      presupuestoAnual: departamento.presupuestoAnual,
      numeroEmpleados: departamento.numeroEmpleados,
      activo: departamento.activo,
    });
  }, [departamento]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      setIsSubmitting(true);
      await departamentoApi.updateDepartamento(departamento.id, formData);
      toast.success('Departamento actualizado exitosamente');
      onSuccess();
    } catch (error: any) {
      console.error('Error actualizando departamento:', error);
      const message = error.response?.data?.message || 'Error al actualizar el departamento';
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
          <h2 className="text-2xl font-bold text-text-primary">Editar Departamento</h2>
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
              <span className="font-semibold">Código:</span> {departamento.codigo}
            </p>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="md:col-span-2">
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
                  .filter(d => d.id !== departamento.id && d.nivel < 5)
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
              {isSubmitting ? 'Guardando...' : 'Guardar Cambios'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
