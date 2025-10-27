/**
 * Page: DepartamentosPage
 *
 * Página de gestión de departamentos organizacionales
 */

import React, { useState, useEffect } from 'react';
import { Building2, Search, Edit, Trash2, ToggleRight, ToggleLeft, ChevronRight } from 'lucide-react';
import { Card, CardContent } from '@shared-ui/components/Card';
import { Button } from '@shared-ui/components/Button';
import { toast } from 'sonner';
import { departamentoApi } from '../../features/departamentos/api/departamento.api';
import type { Departamento } from '../../features/departamentos/types/departamento.types';
import { CreateDepartamentoModal } from '../../features/departamentos/components/CreateDepartamentoModal';
import { EditDepartamentoModal } from '../../features/departamentos/components/EditDepartamentoModal';

/**
 * DepartamentosPage Component
 */
export const DepartamentosPage: React.FC = () => {
  const [departamentos, setDepartamentos] = useState<Departamento[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [selectedDepartamento, setSelectedDepartamento] = useState<Departamento | null>(null);

  useEffect(() => {
    loadDepartamentos();
  }, []);

  const loadDepartamentos = async () => {
    try {
      setIsLoading(true);
      const data = await departamentoApi.getAllDepartamentos();
      setDepartamentos(data);
    } catch (error) {
      console.error('Error cargando departamentos:', error);
      toast.error('Error al cargar los departamentos');
    } finally {
      setIsLoading(false);
    }
  };

  const handleEdit = (departamento: Departamento) => {
    setSelectedDepartamento(departamento);
    setIsEditModalOpen(true);
  };

  const handleDelete = async (departamento: Departamento) => {
    if (!window.confirm(`¿Estás seguro de eliminar el departamento ${departamento.nombre}?`)) {
      return;
    }

    try {
      await departamentoApi.deleteDepartamento(departamento.id);
      toast.success('Departamento eliminado exitosamente');
      loadDepartamentos();
    } catch (error: any) {
      console.error('Error eliminando departamento:', error);
      const message = error.response?.data?.message || 'Error al eliminar el departamento';
      toast.error(message);
    }
  };

  const handleToggleActivo = async (departamento: Departamento) => {
    try {
      await departamentoApi.toggleActivoDepartamento(departamento.id);
      toast.success(`Departamento ${departamento.activo ? 'desactivado' : 'activado'} exitosamente`);
      loadDepartamentos();
    } catch (error) {
      console.error('Error cambiando estado:', error);
      toast.error('Error al cambiar el estado del departamento');
    }
  };

  const handleCreateSuccess = () => {
    setIsCreateModalOpen(false);
    loadDepartamentos();
  };

  const handleEditSuccess = () => {
    setIsEditModalOpen(false);
    setSelectedDepartamento(null);
    loadDepartamentos();
  };

  const filteredDepartamentos = departamentos.filter(
    (dep) =>
      dep.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
      dep.codigo.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (dep.descripcion?.toLowerCase().includes(searchTerm.toLowerCase()) ?? false)
  );

  const formatCurrency = (amount?: number) => {
    if (!amount) return '-';
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN',
    }).format(amount);
  };

  const getNivelBadge = (nivel: number) => {
    const colors = [
      'bg-purple-100 text-purple-800',
      'bg-blue-100 text-blue-800',
      'bg-green-100 text-green-800',
      'bg-yellow-100 text-yellow-800',
      'bg-orange-100 text-orange-800',
      'bg-red-100 text-red-800',
    ];
    return colors[nivel] || 'bg-gray-100 text-gray-800';
  };

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
          <p className="mt-4 text-text-secondary">Cargando departamentos...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-text-primary">Departamentos</h1>
          <p className="mt-1 text-text-secondary">Gestiona la estructura organizacional</p>
        </div>

        <Button
          variant="primary"
          leftIcon={<Building2 size={18} />}
          onClick={() => setIsCreateModalOpen(true)}
        >
          Nuevo Departamento
        </Button>
      </div>

      {/* Search bar */}
      <Card>
        <CardContent className="pt-6">
          <div className="relative">
            <Search
              className="absolute left-3 top-1/2 -translate-y-1/2 text-text-tertiary"
              size={20}
            />
            <input
              type="text"
              placeholder="Buscar por nombre, código o descripción..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full rounded-xl border border-gray-300 pl-10 pr-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
        </CardContent>
      </Card>

      {/* Departments table */}
      <Card>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">
                    Departamento
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">
                    Jerarquía
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">
                    Empleados
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">
                    Presupuesto
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">
                    Estado
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-text-secondary uppercase tracking-wider">
                    Acciones
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredDepartamentos.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="px-6 py-12 text-center text-text-secondary">
                      {searchTerm
                        ? 'No se encontraron departamentos con ese criterio de búsqueda'
                        : 'No hay departamentos registrados'}
                    </td>
                  </tr>
                ) : (
                  filteredDepartamentos.map((dep) => (
                    <tr key={dep.id} className="hover:bg-gray-50 transition-colors">
                      <td className="px-6 py-4">
                        <div>
                          <div className="flex items-center gap-2">
                            <Building2 size={16} className="text-primary" />
                            <span className="text-sm font-medium text-text-primary">{dep.nombre}</span>
                          </div>
                          <div className="text-xs text-text-tertiary mt-1">
                            {dep.codigo}
                          </div>
                          {dep.descripcion && (
                            <div className="text-xs text-text-secondary mt-1 line-clamp-1">
                              {dep.descripcion}
                            </div>
                          )}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex flex-col gap-1">
                          <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${getNivelBadge(dep.nivel)}`}>
                            Nivel {dep.nivel}
                          </span>
                          {dep.parentNombre && (
                            <div className="flex items-center gap-1 text-xs text-text-secondary">
                              <ChevronRight size={12} />
                              <span>{dep.parentNombre}</span>
                            </div>
                          )}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-text-secondary">
                        {dep.numeroEmpleados} empleados
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-text-secondary">
                        {formatCurrency(dep.presupuestoAnual)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span
                          className={`px-2 py-1 rounded-full text-xs font-medium ${
                            dep.activo
                              ? 'bg-green-100 text-green-800'
                              : 'bg-gray-100 text-gray-800'
                          }`}
                        >
                          {dep.activo ? 'Activo' : 'Inactivo'}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <div className="flex justify-end gap-2">
                          <button
                            onClick={() => handleEdit(dep)}
                            className="text-primary hover:text-primary-600 transition-colors"
                            title="Editar"
                          >
                            <Edit size={18} />
                          </button>
                          <button
                            onClick={() => handleToggleActivo(dep)}
                            className="text-blue-600 hover:text-blue-700 transition-colors"
                            title={dep.activo ? 'Desactivar' : 'Activar'}
                          >
                            {dep.activo ? <ToggleRight size={18} /> : <ToggleLeft size={18} />}
                          </button>
                          <button
                            onClick={() => handleDelete(dep)}
                            className="text-red-600 hover:text-red-700 transition-colors"
                            title="Eliminar"
                          >
                            <Trash2 size={18} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>

      {/* Summary */}
      <div className="text-sm text-text-secondary">
        Mostrando {filteredDepartamentos.length} de {departamentos.length} departamentos
      </div>

      {/* Create Modal */}
      {isCreateModalOpen && (
        <CreateDepartamentoModal
          isOpen={isCreateModalOpen}
          onClose={() => setIsCreateModalOpen(false)}
          onSuccess={handleCreateSuccess}
          departamentos={departamentos}
        />
      )}

      {/* Edit Modal */}
      {isEditModalOpen && selectedDepartamento && (
        <EditDepartamentoModal
          isOpen={isEditModalOpen}
          departamento={selectedDepartamento}
          departamentos={departamentos}
          onClose={() => {
            setIsEditModalOpen(false);
            setSelectedDepartamento(null);
          }}
          onSuccess={handleEditSuccess}
        />
      )}
    </div>
  );
};
