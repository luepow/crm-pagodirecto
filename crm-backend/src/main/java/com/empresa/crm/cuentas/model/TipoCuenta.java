package com.empresa.crm.cuentas.model;

/**
 * ENUM for account type
 * Bounded context: Cuentas
 * Aligned with V6 migration schema
 */
public enum TipoCuenta {
    COBRAR("Por Cobrar"),
    PAGAR("Por Pagar");

    private final String displayName;

    TipoCuenta(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
