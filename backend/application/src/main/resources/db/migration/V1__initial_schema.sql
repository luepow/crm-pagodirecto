-- =====================================================================================================================
-- V1__initial_schema.sql
-- Migración inicial del esquema de base de datos para PagoDirecto CRM/ERP
-- Fecha: 2025-10-13
-- Autor: Database Architecture Team
-- Descripción: Creación de todas las tablas base con arquitectura limpia y DDD
-- =====================================================================================================================

-- Habilitar extensión para UUID
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================================================================================
-- TABLA BASE: Unidades de Negocio
-- Esta tabla debe crearse primero ya que es referenciada por todas las demás tablas multi-tenant
-- =====================================================================================================================

CREATE TABLE unidades_negocio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(255) NOT NULL,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE unidades_negocio IS 'Unidades de negocio para soporte multi-tenant';
COMMENT ON COLUMN unidades_negocio.codigo IS 'Código único de la unidad de negocio (e.g., PD-CORP)';
COMMENT ON COLUMN unidades_negocio.activo IS 'Indica si la unidad de negocio está activa';

-- =====================================================================================================================
-- DOMINIO: SEGURIDAD (Security & IAM)
-- Contexto delimitado para gestión de usuarios, roles, permisos y auditoría
-- =====================================================================================================================

-- Tabla: seguridad_usuarios
-- Propósito: Almacena información de usuarios del sistema con soporte para MFA
-- Notas: Password debe almacenarse con bcrypt (mínimo cost 12)
CREATE TABLE seguridad_usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unidad_negocio_id UUID NOT NULL,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    nombre VARCHAR(100),  -- First name
    apellido VARCHAR(100),  -- Last name
    password_hash VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,  -- Active flag (for seed data compatibility)
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_secret VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ultimo_acceso TIMESTAMPTZ,
    intentos_fallidos INTEGER NOT NULL DEFAULT 0,
    bloqueado_hasta TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT chk_seguridad_usuarios_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED', 'SUSPENDED')),
    CONSTRAINT chk_seguridad_usuarios_email CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_seguridad_usuarios_intentos CHECK (intentos_fallidos >= 0 AND intentos_fallidos <= 5)
);

COMMENT ON TABLE seguridad_usuarios IS 'Usuarios del sistema con soporte para autenticación multi-factor';
COMMENT ON COLUMN seguridad_usuarios.password_hash IS 'Hash bcrypt del password (cost >= 12)';
COMMENT ON COLUMN seguridad_usuarios.mfa_secret IS 'Secreto TOTP para autenticación de dos factores';
COMMENT ON COLUMN seguridad_usuarios.intentos_fallidos IS 'Contador de intentos fallidos de login (máximo 5)';
COMMENT ON COLUMN seguridad_usuarios.bloqueado_hasta IS 'Timestamp hasta el cual la cuenta está bloqueada';

-- Tabla: seguridad_roles
-- Propósito: Define roles jerárquicos por departamento
CREATE TABLE seguridad_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unidad_negocio_id UUID NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    departamento VARCHAR(100),
    nivel_jerarquico INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT chk_seguridad_roles_nivel CHECK (nivel_jerarquico >= 0 AND nivel_jerarquico <= 10)
);

COMMENT ON TABLE seguridad_roles IS 'Roles del sistema con jerarquía departamental';
COMMENT ON COLUMN seguridad_roles.nivel_jerarquico IS 'Nivel jerárquico (0=más alto, 10=más bajo)';
COMMENT ON COLUMN seguridad_roles.departamento IS 'Departamento: Administración, Ventas, Finanzas, Almacén, etc.';

-- Tabla: seguridad_permisos
-- Propósito: Permisos granulares CRUD por recurso
CREATE TABLE seguridad_permisos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recurso VARCHAR(100) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    scope VARCHAR(100),
    descripcion TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT chk_seguridad_permisos_accion CHECK (accion IN ('CREATE', 'READ', 'UPDATE', 'DELETE', 'EXECUTE', 'ADMIN'))
);

COMMENT ON TABLE seguridad_permisos IS 'Permisos granulares CRUD por recurso';
COMMENT ON COLUMN seguridad_permisos.recurso IS 'Nombre del recurso (e.g., clientes, ventas, productos)';
COMMENT ON COLUMN seguridad_permisos.accion IS 'Acción permitida: CREATE, READ, UPDATE, DELETE, EXECUTE, ADMIN';
COMMENT ON COLUMN seguridad_permisos.scope IS 'Alcance del permiso (e.g., sales:write, reports:read)';

-- Tabla: seguridad_roles_permisos
-- Propósito: Mapeo muchos a muchos entre roles y permisos
CREATE TABLE seguridad_roles_permisos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rol_id UUID NOT NULL,
    permiso_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    CONSTRAINT fk_seguridad_roles_permisos_rol FOREIGN KEY (rol_id) REFERENCES seguridad_roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_seguridad_roles_permisos_permiso FOREIGN KEY (permiso_id) REFERENCES seguridad_permisos(id) ON DELETE CASCADE,
    CONSTRAINT uk_seguridad_roles_permisos UNIQUE (rol_id, permiso_id)
);

COMMENT ON TABLE seguridad_roles_permisos IS 'Mapeo muchos a muchos entre roles y permisos';

-- Tabla: seguridad_usuarios_roles
-- Propósito: Mapeo muchos a muchos entre usuarios y roles
CREATE TABLE seguridad_usuarios_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    rol_id UUID NOT NULL,
    fecha_asignacion TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    fecha_expiracion TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    CONSTRAINT fk_seguridad_usuarios_roles_usuario FOREIGN KEY (usuario_id) REFERENCES seguridad_usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_seguridad_usuarios_roles_rol FOREIGN KEY (rol_id) REFERENCES seguridad_roles(id) ON DELETE CASCADE,
    CONSTRAINT uk_seguridad_usuarios_roles UNIQUE (usuario_id, rol_id)
);

COMMENT ON TABLE seguridad_usuarios_roles IS 'Mapeo muchos a muchos entre usuarios y roles con soporte para expiración';
COMMENT ON COLUMN seguridad_usuarios_roles.fecha_expiracion IS 'Fecha de expiración del rol (NULL = permanente)';

-- Tabla: seguridad_refresh_tokens
-- Propósito: Tokens de refresco para autenticación JWT
-- Notas: TTL de 30 días, limpieza automática de tokens expirados
CREATE TABLE seguridad_refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revocado BOOLEAN NOT NULL DEFAULT FALSE,
    revocado_at TIMESTAMPTZ,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_seguridad_refresh_tokens_usuario FOREIGN KEY (usuario_id) REFERENCES seguridad_usuarios(id) ON DELETE CASCADE,
    CONSTRAINT chk_seguridad_refresh_tokens_expires CHECK (expires_at > created_at)
);

COMMENT ON TABLE seguridad_refresh_tokens IS 'Tokens de refresco JWT con TTL de 30 días';
COMMENT ON COLUMN seguridad_refresh_tokens.token_hash IS 'Hash SHA-256 del token de refresco';
COMMENT ON COLUMN seguridad_refresh_tokens.expires_at IS 'Fecha de expiración del token (típicamente 30 días)';

-- Tabla: seguridad_audit_log
-- Propósito: Registro inmutable de auditoría (append-only)
-- Notas: Retención de 7 años para cumplimiento financiero
CREATE TABLE seguridad_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID,
    accion VARCHAR(100) NOT NULL,
    recurso VARCHAR(100) NOT NULL,
    recurso_id UUID,
    ip_address INET NOT NULL,
    user_agent TEXT,
    metadata JSONB,
    resultado VARCHAR(20) NOT NULL,
    mensaje_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_seguridad_audit_log_usuario FOREIGN KEY (usuario_id) REFERENCES seguridad_usuarios(id) ON DELETE SET NULL,
    CONSTRAINT chk_seguridad_audit_log_resultado CHECK (resultado IN ('SUCCESS', 'FAILURE', 'PARTIAL'))
);

COMMENT ON TABLE seguridad_audit_log IS 'Registro inmutable de auditoría con retención de 7 años';
COMMENT ON COLUMN seguridad_audit_log.accion IS 'Acción realizada (e.g., LOGIN, CREATE_ORDER, DELETE_CLIENT)';
COMMENT ON COLUMN seguridad_audit_log.metadata IS 'Datos adicionales en formato JSON (cambios, parámetros, etc.)';
COMMENT ON COLUMN seguridad_audit_log.resultado IS 'Resultado de la operación: SUCCESS, FAILURE, PARTIAL';

-- =====================================================================================================================
-- DOMINIO: CLIENTES (Clients/CRM)
-- Contexto delimitado para gestión de clientes, contactos y direcciones
-- =====================================================================================================================

-- Tabla: clientes_clientes
-- Propósito: Información principal de clientes (personas físicas o morales)
CREATE TABLE clientes_clientes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unidad_negocio_id UUID NOT NULL,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    telefono VARCHAR(50),
    tipo VARCHAR(20) NOT NULL,
    rfc VARCHAR(20),
    razon_social VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    segmento VARCHAR(50),
    fuente VARCHAR(50),
    propietario_id UUID,
    notas TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_clientes_clientes_propietario FOREIGN KEY (propietario_id) REFERENCES seguridad_usuarios(id) ON DELETE SET NULL,
    CONSTRAINT fk_clientes_clientes_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_clientes_clientes_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_clientes_clientes_tipo CHECK (tipo IN ('PERSONA', 'EMPRESA')),
    CONSTRAINT chk_clientes_clientes_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'PROSPECT', 'LEAD', 'BLACKLIST'))
);

COMMENT ON TABLE clientes_clientes IS 'Información principal de clientes (personas físicas o morales)';
COMMENT ON COLUMN clientes_clientes.codigo IS 'Código único del cliente (e.g., CLI-00001)';
COMMENT ON COLUMN clientes_clientes.tipo IS 'Tipo de cliente: PERSONA (física) o EMPRESA (moral)';
COMMENT ON COLUMN clientes_clientes.rfc IS 'Registro Federal de Contribuyentes (México)';
COMMENT ON COLUMN clientes_clientes.segmento IS 'Segmento de cliente: CORPORATIVO, PYME, RETAIL, etc.';
COMMENT ON COLUMN clientes_clientes.fuente IS 'Fuente de adquisición: WEBSITE, REFERRAL, CAMPAIGN, etc.';

-- Tabla: clientes_contactos
-- Propósito: Personas de contacto asociadas a un cliente
CREATE TABLE clientes_contactos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id UUID NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    telefono VARCHAR(50),
    telefono_movil VARCHAR(50),
    cargo VARCHAR(100),
    departamento VARCHAR(100),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    notas TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_clientes_contactos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes_clientes(id) ON DELETE CASCADE,
    CONSTRAINT fk_clientes_contactos_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_clientes_contactos_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_clientes_contactos_email CHECK (email IS NULL OR email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

COMMENT ON TABLE clientes_contactos IS 'Personas de contacto asociadas a un cliente';
COMMENT ON COLUMN clientes_contactos.is_primary IS 'Indica si es el contacto principal del cliente';
COMMENT ON COLUMN clientes_contactos.cargo IS 'Cargo del contacto (e.g., Director, Gerente, etc.)';

-- Tabla: clientes_direcciones
-- Propósito: Direcciones físicas de clientes (facturación, envío, etc.)
CREATE TABLE clientes_direcciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id UUID NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    calle VARCHAR(255) NOT NULL,
    numero_exterior VARCHAR(20),
    numero_interior VARCHAR(20),
    colonia VARCHAR(100),
    ciudad VARCHAR(100) NOT NULL,
    estado VARCHAR(100) NOT NULL,
    codigo_postal VARCHAR(10) NOT NULL,
    pais VARCHAR(2) NOT NULL DEFAULT 'MX',
    referencia TEXT,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_clientes_direcciones_cliente FOREIGN KEY (cliente_id) REFERENCES clientes_clientes(id) ON DELETE CASCADE,
    CONSTRAINT fk_clientes_direcciones_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_clientes_direcciones_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_clientes_direcciones_tipo CHECK (tipo IN ('FISCAL', 'ENVIO', 'OTRO')),
    CONSTRAINT chk_clientes_direcciones_pais CHECK (pais ~ '^[A-Z]{2}$')
);

COMMENT ON TABLE clientes_direcciones IS 'Direcciones físicas de clientes (facturación, envío, etc.)';
COMMENT ON COLUMN clientes_direcciones.tipo IS 'Tipo de dirección: FISCAL, ENVIO, OTRO';
COMMENT ON COLUMN clientes_direcciones.pais IS 'Código ISO 3166-1 alpha-2 del país (e.g., MX, US, CA)';
COMMENT ON COLUMN clientes_direcciones.is_default IS 'Indica si es la dirección predeterminada para este tipo';

-- =====================================================================================================================
-- DOMINIO: OPORTUNIDADES (Opportunities/Pipeline)
-- Contexto delimitado para gestión del pipeline de ventas
-- =====================================================================================================================

-- Tabla: oportunidades_etapas_pipeline
-- Propósito: Definición de etapas del pipeline de ventas
CREATE TABLE oportunidades_etapas_pipeline (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unidad_negocio_id UUID NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    tipo VARCHAR(20) NOT NULL,
    orden INTEGER NOT NULL,
    probabilidad_default NUMERIC(5,2),
    color VARCHAR(7),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_oportunidades_etapas_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_oportunidades_etapas_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_oportunidades_etapas_tipo CHECK (tipo IN ('LEAD', 'QUALIFIED', 'PROPOSAL', 'NEGOTIATION', 'CLOSED_WON', 'CLOSED_LOST')),
    CONSTRAINT chk_oportunidades_etapas_probabilidad CHECK (probabilidad_default >= 0 AND probabilidad_default <= 100),
    CONSTRAINT chk_oportunidades_etapas_orden CHECK (orden >= 0)
);

COMMENT ON TABLE oportunidades_etapas_pipeline IS 'Definición de etapas del pipeline de ventas';
COMMENT ON COLUMN oportunidades_etapas_pipeline.tipo IS 'Tipo de etapa: LEAD, QUALIFIED, PROPOSAL, NEGOTIATION, CLOSED_WON, CLOSED_LOST';
COMMENT ON COLUMN oportunidades_etapas_pipeline.probabilidad_default IS 'Probabilidad de cierre por defecto para esta etapa (0-100)';
COMMENT ON COLUMN oportunidades_etapas_pipeline.orden IS 'Orden de la etapa en el pipeline (0=primera)';

-- Tabla: oportunidades_oportunidades
-- Propósito: Oportunidades de venta en el pipeline
CREATE TABLE oportunidades_oportunidades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unidad_negocio_id UUID NOT NULL,
    cliente_id UUID NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    valor_estimado NUMERIC(15,2) NOT NULL,
    moneda VARCHAR(3) NOT NULL DEFAULT 'MXN',
    probabilidad NUMERIC(5,2) NOT NULL,
    etapa_id UUID NOT NULL,
    fecha_cierre_estimada DATE,
    fecha_cierre_real DATE,
    propietario_id UUID NOT NULL,
    fuente VARCHAR(50),
    motivo_perdida TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_oportunidades_oportunidades_cliente FOREIGN KEY (cliente_id) REFERENCES clientes_clientes(id) ON DELETE CASCADE,
    CONSTRAINT fk_oportunidades_oportunidades_etapa FOREIGN KEY (etapa_id) REFERENCES oportunidades_etapas_pipeline(id),
    CONSTRAINT fk_oportunidades_oportunidades_propietario FOREIGN KEY (propietario_id) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_oportunidades_oportunidades_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_oportunidades_oportunidades_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_oportunidades_oportunidades_probabilidad CHECK (probabilidad >= 0 AND probabilidad <= 100),
    CONSTRAINT chk_oportunidades_oportunidades_valor CHECK (valor_estimado >= 0),
    CONSTRAINT chk_oportunidades_oportunidades_moneda CHECK (moneda ~ '^[A-Z]{3}$')
);

COMMENT ON TABLE oportunidades_oportunidades IS 'Oportunidades de venta en el pipeline';
COMMENT ON COLUMN oportunidades_oportunidades.valor_estimado IS 'Valor estimado de la oportunidad en la moneda especificada';
COMMENT ON COLUMN oportunidades_oportunidades.probabilidad IS 'Probabilidad de cierre (0-100)';
COMMENT ON COLUMN oportunidades_oportunidades.moneda IS 'Código ISO 4217 de la moneda (e.g., MXN, USD, EUR)';
COMMENT ON COLUMN oportunidades_oportunidades.motivo_perdida IS 'Razón por la cual se perdió la oportunidad';

-- Tabla: oportunidades_actividades
-- Propósito: Actividades relacionadas con oportunidades (llamadas, reuniones, emails)
CREATE TABLE oportunidades_actividades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    oportunidad_id UUID NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    fecha_actividad TIMESTAMPTZ NOT NULL,
    duracion_minutos INTEGER,
    completada BOOLEAN NOT NULL DEFAULT FALSE,
    resultado TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_oportunidades_actividades_oportunidad FOREIGN KEY (oportunidad_id) REFERENCES oportunidades_oportunidades(id) ON DELETE CASCADE,
    CONSTRAINT fk_oportunidades_actividades_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_oportunidades_actividades_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_oportunidades_actividades_tipo CHECK (tipo IN ('LLAMADA', 'REUNION', 'EMAIL', 'TAREA', 'NOTA', 'OTRO')),
    CONSTRAINT chk_oportunidades_actividades_duracion CHECK (duracion_minutos IS NULL OR duracion_minutos > 0)
);

COMMENT ON TABLE oportunidades_actividades IS 'Actividades relacionadas con oportunidades (llamadas, reuniones, emails)';
COMMENT ON COLUMN oportunidades_actividades.tipo IS 'Tipo de actividad: LLAMADA, REUNION, EMAIL, TAREA, NOTA, OTRO';
COMMENT ON COLUMN oportunidades_actividades.duracion_minutos IS 'Duración de la actividad en minutos';
COMMENT ON COLUMN oportunidades_actividades.resultado IS 'Resultado o notas de la actividad completada';

-- =====================================================================================================================
-- DOMINIO: TAREAS (Tasks/Activities)
-- Contexto delimitado para gestión de tareas y actividades generales
-- =====================================================================================================================

-- Tabla: tareas_tareas
-- Propósito: Tareas generales asignables a usuarios
-- Notas: Puede relacionarse con cualquier entidad vía relacionado_tipo/relacionado_id (polimórfico)
CREATE TABLE tareas_tareas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unidad_negocio_id UUID NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    tipo VARCHAR(50) NOT NULL,
    prioridad VARCHAR(20) NOT NULL DEFAULT 'MEDIA',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_vencimiento DATE,
    fecha_completada TIMESTAMPTZ,
    asignado_a UUID NOT NULL,
    relacionado_tipo VARCHAR(50),
    relacionado_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_tareas_tareas_asignado FOREIGN KEY (asignado_a) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_tareas_tareas_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_tareas_tareas_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_tareas_tareas_tipo CHECK (tipo IN ('LLAMADA', 'EMAIL', 'REUNION', 'SEGUIMIENTO', 'ADMINISTRATIVA', 'TECNICA', 'OTRA')),
    CONSTRAINT chk_tareas_tareas_prioridad CHECK (prioridad IN ('BAJA', 'MEDIA', 'ALTA', 'URGENTE')),
    CONSTRAINT chk_tareas_tareas_status CHECK (status IN ('PENDIENTE', 'EN_PROGRESO', 'COMPLETADA', 'CANCELADA', 'BLOQUEADA'))
);

COMMENT ON TABLE tareas_tareas IS 'Tareas generales asignables a usuarios con relación polimórfica';
COMMENT ON COLUMN tareas_tareas.relacionado_tipo IS 'Tipo de entidad relacionada (e.g., CLIENTE, OPORTUNIDAD, PEDIDO)';
COMMENT ON COLUMN tareas_tareas.relacionado_id IS 'UUID de la entidad relacionada';
COMMENT ON COLUMN tareas_tareas.prioridad IS 'Prioridad: BAJA, MEDIA, ALTA, URGENTE';
COMMENT ON COLUMN tareas_tareas.status IS 'Estado: PENDIENTE, EN_PROGRESO, COMPLETADA, CANCELADA, BLOQUEADA';

-- Tabla: tareas_comentarios
-- Propósito: Comentarios y actualizaciones en tareas
CREATE TABLE tareas_comentarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tarea_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    comentario TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_tareas_comentarios_tarea FOREIGN KEY (tarea_id) REFERENCES tareas_tareas(id) ON DELETE CASCADE,
    CONSTRAINT fk_tareas_comentarios_usuario FOREIGN KEY (usuario_id) REFERENCES seguridad_usuarios(id)
);

COMMENT ON TABLE tareas_comentarios IS 'Comentarios y actualizaciones en tareas';

-- =====================================================================================================================
-- DOMINIO: PRODUCTOS (Products/Catalog)
-- Contexto delimitado para gestión de catálogo de productos y precios
-- =====================================================================================================================

-- Tabla: productos_categorias
-- Propósito: Categorías jerárquicas de productos
CREATE TABLE productos_categorias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unidad_negocio_id UUID NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    parent_id UUID,
    nivel INTEGER NOT NULL DEFAULT 0,
    path VARCHAR(500),
    orden INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_productos_categorias_parent FOREIGN KEY (parent_id) REFERENCES productos_categorias(id) ON DELETE SET NULL,
    CONSTRAINT fk_productos_categorias_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_productos_categorias_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_productos_categorias_nivel CHECK (nivel >= 0 AND nivel <= 5)
);

COMMENT ON TABLE productos_categorias IS 'Categorías jerárquicas de productos (hasta 5 niveles)';
COMMENT ON COLUMN productos_categorias.parent_id IS 'Categoría padre (NULL para categorías raíz)';
COMMENT ON COLUMN productos_categorias.nivel IS 'Nivel de profundidad en el árbol (0=raíz)';
COMMENT ON COLUMN productos_categorias.path IS 'Ruta completa de categorías (e.g., /Electrónica/Computadoras/Laptops)';

-- Tabla: productos_productos
-- Propósito: Catálogo de productos y servicios
CREATE TABLE productos_productos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unidad_negocio_id UUID NOT NULL,
    codigo VARCHAR(100) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    categoria_id UUID,
    tipo VARCHAR(20) NOT NULL DEFAULT 'PRODUCTO',
    precio_base NUMERIC(15,2) NOT NULL,
    moneda VARCHAR(3) NOT NULL DEFAULT 'MXN',
    costo_unitario NUMERIC(15,2),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    stock_actual INTEGER DEFAULT 0,
    stock_minimo INTEGER DEFAULT 0,
    unidad_medida VARCHAR(20),
    peso_kg NUMERIC(10,3),
    sku VARCHAR(100),
    codigo_barras VARCHAR(100),
    imagen_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_productos_productos_categoria FOREIGN KEY (categoria_id) REFERENCES productos_categorias(id) ON DELETE SET NULL,
    CONSTRAINT fk_productos_productos_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_productos_productos_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_productos_productos_tipo CHECK (tipo IN ('PRODUCTO', 'SERVICIO', 'COMBO')),
    CONSTRAINT chk_productos_productos_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DISCONTINUED')),
    CONSTRAINT chk_productos_productos_precio CHECK (precio_base >= 0),
    CONSTRAINT chk_productos_productos_costo CHECK (costo_unitario IS NULL OR costo_unitario >= 0),
    CONSTRAINT chk_productos_productos_stock CHECK (stock_actual >= 0),
    CONSTRAINT chk_productos_productos_moneda CHECK (moneda ~ '^[A-Z]{3}$')
);

COMMENT ON TABLE productos_productos IS 'Catálogo de productos y servicios';
COMMENT ON COLUMN productos_productos.tipo IS 'Tipo: PRODUCTO, SERVICIO, COMBO';
COMMENT ON COLUMN productos_productos.precio_base IS 'Precio base de lista';
COMMENT ON COLUMN productos_productos.costo_unitario IS 'Costo unitario de adquisición (para cálculo de margen)';
COMMENT ON COLUMN productos_productos.stock_actual IS 'Inventario disponible actual';
COMMENT ON COLUMN productos_productos.stock_minimo IS 'Nivel mínimo de inventario para alertas';

-- Tabla: productos_precios
-- Propósito: Precios diferenciados por tipo de cliente, promociones, etc.
CREATE TABLE productos_precios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    producto_id UUID NOT NULL,
    tipo_precio VARCHAR(50) NOT NULL,
    precio NUMERIC(15,2) NOT NULL,
    moneda VARCHAR(3) NOT NULL DEFAULT 'MXN',
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    segmento_cliente VARCHAR(50),
    cantidad_minima INTEGER DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_productos_precios_producto FOREIGN KEY (producto_id) REFERENCES productos_productos(id) ON DELETE CASCADE,
    CONSTRAINT fk_productos_precios_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_productos_precios_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_productos_precios_precio CHECK (precio >= 0),
    CONSTRAINT chk_productos_precios_cantidad CHECK (cantidad_minima > 0),
    CONSTRAINT chk_productos_precios_fechas CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio),
    CONSTRAINT chk_productos_precios_moneda CHECK (moneda ~ '^[A-Z]{3}$')
);

COMMENT ON TABLE productos_precios IS 'Precios diferenciados por tipo de cliente, promociones, volumen, etc.';
COMMENT ON COLUMN productos_precios.tipo_precio IS 'Tipo: LISTA, MAYOREO, DISTRIBUIDOR, PROMOCION, etc.';
COMMENT ON COLUMN productos_precios.segmento_cliente IS 'Segmento al que aplica el precio (NULL = todos)';
COMMENT ON COLUMN productos_precios.cantidad_minima IS 'Cantidad mínima para aplicar este precio';

-- =====================================================================================================================
-- DOMINIO: VENTAS (Sales/Quotes)
-- Contexto delimitado para cotizaciones y pedidos
-- =====================================================================================================================

-- Tabla: ventas_cotizaciones
-- Propósito: Cotizaciones enviadas a clientes
CREATE TABLE ventas_cotizaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unidad_negocio_id UUID NOT NULL,
    cliente_id UUID NOT NULL,
    oportunidad_id UUID,
    numero VARCHAR(50) NOT NULL,
    fecha DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_validez DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    subtotal NUMERIC(15,2) NOT NULL DEFAULT 0,
    descuento_global NUMERIC(15,2) DEFAULT 0,
    impuestos NUMERIC(15,2) NOT NULL DEFAULT 0,
    total NUMERIC(15,2) NOT NULL DEFAULT 0,
    moneda VARCHAR(3) NOT NULL DEFAULT 'MXN',
    notas TEXT,
    terminos_condiciones TEXT,
    propietario_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_ventas_cotizaciones_cliente FOREIGN KEY (cliente_id) REFERENCES clientes_clientes(id),
    CONSTRAINT fk_ventas_cotizaciones_oportunidad FOREIGN KEY (oportunidad_id) REFERENCES oportunidades_oportunidades(id) ON DELETE SET NULL,
    CONSTRAINT fk_ventas_cotizaciones_propietario FOREIGN KEY (propietario_id) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_ventas_cotizaciones_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_ventas_cotizaciones_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_ventas_cotizaciones_status CHECK (status IN ('BORRADOR', 'ENVIADA', 'ACEPTADA', 'RECHAZADA', 'EXPIRADA')),
    CONSTRAINT chk_ventas_cotizaciones_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_ventas_cotizaciones_descuento CHECK (descuento_global >= 0),
    CONSTRAINT chk_ventas_cotizaciones_impuestos CHECK (impuestos >= 0),
    CONSTRAINT chk_ventas_cotizaciones_total CHECK (total >= 0),
    CONSTRAINT chk_ventas_cotizaciones_fechas CHECK (fecha_validez >= fecha)
);

COMMENT ON TABLE ventas_cotizaciones IS 'Cotizaciones enviadas a clientes';
COMMENT ON COLUMN ventas_cotizaciones.numero IS 'Número único de cotización (e.g., COT-2025-00001)';
COMMENT ON COLUMN ventas_cotizaciones.fecha_validez IS 'Fecha hasta la cual es válida la cotización';
COMMENT ON COLUMN ventas_cotizaciones.status IS 'Estado: BORRADOR, ENVIADA, ACEPTADA, RECHAZADA, EXPIRADA';

-- Tabla: ventas_items_cotizacion
-- Propósito: Líneas de detalle de cotizaciones
CREATE TABLE ventas_items_cotizacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cotizacion_id UUID NOT NULL,
    producto_id UUID NOT NULL,
    descripcion TEXT,
    cantidad NUMERIC(10,3) NOT NULL,
    precio_unitario NUMERIC(15,2) NOT NULL,
    descuento_porcentaje NUMERIC(5,2) DEFAULT 0,
    descuento_monto NUMERIC(15,2) DEFAULT 0,
    subtotal NUMERIC(15,2) NOT NULL,
    impuesto_porcentaje NUMERIC(5,2) DEFAULT 0,
    impuesto_monto NUMERIC(15,2) DEFAULT 0,
    total NUMERIC(15,2) NOT NULL,
    orden INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    CONSTRAINT fk_ventas_items_cotizacion_cotizacion FOREIGN KEY (cotizacion_id) REFERENCES ventas_cotizaciones(id) ON DELETE CASCADE,
    CONSTRAINT fk_ventas_items_cotizacion_producto FOREIGN KEY (producto_id) REFERENCES productos_productos(id),
    CONSTRAINT fk_ventas_items_cotizacion_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_ventas_items_cotizacion_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_ventas_items_cotizacion_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_ventas_items_cotizacion_precio CHECK (precio_unitario >= 0),
    CONSTRAINT chk_ventas_items_cotizacion_descuento_pct CHECK (descuento_porcentaje >= 0 AND descuento_porcentaje <= 100),
    CONSTRAINT chk_ventas_items_cotizacion_impuesto_pct CHECK (impuesto_porcentaje >= 0 AND impuesto_porcentaje <= 100)
);

COMMENT ON TABLE ventas_items_cotizacion IS 'Líneas de detalle de cotizaciones';
COMMENT ON COLUMN ventas_items_cotizacion.orden IS 'Orden de presentación de la línea';

-- Tabla: ventas_pedidos
-- Propósito: Pedidos confirmados (órdenes de venta)
CREATE TABLE ventas_pedidos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unidad_negocio_id UUID NOT NULL,
    cotizacion_id UUID,
    cliente_id UUID NOT NULL,
    numero VARCHAR(50) NOT NULL,
    fecha DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_entrega_estimada DATE,
    fecha_entrega_real DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    subtotal NUMERIC(15,2) NOT NULL DEFAULT 0,
    descuento_global NUMERIC(15,2) DEFAULT 0,
    impuestos NUMERIC(15,2) NOT NULL DEFAULT 0,
    total NUMERIC(15,2) NOT NULL DEFAULT 0,
    moneda VARCHAR(3) NOT NULL DEFAULT 'MXN',
    metodo_pago VARCHAR(50),
    terminos_pago VARCHAR(100),
    notas TEXT,
    propietario_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_ventas_pedidos_cotizacion FOREIGN KEY (cotizacion_id) REFERENCES ventas_cotizaciones(id) ON DELETE SET NULL,
    CONSTRAINT fk_ventas_pedidos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes_clientes(id),
    CONSTRAINT fk_ventas_pedidos_propietario FOREIGN KEY (propietario_id) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_ventas_pedidos_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_ventas_pedidos_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_ventas_pedidos_status CHECK (status IN ('PENDIENTE', 'CONFIRMADO', 'EN_PROCESO', 'ENVIADO', 'ENTREGADO', 'CANCELADO', 'DEVUELTO')),
    CONSTRAINT chk_ventas_pedidos_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_ventas_pedidos_descuento CHECK (descuento_global >= 0),
    CONSTRAINT chk_ventas_pedidos_impuestos CHECK (impuestos >= 0),
    CONSTRAINT chk_ventas_pedidos_total CHECK (total >= 0)
);

COMMENT ON TABLE ventas_pedidos IS 'Pedidos confirmados (órdenes de venta)';
COMMENT ON COLUMN ventas_pedidos.numero IS 'Número único de pedido (e.g., PED-2025-00001)';
COMMENT ON COLUMN ventas_pedidos.status IS 'Estado: PENDIENTE, CONFIRMADO, EN_PROCESO, ENVIADO, ENTREGADO, CANCELADO, DEVUELTO';
COMMENT ON COLUMN ventas_pedidos.terminos_pago IS 'Términos de pago (e.g., CONTADO, 30 DIAS, 60 DIAS)';

-- Tabla: ventas_items_pedido
-- Propósito: Líneas de detalle de pedidos
CREATE TABLE ventas_items_pedido (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id UUID NOT NULL,
    producto_id UUID NOT NULL,
    descripcion TEXT,
    cantidad NUMERIC(10,3) NOT NULL,
    cantidad_entregada NUMERIC(10,3) DEFAULT 0,
    precio_unitario NUMERIC(15,2) NOT NULL,
    descuento_porcentaje NUMERIC(5,2) DEFAULT 0,
    descuento_monto NUMERIC(15,2) DEFAULT 0,
    subtotal NUMERIC(15,2) NOT NULL,
    impuesto_porcentaje NUMERIC(5,2) DEFAULT 0,
    impuesto_monto NUMERIC(15,2) DEFAULT 0,
    total NUMERIC(15,2) NOT NULL,
    orden INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    CONSTRAINT fk_ventas_items_pedido_pedido FOREIGN KEY (pedido_id) REFERENCES ventas_pedidos(id) ON DELETE CASCADE,
    CONSTRAINT fk_ventas_items_pedido_producto FOREIGN KEY (producto_id) REFERENCES productos_productos(id),
    CONSTRAINT fk_ventas_items_pedido_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_ventas_items_pedido_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_ventas_items_pedido_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_ventas_items_pedido_cantidad_entregada CHECK (cantidad_entregada >= 0 AND cantidad_entregada <= cantidad),
    CONSTRAINT chk_ventas_items_pedido_precio CHECK (precio_unitario >= 0),
    CONSTRAINT chk_ventas_items_pedido_descuento_pct CHECK (descuento_porcentaje >= 0 AND descuento_porcentaje <= 100),
    CONSTRAINT chk_ventas_items_pedido_impuesto_pct CHECK (impuesto_porcentaje >= 0 AND impuesto_porcentaje <= 100)
);

COMMENT ON TABLE ventas_items_pedido IS 'Líneas de detalle de pedidos';
COMMENT ON COLUMN ventas_items_pedido.cantidad_entregada IS 'Cantidad ya entregada (para entregas parciales)';

-- =====================================================================================================================
-- DOMINIO: REPORTES (Reports/Analytics)
-- Contexto delimitado para dashboards y visualizaciones
-- =====================================================================================================================

-- Tabla: reportes_dashboards
-- Propósito: Definición de dashboards personalizables
CREATE TABLE reportes_dashboards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unidad_negocio_id UUID NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    configuracion JSONB,
    propietario_id UUID NOT NULL,
    es_publico BOOLEAN NOT NULL DEFAULT FALSE,
    orden INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_reportes_dashboards_propietario FOREIGN KEY (propietario_id) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_reportes_dashboards_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_reportes_dashboards_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id)
);

COMMENT ON TABLE reportes_dashboards IS 'Definición de dashboards personalizables';
COMMENT ON COLUMN reportes_dashboards.configuracion IS 'Configuración del dashboard en formato JSON (layout, filtros, etc.)';
COMMENT ON COLUMN reportes_dashboards.es_publico IS 'Si es verdadero, todos los usuarios pueden ver el dashboard';

-- Tabla: reportes_widgets
-- Propósito: Widgets individuales dentro de dashboards
CREATE TABLE reportes_widgets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dashboard_id UUID NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    configuracion JSONB NOT NULL,
    posicion JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_reportes_widgets_dashboard FOREIGN KEY (dashboard_id) REFERENCES reportes_dashboards(id) ON DELETE CASCADE,
    CONSTRAINT fk_reportes_widgets_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_reportes_widgets_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_reportes_widgets_tipo CHECK (tipo IN ('CHART', 'TABLE', 'KPI', 'MAP', 'LIST', 'CALENDAR', 'CUSTOM'))
);

COMMENT ON TABLE reportes_widgets IS 'Widgets individuales dentro de dashboards';
COMMENT ON COLUMN reportes_widgets.tipo IS 'Tipo de widget: CHART, TABLE, KPI, MAP, LIST, CALENDAR, CUSTOM';
COMMENT ON COLUMN reportes_widgets.configuracion IS 'Configuración específica del widget (query, campos, colores, etc.)';
COMMENT ON COLUMN reportes_widgets.posicion IS 'Posición y tamaño en el dashboard: {x, y, width, height}';

-- =====================================================================================================================
-- FIN DE MIGRACIÓN V1__initial_schema.sql
-- =====================================================================================================================
