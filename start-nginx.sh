#!/bin/bash

###############################################################################
# Script para iniciar Nginx - PagoDirecto CRM
###############################################################################

PROJECT_ROOT="/Users/lperez/Workspace/Development/fullstack/crm_pd"
NGINX_CONF="$PROJECT_ROOT/nginx.conf"

echo "================================"
echo "Iniciando Nginx para PagoDirecto CRM"
echo "================================"
echo ""

# Verificar que nginx esté instalado
if ! command -v nginx &> /dev/null; then
    echo "❌ ERROR: Nginx no está instalado"
    echo "Instala Nginx con: brew install nginx"
    exit 1
fi

# Verificar configuración
echo "📋 Verificando configuración de Nginx..."
sudo nginx -t -c "$NGINX_CONF"

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ ERROR: La configuración de Nginx tiene errores"
    exit 1
fi

echo ""
echo "✅ Configuración válida"
echo ""

# Verificar si nginx ya está corriendo
if pgrep -x nginx > /dev/null; then
    echo "⚠️  Nginx ya está corriendo. Recargando configuración..."
    sudo nginx -s reload -c "$NGINX_CONF"
    echo ""
    echo "✅ Nginx recargado exitosamente"
else
    echo "🚀 Iniciando Nginx..."
    sudo nginx -c "$NGINX_CONF"
    echo ""
    echo "✅ Nginx iniciado exitosamente"
fi

echo ""
echo "================================"
echo "✅ Sistema listo!"
echo "================================"
echo ""
echo "📊 URLs de acceso:"
echo "   Frontend: http://localhost"
echo "   API:      http://localhost/api"
echo ""
echo "🔐 Credenciales:"
echo "   Email:    admin@admin.com"
echo "   Password: admin123"
echo ""
echo "💡 Comandos útiles:"
echo "   Ver logs del backend: pm2 logs crm-backend"
echo "   Ver estado PM2:       pm2 status"
echo "   Detener Nginx:        sudo nginx -s stop"
echo "   Ver logs Nginx:       tail -f logs/nginx-access.log"
echo ""
