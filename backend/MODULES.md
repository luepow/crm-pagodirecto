# Backend Modules Structure

## Overview

The backend is organized into 8 modules following Clean/Hexagonal Architecture and Domain-Driven Design principles. Each module represents a bounded context with clear boundaries and dependencies.

## Module Hierarchy

```
crm-erp-parent (1.0.0-SNAPSHOT)
├── core-domain          (shared primitives)
├── seguridad            (authentication & authorization)
├── clientes             (customer management)
├── oportunidades        (sales opportunities)
├── tareas               (task management)
├── productos            (product catalog)
├── ventas               (sales operations)
├── reportes             (reports & analytics)
└── application          (main Spring Boot app)
```

## Module Details

### 1. core-domain
**Purpose**: Shared domain primitives, value objects, and base entities

**Dependencies**: None (foundation module)

**Package Structure**:
- `com.pagodirecto.core.domain` - Base entities and value objects
- `com.pagodirecto.core.application` - Shared application services
- `com.pagodirecto.core.infrastructure` - Common infrastructure utilities

### 2. seguridad
**Purpose**: Authentication, authorization, users, roles, permissions and IAM management

**Dependencies**:
- core-domain
- JWT (JJWT library for token management)

**Package Structure**:
- `com.pagodirecto.seguridad.domain` - User, Role, Permission entities
- `com.pagodirecto.seguridad.application` - Authentication/Authorization use cases
- `com.pagodirecto.seguridad.infrastructure` - JWT token services, repositories
- `com.pagodirecto.seguridad.api` - REST controllers for auth endpoints

**Key Features**:
- OAuth2/OIDC authentication
- JWT token generation and validation
- RBAC/ABAC authorization model
- Password management and MFA

### 3. clientes
**Purpose**: Customer relationship management, client profiles, contacts and account management

**Dependencies**:
- core-domain
- seguridad (for user associations)

**Package Structure**:
- `com.pagodirecto.clientes.domain` - Cliente, Contacto entities
- `com.pagodirecto.clientes.application` - Customer management use cases
- `com.pagodirecto.clientes.infrastructure` - Repositories, external integrations
- `com.pagodirecto.clientes.api` - REST controllers for customer operations

**Key Features**:
- Customer profile management
- Contact information tracking
- Account hierarchy
- Customer segmentation

### 4. oportunidades
**Purpose**: Sales opportunities, pipeline management, lead tracking and opportunity workflow

**Dependencies**:
- core-domain
- seguridad (for user assignments)
- clientes (for customer associations)

**Package Structure**:
- `com.pagodirecto.oportunidades.domain` - Oportunidad, Etapa entities
- `com.pagodirecto.oportunidades.application` - Pipeline management use cases
- `com.pagodirecto.oportunidades.infrastructure` - Repositories, workflow engine
- `com.pagodirecto.oportunidades.api` - REST controllers for opportunity operations

**Key Features**:
- Opportunity lifecycle management
- Sales pipeline visualization
- Lead qualification and scoring
- Sales forecast calculations

### 5. tareas
**Purpose**: Task management, activities tracking, calendar events and scheduling

**Dependencies**:
- core-domain
- seguridad (for user assignments)
- clientes (for customer-related tasks)
- oportunidades (for opportunity-related tasks)

**Package Structure**:
- `com.pagodirecto.tareas.domain` - Tarea, Evento entities
- `com.pagodirecto.tareas.application` - Task management use cases
- `com.pagodirecto.tareas.infrastructure` - Repositories, calendar integrations
- `com.pagodirecto.tareas.api` - REST controllers for task operations

**Key Features**:
- Task creation and assignment
- Calendar event scheduling
- Activity tracking and logging
- Reminders and notifications

### 6. productos
**Purpose**: Product catalog, inventory management, pricing and product lifecycle

**Dependencies**:
- core-domain
- seguridad (for access control)

**Package Structure**:
- `com.pagodirecto.productos.domain` - Producto, Categoria, Precio entities
- `com.pagodirecto.productos.application` - Catalog management use cases
- `com.pagodirecto.productos.infrastructure` - Repositories, inventory systems
- `com.pagodirecto.productos.api` - REST controllers for product operations

**Key Features**:
- Product catalog management
- Category hierarchy
- Pricing strategies
- Inventory tracking

### 7. ventas
**Purpose**: Sales operations, quotes, orders, invoices and sales lifecycle management

**Dependencies**:
- core-domain
- seguridad (for user associations)
- clientes (for customer orders)
- oportunidades (for opportunity conversion)
- productos (for product details)

**Package Structure**:
- `com.pagodirecto.ventas.domain` - Cotizacion, Pedido, Factura entities
- `com.pagodirecto.ventas.application` - Sales process use cases
- `com.pagodirecto.ventas.infrastructure` - Repositories, payment integrations
- `com.pagodirecto.ventas.api` - REST controllers for sales operations

**Key Features**:
- Quote generation and approval
- Order processing
- Invoice generation
- Payment tracking

### 8. reportes
**Purpose**: Business intelligence, analytics, reporting and dashboard generation

**Dependencies**:
- core-domain
- seguridad (for access control)
- clientes (customer analytics)
- oportunidades (pipeline analytics)
- productos (inventory analytics)
- ventas (sales analytics)

**Package Structure**:
- `com.pagodirecto.reportes.domain` - Reporte, Dashboard entities
- `com.pagodirecto.reportes.application` - Report generation use cases
- `com.pagodirecto.reportes.infrastructure` - Repositories, BI integrations
- `com.pagodirecto.reportes.api` - REST controllers for report operations

**Key Features**:
- Dynamic report generation
- Dashboard configuration
- Data aggregation and analytics
- Export to multiple formats (PDF, Excel, CSV)

## Dependency Graph

```
core-domain (foundation)
    ↓
seguridad (authentication)
    ↓
    ├── clientes (customers)
    │       ↓
    │       ├── oportunidades (opportunities)
    │       │       ↓
    │       │       └── tareas (tasks) ←──┐
    │       │                              │
    │       └── tareas (tasks) ────────────┘
    │
    ├── productos (products)
    │
    └── ventas (sales) ← depends on: clientes, oportunidades, productos
            ↓
        reportes (reports) ← depends on: all business modules
```

## Architecture Layers

Each module follows the same architectural pattern:

### Domain Layer
- **Entities**: Core business objects with identity
- **Value Objects**: Immutable objects without identity
- **Domain Events**: Events representing business facts
- **Domain Services**: Business logic that doesn't belong to entities
- **Repository Interfaces**: Port definitions for data access

### Application Layer
- **Use Cases**: Application-specific business logic
- **Application Services**: Orchestration of domain operations
- **DTOs**: Data transfer objects for API contracts
- **Mappers**: Domain-to-DTO transformation (MapStruct)

### Infrastructure Layer
- **Repository Implementations**: JPA/Hibernate implementations
- **External Integrations**: Third-party service adapters
- **Configuration**: Spring configuration classes
- **Persistence Entities**: JPA entities (if separate from domain)

### API Layer (Presentation)
- **REST Controllers**: HTTP endpoints
- **Request/Response Models**: API-specific data structures
- **Exception Handlers**: Error response formatting
- **Security Configuration**: Endpoint-level authorization

## Build Configuration

### Parent POM Features
- **Spring Boot 3.2.5** with Java 17
- **Spring Security** for authentication/authorization
- **Spring Data JPA** with PostgreSQL
- **Flyway** for database migrations
- **Lombok** for boilerplate reduction
- **MapStruct** for DTO mapping
- **SpringDoc OpenAPI** for API documentation
- **Spring Actuator** for monitoring

### Common Dependencies (Inherited)
All modules inherit these dependencies from parent:
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-validation
- spring-boot-starter-actuator
- postgresql driver
- flyway-core
- lombok
- spring-boot-starter-test

### Module-Specific Dependencies
Each module adds:
- core-domain dependency
- Module-specific dependencies (e.g., JWT for seguridad)
- MapStruct for DTO mapping
- SpringDoc for API documentation

## Development Guidelines

### Adding a New Module
1. Create module directory under `backend/`
2. Create `pom.xml` with parent reference
3. Add module to parent's `<modules>` section
4. Create standard package structure:
   - `src/main/java/com/pagodirecto/{module}/{domain,application,infrastructure,api}`
   - `src/main/resources/`
   - `src/test/java/com/pagodirecto/{module}/`
   - `src/test/resources/`
5. Define module dependencies in pom.xml
6. Update this documentation

### Testing
- **Unit Tests**: Test domain logic in isolation
- **Integration Tests**: Test with Spring context and database
- **API Tests**: Test REST endpoints with MockMvc
- **Coverage Target**: >80% for business logic

### Database Migrations
Each module manages its own database schema via Flyway migrations:
- Location: `src/main/resources/db/migration/{module}/`
- Naming: `V{version}__{description}.sql`
- Example: `V1.0.0__create_clientes_tables.sql`

### API Documentation
Each module's REST endpoints are automatically documented via SpringDoc:
- Access at: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`

## Build Commands

```bash
# Build all modules
mvn clean install

# Build specific module
mvn clean install -pl seguridad

# Run tests for all modules
mvn test

# Run tests for specific module
mvn test -pl clientes

# Skip tests
mvn clean install -DskipTests

# Verify dependencies
mvn dependency:tree
```

## Module Isolation Principles

1. **No Circular Dependencies**: Modules must have acyclic dependency graph
2. **Domain Independence**: Domain layer has no infrastructure dependencies
3. **Explicit Contracts**: Inter-module communication via published interfaces
4. **Event-Driven Integration**: Use domain events for loose coupling
5. **Database Schemas**: Each module can have separate schema for isolation

## Security Considerations

- **Seguridad module** is the single source of truth for authentication
- Each module enforces its own authorization rules via Spring Security
- Row-level security implemented via PostgreSQL RLS policies
- Multi-tenant isolation enforced at database and application layers

## Performance Optimization

- **Lazy Loading**: Prevent N+1 queries with explicit JOIN FETCH
- **Caching**: Use Spring Cache abstraction for read-heavy operations
- **Connection Pooling**: HikariCP configured in application module
- **Query Optimization**: Monitor with EXPLAIN ANALYZE
- **Pagination**: Required for all list endpoints

## Monitoring and Observability

- **Spring Actuator**: Health checks, metrics, info endpoints
- **Distributed Tracing**: OpenTelemetry integration (planned)
- **Logging**: Structured JSON logs with correlation IDs
- **Metrics**: Business and technical metrics exposed via Actuator

## Next Steps

1. Implement domain entities in each module
2. Create repository interfaces and implementations
3. Define use cases in application layer
4. Build REST controllers in API layer
5. Write comprehensive tests for each layer
6. Create Flyway migrations for database schema
7. Document APIs with OpenAPI annotations
8. Configure security rules per module
9. Implement integration tests across modules
10. Set up CI/CD pipeline for automated builds
