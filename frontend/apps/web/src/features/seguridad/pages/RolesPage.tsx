/**
 * Página de gestión de Roles
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */

import React, { useState, useEffect } from 'react';
import { toast } from 'sonner';
import { Shield, Plus, Edit, Trash2, Key } from 'lucide-react';
import { rolApi } from '../api/rol.api';
import { permisoApi } from '../api/permiso.api';
import { RolWithPermisosDTO, CreateRolRequest, UpdateRolRequest } from '../types/rol.types';
import { PermisoDTO } from '../types/permiso.types';

export const RolesPage: React.FC = () => {
  const [roles, setRoles] = useState<RolWithPermisosDTO[]>([]);
  const [permisos, setPermisos] = useState<PermisoDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [showPermisosModal, setShowPermisosModal] = useState(false);
  const [editingRol, setEditingRol] = useState<RolWithPermisosDTO | null>(null);
  const [selectedRolForPermisos, setSelectedRolForPermisos] = useState<RolWithPermisosDTO | null>(null);

  // Form state
  const [formData, setFormData] = useState<CreateRolRequest>({
    unidadNegocioId: '00000000-0000-0000-0000-000000000001',
    nombre: '',
    descripcion: '',
    departamento: '',
    nivelJerarquico: 0,
    permisoIds: [],
  });

  const [selectedPermisoIds, setSelectedPermisoIds] = useState<string[]>([]);

  useEffect(() => {
    loadRoles();
    loadPermisos();
  }, []);

  const loadRoles = async () => {
    try {
      setLoading(true);
      const data = await rolApi.getAllRoles();
      setRoles(data);
    } catch (error: any) {
      toast.error('Error al cargar roles', {
        description: error.response?.data?.message || error.message,
      });
    } finally {
      setLoading(false);
    }
  };

  const loadPermisos = async () => {
    try {
      const data = await permisoApi.getAllPermisos();
      setPermisos(data);
    } catch (error: any) {
      toast.error('Error al cargar permisos', {
        description: error.response?.data?.message || error.message,
      });
    }
  };

  const handleCreate = () => {
    setEditingRol(null);
    setFormData({
      unidadNegocioId: '00000000-0000-0000-0000-000000000001',
      nombre: '',
      descripcion: '',
      departamento: '',
      nivelJerarquico: 0,
      permisoIds: [],
    });
    setShowModal(true);
  };

  const handleEdit = (rol: RolWithPermisosDTO) => {
    setEditingRol(rol);
    setFormData({
      unidadNegocioId: rol.unidadNegocioId,
      nombre: rol.nombre,
      descripcion: rol.descripcion,
      departamento: rol.departamento,
      nivelJerarquico: rol.nivelJerarquico,
      permisoIds: rol.permisos.map(p => p.id),
    });
    setShowModal(true);
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('¿Está seguro de eliminar este rol?')) return;

    try {
      await rolApi.deleteRol(id);
      toast.success('Rol eliminado exitosamente');
      loadRoles();
    } catch (error: any) {
      toast.error('Error al eliminar rol', {
        description: error.response?.data?.message || error.message,
      });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      if (editingRol) {
        const updateData: UpdateRolRequest = {
          nombre: formData.nombre,
          descripcion: formData.descripcion,
          departamento: formData.departamento,
          nivelJerarquico: formData.nivelJerarquico,
        };
        await rolApi.updateRol(editingRol.id, updateData);
        toast.success('Rol actualizado exitosamente');
      } else {
        await rolApi.createRol(formData);
        toast.success('Rol creado exitosamente');
      }
      setShowModal(false);
      loadRoles();
    } catch (error: any) {
      toast.error(editingRol ? 'Error al actualizar rol' : 'Error al crear rol', {
        description: error.response?.data?.message || error.message,
      });
    }
  };

  const handleManagePermisos = (rol: RolWithPermisosDTO) => {
    setSelectedRolForPermisos(rol);
    setSelectedPermisoIds(rol.permisos.map(p => p.id));
    setShowPermisosModal(true);
  };

  const handleSavePermisos = async () => {
    if (!selectedRolForPermisos) return;

    try {
      const currentPermisoIds = selectedRolForPermisos.permisos.map(p => p.id);
      const toAdd = selectedPermisoIds.filter(id => !currentPermisoIds.includes(id));
      const toRemove = currentPermisoIds.filter(id => !selectedPermisoIds.includes(id));

      if (toAdd.length > 0) {
        await rolApi.assignPermisos(selectedRolForPermisos.id, toAdd);
      }
      if (toRemove.length > 0) {
        await rolApi.removePermisos(selectedRolForPermisos.id, toRemove);
      }

      toast.success('Permisos actualizados exitosamente');
      setShowPermisosModal(false);
      loadRoles();
    } catch (error: any) {
      toast.error('Error al actualizar permisos', {
        description: error.response?.data?.message || error.message,
      });
    }
  };

  const togglePermiso = (permisoId: string) => {
    setSelectedPermisoIds(prev =>
      prev.includes(permisoId)
        ? prev.filter(id => id !== permisoId)
        : [...prev, permisoId]
    );
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-lg">Cargando roles...</div>
      </div>
    );
  }

  return (
    <div className="p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <Shield className="w-8 h-8 text-blue-600" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Gestión de Roles</h1>
            <p className="text-sm text-gray-600">Administre roles y permisos del sistema</p>
          </div>
        </div>
        <button
          onClick={handleCreate}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          <Plus className="w-5 h-5" />
          Nuevo Rol
        </button>
      </div>

      {/* Tabla de Roles */}
      <div className="bg-white rounded-lg shadow">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Nombre
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Descripción
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Departamento
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Nivel
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Permisos
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Acciones
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {roles.map((rol) => (
              <tr key={rol.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm font-medium text-gray-900">{rol.nombre}</div>
                </td>
                <td className="px-6 py-4">
                  <div className="text-sm text-gray-900">{rol.descripcion}</div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-gray-900">{rol.departamento}</div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-gray-900">{rol.nivelJerarquico}</div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-gray-900">{rol.permisos.length} permisos</div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <button
                    onClick={() => handleManagePermisos(rol)}
                    className="text-purple-600 hover:text-purple-900 mr-4"
                    title="Gestionar Permisos"
                  >
                    <Key className="w-5 h-5" />
                  </button>
                  <button
                    onClick={() => handleEdit(rol)}
                    className="text-blue-600 hover:text-blue-900 mr-4"
                    title="Editar"
                  >
                    <Edit className="w-5 h-5" />
                  </button>
                  <button
                    onClick={() => handleDelete(rol.id)}
                    className="text-red-600 hover:text-red-900"
                    title="Eliminar"
                  >
                    <Trash2 className="w-5 h-5" />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Modal Crear/Editar Rol */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-md w-full p-6">
            <h2 className="text-xl font-bold mb-4">
              {editingRol ? 'Editar Rol' : 'Nuevo Rol'}
            </h2>
            <form onSubmit={handleSubmit}>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Nombre *
                  </label>
                  <input
                    type="text"
                    value={formData.nombre}
                    onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                    maxLength={100}
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Descripción *
                  </label>
                  <textarea
                    value={formData.descripcion}
                    onChange={(e) => setFormData({ ...formData, descripcion: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    rows={3}
                    required
                    maxLength={500}
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Departamento *
                  </label>
                  <input
                    type="text"
                    value={formData.departamento}
                    onChange={(e) => setFormData({ ...formData, departamento: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                    maxLength={100}
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Nivel Jerárquico (0-10)
                  </label>
                  <input
                    type="number"
                    value={formData.nivelJerarquico}
                    onChange={(e) => setFormData({ ...formData, nivelJerarquico: parseInt(e.target.value) })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    min={0}
                    max={10}
                  />
                </div>
              </div>

              <div className="flex justify-end gap-3 mt-6">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                >
                  {editingRol ? 'Actualizar' : 'Crear'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Gestionar Permisos */}
      {showPermisosModal && selectedRolForPermisos && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-2xl w-full p-6 max-h-[80vh] overflow-y-auto">
            <h2 className="text-xl font-bold mb-4">
              Gestionar Permisos - {selectedRolForPermisos.nombre}
            </h2>
            <div className="space-y-2 mb-6">
              {permisos.map((permiso) => (
                <label
                  key={permiso.id}
                  className="flex items-center gap-3 p-3 border border-gray-200 rounded-lg hover:bg-gray-50 cursor-pointer"
                >
                  <input
                    type="checkbox"
                    checked={selectedPermisoIds.includes(permiso.id)}
                    onChange={() => togglePermiso(permiso.id)}
                    className="w-4 h-4 text-blue-600 rounded focus:ring-2 focus:ring-blue-500"
                  />
                  <div className="flex-1">
                    <div className="font-medium text-gray-900">
                      {permiso.recurso} - {permiso.accion}
                    </div>
                    <div className="text-sm text-gray-600">{permiso.descripcion}</div>
                    <div className="text-xs text-gray-500 mt-1">Scope: {permiso.scope}</div>
                  </div>
                </label>
              ))}
            </div>

            <div className="flex justify-end gap-3">
              <button
                type="button"
                onClick={() => setShowPermisosModal(false)}
                className="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={handleSavePermisos}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
              >
                Guardar Permisos
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
