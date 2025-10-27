/**
 * ProductoDialog - Modal dialog for product form
 */

import React from 'react';
import { X } from 'lucide-react';
import { ProductoFormulario } from './ProductoFormulario';
import type { Producto } from '../types/producto.types';
import type { ProductoSchemaType } from '../schemas/producto.schema';

interface ProductoDialogProps {
  open: boolean;
  producto?: Producto;
  unidadNegocioId: string;
  onClose: () => void;
  onSubmit: (data: ProductoSchemaType & { unidadNegocioId: string }) => Promise<void>;
  isLoading?: boolean;
}

export const ProductoDialog: React.FC<ProductoDialogProps> = ({
  open,
  producto,
  unidadNegocioId,
  onClose,
  onSubmit,
  isLoading,
}) => {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
        onClick={onClose}
        aria-hidden="true"
      />

      {/* Dialog Container */}
      <div className="flex min-h-screen items-center justify-center p-4">
        <div
          className="relative w-full max-w-4xl bg-white rounded-lg shadow-xl transform transition-all"
          onClick={(e) => e.stopPropagation()}
        >
          {/* Header */}
          <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
            <h2 className="text-xl font-semibold text-gray-900">
              {producto ? 'Editar Producto' : 'Nuevo Producto'}
            </h2>
            <button
              type="button"
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 transition-colors"
              disabled={isLoading}
            >
              <X size={24} />
            </button>
          </div>

          {/* Content */}
          <div className="px-6 py-4 max-h-[calc(100vh-200px)] overflow-y-auto">
            <ProductoFormulario
              producto={producto}
              unidadNegocioId={unidadNegocioId}
              onSubmit={onSubmit}
              onCancel={onClose}
              isLoading={isLoading}
            />
          </div>
        </div>
      </div>
    </div>
  );
};
