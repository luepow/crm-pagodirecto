#!/bin/bash
###############################################################################
# Clean Docker Resources Script
#
# Simple Analogy: Like deep cleaning your house - removes old containers,
# unused images, and optionally data to free up space.
#
# Usage: ./clean.sh [--all] [--volumes] [--force]
#   --all: Remove all Docker resources including images
#   --volumes: Remove volumes (WARNING: deletes data!)
#   --force: Skip confirmation prompts
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

# Parse arguments
CLEAN_ALL=false
CLEAN_VOLUMES=false
FORCE=false

for arg in "$@"; do
    case $arg in
        --all)
            CLEAN_ALL=true
            ;;
        --volumes)
            CLEAN_VOLUMES=true
            ;;
        --force)
            FORCE=true
            ;;
        *)
            log_error "Unknown argument: $arg"
            echo "Usage: ./clean.sh [--all] [--volumes] [--force]"
            exit 1
            ;;
    esac
done

echo ""
echo "╔═══════════════════════════════════════════════════════╗"
echo "║      PagoDirecto CRM/ERP - Clean Resources           ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""

cd "${DOCKER_DIR}"

# Show what will be cleaned
log_info "Cleanup plan:"
echo "  - Stop and remove containers: YES"
echo "  - Remove networks: YES"
echo "  - Remove images: $([ "$CLEAN_ALL" = true ] && echo "YES" || echo "NO")"
echo "  - Remove volumes: $([ "$CLEAN_VOLUMES" = true ] && echo "YES (DATA WILL BE DELETED!)" || echo "NO")"
echo ""

# Confirmation
if [ "$FORCE" != true ]; then
    if [ "$CLEAN_VOLUMES" = true ]; then
        log_warn "WARNING: This will delete all database data!"
        echo -n "Are you absolutely sure? (type 'yes' to confirm): "
        read -r confirmation
        if [ "$confirmation" != "yes" ]; then
            log_info "Cleanup cancelled"
            exit 0
        fi
    else
        echo -n "Continue with cleanup? (y/n): "
        read -r confirmation
        if [ "$confirmation" != "y" ]; then
            log_info "Cleanup cancelled"
            exit 0
        fi
    fi
fi

echo ""

# Stop containers
log_step "Stopping containers..."
docker compose down --remove-orphans
echo ""

# Remove volumes if requested
if [ "$CLEAN_VOLUMES" = true ]; then
    log_step "Removing volumes..."
    docker compose down -v
    log_warn "Database data has been deleted!"
    echo ""
fi

# Remove images if requested
if [ "$CLEAN_ALL" = true ]; then
    log_step "Removing images..."

    # Remove project images
    docker images | grep "pagodirecto" | awk '{print $3}' | xargs -r docker rmi -f 2>/dev/null || true

    # Remove dangling images
    docker image prune -f
    echo ""

    log_step "Removing build cache..."
    docker builder prune -f
    echo ""
fi

# Clean up system
log_step "Cleaning up unused Docker resources..."
docker system prune -f
echo ""

# Show disk space reclaimed
echo ""
log_info "Cleanup completed!"
echo ""
log_info "Current Docker disk usage:"
docker system df
echo ""

echo "╔═══════════════════════════════════════════════════════╗"
echo "║          Cleanup Completed Successfully!              ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""
log_info "To start services again, run: ./start.sh"
echo ""
