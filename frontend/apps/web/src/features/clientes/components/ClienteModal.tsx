/**
 * Component: ClienteModal
 *
 * Modal para crear/editar clientes.
 * Contiene el formulario de cliente con overlay y animaciones.
 */

import { useEffect } from 'react';
import { X } from 'lucide-react';
import { ClienteFormulario } from './ClienteFormulario';
import type { Cliente, ClienteFormData } from '../types/cliente.types';

interface ClienteModalProps {
  isOpen: boolean;
  cliente?: Cliente;
  unidadNegocioId: string;
  onClose: () => void;
  onSubmit: (data: ClienteFormData) => Promise<void>;
  isSubmitting?: boolean;
}

export function ClienteModal({
  isOpen,
  cliente,
  unidadNegocioId,
  onClose,
  onSubmit,
  isSubmitting = false,
}: ClienteModalProps) {
  // Bloquear scroll del body cuando el modal está abierto
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

  // Cerrar con tecla ESC
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
      {/* Overlay */}
      <div
        className="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
        onClick={!isSubmitting ? onClose : undefined}
      />

      {/* Modal */}
      <div className="flex min-h-full items-center justify-center p-4">
        <div
          className="relative w-full max-w-3xl bg-white rounded-lg shadow-xl transform transition-all"
          onClick={(e) => e.stopPropagation()}
        >
          {/* Header */}
          <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
            <h2 className="text-xl font-semibold text-gray-900">
              {cliente ? 'Editar Cliente' : 'Nuevo Cliente'}
            </h2>
            <button
              onClick={onClose}
              disabled={isSubmitting}
              className="text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 rounded-lg p-1 disabled:opacity-50 disabled:cursor-not-allowed"
              aria-label="Cerrar modal"
            >
              <X size={24} />
            </button>
          </div>

          {/* Body */}
          <div className="px-6 py-4 max-h-[calc(100vh-200px)] overflow-y-auto">
            <ClienteFormulario
              cliente={cliente}
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
