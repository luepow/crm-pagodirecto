package com.empresa.crm.cuentas.dto;

import com.empresa.crm.clientes.dto.ClienteDTO;
import com.empresa.crm.cuentas.model.Cuenta;
import com.empresa.crm.cuentas.model.EstadoCuenta;
import com.empresa.crm.cuentas.model.TipoCuenta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for Cuenta entity
 * Bounded context: Cuentas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaDTO {

    private Long id;
    private String folio;
    private TipoCuenta tipo;
    private String referenciaTipo;
    private Long referenciaId;
    private ClienteDTO cliente;
    private String descripcion;
    private BigDecimal monto;
    private BigDecimal saldo;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private EstadoCuenta estado;
    private Integer diasVencidos;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;

    /**
     * Convert Cuenta entity to DTO
     */
    public static CuentaDTO fromEntity(Cuenta cuenta) {
        return CuentaDTO.builder()
                .id(cuenta.getId())
                .folio(cuenta.getFolio())
                .tipo(cuenta.getTipo())
                .referenciaTipo(cuenta.getReferenciaTipo())
                .referenciaId(cuenta.getReferenciaId())
                .cliente(ClienteDTO.fromEntity(cuenta.getCliente()))
                .descripcion(cuenta.getDescripcion())
                .monto(cuenta.getMonto())
                .saldo(cuenta.getSaldo())
                .fechaEmision(cuenta.getFechaEmision())
                .fechaVencimiento(cuenta.getFechaVencimiento())
                .estado(cuenta.getEstado())
                .diasVencidos(cuenta.getDiasVencidos())
                .createdAt(cuenta.getCreatedAt())
                .createdBy(cuenta.getCreatedBy())
                .updatedAt(cuenta.getUpdatedAt())
                .updatedBy(cuenta.getUpdatedBy())
                .build();
    }

    /**
     * Convert Cuenta entity to DTO without cliente details
     */
    public static CuentaDTO fromEntitySimple(Cuenta cuenta) {
        return CuentaDTO.builder()
                .id(cuenta.getId())
                .folio(cuenta.getFolio())
                .tipo(cuenta.getTipo())
                .referenciaTipo(cuenta.getReferenciaTipo())
                .referenciaId(cuenta.getReferenciaId())
                .descripcion(cuenta.getDescripcion())
                .monto(cuenta.getMonto())
                .saldo(cuenta.getSaldo())
                .fechaEmision(cuenta.getFechaEmision())
                .fechaVencimiento(cuenta.getFechaVencimiento())
                .estado(cuenta.getEstado())
                .diasVencidos(cuenta.getDiasVencidos())
                .createdAt(cuenta.getCreatedAt())
                .createdBy(cuenta.getCreatedBy())
                .updatedAt(cuenta.getUpdatedAt())
                .updatedBy(cuenta.getUpdatedBy())
                .build();
    }
}
