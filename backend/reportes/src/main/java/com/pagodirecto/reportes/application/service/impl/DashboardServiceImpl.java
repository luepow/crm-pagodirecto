package com.pagodirecto.reportes.application.service.impl;

import com.pagodirecto.reportes.application.dto.DashboardStatsDTO;
import com.pagodirecto.reportes.application.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Service Implementation: DashboardServiceImpl
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public DashboardStatsDTO obtenerEstadisticas() {
        log.info("Obteniendo estadísticas del dashboard");

        LocalDate inicioMesActual = LocalDate.now().withDayOfMonth(1);
        LocalDate finMesActual = LocalDate.now();
        LocalDate inicioMesAnterior = inicioMesActual.minusMonths(1);
        LocalDate finMesAnterior = inicioMesActual.minusDays(1);

        DashboardStatsDTO stats = DashboardStatsDTO.builder()
                // Clientes
                .totalClientes(obtenerTotalClientes())
                .clientesNuevosEsteMes(obtenerClientesNuevosPorPeriodo(inicioMesActual, finMesActual))
                .clientesCambioMensual(calcularCambioMensualClientes(inicioMesActual, inicioMesAnterior, finMesAnterior))

                // Oportunidades
                .oportunidadesActivas(obtenerOportunidadesActivas())
                .oportunidadesGanadas(obtenerOportunidadesGanadas())
                .valorTotalOportunidades(obtenerValorTotalOportunidades())
                .oportunidadesCambioMensual(calcularCambioMensualOportunidades(inicioMesActual, inicioMesAnterior, finMesAnterior))

                // Tareas
                .tareasPendientes(obtenerTareasPendientes())
                .tareasCompletadas(obtenerTareasCompletadas())
                .tareasVencidas(obtenerTareasVencidas())
                .tareasCambioMensual(calcularCambioMensualTareas(inicioMesActual, inicioMesAnterior, finMesAnterior))

                // Ventas
                .totalPedidos(obtenerTotalPedidos())
                .pedidosEsteMes(obtenerPedidosPorPeriodo(inicioMesActual, finMesActual))
                .ventasTotalesEsteMes(obtenerVentasPorPeriodo(inicioMesActual, finMesActual))
                .ventasMesAnterior(obtenerVentasPorPeriodo(inicioMesAnterior, finMesAnterior))
                .ventasCambioMensual(calcularCambioMensualVentas(inicioMesActual, inicioMesAnterior, finMesAnterior))

                // Productos
                .totalProductos(obtenerTotalProductos())
                .productosStockBajo(obtenerProductosStockBajo())
                .build();

        log.info("Estadísticas obtenidas: {} clientes, {} oportunidades, {} tareas",
                stats.getTotalClientes(), stats.getOportunidadesActivas(), stats.getTareasPendientes());

        return stats;
    }

    // Clientes
    private Long obtenerTotalClientes() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM clientes_clientes WHERE deleted_at IS NULL",
                Long.class
        );
    }

    private Long obtenerClientesNuevosPorPeriodo(LocalDate inicio, LocalDate fin) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM clientes_clientes WHERE deleted_at IS NULL " +
                "AND DATE(created_at) BETWEEN ? AND ?",
                Long.class, inicio, fin
        );
    }

    private Double calcularCambioMensualClientes(LocalDate inicioActual, LocalDate inicioAnterior, LocalDate finAnterior) {
        Long clientesActual = obtenerClientesNuevosPorPeriodo(inicioActual, LocalDate.now());
        Long clientesAnterior = obtenerClientesNuevosPorPeriodo(inicioAnterior, finAnterior);
        return calcularPorcentajeCambio(clientesAnterior, clientesActual);
    }

    // Oportunidades
    private Long obtenerOportunidadesActivas() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM oportunidades_oportunidades " +
                "WHERE deleted_at IS NULL AND probabilidad < 100",
                Long.class
        );
    }

    private Long obtenerOportunidadesGanadas() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM oportunidades_oportunidades " +
                "WHERE deleted_at IS NULL AND probabilidad = 100",
                Long.class
        );
    }

    private BigDecimal obtenerValorTotalOportunidades() {
        BigDecimal valor = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(valor_estimado), 0) FROM oportunidades_oportunidades " +
                "WHERE deleted_at IS NULL",
                BigDecimal.class
        );
        return valor != null ? valor : BigDecimal.ZERO;
    }

    private Double calcularCambioMensualOportunidades(LocalDate inicioActual, LocalDate inicioAnterior, LocalDate finAnterior) {
        Long oportActual = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM oportunidades_oportunidades WHERE deleted_at IS NULL " +
                "AND DATE(created_at) >= ?",
                Long.class, inicioActual
        );
        Long oportAnterior = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM oportunidades_oportunidades WHERE deleted_at IS NULL " +
                "AND DATE(created_at) BETWEEN ? AND ?",
                Long.class, inicioAnterior, finAnterior
        );
        return calcularPorcentajeCambio(oportAnterior, oportActual);
    }

    // Tareas
    private Long obtenerTareasPendientes() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tareas_tareas " +
                "WHERE deleted_at IS NULL AND status IN ('PENDIENTE', 'EN_PROGRESO')",
                Long.class
        );
    }

    private Long obtenerTareasCompletadas() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tareas_tareas " +
                "WHERE deleted_at IS NULL AND status = 'COMPLETADA'",
                Long.class
        );
    }

    private Long obtenerTareasVencidas() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tareas_tareas " +
                "WHERE deleted_at IS NULL AND fecha_vencimiento < CURRENT_DATE " +
                "AND status NOT IN ('COMPLETADA', 'CANCELADA')",
                Long.class
        );
    }

    private Double calcularCambioMensualTareas(LocalDate inicioActual, LocalDate inicioAnterior, LocalDate finAnterior) {
        Long tareasActual = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tareas_tareas WHERE deleted_at IS NULL " +
                "AND DATE(created_at) >= ?",
                Long.class, inicioActual
        );
        Long tareasAnterior = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tareas_tareas WHERE deleted_at IS NULL " +
                "AND DATE(created_at) BETWEEN ? AND ?",
                Long.class, inicioAnterior, finAnterior
        );
        return calcularPorcentajeCambio(tareasAnterior, tareasActual);
    }

    // Ventas
    private Long obtenerTotalPedidos() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ventas_pedidos WHERE deleted_at IS NULL",
                Long.class
        );
    }

    private Long obtenerPedidosPorPeriodo(LocalDate inicio, LocalDate fin) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ventas_pedidos " +
                "WHERE deleted_at IS NULL AND fecha BETWEEN ? AND ?",
                Long.class, inicio, fin
        );
    }

    private BigDecimal obtenerVentasPorPeriodo(LocalDate inicio, LocalDate fin) {
        BigDecimal total = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total), 0) FROM ventas_pedidos " +
                "WHERE deleted_at IS NULL AND fecha BETWEEN ? AND ? " +
                "AND status NOT IN ('CANCELADO', 'DEVUELTO')",
                BigDecimal.class, inicio, fin
        );
        return total != null ? total : BigDecimal.ZERO;
    }

    private Double calcularCambioMensualVentas(LocalDate inicioActual, LocalDate inicioAnterior, LocalDate finAnterior) {
        BigDecimal ventasActual = obtenerVentasPorPeriodo(inicioActual, LocalDate.now());
        BigDecimal ventasAnterior = obtenerVentasPorPeriodo(inicioAnterior, finAnterior);

        if (ventasAnterior.compareTo(BigDecimal.ZERO) == 0) {
            return ventasActual.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }

        return ventasActual.subtract(ventasAnterior)
                .divide(ventasAnterior, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    // Productos
    private Long obtenerTotalProductos() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM productos_productos WHERE deleted_at IS NULL",
                Long.class
        );
    }

    private Long obtenerProductosStockBajo() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM productos_productos " +
                "WHERE deleted_at IS NULL AND stock_actual <= stock_minimo",
                Long.class
        );
    }

    // Utilidad
    private Double calcularPorcentajeCambio(Long valorAnterior, Long valorActual) {
        if (valorAnterior == 0) {
            return valorActual > 0 ? 100.0 : 0.0;
        }
        return ((valorActual - valorAnterior) / (double) valorAnterior) * 100.0;
    }
}
