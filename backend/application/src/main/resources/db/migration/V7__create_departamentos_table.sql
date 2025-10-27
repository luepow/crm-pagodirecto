-------------------------------------------------------------------------------
-- Migración V7: Crear Tabla de Departamentos
--
-- Descripción:
-- Crea la tabla para almacenar departamentos organizacionales con jerarquía
-- Soporta estructura árbol para departamentos y sub-departamentos
--
-- Autor: PagoDirecto CRM Team
-- Fecha: 2025-10-13
-- Version: 7
-------------------------------------------------------------------------------

-- Crear tabla de departamentos
CREATE TABLE IF NOT EXISTS departamentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unidad_negocio_id UUID NOT NULL,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    parent_id UUID,
    nivel INTEGER NOT NULL DEFAULT 0,
    path VARCHAR(500),
    jefe_id UUID,
    email_departamento VARCHAR(255),
    telefono_departamento VARCHAR(50),
    ubicacion VARCHAR(255),
    presupuesto_anual NUMERIC(15,2),
    numero_empleados INTEGER DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_departamentos_parent FOREIGN KEY (parent_id) REFERENCES departamentos(id) ON DELETE SET NULL,
    CONSTRAINT fk_departamentos_jefe FOREIGN KEY (jefe_id) REFERENCES seguridad_usuarios(id) ON DELETE SET NULL,
    CONSTRAINT fk_departamentos_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_departamentos_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_departamentos_nivel CHECK (nivel >= 0 AND nivel <= 5),
    CONSTRAINT chk_departamentos_presupuesto CHECK (presupuesto_anual IS NULL OR presupuesto_anual >= 0),
    CONSTRAINT chk_departamentos_empleados CHECK (numero_empleados >= 0)
);

-- Índices
CREATE INDEX idx_departamentos_unidad_negocio ON departamentos(unidad_negocio_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_departamentos_parent ON departamentos(parent_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_departamentos_jefe ON departamentos(jefe_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_departamentos_codigo ON departamentos(codigo) WHERE deleted_at IS NULL;
CREATE INDEX idx_departamentos_activo ON departamentos(activo) WHERE deleted_at IS NULL;

-- Comentarios
COMMENT ON TABLE departamentos IS 'Departamentos organizacionales con jerarquía tipo árbol';
COMMENT ON COLUMN departamentos.codigo IS 'Código único del departamento (ej: ADM-001, VEN-001)';
COMMENT ON COLUMN departamentos.parent_id IS 'Departamento padre (NULL para departamentos raíz)';
COMMENT ON COLUMN departamentos.nivel IS 'Nivel de profundidad en el árbol (0=raíz, 5=máximo)';
COMMENT ON COLUMN departamentos.path IS 'Ruta completa de departamentos (ej: /Dirección/Finanzas/Contabilidad)';
COMMENT ON COLUMN departamentos.jefe_id IS 'Usuario responsable del departamento';
COMMENT ON COLUMN departamentos.presupuesto_anual IS 'Presupuesto anual asignado al departamento';
COMMENT ON COLUMN departamentos.numero_empleados IS 'Número de empleados en el departamento';

-------------------------------------------------------------------------------
-- Insertar departamentos por defecto
-------------------------------------------------------------------------------

-- Departamentos raíz
INSERT INTO departamentos (codigo, nombre, descripcion, unidad_negocio_id, nivel, path, activo)
VALUES
    ('DIR-001', 'Dirección General', 'Dirección general de la empresa', '00000000-0000-0000-0000-000000000001', 0, '/Dirección General', true),
    ('ADM-001', 'Administración', 'Departamento de administración y recursos humanos', '00000000-0000-0000-0000-000000000001', 0, '/Administración', true),
    ('VEN-001', 'Ventas', 'Departamento de ventas y atención a clientes', '00000000-0000-0000-0000-000000000001', 0, '/Ventas', true),
    ('FIN-001', 'Finanzas', 'Departamento de finanzas y contabilidad', '00000000-0000-0000-0000-000000000001', 0, '/Finanzas', true),
    ('TEC-001', 'Tecnología', 'Departamento de tecnología e informática', '00000000-0000-0000-0000-000000000001', 0, '/Tecnología', true),
    ('ALM-001', 'Almacén', 'Departamento de almacén e inventarios', '00000000-0000-0000-0000-000000000001', 0, '/Almacén', true);

-- Sub-departamentos de Administración
INSERT INTO departamentos (codigo, nombre, descripcion, parent_id, unidad_negocio_id, nivel, path, activo)
SELECT
    'RH-001',
    'Recursos Humanos',
    'Gestión de personal y nómina',
    id,
    '00000000-0000-0000-0000-000000000001',
    1,
    '/Administración/Recursos Humanos',
    true
FROM departamentos WHERE codigo = 'ADM-001';

INSERT INTO departamentos (codigo, nombre, descripcion, parent_id, unidad_negocio_id, nivel, path, activo)
SELECT
    'LEG-001',
    'Legal',
    'Asesoría legal y cumplimiento',
    id,
    '00000000-0000-0000-0000-000000000001',
    1,
    '/Administración/Legal',
    true
FROM departamentos WHERE codigo = 'ADM-001';

-- Sub-departamentos de Ventas
INSERT INTO departamentos (codigo, nombre, descripcion, parent_id, unidad_negocio_id, nivel, path, activo)
SELECT
    'VEN-COM',
    'Ventas Comerciales',
    'Equipo de ventas directas',
    id,
    '00000000-0000-0000-0000-000000000001',
    1,
    '/Ventas/Ventas Comerciales',
    true
FROM departamentos WHERE codigo = 'VEN-001';

INSERT INTO departamentos (codigo, nombre, descripcion, parent_id, unidad_negocio_id, nivel, path, activo)
SELECT
    'MKT-001',
    'Marketing',
    'Marketing y comunicación',
    id,
    '00000000-0000-0000-0000-000000000001',
    1,
    '/Ventas/Marketing',
    true
FROM departamentos WHERE codigo = 'VEN-001';

-- Sub-departamentos de Finanzas
INSERT INTO departamentos (codigo, nombre, descripcion, parent_id, unidad_negocio_id, nivel, path, activo)
SELECT
    'CON-001',
    'Contabilidad',
    'Contabilidad general y fiscal',
    id,
    '00000000-0000-0000-0000-000000000001',
    1,
    '/Finanzas/Contabilidad',
    true
FROM departamentos WHERE codigo = 'FIN-001';

INSERT INTO departamentos (codigo, nombre, descripcion, parent_id, unidad_negocio_id, nivel, path, activo)
SELECT
    'TES-001',
    'Tesorería',
    'Gestión de tesorería y pagos',
    id,
    '00000000-0000-0000-0000-000000000001',
    1,
    '/Finanzas/Tesorería',
    true
FROM departamentos WHERE codigo = 'FIN-001';

-- Sub-departamentos de Tecnología
INSERT INTO departamentos (codigo, nombre, descripcion, parent_id, unidad_negocio_id, nivel, path, activo)
SELECT
    'DEV-001',
    'Desarrollo',
    'Desarrollo de software',
    id,
    '00000000-0000-0000-0000-000000000001',
    1,
    '/Tecnología/Desarrollo',
    true
FROM departamentos WHERE codigo = 'TEC-001';

INSERT INTO departamentos (codigo, nombre, descripcion, parent_id, unidad_negocio_id, nivel, path, activo)
SELECT
    'OPS-001',
    'Operaciones TI',
    'Infraestructura y operaciones',
    id,
    '00000000-0000-0000-0000-000000000001',
    1,
    '/Tecnología/Operaciones TI',
    true
FROM departamentos WHERE codigo = 'TEC-001';

-------------------------------------------------------------------------------
-- Validaciones post-migración
-------------------------------------------------------------------------------

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'departamentos') THEN
        RAISE EXCEPTION 'Error: tabla departamentos no fue creada';
    END IF;

    -- Verificar que se insertaron los departamentos por defecto
    IF (SELECT COUNT(*) FROM departamentos) < 10 THEN
        RAISE WARNING 'Advertencia: Se esperaban al menos 10 departamentos por defecto';
    END IF;

    RAISE NOTICE 'Migración V7 completada exitosamente: Tabla de departamentos creada con % registros', (SELECT COUNT(*) FROM departamentos);
END $$;
