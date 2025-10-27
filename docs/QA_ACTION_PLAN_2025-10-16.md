# PLAN DE ACCION - CORRECCION DE BUGS QA

**Sistema**: PagoDirecto CRM/ERP v1.0.0
**Fecha**: 2025-10-16
**Estado actual**: BLOQUEADO PARA PRODUCCION
**Objetivo**: Resolver bugs criticos y preparar para re-certificacion

---

## PRIORIZACION DE BUGS

### CRITICOS (P0 - Resolver INMEDIATAMENTE)
- BUG-002 / BUG-005: Operaciones de escritura fallan por UserDetails nulo
- BUG-006: Falta seed data para etapas de pipeline
- BUG-008: Sistema de autenticacion JWT no funcional

### ALTA SEVERIDAD (P1 - Resolver antes de UAT)
- BUG-001: Endpoint de busqueda roto
- BUG-003 / BUG-004: Errores 404 retornan HTTP 500

### MEDIA SEVERIDAD (P2 - Resolver antes de produccion)
- BUG-007: Faltan headers CSP y HSTS

### BAJA SEVERIDAD (P3 - Nice to have)
- BUG-009: Endpoint search con query vacia

---

## FASE 1: BUGS CRITICOS (SPRINT 1 - 1 SEMANA)

### TAREA 1.1: Implementar autenticacion JWT completa
**Responsable**: Backend Team Lead
**Tiempo estimado**: 2-3 dias
**Prioridad**: P0 - CRITICA
**Bugs que resuelve**: BUG-002, BUG-005, BUG-008

#### Subtareas:

**1.1.1. Crear AuthController con endpoints de autenticacion**
```java
// backend/application/src/main/java/com/pagodirecto/application/api/AuthController.java

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Autenticar usuario
        Authentication authentication = authenticationService.authenticate(
            request.getUsername(),
            request.getPassword()
        );

        // Generar token JWT
        String token = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // Extraer userId del usuario autenticado
        UUID userId = extractUserId(authentication);

        return ResponseEntity.ok(AuthResponse.builder()
            .token(token)
            .refreshToken(refreshToken)
            .userId(userId)
            .username(request.getUsername())
            .expiresIn(86400000L)
            .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        // Validar refresh token
        // Generar nuevo access token
        // Retornar nuevo token
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetails userDetails) {
        // Invalidar token (agregar a blacklist en Redis)
        return ResponseEntity.noContent().build();
    }
}
```

**1.1.2. Crear JwtAuthenticationFilter**
```java
// backend/seguridad/src/main/java/com/pagodirecto/seguridad/infrastructure/security/JwtAuthenticationFilter.java

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = extractTokenFromRequest(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromToken(token);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

**1.1.3. Actualizar SecurityConfig**
```java
// backend/seguridad/src/main/java/com/pagodirecto/seguridad/infrastructure/security/SecurityConfig.java

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/docs/**", "/api/swagger-ui/**").permitAll()
                .requestMatchers("/api/actuator/health").permitAll()
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

**1.1.4. Actualizar controladores para manejar UserDetails opcional (fallback para desarrollo)**
```java
// backend/clientes/src/main/java/com/pagodirecto/clientes/api/controller/ClienteController.java

@PostMapping
public ResponseEntity<ClienteDTO> crear(
        @Valid @RequestBody ClienteDTO clienteDTO,
        @AuthenticationPrincipal UserDetails userDetails) {

    // SOLUCION TEMPORAL: Manejar userDetails nulo en desarrollo
    UUID usuarioId;
    if (userDetails != null) {
        // Produccion: extraer userId real del token
        usuarioId = extractUserIdFromUserDetails(userDetails);
    } else {
        // Desarrollo: usar usuario por defecto
        log.warn("UserDetails is null - using default system user (DEVELOPMENT ONLY)");
        usuarioId = UUID.fromString("30000000-0000-0000-0000-000000000001");
    }

    log.info("Creando nuevo cliente - usuario: {}", usuarioId);
    ClienteDTO created = clienteService.crear(clienteDTO, usuarioId);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}

private UUID extractUserIdFromUserDetails(UserDetails userDetails) {
    // Implementar extraccion real del userId
    // Opcion 1: Cast a custom UserDetails que tenga getId()
    // Opcion 2: Buscar usuario por username
    // Opcion 3: Almacenar userId en JWT claims
}
```

**1.1.5. Crear DTOs de autenticacion**
```java
// LoginRequest.java
@Data
public class LoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}

// AuthResponse.java
@Data
@Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
    private UUID userId;
    private String username;
    private Long expiresIn;
}
```

**Testing de la tarea 1.1**:
```bash
# Test 1: Login exitoso
curl -X POST http://localhost:28080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Esperado: HTTP 200 con token JWT

# Test 2: Crear cliente con token
curl -X POST http://localhost:28080/api/v1/clientes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{
    "unidadNegocioId": "00000000-0000-0000-0000-000000000001",
    "codigo": "CLI-TEST-001",
    "nombre": "Cliente Test",
    "email": "test@example.com",
    "tipo": "EMPRESA",
    "status": "LEAD"
  }'

# Esperado: HTTP 201 con cliente creado
```

**Definition of Done**:
- [ ] Endpoint POST /v1/auth/login retorna token JWT valido
- [ ] Endpoint POST /v1/auth/refresh funciona
- [ ] JwtAuthenticationFilter valida tokens correctamente
- [ ] SecurityConfig protege endpoints correctamente
- [ ] Operaciones POST/PUT/DELETE funcionan con token valido
- [ ] Tests unitarios para JwtTokenProvider
- [ ] Tests de integracion para AuthController

---

### TAREA 1.2: Agregar seed data para etapas de pipeline
**Responsable**: Backend Developer
**Tiempo estimado**: 2 horas
**Prioridad**: P0 - CRITICA
**Bugs que resuelve**: BUG-006

#### Subtareas:

**1.2.1. Crear migration Flyway**
```sql
-- backend/oportunidades/src/main/resources/db/migration/V1.3__seed_etapas_pipeline.sql

-- Insertar etapas basicas de pipeline de ventas
INSERT INTO oportunidades_etapas_pipeline
  (id, unidad_negocio_id, nombre, descripcion, orden, probabilidad_defecto, es_ganada, es_perdida, color, activa, created_at, created_by, updated_at, updated_by)
VALUES
  -- Etapa 1: Prospecto (contacto inicial)
  ('e0000000-0000-0000-0000-000000000001',
   '00000000-0000-0000-0000-000000000001',
   'Prospecto',
   'Contacto inicial - lead calificado',
   1,
   10.0,
   false,
   false,
   '#94A3B8',
   true,
   NOW(),
   '30000000-0000-0000-0000-000000000001',
   NOW(),
   '30000000-0000-0000-0000-000000000001'),

  -- Etapa 2: Calificacion (necesidades identificadas)
  ('e0000000-0000-0000-0000-000000000002',
   '00000000-0000-0000-0000-000000000001',
   'Calificacion',
   'Necesidades del cliente identificadas',
   2,
   25.0,
   false,
   false,
   '#60A5FA',
   true,
   NOW(),
   '30000000-0000-0000-0000-000000000001',
   NOW(),
   '30000000-0000-0000-0000-000000000001'),

  -- Etapa 3: Propuesta (solucion presentada)
  ('e0000000-0000-0000-0000-000000000003',
   '00000000-0000-0000-0000-000000000001',
   'Propuesta',
   'Propuesta comercial enviada al cliente',
   3,
   50.0,
   false,
   false,
   '#FBBF24',
   true,
   NOW(),
   '30000000-0000-0000-0000-000000000001',
   NOW(),
   '30000000-0000-0000-0000-000000000001'),

  -- Etapa 4: Negociacion (terminos en discusion)
  ('e0000000-0000-0000-0000-000000000004',
   '00000000-0000-0000-0000-000000000001',
   'Negociacion',
   'Negociacion de terminos y condiciones',
   4,
   75.0,
   false,
   false,
   '#F59E0B',
   true,
   NOW(),
   '30000000-0000-0000-0000-000000000001',
   NOW(),
   '30000000-0000-0000-0000-000000000001'),

  -- Etapa 5: Cierre (contrato por firmar)
  ('e0000000-0000-0000-0000-000000000005',
   '00000000-0000-0000-0000-000000000001',
   'Cierre',
   'Contrato listo para firma',
   5,
   90.0,
   false,
   false,
   '#10B981',
   true,
   NOW(),
   '30000000-0000-0000-0000-000000000001',
   NOW(),
   '30000000-0000-0000-0000-000000000001'),

  -- Etapa 6: Ganada (deal cerrado exitosamente)
  ('e0000000-0000-0000-0000-000000000006',
   '00000000-0000-0000-0000-000000000001',
   'Ganada',
   'Oportunidad cerrada exitosamente',
   6,
   100.0,
   true,  -- es_ganada = true
   false,
   '#059669',
   true,
   NOW(),
   '30000000-0000-0000-0000-000000000001',
   NOW(),
   '30000000-0000-0000-0000-000000000001'),

  -- Etapa 7: Perdida (deal no concretado)
  ('e0000000-0000-0000-0000-000000000007',
   '00000000-0000-0000-0000-000000000001',
   'Perdida',
   'Oportunidad no concretada',
   7,
   0.0,
   false,
   true,  -- es_perdida = true
   '#EF4444',
   true,
   NOW(),
   '30000000-0000-0000-0000-000000000001',
   NOW(),
   '30000000-0000-0000-0000-000000000001');

-- Verificar insercion
SELECT id, nombre, orden, probabilidad_defecto, es_ganada, es_perdida
FROM oportunidades_etapas_pipeline
ORDER BY orden;
```

**1.2.2. Ejecutar migration**
```bash
cd backend/application
./mvnw flyway:migrate

# O si el backend esta corriendo, reiniciar:
docker-compose restart backend
```

**1.2.3. Verificar datos**
```bash
PGPASSWORD=dev_password_123 psql -h localhost -p 28432 -U pagodirecto_dev -d pagodirecto_crm_dev \
  -c "SELECT COUNT(*) FROM oportunidades_etapas_pipeline;"

# Esperado: 7 registros
```

**Testing de la tarea 1.2**:
```bash
# Test 1: Verificar etapas en BD
curl -s "http://localhost:28080/api/v1/etapas" | jq '.content | length'
# Esperado: 7

# Test 2: Crear oportunidad con etapa valida
curl -X POST http://localhost:28080/api/v1/oportunidades \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{
    "unidadNegocioId": "00000000-0000-0000-0000-000000000001",
    "clienteId": "c1111111-0000-0000-0000-000000000001",
    "titulo": "Venta Sistema CRM",
    "valorEstimado": 50000.00,
    "moneda": "MXN",
    "probabilidad": 25.0,
    "etapaId": "e0000000-0000-0000-0000-000000000002",
    "propietarioId": "30000000-0000-0000-0000-000000000001"
  }'

# Esperado: HTTP 201 con oportunidad creada
```

**Definition of Done**:
- [ ] Migration V1.3 ejecutada exitosamente
- [ ] 7 etapas insertadas en BD
- [ ] Endpoint GET /v1/etapas retorna 7 registros
- [ ] Se pueden crear oportunidades con etapaId valido
- [ ] Colores y orden correctos

---

## FASE 2: BUGS ALTA SEVERIDAD (SPRINT 1 - 2 DIAS)

### TAREA 2.1: Corregir manejo de excepciones 404
**Responsable**: Backend Developer
**Tiempo estimado**: 4 horas
**Prioridad**: P1 - ALTA
**Bugs que resuelve**: BUG-003, BUG-004

#### Subtareas:

**2.1.1. Crear excepciones personalizadas**
```java
// backend/core-domain/src/main/java/com/pagodirecto/core/exceptions/ResourceNotFoundException.java

public class ResourceNotFoundException extends RuntimeException {
    private final String resourceType;
    private final String resourceId;

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s no encontrado con ID: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = null;
        this.resourceId = null;
    }
}
```

**2.1.2. Crear GlobalExceptionHandler**
```java
// backend/application/src/main/java/com/pagodirecto/application/api/GlobalExceptionHandler.java

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            WebRequest request) {

        ErrorResponse error = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(404)
            .error("Not Found")
            .message(ex.getMessage())
            .path(extractPath(request))
            .build();

        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .collect(Collectors.toList());

        ErrorResponse error = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(400)
            .error("Validation Failed")
            .message("Error de validacion en los datos enviados")
            .path(extractPath(request))
            .errors(errors)
            .build();

        log.warn("Validation error: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            WebRequest request) {

        String message = "Error de integridad de datos";

        // Detectar violaciones comunes
        if (ex.getMessage().contains("unique constraint")) {
            message = "El registro ya existe (codigo duplicado)";
        } else if (ex.getMessage().contains("foreign key constraint")) {
            message = "Referencia invalida a otro registro";
        }

        ErrorResponse error = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(409)
            .error("Conflict")
            .message(message)
            .path(extractPath(request))
            .build();

        log.error("Data integrity violation", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {

        ErrorResponse error = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(500)
            .error("Internal Server Error")
            .message("Ha ocurrido un error inesperado")
            .path(extractPath(request))
            .build();

        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
```

**2.1.3. Crear ErrorResponse DTO**
```java
// backend/core-domain/src/main/java/com/pagodirecto/core/dto/ErrorResponse.java

@Data
@Builder
public class ErrorResponse {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> errors;  // Para errores de validacion multiples
}
```

**2.1.4. Actualizar servicios para lanzar excepciones correctas**
```java
// backend/clientes/src/main/java/com/pagodirecto/clientes/application/service/ClienteServiceImpl.java

@Override
public ClienteDTO buscarPorId(UUID id) {
    Cliente cliente = clienteRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Cliente", id.toString()));

    return clienteMapper.toDTO(cliente);
}

@Override
public ClienteDTO buscarPorCodigo(String codigo) {
    Cliente cliente = clienteRepository.findByCodigo(codigo)
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format("Cliente no encontrado con codigo: %s", codigo)
        ));

    return clienteMapper.toDTO(cliente);
}
```

**Testing de la tarea 2.1**:
```bash
# Test 1: GET cliente inexistente (debe retornar 404)
curl -s -w "\nHTTP: %{http_code}\n" \
  "http://localhost:28080/api/v1/clientes/99999999-9999-9999-9999-999999999999" \
  | jq '.'

# Esperado:
# {
#   "timestamp": "...",
#   "status": 404,
#   "error": "Not Found",
#   "message": "Cliente no encontrado con ID: ...",
#   "path": "/api/v1/clientes/..."
# }
# HTTP: 404

# Test 2: GET cliente por codigo inexistente (debe retornar 404)
curl -s -w "\nHTTP: %{http_code}\n" \
  "http://localhost:28080/api/v1/clientes/codigo/NOEXISTE" \
  | jq '.'

# Esperado: HTTP 404

# Test 3: POST cliente con datos invalidos (debe retornar 400)
curl -s -w "\nHTTP: %{http_code}\n" \
  -X POST "http://localhost:28080/api/v1/clientes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{"nombre":""}' \
  | jq '.'

# Esperado: HTTP 400 con errores de validacion

# Test 4: POST cliente con codigo duplicado (debe retornar 409)
curl -s -w "\nHTTP: %{http_code}\n" \
  -X POST "http://localhost:28080/api/v1/clientes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{
    "unidadNegocioId": "00000000-0000-0000-0000-000000000001",
    "codigo": "CLI-001",
    "nombre": "Duplicado",
    "tipo": "EMPRESA",
    "status": "LEAD"
  }' \
  | jq '.'

# Esperado: HTTP 409 Conflict
```

**Definition of Done**:
- [ ] ResourceNotFoundException creada
- [ ] GlobalExceptionHandler implementado
- [ ] ErrorResponse DTO estandarizado
- [ ] Servicios lanzan excepciones correctas
- [ ] Tests de errores 404 pasan
- [ ] Tests de errores 400 pasan
- [ ] Tests de errores 409 pasan
- [ ] Logs no muestran stacktraces para errores esperados (404, 400)

---

### TAREA 2.2: Reparar endpoint de busqueda
**Responsable**: Backend Developer
**Tiempo estimado**: 4-6 horas
**Prioridad**: P1 - ALTA
**Bugs que resuelve**: BUG-001

#### Subtareas:

**2.2.1. Debuggear endpoint actual**
```bash
# Reproducir error
curl -v "http://localhost:28080/api/v1/clientes/search?q=Maria&page=0&size=5"

# Revisar logs del backend para identificar causa
```

**2.2.2. Verificar implementacion de ClienteService.buscar()**
```java
// backend/clientes/src/main/java/com/pagodirecto/clientes/application/service/ClienteServiceImpl.java

@Override
public Page<ClienteDTO> buscar(String query, Pageable pageable) {
    log.debug("Buscando clientes con query: {}", query);

    // VERIFICAR: Esta query puede estar causando el error
    Page<Cliente> clientes = clienteRepository.searchByQuery(query, pageable);

    return clientes.map(clienteMapper::toDTO);
}
```

**2.2.3. Revisar query en ClienteRepository**
```java
// backend/clientes/src/main/java/com/pagodirecto/clientes/infrastructure/persistence/ClienteRepository.java

// OPCION A: JPQL Query
@Query("""
    SELECT c FROM Cliente c
    WHERE c.deletedAt IS NULL
      AND (
        LOWER(c.nombre) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(c.codigo) LIKE LOWER(CONCAT('%', :query, '%'))
      )
    """)
Page<Cliente> searchByQuery(@Param("query") String query, Pageable pageable);

// OPCION B: Specification (mas flexible)
default Page<Cliente> searchByQuery(String query, Pageable pageable) {
    Specification<Cliente> spec = (root, criteriaQuery, criteriaBuilder) -> {
        if (query == null || query.isBlank()) {
            return criteriaBuilder.conjunction();
        }

        String likePattern = "%" + query.toLowerCase() + "%";

        Predicate nombrePredicate = criteriaBuilder.like(
            criteriaBuilder.lower(root.get("nombre")), likePattern);

        Predicate emailPredicate = criteriaBuilder.like(
            criteriaBuilder.lower(root.get("email")), likePattern);

        Predicate razonSocialPredicate = criteriaBuilder.like(
            criteriaBuilder.lower(root.get("razonSocial")), likePattern);

        Predicate codigoPredicate = criteriaBuilder.like(
            criteriaBuilder.lower(root.get("codigo")), likePattern);

        Predicate deletedPredicate = criteriaBuilder.isNull(root.get("deletedAt"));

        return criteriaBuilder.and(
            deletedPredicate,
            criteriaBuilder.or(nombrePredicate, emailPredicate, razonSocialPredicate, codigoPredicate)
        );
    };

    return findAll(spec, pageable);
}
```

**2.2.4. Agregar validacion en el controller**
```java
// backend/clientes/src/main/java/com/pagodirecto/clientes/api/controller/ClienteController.java

@GetMapping("/search")
public ResponseEntity<Page<ClienteDTO>> buscar(
        @Parameter(description = "Termino de busqueda")
        @RequestParam(required = false, defaultValue = "") String q,
        @PageableDefault(size = 20, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {

    // Validar query minima (al menos 2 caracteres)
    if (q != null && q.length() > 0 && q.length() < 2) {
        throw new IllegalArgumentException("El termino de busqueda debe tener al menos 2 caracteres");
    }

    log.debug("Buscando clientes con termino: {}", q);
    Page<ClienteDTO> clientes = clienteService.buscar(q, pageable);
    return ResponseEntity.ok(clientes);
}
```

**2.2.5. Agregar tests unitarios**
```java
// backend/clientes/src/test/java/com/pagodirecto/clientes/application/service/ClienteServiceTest.java

@Test
void buscar_conTerminoValido_retornaResultados() {
    // Arrange
    String query = "Maria";
    Pageable pageable = PageRequest.of(0, 20);

    // Act
    Page<ClienteDTO> resultado = clienteService.buscar(query, pageable);

    // Assert
    assertNotNull(resultado);
    assertTrue(resultado.getTotalElements() > 0);
}

@Test
void buscar_conTerminoVacio_retornaTodos() {
    // Arrange
    String query = "";
    Pageable pageable = PageRequest.of(0, 20);

    // Act
    Page<ClienteDTO> resultado = clienteService.buscar(query, pageable);

    // Assert
    assertNotNull(resultado);
    // Debe retornar todos los clientes activos
}

@Test
void buscar_conTerminoSinCoincidencias_retornaVacio() {
    // Arrange
    String query = "XYZABC123456";
    Pageable pageable = PageRequest.of(0, 20);

    // Act
    Page<ClienteDTO> resultado = clienteService.buscar(query, pageable);

    // Assert
    assertNotNull(resultado);
    assertEquals(0, resultado.getTotalElements());
}
```

**Testing de la tarea 2.2**:
```bash
# Test 1: Busqueda con termino valido
curl -s "http://localhost:28080/api/v1/clientes/search?q=Maria&page=0&size=5" | jq '.totalElements'
# Esperado: >0

# Test 2: Busqueda por email
curl -s "http://localhost:28080/api/v1/clientes/search?q=gonzalez&page=0&size=5" | jq '.content[0].email'
# Esperado: "maria.gonzalez@email.com"

# Test 3: Busqueda por codigo
curl -s "http://localhost:28080/api/v1/clientes/search?q=CLI-001&page=0&size=5" | jq '.content[0].codigo'
# Esperado: "CLI-001"

# Test 4: Busqueda sin resultados
curl -s "http://localhost:28080/api/v1/clientes/search?q=NoExiste123&page=0&size=5" | jq '.totalElements'
# Esperado: 0

# Test 5: Busqueda con query vacia
curl -s "http://localhost:28080/api/v1/clientes/search?q=&page=0&size=5" | jq '.totalElements'
# Esperado: total de clientes activos

# Test 6: Case insensitive
curl -s "http://localhost:28080/api/v1/clientes/search?q=MARIA&page=0&size=5" | jq '.totalElements'
# Esperado: >0 (mismo resultado que "Maria")
```

**Definition of Done**:
- [ ] Endpoint /search retorna HTTP 200
- [ ] Busqueda funciona por nombre, email, razon social, codigo
- [ ] Busqueda es case-insensitive
- [ ] Busqueda con query vacia retorna todos los registros
- [ ] Busqueda sin resultados retorna array vacio (no error)
- [ ] Paginacion funciona correctamente
- [ ] Tests unitarios agregados y pasando
- [ ] Performance aceptable (<50ms con 1000 registros)

---

## FASE 3: BUGS MEDIA SEVERIDAD (SPRINT 2 - 1 DIA)

### TAREA 3.1: Agregar headers de seguridad CSP y HSTS
**Responsable**: Backend Developer
**Tiempo estimado**: 2 horas
**Prioridad**: P2 - MEDIA
**Bugs que resuelve**: BUG-007

#### Subtareas:

**3.1.1. Actualizar SecurityConfig con headers de seguridad**
```java
// backend/seguridad/src/main/java/com/pagodirecto/seguridad/infrastructure/security/SecurityConfig.java

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // ... configuracion existente ...
        .headers(headers -> headers
            .contentSecurityPolicy(csp -> csp
                .policyDirectives(
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline'; " +
                    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                    "font-src 'self' https://fonts.gstatic.com; " +
                    "img-src 'self' data: https:; " +
                    "connect-src 'self' http://localhost:28000"
                ))
            .httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000)  // 1 ano
                .preload(true))
            .referrerPolicy(referrer -> referrer
                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            .permissionsPolicy(permissions -> permissions
                .policy("geolocation=(), microphone=(), camera=()"))
        );

    return http.build();
}
```

**3.1.2. Agregar configuracion condicional para desarrollo vs produccion**
```java
// application.yml
app:
  security:
    hsts:
      enabled: ${HSTS_ENABLED:false}  # false en desarrollo, true en produccion
    csp:
      enabled: ${CSP_ENABLED:true}
```

```java
// SecurityConfig.java
@Value("${app.security.hsts.enabled}")
private boolean hstsEnabled;

@Value("${app.security.csp.enabled}")
private boolean cspEnabled;

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .headers(headers -> {
            if (cspEnabled) {
                headers.contentSecurityPolicy(csp -> csp.policyDirectives(...));
            }

            if (hstsEnabled) {
                headers.httpStrictTransportSecurity(hsts -> hsts...);
            }

            headers
                .referrerPolicy(...)
                .permissionsPolicy(...);
        });

    return http.build();
}
```

**Testing de la tarea 3.1**:
```bash
# Test 1: Verificar CSP header
curl -s -I "http://localhost:28080/api/v1/clientes" | grep -i "content-security-policy"
# Esperado: Content-Security-Policy: default-src 'self'; ...

# Test 2: Verificar HSTS header (solo si habilitado)
curl -s -I "http://localhost:28080/api/v1/clientes" | grep -i "strict-transport-security"
# Esperado: Strict-Transport-Security: max-age=31536000; includeSubDomains; preload

# Test 3: Verificar Referrer-Policy
curl -s -I "http://localhost:28080/api/v1/clientes" | grep -i "referrer-policy"
# Esperado: Referrer-Policy: strict-origin-when-cross-origin

# Test 4: Verificar Permissions-Policy
curl -s -I "http://localhost:28080/api/v1/clientes" | grep -i "permissions-policy"
# Esperado: Permissions-Policy: geolocation=(), microphone=(), camera=()
```

**Definition of Done**:
- [ ] CSP header configurado y funcional
- [ ] HSTS configurado (deshabilitado en dev, habilitado en prod)
- [ ] Referrer-Policy configurado
- [ ] Permissions-Policy configurado
- [ ] Tests de headers pasan
- [ ] Frontend no se rompe por CSP (React puede cargar correctamente)

---

## FASE 4: RE-CERTIFICACION QA (SPRINT 2 - 2-3 DIAS)

### TAREA 4.1: Ejecutar suite completa de pruebas QA
**Responsable**: QA Engineer
**Tiempo estimado**: 2 dias
**Prioridad**: P1

#### Subtareas:

**4.1.1. Pruebas de regresion**
- Repetir todos los casos de prueba que fallaron anteriormente
- Verificar que bugs criticos estan resueltos
- Ejecutar nuevos casos de prueba para autenticacion

**4.1.2. Pruebas de flujo completo (E2E)**
```bash
# Flujo 1: Autenticacion y creacion de cliente
1. POST /v1/auth/login -> Obtener token
2. POST /v1/clientes con token -> Crear cliente
3. GET /v1/clientes/{id} -> Verificar cliente creado
4. PUT /v1/clientes/{id} -> Actualizar cliente
5. DELETE /v1/clientes/{id} -> Eliminar (soft delete)
6. GET /v1/clientes/{id} -> Verificar 404

# Flujo 2: Creacion de oportunidad completa
1. POST /v1/auth/login -> Obtener token
2. POST /v1/oportunidades -> Crear oportunidad en etapa "Prospecto"
3. PUT /v1/oportunidades/{id}/mover-etapa -> Mover a "Calificacion"
4. PUT /v1/oportunidades/{id}/mover-etapa -> Mover a "Propuesta"
5. PUT /v1/oportunidades/{id}/marcar-ganada -> Cerrar como ganada
6. GET /v1/oportunidades/{id} -> Verificar estado final
```

**4.1.3. Pruebas de seguridad**
- Intentar acceder a endpoints protegidos sin token (esperado: 401)
- Intentar acceder con token expirado (esperado: 401)
- Intentar acceder con token invalido (esperado: 401)
- Verificar que operaciones requieren autenticacion
- SQL injection re-test
- XSS re-test

**4.1.4. Pruebas de performance**
- Load test con 100 usuarios concurrentes
- Medir tiempos de operaciones de escritura (POST/PUT)
- Verificar que latency budgets se cumplen

**4.1.5. Actualizar reporte de certificacion**
- Crear nuevo reporte QA con resultados actualizados
- Documentar bugs resueltos
- Documentar nuevos bugs encontrados (si los hay)
- Actualizar metricas de calidad

**Definition of Done**:
- [ ] 100% de bugs criticos resueltos
- [ ] 100% de bugs alta severidad resueltos
- [ ] Tasa de exito API >95%
- [ ] Endpoints funcionales >95%
- [ ] Performance dentro de budgets
- [ ] Security audit aprobado
- [ ] Reporte QA actualizado

---

## RESUMEN DE TIMELINE

| Sprint | Semana | Tareas | Responsables |
|--------|--------|--------|--------------|
| **Sprint 1** | Semana 1 | Fase 1 + Fase 2 | Backend Team (2-3 devs) |
| | Dias 1-3 | Tarea 1.1: Autenticacion JWT | Backend Lead |
| | Dia 1 | Tarea 1.2: Seed data etapas | Backend Dev |
| | Dias 4-5 | Tarea 2.1: Excepciones 404 | Backend Dev |
| | Dias 4-5 | Tarea 2.2: Endpoint search | Backend Dev |
| **Sprint 2** | Semana 2 | Fase 3 + Fase 4 | Backend + QA |
| | Dia 1 | Tarea 3.1: Security headers | Backend Dev |
| | Dias 2-4 | Tarea 4.1: Re-certificacion QA | QA Engineer |

**Duracion total**: 2 semanas (10 dias habiles)

**Hitos**:
- Dia 3: Autenticacion funcional (bloqueante resuelto)
- Dia 5: Bugs P0 y P1 resueltos
- Dia 6: Bugs P2 resueltos
- Dia 10: Re-certificacion completada

---

## CRITERIOS DE ACEPTACION FINAL

### Para aprobar paso a STAGING:
- [ ] Bugs P0 (criticos) resueltos: 4/4
- [ ] Bugs P1 (alta severidad) resueltos: 3/3
- [ ] Operaciones de escritura funcionan: 100%
- [ ] Autenticacion JWT funcional
- [ ] Tests E2E pasando

### Para aprobar paso a PRODUCCION:
- [ ] Todos los bugs resueltos (P0, P1, P2)
- [ ] Re-certificacion QA aprobada
- [ ] Load testing exitoso (1000 usuarios concurrentes)
- [ ] Security audit sin hallazgos criticos/altos
- [ ] Backups configurados y probados
- [ ] Monitoreo y alertas configurados
- [ ] Documentacion actualizada
- [ ] Runbooks operacionales creados

---

## RIESGOS Y CONTINGENCIAS

| Riesgo | Probabilidad | Impacto | Contingencia |
|--------|--------------|---------|--------------|
| Autenticacion toma mas de 3 dias | Media | Alto | Asignar desarrollador adicional |
| Nuevos bugs encontrados en QA | Alta | Medio | Buffer de 2 dias en Sprint 2 |
| Performance no cumple con load test | Baja | Alto | Optimizar queries, agregar cache |
| Dependencias externas no disponibles | Baja | Bajo | Usar mocks en desarrollo |

---

## CONTACTO Y ESCALACION

**Project Manager**: [Nombre]
**Backend Team Lead**: [Nombre]
**QA Engineer Lead**: Claude Code
**DevOps Engineer**: [Nombre]

**Escalacion**:
1. Desarrolladores <-> Team Lead (resolucion inmediata)
2. Team Lead <-> PM (bloqueantes mayores a 1 dia)
3. PM <-> CTO (cambios de scope o timeline)

**Comunicacion diaria**:
- Daily standup 9:00 AM
- Reporte de progreso EOD via Slack/Email
- Reunion de sprint review al finalizar cada sprint

---

**FIN DEL PLAN DE ACCION**
