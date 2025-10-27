# Productos Module

Enterprise product catalog management module following Clean/Hexagonal architecture and DDD principles.

## Module Structure

```
productos/
├── domain/                              # Domain Layer
│   ├── Producto.java                    # Product entity with business logic
│   └── CategoriaProducto.java           # Product category entity
│
├── application/                         # Application Layer
│   ├── dto/                             # Data Transfer Objects
│   │   ├── ProductoDTO.java             # Product response DTO
│   │   ├── CrearProductoRequest.java    # Create product request
│   │   ├── ActualizarProductoRequest.java # Update product request
│   │   ├── AjustarStockRequest.java     # Stock adjustment request
│   │   ├── CategoriaProductoDTO.java    # Category response DTO
│   │   ├── CrearCategoriaRequest.java   # Create category request
│   │   └── ActualizarCategoriaRequest.java # Update category request
│   │
│   └── service/                         # Application Services
│       ├── ProductoService.java         # Product use cases
│       └── CategoriaProductoService.java # Category use cases
│
├── infrastructure/                      # Infrastructure Layer
│   └── repository/                      # Data persistence
│       ├── ProductoRepository.java      # Product repository with custom queries
│       └── CategoriaProductoRepository.java # Category repository
│
└── presentation/                        # Presentation Layer
    └── controller/                      # REST API Controllers
        ├── ProductoController.java      # Product endpoints
        └── CategoriaProductoController.java # Category endpoints
```

## Domain Model

### Producto Entity
Aligned with V3 migration schema with the following fields:
- `id` (UUID): Primary key
- `codigo` (String): Unique product code (uppercase, alphanumeric with hyphens)
- `nombre` (String): Product name
- `descripcion` (String): Product description
- `categoria` (CategoriaProducto): Product category relationship
- `precio` (BigDecimal): Sale price
- `costo` (BigDecimal): Cost price
- `stock` (Integer): Current stock quantity
- `stockMinimo` (Integer): Minimum stock threshold
- `unidadMedida` (String): Unit of measure (e.g., "UNIDAD", "KG", "LITRO")
- `activo` (Boolean): Active status flag
- Audit fields: `createdAt`, `createdBy`, `updatedAt`, `updatedBy`, `deletedAt`

**Business Logic:**
- `hasLowStock()`: Checks if stock <= stockMinimo
- `calculateProfitMargin()`: Returns profit margin percentage
- `adjustStock(cantidad)`: Adjusts stock by quantity (validates non-negative)
- `validate()`: Validates business invariants (precio >= costo, stock >= 0)
- `softDelete()`: Implements soft delete pattern

### CategoriaProducto Entity
- `id` (UUID): Primary key
- `nombre` (String): Category name (unique)
- `descripcion` (String): Category description
- `activo` (Boolean): Active status flag
- Timestamps: `createdAt`, `updatedAt`, `deletedAt`

## Repository Queries

### ProductoRepository
- `findAllActive()`: All active products with category eagerly loaded
- `findByIdAndNotDeleted(id)`: Single product by ID (excludes soft deleted)
- `findByCodigo(codigo)`: Find by unique product code
- `findByNombreContaining(nombre)`: Case-insensitive partial name search
- `findByCategoriaId(categoriaId)`: Products by category
- `findProductosConStockBajo()`: Products with stock <= stockMinimo
- `searchProductos(nombre, categoriaId, activo)`: Advanced multi-filter search
- `existsByCodigo(codigo)`: Check code uniqueness
- `countByCategoriaId(categoriaId)`: Count products in category

### CategoriaProductoRepository
- `findAllActive()`: All active categories ordered by name
- `findByIdAndNotDeleted(id)`: Single category by ID
- `findByNombre(nombre)`: Find by category name
- `existsByNombre(nombre)`: Check name uniqueness

## REST API Endpoints

### Product Endpoints (`/api/v1/productos`)

| Method | Endpoint | Security | Description |
|--------|----------|----------|-------------|
| GET | `/` | USER, MANAGER, ADMIN | Get all active products |
| GET | `/{id}` | USER, MANAGER, ADMIN | Get product by ID |
| GET | `/codigo/{codigo}` | USER, MANAGER, ADMIN | Get product by code |
| GET | `/buscar/nombre?nombre={nombre}` | USER, MANAGER, ADMIN | Search by name |
| GET | `/categoria/{categoriaId}` | USER, MANAGER, ADMIN | Get products by category |
| GET | `/stock-bajo` | USER, MANAGER, ADMIN | Get low stock products |
| GET | `/buscar?nombre=&categoriaId=&activo=` | USER, MANAGER, ADMIN | Advanced search |
| POST | `/` | MANAGER, ADMIN | Create new product |
| PUT | `/{id}` | MANAGER, ADMIN | Update product |
| DELETE | `/{id}` | ADMIN | Soft delete product |
| PATCH | `/{id}/ajustar-stock` | MANAGER, ADMIN | Adjust stock quantity |
| PATCH | `/{id}/activar` | MANAGER, ADMIN | Activate product |
| PATCH | `/{id}/desactivar` | MANAGER, ADMIN | Deactivate product |

### Category Endpoints (`/api/v1/categorias`)

| Method | Endpoint | Security | Description |
|--------|----------|----------|-------------|
| GET | `/` | USER, MANAGER, ADMIN | Get all active categories |
| GET | `/{id}` | USER, MANAGER, ADMIN | Get category by ID |
| POST | `/` | MANAGER, ADMIN | Create new category |
| PUT | `/{id}` | MANAGER, ADMIN | Update category |
| DELETE | `/{id}` | ADMIN | Soft delete category |
| PATCH | `/{id}/activar` | MANAGER, ADMIN | Activate category |
| PATCH | `/{id}/desactivar` | MANAGER, ADMIN | Deactivate category |

## Security Model

### Role-Based Access Control (RBAC)

- **ADMIN**: Full access to all operations including deletions
- **MANAGER**: Create and update products/categories, adjust stock
- **USER**: Read-only access to products and categories

### Implementation
Security is enforced via Spring Security `@PreAuthorize` annotations on controller methods.

## Request/Response Examples

### Create Product
```json
POST /api/v1/productos
{
  "codigo": "PROD-001",
  "nombre": "Laptop Dell XPS 15",
  "descripcion": "High-performance laptop with 16GB RAM",
  "categoriaId": "550e8400-e29b-41d4-a716-446655440000",
  "precio": 1299.99,
  "costo": 899.99,
  "stock": 50,
  "stockMinimo": 10,
  "unidadMedida": "UNIDAD",
  "activo": true
}
```

### Update Product (Partial)
```json
PUT /api/v1/productos/{id}
{
  "precio": 1199.99,
  "stock": 45
}
```

### Adjust Stock
```json
PATCH /api/v1/productos/{id}/ajustar-stock
{
  "cantidad": -5,
  "motivo": "Venta realizada"
}
```

### Create Category
```json
POST /api/v1/categorias
{
  "nombre": "Electrónica",
  "descripcion": "Productos electrónicos y computadoras",
  "activo": true
}
```

### Product Response
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "codigo": "PROD-001",
  "nombre": "Laptop Dell XPS 15",
  "descripcion": "High-performance laptop with 16GB RAM",
  "categoria": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "nombre": "Electrónica",
    "descripcion": "Productos electrónicos y computadoras",
    "activo": true,
    "createdAt": "2025-01-15T10:30:00",
    "updatedAt": "2025-01-15T10:30:00"
  },
  "precio": 1299.99,
  "costo": 899.99,
  "stock": 50,
  "stockMinimo": 10,
  "unidadMedida": "UNIDAD",
  "activo": true,
  "stockBajo": false,
  "margenBeneficio": 30.77,
  "createdAt": "2025-01-15T10:30:00",
  "createdBy": "user-uuid",
  "updatedAt": "2025-01-15T10:30:00",
  "updatedBy": "user-uuid"
}
```

## Validation Rules

### Product Creation
- `codigo`: Required, 1-50 chars, uppercase alphanumeric + hyphens, unique
- `nombre`: Required, 2-200 chars
- `descripcion`: Optional, max 1000 chars
- `categoriaId`: Required, must reference existing category
- `precio`: Required, > 0, max 10 integer digits + 2 decimals
- `costo`: Required, > 0, max 10 integer digits + 2 decimals
- `stock`: Required, >= 0
- `stockMinimo`: Required, >= 0
- `unidadMedida`: Required, max 20 chars
- Business rule: `precio >= costo`

### Category Creation
- `nombre`: Required, 2-100 chars, unique
- `descripcion`: Optional, max 500 chars
- `activo`: Defaults to true

## Business Rules

1. **Unique Product Code**: Product `codigo` must be unique across active products
2. **Price Validation**: Sale price must be >= cost price
3. **Stock Validation**: Stock cannot be negative after adjustments
4. **Category Deletion**: Cannot delete categories with associated products
5. **Low Stock Alert**: Products with `stock <= stockMinimo` are flagged
6. **Soft Delete**: All deletions are soft deletes (sets `deletedAt` timestamp)
7. **Audit Trail**: All modifications tracked via `createdBy`/`updatedBy` fields

## Database Schema Alignment

This module is fully aligned with the V3 migration schema:

**Table: `productos`**
- Indexes on: `codigo`, `categoria_id`, `activo`
- Foreign key to `productos_categorias`
- Audit fields for compliance

**Table: `productos_categorias`**
- Soft delete support
- Timestamp tracking

## Error Handling

Common error scenarios:
- **404 Not Found**: Entity doesn't exist or is soft deleted
- **409 Conflict**: Duplicate `codigo` or category `nombre`
- **422 Validation Error**: Invalid input data
- **403 Forbidden**: Insufficient permissions
- **400 Bad Request**: Business rule violation (e.g., precio < costo, negative stock)

## Integration Points

### Required Dependencies
- Spring Boot Starter Data JPA
- Spring Boot Starter Validation
- Spring Boot Starter Security
- PostgreSQL Driver
- Lombok
- Jakarta Persistence API

### Security Context
Services extract current user ID from `SecurityContextHolder` for audit fields.

## Future Enhancements

- [ ] Pagination support for list endpoints
- [ ] Product image management
- [ ] Inventory movement history tracking
- [ ] Bulk import/export capabilities
- [ ] Advanced reporting (top products, stock valuation)
- [ ] Product variants/SKU management
- [ ] Supplier integration
- [ ] Price history tracking
- [ ] Barcode generation/scanning

## Testing Recommendations

### Unit Tests
- Domain entity business logic (validate, hasLowStock, calculateProfitMargin)
- Service layer use cases
- DTO conversion methods

### Integration Tests
- Repository custom queries
- Controller endpoints with security
- Database constraints and transactions

### Performance Tests
- Search queries with large datasets
- Concurrent stock adjustments
- Bulk product operations

## Monitoring

Key metrics to track:
- Product creation/update rates
- Low stock alerts frequency
- Search query performance
- Stock adjustment patterns
- Category distribution

---

**Module Status**: Production Ready
**Last Updated**: 2025-10-20
**Maintainer**: Chief Systems Engineer
