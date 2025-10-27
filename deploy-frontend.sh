#!/bin/bash

###############################################################################
# Script de Deployment del Frontend - PagoDirecto CRM
#
# Este script:
# 1. Compila el frontend React localmente
# 2. (Opcional) Sube los archivos al servidor via rsync/SCP
# 3. Recarga Nginx
#
# Uso:
#   ./deploy-frontend.sh              # Deploy local
#   ./deploy-frontend.sh --remote     # Deploy a servidor remoto
###############################################################################

set -e  # Exit on error

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_ROOT="/Users/lperez/Workspace/Development/fullstack/crm_pd"
FRONTEND_DIR="$PROJECT_ROOT/frontend/apps/web"
DIST_DIR="$FRONTEND_DIR/dist"

# Remote server configuration (configure según tu servidor)
REMOTE_USER="${REMOTE_USER:-root}"
REMOTE_HOST="${REMOTE_HOST:-your-server.com}"
REMOTE_PATH="${REMOTE_PATH:-/opt/crm-frontend}"
NGINX_CONF="${NGINX_CONF:-/etc/nginx/nginx.conf}"

# Functions
print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ ERROR: $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ️  $1${NC}"
}

# Parse arguments
DEPLOY_MODE="local"
if [[ "$1" == "--remote" ]]; then
    DEPLOY_MODE="remote"
fi

print_header "PagoDirecto CRM - Frontend Deployment"
echo ""
echo "Modo: $DEPLOY_MODE"
echo ""

# Step 1: Limpiar build anterior
print_header "1. Limpiando Build Anterior"
echo ""

cd "$FRONTEND_DIR"

if [ -d "$DIST_DIR" ]; then
    print_info "Eliminando directorio dist anterior..."
    rm -rf "$DIST_DIR"
    print_success "Build anterior eliminado"
else
    print_info "No hay build anterior para limpiar"
fi

echo ""

# Step 2: Compilar el frontend
print_header "2. Compilando Frontend React"
echo ""

cd "$FRONTEND_DIR"

# Verificar que node_modules existe
if [ ! -d "node_modules" ]; then
    print_info "Instalando dependencias npm..."
    npm install

    if [ $? -ne 0 ]; then
        print_error "La instalación de dependencias falló"
        exit 1
    fi
fi

print_info "Compilando con Vite (modo producción)..."
npx vite build --mode production

if [ $? -ne 0 ]; then
    print_error "La compilación falló"
    exit 1
fi

print_success "Frontend compilado exitosamente"
echo ""

# Verificar que el directorio dist existe y tiene contenido
if [ ! -d "$DIST_DIR" ]; then
    print_error "Directorio dist no encontrado en: $DIST_DIR"
    exit 1
fi

if [ ! -f "$DIST_DIR/index.html" ]; then
    print_error "index.html no encontrado en dist/"
    exit 1
fi

# Mostrar tamaño del build
DIST_SIZE=$(du -sh "$DIST_DIR" | cut -f1)
print_info "Tamaño del build: $DIST_SIZE"
echo ""

# Step 3: Deployment según modo
if [ "$DEPLOY_MODE" == "local" ]; then
    print_header "3. Desplegando Frontend Local"
    echo ""

    print_info "Frontend compilado en: $DIST_DIR"
    print_info "Nginx servirá los archivos desde esta ubicación"
    echo ""

    # Verificar que Nginx esté corriendo
    if pgrep -x nginx > /dev/null; then
        print_info "Nginx está corriendo. Recargando configuración..."

        # Intentar recargar Nginx (requiere que el usuario tenga permisos)
        if sudo nginx -s reload 2>/dev/null; then
            print_success "Nginx recargado exitosamente"
        else
            print_info "No se pudo recargar Nginx automáticamente"
            print_info "Ejecuta manualmente: sudo nginx -s reload"
        fi
    else
        print_info "Nginx no está corriendo. Inicia Nginx con:"
        print_info "  ./start-nginx.sh"
        print_info "  O: sudo nginx -c $PROJECT_ROOT/nginx.conf"
    fi

    echo ""
    print_success "✅ Frontend desplegado localmente"
    echo ""
    echo "📊 Acceso:"
    echo "   Frontend: http://localhost"
    echo "   API:      http://localhost/api"

elif [ "$DEPLOY_MODE" == "remote" ]; then
    print_header "3. Desplegando a Servidor Remoto"
    echo ""

    print_info "Servidor: $REMOTE_USER@$REMOTE_HOST"
    print_info "Ruta remota: $REMOTE_PATH"
    echo ""

    # Crear backup del frontend anterior en el servidor
    print_info "Creando backup del frontend anterior..."
    ssh "$REMOTE_USER@$REMOTE_HOST" "cd $REMOTE_PATH && [ -d dist ] && mv dist dist.backup.$(date +%Y%m%d_%H%M%S) || true"

    # Crear directorio si no existe
    print_info "Asegurando que el directorio remoto existe..."
    ssh "$REMOTE_USER@$REMOTE_HOST" "mkdir -p $REMOTE_PATH"

    # Subir el nuevo build usando rsync (más eficiente que scp para directorios)
    print_info "Subiendo archivos al servidor (esto puede tomar un momento)..."

    if command -v rsync &> /dev/null; then
        # Usar rsync si está disponible (más eficiente)
        rsync -avz --delete "$DIST_DIR/" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_PATH/dist/"
    else
        # Fallback a scp si rsync no está disponible
        scp -r "$DIST_DIR" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_PATH/"
    fi

    if [ $? -ne 0 ]; then
        print_error "Falló la subida de archivos al servidor"
        exit 1
    fi

    print_success "Archivos subidos exitosamente"
    echo ""

    # Recargar Nginx en el servidor remoto
    print_info "Recargando Nginx en el servidor..."
    ssh "$REMOTE_USER@$REMOTE_HOST" << 'ENDSSH'
        # Verificar configuración de Nginx
        nginx -t

        if [ $? -eq 0 ]; then
            # Recargar Nginx
            nginx -s reload || systemctl reload nginx
            echo "Nginx recargado exitosamente"
        else
            echo "ERROR: Configuración de Nginx inválida"
            exit 1
        fi

        # Verificar que Nginx está corriendo
        if pgrep -x nginx > /dev/null || systemctl is-active --quiet nginx; then
            echo "✅ Nginx está corriendo"
        else
            echo "⚠️ Nginx no está corriendo. Iniciando..."
            systemctl start nginx
        fi
ENDSSH

    if [ $? -ne 0 ]; then
        print_error "Falló la recarga de Nginx en el servidor"
        exit 1
    fi

    print_success "✅ Frontend desplegado exitosamente en el servidor"
fi

echo ""
print_header "Deployment Completado"
echo ""
print_success "🎉 Frontend actualizado"
echo ""
echo "📝 Próximos pasos:"
if [ "$DEPLOY_MODE" == "local" ]; then
    echo "   - Verificar en navegador: http://localhost"
    echo "   - Ver logs de Nginx: tail -f logs/nginx-access.log"
    echo "   - Verificar estado: ps aux | grep nginx"
else
    echo "   - Verificar en navegador: http://$REMOTE_HOST"
    echo "   - Ver logs remotos: ssh $REMOTE_USER@$REMOTE_HOST 'tail -f /var/log/nginx/access.log'"
    echo "   - Verificar estado: ssh $REMOTE_USER@$REMOTE_HOST 'systemctl status nginx'"
fi
echo ""

# Mostrar estadísticas del build
echo "📊 Estadísticas del Build:"
echo "   Archivos compilados: $(find "$DIST_DIR" -type f | wc -l | tr -d ' ') archivos"
echo "   Tamaño total: $DIST_SIZE"
echo "   HTML: $(find "$DIST_DIR" -name "*.html" | wc -l | tr -d ' ') archivo(s)"
echo "   JS: $(find "$DIST_DIR" -name "*.js" | wc -l | tr -d ' ') archivo(s)"
echo "   CSS: $(find "$DIST_DIR" -name "*.css" | wc -l | tr -d ' ') archivo(s)"
echo ""
