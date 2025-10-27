#!/bin/bash
###############################################################################
# Database Backup Script
#
# Simple Analogy: Like making a photocopy of all your important documents
# and storing them in a safe place. If something happens to the originals,
# you can restore from the backup.
#
# Usage: ./backup-db.sh [backup_name]
#   backup_name: Optional custom name for backup (default: timestamp)
#
# Technical: Creates PostgreSQL dump using pg_dump. Includes all schemas,
# data, and sequences. Compressed with gzip. Stored in backups directory.
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
echo "║       PagoDirecto CRM/ERP - Database Backup          ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""

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

# Create backup directory if it doesn't exist
mkdir -p "${BACKUP_DIR}"

# Generate backup name
BACKUP_NAME="${1:-}"
if [ -z "${BACKUP_NAME}" ]; then
    TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
    BACKUP_NAME="pagodirecto_crm_${TIMESTAMP}"
fi

BACKUP_FILE="${BACKUP_DIR}/${BACKUP_NAME}.sql.gz"

log_info "Backup details:"
echo "  Database: ${POSTGRES_DB}"
echo "  Container: pagodirecto_postgres"
echo "  Backup file: ${BACKUP_FILE}"
echo ""

# Create backup
log_step "Creating database backup..."

docker exec pagodirecto_postgres pg_dump \
    -U "${POSTGRES_USER}" \
    -d "${POSTGRES_DB}" \
    --format=plain \
    --no-owner \
    --no-privileges \
    --verbose \
    2>&1 | gzip > "${BACKUP_FILE}"

# Check if backup was successful
if [ -f "${BACKUP_FILE}" ]; then
    BACKUP_SIZE=$(du -h "${BACKUP_FILE}" | cut -f1)

    echo ""
    log_info "Backup completed successfully!"
    echo "  File: ${BACKUP_FILE}"
    echo "  Size: ${BACKUP_SIZE}"
    echo ""

    # List recent backups
    log_info "Recent backups:"
    ls -lh "${BACKUP_DIR}" | tail -n 5
    echo ""

    # Cleanup old backups (keep last 30 days)
    log_step "Cleaning up old backups (retention: 30 days)..."
    find "${BACKUP_DIR}" -name "*.sql.gz" -type f -mtime +30 -delete 2>/dev/null || true

    BACKUP_COUNT=$(ls -1 "${BACKUP_DIR}"/*.sql.gz 2>/dev/null | wc -l)
    log_info "Total backups: ${BACKUP_COUNT}"
    echo ""

    echo "╔═══════════════════════════════════════════════════════╗"
    echo "║          Backup Completed Successfully!               ║"
    echo "╚═══════════════════════════════════════════════════════╝"
    echo ""
    log_info "To restore this backup, run:"
    echo "  ./restore-db.sh ${BACKUP_NAME}"
    echo ""
else
    log_error "Backup failed!"
    exit 1
fi
