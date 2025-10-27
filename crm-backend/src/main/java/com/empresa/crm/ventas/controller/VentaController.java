package com.empresa.crm.ventas.controller;

import com.empresa.crm.shared.exception.ResourceNotFoundException;
import com.empresa.crm.ventas.dto.ActualizarEstadoVentaRequest;
import com.empresa.crm.ventas.dto.CrearVentaRequest;
import com.empresa.crm.ventas.dto.VentaDTO;
import com.empresa.crm.ventas.model.EstadoVenta;
import com.empresa.crm.ventas.service.VentaService;
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

/**
 * REST Controller for Venta operations
 * Bounded context: Ventas
 */
@RestController
@RequestMapping("/api/v1/ventas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ventas", description = "Sales management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class VentaController {

    private final VentaService ventaService;

    @GetMapping
    @Operation(
        summary = "List all ventas",
        description = "Get paginated list of all ventas with optional search functionality"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<VentaDTO>> listar(
            @Parameter(description = "Search term for folio, cliente, or estado")
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.debug("GET /api/v1/ventas - search: {}, pageable: {}", search, pageable);
        Page<VentaDTO> ventas = ventaService.listar(search, pageable);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get venta by ID",
        description = "Get detailed information about a specific venta including detalles"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved venta"),
        @ApiResponse(responseCode = "404", description = "Venta not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<VentaDTO> obtener(
            @Parameter(description = "Venta ID", required = true)
            @PathVariable Long id
    ) {
        log.debug("GET /api/v1/ventas/{}", id);
        return ventaService.obtener(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));
    }

    @GetMapping("/folio/{folio}")
    @Operation(
        summary = "Get venta by folio",
        description = "Get detailed information about a specific venta by folio"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved venta"),
        @ApiResponse(responseCode = "404", description = "Venta not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<VentaDTO> obtenerPorFolio(
            @Parameter(description = "Venta folio", required = true)
            @PathVariable String folio
    ) {
        log.debug("GET /api/v1/ventas/folio/{}", folio);
        return ventaService.obtenerPorFolio(folio)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con folio: " + folio));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(
        summary = "Get ventas by cliente",
        description = "Get paginated list of ventas for a specific cliente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<VentaDTO>> obtenerPorCliente(
            @Parameter(description = "Cliente ID", required = true)
            @PathVariable Long clienteId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.debug("GET /api/v1/ventas/cliente/{}", clienteId);
        Page<VentaDTO> ventas = ventaService.obtenerPorCliente(clienteId, pageable);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/estado/{estado}")
    @Operation(
        summary = "Get ventas by estado",
        description = "Get paginated list of ventas with a specific estado"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<VentaDTO>> obtenerPorEstado(
            @Parameter(description = "Estado venta", required = true)
            @PathVariable EstadoVenta estado,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.debug("GET /api/v1/ventas/estado/{}", estado);
        Page<VentaDTO> ventas = ventaService.obtenerPorEstado(estado, pageable);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/fecha-range")
    @Operation(
        summary = "Get ventas by fecha range",
        description = "Get paginated list of ventas within a date range"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<VentaDTO>> obtenerPorFechaRange(
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "fechaVenta") Pageable pageable
    ) {
        log.debug("GET /api/v1/ventas/fecha-range?startDate={}&endDate={}", startDate, endDate);
        Page<VentaDTO> ventas = ventaService.obtenerPorFechaRange(startDate, endDate, pageable);
        return ResponseEntity.ok(ventas);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Create new venta",
        description = "Create a new venta with detalles. Requires ADMIN or MANAGER role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Venta created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or MANAGER role"),
        @ApiResponse(responseCode = "404", description = "Cliente or Producto not found")
    })
    public ResponseEntity<VentaDTO> crear(
            @Valid @RequestBody CrearVentaRequest request
    ) {
        log.info("POST /api/v1/ventas - Creating new venta for cliente: {}", request.getClienteId());
        VentaDTO createdVenta = ventaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVenta);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Update venta estado",
        description = "Update the estado of a venta. Requires ADMIN or MANAGER role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid estado transition"),
        @ApiResponse(responseCode = "404", description = "Venta not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or MANAGER role")
    })
    public ResponseEntity<VentaDTO> actualizarEstado(
            @Parameter(description = "Venta ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoVentaRequest request
    ) {
        log.info("PATCH /api/v1/ventas/{}/estado - Updating estado to {}", id, request.getEstado());
        return ventaService.actualizarEstado(id, request)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Cancel venta",
        description = "Cancel a venta. Requires ADMIN or MANAGER role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Venta cancelled successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot cancel venta"),
        @ApiResponse(responseCode = "404", description = "Venta not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or MANAGER role")
    })
    public ResponseEntity<VentaDTO> cancelar(
            @Parameter(description = "Venta ID", required = true)
            @PathVariable Long id
    ) {
        log.info("PATCH /api/v1/ventas/{}/cancelar - Cancelling venta", id);
        return ventaService.cancelar(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete venta",
        description = "Soft delete a venta. Only BORRADOR or CANCELADA ventas can be deleted. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Venta deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot delete venta in current estado"),
        @ApiResponse(responseCode = "404", description = "Venta not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Venta ID", required = true)
            @PathVariable Long id
    ) {
        log.info("DELETE /api/v1/ventas/{} - Deleting venta", id);
        ventaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
