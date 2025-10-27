package com.empresa.crm.clientes.service;

import com.empresa.crm.clientes.dto.ActualizarClienteRequest;
import com.empresa.crm.clientes.dto.ClienteDTO;
import com.empresa.crm.clientes.dto.CrearClienteRequest;
import com.empresa.crm.clientes.model.Cliente;
import com.empresa.crm.clientes.repository.ClienteRepository;
import com.empresa.crm.shared.exception.BusinessException;
import com.empresa.crm.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteDTO> listar(String search, Pageable pageable) {
        log.debug("Listando clientes con búsqueda: {} y paginación: {}", search, pageable);

        Page<Cliente> clientes;
        if (search != null && !search.trim().isEmpty()) {
            clientes = clienteRepository.searchClientes(search.trim(), pageable);
        } else {
            clientes = clienteRepository.findByDeletedAtIsNull(pageable);
        }

        return clientes.map(ClienteDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClienteDTO> obtener(Long id) {
        log.debug("Obteniendo cliente con id: {}", id);
        return clienteRepository.findByIdAndDeletedAtIsNull(id)
                .map(ClienteDTO::fromEntity);
    }

    @Override
    @Transactional
    public ClienteDTO crear(CrearClienteRequest request) {
        log.info("Creando nuevo cliente: {}", request.getEmail());

        // Validate email uniqueness
        if (clienteRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new BusinessException("Ya existe un cliente con el email: " + request.getEmail());
        }

        Long currentUserId = getCurrentUserId();

        Cliente cliente = Cliente.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .direccion(request.getDireccion())
                .ciudad(request.getCiudad())
                .pais(request.getPais())
                .codigoPostal(request.getCodigoPostal())
                .rfc(request.getRfc())
                .activo(true)
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();

        Cliente savedCliente = clienteRepository.save(cliente);
        log.info("Cliente creado exitosamente con id: {}", savedCliente.getId());

        return ClienteDTO.fromEntity(savedCliente);
    }

    @Override
    @Transactional
    public Optional<ClienteDTO> actualizar(Long id, ActualizarClienteRequest request) {
        log.info("Actualizando cliente con id: {}", id);

        return clienteRepository.findByIdAndDeletedAtIsNull(id)
                .map(cliente -> {
                    // Validate email uniqueness for updates
                    if (!cliente.getEmail().equals(request.getEmail()) &&
                        clienteRepository.existsByEmailAndIdNotAndDeletedAtIsNull(request.getEmail(), id)) {
                        throw new BusinessException("Ya existe otro cliente con el email: " + request.getEmail());
                    }

                    Long currentUserId = getCurrentUserId();

                    // Update fields
                    cliente.setNombre(request.getNombre());
                    cliente.setEmail(request.getEmail());
                    cliente.setTelefono(request.getTelefono());
                    cliente.setDireccion(request.getDireccion());
                    cliente.setCiudad(request.getCiudad());
                    cliente.setPais(request.getPais());
                    cliente.setCodigoPostal(request.getCodigoPostal());
                    cliente.setRfc(request.getRfc());
                    cliente.setUpdatedBy(currentUserId);

                    Cliente updatedCliente = clienteRepository.save(cliente);
                    log.info("Cliente actualizado exitosamente: {}", updatedCliente.getId());

                    return ClienteDTO.fromEntity(updatedCliente);
                });
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando cliente con id: {}", id);

        Cliente cliente = clienteRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        cliente.softDelete();
        clienteRepository.save(cliente);

        log.info("Cliente eliminado exitosamente: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        return clienteRepository.existsByEmailAndDeletedAtIsNull(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEmailParaOtro(String email, Long id) {
        return clienteRepository.existsByEmailAndIdNotAndDeletedAtIsNull(email, id);
    }

    /**
     * Get current authenticated user ID
     * For now, returns a default value. Should be updated to get actual user from SecurityContext
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // TODO: Implement proper user ID extraction from JWT or UserDetails
            // For now, return a default value
            return 1L;
        }
        return 1L;
    }
}
