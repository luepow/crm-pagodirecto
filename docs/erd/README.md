# Database Documentation - PagoDirecto CRM/ERP

**Version:** 1.0.0
**Date:** 2025-10-13
**Status:** Production Ready

---

## Quick Navigation

| Document | Purpose | Audience |
|----------|---------|----------|
| **[database-schema.md](./database-schema.md)** | Complete schema documentation with all domains, tables, indexes, and maintenance guides | DBAs, Backend Developers |
| **[erd-visual.md](./erd-visual.md)** | Visual entity relationship diagrams in ASCII format | All Technical Staff |
| **[database-security-guide.md](./database-security-guide.md)** | Security architecture, RLS implementation, PCI DSS compliance, incident response | Security Team, DBAs, DevOps |
| **[Migration README](../../backend/application/src/main/resources/db/migration/README.md)** | Flyway migration guide with troubleshooting | DevOps, DBAs |

---

## Project Overview

**PagoDirecto CRM/ERP** is an enterprise-grade customer relationship management and enterprise resource planning system built with:

- **Architecture:** Clean/Hexagonal with Domain-Driven Design
- **Database:** PostgreSQL 16+
- **Security:** Row-Level Security (RLS) for multi-tenant isolation
- **Compliance:** PCI DSS Level 1, GDPR, CCPA

---

## Database Architecture

### Domain Contexts

```
┌─────────────────────────────────────────────────────────────┐
│                      PagoDirecto CRM/ERP                    │
│                    PostgreSQL Database                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. SEGURIDAD (Security & IAM) ────────── 7 tables         │
│     • Users, Roles, Permissions                             │
│     • JWT Refresh Tokens                                    │
│     • Immutable Audit Log                                   │
│                                                             │
│  2. CLIENTES (Clients/CRM) ───────────── 3 tables          │
│     • Clients (Personas/Empresas)                           │
│     • Contacts, Addresses                                   │
│                                                             │
│  3. OPORTUNIDADES (Sales Pipeline) ────── 3 tables         │
│     • Pipeline Stages                                       │
│     • Opportunities, Activities                             │
│                                                             │
│  4. TAREAS (Tasks) ───────────────────── 2 tables          │
│     • Tasks (with polymorphic relations)                    │
│     • Comments/Discussion                                   │
│                                                             │
│  5. PRODUCTOS (Catalog) ──────────────── 3 tables          │
│     • Hierarchical Categories (5 levels)                    │
│     • Products, Differential Pricing                        │
│                                                             │
│  6. VENTAS (Sales) ───────────────────── 4 tables          │
│     • Quotes → Orders (full lifecycle)                      │
│     • Line Items (products + pricing)                       │
│                                                             │
│  7. REPORTES (Analytics) ─────────────── 2 tables          │
│     • Dashboards, Widgets (JSONB config)                    │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│  Total: 24 tables | 150+ indexes | 50+ RLS policies        │
└─────────────────────────────────────────────────────────────┘
```

### Key Features

**Multi-Tenant Isolation:**
- Every table has `unidad_negocio_id` (tenant UUID)
- Row-Level Security (RLS) enforces complete data isolation
- Zero-trust: Every query filtered by tenant context

**Audit Trail:**
- Standard columns: `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`
- Immutable audit log with 7-year retention
- Real-time alerting on suspicious activity

**Security:**
- bcrypt password hashing (cost 12)
- Multi-factor authentication (TOTP)
- JWT access tokens (5 min) + refresh tokens (30 days)
- TLS 1.3 enforced

**Performance:**
- UUID primary keys (distributed-friendly)
- 150+ optimized indexes (unique, composite, partial, GIN)
- Expected latency: <100ms for 95% of queries
- Connection pooling (HikariCP, 20-50 connections)

---

## Quick Start

### 1. Prerequisites

```bash
# Install PostgreSQL 16+
brew install postgresql@16  # macOS
# or
apt install postgresql-16   # Ubuntu

# Start PostgreSQL
brew services start postgresql@16
```

### 2. Create Database

```bash
createdb crm_db
psql -d crm_db -c "CREATE EXTENSION IF NOT EXISTS pgcrypto;"
```

### 3. Run Migrations

**Using Flyway CLI:**
```bash
export FLYWAY_URL="jdbc:postgresql://localhost:5432/crm_db"
export FLYWAY_USER="postgres"
export FLYWAY_PASSWORD="your_password"

flyway migrate
```

**Using Maven:**
```bash
cd /Users/lperez/Workspace/Development/next/crm_pd/backend
./mvnw flyway:migrate
```

**Using Spring Boot (automatic):**
```yaml
# application.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

```bash
./mvnw spring-boot:run
```

### 4. Verify Installation

```sql
-- Check tables
SELECT count(*) FROM information_schema.tables
WHERE table_schema = 'public' AND table_type = 'BASE TABLE';
-- Expected: 24

-- Check RLS enabled
SELECT tablename, rowsecurity
FROM pg_tables
WHERE schemaname = 'public'
LIMIT 5;
-- Expected: rowsecurity = true

-- Test session context
SELECT set_app_session_context(
    gen_random_uuid(),  -- tenant_id
    gen_random_uuid(),  -- user_id
    'ADMIN',            -- roles
    FALSE               -- bypass_rls
);

SELECT app_current_tenant(), app_current_user();
-- Expected: UUIDs returned
```

---

## Migration Files

Located in: `/backend/application/src/main/resources/db/migration/`

| File | Description | Time |
|------|-------------|------|
| `V1__initial_schema.sql` | All tables, constraints, comments | ~5s |
| `V2__add_indexes.sql` | 150+ performance indexes | ~10s |
| `V3__add_rls_policies.sql` | Row-Level Security policies | ~3s |

**Total:** ~18 seconds on empty database

See [Migration README](../../backend/application/src/main/resources/db/migration/README.md) for detailed guide.

---

## Common Tasks

### Create New Tenant

```sql
-- 1. Create tenant
INSERT INTO unidades_negocio (id, nombre, plan, status)
VALUES (gen_random_uuid(), 'Acme Corp', 'ENTERPRISE', 'ACTIVE')
RETURNING id;

-- 2. Create admin user
INSERT INTO seguridad_usuarios (
    unidad_negocio_id, username, email, password_hash, status
) VALUES (
    '<tenant-id-from-step-1>',
    'admin@acmecorp.com',
    'admin@acmecorp.com',
    '$2a$12$...',  -- bcrypt hash
    'ACTIVE'
);

-- 3. Assign admin role
-- (via application logic)
```

### Set User Session Context

```java
// In Spring interceptor or filter
@Override
public boolean preHandle(HttpServletRequest request, ...) {
    JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

    UUID tenantId = auth.getToken().getClaim("tenant_id");
    UUID userId = auth.getToken().getClaim("sub");
    List<String> roles = auth.getToken().getClaim("roles");

    jdbcTemplate.update(
        "SELECT set_app_session_context(?, ?, ?, ?)",
        tenantId,
        userId,
        String.join(",", roles),
        roles.contains("ADMIN") && isReportEndpoint(request)
    );

    return true;
}
```

### Query with RLS

```java
// All queries automatically filtered by tenant
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    // This query automatically includes: WHERE unidad_negocio_id = app_current_tenant()
    List<Cliente> findByStatus(String status);

    // RLS enforces ownership policy
    Optional<Cliente> findById(UUID id);
}
```

### Audit Log Query

```sql
-- Recent failed login attempts
SELECT usuario_id, ip_address, count(*) as attempts
FROM seguridad_audit_log
WHERE accion = 'LOGIN'
  AND resultado = 'FAILURE'
  AND created_at > NOW() - INTERVAL '1 hour'
GROUP BY usuario_id, ip_address
ORDER BY attempts DESC;

-- User activity timeline
SELECT created_at, accion, recurso, resultado
FROM seguridad_audit_log
WHERE usuario_id = 'user-uuid'
  AND created_at > NOW() - INTERVAL '7 days'
ORDER BY created_at DESC;
```

---

## Performance Optimization

### Expected Query Performance (p95)

| Query Type | Latency |
|------------|---------|
| Primary key lookup | <5ms |
| Index scan (1K rows) | <50ms |
| Join query (2-3 tables) | <100ms |
| Aggregation (10K rows) | <200ms |
| Report query (100K rows) | <2s |

### Optimization Tips

1. **Always use indexes:** All foreign keys and search fields indexed
2. **Leverage partial indexes:** Filter by `deleted_at IS NULL`
3. **Use covering indexes:** Include columns for index-only scans
4. **Analyze queries:** `EXPLAIN ANALYZE` before production
5. **Connection pooling:** HikariCP with 20-50 connections
6. **Batch operations:** Bulk inserts/updates when possible

### Monitoring

```sql
-- Cache hit ratio (should be >99%)
SELECT
    sum(heap_blks_hit) / (sum(heap_blks_hit) + sum(heap_blks_read)) AS cache_hit_ratio
FROM pg_statio_user_tables;

-- Slow queries (>1s)
SELECT query, calls, total_exec_time, mean_exec_time
FROM pg_stat_statements
WHERE mean_exec_time > 1000
ORDER BY total_exec_time DESC
LIMIT 10;

-- Index usage
SELECT schemaname, tablename, indexname, idx_scan
FROM pg_stat_user_indexes
WHERE idx_scan = 0
ORDER BY pg_relation_size(indexrelid) DESC;
```

---

## Security Best Practices

### Multi-Tenant Isolation

✅ **DO:**
- Always set session context at request start
- Clear session context at request end
- Test tenant isolation regularly
- Use `app_current_tenant()` in custom queries

❌ **DON'T:**
- Hardcode tenant IDs in queries
- Trust client-provided tenant IDs
- Share database users across tenants
- Disable RLS in production

### Password Security

✅ **DO:**
- Use bcrypt with cost ≥ 12
- Enforce 12+ character passwords
- Require password complexity
- Implement account lockout (5 attempts)
- Enable MFA for admins

❌ **DON'T:**
- Store plaintext passwords
- Log passwords (even hashed)
- Allow common passwords
- Use MD5/SHA1 for passwords

### Audit Logging

✅ **DO:**
- Log all authentication events
- Log all authorization failures
- Log all financial transactions
- Retain logs for 7 years
- Set up real-time alerts

❌ **DON'T:**
- Modify audit logs
- Delete audit logs (except archival)
- Log sensitive PII unnecessarily
- Ignore failed login patterns

See [database-security-guide.md](./database-security-guide.md) for complete security documentation.

---

## Troubleshooting

### Issue: RLS Denies Access

**Symptom:** Queries return 0 rows despite data existing

**Solution:**
```sql
-- Check session context
SELECT
    current_setting('app.current_tenant', TRUE) as tenant,
    current_setting('app.current_user', TRUE) as user,
    current_setting('app.user_roles', TRUE) as roles;

-- Set context if NULL
SELECT set_app_session_context(
    'tenant-uuid'::UUID,
    'user-uuid'::UUID,
    'ADMIN',
    FALSE
);
```

### Issue: Slow Queries

**Symptom:** Query takes >1 second

**Solution:**
```sql
-- Check query plan
EXPLAIN ANALYZE <your-query>;

-- Update statistics
ANALYZE <table_name>;

-- Verify index exists
SELECT * FROM pg_indexes WHERE tablename = '<table_name>';
```

### Issue: Connection Exhausted

**Symptom:** "FATAL: too many connections"

**Solution:**
```sql
-- Check active connections
SELECT count(*) FROM pg_stat_activity WHERE state = 'active';

-- Kill idle connections
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE state = 'idle' AND state_change < NOW() - INTERVAL '10 minutes';

-- Increase max_connections (postgresql.conf)
max_connections = 200
```

---

## Maintenance Schedule

| Task | Frequency | Command |
|------|-----------|---------|
| Backup database | Daily | `pg_dump -Fc crm_db > backup_$(date +%Y%m%d).dump` |
| Update statistics | Weekly | `ANALYZE;` |
| Cleanup expired tokens | Daily | `DELETE FROM seguridad_refresh_tokens WHERE expires_at < NOW();` |
| Vacuum | Weekly | `VACUUM ANALYZE;` |
| Reindex | Monthly | `REINDEX DATABASE crm_db;` |
| Archive audit logs | Monthly | Export to CSV, delete >2 years |
| Security review | Monthly | Review failed logins, permissions |

---

## Support & Contact

**Database Team:**
- Email: dba@pagodirecto.com
- Slack: #database-team
- On-call: +1-555-0100 (24/7)

**Documentation:**
- Internal Wiki: https://wiki.pagodirecto.com/database
- Issue Tracker: https://jira.pagodirecto.com/database
- Code Repository: https://github.com/pagodirecto/crm_pd

**Emergency Contacts:**
- Security Team: security@pagodirecto.com
- Incident Response: +1-555-0100
- Legal/Compliance: compliance@pagodirecto.com

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2025-10-13 | Initial schema release |

---

## License

**Proprietary and Confidential**

Copyright © 2025 PagoDirecto. All rights reserved.

This documentation contains proprietary information and trade secrets. Unauthorized distribution or reproduction is strictly prohibited.

---

**END OF README**
