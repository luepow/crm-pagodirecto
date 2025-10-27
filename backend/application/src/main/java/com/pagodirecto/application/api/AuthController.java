package com.pagodirecto.application.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        log.info("Intento de login para usuario: {}", request.get("email"));

        String email = request.get("email");
        String password = request.get("password");

        if ("admin@pagodirecto.com".equals(email) && "admin123".equals(password)) {
            String userId = UUID.randomUUID().toString();

            // Mock tokens for development
            String accessToken = "mock-access-token-" + userId;
            String refreshToken = "mock-refresh-token-" + userId;

            Map<String, Object> response = Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "tokenType", "Bearer",
                    "expiresIn", 86400L,
                    "user", Map.of(
                            "id", userId,
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
