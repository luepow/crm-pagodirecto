/**
 * Página de gestión de Permisos
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */

import React, { useState, useEffect } from 'react';
import { toast } from 'sonner';
import { Key, Plus, Edit, Trash2 } from 'lucide-react';
import { permisoApi } from '../api/permiso.api';
import { PermisoDTO, CreatePermisoRequest, UpdatePermisoRequest, AccionType } from '../types/permiso.types';

const ACCIONES: AccionType[] = ['CREATE', 'READ', 'UPDATE', 'DELETE', 'EXECUTE', 'ADMIN'];

export const PermisosPage: React.FC = () => {
  const [permisos, setPermisos] = useState<PermisoDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingPermiso, setEditingPermiso] = useState<PermisoDTO | null>(null);

  // Form state
  const [formData, setFormData] = useState<CreatePermisoRequest>({
    recurso: '',
    accion: 'READ',
    scope: '',
    descripcion: '',
  });

  const [updateFormData, setUpdateFormData] = useState<UpdatePermisoRequest>({
    scope: '',
    descripcion: '',
  });

  useEffect(() => {
    loadPermisos();
  }, []);

  const loadPermisos = async () => {
    try {
      setLoading(true);
      const data = await permisoApi.getAllPermisos();
      setPermisos(data);
    } catch (error: any) {
      toast.error('Error al cargar permisos', {
        description: error.response?.data?.message || error.message,
      });
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingPermiso(null);
    setFormData({
      recurso: '',
      accion: 'READ',
      scope: '',
      descripcion: '',
    });
    setShowModal(true);
  };

  const handleEdit = (permiso: PermisoDTO) => {
    setEditingPermiso(permiso);
    setUpdateFormData({
      scope: permiso.scope,
      descripcion: permiso.descripcion,
    });
    setShowModal(true);
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('¿Está seguro de eliminar este permiso?')) return;

    try {
      await permisoApi.deletePermiso(id);
      toast.success('Permiso eliminado exitosamente');
      loadPermisos();
    } catch (error: any) {
      toast.error('Error al eliminar permiso', {
        description: error.response?.data?.message || error.message,
      });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      if (editingPermiso) {
        await permisoApi.updatePermiso(editingPermiso.id, updateFormData);
        toast.success('Permiso actualizado exitosamente');
      } else {
        await permisoApi.createPermiso(formData);
        toast.success('Permiso creado exitosamente');
      }
      setShowModal(false);
      loadPermisos();
    } catch (error: any) {
      toast.error(editingPermiso ? 'Error al actualizar permiso' : 'Error al crear permiso', {
        description: error.response?.data?.message || error.message,
      });
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-lg">Cargando permisos...</div>
      </div>
    );
  }

  return (
    <div className="p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <Key className="w-8 h-8 text-purple-600" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Gestión de Permisos</h1>
            <p className="text-sm text-gray-600">Administre permisos granulares del sistema</p>
          </div>
        </div>
        <button
          onClick={handleCreate}
          className="flex items-center gap-2 px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
        >
          <Plus className="w-5 h-5" />
          Nuevo Permiso
        </button>
      </div>

      {/* Tabla de Permisos */}
      <div className="bg-white rounded-lg shadow">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Recurso
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Acción
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Scope
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Descripción
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Acciones
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {permisos.map((permiso) => (
              <tr key={permiso.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm font-medium text-gray-900">{permiso.recurso}</div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800">
                    {permiso.accion}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-gray-900">{permiso.scope}</div>
                </td>
                <td className="px-6 py-4">
                  <div className="text-sm text-gray-900">{permiso.descripcion}</div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <button
                    onClick={() => handleEdit(permiso)}
                    className="text-blue-600 hover:text-blue-900 mr-4"
                    title="Editar"
                  >
                    <Edit className="w-5 h-5" />
                  </button>
                  <button
                    onClick={() => handleDelete(permiso.id)}
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

      {/* Modal Crear/Editar Permiso */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-md w-full p-6">
            <h2 className="text-xl font-bold mb-4">
              {editingPermiso ? 'Editar Permiso' : 'Nuevo Permiso'}
            </h2>
            <form onSubmit={handleSubmit}>
              <div className="space-y-4">
                {!editingPermiso && (
                  <>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Recurso *
                      </label>
                      <input
                        type="text"
                        value={formData.recurso}
                        onChange={(e) => setFormData({ ...formData, recurso: e.target.value })}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                        required
                        maxLength={100}
                        placeholder="ej: clientes, ventas, productos"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Acción *
                      </label>
                      <select
                        value={formData.accion}
                        onChange={(e) => setFormData({ ...formData, accion: e.target.value as AccionType })}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                        required
                      >
                        {ACCIONES.map((accion) => (
                          <option key={accion} value={accion}>
                            {accion}
                          </option>
                        ))}
                      </select>
                    </div>
                  </>
                )}

                {editingPermiso && (
                  <div className="bg-gray-50 p-4 rounded-lg mb-4">
                    <div className="text-sm text-gray-600">
                      <div className="mb-2">
                        <span className="font-medium">Recurso:</span> {editingPermiso.recurso}
                      </div>
                      <div>
                        <span className="font-medium">Acción:</span>{' '}
                        <span className="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800">
                          {editingPermiso.accion}
                        </span>
                      </div>
                    </div>
                  </div>
                )}

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Scope *
                  </label>
                  <input
                    type="text"
                    value={editingPermiso ? updateFormData.scope : formData.scope}
                    onChange={(e) =>
                      editingPermiso
                        ? setUpdateFormData({ ...updateFormData, scope: e.target.value })
                        : setFormData({ ...formData, scope: e.target.value })
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                    required
                    maxLength={100}
                    placeholder="ej: clients:read, sales:write"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Descripción *
                  </label>
                  <textarea
                    value={editingPermiso ? updateFormData.descripcion : formData.descripcion}
                    onChange={(e) =>
                      editingPermiso
                        ? setUpdateFormData({ ...updateFormData, descripcion: e.target.value })
                        : setFormData({ ...formData, descripcion: e.target.value })
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                    rows={3}
                    required
                    maxLength={500}
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
                  className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
                >
                  {editingPermiso ? 'Actualizar' : 'Crear'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
