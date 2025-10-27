package com.pagodirecto.spidi.domain;

/**
 * Intervalo de agregación para estadísticas
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
public enum BucketInterval {
    /**
     * Agregación por minuto
     */
    MINUTE,

    /**
     * Agregación por hora
     */
    HOUR,

    /**
     * Agregación por día
     */
    DAY,

    /**
     * Agregación por semana
     */
    WEEK,

    /**
     * Agregación por mes
     */
    MONTH
}
