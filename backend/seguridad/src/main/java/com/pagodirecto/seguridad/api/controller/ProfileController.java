package com.pagodirecto.seguridad.api.controller;

import com.pagodirecto.seguridad.application.dto.ChangePasswordRequest;
import com.pagodirecto.seguridad.application.dto.ProfileDTO;
import com.pagodirecto.seguridad.application.dto.UpdateProfileRequest;
import com.pagodirecto.seguridad.application.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller: ProfileController
 *
 * API para gestión del perfil de usuario
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@RestController
@RequestMapping("/v1/profile")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Profile", description = "API de gestión del perfil de usuario")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @Operation(summary = "Obtener perfil del usuario actual")
    public ResponseEntity<ProfileDTO> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = getUserIdFromPrincipal(userDetails);
        log.info("Usuario {} solicitando su perfil", userId);

        ProfileDTO profile = profileService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    @Operation(summary = "Actualizar perfil del usuario actual")
    public ResponseEntity<ProfileDTO> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromPrincipal(userDetails);
        log.info("Usuario {} actualizando su perfil", userId);

        ProfileDTO updatedProfile = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/change-password")
    @Operation(summary = "Cambiar contraseña del usuario actual")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromPrincipal(userDetails);
        log.info("Usuario {} cambiando contraseña", userId);

        profileService.changePassword(userId, request);

        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
    }

    @PostMapping("/mfa/enable")
    @Operation(summary = "Habilitar autenticación multi-factor (MFA)")
    public ResponseEntity<Map<String, String>> enableMFA(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = getUserIdFromPrincipal(userDetails);
        log.info("Usuario {} habilitando MFA", userId);

        String mfaSecret = profileService.enableMFA(userId);

        return ResponseEntity.ok(Map.of(
            "message", "MFA habilitado exitosamente",
            "secret", mfaSecret
        ));
    }

    @PostMapping("/mfa/disable")
    @Operation(summary = "Deshabilitar autenticación multi-factor (MFA)")
    public ResponseEntity<Map<String, String>> disableMFA(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = getUserIdFromPrincipal(userDetails);
        log.info("Usuario {} deshabilitando MFA", userId);

        profileService.disableMFA(userId);

        return ResponseEntity.ok(Map.of("message", "MFA deshabilitado exitosamente"));
    }

    /**
     * Extrae el UUID del usuario desde el UserDetails
     *
     * Nota: Este método asume que el username es el UUID del usuario.
     * Si tu sistema usa username diferente, deberás ajustar esta lógica.
     */
    private UUID getUserIdFromPrincipal(UserDetails userDetails) {
        try {
            // Intenta parsear el username como UUID
            return UUID.fromString(userDetails.getUsername());
        } catch (IllegalArgumentException e) {
            // Si el username no es un UUID, lanza una excepción más descriptiva
            log.error("No se pudo extraer UUID del username: {}", userDetails.getUsername());
            throw new IllegalArgumentException("Username no es un UUID válido");
        }
    }
}
