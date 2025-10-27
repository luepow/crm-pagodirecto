package com.pagodirecto.seguridad.domain;

/**
 * Enum: AuditResultado
 *
 * Resultado de una operación auditada
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
public enum AuditResultado {
    /**
     * Operación completada exitosamente
     */
    SUCCESS,

    /**
     * Operación falló completamente
     */
    FAILURE,

    /**
     * Operación completada parcialmente con advertencias
     */
    PARTIAL
}
