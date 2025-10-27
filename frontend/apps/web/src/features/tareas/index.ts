/**
 * Feature: Tareas
 *
 * Módulo de gestión de tareas y actividades.
 */

// Components
export { TareaFormulario } from './components/TareaFormulario';
export { TareaModal } from './components/TareaModal';

// API
export { tareasApi } from './api/tareas.api';

// Types
export type {
  Tarea,
  TareaFormData,
  TareaListParams,
  TareaStats,
  Page,
} from './types/tarea.types';

export { TipoTarea, PrioridadTarea, StatusTarea } from './types/tarea.types';
