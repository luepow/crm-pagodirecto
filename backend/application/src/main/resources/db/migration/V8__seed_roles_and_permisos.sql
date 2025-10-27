-------------------------------------------------------------------------------
-- Migración V8: Crear Permisos y Roles por Defecto
--
-- Descripción:
-- Inserta permisos granulares y roles básicos con asignación de permisos
-- Sistema RBAC completo para control de acceso
--
-- Autor: PagoDirecto CRM Team
-- Fecha: 2025-10-13
-- Version: 8
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
-- Insertar Permisos por Defecto
-------------------------------------------------------------------------------

-- Permisos para Clientes
INSERT INTO seguridad_permisos (recurso, accion, scope, descripcion)
VALUES
    ('clientes', 'CREATE', 'clients:write', 'Crear nuevos clientes'),
    ('clientes', 'READ', 'clients:read', 'Ver información de clientes'),
    ('clientes', 'UPDATE', 'clients:write', 'Actualizar información de clientes'),
    ('clientes', 'DELETE', 'clients:admin', 'Eliminar clientes'),
    ('clientes', 'ADMIN', 'clients:admin', 'Administración completa de clientes');

-- Permisos para Oportunidades
INSERT INTO seguridad_permisos (recurso, accion, scope, descripcion)
VALUES
    ('oportunidades', 'CREATE', 'opportunities:write', 'Crear nuevas oportunidades'),
    ('oportunidades', 'READ', 'opportunities:read', 'Ver oportunidades'),
    ('oportunidades', 'UPDATE', 'opportunities:write', 'Actualizar oportunidades'),
    ('oportunidades', 'DELETE', 'opportunities:admin', 'Eliminar oportunidades'),
    ('oportunidades', 'ADMIN', 'opportunities:admin', 'Administración completa de oportunidades');

-- Permisos para Productos
INSERT INTO seguridad_permisos (recurso, accion, scope, descripcion)
VALUES
    ('productos', 'CREATE', 'products:write', 'Crear nuevos productos'),
    ('productos', 'READ', 'products:read', 'Ver catálogo de productos'),
    ('productos', 'UPDATE', 'products:write', 'Actualizar productos'),
    ('productos', 'DELETE', 'products:admin', 'Eliminar productos'),
    ('productos', 'ADMIN', 'products:admin', 'Administración completa de productos');

-- Permisos para Ventas
INSERT INTO seguridad_permisos (recurso, accion, scope, descripcion)
VALUES
    ('ventas', 'CREATE', 'sales:write', 'Crear nuevas ventas'),
    ('ventas', 'READ', 'sales:read', 'Ver ventas'),
    ('ventas', 'UPDATE', 'sales:write', 'Actualizar ventas'),
    ('ventas', 'DELETE', 'sales:admin', 'Eliminar ventas'),
    ('ventas', 'ADMIN', 'sales:admin', 'Administración completa de ventas');

-- Permisos para Reportes
INSERT INTO seguridad_permisos (recurso, accion, scope, descripcion)
VALUES
    ('reportes', 'READ', 'reports:read', 'Ver reportes básicos'),
    ('reportes', 'EXECUTE', 'reports:execute', 'Ejecutar reportes personalizados'),
    ('reportes', 'ADMIN', 'reports:admin', 'Administración completa de reportes');

-- Permisos para Usuarios
INSERT INTO seguridad_permisos (recurso, accion, scope, descripcion)
VALUES
    ('usuarios', 'CREATE', 'users:write', 'Crear nuevos usuarios'),
    ('usuarios', 'READ', 'users:read', 'Ver usuarios'),
    ('usuarios', 'UPDATE', 'users:write', 'Actualizar usuarios'),
    ('usuarios', 'DELETE', 'users:admin', 'Eliminar usuarios'),
    ('usuarios', 'ADMIN', 'users:admin', 'Administración completa de usuarios');

-- Permisos para Roles
INSERT INTO seguridad_permisos (recurso, accion, scope, descripcion)
VALUES
    ('roles', 'CREATE', 'roles:write', 'Crear nuevos roles'),
    ('roles', 'READ', 'roles:read', 'Ver roles'),
    ('roles', 'UPDATE', 'roles:write', 'Actualizar roles'),
    ('roles', 'DELETE', 'roles:admin', 'Eliminar roles'),
    ('roles', 'ADMIN', 'roles:admin', 'Administración completa de roles');

-- Permisos para Departamentos
INSERT INTO seguridad_permisos (recurso, accion, scope, descripcion)
VALUES
    ('departamentos', 'CREATE', 'departments:write', 'Crear nuevos departamentos'),
    ('departamentos', 'READ', 'departments:read', 'Ver departamentos'),
    ('departamentos', 'UPDATE', 'departments:write', 'Actualizar departamentos'),
    ('departamentos', 'DELETE', 'departments:admin', 'Eliminar departamentos'),
    ('departamentos', 'ADMIN', 'departments:admin', 'Administración completa de departamentos');

-- Permisos para Configuración
INSERT INTO seguridad_permisos (recurso, accion, scope, descripcion)
VALUES
    ('configuracion', 'READ', 'config:read', 'Ver configuración del sistema'),
    ('configuracion', 'UPDATE', 'config:write', 'Actualizar configuración'),
    ('configuracion', 'ADMIN', 'config:admin', 'Administración completa de configuración');

-- Permiso especial de administrador total
INSERT INTO seguridad_permisos (recurso, accion, scope, descripcion)
VALUES
    ('sistema', 'ADMIN', 'admin:*', 'Acceso total al sistema (Super Admin)');

-------------------------------------------------------------------------------
-- Crear Roles por Defecto y Asignar Permisos
-------------------------------------------------------------------------------

-- Rol: Super Administrador
INSERT INTO seguridad_roles (unidad_negocio_id, nombre, descripcion, departamento, nivel_jerarquico)
VALUES ('00000000-0000-0000-0000-000000000001', 'Super Administrador', 'Acceso total al sistema', 'Administración', 0);

-- Asignar todos los permisos al Super Administrador
INSERT INTO seguridad_roles_permisos (rol_id, permiso_id)
SELECT 
    (SELECT id FROM seguridad_roles WHERE nombre = 'Super Administrador'),
    id
FROM seguridad_permisos;

-- Rol: Gerente de Ventas
INSERT INTO seguridad_roles (unidad_negocio_id, nombre, descripcion, departamento, nivel_jerarquico)
VALUES ('00000000-0000-0000-0000-000000000001', 'Gerente de Ventas', 'Gestión completa de ventas y clientes', 'Ventas', 1);

-- Asignar permisos al Gerente de Ventas
INSERT INTO seguridad_roles_permisos (rol_id, permiso_id)
SELECT 
    (SELECT id FROM seguridad_roles WHERE nombre = 'Gerente de Ventas'),
    id
FROM seguridad_permisos
WHERE recurso IN ('clientes', 'oportunidades', 'ventas', 'productos')
   OR (recurso = 'reportes' AND accion IN ('READ', 'EXECUTE'));

-- Rol: Vendedor
INSERT INTO seguridad_roles (unidad_negocio_id, nombre, descripcion, departamento, nivel_jerarquico)
VALUES ('00000000-0000-0000-0000-000000000001', 'Vendedor', 'Crear y gestionar ventas', 'Ventas', 2);

-- Asignar permisos al Vendedor
INSERT INTO seguridad_roles_permisos (rol_id, permiso_id)
SELECT 
    (SELECT id FROM seguridad_roles WHERE nombre = 'Vendedor'),
    id
FROM seguridad_permisos
WHERE (recurso = 'clientes' AND accion IN ('READ', 'UPDATE'))
   OR (recurso = 'oportunidades' AND accion IN ('CREATE', 'READ', 'UPDATE'))
   OR (recurso = 'ventas' AND accion IN ('CREATE', 'READ', 'UPDATE'))
   OR (recurso = 'productos' AND accion = 'READ');

-- Rol: Gerente de Finanzas
INSERT INTO seguridad_roles (unidad_negocio_id, nombre, descripcion, departamento, nivel_jerarquico)
VALUES ('00000000-0000-0000-0000-000000000001', 'Gerente de Finanzas', 'Acceso a reportes financieros y configuración', 'Finanzas', 1);

-- Asignar permisos al Gerente de Finanzas
INSERT INTO seguridad_roles_permisos (rol_id, permiso_id)
SELECT 
    (SELECT id FROM seguridad_roles WHERE nombre = 'Gerente de Finanzas'),
    id
FROM seguridad_permisos
WHERE recurso IN ('reportes', 'configuracion')
   OR (recurso = 'ventas' AND accion = 'READ')
   OR (recurso = 'clientes' AND accion = 'READ');

-- Rol: Soporte/Consulta
INSERT INTO seguridad_roles (unidad_negocio_id, nombre, descripcion, departamento, nivel_jerarquico)
VALUES ('00000000-0000-0000-0000-000000000001', 'Soporte', 'Solo lectura de información', 'Administración', 3);

-- Asignar permisos de solo lectura al Soporte
INSERT INTO seguridad_roles_permisos (rol_id, permiso_id)
SELECT 
    (SELECT id FROM seguridad_roles WHERE nombre = 'Soporte'),
    id
FROM seguridad_permisos
WHERE accion = 'READ';

-------------------------------------------------------------------------------
-- Asignar rol Super Administrador al usuario admin por defecto
-------------------------------------------------------------------------------

INSERT INTO seguridad_usuarios_roles (usuario_id, rol_id)
SELECT 
    u.id,
    r.id
FROM seguridad_usuarios u
CROSS JOIN seguridad_roles r
WHERE u.username = 'admin'
  AND r.nombre = 'Super Administrador'
  AND NOT EXISTS (
      SELECT 1 FROM seguridad_usuarios_roles ur
      WHERE ur.usuario_id = u.id AND ur.rol_id = r.id
  );

-------------------------------------------------------------------------------
-- Validaciones post-migración
-------------------------------------------------------------------------------

DO $$
BEGIN
    -- Verificar permisos
    IF (SELECT COUNT(*) FROM seguridad_permisos) < 40 THEN
        RAISE WARNING 'Advertencia: Se esperaban al menos 40 permisos por defecto';
    END IF;

    -- Verificar roles
    IF (SELECT COUNT(*) FROM seguridad_roles) < 5 THEN
        RAISE WARNING 'Advertencia: Se esperaban al menos 5 roles por defecto';
    END IF;

    -- Verificar asignaciones
    IF (SELECT COUNT(*) FROM seguridad_roles_permisos) < 50 THEN
        RAISE WARNING 'Advertencia: Se esperaban al menos 50 asignaciones de permisos';
    END IF;

    RAISE NOTICE 'Migración V8 completada exitosamente:';
    RAISE NOTICE '- Permisos creados: %', (SELECT COUNT(*) FROM seguridad_permisos);
    RAISE NOTICE '- Roles creados: %', (SELECT COUNT(*) FROM seguridad_roles);
    RAISE NOTICE '- Asignaciones: %', (SELECT COUNT(*) FROM seguridad_roles_permisos);
END $$;
