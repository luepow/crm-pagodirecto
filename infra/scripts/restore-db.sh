#!/bin/bash
###############################################################################
# Database Restore Script
#
# Simple Analogy: Like using your photocopied documents to restore the originals
# after they've been lost or damaged. Very useful when you need to recover data!
#
# Usage: ./restore-db.sh <backup_name>
#   backup_name: Name of backup file (without .sql.gz extension)
#
# WARNING: This will replace all current database data!
#
# Author: PagoDirecto Infrastructure Team
###############################################################################

set -e

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="${SCRIPT_DIR}/../docker"
BACKUP_DIR="${DOCKER_DIR}/backups"

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

echo ""
echo "╔═══════════════════════════════════════════════════════╗"
echo "║      PagoDirecto CRM/ERP - Database Restore          ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""

# Check if backup name provided
if [ -z "$1" ]; then
    log_error "No backup name provided"
    echo ""
    echo "Usage: ./restore-db.sh <backup_name>"
    echo ""
    log_info "Available backups:"
    ls -1h "${BACKUP_DIR}"/*.sql.gz 2>/dev/null | xargs -n 1 basename | sed 's/.sql.gz$//' || log_warn "No backups found"
    echo ""
    exit 1
fi

BACKUP_NAME="$1"
BACKUP_FILE="${BACKUP_DIR}/${BACKUP_NAME}.sql.gz"

# Check if backup file exists
if [ ! -f "${BACKUP_FILE}" ]; then
    log_error "Backup file not found: ${BACKUP_FILE}"
    echo ""
    log_info "Available backups:"
    ls -1h "${BACKUP_DIR}"/*.sql.gz 2>/dev/null | xargs -n 1 basename | sed 's/.sql.gz$//' || log_warn "No backups found"
    echo ""
    exit 1
fi

# Load environment variables
if [ -f "${DOCKER_DIR}/.env" ]; then
    source "${DOCKER_DIR}/.env"
else
    log_error "Environment file not found: ${DOCKER_DIR}/.env"
    exit 1
fi

# Check if PostgreSQL container is running
cd "${DOCKER_DIR}"
if ! docker compose ps postgres | grep -q "Up"; then
    log_error "PostgreSQL container is not running"
    log_info "Start services with: ./start.sh"
    exit 1
fi

BACKUP_SIZE=$(du -h "${BACKUP_FILE}" | cut -f1)

log_warn "WARNING: This will replace all current database data!"
log_info "Restore details:"
echo "  Database: ${POSTGRES_DB}"
echo "  Backup file: ${BACKUP_FILE}"
echo "  Size: ${BACKUP_SIZE}"
echo ""

# Confirmation
echo -n "Are you sure you want to restore? (type 'yes' to confirm): "
read -r confirmation

if [ "$confirmation" != "yes" ]; then
    log_info "Restore cancelled"
    exit 0
fi

echo ""

# Create a safety backup before restore
log_step "Creating safety backup before restore..."
SAFETY_BACKUP="${BACKUP_DIR}/before_restore_$(date +%Y%m%d_%H%M%S).sql.gz"

docker exec pagodirecto_postgres pg_dump \
    -U "${POSTGRES_USER}" \
    -d "${POSTGRES_DB}" \
    --format=plain \
    --no-owner \
    --no-privileges \
    2>/dev/null | gzip > "${SAFETY_BACKUP}" || log_warn "Safety backup failed"

if [ -f "${SAFETY_BACKUP}" ]; then
    log_info "Safety backup created: ${SAFETY_BACKUP}"
fi
echo ""

# Stop backend to prevent connections
log_step "Stopping backend service..."
docker compose stop backend 2>/dev/null || true
echo ""

# Terminate active connections
log_step "Terminating active database connections..."
docker exec pagodirecto_postgres psql -U "${POSTGRES_USER}" -d postgres <<-EOSQL
    SELECT pg_terminate_backend(pg_stat_activity.pid)
    FROM pg_stat_activity
    WHERE pg_stat_activity.datname = '${POSTGRES_DB}'
      AND pid <> pg_backend_pid();
EOSQL
echo ""

# Drop and recreate database
log_step "Dropping and recreating database..."
docker exec pagodirecto_postgres psql -U "${POSTGRES_USER}" -d postgres <<-EOSQL
    DROP DATABASE IF EXISTS ${POSTGRES_DB};
    CREATE DATABASE ${POSTGRES_DB}
        WITH
        OWNER = ${POSTGRES_USER}
        ENCODING = 'UTF8'
        LC_COLLATE = 'en_US.utf8'
        LC_CTYPE = 'en_US.utf8'
        TABLESPACE = pg_default
        CONNECTION LIMIT = -1
        TEMPLATE = template0;
EOSQL
echo ""

# Restore backup
log_step "Restoring database from backup..."
gunzip -c "${BACKUP_FILE}" | docker exec -i pagodirecto_postgres psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" > /dev/null 2>&1

echo ""

# Verify restore
log_step "Verifying restore..."
TABLE_COUNT=$(docker exec pagodirecto_postgres psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema NOT IN ('pg_catalog', 'information_schema');")

log_info "Tables restored: ${TABLE_COUNT}"
echo ""

# Restart backend
log_step "Starting backend service..."
docker compose start backend
echo ""

echo "╔═══════════════════════════════════════════════════════╗"
echo "║         Restore Completed Successfully!               ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""
log_info "Database restored from: ${BACKUP_FILE}"
log_info "Safety backup saved: ${SAFETY_BACKUP}"
echo ""
log_info "Wait a few seconds for backend to restart, then access:"
echo "  Application: http://localhost:${FRONTEND_PORT:-3000}"
echo ""
