package com.pagodirecto.oportunidades.application.service.impl;

import com.pagodirecto.oportunidades.application.dto.OportunidadDTO;
import com.pagodirecto.oportunidades.application.mapper.OportunidadMapper;
import com.pagodirecto.oportunidades.application.service.OportunidadService;
import com.pagodirecto.oportunidades.domain.Oportunidad;
import com.pagodirecto.oportunidades.infrastructure.repository.OportunidadRepository;
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
 * Implementación: OportunidadService
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OportunidadServiceImpl implements OportunidadService {

    private final OportunidadRepository oportunidadRepository;
    private final OportunidadMapper oportunidadMapper;

    @Override
    public OportunidadDTO crear(OportunidadDTO oportunidadDTO, UUID usuarioId) {
        log.info("Creando nueva oportunidad: {}", oportunidadDTO.getTitulo());

        Oportunidad oportunidad = oportunidadMapper.toEntity(oportunidadDTO);
        oportunidad.setCreatedBy(usuarioId);
        oportunidad.setUpdatedBy(usuarioId);
        oportunidad.setCreatedAt(Instant.now());
        oportunidad.setUpdatedAt(Instant.now());

        oportunidad = oportunidadRepository.save(oportunidad);
        return oportunidadMapper.toDTO(oportunidad);
    }

    @Override
    public OportunidadDTO actualizar(UUID id, OportunidadDTO oportunidadDTO, UUID usuarioId) {
        log.info("Actualizando oportunidad: {}", id);

        Oportunidad oportunidad = oportunidadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidad no encontrada: " + id));

        oportunidadMapper.updateEntityFromDTO(oportunidadDTO, oportunidad);
        oportunidad.setUpdatedBy(usuarioId);
        oportunidad.setUpdatedAt(Instant.now());

        oportunidad = oportunidadRepository.save(oportunidad);
        return oportunidadMapper.toDTO(oportunidad);
    }

    @Override
    @Transactional(readOnly = true)
    public OportunidadDTO buscarPorId(UUID id) {
        log.debug("Buscando oportunidad por ID: {}", id);

        Oportunidad oportunidad = oportunidadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidad no encontrada: " + id));

        return oportunidadMapper.toDTO(oportunidad);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OportunidadDTO> listarTodas(Pageable pageable) {
        log.debug("Listando todas las oportunidades");
        return oportunidadRepository.findAll(pageable)
                .map(oportunidadMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OportunidadDTO> buscarPorCliente(UUID clienteId, Pageable pageable) {
        log.debug("Buscando oportunidades por cliente: {}", clienteId);
        return oportunidadRepository.findByClienteId(clienteId, pageable)
                .map(oportunidadMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OportunidadDTO> buscarPorEtapa(UUID etapaId, Pageable pageable) {
        log.debug("Buscando oportunidades por etapa: {}", etapaId);
        return oportunidadRepository.findByEtapaId(etapaId, pageable)
                .map(oportunidadMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OportunidadDTO> buscarPorPropietario(UUID propietarioId, Pageable pageable) {
        log.debug("Buscando oportunidades por propietario: {}", propietarioId);
        return oportunidadRepository.findByPropietarioId(propietarioId, pageable)
                .map(oportunidadMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OportunidadDTO> buscar(String searchTerm, Pageable pageable) {
        log.debug("Buscando oportunidades con término: {}", searchTerm);
        return oportunidadRepository.searchOportunidades(searchTerm, pageable)
                .map(oportunidadMapper::toDTO);
    }

    @Override
    public OportunidadDTO moverAEtapa(UUID id, UUID etapaId, BigDecimal probabilidad, UUID usuarioId) {
        log.info("Moviendo oportunidad {} a etapa {}", id, etapaId);

        Oportunidad oportunidad = oportunidadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidad no encontrada: " + id));

        oportunidad.moverAEtapa(etapaId, probabilidad);
        oportunidad.setUpdatedBy(usuarioId);

        oportunidad = oportunidadRepository.save(oportunidad);
        return oportunidadMapper.toDTO(oportunidad);
    }

    @Override
    public OportunidadDTO marcarComoGanada(UUID id, LocalDate fechaCierre, UUID usuarioId) {
        log.info("Marcando oportunidad {} como ganada", id);

        Oportunidad oportunidad = oportunidadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidad no encontrada: " + id));

        oportunidad.marcarComoGanada(fechaCierre);
        oportunidad.setUpdatedBy(usuarioId);

        oportunidad = oportunidadRepository.save(oportunidad);
        return oportunidadMapper.toDTO(oportunidad);
    }

    @Override
    public OportunidadDTO marcarComoPerdida(UUID id, String motivo, UUID usuarioId) {
        log.info("Marcando oportunidad {} como perdida", id);

        Oportunidad oportunidad = oportunidadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidad no encontrada: " + id));

        oportunidad.marcarComoPerdida(motivo);
        oportunidad.setUpdatedBy(usuarioId);

        oportunidad = oportunidadRepository.save(oportunidad);
        return oportunidadMapper.toDTO(oportunidad);
    }

    @Override
    public void eliminar(UUID id) {
        log.info("Eliminando oportunidad: {}", id);

        if (!oportunidadRepository.existsById(id)) {
            throw new RuntimeException("Oportunidad no encontrada: " + id);
        }

        oportunidadRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorEtapa(UUID etapaId) {
        return oportunidadRepository.countByEtapaId(etapaId);
    }
}
