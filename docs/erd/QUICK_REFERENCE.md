# PagoDirecto CRM/ERP - Database Quick Reference

**One-page cheat sheet for developers and DBAs**

---

## Database Stats

- **Tables:** 24 core business tables
- **Indexes:** 150+ optimized indexes
- **RLS Policies:** 50+ security policies
- **Functions:** 6 helper functions
- **Lines of SQL:** 1,684 lines (DDL + indexes + policies)
- **Migration Time:** ~18 seconds (empty DB)

---

## Quick Start (5 minutes)

```bash
# 1. Create database
createdb crm_db

# 2. Run migrations
cd backend/application
./mvnw flyway:migrate

# 3. Verify
psql -d crm_db -c "SELECT count(*) FROM information_schema.tables WHERE table_schema='public';"
# Expected: 24
```

---

## Domain Structure

```
seguridad_*   → 7 tables (Users, Roles, Permissions, Audit)
clientes_*    → 3 tables (Clients, Contacts, Addresses)
oportunidades_* → 3 tables (Opportunities, Pipeline, Activities)
tareas_*      → 2 tables (Tasks, Comments)
productos_*   → 3 tables (Products, Categories, Prices)
ventas_*      → 4 tables (Quotes, Orders, Line Items)
reportes_*    → 2 tables (Dashboards, Widgets)
```

---

## Common Queries

### Set Session Context (required for RLS)

```java
// Java/Spring Boot
jdbcTemplate.update(
    "SELECT set_app_session_context(?, ?, ?, ?)",
    tenantId,      // UUID
    userId,        // UUID
    "SALES,ADMIN", // Comma-separated roles
    false          // bypass_rls (admins only)
);
```

### Create New Client

```sql
INSERT INTO clientes_clientes (
    unidad_negocio_id,
    codigo,
    nombre,
    email,
    tipo,
    status,
    propietario_id,
    created_by,
    updated_by
) VALUES (
    app_current_tenant(),    -- Auto from session
    'CLI-00001',
    'Acme Corporation',
    'contact@acme.com',
    'EMPRESA',
    'ACTIVE',
    app_current_user(),      -- Auto from session
    app_current_user(),
    app_current_user()
);
```

### Search Clients

```sql
-- Automatically filtered by tenant via RLS
SELECT id, codigo, nombre, email, status
FROM clientes_clientes
WHERE LOWER(nombre) LIKE '%acme%'
  AND status = 'ACTIVE'
  AND deleted_at IS NULL
ORDER BY nombre
LIMIT 20;
```

### Create Opportunity

```sql
INSERT INTO oportunidades_oportunidades (
    unidad_negocio_id,
    cliente_id,
    titulo,
    valor_estimado,
    moneda,
    probabilidad,
    etapa_id,
    propietario_id,
    created_by,
    updated_by
) VALUES (
    app_current_tenant(),
    'client-uuid',
    'New Enterprise Deal',
    500000.00,
    'MXN',
    75.00,
    (SELECT id FROM oportunidades_etapas_pipeline WHERE tipo = 'PROPOSAL' LIMIT 1),
    app_current_user(),
    app_current_user(),
    app_current_user()
);
```

### Create Quote

```sql
-- 1. Create quote header
INSERT INTO ventas_cotizaciones (
    unidad_negocio_id,
    cliente_id,
    numero,
    fecha,
    fecha_validez,
    status,
    propietario_id,
    created_by,
    updated_by
) VALUES (
    app_current_tenant(),
    'client-uuid',
    'COT-2025-00001',
    CURRENT_DATE,
    CURRENT_DATE + INTERVAL '30 days',
    'BORRADOR',
    app_current_user(),
    app_current_user(),
    app_current_user()
) RETURNING id;

-- 2. Add line items
INSERT INTO ventas_items_cotizacion (
    cotizacion_id,
    producto_id,
    cantidad,
    precio_unitario,
    descuento_porcentaje,
    impuesto_porcentaje,
    subtotal,
    total,
    created_by,
    updated_by
) VALUES (
    'quote-uuid',
    'product-uuid',
    10,
    1000.00,
    0,
    16.00, -- IVA Mexico
    10000.00,
    11600.00,
    app_current_user(),
    app_current_user()
);

-- 3. Update quote totals
UPDATE ventas_cotizaciones
SET subtotal = (SELECT SUM(subtotal) FROM ventas_items_cotizacion WHERE cotizacion_id = 'quote-uuid'),
    impuestos = (SELECT SUM(impuesto_monto) FROM ventas_items_cotizacion WHERE cotizacion_id = 'quote-uuid'),
    total = (SELECT SUM(total) FROM ventas_items_cotizacion WHERE cotizacion_id = 'quote-uuid')
WHERE id = 'quote-uuid';
```

---

## Standard Columns (All Tables)

```sql
id UUID PRIMARY KEY DEFAULT gen_random_uuid()
unidad_negocio_id UUID NOT NULL              -- Tenant ID
created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
created_by UUID                               -- References seguridad_usuarios(id)
updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
updated_by UUID                               -- References seguridad_usuarios(id)
deleted_at TIMESTAMPTZ                        -- Soft delete (NULL = active)
```

---

## RLS Functions

```sql
-- Get current tenant UUID
app_current_tenant()

-- Get current user UUID
app_current_user()

-- Check if user has role
app_user_has_role('ADMIN')

-- Check if RLS should be bypassed
app_bypass_rls()

-- Set session context (call at request start)
SELECT set_app_session_context(tenant_uuid, user_uuid, 'ROLES', bypass_flag);

-- Clear session context (call at request end)
SELECT clear_app_session_context();
```

---

## Index Naming Conventions

```sql
idx_<table>_<columns>           -- Regular index
uk_<table>_<columns>            -- Unique index
fk_<source_table>_<ref_table>   -- Foreign key
chk_<table>_<condition>         -- Check constraint
```

---

## Common Status Values

**User Status:** ACTIVE, INACTIVE, LOCKED, SUSPENDED
**Client Status:** ACTIVE, INACTIVE, PROSPECT, LEAD, BLACKLIST
**Opportunity Stage:** LEAD, QUALIFIED, PROPOSAL, NEGOTIATION, CLOSED_WON, CLOSED_LOST
**Task Status:** PENDIENTE, EN_PROGRESO, COMPLETADA, CANCELADA, BLOQUEADA
**Quote Status:** BORRADOR, ENVIADA, ACEPTADA, RECHAZADA, EXPIRADA
**Order Status:** PENDIENTE, CONFIRMADO, EN_PROCESO, ENVIADO, ENTREGADO, CANCELADO

---

## Performance Tips

```sql
-- Always use WHERE deleted_at IS NULL
SELECT * FROM clientes_clientes WHERE deleted_at IS NULL;

-- Use LIMIT for large result sets
SELECT * FROM ventas_pedidos ORDER BY fecha DESC LIMIT 100;

-- Use partial indexes (already created)
-- Example: idx_clientes_clientes_nombre includes WHERE deleted_at IS NULL

-- Check query plan before production
EXPLAIN ANALYZE SELECT ...;

-- Update statistics after bulk operations
ANALYZE clientes_clientes;
```

---

## Security Checklist

- [ ] Session context set at request start
- [ ] Session context cleared at request end
- [ ] JWT tokens validated
- [ ] User permissions checked
- [ ] Sensitive data not logged
- [ ] SQL injection prevented (parameterized queries)
- [ ] Audit log entry created for critical actions

---

## Audit Logging

```java
// Java example
public void logAction(String action, String resource, UUID resourceId, String result) {
    jdbcTemplate.update(
        "INSERT INTO seguridad_audit_log (usuario_id, accion, recurso, recurso_id, ip_address, metadata, resultado) " +
        "VALUES (?, ?, ?, ?, ?::inet, ?::jsonb, ?)",
        getCurrentUserId(),
        action,
        resource,
        resourceId,
        getClientIp(),
        metadataJson,
        result
    );
}
```

**What to Audit:**
- ✅ Login/logout
- ✅ Permission changes
- ✅ Financial transactions
- ✅ Data exports
- ✅ Configuration changes
- ✅ Failed access attempts

---

## Troubleshooting

### Problem: Query returns 0 rows (but data exists)

```sql
-- Check session context
SELECT current_setting('app.current_tenant', TRUE);
-- If NULL, set context:
SELECT set_app_session_context('tenant-uuid'::UUID, 'user-uuid'::UUID, 'ADMIN', FALSE);
```

### Problem: Slow query

```sql
-- Check query plan
EXPLAIN ANALYZE <your-query>;

-- Update statistics
ANALYZE <table_name>;

-- Check if index exists
\d <table_name>
```

### Problem: Unique constraint violation

```sql
-- Find duplicate
SELECT <unique_column>, count(*)
FROM <table>
WHERE deleted_at IS NULL
GROUP BY <unique_column>
HAVING count(*) > 1;
```

---

## Maintenance Commands

```sql
-- Daily: Cleanup expired tokens
DELETE FROM seguridad_refresh_tokens WHERE expires_at < NOW() - INTERVAL '1 day';

-- Weekly: Update statistics
ANALYZE;

-- Monthly: Vacuum
VACUUM ANALYZE;

-- Monthly: Reindex (during maintenance window)
REINDEX DATABASE crm_db;

-- View table sizes
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

---

## Connection String Examples

**Java/Spring Boot:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/crm_db?sslmode=require
    username: app_user
    password: ${DB_PASSWORD}  # From environment variable
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

**Node.js:**
```javascript
const pool = new Pool({
  host: 'localhost',
  port: 5432,
  database: 'crm_db',
  user: 'app_user',
  password: process.env.DB_PASSWORD,
  ssl: { rejectUnauthorized: true },
  max: 20,
  idleTimeoutMillis: 30000
});
```

**Python:**
```python
import psycopg2
from psycopg2 import pool

connection_pool = pool.SimpleConnectionPool(
    5,  # minconn
    20, # maxconn
    host='localhost',
    port=5432,
    database='crm_db',
    user='app_user',
    password=os.environ['DB_PASSWORD'],
    sslmode='require'
)
```

---

## Role Hierarchy

```
ADMIN (level 0) ─────┬─── SALES_MANAGER (level 1) ──── SALES (level 2)
                     │
                     ├─── FINANCE_MANAGER (level 1) ── FINANCE (level 2)
                     │
                     ├─── WAREHOUSE_MANAGER (level 1) ─ WAREHOUSE (level 2)
                     │
                     └─── USER (level 3)
```

**Permission Scopes:**
- `admin:*` - All permissions
- `clients:read`, `clients:write` - Client management
- `sales:write` - Create quotes/orders
- `products:write` - Manage catalog
- `reports:read` - View analytics

---

## File Locations

**Migrations:**
- `/backend/application/src/main/resources/db/migration/V1__initial_schema.sql`
- `/backend/application/src/main/resources/db/migration/V2__add_indexes.sql`
- `/backend/application/src/main/resources/db/migration/V3__add_rls_policies.sql`
- `/backend/application/src/main/resources/db/migration/README.md`

**Documentation:**
- `/docs/erd/README.md` - Start here
- `/docs/erd/database-schema.md` - Complete reference
- `/docs/erd/erd-visual.md` - Visual diagrams
- `/docs/erd/database-security-guide.md` - Security deep-dive
- `/docs/erd/QUICK_REFERENCE.md` - This file

---

## Support Contacts

- **DBA Team:** dba@pagodirecto.com
- **Security:** security@pagodirecto.com
- **24/7 On-call:** +1-555-0100
- **Slack:** #database-team
- **Wiki:** https://wiki.pagodirecto.com/database

---

## Emergency Commands

**Stop all connections:**
```sql
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'crm_db' AND pid <> pg_backend_pid();
```

**Lock all accounts (breach response):**
```sql
UPDATE seguridad_usuarios SET status = 'SUSPENDED';
UPDATE seguridad_refresh_tokens SET revocado = TRUE;
```

**Backup database:**
```bash
pg_dump -Fc crm_db > emergency_backup_$(date +%Y%m%d_%H%M%S).dump
```

**Restore backup:**
```bash
pg_restore -d crm_db -c emergency_backup_20251013_143000.dump
```

---

**Last Updated:** 2025-10-13
**Version:** 1.0.0
**For:** Production deployment

**Print this page and keep near your workstation!**
