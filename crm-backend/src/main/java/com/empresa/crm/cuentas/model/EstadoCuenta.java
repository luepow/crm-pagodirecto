package com.empresa.crm.cuentas.model;

/**
 * ENUM for account status
 * Bounded context: Cuentas
 * Aligned with V6 migration schema
 */
public enum EstadoCuenta {
    PENDIENTE("Pendiente"),
    PAGADO("Pagado"),
    VENCIDO("Vencido"),
    CANCELADO("Cancelado");

    private final String displayName;

    EstadoCuenta(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
