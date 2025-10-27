/**
 * Feature: Oportunidades
 *
 * Módulo de gestión de oportunidades de venta.
 */

// Components
export { OportunidadFormulario } from './components/OportunidadFormulario';
export { OportunidadModal } from './components/OportunidadModal';

// API
export { oportunidadesApi } from './api/oportunidades.api';

// Types
export type {
  Oportunidad,
  OportunidadFormData,
  OportunidadListParams,
  Page,
} from './types/oportunidad.types';
