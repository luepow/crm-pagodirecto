package com.empresa.crm.reportes.dto;

import com.empresa.crm.cuentas.model.TipoCuenta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteCuentasDTO {
    private TipoCuenta tipo;
    private Long cantidadPendientes;
    private BigDecimal montoTotalPendiente;
    private Long cantidadVencidas;
    private BigDecimal montoTotalVencido;
}
