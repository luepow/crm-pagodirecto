package com.empresa.crm.reportes.service;

import com.empresa.crm.cuentas.model.EstadoCuenta;
import com.empresa.crm.cuentas.model.TipoCuenta;
import com.empresa.crm.cuentas.repository.CuentaRepository;
import com.empresa.crm.productos.infrastructure.repository.ProductoRepository;
import com.empresa.crm.reportes.dto.ReporteCuentasDTO;
import com.empresa.crm.reportes.dto.ReporteProductosDTO;
import com.empresa.crm.reportes.dto.ReporteVentasDTO;
import com.empresa.crm.ventas.model.DetalleVenta;
import com.empresa.crm.ventas.model.Venta;
import com.empresa.crm.ventas.repository.DetalleVentaRepository;
import com.empresa.crm.ventas.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ReporteServiceImpl implements ReporteService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoRepository productoRepository;
    private final CuentaRepository cuentaRepository;

    @Override
    public List<ReporteVentasDTO> generarReporteVentasPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        log.info("Generando reporte de ventas desde {} hasta {}", fechaInicio, fechaFin);

        // Query using existing repository method with Pageable.unpaged()
        var ventas = ventaRepository.findByFechaRange(
                fechaInicio,
                fechaFin,
                org.springframework.data.domain.Pageable.unpaged()
        ).getContent();

        return ventas.stream()
                .collect(Collectors.groupingBy(v -> v.getFechaVenta()))
                .entrySet().stream()
                .map(entry -> ReporteVentasDTO.builder()
                        .fecha(entry.getKey())
                        .cantidadVentas((long) entry.getValue().size())
                        .totalVentas(entry.getValue().stream()
                                .map(Venta::getTotal)
                                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add))
                        .promedioVenta(entry.getValue().stream()
                                .map(Venta::getTotal)
                                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                                .divide(java.math.BigDecimal.valueOf(entry.getValue().size()), 2, java.math.RoundingMode.HALF_UP))
                        .build())
                .sorted((a, b) -> a.getFecha().compareTo(b.getFecha()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReporteProductosDTO> generarReporteProductosMasVendidos(LocalDate fechaInicio, LocalDate fechaFin, Integer limit) {
        log.info("Generando reporte de productos más vendidos desde {} hasta {}", fechaInicio, fechaFin);

        // Get all detalles for the period
        var ventas = ventaRepository.findByFechaRange(
                fechaInicio,
                fechaFin,
                org.springframework.data.domain.Pageable.unpaged()
        ).getContent();

        var ventasIds = ventas.stream().map(Venta::getId).collect(Collectors.toList());
        var detalles = detalleVentaRepository.findByVentaIdIn(ventasIds);

        return detalles.stream()
                .collect(Collectors.groupingBy(DetalleVenta::getProducto))
                .entrySet().stream()
                .map(entry -> {
                    var producto = entry.getKey();
                    var cantidadVendida = entry.getValue().stream()
                            .mapToLong(DetalleVenta::getCantidad)
                            .sum();

                    return ReporteProductosDTO.builder()
                            .productoId(producto.getId())
                            .codigo(producto.getCodigo())
                            .nombre(producto.getNombre())
                            .cantidadVendida(cantidadVendida)
                            .stockActual(producto.getStock())
                            .build();
                })
                .sorted((a, b) -> b.getCantidadVendida().compareTo(a.getCantidadVendida()))
                .limit(limit != null ? limit : 10)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReporteProductosDTO> generarReporteStockBajo() {
        log.info("Generando reporte de stock bajo");

        var productosStockBajo = productoRepository.findProductosConStockBajo();

        return productosStockBajo.stream()
                .map(p -> ReporteProductosDTO.builder()
                        .productoId(p.getId())
                        .codigo(p.getCodigo())
                        .nombre(p.getNombre())
                        .cantidadVendida(0L)
                        .stockActual(p.getStock())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ReporteCuentasDTO generarReporteCuentas(TipoCuenta tipo) {
        log.info("Generando reporte de cuentas tipo {}", tipo);

        var cuentasPendientes = cuentaRepository.findByTipoAndEstadoAndDeletedAtIsNull(
                tipo,
                EstadoCuenta.PENDIENTE,
                org.springframework.data.domain.Pageable.unpaged()
        ).getContent();

        var cuentasVencidas = cuentaRepository.findCuentasVencidas(
                LocalDate.now(),
                org.springframework.data.domain.Pageable.unpaged()
        ).getContent();

        var montoTotalPendiente = cuentasPendientes.stream()
                .map(c -> c.getSaldo())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        var cuentasVencidasTipo = cuentasVencidas.stream()
                .filter(c -> c.getTipo() == tipo)
                .collect(Collectors.toList());

        var montoTotalVencido = cuentasVencidasTipo.stream()
                .map(c -> c.getSaldo())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return ReporteCuentasDTO.builder()
                .tipo(tipo)
                .cantidadPendientes((long) cuentasPendientes.size())
                .montoTotalPendiente(montoTotalPendiente)
                .cantidadVencidas((long) cuentasVencidasTipo.size())
                .montoTotalVencido(montoTotalVencido)
                .build();
    }
}
