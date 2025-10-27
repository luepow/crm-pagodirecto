package com.empresa.crm.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteProductosDTO {
    private UUID productoId;
    private String codigo;
    private String nombre;
    private Long cantidadVendida;
    private Integer stockActual;
}
