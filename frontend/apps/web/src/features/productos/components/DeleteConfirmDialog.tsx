/**
 * DeleteConfirmDialog - Confirmation dialog for deleting products
 */

import React from 'react';
import { AlertTriangle } from 'lucide-react';
import { Button } from '@shared-ui/components/Button';

interface DeleteConfirmDialogProps {
  open: boolean;
  productName: string;
  onConfirm: () => void;
  onCancel: () => void;
  isDeleting?: boolean;
}

export const DeleteConfirmDialog: React.FC<DeleteConfirmDialogProps> = ({
  open,
  productName,
  onConfirm,
  onCancel,
  isDeleting,
}) => {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
        onClick={onCancel}
        aria-hidden="true"
      />

      {/* Dialog Container */}
      <div className="flex min-h-screen items-center justify-center p-4">
        <div
          className="relative w-full max-w-md bg-white rounded-lg shadow-xl transform transition-all"
          onClick={(e) => e.stopPropagation()}
        >
          <div className="p-6">
            {/* Icon */}
            <div className="flex items-center justify-center w-12 h-12 mx-auto bg-red-100 rounded-full">
              <AlertTriangle className="w-6 h-6 text-red-600" />
            </div>

            {/* Content */}
            <div className="mt-4 text-center">
              <h3 className="text-lg font-semibold text-gray-900">Eliminar Producto</h3>
              <p className="mt-2 text-sm text-gray-600">
                ¿Estás seguro que deseas eliminar el producto <span className="font-semibold">"{productName}"</span>?
              </p>
              <p className="mt-1 text-sm text-red-600">Esta acción no se puede deshacer.</p>
            </div>

            {/* Actions */}
            <div className="mt-6 flex gap-3 justify-end">
              <Button variant="outline" onClick={onCancel} disabled={isDeleting}>
                Cancelar
              </Button>
              <Button variant="danger" onClick={onConfirm} disabled={isDeleting}>
                {isDeleting ? 'Eliminando...' : 'Eliminar'}
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
