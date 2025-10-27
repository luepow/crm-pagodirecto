# Database Security Guide - PagoDirecto CRM/ERP

**Version:** 1.0.0
**Date:** 2025-10-13
**Classification:** INTERNAL USE ONLY
**Compliance:** PCI DSS Level 1, GDPR, CCPA

---

## Table of Contents

1. [Security Architecture Overview](#security-architecture-overview)
2. [Authentication & Authorization](#authentication--authorization)
3. [Multi-Tenant Isolation](#multi-tenant-isolation)
4. [Data Encryption](#data-encryption)
5. [Audit Logging](#audit-logging)
6. [Access Control Best Practices](#access-control-best-practices)
7. [PCI DSS Compliance](#pci-dss-compliance)
8. [Security Checklist](#security-checklist)
9. [Incident Response](#incident-response)

---

## Security Architecture Overview

### Defense in Depth Layers

```
┌─────────────────────────────────────────────────────────────┐
│ Layer 7: Application Security (Input Validation, CSRF)     │
├─────────────────────────────────────────────────────────────┤
│ Layer 6: API Security (JWT, Rate Limiting, CORS)           │
├─────────────────────────────────────────────────────────────┤
│ Layer 5: Business Logic (Authorization, Row-Level Security)│ ← THIS GUIDE
├─────────────────────────────────────────────────────────────┤
│ Layer 4: Database Security (RLS, Encryption, Audit)        │ ← THIS GUIDE
├─────────────────────────────────────────────────────────────┤
│ Layer 3: Network Security (TLS, Firewall, VPN)             │
├─────────────────────────────────────────────────────────────┤
│ Layer 2: Infrastructure Security (OS Hardening, Patching)  │
├─────────────────────────────────────────────────────────────┤
│ Layer 1: Physical Security (Data Center Access Control)    │
└─────────────────────────────────────────────────────────────┘
```

### Security Principles

1. **Least Privilege**: Users/apps get minimum permissions needed
2. **Defense in Depth**: Multiple security layers
3. **Fail Secure**: Default deny, explicit allow
4. **Audit Everything**: Complete traceability
5. **Zero Trust**: Verify every access request

---

## Authentication & Authorization

### User Authentication Flow

```
┌──────────┐  1. Login Request      ┌─────────────┐
│          │ ───────────────────────▶│             │
│  Client  │                         │   Keycloak  │
│          │ ◀───────────────────────│   (SSO)     │
└──────────┘  2. JWT Token           └──────┬──────┘
     │                                      │
     │ 3. API Request + JWT                 │
     │                                      │
     ▼                                      ▼
┌──────────┐  4. Validate Token     ┌─────────────┐
│          │ ───────────────────────▶│             │
│   API    │                         │  Validate & │
│  Server  │ ◀───────────────────────│  Extract    │
│          │  5. User Context        │  Claims     │
└────┬─────┘                         └─────────────┘
     │
     │ 6. Set Session Context
     │    (tenant, user, roles)
     ▼
┌─────────────────────────────────────────┐
│      PostgreSQL (with RLS)              │
│                                         │
│  SELECT set_app_session_context(        │
│    tenant_id,                           │
│    user_id,                             │
│    'SALES,SALES_MANAGER',               │
│    false                                │
│  );                                     │
│                                         │
│  All queries now filtered by RLS        │
└─────────────────────────────────────────┘
```

### JWT Token Structure

```json
{
  "sub": "user-uuid-here",
  "tenant_id": "tenant-uuid-here",
  "roles": ["SALES", "SALES_MANAGER"],
  "permissions": ["clients:read", "quotes:write"],
  "exp": 1700000000,
  "iat": 1699996400
}
```

### Password Security

**Requirements:**
- Minimum 12 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 number
- At least 1 special character
- No common passwords (use dictionary check)
- No username/email in password

**Storage:**
- Algorithm: bcrypt
- Cost factor: 12 (minimum)
- Never store plaintext passwords
- Never log passwords (even hashed)

**Example (Java):**
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
String hashedPassword = encoder.encode(rawPassword);

// Store hashedPassword in seguridad_usuarios.password_hash
```

### Multi-Factor Authentication (MFA)

**TOTP (Time-Based One-Time Password):**

1. **Enrollment:**
   ```sql
   -- Generate secret (32-byte random)
   UPDATE seguridad_usuarios
   SET mfa_secret = encode(gen_random_bytes(32), 'base64'),
       mfa_enabled = TRUE
   WHERE id = 'user-uuid';
   ```

2. **Verification:**
   - User enters 6-digit code from authenticator app
   - Server validates code against secret
   - Code valid for ±30 seconds window

3. **Backup Codes:**
   - Generate 10 single-use backup codes
   - Hash and store separately
   - Allow account recovery if device lost

**Implementation Example (Java):**
```java
import com.warrenstrange.googleauth.GoogleAuthenticator;

GoogleAuthenticator gAuth = new GoogleAuthenticator();
String secret = user.getMfaSecret();
boolean valid = gAuth.authorize(secret, codeFromUser);
```

### Session Management

**JWT Access Tokens:**
- Lifetime: 5 minutes (short-lived)
- Stored in memory only (never localStorage)
- Contains user context for RLS

**Refresh Tokens:**
- Lifetime: 30 days
- Stored in `seguridad_refresh_tokens` (hashed)
- Rotated on each use (automatic revocation)
- Tied to device (IP + User-Agent)

**Token Rotation:**
```java
// On token refresh request
public TokenResponse refreshAccessToken(String refreshToken) {
    // 1. Hash incoming token
    String tokenHash = hash(refreshToken);

    // 2. Verify token exists and not expired
    RefreshToken storedToken = refreshTokenRepo.findByTokenHash(tokenHash);
    if (storedToken == null || storedToken.isExpired() || storedToken.isRevoked()) {
        throw new InvalidTokenException();
    }

    // 3. Revoke old token
    storedToken.setRevoked(true);
    refreshTokenRepo.save(storedToken);

    // 4. Generate new tokens
    String newAccessToken = generateAccessToken(storedToken.getUser());
    String newRefreshToken = generateRefreshToken(storedToken.getUser());

    // 5. Store new refresh token (hashed)
    RefreshToken newToken = new RefreshToken();
    newToken.setTokenHash(hash(newRefreshToken));
    newToken.setUser(storedToken.getUser());
    newToken.setExpiresAt(LocalDateTime.now().plusDays(30));
    refreshTokenRepo.save(newToken);

    return new TokenResponse(newAccessToken, newRefreshToken);
}
```

### Role-Based Access Control (RBAC)

**Role Hierarchy:**

```
ADMIN (level 0)
  ├─▶ SALES_MANAGER (level 1)
  │     └─▶ SALES (level 2)
  │
  ├─▶ FINANCE_MANAGER (level 1)
  │     └─▶ FINANCE (level 2)
  │
  ├─▶ WAREHOUSE_MANAGER (level 1)
  │     └─▶ WAREHOUSE (level 2)
  │
  └─▶ USER (level 3)
```

**Permission Examples:**

| Role | Permissions |
|------|-------------|
| ADMIN | `admin:*` (all permissions) |
| SALES_MANAGER | `clients:*, quotes:*, orders:read, reports:read` |
| SALES | `clients:read, clients:write, quotes:*, orders:read` |
| WAREHOUSE_MANAGER | `products:*, orders:*, inventory:*` |
| FINANCE | `payments:*, reports:read, invoices:*` |

**Checking Permissions (Application Layer):**

```java
@PreAuthorize("hasPermission('clients', 'WRITE')")
public Cliente createCliente(ClienteDTO dto) {
    // Method only executes if user has clients:write permission
}

// Or manually:
public void updateOrder(UUID orderId, OrderDTO dto) {
    if (!securityService.hasPermission("orders", "UPDATE")) {
        throw new ForbiddenException("Insufficient permissions");
    }
    // ... update logic
}
```

---

## Multi-Tenant Isolation

### Tenant Architecture

**Hard Multi-Tenancy:** Each tenant's data is completely isolated via Row-Level Security (RLS).

```
Tenant A (unidad_negocio_id = UUID-A)
  ├─ Users: 50
  ├─ Clients: 1,000
  └─ Orders: 10,000

Tenant B (unidad_negocio_id = UUID-B)
  ├─ Users: 30
  ├─ Clients: 500
  └─ Orders: 5,000

Database View:
┌────────────────────────────────────────┐
│ All tables have unidad_negocio_id      │
│ RLS policies enforce isolation         │
│ Users CANNOT see other tenant's data   │
└────────────────────────────────────────┘
```

### Setting Tenant Context

**At Application Startup (per request):**

```java
@Component
public class TenantContextInterceptor implements HandlerInterceptor {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        // Extract from JWT claims
        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UUID tenantId = auth.getToken().getClaim("tenant_id");
        UUID userId = auth.getToken().getClaim("sub");
        List<String> roles = auth.getToken().getClaim("roles");

        // Set database session context
        jdbcTemplate.update(
            "SELECT set_app_session_context(?, ?, ?, ?)",
            tenantId,
            userId,
            String.join(",", roles),
            roles.contains("ADMIN") // bypass_rls for admin reports
        );

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        // Clear context after request
        jdbcTemplate.update("SELECT clear_app_session_context()");
    }
}
```

### Testing Tenant Isolation

```sql
-- Test 1: User can only see their tenant's data
SELECT set_app_session_context(
    'tenant-a-uuid'::UUID,
    'user-1-uuid'::UUID,
    'SALES',
    FALSE
);

SELECT count(*) FROM clientes_clientes;
-- Expected: Only Tenant A clients

SELECT count(*) FROM clientes_clientes WHERE unidad_negocio_id = 'tenant-b-uuid'::UUID;
-- Expected: 0 (RLS blocks access)

-- Test 2: Attempt to insert into another tenant
INSERT INTO clientes_clientes (unidad_negocio_id, codigo, nombre, tipo)
VALUES ('tenant-b-uuid'::UUID, 'CLI-9999', 'Test Client', 'PERSONA');
-- Expected: ERROR or silent failure (RLS blocks)

-- Test 3: Admin with bypass sees all tenants
SELECT set_app_session_context(
    'tenant-a-uuid'::UUID,
    'admin-uuid'::UUID,
    'ADMIN',
    TRUE  -- bypass RLS
);

SELECT count(*) FROM clientes_clientes;
-- Expected: ALL clients across ALL tenants
```

### Tenant Onboarding Checklist

When creating a new tenant:

1. **Create Tenant Record:**
   ```sql
   INSERT INTO unidades_negocio (id, nombre, plan, status)
   VALUES (gen_random_uuid(), 'Acme Corp', 'ENTERPRISE', 'ACTIVE');
   ```

2. **Create Admin User:**
   ```sql
   INSERT INTO seguridad_usuarios (
       unidad_negocio_id, username, email, password_hash, status
   ) VALUES (
       'tenant-id-uuid',
       'admin@acmecorp.com',
       'admin@acmecorp.com',
       '$2a$12$hashed_password',
       'ACTIVE'
   );
   ```

3. **Assign Admin Role:**
   ```sql
   INSERT INTO seguridad_usuarios_roles (usuario_id, rol_id)
   SELECT u.id, r.id
   FROM seguridad_usuarios u, seguridad_roles r
   WHERE u.email = 'admin@acmecorp.com'
     AND r.nombre = 'ADMIN'
     AND r.unidad_negocio_id = 'tenant-id-uuid';
   ```

4. **Initialize Default Data:**
   - Pipeline stages (LEAD, QUALIFIED, etc.)
   - Product categories
   - Default dashboard

5. **Verify Isolation:**
   - Login as new admin
   - Verify only sees their tenant's data
   - Attempt to access another tenant's data (should fail)

---

## Data Encryption

### Encryption at Rest

**PostgreSQL Transparent Data Encryption (TDE):**

```bash
# Option 1: File-system level encryption (LUKS)
cryptsetup luksFormat /dev/sdb
cryptsetup open /dev/sdb pgdata_encrypted
mkfs.ext4 /dev/mapper/pgdata_encrypted
mount /dev/mapper/pgdata_encrypted /var/lib/postgresql/data

# Option 2: ZFS with encryption
zfs create -o encryption=aes-256-gcm -o keyformat=passphrase rpool/pgdata
```

**Column-Level Encryption (for PII):**

```sql
-- Encrypt sensitive fields (if needed beyond TDE)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Encrypt during insert
INSERT INTO clientes_clientes (nombre, email, rfc_encrypted)
VALUES (
    'John Doe',
    'john@example.com',
    pgp_sym_encrypt('RFC123456ABC', 'encryption-key-from-vault')
);

-- Decrypt during select
SELECT
    nombre,
    email,
    pgp_sym_decrypt(rfc_encrypted::bytea, 'encryption-key-from-vault') AS rfc
FROM clientes_clientes;
```

**Key Management:**

- **DO NOT** hardcode encryption keys in code
- **USE** HashiCorp Vault or AWS Secrets Manager
- **ROTATE** keys quarterly
- **AUDIT** key access

**Example (Vault Integration):**

```java
@Service
public class EncryptionService {

    @Autowired
    private VaultTemplate vaultTemplate;

    public String encrypt(String plaintext) {
        TransitOperations transit = vaultTemplate.opsForTransit("transit");
        String ciphertext = transit.encrypt("crm-key", plaintext);
        return ciphertext;
    }

    public String decrypt(String ciphertext) {
        TransitOperations transit = vaultTemplate.opsForTransit("transit");
        String plaintext = transit.decrypt("crm-key", ciphertext);
        return plaintext;
    }
}
```

### Encryption in Transit

**TLS 1.3 Configuration:**

```bash
# postgresql.conf
ssl = on
ssl_cert_file = '/etc/ssl/certs/server.crt'
ssl_key_file = '/etc/ssl/private/server.key'
ssl_ca_file = '/etc/ssl/certs/ca.crt'
ssl_min_protocol_version = 'TLSv1.3'
ssl_ciphers = 'TLS_AES_256_GCM_SHA384:TLS_CHACHA20_POLY1305_SHA256'
ssl_prefer_server_ciphers = on
```

**Force SSL Connections:**

```bash
# pg_hba.conf
hostssl all all 0.0.0.0/0 md5 clientcert=verify-ca
```

**Application Connection String:**

```
jdbc:postgresql://db.example.com:5432/crm_db?sslmode=verify-full&sslrootcert=/path/to/ca.crt
```

---

## Audit Logging

### Audit Log Structure

Every security-relevant action is logged in `seguridad_audit_log`:

```sql
-- Example: User login
INSERT INTO seguridad_audit_log (
    usuario_id,
    accion,
    recurso,
    ip_address,
    user_agent,
    metadata,
    resultado
) VALUES (
    'user-uuid',
    'LOGIN',
    'authentication',
    '192.168.1.100'::inet,
    'Mozilla/5.0...',
    '{"mfa_used": true, "device_fingerprint": "abc123"}'::jsonb,
    'SUCCESS'
);

-- Example: Failed access attempt
INSERT INTO seguridad_audit_log (
    usuario_id,
    accion,
    recurso,
    recurso_id,
    ip_address,
    metadata,
    resultado,
    mensaje_error
) VALUES (
    'user-uuid',
    'READ',
    'clientes_clientes',
    'client-uuid',
    '192.168.1.100'::inet,
    '{"attempted_tenant": "tenant-b-uuid"}'::jsonb,
    'FAILURE',
    'Insufficient permissions: RLS denied access'
);
```

### What to Audit

**Authentication Events:**
- ✅ Login (success/failure)
- ✅ Logout
- ✅ MFA enrollment/verification
- ✅ Password change
- ✅ Token refresh
- ✅ Account lockout

**Authorization Events:**
- ✅ Permission grant/revoke
- ✅ Role assignment/removal
- ✅ Access denied (403)

**Data Access Events:**
- ✅ Read sensitive data (PII, financial)
- ✅ Mass export (>100 records)
- ✅ Cross-tenant access attempt

**Data Modification Events:**
- ✅ Create/Update/Delete operations on critical tables
- ✅ Bulk updates (>10 records)
- ✅ Financial transactions

**Administrative Events:**
- ✅ User creation/deletion
- ✅ Configuration changes
- ✅ Backup/restore operations
- ✅ Database schema changes

### Audit Log Retention

**Retention Policy:**
- **Financial Data:** 7 years (regulatory requirement)
- **Security Events:** 2 years
- **General Activity:** 1 year

**Archival Process:**

```sql
-- Monthly: Archive old audit logs to cold storage
\copy (
    SELECT * FROM seguridad_audit_log
    WHERE created_at < NOW() - INTERVAL '2 years'
    AND created_at >= NOW() - INTERVAL '25 months'
) TO '/backup/audit_archive_2023_01.csv' CSV HEADER;

-- Delete archived records
DELETE FROM seguridad_audit_log
WHERE created_at < NOW() - INTERVAL '2 years';

-- Financial data (7 years)
\copy (
    SELECT * FROM seguridad_audit_log
    WHERE created_at < NOW() - INTERVAL '7 years'
    AND accion IN ('PAYMENT', 'REFUND', 'INVOICE', 'SETTLEMENT')
) TO '/backup/audit_archive_financial_2018.csv' CSV HEADER;
```

### Real-Time Alerting

**Critical Events to Alert On:**

1. **Multiple Failed Logins:**
   ```sql
   SELECT usuario_id, count(*) as failed_attempts
   FROM seguridad_audit_log
   WHERE accion = 'LOGIN'
     AND resultado = 'FAILURE'
     AND created_at > NOW() - INTERVAL '15 minutes'
   GROUP BY usuario_id
   HAVING count(*) >= 5;
   ```

2. **Privilege Escalation:**
   ```sql
   SELECT usuario_id, metadata
   FROM seguridad_audit_log
   WHERE accion = 'ROLE_ASSIGNMENT'
     AND metadata->>'new_role' IN ('ADMIN', 'SUPER_ADMIN')
     AND created_at > NOW() - INTERVAL '5 minutes';
   ```

3. **Mass Data Export:**
   ```sql
   SELECT usuario_id, recurso, metadata->>'record_count' as count
   FROM seguridad_audit_log
   WHERE accion = 'EXPORT'
     AND (metadata->>'record_count')::int > 1000
     AND created_at > NOW() - INTERVAL '1 hour';
   ```

4. **Access from Unusual Location:**
   ```sql
   -- Requires geolocation lookup
   SELECT usuario_id, ip_address, metadata->>'country' as country
   FROM seguridad_audit_log
   WHERE created_at > NOW() - INTERVAL '1 hour'
     AND metadata->>'country' NOT IN (
         SELECT DISTINCT metadata->>'country'
         FROM seguridad_audit_log
         WHERE usuario_id = seguridad_audit_log.usuario_id
           AND created_at BETWEEN NOW() - INTERVAL '30 days' AND NOW() - INTERVAL '1 hour'
     );
   ```

---

## Access Control Best Practices

### Principle of Least Privilege

**Application Database User:**

```sql
-- Create app user with minimal permissions
CREATE ROLE app_user WITH LOGIN PASSWORD 'secure_password';

-- Grant only necessary permissions
GRANT CONNECT ON DATABASE crm_db TO app_user;
GRANT USAGE ON SCHEMA public TO app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_user;

-- REVOKE dangerous permissions
REVOKE CREATE ON SCHEMA public FROM app_user;
REVOKE DROP ON ALL TABLES IN SCHEMA public FROM app_user;
REVOKE TRUNCATE ON ALL TABLES IN SCHEMA public FROM app_user;

-- No superuser privileges
ALTER ROLE app_user WITH NOSUPERUSER NOCREATEDB NOCREATEROLE;
```

**Read-Only User (for reporting):**

```sql
CREATE ROLE readonly_user WITH LOGIN PASSWORD 'secure_password';
GRANT CONNECT ON DATABASE crm_db TO readonly_user;
GRANT USAGE ON SCHEMA public TO readonly_user;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly_user;

-- Explicitly deny writes
REVOKE INSERT, UPDATE, DELETE, TRUNCATE ON ALL TABLES IN SCHEMA public FROM readonly_user;
```

### Network Security

**Firewall Rules (pg_hba.conf):**

```bash
# TYPE  DATABASE    USER            ADDRESS                 METHOD
# Local connections
local   all         all                                     peer

# Application servers (specific IPs)
hostssl crm_db      app_user        10.0.1.0/24             md5
hostssl crm_db      app_user        10.0.2.0/24             md5

# Read-only replicas
hostssl crm_db      readonly_user   10.0.3.0/24             md5

# Admin access (VPN only)
hostssl crm_db      postgres        172.16.0.0/16           cert clientcert=verify-full

# Deny all others
hostssl all         all             0.0.0.0/0               reject
```

### Connection Pooling Security

**HikariCP Configuration:**

```java
@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://db.example.com:5432/crm_db");
        config.setUsername("app_user");
        config.setPassword(getPasswordFromVault());

        // Security settings
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);

        // SSL/TLS
        config.addDataSourceProperty("sslmode", "verify-full");
        config.addDataSourceProperty("sslrootcert", "/etc/ssl/certs/ca.crt");

        // Connection validation
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);

        return new HikariDataSource(config);
    }

    private String getPasswordFromVault() {
        // Retrieve from HashiCorp Vault or AWS Secrets Manager
        return vaultTemplate.read("secret/database/app_user").getData().get("password");
    }
}
```

---

## PCI DSS Compliance

### Requirements Mapping

| PCI DSS Req | Implementation |
|-------------|----------------|
| **3.4** Render PAN unreadable | Never store full PAN; use tokenization |
| **3.5** Protect cryptographic keys | Vault integration, key rotation |
| **3.6** Document key mgmt | Key lifecycle in runbook |
| **8.2** MFA for admin access | `seguridad_usuarios.mfa_enabled` |
| **8.3** Secure authentication | bcrypt (cost 12), TLS 1.3 |
| **10.1** Audit trail | `seguridad_audit_log` (immutable) |
| **10.2** Log security events | All auth events logged |
| **10.3** Log entry details | User, timestamp, action, result, IP |
| **10.5** Protect logs | Append-only, RLS prevents tampering |
| **10.6** Review logs | Daily automated alerts |
| **10.7** Retain logs 1 year | 7-year retention policy |

### Tokenization (Never Store Full PAN)

**DO NOT STORE:**
- Full credit card numbers (PAN)
- CVV/CVC codes
- Magnetic stripe data
- PIN blocks

**INSTEAD:**
- Use payment gateway tokenization (Stripe, Adyen, etc.)
- Store only: `token_id`, `last_4_digits`, `expiry_month`, `expiry_year`, `card_brand`

**Example Schema (if processing payments):**

```sql
CREATE TABLE pagos_metodos_pago (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id UUID NOT NULL,
    tipo VARCHAR(20) NOT NULL, -- CARD, BANK_ACCOUNT, WALLET
    token_externo VARCHAR(255) NOT NULL, -- From payment gateway
    ultimos_4_digitos VARCHAR(4),
    fecha_expiracion DATE,
    marca VARCHAR(50), -- VISA, MASTERCARD, AMEX
    titular VARCHAR(255),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_pagos_metodos_pago_cliente FOREIGN KEY (cliente_id) REFERENCES clientes_clientes(id)
);

-- NO PAN stored, only gateway token
```

### Quarterly Vulnerability Scanning

**Database Security Audit:**

```bash
# Check for default passwords
SELECT usename, passwd IS NULL as no_password
FROM pg_shadow
WHERE passwd IS NULL OR passwd = '';

# Check for weak SSL ciphers
SHOW ssl_ciphers;

# Check for public schema grants
SELECT grantee, privilege_type
FROM information_schema.role_table_grants
WHERE grantee = 'PUBLIC';

# Check for superuser accounts
SELECT rolname FROM pg_roles WHERE rolsuper = true;
```

---

## Security Checklist

### Pre-Production Checklist

**Database Security:**
- [ ] TLS 1.3 enabled and enforced
- [ ] Strong SSL cipher suites configured
- [ ] Certificate validation enabled (verify-full)
- [ ] Default passwords changed
- [ ] Unnecessary extensions disabled
- [ ] pg_hba.conf restricts access to known IPs
- [ ] Application user has least privilege
- [ ] Superuser access restricted to VPN only
- [ ] Connection pooling configured (max 50 connections)
- [ ] Statement timeout set (30 seconds)

**Application Security:**
- [ ] JWT secret key in Vault (not in code)
- [ ] JWT tokens expire in 5 minutes
- [ ] Refresh tokens rotate on use
- [ ] MFA enforced for admin accounts
- [ ] Password policy enforced (12+ chars, complexity)
- [ ] bcrypt cost factor >= 12
- [ ] SQL injection prevention (parameterized queries)
- [ ] CSRF protection enabled
- [ ] CORS configured (whitelist only)
- [ ] Rate limiting enabled (100 req/min/user)

**Row-Level Security:**
- [ ] RLS enabled on all tables
- [ ] Session context set on every request
- [ ] Context cleared after request
- [ ] Tenant isolation tested (no cross-tenant access)
- [ ] Ownership policies tested (users see only their data)
- [ ] Admin bypass tested (works for reports only)

**Audit Logging:**
- [ ] All authentication events logged
- [ ] All authorization failures logged
- [ ] All financial transactions logged
- [ ] Logs immutable (no UPDATE/DELETE)
- [ ] Real-time alerting configured
- [ ] Log retention policy implemented (7 years)
- [ ] Log export to SIEM (Splunk, ELK)

**Data Encryption:**
- [ ] Encryption at rest enabled (file-system or TDE)
- [ ] Encryption in transit enforced (TLS 1.3)
- [ ] Encryption keys in Vault (not in code)
- [ ] Key rotation schedule defined (quarterly)
- [ ] PII encrypted at column level (if required)

**Backup & Recovery:**
- [ ] Daily full backups automated
- [ ] WAL archiving enabled (PITR)
- [ ] Backup encryption enabled
- [ ] Backup restoration tested (monthly)
- [ ] Backup retention policy (30 days full, 7 years archived)
- [ ] Backup stored off-site (S3, GCS)

### Monthly Security Review

```sql
-- 1. Review failed login attempts
SELECT usuario_id, count(*) as failed_logins
FROM seguridad_audit_log
WHERE accion = 'LOGIN'
  AND resultado = 'FAILURE'
  AND created_at > NOW() - INTERVAL '30 days'
GROUP BY usuario_id
ORDER BY failed_logins DESC;

-- 2. Review users with admin privileges
SELECT u.username, u.email, r.nombre as rol
FROM seguridad_usuarios u
JOIN seguridad_usuarios_roles ur ON u.id = ur.usuario_id
JOIN seguridad_roles r ON ur.rol_id = r.id
WHERE r.nombre IN ('ADMIN', 'SUPER_ADMIN')
  AND u.deleted_at IS NULL;

-- 3. Review inactive users (90+ days)
SELECT username, email, ultimo_acceso
FROM seguridad_usuarios
WHERE ultimo_acceso < NOW() - INTERVAL '90 days'
  AND deleted_at IS NULL
  AND status = 'ACTIVE'
ORDER BY ultimo_acceso;

-- 4. Review orphaned refresh tokens
SELECT count(*) as orphaned_tokens
FROM seguridad_refresh_tokens
WHERE expires_at < NOW()
  AND revocado = FALSE;

-- 5. Review audit log integrity
SELECT date_trunc('day', created_at) as date,
       count(*) as events,
       count(DISTINCT usuario_id) as unique_users
FROM seguridad_audit_log
WHERE created_at > NOW() - INTERVAL '30 days'
GROUP BY date_trunc('day', created_at)
ORDER BY date DESC;
```

---

## Incident Response

### Security Incident Types

1. **Brute Force Attack**
2. **SQL Injection Attempt**
3. **Unauthorized Access**
4. **Data Breach**
5. **Privilege Escalation**
6. **DDoS Attack**

### Response Playbook

#### 1. Brute Force Attack

**Detection:**
```sql
SELECT usuario_id, ip_address, count(*) as attempts
FROM seguridad_audit_log
WHERE accion = 'LOGIN'
  AND resultado = 'FAILURE'
  AND created_at > NOW() - INTERVAL '10 minutes'
GROUP BY usuario_id, ip_address
HAVING count(*) >= 10;
```

**Response:**
1. Lock affected accounts (30 minutes)
2. Block source IP at firewall
3. Notify affected users
4. Review for compromised credentials

**SQL:**
```sql
-- Lock account
UPDATE seguridad_usuarios
SET bloqueado_hasta = NOW() + INTERVAL '30 minutes'
WHERE id = 'compromised-user-id';

-- Revoke all refresh tokens
UPDATE seguridad_refresh_tokens
SET revocado = TRUE
WHERE usuario_id = 'compromised-user-id';
```

#### 2. Unauthorized Access

**Detection:**
```sql
SELECT usuario_id, recurso, recurso_id, metadata
FROM seguridad_audit_log
WHERE resultado = 'FAILURE'
  AND accion IN ('READ', 'UPDATE', 'DELETE')
  AND created_at > NOW() - INTERVAL '1 hour'
ORDER BY created_at DESC;
```

**Response:**
1. Identify affected user and resource
2. Review user permissions
3. Verify RLS policies effective
4. Notify security team
5. Force password reset if suspicious

#### 3. Data Breach

**Immediate Actions:**
1. **Isolate**: Disable affected user accounts
2. **Contain**: Revoke all active sessions
3. **Assess**: Determine scope of breach
4. **Notify**: Inform affected parties (GDPR 72 hours)
5. **Remediate**: Fix vulnerability
6. **Document**: Complete incident report

**SQL:**
```sql
-- Disable all accounts (emergency)
UPDATE seguridad_usuarios SET status = 'SUSPENDED';

-- Revoke all sessions
UPDATE seguridad_refresh_tokens SET revocado = TRUE;

-- Audit what was accessed
SELECT usuario_id, accion, recurso, recurso_id, created_at
FROM seguridad_audit_log
WHERE created_at BETWEEN 'breach-start-time' AND 'breach-end-time'
ORDER BY created_at;
```

### Emergency Contacts

- **Security Team:** security@pagodirecto.com
- **DBA Team:** dba@pagodirecto.com
- **Incident Response:** +1-555-0100 (24/7)
- **Legal/Compliance:** compliance@pagodirecto.com

---

## Additional Resources

- **OWASP Top 10:** https://owasp.org/www-project-top-ten/
- **PCI DSS Requirements:** https://www.pcisecuritystandards.org/
- **NIST Cybersecurity Framework:** https://www.nist.gov/cyberframework
- **PostgreSQL Security:** https://www.postgresql.org/docs/16/security.html

---

**END OF SECURITY GUIDE**

**Classification:** INTERNAL USE ONLY
**Last Updated:** 2025-10-13
**Next Review:** 2025-11-13
