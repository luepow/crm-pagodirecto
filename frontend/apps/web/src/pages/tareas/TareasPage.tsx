/**
 * Tareas Page
 *
 * Task management page with CRUD operations and filtering.
 */

import React from 'react';
import { Plus, Search, Filter, CheckCircle2, Clock, AlertCircle } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@shared-ui/components/Card';
import { Button } from '@shared-ui/components/Button';
import { Input } from '@shared-ui/components/Input';
import { Badge } from '@shared-ui/components/Badge';
import { TableSkeleton } from '@shared-ui/components/Skeleton';
import { TareaModal } from '../../features/tareas';
// Usando mock API temporalmente mientras el backend está en desarrollo
// TODO: Cambiar a tareasApi cuando el backend esté listo
import { tareasMockApi as tareasApi } from '../../features/tareas/api/tareas.api.mock';
import type { Tarea, TareaFormData, StatusTarea, PrioridadTarea } from '../../features/tareas';
import { toast } from 'sonner';

export const TareasPage: React.FC = () => {
  const [isLoading, setIsLoading] = React.useState(true);
  const [tareas, setTareas] = React.useState<Tarea[]>([]);
  const [showFormModal, setShowFormModal] = React.useState(false);
  const [selectedTarea, setSelectedTarea] = React.useState<Tarea | undefined>();
  const [isSubmitting, setIsSubmitting] = React.useState(false);
  const [statusFilter, setStatusFilter] = React.useState<StatusTarea | 'TODAS'>('TODAS');

  // TODO: Obtener del contexto de autenticación
  const unidadNegocioId = '00000000-0000-0000-0000-000000000001';

  React.useEffect(() => {
    loadTareas();
  }, [statusFilter]);

  const loadTareas = async () => {
    try {
      setIsLoading(true);
      const params = statusFilter !== 'TODAS' ? { status: statusFilter as StatusTarea, size: 50 } : { size: 50 };
      const response = await tareasApi.list(params);
      setTareas(response.content);
    } catch (error) {
      console.error('Error cargando tareas:', error);
      toast.error('Error al cargar las tareas');
    } finally {
      setIsLoading(false);
    }
  };

  const handleOpenCreateModal = () => {
    setSelectedTarea(undefined);
    setShowFormModal(true);
  };

  const handleOpenEditModal = (tarea: Tarea) => {
    setSelectedTarea(tarea);
    setShowFormModal(true);
  };

  const handleCloseModal = () => {
    setShowFormModal(false);
    setSelectedTarea(undefined);
  };

  const handleSubmitTarea = async (data: TareaFormData) => {
    setIsSubmitting(true);
    try {
      if (selectedTarea) {
        await tareasApi.update(selectedTarea.id!, data);
        toast.success('Tarea actualizada exitosamente');
      } else {
        await tareasApi.create(data);
        toast.success('Tarea creada exitosamente');
      }
      await loadTareas();
      handleCloseModal();
    } catch (error) {
      console.error('Error al guardar tarea:', error);
      toast.error('Error al guardar la tarea');
      throw error;
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCompletarTarea = async (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await tareasApi.completar(id);
      toast.success('Tarea completada');
      await loadTareas();
    } catch (error) {
      console.error('Error al completar tarea:', error);
      toast.error('Error al completar la tarea');
    }
  };

  const getStatusBadge = (status: StatusTarea) => {
    const statusConfig = {
      PENDIENTE: { label: 'Pendiente', variant: 'warning' as const },
      EN_PROGRESO: { label: 'En Progreso', variant: 'info' as const },
      COMPLETADA: { label: 'Completada', variant: 'success' as const },
      CANCELADA: { label: 'Cancelada', variant: 'default' as const },
      BLOQUEADA: { label: 'Bloqueada', variant: 'error' as const },
    };
    const config = statusConfig[status];
    return <Badge variant={config.variant}>{config.label}</Badge>;
  };

  const getPrioridadBadge = (prioridad: PrioridadTarea) => {
    const prioridadConfig = {
      BAJA: { label: 'Baja', variant: 'success' as const },
      MEDIA: { label: 'Media', variant: 'warning' as const },
      ALTA: { label: 'Alta', variant: 'error' as const },
      URGENTE: { label: 'Urgente', variant: 'error' as const },
    };
    const config = prioridadConfig[prioridad];
    return <Badge variant={config.variant}>{config.label}</Badge>;
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('es-VE', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    }).format(date);
  };

  const isVencida = (tarea: Tarea) => {
    if (!tarea.fechaVencimiento || tarea.status === 'COMPLETADA' || tarea.status === 'CANCELADA') {
      return false;
    }
    return new Date(tarea.fechaVencimiento) < new Date();
  };

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-text-primary">Tareas</h1>
          <p className="mt-1 text-text-secondary">Organiza y da seguimiento a tus actividades</p>
        </div>
        <Button variant="primary" leftIcon={<Plus size={18} />} onClick={handleOpenCreateModal}>
          Nueva Tarea
        </Button>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardContent padding="md">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-yellow-100 rounded-lg">
                <Clock className="w-6 h-6 text-yellow-600" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Pendientes</p>
                <p className="text-2xl font-bold text-gray-900">
                  {tareas.filter((t) => t.status === 'PENDIENTE').length}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent padding="md">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-blue-100 rounded-lg">
                <Filter className="w-6 h-6 text-blue-600" />
              </div>
              <div>
                <p className="text-sm text-gray-600">En Progreso</p>
                <p className="text-2xl font-bold text-gray-900">
                  {tareas.filter((t) => t.status === 'EN_PROGRESO').length}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent padding="md">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-green-100 rounded-lg">
                <CheckCircle2 className="w-6 h-6 text-green-600" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Completadas</p>
                <p className="text-2xl font-bold text-gray-900">
                  {tareas.filter((t) => t.status === 'COMPLETADA').length}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent padding="md">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-red-100 rounded-lg">
                <AlertCircle className="w-6 h-6 text-red-600" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Vencidas</p>
                <p className="text-2xl font-bold text-gray-900">
                  {tareas.filter((t) => isVencida(t)).length}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Filters */}
      <Card>
        <CardContent padding="md">
          <div className="flex flex-col gap-3 sm:flex-row">
            <div className="flex-1">
              <Input placeholder="Buscar tareas..." leftIcon={<Search size={20} />} />
            </div>
            <div className="flex gap-2">
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value as StatusTarea | 'TODAS')}
                className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="TODAS">Todas</option>
                <option value="PENDIENTE">Pendientes</option>
                <option value="EN_PROGRESO">En Progreso</option>
                <option value="COMPLETADA">Completadas</option>
                <option value="BLOQUEADA">Bloqueadas</option>
              </select>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Tareas List */}
      <Card>
        <CardHeader>
          <CardTitle>Lista de Tareas ({tareas.length})</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <TableSkeleton rows={5} />
          ) : tareas.length === 0 ? (
            <div className="text-center py-12">
              <Clock className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900">No hay tareas</h3>
              <p className="mt-1 text-sm text-gray-500">Comienza creando una nueva tarea.</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Tarea
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Tipo
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Prioridad
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Estado
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Vencimiento
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Acciones
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {tareas.map((tarea) => (
                    <tr
                      key={tarea.id}
                      className={`hover:bg-gray-50 cursor-pointer ${
                        isVencida(tarea) ? 'bg-red-50' : ''
                      }`}
                      onClick={() => handleOpenEditModal(tarea)}
                    >
                      <td className="px-6 py-4">
                        <div className="text-sm font-medium text-gray-900">{tarea.titulo}</div>
                        {tarea.descripcion && (
                          <div className="text-sm text-gray-500 truncate max-w-xs">{tarea.descripcion}</div>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {tarea.tipo}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {getPrioridadBadge(tarea.prioridad as PrioridadTarea)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {getStatusBadge(tarea.status as StatusTarea)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm">
                        <span className={isVencida(tarea) ? 'text-red-600 font-semibold' : 'text-gray-500'}>
                          {formatDate(tarea.fechaVencimiento)}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm">
                        {tarea.status !== 'COMPLETADA' && tarea.status !== 'CANCELADA' && (
                          <button
                            onClick={(e) => handleCompletarTarea(tarea.id!, e)}
                            className="text-green-600 hover:text-green-900 font-medium"
                          >
                            Completar
                          </button>
                        )}
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
      <TareaModal
        isOpen={showFormModal}
        tarea={selectedTarea}
        unidadNegocioId={unidadNegocioId}
        onClose={handleCloseModal}
        onSubmit={handleSubmitTarea}
        isSubmitting={isSubmitting}
      />
    </div>
  );
};
