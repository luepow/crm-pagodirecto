package com.pagodirecto.ventas.application.dto;

import com.pagodirecto.ventas.domain.PedidoStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para transferencia de datos de Pedidos
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoDTO {

    private UUID id;

    @NotNull(message = "La unidad de negocio es obligatoria")
    private UUID unidadNegocioId;

    private UUID cotizacionId;

    @NotNull(message = "El cliente es obligatorio")
    private UUID clienteId;

    private String clienteNombre;

    @NotBlank(message = "El número de pedido es obligatorio")
    @Size(max = 50, message = "El número no puede exceder 50 caracteres")
    private String numero;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    private LocalDate fechaEntregaEstimada;

    private LocalDate fechaEntregaReal;

    @NotNull(message = "El status es obligatorio")
    private PedidoStatus status;

    @NotNull(message = "El subtotal es obligatorio")
    @DecimalMin(value = "0.0", message = "El subtotal no puede ser negativo")
    @Digits(integer = 13, fraction = 2, message = "Formato de subtotal inválido")
    private BigDecimal subtotal;

    @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
    @Digits(integer = 13, fraction = 2, message = "Formato de descuento inválido")
    private BigDecimal descuentoGlobal;

    @NotNull(message = "Los impuestos son obligatorios")
    @DecimalMin(value = "0.0", message = "Los impuestos no pueden ser negativos")
    @Digits(integer = 13, fraction = 2, message = "Formato de impuestos inválido")
    private BigDecimal impuestos;

    @NotNull(message = "El total es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El total debe ser mayor a 0")
    @Digits(integer = 13, fraction = 2, message = "Formato de total inválido")
    private BigDecimal total;

    @NotBlank(message = "La moneda es obligatoria")
    @Size(min = 3, max = 3, message = "La moneda debe tener 3 caracteres (ISO 4217)")
    private String moneda;

    @Size(max = 50, message = "El método de pago no puede exceder 50 caracteres")
    private String metodoPago;

    @Size(max = 100, message = "Los términos de pago no pueden exceder 100 caracteres")
    private String terminosPago;

    @Size(max = 5000, message = "Las notas no pueden exceder 5000 caracteres")
    private String notas;

    @NotNull(message = "El propietario es obligatorio")
    private UUID propietarioId;

    private String propietarioNombre;

    private Integer cantidadItems;

    private Instant createdAt;

    private UUID createdBy;

    private String createdByNombre;

    private Instant updatedAt;

    private UUID updatedBy;
}
