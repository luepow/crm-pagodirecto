#!/bin/bash

###############################################################################
# Script de Deployment del Backend - PagoDirecto CRM
#
# Este script:
# 1. Compila el backend Java localmente
# 2. (Opcional) Sube el JAR al servidor via SCP
# 3. Reinicia el servicio con PM2
#
# Uso:
#   ./deploy-backend.sh              # Deploy local
#   ./deploy-backend.sh --remote     # Deploy a servidor remoto
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
BACKEND_DIR="$PROJECT_ROOT/backend"
JAR_PATH="$BACKEND_DIR/application/target/application-1.0.0-SNAPSHOT.jar"

# Remote server configuration (configure según tu servidor)
REMOTE_USER="${REMOTE_USER:-root}"
REMOTE_HOST="${REMOTE_HOST:-your-server.com}"
REMOTE_PATH="${REMOTE_PATH:-/opt/crm-backend}"
PM2_APP_NAME="crm-backend"

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

print_header "PagoDirecto CRM - Backend Deployment"
echo ""
echo "Modo: $DEPLOY_MODE"
echo ""

# Step 1: Compilar el backend
print_header "1. Compilando Backend Java"
echo ""

cd "$BACKEND_DIR"

if [ -f "mvnw" ]; then
    print_info "Usando Maven Wrapper..."
    ./mvnw clean package -DskipTests
elif command -v mvn &> /dev/null; then
    print_info "Usando Maven instalado..."
    mvn clean package -DskipTests
else
    print_error "Maven no está disponible"
    exit 1
fi

if [ $? -ne 0 ]; then
    print_error "La compilación falló"
    exit 1
fi

print_success "Backend compilado exitosamente"
echo ""

# Verificar que el JAR existe
if [ ! -f "$JAR_PATH" ]; then
    print_error "JAR no encontrado en: $JAR_PATH"
    exit 1
fi

JAR_SIZE=$(du -h "$JAR_PATH" | cut -f1)
print_info "Tamaño del JAR: $JAR_SIZE"
echo ""

# Step 2: Deployment según modo
if [ "$DEPLOY_MODE" == "local" ]; then
    print_header "2. Reiniciando Servicio Local (PM2)"
    echo ""

    # Verificar que PM2 esté instalado
    if ! command -v pm2 &> /dev/null; then
        print_error "PM2 no está instalado. Instálalo con: npm install -g pm2"
        exit 1
    fi

    # Verificar que el proceso existe
    if pm2 list | grep -q "$PM2_APP_NAME"; then
        print_info "Reiniciando $PM2_APP_NAME..."
        pm2 restart "$PM2_APP_NAME"
        pm2 save
        print_success "Servicio reiniciado"
    else
        print_info "El servicio no existe. Iniciando..."
        cd "$BACKEND_DIR"
        pm2 start ecosystem.config.js
        pm2 save
        print_success "Servicio iniciado"
    fi

    echo ""
    print_info "Esperando 10 segundos para que el servicio arranque..."
    sleep 10

    # Verificar que está corriendo
    if pm2 list | grep -q "$PM2_APP_NAME.*online"; then
        print_success "✅ Backend desplegado y corriendo localmente"
        echo ""
        echo "📊 Estado del servicio:"
        pm2 show "$PM2_APP_NAME" | grep -E "status|uptime|memory|cpu"
    else
        print_error "El servicio no arrancó correctamente"
        echo ""
        echo "Logs:"
        pm2 logs "$PM2_APP_NAME" --lines 20 --nostream
        exit 1
    fi

elif [ "$DEPLOY_MODE" == "remote" ]; then
    print_header "2. Desplegando a Servidor Remoto"
    echo ""

    print_info "Servidor: $REMOTE_USER@$REMOTE_HOST"
    print_info "Ruta remota: $REMOTE_PATH"
    echo ""

    # Crear backup del JAR anterior en el servidor
    print_info "Creando backup del JAR anterior..."
    ssh "$REMOTE_USER@$REMOTE_HOST" "cd $REMOTE_PATH && [ -f application.jar ] && mv application.jar application.jar.backup.$(date +%Y%m%d_%H%M%S) || true"

    # Subir el nuevo JAR
    print_info "Subiendo nuevo JAR al servidor..."
    scp "$JAR_PATH" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_PATH/application.jar"

    if [ $? -ne 0 ]; then
        print_error "Falló la subida del JAR al servidor"
        exit 1
    fi

    print_success "JAR subido exitosamente"
    echo ""

    # Reiniciar el servicio en el servidor remoto
    print_info "Reiniciando servicio en el servidor..."
    ssh "$REMOTE_USER@$REMOTE_HOST" << 'ENDSSH'
        cd /opt/crm-backend
        pm2 restart crm-backend || pm2 start ecosystem.config.js
        pm2 save

        echo "Esperando 10 segundos para que el servicio arranque..."
        sleep 10

        # Verificar estado
        pm2 list
ENDSSH

    if [ $? -ne 0 ]; then
        print_error "Falló el reinicio del servicio en el servidor"
        exit 1
    fi

    print_success "✅ Backend desplegado exitosamente en el servidor"
fi

echo ""
print_header "Deployment Completado"
echo ""
print_success "🎉 Backend actualizado y corriendo"
echo ""
echo "📝 Próximos pasos:"
echo "   - Verificar logs: pm2 logs $PM2_APP_NAME"
echo "   - Verificar estado: pm2 status"
if [ "$DEPLOY_MODE" == "local" ]; then
    echo "   - Probar API: curl http://localhost:8082/api/actuator/health"
else
    echo "   - Probar API: curl http://$REMOTE_HOST/api/actuator/health"
fi
echo ""
