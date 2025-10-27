/**
 * API Mock: Tareas
 *
 * Mock service para tareas mientras el backend está en desarrollo.
 * Simula el comportamiento de la API real con datos en memoria.
 */

import type {
  Tarea,
  TareaFormData,
  TareaListParams,
  Page,
  StatusTarea,
  TipoTarea,
  PrioridadTarea,
} from '../types/tarea.types';

// Simular delay de red
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

// Mock data storage (in-memory)
let mockTareas: Tarea[] = [
  {
    id: '1',
    unidadNegocioId: '00000000-0000-0000-0000-000000000001',
    titulo: 'Llamar a cliente ABC Corp',
    descripcion: 'Seguimiento de propuesta comercial enviada la semana pasada',
    tipo: 'LLAMADA' as TipoTarea,
    prioridad: 'ALTA' as PrioridadTarea,
    status: 'PENDIENTE' as StatusTarea,
    fechaVencimiento: '2025-10-15',
    asignadoA: 'user-001',
    asignadoNombre: 'Juan Pérez',
    relacionadoTipo: 'CLIENTE',
    relacionadoId: 'cliente-001',
    relacionadoNombre: 'ABC Corp',
    vencida: false,
    createdAt: '2025-10-10T10:00:00Z',
    createdBy: 'user-admin',
    createdByNombre: 'Admin User',
  },
  {
    id: '2',
    unidadNegocioId: '00000000-0000-0000-0000-000000000001',
    titulo: 'Preparar demo de producto',
    descripcion: 'Demo técnica para cliente potencial XYZ Solutions',
    tipo: 'TECNICA' as TipoTarea,
    prioridad: 'URGENTE' as PrioridadTarea,
    status: 'EN_PROGRESO' as StatusTarea,
    fechaVencimiento: '2025-10-14',
    asignadoA: 'user-002',
    asignadoNombre: 'María García',
    relacionadoTipo: 'OPORTUNIDAD',
    relacionadoId: 'oportunidad-005',
    relacionadoNombre: 'Proyecto XYZ',
    vencida: false,
    createdAt: '2025-10-08T14:30:00Z',
    createdBy: 'user-admin',
    createdByNombre: 'Admin User',
  },
  {
    id: '3',
    unidadNegocioId: '00000000-0000-0000-0000-000000000001',
    titulo: 'Enviar cotización a TechStart',
    descripcion: 'Cotización para licencias anuales',
    tipo: 'EMAIL' as TipoTarea,
    prioridad: 'MEDIA' as PrioridadTarea,
    status: 'COMPLETADA' as StatusTarea,
    fechaVencimiento: '2025-10-12',
    fechaCompletada: '2025-10-11T16:45:00Z',
    asignadoA: 'user-001',
    asignadoNombre: 'Juan Pérez',
    relacionadoTipo: 'OPORTUNIDAD',
    relacionadoId: 'oportunidad-003',
    relacionadoNombre: 'TechStart Licenses',
    vencida: false,
    createdAt: '2025-10-05T09:00:00Z',
    createdBy: 'user-admin',
    createdByNombre: 'Admin User',
  },
  {
    id: '4',
    unidadNegocioId: '00000000-0000-0000-0000-000000000001',
    titulo: 'Revisar contrato legal',
    descripcion: 'Revisión de términos y condiciones con el equipo legal',
    tipo: 'ADMINISTRATIVA' as TipoTarea,
    prioridad: 'ALTA' as PrioridadTarea,
    status: 'EN_PROGRESO' as StatusTarea,
    fechaVencimiento: '2025-10-16',
    asignadoA: 'user-003',
    asignadoNombre: 'Carlos López',
    vencida: false,
    createdAt: '2025-10-09T11:20:00Z',
    createdBy: 'user-admin',
    createdByNombre: 'Admin User',
  },
  {
    id: '5',
    unidadNegocioId: '00000000-0000-0000-0000-000000000001',
    titulo: 'Reunión de seguimiento mensual',
    descripcion: 'Revisión de KPIs y objetivos del equipo de ventas',
    tipo: 'REUNION' as TipoTarea,
    prioridad: 'MEDIA' as PrioridadTarea,
    status: 'PENDIENTE' as StatusTarea,
    fechaVencimiento: '2025-10-20',
    asignadoA: 'user-001',
    asignadoNombre: 'Juan Pérez',
    vencida: false,
    createdAt: '2025-10-01T08:00:00Z',
    createdBy: 'user-admin',
    createdByNombre: 'Admin User',
  },
  {
    id: '6',
    unidadNegocioId: '00000000-0000-0000-0000-000000000001',
    titulo: 'Actualizar CRM con nuevos contactos',
    descripcion: 'Cargar lista de contactos de evento networking',
    tipo: 'ADMINISTRATIVA' as TipoTarea,
    prioridad: 'BAJA' as PrioridadTarea,
    status: 'PENDIENTE' as StatusTarea,
    fechaVencimiento: '2025-10-25',
    asignadoA: 'user-002',
    asignadoNombre: 'María García',
    vencida: false,
    createdAt: '2025-10-11T13:15:00Z',
    createdBy: 'user-admin',
    createdByNombre: 'Admin User',
  },
];

// Generador de IDs únicos
let nextId = mockTareas.length + 1;

export const tareasMockApi = {
  /**
   * Lista tareas con filtros y paginación
   */
  list: async (params: TareaListParams = {}): Promise<Page<Tarea>> => {
    await delay(300); // Simular latencia de red

    const {
      page = 0,
      size = 20,
      sort = 'createdAt',
      direction = 'DESC',
      asignadoA,
      status,
      relacionadoTipo,
      relacionadoId,
      q,
    } = params;

    let filtered = [...mockTareas];

    // Aplicar filtros
    if (asignadoA) {
      filtered = filtered.filter((t) => t.asignadoA === asignadoA);
    }

    if (status) {
      filtered = filtered.filter((t) => t.status === status);
    }

    if (relacionadoTipo && relacionadoId) {
      filtered = filtered.filter(
        (t) => t.relacionadoTipo === relacionadoTipo && t.relacionadoId === relacionadoId
      );
    }

    if (q) {
      const searchLower = q.toLowerCase();
      filtered = filtered.filter(
        (t) =>
          t.titulo.toLowerCase().includes(searchLower) ||
          t.descripcion?.toLowerCase().includes(searchLower)
      );
    }

    // Ordenar
    filtered.sort((a, b) => {
      const aValue = a[sort as keyof Tarea] || '';
      const bValue = b[sort as keyof Tarea] || '';
      const comparison = aValue > bValue ? 1 : aValue < bValue ? -1 : 0;
      return direction === 'DESC' ? -comparison : comparison;
    });

    // Paginación
    const start = page * size;
    const end = start + size;
    const paginatedContent = filtered.slice(start, end);

    return {
      content: paginatedContent,
      totalElements: filtered.length,
      totalPages: Math.ceil(filtered.length / size),
      size,
      number: page,
      first: page === 0,
      last: end >= filtered.length,
    };
  },

  /**
   * Obtiene una tarea por ID
   */
  getById: async (id: string): Promise<Tarea> => {
    await delay(200);
    const tarea = mockTareas.find((t) => t.id === id);
    if (!tarea) {
      throw new Error('Tarea no encontrada');
    }
    return tarea;
  },

  /**
   * Crea una nueva tarea
   */
  create: async (tarea: TareaFormData): Promise<Tarea> => {
    await delay(400);

    const now = new Date().toISOString();
    const nuevaTarea: Tarea = {
      ...tarea,
      id: String(nextId++),
      vencida: false,
      createdAt: now,
      createdBy: 'current-user',
      createdByNombre: 'Usuario Actual',
      updatedAt: now,
    };

    mockTareas.push(nuevaTarea);
    return nuevaTarea;
  },

  /**
   * Actualiza una tarea existente
   */
  update: async (id: string, tarea: TareaFormData): Promise<Tarea> => {
    await delay(400);

    const index = mockTareas.findIndex((t) => t.id === id);
    if (index === -1) {
      throw new Error('Tarea no encontrada');
    }

    const updatedTarea: Tarea = {
      ...mockTareas[index],
      ...tarea,
      updatedAt: new Date().toISOString(),
      updatedBy: 'current-user',
    };

    mockTareas[index] = updatedTarea;
    return updatedTarea;
  },

  /**
   * Elimina una tarea (soft delete)
   */
  delete: async (id: string): Promise<void> => {
    await delay(300);
    mockTareas = mockTareas.filter((t) => t.id !== id);
  },

  /**
   * Marca una tarea como completada
   */
  completar: async (id: string): Promise<Tarea> => {
    await delay(300);

    const index = mockTareas.findIndex((t) => t.id === id);
    if (index === -1) {
      throw new Error('Tarea no encontrada');
    }

    mockTareas[index] = {
      ...mockTareas[index],
      status: 'COMPLETADA' as StatusTarea,
      fechaCompletada: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      updatedBy: 'current-user',
    };

    return mockTareas[index];
  },

  /**
   * Cancela una tarea
   */
  cancelar: async (id: string): Promise<Tarea> => {
    await delay(300);

    const index = mockTareas.findIndex((t) => t.id === id);
    if (index === -1) {
      throw new Error('Tarea no encontrada');
    }

    mockTareas[index] = {
      ...mockTareas[index],
      status: 'CANCELADA' as StatusTarea,
      updatedAt: new Date().toISOString(),
      updatedBy: 'current-user',
    };

    return mockTareas[index];
  },

  /**
   * Reasigna una tarea a otro usuario
   */
  reasignar: async (id: string, asignadoA: string): Promise<Tarea> => {
    await delay(300);

    const index = mockTareas.findIndex((t) => t.id === id);
    if (index === -1) {
      throw new Error('Tarea no encontrada');
    }

    mockTareas[index] = {
      ...mockTareas[index],
      asignadoA,
      asignadoNombre: `Usuario ${asignadoA}`, // En prod vendría del backend
      updatedAt: new Date().toISOString(),
      updatedBy: 'current-user',
    };

    return mockTareas[index];
  },

  /**
   * Lista tareas vencidas
   */
  listarVencidas: async (page = 0, size = 20): Promise<Page<Tarea>> => {
    await delay(300);

    const today = new Date().toISOString().split('T')[0];
    const vencidas = mockTareas.filter(
      (t) =>
        t.fechaVencimiento &&
        t.fechaVencimiento < today &&
        t.status !== 'COMPLETADA' &&
        t.status !== 'CANCELADA'
    );

    const start = page * size;
    const end = start + size;
    const paginatedContent = vencidas.slice(start, end);

    return {
      content: paginatedContent,
      totalElements: vencidas.length,
      totalPages: Math.ceil(vencidas.length / size),
      size,
      number: page,
      first: page === 0,
      last: end >= vencidas.length,
    };
  },

  /**
   * Lista tareas por vencer en los próximos N días
   */
  listarPorVencer: async (dias = 7, page = 0, size = 20): Promise<Page<Tarea>> => {
    await delay(300);

    const today = new Date();
    const futureDate = new Date(today);
    futureDate.setDate(futureDate.getDate() + dias);
    const futureDateStr = futureDate.toISOString().split('T')[0];

    const porVencer = mockTareas.filter(
      (t) =>
        t.fechaVencimiento &&
        t.fechaVencimiento <= futureDateStr &&
        t.status !== 'COMPLETADA' &&
        t.status !== 'CANCELADA'
    );

    const start = page * size;
    const end = start + size;
    const paginatedContent = porVencer.slice(start, end);

    return {
      content: paginatedContent,
      totalElements: porVencer.length,
      totalPages: Math.ceil(porVencer.length / size),
      size,
      number: page,
      first: page === 0,
      last: end >= porVencer.length,
    };
  },

  /**
   * Cuenta tareas por usuario y status
   */
  contarPorAsignadoYStatus: async (usuarioId: string, status: StatusTarea): Promise<number> => {
    await delay(200);
    return mockTareas.filter((t) => t.asignadoA === usuarioId && t.status === status).length;
  },

  /**
   * Encuentra tareas inactivas (sin actualizar en X días)
   */
  encontrarInactivas: async (dias = 30): Promise<Tarea[]> => {
    await delay(300);

    const cutoffDate = new Date();
    cutoffDate.setDate(cutoffDate.getDate() - dias);
    const cutoffDateStr = cutoffDate.toISOString();

    return mockTareas.filter(
      (t) =>
        t.updatedAt &&
        t.updatedAt < cutoffDateStr &&
        t.status !== 'COMPLETADA' &&
        t.status !== 'CANCELADA'
    );
  },
};
