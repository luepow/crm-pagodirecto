package com.pagodirecto.seguridad.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Filtro: JwtAuthenticationFilter
 *
 * Intercepta requests HTTP y valida tokens JWT en el header Authorization.
 * Si el token es válido, establece la autenticación en el SecurityContext de Spring.
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final RLSContextManager rlsContextManager;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);
            log.warn("DEBUG - JWT extracted: {}", jwt != null ? jwt.substring(0, Math.min(30, jwt.length())) : "null");

            if (StringUtils.hasText(jwt)) {
                log.warn("DEBUG - JWT has text, checking if mock...");
                // Handle mock tokens for development
                if (jwt.startsWith("mock-access-token-")) {
                    log.warn("DEBUG - IS MOCK TOKEN! Authenticating...");
                    authenticateMockUser(jwt, request);
                } else if (tokenProvider.validateToken(jwt)) {
                    log.warn("DEBUG - Real token validated");
                    authenticateUser(jwt, request);
                } else {
                    log.warn("DEBUG - Token validation failed");
                }
            } else {
                log.warn("DEBUG - No JWT found in request");
            }
        } catch (Exception ex) {
            log.error("No se pudo establecer la autenticación del usuario en el security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization
     *
     * @param request el request HTTP
     * @return el token JWT o null si no existe
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Autentica al usuario basándose en el token JWT
     *
     * @param jwt     el token JWT
     * @param request el request HTTP
     */
    private void authenticateUser(String jwt, HttpServletRequest request) {
        UUID userId = tokenProvider.getUserIdFromToken(jwt);
        String username = tokenProvider.getUsernameFromToken(jwt);
        UUID unidadNegocioId = tokenProvider.getUnidadNegocioIdFromToken(jwt);
        Set<String> roles = tokenProvider.getRolesFromToken(jwt);
        Set<String> permissions = tokenProvider.getPermissionsFromToken(jwt);

        // Convierte roles y permisos a GrantedAuthorities de Spring Security
        Set<SimpleGrantedAuthority> authorities = roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toSet());

        authorities.addAll(permissions.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet()));

        // Crea el objeto de autenticación de Spring Security
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(username, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Establece la autenticación en el SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Configura el contexto RLS para PostgreSQL
        rlsContextManager.setSessionContext(unidadNegocioId, userId, roles);

        log.debug("Usuario autenticado: {} (ID: {}), Unidad Negocio: {}, Roles: {}",
            username, userId, unidadNegocioId, roles);
    }

    /**
     * Autentica al usuario usando un token mock para desarrollo
     *
     * @param mockToken el token mock
     * @param request   el request HTTP
     */
    private void authenticateMockUser(String mockToken, HttpServletRequest request) {
        // Extraer el UUID del mock token (formato: mock-access-token-UUID)
        String userId = mockToken.replace("mock-access-token-", "");

        // Crear un usuario mock con permisos de ADMIN
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        authorities.add(new SimpleGrantedAuthority("READ"));
        authorities.add(new SimpleGrantedAuthority("WRITE"));
        authorities.add(new SimpleGrantedAuthority("DELETE"));

        // Crea el objeto de autenticación de Spring Security
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken("admin@pagodirecto.com", null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Establece la autenticación en el SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Configura el contexto RLS con valores mock
        try {
            UUID mockUserId = UUID.fromString(userId);
            UUID mockUnidadNegocioId = UUID.randomUUID();
            rlsContextManager.setSessionContext(mockUnidadNegocioId, mockUserId, Set.of("ADMIN"));
        } catch (IllegalArgumentException e) {
            // Si el UUID no es válido, usar valores por defecto
            rlsContextManager.setSessionContext(UUID.randomUUID(), UUID.randomUUID(), Set.of("ADMIN"));
        }

        log.warn("MOCK authentication used for development - Token: {}", mockToken.substring(0, Math.min(30, mockToken.length())));
    }
}
