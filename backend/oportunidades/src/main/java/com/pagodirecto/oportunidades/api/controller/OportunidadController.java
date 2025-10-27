package com.pagodirecto.oportunidades.api.controller;

import com.pagodirecto.oportunidades.application.dto.OportunidadDTO;
import com.pagodirecto.oportunidades.application.service.OportunidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador REST: Oportunidad
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@RestController
@RequestMapping("/v1/oportunidades")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Oportunidades", description = "API para gestión de oportunidades de venta")
public class OportunidadController {

    private final OportunidadService oportunidadService;

    @Operation(summary = "Crear nueva oportunidad")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Oportunidad creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping
    public ResponseEntity<OportunidadDTO> crear(
            @Valid @RequestBody OportunidadDTO oportunidadDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Creando nueva oportunidad - usuario: {}", userDetails.getUsername());
        UUID usuarioId = UUID.randomUUID(); // TODO: Obtener del contexto de seguridad
        OportunidadDTO created = oportunidadService.crear(oportunidadDTO, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualizar oportunidad")
    @PutMapping("/{id}")
    public ResponseEntity<OportunidadDTO> actualizar(
            @Parameter(description = "UUID de la oportunidad") @PathVariable UUID id,
            @Valid @RequestBody OportunidadDTO oportunidadDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Actualizando oportunidad {} - usuario: {}", id, userDetails.getUsername());
        UUID usuarioId = UUID.randomUUID(); // TODO: Obtener del contexto de seguridad
        OportunidadDTO updated = oportunidadService.actualizar(id, oportunidadDTO, usuarioId);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Obtener oportunidad por ID")
    @GetMapping("/{id}")
    public ResponseEntity<OportunidadDTO> obtenerPorId(
            @Parameter(description = "UUID de la oportunidad") @PathVariable UUID id) {

        log.debug("Obteniendo oportunidad por ID: {}", id);
        OportunidadDTO oportunidad = oportunidadService.buscarPorId(id);
        return ResponseEntity.ok(oportunidad);
    }

    @Operation(summary = "Listar todas las oportunidades")
    @GetMapping
    public ResponseEntity<Page<OportunidadDTO>> listarTodas(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Listando todas las oportunidades");
        Page<OportunidadDTO> oportunidades = oportunidadService.listarTodas(pageable);
        return ResponseEntity.ok(oportunidades);
    }

    @Operation(summary = "Buscar oportunidades")
    @GetMapping("/search")
    public ResponseEntity<Page<OportunidadDTO>> buscar(
            @Parameter(description = "Término de búsqueda") @RequestParam String q,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando oportunidades con término: {}", q);
        Page<OportunidadDTO> oportunidades = oportunidadService.buscar(q, pageable);
        return ResponseEntity.ok(oportunidades);
    }

    @Operation(summary = "Buscar por cliente")
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<Page<OportunidadDTO>> buscarPorCliente(
            @Parameter(description = "UUID del cliente") @PathVariable UUID clienteId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando oportunidades por cliente: {}", clienteId);
        Page<OportunidadDTO> oportunidades = oportunidadService.buscarPorCliente(clienteId, pageable);
        return ResponseEntity.ok(oportunidades);
    }

    @Operation(summary = "Buscar por etapa")
    @GetMapping("/etapa/{etapaId}")
    public ResponseEntity<Page<OportunidadDTO>> buscarPorEtapa(
            @Parameter(description = "UUID de la etapa") @PathVariable UUID etapaId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando oportunidades por etapa: {}", etapaId);
        Page<OportunidadDTO> oportunidades = oportunidadService.buscarPorEtapa(etapaId, pageable);
        return ResponseEntity.ok(oportunidades);
    }

    @Operation(summary = "Buscar por propietario")
    @GetMapping("/propietario/{propietarioId}")
    public ResponseEntity<Page<OportunidadDTO>> buscarPorPropietario(
            @Parameter(description = "UUID del propietario") @PathVariable UUID propietarioId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando oportunidades por propietario: {}", propietarioId);
        Page<OportunidadDTO> oportunidades = oportunidadService.buscarPorPropietario(propietarioId, pageable);
        return ResponseEntity.ok(oportunidades);
    }

    @Operation(summary = "Mover oportunidad a nueva etapa")
    @PutMapping("/{id}/mover-etapa")
    public ResponseEntity<OportunidadDTO> moverAEtapa(
            @Parameter(description = "UUID de la oportunidad") @PathVariable UUID id,
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID etapaId = UUID.fromString((String) request.get("etapaId"));
        BigDecimal probabilidad = new BigDecimal(request.get("probabilidad").toString());

        log.info("Moviendo oportunidad {} a etapa {} - usuario: {}", id, etapaId, userDetails.getUsername());
        UUID usuarioId = UUID.randomUUID(); // TODO: Obtener del contexto de seguridad
        OportunidadDTO oportunidad = oportunidadService.moverAEtapa(id, etapaId, probabilidad, usuarioId);
        return ResponseEntity.ok(oportunidad);
    }

    @Operation(summary = "Marcar oportunidad como ganada")
    @PutMapping("/{id}/marcar-ganada")
    public ResponseEntity<OportunidadDTO> marcarComoGanada(
            @Parameter(description = "UUID de la oportunidad") @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {

        LocalDate fechaCierre = request != null && request.containsKey("fechaCierre")
                ? LocalDate.parse(request.get("fechaCierre"))
                : LocalDate.now();

        log.info("Marcando oportunidad {} como ganada - usuario: {}", id, userDetails.getUsername());
        UUID usuarioId = UUID.randomUUID(); // TODO: Obtener del contexto de seguridad
        OportunidadDTO oportunidad = oportunidadService.marcarComoGanada(id, fechaCierre, usuarioId);
        return ResponseEntity.ok(oportunidad);
    }

    @Operation(summary = "Marcar oportunidad como perdida")
    @PutMapping("/{id}/marcar-perdida")
    public ResponseEntity<OportunidadDTO> marcarComoPerdida(
            @Parameter(description = "UUID de la oportunidad") @PathVariable UUID id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String motivo = request.get("motivo");

        log.info("Marcando oportunidad {} como perdida - usuario: {}", id, userDetails.getUsername());
        UUID usuarioId = UUID.randomUUID(); // TODO: Obtener del contexto de seguridad
        OportunidadDTO oportunidad = oportunidadService.marcarComoPerdida(id, motivo, usuarioId);
        return ResponseEntity.ok(oportunidad);
    }

    @Operation(summary = "Eliminar oportunidad")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "UUID de la oportunidad") @PathVariable UUID id) {

        log.info("Eliminando oportunidad: {}", id);
        oportunidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Contar por etapa")
    @GetMapping("/count/etapa/{etapaId}")
    public ResponseEntity<Long> contarPorEtapa(
            @Parameter(description = "UUID de la etapa") @PathVariable UUID etapaId) {

        log.debug("Contando oportunidades por etapa: {}", etapaId);
        long count = oportunidadService.contarPorEtapa(etapaId);
        return ResponseEntity.ok(count);
    }
}
