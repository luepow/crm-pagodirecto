package com.empresa.crm.reportes.controller;

import com.empresa.crm.cuentas.model.TipoCuenta;
import com.empresa.crm.reportes.dto.ReporteCuentasDTO;
import com.empresa.crm.reportes.dto.ReporteProductosDTO;
import com.empresa.crm.reportes.dto.ReporteVentasDTO;
import com.empresa.crm.reportes.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Endpoints para reportes y análisis")
@SecurityRequirement(name = "bearer-jwt")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/ventas/periodo")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE')")
    @Operation(summary = "Reporte de ventas por período", description = "Genera reporte agregado de ventas por fecha")
    public ResponseEntity<List<ReporteVentasDTO>> reporteVentasPorPeriodo(
            @Parameter(description = "Fecha inicio (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(reporteService.generarReporteVentasPorPeriodo(fechaInicio, fechaFin));
    }

    @GetMapping("/productos/mas-vendidos")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE')")
    @Operation(summary = "Productos más vendidos", description = "Lista los productos más vendidos en un período")
    public ResponseEntity<List<ReporteProductosDTO>> productosMasVendidos(
            @Parameter(description = "Fecha inicio (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @Parameter(description = "Límite de resultados (default: 10)")
            @RequestParam(required = false, defaultValue = "10") Integer limit
    ) {
        return ResponseEntity.ok(reporteService.generarReporteProductosMasVendidos(fechaInicio, fechaFin, limit));
    }

    @GetMapping("/productos/stock-bajo")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Productos con stock bajo", description = "Lista productos que están por debajo del stock mínimo")
    public ResponseEntity<List<ReporteProductosDTO>> reporteStockBajo() {
        return ResponseEntity.ok(reporteService.generarReporteStockBajo());
    }

    @GetMapping("/cuentas/{tipo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Reporte de cuentas", description = "Reporte de cuentas por cobrar o pagar")
    public ResponseEntity<ReporteCuentasDTO> reporteCuentas(
            @Parameter(description = "Tipo de cuenta: COBRAR o PAGAR")
            @PathVariable TipoCuenta tipo
    ) {
        return ResponseEntity.ok(reporteService.generarReporteCuentas(tipo));
    }
}
