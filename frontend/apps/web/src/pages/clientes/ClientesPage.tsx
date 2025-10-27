/**
 * Clientes Page
 *
 * Customer management page with list view, search, and import functionality.
 */

import React from 'react';
import { Plus, Upload, Search, Filter } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@shared-ui/components/Card';
import { Button } from '@shared-ui/components/Button';
import { Input } from '@shared-ui/components/Input';
import { Badge } from '@shared-ui/components/Badge';
import { TableSkeleton } from '@shared-ui/components/Skeleton';
import { ClienteImportador, ClienteModal } from '../../features/clientes';
import { clientesApi } from '../../features/clientes';
import type { Cliente, ClienteFormData } from '../../features/clientes';
import { toast } from 'sonner';

/**
 * Clientes Page Component
 */
export const ClientesPage: React.FC = () => {
  const [isLoading, setIsLoading] = React.useState(true);
  const [clientes, setClientes] = React.useState<Cliente[]>([]);
  const [showImporter, setShowImporter] = React.useState(false);
  const [showFormModal, setShowFormModal] = React.useState(false);
  const [selectedCliente, setSelectedCliente] = React.useState<Cliente | undefined>();
  const [isSubmitting, setIsSubmitting] = React.useState(false);

  // TODO: Obtener del contexto de autenticación
  const unidadNegocioId = '00000000-0000-0000-0000-000000000001';

  React.useEffect(() => {
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

  const handleImportSuccess = () => {
    loadClientes();
    setShowImporter(false);
    toast.success('Clientes importados exitosamente');
  };

  const handleOpenCreateModal = () => {
    setSelectedCliente(undefined);
    setShowFormModal(true);
  };

  const handleOpenEditModal = (cliente: Cliente) => {
    setSelectedCliente(cliente);
    setShowFormModal(true);
  };

  const handleCloseModal = () => {
    setShowFormModal(false);
    setSelectedCliente(undefined);
  };

  const handleSubmitCliente = async (data: ClienteFormData) => {
    setIsSubmitting(true);
    try {
      if (selectedCliente) {
        // Actualizar cliente existente
        await clientesApi.update(selectedCliente.id!, data);
        toast.success('Cliente actualizado exitosamente');
      } else {
        // Crear nuevo cliente
        await clientesApi.create(data);
        toast.success('Cliente creado exitosamente');
      }
      await loadClientes();
      handleCloseModal();
    } catch (error) {
      console.error('Error al guardar cliente:', error);
      toast.error('Error al guardar el cliente. Por favor intenta de nuevo.');
      throw error;
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-text-primary">Clientes</h1>
          <p className="mt-1 text-text-secondary">
            Gestiona tu cartera de clientes empresariales
          </p>
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            leftIcon={<Upload size={18} />}
            onClick={() => setShowImporter(!showImporter)}
          >
            Importar CSV
          </Button>
          <Button variant="primary" leftIcon={<Plus size={18} />} onClick={handleOpenCreateModal}>
            Nuevo Cliente
          </Button>
        </div>
      </div>

      {/* Importador */}
      {showImporter && (
        <Card>
          <CardHeader>
            <CardTitle>Importar Clientes desde CSV</CardTitle>
          </CardHeader>
          <CardContent>
            <ClienteImportador
              unidadNegocioId={unidadNegocioId}
              onSuccess={handleImportSuccess}
              onError={(error) => console.error('Error en importación:', error)}
            />
          </CardContent>
        </Card>
      )}

      {/* Filters */}
      <Card>
        <CardContent padding="md">
          <div className="flex flex-col gap-3 sm:flex-row">
            <div className="flex-1">
              <Input
                placeholder="Buscar por nombre, email o empresa..."
                leftIcon={<Search size={20} />}
              />
            </div>
            <Button variant="outline" leftIcon={<Filter size={18} />}>
              Filtros
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Clientes List */}
      <Card>
        <CardHeader>
          <CardTitle>Lista de Clientes ({clientes.length})</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <TableSkeleton rows={5} />
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
                No hay clientes registrados
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
                      Nombre / Razón Social
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Email
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Teléfono
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
                    <tr
                      key={cliente.id}
                      className="hover:bg-gray-50 cursor-pointer"
                      onClick={() => handleOpenEditModal(cliente)}
                    >
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        {cliente.codigo}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-gray-900">{cliente.nombre}</div>
                        {cliente.razonSocial && cliente.razonSocial !== cliente.nombre && (
                          <div className="text-sm text-gray-500">{cliente.razonSocial}</div>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {cliente.email || '-'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {cliente.telefono || '-'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <Badge
                          variant={
                            cliente.status === 'ACTIVE'
                              ? 'success'
                              : cliente.status === 'INACTIVE'
                              ? 'default'
                              : cliente.status === 'PROSPECT'
                              ? 'info'
                              : cliente.status === 'LEAD'
                              ? 'warning'
                              : 'error'
                          }
                          size="sm"
                        >
                          {cliente.status}
                        </Badge>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <Badge variant="default" size="sm">
                          {cliente.tipo === 'EMPRESA' ? 'Empresa' : 'Persona'}
                        </Badge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Modal de Formulario */}
      <ClienteModal
        isOpen={showFormModal}
        cliente={selectedCliente}
        unidadNegocioId={unidadNegocioId}
        onClose={handleCloseModal}
        onSubmit={handleSubmitCliente}
        isSubmitting={isSubmitting}
      />
    </div>
  );
};
