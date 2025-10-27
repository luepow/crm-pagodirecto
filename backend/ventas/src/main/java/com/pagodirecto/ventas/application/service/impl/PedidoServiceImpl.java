package com.pagodirecto.ventas.application.service.impl;

import com.pagodirecto.ventas.application.dto.PedidoDTO;
import com.pagodirecto.ventas.application.mapper.PedidoMapper;
import com.pagodirecto.ventas.application.service.PedidoService;
import com.pagodirecto.ventas.domain.Pedido;
import com.pagodirecto.ventas.domain.PedidoStatus;
import com.pagodirecto.ventas.infrastructure.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Service Implementation: PedidoServiceImpl
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoMapper pedidoMapper;

    @Override
    public PedidoDTO crear(PedidoDTO pedidoDTO) {
        log.info("Creando nuevo pedido: {}", pedidoDTO.getNumero());

        if (pedidoRepository.existsByNumero(pedidoDTO.getNumero())) {
            throw new IllegalArgumentException("Ya existe un pedido con el número: " + pedidoDTO.getNumero());
        }

        Pedido pedido = pedidoMapper.toEntity(pedidoDTO);
        pedido.setCreatedAt(Instant.now());
        pedido.setUpdatedAt(Instant.now());

        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        log.info("Pedido creado exitosamente con ID: {}", pedidoGuardado.getId());

        return pedidoMapper.toDTO(pedidoGuardado);
    }

    @Override
    public PedidoDTO actualizar(UUID id, PedidoDTO pedidoDTO) {
        log.info("Actualizando pedido con ID: {}", id);

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con ID: " + id));

        pedidoMapper.updateEntityFromDTO(pedidoDTO, pedido);
        pedido.setUpdatedAt(Instant.now());

        Pedido pedidoActualizado = pedidoRepository.save(pedido);
        log.info("Pedido actualizado exitosamente: {}", id);

        return pedidoMapper.toDTO(pedidoActualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoDTO buscarPorId(UUID id) {
        log.debug("Buscando pedido con ID: {}", id);

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con ID: " + id));

        return pedidoMapper.toDTO(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoDTO buscarPorNumero(String numero) {
        log.debug("Buscando pedido con número: {}", numero);

        Pedido pedido = pedidoRepository.findByNumero(numero)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con número: " + numero));

        return pedidoMapper.toDTO(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoDTO> listar(Pageable pageable) {
        log.debug("Listando todos los pedidos con paginación");
        return pedidoRepository.findAll(pageable)
                .map(pedidoMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoDTO> listarPorCliente(UUID clienteId, Pageable pageable) {
        log.debug("Listando pedidos del cliente: {}", clienteId);
        return pedidoRepository.findByClienteId(clienteId, pageable)
                .map(pedidoMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoDTO> listarPorStatus(PedidoStatus status, Pageable pageable) {
        log.debug("Listando pedidos con status: {}", status);
        return pedidoRepository.findByStatus(status, pageable)
                .map(pedidoMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoDTO> listarPorFechaRange(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        log.debug("Listando pedidos entre {} y {}", fechaInicio, fechaFin);
        return pedidoRepository.findByFechaRange(fechaInicio, fechaFin, pageable)
                .map(pedidoMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoDTO> listarPorPropietario(UUID propietarioId, Pageable pageable) {
        log.debug("Listando pedidos del vendedor: {}", propietarioId);
        return pedidoRepository.findByPropietarioId(propietarioId, pageable)
                .map(pedidoMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoDTO> buscar(String query, Pageable pageable) {
        log.debug("Buscando pedidos con query: {}", query);
        return pedidoRepository.searchByText(query, pageable)
                .map(pedidoMapper::toDTO);
    }

    @Override
    public PedidoDTO confirmar(UUID id) {
        log.info("Confirmando pedido con ID: {}", id);

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con ID: " + id));

        pedido.confirmar();
        Pedido pedidoConfirmado = pedidoRepository.save(pedido);

        log.info("Pedido confirmado exitosamente: {}", id);
        return pedidoMapper.toDTO(pedidoConfirmado);
    }

    @Override
    public PedidoDTO marcarEnProceso(UUID id) {
        log.info("Marcando pedido {} en proceso", id);

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con ID: " + id));

        pedido.marcarEnProceso();
        Pedido pedidoEnProceso = pedidoRepository.save(pedido);

        log.info("Pedido marcado en proceso exitosamente: {}", id);
        return pedidoMapper.toDTO(pedidoEnProceso);
    }

    @Override
    public PedidoDTO marcarEnviado(UUID id) {
        log.info("Marcando pedido {} como enviado", id);

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con ID: " + id));

        pedido.marcarEnviado();
        Pedido pedidoEnviado = pedidoRepository.save(pedido);

        log.info("Pedido marcado como enviado exitosamente: {}", id);
        return pedidoMapper.toDTO(pedidoEnviado);
    }

    @Override
    public PedidoDTO marcarEntregado(UUID id, LocalDate fechaEntrega) {
        log.info("Marcando pedido {} como entregado el {}", id, fechaEntrega);

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con ID: " + id));

        pedido.marcarEntregado(fechaEntrega);
        Pedido pedidoEntregado = pedidoRepository.save(pedido);

        log.info("Pedido marcado como entregado exitosamente: {}", id);
        return pedidoMapper.toDTO(pedidoEntregado);
    }

    @Override
    public PedidoDTO cancelar(UUID id) {
        log.info("Cancelando pedido con ID: {}", id);

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con ID: " + id));

        pedido.cancelar();
        Pedido pedidoCancelado = pedidoRepository.save(pedido);

        log.info("Pedido cancelado exitosamente: {}", id);
        return pedidoMapper.toDTO(pedidoCancelado);
    }

    @Override
    public void eliminar(UUID id) {
        log.info("Eliminando pedido con ID: {}", id);

        if (!pedidoRepository.existsById(id)) {
            throw new IllegalArgumentException("Pedido no encontrado con ID: " + id);
        }

        pedidoRepository.deleteById(id);
        log.info("Pedido eliminado exitosamente: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorStatus(PedidoStatus status) {
        log.debug("Contando pedidos con status: {}", status);
        return pedidoRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularVentasTotalesPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        log.debug("Calculando ventas totales entre {} y {}", fechaInicio, fechaFin);
        BigDecimal total = pedidoRepository.sumTotalByFechaRange(fechaInicio, fechaFin);
        return total != null ? total : BigDecimal.ZERO;
    }
}
