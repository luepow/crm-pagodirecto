package com.pagodirecto.application.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad permisiva para desarrollo
 *
 * NOTA: Esta configuración SOLO se aplica en el perfil "development"
 * y permite acceso sin autenticación a los endpoints de auth.
 */
@Configuration
@Profile("development")
@Slf4j
public class DevSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        log.warn("=".repeat(80));
        log.warn("DEVELOPMENT MODE: Configuración de seguridad permisiva activada");
        log.warn("TODOS los endpoints son públicos sin autenticación");
        log.warn("Esta configuración SOLO debe usarse en desarrollo");
        log.warn("=".repeat(80));

        http
            .securityMatcher("/**")
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
