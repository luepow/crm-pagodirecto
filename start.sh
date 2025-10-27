#!/bin/bash

# Script de inicio rápido para CRM Backend
# Ejecutar con: ./start.sh

set -e

echo "🚀 Iniciando CRM Backend..."
echo ""

# Verificar si Docker está corriendo
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker no está corriendo"
    echo "   Por favor inicia Docker Desktop y vuelve a intentar"
    exit 1
fi

echo "✅ Docker está corriendo"
echo ""

# Levantar PostgreSQL
echo "📦 Iniciando PostgreSQL..."
docker compose up postgres -d

# Esperar a que PostgreSQL esté listo
echo "⏳ Esperando a que PostgreSQL esté listo..."
sleep 5

# Verificar que PostgreSQL está corriendo
if docker compose ps postgres | grep -q "Up"; then
    echo "✅ PostgreSQL está corriendo"
else
    echo "❌ Error: PostgreSQL no pudo iniciarse"
    docker compose logs postgres
    exit 1
fi

echo ""
echo "🔨 Compilando backend..."
cd crm-backend

# Compilar proyecto
./mvnw clean compile

echo ""
echo "🚀 Iniciando backend en puerto 28080..."
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Backend corriendo en: http://localhost:28080"
echo "  Swagger UI: http://localhost:28080/swagger-ui.html"
echo "  Health Check: http://localhost:28080/actuator/health"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "👤 Usuario por defecto:"
echo "   Username: admin"
echo "   Password: admin123"
echo ""
echo "Para detener: Ctrl+C"
echo ""

# Ejecutar aplicación
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
