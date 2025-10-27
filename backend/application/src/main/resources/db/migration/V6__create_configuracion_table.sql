-------------------------------------------------------------------------------
-- Migración V6: Crear Tabla de Configuración del Sistema
--
-- Descripción:
-- Crea la tabla para almacenar configuraciones del sistema con patrón key-value
-- Soporta múltiples tipos de datos y categorización
--
-- Autor: PagoDirecto CRM Team
-- Fecha: 2025-10-13
-- Version: 6
-------------------------------------------------------------------------------

-- Crear tabla de configuraciones
CREATE TABLE IF NOT EXISTS configuracion_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unidad_negocio_id UUID,
    clave VARCHAR(100) NOT NULL UNIQUE,
    valor TEXT,
    categoria VARCHAR(30) NOT NULL,
    tipo_dato VARCHAR(20) NOT NULL DEFAULT 'STRING',
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    valor_por_defecto TEXT,
    es_publica BOOLEAN NOT NULL DEFAULT false,
    es_modificable BOOLEAN NOT NULL DEFAULT true,
    validacion_regex VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_configuracion_created_by FOREIGN KEY (created_by) REFERENCES seguridad_usuarios(id),
    CONSTRAINT fk_configuracion_updated_by FOREIGN KEY (updated_by) REFERENCES seguridad_usuarios(id)
);

-- Índices
CREATE INDEX idx_configuracion_clave ON configuracion_settings(clave) WHERE deleted_at IS NULL;
CREATE INDEX idx_configuracion_categoria ON configuracion_settings(categoria) WHERE deleted_at IS NULL;
CREATE INDEX idx_configuracion_unidad_negocio ON configuracion_settings(unidad_negocio_id) WHERE deleted_at IS NULL;

-- Comentarios
COMMENT ON TABLE configuracion_settings IS 'Configuraciones del sistema con patrón key-value';
COMMENT ON COLUMN configuracion_settings.clave IS 'Clave única de la configuración (ej: general.nombre_empresa)';
COMMENT ON COLUMN configuracion_settings.valor IS 'Valor actual de la configuración';
COMMENT ON COLUMN configuracion_settings.categoria IS 'Categoría de la configuración (GENERAL, NOTIFICACIONES, INTEGRACIONES, SEGURIDAD)';
COMMENT ON COLUMN configuracion_settings.tipo_dato IS 'Tipo de dato del valor (STRING, INTEGER, BOOLEAN, DECIMAL, JSON, URL, EMAIL, PHONE)';
COMMENT ON COLUMN configuracion_settings.es_publica IS 'Indica si la configuración es visible públicamente';
COMMENT ON COLUMN configuracion_settings.es_modificable IS 'Indica si la configuración puede ser modificada';

-------------------------------------------------------------------------------
-- Insertar configuraciones por defecto
-------------------------------------------------------------------------------

-- GENERAL
INSERT INTO configuracion_settings (clave, valor, categoria, tipo_dato, nombre, descripcion, valor_por_defecto, es_publica, es_modificable)
VALUES
    ('general.nombre_empresa', 'PagoDirecto CRM', 'GENERAL', 'STRING', 'Nombre de la empresa', 'Nombre de la empresa o negocio', 'PagoDirecto CRM', true, true),
    ('general.logo_url', '', 'GENERAL', 'URL', 'URL del logo', 'URL de la imagen del logo de la empresa', '', true, true),
    ('general.zona_horaria', 'America/Mexico_City', 'GENERAL', 'STRING', 'Zona horaria', 'Zona horaria por defecto del sistema', 'America/Mexico_City', true, true),
    ('general.moneda', 'MXN', 'GENERAL', 'STRING', 'Moneda', 'Moneda por defecto del sistema', 'MXN', true, true),
    ('general.idioma', 'es-MX', 'GENERAL', 'STRING', 'Idioma', 'Idioma por defecto del sistema', 'es-MX', true, true),
    ('general.formato_fecha', 'dd/MM/yyyy', 'GENERAL', 'STRING', 'Formato de fecha', 'Formato de visualización de fechas', 'dd/MM/yyyy', true, true),
    ('general.formato_hora', 'HH:mm:ss', 'GENERAL', 'STRING', 'Formato de hora', 'Formato de visualización de horas', 'HH:mm:ss', true, true),
    ('general.telefono_contacto', '', 'GENERAL', 'PHONE', 'Teléfono de contacto', 'Teléfono principal de la empresa', '', true, true),
    ('general.email_contacto', '', 'GENERAL', 'EMAIL', 'Email de contacto', 'Correo electrónico principal de la empresa', '', true, true),
    ('general.direccion', '', 'GENERAL', 'STRING', 'Dirección', 'Dirección física de la empresa', '', true, true);

-- NOTIFICACIONES
INSERT INTO configuracion_settings (clave, valor, categoria, tipo_dato, nombre, descripcion, valor_por_defecto, es_publica, es_modificable)
VALUES
    ('notif.email_habilitado', 'true', 'NOTIFICACIONES', 'BOOLEAN', 'Email habilitado', 'Habilitar notificaciones por email', 'true', false, true),
    ('notif.smtp_host', 'smtp.gmail.com', 'NOTIFICACIONES', 'STRING', 'Servidor SMTP', 'Servidor SMTP para envío de correos', 'smtp.gmail.com', false, true),
    ('notif.smtp_port', '587', 'NOTIFICACIONES', 'INTEGER', 'Puerto SMTP', 'Puerto del servidor SMTP', '587', false, true),
    ('notif.smtp_username', '', 'NOTIFICACIONES', 'STRING', 'Usuario SMTP', 'Usuario para autenticación SMTP', '', false, true),
    ('notif.smtp_tls', 'true', 'NOTIFICACIONES', 'BOOLEAN', 'SMTP TLS', 'Usar TLS para SMTP', 'true', false, true),
    ('notif.email_from', 'noreply@pagodirecto.com', 'NOTIFICACIONES', 'EMAIL', 'Email remitente', 'Dirección de correo remitente', 'noreply@pagodirecto.com', false, true),
    ('notif.email_from_name', 'PagoDirecto CRM', 'NOTIFICACIONES', 'STRING', 'Nombre remitente', 'Nombre del remitente de emails', 'PagoDirecto CRM', false, true),
    ('notif.push_habilitado', 'false', 'NOTIFICACIONES', 'BOOLEAN', 'Push habilitado', 'Habilitar notificaciones push', 'false', false, true),
    ('notif.fcm_api_key', '', 'NOTIFICACIONES', 'STRING', 'FCM API Key', 'API Key de Firebase Cloud Messaging', '', false, true),
    ('notif.sms_habilitado', 'false', 'NOTIFICACIONES', 'BOOLEAN', 'SMS habilitado', 'Habilitar notificaciones SMS', 'false', false, true),
    ('notif.sms_proveedor', 'twilio', 'NOTIFICACIONES', 'STRING', 'Proveedor SMS', 'Proveedor de servicios SMS', 'twilio', false, true),
    ('notif.sms_account_sid', '', 'NOTIFICACIONES', 'STRING', 'SMS Account SID', 'Account SID del proveedor SMS', '', false, true),
    ('notif.sms_from', '', 'NOTIFICACIONES', 'PHONE', 'SMS remitente', 'Número telefónico remitente', '', false, true),
    ('notif.nuevos_clientes', 'true', 'NOTIFICACIONES', 'BOOLEAN', 'Notificar nuevos clientes', 'Enviar notificación al crear cliente', 'true', false, true),
    ('notif.nuevas_oportunidades', 'true', 'NOTIFICACIONES', 'BOOLEAN', 'Notificar nuevas oportunidades', 'Enviar notificación al crear oportunidad', 'true', false, true),
    ('notif.tareas_vencidas', 'true', 'NOTIFICACIONES', 'BOOLEAN', 'Notificar tareas vencidas', 'Enviar notificación de tareas vencidas', 'true', false, true),
    ('notif.nuevas_ventas', 'true', 'NOTIFICACIONES', 'BOOLEAN', 'Notificar nuevas ventas', 'Enviar notificación al registrar venta', 'true', false, true);

-- INTEGRACIONES
INSERT INTO configuracion_settings (clave, valor, categoria, tipo_dato, nombre, descripcion, valor_por_defecto, es_publica, es_modificable)
VALUES
    ('integ.google_habilitado', 'false', 'INTEGRACIONES', 'BOOLEAN', 'Google habilitado', 'Habilitar integración con Google', 'false', false, true),
    ('integ.google_client_id', '', 'INTEGRACIONES', 'STRING', 'Google Client ID', 'Client ID de Google OAuth', '', false, true),
    ('integ.google_calendar', 'false', 'INTEGRACIONES', 'BOOLEAN', 'Google Calendar', 'Habilitar sincronización con Google Calendar', 'false', false, true),
    ('integ.pagos_habilitado', 'false', 'INTEGRACIONES', 'BOOLEAN', 'Pasarela de pagos', 'Habilitar pasarela de pagos', 'false', false, true),
    ('integ.pagos_proveedor', 'stripe', 'INTEGRACIONES', 'STRING', 'Proveedor de pagos', 'Proveedor de pasarela de pagos', 'stripe', false, true),
    ('integ.pagos_api_key', '', 'INTEGRACIONES', 'STRING', 'API Key de pagos', 'API Key del proveedor de pagos', '', false, true),
    ('integ.pagos_webhook_url', '', 'INTEGRACIONES', 'URL', 'Webhook de pagos', 'URL para recibir webhooks de pagos', '', false, true),
    ('integ.webhooks_habilitado', 'false', 'INTEGRACIONES', 'BOOLEAN', 'Webhooks habilitado', 'Habilitar envío de webhooks', 'false', false, true),
    ('integ.webhook_clientes_url', '', 'INTEGRACIONES', 'URL', 'Webhook de clientes', 'URL para webhooks de clientes', '', false, true),
    ('integ.webhook_oportunidades_url', '', 'INTEGRACIONES', 'URL', 'Webhook de oportunidades', 'URL para webhooks de oportunidades', '', false, true),
    ('integ.webhook_ventas_url', '', 'INTEGRACIONES', 'URL', 'Webhook de ventas', 'URL para webhooks de ventas', '', false, true),
    ('integ.webhook_secret', '', 'INTEGRACIONES', 'STRING', 'Webhook secret', 'Secret para firma de webhooks', '', false, true),
    ('integ.storage_proveedor', 'local', 'INTEGRACIONES', 'STRING', 'Proveedor de almacenamiento', 'Proveedor para almacenamiento de archivos', 'local', false, true),
    ('integ.s3_bucket_name', '', 'INTEGRACIONES', 'STRING', 'S3 Bucket', 'Nombre del bucket de AWS S3', '', false, true),
    ('integ.s3_region', 'us-east-1', 'INTEGRACIONES', 'STRING', 'S3 Region', 'Región de AWS S3', 'us-east-1', false, true),
    ('integ.api_externa_url', '', 'INTEGRACIONES', 'URL', 'API externa URL', 'URL de API externa', '', false, true),
    ('integ.api_externa_key', '', 'INTEGRACIONES', 'STRING', 'API externa key', 'API Key para API externa', '', false, true),
    ('integ.api_timeout', '30', 'INTEGRACIONES', 'INTEGER', 'Timeout de API', 'Timeout en segundos para llamadas API', '30', false, true);

-- SEGURIDAD
INSERT INTO configuracion_settings (clave, valor, categoria, tipo_dato, nombre, descripcion, valor_por_defecto, es_publica, es_modificable)
VALUES
    ('seg.password_min_length', '8', 'SEGURIDAD', 'INTEGER', 'Longitud mínima de contraseña', 'Cantidad mínima de caracteres', '8', false, true),
    ('seg.password_req_mayusculas', 'true', 'SEGURIDAD', 'BOOLEAN', 'Requiere mayúsculas', 'Contraseña debe incluir mayúsculas', 'true', false, true),
    ('seg.password_req_minusculas', 'true', 'SEGURIDAD', 'BOOLEAN', 'Requiere minúsculas', 'Contraseña debe incluir minúsculas', 'true', false, true),
    ('seg.password_req_numeros', 'true', 'SEGURIDAD', 'BOOLEAN', 'Requiere números', 'Contraseña debe incluir números', 'true', false, true),
    ('seg.password_req_especiales', 'true', 'SEGURIDAD', 'BOOLEAN', 'Requiere caracteres especiales', 'Contraseña debe incluir caracteres especiales', 'true', false, true),
    ('seg.password_dias_expiracion', '90', 'SEGURIDAD', 'INTEGER', 'Días de expiración', 'Días antes de que expire la contraseña', '90', false, true),
    ('seg.password_historial', '5', 'SEGURIDAD', 'INTEGER', 'Historial de contraseñas', 'No repetir las últimas N contraseñas', '5', false, true),
    ('seg.session_duracion', '60', 'SEGURIDAD', 'INTEGER', 'Duración de sesión', 'Duración de sesión en minutos', '60', false, true),
    ('seg.session_timeout_inactividad', '30', 'SEGURIDAD', 'INTEGER', 'Timeout de inactividad', 'Minutos de inactividad antes de cerrar sesión', '30', false, true),
    ('seg.session_max_simultaneas', '3', 'SEGURIDAD', 'INTEGER', 'Sesiones simultáneas', 'Máximo de sesiones simultáneas por usuario', '3', false, true),
    ('seg.login_max_intentos', '5', 'SEGURIDAD', 'INTEGER', 'Intentos de login', 'Máximo de intentos fallidos de login', '5', false, true),
    ('seg.login_duracion_bloqueo', '30', 'SEGURIDAD', 'INTEGER', 'Duración de bloqueo', 'Minutos de bloqueo tras intentos fallidos', '30', false, true),
    ('seg.mfa_obligatorio', 'false', 'SEGURIDAD', 'BOOLEAN', 'MFA obligatorio', 'MFA obligatorio para todos los usuarios', 'false', false, true),
    ('seg.mfa_obligatorio_admins', 'true', 'SEGURIDAD', 'BOOLEAN', 'MFA para admins', 'MFA obligatorio para administradores', 'true', false, true),
    ('seg.ip_restriccion_habilitada', 'false', 'SEGURIDAD', 'BOOLEAN', 'Restricción por IP', 'Habilitar restricción de acceso por IP', 'false', false, true),
    ('seg.ip_lista_permitidas', '', 'SEGURIDAD', 'STRING', 'IPs permitidas', 'Lista de IPs permitidas (separadas por coma)', '', false, true),
    ('seg.audit_habilitado', 'true', 'SEGURIDAD', 'BOOLEAN', 'Auditoría habilitada', 'Habilitar registro de auditoría', 'true', false, false),
    ('seg.audit_retencion_dias', '365', 'SEGURIDAD', 'INTEGER', 'Retención de auditoría', 'Días de retención de logs de auditoría', '365', false, true),
    ('seg.audit_datos_sensibles', 'true', 'SEGURIDAD', 'BOOLEAN', 'Auditar datos sensibles', 'Registrar acceso a datos sensibles', 'true', false, false),
    ('seg.cors_origenes', '*', 'SEGURIDAD', 'STRING', 'CORS orígenes', 'Orígenes permitidos para CORS', '*', false, true),
    ('seg.rate_limit_habilitado', 'true', 'SEGURIDAD', 'BOOLEAN', 'Rate limiting', 'Habilitar límite de peticiones', 'true', false, true),
    ('seg.rate_limit_max_requests', '100', 'SEGURIDAD', 'INTEGER', 'Max requests', 'Máximo de peticiones por minuto', '100', false, true);

-------------------------------------------------------------------------------
-- Validaciones post-migración
-------------------------------------------------------------------------------

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'configuracion_settings') THEN
        RAISE EXCEPTION 'Error: tabla configuracion_settings no fue creada';
    END IF;

    -- Verificar que se insertaron las configuraciones por defecto
    IF (SELECT COUNT(*) FROM configuracion_settings) < 50 THEN
        RAISE WARNING 'Advertencia: Se esperaban al menos 50 configuraciones por defecto';
    END IF;

    RAISE NOTICE 'Migración V6 completada exitosamente: Tabla de configuración creada con % registros', (SELECT COUNT(*) FROM configuracion_settings);
END $$;
