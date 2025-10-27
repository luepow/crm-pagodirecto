-- =====================================================================================================================
-- V2__add_indexes.sql
-- Migración de índices para optimización de consultas
-- Fecha: 2025-10-13
-- Autor: Database Architecture Team
-- Descripción: Índices para foreign keys, búsquedas frecuentes, ordenamiento y cobertura
-- =====================================================================================================================

-- =====================================================================================================================
-- DOMINIO: SEGURIDAD (Security & IAM)
-- =====================================================================================================================

-- Índices únicos para seguridad_usuarios
CREATE UNIQUE INDEX uk_seguridad_usuarios_username ON seguridad_usuarios(unidad_negocio_id, LOWER(username)) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uk_seguridad_usuarios_email ON seguridad_usuarios(unidad_negocio_id, LOWER(email)) WHERE deleted_at IS NULL;

-- Índices de búsqueda y filtrado para seguridad_usuarios
CREATE INDEX idx_seguridad_usuarios_unidad ON seguridad_usuarios(unidad_negocio_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_seguridad_usuarios_status ON seguridad_usuarios(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_seguridad_usuarios_ultimo_acceso ON seguridad_usuarios(ultimo_acceso) WHERE deleted_at IS NULL;
CREATE INDEX idx_seguridad_usuarios_bloqueado ON seguridad_usuarios(bloqueado_hasta) WHERE bloqueado_hasta IS NOT NULL;

-- Índices para seguridad_roles
CREATE INDEX idx_seguridad_roles_unidad ON seguridad_roles(unidad_negocio_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_seguridad_roles_departamento ON seguridad_roles(departamento) WHERE deleted_at IS NULL;
CREATE INDEX idx_seguridad_roles_nivel ON seguridad_roles(nivel_jerarquico) WHERE deleted_at IS NULL;

-- Índices únicos para seguridad_permisos
CREATE UNIQUE INDEX uk_seguridad_permisos_recurso_accion ON seguridad_permisos(recurso, accion, COALESCE(scope, '')) WHERE deleted_at IS NULL;

-- Índices de búsqueda para seguridad_permisos
CREATE INDEX idx_seguridad_permisos_recurso ON seguridad_permisos(recurso) WHERE deleted_at IS NULL;
CREATE INDEX idx_seguridad_permisos_scope ON seguridad_permisos(scope) WHERE deleted_at IS NULL AND scope IS NOT NULL;

-- Índices para seguridad_roles_permisos
CREATE INDEX idx_seguridad_roles_permisos_rol ON seguridad_roles_permisos(rol_id);
CREATE INDEX idx_seguridad_roles_permisos_permiso ON seguridad_roles_permisos(permiso_id);

-- Índices para seguridad_usuarios_roles
CREATE INDEX idx_seguridad_usuarios_roles_usuario ON seguridad_usuarios_roles(usuario_id);
CREATE INDEX idx_seguridad_usuarios_roles_rol ON seguridad_usuarios_roles(rol_id);
CREATE INDEX idx_seguridad_usuarios_roles_expiracion ON seguridad_usuarios_roles(fecha_expiracion) WHERE fecha_expiracion IS NOT NULL;

-- Índices para seguridad_refresh_tokens
CREATE UNIQUE INDEX uk_seguridad_refresh_tokens_hash ON seguridad_refresh_tokens(token_hash) WHERE revocado = FALSE;
CREATE INDEX idx_seguridad_refresh_tokens_usuario ON seguridad_refresh_tokens(usuario_id);
CREATE INDEX idx_seguridad_refresh_tokens_expires ON seguridad_refresh_tokens(expires_at) WHERE revocado = FALSE;
-- Note: Cannot use NOW() in index predicate as it's not IMMUTABLE
-- Use query-time filtering instead: WHERE revocado = FALSE AND expires_at < NOW()
CREATE INDEX idx_seguridad_refresh_tokens_cleanup ON seguridad_refresh_tokens(expires_at) WHERE revocado = FALSE;

-- Índices para seguridad_audit_log (tabla de alto volumen)
CREATE INDEX idx_seguridad_audit_log_usuario ON seguridad_audit_log(usuario_id, created_at DESC);
CREATE INDEX idx_seguridad_audit_log_recurso ON seguridad_audit_log(recurso, created_at DESC);
CREATE INDEX idx_seguridad_audit_log_accion ON seguridad_audit_log(accion, created_at DESC);
CREATE INDEX idx_seguridad_audit_log_resultado ON seguridad_audit_log(resultado, created_at DESC) WHERE resultado = 'FAILURE';
CREATE INDEX idx_seguridad_audit_log_created ON seguridad_audit_log(created_at DESC);
-- Índice GIN para búsquedas en metadata JSON
CREATE INDEX idx_seguridad_audit_log_metadata ON seguridad_audit_log USING gin(metadata);

-- =====================================================================================================================
-- DOMINIO: CLIENTES (Clients/CRM)
-- =====================================================================================================================

-- Índices únicos para clientes_clientes
CREATE UNIQUE INDEX uk_clientes_clientes_codigo ON clientes_clientes(unidad_negocio_id, codigo) WHERE deleted_at IS NULL;
CREATE INDEX uk_clientes_clientes_rfc ON clientes_clientes(unidad_negocio_id, rfc) WHERE deleted_at IS NULL AND rfc IS NOT NULL;

-- Índices de búsqueda para clientes_clientes
CREATE INDEX idx_clientes_clientes_unidad ON clientes_clientes(unidad_negocio_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_clientes_clientes_nombre ON clientes_clientes(unidad_negocio_id, LOWER(nombre)) WHERE deleted_at IS NULL;
CREATE INDEX idx_clientes_clientes_email ON clientes_clientes(LOWER(email)) WHERE deleted_at IS NULL AND email IS NOT NULL;
CREATE INDEX idx_clientes_clientes_status ON clientes_clientes(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_clientes_clientes_tipo ON clientes_clientes(tipo) WHERE deleted_at IS NULL;
CREATE INDEX idx_clientes_clientes_segmento ON clientes_clientes(segmento) WHERE deleted_at IS NULL AND segmento IS NOT NULL;
CREATE INDEX idx_clientes_clientes_propietario ON clientes_clientes(propietario_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_clientes_clientes_created ON clientes_clientes(created_at DESC) WHERE deleted_at IS NULL;

-- Índice compuesto para búsquedas por propietario y status
CREATE INDEX idx_clientes_clientes_propietario_status ON clientes_clientes(propietario_id, status) WHERE deleted_at IS NULL;

-- Índices para clientes_contactos
CREATE INDEX idx_clientes_contactos_cliente ON clientes_contactos(cliente_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_clientes_contactos_email ON clientes_contactos(LOWER(email)) WHERE deleted_at IS NULL AND email IS NOT NULL;
CREATE INDEX idx_clientes_contactos_primary ON clientes_contactos(cliente_id) WHERE deleted_at IS NULL AND is_primary = TRUE;

-- Índices para clientes_direcciones
CREATE INDEX idx_clientes_direcciones_cliente ON clientes_direcciones(cliente_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_clientes_direcciones_tipo ON clientes_direcciones(cliente_id, tipo) WHERE deleted_at IS NULL;
CREATE INDEX idx_clientes_direcciones_default ON clientes_direcciones(cliente_id, tipo) WHERE deleted_at IS NULL AND is_default = TRUE;
CREATE INDEX idx_clientes_direcciones_ciudad ON clientes_direcciones(ciudad, estado) WHERE deleted_at IS NULL;

-- =====================================================================================================================
-- DOMINIO: OPORTUNIDADES (Opportunities/Pipeline)
-- =====================================================================================================================

-- Índices para oportunidades_etapas_pipeline
CREATE INDEX idx_oportunidades_etapas_unidad ON oportunidades_etapas_pipeline(unidad_negocio_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_oportunidades_etapas_orden ON oportunidades_etapas_pipeline(unidad_negocio_id, orden) WHERE deleted_at IS NULL;
CREATE INDEX idx_oportunidades_etapas_tipo ON oportunidades_etapas_pipeline(tipo) WHERE deleted_at IS NULL;

-- Índices para oportunidades_oportunidades
CREATE INDEX idx_oportunidades_oportunidades_unidad ON oportunidades_oportunidades(unidad_negocio_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_oportunidades_oportunidades_cliente ON oportunidades_oportunidades(cliente_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_oportunidades_oportunidades_etapa ON oportunidades_oportunidades(etapa_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_oportunidades_oportunidades_propietario ON oportunidades_oportunidades(propietario_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_oportunidades_oportunidades_created ON oportunidades_oportunidades(created_at DESC) WHERE deleted_at IS NULL;

-- Índices compuestos para reportes de pipeline
CREATE INDEX idx_oportunidades_oportunidades_propietario_etapa ON oportunidades_oportunidades(propietario_id, etapa_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_oportunidades_oportunidades_fecha_cierre ON oportunidades_oportunidades(fecha_cierre_estimada) WHERE deleted_at IS NULL AND fecha_cierre_estimada IS NOT NULL;
CREATE INDEX idx_oportunidades_oportunidades_valor ON oportunidades_oportunidades(valor_estimado DESC) WHERE deleted_at IS NULL;

-- Índice de cobertura para cálculos de valor total por etapa
CREATE INDEX idx_oportunidades_oportunidades_etapa_valor ON oportunidades_oportunidades(etapa_id, valor_estimado, probabilidad) WHERE deleted_at IS NULL;

-- Índices para oportunidades_actividades
CREATE INDEX idx_oportunidades_actividades_oportunidad ON oportunidades_actividades(oportunidad_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_oportunidades_actividades_tipo ON oportunidades_actividades(tipo) WHERE deleted_at IS NULL;
CREATE INDEX idx_oportunidades_actividades_fecha ON oportunidades_actividades(fecha_actividad DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_oportunidades_actividades_completada ON oportunidades_actividades(completada, fecha_actividad) WHERE deleted_at IS NULL;

-- =====================================================================================================================
-- DOMINIO: TAREAS (Tasks/Activities)
-- =====================================================================================================================

-- Índices para tareas_tareas
CREATE INDEX idx_tareas_tareas_unidad ON tareas_tareas(unidad_negocio_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_tareas_tareas_asignado ON tareas_tareas(asignado_a, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_tareas_tareas_status ON tareas_tareas(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_tareas_tareas_prioridad ON tareas_tareas(prioridad, fecha_vencimiento) WHERE deleted_at IS NULL;
CREATE INDEX idx_tareas_tareas_vencimiento ON tareas_tareas(fecha_vencimiento) WHERE deleted_at IS NULL AND fecha_vencimiento IS NOT NULL;
CREATE INDEX idx_tareas_tareas_tipo ON tareas_tareas(tipo) WHERE deleted_at IS NULL;

-- Índice polimórfico para relaciones
CREATE INDEX idx_tareas_tareas_relacionado ON tareas_tareas(relacionado_tipo, relacionado_id) WHERE deleted_at IS NULL AND relacionado_id IS NOT NULL;

-- Índice compuesto para dashboard de tareas del usuario
CREATE INDEX idx_tareas_tareas_asignado_vencimiento ON tareas_tareas(asignado_a, fecha_vencimiento, status) WHERE deleted_at IS NULL;

-- Índices para tareas_comentarios
CREATE INDEX idx_tareas_comentarios_tarea ON tareas_comentarios(tarea_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_tareas_comentarios_usuario ON tareas_comentarios(usuario_id, created_at DESC) WHERE deleted_at IS NULL;

-- =====================================================================================================================
-- DOMINIO: PRODUCTOS (Products/Catalog)
-- =====================================================================================================================

-- Índices para productos_categorias (árbol jerárquico)
CREATE INDEX idx_productos_categorias_unidad ON productos_categorias(unidad_negocio_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_productos_categorias_parent ON productos_categorias(parent_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_productos_categorias_nivel ON productos_categorias(nivel, orden) WHERE deleted_at IS NULL;
CREATE INDEX idx_productos_categorias_path ON productos_categorias(path) WHERE deleted_at IS NULL AND path IS NOT NULL;

-- Índices únicos para productos_productos
CREATE UNIQUE INDEX uk_productos_productos_codigo ON productos_productos(unidad_negocio_id, codigo) WHERE deleted_at IS NULL;
CREATE INDEX uk_productos_productos_sku ON productos_productos(sku) WHERE deleted_at IS NULL AND sku IS NOT NULL;
CREATE INDEX uk_productos_productos_barras ON productos_productos(codigo_barras) WHERE deleted_at IS NULL AND codigo_barras IS NOT NULL;

-- Índices de búsqueda para productos_productos
CREATE INDEX idx_productos_productos_unidad ON productos_productos(unidad_negocio_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_productos_productos_nombre ON productos_productos(LOWER(nombre)) WHERE deleted_at IS NULL;
CREATE INDEX idx_productos_productos_categoria ON productos_productos(categoria_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_productos_productos_tipo ON productos_productos(tipo) WHERE deleted_at IS NULL;
CREATE INDEX idx_productos_productos_status ON productos_productos(status) WHERE deleted_at IS NULL;

-- Índice para control de inventario
CREATE INDEX idx_productos_productos_stock_bajo ON productos_productos(stock_actual) WHERE deleted_at IS NULL AND status = 'ACTIVE' AND stock_actual <= stock_minimo;

-- Índice compuesto para búsqueda en catálogo
CREATE INDEX idx_productos_productos_categoria_status ON productos_productos(categoria_id, status, precio_base) WHERE deleted_at IS NULL;

-- Índices para productos_precios
CREATE INDEX idx_productos_precios_producto ON productos_precios(producto_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_productos_precios_tipo ON productos_precios(tipo_precio) WHERE deleted_at IS NULL;
CREATE INDEX idx_productos_precios_vigencia ON productos_precios(fecha_inicio, fecha_fin) WHERE deleted_at IS NULL;
CREATE INDEX idx_productos_precios_segmento ON productos_precios(segmento_cliente) WHERE deleted_at IS NULL AND segmento_cliente IS NOT NULL;

-- Índice compuesto para búsqueda de precio aplicable
CREATE INDEX idx_productos_precios_producto_fecha ON productos_precios(producto_id, fecha_inicio, fecha_fin) WHERE deleted_at IS NULL;

-- =====================================================================================================================
-- DOMINIO: VENTAS (Sales/Quotes)
-- =====================================================================================================================

-- Índices únicos para ventas_cotizaciones
CREATE UNIQUE INDEX uk_ventas_cotizaciones_numero ON ventas_cotizaciones(unidad_negocio_id, numero) WHERE deleted_at IS NULL;

-- Índices de búsqueda para ventas_cotizaciones
CREATE INDEX idx_ventas_cotizaciones_unidad ON ventas_cotizaciones(unidad_negocio_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_ventas_cotizaciones_cliente ON ventas_cotizaciones(cliente_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_ventas_cotizaciones_oportunidad ON ventas_cotizaciones(oportunidad_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_ventas_cotizaciones_propietario ON ventas_cotizaciones(propietario_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_ventas_cotizaciones_status ON ventas_cotizaciones(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_ventas_cotizaciones_fecha ON ventas_cotizaciones(fecha DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_ventas_cotizaciones_validez ON ventas_cotizaciones(fecha_validez) WHERE deleted_at IS NULL AND status = 'ENVIADA';

-- Índice compuesto para reportes de ventas
CREATE INDEX idx_ventas_cotizaciones_propietario_fecha ON ventas_cotizaciones(propietario_id, fecha DESC, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_ventas_cotizaciones_status_total ON ventas_cotizaciones(status, total DESC) WHERE deleted_at IS NULL;

-- Índices para ventas_items_cotizacion
CREATE INDEX idx_ventas_items_cotizacion_cotizacion ON ventas_items_cotizacion(cotizacion_id);
CREATE INDEX idx_ventas_items_cotizacion_producto ON ventas_items_cotizacion(producto_id);
CREATE INDEX idx_ventas_items_cotizacion_orden ON ventas_items_cotizacion(cotizacion_id, orden);

-- Índices únicos para ventas_pedidos
CREATE UNIQUE INDEX uk_ventas_pedidos_numero ON ventas_pedidos(unidad_negocio_id, numero) WHERE deleted_at IS NULL;

-- Índices de búsqueda para ventas_pedidos
CREATE INDEX idx_ventas_pedidos_unidad ON ventas_pedidos(unidad_negocio_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_ventas_pedidos_cotizacion ON ventas_pedidos(cotizacion_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_ventas_pedidos_cliente ON ventas_pedidos(cliente_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_ventas_pedidos_propietario ON ventas_pedidos(propietario_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_ventas_pedidos_status ON ventas_pedidos(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_ventas_pedidos_fecha ON ventas_pedidos(fecha DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_ventas_pedidos_entrega_estimada ON ventas_pedidos(fecha_entrega_estimada) WHERE deleted_at IS NULL AND fecha_entrega_estimada IS NOT NULL;

-- Índice compuesto para dashboard de pedidos
CREATE INDEX idx_ventas_pedidos_propietario_fecha ON ventas_pedidos(propietario_id, fecha DESC, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_ventas_pedidos_status_entrega ON ventas_pedidos(status, fecha_entrega_estimada) WHERE deleted_at IS NULL AND status IN ('CONFIRMADO', 'EN_PROCESO');

-- Índice de cobertura para cálculo de ingresos
CREATE INDEX idx_ventas_pedidos_fecha_total ON ventas_pedidos(fecha, total, status) WHERE deleted_at IS NULL AND status IN ('CONFIRMADO', 'EN_PROCESO', 'ENVIADO', 'ENTREGADO');

-- Índices para ventas_items_pedido
CREATE INDEX idx_ventas_items_pedido_pedido ON ventas_items_pedido(pedido_id);
CREATE INDEX idx_ventas_items_pedido_producto ON ventas_items_pedido(producto_id);
CREATE INDEX idx_ventas_items_pedido_orden ON ventas_items_pedido(pedido_id, orden);

-- Índice para control de entregas parciales
CREATE INDEX idx_ventas_items_pedido_pendiente ON ventas_items_pedido(pedido_id) WHERE cantidad_entregada < cantidad;

-- =====================================================================================================================
-- DOMINIO: REPORTES (Reports/Analytics)
-- =====================================================================================================================

-- Índices para reportes_dashboards
CREATE INDEX idx_reportes_dashboards_unidad ON reportes_dashboards(unidad_negocio_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_reportes_dashboards_propietario ON reportes_dashboards(propietario_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_reportes_dashboards_publico ON reportes_dashboards(es_publico, orden) WHERE deleted_at IS NULL AND es_publico = TRUE;

-- Índice GIN para búsquedas en configuración JSON
CREATE INDEX idx_reportes_dashboards_configuracion ON reportes_dashboards USING gin(configuracion);

-- Índices para reportes_widgets
CREATE INDEX idx_reportes_widgets_dashboard ON reportes_widgets(dashboard_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_reportes_widgets_tipo ON reportes_widgets(tipo) WHERE deleted_at IS NULL;

-- Índice GIN para búsquedas en configuración JSON
CREATE INDEX idx_reportes_widgets_configuracion ON reportes_widgets USING gin(configuracion);

-- =====================================================================================================================
-- ÍNDICES DE ANÁLISIS Y LIMPIEZA
-- Para procesos de mantenimiento y optimización
-- =====================================================================================================================

-- Índices para identificar registros eliminados (soft delete)
CREATE INDEX idx_seguridad_usuarios_deleted ON seguridad_usuarios(deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX idx_clientes_clientes_deleted ON clientes_clientes(deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX idx_oportunidades_oportunidades_deleted ON oportunidades_oportunidades(deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX idx_tareas_tareas_deleted ON tareas_tareas(deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX idx_productos_productos_deleted ON productos_productos(deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX idx_ventas_cotizaciones_deleted ON ventas_cotizaciones(deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX idx_ventas_pedidos_deleted ON ventas_pedidos(deleted_at) WHERE deleted_at IS NOT NULL;

-- =====================================================================================================================
-- FIN DE MIGRACIÓN V2__add_indexes.sql
-- =====================================================================================================================
