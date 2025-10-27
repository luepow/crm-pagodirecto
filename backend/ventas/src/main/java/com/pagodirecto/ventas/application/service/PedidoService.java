package com.pagodirecto.ventas.application.service;

import com.pagodirecto.ventas.application.dto.PedidoDTO;
import com.pagodirecto.ventas.domain.PedidoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Service: PedidoService
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
public interface PedidoService {

    PedidoDTO crear(PedidoDTO pedidoDTO);

    PedidoDTO actualizar(UUID id, PedidoDTO pedidoDTO);

    PedidoDTO buscarPorId(UUID id);

    PedidoDTO buscarPorNumero(String numero);

    Page<PedidoDTO> listar(Pageable pageable);

    Page<PedidoDTO> listarPorCliente(UUID clienteId, Pageable pageable);

    Page<PedidoDTO> listarPorStatus(PedidoStatus status, Pageable pageable);

    Page<PedidoDTO> listarPorFechaRange(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);

    Page<PedidoDTO> listarPorPropietario(UUID propietarioId, Pageable pageable);

    Page<PedidoDTO> buscar(String query, Pageable pageable);

    PedidoDTO confirmar(UUID id);

    PedidoDTO marcarEnProceso(UUID id);

    PedidoDTO marcarEnviado(UUID id);

    PedidoDTO marcarEntregado(UUID id, LocalDate fechaEntrega);

    PedidoDTO cancelar(UUID id);

    void eliminar(UUID id);

    long contarPorStatus(PedidoStatus status);

    BigDecimal calcularVentasTotalesPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin);
}
