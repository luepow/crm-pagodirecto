#!/bin/bash
###############################################################################
# PostgreSQL Database Initialization Script
#
# Simple Analogy: This is like setting up a new filing cabinet. We create the
# drawers (database), install special organizers (extensions), and set up the
# filing system (schemas).
#
# Technical: Initialization script run once on first PostgreSQL container start.
# Creates database, installs required extensions (uuid-ossp, pgcrypto),
# configures schemas, and sets up basic security policies.
#
# Author: PagoDirecto Infrastructure Team
# Version: 1.0.0
#
# Usage: Automatically executed by PostgreSQL Docker entrypoint
###############################################################################

set -e  # Exit on error
set -u  # Exit on undefined variable

###############################################################################
# Configuration Variables
###############################################################################

DB_NAME="${POSTGRES_DB:-pagodirecto_crm}"
DB_USER="${POSTGRES_USER:-pagodirecto}"
DB_PASSWORD="${POSTGRES_PASSWORD}"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

###############################################################################
# Helper Functions
###############################################################################

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

###############################################################################
# Main Initialization
###############################################################################

log_info "Starting PostgreSQL initialization for PagoDirecto CRM/ERP"
log_info "Database: ${DB_NAME}"
log_info "User: ${DB_USER}"

###############################################################################
# Configure pg_hba.conf for Docker Network Access
###############################################################################

log_info "Configuring pg_hba.conf for Docker network access..."

# Add entry to allow connections from Docker network
# This allows the backend container to connect to PostgreSQL
cat >> "$PGDATA/pg_hba.conf" <<EOF
# Allow connections from Docker network (IPv4)
host    all             all             0.0.0.0/0            scram-sha-256
# Allow connections from Docker network (IPv6)
host    all             all             ::0/0                scram-sha-256
EOF

log_info "pg_hba.conf configured successfully"

# Reload PostgreSQL configuration
pg_ctl reload -D "$PGDATA"

# Wait for PostgreSQL to be ready
sleep 2

###############################################################################
# Create Database (if not exists)
###############################################################################

log_info "Checking if database '${DB_NAME}' exists..."

if psql -U "${POSTGRES_USER}" -lqt | cut -d \| -f 1 | grep -qw "${DB_NAME}"; then
    log_warn "Database '${DB_NAME}' already exists, skipping creation"
else
    log_info "Creating database '${DB_NAME}'..."
    psql -U "${POSTGRES_USER}" <<-EOSQL
        CREATE DATABASE ${DB_NAME}
            WITH
            OWNER = ${DB_USER}
            ENCODING = 'UTF8'
            LC_COLLATE = 'en_US.utf8'
            LC_CTYPE = 'en_US.utf8'
            TABLESPACE = pg_default
            CONNECTION LIMIT = -1
            TEMPLATE = template0;
EOSQL
    log_info "Database '${DB_NAME}' created successfully"
fi

###############################################################################
# Install Extensions
#
# Simple Analogy: Like installing special tools in our filing cabinet that
# help us work faster and more securely.
#
# Technical: PostgreSQL extensions add functionality:
# - uuid-ossp: UUID generation functions
# - pgcrypto: Cryptographic functions
# - pg_stat_statements: Query performance monitoring
###############################################################################

log_info "Installing PostgreSQL extensions..."

psql -U "${POSTGRES_USER}" -d "${DB_NAME}" <<-EOSQL
    -- Enable UUID generation
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

    -- Enable cryptographic functions
    CREATE EXTENSION IF NOT EXISTS "pgcrypto";

    -- Enable query performance monitoring (for development)
    CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

    -- Enable trigram similarity (for fuzzy text search)
    CREATE EXTENSION IF NOT EXISTS "pg_trgm";
EOSQL

log_info "Extensions installed successfully"

###############################################################################
# Create Schemas
#
# Simple Analogy: Like creating different sections in our filing cabinet for
# different departments - one section for customers, one for products, etc.
#
# Technical: Database schemas organize tables into logical namespaces.
# Implements bounded contexts from DDD architecture.
###############################################################################

log_info "Creating application schemas..."

psql -U "${POSTGRES_USER}" -d "${DB_NAME}" <<-EOSQL
    -- Core domain schema
    CREATE SCHEMA IF NOT EXISTS core_domain AUTHORIZATION ${DB_USER};

    -- Security domain schema
    CREATE SCHEMA IF NOT EXISTS seguridad AUTHORIZATION ${DB_USER};

    -- Clients domain schema
    CREATE SCHEMA IF NOT EXISTS clientes AUTHORIZATION ${DB_USER};

    -- Opportunities domain schema
    CREATE SCHEMA IF NOT EXISTS oportunidades AUTHORIZATION ${DB_USER};

    -- Tasks domain schema
    CREATE SCHEMA IF NOT EXISTS tareas AUTHORIZATION ${DB_USER};

    -- Products domain schema
    CREATE SCHEMA IF NOT EXISTS productos AUTHORIZATION ${DB_USER};

    -- Sales domain schema
    CREATE SCHEMA IF NOT EXISTS ventas AUTHORIZATION ${DB_USER};

    -- Reports domain schema
    CREATE SCHEMA IF NOT EXISTS reportes AUTHORIZATION ${DB_USER};

    -- Set default schema search path
    ALTER DATABASE ${DB_NAME} SET search_path TO public, core_domain, seguridad, clientes, oportunidades, tareas, productos, ventas, reportes;
EOSQL

log_info "Schemas created successfully"

###############################################################################
# Create Common Functions
#
# Technical: Reusable database functions used across multiple domains.
###############################################################################

log_info "Creating common database functions..."

psql -U "${POSTGRES_USER}" -d "${DB_NAME}" <<-EOSQL
    -- Function: Update timestamp on record modification
    CREATE OR REPLACE FUNCTION core_domain.update_updated_at_column()
    RETURNS TRIGGER AS \$\$
    BEGIN
        NEW.updated_at = NOW();
        RETURN NEW;
    END;
    \$\$ LANGUAGE plpgsql;

    -- Function: Generate UUID v4
    CREATE OR REPLACE FUNCTION core_domain.generate_uuid()
    RETURNS UUID AS \$\$
    BEGIN
        RETURN uuid_generate_v4();
    END;
    \$\$ LANGUAGE plpgsql;

    -- Function: Soft delete (set deleted_at timestamp)
    CREATE OR REPLACE FUNCTION core_domain.soft_delete()
    RETURNS TRIGGER AS \$\$
    BEGIN
        IF TG_OP = 'DELETE' THEN
            UPDATE pg_catalog.pg_class
            SET deleted_at = NOW()
            WHERE oid = TG_RELID;
            RETURN NULL;
        END IF;
        RETURN NEW;
    END;
    \$\$ LANGUAGE plpgsql;
EOSQL

log_info "Common functions created successfully"

###############################################################################
# Configure Row-Level Security (RLS)
#
# Simple Analogy: Like having separate locked drawers for each business unit.
# Each unit can only see their own files, not other units' files.
#
# Technical: PostgreSQL Row-Level Security enforces multi-tenant isolation.
# Policies filter rows based on current_setting('app.current_tenant').
###############################################################################

log_info "Configuring Row-Level Security policies..."

psql -U "${POSTGRES_USER}" -d "${DB_NAME}" <<-EOSQL
    -- Enable RLS on all schemas
    -- Note: Actual RLS policies will be created by Flyway migrations
    -- This just sets up the infrastructure

    -- Create tenant context function
    CREATE OR REPLACE FUNCTION core_domain.current_tenant_id()
    RETURNS UUID AS \$\$
    BEGIN
        -- Get tenant ID from session variable
        -- This should be set by the application on connection
        RETURN COALESCE(
            NULLIF(current_setting('app.current_tenant', true), '')::UUID,
            '00000000-0000-0000-0000-000000000000'::UUID
        );
    END;
    \$\$ LANGUAGE plpgsql STABLE;

    COMMENT ON FUNCTION core_domain.current_tenant_id() IS
        'Returns the current tenant ID from session variable app.current_tenant';
EOSQL

log_info "Row-Level Security infrastructure configured"

###############################################################################
# Create Database Roles
#
# Simple Analogy: Like creating employee badges with different access levels.
# Some badges open all doors, others only specific rooms.
#
# Technical: Database roles implement principle of least privilege. Application
# role has limited permissions, read-only role for reporting, admin role for
# maintenance.
###############################################################################

log_info "Creating database roles..."

psql -U "${POSTGRES_USER}" -d "${DB_NAME}" <<-EOSQL
    -- Application role (read/write on application schemas)
    DO \$\$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'pagodirecto_app') THEN
            CREATE ROLE pagodirecto_app LOGIN PASSWORD '${DB_PASSWORD}';
        END IF;
    END
    \$\$;

    -- Read-only role (for reporting and analytics)
    DO \$\$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'pagodirecto_readonly') THEN
            CREATE ROLE pagodirecto_readonly LOGIN PASSWORD '${DB_PASSWORD}_readonly';
        END IF;
    END
    \$\$;

    -- Grant permissions to application role
    GRANT CONNECT ON DATABASE ${DB_NAME} TO pagodirecto_app;
    GRANT USAGE ON SCHEMA core_domain, seguridad, clientes, oportunidades, tareas, productos, ventas, reportes TO pagodirecto_app;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA core_domain, seguridad, clientes, oportunidades, tareas, productos, ventas, reportes TO pagodirecto_app;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA core_domain, seguridad, clientes, oportunidades, tareas, productos, ventas, reportes TO pagodirecto_app;

    -- Grant permissions to read-only role
    GRANT CONNECT ON DATABASE ${DB_NAME} TO pagodirecto_readonly;
    GRANT USAGE ON SCHEMA core_domain, seguridad, clientes, oportunidades, tareas, productos, ventas, reportes TO pagodirecto_readonly;
    GRANT SELECT ON ALL TABLES IN SCHEMA core_domain, seguridad, clientes, oportunidades, tareas, productos, ventas, reportes TO pagodirecto_readonly;

    -- Set default privileges for future tables
    ALTER DEFAULT PRIVILEGES IN SCHEMA core_domain, seguridad, clientes, oportunidades, tareas, productos, ventas, reportes
        GRANT ALL ON TABLES TO pagodirecto_app;
    ALTER DEFAULT PRIVILEGES IN SCHEMA core_domain, seguridad, clientes, oportunidades, tareas, productos, ventas, reportes
        GRANT SELECT ON TABLES TO pagodirecto_readonly;
EOSQL

log_info "Database roles created and configured"

###############################################################################
# Performance Tuning
###############################################################################

log_info "Applying performance tuning..."

psql -U "${POSTGRES_USER}" -d "${DB_NAME}" <<-EOSQL
    -- Analyze database for query planner statistics
    ANALYZE;

    -- Vacuum to reclaim storage
    VACUUM ANALYZE;
EOSQL

log_info "Performance tuning applied"

###############################################################################
# Database Information
###############################################################################

log_info "Database initialization completed successfully!"
log_info "Database details:"

psql -U "${POSTGRES_USER}" -d "${DB_NAME}" <<-EOSQL
    SELECT
        'Database' AS type,
        current_database() AS name,
        pg_size_pretty(pg_database_size(current_database())) AS size
    UNION ALL
    SELECT
        'Extensions' AS type,
        string_agg(extname, ', ') AS name,
        NULL AS size
    FROM pg_extension
    WHERE extname NOT IN ('plpgsql')
    UNION ALL
    SELECT
        'Schemas' AS type,
        string_agg(schema_name, ', ') AS name,
        NULL AS size
    FROM information_schema.schemata
    WHERE schema_name NOT IN ('pg_catalog', 'information_schema', 'pg_toast')
    ORDER BY type;
EOSQL

log_info "=============================================="
log_info "Next Steps:"
log_info "1. Flyway migrations will run automatically on application startup"
log_info "2. Check logs for any migration errors"
log_info "3. Access Adminer at http://localhost:8081 to inspect database"
log_info "=============================================="

exit 0
