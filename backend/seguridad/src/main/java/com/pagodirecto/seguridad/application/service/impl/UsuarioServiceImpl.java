package com.pagodirecto.seguridad.application.service.impl;

import com.pagodirecto.seguridad.application.dto.*;
import com.pagodirecto.seguridad.application.service.UsuarioService;
import com.pagodirecto.seguridad.domain.Rol;
import com.pagodirecto.seguridad.domain.Usuario;
import com.pagodirecto.seguridad.domain.UsuarioStatus;
import com.pagodirecto.seguridad.infrastructure.repository.RolRepository;
import com.pagodirecto.seguridad.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación: UsuarioServiceImpl
 *
 * Implementación del servicio de gestión CRUD de usuarios
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> getAllUsuarios() {
        log.info("Obteniendo todos los usuarios");
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream()
            .map(this::toUsuarioDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> getUsuariosByUnidadNegocio(UUID unidadNegocioId) {
        log.info("Obteniendo usuarios por unidad de negocio: {}", unidadNegocioId);
        List<Usuario> usuarios = usuarioRepository.findByUnidadNegocioId(unidadNegocioId);
        return usuarios.stream()
            .map(this::toUsuarioDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO getUsuarioById(UUID id) {
        log.info("Obteniendo usuario por ID: {}", id);
        Usuario usuario = usuarioRepository.findByIdWithRoles(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return toUsuarioDTO(usuario);
    }

    @Override
    public UsuarioDTO createUsuario(CreateUsuarioRequest request, UUID creatorId) {
        log.info("Creando nuevo usuario: {}", request.getUsername());

        // Verificar que no exista el username
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El username ya existe: " + request.getUsername());
        }

        // Verificar que no exista el email
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya existe: " + request.getEmail());
        }

        // Crear el usuario
        Usuario usuario = Usuario.builder()
            .unidadNegocioId(request.getUnidadNegocioId())
            .username(request.getUsername())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .nombreCompleto(request.getNombreCompleto())
            .telefono(request.getTelefono())
            .cargo(request.getCargo())
            .departamento(request.getDepartamento())
            .photoUrl(request.getPhotoUrl())
            .status(request.getStatus() != null ? request.getStatus() : UsuarioStatus.ACTIVE)
            .mfaEnabled(false)
            .intentosFallidos(0)
            .createdAt(Instant.now())
            .createdBy(creatorId)
            .updatedAt(Instant.now())
            .updatedBy(creatorId)
            .build();

        // Asignar roles si se proporcionaron
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Rol> roles = new HashSet<>(rolRepository.findAllById(request.getRoleIds()));
            usuario.setRoles(roles);
        }

        Usuario savedUsuario = usuarioRepository.save(usuario);
        log.info("Usuario creado exitosamente: {}", savedUsuario.getId());

        return toUsuarioDTO(savedUsuario);
    }

    @Override
    public UsuarioDTO updateUsuario(UUID id, UpdateUsuarioRequest request, UUID updaterId) {
        log.info("Actualizando usuario: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // Verificar email único (si cambió)
        if (!usuario.getEmail().equals(request.getEmail()) &&
            usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya existe: " + request.getEmail());
        }

        // Actualizar datos
        usuario.setEmail(request.getEmail());
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setTelefono(request.getTelefono());
        usuario.setCargo(request.getCargo());
        usuario.setDepartamento(request.getDepartamento());
        usuario.setPhotoUrl(request.getPhotoUrl());
        
        if (request.getStatus() != null) {
            usuario.setStatus(request.getStatus());
        }

        usuario.setUpdatedAt(Instant.now());
        usuario.setUpdatedBy(updaterId);

        // Actualizar roles si se proporcionaron
        if (request.getRoleIds() != null) {
            Set<Rol> roles = new HashSet<>(rolRepository.findAllById(request.getRoleIds()));
            usuario.setRoles(roles);
        }

        Usuario updatedUsuario = usuarioRepository.save(usuario);
        log.info("Usuario actualizado exitosamente: {}", id);

        return toUsuarioDTO(updatedUsuario);
    }

    @Override
    public void deleteUsuario(UUID id) {
        log.info("Eliminando usuario (soft delete): {}", id);

        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // Soft delete (Hibernate interceptará con @SQLDelete)
        usuarioRepository.delete(usuario);

        log.info("Usuario eliminado exitosamente: {}", id);
    }

    @Override
    public void bloquearUsuario(UUID id, Integer duracionSegundos) {
        log.info("Bloqueando usuario: {} por {} segundos", id, duracionSegundos);

        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.bloquear(duracionSegundos);
        usuarioRepository.save(usuario);

        log.info("Usuario bloqueado exitosamente: {}", id);
    }

    @Override
    public void desbloquearUsuario(UUID id) {
        log.info("Desbloqueando usuario: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.desbloquear();
        usuarioRepository.save(usuario);

        log.info("Usuario desbloqueado exitosamente: {}", id);
    }

    @Override
    public void resetPassword(UUID id, String newPassword) {
        log.info("Restableciendo contraseña para usuario: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setPasswordHash(passwordEncoder.encode(newPassword));
        usuario.setUpdatedAt(Instant.now());
        usuarioRepository.save(usuario);

        log.info("Contraseña restablecida exitosamente para usuario: {}", id);
    }

    /**
     * Convierte una entidad Usuario a UsuarioDTO
     */
    private UsuarioDTO toUsuarioDTO(Usuario usuario) {
        Set<RoleDTO> roleDTOs = usuario.getRoles().stream()
            .map(rol -> RoleDTO.builder()
                .id(rol.getId())
                .nombre(rol.getNombre())
                .descripcion(rol.getDescripcion())
                .departamento(rol.getDepartamento())
                .nivelJerarquico(rol.getNivelJerarquico())
                .createdAt(rol.getCreatedAt())
                .build())
            .collect(Collectors.toSet());

        return UsuarioDTO.builder()
            .id(usuario.getId())
            .unidadNegocioId(usuario.getUnidadNegocioId())
            .username(usuario.getUsername())
            .email(usuario.getEmail())
            .nombreCompleto(usuario.getNombreCompleto())
            .telefono(usuario.getTelefono())
            .cargo(usuario.getCargo())
            .departamento(usuario.getDepartamento())
            .photoUrl(usuario.getPhotoUrl())
            .mfaEnabled(usuario.getMfaEnabled())
            .status(usuario.getStatus())
            .ultimoAcceso(usuario.getUltimoAcceso())
            .intentosFallidos(usuario.getIntentosFallidos())
            .bloqueadoHasta(usuario.getBloqueadoHasta())
            .roles(roleDTOs)
            .createdAt(usuario.getCreatedAt())
            .updatedAt(usuario.getUpdatedAt())
            .build();
    }
}
