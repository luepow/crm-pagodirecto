package com.empresa.crm.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteVentasDTO {
    private LocalDate fecha;
    private Long cantidadVentas;
    private BigDecimal totalVentas;
    private BigDecimal promedioVenta;
}
