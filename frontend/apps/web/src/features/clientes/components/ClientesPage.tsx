/**
 * Page: Clientes
 *
 * Página principal para gestión de clientes con importación y listado.
 */

import { useState, useEffect } from 'react';
import { ClienteImportador } from './ClienteImportador';
import { clientesApi } from '../api/clientes.api';
import type { Cliente, ImportacionResult } from '../types/cliente.types';

export function ClientesPage() {
  const [clientes, setClientes] = useState<Cliente[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [showImporter, setShowImporter] = useState(false);

  // TODO: Obtener de contexto de autenticación
  const unidadNegocioId = '00000000-0000-0000-0000-000000000001';

  useEffect(() => {
    loadClientes();
  }, []);

  const loadClientes = async () => {
    try {
      setIsLoading(true);
      const response = await clientesApi.list({ size: 50 });
      setClientes(response.content);
    } catch (error) {
      console.error('Error cargando clientes:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleImportSuccess = (result: ImportacionResult) => {
    console.log('Importación exitosa:', result);
    // Recargar la lista de clientes
    loadClientes();
  };

  const handleImportError = (error: Error) => {
    console.error('Error en importación:', error);
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Clientes</h1>
          <p className="mt-2 text-sm text-gray-600">
            Gestiona tus clientes y contactos empresariales
          </p>
        </div>

        {/* Botón para mostrar importador */}
        <div className="mb-6">
          <button
            onClick={() => setShowImporter(!showImporter)}
            className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            <svg
              className="-ml-1 mr-2 h-5 w-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
              />
            </svg>
            {showImporter ? 'Ocultar' : 'Importar'} Clientes
          </button>
        </div>

        {/* Importador */}
        {showImporter && (
          <div className="bg-white shadow rounded-lg p-6 mb-6">
            <h2 className="text-lg font-medium text-gray-900 mb-4">
              Importar Clientes desde CSV
            </h2>
            <ClienteImportador
              unidadNegocioId={unidadNegocioId}
              onSuccess={handleImportSuccess}
              onError={handleImportError}
            />
          </div>
        )}

        {/* Lista de clientes */}
        <div className="bg-white shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <h2 className="text-lg font-medium text-gray-900 mb-4">
              Clientes Registrados
            </h2>

            {isLoading ? (
              <div className="flex justify-center py-12">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              </div>
            ) : clientes.length === 0 ? (
              <div className="text-center py-12">
                <svg
                  className="mx-auto h-12 w-12 text-gray-400"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                  />
                </svg>
                <h3 className="mt-2 text-sm font-medium text-gray-900">
                  No hay clientes
                </h3>
                <p className="mt-1 text-sm text-gray-500">
                  Comienza importando clientes desde un archivo CSV.
                </p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Código
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Nombre
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Razón Social
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Status
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Tipo
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {clientes.map((cliente) => (
                      <tr key={cliente.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          {cliente.codigo}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {cliente.nombre}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {cliente.razonSocial || '-'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span
                            className={`inline-flex px-2 text-xs font-semibold rounded-full ${
                              cliente.status === 'ACTIVE'
                                ? 'bg-green-100 text-green-800'
                                : cliente.status === 'INACTIVE'
                                ? 'bg-gray-100 text-gray-800'
                                : cliente.status === 'PROSPECT'
                                ? 'bg-blue-100 text-blue-800'
                                : cliente.status === 'LEAD'
                                ? 'bg-yellow-100 text-yellow-800'
                                : 'bg-red-100 text-red-800'
                            }`}
                          >
                            {cliente.status}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {cliente.tipo === 'EMPRESA' ? 'Empresa' : 'Persona'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
