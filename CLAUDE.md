# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Enterprise ERP/CRM system built as a **modular monolith** using Spring Boot with clear separation of concerns. The system manages core business domains: Cuentas, Clientes, Productos, Ventas, Pagos, Seguridad, and Reportes.

**Philosophy**: Start simple, optimize for maintainability and team velocity. We favor pragmatic solutions over architectural complexity. Scale when needed, not before.

**Chief Systems Engineer Agent**: This project follows the CSE persona specifications defined in the agent configuration - a pragmatic, business-oriented approach with 30+ years of backend, security, and DBA expertise.

## Technology Stack

**Backend:**
- Java 17 + Spring Boot 3.x
- Spring Data JPA + Hibernate
- PostgreSQL (primary datastore)
- Maven (build tool)

**Frontend:**
- React + TypeScript + Tailwind CSS
- Vite (build tool)
- React Router for navigation
- TanStack Query for server state
- Mobile: Flutter/iOS native (when required)

**Infrastructure:**
- Docker + docker-compose
- CI/CD: GitHub Actions
- Production: Single JAR deployment (scale vertically first)

**Security:**
- OAuth2/OIDC authentication
- JWT with rotation
- RBAC/ABAC authorization model
- OWASP ASVS compliance

## Architecture Principles

### Modular Monolith Pattern
This is a **single Spring Boot application** organized into cohesive modules by business domain. Each module follows a simple layered architecture:

- **Controller Layer**: REST endpoints, request/response DTOs, validation
- **Service Layer**: Business logic, orchestration, transactions
- **Repository Layer**: Data access via Spring Data JPA
- **Model Layer**: JPA entities, domain logic

### Module Organization Rules
- **Package-by-feature**: Group by business capability, not technical layer
- **No circular dependencies**: Modules should not depend on each other directly
- **Shared code**: Common utilities, security, and configuration in `shared` package
- **Keep it simple**: Start with CRUD, add complexity only when needed

### Project Structure
```
crm-backend/                      # Single Spring Boot application
├── src/main/java/com/empresa/crm/
│   ├── CrmApplication.java       # Main Spring Boot entry point
│   ├── shared/                   # Cross-cutting concerns
│   │   ├── config/               # SecurityConfig, WebConfig, OpenApiConfig
│   │   ├── exception/            # GlobalExceptionHandler, custom exceptions
│   │   ├── security/             # JWT utilities, authentication
│   │   └── util/                 # Date helpers, validators, etc.
│   ├── cuentas/                  # Accounts module
│   │   ├── controller/
│   │   │   └── CuentaController.java
│   │   ├── service/
│   │   │   ├── CuentaService.java
│   │   │   └── CuentaServiceImpl.java
│   │   ├── repository/
│   │   │   └── CuentaRepository.java
│   │   ├── model/
│   │   │   └── Cuenta.java
│   │   └── dto/
│   │       ├── CuentaDTO.java
│   │       └── CrearCuentaRequest.java
│   ├── clientes/                 # Customers module
│   ├── productos/                # Products module
│   ├── ventas/                   # Sales module
│   ├── pagos/                    # Payments module
│   ├── seguridad/                # Security/Users module
│   └── reportes/                 # Reports module
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/             # Flyway migrations
│       ├── V1__init_schema.sql
│       └── V2__add_clientes.sql
├── src/test/java/
│   └── com/empresa/crm/
│       └── clientes/
│           ├── ClienteServiceTest.java
│           └── ClienteControllerTest.java
└── pom.xml

frontend/                         # React SPA
├── src/
│   ├── features/                 # Feature-based organization
│   │   ├── clientes/
│   │   ├── productos/
│   │   └── ventas/
│   ├── components/               # Shared UI components
│   ├── lib/                      # API clients, utilities
│   └── App.tsx
├── package.json
└── vite.config.ts

docker/
├── Dockerfile
├── docker-compose.yml
└── docker-compose.prod.yml

docs/
├── api/                          # OpenAPI specs
├── adrs/                         # Architecture decisions (when needed)
└── setup.md                      # Development setup guide
```

### When to Extract a Microservice
Only consider breaking out services when you have:
- Clear performance bottleneck that can't be solved with caching/optimization
- Team >10 developers with independent release cycles
- Regulatory requirement for isolation (e.g., PCI DSS payment processing)

**Default answer: Don't. Keep it simple.**

## Development Commands

### Backend (Java + Spring Boot)
```bash
# Build
cd crm-backend
./mvnw clean install

# Run tests
./mvnw test
./mvnw test -Dtest=ClienteServiceTest#deberiaCrearCliente  # Single test

# Run application (dev mode)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run with specific port
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081

# Database migrations (Flyway - runs automatically on startup)
# Manual migration commands:
./mvnw flyway:migrate
./mvnw flyway:info
./mvnw flyway:validate

# Generate OpenAPI spec
./mvnw spring-boot:run # Then visit http://localhost:8080/v3/api-docs
```

### Frontend (React + TypeScript)
```bash
# Install dependencies
cd frontend
npm install

# Development server (with backend proxy)
npm run dev
# Opens http://localhost:5173

# Build for production
npm run build

# Preview production build
npm run preview

# Run tests
npm test
npm test -- ClienteList.test.tsx  # Single test

# Linting
npm run lint
npm run lint:fix

# Type checking
npm run type-check
```

### Docker (Full Stack)
```bash
# Start all services (Postgres + Backend + Frontend)
docker-compose up -d

# View logs
docker-compose logs -f backend
docker-compose logs -f postgres

# Rebuild after code changes
docker-compose up -d --build

# Stop services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v

# Access database directly
docker-compose exec postgres psql -U crm_user -d crm_db
```

## Database Guidelines

### Schema Design
- **Primary Keys**: Use `BIGSERIAL` (auto-increment Long) for simplicity. Use UUIDs only if you have distributed write requirements.
- **Timestamps**: All tables have `created_at`, `updated_at` with timezone (`TIMESTAMPTZ`)
- **Soft Delete**: Use `deleted_at` timestamp instead of hard deletes for business records
- **Audit Trail**: Include `created_by`, `updated_by` (user ID references)
- **Naming**: Snake_case for tables/columns (PostgreSQL convention)

### Partitioning Strategy
**Start without partitioning.** Add it only when you have:
- Tables >100M rows
- Clear partition key (date, tenant_id)
- Performance issues proven by EXPLAIN ANALYZE

Most applications never need it.

### Indexing Rules
- **Always index foreign keys** (JPA won't do this automatically)
- **Index columns in WHERE clauses** for common queries
- **Partial indexes** for soft-deleted tables: `WHERE deleted_at IS NULL`
- **Monitor slow queries**: Enable `log_min_duration_statement = 500` in dev

### Example Table Template (Flyway Migration)
```sql
-- V1__create_clientes.sql
CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    telefono VARCHAR(20),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by BIGINT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by BIGINT NOT NULL,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_clientes_created_by FOREIGN KEY (created_by) REFERENCES usuarios(id),
    CONSTRAINT fk_clientes_updated_by FOREIGN KEY (updated_by) REFERENCES usuarios(id)
);

-- Indexes
CREATE INDEX idx_clientes_email ON clientes(email) WHERE deleted_at IS NULL;
CREATE INDEX idx_clientes_created_by ON clientes(created_by);
CREATE INDEX idx_clientes_activo ON clientes(activo) WHERE deleted_at IS NULL;

-- Comments for documentation
COMMENT ON TABLE clientes IS 'Clientes/Customers - Core customer management';
COMMENT ON COLUMN clientes.activo IS 'Business active status (different from soft delete)';
```

### Corresponding JPA Entity
```java
@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", nullable = false)
    private Long updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        // Set createdBy/updatedBy from SecurityContext
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Set updatedBy from SecurityContext
    }
}
```

## Security Implementation

### RBAC Model (Start Simple)
- **Roles**: ADMIN, MANAGER, USER (stored in database)
- **Spring Security**: `@PreAuthorize("hasRole('ADMIN')")` on controller methods
- **JWT Claims**: Include user roles in JWT token
- **Multi-tenancy**: Add `tenantId` filter in service layer (not RLS initially)

### Security Checklist (Per Release)
- [ ] Dependencies updated (Dependabot alerts resolved)
- [ ] SAST/DAST scans completed
- [ ] Secrets scanning passed (no hardcoded credentials)
- [ ] Backup restore tested successfully
- [ ] Security headers configured (CSP, HSTS, X-Frame-Options)
- [ ] Password policies enforced (complexity, expiration, history)
- [ ] MFA enabled for privileged accounts

### Secrets Management
- Never commit secrets to git
- Use environment variables or external vault (HashiCorp Vault, AWS Secrets Manager)
- Rotate JWT signing keys quarterly
- Database credentials via secret manager only

## API Guidelines

### REST Conventions
- **Versioning**: `/api/v1/resource`
- **Pagination**: `?page=0&size=20&sort=createdAt,desc`
- **Filtering**: `?filter=status:eq:active,amount:gt:1000`
- **Error Format**: RFC 7807 Problem Details
```json
{
  "type": "https://api.example.com/errors/validation-failed",
  "title": "Validation Failed",
  "status": 422,
  "detail": "Field 'email' must be a valid email address",
  "instance": "/api/v1/clientes",
  "errors": [
    {"field": "email", "code": "invalid_format"}
  ]
}
```

### Idempotency
- Use `Idempotency-Key` header for mutation operations
- Store request fingerprints with TTL (24 hours)
- Return cached response for duplicate requests

## Testing Requirements

### Coverage Targets
- **Unit Tests**: >80% code coverage
- **Integration Tests**: Critical business workflows
- **Contract Tests**: API versioning guarantees
- **E2E Tests**: User journeys for core features
- **Load Tests**: Performance benchmarks for key endpoints

### Test Naming Convention
```java
// Java (JUnit)
@Test
void whenUserCreatesOrder_thenInventoryShouldDecrement() { }

// TypeScript (Jest)
describe('OrderService', () => {
  it('should decrement inventory when user creates order', () => {});
});
```

## Performance Guidelines

### Latency Budgets (p95)
- **Read APIs**: <200ms
- **Write APIs**: <500ms
- **Batch Operations**: <2s
- **Reports**: <5s (with pagination)

### N+1 Query Prevention
- Use `@EntityGraph` or JOIN FETCH in JPA queries
- Enable Hibernate query logging in dev
- Regular query plan reviews with DBA

### Caching Strategy (Add When Needed)
- **Start**: No caching. Measure first.
- **L1**: Spring `@Cacheable` with Caffeine (in-memory)
- **L2**: Redis only if you have multiple backend instances
- **Database**: PostgreSQL is fast. Proper indexes > caching.

## Observability

### Logging
- **Structured JSON logs** (include trace_id, user_id, tenant_id)
- **Log Levels**: ERROR (actionable), WARN (degraded), INFO (lifecycle), DEBUG (troubleshooting)
- **Sensitive Data**: Never log passwords, tokens, PII

### Metrics
- **RED metrics**: Rate, Errors, Duration per endpoint
- **Database**: Connection pool usage, query latency
- **Business**: Orders/hour, revenue, active users

### Distributed Tracing
- Not needed for monolith initially
- Spring Boot Actuator + Micrometer provides sufficient metrics
- Add OpenTelemetry only when you have microservices

## CI/CD Pipeline

### Stages
1. **Build**: Compile source code
2. **Test**: Unit + integration tests
3. **Lint**: Code style and static analysis
4. **Security Scan**: SAST (SonarQube), SCA (OWASP Dependency-Check)
5. **Package**: Build Docker images
6. **Deploy**: Rolling deployment with health checks

### Git Workflow
- **Trunk-based development** with short-lived feature branches
- **Feature flags** for incomplete features
- **Semantic versioning**: MAJOR.MINOR.PATCH
- **Database migrations**: Automated via Flyway on deploy
- **No direct commits to main**: All changes via pull requests

## Definition of Done

Before marking a story complete:
- [ ] All acceptance criteria met
- [ ] Unit tests written and passing (>80% coverage)
- [ ] Integration tests for happy path + error cases
- [ ] API documentation updated (OpenAPI spec)
- [ ] Security checklist reviewed (authentication, authorization, input validation)
- [ ] Performance tested (latency within budget)
- [ ] Monitoring/alerting configured
- [ ] Documentation updated (README, ADR if architectural change)
- [ ] Code review approved by at least one peer
- [ ] Database migration tested (up and rollback)

## Roles & Permissions Matrix

Map departments to roles, then roles to permissions:

```
Department     → Role          → Spring Security Expression
---------------------------------------------------------------------------
Administración → ROLE_ADMIN    → hasRole('ADMIN') - Full access
Ventas         → ROLE_MANAGER  → hasRole('MANAGER') - CRUD Clientes, Ventas
Ventas         → ROLE_USER     → hasRole('USER') - Read Clientes, Create Ventas
Finanzas       → ROLE_FINANCE  → hasRole('FINANCE') - CRUD Pagos, Reports
```

**Implementation:**
```java
@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ClienteDTO crear(@Valid @RequestBody ClienteDTO dto) {
        return clienteService.crear(dto);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<ClienteDTO> listar(Pageable pageable) {
        return clienteService.listar(pageable);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
    }
}
```

## i18n and Accessibility

### Internationalization
- Spanish (es-MX) as primary locale
- English (en-US) as secondary
- Use `react-intl` or `i18next` for frontend
- Store translations in `backend/src/main/resources/messages_*.properties`

### Accessibility (WCAG 2.1 AA)
- Semantic HTML5 elements
- ARIA labels for interactive components
- Keyboard navigation support
- Color contrast ratio ≥4.5:1
- Focus indicators visible

## Docker Best Practices

### Dockerfile Template
```dockerfile
FROM eclipse-temurin:17-jre-alpine AS runtime
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
WORKDIR /app
COPY --chown=appuser:appgroup target/*.jar app.jar
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Security
- Multi-stage builds to minimize image size
- Non-root user
- Health checks for orchestration
- SBOM generation for supply chain security

## Code Examples

### Complete CRUD Module Example

Here's a complete working example for the `clientes` module:

**Controller (ClienteController.java)**
```java
@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService clienteService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<ClienteDTO> listar(
        @RequestParam(required = false) String search,
        Pageable pageable
    ) {
        return clienteService.listar(search, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClienteDTO> obtener(@PathVariable Long id) {
        return clienteService.obtener(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ClienteDTO> crear(@Valid @RequestBody CrearClienteRequest request) {
        ClienteDTO created = clienteService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ClienteDTO> actualizar(
        @PathVariable Long id,
        @Valid @RequestBody ActualizarClienteRequest request
    ) {
        return clienteService.actualizar(id, request)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Service Interface (ClienteService.java)**
```java
public interface ClienteService {
    Page<ClienteDTO> listar(String search, Pageable pageable);
    Optional<ClienteDTO> obtener(Long id);
    ClienteDTO crear(CrearClienteRequest request);
    Optional<ClienteDTO> actualizar(Long id, ActualizarClienteRequest request);
    void eliminar(Long id);
}
```

**Service Implementation (ClienteServiceImpl.java)**
```java
@Service
@Transactional
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {
    private final ClienteRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteDTO> listar(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return repository.findByNombreContainingIgnoreCaseAndDeletedAtIsNull(search, pageable)
                .map(this::toDTO);
        }
        return repository.findByDeletedAtIsNull(pageable)
            .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClienteDTO> obtener(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id)
            .map(this::toDTO);
    }

    @Override
    public ClienteDTO crear(CrearClienteRequest request) {
        Cliente cliente = new Cliente();
        cliente.setNombre(request.getNombre());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setActivo(true);

        return toDTO(repository.save(cliente));
    }

    @Override
    public Optional<ClienteDTO> actualizar(Long id, ActualizarClienteRequest request) {
        return repository.findByIdAndDeletedAtIsNull(id)
            .map(cliente -> {
                cliente.setNombre(request.getNombre());
                cliente.setEmail(request.getEmail());
                cliente.setTelefono(request.getTelefono());
                return toDTO(repository.save(cliente));
            });
    }

    @Override
    public void eliminar(Long id) {
        repository.findByIdAndDeletedAtIsNull(id)
            .ifPresent(cliente -> {
                cliente.setDeletedAt(LocalDateTime.now());
                repository.save(cliente);
            });
    }

    private ClienteDTO toDTO(Cliente entity) {
        return ClienteDTO.builder()
            .id(entity.getId())
            .nombre(entity.getNombre())
            .email(entity.getEmail())
            .telefono(entity.getTelefono())
            .activo(entity.getActivo())
            .build();
    }
}
```

**Repository (ClienteRepository.java)**
```java
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Page<Cliente> findByDeletedAtIsNull(Pageable pageable);
    Optional<Cliente> findByIdAndDeletedAtIsNull(Long id);
    Page<Cliente> findByNombreContainingIgnoreCaseAndDeletedAtIsNull(String nombre, Pageable pageable);
}
```

**DTOs**
```java
@Data
@Builder
public class ClienteDTO {
    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private Boolean activo;
}

@Data
public class CrearClienteRequest {
    @NotBlank(message = "Nombre es requerido")
    @Size(max = 200)
    private String nombre;

    @NotBlank(message = "Email es requerido")
    @Email(message = "Email debe ser válido")
    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String telefono;
}

@Data
public class ActualizarClienteRequest {
    @NotBlank
    @Size(max = 200)
    private String nombre;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String telefono;
}
```

## Critical Architectural Decisions

When making significant architectural changes:
1. Document in `docs/adrs/NNNN-title.md` if it's a major decision
2. Use template: Context, Decision, Consequences, Alternatives Considered
3. Most decisions don't need ADRs - use code comments for clarity

## Performance Optimization Checklist

Before production release:
- [ ] Database indexes validated with EXPLAIN ANALYZE
- [ ] N+1 queries identified and fixed
- [ ] Load testing completed (target: 1000 concurrent users)
- [ ] Caching strategy implemented for read-heavy endpoints
- [ ] Database connection pool sized appropriately
- [ ] API rate limiting configured
- [ ] CDN configured for static assets

## Common Pitfalls

### Database
- **Avoid**: `SELECT *` in production code
- **Use**: Explicit column selection, DTOs for projections
- **Avoid**: Lazy loading causing N+1 queries
- **Use**: Explicit JOIN FETCH or @EntityGraph

### Security
- **Avoid**: Trusting client-side validation alone
- **Use**: Server-side validation + sanitization
- **Avoid**: Using `@PreAuthorize` without testing
- **Use**: Integration tests for authorization rules

### API Design
- **Avoid**: Returning full entities with sensitive data
- **Use**: DTOs with explicit field whitelisting
- **Avoid**: Breaking API changes in minor versions
- **Use**: API versioning, deprecation notices

## Getting Started Checklist

When starting a new module (e.g., `productos`):

1. **Database First**
   - [ ] Create Flyway migration: `V{N}__create_productos.sql`
   - [ ] Define table with indexes
   - [ ] Add audit columns (created_at, updated_at, etc.)

2. **Backend (Java)**
   - [ ] Create `@Entity` class in `model/` package
   - [ ] Create `Repository` interface extending `JpaRepository`
   - [ ] Create `Service` interface and implementation
   - [ ] Create DTOs (EntityDTO, CrearRequest, ActualizarRequest)
   - [ ] Create `@RestController` with CRUD endpoints
   - [ ] Add `@PreAuthorize` annotations for security

3. **Testing**
   - [ ] Write unit tests for service layer
   - [ ] Write integration tests for controller
   - [ ] Test with Postman/curl

4. **Frontend (React)**
   - [ ] Create feature folder: `src/features/productos/`
   - [ ] Create API client functions
   - [ ] Create list/detail/form components
   - [ ] Wire up routing

5. **Documentation**
   - [ ] Update OpenAPI docs (auto-generated)
   - [ ] Add comments for non-obvious business logic

## Development Principles

**Remember:**
- **YAGNI**: You Aren't Gonna Need It - Don't build features "just in case"
- **KISS**: Keep It Simple, Stupid - Prefer simple solutions
- **Measure first**: Don't optimize without profiling
- **Start with the database**: Schema design drives your domain model
- **DTOs always**: Never expose entities directly in APIs
- **Test what matters**: Focus on business logic, not getters/setters

## Contact & Escalation

For architectural questions or blockers:
1. Review this CLAUDE.md for patterns and examples
2. Check existing code in similar modules
3. Consult with team lead or senior developer
4. Document significant decisions in code comments
