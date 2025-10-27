package com.pagodirecto.seguridad.domain;

/**
 * Enum: UsuarioStatus
 *
 * Estados posibles de un usuario en el sistema
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
public enum UsuarioStatus {
    /**
     * Usuario activo, puede autenticarse y usar el sistema
     */
    ACTIVE,

    /**
     * Usuario inactivo, no puede autenticarse
     */
    INACTIVE,

    /**
     * Usuario bloqueado temporalmente (por intentos fallidos o administrativamente)
     */
    LOCKED,

    /**
     * Usuario suspendido por razones administrativas
     */
    SUSPENDED
}
