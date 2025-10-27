package com.pagodirecto.application.api;

import com.pagodirecto.seguridad.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Controlador REST para autenticación
 *
 * NOTA: Este es un controlador MOCK para desarrollo.
 * En producción, debe implementarse con autenticación real.
 *
 * Credenciales de prueba:
 * - Email: admin@pagodirecto.com
 * - Password: admin123
 */
@RestController
@RequestMapping("/v1/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        // Soportar tanto "email" como "username" en el request
        String email = request.get("email") != null ? request.get("email") : request.get("username");
        String password = request.get("password");

        log.info("Intento de login para usuario: {}", email);

        // Soportar múltiples usuarios de prueba
        if (("admin@pagodirecto.com".equals(email) || "admin@admin.com".equals(email)) && "admin123".equals(password)) {
            UUID userId = UUID.randomUUID();
            UUID unidadNegocioId = UUID.randomUUID();

            // Generate real JWT tokens using JwtTokenProvider
            String accessToken = jwtTokenProvider.generateToken(
                userId,
                email,
                unidadNegocioId,
                Set.of("ADMIN"),
                Set.of("READ", "WRITE", "DELETE")
            );

            String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

            Map<String, Object> response = Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "tokenType", "Bearer",
                    "expiresIn", 86400L,
                    "user", Map.of(
                            "id", userId.toString(),
                            "email", email,
                            "nombre", "Administrador",
                            "apellido", "Sistema",
                            "rol", "ADMIN"
                    )
            );

            log.info("Login exitoso para usuario: {}", email);
            return ResponseEntity.ok(response);
        }

        log.warn("Login fallido para usuario: {}", email);
        return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        return ResponseEntity.ok(Map.of(
                "id", UUID.randomUUID().toString(),
                "email", "admin@pagodirecto.com",
                "nombre", "Administrador",
                "apellido", "Sistema",
                "rol", "ADMIN"
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is healthy");
    }
}
