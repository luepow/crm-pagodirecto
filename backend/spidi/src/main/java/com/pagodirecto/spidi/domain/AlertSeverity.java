package com.pagodirecto.spidi.domain;

/**
 * Severidad de alertas
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
public enum AlertSeverity {
    /**
     * Información general (no requiere acción inmediata)
     */
    INFO,

    /**
     * Advertencia (requiere atención)
     */
    WARNING,

    /**
     * Error (requiere acción correctiva)
     */
    ERROR,

    /**
     * Crítico (requiere acción inmediata)
     */
    CRITICAL
}
