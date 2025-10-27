package com.empresa.crm.pagos.controller;

import com.empresa.crm.pagos.dto.ActualizarEstadoPagoRequest;
import com.empresa.crm.pagos.dto.CrearPagoRequest;
import com.empresa.crm.pagos.dto.PagoDTO;
import com.empresa.crm.pagos.model.EstadoPago;
import com.empresa.crm.pagos.model.MetodoPago;
import com.empresa.crm.pagos.service.PagoService;
import com.empresa.crm.shared.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Pago operations
 * Bounded context: Pagos
 */
@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pagos", description = "Payment management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class PagoController {

    private final PagoService pagoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE')")
    @Operation(
        summary = "List all pagos",
        description = "Get paginated list of all pagos with optional search functionality. Requires ADMIN, MANAGER, or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<PagoDTO>> listar(
            @Parameter(description = "Search term for folio, referencia, or venta folio")
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.debug("GET /api/v1/pagos - search: {}, pageable: {}", search, pageable);
        Page<PagoDTO> pagos = pagoService.listar(search, pageable);
        return ResponseEntity.ok(pagos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE')")
    @Operation(
        summary = "Get pago by ID",
        description = "Get detailed information about a specific pago. Requires ADMIN, MANAGER, or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved pago"),
        @ApiResponse(responseCode = "404", description = "Pago not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PagoDTO> obtener(
            @Parameter(description = "Pago ID", required = true)
            @PathVariable Long id
    ) {
        log.debug("GET /api/v1/pagos/{}", id);
        return pagoService.obtener(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id: " + id));
    }

    @GetMapping("/folio/{folio}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE')")
    @Operation(
        summary = "Get pago by folio",
        description = "Get detailed information about a specific pago by folio. Requires ADMIN, MANAGER, or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved pago"),
        @ApiResponse(responseCode = "404", description = "Pago not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PagoDTO> obtenerPorFolio(
            @Parameter(description = "Pago folio", required = true)
            @PathVariable String folio
    ) {
        log.debug("GET /api/v1/pagos/folio/{}", folio);
        return pagoService.obtenerPorFolio(folio)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con folio: " + folio));
    }

    @GetMapping("/venta/{ventaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE')")
    @Operation(
        summary = "Get pagos by venta",
        description = "Get list of pagos for a specific venta. Requires ADMIN, MANAGER, or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<PagoDTO>> obtenerPorVenta(
            @Parameter(description = "Venta ID", required = true)
            @PathVariable Long ventaId
    ) {
        log.debug("GET /api/v1/pagos/venta/{}", ventaId);
        List<PagoDTO> pagos = pagoService.obtenerPorVenta(ventaId);
        return ResponseEntity.ok(pagos);
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE')")
    @Operation(
        summary = "Get pagos by estado",
        description = "Get paginated list of pagos with a specific estado. Requires ADMIN, MANAGER, or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<PagoDTO>> obtenerPorEstado(
            @Parameter(description = "Estado pago", required = true)
            @PathVariable EstadoPago estado,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.debug("GET /api/v1/pagos/estado/{}", estado);
        Page<PagoDTO> pagos = pagoService.obtenerPorEstado(estado, pageable);
        return ResponseEntity.ok(pagos);
    }

    @GetMapping("/metodo/{metodoPago}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE')")
    @Operation(
        summary = "Get pagos by metodo pago",
        description = "Get paginated list of pagos with a specific payment method. Requires ADMIN, MANAGER, or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<PagoDTO>> obtenerPorMetodoPago(
            @Parameter(description = "Metodo pago", required = true)
            @PathVariable MetodoPago metodoPago,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.debug("GET /api/v1/pagos/metodo/{}", metodoPago);
        Page<PagoDTO> pagos = pagoService.obtenerPorMetodoPago(metodoPago, pageable);
        return ResponseEntity.ok(pagos);
    }

    @GetMapping("/fecha-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE')")
    @Operation(
        summary = "Get pagos by fecha range",
        description = "Get paginated list of pagos within a date range. Requires ADMIN, MANAGER, or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<PagoDTO>> obtenerPorFechaRange(
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "fechaPago") Pageable pageable
    ) {
        log.debug("GET /api/v1/pagos/fecha-range?startDate={}&endDate={}", startDate, endDate);
        Page<PagoDTO> pagos = pagoService.obtenerPorFechaRange(startDate, endDate, pageable);
        return ResponseEntity.ok(pagos);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(
        summary = "Create new pago",
        description = "Create a new pago for a venta. Requires ADMIN or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pago created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or FINANCE role"),
        @ApiResponse(responseCode = "404", description = "Venta not found")
    })
    public ResponseEntity<PagoDTO> crear(
            @Valid @RequestBody CrearPagoRequest request
    ) {
        log.info("POST /api/v1/pagos - Creating new pago for venta: {}", request.getVentaId());
        PagoDTO createdPago = pagoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPago);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(
        summary = "Update pago estado",
        description = "Update the estado of a pago. Requires ADMIN or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid estado transition"),
        @ApiResponse(responseCode = "404", description = "Pago not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or FINANCE role")
    })
    public ResponseEntity<PagoDTO> actualizarEstado(
            @Parameter(description = "Pago ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoPagoRequest request
    ) {
        log.info("PATCH /api/v1/pagos/{}/estado - Updating estado to {}", id, request.getEstado());
        return pagoService.actualizarEstado(id, request)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id: " + id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete pago",
        description = "Soft delete a pago. Only PENDIENTE or FALLIDO pagos can be deleted. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Pago deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot delete pago in current estado"),
        @ApiResponse(responseCode = "404", description = "Pago not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Pago ID", required = true)
            @PathVariable Long id
    ) {
        log.info("DELETE /api/v1/pagos/{} - Deleting pago", id);
        pagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
