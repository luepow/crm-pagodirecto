#!/bin/bash
###############################################################################
# Stop Docker Services Script
#
# Simple Analogy: Like turning off your car - this stops all running parts
# of the application safely.
#
# Usage: ./stop.sh [profile]
#
# Author: PagoDirecto Infrastructure Team
###############################################################################

set -e

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="${SCRIPT_DIR}/../docker"

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

PROFILE="${1:-development}"

echo ""
echo "╔═══════════════════════════════════════════════════════╗"
echo "║       PagoDirecto CRM/ERP - Stop Services            ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""

log_info "Profile: ${PROFILE}"
log_info "Stopping services..."
echo ""

cd "${DOCKER_DIR}"

# Stop services gracefully
docker compose --profile ${PROFILE} stop

log_info "Services stopped"
echo ""

# Show remaining containers
log_info "Container status:"
docker compose ps
echo ""

echo "╔═══════════════════════════════════════════════════════╗"
echo "║            Services Stopped Successfully!             ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""
log_info "To remove containers and networks, run:"
echo "  docker compose down"
echo ""
log_info "To remove volumes as well (WARNING: deletes data!):"
echo "  docker compose down -v"
echo ""
