package com.empresa.crm.reportes.service;

import com.empresa.crm.cuentas.model.TipoCuenta;
import com.empresa.crm.reportes.dto.ReporteCuentasDTO;
import com.empresa.crm.reportes.dto.ReporteProductosDTO;
import com.empresa.crm.reportes.dto.ReporteVentasDTO;

import java.time.LocalDate;
import java.util.List;

public interface ReporteService {

    /**
     * Reporte de ventas por período
     */
    List<ReporteVentasDTO> generarReporteVentasPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Productos más vendidos
     */
    List<ReporteProductosDTO> generarReporteProductosMasVendidos(LocalDate fechaInicio, LocalDate fechaFin, Integer limit);

    /**
     * Productos con stock bajo
     */
    List<ReporteProductosDTO> generarReporteStockBajo();

    /**
     * Reporte de cuentas por cobrar/pagar
     */
    ReporteCuentasDTO generarReporteCuentas(TipoCuenta tipo);
}
