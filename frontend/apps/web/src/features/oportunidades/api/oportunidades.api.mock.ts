/**
 * API Mock: Oportunidades
 *
 * Mock service para oportunidades mientras el backend está en desarrollo.
 * Simula el comportamiento de la API real con datos en memoria.
 */

import type {
  Oportunidad,
  OportunidadFormData,
  OportunidadListParams,
  Page,
} from '../types/oportunidad.types';

// Simular delay de red
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

// Mock data storage (in-memory)
let mockOportunidades: Oportunidad[] = [
  {
    id: '1',
    unidadNegocioId: '00000000-0000-0000-0000-000000000001',
    clienteId: 'cliente-001',
    clienteNombre: 'ABC Corp',
    titulo: 'Implementación ERP Completo',
    descripcion: 'Proyecto de implementación de sistema ERP para gestión integral de operaciones',
    valorEstimado: 150000,
    moneda: 'USD',
    probabilidad: 75,
    valorPonderado: 112500,
    etapaId: 'etapa-negociacion',
    etapaNombre: 'Negociación',
    fechaCierreEstimada: '2025-11-15',
    propietarioId: 'user-001',
    propietarioNombre: 'Juan Pérez',
    fuente: 'Referido',
    createdAt: '2025-09-15T10:00:00Z',
    createdBy: 'user-admin',
  },
  {
    id: '2',
    unidadNegocioId: '00000000-0000-0000-0000-000000000001',
    clienteId: 'cliente-002',
    clienteNombre: 'TechStart Solutions',
    titulo: 'Licencias de Software Anuales',
    descripcion: 'Renovación de licencias y expansión de usuarios',
    valorEstimado: 45000,
    moneda: 'USD',
    probabilidad: 90,
    valorPonderado: 40500,
    etapaId: 'etapa-propuesta',
    etapaNombre: 'Propuesta Enviada',
    fechaCierreEstimada: '2025-10-25',
    propietarioId: 'user-001',
    propietarioNombre: 'Juan Pérez',
    fuente: 'Cliente Existente',
    createdAt: '2025-10-01T14:30:00Z',
    createdBy: 'user-admin',
  },
  {
    id: '3',
    unidadNegocioId: '00000000-0000-0000-0000-000000000001',
    clienteId: 'cliente-003',
    clienteNombre: 'InnovateCo',
    titulo: 'Consultoría Digital',
    descripcion: 'Proyecto de transformación digital y migración a la nube',
    valorEstimado: 85000,
    moneda: 'USD',
    probabilidad: 60,
    valorPonderado: 51000,
    etapaId: 'etapa-calificacion',
    etapaNombre: 'Calificación',
    fechaCierreEstimada: '2025-12-01',
    propietarioId: 'user-002',
    propietarioNombre: 'María García',
    fuente: 'Marketing Digital',
    createdAt: '2025-10-05T09:00:00Z',
    createdBy: 'user-admin',
  },
  {
    id: '4',
    unidadNegocioId: '00000000-0000-0000-0000-000000000001',
    clienteId: 'cliente-004',
    clienteNombre: 'GlobalTrade Inc',
    titulo: 'Módulo de Inventario',
    descripcion: 'Implementación de módulo de gestión de inventario y almacenes',
    valorEstimado: 62000,
    moneda: 'USD',
    probabilidad: 45,
    valorPonderado: 27900,
    etapaId: 'etapa-descubrimiento',
    etapaNombre: 'Descubrimiento',
    fechaCierreEstimada: '2025-11-30',
    propietarioId: 'user-003',
    propietarioNombre: 'Carlos López',
    fuente: 'Cold Call',
    createdAt: '2025-10-08T11:20:00Z',
    createdBy: 'user-admin',
  },
  {
    id: '5',
    unidadNegocioId: '00000000-0000-0000-0000-000000000001',
    clienteId: 'cliente-005',
    clienteNombre: 'RetailMax',
    titulo: 'Sistema POS para Retail',
    descripcion: 'Implementación de sistema punto de venta para cadena de tiendas',
    valorEstimado: 120000,
    moneda: 'USD',
    probabilidad: 80,
    valorPonderado: 96000,
    etapaId: 'etapa-propuesta',
    etapaNombre: 'Propuesta Enviada',
    fechaCierreEstimada: '2025-10-30',
    propietarioId: 'user-002',
    propietarioNombre: 'María García',
    fuente: 'Evento/Feria',
    createdAt: '2025-09-20T15:45:00Z',
    createdBy: 'user-admin',
  },
  {
    id: '6',
    unidadNegocioId: '00000000-0000-0000-0000-000000000001',
    clienteId: 'cliente-006',
    clienteNombre: 'HealthCare Plus',
    titulo: 'Sistema de Gestión Hospitalaria',
    descripcion: 'Plataforma integral para gestión de pacientes, citas y facturación médica',
    valorEstimado: 200000,
    moneda: 'USD',
    probabilidad: 50,
    valorPonderado: 100000,
    etapaId: 'etapa-calificacion',
    etapaNombre: 'Calificación',
    fechaCierreEstimada: '2026-01-15',
    propietarioId: 'user-001',
    propietarioNombre: 'Juan Pérez',
    fuente: 'Licitación',
    createdAt: '2025-10-10T08:30:00Z',
    createdBy: 'user-admin',
  },
];

// Generador de IDs únicos
let nextId = mockOportunidades.length + 1;

export const oportunidadesMockApi = {
  /**
   * Lista oportunidades con filtros y paginación
   */
  list: async (params: OportunidadListParams = {}): Promise<Page<Oportunidad>> => {
    await delay(300); // Simular latencia de red

    const {
      page = 0,
      size = 20,
      sort = 'createdAt',
      direction = 'DESC',
      clienteId,
      etapaId,
      propietarioId,
      q,
    } = params;

    let filtered = [...mockOportunidades];

    // Aplicar filtros
    if (clienteId) {
      filtered = filtered.filter((o) => o.clienteId === clienteId);
    }

    if (etapaId) {
      filtered = filtered.filter((o) => o.etapaId === etapaId);
    }

    if (propietarioId) {
      filtered = filtered.filter((o) => o.propietarioId === propietarioId);
    }

    if (q) {
      const searchLower = q.toLowerCase();
      filtered = filtered.filter(
        (o) =>
          o.titulo.toLowerCase().includes(searchLower) ||
          o.descripcion?.toLowerCase().includes(searchLower) ||
          o.clienteNombre?.toLowerCase().includes(searchLower)
      );
    }

    // Ordenar
    filtered.sort((a, b) => {
      const aValue = a[sort as keyof Oportunidad] || '';
      const bValue = b[sort as keyof Oportunidad] || '';
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
      empty: filtered.length === 0,
    };
  },

  /**
   * Obtiene una oportunidad por ID
   */
  getById: async (id: string): Promise<Oportunidad> => {
    await delay(200);
    const oportunidad = mockOportunidades.find((o) => o.id === id);
    if (!oportunidad) {
      throw new Error('Oportunidad no encontrada');
    }
    return oportunidad;
  },

  /**
   * Crea una nueva oportunidad
   */
  create: async (oportunidad: OportunidadFormData): Promise<Oportunidad> => {
    await delay(400);

    const now = new Date().toISOString();

    // Calcular valor ponderado
    const valorPonderado = (oportunidad.valorEstimado * oportunidad.probabilidad) / 100;

    const nuevaOportunidad: Oportunidad = {
      ...oportunidad,
      id: String(nextId++),
      valorPonderado,
      // Datos desnormalizados (en producción vendrían del backend)
      clienteNombre: `Cliente ${oportunidad.clienteId}`,
      etapaNombre: `Etapa ${oportunidad.etapaId}`,
      propietarioNombre: `Usuario ${oportunidad.propietarioId}`,
      createdAt: now,
      createdBy: 'current-user',
      updatedAt: now,
      updatedBy: 'current-user',
    };

    mockOportunidades.push(nuevaOportunidad);
    return nuevaOportunidad;
  },

  /**
   * Actualiza una oportunidad existente
   */
  update: async (id: string, oportunidad: OportunidadFormData): Promise<Oportunidad> => {
    await delay(400);

    const index = mockOportunidades.findIndex((o) => o.id === id);
    if (index === -1) {
      throw new Error('Oportunidad no encontrada');
    }

    // Calcular valor ponderado
    const valorPonderado = (oportunidad.valorEstimado * oportunidad.probabilidad) / 100;

    const updatedOportunidad: Oportunidad = {
      ...mockOportunidades[index],
      ...oportunidad,
      valorPonderado,
      updatedAt: new Date().toISOString(),
      updatedBy: 'current-user',
    };

    mockOportunidades[index] = updatedOportunidad;
    return updatedOportunidad;
  },

  /**
   * Elimina una oportunidad (soft delete)
   */
  delete: async (id: string): Promise<void> => {
    await delay(300);
    mockOportunidades = mockOportunidades.filter((o) => o.id !== id);
  },

  /**
   * Actualiza la etapa de una oportunidad
   */
  actualizarEtapa: async (id: string, etapaId: string): Promise<Oportunidad> => {
    await delay(300);

    const index = mockOportunidades.findIndex((o) => o.id === id);
    if (index === -1) {
      throw new Error('Oportunidad no encontrada');
    }

    mockOportunidades[index] = {
      ...mockOportunidades[index],
      etapaId,
      etapaNombre: `Etapa ${etapaId}`,
      updatedAt: new Date().toISOString(),
      updatedBy: 'current-user',
    };

    return mockOportunidades[index];
  },

  /**
   * Marca una oportunidad como ganada
   */
  marcarComoGanada: async (id: string): Promise<Oportunidad> => {
    await delay(300);

    const index = mockOportunidades.findIndex((o) => o.id === id);
    if (index === -1) {
      throw new Error('Oportunidad no encontrada');
    }

    mockOportunidades[index] = {
      ...mockOportunidades[index],
      probabilidad: 100,
      valorPonderado: mockOportunidades[index].valorEstimado,
      fechaCierreReal: new Date().toISOString().split('T')[0],
      etapaId: 'etapa-ganada',
      etapaNombre: 'Ganada',
      updatedAt: new Date().toISOString(),
      updatedBy: 'current-user',
    };

    return mockOportunidades[index];
  },

  /**
   * Marca una oportunidad como perdida
   */
  marcarComoPerdida: async (id: string, motivo: string): Promise<Oportunidad> => {
    await delay(300);

    const index = mockOportunidades.findIndex((o) => o.id === id);
    if (index === -1) {
      throw new Error('Oportunidad no encontrada');
    }

    mockOportunidades[index] = {
      ...mockOportunidades[index],
      probabilidad: 0,
      valorPonderado: 0,
      motivoPerdida: motivo,
      fechaCierreReal: new Date().toISOString().split('T')[0],
      etapaId: 'etapa-perdida',
      etapaNombre: 'Perdida',
      updatedAt: new Date().toISOString(),
      updatedBy: 'current-user',
    };

    return mockOportunidades[index];
  },

  /**
   * Obtiene estadísticas de oportunidades
   */
  obtenerEstadisticas: async () => {
    await delay(200);

    const total = mockOportunidades.length;
    const valorTotal = mockOportunidades.reduce((sum, o) => sum + o.valorEstimado, 0);
    const valorPonderadoTotal = mockOportunidades.reduce((sum, o) => sum + (o.valorPonderado || 0), 0);
    const probabilidadPromedio = mockOportunidades.reduce((sum, o) => sum + o.probabilidad, 0) / total;

    return {
      totalOportunidades: total,
      valorTotal,
      valorPonderadoTotal,
      probabilidadPromedio,
      porEtapa: {
        descubrimiento: mockOportunidades.filter((o) => o.etapaId === 'etapa-descubrimiento').length,
        calificacion: mockOportunidades.filter((o) => o.etapaId === 'etapa-calificacion').length,
        propuesta: mockOportunidades.filter((o) => o.etapaId === 'etapa-propuesta').length,
        negociacion: mockOportunidades.filter((o) => o.etapaId === 'etapa-negociacion').length,
        ganada: mockOportunidades.filter((o) => o.etapaId === 'etapa-ganada').length,
        perdida: mockOportunidades.filter((o) => o.etapaId === 'etapa-perdida').length,
      },
    };
  },
};
