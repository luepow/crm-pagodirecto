package com.pagodirecto.seguridad.domain;

/**
 * Enum: PermissionAction
 *
 * Acciones posibles sobre recursos del sistema
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
public enum PermissionAction {
    /**
     * Permiso para crear nuevos recursos
     */
    CREATE,

    /**
     * Permiso para leer/consultar recursos
     */
    READ,

    /**
     * Permiso para actualizar recursos existentes
     */
    UPDATE,

    /**
     * Permiso para eliminar recursos
     */
    DELETE,

    /**
     * Permiso para ejecutar operaciones especiales
     */
    EXECUTE,

    /**
     * Permiso administrativo total sobre el recurso
     */
    ADMIN
}
