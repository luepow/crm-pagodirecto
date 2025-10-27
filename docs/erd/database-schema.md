# PagoDirecto CRM/ERP - Database Schema Documentation

**Version:** 1.0.0
**Date:** 2025-10-13
**Database Engine:** PostgreSQL 16+
**Author:** Database Architecture Team

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Domain Contexts](#domain-contexts)
3. [Common Patterns](#common-patterns)
4. [Entity Relationship Diagram](#entity-relationship-diagram)
5. [Domain Details](#domain-details)
6. [Security & Multi-Tenancy](#security--multi-tenancy)
7. [Performance Optimization](#performance-optimization)
8. [Maintenance & Operations](#maintenance--operations)

---

## Architecture Overview

### Design Principles

The database schema follows **Clean/Hexagonal Architecture** and **Domain-Driven Design (DDD)** principles:

- **Bounded Contexts**: Each business domain is isolated with clear boundaries
- **Multi-Tenant Architecture**: Row-Level Security (RLS) ensures data isolation per `unidad_negocio_id`
- **Audit Trail**: Complete traceability with `created_at`, `created_by`, `updated_at`, `updated_by`
- **Soft Delete**: Records are marked as deleted (`deleted_at`) rather than physically removed
- **UUID Primary Keys**: Distributed-friendly identifiers for all entities
- **ACID Compliance**: Transactional integrity with proper constraints and foreign keys

### Technology Stack

- **PostgreSQL 16+**: Primary relational database
- **Extensions**: `pgcrypto` for UUID generation
- **Migration Tool**: Flyway for versioned schema migrations
- **Connection Pool**: HikariCP (recommended 20-50 connections)
- **Backup Strategy**: Daily full backups + WAL archiving for PITR

---

## Domain Contexts

The database is organized into 7 bounded contexts:

| Domain | Prefix | Description | Tables |
|--------|--------|-------------|--------|
| **Seguridad** | `seguridad_*` | Security, IAM, audit logging | 7 tables |
| **Clientes** | `clientes_*` | Clients, contacts, addresses | 3 tables |
| **Oportunidades** | `oportunidades_*` | Sales pipeline, opportunities | 3 tables |
| **Tareas** | `tareas_*` | Tasks, activities, comments | 2 tables |
| **Productos** | `productos_*` | Product catalog, categories, pricing | 3 tables |
| **Ventas** | `ventas_*` | Quotes, orders, line items | 4 tables |
| **Reportes** | `reportes_*` | Dashboards, widgets, analytics | 2 tables |

**Total Tables:** 24 core business tables

---

## Common Patterns

### Standard Audit Columns

Every table includes these columns for traceability:

```sql
created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
created_by UUID,
updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updated_by UUID,
deleted_at TIMESTAMPTZ
```

- **created_at**: Timestamp when record was created (immutable)
- **created_by**: UUID of user who created the record
- **updated_at**: Timestamp of last update (auto-updated via trigger)
- **updated_by**: UUID of user who last updated the record
- **deleted_at**: Soft delete timestamp (NULL = active record)

### Multi-Tenant Isolation

Most tables include:

```sql
unidad_negocio_id UUID NOT NULL
```

This field combined with **Row-Level Security (RLS)** policies ensures complete data isolation between tenants.

### Foreign Key Naming Convention

```
fk_<source_table>_<referenced_table>
```

Example: `fk_clientes_contactos_cliente`

### Index Naming Convention

```
idx_<table>_<columns>_<purpose>
uk_<table>_<columns>  -- unique index
```

Example:
- `idx_clientes_clientes_nombre` (search index)
- `uk_clientes_clientes_codigo` (unique constraint)

### Check Constraint Naming Convention

```
chk_<table>_<condition>
```

Example: `chk_ventas_cotizaciones_status`

---

## Entity Relationship Diagram

### High-Level Domain View

```
┌─────────────────┐
│   SEGURIDAD     │
│  (Security/IAM) │
└────────┬────────┘
         │
         │ created_by/updated_by (all tables)
         │
         ▼
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  ┌───────────┐      ┌──────────────┐      ┌──────────┐     │
│  │ CLIENTES  │──────│ OPORTUNIDADES│──────│  TAREAS  │     │
│  │  (CRM)    │      │  (Pipeline)  │      │          │     │
│  └─────┬─────┘      └──────┬───────┘      └──────────┘     │
│        │                   │                                │
│        │                   │                                │
│        │                   ▼                                │
│        │            ┌──────────────┐                        │
│        └───────────▶│    VENTAS    │◀───────────────┐       │
│                     │ (Quotes/Orders)                │       │
│                     └──────┬───────┘                 │       │
│                            │                         │       │
│                            ▼                         │       │
│                     ┌──────────────┐                 │       │
│                     │  PRODUCTOS   │─────────────────┘       │
│                     │  (Catalog)   │                         │
│                     └──────────────┘                         │
│                                                              │
│  ┌──────────────────────────────────────────────┐           │
│  │          REPORTES (Dashboards)               │           │
│  └──────────────────────────────────────────────┘           │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## Domain Details

### 1. SEGURIDAD (Security & IAM)

#### Tables

**seguridad_usuarios** (Users)
- Primary authentication table
- Supports MFA (Multi-Factor Authentication) via `mfa_secret`
- Account lockout mechanism (`intentos_fallidos`, `bloqueado_hasta`)
- Password stored as bcrypt hash (cost >= 12)

**seguridad_roles** (Roles)
- Hierarchical roles by department
- `nivel_jerarquico`: 0 (highest) to 10 (lowest)
- Examples: Admin, Sales Manager, Sales Rep, Warehouse Manager

**seguridad_permisos** (Permissions)
- Granular CRUD permissions per resource
- Actions: CREATE, READ, UPDATE, DELETE, EXECUTE, ADMIN
- Scopes: API-level access control (e.g., `sales:write`, `reports:read`)

**seguridad_roles_permisos** (Role-Permission Mapping)
- Many-to-many relationship

**seguridad_usuarios_roles** (User-Role Mapping)
- Many-to-many relationship
- Supports role expiration (`fecha_expiracion`)

**seguridad_refresh_tokens** (JWT Refresh Tokens)
- Token stored as SHA-256 hash
- TTL: 30 days typical
- Revocation support (`revocado`, `revocado_at`)
- Automatic cleanup of expired tokens

**seguridad_audit_log** (Audit Trail)
- **IMMUTABLE**: No updates or deletes allowed (enforced by RLS)
- 7-year retention for financial compliance
- JSONB metadata for flexible event data
- Indexes for fast querying by user, resource, action, date

#### Key Relationships

```
seguridad_usuarios ──┬──▶ seguridad_usuarios_roles ──▶ seguridad_roles ──▶ seguridad_roles_permisos ──▶ seguridad_permisos
                     │
                     └──▶ seguridad_refresh_tokens
                     │
                     └──▶ seguridad_audit_log
```

#### Security Features

- **Password Policy**: Enforced at application level (min 12 chars, complexity)
- **MFA**: TOTP-based two-factor authentication
- **Account Lockout**: 5 failed attempts → 30 minutes lockout
- **Session Management**: JWT access tokens (5 min TTL) + refresh tokens (30 days)

---

### 2. CLIENTES (Clients/CRM)

#### Tables

**clientes_clientes** (Clients)
- Core client information
- Types: PERSONA (individual), EMPRESA (company)
- Status: ACTIVE, INACTIVE, PROSPECT, LEAD, BLACKLIST
- Segmentation: CORPORATIVO, PYME, RETAIL
- Source tracking: WEBSITE, REFERRAL, CAMPAIGN
- Ownership: `propietario_id` → sales representative

**clientes_contactos** (Contacts)
- Multiple contacts per client
- Primary contact flag (`is_primary`)
- Position and department tracking

**clientes_direcciones** (Addresses)
- Multiple addresses per client
- Types: FISCAL (billing), ENVIO (shipping), OTRO (other)
- Default address per type (`is_default`)
- Full Mexican address format

#### Key Relationships

```
clientes_clientes ──┬──▶ clientes_contactos
                    │
                    └──▶ clientes_direcciones
                    │
                    └──▶ oportunidades_oportunidades
                    │
                    └──▶ ventas_cotizaciones
                    │
                    └──▶ ventas_pedidos
```

#### Business Rules

- **Unique Code**: Client code must be unique per tenant
- **RFC Validation**: Mexican tax ID format validation
- **Email Validation**: RFC 5322 email format
- **Primary Contact**: Enforced at application level (exactly one per client)
- **Default Address**: Enforced at application level (exactly one per type)

---

### 3. OPORTUNIDADES (Opportunities/Pipeline)

#### Tables

**oportunidades_etapas_pipeline** (Pipeline Stages)
- Configurable sales stages
- Types: LEAD → QUALIFIED → PROPOSAL → NEGOTIATION → CLOSED_WON / CLOSED_LOST
- Default probability per stage (0-100%)
- Ordering: Sequential stage progression

**oportunidades_oportunidades** (Opportunities)
- Sales opportunities in pipeline
- Estimated value and currency
- Probability of closure (0-100%)
- Estimated and actual close dates
- Loss reason tracking (`motivo_perdida`)

**oportunidades_actividades** (Opportunity Activities)
- Activity log per opportunity
- Types: LLAMADA, REUNION, EMAIL, TAREA, NOTA, OTRO
- Duration tracking (minutes)
- Completion status and results

#### Key Relationships

```
oportunidades_etapas_pipeline ←──── oportunidades_oportunidades ──┬──▶ oportunidades_actividades
                                                                   │
clientes_clientes ◀─────────────────────────────────────────────────┘
                                                                   │
seguridad_usuarios (propietario) ◀─────────────────────────────────┘
```

#### Business Rules

- **Probability Validation**: 0-100% range
- **Stage Progression**: Typically moves forward (can move backward for re-qualification)
- **Currency Support**: Multi-currency (MXN, USD, EUR) via ISO 4217 codes
- **Activity Tracking**: Complete history of interactions

---

### 4. TAREAS (Tasks/Activities)

#### Tables

**tareas_tareas** (Tasks)
- General-purpose task management
- Types: LLAMADA, EMAIL, REUNION, SEGUIMIENTO, ADMINISTRATIVA, TECNICA, OTRA
- Priority: BAJA, MEDIA, ALTA, URGENTE
- Status: PENDIENTE, EN_PROGRESO, COMPLETADA, CANCELADA, BLOQUEADA
- **Polymorphic Relationship**: `relacionado_tipo` + `relacionado_id`
  - Can link to CLIENTE, OPORTUNIDAD, PEDIDO, etc.

**tareas_comentarios** (Task Comments)
- Discussion thread per task
- Chronological comment history

#### Key Relationships

```
tareas_tareas ──┬──▶ tareas_comentarios
                │
                └──▶ [ANY ENTITY] (polymorphic via relacionado_tipo/relacionado_id)
                │
seguridad_usuarios (asignado_a) ◀──┘
```

#### Business Rules

- **Assignment**: Every task must be assigned to a user
- **Due Date**: Optional deadline for task completion
- **Polymorphic Links**: Flexible association with any business entity
- **Comment Threading**: Full discussion history preserved

---

### 5. PRODUCTOS (Products/Catalog)

#### Tables

**productos_categorias** (Product Categories)
- Hierarchical tree structure (up to 5 levels)
- Self-referencing: `parent_id` → `productos_categorias.id`
- Path materialization for fast tree queries
- Ordering within level

**productos_productos** (Products)
- Core product catalog
- Types: PRODUCTO, SERVICIO, COMBO
- Status: ACTIVE, INACTIVE, DISCONTINUED
- Inventory tracking: `stock_actual`, `stock_minimo`
- Cost and pricing: `costo_unitario`, `precio_base`
- SKU, barcode, and image support

**productos_precios** (Product Prices)
- Differential pricing strategies
- Types: LISTA, MAYOREO, DISTRIBUIDOR, PROMOCION
- Date range validity (`fecha_inicio`, `fecha_fin`)
- Customer segment targeting
- Volume pricing: `cantidad_minima`

#### Key Relationships

```
productos_categorias ──▶ productos_categorias (self-reference)
                │
                └──▶ productos_productos ──▶ productos_precios
                                        │
                                        └──▶ ventas_items_cotizacion
                                        │
                                        └──▶ ventas_items_pedido
```

#### Business Rules

- **Unique Code**: Product code unique per tenant
- **SKU/Barcode**: Unique identifiers for inventory systems
- **Stock Alerts**: `stock_actual <= stock_minimo` triggers reorder
- **Price Validity**: Date-based pricing with overlapping periods allowed
- **Multi-Currency**: Prices can be in any currency (ISO 4217)

---

### 6. VENTAS (Sales/Quotes)

#### Tables

**ventas_cotizaciones** (Quotes)
- Sales quotations sent to clients
- Status: BORRADOR, ENVIADA, ACEPTADA, RECHAZADA, EXPIRADA
- Validity period: `fecha_validez`
- Financial summary: `subtotal`, `impuestos`, `total`
- Global discount support
- Links to opportunity

**ventas_items_cotizacion** (Quote Line Items)
- Product lines in quote
- Quantity, unit price, discount
- Line-level tax calculation
- Ordering: Display sequence

**ventas_pedidos** (Orders)
- Confirmed sales orders
- Status: PENDIENTE, CONFIRMADO, EN_PROCESO, ENVIADO, ENTREGADO, CANCELADO, DEVUELTO
- Delivery tracking: `fecha_entrega_estimada`, `fecha_entrega_real`
- Payment terms: CONTADO, 30 DIAS, 60 DIAS
- Links to quote (optional)

**ventas_items_pedido** (Order Line Items)
- Product lines in order
- Partial delivery support: `cantidad_entregada`
- Mirrors structure of quote items

#### Key Relationships

```
clientes_clientes ◀────────┬──── ventas_cotizaciones ──┬──▶ ventas_items_cotizacion
                           │                           │
oportunidades_oportunidades◀┘                           └──▶ productos_productos
                                                        │
                                                        └──▶ ventas_pedidos ──┬──▶ ventas_items_pedido
                                                                               │
                                                                               └──▶ productos_productos
```

#### Business Rules

- **Quote → Order Conversion**: `ventas_pedidos.cotizacion_id` links back to original quote
- **Quote Numbering**: Auto-generated (e.g., COT-2025-00001)
- **Order Numbering**: Auto-generated (e.g., PED-2025-00001)
- **Financial Integrity**:
  - `subtotal = SUM(items.subtotal)`
  - `total = subtotal - descuento_global + impuestos`
- **Partial Deliveries**: `cantidad_entregada` tracks progress
- **Tax Calculation**: Supports multiple tax rates per line item

---

### 7. REPORTES (Reports/Analytics)

#### Tables

**reportes_dashboards** (Dashboards)
- Customizable dashboard definitions
- JSONB configuration for layout, filters
- Public or private (user-owned)
- Ordering for display

**reportes_widgets** (Dashboard Widgets)
- Individual chart/table/KPI components
- Types: CHART, TABLE, KPI, MAP, LIST, CALENDAR, CUSTOM
- JSONB configuration for queries, styling
- Positioning: `{x, y, width, height}` grid layout

#### Key Relationships

```
seguridad_usuarios (propietario) ──▶ reportes_dashboards ──▶ reportes_widgets
```

#### Business Rules

- **Public Dashboards**: Visible to all users in tenant
- **Private Dashboards**: Only owner and admins can access
- **Widget Types**: Extensible via JSON configuration
- **Data Sources**: Widgets query any business entity

---

## Security & Multi-Tenancy

### Row-Level Security (RLS)

All tables have RLS policies enforcing:

1. **Tenant Isolation**: Users only see data from their `unidad_negocio_id`
2. **Ownership Control**: Users see records they own or are assigned to
3. **Role-Based Access**: Admins and managers have broader visibility
4. **Bypass for Reports**: Admin users can bypass RLS for global analytics

### Session Context

The application sets these session variables for each request:

```sql
-- Set context at start of request
SELECT set_app_session_context(
    '123e4567-e89b-12d3-a456-426614174000'::UUID,  -- tenant_id
    '987fcdeb-51a2-43e7-b456-426614174000'::UUID,  -- user_id
    'SALES,SALES_MANAGER',                         -- roles (comma-separated)
    FALSE                                          -- bypass_rls (admin only)
);

-- Clear context at end of request
SELECT clear_app_session_context();
```

### Policy Examples

**Tenant Isolation:**
```sql
CREATE POLICY tenant_isolation_clientes_clientes ON clientes_clientes
    FOR SELECT
    USING (unidad_negocio_id = app_current_tenant() OR app_bypass_rls());
```

**Ownership Control:**
```sql
CREATE POLICY owner_access_oportunidades_oportunidades ON oportunidades_oportunidades
    FOR SELECT
    USING (
        propietario_id = app_current_user()
        OR app_user_has_role('ADMIN')
        OR app_user_has_role('SALES_MANAGER')
    );
```

**Immutable Audit Log:**
```sql
CREATE POLICY immutable_seguridad_audit_log ON seguridad_audit_log
    FOR UPDATE
    USING (FALSE);  -- No updates allowed
```

### Role Hierarchy

```
ADMIN
  └─▶ SALES_MANAGER
       └─▶ SALES (Sales Rep)

  └─▶ FINANCE_MANAGER (CFO)
       └─▶ FINANCE

  └─▶ WAREHOUSE_MANAGER
       └─▶ WAREHOUSE

  └─▶ USER (Basic)
```

---

## Performance Optimization

### Indexing Strategy

#### 1. Foreign Key Indexes
All foreign keys have indexes for fast joins:
```sql
CREATE INDEX idx_clientes_contactos_cliente ON clientes_contactos(cliente_id);
```

#### 2. Search Indexes
Common search fields indexed with partial indexes for active records:
```sql
CREATE INDEX idx_clientes_clientes_nombre
ON clientes_clientes(LOWER(nombre))
WHERE deleted_at IS NULL;
```

#### 3. Composite Indexes
Multi-column indexes for common query patterns:
```sql
CREATE INDEX idx_ventas_pedidos_propietario_fecha
ON ventas_pedidos(propietario_id, fecha DESC, status)
WHERE deleted_at IS NULL;
```

#### 4. Covering Indexes
Include columns for index-only scans:
```sql
CREATE INDEX idx_oportunidades_oportunidades_etapa_valor
ON oportunidades_oportunidades(etapa_id, valor_estimado, probabilidad)
WHERE deleted_at IS NULL;
```

#### 5. GIN Indexes
For JSONB columns:
```sql
CREATE INDEX idx_seguridad_audit_log_metadata
ON seguridad_audit_log USING gin(metadata);
```

#### 6. Partial Indexes
For filtered queries:
```sql
CREATE INDEX idx_productos_productos_stock_bajo
ON productos_productos(stock_actual)
WHERE deleted_at IS NULL
  AND status = 'ACTIVE'
  AND stock_actual <= stock_minimo;
```

### Query Optimization Tips

1. **Avoid SELECT * **: Specify only needed columns
2. **Use EXPLAIN ANALYZE**: Review query plans regularly
3. **Leverage Covering Indexes**: Reduce heap lookups
4. **Batch Operations**: Use `INSERT ... VALUES (...), (...)` for bulk inserts
5. **Connection Pooling**: Use HikariCP with 20-50 connections
6. **Prepared Statements**: Prevent SQL injection + plan caching

### Expected Performance (p95 Latency)

| Query Type | Expected Latency |
|------------|------------------|
| Primary key lookup | <5ms |
| Index scan (1000 rows) | <50ms |
| Join query (2-3 tables) | <100ms |
| Aggregation (10K rows) | <200ms |
| Report query (100K rows) | <2s |

---

## Maintenance & Operations

### Backup Strategy

**Daily Full Backups:**
```bash
pg_dump -Fc -d crm_db -f backup_$(date +%Y%m%d).dump
```

**Continuous WAL Archiving:**
```
wal_level = replica
archive_mode = on
archive_command = 'cp %p /backup/wal/%f'
```

**Point-in-Time Recovery (PITR):**
```bash
pg_basebackup -D /backup/base -Fp -Xs -P
```

### Routine Maintenance

**Vacuum Full (Monthly):**
```sql
VACUUM FULL ANALYZE;
```

**Reindex (Quarterly):**
```sql
REINDEX DATABASE crm_db;
```

**Update Statistics:**
```sql
ANALYZE;
```

**Cleanup Old Audit Logs (7 years):**
```sql
DELETE FROM seguridad_audit_log
WHERE created_at < NOW() - INTERVAL '7 years';
```

**Cleanup Expired Tokens (Daily):**
```sql
DELETE FROM seguridad_refresh_tokens
WHERE expires_at < NOW() AND revocado = FALSE;
```

### Monitoring Queries

**Active Connections:**
```sql
SELECT count(*) FROM pg_stat_activity WHERE state = 'active';
```

**Long-Running Queries:**
```sql
SELECT pid, now() - pg_stat_activity.query_start AS duration, query
FROM pg_stat_activity
WHERE state = 'active'
  AND now() - pg_stat_activity.query_start > interval '5 minutes';
```

**Table Sizes:**
```sql
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

**Index Usage:**
```sql
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan,
    pg_size_pretty(pg_relation_size(indexrelid)) AS size
FROM pg_stat_user_indexes
ORDER BY idx_scan ASC, pg_relation_size(indexrelid) DESC;
```

**Cache Hit Ratio (should be >99%):**
```sql
SELECT
    sum(heap_blks_hit) / (sum(heap_blks_hit) + sum(heap_blks_read)) AS cache_hit_ratio
FROM pg_statio_user_tables;
```

### Schema Migration Checklist

Before deploying migrations:

- [ ] Backup database
- [ ] Test migration on staging environment
- [ ] Review EXPLAIN ANALYZE for impacted queries
- [ ] Check migration time estimate (keep under 5 minutes)
- [ ] Verify backward compatibility (for zero-downtime deploys)
- [ ] Document rollback procedure
- [ ] Schedule during maintenance window if needed
- [ ] Monitor connection pool after deployment

### Troubleshooting

**Deadlocks:**
```sql
-- View deadlock logs
SELECT * FROM pg_stat_database_conflicts WHERE datname = 'crm_db';

-- Kill blocking query
SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE pid = <blocking_pid>;
```

**Slow Queries:**
```sql
-- Enable slow query logging
ALTER DATABASE crm_db SET log_min_duration_statement = 1000; -- 1 second

-- View slow queries
SELECT * FROM pg_stat_statements ORDER BY total_exec_time DESC LIMIT 10;
```

**Bloat:**
```sql
-- Check table bloat
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS total_size,
    round(100 * pg_relation_size(schemaname||'.'||tablename) /
          pg_total_relation_size(schemaname||'.'||tablename)) AS table_pct
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

---

## Appendix

### Flyway Migration Files

1. **V1__initial_schema.sql**: All table DDL with constraints and comments
2. **V2__add_indexes.sql**: All indexes (unique, composite, partial, GIN)
3. **V3__add_rls_policies.sql**: Row-Level Security policies and helper functions

### Extension Requirements

```sql
-- Required extensions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";  -- UUID generation
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";  -- Query monitoring (optional)
```

### Connection String Format

```
postgresql://app_user:secure_password@localhost:5432/crm_db?sslmode=require
```

### Environment Variables

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=crm_db
DB_USER=app_user
DB_PASSWORD=secure_password
DB_POOL_SIZE=20
DB_MAX_LIFETIME=1800000  # 30 minutes
```

---

## Changelog

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0.0 | 2025-10-13 | DB Architecture Team | Initial schema release |

---

## Contact & Support

For questions or issues:
- **Database Team**: dba@pagodirecto.com
- **Documentation**: https://docs.pagodirecto.com/database
- **Issue Tracker**: https://jira.pagodirecto.com/database

---

**END OF DOCUMENT**
