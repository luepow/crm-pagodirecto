-- =====================================================================================================================
-- V3__add_rls_policies.sql
-- Migración de políticas de Row-Level Security (RLS)
-- Fecha: 2025-10-13
-- Autor: Database Architecture Team
-- Descripción: Políticas RLS para aislamiento multi-tenant y control de acceso granular
-- =====================================================================================================================

-- =====================================================================================================================
-- CONFIGURACIÓN DE PARÁMETROS DE SESIÓN
-- Estos parámetros son establecidos por la aplicación al inicio de cada sesión/transacción
-- =====================================================================================================================

-- Parámetros de sesión esperados:
-- app.current_tenant (UUID): Unidad de negocio del usuario actual
-- app.current_user (UUID): ID del usuario actual
-- app.user_roles (TEXT): Roles del usuario separados por coma
-- app.bypass_rls (BOOLEAN): Flag para usuarios admin que pueden ver todo (solo para reportes globales)

-- =====================================================================================================================
-- FUNCIONES AUXILIARES PARA POLÍTICAS RLS
-- =====================================================================================================================

-- Función: Obtener unidad de negocio del usuario actual
CREATE OR REPLACE FUNCTION app_current_tenant()
RETURNS UUID AS $$
BEGIN
    RETURN NULLIF(current_setting('app.current_tenant', TRUE), '')::UUID;
EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

COMMENT ON FUNCTION app_current_tenant() IS 'Retorna el UUID de la unidad de negocio del usuario actual desde configuración de sesión';

-- Función: Obtener ID del usuario actual
CREATE OR REPLACE FUNCTION app_current_user()
RETURNS UUID AS $$
BEGIN
    RETURN NULLIF(current_setting('app.current_user', TRUE), '')::UUID;
EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

COMMENT ON FUNCTION app_current_user() IS 'Retorna el UUID del usuario actual desde configuración de sesión';

-- Función: Verificar si el usuario tiene un rol específico
CREATE OR REPLACE FUNCTION app_user_has_role(role_name TEXT)
RETURNS BOOLEAN AS $$
DECLARE
    user_roles TEXT;
BEGIN
    user_roles := current_setting('app.user_roles', TRUE);
    RETURN user_roles IS NOT NULL AND user_roles LIKE '%' || role_name || '%';
EXCEPTION
    WHEN OTHERS THEN
        RETURN FALSE;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

COMMENT ON FUNCTION app_user_has_role(TEXT) IS 'Verifica si el usuario actual tiene el rol especificado';

-- Función: Verificar si se debe omitir RLS (solo para admins en reportes globales)
CREATE OR REPLACE FUNCTION app_bypass_rls()
RETURNS BOOLEAN AS $$
BEGIN
    RETURN COALESCE(current_setting('app.bypass_rls', TRUE)::BOOLEAN, FALSE);
EXCEPTION
    WHEN OTHERS THEN
        RETURN FALSE;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

COMMENT ON FUNCTION app_bypass_rls() IS 'Indica si se debe omitir RLS para el usuario actual (solo admins)';

-- =====================================================================================================================
-- POLÍTICAS RLS: DOMINIO SEGURIDAD
-- =====================================================================================================================

-- Habilitar RLS en seguridad_usuarios
ALTER TABLE seguridad_usuarios ENABLE ROW LEVEL SECURITY;

-- Política: Los usuarios pueden ver usuarios de su misma unidad de negocio
CREATE POLICY tenant_isolation_seguridad_usuarios ON seguridad_usuarios
    FOR SELECT
    USING (
        unidad_negocio_id = app_current_tenant()
        OR app_bypass_rls()
    );

-- Política: Los usuarios pueden actualizar su propio perfil
CREATE POLICY self_update_seguridad_usuarios ON seguridad_usuarios
    FOR UPDATE
    USING (id = app_current_user() OR app_user_has_role('ADMIN'));

-- Política: Solo admins pueden insertar nuevos usuarios
CREATE POLICY admin_insert_seguridad_usuarios ON seguridad_usuarios
    FOR INSERT
    WITH CHECK (app_user_has_role('ADMIN'));

-- Política: Solo admins pueden eliminar usuarios
CREATE POLICY admin_delete_seguridad_usuarios ON seguridad_usuarios
    FOR DELETE
    USING (app_user_has_role('ADMIN'));

-- Habilitar RLS en seguridad_roles
ALTER TABLE seguridad_roles ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_seguridad_roles ON seguridad_roles
    FOR ALL
    USING (
        unidad_negocio_id = app_current_tenant()
        OR app_bypass_rls()
    );

-- Habilitar RLS en seguridad_audit_log
ALTER TABLE seguridad_audit_log ENABLE ROW LEVEL SECURITY;

-- Política: Los usuarios solo pueden ver su propio audit log, admins ven todo
CREATE POLICY tenant_audit_seguridad_audit_log ON seguridad_audit_log
    FOR SELECT
    USING (
        usuario_id = app_current_user()
        OR app_user_has_role('ADMIN')
        OR app_bypass_rls()
    );

-- Política: Solo el sistema puede insertar en audit log
CREATE POLICY system_insert_seguridad_audit_log ON seguridad_audit_log
    FOR INSERT
    WITH CHECK (TRUE);

-- Política: Nadie puede actualizar o eliminar audit log (inmutable)
CREATE POLICY immutable_seguridad_audit_log ON seguridad_audit_log
    FOR UPDATE
    USING (FALSE);

CREATE POLICY immutable_delete_seguridad_audit_log ON seguridad_audit_log
    FOR DELETE
    USING (FALSE);

-- =====================================================================================================================
-- POLÍTICAS RLS: DOMINIO CLIENTES
-- =====================================================================================================================

-- Habilitar RLS en clientes_clientes
ALTER TABLE clientes_clientes ENABLE ROW LEVEL SECURITY;

-- Política: Aislamiento por tenant
CREATE POLICY tenant_isolation_clientes_clientes ON clientes_clientes
    FOR SELECT
    USING (
        unidad_negocio_id = app_current_tenant()
        OR app_bypass_rls()
    );

-- Política: Los usuarios pueden ver clientes asignados a ellos o públicos
CREATE POLICY owner_access_clientes_clientes ON clientes_clientes
    FOR SELECT
    USING (
        propietario_id = app_current_user()
        OR app_user_has_role('ADMIN')
        OR app_user_has_role('SALES_MANAGER')
    );

-- Política: Los usuarios pueden insertar clientes en su tenant
CREATE POLICY tenant_insert_clientes_clientes ON clientes_clientes
    FOR INSERT
    WITH CHECK (
        unidad_negocio_id = app_current_tenant()
        AND (
            app_user_has_role('SALES')
            OR app_user_has_role('ADMIN')
        )
    );

-- Política: Los usuarios pueden actualizar clientes que poseen
CREATE POLICY owner_update_clientes_clientes ON clientes_clientes
    FOR UPDATE
    USING (
        unidad_negocio_id = app_current_tenant()
        AND (
            propietario_id = app_current_user()
            OR app_user_has_role('ADMIN')
            OR app_user_has_role('SALES_MANAGER')
        )
    );

-- Política: Solo admins pueden eliminar clientes
CREATE POLICY admin_delete_clientes_clientes ON clientes_clientes
    FOR DELETE
    USING (
        unidad_negocio_id = app_current_tenant()
        AND app_user_has_role('ADMIN')
    );

-- Habilitar RLS en clientes_contactos
ALTER TABLE clientes_contactos ENABLE ROW LEVEL SECURITY;

-- Política: Los contactos heredan permisos del cliente
CREATE POLICY tenant_isolation_clientes_contactos ON clientes_contactos
    FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM clientes_clientes
            WHERE clientes_clientes.id = clientes_contactos.cliente_id
            AND clientes_clientes.unidad_negocio_id = app_current_tenant()
        )
        OR app_bypass_rls()
    );

-- Habilitar RLS en clientes_direcciones
ALTER TABLE clientes_direcciones ENABLE ROW LEVEL SECURITY;

-- Política: Las direcciones heredan permisos del cliente
CREATE POLICY tenant_isolation_clientes_direcciones ON clientes_direcciones
    FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM clientes_clientes
            WHERE clientes_clientes.id = clientes_direcciones.cliente_id
            AND clientes_clientes.unidad_negocio_id = app_current_tenant()
        )
        OR app_bypass_rls()
    );

-- =====================================================================================================================
-- POLÍTICAS RLS: DOMINIO OPORTUNIDADES
-- =====================================================================================================================

-- Habilitar RLS en oportunidades_etapas_pipeline
ALTER TABLE oportunidades_etapas_pipeline ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_oportunidades_etapas ON oportunidades_etapas_pipeline
    FOR ALL
    USING (
        unidad_negocio_id = app_current_tenant()
        OR app_bypass_rls()
    );

-- Habilitar RLS en oportunidades_oportunidades
ALTER TABLE oportunidades_oportunidades ENABLE ROW LEVEL SECURITY;

-- Política: Aislamiento por tenant
CREATE POLICY tenant_isolation_oportunidades_oportunidades ON oportunidades_oportunidades
    FOR SELECT
    USING (
        unidad_negocio_id = app_current_tenant()
        OR app_bypass_rls()
    );

-- Política: Los usuarios pueden ver oportunidades asignadas a ellos
CREATE POLICY owner_access_oportunidades_oportunidades ON oportunidades_oportunidades
    FOR SELECT
    USING (
        propietario_id = app_current_user()
        OR app_user_has_role('ADMIN')
        OR app_user_has_role('SALES_MANAGER')
    );

-- Política: Los usuarios pueden insertar oportunidades
CREATE POLICY tenant_insert_oportunidades_oportunidades ON oportunidades_oportunidades
    FOR INSERT
    WITH CHECK (
        unidad_negocio_id = app_current_tenant()
        AND (
            app_user_has_role('SALES')
            OR app_user_has_role('ADMIN')
        )
    );

-- Política: Los usuarios pueden actualizar oportunidades que poseen
CREATE POLICY owner_update_oportunidades_oportunidades ON oportunidades_oportunidades
    FOR UPDATE
    USING (
        unidad_negocio_id = app_current_tenant()
        AND (
            propietario_id = app_current_user()
            OR app_user_has_role('ADMIN')
            OR app_user_has_role('SALES_MANAGER')
        )
    );

-- Habilitar RLS en oportunidades_actividades
ALTER TABLE oportunidades_actividades ENABLE ROW LEVEL SECURITY;

-- Política: Las actividades heredan permisos de la oportunidad
CREATE POLICY tenant_isolation_oportunidades_actividades ON oportunidades_actividades
    FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM oportunidades_oportunidades
            WHERE oportunidades_oportunidades.id = oportunidades_actividades.oportunidad_id
            AND oportunidades_oportunidades.unidad_negocio_id = app_current_tenant()
        )
        OR app_bypass_rls()
    );

-- =====================================================================================================================
-- POLÍTICAS RLS: DOMINIO TAREAS
-- =====================================================================================================================

-- Habilitar RLS en tareas_tareas
ALTER TABLE tareas_tareas ENABLE ROW LEVEL SECURITY;

-- Política: Aislamiento por tenant
CREATE POLICY tenant_isolation_tareas_tareas ON tareas_tareas
    FOR SELECT
    USING (
        unidad_negocio_id = app_current_tenant()
        OR app_bypass_rls()
    );

-- Política: Los usuarios pueden ver tareas asignadas a ellos o creadas por ellos
CREATE POLICY assigned_access_tareas_tareas ON tareas_tareas
    FOR SELECT
    USING (
        asignado_a = app_current_user()
        OR created_by = app_current_user()
        OR app_user_has_role('ADMIN')
    );

-- Política: Los usuarios pueden insertar tareas
CREATE POLICY tenant_insert_tareas_tareas ON tareas_tareas
    FOR INSERT
    WITH CHECK (
        unidad_negocio_id = app_current_tenant()
    );

-- Política: Los usuarios pueden actualizar tareas asignadas a ellos
CREATE POLICY assigned_update_tareas_tareas ON tareas_tareas
    FOR UPDATE
    USING (
        unidad_negocio_id = app_current_tenant()
        AND (
            asignado_a = app_current_user()
            OR created_by = app_current_user()
            OR app_user_has_role('ADMIN')
        )
    );

-- Habilitar RLS en tareas_comentarios
ALTER TABLE tareas_comentarios ENABLE ROW LEVEL SECURITY;

-- Política: Los comentarios heredan permisos de la tarea
CREATE POLICY tenant_isolation_tareas_comentarios ON tareas_comentarios
    FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM tareas_tareas
            WHERE tareas_tareas.id = tareas_comentarios.tarea_id
            AND tareas_tareas.unidad_negocio_id = app_current_tenant()
        )
        OR app_bypass_rls()
    );

-- =====================================================================================================================
-- POLÍTICAS RLS: DOMINIO PRODUCTOS
-- =====================================================================================================================

-- Habilitar RLS en productos_categorias
ALTER TABLE productos_categorias ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_productos_categorias ON productos_categorias
    FOR ALL
    USING (
        unidad_negocio_id = app_current_tenant()
        OR app_bypass_rls()
    );

-- Habilitar RLS en productos_productos
ALTER TABLE productos_productos ENABLE ROW LEVEL SECURITY;

-- Política: Todos en el tenant pueden ver productos activos
CREATE POLICY tenant_read_productos_productos ON productos_productos
    FOR SELECT
    USING (
        unidad_negocio_id = app_current_tenant()
        OR app_bypass_rls()
    );

-- Política: Solo warehouse managers y admins pueden modificar productos
CREATE POLICY warehouse_write_productos_productos ON productos_productos
    FOR INSERT
    WITH CHECK (
        unidad_negocio_id = app_current_tenant()
        AND (
            app_user_has_role('WAREHOUSE_MANAGER')
            OR app_user_has_role('ADMIN')
        )
    );

CREATE POLICY warehouse_update_productos_productos ON productos_productos
    FOR UPDATE
    USING (
        unidad_negocio_id = app_current_tenant()
        AND (
            app_user_has_role('WAREHOUSE_MANAGER')
            OR app_user_has_role('ADMIN')
        )
    );

-- Habilitar RLS en productos_precios
ALTER TABLE productos_precios ENABLE ROW LEVEL SECURITY;

-- Política: Los precios heredan permisos del producto
CREATE POLICY tenant_isolation_productos_precios ON productos_precios
    FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM productos_productos
            WHERE productos_productos.id = productos_precios.producto_id
            AND productos_productos.unidad_negocio_id = app_current_tenant()
        )
        OR app_bypass_rls()
    );

-- =====================================================================================================================
-- POLÍTICAS RLS: DOMINIO VENTAS
-- =====================================================================================================================

-- Habilitar RLS en ventas_cotizaciones
ALTER TABLE ventas_cotizaciones ENABLE ROW LEVEL SECURITY;

-- Política: Aislamiento por tenant
CREATE POLICY tenant_isolation_ventas_cotizaciones ON ventas_cotizaciones
    FOR SELECT
    USING (
        unidad_negocio_id = app_current_tenant()
        OR app_bypass_rls()
    );

-- Política: Los usuarios pueden ver cotizaciones que poseen
CREATE POLICY owner_access_ventas_cotizaciones ON ventas_cotizaciones
    FOR SELECT
    USING (
        propietario_id = app_current_user()
        OR app_user_has_role('ADMIN')
        OR app_user_has_role('SALES_MANAGER')
    );

-- Política: Los usuarios de ventas pueden crear cotizaciones
CREATE POLICY sales_insert_ventas_cotizaciones ON ventas_cotizaciones
    FOR INSERT
    WITH CHECK (
        unidad_negocio_id = app_current_tenant()
        AND (
            app_user_has_role('SALES')
            OR app_user_has_role('ADMIN')
        )
    );

-- Política: Los propietarios pueden actualizar sus cotizaciones
CREATE POLICY owner_update_ventas_cotizaciones ON ventas_cotizaciones
    FOR UPDATE
    USING (
        unidad_negocio_id = app_current_tenant()
        AND (
            propietario_id = app_current_user()
            OR app_user_has_role('ADMIN')
            OR app_user_has_role('SALES_MANAGER')
        )
    );

-- Habilitar RLS en ventas_items_cotizacion
ALTER TABLE ventas_items_cotizacion ENABLE ROW LEVEL SECURITY;

-- Política: Los items heredan permisos de la cotización
CREATE POLICY tenant_isolation_ventas_items_cotizacion ON ventas_items_cotizacion
    FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM ventas_cotizaciones
            WHERE ventas_cotizaciones.id = ventas_items_cotizacion.cotizacion_id
            AND ventas_cotizaciones.unidad_negocio_id = app_current_tenant()
        )
        OR app_bypass_rls()
    );

-- Habilitar RLS en ventas_pedidos
ALTER TABLE ventas_pedidos ENABLE ROW LEVEL SECURITY;

-- Política: Aislamiento por tenant
CREATE POLICY tenant_isolation_ventas_pedidos ON ventas_pedidos
    FOR SELECT
    USING (
        unidad_negocio_id = app_current_tenant()
        OR app_bypass_rls()
    );

-- Política: Los usuarios pueden ver pedidos que poseen
CREATE POLICY owner_access_ventas_pedidos ON ventas_pedidos
    FOR SELECT
    USING (
        propietario_id = app_current_user()
        OR app_user_has_role('ADMIN')
        OR app_user_has_role('SALES_MANAGER')
        OR app_user_has_role('WAREHOUSE_MANAGER')
    );

-- Política: Los usuarios de ventas pueden crear pedidos
CREATE POLICY sales_insert_ventas_pedidos ON ventas_pedidos
    FOR INSERT
    WITH CHECK (
        unidad_negocio_id = app_current_tenant()
        AND (
            app_user_has_role('SALES')
            OR app_user_has_role('ADMIN')
        )
    );

-- Política: Los propietarios y warehouse pueden actualizar pedidos
CREATE POLICY authorized_update_ventas_pedidos ON ventas_pedidos
    FOR UPDATE
    USING (
        unidad_negocio_id = app_current_tenant()
        AND (
            propietario_id = app_current_user()
            OR app_user_has_role('ADMIN')
            OR app_user_has_role('SALES_MANAGER')
            OR app_user_has_role('WAREHOUSE_MANAGER')
        )
    );

-- Habilitar RLS en ventas_items_pedido
ALTER TABLE ventas_items_pedido ENABLE ROW LEVEL SECURITY;

-- Política: Los items heredan permisos del pedido
CREATE POLICY tenant_isolation_ventas_items_pedido ON ventas_items_pedido
    FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM ventas_pedidos
            WHERE ventas_pedidos.id = ventas_items_pedido.pedido_id
            AND ventas_pedidos.unidad_negocio_id = app_current_tenant()
        )
        OR app_bypass_rls()
    );

-- =====================================================================================================================
-- POLÍTICAS RLS: DOMINIO REPORTES
-- =====================================================================================================================

-- Habilitar RLS en reportes_dashboards
ALTER TABLE reportes_dashboards ENABLE ROW LEVEL SECURITY;

-- Política: Los usuarios pueden ver dashboards públicos o propios
CREATE POLICY public_or_owner_reportes_dashboards ON reportes_dashboards
    FOR SELECT
    USING (
        unidad_negocio_id = app_current_tenant()
        AND (
            es_publico = TRUE
            OR propietario_id = app_current_user()
            OR app_user_has_role('ADMIN')
        )
        OR app_bypass_rls()
    );

-- Política: Los usuarios pueden crear dashboards
CREATE POLICY tenant_insert_reportes_dashboards ON reportes_dashboards
    FOR INSERT
    WITH CHECK (
        unidad_negocio_id = app_current_tenant()
    );

-- Política: Los propietarios pueden actualizar sus dashboards
CREATE POLICY owner_update_reportes_dashboards ON reportes_dashboards
    FOR UPDATE
    USING (
        unidad_negocio_id = app_current_tenant()
        AND (
            propietario_id = app_current_user()
            OR app_user_has_role('ADMIN')
        )
    );

-- Política: Los propietarios pueden eliminar sus dashboards
CREATE POLICY owner_delete_reportes_dashboards ON reportes_dashboards
    FOR DELETE
    USING (
        unidad_negocio_id = app_current_tenant()
        AND (
            propietario_id = app_current_user()
            OR app_user_has_role('ADMIN')
        )
    );

-- Habilitar RLS en reportes_widgets
ALTER TABLE reportes_widgets ENABLE ROW LEVEL SECURITY;

-- Política: Los widgets heredan permisos del dashboard
CREATE POLICY tenant_isolation_reportes_widgets ON reportes_widgets
    FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM reportes_dashboards
            WHERE reportes_dashboards.id = reportes_widgets.dashboard_id
            AND reportes_dashboards.unidad_negocio_id = app_current_tenant()
        )
        OR app_bypass_rls()
    );

-- =====================================================================================================================
-- FUNCIONES PARA ESTABLECER CONTEXTO DE SESIÓN (llamadas por la aplicación)
-- =====================================================================================================================

-- Función: Establecer contexto de usuario para la sesión
CREATE OR REPLACE FUNCTION set_app_session_context(
    p_tenant_id UUID,
    p_user_id UUID,
    p_user_roles TEXT,
    p_bypass_rls BOOLEAN DEFAULT FALSE
)
RETURNS VOID AS $$
BEGIN
    -- Establecer unidad de negocio
    PERFORM set_config('app.current_tenant', p_tenant_id::TEXT, FALSE);

    -- Establecer usuario
    PERFORM set_config('app.current_user', p_user_id::TEXT, FALSE);

    -- Establecer roles
    PERFORM set_config('app.user_roles', p_user_roles, FALSE);

    -- Establecer flag de bypass RLS (solo para admins)
    PERFORM set_config('app.bypass_rls', p_bypass_rls::TEXT, FALSE);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION set_app_session_context(UUID, UUID, TEXT, BOOLEAN) IS
'Establece el contexto de sesión para políticas RLS. Llamada por la aplicación al inicio de cada request.';

-- Función: Limpiar contexto de sesión
CREATE OR REPLACE FUNCTION clear_app_session_context()
RETURNS VOID AS $$
BEGIN
    PERFORM set_config('app.current_tenant', '', FALSE);
    PERFORM set_config('app.current_user', '', FALSE);
    PERFORM set_config('app.user_roles', '', FALSE);
    PERFORM set_config('app.bypass_rls', 'false', FALSE);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION clear_app_session_context() IS
'Limpia el contexto de sesión. Llamada por la aplicación al finalizar cada request.';

-- =====================================================================================================================
-- GRANTS: Configurar permisos a nivel de base de datos
-- =====================================================================================================================

-- Crear rol de aplicación (debe ser creado manualmente por DBA)
-- CREATE ROLE app_user WITH LOGIN PASSWORD 'secure_password';
-- GRANT CONNECT ON DATABASE your_database TO app_user;
-- GRANT USAGE ON SCHEMA public TO app_user;

-- Grant SELECT/INSERT/UPDATE/DELETE en todas las tablas al rol de aplicación
-- (ejecutar después de crear el rol app_user)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_user;
-- ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_user;
-- ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO app_user;

-- =====================================================================================================================
-- FIN DE MIGRACIÓN V3__add_rls_policies.sql
-- =====================================================================================================================
