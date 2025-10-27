#!/bin/bash

echo "🔄 Reiniciando aplicación PagoDirecto CRM..."
echo ""

# Detener contenedores
echo "⏹️  Deteniendo contenedores..."
cd infra/docker
docker compose --profile development down

# Limpiar volúmenes de maven para forzar recompilación
echo "🧹 Limpiando caché de Maven..."
docker volume rm pagodirecto_maven_cache 2>/dev/null || true

# Reconstruir backend
echo "🔨 Reconstruyendo backend..."
docker compose --profile development build backend

# Iniciar todos los servicios
echo "🚀 Iniciando servicios..."
docker compose --profile development up -d

echo ""
echo "✅ Aplicación iniciada!"
echo ""
echo "📊 Servicios disponibles:"
echo "  - Frontend: http://localhost:23000"
echo "  - Backend:  http://localhost:28080"
echo "  - Database: localhost:25432"
echo "  - Adminer:  http://localhost:8081"
echo ""
echo "📝 Ver logs del backend:"
echo "  docker compose --profile development logs -f backend"
echo ""
