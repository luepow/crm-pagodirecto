package com.pagodirecto.ventas.api.controller;

import com.pagodirecto.ventas.application.dto.PedidoDTO;
import com.pagodirecto.ventas.application.service.PedidoService;
import com.pagodirecto.ventas.domain.PedidoStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
 * REST Controller: Pedidos (Ventas)
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@RestController
@RequestMapping("/v1/ventas/pedidos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ventas - Pedidos", description = "API de gestión de pedidos y órdenes de venta")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @Operation(summary = "Crear nuevo pedido")
    public ResponseEntity<PedidoDTO> crear(
            @Valid @RequestBody PedidoDTO pedidoDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Solicitud para crear pedido: {} por usuario: {}",
                pedidoDTO.getNumero(), userDetails.getUsername());

        PedidoDTO pedidoCreado = pedidoService.crear(pedidoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoCreado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar pedido existente")
    public ResponseEntity<PedidoDTO> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody PedidoDTO pedidoDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Solicitud para actualizar pedido {} por usuario: {}",
                id, userDetails.getUsername());

        PedidoDTO pedidoActualizado = pedidoService.actualizar(id, pedidoDTO);
        return ResponseEntity.ok(pedidoActualizado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pedido por ID")
    public ResponseEntity<PedidoDTO> obtenerPorId(@PathVariable UUID id) {
        log.debug("Solicitud para obtener pedido con ID: {}", id);
        PedidoDTO pedido = pedidoService.buscarPorId(id);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/numero/{numero}")
    @Operation(summary = "Obtener pedido por número")
    public ResponseEntity<PedidoDTO> obtenerPorNumero(@PathVariable String numero) {
        log.debug("Solicitud para obtener pedido con número: {}", numero);
        PedidoDTO pedido = pedidoService.buscarPorNumero(numero);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping
    @Operation(summary = "Listar todos los pedidos con paginación")
    public ResponseEntity<Page<PedidoDTO>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fecha,desc") String[] sort) {
        log.debug("Solicitud para listar pedidos - página: {}, tamaño: {}", page, size);

        Pageable pageable = crearPageable(page, size, sort);
        Page<PedidoDTO> pedidos = pedidoService.listar(pageable);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar pedidos de un cliente")
    public ResponseEntity<Page<PedidoDTO>> listarPorCliente(
            @PathVariable UUID clienteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fecha,desc") String[] sort) {
        log.debug("Solicitud para listar pedidos del cliente: {}", clienteId);

        Pageable pageable = crearPageable(page, size, sort);
        Page<PedidoDTO> pedidos = pedidoService.listarPorCliente(clienteId, pageable);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar pedidos por status")
    public ResponseEntity<Page<PedidoDTO>> listarPorStatus(
            @PathVariable PedidoStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fecha,desc") String[] sort) {
        log.debug("Solicitud para listar pedidos con status: {}", status);

        Pageable pageable = crearPageable(page, size, sort);
        Page<PedidoDTO> pedidos = pedidoService.listarPorStatus(status, pageable);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/fecha-range")
    @Operation(summary = "Listar pedidos por rango de fechas")
    public ResponseEntity<Page<PedidoDTO>> listarPorFechaRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fecha,desc") String[] sort) {
        log.debug("Solicitud para listar pedidos entre {} y {}", fechaInicio, fechaFin);

        Pageable pageable = crearPageable(page, size, sort);
        Page<PedidoDTO> pedidos = pedidoService.listarPorFechaRange(fechaInicio, fechaFin, pageable);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/propietario/{propietarioId}")
    @Operation(summary = "Listar pedidos de un vendedor")
    public ResponseEntity<Page<PedidoDTO>> listarPorPropietario(
            @PathVariable UUID propietarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fecha,desc") String[] sort) {
        log.debug("Solicitud para listar pedidos del vendedor: {}", propietarioId);

        Pageable pageable = crearPageable(page, size, sort);
        Page<PedidoDTO> pedidos = pedidoService.listarPorPropietario(propietarioId, pageable);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar pedidos por texto")
    public ResponseEntity<Page<PedidoDTO>> buscar(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fecha,desc") String[] sort) {
        log.debug("Solicitud de búsqueda de pedidos con query: {}", q);

        Pageable pageable = crearPageable(page, size, sort);
        Page<PedidoDTO> pedidos = pedidoService.buscar(q, pageable);
        return ResponseEntity.ok(pedidos);
    }

    @PutMapping("/{id}/confirmar")
    @Operation(summary = "Confirmar pedido")
    public ResponseEntity<PedidoDTO> confirmar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Solicitud para confirmar pedido {} por usuario: {}",
                id, userDetails.getUsername());

        PedidoDTO pedidoConfirmado = pedidoService.confirmar(id);
        return ResponseEntity.ok(pedidoConfirmado);
    }

    @PutMapping("/{id}/en-proceso")
    @Operation(summary = "Marcar pedido en proceso")
    public ResponseEntity<PedidoDTO> marcarEnProceso(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Solicitud para marcar pedido {} en proceso por usuario: {}",
                id, userDetails.getUsername());

        PedidoDTO pedidoEnProceso = pedidoService.marcarEnProceso(id);
        return ResponseEntity.ok(pedidoEnProceso);
    }

    @PutMapping("/{id}/enviado")
    @Operation(summary = "Marcar pedido como enviado")
    public ResponseEntity<PedidoDTO> marcarEnviado(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Solicitud para marcar pedido {} como enviado por usuario: {}",
                id, userDetails.getUsername());

        PedidoDTO pedidoEnviado = pedidoService.marcarEnviado(id);
        return ResponseEntity.ok(pedidoEnviado);
    }

    @PutMapping("/{id}/entregado")
    @Operation(summary = "Marcar pedido como entregado")
    public ResponseEntity<PedidoDTO> marcarEntregado(
            @PathVariable UUID id,
            @RequestBody Map<String, LocalDate> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        LocalDate fechaEntrega = body.get("fechaEntrega");
        log.info("Solicitud para marcar pedido {} como entregado el {} por usuario: {}",
                id, fechaEntrega, userDetails.getUsername());

        PedidoDTO pedidoEntregado = pedidoService.marcarEntregado(id, fechaEntrega);
        return ResponseEntity.ok(pedidoEntregado);
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar pedido")
    public ResponseEntity<PedidoDTO> cancelar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Solicitud para cancelar pedido {} por usuario: {}",
                id, userDetails.getUsername());

        PedidoDTO pedidoCancelado = pedidoService.cancelar(id);
        return ResponseEntity.ok(pedidoCancelado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar pedido (soft delete)")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Solicitud para eliminar pedido {} por usuario: {}",
                id, userDetails.getUsername());

        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/status/{status}")
    @Operation(summary = "Contar pedidos por status")
    public ResponseEntity<Long> contarPorStatus(@PathVariable PedidoStatus status) {
        log.debug("Solicitud para contar pedidos con status: {}", status);

        long count = pedidoService.contarPorStatus(status);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/ventas-totales")
    @Operation(summary = "Calcular ventas totales por período")
    public ResponseEntity<BigDecimal> calcularVentasTotales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        log.debug("Solicitud para calcular ventas totales entre {} y {}", fechaInicio, fechaFin);

        BigDecimal total = pedidoService.calcularVentasTotalesPorPeriodo(fechaInicio, fechaFin);
        return ResponseEntity.ok(total);
    }

    private Pageable crearPageable(int page, int size, String[] sort) {
        String sortField = sort[0];
        String sortDirection = sort.length > 1 ? sort[1] : "asc";

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }
}
