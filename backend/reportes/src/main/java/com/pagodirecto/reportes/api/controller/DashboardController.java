package com.pagodirecto.reportes.api.controller;

import com.pagodirecto.reportes.application.dto.DashboardStatsDTO;
import com.pagodirecto.reportes.application.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller: Dashboard
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "API de estadísticas del dashboard")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", allowCredentials = "true")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Obtener estadísticas del dashboard")
    public ResponseEntity<DashboardStatsDTO> obtenerEstadisticas() {
        log.info("Solicitud para obtener estadísticas del dashboard");
        DashboardStatsDTO stats = dashboardService.obtenerEstadisticas();
        return ResponseEntity.ok(stats);
    }
}
