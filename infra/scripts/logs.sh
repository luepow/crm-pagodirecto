#!/bin/bash
###############################################################################
# View Docker Service Logs Script
#
# Simple Analogy: Like reading the logbook of a ship - shows what has been
# happening with each part of the application.
#
# Usage: ./logs.sh [service] [lines]
#   service: Optional service name (backend, frontend, postgres, adminer, nginx)
#   lines: Optional number of lines to show (default: all, use -f for follow)
#
# Examples:
#   ./logs.sh              # Show all logs
#   ./logs.sh backend      # Show backend logs
#   ./logs.sh backend -f   # Follow backend logs in real-time
#   ./logs.sh backend 100  # Show last 100 lines of backend logs
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

SERVICE="${1:-}"
LINES="${2:-}"

cd "${DOCKER_DIR}"

echo ""
echo "╔═══════════════════════════════════════════════════════╗"
echo "║       PagoDirecto CRM/ERP - Service Logs             ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""

if [ -z "${SERVICE}" ]; then
    log_info "Showing logs for all services..."
    echo ""

    if [ "${LINES}" = "-f" ]; then
        docker compose logs -f
    elif [ -n "${LINES}" ]; then
        docker compose logs --tail=${LINES}
    else
        docker compose logs
    fi
else
    log_info "Showing logs for service: ${SERVICE}"
    echo ""

    if [ "${LINES}" = "-f" ]; then
        docker compose logs -f ${SERVICE}
    elif [ -n "${LINES}" ]; then
        docker compose logs --tail=${LINES} ${SERVICE}
    else
        docker compose logs ${SERVICE}
    fi
fi
