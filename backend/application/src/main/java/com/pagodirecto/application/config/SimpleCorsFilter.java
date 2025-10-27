package com.pagodirecto.application.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filtro CORS simple que se ejecuta ANTES de Spring Security
 *
 * NOTA: Este es un filtro temporal para desarrollo.
 * En producción, usar la configuración CORS de Spring Security.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class SimpleCorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        String origin = request.getHeader("Origin");
        log.debug("SimpleCorsFilter: Processing request from origin: {}", origin);

        // Permitir cualquier localhost en desarrollo
        if (origin != null && (origin.startsWith("http://localhost:") || origin.startsWith("http://127.0.0.1:"))) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers");
            response.setHeader("Access-Control-Expose-Headers", "Content-Type, Authorization, X-Total-Count");
            response.setHeader("Access-Control-Max-Age", "3600");

            log.debug("SimpleCorsFilter: CORS headers set for origin: {}", origin);
        }

        // Para peticiones OPTIONS (preflight), responder inmediatamente
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.info("SimpleCorsFilter: Handling OPTIONS preflight request to: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        log.info("SimpleCorsFilter initialized");
    }

    @Override
    public void destroy() {
        log.info("SimpleCorsFilter destroyed");
    }
}
