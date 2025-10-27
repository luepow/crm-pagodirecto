/**
 * Component: ClienteImportador
 *
 * Componente para importar clientes desde archivo CSV con drag & drop.
 * Muestra estadísticas y errores de la importación.
 */

import { useState, useRef } from 'react';
import { clientesApi } from '../api/clientes.api';
import type { ImportacionResult } from '../types/cliente.types';

interface ClienteImportadorProps {
  unidadNegocioId: string;
  onSuccess?: (result: ImportacionResult) => void;
  onError?: (error: Error) => void;
}

export function ClienteImportador({
  unidadNegocioId,
  onSuccess,
  onError,
}: ClienteImportadorProps) {
  const [isDragging, setIsDragging] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [result, setResult] = useState<ImportacionResult | null>(null);
  const [error, setError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleDragEnter = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
  };

  const handleDrop = async (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);

    const files = Array.from(e.dataTransfer.files);
    if (files.length > 0) {
      await handleFileUpload(files[0]!);
    }
  };

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      await handleFileUpload(files[0]!);
    }
  };

  const handleFileUpload = async (file: File) => {
    // Validar que sea CSV
    if (!file.name.toLowerCase().endsWith('.csv')) {
      const errorMsg = 'El archivo debe ser formato CSV';
      setError(errorMsg);
      onError?.(new Error(errorMsg));
      return;
    }

    setIsUploading(true);
    setError(null);
    setResult(null);

    try {
      const importResult = await clientesApi.importFromCSV(file, unidadNegocioId);
      setResult(importResult);
      onSuccess?.(importResult);
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : 'Error al importar clientes';
      setError(errorMsg);
      onError?.(err instanceof Error ? err : new Error(errorMsg));
    } finally {
      setIsUploading(false);
      // Reset file input
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const handleClickUpload = () => {
    fileInputRef.current?.click();
  };

  const resetImport = () => {
    setResult(null);
    setError(null);
  };

  return (
    <div className="space-y-4">
      {/* Área de drag & drop */}
      <div
        className={`
          relative border-2 border-dashed rounded-lg p-8 text-center transition-colors
          ${isDragging ? 'border-blue-500 bg-blue-50' : 'border-gray-300 hover:border-gray-400'}
          ${isUploading ? 'opacity-50 pointer-events-none' : 'cursor-pointer'}
        `}
        onDragEnter={handleDragEnter}
        onDragLeave={handleDragLeave}
        onDragOver={handleDragOver}
        onDrop={handleDrop}
        onClick={handleClickUpload}
      >
        <input
          ref={fileInputRef}
          type="file"
          accept=".csv"
          onChange={handleFileSelect}
          className="hidden"
        />

        <div className="space-y-2">
          {isUploading ? (
            <>
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
              <p className="text-sm text-gray-600">Importando clientes...</p>
            </>
          ) : (
            <>
              <svg
                className="mx-auto h-12 w-12 text-gray-400"
                stroke="currentColor"
                fill="none"
                viewBox="0 0 48 48"
                aria-hidden="true"
              >
                <path
                  d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02"
                  strokeWidth={2}
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </svg>
              <div className="text-sm text-gray-600">
                <p className="font-medium">Arrastra tu archivo CSV aquí</p>
                <p className="mt-1">o haz clic para seleccionar</p>
              </div>
              <p className="text-xs text-gray-500 mt-2">
                Formato: una columna con nombres de empresas (sin encabezado)
              </p>
            </>
          )}
        </div>
      </div>

      {/* Mensaje de error */}
      {error && (
        <div className="rounded-md bg-red-50 p-4 border border-red-200">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg
                className="h-5 w-5 text-red-400"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fillRule="evenodd"
                  d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                  clipRule="evenodd"
                />
              </svg>
            </div>
            <div className="ml-3">
              <h3 className="text-sm font-medium text-red-800">Error</h3>
              <p className="text-sm text-red-700 mt-1">{error}</p>
            </div>
          </div>
        </div>
      )}

      {/* Resultado de importación */}
      {result && (
        <div className="space-y-4">
          {/* Resumen */}
          <div className={`rounded-md p-4 border ${
            result.exitoCompleto
              ? 'bg-green-50 border-green-200'
              : result.registrosExitosos > 0
              ? 'bg-yellow-50 border-yellow-200'
              : 'bg-red-50 border-red-200'
          }`}>
            <div className="flex">
              <div className="flex-shrink-0">
                {result.exitoCompleto ? (
                  <svg
                    className="h-5 w-5 text-green-400"
                    viewBox="0 0 20 20"
                    fill="currentColor"
                  >
                    <path
                      fillRule="evenodd"
                      d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                      clipRule="evenodd"
                    />
                  </svg>
                ) : (
                  <svg
                    className="h-5 w-5 text-yellow-400"
                    viewBox="0 0 20 20"
                    fill="currentColor"
                  >
                    <path
                      fillRule="evenodd"
                      d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
                      clipRule="evenodd"
                    />
                  </svg>
                )}
              </div>
              <div className="ml-3 flex-1">
                <h3 className="text-sm font-medium text-gray-900">{result.mensaje}</h3>
                <div className="mt-2 text-sm">
                  <div className="grid grid-cols-3 gap-4">
                    <div>
                      <span className="font-medium text-gray-700">Total:</span>
                      <span className="ml-1 text-gray-900">{result.totalRegistros}</span>
                    </div>
                    <div>
                      <span className="font-medium text-green-700">Exitosos:</span>
                      <span className="ml-1 text-green-900">{result.registrosExitosos}</span>
                    </div>
                    <div>
                      <span className="font-medium text-red-700">Errores:</span>
                      <span className="ml-1 text-red-900">{result.registrosConErrores}</span>
                    </div>
                  </div>
                  <div className="mt-2">
                    <span className="font-medium text-gray-700">Tasa de éxito:</span>
                    <span className="ml-1 text-gray-900">{result.tasaExito.toFixed(1)}%</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Lista de errores */}
          {result.errores.length > 0 && (
            <div className="rounded-md bg-red-50 p-4 border border-red-200">
              <h4 className="text-sm font-medium text-red-800 mb-2">
                Errores encontrados ({result.errores.length})
              </h4>
              <div className="max-h-40 overflow-y-auto">
                <ul className="text-sm text-red-700 space-y-1">
                  {result.errores.map((error, index) => (
                    <li key={index} className="flex items-start">
                      <span className="mr-2">•</span>
                      <span>{error}</span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          )}

          {/* Botón para nueva importación */}
          <div className="text-center">
            <button
              onClick={resetImport}
              className="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              Importar otro archivo
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
