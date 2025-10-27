package com.empresa.crm.ventas.model;

/**
 * ENUM for Venta state
 * Bounded context: Ventas
 * Aligned with V4 migration schema
 */
public enum EstadoVenta {
    BORRADOR("Borrador"),
    CONFIRMADA("Confirmada"),
    ENVIADA("Enviada"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada");

    private final String displayName;

    EstadoVenta(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
