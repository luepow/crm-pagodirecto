package com.pagodirecto.seguridad.application.service.impl;

import com.pagodirecto.seguridad.application.dto.ChangePasswordRequest;
import com.pagodirecto.seguridad.application.dto.ProfileDTO;
import com.pagodirecto.seguridad.application.dto.RoleDTO;
import com.pagodirecto.seguridad.application.dto.UpdateProfileRequest;
import com.pagodirecto.seguridad.application.exception.UserNotFoundException;
import com.pagodirecto.seguridad.application.service.ProfileService;
import com.pagodirecto.seguridad.domain.Usuario;
import com.pagodirecto.seguridad.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service Implementation: ProfileServiceImpl
 *
 * Implementación del servicio de gestión de perfiles de usuario
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public ProfileDTO getProfile(UUID userId) {
        log.debug("Obteniendo perfil del usuario: {}", userId);

        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        return mapToProfileDTO(usuario);
    }

    @Override
    public ProfileDTO updateProfile(UUID userId, UpdateProfileRequest request) {
        log.info("Actualizando perfil del usuario: {}", userId);

        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        // Actualizar campos
        usuario.setEmail(request.getEmail());
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setTelefono(request.getTelefono());
        usuario.setCargo(request.getCargo());
        usuario.setDepartamento(request.getDepartamento());
        usuario.setPhotoUrl(request.getPhotoUrl());
        usuario.setUpdatedAt(Instant.now());
        usuario.setUpdatedBy(userId);

        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        log.info("Perfil actualizado exitosamente para usuario: {}", userId);
        return mapToProfileDTO(usuarioActualizado);
    }

    @Override
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        log.info("Cambiando contraseña del usuario: {}", userId);

        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        // Verificar contraseña actual
        if (!passwordEncoder.matches(request.getCurrentPassword(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        // Validar que las nuevas contraseñas coincidan
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas nuevas no coinciden");
        }

        // Actualizar contraseña
        usuario.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        usuario.setUpdatedAt(Instant.now());
        usuario.setUpdatedBy(userId);

        usuarioRepository.save(usuario);

        log.info("Contraseña cambiada exitosamente para usuario: {}", userId);
    }

    @Override
    public String enableMFA(UUID userId) {
        log.info("Habilitando MFA para usuario: {}", userId);

        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        // Generar secret de MFA (base32 para compatibilidad con Google Authenticator)
        String mfaSecret = generateMFASecret();

        usuario.setMfaEnabled(true);
        usuario.setMfaSecret(mfaSecret);
        usuario.setUpdatedAt(Instant.now());
        usuario.setUpdatedBy(userId);

        usuarioRepository.save(usuario);

        log.info("MFA habilitado exitosamente para usuario: {}", userId);
        return mfaSecret;
    }

    @Override
    public void disableMFA(UUID userId) {
        log.info("Deshabilitando MFA para usuario: {}", userId);

        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        usuario.setMfaEnabled(false);
        usuario.setMfaSecret(null);
        usuario.setUpdatedAt(Instant.now());
        usuario.setUpdatedBy(userId);

        usuarioRepository.save(usuario);

        log.info("MFA deshabilitado exitosamente para usuario: {}", userId);
    }

    /**
     * Mapea una entidad Usuario a ProfileDTO
     */
    private ProfileDTO mapToProfileDTO(Usuario usuario) {
        Set<RoleDTO> rolesDTO = usuario.getRoles().stream()
            .map(rol -> RoleDTO.builder()
                .id(rol.getId())
                .nombre(rol.getNombre())
                .descripcion(rol.getDescripcion())
                .build())
            .collect(Collectors.toSet());

        return ProfileDTO.builder()
            .id(usuario.getId())
            .username(usuario.getUsername())
            .email(usuario.getEmail())
            .nombreCompleto(usuario.getNombreCompleto())
            .telefono(usuario.getTelefono())
            .cargo(usuario.getCargo())
            .departamento(usuario.getDepartamento())
            .photoUrl(usuario.getPhotoUrl())
            .status(usuario.getStatus())
            .mfaEnabled(usuario.getMfaEnabled())
            .ultimoAcceso(usuario.getUltimoAcceso())
            .roles(rolesDTO)
            .createdAt(usuario.getCreatedAt())
            .build();
    }

    /**
     * Genera un secret aleatorio de 20 bytes para MFA
     */
    private String generateMFASecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
