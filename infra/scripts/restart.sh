#!/bin/bash
###############################################################################
# Restart Docker Services Script
#
# Usage: ./restart.sh [service] [profile]
#   service: Optional specific service name (backend, frontend, postgres, etc.)
#   profile: development (default), production, testing
#
# Author: PagoDirecto Infrastructure Team
###############################################################################

set -e

# Color codes
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="${SCRIPT_DIR}/../docker"

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

SERVICE="${1:-}"
PROFILE="${2:-development}"

echo ""
echo "╔═══════════════════════════════════════════════════════╗"
echo "║      PagoDirecto CRM/ERP - Restart Services          ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""

cd "${DOCKER_DIR}"

if [ -z "${SERVICE}" ]; then
    log_info "Restarting all services (profile: ${PROFILE})..."
    echo ""

    log_step "Stopping services..."
    docker compose --profile ${PROFILE} stop

    log_step "Starting services..."
    docker compose --profile ${PROFILE} up -d
else
    log_info "Restarting service: ${SERVICE}"
    echo ""

    docker compose restart ${SERVICE}
fi

echo ""
log_info "Services restarted successfully"
echo ""

# Show status
docker compose ps

echo ""
