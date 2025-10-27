# CRM Backend - Spring Boot 3

Backend del sistema CRM/ERP construido como **monolito modular** con Spring Boot 3.

## Inicio Rápido

```bash
# Levantar PostgreSQL
docker-compose up postgres -d

# Ejecutar aplicación
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Acceder a Swagger
# http://localhost:8080/swagger-ui.html
```

## Estructura del Proyecto

```
src/main/java/com/empresa/crm/
├── CrmApplication.java          # Main application
├── shared/                      # Shared components
│   ├── config/                  # Security, OpenAPI, Web config
│   ├── exception/               # Global exception handling
│   ├── security/                # JWT utilities
│   └── util/                    # Common utilities
├── seguridad/                   # Authentication module
├── clientes/                    # Customers module
├── productos/                   # Products & categories module
├── ventas/                      # Sales orders module
├── pagos/                       # Payments module
├── cuentas/                     # Accounts receivable/payable module
└── reportes/                    # Reports & analytics module
```

## Módulos Implementados

| Módulo | Entities | Endpoints | Estado |
|--------|----------|-----------|--------|
| **Seguridad** | Usuario, Rol | `/api/v1/auth/**` | ✅ Completo |
| **Clientes** | Cliente | `/api/v1/clientes/**` | ✅ Completo |
| **Productos** | Producto, CategoriaProducto | `/api/v1/productos/**`, `/api/v1/categorias/**` | ✅ Completo |
| **Ventas** | Venta, DetalleVenta | `/api/v1/ventas/**` | ✅ Completo |
| **Pagos** | Pago | `/api/v1/pagos/**` | ✅ Completo |
| **Cuentas** | Cuenta | `/api/v1/cuentas/**` | ✅ Completo |
| **Reportes** | - | `/api/v1/reportes/**` | ✅ Completo |

## Comandos Maven

```bash
# Compilar
./mvnw clean compile

# Ejecutar tests
./mvnw test

# Package JAR
./mvnw clean package

# Ejecutar aplicación
./mvnw spring-boot:run

# Flyway migrations
./mvnw flyway:migrate
./mvnw flyway:info
```

## API Documentation

**Swagger UI**: http://localhost:8080/swagger-ui.html
**OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Autenticación

1. POST `/api/v1/auth/login`
```json
{
  "username": "admin",
  "password": "admin123"
}
```

2. Copiar el `token` de la respuesta

3. En Swagger: Click "Authorize" → Ingresar: `Bearer {token}`

## Database

**PostgreSQL 15** con **Flyway** para migraciones.

### Migraciones

```
src/main/resources/db/migration/
├── V1__init_schema.sql          # Usuarios, roles
├── V2__create_clientes.sql       # Clientes
├── V3__create_productos.sql      # Productos, categorías
├── V4__create_ventas.sql         # Ventas, detalles
├── V5__create_pagos.sql          # Pagos
└── V6__create_cuentas.sql        # Cuentas por cobrar/pagar
```

### Conectar a PostgreSQL

```bash
docker-compose exec postgres psql -U crm_user -d crm_db
```

## Seguridad

### Roles

- **ROLE_ADMIN**: Full access
- **ROLE_MANAGER**: Create/update Clientes, Productos, Ventas
- **ROLE_FINANCE**: Full access Pagos y Cuentas
- **ROLE_USER**: Read-only

### JWT Configuration

```yaml
jwt:
  secret: "tu-secreto-minimo-256-bits"
  expiration: 86400000  # 24 horas
```

## Testing

```bash
# Todos los tests
./mvnw test

# Test específico
./mvnw test -Dtest=ClienteServiceTest

# Con coverage
./mvnw verify
```

## Build Production

```bash
# Build JAR
./mvnw clean package -DskipTests

# Ejecutar JAR
java -jar target/crm-backend-1.0.0.jar --spring.profiles.active=prod
```

## Docker

```bash
# Build imagen
docker build -t crm-backend:latest .

# Run container
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:postgresql://host:5432/crm_db \
  crm-backend:latest
```

## Tecnologías

- Java 17
- Spring Boot 3.2.1
- Spring Data JPA
- Spring Security + JWT
- PostgreSQL 15
- Flyway
- Lombok
- SpringDoc OpenAPI 3.0

---

**Generado con Claude Code** 🤖
