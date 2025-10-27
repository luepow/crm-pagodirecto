package com.pagodirecto.clientes.api.controller;

import com.pagodirecto.clientes.application.dto.ClienteDTO;
import com.pagodirecto.clientes.application.dto.ImportacionResultDTO;
import com.pagodirecto.clientes.application.service.ClienteImportacionService;
import com.pagodirecto.clientes.application.service.ClienteService;
import com.pagodirecto.clientes.domain.ClienteStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Controlador REST: Cliente
 *
 * Expone endpoints RESTful para la gestión de clientes.
 * Incluye operaciones CRUD y funcionalidades de negocio específicas.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@RestController
@RequestMapping("/v1/clientes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Clientes", description = "API para gestión de clientes (CRM)")
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteImportacionService clienteImportacionService;

    @Operation(summary = "Crear nuevo cliente", description = "Crea un nuevo cliente en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente",
                     content = @Content(schema = @Schema(implementation = ClienteDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o cliente duplicado"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping
    public ResponseEntity<ClienteDTO> crear(
            @Valid @RequestBody ClienteDTO clienteDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Creando nuevo cliente - usuario: {}", userDetails.getUsername());
        UUID usuarioId = UUID.randomUUID(); // TODO: Obtener del contexto de seguridad
        ClienteDTO created = clienteService.crear(clienteDTO, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualizar cliente", description = "Actualiza un cliente existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente",
                     content = @Content(schema = @Schema(implementation = ClienteDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> actualizar(
            @Parameter(description = "UUID del cliente") @PathVariable UUID id,
            @Valid @RequestBody ClienteDTO clienteDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Actualizando cliente {} - usuario: {}", id, userDetails.getUsername());
        UUID usuarioId = UUID.randomUUID(); // TODO: Obtener del contexto de seguridad
        ClienteDTO updated = clienteService.actualizar(id, clienteDTO, usuarioId);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Obtener cliente por ID", description = "Busca un cliente por su UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                     content = @Content(schema = @Schema(implementation = ClienteDTO.class))),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> obtenerPorId(
            @Parameter(description = "UUID del cliente") @PathVariable UUID id) {

        log.debug("Obteniendo cliente por ID: {}", id);
        ClienteDTO cliente = clienteService.buscarPorId(id);
        return ResponseEntity.ok(cliente);
    }

    @Operation(summary = "Obtener cliente por código", description = "Busca un cliente por su código único")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                     content = @Content(schema = @Schema(implementation = ClienteDTO.class))),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ClienteDTO> obtenerPorCodigo(
            @Parameter(description = "Código único del cliente") @PathVariable String codigo) {

        log.debug("Obteniendo cliente por código: {}", codigo);
        ClienteDTO cliente = clienteService.buscarPorCodigo(codigo);
        return ResponseEntity.ok(cliente);
    }

    @Operation(summary = "Listar todos los clientes", description = "Obtiene una lista paginada de todos los clientes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping
    public ResponseEntity<Page<ClienteDTO>> listarTodos(
            @PageableDefault(size = 20, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {

        log.debug("Listando todos los clientes - página: {}", pageable.getPageNumber());
        Page<ClienteDTO> clientes = clienteService.listarTodos(pageable);
        return ResponseEntity.ok(clientes);
    }

    @Operation(summary = "Buscar clientes", description = "Busca clientes por nombre, email, RFC o razón social")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Búsqueda completada exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<ClienteDTO>> buscar(
            @Parameter(description = "Término de búsqueda") @RequestParam String q,
            @PageableDefault(size = 20, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {

        log.debug("Buscando clientes con término: {}", q);
        Page<ClienteDTO> clientes = clienteService.buscar(q, pageable);
        return ResponseEntity.ok(clientes);
    }

    @Operation(summary = "Buscar por status", description = "Obtiene clientes filtrados por status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Búsqueda completada exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ClienteDTO>> buscarPorStatus(
            @Parameter(description = "Status del cliente") @PathVariable ClienteStatus status,
            @PageableDefault(size = 20, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {

        log.debug("Buscando clientes por status: {}", status);
        Page<ClienteDTO> clientes = clienteService.buscarPorStatus(status, pageable);
        return ResponseEntity.ok(clientes);
    }

    @Operation(summary = "Buscar por propietario", description = "Obtiene clientes asignados a un propietario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Búsqueda completada exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/propietario/{propietarioId}")
    public ResponseEntity<Page<ClienteDTO>> buscarPorPropietario(
            @Parameter(description = "UUID del propietario") @PathVariable UUID propietarioId,
            @PageableDefault(size = 20, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {

        log.debug("Buscando clientes por propietario: {}", propietarioId);
        Page<ClienteDTO> clientes = clienteService.buscarPorPropietario(propietarioId, pageable);
        return ResponseEntity.ok(clientes);
    }

    @Operation(summary = "Eliminar cliente", description = "Elimina un cliente (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cliente eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "UUID del cliente") @PathVariable UUID id) {

        log.info("Eliminando cliente: {}", id);
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activar cliente", description = "Activa un cliente inactivo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente activado exitosamente",
                     content = @Content(schema = @Schema(implementation = ClienteDTO.class))),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PutMapping("/{id}/activar")
    public ResponseEntity<ClienteDTO> activar(
            @Parameter(description = "UUID del cliente") @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Activando cliente {} - usuario: {}", id, userDetails.getUsername());
        UUID usuarioId = UUID.randomUUID(); // TODO: Obtener del contexto de seguridad
        ClienteDTO cliente = clienteService.activar(id, usuarioId);
        return ResponseEntity.ok(cliente);
    }

    @Operation(summary = "Desactivar cliente", description = "Desactiva un cliente activo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente desactivado exitosamente",
                     content = @Content(schema = @Schema(implementation = ClienteDTO.class))),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<ClienteDTO> desactivar(
            @Parameter(description = "UUID del cliente") @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Desactivando cliente {} - usuario: {}", id, userDetails.getUsername());
        UUID usuarioId = UUID.randomUUID(); // TODO: Obtener del contexto de seguridad
        ClienteDTO cliente = clienteService.desactivar(id, usuarioId);
        return ResponseEntity.ok(cliente);
    }

    @Operation(summary = "Convertir lead a prospecto", description = "Califica un lead y lo convierte en prospecto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lead convertido exitosamente",
                     content = @Content(schema = @Schema(implementation = ClienteDTO.class))),
        @ApiResponse(responseCode = "400", description = "El cliente no es un lead"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PutMapping("/{id}/convertir-a-prospecto")
    public ResponseEntity<ClienteDTO> convertirAProspecto(
            @Parameter(description = "UUID del cliente") @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Convirtiendo lead a prospecto {} - usuario: {}", id, userDetails.getUsername());
        UUID usuarioId = UUID.randomUUID(); // TODO: Obtener del contexto de seguridad
        ClienteDTO cliente = clienteService.convertirAProspecto(id, usuarioId);
        return ResponseEntity.ok(cliente);
    }

    @Operation(summary = "Convertir prospecto a cliente", description = "Convierte un prospecto en cliente activo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Prospecto convertido exitosamente",
                     content = @Content(schema = @Schema(implementation = ClienteDTO.class))),
        @ApiResponse(responseCode = "400", description = "El cliente no es un prospecto"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PutMapping("/{id}/convertir-a-cliente")
    public ResponseEntity<ClienteDTO> convertirACliente(
            @Parameter(description = "UUID del cliente") @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Convirtiendo prospecto a cliente {} - usuario: {}", id, userDetails.getUsername());
        UUID usuarioId = UUID.randomUUID(); // TODO: Obtener del contexto de seguridad
        ClienteDTO cliente = clienteService.convertirACliente(id, usuarioId);
        return ResponseEntity.ok(cliente);
    }

    @Operation(summary = "Agregar a blacklist", description = "Agrega un cliente a la lista negra")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente agregado a blacklist exitosamente",
                     content = @Content(schema = @Schema(implementation = ClienteDTO.class))),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PutMapping("/{id}/blacklist")
    public ResponseEntity<ClienteDTO> agregarABlacklist(
            @Parameter(description = "UUID del cliente") @PathVariable UUID id,
            @Parameter(description = "Motivo del blacklist") @RequestParam String motivo,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Agregando cliente a blacklist {} - usuario: {} - motivo: {}",
                 id, userDetails.getUsername(), motivo);
        UUID usuarioId = UUID.randomUUID(); // TODO: Obtener del contexto de seguridad
        ClienteDTO cliente = clienteService.agregarABlacklist(id, motivo, usuarioId);
        return ResponseEntity.ok(cliente);
    }

    @Operation(summary = "Contar por status", description = "Cuenta clientes por status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conteo realizado exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> contarPorStatus(
            @Parameter(description = "Status del cliente") @PathVariable ClienteStatus status) {

        log.debug("Contando clientes por status: {}", status);
        long count = clienteService.contarPorStatus(status);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Importar clientes desde CSV",
               description = "Importa múltiples clientes desde un archivo CSV con nombres de empresas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Importación procesada (puede tener errores parciales)",
                     content = @Content(schema = @Schema(implementation = ImportacionResultDTO.class))),
        @ApiResponse(responseCode = "400", description = "Archivo inválido o vacío"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping(value = "/importar", consumes = "multipart/form-data")
    public ResponseEntity<ImportacionResultDTO> importarDesdeCSV(
            @Parameter(description = "Archivo CSV con nombres de empresas")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "UUID de la unidad de negocio")
            @RequestParam("unidadNegocioId") UUID unidadNegocioId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Importando clientes desde CSV - usuario: {}, archivo: {}",
                 userDetails.getUsername(), file.getOriginalFilename());

        // Validar que sea un archivo CSV
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            ImportacionResultDTO error = ImportacionResultDTO.builder()
                    .mensaje("El archivo debe ser formato CSV")
                    .totalRegistros(0)
                    .registrosExitosos(0)
                    .registrosConErrores(0)
                    .build();
            return ResponseEntity.badRequest().body(error);
        }

        UUID usuarioId = UUID.randomUUID(); // TODO: Obtener del contexto de seguridad
        ImportacionResultDTO resultado = clienteImportacionService.importarDesdeCSV(
                file, unidadNegocioId, usuarioId);

        return ResponseEntity.ok(resultado);
    }
}
