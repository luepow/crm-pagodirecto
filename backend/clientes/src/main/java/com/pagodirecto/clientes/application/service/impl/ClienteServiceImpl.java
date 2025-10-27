package com.pagodirecto.clientes.application.service.impl;

import com.pagodirecto.clientes.application.dto.ClienteDTO;
import com.pagodirecto.clientes.application.mapper.ClienteMapper;
import com.pagodirecto.clientes.application.service.ClienteService;
import com.pagodirecto.clientes.domain.Cliente;
import com.pagodirecto.clientes.infrastructure.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Implementación del servicio: Cliente
 *
 * Implementa la lógica de negocio para operaciones con clientes.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Override
    @Transactional
    public ClienteDTO crear(ClienteDTO clienteDTO, Long usuarioId) {
        log.info("Creando nuevo cliente: {}", clienteDTO.getNombre());

        // Validar duplicados
        if (clienteDTO.getEmail() != null && clienteRepository.existsByEmail(clienteDTO.getEmail())) {
            throw new IllegalArgumentException("Ya existe un cliente con el email: " + clienteDTO.getEmail());
        }

        Cliente cliente = clienteMapper.toEntity(clienteDTO);
        cliente.setCreatedBy(usuarioId);
        cliente.setUpdatedBy(usuarioId);
        cliente.setCreatedAt(Instant.now());
        cliente.setUpdatedAt(Instant.now());

        Cliente clienteGuardado = clienteRepository.save(cliente);
        log.info("Cliente creado exitosamente con ID: {}", clienteGuardado.getId());

        return clienteMapper.toDTO(clienteGuardado);
    }

    @Override
    @Transactional
    public ClienteDTO actualizar(Long id, ClienteDTO clienteDTO, Long usuarioId) {
        log.info("Actualizando cliente con ID: {}", id);

        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));

        // Validar cambios en email
        if (clienteDTO.getEmail() != null &&
            !clienteDTO.getEmail().equals(cliente.getEmail()) &&
            clienteRepository.existsByEmail(clienteDTO.getEmail())) {
            throw new IllegalArgumentException("Ya existe un cliente con el email: " + clienteDTO.getEmail());
        }

        clienteMapper.updateEntityFromDTO(clienteDTO, cliente);
        cliente.setUpdatedBy(usuarioId);
        cliente.setUpdatedAt(Instant.now());

        Cliente clienteActualizado = clienteRepository.save(cliente);
        log.info("Cliente actualizado exitosamente con ID: {}", clienteActualizado.getId());

        return clienteMapper.toDTO(clienteActualizado);
    }

    @Override
    public ClienteDTO buscarPorId(Long id) {
        log.debug("Buscando cliente con ID: {}", id);
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));
        return clienteMapper.toDTO(cliente);
    }

    @Override
    public Page<ClienteDTO> listarTodos(Pageable pageable) {
        log.debug("Listando todos los clientes - página: {}", pageable.getPageNumber());
        return clienteRepository.findAll(pageable)
            .map(clienteMapper::toDTO);
    }

    @Override
    public Page<ClienteDTO> buscarPorActivo(Boolean activo, Pageable pageable) {
        log.debug("Buscando clientes activos: {} - página: {}", activo, pageable.getPageNumber());
        return clienteRepository.findByActivo(activo, pageable)
            .map(clienteMapper::toDTO);
    }

    @Override
    public Page<ClienteDTO> buscar(String searchTerm, Pageable pageable) {
        log.debug("Buscando clientes con término: {} - página: {}", searchTerm, pageable.getPageNumber());
        return clienteRepository.searchClientes(searchTerm, pageable)
            .map(clienteMapper::toDTO);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando cliente con ID: {}", id);
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));
        clienteRepository.delete(cliente);
        log.info("Cliente eliminado exitosamente con ID: {}", id);
    }

    @Override
    @Transactional
    public ClienteDTO activar(Long id, Long usuarioId) {
        log.info("Activando cliente con ID: {}", id);
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));

        cliente.activar();
        cliente.setUpdatedBy(usuarioId);

        Cliente clienteActualizado = clienteRepository.save(cliente);
        log.info("Cliente activado exitosamente con ID: {}", id);

        return clienteMapper.toDTO(clienteActualizado);
    }

    @Override
    @Transactional
    public ClienteDTO desactivar(Long id, Long usuarioId) {
        log.info("Desactivando cliente con ID: {}", id);
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));

        cliente.desactivar();
        cliente.setUpdatedBy(usuarioId);

        Cliente clienteActualizado = clienteRepository.save(cliente);
        log.info("Cliente desactivado exitosamente con ID: {}", id);

        return clienteMapper.toDTO(clienteActualizado);
    }
}
