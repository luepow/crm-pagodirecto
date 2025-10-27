#!/bin/bash
###############################################################################
# Start Docker Services Script
#
# Simple Analogy: Like starting your car - this script starts all parts of
# the application (database, backend, frontend) in the right order.
#
# Usage: ./start.sh [profile]
#   Profiles: development (default), production, testing
#
# Author: PagoDirecto Infrastructure Team
###############################################################################

set -e  # Exit on error

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="${SCRIPT_DIR}/../docker"

# Helper functions
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
PROFILE="${1:-development}"
ENV_FILE="${DOCKER_DIR}/.env.${PROFILE}"

# Banner
echo ""
echo "╔═══════════════════════════════════════════════════════╗"
echo "║       PagoDirecto CRM/ERP - Start Services           ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""

log_info "Profile: ${PROFILE}"
log_info "Docker directory: ${DOCKER_DIR}"
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    log_error "Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed (new version integrated in Docker)
if ! docker compose version &> /dev/null; then
    log_error "Docker Compose is not available. Please update Docker to latest version."
    log_info "Docker Compose v2 is now integrated into Docker CLI."
    exit 1
fi

# Check if Docker daemon is running
if ! docker info &> /dev/null; then
    log_error "Docker daemon is not running. Please start Docker first."
    exit 1
fi

# Check if environment file exists
if [ ! -f "${ENV_FILE}" ]; then
    log_warn "Environment file not found: ${ENV_FILE}"
    log_info "Using default .env file if available..."
    if [ -f "${DOCKER_DIR}/.env" ]; then
        ENV_FILE="${DOCKER_DIR}/.env"
    else
        log_error "No environment file found. Please create one from .env.example"
        log_info "Run: cp ${DOCKER_DIR}/.env.example ${DOCKER_DIR}/.env"
        exit 1
    fi
fi

log_info "Using environment file: ${ENV_FILE}"
echo ""

# Load environment variables from the env file so this script can use them
if [ -f "${ENV_FILE}" ]; then
    # Export all variables defined in the env file into current shell
    set -a
    . "${ENV_FILE}"
    set +a
fi

# Utilities to find free random ports (development convenience)
is_port_in_use() {
    local port=$1
    if command -v lsof >/dev/null 2>&1; then
        lsof -iTCP -sTCP:LISTEN -P -n | grep -q ":${port} "
        return $?
    elif command -v ss >/dev/null 2>&1; then
        ss -ltn | awk '{print $4}' | grep -E ":[${port}]$|:${port}$" -q
        return $?
    else
        nc -z localhost "$port" >/dev/null 2>&1
        return $?
    fi
}

random_port() {
    # Use high, likely-free ephemeral range
    echo $(( (RANDOM % 20000) + 20000 ))
}

pick_free_port() {
    local tries=50
    local port
    while [ $tries -gt 0 ]; do
        port=$(random_port)
        if ! is_port_in_use "$port"; then
            echo "$port"
            return 0
        fi
        tries=$((tries-1))
    done
    # Fallback to defaults if all else fails
    echo "$1"
}

# For development profile: avoid standard ports by choosing random free ones when needed
if [ "${PROFILE}" = "development" ]; then
    # Decide if we should randomize: if port is standard or already busy
    DEFAULT_PG=${POSTGRES_PORT:-5432}
    DEFAULT_BE=${BACKEND_PORT:-8080}
    DEFAULT_FE=${FRONTEND_PORT:-3000}
    DEFAULT_AD=${ADMINER_PORT:-8081}

    # PostgreSQL
    if [ "$DEFAULT_PG" = "5432" ] || is_port_in_use "$DEFAULT_PG"; then
        POSTGRES_PORT=$(pick_free_port 5432)
        export POSTGRES_PORT
        log_info "Asignando puerto aleatorio para PostgreSQL: ${POSTGRES_PORT}"
    else
        export POSTGRES_PORT
    fi

    # Backend
    if [ "$DEFAULT_BE" = "8080" ] || is_port_in_use "$DEFAULT_BE"; then
        BACKEND_PORT=$(pick_free_port 8080)
        export BACKEND_PORT
        log_info "Asignando puerto aleatorio para Backend: ${BACKEND_PORT}"
    else
        export BACKEND_PORT
    fi

    # Frontend
    if [ "$DEFAULT_FE" = "3000" ] || is_port_in_use "$DEFAULT_FE"; then
        FRONTEND_PORT=$(pick_free_port 3000)
        export FRONTEND_PORT
        log_info "Asignando puerto aleatorio para Frontend: ${FRONTEND_PORT}"
    else
        export FRONTEND_PORT
    fi

    # Adminer
    if [ "$DEFAULT_AD" = "8081" ] || is_port_in_use "$DEFAULT_AD"; then
        ADMINER_PORT=$(pick_free_port 8081)
        export ADMINER_PORT
        log_info "Asignando puerto aleatorio para Adminer: ${ADMINER_PORT}"
    else
        export ADMINER_PORT
    fi

    # Update dependent URLs for dev
    export VITE_API_URL="http://localhost:${BACKEND_PORT}/api"

    # Expand CORS origins to include the chosen frontend port
    if [ -z "${CORS_ALLOWED_ORIGINS}" ]; then
        export CORS_ALLOWED_ORIGINS="http://localhost:${FRONTEND_PORT},http://127.0.0.1:${FRONTEND_PORT}"
    else
        if ! echo "$CORS_ALLOWED_ORIGINS" | grep -q "localhost:${FRONTEND_PORT}"; then
            export CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS},http://localhost:${FRONTEND_PORT},http://127.0.0.1:${FRONTEND_PORT}"
        fi
    fi

    echo ""
    log_info "Puertos en uso (desarrollo):"
    echo "  Postgres: ${POSTGRES_PORT}"
    echo "  Backend:  ${BACKEND_PORT}"
    echo "  Frontend: ${FRONTEND_PORT}"
    echo "  Adminer:  ${ADMINER_PORT}"
    echo ""
fi

# Change to docker directory
cd "${DOCKER_DIR}"

# Stop any running containers first (cleanup)
log_step "Stopping any existing containers..."
docker compose --profile ${PROFILE} down 2>/dev/null || true
echo ""

# Pull latest images (optional, comment out for faster starts)
# log_step "Pulling latest images..."
# docker compose --profile ${PROFILE} pull
# echo ""

# Build images
log_step "Building Docker images..."
docker compose --profile ${PROFILE} build --parallel
echo ""

# Start services
log_step "Starting services..."
docker compose --env-file "${ENV_FILE}" --profile ${PROFILE} up -d
echo ""

# Wait for services to be healthy
log_step "Waiting for services to be healthy..."
echo ""

# Wait for PostgreSQL
log_info "Waiting for PostgreSQL..."
RETRIES=30
until docker compose exec -T postgres pg_isready -U "${POSTGRES_USER:-pagodirecto}" &>/dev/null || [ $RETRIES -eq 0 ]; do
    echo -n "."
    RETRIES=$((RETRIES-1))
    sleep 2
done
echo ""

if [ $RETRIES -eq 0 ]; then
    log_error "PostgreSQL failed to start"
    docker compose logs postgres
    exit 1
fi

log_info "PostgreSQL is ready"
echo ""

# Wait for Backend
log_info "Waiting for Backend API..."
RETRIES=60
until curl -sf http://localhost:${BACKEND_PORT:-8080}/actuator/health &>/dev/null || [ $RETRIES -eq 0 ]; do
    echo -n "."
    RETRIES=$((RETRIES-1))
    sleep 2
done
echo ""

if [ $RETRIES -eq 0 ]; then
    log_warn "Backend API may not be ready yet (timeout after 2 minutes)"
    log_info "Check logs with: ./logs.sh backend"
else
    log_info "Backend API is ready"
fi
echo ""

# Wait for Frontend
log_info "Waiting for Frontend..."
RETRIES=30
until curl -sf http://localhost:${FRONTEND_PORT:-3000}/health &>/dev/null || [ $RETRIES -eq 0 ]; do
    echo -n "."
    RETRIES=$((RETRIES-1))
    sleep 2
done
echo ""

if [ $RETRIES -eq 0 ]; then
    log_warn "Frontend may not be ready yet"
else
    log_info "Frontend is ready"
fi
echo ""

# Show status
log_step "Service Status:"
docker compose ps
echo ""

# Success message
echo "╔═══════════════════════════════════════════════════════╗"
echo "║            Services Started Successfully!             ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""
log_info "Access the application:"
echo ""
echo "  Frontend:  http://localhost:${FRONTEND_PORT:-3000}"
echo "  Backend:   http://localhost:${BACKEND_PORT:-8080}/api"
echo "  API Docs:  http://localhost:${BACKEND_PORT:-8080}/swagger-ui.html"
echo "  Adminer:   http://localhost:${ADMINER_PORT:-8081}"
echo "  Health:    http://localhost:${BACKEND_PORT:-8080}/actuator/health"
echo ""
log_info "View logs:"
echo "  All:       docker compose logs -f"
echo "  Backend:   docker compose logs -f backend"
echo "  Frontend:  docker compose logs -f frontend"
echo "  Database:  docker compose logs -f postgres"
echo ""
log_info "Or use the utility scripts:"
echo "  ./logs.sh [service]"
echo "  ./stop.sh"
echo "  ./restart.sh"
echo ""
