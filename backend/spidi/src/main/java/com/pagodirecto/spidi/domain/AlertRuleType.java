package com.pagodirecto.spidi.domain;

/**
 * Tipo de regla de alerta
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
public enum AlertRuleType {
    /**
     * Alerta de capacidad (% de usuarios conectados vs capacidad)
     */
    CAPACITY,

    /**
     * Alerta de latencia alta
     */
    LATENCY,

    /**
     * Alerta de heartbeat caído (sin señal de vida)
     */
    HEARTBEAT,

    /**
     * Alerta personalizada con expresión Groovy
     */
    CUSTOM
}
