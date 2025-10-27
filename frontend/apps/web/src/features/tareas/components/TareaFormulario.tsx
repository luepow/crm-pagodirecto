/**
 * Component: TareaFormulario
 *
 * Formulario para crear/editar tareas.
 */

import { useState } from 'react';
import type { Tarea, TareaFormData, TipoTarea, PrioridadTarea, StatusTarea } from '../types/tarea.types';

interface TareaFormularioProps {
  tarea?: Tarea;
  unidadNegocioId: string;
  onSubmit: (data: TareaFormData) => Promise<void>;
  onCancel: () => void;
  isSubmitting?: boolean;
}

export function TareaFormulario({
  tarea,
  unidadNegocioId,
  onSubmit,
  onCancel,
  isSubmitting = false,
}: TareaFormularioProps) {
  const [formData, setFormData] = useState<TareaFormData>({
    unidadNegocioId,
    titulo: tarea?.titulo || '',
    descripcion: tarea?.descripcion || '',
    tipo: (tarea?.tipo as TipoTarea) || 'SEGUIMIENTO',
    prioridad: (tarea?.prioridad as PrioridadTarea) || 'MEDIA',
    status: (tarea?.status as StatusTarea) || 'PENDIENTE',
    fechaVencimiento: tarea?.fechaVencimiento || '',
    asignadoA: tarea?.asignadoA || '00000000-0000-0000-0000-000000000001', // TODO: Del contexto
    relacionadoTipo: tarea?.relacionadoTipo || '',
    relacionadoId: tarea?.relacionadoId || '',
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors[name];
        return newErrors;
      });
    }
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.titulo.trim()) {
      newErrors.titulo = 'El título es obligatorio';
    }

    if (!formData.tipo) {
      newErrors.tipo = 'El tipo de tarea es obligatorio';
    }

    if (!formData.asignadoA) {
      newErrors.asignadoA = 'Debe asignar la tarea a un usuario';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    try {
      await onSubmit(formData);
    } catch (error) {
      console.error('Error al guardar tarea:', error);
    }
  };

  const getPrioridadColor = (prioridad: string) => {
    switch (prioridad) {
      case 'URGENTE':
        return 'border-red-500';
      case 'ALTA':
        return 'border-orange-500';
      case 'MEDIA':
        return 'border-yellow-500';
      case 'BAJA':
        return 'border-green-500';
      default:
        return 'border-gray-300';
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        {/* Título */}
        <div className="sm:col-span-2">
          <label htmlFor="titulo" className="block text-sm font-medium text-gray-700 mb-1">
            Título de la Tarea <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            id="titulo"
            name="titulo"
            value={formData.titulo}
            onChange={handleChange}
            placeholder="Ej: Llamar a cliente para seguimiento"
            className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
              errors.titulo ? 'border-red-500' : 'border-gray-300'
            }`}
            required
          />
          {errors.titulo && <p className="mt-1 text-sm text-red-600">{errors.titulo}</p>}
        </div>

        {/* Tipo */}
        <div>
          <label htmlFor="tipo" className="block text-sm font-medium text-gray-700 mb-1">
            Tipo de Tarea <span className="text-red-500">*</span>
          </label>
          <select
            id="tipo"
            name="tipo"
            value={formData.tipo}
            onChange={handleChange}
            className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
              errors.tipo ? 'border-red-500' : 'border-gray-300'
            }`}
            required
          >
            <option value="LLAMADA">Llamada</option>
            <option value="EMAIL">Email</option>
            <option value="REUNION">Reunión</option>
            <option value="SEGUIMIENTO">Seguimiento</option>
            <option value="ADMINISTRATIVA">Administrativa</option>
            <option value="TECNICA">Técnica</option>
            <option value="OTRA">Otra</option>
          </select>
          {errors.tipo && <p className="mt-1 text-sm text-red-600">{errors.tipo}</p>}
        </div>

        {/* Prioridad */}
        <div>
          <label htmlFor="prioridad" className="block text-sm font-medium text-gray-700 mb-1">
            Prioridad <span className="text-red-500">*</span>
          </label>
          <select
            id="prioridad"
            name="prioridad"
            value={formData.prioridad}
            onChange={handleChange}
            className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
              getPrioridadColor(formData.prioridad)
            }`}
            required
          >
            <option value="BAJA">Baja</option>
            <option value="MEDIA">Media</option>
            <option value="ALTA">Alta</option>
            <option value="URGENTE">Urgente</option>
          </select>
        </div>

        {/* Status */}
        <div>
          <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-1">
            Estado <span className="text-red-500">*</span>
          </label>
          <select
            id="status"
            name="status"
            value={formData.status}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          >
            <option value="PENDIENTE">Pendiente</option>
            <option value="EN_PROGRESO">En Progreso</option>
            <option value="COMPLETADA">Completada</option>
            <option value="CANCELADA">Cancelada</option>
            <option value="BLOQUEADA">Bloqueada</option>
          </select>
        </div>

        {/* Fecha Vencimiento */}
        <div>
          <label htmlFor="fechaVencimiento" className="block text-sm font-medium text-gray-700 mb-1">
            Fecha de Vencimiento
          </label>
          <input
            type="date"
            id="fechaVencimiento"
            name="fechaVencimiento"
            value={formData.fechaVencimiento}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* Asignado A (temporal - en producción sería un selector de usuarios) */}
        <div className="sm:col-span-2">
          <label htmlFor="asignadoA" className="block text-sm font-medium text-gray-700 mb-1">
            Asignado a <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            id="asignadoA"
            name="asignadoA"
            value={formData.asignadoA}
            onChange={handleChange}
            placeholder="UUID del usuario"
            className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
              errors.asignadoA ? 'border-red-500' : 'border-gray-300'
            }`}
            required
          />
          {errors.asignadoA && <p className="mt-1 text-sm text-red-600">{errors.asignadoA}</p>}
          <p className="mt-1 text-xs text-gray-500">
            Nota: En producción será un selector de usuarios
          </p>
        </div>

        {/* Relacionado Tipo */}
        <div>
          <label htmlFor="relacionadoTipo" className="block text-sm font-medium text-gray-700 mb-1">
            Relacionado con
          </label>
          <select
            id="relacionadoTipo"
            name="relacionadoTipo"
            value={formData.relacionadoTipo}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">Ninguno</option>
            <option value="CLIENTE">Cliente</option>
            <option value="OPORTUNIDAD">Oportunidad</option>
            <option value="VENTA">Venta</option>
            <option value="PRODUCTO">Producto</option>
          </select>
        </div>

        {/* Relacionado ID */}
        {formData.relacionadoTipo && (
          <div>
            <label htmlFor="relacionadoId" className="block text-sm font-medium text-gray-700 mb-1">
              ID del {formData.relacionadoTipo}
            </label>
            <input
              type="text"
              id="relacionadoId"
              name="relacionadoId"
              value={formData.relacionadoId}
              onChange={handleChange}
              placeholder={`UUID del ${formData.relacionadoTipo.toLowerCase()}`}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        )}

        {/* Descripción */}
        <div className="sm:col-span-2">
          <label htmlFor="descripcion" className="block text-sm font-medium text-gray-700 mb-1">
            Descripción
          </label>
          <textarea
            id="descripcion"
            name="descripcion"
            value={formData.descripcion}
            onChange={handleChange}
            rows={4}
            placeholder="Detalles de la tarea..."
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
          />
        </div>
      </div>

      {/* Botones */}
      <div className="flex justify-end gap-3 pt-4 border-t border-gray-200">
        <button
          type="button"
          onClick={onCancel}
          disabled={isSubmitting}
          className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50"
        >
          Cancelar
        </button>
        <button
          type="submit"
          disabled={isSubmitting}
          className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2"
        >
          {isSubmitting && (
            <svg className="animate-spin h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              />
            </svg>
          )}
          {isSubmitting ? 'Guardando...' : tarea ? 'Actualizar' : 'Crear Tarea'}
        </button>
      </div>
    </form>
  );
}
