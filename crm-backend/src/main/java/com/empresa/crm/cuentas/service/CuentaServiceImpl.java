package com.empresa.crm.cuentas.service;

import com.empresa.crm.clientes.model.Cliente;
import com.empresa.crm.clientes.repository.ClienteRepository;
import com.empresa.crm.cuentas.dto.AplicarPagoRequest;
import com.empresa.crm.cuentas.dto.CrearCuentaRequest;
import com.empresa.crm.cuentas.dto.CuentaDTO;
import com.empresa.crm.cuentas.model.Cuenta;
import com.empresa.crm.cuentas.model.EstadoCuenta;
import com.empresa.crm.cuentas.model.TipoCuenta;
import com.empresa.crm.cuentas.repository.CuentaRepository;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Service implementation for Cuenta operations
 * Bounded context: Cuentas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final VentaRepository ventaRepository;

    private static final AtomicInteger folioCounter = new AtomicInteger(0);

    @Override
    @Transactional(readOnly = true)
    public Page<CuentaDTO> listar(String search, Pageable pageable) {
        log.debug("Listando cuentas con búsqueda: {} y paginación: {}", search, pageable);

        Page<Cuenta> cuentas;
        if (search != null && !search.trim().isEmpty()) {
            cuentas = cuentaRepository.searchCuentas(search.trim(), pageable);
        } else {
            cuentas = cuentaRepository.findByDeletedAtIsNull(pageable);
        }

        return cuentas.map(CuentaDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CuentaDTO> obtener(Long id) {
        log.debug("Obteniendo cuenta con id: {}", id);
        return cuentaRepository.findByIdAndDeletedAtIsNull(id)
                .map(CuentaDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CuentaDTO> obtenerPorFolio(String folio) {
        log.debug("Obteniendo cuenta con folio: {}", folio);
        return cuentaRepository.findByFolioAndDeletedAtIsNull(folio)
                .map(CuentaDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CuentaDTO> obtenerPorTipo(TipoCuenta tipo, Pageable pageable) {
        log.debug("Obteniendo cuentas por tipo: {}", tipo);
        return cuentaRepository.findByTipoAndDeletedAtIsNull(tipo, pageable)
                .map(CuentaDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CuentaDTO> obtenerPorEstado(EstadoCuenta estado, Pageable pageable) {
        log.debug("Obteniendo cuentas por estado: {}", estado);
        return cuentaRepository.findByEstadoAndDeletedAtIsNull(estado, pageable)
                .map(CuentaDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CuentaDTO> obtenerPorTipoYEstado(TipoCuenta tipo, EstadoCuenta estado, Pageable pageable) {
        log.debug("Obteniendo cuentas por tipo {} y estado {}", tipo, estado);
        return cuentaRepository.findByTipoAndEstadoAndDeletedAtIsNull(tipo, estado, pageable)
                .map(CuentaDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CuentaDTO> obtenerPorCliente(Long clienteId, Pageable pageable) {
        log.debug("Obteniendo cuentas por cliente: {}", clienteId);
        return cuentaRepository.findByClienteId(clienteId, pageable)
                .map(CuentaDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CuentaDTO> obtenerVencidas(Pageable pageable) {
        log.debug("Obteniendo cuentas vencidas");
        LocalDate today = LocalDate.now();
        return cuentaRepository.findCuentasVencidas(today, pageable)
                .map(CuentaDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CuentaDTO> obtenerVencidasPorTipo(TipoCuenta tipo, Pageable pageable) {
        log.debug("Obteniendo cuentas vencidas por tipo: {}", tipo);
        LocalDate today = LocalDate.now();
        return cuentaRepository.findCuentasVencidasByTipo(tipo, today, pageable)
                .map(CuentaDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaDTO> obtenerPorReferencia(String referenciaTipo, Long referenciaId) {
        log.debug("Obteniendo cuentas por referencia: {} - {}", referenciaTipo, referenciaId);
        return cuentaRepository.findByReferencia(referenciaTipo, referenciaId).stream()
                .map(CuentaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CuentaDTO crear(CrearCuentaRequest request) {
        log.info("Creando nueva cuenta tipo {} para cliente: {}", request.getTipo(), request.getClienteId());

        // Validate cliente exists
        Cliente cliente = clienteRepository.findByIdAndDeletedAtIsNull(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con id: " + request.getClienteId()));

        // Validate fecha vencimiento is after fecha emision
        if (request.getFechaVencimiento().isBefore(request.getFechaEmision())) {
            throw new BusinessException(
                    "Fecha vencimiento debe ser posterior a fecha emision");
        }

        // Generate unique folio
        String folio = generarFolio(request.getTipo());

        Long currentUserId = getCurrentUserId();

        // Create cuenta
        Cuenta cuenta = Cuenta.builder()
                .folio(folio)
                .tipo(request.getTipo())
                .referenciaTipo(request.getReferenciaTipo())
                .referenciaId(request.getReferenciaId())
                .cliente(cliente)
                .descripcion(request.getDescripcion())
                .monto(request.getMonto())
                .saldo(request.getMonto())
                .fechaEmision(request.getFechaEmision())
                .fechaVencimiento(request.getFechaVencimiento())
                .estado(EstadoCuenta.PENDIENTE)
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();

        // Validate cuenta
        cuenta.validate();

        // Check if already vencida
        cuenta.checkVencimiento();

        // Save cuenta
        Cuenta savedCuenta = cuentaRepository.save(cuenta);
        log.info("Cuenta creada exitosamente con folio: {}", savedCuenta.getFolio());

        return CuentaDTO.fromEntity(savedCuenta);
    }

    @Override
    @Transactional
    public CuentaDTO crearDesdeVenta(Long ventaId) {
        log.info("Creando cuenta por cobrar desde venta: {}", ventaId);

        // Validate venta exists
        Venta venta = ventaRepository.findByIdAndDeletedAtIsNull(ventaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Venta no encontrada con id: " + ventaId));

        // Check if cuenta already exists for this venta
        List<Cuenta> existingCuentas = cuentaRepository.findByReferencia("VENTA", ventaId);
        if (!existingCuentas.isEmpty()) {
            throw new BusinessException(
                    "Ya existe una cuenta por cobrar para la venta: " + venta.getFolio());
        }

        // Create cuenta por cobrar
        String folio = generarFolio(TipoCuenta.COBRAR);
        Long currentUserId = getCurrentUserId();

        Cuenta cuenta = Cuenta.builder()
                .folio(folio)
                .tipo(TipoCuenta.COBRAR)
                .referenciaTipo("VENTA")
                .referenciaId(venta.getId())
                .cliente(venta.getCliente())
                .descripcion("Cuenta por cobrar - Venta " + venta.getFolio())
                .monto(venta.getTotal())
                .saldo(venta.getTotal())
                .fechaEmision(venta.getFechaVenta())
                .fechaVencimiento(venta.getFechaVenta().plusDays(30)) // Default: 30 days
                .estado(EstadoCuenta.PENDIENTE)
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();

        // Validate and check vencimiento
        cuenta.validate();
        cuenta.checkVencimiento();

        // Save cuenta
        Cuenta savedCuenta = cuentaRepository.save(cuenta);
        log.info("Cuenta por cobrar creada desde venta con folio: {}", savedCuenta.getFolio());

        return CuentaDTO.fromEntity(savedCuenta);
    }

    @Override
    @Transactional
    public Optional<CuentaDTO> aplicarPago(Long id, AplicarPagoRequest request) {
        log.info("Aplicando pago de {} a cuenta: {}", request.getMontoPago(), id);

        return cuentaRepository.findByIdAndDeletedAtIsNull(id)
                .map(cuenta -> {
                    cuenta.aplicarPago(request.getMontoPago());
                    cuenta.setUpdatedBy(getCurrentUserId());

                    Cuenta updatedCuenta = cuentaRepository.save(cuenta);
                    log.info("Pago aplicado exitosamente a cuenta: {}", updatedCuenta.getFolio());

                    return CuentaDTO.fromEntity(updatedCuenta);
                });
    }

    @Override
    @Transactional
    public Optional<CuentaDTO> marcarPagada(Long id) {
        log.info("Marcando cuenta como pagada: {}", id);

        return cuentaRepository.findByIdAndDeletedAtIsNull(id)
                .map(cuenta -> {
                    cuenta.marcarPagada();
                    cuenta.setUpdatedBy(getCurrentUserId());

                    Cuenta updatedCuenta = cuentaRepository.save(cuenta);
                    log.info("Cuenta marcada como pagada: {}", updatedCuenta.getFolio());

                    return CuentaDTO.fromEntity(updatedCuenta);
                });
    }

    @Override
    @Transactional
    public Optional<CuentaDTO> cancelar(Long id) {
        log.info("Cancelando cuenta: {}", id);

        return cuentaRepository.findByIdAndDeletedAtIsNull(id)
                .map(cuenta -> {
                    cuenta.cancelar();
                    cuenta.setUpdatedBy(getCurrentUserId());

                    Cuenta cancelledCuenta = cuentaRepository.save(cuenta);
                    log.info("Cuenta cancelada exitosamente: {}", cancelledCuenta.getFolio());

                    return CuentaDTO.fromEntity(cancelledCuenta);
                });
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando cuenta con id: {}", id);

        Cuenta cuenta = cuentaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + id));

        // Only allow deletion of PENDIENTE or CANCELADO cuentas
        if (cuenta.getEstado() != EstadoCuenta.PENDIENTE && cuenta.getEstado() != EstadoCuenta.CANCELADO) {
            throw new BusinessException(
                    "Solo se pueden eliminar cuentas en estado PENDIENTE o CANCELADO");
        }

        cuenta.softDelete();
        cuentaRepository.save(cuenta);

        log.info("Cuenta eliminada exitosamente: {}", id);
    }

    @Override
    public String generarFolio(TipoCuenta tipo) {
        String prefix = tipo == TipoCuenta.COBRAR ? "CXC" : "CXP";
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int secuencia = folioCounter.incrementAndGet();
        String folio = String.format("%s-%s-%04d", prefix, fecha, secuencia);

        // Ensure uniqueness
        while (cuentaRepository.existsByFolioAndDeletedAtIsNull(folio)) {
            secuencia = folioCounter.incrementAndGet();
            folio = String.format("%s-%s-%04d", prefix, fecha, secuencia);
        }

        return folio;
    }

    @Override
    @Transactional
    public void procesarCuentasVencidas() {
        log.info("Procesando cuentas vencidas...");

        LocalDate today = LocalDate.now();
        Page<Cuenta> cuentasVencidas = cuentaRepository.findCuentasVencidas(
            today,
            Pageable.unpaged()
        );

        int count = 0;
        for (Cuenta cuenta : cuentasVencidas) {
            cuenta.checkVencimiento();
            cuentaRepository.save(cuenta);
            count++;
        }

        log.info("Procesadas {} cuentas vencidas", count);
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
