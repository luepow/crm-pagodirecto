#!/bin/bash

# Script de configuración para el proyecto CRM Backend
# Este script ayuda a configurar el entorno de desarrollo

set -e

echo "=================================================="
echo "  Configuración del Proyecto CRM Backend"
echo "=================================================="
echo ""

# Verificar Java
echo "1. Verificando instalación de Java..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo "   ✓ Java encontrado: $JAVA_VERSION"
else
    echo "   ✗ Java no encontrado"
    echo "   Por favor instala Java 21:"
    echo "   brew install --cask corretto"
    exit 1
fi

# Verificar Maven
echo ""
echo "2. Verificando instalación de Maven..."
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1)
    echo "   ✓ Maven encontrado: $MVN_VERSION"
else
    echo "   ✗ Maven no encontrado"
    echo "   Instalando Maven..."
    brew install maven
fi

# Limpiar compilaciones anteriores
echo ""
echo "3. Limpiando compilaciones anteriores..."
if [ -d "target" ]; then
    rm -rf target
    echo "   ✓ Carpeta target eliminada"
fi

# Compilar el proyecto
echo ""
echo "4. Compilando el proyecto..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo "   ✓ Compilación exitosa"
else
    echo "   ✗ Error en la compilación"
    exit 1
fi

# Verificar PostgreSQL
echo ""
echo "5. Verificando PostgreSQL..."
if command -v psql &> /dev/null; then
    echo "   ✓ PostgreSQL encontrado"

    # Verificar si el servicio está corriendo
    if pg_isready &> /dev/null; then
        echo "   ✓ PostgreSQL está corriendo"
    else
        echo "   ⚠ PostgreSQL no está corriendo"
        echo "   Iniciando PostgreSQL..."
        brew services start postgresql
    fi
else
    echo "   ⚠ PostgreSQL no encontrado"
    echo "   ¿Deseas instalar PostgreSQL? (s/n)"
    read -r INSTALL_PG
    if [ "$INSTALL_PG" = "s" ] || [ "$INSTALL_PG" = "S" ]; then
        brew install postgresql
        brew services start postgresql
        echo "   ✓ PostgreSQL instalado e iniciado"
    fi
fi

# Crear base de datos si no existe
echo ""
echo "6. Configurando base de datos..."
if command -v psql &> /dev/null && pg_isready &> /dev/null; then
    if psql -lqt | cut -d \| -f 1 | grep -qw crm_db; then
        echo "   ✓ Base de datos 'crm_db' ya existe"
    else
        echo "   Creando base de datos 'crm_db'..."
        psql postgres <<EOF
CREATE DATABASE crm_db;
CREATE USER crm_user WITH PASSWORD 'crm_password';
GRANT ALL PRIVILEGES ON DATABASE crm_db TO crm_user;
EOF
        echo "   ✓ Base de datos creada"
    fi
fi

echo ""
echo "=================================================="
echo "  Configuración completada exitosamente"
echo "=================================================="
echo ""
echo "Próximos pasos:"
echo ""
echo "1. Abre IntelliJ IDEA"
echo "2. File → Invalidate Caches → Invalidate and Restart"
echo "3. Espera a que IntelliJ indexe el proyecto"
echo "4. Haz clic derecho en CrmApplication.java"
echo "5. Selecciona 'Run CrmApplication'"
echo ""
echo "O ejecuta desde terminal:"
echo "  mvn spring-boot:run"
echo ""
echo "La aplicación estará disponible en:"
echo "  http://localhost:28080"
echo "  Swagger UI: http://localhost:28080/swagger-ui.html"
echo ""
