package com.pagodirecto.seguridad.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración: SecurityConfig
 *
 * Configuración de seguridad de Spring Security con JWT
 * - Stateless session (JWT-based)
 * - BCrypt password encoder con cost 12
 * - Endpoints públicos y protegidos
 * - CORS habilitado
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:23000,https://sig.pagodirecto.com,http://sig.pagodirecto.com,http://128.199.13.76,https://128.199.13.76}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private Boolean allowCredentials;

    /**
     * Configura el password encoder con BCrypt (cost 12)
     *
     * @return el password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Configura el authentication manager
     *
     * @param authConfig la configuración de autenticación
     * @return el authentication manager
     * @throws Exception si hay un error en la configuración
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configura la cadena de filtros de seguridad
     *
     * @param http el HttpSecurity a configurar
     * @return la cadena de filtros configurada
     * @throws Exception si hay un error en la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilita CSRF (usamos JWT, no cookies de sesión)
            .csrf(AbstractHttpConfigurer::disable)

            // Configura CORS ANTES de autorización
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Configura autorización de requests
            // NOTA: Los paths no incluyen /api porque server.servlet.context-path=/api en application.yml
            .authorizeHttpRequests(auth -> auth
                // PRIMERO: Permitir todas las peticiones OPTIONS (CORS preflight)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Endpoints públicos de autenticación
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/logout").permitAll()
                .requestMatchers(HttpMethod.GET, "/auth/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/auth/me").permitAll()
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/v1/auth/**").permitAll()

                // Endpoints públicos de monitoreo y documentación
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/docs/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()

                // Todos los demás endpoints requieren autenticación
                .anyRequest().authenticated()
            )

            // Configura gestión de sesiones (stateless para JWT)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Agrega el filtro JWT antes del filtro de autenticación estándar
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configura CORS para permitir requests desde el frontend
     * Lee la configuración desde variables de entorno para flexibilidad
     *
     * @return la configuración de CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Parse allowed origins from environment variable
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        log.info("Configurando CORS con orígenes permitidos: {}", origins);
        configuration.setAllowedOrigins(origins);

        // También permitir patterns para desarrollo y producción
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "https://sig.pagodirecto.com",
            "https://*.pagodirecto.com"
        ));

        // Parse allowed methods from environment variable
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        log.info("Métodos permitidos: {}", methods);
        configuration.setAllowedMethods(methods);

        // Parse allowed headers - IMPORTANTE: debe permitir todos los headers
        configuration.setAllowedHeaders(List.of("*"));
        log.info("Headers permitidos: *");

        // Headers expuestos al cliente
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));

        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(3600L);

        log.info("CORS configurado - Credentials: {}, MaxAge: 3600", allowCredentials);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
