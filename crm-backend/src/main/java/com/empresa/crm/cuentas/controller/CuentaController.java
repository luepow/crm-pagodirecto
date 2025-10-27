package com.empresa.crm.cuentas.controller;

import com.empresa.crm.cuentas.dto.AplicarPagoRequest;
import com.empresa.crm.cuentas.dto.CrearCuentaRequest;
import com.empresa.crm.cuentas.dto.CuentaDTO;
import com.empresa.crm.cuentas.model.EstadoCuenta;
import com.empresa.crm.cuentas.model.TipoCuenta;
import com.empresa.crm.cuentas.service.CuentaService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Cuenta operations
 * Bounded context: Cuentas
 */
@RestController
@RequestMapping("/api/v1/cuentas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cuentas", description = "Accounts payable/receivable management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class CuentaController {

    private final CuentaService cuentaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE')")
    @Operation(
        summary = "List all cuentas",
        description = "Get paginated list of all cuentas with optional search functionality. Requires ADMIN, MANAGER, or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<CuentaDTO>> listar(
            @Parameter(description = "Search term for folio, cliente, or descripcion")
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.debug("GET /api/v1/cuentas - search: {}, pageable: {}", search, pageable);
        Page<CuentaDTO> cuentas = cuentaService.listar(search, pageable);
        return ResponseEntity.ok(cuentas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE')")
    @Operation(
        summary = "Get cuenta by ID",
        description = "Get detailed information about a specific cuenta. Requires ADMIN, MANAGER, or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved cuenta"),
        @ApiResponse(responseCode = "404", description = "Cuenta not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<CuentaDTO> obtener(
            @Parameter(description = "Cuenta ID", required = true)
            @PathVariable Long id
    ) {
        log.debug("GET /api/v1/cuentas/{}", id);
        return cuentaService.obtener(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + id));
    }

    @GetMapping("/folio/{folio}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE')")
    @Operation(
        summary = "Get cuenta by folio",
        description = "Get detailed information about a specific cuenta by folio. Requires ADMIN, MANAGER, or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved cuenta"),
        @ApiResponse(responseCode = "404", description = "Cuenta not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<CuentaDTO> obtenerPorFolio(
            @Parameter(description = "Cuenta folio", required = true)
            @PathVariable String folio
    ) {
        log.debug("GET /api/v1/cuentas/folio/{}", folio);
        return cuentaService.obtenerPorFolio(folio)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con folio: " + folio));
    }

    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE')")
    @Operation(
        summary = "Get cuentas by tipo",
        description = "Get paginated list of cuentas with a specific tipo. Requires ADMIN, MANAGER, or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<CuentaDTO>> obtenerPorTipo(
            @Parameter(description = "Tipo cuenta", required = true)
            @PathVariable TipoCuenta tipo,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.debug("GET /api/v1/cuentas/tipo/{}", tipo);
        Page<CuentaDTO> cuentas = cuentaService.obtenerPorTipo(tipo, pageable);
        return ResponseEntity.ok(cuentas);
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE')")
    @Operation(
        summary = "Get cuentas by estado",
        description = "Get paginated list of cuentas with a specific estado. Requires ADMIN, MANAGER, or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<CuentaDTO>> obtenerPorEstado(
            @Parameter(description = "Estado cuenta", required = true)
            @PathVariable EstadoCuenta estado,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.debug("GET /api/v1/cuentas/estado/{}", estado);
        Page<CuentaDTO> cuentas = cuentaService.obtenerPorEstado(estado, pageable);
        return ResponseEntity.ok(cuentas);
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE')")
    @Operation(
        summary = "Get cuentas by cliente",
        description = "Get paginated list of cuentas for a specific cliente. Requires ADMIN, MANAGER, or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<CuentaDTO>> obtenerPorCliente(
            @Parameter(description = "Cliente ID", required = true)
            @PathVariable Long clienteId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.debug("GET /api/v1/cuentas/cliente/{}", clienteId);
        Page<CuentaDTO> cuentas = cuentaService.obtenerPorCliente(clienteId, pageable);
        return ResponseEntity.ok(cuentas);
    }

    @GetMapping("/vencidas")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE')")
    @Operation(
        summary = "Get cuentas vencidas",
        description = "Get paginated list of overdue cuentas. Requires ADMIN, MANAGER, or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<CuentaDTO>> obtenerVencidas(
            @PageableDefault(size = 20, sort = "fechaVencimiento") Pageable pageable
    ) {
        log.debug("GET /api/v1/cuentas/vencidas");
        Page<CuentaDTO> cuentas = cuentaService.obtenerVencidas(pageable);
        return ResponseEntity.ok(cuentas);
    }

    @GetMapping("/vencidas/tipo/{tipo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE')")
    @Operation(
        summary = "Get cuentas vencidas by tipo",
        description = "Get paginated list of overdue cuentas filtered by tipo. Requires ADMIN, MANAGER, or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<CuentaDTO>> obtenerVencidasPorTipo(
            @Parameter(description = "Tipo cuenta", required = true)
            @PathVariable TipoCuenta tipo,
            @PageableDefault(size = 20, sort = "fechaVencimiento") Pageable pageable
    ) {
        log.debug("GET /api/v1/cuentas/vencidas/tipo/{}", tipo);
        Page<CuentaDTO> cuentas = cuentaService.obtenerVencidasPorTipo(tipo, pageable);
        return ResponseEntity.ok(cuentas);
    }

    @GetMapping("/referencia/{referenciaTipo}/{referenciaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE')")
    @Operation(
        summary = "Get cuentas by referencia",
        description = "Get list of cuentas for a specific referencia. Requires ADMIN, MANAGER, or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<CuentaDTO>> obtenerPorReferencia(
            @Parameter(description = "Referencia tipo", required = true)
            @PathVariable String referenciaTipo,
            @Parameter(description = "Referencia ID", required = true)
            @PathVariable Long referenciaId
    ) {
        log.debug("GET /api/v1/cuentas/referencia/{}/{}", referenciaTipo, referenciaId);
        List<CuentaDTO> cuentas = cuentaService.obtenerPorReferencia(referenciaTipo, referenciaId);
        return ResponseEntity.ok(cuentas);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(
        summary = "Create new cuenta",
        description = "Create a new cuenta. Requires ADMIN or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cuenta created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or FINANCE role"),
        @ApiResponse(responseCode = "404", description = "Cliente not found")
    })
    public ResponseEntity<CuentaDTO> crear(
            @Valid @RequestBody CrearCuentaRequest request
    ) {
        log.info("POST /api/v1/cuentas - Creating new cuenta tipo {} for cliente: {}",
                 request.getTipo(), request.getClienteId());
        CuentaDTO createdCuenta = cuentaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCuenta);
    }

    @PostMapping("/desde-venta/{ventaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(
        summary = "Create cuenta from venta",
        description = "Create a cuenta por cobrar from a venta. Requires ADMIN or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cuenta created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or cuenta already exists"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or FINANCE role"),
        @ApiResponse(responseCode = "404", description = "Venta not found")
    })
    public ResponseEntity<CuentaDTO> crearDesdeVenta(
            @Parameter(description = "Venta ID", required = true)
            @PathVariable Long ventaId
    ) {
        log.info("POST /api/v1/cuentas/desde-venta/{} - Creating cuenta from venta", ventaId);
        CuentaDTO createdCuenta = cuentaService.crearDesdeVenta(ventaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCuenta);
    }

    @PatchMapping("/{id}/aplicar-pago")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(
        summary = "Apply payment to cuenta",
        description = "Apply a partial or full payment to a cuenta. Requires ADMIN or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment applied successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payment amount"),
        @ApiResponse(responseCode = "404", description = "Cuenta not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or FINANCE role")
    })
    public ResponseEntity<CuentaDTO> aplicarPago(
            @Parameter(description = "Cuenta ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody AplicarPagoRequest request
    ) {
        log.info("PATCH /api/v1/cuentas/{}/aplicar-pago - Applying payment of {}", id, request.getMontoPago());
        return cuentaService.aplicarPago(id, request)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + id));
    }

    @PatchMapping("/{id}/marcar-pagada")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(
        summary = "Mark cuenta as paid",
        description = "Mark a cuenta as fully paid. Requires ADMIN or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cuenta marked as paid successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot mark cuenta as paid"),
        @ApiResponse(responseCode = "404", description = "Cuenta not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or FINANCE role")
    })
    public ResponseEntity<CuentaDTO> marcarPagada(
            @Parameter(description = "Cuenta ID", required = true)
            @PathVariable Long id
    ) {
        log.info("PATCH /api/v1/cuentas/{}/marcar-pagada - Marking cuenta as paid", id);
        return cuentaService.marcarPagada(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + id));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(
        summary = "Cancel cuenta",
        description = "Cancel a cuenta. Requires ADMIN or FINANCE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cuenta cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Cuenta not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or FINANCE role")
    })
    public ResponseEntity<CuentaDTO> cancelar(
            @Parameter(description = "Cuenta ID", required = true)
            @PathVariable Long id
    ) {
        log.info("PATCH /api/v1/cuentas/{}/cancelar - Cancelling cuenta", id);
        return cuentaService.cancelar(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete cuenta",
        description = "Soft delete a cuenta. Only PENDIENTE or CANCELADO cuentas can be deleted. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cuenta deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot delete cuenta in current estado"),
        @ApiResponse(responseCode = "404", description = "Cuenta not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Cuenta ID", required = true)
            @PathVariable Long id
    ) {
        log.info("DELETE /api/v1/cuentas/{} - Deleting cuenta", id);
        cuentaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
