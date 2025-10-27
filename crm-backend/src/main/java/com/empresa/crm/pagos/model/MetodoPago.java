package com.empresa.crm.pagos.model;

/**
 * ENUM for payment methods
 * Bounded context: Pagos
 * Aligned with V5 migration schema
 */
public enum MetodoPago {
    EFECTIVO("Efectivo"),
    TARJETA_CREDITO("Tarjeta de Crédito"),
    TARJETA_DEBITO("Tarjeta de Débito"),
    TRANSFERENCIA("Transferencia Bancaria"),
    CHEQUE("Cheque"),
    OTRO("Otro");

    private final String displayName;

    MetodoPago(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
