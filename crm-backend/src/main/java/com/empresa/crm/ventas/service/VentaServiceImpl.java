package com.empresa.crm.ventas.service;

import com.empresa.crm.clientes.model.Cliente;
import com.empresa.crm.clientes.repository.ClienteRepository;
import com.empresa.crm.productos.domain.Producto;
import com.empresa.crm.productos.infrastructure.repository.ProductoRepository;
import com.empresa.crm.shared.exception.BusinessException;
import com.empresa.crm.shared.exception.ResourceNotFoundException;
import com.empresa.crm.ventas.dto.ActualizarEstadoVentaRequest;
import com.empresa.crm.ventas.dto.CrearDetalleVentaRequest;
import com.empresa.crm.ventas.dto.CrearVentaRequest;
import com.empresa.crm.ventas.dto.VentaDTO;
import com.empresa.crm.ventas.model.DetalleVenta;
import com.empresa.crm.ventas.model.EstadoVenta;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service implementation for Venta operations
 * Bounded context: Ventas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;

    private static final AtomicInteger folioCounter = new AtomicInteger(0);

    @Override
    @Transactional(readOnly = true)
    public Page<VentaDTO> listar(String search, Pageable pageable) {
        log.debug("Listando ventas con búsqueda: {} y paginación: {}", search, pageable);

        Page<Venta> ventas;
        if (search != null && !search.trim().isEmpty()) {
            ventas = ventaRepository.searchVentas(search.trim(), pageable);
        } else {
            ventas = ventaRepository.findByDeletedAtIsNull(pageable);
        }

        return ventas.map(VentaDTO::fromEntityWithoutDetalles);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VentaDTO> obtener(Long id) {
        log.debug("Obteniendo venta con id: {}", id);
        return ventaRepository.findByIdAndDeletedAtIsNull(id)
                .map(VentaDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VentaDTO> obtenerPorFolio(String folio) {
        log.debug("Obteniendo venta con folio: {}", folio);
        return ventaRepository.findByFolioAndDeletedAtIsNull(folio)
                .map(VentaDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaDTO> obtenerPorCliente(Long clienteId, Pageable pageable) {
        log.debug("Obteniendo ventas por cliente: {}", clienteId);
        return ventaRepository.findByClienteId(clienteId, pageable)
                .map(VentaDTO::fromEntityWithoutDetalles);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaDTO> obtenerPorEstado(EstadoVenta estado, Pageable pageable) {
        log.debug("Obteniendo ventas por estado: {}", estado);
        return ventaRepository.findByEstadoAndDeletedAtIsNull(estado, pageable)
                .map(VentaDTO::fromEntityWithoutDetalles);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaDTO> obtenerPorFechaRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Obteniendo ventas por rango de fechas: {} - {}", startDate, endDate);
        return ventaRepository.findByFechaRange(startDate, endDate, pageable)
                .map(VentaDTO::fromEntityWithoutDetalles);
    }

    @Override
    @Transactional
    public VentaDTO crear(CrearVentaRequest request) {
        log.info("Creando nueva venta para cliente: {}", request.getClienteId());

        // Validate cliente exists
        Cliente cliente = clienteRepository.findByIdAndDeletedAtIsNull(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con id: " + request.getClienteId()));

        // Generate unique folio
        String folio = generarFolio();

        Long currentUserId = getCurrentUserId();

        // Create venta
        Venta venta = Venta.builder()
                .folio(folio)
                .cliente(cliente)
                .fechaVenta(request.getFechaVenta())
                .estado(EstadoVenta.BORRADOR)
                .descuento(request.getDescuento() != null ? request.getDescuento() : BigDecimal.ZERO)
                .impuestos(request.getImpuestos() != null ? request.getImpuestos() : BigDecimal.ZERO)
                .notas(request.getNotas())
                .detalles(new ArrayList<>())
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();

        // Add detalles
        for (CrearDetalleVentaRequest detalleRequest : request.getDetalles()) {
            DetalleVenta detalle = createDetalleFromRequest(detalleRequest, venta);
            venta.addDetalle(detalle);
        }

        // Validate stock availability
        validateStockAvailability(venta);

        // Calculate totals
        venta.calculateTotals();

        // Validate venta
        venta.validate();

        // Save venta
        Venta savedVenta = ventaRepository.save(venta);
        log.info("Venta creada exitosamente con folio: {}", savedVenta.getFolio());

        return VentaDTO.fromEntity(savedVenta);
    }

    @Override
    @Transactional
    public Optional<VentaDTO> actualizarEstado(Long id, ActualizarEstadoVentaRequest request) {
        log.info("Actualizando estado de venta con id: {} a {}", id, request.getEstado());

        return ventaRepository.findByIdAndDeletedAtIsNull(id)
                .map(venta -> {
                    venta.cambiarEstado(request.getEstado());
                    venta.setUpdatedBy(getCurrentUserId());

                    // If confirming, validate stock and reduce inventory
                    if (request.getEstado() == EstadoVenta.CONFIRMADA) {
                        venta.confirmar();
                        validateStockAvailability(venta);
                        reducirInventario(venta);
                    }

                    Venta updatedVenta = ventaRepository.save(venta);
                    log.info("Estado de venta actualizado exitosamente: {}", updatedVenta.getFolio());

                    return VentaDTO.fromEntity(updatedVenta);
                });
    }

    @Override
    @Transactional
    public Optional<VentaDTO> cancelar(Long id) {
        log.info("Cancelando venta con id: {}", id);

        return ventaRepository.findByIdAndDeletedAtIsNull(id)
                .map(venta -> {
                    EstadoVenta estadoAnterior = venta.getEstado();
                    venta.cancelar();
                    venta.setUpdatedBy(getCurrentUserId());

                    // If venta was confirmed, restore inventory
                    if (estadoAnterior == EstadoVenta.CONFIRMADA ||
                        estadoAnterior == EstadoVenta.ENVIADA ||
                        estadoAnterior == EstadoVenta.COMPLETADA) {
                        restaurarInventario(venta);
                    }

                    Venta cancelledVenta = ventaRepository.save(venta);
                    log.info("Venta cancelada exitosamente: {}", cancelledVenta.getFolio());

                    return VentaDTO.fromEntity(cancelledVenta);
                });
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando venta con id: {}", id);

        Venta venta = ventaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));

        // Only allow deletion of BORRADOR or CANCELADA ventas
        if (venta.getEstado() != EstadoVenta.BORRADOR && venta.getEstado() != EstadoVenta.CANCELADA) {
            throw new BusinessException(
                    "Solo se pueden eliminar ventas en estado BORRADOR o CANCELADA");
        }

        venta.softDelete();
        ventaRepository.save(venta);

        log.info("Venta eliminada exitosamente: {}", id);
    }

    @Override
    public String generarFolio() {
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int secuencia = folioCounter.incrementAndGet();
        String folio = String.format("VTA-%s-%04d", fecha, secuencia);

        // Ensure uniqueness
        while (ventaRepository.existsByFolioAndDeletedAtIsNull(folio)) {
            secuencia = folioCounter.incrementAndGet();
            folio = String.format("VTA-%s-%04d", fecha, secuencia);
        }

        return folio;
    }

    /**
     * Create DetalleVenta from request
     */
    private DetalleVenta createDetalleFromRequest(CrearDetalleVentaRequest request, Venta venta) {
        Producto producto = productoRepository.findByIdAndNotDeleted(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id: " + request.getProductoId()));

        return DetalleVenta.builder()
                .venta(venta)
                .producto(producto)
                .cantidad(request.getCantidad())
                .precioUnitario(request.getPrecioUnitario())
                .descuento(request.getDescuento() != null ? request.getDescuento() : BigDecimal.ZERO)
                .build();
    }

    /**
     * Validate stock availability for all detalles
     */
    private void validateStockAvailability(Venta venta) {
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            if (producto.getStock() < detalle.getCantidad()) {
                throw new BusinessException(
                        String.format("Stock insuficiente para producto %s. Disponible: %d, Solicitado: %d",
                                producto.getNombre(), producto.getStock(), detalle.getCantidad()));
            }
        }
    }

    /**
     * Reduce inventory stock
     */
    private void reducirInventario(Venta venta) {
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.adjustStock(-detalle.getCantidad());
            productoRepository.save(producto);
            log.debug("Inventario reducido para producto {}: {}", producto.getCodigo(), detalle.getCantidad());
        }
    }

    /**
     * Restore inventory stock
     */
    private void restaurarInventario(Venta venta) {
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.adjustStock(detalle.getCantidad());
            productoRepository.save(producto);
            log.debug("Inventario restaurado para producto {}: {}", producto.getCodigo(), detalle.getCantidad());
        }
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
