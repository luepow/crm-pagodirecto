/**
 * Productos Page - Product catalog management with full CRUD
 */

import React, { useState, useEffect } from 'react';
import { Plus, Package, AlertTriangle, Search, Edit, Trash2, Filter, RefreshCw } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@shared-ui/components/Card';
import { Button } from '@shared-ui/components/Button';
import { Badge } from '@shared-ui/components/Badge';
import { TableSkeleton } from '@shared-ui/components/Skeleton';
import {
  productosApi,
  ProductoDialog,
  DeleteConfirmDialog,
  type Producto,
  type ProductoStatus,
  type ProductoSchemaType,
  ProductoStatusEnum,
} from '../../features/productos';
import { toast } from 'sonner';

// Mock unidad de negocio ID (en producción vendría del contexto de usuario)
const MOCK_UNIDAD_NEGOCIO_ID = 'eb545d33-0c5e-4c8f-be21-f92b9ffeb94a';

export const ProductosPage: React.FC = () => {
  const [isLoading, setIsLoading] = useState(true);
  const [productos, setProductos] = useState<Producto[]>([]);
  const [filteredProductos, setFilteredProductos] = useState<Producto[]>([]);

  // State para modales
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [selectedProducto, setSelectedProducto] = useState<Producto | undefined>();
  const [productoToDelete, setProductoToDelete] = useState<Producto | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // State para filtros
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<ProductoStatus | 'ALL'>('ALL');

  useEffect(() => {
    loadProductos();
  }, []);

  useEffect(() => {
    filterProductos();
  }, [productos, searchTerm, statusFilter]);

  const loadProductos = async () => {
    try {
      setIsLoading(true);
      const response = await productosApi.list({ size: 100 });
      setProductos(response.content);
    } catch (error) {
      console.error('Error cargando productos:', error);
      toast.error('Error al cargar los productos');
    } finally {
      setIsLoading(false);
    }
  };

  const filterProductos = () => {
    let filtered = [...productos];

    // Filtrar por búsqueda
    if (searchTerm) {
      const search = searchTerm.toLowerCase();
      filtered = filtered.filter(
        (p) =>
          p.nombre.toLowerCase().includes(search) ||
          p.codigo.toLowerCase().includes(search) ||
          p.descripcion?.toLowerCase().includes(search)
      );
    }

    // Filtrar por status
    if (statusFilter !== 'ALL') {
      filtered = filtered.filter((p) => p.status === statusFilter);
    }

    setFilteredProductos(filtered);
  };

  const handleOpenDialog = (producto?: Producto) => {
    setSelectedProducto(producto);
    setIsDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setIsDialogOpen(false);
    setSelectedProducto(undefined);
  };

  const handleSubmit = async (data: ProductoSchemaType & { unidadNegocioId: string }) => {
    try {
      setIsSubmitting(true);

      if (selectedProducto?.id) {
        // Actualizar
        await productosApi.update(selectedProducto.id, data);
        toast.success('Producto actualizado exitosamente');
      } else {
        // Crear
        await productosApi.create(data);
        toast.success('Producto creado exitosamente');
      }

      handleCloseDialog();
      await loadProductos();
    } catch (error: any) {
      console.error('Error guardando producto:', error);
      const errorMessage = error.response?.data?.message || 'Error al guardar el producto';
      toast.error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleOpenDeleteDialog = (producto: Producto) => {
    setProductoToDelete(producto);
    setIsDeleteDialogOpen(true);
  };

  const handleCloseDeleteDialog = () => {
    setIsDeleteDialogOpen(false);
    setProductoToDelete(null);
  };

  const handleDelete = async () => {
    if (!productoToDelete?.id) return;

    try {
      setIsSubmitting(true);
      await productosApi.delete(productoToDelete.id);
      toast.success('Producto eliminado exitosamente');
      handleCloseDeleteDialog();
      await loadProductos();
    } catch (error: any) {
      console.error('Error eliminando producto:', error);
      const errorMessage = error.response?.data?.message || 'Error al eliminar el producto';
      toast.error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const getStatusBadge = (status: ProductoStatus) => {
    const config = {
      ACTIVE: { label: 'Activo', variant: 'success' as const },
      INACTIVE: { label: 'Inactivo', variant: 'default' as const },
      DISCONTINUED: { label: 'Descontinuado', variant: 'error' as const },
    };
    return <Badge variant={config[status].variant}>{config[status].label}</Badge>;
  };

  const formatCurrency = (value: number, currency: string) => {
    return new Intl.NumberFormat('es-VE', {
      style: 'currency',
      currency: currency || 'USD',
    }).format(value);
  };

  const statsActivos = productos.filter((p) => p.status === 'ACTIVE').length;
  const statsStockBajo = productos.filter((p) => p.requiereReabastecimiento).length;
  const statsTotal = productos.length;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-text-primary">Productos</h1>
          <p className="mt-1 text-text-secondary">
            Catálogo de productos y servicios ({filteredProductos.length} de {statsTotal})
          </p>
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            leftIcon={<RefreshCw size={18} />}
            onClick={loadProductos}
            disabled={isLoading}
          >
            Actualizar
          </Button>
          <Button
            variant="primary"
            leftIcon={<Plus size={18} />}
            onClick={() => handleOpenDialog()}
          >
            Nuevo Producto
          </Button>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <Card>
          <CardContent padding="md">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-green-100 rounded-lg">
                <Package className="w-6 h-6 text-green-600" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Productos Activos</p>
                <p className="text-2xl font-bold text-gray-900">{statsActivos}</p>
              </div>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent padding="md">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-yellow-100 rounded-lg">
                <AlertTriangle className="w-6 h-6 text-yellow-600" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Stock Bajo</p>
                <p className="text-2xl font-bold text-gray-900">{statsStockBajo}</p>
              </div>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent padding="md">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-blue-100 rounded-lg">
                <Package className="w-6 h-6 text-blue-600" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Total Productos</p>
                <p className="text-2xl font-bold text-gray-900">{statsTotal}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Filtros */}
      <Card>
        <CardContent padding="md">
          <div className="flex flex-col sm:flex-row gap-4">
            {/* Búsqueda */}
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                <input
                  type="text"
                  placeholder="Buscar por nombre, código o descripción..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
            </div>

            {/* Filtro por Estado */}
            <div className="w-full sm:w-48">
              <div className="relative">
                <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                <select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value as ProductoStatus | 'ALL')}
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 appearance-none"
                >
                  <option value="ALL">Todos los estados</option>
                  <option value={ProductoStatusEnum.ACTIVE}>Activos</option>
                  <option value={ProductoStatusEnum.INACTIVE}>Inactivos</option>
                  <option value={ProductoStatusEnum.DISCONTINUED}>Descontinuados</option>
                </select>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Products List */}
      <Card>
        <CardHeader>
          <CardTitle>Lista de Productos</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <TableSkeleton rows={5} />
          ) : filteredProductos.length === 0 ? (
            <div className="text-center py-12">
              <Package className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900">
                {searchTerm || statusFilter !== 'ALL' ? 'No se encontraron productos' : 'No hay productos'}
              </h3>
              <p className="mt-1 text-sm text-gray-500">
                {searchTerm || statusFilter !== 'ALL'
                  ? 'Intenta cambiar los filtros de búsqueda'
                  : 'Comienza agregando un producto al catálogo'}
              </p>
              {!searchTerm && statusFilter === 'ALL' && (
                <div className="mt-6">
                  <Button variant="primary" leftIcon={<Plus size={18} />} onClick={() => handleOpenDialog()}>
                    Nuevo Producto
                  </Button>
                </div>
              )}
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
                      Precio
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Stock
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Estado
                    </th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Acciones
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {filteredProductos.map((producto) => (
                    <tr key={producto.id} className="hover:bg-gray-50 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="text-sm font-mono font-medium text-gray-900">{producto.codigo}</span>
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm font-medium text-gray-900">{producto.nombre}</div>
                        {producto.descripcion && (
                          <div className="text-sm text-gray-500 truncate max-w-xs">{producto.descripcion}</div>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="text-sm text-gray-900 font-semibold">
                          {formatCurrency(producto.precioBase, producto.moneda)}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span
                          className={`text-sm ${
                            producto.requiereReabastecimiento
                              ? 'text-red-600 font-semibold'
                              : 'text-gray-900'
                          }`}
                        >
                          {producto.stockActual ?? 0}
                          {producto.stockMinimo && (
                            <span className="text-xs text-gray-500 ml-1">/ {producto.stockMinimo} mín</span>
                          )}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {getStatusBadge(producto.status as ProductoStatus)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() => handleOpenDialog(producto)}
                            className="text-blue-600 hover:text-blue-900 transition-colors p-1"
                            title="Editar"
                          >
                            <Edit size={18} />
                          </button>
                          <button
                            onClick={() => handleOpenDeleteDialog(producto)}
                            className="text-red-600 hover:text-red-900 transition-colors p-1"
                            title="Eliminar"
                          >
                            <Trash2 size={18} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Dialogs */}
      <ProductoDialog
        open={isDialogOpen}
        producto={selectedProducto}
        unidadNegocioId={MOCK_UNIDAD_NEGOCIO_ID}
        onClose={handleCloseDialog}
        onSubmit={handleSubmit}
        isLoading={isSubmitting}
      />

      <DeleteConfirmDialog
        open={isDeleteDialogOpen}
        productName={productoToDelete?.nombre || ''}
        onConfirm={handleDelete}
        onCancel={handleCloseDeleteDialog}
        isDeleting={isSubmitting}
      />
    </div>
  );
};
