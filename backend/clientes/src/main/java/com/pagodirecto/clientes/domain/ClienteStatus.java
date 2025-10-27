package com.pagodirecto.clientes.domain;

/**
 * Enumeración: Estado del Cliente
 *
 * Define los posibles estados de un cliente en el ciclo de vida CRM.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
public enum ClienteStatus {
    /**
     * Cliente activo con cuenta completa
     */
    ACTIVE,

    /**
     * Cliente inactivo (sin actividad reciente)
     */
    INACTIVE,

    /**
     * Prospecto (cliente potencial en evaluación)
     */
    PROSPECT,

    /**
     * Lead (contacto inicial sin calificar)
     */
    LEAD,

    /**
     * Cliente en lista negra (no comerciar)
     */
    BLACKLIST
}
