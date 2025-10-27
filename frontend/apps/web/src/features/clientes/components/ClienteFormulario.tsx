/**
 * Component: ClienteFormulario
 *
 * Formulario completo para crear/editar clientes empresariales.
 * Incluye validaciones para datos venezolanos (RIF, teléfonos).
 */

import { useState } from 'react';
import type { Cliente, ClienteFormData } from '../types/cliente.types';
import { ClienteTipo, ClienteStatus } from '../types/cliente.types';

interface ClienteFormularioProps {
  cliente?: Cliente;
  unidadNegocioId: string;
  onSubmit: (data: ClienteFormData) => Promise<void>;
  onCancel: () => void;
  isSubmitting?: boolean;
}

export function ClienteFormulario({
  cliente,
  unidadNegocioId,
  onSubmit,
  onCancel,
  isSubmitting = false,
}: ClienteFormularioProps) {
  const [formData, setFormData] = useState<ClienteFormData>({
    unidadNegocioId,
    codigo: cliente?.codigo || '',
    nombre: cliente?.nombre || '',
    email: cliente?.email || '',
    telefono: cliente?.telefono || '',
    tipo: cliente?.tipo || ClienteTipo.EMPRESA,
    rfc: cliente?.rfc || '',
    razonSocial: cliente?.razonSocial || '',
    status: cliente?.status || ClienteStatus.ACTIVE,
    segmento: cliente?.segmento || '',
    fuente: cliente?.fuente || '',
    propietarioId: cliente?.propietarioId,
    notas: cliente?.notas || '',
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    // Limpiar error del campo cuando el usuario empieza a escribir
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

    // Validaciones obligatorias
    if (!formData.nombre.trim()) {
      newErrors.nombre = 'El nombre es obligatorio';
    }

    // Validación de email
    if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Email inválido';
    }

    // Validación de RIF venezolano (formato: J-12345678-9)
    if (formData.rfc && !/^[JGVEP]-\d{8,9}-?\d?$/.test(formData.rfc)) {
      newErrors.rfc = 'RIF inválido. Formato: J-12345678-9';
    }

    // Validación de teléfono venezolano (formato: +58-xxx-xxxxxxx o 0xxx-xxxxxxx)
    if (
      formData.telefono &&
      !/^(\+58|0)(2[0-9]{2}|4[0-9]{2}|5[0-9]{2})-?\d{7}$/.test(formData.telefono.replace(/\s/g, ''))
    ) {
      newErrors.telefono = 'Teléfono inválido. Ej: 0414-1234567 o +58-414-1234567';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    try {
      await onSubmit(formData);
    } catch (error) {
      console.error('Error al guardar cliente:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* Información Básica */}
      <div>
        <h3 className="text-lg font-medium text-gray-900 mb-4">Información Básica</h3>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {/* Tipo */}
          <div>
            <label htmlFor="tipo" className="block text-sm font-medium text-gray-700 mb-1">
              Tipo de Cliente <span className="text-red-500">*</span>
            </label>
            <select
              id="tipo"
              name="tipo"
              value={formData.tipo}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              required
            >
              <option value="EMPRESA">Empresa</option>
              <option value="PERSONA">Persona Natural</option>
            </select>
          </div>

          {/* Status */}
          <div>
            <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-1">
              Status <span className="text-red-500">*</span>
            </label>
            <select
              id="status"
              name="status"
              value={formData.status}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              required
            >
              <option value="LEAD">Lead</option>
              <option value="PROSPECT">Prospecto</option>
              <option value="ACTIVE">Activo</option>
              <option value="INACTIVE">Inactivo</option>
            </select>
          </div>

          {/* Nombre */}
          <div className="sm:col-span-2">
            <label htmlFor="nombre" className="block text-sm font-medium text-gray-700 mb-1">
              Nombre {formData.tipo === 'EMPRESA' ? 'Comercial' : 'Completo'}{' '}
              <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              id="nombre"
              name="nombre"
              value={formData.nombre}
              onChange={handleChange}
              placeholder={
                formData.tipo === 'EMPRESA'
                  ? 'Ej: Distribuidora La Venezolana'
                  : 'Ej: Juan Pérez'
              }
              className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                errors.nombre ? 'border-red-500' : 'border-gray-300'
              }`}
              required
            />
            {errors.nombre && <p className="mt-1 text-sm text-red-600">{errors.nombre}</p>}
          </div>

          {/* Razón Social (solo para empresas) */}
          {formData.tipo === 'EMPRESA' && (
            <div className="sm:col-span-2">
              <label htmlFor="razonSocial" className="block text-sm font-medium text-gray-700 mb-1">
                Razón Social
              </label>
              <input
                type="text"
                id="razonSocial"
                name="razonSocial"
                value={formData.razonSocial}
                onChange={handleChange}
                placeholder="Ej: Distribuidora La Venezolana, C.A."
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
          )}

          {/* RIF */}
          <div>
            <label htmlFor="rfc" className="block text-sm font-medium text-gray-700 mb-1">
              RIF {formData.tipo === 'EMPRESA' ? '(J/G)' : '(V/E)'}
            </label>
            <input
              type="text"
              id="rfc"
              name="rfc"
              value={formData.rfc}
              onChange={handleChange}
              placeholder="Ej: J-12345678-9"
              className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                errors.rfc ? 'border-red-500' : 'border-gray-300'
              }`}
            />
            {errors.rfc && <p className="mt-1 text-sm text-red-600">{errors.rfc}</p>}
          </div>

          {/* Código (solo para edición) */}
          {cliente && (
            <div>
              <label htmlFor="codigo" className="block text-sm font-medium text-gray-700 mb-1">
                Código
              </label>
              <input
                type="text"
                id="codigo"
                name="codigo"
                value={formData.codigo}
                disabled
                className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-500"
              />
            </div>
          )}
        </div>
      </div>

      {/* Información de Contacto */}
      <div>
        <h3 className="text-lg font-medium text-gray-900 mb-4">Información de Contacto</h3>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {/* Email */}
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
              Email
            </label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="email@ejemplo.com"
              className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                errors.email ? 'border-red-500' : 'border-gray-300'
              }`}
            />
            {errors.email && <p className="mt-1 text-sm text-red-600">{errors.email}</p>}
          </div>

          {/* Teléfono */}
          <div>
            <label htmlFor="telefono" className="block text-sm font-medium text-gray-700 mb-1">
              Teléfono
            </label>
            <input
              type="tel"
              id="telefono"
              name="telefono"
              value={formData.telefono}
              onChange={handleChange}
              placeholder="0414-1234567"
              className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                errors.telefono ? 'border-red-500' : 'border-gray-300'
              }`}
            />
            {errors.telefono && <p className="mt-1 text-sm text-red-600">{errors.telefono}</p>}
          </div>
        </div>
      </div>

      {/* Información Comercial */}
      <div>
        <h3 className="text-lg font-medium text-gray-900 mb-4">Información Comercial</h3>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {/* Segmento */}
          <div>
            <label htmlFor="segmento" className="block text-sm font-medium text-gray-700 mb-1">
              Segmento
            </label>
            <select
              id="segmento"
              name="segmento"
              value={formData.segmento}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="">Seleccionar...</option>
              <option value="CORPORATIVO">Corporativo</option>
              <option value="PYME">PyME</option>
              <option value="GOBIERNO">Gobierno</option>
              <option value="DISTRIBUIDOR">Distribuidor</option>
              <option value="RETAIL">Retail</option>
              <option value="SERVICIOS">Servicios</option>
              <option value="MANUFACTURA">Manufactura</option>
              <option value="OTRO">Otro</option>
            </select>
          </div>

          {/* Fuente */}
          <div>
            <label htmlFor="fuente" className="block text-sm font-medium text-gray-700 mb-1">
              Fuente de Contacto
            </label>
            <select
              id="fuente"
              name="fuente"
              value={formData.fuente}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="">Seleccionar...</option>
              <option value="REFERIDO">Referido</option>
              <option value="WEB">Sitio Web</option>
              <option value="REDES_SOCIALES">Redes Sociales</option>
              <option value="LLAMADA_FRIA">Llamada en Frío</option>
              <option value="EVENTO">Evento</option>
              <option value="PUBLICIDAD">Publicidad</option>
              <option value="IMPORTACION_CSV">Importación CSV</option>
              <option value="OTRO">Otro</option>
            </select>
          </div>
        </div>
      </div>

      {/* Notas */}
      <div>
        <label htmlFor="notas" className="block text-sm font-medium text-gray-700 mb-1">
          Notas Adicionales
        </label>
        <textarea
          id="notas"
          name="notas"
          value={formData.notas}
          onChange={handleChange}
          rows={4}
          placeholder="Información adicional sobre el cliente..."
          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
        />
      </div>

      {/* Botones */}
      <div className="flex justify-end gap-3 pt-4 border-t border-gray-200">
        <button
          type="button"
          onClick={onCancel}
          disabled={isSubmitting}
          className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Cancelar
        </button>
        <button
          type="submit"
          disabled={isSubmitting}
          className="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
        >
          {isSubmitting && (
            <svg
              className="animate-spin h-4 w-4 text-white"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
            >
              <circle
                className="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                strokeWidth="4"
              />
              <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              />
            </svg>
          )}
          {isSubmitting ? 'Guardando...' : cliente ? 'Actualizar Cliente' : 'Crear Cliente'}
        </button>
      </div>
    </form>
  );
}
