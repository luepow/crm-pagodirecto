package com.empresa.crm.pagos.dto;

import com.empresa.crm.pagos.model.EstadoPago;
import com.empresa.crm.pagos.model.MetodoPago;
import com.empresa.crm.pagos.model.Pago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for Pago entity
 * Bounded context: Pagos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoDTO {

    private Long id;
    private String folio;
    private Long ventaId;
    private String ventaFolio;
    private MetodoPago metodoPago;
    private BigDecimal monto;
    private LocalDate fechaPago;
    private EstadoPago estado;
    private String referencia;
    private String notas;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;

    /**
     * Convert Pago entity to DTO
     */
    public static PagoDTO fromEntity(Pago pago) {
        return PagoDTO.builder()
                .id(pago.getId())
                .folio(pago.getFolio())
                .ventaId(pago.getVenta().getId())
                .ventaFolio(pago.getVenta().getFolio())
                .metodoPago(pago.getMetodoPago())
                .monto(pago.getMonto())
                .fechaPago(pago.getFechaPago())
                .estado(pago.getEstado())
                .referencia(pago.getReferencia())
                .notas(pago.getNotas())
                .createdAt(pago.getCreatedAt())
                .createdBy(pago.getCreatedBy())
                .updatedAt(pago.getUpdatedAt())
                .updatedBy(pago.getUpdatedBy())
                .build();
    }
}
