-- =====================================================
-- PagoDirecto CRM/ERP - Seed Data: Etapas de Pipeline
-- =====================================================
-- Purpose: Seed data for sales pipeline stages
-- Author: PagoDirecto Development Team
-- Version: 1.0.0
-- Date: 2025-10-16
-- =====================================================

-- =====================================================
-- ETAPAS DE PIPELINE (Sales Pipeline Stages)
-- =====================================================
-- Standard CRM pipeline stages with probability percentages
-- Tipos: LEAD, QUALIFIED, PROPOSAL, NEGOTIATION, CLOSED_WON, CLOSED_LOST
-- =====================================================

INSERT INTO oportunidades_etapas_pipeline (
    id,
    unidad_negocio_id,
    nombre,
    descripcion,
    tipo,
    orden,
    probabilidad_default,
    color,
    created_at,
    updated_at,
    created_by,
    updated_by
) VALUES

-- Etapa 1: Lead (10%)
('e1111111-0000-0000-0000-000000000001',
 '00000000-0000-0000-0000-000000000001',
 'Lead',
 'Identificación de potenciales clientes y primer contacto',
 'LEAD',
 1,
 10.00,
 '#64748b',  -- Slate gray
 NOW(),
 NOW(),
 '30000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000001'),

-- Etapa 2: Calificación (20%)
('e1111111-0000-0000-0000-000000000002',
 '00000000-0000-0000-0000-000000000001',
 'Calificado',
 'Evaluación de necesidades y presupuesto del cliente',
 'QUALIFIED',
 2,
 20.00,
 '#3b82f6',  -- Blue
 NOW(),
 NOW(),
 '30000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000001'),

-- Etapa 3: Propuesta (40%)
('e1111111-0000-0000-0000-000000000003',
 '00000000-0000-0000-0000-000000000001',
 'Propuesta',
 'Presentación de propuesta comercial y cotización',
 'PROPOSAL',
 3,
 40.00,
 '#f59e0b',  -- Amber
 NOW(),
 NOW(),
 '30000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000001'),

-- Etapa 4: Negociación (60%)
('e1111111-0000-0000-0000-000000000004',
 '00000000-0000-0000-0000-000000000001',
 'Negociación',
 'Negociación de términos, precios y condiciones',
 'NEGOTIATION',
 4,
 60.00,
 '#8b5cf6',  -- Violet
 NOW(),
 NOW(),
 '30000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000001'),

-- Etapa 5: Ganada (100%)
('e1111111-0000-0000-0000-000000000005',
 '00000000-0000-0000-0000-000000000001',
 'Ganada',
 'Oportunidad ganada - cliente convertido',
 'CLOSED_WON',
 5,
 100.00,
 '#10b981',  -- Green
 NOW(),
 NOW(),
 '30000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000001'),

-- Etapa 6: Perdida (0%)
('e1111111-0000-0000-0000-000000000006',
 '00000000-0000-0000-0000-000000000001',
 'Perdida',
 'Oportunidad perdida - cliente descartado',
 'CLOSED_LOST',
 6,
 0.00,
 '#ef4444',  -- Red
 NOW(),
 NOW(),
 '30000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000001')
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- RESUMEN DE DATOS CREADOS
-- =====================================================
-- - 6 Etapas de Pipeline
--   * Lead (10%) - LEAD
--   * Calificado (20%) - QUALIFIED
--   * Propuesta (40%) - PROPOSAL
--   * Negociación (60%) - NEGOTIATION
--   * Ganada (100%) - CLOSED_WON [FINAL]
--   * Perdida (0%) - CLOSED_LOST [FINAL]
-- =====================================================

-- Verification query
SELECT
    nombre,
    tipo,
    orden,
    probabilidad_default,
    color
FROM oportunidades_etapas_pipeline
WHERE deleted_at IS NULL
ORDER BY orden;
