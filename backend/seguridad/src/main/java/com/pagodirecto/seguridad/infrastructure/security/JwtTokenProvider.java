package com.pagodirecto.seguridad.infrastructure.security;

import com.pagodirecto.seguridad.application.exception.InvalidTokenException;
import com.pagodirecto.seguridad.domain.Permiso;
import com.pagodirecto.seguridad.domain.Rol;
import com.pagodirecto.seguridad.domain.Usuario;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Componente: JwtTokenProvider
 *
 * Generador y validador de tokens JWT para autenticación
 * - Access tokens: 5 minutos de vigencia
 * - Refresh tokens: 30 días de vigencia
 * - Incluye claims personalizados: roles, permisos, tenant
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret:PagoDirecto2025SecretKeyMustBeLongEnoughForHS256Algorithm}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration-ms:300000}") // 5 minutos
    private Long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms:2592000000}") // 30 días
    private Long refreshTokenExpirationMs;

    private SecretKey secretKey;

    /**
     * Inicializa la clave secreta para firmar los tokens
     */
    @PostConstruct
    protected void init() {
        // Genera una clave segura basada en el secret configurado
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT Token Provider inicializado. Access token expiration: {}ms, Refresh token expiration: {}ms",
            accessTokenExpirationMs, refreshTokenExpirationMs);
    }

    /**
     * Genera un access token JWT para un usuario
     *
     * @param usuario el usuario autenticado
     * @return el access token JWT
     */
    public String generateAccessToken(Usuario usuario) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(accessTokenExpirationMs);

        // Extrae roles y permisos del usuario
        Set<String> roles = usuario.getRoles().stream()
            .map(Rol::getNombre)
            .collect(Collectors.toSet());

        Set<String> permissions = usuario.getRoles().stream()
            .flatMap(rol -> rol.getPermisos().stream())
            .map(Permiso::getScope)
            .collect(Collectors.toSet());

        return Jwts.builder()
            .subject(usuario.getId().toString())
            .claim("username", usuario.getUsername())
            .claim("email", usuario.getEmail())
            .claim("unidadNegocioId", usuario.getUnidadNegocioId().toString())
            .claim("roles", roles)
            .claim("permissions", permissions)
            .claim("type", "access")
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiryDate))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
    }

    /**
     * Genera un refresh token (simple UUID, no JWT)
     *
     * @return el refresh token UUID
     */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Obtiene el tiempo de expiración del refresh token en milisegundos
     *
     * @return milisegundos de vigencia
     */
    public Long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    /**
     * Obtiene el tiempo de expiración del access token en segundos
     *
     * @return segundos de vigencia
     */
    public Long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMs / 1000;
    }

    /**
     * Extrae el ID de usuario de un token JWT
     *
     * @param token el token JWT
     * @return el ID del usuario
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return UUID.fromString(claims.getSubject());
    }

    /**
     * Extrae el username de un token JWT
     *
     * @param token el token JWT
     * @return el username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * Extrae el ID de unidad de negocio de un token JWT
     *
     * @param token el token JWT
     * @return el ID de unidad de negocio
     */
    public UUID getUnidadNegocioIdFromToken(String token) {
        Claims claims = parseToken(token);
        String unidadNegocioId = claims.get("unidadNegocioId", String.class);
        return UUID.fromString(unidadNegocioId);
    }

    /**
     * Extrae los roles de un token JWT
     *
     * @param token el token JWT
     * @return set de roles
     */
    @SuppressWarnings("unchecked")
    public Set<String> getRolesFromToken(String token) {
        Claims claims = parseToken(token);
        return (Set<String>) claims.get("roles");
    }

    /**
     * Extrae los permisos de un token JWT
     *
     * @param token el token JWT
     * @return set de permisos
     */
    @SuppressWarnings("unchecked")
    public Set<String> getPermissionsFromToken(String token) {
        Claims claims = parseToken(token);
        return (Set<String>) claims.get("permissions");
    }

    /**
     * Valida un token JWT
     *
     * @param token el token JWT
     * @return true si es válido, false en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (SignatureException e) {
            log.error("Token JWT con firma inválida: {}", e.getMessage());
            throw new InvalidTokenException("Firma del token inválida", e);
        } catch (MalformedJwtException e) {
            log.error("Token JWT malformado: {}", e.getMessage());
            throw new InvalidTokenException("Token malformado", e);
        } catch (ExpiredJwtException e) {
            log.error("Token JWT expirado: {}", e.getMessage());
            throw new InvalidTokenException("Token expirado", e);
        } catch (UnsupportedJwtException e) {
            log.error("Token JWT no soportado: {}", e.getMessage());
            throw new InvalidTokenException("Token no soportado", e);
        } catch (IllegalArgumentException e) {
            log.error("Claims del token JWT vacíos: {}", e.getMessage());
            throw new InvalidTokenException("Claims del token vacíos", e);
        }
    }

    /**
     * Parsea un token JWT y extrae los claims
     *
     * @param token el token JWT
     * @return los claims del token
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * Obtiene la fecha de expiración de un token
     *
     * @param token el token JWT
     * @return la fecha de expiración
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    /**
     * Verifica si un token ha expirado
     *
     * @param token el token JWT
     * @return true si ha expirado, false en caso contrario
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}
