package com.empresa.crm.ventas.dto;

import com.empresa.crm.productos.application.dto.ProductoDTO;
import com.empresa.crm.ventas.model.DetalleVenta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for DetalleVenta entity
 * Bounded context: Ventas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVentaDTO {

    private Long id;
    private UUID productoId;
    private String productoNombre;
    private String productoCodigo;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal descuento;
    private BigDecimal subtotal;

    /**
     * Convert DetalleVenta entity to DTO
     */
    public static DetalleVentaDTO fromEntity(DetalleVenta detalle) {
        return DetalleVentaDTO.builder()
                .id(detalle.getId())
                .productoId(detalle.getProducto().getId())
                .productoNombre(detalle.getProducto().getNombre())
                .productoCodigo(detalle.getProducto().getCodigo())
                .cantidad(detalle.getCantidad())
                .precioUnitario(detalle.getPrecioUnitario())
                .descuento(detalle.getDescuento())
                .subtotal(detalle.getSubtotal())
                .build();
    }
}
