/**
 * Producto Validation Schema - Zod
 */

import { z } from 'zod';
import { ProductoTipo, ProductoStatus } from '../types/producto.types';

export const productoSchema = z.object({
  codigo: z
    .string()
    .min(1, 'El código es requerido')
    .max(50, 'El código no puede exceder 50 caracteres')
    .regex(/^[A-Z0-9-]+$/, 'El código solo puede contener letras mayúsculas, números y guiones'),

  nombre: z
    .string()
    .min(1, 'El nombre es requerido')
    .max(200, 'El nombre no puede exceder 200 caracteres'),

  descripcion: z
    .string()
    .max(1000, 'La descripción no puede exceder 1000 caracteres')
    .optional()
    .or(z.literal('')),

  categoriaId: z.string().uuid('ID de categoría inválido').optional().or(z.literal('')),

  tipo: z.nativeEnum(ProductoTipo, {
    errorMap: () => ({ message: 'Tipo de producto inválido' }),
  }),

  precioBase: z
    .number({
      required_error: 'El precio es requerido',
      invalid_type_error: 'El precio debe ser un número',
    })
    .positive('El precio debe ser mayor a 0')
    .max(999999999.99, 'El precio es demasiado alto'),

  moneda: z
    .string()
    .length(3, 'La moneda debe ser un código ISO de 3 letras')
    .regex(/^[A-Z]{3}$/, 'Formato de moneda inválido (ej: USD, EUR, VES)')
    .default('USD'),

  costoUnitario: z
    .number({
      invalid_type_error: 'El costo debe ser un número',
    })
    .nonnegative('El costo no puede ser negativo')
    .max(999999999.99, 'El costo es demasiado alto')
    .optional()
    .or(z.literal(0)),

  status: z.nativeEnum(ProductoStatus, {
    errorMap: () => ({ message: 'Estado inválido' }),
  }),

  stockActual: z
    .number({
      invalid_type_error: 'El stock debe ser un número',
    })
    .int('El stock debe ser un número entero')
    .nonnegative('El stock no puede ser negativo')
    .max(999999999, 'El stock es demasiado alto')
    .optional()
    .or(z.literal(0)),

  stockMinimo: z
    .number({
      invalid_type_error: 'El stock mínimo debe ser un número',
    })
    .int('El stock mínimo debe ser un número entero')
    .nonnegative('El stock mínimo no puede ser negativo')
    .max(999999999, 'El stock mínimo es demasiado alto')
    .optional()
    .or(z.literal(0)),

  unidadMedida: z
    .string()
    .max(20, 'La unidad de medida no puede exceder 20 caracteres')
    .optional()
    .or(z.literal('')),

  pesoKg: z
    .number({
      invalid_type_error: 'El peso debe ser un número',
    })
    .nonnegative('El peso no puede ser negativo')
    .max(999999.999, 'El peso es demasiado alto')
    .optional()
    .or(z.literal(0)),

  sku: z
    .string()
    .max(100, 'El SKU no puede exceder 100 caracteres')
    .optional()
    .or(z.literal('')),

  codigoBarras: z
    .string()
    .max(50, 'El código de barras no puede exceder 50 caracteres')
    .optional()
    .or(z.literal('')),

  imagenUrl: z
    .string()
    .url('Debe ser una URL válida')
    .max(500, 'La URL no puede exceder 500 caracteres')
    .optional()
    .or(z.literal('')),
}).refine(
  (data) => {
    // Validar que el costo unitario no sea mayor al precio base
    if (data.costoUnitario && data.precioBase && data.costoUnitario > data.precioBase) {
      return false;
    }
    return true;
  },
  {
    message: 'El costo unitario no puede ser mayor al precio de venta',
    path: ['costoUnitario'],
  }
);

export type ProductoSchemaType = z.infer<typeof productoSchema>;
