/**
 * Component: OportunidadModal
 *
 * Modal para crear/editar oportunidades.
 */

import { useEffect } from 'react';
import { X } from 'lucide-react';
import { OportunidadFormulario } from './OportunidadFormulario';
import type { Oportunidad, OportunidadFormData } from '../types/oportunidad.types';

interface OportunidadModalProps {
  isOpen: boolean;
  oportunidad?: Oportunidad;
  unidadNegocioId: string;
  onClose: () => void;
  onSubmit: (data: OportunidadFormData) => Promise<void>;
  isSubmitting?: boolean;
}

export function OportunidadModal({
  isOpen,
  oportunidad,
  unidadNegocioId,
  onClose,
  onSubmit,
  isSubmitting = false,
}: OportunidadModalProps) {
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }
    return () => {
      document.body.style.overflow = 'unset';
    };
  }, [isOpen]);

  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isOpen && !isSubmitting) {
        onClose();
      }
    };
    document.addEventListener('keydown', handleEscape);
    return () => document.removeEventListener('keydown', handleEscape);
  }, [isOpen, isSubmitting, onClose]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="fixed inset-0 bg-black bg-opacity-50 transition-opacity" onClick={!isSubmitting ? onClose : undefined} />
      <div className="flex min-h-full items-center justify-center p-4">
        <div className="relative w-full max-w-3xl bg-white rounded-lg shadow-xl" onClick={(e) => e.stopPropagation()}>
          <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
            <h2 className="text-xl font-semibold text-gray-900">
              {oportunidad ? 'Editar Oportunidad' : 'Nueva Oportunidad'}
            </h2>
            <button
              onClick={onClose}
              disabled={isSubmitting}
              className="text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 rounded-lg p-1 disabled:opacity-50"
            >
              <X size={24} />
            </button>
          </div>
          <div className="px-6 py-4 max-h-[calc(100vh-200px)] overflow-y-auto">
            <OportunidadFormulario
              oportunidad={oportunidad}
              unidadNegocioId={unidadNegocioId}
              onSubmit={onSubmit}
              onCancel={onClose}
              isSubmitting={isSubmitting}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
