package com.empresa.crm.ventas.dto;

import com.empresa.crm.clientes.dto.ClienteDTO;
import com.empresa.crm.ventas.model.EstadoVenta;
import com.empresa.crm.ventas.model.Venta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for Venta entity
 * Bounded context: Ventas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaDTO {

    private Long id;
    private String folio;
    private ClienteDTO cliente;
    private LocalDate fechaVenta;
    private EstadoVenta estado;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal impuestos;
    private BigDecimal total;
    private String notas;
    private List<DetalleVentaDTO> detalles;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;

    /**
     * Convert Venta entity to DTO
     */
    public static VentaDTO fromEntity(Venta venta) {
        return VentaDTO.builder()
                .id(venta.getId())
                .folio(venta.getFolio())
                .cliente(ClienteDTO.fromEntity(venta.getCliente()))
                .fechaVenta(venta.getFechaVenta())
                .estado(venta.getEstado())
                .subtotal(venta.getSubtotal())
                .descuento(venta.getDescuento())
                .impuestos(venta.getImpuestos())
                .total(venta.getTotal())
                .notas(venta.getNotas())
                .detalles(venta.getDetalles().stream()
                        .map(DetalleVentaDTO::fromEntity)
                        .collect(Collectors.toList()))
                .createdAt(venta.getCreatedAt())
                .createdBy(venta.getCreatedBy())
                .updatedAt(venta.getUpdatedAt())
                .updatedBy(venta.getUpdatedBy())
                .build();
    }

    /**
     * Convert Venta entity to DTO without detalles (for list views)
     */
    public static VentaDTO fromEntityWithoutDetalles(Venta venta) {
        return VentaDTO.builder()
                .id(venta.getId())
                .folio(venta.getFolio())
                .cliente(ClienteDTO.fromEntity(venta.getCliente()))
                .fechaVenta(venta.getFechaVenta())
                .estado(venta.getEstado())
                .subtotal(venta.getSubtotal())
                .descuento(venta.getDescuento())
                .impuestos(venta.getImpuestos())
                .total(venta.getTotal())
                .notas(venta.getNotas())
                .createdAt(venta.getCreatedAt())
                .createdBy(venta.getCreatedBy())
                .updatedAt(venta.getUpdatedAt())
                .updatedBy(venta.getUpdatedBy())
                .build();
    }
}
