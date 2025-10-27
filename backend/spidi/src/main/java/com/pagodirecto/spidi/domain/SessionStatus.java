package com.pagodirecto.spidi.domain;

/**
 * Estado de una sesión de conexión
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
public enum SessionStatus {
    /**
     * Sesión activa con heartbeat reciente
     */
    ACTIVE,

    /**
     * Sesión expirada por timeout de heartbeat
     */
    EXPIRED,

    /**
     * Sesión desconectada por el usuario
     */
    DISCONNECTED,

    /**
     * Sesión terminada por el servidor
     */
    TERMINATED
}
