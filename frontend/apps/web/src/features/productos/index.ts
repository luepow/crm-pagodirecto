/**
 * Feature: Productos
 */

export { productosApi } from './api/productos.api';
export type { Producto, ProductoFormData, ProductoListParams, Page, ProductoStatus } from './types/producto.types';
export { ProductoTipo, ProductoStatus as ProductoStatusEnum } from './types/producto.types';
export { ProductoFormulario } from './components/ProductoFormulario';
export { ProductoDialog } from './components/ProductoDialog';
export { DeleteConfirmDialog } from './components/DeleteConfirmDialog';
export { productoSchema, type ProductoSchemaType } from './schemas/producto.schema';
