-- =====================================================
-- PagoDirecto CRM/ERP - Sample Business Data (Minimal)
-- =====================================================
-- Purpose: Minimal sample data for testing
-- Author: PagoDirecto Development Team
-- Version: 1.0.0-MINIMAL
-- =====================================================

-- Variables for reference
-- unidad_negocio_id: 00000000-0000-0000-0000-000000000001
-- admin_user_id: 30000000-0000-0000-0000-000000000001

-- =====================================================
-- 1. CLIENTES (Customers) - Minimal Set
-- =====================================================

INSERT INTO clientes_clientes (id, unidad_negocio_id, codigo, nombre, email, telefono,
    tipo, status, created_at, updated_at, created_by, updated_by) VALUES

('c1111111-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001',
    'CLI-001', 'Tech Solutions C.A.', 'contacto@techsolutions.com',
    '+58-212-555-0101', 'EMPRESA', 'ACTIVE',
    NOW(), NOW(), '30000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001'),

('c1111111-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001',
    'CLI-002', 'María González', 'maria.gonzalez@email.com',
    '+58-424-555-0102', 'PERSONA', 'ACTIVE',
    NOW(), NOW(), '30000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001');

-- =====================================================
-- 2. PRODUCTOS (Products) - Minimal Set
-- =====================================================

INSERT INTO productos_productos (id, unidad_negocio_id, codigo, nombre, descripcion,
    tipo, precio_base, moneda, status,
    created_at, updated_at, created_by, updated_by) VALUES

('a1111111-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001',
    'PROD-001', 'POS Terminal Básico', 'Terminal punto de venta',
    'PRODUCTO', 150.00, 'USD', 'ACTIVE',
    NOW(), NOW(), '30000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001'),

('a1111111-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001',
    'SERV-001', 'Plan Emprendedor', 'Plan de procesamiento de pagos',
    'SERVICIO', 25.00, 'USD', 'ACTIVE',
    NOW(), NOW(), '30000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001');

-- =====================================================
-- RESUMEN DE DATOS CREADOS
-- =====================================================
-- - 2 Clientes (1 empresa, 1 persona)
-- - 2 Productos (1 producto, 1 servicio)
-- =====================================================
