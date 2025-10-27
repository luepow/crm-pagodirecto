package com.empresa.crm.clientes.controller;

import com.empresa.crm.clientes.dto.ActualizarClienteRequest;
import com.empresa.crm.clientes.dto.ClienteDTO;
import com.empresa.crm.clientes.dto.CrearClienteRequest;
import com.empresa.crm.clientes.service.ClienteService;
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

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Clientes", description = "Customer/Client management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @Operation(
        summary = "List all clientes",
        description = "Get paginated list of all clientes with optional search functionality. " +
                     "Search by nombre, email, or RFC."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<ClienteDTO>> listar(
            @Parameter(description = "Search term for nombre, email, or RFC")
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.debug("GET /api/v1/clientes - search: {}, pageable: {}", search, pageable);
        Page<ClienteDTO> clientes = clienteService.listar(search, pageable);
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get cliente by ID",
        description = "Get detailed information about a specific cliente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved cliente"),
        @ApiResponse(responseCode = "404", description = "Cliente not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ClienteDTO> obtener(
            @Parameter(description = "Cliente ID", required = true)
            @PathVariable Long id
    ) {
        log.debug("GET /api/v1/clientes/{}", id);
        return clienteService.obtener(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Create new cliente",
        description = "Create a new cliente. Requires ADMIN or MANAGER role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cliente created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or MANAGER role"),
        @ApiResponse(responseCode = "409", description = "Cliente with email already exists")
    })
    public ResponseEntity<ClienteDTO> crear(
            @Valid @RequestBody CrearClienteRequest request
    ) {
        log.info("POST /api/v1/clientes - Creating new cliente: {}", request.getEmail());
        ClienteDTO createdCliente = clienteService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCliente);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Update cliente",
        description = "Update an existing cliente. Requires ADMIN or MANAGER role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Cliente not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or MANAGER role"),
        @ApiResponse(responseCode = "409", description = "Email already in use by another cliente")
    })
    public ResponseEntity<ClienteDTO> actualizar(
            @Parameter(description = "Cliente ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ActualizarClienteRequest request
    ) {
        log.info("PUT /api/v1/clientes/{} - Updating cliente", id);
        return clienteService.actualizar(id, request)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete cliente",
        description = "Soft delete a cliente (sets deleted_at timestamp). Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cliente deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Cliente not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Cliente ID", required = true)
            @PathVariable Long id
    ) {
        log.info("DELETE /api/v1/clientes/{} - Deleting cliente", id);
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check-email")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Check if email exists",
        description = "Check if an email is already registered. Useful for validation. Requires ADMIN or MANAGER role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check completed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Boolean> checkEmail(
            @Parameter(description = "Email to check", required = true)
            @RequestParam String email
    ) {
        log.debug("GET /api/v1/clientes/check-email - email: {}", email);
        boolean exists = clienteService.existeEmail(email);
        return ResponseEntity.ok(exists);
    }
}
