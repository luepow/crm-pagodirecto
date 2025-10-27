/**
 * Component: OportunidadFormulario
 *
 * Formulario para crear/editar oportunidades de venta.
 */

import { useState } from 'react';
import type { Oportunidad, OportunidadFormData } from '../types/oportunidad.types';

interface OportunidadFormularioProps {
  oportunidad?: Oportunidad;
  unidadNegocioId: string;
  onSubmit: (data: OportunidadFormData) => Promise<void>;
  onCancel: () => void;
  isSubmitting?: boolean;
}

export function OportunidadFormulario({
  oportunidad,
  unidadNegocioId,
  onSubmit,
  onCancel,
  isSubmitting = false,
}: OportunidadFormularioProps) {
  const [formData, setFormData] = useState<OportunidadFormData>({
    unidadNegocioId,
    clienteId: oportunidad?.clienteId || '',
    titulo: oportunidad?.titulo || '',
    descripcion: oportunidad?.descripcion || '',
    valorEstimado: oportunidad?.valorEstimado || 0,
    moneda: oportunidad?.moneda || 'USD',
    probabilidad: oportunidad?.probabilidad || 50,
    etapaId: oportunidad?.etapaId || '00000000-0000-0000-0000-000000000001', // TODO: Etapa inicial
    fechaCierreEstimada: oportunidad?.fechaCierreEstimada || '',
    propietarioId: oportunidad?.propietarioId || '00000000-0000-0000-0000-000000000001', // TODO: Del contexto
    fuente: oportunidad?.fuente || '',
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

    if (!formData.clienteId) {
      newErrors.clienteId = 'El cliente es obligatorio';
    }

    if (formData.valorEstimado <= 0) {
      newErrors.valorEstimado = 'El valor debe ser mayor a 0';
    }

    if (formData.probabilidad < 0 || formData.probabilidad > 100) {
      newErrors.probabilidad = 'La probabilidad debe estar entre 0 y 100';
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
      console.error('Error al guardar oportunidad:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        {/* Título */}
        <div className="sm:col-span-2">
          <label htmlFor="titulo" className="block text-sm font-medium text-gray-700 mb-1">
            Título de la Oportunidad <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            id="titulo"
            name="titulo"
            value={formData.titulo}
            onChange={handleChange}
            placeholder="Ej: Venta de Software CRM"
            className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
              errors.titulo ? 'border-red-500' : 'border-gray-300'
            }`}
            required
          />
          {errors.titulo && <p className="mt-1 text-sm text-red-600">{errors.titulo}</p>}
        </div>

        {/* Cliente ID (temporal - en producción sería un selector) */}
        <div>
          <label htmlFor="clienteId" className="block text-sm font-medium text-gray-700 mb-1">
            Cliente ID <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            id="clienteId"
            name="clienteId"
            value={formData.clienteId}
            onChange={handleChange}
            placeholder="UUID del cliente"
            className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
              errors.clienteId ? 'border-red-500' : 'border-gray-300'
            }`}
            required
          />
          {errors.clienteId && <p className="mt-1 text-sm text-red-600">{errors.clienteId}</p>}
        </div>

        {/* Fecha Cierre Estimada */}
        <div>
          <label htmlFor="fechaCierreEstimada" className="block text-sm font-medium text-gray-700 mb-1">
            Fecha de Cierre Estimada
          </label>
          <input
            type="date"
            id="fechaCierreEstimada"
            name="fechaCierreEstimada"
            value={formData.fechaCierreEstimada}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* Valor Estimado */}
        <div>
          <label htmlFor="valorEstimado" className="block text-sm font-medium text-gray-700 mb-1">
            Valor Estimado <span className="text-red-500">*</span>
          </label>
          <input
            type="number"
            id="valorEstimado"
            name="valorEstimado"
            value={formData.valorEstimado}
            onChange={handleChange}
            step="0.01"
            min="0"
            placeholder="0.00"
            className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
              errors.valorEstimado ? 'border-red-500' : 'border-gray-300'
            }`}
            required
          />
          {errors.valorEstimado && <p className="mt-1 text-sm text-red-600">{errors.valorEstimado}</p>}
        </div>

        {/* Moneda */}
        <div>
          <label htmlFor="moneda" className="block text-sm font-medium text-gray-700 mb-1">
            Moneda <span className="text-red-500">*</span>
          </label>
          <select
            id="moneda"
            name="moneda"
            value={formData.moneda}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          >
            <option value="USD">USD - Dólar</option>
            <option value="VES">VES - Bolívar</option>
            <option value="EUR">EUR - Euro</option>
          </select>
        </div>

        {/* Probabilidad */}
        <div>
          <label htmlFor="probabilidad" className="block text-sm font-medium text-gray-700 mb-1">
            Probabilidad de Cierre (%) <span className="text-red-500">*</span>
          </label>
          <input
            type="number"
            id="probabilidad"
            name="probabilidad"
            value={formData.probabilidad}
            onChange={handleChange}
            min="0"
            max="100"
            className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
              errors.probabilidad ? 'border-red-500' : 'border-gray-300'
            }`}
            required
          />
          {errors.probabilidad && <p className="mt-1 text-sm text-red-600">{errors.probabilidad}</p>}
        </div>

        {/* Fuente */}
        <div>
          <label htmlFor="fuente" className="block text-sm font-medium text-gray-700 mb-1">
            Fuente
          </label>
          <select
            id="fuente"
            name="fuente"
            value={formData.fuente}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">Seleccionar...</option>
            <option value="REFERIDO">Referido</option>
            <option value="WEB">Sitio Web</option>
            <option value="REDES_SOCIALES">Redes Sociales</option>
            <option value="LLAMADA_FRIA">Llamada en Frío</option>
            <option value="EVENTO">Evento</option>
            <option value="OTRO">Otro</option>
          </select>
        </div>

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
            placeholder="Detalles de la oportunidad..."
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
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
            </svg>
          )}
          {isSubmitting ? 'Guardando...' : oportunidad ? 'Actualizar' : 'Crear Oportunidad'}
        </button>
      </div>
    </form>
  );
}
