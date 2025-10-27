package com.empresa.crm.pagos.model;

/**
 * ENUM for payment status
 * Bounded context: Pagos
 * Aligned with V5 migration schema
 */
public enum EstadoPago {
    PENDIENTE("Pendiente"),
    COMPLETADO("Completado"),
    FALLIDO("Fallido"),
    REEMBOLSADO("Reembolsado");

    private final String displayName;

    EstadoPago(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
