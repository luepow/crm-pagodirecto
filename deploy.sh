#!/bin/bash

###############################################################################
# PagoDirecto CRM - Deployment Script
#
# This script deploys the CRM system using:
# - PM2 for backend process management
# - Nginx as reverse proxy
#
# Usage: ./deploy.sh [start|stop|restart|status]
###############################################################################

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Directories
PROJECT_ROOT="/Users/lperez/Workspace/Development/fullstack/crm_pd"
BACKEND_DIR="$PROJECT_ROOT/backend"
FRONTEND_DIR="$PROJECT_ROOT/frontend/apps/web"
NGINX_CONF="$PROJECT_ROOT/nginx.conf"
PM2_CONFIG="$BACKEND_DIR/ecosystem.config.js"

# Functions
print_header() {
    echo -e "${GREEN}================================${NC}"
    echo -e "${GREEN}$1${NC}"
    echo -e "${GREEN}================================${NC}"
}

print_info() {
    echo -e "${YELLOW}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_dependencies() {
    print_header "Checking Dependencies"

    # Check PM2
    if ! command -v pm2 &> /dev/null; then
        print_error "PM2 is not installed. Install it with: npm install -g pm2"
        exit 1
    fi
    print_success "PM2 is installed"

    # Check Nginx
    if ! command -v nginx &> /dev/null; then
        print_error "Nginx is not installed. Install it with: brew install nginx"
        exit 1
    fi
    print_success "Nginx is installed"

    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed"
        exit 1
    fi
    print_success "Java is installed: $(java -version 2>&1 | head -n 1)"
}

start_backend() {
    print_header "Starting Backend with PM2"

    cd "$BACKEND_DIR"

    # Check if already running
    if pm2 list | grep -q "crm-backend.*online"; then
        print_info "Backend is already running. Restarting..."
        pm2 restart crm-backend
    else
        print_info "Starting backend..."
        pm2 start "$PM2_CONFIG"
    fi

    pm2 save
    print_success "Backend started successfully"
}

stop_backend() {
    print_header "Stopping Backend"

    if pm2 list | grep -q "crm-backend"; then
        pm2 stop crm-backend
        pm2 delete crm-backend
        print_success "Backend stopped"
    else
        print_info "Backend is not running"
    fi
}

start_nginx() {
    print_header "Starting Nginx"

    # Test nginx configuration
    sudo nginx -t -c "$NGINX_CONF"

    # Check if nginx is running
    if pgrep -x nginx > /dev/null; then
        print_info "Nginx is running. Reloading configuration..."
        sudo nginx -s reload -c "$NGINX_CONF"
    else
        print_info "Starting Nginx..."
        sudo nginx -c "$NGINX_CONF"
    fi

    print_success "Nginx started successfully"
}

stop_nginx() {
    print_header "Stopping Nginx"

    if pgrep -x nginx > /dev/null; then
        sudo nginx -s stop
        print_success "Nginx stopped"
    else
        print_info "Nginx is not running"
    fi
}

show_status() {
    print_header "Deployment Status"

    echo ""
    echo "=== Backend (PM2) ==="
    pm2 list | grep -E "crm-backend|App name"

    echo ""
    echo "=== Nginx ==="
    if pgrep -x nginx > /dev/null; then
        echo -e "${GREEN}Nginx is running${NC}"
        ps aux | grep nginx | grep -v grep
    else
        echo -e "${RED}Nginx is not running${NC}"
    fi

    echo ""
    echo "=== Access URLs ==="
    echo "Frontend: http://localhost"
    echo "Backend API: http://localhost/api"
    echo "Health Check: http://localhost/health"

    echo ""
    echo "=== Logs ==="
    echo "Backend PM2: pm2 logs crm-backend"
    echo "Nginx Access: tail -f $PROJECT_ROOT/logs/nginx-access.log"
    echo "Nginx Error: tail -f $PROJECT_ROOT/logs/nginx-error.log"
}

# Main script
case "${1:-}" in
    start)
        check_dependencies
        start_backend
        start_nginx
        show_status
        ;;

    stop)
        stop_backend
        stop_nginx
        print_success "All services stopped"
        ;;

    restart)
        check_dependencies
        stop_backend
        stop_nginx
        sleep 2
        start_backend
        start_nginx
        show_status
        ;;

    status)
        show_status
        ;;

    *)
        echo "Usage: $0 {start|stop|restart|status}"
        echo ""
        echo "Commands:"
        echo "  start   - Start backend (PM2) and nginx"
        echo "  stop    - Stop backend and nginx"
        echo "  restart - Restart all services"
        echo "  status  - Show deployment status"
        exit 1
        ;;
esac

print_success "Deployment script completed"
