package com.pagodirecto.clientes.domain;

/**
 * Enumeración: Tipo de Dirección
 *
 * Define los tipos de direcciones que puede tener un cliente.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
public enum DireccionTipo {
    /**
     * Dirección fiscal para facturación
     */
    FISCAL,

    /**
     * Dirección de envío/entrega
     */
    ENVIO,

    /**
     * Otro tipo de dirección
     */
    OTRO
}
