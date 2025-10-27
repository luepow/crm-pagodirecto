package com.empresa.crm.pagos.service;

import com.empresa.crm.pagos.dto.ActualizarEstadoPagoRequest;
import com.empresa.crm.pagos.dto.CrearPagoRequest;
import com.empresa.crm.pagos.dto.PagoDTO;
import com.empresa.crm.pagos.model.EstadoPago;
import com.empresa.crm.pagos.model.MetodoPago;
import com.empresa.crm.pagos.model.Pago;
import com.empresa.crm.pagos.repository.PagoRepository;
import com.empresa.crm.shared.exception.BusinessException;
import com.empresa.crm.shared.exception.ResourceNotFoundException;
import com.empresa.crm.ventas.model.Venta;
import com.empresa.crm.ventas.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Service implementation for Pago operations
 * Bounded context: Pagos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final VentaRepository ventaRepository;

    private static final AtomicInteger folioCounter = new AtomicInteger(0);

    @Override
    @Transactional(readOnly = true)
    public Page<PagoDTO> listar(String search, Pageable pageable) {
        log.debug("Listando pagos con búsqueda: {} y paginación: {}", search, pageable);

        Page<Pago> pagos;
        if (search != null && !search.trim().isEmpty()) {
            pagos = pagoRepository.searchPagos(search.trim(), pageable);
        } else {
            pagos = pagoRepository.findByDeletedAtIsNull(pageable);
        }

        return pagos.map(PagoDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PagoDTO> obtener(Long id) {
        log.debug("Obteniendo pago con id: {}", id);
        return pagoRepository.findByIdAndDeletedAtIsNull(id)
                .map(PagoDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PagoDTO> obtenerPorFolio(String folio) {
        log.debug("Obteniendo pago con folio: {}", folio);
        return pagoRepository.findByFolioAndDeletedAtIsNull(folio)
                .map(PagoDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoDTO> obtenerPorVenta(Long ventaId) {
        log.debug("Obteniendo pagos por venta: {}", ventaId);
        return pagoRepository.findByVentaId(ventaId).stream()
                .map(PagoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PagoDTO> obtenerPorEstado(EstadoPago estado, Pageable pageable) {
        log.debug("Obteniendo pagos por estado: {}", estado);
        return pagoRepository.findByEstadoAndDeletedAtIsNull(estado, pageable)
                .map(PagoDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PagoDTO> obtenerPorMetodoPago(MetodoPago metodoPago, Pageable pageable) {
        log.debug("Obteniendo pagos por metodo: {}", metodoPago);
        return pagoRepository.findByMetodoPagoAndDeletedAtIsNull(metodoPago, pageable)
                .map(PagoDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PagoDTO> obtenerPorFechaRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Obteniendo pagos por rango de fechas: {} - {}", startDate, endDate);
        return pagoRepository.findByFechaRange(startDate, endDate, pageable)
                .map(PagoDTO::fromEntity);
    }

    @Override
    @Transactional
    public PagoDTO crear(CrearPagoRequest request) {
        log.info("Creando nuevo pago para venta: {}", request.getVentaId());

        // Validate venta exists
        Venta venta = ventaRepository.findByIdAndDeletedAtIsNull(request.getVentaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Venta no encontrada con id: " + request.getVentaId()));

        // Validate monto does not exceed venta total
        BigDecimal totalPagado = pagoRepository.getTotalPagosByVenta(request.getVentaId());
        BigDecimal saldoPendiente = venta.getTotal().subtract(totalPagado);

        if (request.getMonto().compareTo(saldoPendiente) > 0) {
            throw new BusinessException(
                    String.format("Monto del pago (%.2f) excede el saldo pendiente (%.2f)",
                            request.getMonto(), saldoPendiente));
        }

        // Generate unique folio
        String folio = generarFolio();

        Long currentUserId = getCurrentUserId();

        // Create pago
        Pago pago = Pago.builder()
                .folio(folio)
                .venta(venta)
                .metodoPago(request.getMetodoPago())
                .monto(request.getMonto())
                .fechaPago(request.getFechaPago())
                .estado(EstadoPago.PENDIENTE)
                .referencia(request.getReferencia())
                .notas(request.getNotas())
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();

        // Validate pago
        pago.validate();

        // Save pago
        Pago savedPago = pagoRepository.save(pago);
        log.info("Pago creado exitosamente con folio: {}", savedPago.getFolio());

        return PagoDTO.fromEntity(savedPago);
    }

    @Override
    @Transactional
    public Optional<PagoDTO> actualizarEstado(Long id, ActualizarEstadoPagoRequest request) {
        log.info("Actualizando estado de pago con id: {} a {}", id, request.getEstado());

        return pagoRepository.findByIdAndDeletedAtIsNull(id)
                .map(pago -> {
                    pago.cambiarEstado(request.getEstado());
                    pago.setUpdatedBy(getCurrentUserId());

                    Pago updatedPago = pagoRepository.save(pago);
                    log.info("Estado de pago actualizado exitosamente: {}", updatedPago.getFolio());

                    return PagoDTO.fromEntity(updatedPago);
                });
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando pago con id: {}", id);

        Pago pago = pagoRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id: " + id));

        // Only allow deletion of PENDIENTE or FALLIDO pagos
        if (pago.getEstado() != EstadoPago.PENDIENTE && pago.getEstado() != EstadoPago.FALLIDO) {
            throw new BusinessException(
                    "Solo se pueden eliminar pagos en estado PENDIENTE o FALLIDO");
        }

        pago.softDelete();
        pagoRepository.save(pago);

        log.info("Pago eliminado exitosamente: {}", id);
    }

    @Override
    public String generarFolio() {
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int secuencia = folioCounter.incrementAndGet();
        String folio = String.format("PAG-%s-%04d", fecha, secuencia);

        // Ensure uniqueness
        while (pagoRepository.existsByFolioAndDeletedAtIsNull(folio)) {
            secuencia = folioCounter.incrementAndGet();
            folio = String.format("PAG-%s-%04d", fecha, secuencia);
        }

        return folio;
    }

    /**
     * Get current authenticated user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // TODO: Implement proper user ID extraction from JWT or UserDetails
            return 1L;
        }
        return 1L;
    }
}
