/**
 * ProductoFormulario - Form for creating/editing products
 */

import React from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { productoSchema, type ProductoSchemaType } from '../schemas/producto.schema';
import { ProductoTipo, ProductoStatus, type Producto } from '../types/producto.types';
import { Button } from '@shared-ui/components/Button';
import { Package, DollarSign, Layers, BarChart } from 'lucide-react';

interface ProductoFormularioProps {
  producto?: Producto;
  unidadNegocioId: string;
  onSubmit: (data: ProductoSchemaType & { unidadNegocioId: string }) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
}

export const ProductoFormulario: React.FC<ProductoFormularioProps> = ({
  producto,
  unidadNegocioId,
  onSubmit,
  onCancel,
  isLoading = false,
}) => {
  const {
    register,
    handleSubmit,
    control,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<ProductoSchemaType>({
    resolver: zodResolver(productoSchema),
    defaultValues: producto || {
      codigo: '',
      nombre: '',
      descripcion: '',
      tipo: ProductoTipo.PRODUCTO,
      precioBase: 0,
      moneda: 'USD',
      costoUnitario: 0,
      status: ProductoStatus.ACTIVE,
      stockActual: 0,
      stockMinimo: 0,
      unidadMedida: 'UND',
    },
  });

  const tipo = watch('tipo');
  const precioBase = watch('precioBase');
  const costoUnitario = watch('costoUnitario') || 0;
  const margenBruto = precioBase && costoUnitario ? ((precioBase - costoUnitario) / precioBase) * 100 : 0;

  const handleFormSubmit = async (data: ProductoSchemaType) => {
    await onSubmit({ ...data, unidadNegocioId });
  };

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-6">
      {/* Información Básica */}
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <div className="flex items-center gap-2 mb-4">
          <Package className="w-5 h-5 text-blue-600" />
          <h3 className="text-lg font-semibold text-gray-900">Información Básica</h3>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Código */}
          <div>
            <label htmlFor="codigo" className="block text-sm font-medium text-gray-700 mb-1">
              Código <span className="text-red-500">*</span>
            </label>
            <input
              {...register('codigo')}
              type="text"
              id="codigo"
              placeholder="PROD-001"
              className={`w-full px-3 py-2 border rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                errors.codigo ? 'border-red-500' : 'border-gray-300'
              }`}
            />
            {errors.codigo && <p className="mt-1 text-sm text-red-600">{errors.codigo.message}</p>}
          </div>

          {/* Nombre */}
          <div className="md:col-span-1">
            <label htmlFor="nombre" className="block text-sm font-medium text-gray-700 mb-1">
              Nombre <span className="text-red-500">*</span>
            </label>
            <input
              {...register('nombre')}
              type="text"
              id="nombre"
              placeholder="Nombre del producto"
              className={`w-full px-3 py-2 border rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                errors.nombre ? 'border-red-500' : 'border-gray-300'
              }`}
            />
            {errors.nombre && <p className="mt-1 text-sm text-red-600">{errors.nombre.message}</p>}
          </div>

          {/* Descripción */}
          <div className="md:col-span-2">
            <label htmlFor="descripcion" className="block text-sm font-medium text-gray-700 mb-1">
              Descripción
            </label>
            <textarea
              {...register('descripcion')}
              id="descripcion"
              rows={3}
              placeholder="Descripción detallada del producto"
              className={`w-full px-3 py-2 border rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                errors.descripcion ? 'border-red-500' : 'border-gray-300'
              }`}
            />
            {errors.descripcion && <p className="mt-1 text-sm text-red-600">{errors.descripcion.message}</p>}
          </div>

          {/* Tipo */}
          <div>
            <label htmlFor="tipo" className="block text-sm font-medium text-gray-700 mb-1">
              Tipo <span className="text-red-500">*</span>
            </label>
            <select
              {...register('tipo')}
              id="tipo"
              className={`w-full px-3 py-2 border rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                errors.tipo ? 'border-red-500' : 'border-gray-300'
              }`}
            >
              <option value={ProductoTipo.PRODUCTO}>Producto Físico</option>
              <option value={ProductoTipo.SERVICIO}>Servicio</option>
              <option value={ProductoTipo.COMBO}>Combo/Paquete</option>
            </select>
            {errors.tipo && <p className="mt-1 text-sm text-red-600">{errors.tipo.message}</p>}
          </div>

          {/* Estado */}
          <div>
            <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-1">
              Estado <span className="text-red-500">*</span>
            </label>
            <select
              {...register('status')}
              id="status"
              className={`w-full px-3 py-2 border rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                errors.status ? 'border-red-500' : 'border-gray-300'
              }`}
            >
              <option value={ProductoStatus.ACTIVE}>Activo</option>
              <option value={ProductoStatus.INACTIVE}>Inactivo</option>
              <option value={ProductoStatus.DISCONTINUED}>Descontinuado</option>
            </select>
            {errors.status && <p className="mt-1 text-sm text-red-600">{errors.status.message}</p>}
          </div>
        </div>
      </div>

      {/* Precios */}
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <div className="flex items-center gap-2 mb-4">
          <DollarSign className="w-5 h-5 text-green-600" />
          <h3 className="text-lg font-semibold text-gray-900">Precios y Costos</h3>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Precio Base */}
          <div>
            <label htmlFor="precioBase" className="block text-sm font-medium text-gray-700 mb-1">
              Precio de Venta <span className="text-red-500">*</span>
            </label>
            <Controller
              name="precioBase"
              control={control}
              render={({ field }) => (
                <input
                  {...field}
                  type="number"
                  step="0.01"
                  min="0"
                  id="precioBase"
                  placeholder="0.00"
                  onChange={(e) => field.onChange(parseFloat(e.target.value) || 0)}
                  className={`w-full px-3 py-2 border rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                    errors.precioBase ? 'border-red-500' : 'border-gray-300'
                  }`}
                />
              )}
            />
            {errors.precioBase && <p className="mt-1 text-sm text-red-600">{errors.precioBase.message}</p>}
          </div>

          {/* Moneda */}
          <div>
            <label htmlFor="moneda" className="block text-sm font-medium text-gray-700 mb-1">
              Moneda <span className="text-red-500">*</span>
            </label>
            <input
              {...register('moneda')}
              type="text"
              id="moneda"
              placeholder="USD"
              maxLength={3}
              className={`w-full px-3 py-2 border rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 uppercase ${
                errors.moneda ? 'border-red-500' : 'border-gray-300'
              }`}
            />
            {errors.moneda && <p className="mt-1 text-sm text-red-600">{errors.moneda.message}</p>}
          </div>

          {/* Costo Unitario */}
          <div>
            <label htmlFor="costoUnitario" className="block text-sm font-medium text-gray-700 mb-1">
              Costo Unitario
            </label>
            <Controller
              name="costoUnitario"
              control={control}
              render={({ field }) => (
                <input
                  {...field}
                  type="number"
                  step="0.01"
                  min="0"
                  id="costoUnitario"
                  placeholder="0.00"
                  onChange={(e) => field.onChange(parseFloat(e.target.value) || 0)}
                  className={`w-full px-3 py-2 border rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                    errors.costoUnitario ? 'border-red-500' : 'border-gray-300'
                  }`}
                />
              )}
            />
            {errors.costoUnitario && <p className="mt-1 text-sm text-red-600">{errors.costoUnitario.message}</p>}
          </div>

          {/* Margen Bruto (calculado) */}
          {precioBase > 0 && costoUnitario > 0 && (
            <div className="md:col-span-3 mt-2">
              <div className="bg-blue-50 border border-blue-200 rounded-md p-3">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-blue-900">Margen Bruto:</span>
                  <span className={`text-lg font-bold ${margenBruto >= 30 ? 'text-green-600' : margenBruto >= 15 ? 'text-yellow-600' : 'text-red-600'}`}>
                    {margenBruto.toFixed(2)}%
                  </span>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Inventario (solo para productos físicos) */}
      {tipo === ProductoTipo.PRODUCTO && (
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <div className="flex items-center gap-2 mb-4">
            <Layers className="w-5 h-5 text-purple-600" />
            <h3 className="text-lg font-semibold text-gray-900">Inventario</h3>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {/* Stock Actual */}
            <div>
              <label htmlFor="stockActual" className="block text-sm font-medium text-gray-700 mb-1">
                Stock Actual
              </label>
              <Controller
                name="stockActual"
                control={control}
                render={({ field }) => (
                  <input
                    {...field}
                    type="number"
                    min="0"
                    id="stockActual"
                    placeholder="0"
                    onChange={(e) => field.onChange(parseInt(e.target.value) || 0)}
                    className={`w-full px-3 py-2 border rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                      errors.stockActual ? 'border-red-500' : 'border-gray-300'
                    }`}
                  />
                )}
              />
              {errors.stockActual && <p className="mt-1 text-sm text-red-600">{errors.stockActual.message}</p>}
            </div>

            {/* Stock Mínimo */}
            <div>
              <label htmlFor="stockMinimo" className="block text-sm font-medium text-gray-700 mb-1">
                Stock Mínimo
              </label>
              <Controller
                name="stockMinimo"
                control={control}
                render={({ field }) => (
                  <input
                    {...field}
                    type="number"
                    min="0"
                    id="stockMinimo"
                    placeholder="0"
                    onChange={(e) => field.onChange(parseInt(e.target.value) || 0)}
                    className={`w-full px-3 py-2 border rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                      errors.stockMinimo ? 'border-red-500' : 'border-gray-300'
                    }`}
                  />
                )}
              />
              {errors.stockMinimo && <p className="mt-1 text-sm text-red-600">{errors.stockMinimo.message}</p>}
            </div>

            {/* Unidad de Medida */}
            <div>
              <label htmlFor="unidadMedida" className="block text-sm font-medium text-gray-700 mb-1">
                Unidad de Medida
              </label>
              <input
                {...register('unidadMedida')}
                type="text"
                id="unidadMedida"
                placeholder="UND, KG, LTS..."
                className={`w-full px-3 py-2 border rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 uppercase ${
                  errors.unidadMedida ? 'border-red-500' : 'border-gray-300'
                }`}
              />
              {errors.unidadMedida && <p className="mt-1 text-sm text-red-600">{errors.unidadMedida.message}</p>}
            </div>
          </div>
        </div>
      )}

      {/* Datos Adicionales */}
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <div className="flex items-center gap-2 mb-4">
          <BarChart className="w-5 h-5 text-indigo-600" />
          <h3 className="text-lg font-semibold text-gray-900">Datos Adicionales</h3>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* SKU */}
          <div>
            <label htmlFor="sku" className="block text-sm font-medium text-gray-700 mb-1">
              SKU
            </label>
            <input
              {...register('sku')}
              type="text"
              id="sku"
              placeholder="SKU-12345"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          {/* Código de Barras */}
          <div>
            <label htmlFor="codigoBarras" className="block text-sm font-medium text-gray-700 mb-1">
              Código de Barras
            </label>
            <input
              {...register('codigoBarras')}
              type="text"
              id="codigoBarras"
              placeholder="123456789012"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          {/* Peso */}
          {tipo === ProductoTipo.PRODUCTO && (
            <div>
              <label htmlFor="pesoKg" className="block text-sm font-medium text-gray-700 mb-1">
                Peso (KG)
              </label>
              <Controller
                name="pesoKg"
                control={control}
                render={({ field }) => (
                  <input
                    {...field}
                    type="number"
                    step="0.001"
                    min="0"
                    id="pesoKg"
                    placeholder="0.000"
                    onChange={(e) => field.onChange(parseFloat(e.target.value) || 0)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  />
                )}
              />
            </div>
          )}

          {/* Imagen URL */}
          <div className={tipo === ProductoTipo.PRODUCTO ? 'md:col-span-1' : 'md:col-span-2'}>
            <label htmlFor="imagenUrl" className="block text-sm font-medium text-gray-700 mb-1">
              URL de Imagen
            </label>
            <input
              {...register('imagenUrl')}
              type="url"
              id="imagenUrl"
              placeholder="https://ejemplo.com/imagen.jpg"
              className={`w-full px-3 py-2 border rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                errors.imagenUrl ? 'border-red-500' : 'border-gray-300'
              }`}
            />
            {errors.imagenUrl && <p className="mt-1 text-sm text-red-600">{errors.imagenUrl.message}</p>}
          </div>
        </div>
      </div>

      {/* Acciones */}
      <div className="flex justify-end gap-3 pt-4 border-t border-gray-200">
        <Button type="button" variant="outline" onClick={onCancel} disabled={isSubmitting || isLoading}>
          Cancelar
        </Button>
        <Button type="submit" variant="primary" disabled={isSubmitting || isLoading}>
          {isSubmitting || isLoading ? 'Guardando...' : producto ? 'Actualizar Producto' : 'Crear Producto'}
        </Button>
      </div>
    </form>
  );
};
