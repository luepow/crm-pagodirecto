package com.pagodirecto.seguridad.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/**
 * Componente: RLSContextManager
 *
 * Gestiona el contexto de sesión para Row-Level Security (RLS) en PostgreSQL.
 * Establece variables de sesión que las políticas RLS utilizan para filtrar datos.
 *
 * Función PostgreSQL requerida: set_app_session_context(tenant_id, user_id, roles)
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RLSContextManager {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Establece el contexto de sesión para RLS en PostgreSQL
     *
     * Este método debe llamarse después de autenticar al usuario y antes de
     * ejecutar cualquier query que dependa de RLS.
     *
     * @param unidadNegocioId ID de la unidad de negocio (tenant)
     * @param userId          ID del usuario autenticado
     * @param roles           roles del usuario
     */
    public void setSessionContext(UUID unidadNegocioId, UUID userId, Set<String> roles) {
        try {
            // Convierte el set de roles a una cadena separada por comas
            String rolesStr = String.join(",", roles);

            // Establece las variables de sesión de PostgreSQL
            jdbcTemplate.execute(
                String.format(
                    "SELECT set_config('app.current_tenant', '%s', false)",
                    unidadNegocioId
                )
            );

            jdbcTemplate.execute(
                String.format(
                    "SELECT set_config('app.current_user', '%s', false)",
                    userId
                )
            );

            jdbcTemplate.execute(
                String.format(
                    "SELECT set_config('app.current_roles', '%s', false)",
                    rolesStr
                )
            );

            log.debug("RLS context establecido: tenant={}, user={}, roles={}",
                unidadNegocioId, userId, rolesStr);

        } catch (Exception e) {
            log.error("Error al establecer el contexto RLS: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo establecer el contexto de seguridad RLS", e);
        }
    }

    /**
     * Limpia el contexto de sesión RLS
     *
     * Debe llamarse al finalizar el procesamiento del request para evitar
     * que la información de sesión se filtre a otros requests.
     */
    public void clearSessionContext() {
        try {
            jdbcTemplate.execute("SELECT set_config('app.current_tenant', '', false)");
            jdbcTemplate.execute("SELECT set_config('app.current_user', '', false)");
            jdbcTemplate.execute("SELECT set_config('app.current_roles', '', false)");

            log.debug("RLS context limpiado");
        } catch (Exception e) {
            log.error("Error al limpiar el contexto RLS: {}", e.getMessage(), e);
        }
    }

    /**
     * Obtiene el tenant ID actual del contexto de sesión
     *
     * @return el UUID del tenant actual o null si no está configurado
     */
    public UUID getCurrentTenant() {
        try {
            String tenantId = jdbcTemplate.queryForObject(
                "SELECT current_setting('app.current_tenant', true)",
                String.class
            );
            return tenantId != null && !tenantId.isEmpty() ? UUID.fromString(tenantId) : null;
        } catch (Exception e) {
            log.warn("No se pudo obtener el tenant actual del contexto: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene el user ID actual del contexto de sesión
     *
     * @return el UUID del usuario actual o null si no está configurado
     */
    public UUID getCurrentUser() {
        try {
            String userId = jdbcTemplate.queryForObject(
                "SELECT current_setting('app.current_user', true)",
                String.class
            );
            return userId != null && !userId.isEmpty() ? UUID.fromString(userId) : null;
        } catch (Exception e) {
            log.warn("No se pudo obtener el usuario actual del contexto: {}", e.getMessage());
            return null;
        }
    }
}
