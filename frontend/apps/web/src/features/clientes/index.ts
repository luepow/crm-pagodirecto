/**
 * Feature: Clientes
 *
 * Módulo de gestión de clientes empresariales.
 */

// Components
export { ClientesPage } from './components/ClientesPage';
export { ClienteImportador } from './components/ClienteImportador';
export { ClienteFormulario } from './components/ClienteFormulario';
export { ClienteModal } from './components/ClienteModal';

// API
export { clientesApi } from './api/clientes.api';

// Types
export type {
  Cliente,
  ClienteFormData,
  ClienteStatus,
  ClienteTipo,
  ImportacionResult,
  ClienteListParams,
  Page,
} from './types/cliente.types';

export { ClienteStatus as ClienteStatusEnum, ClienteTipo as ClienteTipoEnum } from './types/cliente.types';
