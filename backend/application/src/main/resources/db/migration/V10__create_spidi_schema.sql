-- =====================================================================================================================
-- V10__create_spidi_schema.sql
-- Migración para el módulo Spidi - Real-Time Presence Monitoring
-- Fecha: 2025-10-13
-- Autor: Database Architecture Team
-- Descripción: Esquema completo para monitoreo de presencia en tiempo real de arquitectura Spidi
-- Referencia: ADR-0001 (docs/adrs/0001-spidi-presence-monitoring-architecture.md)
-- =====================================================================================================================

-- =====================================================================================================================
-- DOMINIO: SPIDI (Real-Time Presence Monitoring)
-- Contexto delimitado para monitoreo de salas de comunicación (nodos Spidi)
-- =====================================================================================================================

-- Tabla: tba_spd_room_type
-- Propósito: Catálogo de tipos de salas (WebRTC, Chat, Notification, etc.)
-- Notas: Tabla de catálogo inmutable después de seeding inicial
CREATE TABLE tba_spd_room_type (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon VARCHAR(50),
    color VARCHAR(7),
    default_capacity INTEGER NOT NULL DEFAULT 100,
    default_ttl_seconds INTEGER NOT NULL DEFAULT 3600,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    CONSTRAINT fk_tba_spd_room_type_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_tba_spd_room_type_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_tba_spd_room_type_capacity CHECK (default_capacity > 0),
    CONSTRAINT chk_tba_spd_room_type_ttl CHECK (default_ttl_seconds >= 60 AND default_ttl_seconds <= 86400),
    CONSTRAINT chk_tba_spd_room_type_color CHECK (color IS NULL OR color ~ '^#[0-9A-Fa-f]{6}$')
);

CREATE INDEX idx_tba_spd_room_type_code ON tba_spd_room_type(code);

COMMENT ON TABLE tba_spd_room_type IS 'Catálogo de tipos de salas Spidi (WebRTC, Chat, Notification, etc.)';
COMMENT ON COLUMN tba_spd_room_type.code IS 'Código único del tipo (e.g., WEBRTC, CHAT, NOTIFICATION)';
COMMENT ON COLUMN tba_spd_room_type.default_capacity IS 'Capacidad máxima por defecto para salas de este tipo';
COMMENT ON COLUMN tba_spd_room_type.default_ttl_seconds IS 'TTL de sesión por defecto en segundos (60-86400)';
COMMENT ON COLUMN tba_spd_room_type.color IS 'Color hexadecimal para UI (#RRGGBB)';

-- Tabla: dat_spd_room
-- Propósito: Definición de salas de comunicación
-- Notas: Soft delete habilitado, particionable por unidad_negocio_id para multi-tenancy
CREATE TABLE dat_spd_room (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unidad_negocio_id UUID NOT NULL,
    room_type_id UUID NOT NULL,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    capacity INTEGER NOT NULL DEFAULT 100,
    ttl_seconds INTEGER NOT NULL DEFAULT 3600,
    tags JSONB,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_dat_spd_room_room_type FOREIGN KEY (room_type_id) REFERENCES tba_spd_room_type(id),
    CONSTRAINT fk_dat_spd_room_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_dat_spd_room_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_dat_spd_room_status CHECK (status IN ('ACTIVE', 'MAINTENANCE', 'DISABLED', 'ARCHIVED')),
    CONSTRAINT chk_dat_spd_room_capacity CHECK (capacity > 0 AND capacity <= 10000),
    CONSTRAINT chk_dat_spd_room_ttl CHECK (ttl_seconds >= 60 AND ttl_seconds <= 86400),
    CONSTRAINT uk_dat_spd_room_code UNIQUE (unidad_negocio_id, code)
);

CREATE INDEX idx_dat_spd_room_unidad_negocio ON dat_spd_room(unidad_negocio_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_dat_spd_room_code ON dat_spd_room(code) WHERE deleted_at IS NULL;
CREATE INDEX idx_dat_spd_room_status ON dat_spd_room(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_dat_spd_room_type ON dat_spd_room(room_type_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_dat_spd_room_tags ON dat_spd_room USING gin(tags) WHERE deleted_at IS NULL;

COMMENT ON TABLE dat_spd_room IS 'Salas de comunicación Spidi para monitoreo de presencia';
COMMENT ON COLUMN dat_spd_room.code IS 'Código único de la sala (e.g., ROOM-EVENT-001, LOBBY-MAIN)';
COMMENT ON COLUMN dat_spd_room.status IS 'Estado: ACTIVE (operativa), MAINTENANCE (mantenimiento), DISABLED (deshabilitada), ARCHIVED (archivada)';
COMMENT ON COLUMN dat_spd_room.capacity IS 'Capacidad máxima de usuarios concurrentes (1-10000)';
COMMENT ON COLUMN dat_spd_room.ttl_seconds IS 'TTL de sesión en segundos (60-86400, típicamente 3600 = 1 hora)';
COMMENT ON COLUMN dat_spd_room.tags IS 'Etiquetas para clasificación y búsqueda: ["evento", "publico", "premium"]';
COMMENT ON COLUMN dat_spd_room.metadata IS 'Metadatos adicionales específicos del tipo de sala';

-- Tabla: dat_spd_room_attr
-- Propósito: Atributos personalizados de salas (patrón EAV)
-- Notas: Para extensibilidad sin cambios de esquema
CREATE TABLE dat_spd_room_attr (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID NOT NULL,
    attr_key VARCHAR(100) NOT NULL,
    attr_value TEXT NOT NULL,
    data_type VARCHAR(20) NOT NULL DEFAULT 'STRING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    CONSTRAINT fk_dat_spd_room_attr_room FOREIGN KEY (room_id) REFERENCES dat_spd_room(id) ON DELETE CASCADE,
    CONSTRAINT fk_dat_spd_room_attr_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_dat_spd_room_attr_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_dat_spd_room_attr_data_type CHECK (data_type IN ('STRING', 'INTEGER', 'FLOAT', 'BOOLEAN', 'JSON', 'DATE')),
    CONSTRAINT uk_dat_spd_room_attr_key UNIQUE (room_id, attr_key)
);

CREATE INDEX idx_dat_spd_room_attr_room ON dat_spd_room_attr(room_id);
CREATE INDEX idx_dat_spd_room_attr_key ON dat_spd_room_attr(attr_key);

COMMENT ON TABLE dat_spd_room_attr IS 'Atributos personalizados de salas (patrón EAV para extensibilidad)';
COMMENT ON COLUMN dat_spd_room_attr.attr_key IS 'Clave del atributo (e.g., max_video_quality, enable_recording)';
COMMENT ON COLUMN dat_spd_room_attr.attr_value IS 'Valor del atributo (almacenado como texto, convertir según data_type)';
COMMENT ON COLUMN dat_spd_room_attr.data_type IS 'Tipo de dato: STRING, INTEGER, FLOAT, BOOLEAN, JSON, DATE';

-- Tabla: dat_spd_session
-- Propósito: Sesiones de conexión de usuarios a salas
-- Notas: Particionable por started_at (mensual) para gestión de retención de datos
CREATE TABLE dat_spd_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID NOT NULL,
    user_id UUID,
    client_id VARCHAR(255) NOT NULL,
    device VARCHAR(100),
    os VARCHAR(100),
    app_version VARCHAR(50),
    ip_address INET NOT NULL,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_heartbeat_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ended_at TIMESTAMPTZ,
    avg_latency_ms INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    disconnect_reason VARCHAR(100),
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_dat_spd_session_room FOREIGN KEY (room_id) REFERENCES dat_spd_room(id) ON DELETE CASCADE,
    CONSTRAINT fk_dat_spd_session_user FOREIGN KEY (user_id) REFERENCES seguridad_usuarios(id) ON DELETE SET NULL,
    CONSTRAINT chk_dat_spd_session_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'DISCONNECTED', 'TERMINATED')),
    CONSTRAINT chk_dat_spd_session_latency CHECK (avg_latency_ms IS NULL OR avg_latency_ms >= 0),
    CONSTRAINT chk_dat_spd_session_timestamps CHECK (ended_at IS NULL OR ended_at >= started_at)
);

CREATE INDEX idx_dat_spd_session_room ON dat_spd_session(room_id);
CREATE INDEX idx_dat_spd_session_user ON dat_spd_session(user_id);
CREATE INDEX idx_dat_spd_session_client ON dat_spd_session(client_id);
CREATE INDEX idx_dat_spd_session_started ON dat_spd_session(started_at);
CREATE INDEX idx_dat_spd_session_last_heartbeat ON dat_spd_session(last_heartbeat_at) WHERE status = 'ACTIVE';
CREATE INDEX idx_dat_spd_session_status ON dat_spd_session(status);
CREATE INDEX idx_dat_spd_session_active_room ON dat_spd_session(room_id, status) WHERE status = 'ACTIVE';

COMMENT ON TABLE dat_spd_session IS 'Sesiones de conexión de usuarios a salas con tracking de heartbeat';
COMMENT ON COLUMN dat_spd_session.client_id IS 'ID único del cliente (UUID generado por cliente, para múltiples sesiones de un usuario)';
COMMENT ON COLUMN dat_spd_session.device IS 'Información del dispositivo (e.g., iPhone 13, Chrome on Windows)';
COMMENT ON COLUMN dat_spd_session.os IS 'Sistema operativo (e.g., iOS 16.4, Windows 11)';
COMMENT ON COLUMN dat_spd_session.app_version IS 'Versión de la aplicación cliente (e.g., 2.5.1)';
COMMENT ON COLUMN dat_spd_session.last_heartbeat_at IS 'Última señal de vida recibida (timeout después de 45 segundos)';
COMMENT ON COLUMN dat_spd_session.avg_latency_ms IS 'Latencia promedio en milisegundos (rolling average)';
COMMENT ON COLUMN dat_spd_session.disconnect_reason IS 'Razón de desconexión: TIMEOUT, USER_LEAVE, SERVER_SHUTDOWN, ERROR';

-- Tabla: dat_spd_room_stats
-- Propósito: Estadísticas agregadas de salas por intervalos de tiempo
-- Notas: Particionable por ts_bucket (trimestral) para análisis histórico
CREATE TABLE dat_spd_room_stats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID NOT NULL,
    ts_bucket TIMESTAMPTZ NOT NULL,
    bucket_interval VARCHAR(20) NOT NULL DEFAULT 'HOUR',
    count_online INTEGER NOT NULL DEFAULT 0,
    peak_online INTEGER NOT NULL DEFAULT 0,
    avg_latency_ms INTEGER,
    total_sessions INTEGER NOT NULL DEFAULT 0,
    total_connects INTEGER NOT NULL DEFAULT 0,
    total_disconnects INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_dat_spd_room_stats_room FOREIGN KEY (room_id) REFERENCES dat_spd_room(id) ON DELETE CASCADE,
    CONSTRAINT chk_dat_spd_room_stats_interval CHECK (bucket_interval IN ('MINUTE', 'HOUR', 'DAY', 'WEEK', 'MONTH')),
    CONSTRAINT chk_dat_spd_room_stats_counts CHECK (count_online >= 0 AND peak_online >= 0 AND total_sessions >= 0),
    CONSTRAINT chk_dat_spd_room_stats_latency CHECK (avg_latency_ms IS NULL OR avg_latency_ms >= 0),
    CONSTRAINT uk_dat_spd_room_stats_bucket UNIQUE (room_id, ts_bucket, bucket_interval)
);

CREATE INDEX idx_dat_spd_room_stats_room ON dat_spd_room_stats(room_id);
CREATE INDEX idx_dat_spd_room_stats_ts_bucket ON dat_spd_room_stats(ts_bucket);
CREATE INDEX idx_dat_spd_room_stats_room_ts ON dat_spd_room_stats(room_id, ts_bucket);

COMMENT ON TABLE dat_spd_room_stats IS 'Estadísticas agregadas de salas por intervalos de tiempo (time-series)';
COMMENT ON COLUMN dat_spd_room_stats.ts_bucket IS 'Timestamp del bucket (truncado según interval: hora, día, etc.)';
COMMENT ON COLUMN dat_spd_room_stats.bucket_interval IS 'Intervalo de agregación: MINUTE, HOUR, DAY, WEEK, MONTH';
COMMENT ON COLUMN dat_spd_room_stats.count_online IS 'Usuarios online al final del bucket';
COMMENT ON COLUMN dat_spd_room_stats.peak_online IS 'Pico de usuarios concurrentes durante el bucket';
COMMENT ON COLUMN dat_spd_room_stats.avg_latency_ms IS 'Latencia promedio durante el bucket';
COMMENT ON COLUMN dat_spd_room_stats.total_sessions IS 'Total de sesiones activas durante el bucket';

-- Tabla: dat_spd_alert_rule
-- Propósito: Definición de reglas de alertas para salas
-- Notas: Motor de reglas con expresiones Groovy para flexibilidad
CREATE TABLE dat_spd_alert_rule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unidad_negocio_id UUID NOT NULL,
    room_id UUID,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    rule_type VARCHAR(50) NOT NULL,
    condition_expression TEXT NOT NULL,
    threshold_value NUMERIC(15,2),
    severity VARCHAR(20) NOT NULL DEFAULT 'WARNING',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    rate_limit_minutes INTEGER DEFAULT 5,
    notification_channels JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_dat_spd_alert_rule_room FOREIGN KEY (room_id) REFERENCES dat_spd_room(id) ON DELETE CASCADE,
    CONSTRAINT fk_dat_spd_alert_rule_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_dat_spd_alert_rule_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_dat_spd_alert_rule_type CHECK (rule_type IN ('CAPACITY', 'LATENCY', 'HEARTBEAT', 'CUSTOM')),
    CONSTRAINT chk_dat_spd_alert_rule_severity CHECK (severity IN ('INFO', 'WARNING', 'ERROR', 'CRITICAL')),
    CONSTRAINT chk_dat_spd_alert_rule_rate_limit CHECK (rate_limit_minutes >= 1)
);

CREATE INDEX idx_dat_spd_alert_rule_room ON dat_spd_alert_rule(room_id) WHERE deleted_at IS NULL AND enabled = true;
CREATE INDEX idx_dat_spd_alert_rule_enabled ON dat_spd_alert_rule(enabled) WHERE deleted_at IS NULL;
CREATE INDEX idx_dat_spd_alert_rule_unidad_negocio ON dat_spd_alert_rule(unidad_negocio_id) WHERE deleted_at IS NULL;

COMMENT ON TABLE dat_spd_alert_rule IS 'Reglas de alertas para monitoreo de salas';
COMMENT ON COLUMN dat_spd_alert_rule.room_id IS 'Sala específica (NULL = aplica a todas las salas de la unidad de negocio)';
COMMENT ON COLUMN dat_spd_alert_rule.rule_type IS 'Tipo: CAPACITY (capacidad), LATENCY (latencia alta), HEARTBEAT (sin heartbeat), CUSTOM (expresión)';
COMMENT ON COLUMN dat_spd_alert_rule.condition_expression IS 'Expresión Groovy para evaluar condición (e.g., "online_count / capacity > 0.8")';
COMMENT ON COLUMN dat_spd_alert_rule.threshold_value IS 'Valor umbral (significado depende del rule_type)';
COMMENT ON COLUMN dat_spd_alert_rule.severity IS 'Severidad: INFO, WARNING, ERROR, CRITICAL';
COMMENT ON COLUMN dat_spd_alert_rule.rate_limit_minutes IS 'Minutos mínimos entre alertas consecutivas (evita spam)';
COMMENT ON COLUMN dat_spd_alert_rule.notification_channels IS 'Canales de notificación: ["in_app", "webhook", "email"]';

-- Tabla: dat_spd_alert_event
-- Propósito: Historial de alertas disparadas
-- Notas: Append-only para auditoría, particionable por created_at (mensual)
CREATE TABLE dat_spd_alert_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alert_rule_id UUID NOT NULL,
    room_id UUID NOT NULL,
    severity VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    current_value NUMERIC(15,2),
    threshold_value NUMERIC(15,2),
    metadata JSONB,
    acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    acknowledged_at TIMESTAMPTZ,
    acknowledged_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_dat_spd_alert_event_rule FOREIGN KEY (alert_rule_id) REFERENCES dat_spd_alert_rule(id) ON DELETE CASCADE,
    CONSTRAINT fk_dat_spd_alert_event_room FOREIGN KEY (room_id) REFERENCES dat_spd_room(id) ON DELETE CASCADE,
    CONSTRAINT fk_dat_spd_alert_event_ack_by FOREIGN KEY (acknowledged_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT chk_dat_spd_alert_event_severity CHECK (severity IN ('INFO', 'WARNING', 'ERROR', 'CRITICAL'))
);

CREATE INDEX idx_dat_spd_alert_event_rule ON dat_spd_alert_event(alert_rule_id);
CREATE INDEX idx_dat_spd_alert_event_room ON dat_spd_alert_event(room_id);
CREATE INDEX idx_dat_spd_alert_event_created ON dat_spd_alert_event(created_at);
CREATE INDEX idx_dat_spd_alert_event_unack ON dat_spd_alert_event(acknowledged) WHERE acknowledged = false;
CREATE INDEX idx_dat_spd_alert_event_severity ON dat_spd_alert_event(severity);

COMMENT ON TABLE dat_spd_alert_event IS 'Historial de alertas disparadas (append-only para auditoría)';
COMMENT ON COLUMN dat_spd_alert_event.current_value IS 'Valor actual que disparó la alerta';
COMMENT ON COLUMN dat_spd_alert_event.threshold_value IS 'Valor umbral definido en la regla';
COMMENT ON COLUMN dat_spd_alert_event.acknowledged IS 'Indica si la alerta fue reconocida por un operador';
COMMENT ON COLUMN dat_spd_alert_event.metadata IS 'Contexto adicional (usuarios afectados, métricas, etc.)';

-- =====================================================================================================================
-- TRIGGERS Y FUNCIONES
-- =====================================================================================================================

-- Función: Actualizar updated_at automáticamente
CREATE OR REPLACE FUNCTION update_spd_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Aplicar trigger a todas las tablas con updated_at
CREATE TRIGGER trg_tba_spd_room_type_updated_at
    BEFORE UPDATE ON tba_spd_room_type
    FOR EACH ROW EXECUTE FUNCTION update_spd_updated_at();

CREATE TRIGGER trg_dat_spd_room_updated_at
    BEFORE UPDATE ON dat_spd_room
    FOR EACH ROW EXECUTE FUNCTION update_spd_updated_at();

CREATE TRIGGER trg_dat_spd_room_attr_updated_at
    BEFORE UPDATE ON dat_spd_room_attr
    FOR EACH ROW EXECUTE FUNCTION update_spd_updated_at();

CREATE TRIGGER trg_dat_spd_session_updated_at
    BEFORE UPDATE ON dat_spd_session
    FOR EACH ROW EXECUTE FUNCTION update_spd_updated_at();

CREATE TRIGGER trg_dat_spd_room_stats_updated_at
    BEFORE UPDATE ON dat_spd_room_stats
    FOR EACH ROW EXECUTE FUNCTION update_spd_updated_at();

CREATE TRIGGER trg_dat_spd_alert_rule_updated_at
    BEFORE UPDATE ON dat_spd_alert_rule
    FOR EACH ROW EXECUTE FUNCTION update_spd_updated_at();

-- =====================================================================================================================
-- VISTAS MATERIALIZADAS (para dashboards de alto rendimiento)
-- =====================================================================================================================

-- Vista: Resumen en tiempo real de salas activas
CREATE MATERIALIZED VIEW mv_spd_room_summary AS
SELECT
    r.id AS room_id,
    r.code AS room_code,
    r.name AS room_name,
    r.capacity,
    rt.name AS room_type_name,
    COUNT(s.id) FILTER (WHERE s.status = 'ACTIVE') AS online_count,
    AVG(s.avg_latency_ms) FILTER (WHERE s.status = 'ACTIVE') AS avg_latency_ms,
    MAX(s.last_heartbeat_at) FILTER (WHERE s.status = 'ACTIVE') AS last_activity_at,
    ROUND(COUNT(s.id) FILTER (WHERE s.status = 'ACTIVE')::NUMERIC / r.capacity * 100, 2) AS capacity_percent
FROM dat_spd_room r
INNER JOIN tba_spd_room_type rt ON r.room_type_id = rt.id
LEFT JOIN dat_spd_session s ON r.id = s.room_id AND s.status = 'ACTIVE'
WHERE r.deleted_at IS NULL AND r.status = 'ACTIVE'
GROUP BY r.id, r.code, r.name, r.capacity, rt.name;

CREATE UNIQUE INDEX idx_mv_spd_room_summary_room_id ON mv_spd_room_summary(room_id);
CREATE INDEX idx_mv_spd_room_summary_capacity_pct ON mv_spd_room_summary(capacity_percent);

COMMENT ON MATERIALIZED VIEW mv_spd_room_summary IS 'Resumen en tiempo real de salas activas (refrescar cada 30 segundos)';

-- =====================================================================================================================
-- PERMISOS Y SEGURIDAD
-- =====================================================================================================================

-- Crear rol de solo lectura para dashboards (opcional)
-- CREATE ROLE spidi_reader;
-- GRANT SELECT ON ALL TABLES IN SCHEMA public TO spidi_reader;
-- GRANT SELECT ON mv_spd_room_summary TO spidi_reader;

-- Row-Level Security (RLS) para multi-tenancy
ALTER TABLE dat_spd_room ENABLE ROW LEVEL SECURITY;
ALTER TABLE dat_spd_alert_rule ENABLE ROW LEVEL SECURITY;

-- Política RLS: Los usuarios solo ven salas de su unidad de negocio
CREATE POLICY tenant_isolation_spd_room ON dat_spd_room
    USING (unidad_negocio_id = current_setting('app.current_tenant_id', TRUE)::UUID);

CREATE POLICY tenant_isolation_spd_alert_rule ON dat_spd_alert_rule
    USING (unidad_negocio_id = current_setting('app.current_tenant_id', TRUE)::UUID);

-- =====================================================================================================================
-- NOTAS DE IMPLEMENTACIÓN
-- =====================================================================================================================

-- 1. PARTICIONAMIENTO (implementar en producción):
--    - dat_spd_session: Particionar por RANGE(started_at) mensualmente
--    - dat_spd_room_stats: Particionar por RANGE(ts_bucket) trimestralmente
--    - dat_spd_alert_event: Particionar por RANGE(created_at) mensualmente
--
-- 2. RETENCIÓN DE DATOS:
--    - dat_spd_session: 90 días hot, 2 años cold (archivado)
--    - dat_spd_room_stats: Indefinido (datos agregados)
--    - dat_spd_alert_event: 1 año
--
-- 3. MANTENIMIENTO:
--    - REFRESH MATERIALIZED VIEW CONCURRENTLY mv_spd_room_summary cada 30 segundos (job schedulado)
--    - VACUUM ANALYZE diario en tablas de alta escritura (dat_spd_session)
--    - Reindex semanal en índices de alta fragmentación
--
-- 4. REDIS CACHE:
--    - session:{sessionId} → Hash con datos de sesión, TTL 1 hora
--    - room:{roomId}:online → SortedSet de sessionIds activos
--    - room:{roomId}:metrics → Hash con métricas en tiempo real
--
-- 5. MONITOREO:
--    - Query latency para dat_spd_session (objetivo p95 < 50ms)
--    - Tamaño de tabla dat_spd_session (alertar si > 10 millones de filas)
--    - Dead tuples ratio (vacuum si > 20%)

-- =====================================================================================================================
-- FIN DE MIGRACIÓN V10__create_spidi_schema.sql
-- =====================================================================================================================
