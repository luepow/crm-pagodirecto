/**
 * Oportunidades Page
 *
 * Opportunities management with table view and CRUD operations.
 */

import React from 'react';
import { Plus, Search, Filter, DollarSign } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@shared-ui/components/Card';
import { Button } from '@shared-ui/components/Button';
import { Input } from '@shared-ui/components/Input';
import { TableSkeleton } from '@shared-ui/components/Skeleton';
import { OportunidadModal } from '../../features/oportunidades';
// Usando mock API temporalmente mientras el backend está en desarrollo
// TODO: Cambiar a oportunidadesApi cuando el backend esté listo
import { oportunidadesMockApi as oportunidadesApi } from '../../features/oportunidades/api/oportunidades.api.mock';
import type { Oportunidad, OportunidadFormData } from '../../features/oportunidades';
import { toast } from 'sonner';

export const OportunidadesPage: React.FC = () => {
  const [isLoading, setIsLoading] = React.useState(true);
  const [oportunidades, setOportunidades] = React.useState<Oportunidad[]>([]);
  const [showFormModal, setShowFormModal] = React.useState(false);
  const [selectedOportunidad, setSelectedOportunidad] = React.useState<Oportunidad | undefined>();
  const [isSubmitting, setIsSubmitting] = React.useState(false);

  // TODO: Obtener del contexto de autenticación
  const unidadNegocioId = '00000000-0000-0000-0000-000000000001';

  React.useEffect(() => {
    loadOportunidades();
  }, []);

  const loadOportunidades = async () => {
    try {
      setIsLoading(true);
      const response = await oportunidadesApi.list({ size: 50 });
      setOportunidades(response.content);
    } catch (error) {
      console.error('Error cargando oportunidades:', error);
      toast.error('Error al cargar las oportunidades');
    } finally {
      setIsLoading(false);
    }
  };

  const handleOpenCreateModal = () => {
    setSelectedOportunidad(undefined);
    setShowFormModal(true);
  };

  const handleOpenEditModal = (oportunidad: Oportunidad) => {
    setSelectedOportunidad(oportunidad);
    setShowFormModal(true);
  };

  const handleCloseModal = () => {
    setShowFormModal(false);
    setSelectedOportunidad(undefined);
  };

  const handleSubmitOportunidad = async (data: OportunidadFormData) => {
    setIsSubmitting(true);
    try {
      if (selectedOportunidad) {
        await oportunidadesApi.update(selectedOportunidad.id!, data);
        toast.success('Oportunidad actualizada exitosamente');
      } else {
        await oportunidadesApi.create(data);
        toast.success('Oportunidad creada exitosamente');
      }
      await loadOportunidades();
      handleCloseModal();
    } catch (error) {
      console.error('Error al guardar oportunidad:', error);
      toast.error('Error al guardar la oportunidad');
      throw error;
    } finally {
      setIsSubmitting(false);
    }
  };

  const formatCurrency = (value: number, currency: string) => {
    return new Intl.NumberFormat('es-VE', {
      style: 'currency',
      currency: currency || 'USD',
    }).format(value);
  };

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-text-primary">Oportunidades</h1>
          <p className="mt-1 text-text-secondary">Pipeline de ventas y oportunidades</p>
        </div>
        <Button variant="primary" leftIcon={<Plus size={18} />} onClick={handleOpenCreateModal}>
          Nueva Oportunidad
        </Button>
      </div>

      {/* Filters */}
      <Card>
        <CardContent padding="md">
          <div className="flex flex-col gap-3 sm:flex-row">
            <div className="flex-1">
              <Input placeholder="Buscar oportunidades..." leftIcon={<Search size={20} />} />
            </div>
            <Button variant="outline" leftIcon={<Filter size={18} />}>
              Filtros
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Oportunidades List */}
      <Card>
        <CardHeader>
          <CardTitle>Lista de Oportunidades ({oportunidades.length})</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <TableSkeleton rows={5} />
          ) : oportunidades.length === 0 ? (
            <div className="text-center py-12">
              <DollarSign className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900">No hay oportunidades</h3>
              <p className="mt-1 text-sm text-gray-500">Comienza creando una nueva oportunidad.</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Título
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Cliente
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Valor
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Probabilidad
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Valor Ponderado
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Fecha Cierre
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {oportunidades.map((oportunidad) => (
                    <tr
                      key={oportunidad.id}
                      className="hover:bg-gray-50 cursor-pointer"
                      onClick={() => handleOpenEditModal(oportunidad)}
                    >
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-gray-900">{oportunidad.titulo}</div>
                        {oportunidad.descripcion && (
                          <div className="text-sm text-gray-500 truncate max-w-xs">{oportunidad.descripcion}</div>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {oportunidad.clienteNombre || oportunidad.clienteId}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-medium">
                        {formatCurrency(oportunidad.valorEstimado, oportunidad.moneda)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <div className="flex-1 bg-gray-200 rounded-full h-2 mr-2 w-20">
                            <div
                              className="bg-green-500 h-2 rounded-full"
                              style={{ width: `${oportunidad.probabilidad}%` }}
                            />
                          </div>
                          <span className="text-sm text-gray-600">{oportunidad.probabilidad}%</span>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {oportunidad.valorPonderado
                          ? formatCurrency(oportunidad.valorPonderado, oportunidad.moneda)
                          : '-'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {oportunidad.fechaCierreEstimada || '-'}
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
      <OportunidadModal
        isOpen={showFormModal}
        oportunidad={selectedOportunidad}
        unidadNegocioId={unidadNegocioId}
        onClose={handleCloseModal}
        onSubmit={handleSubmitOportunidad}
        isSubmitting={isSubmitting}
      />
    </div>
  );
};
