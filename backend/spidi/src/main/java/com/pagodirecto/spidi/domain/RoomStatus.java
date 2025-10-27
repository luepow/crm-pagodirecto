package com.pagodirecto.spidi.domain;

/**
 * Estado de una sala Spidi
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
public enum RoomStatus {
    /**
     * Sala activa y operativa
     */
    ACTIVE,

    /**
     * Sala en mantenimiento (visible pero no acepta nuevas conexiones)
     */
    MAINTENANCE,

    /**
     * Sala deshabilitada (no visible ni accesible)
     */
    DISABLED,

    /**
     * Sala archivada (histórico, solo lectura)
     */
    ARCHIVED
}
