-- =====================================================
-- PagoDirecto CRM/ERP - Minimal Seed Data
-- =====================================================
-- Purpose: Essential seed data for development (minimal viable dataset)
-- Author: PagoDirecto Development Team
-- Version: 1.0.0-SIMPLIFIED
-- =====================================================

-- =====================================================
-- 1. UNIDADES DE NEGOCIO (Business Units)
-- =====================================================

INSERT INTO unidades_negocio (id, nombre, codigo, activo, created_at, updated_at) VALUES
('00000000-0000-0000-0000-000000000001', 'PagoDirecto Corporativo', 'PD-CORP', true, NOW(), NOW());

-- =====================================================
-- 2. ROLES (Roles)
-- =====================================================

INSERT INTO seguridad_roles (id, unidad_negocio_id, nombre, descripcion, departamento, nivel_jerarquico, created_at, updated_at) VALUES
('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'ADMIN', 'Administrador del Sistema', 'Administración', 0, NOW(), NOW()),
('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', 'USER', 'Usuario Estándar', 'General', 5, NOW(), NOW());

-- =====================================================
-- 3. PERMISOS (Permissions)
-- =====================================================

INSERT INTO seguridad_permisos (id, recurso, accion, descripcion, created_at, updated_at) VALUES
-- Clientes
('20000000-0000-0000-0000-000000000001', 'clientes', 'READ', 'Ver clientes', NOW(), NOW()),
('20000000-0000-0000-0000-000000000002', 'clientes', 'CREATE', 'Crear clientes', NOW(), NOW()),
('20000000-0000-0000-0000-000000000003', 'clientes', 'UPDATE', 'Actualizar clientes', NOW(), NOW()),
('20000000-0000-0000-0000-000000000004', 'clientes', 'DELETE', 'Eliminar clientes', NOW(), NOW()),
-- Admin permissions
('20000000-0000-0000-0000-000000000099', 'system', 'ADMIN', 'Administrador del sistema', NOW(), NOW());

-- Asignar todos los permisos al rol ADMIN
INSERT INTO seguridad_roles_permisos (rol_id, permiso_id)
SELECT '10000000-0000-0000-0000-000000000001', id FROM seguridad_permisos;

-- Asignar permisos de lectura al rol USER
INSERT INTO seguridad_roles_permisos (rol_id, permiso_id) VALUES
('10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001');

-- =====================================================
-- 4. USUARIO ADMIN (Admin User)
-- =====================================================
-- Username: admin
-- Password: admin123 (BCrypt hash with cost 12)
--
-- IMPORTANT: Change this password in production!
-- =====================================================

INSERT INTO seguridad_usuarios (
    id,
    unidad_negocio_id,
    username,
    email,
    nombre,
    apellido,
    password_hash,
    activo,
    created_at,
    updated_at
) VALUES (
    '30000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    'admin',
    'admin@pagodirecto.com',
    'Admin',
    'Sistema',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYU.5MelnRC',
    true,
    NOW(),
    NOW()
);

-- Asignar rol ADMIN al usuario admin
INSERT INTO seguridad_usuarios_roles (usuario_id, rol_id) VALUES
('30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001');

-- =====================================================
-- RESUMEN
-- =====================================================
-- Datos creados:
-- - 1 Unidad de Negocio (PagoDirecto Corporativo)
-- - 2 Roles (ADMIN, USER)
-- - 5 Permisos básicos
-- - 1 Usuario admin con password: admin123
--
-- Para agregar más datos de prueba (clientes, productos, etc.)
-- utiliza la interfaz web una vez que el sistema esté funcionando.
-- =====================================================
