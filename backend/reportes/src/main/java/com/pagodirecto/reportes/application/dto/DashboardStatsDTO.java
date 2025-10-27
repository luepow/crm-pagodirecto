package com.pagodirecto.reportes.application.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO: DashboardStatsDTO
 *
 * Estadísticas generales del dashboard
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {

    // Clientes
    private Long totalClientes;
    private Long clientesNuevosEsteMes;
    private Double clientesCambioMensual;

    // Oportunidades
    private Long oportunidadesActivas;
    private Long oportunidadesGanadas;
    private Double oportunidadesCambioMensual;
    private BigDecimal valorTotalOportunidades;

    // Tareas
    private Long tareasPendientes;
    private Long tareasCompletadas;
    private Long tareasVencidas;
    private Double tareasCambioMensual;

    // Ventas
    private Long totalPedidos;
    private Long pedidosEsteMes;
    private BigDecimal ventasTotalesEsteMes;
    private BigDecimal ventasMesAnterior;
    private Double ventasCambioMensual;

    // Productos
    private Long totalProductos;
    private Long productosStockBajo;
}
