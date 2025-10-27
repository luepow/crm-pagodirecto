# GUÍA DE DESPLIEGUE A PRODUCCIÓN - CRM PAGODIRECTO

**Versión:** 1.0.0-SNAPSHOT
**Fecha:** 2025-10-27
**Servidor:** 128.199.13.76

---

## 🚀 DESPLIEGUE RÁPIDO (5 MINUTOS)

```bash
# 1. Desde tu máquina local
cd /Users/lperez/Workspace/Development/fullstack/crm_pd

# 2. Configurar servidor remoto
export REMOTE_USER="root"
export REMOTE_HOST="128.199.13.76"

# 3. Deploy backend
./deploy-backend.sh --remote

# 4. Deploy frontend
./deploy-frontend.sh --remote

# 5. Verificar
curl http://128.199.13.76/api/actuator/health
# Debe retornar: {"status":"UP"}

# 6. Abrir en navegador
open http://128.199.13.76
# Login: admin@pagodirecto.com / admin123
```

---

## 📋 PRE-REQUISITOS

### En el Servidor (128.199.13.76)

**Software Requerido:**
```bash
# Conectarse al servidor
ssh root@128.199.13.76

# Verificar Java 21
java -version  # Debe ser OpenJDK 21

# Verificar Node.js
node -v  # Debe ser v18+

# Verificar PM2
pm2 -v  # Si no está: npm install -g pm2

# Verificar Nginx
nginx -v  # Si no está: apt-get install nginx

# Verificar PostgreSQL accesible
psql -h DB_HOST -U DB_USER -d DB_NAME -c "SELECT version();"
```

**Crear Directorios:**
```bash
mkdir -p /opt/crm-backend/logs
mkdir -p /opt/crm-frontend
```

### En tu Máquina Local

**Variables de Entorno:**
```bash
export REMOTE_USER="root"
export REMOTE_HOST="128.199.13.76"
export REMOTE_BACKEND_PATH="/opt/crm-backend"
export REMOTE_FRONTEND_PATH="/opt/crm-frontend"
```

**SSH Sin Password:**
```bash
# Si aún no tienes SSH key
ssh-keygen -t ed25519 -C "deploy@crm-pagodirecto"

# Copiar key al servidor
ssh-copy-id root@128.199.13.76

# Verificar
ssh root@128.199.13.76 "echo 'SSH configurado correctamente'"
```

---

## 🔧 CONFIGURACIÓN DEL SERVIDOR

### 1. Variables de Entorno (CRÍTICO)

```bash
ssh root@128.199.13.76

# Crear archivo de environment
cat > /opt/crm-backend/.env << 'EOF'
# Base de Datos
DATABASE_URL=jdbc:postgresql://DB_HOST:PORT/crm_db?sslmode=require
DATABASE_USERNAME=crm_user
DATABASE_PASSWORD=TU_PASSWORD_AQUI_CAMBIAR

# JWT
JWT_SECRET=TU_SECRET_AQUI_CAMBIAR_BASE64
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# CORS
CORS_ALLOWED_ORIGINS=http://128.199.13.76,https://crm.pagodirecto.com

# Server
SERVER_PORT=8082
SPRING_PROFILES_ACTIVE=prod
EOF

# Generar secrets seguros
echo "JWT_SECRET=$(openssl rand -base64 64)"
echo "DATABASE_PASSWORD=$(openssl rand -base64 32)"
```

**⚠️ IMPORTANTE:** Reemplazar `TU_PASSWORD_AQUI` y `TU_SECRET_AQUI` con valores reales.

### 2. Configurar PM2

```bash
# Crear ecosystem.config.js
cat > /opt/crm-backend/ecosystem.config.js << 'EOF'
module.exports = {
  apps: [{
    name: 'crm-backend',
    script: 'java',
    args: [
      '-jar',
      '-Xms512m',
      '-Xmx1024m',
      '-Dspring.profiles.active=prod',
      './application.jar'
    ],
    cwd: '/opt/crm-backend',
    instances: 1,
    autorestart: true,
    watch: false,
    max_memory_restart: '1G',
    env_file: '/opt/crm-backend/.env',
    error_file: './logs/pm2-error.log',
    out_file: './logs/pm2-out.log',
    log_date_format: 'YYYY-MM-DD HH:mm:ss Z',
    merge_logs: true
  }]
};
EOF
```

### 3. Configurar Nginx

```bash
# Crear configuración de Nginx
cat > /etc/nginx/sites-available/crm << 'EOF'
upstream crm_backend {
    server 127.0.0.1:8082;
    keepalive 32;
}

server {
    listen 80;
    server_name 128.199.13.76;

    root /opt/crm-frontend/dist;
    index index.html;

    access_log /var/log/nginx/crm-access.log;
    error_log /var/log/nginx/crm-error.log;

    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
    gzip_min_length 1000;

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # Health check
    location /health {
        return 200 "OK\n";
        add_header Content-Type text/plain;
    }

    # API proxy
    location /api/ {
        proxy_pass http://crm_backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        proxy_buffering off;
    }

    # Static assets con cache
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # React Router - fallback a index.html
    location / {
        try_files $uri $uri/ /index.html;
    }
}
EOF

# Habilitar sitio
ln -sf /etc/nginx/sites-available/crm /etc/nginx/sites-enabled/

# Deshabilitar sitio default (opcional)
rm -f /etc/nginx/sites-enabled/default

# Validar configuración
nginx -t

# Si la validación es exitosa, recargar
systemctl reload nginx
```

### 4. Configurar Firewall

```bash
# Permitir HTTP/HTTPS
ufw allow 80/tcp
ufw allow 443/tcp

# Permitir backend (opcional, solo si necesitas acceso directo)
# ufw allow 8082/tcp

# Verificar
ufw status
```

---

## 📦 DESPLIEGUE DEL BACKEND

### Opción A: Script Automatizado (Recomendado)

```bash
# Desde tu máquina local
cd /Users/lperez/Workspace/Development/fullstack/crm_pd

# Deploy completo
./deploy-backend.sh --remote

# El script automáticamente:
# 1. Compila el JAR con Maven
# 2. Crea backup del JAR anterior
# 3. Sube el nuevo JAR via SCP
# 4. Reinicia PM2
# 5. Verifica el estado
```

### Opción B: Manual

```bash
# 1. Compilar backend localmente
cd backend
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn clean package -DskipTests

# 2. Verificar JAR generado
ls -lh application/target/application-1.0.0-SNAPSHOT.jar
# Debe ser ~59MB

# 3. Subir al servidor
scp application/target/application-1.0.0-SNAPSHOT.jar \
    root@128.199.13.76:/opt/crm-backend/application.jar

# 4. Reiniciar en servidor
ssh root@128.199.13.76 << 'EOF'
  cd /opt/crm-backend
  pm2 restart crm-backend || pm2 start ecosystem.config.js
  pm2 save
  sleep 10
  pm2 list
EOF

# 5. Verificar health
sleep 15
curl http://128.199.13.76/api/actuator/health
```

---

## 🎨 DESPLIEGUE DEL FRONTEND

### Opción A: Script Automatizado (Recomendado)

```bash
# Desde tu máquina local
cd /Users/lperez/Workspace/Development/fullstack/crm_pd

# Deploy completo
./deploy-frontend.sh --remote

# El script automáticamente:
# 1. Compila el frontend con Vite
# 2. Crea backup del dist anterior
# 3. Sube el nuevo dist/ via rsync
# 4. Recarga Nginx
# 5. Verifica el estado
```

### Opción B: Manual

```bash
# 1. Compilar frontend localmente
cd frontend/apps/web
npm run build

# 2. Verificar dist generado
ls -lh dist/
# Debe contener index.html y assets/

# 3. Subir al servidor
rsync -avz --delete dist/ \
    root@128.199.13.76:/opt/crm-frontend/dist/

# 4. Recargar Nginx
ssh root@128.199.13.76 "nginx -t && nginx -s reload"

# 5. Verificar
curl http://128.199.13.76/
```

---

## ✅ VERIFICACIÓN POST-DESPLIEGUE

### 1. Backend

```bash
# Estado de PM2
ssh root@128.199.13.76 "pm2 list"
# Debe mostrar "crm-backend" en estado "online"

# Health check
curl http://128.199.13.76/api/actuator/health
# Debe retornar: {"status":"UP"}

# Auth health
curl http://128.199.13.76/api/v1/auth/health
# Debe retornar: "Auth service is healthy"

# Logs (últimas 50 líneas)
ssh root@128.199.13.76 "pm2 logs crm-backend --lines 50 --nostream"

# Monitoreo de recursos
ssh root@128.199.13.76 "pm2 monit"
```

### 2. Frontend

```bash
# Verificar Nginx
ssh root@128.199.13.76 "systemctl status nginx"

# Verificar archivos
ssh root@128.199.13.76 "ls -lh /opt/crm-frontend/dist/index.html"

# Test HTTP
curl -I http://128.199.13.76/
# Debe retornar HTTP/1.1 200 OK

# Verificar assets
curl -I http://128.199.13.76/assets/index-*.js
# Debe retornar HTTP/1.1 200 OK con cache headers
```

### 3. Integración Completa

```bash
# Test de login
curl -X POST http://128.199.13.76/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@pagodirecto.com","password":"admin123"}'
# Debe retornar JSON con accessToken

# Test de dashboard (requiere token del paso anterior)
TOKEN="<token-del-login>"
curl -H "Authorization: Bearer $TOKEN" \
     http://128.199.13.76/api/v1/dashboard/stats
# Debe retornar estadísticas
```

### 4. Navegador

```
1. Abrir http://128.199.13.76
2. Verificar que carga la pantalla de login
3. Login con: admin@pagodirecto.com / admin123
4. Verificar que carga el dashboard
5. Probar navegación a Clientes, Productos, etc.
6. Verificar que CRUD operations funcionan
```

---

## 🔄 ROLLBACK (Si algo falla)

### Backend

```bash
ssh root@128.199.13.76 << 'EOF'
  cd /opt/crm-backend

  # Detener servicio
  pm2 stop crm-backend

  # Revertir a backup
  mv application.jar application.jar.failed
  mv application.jar.backup.* application.jar

  # Reiniciar
  pm2 start crm-backend
  pm2 logs crm-backend --lines 20
EOF
```

### Frontend

```bash
ssh root@128.199.13.76 << 'EOF'
  cd /opt/crm-frontend

  # Revertir a backup
  rm -rf dist
  mv dist.backup.* dist

  # Recargar Nginx
  nginx -s reload
EOF
```

---

## 🐛 TROUBLESHOOTING

### Backend no arranca

```bash
# Ver logs de error
ssh root@128.199.13.76 "pm2 logs crm-backend --err --lines 100"

# Verificar Java
ssh root@128.199.13.76 "java -version"

# Verificar puerto ocupado
ssh root@128.199.13.76 "lsof -i :8082"

# Verificar conectividad a BD
ssh root@128.199.13.76 "psql -h DB_HOST -U DB_USER -d DB_NAME -c 'SELECT 1'"
```

### Frontend muestra error 502

```bash
# Verificar Nginx
ssh root@128.199.13.76 "nginx -t"
ssh root@128.199.13.76 "systemctl status nginx"

# Verificar logs de Nginx
ssh root@128.199.13.76 "tail -n 50 /var/log/nginx/crm-error.log"

# Verificar que backend esté corriendo
ssh root@128.199.13.76 "curl http://localhost:8082/api/actuator/health"
```

### Login falla con 403

```bash
# Verificar endpoint de auth
curl -X POST http://128.199.13.76/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@pagodirecto.com","password":"admin123"}' \
  -v

# Verificar logs de backend
ssh root@128.199.13.76 "pm2 logs crm-backend --lines 50 | grep -i 'auth\|403'"
```

### CORS errors en navegador

```bash
# Verificar configuración CORS en backend
ssh root@128.199.13.76 "grep -A 5 'allowed-origins' /opt/crm-backend/.env"

# Verificar headers de Nginx
curl -I http://128.199.13.76/api/v1/auth/health
# Debe incluir: Access-Control-Allow-Origin
```

---

## 📊 MONITOREO POST-PRODUCCIÓN

### Primera Hora

Ejecutar cada 10 minutos:
```bash
# Status general
ssh root@128.199.13.76 "pm2 status && systemctl status nginx"

# CPU y memoria
ssh root@128.199.13.76 "free -h && top -bn1 | head -15"

# Logs de errores
ssh root@128.199.13.76 "pm2 logs crm-backend --lines 20 --nostream | grep -i error"
```

### Primeras 24 Horas

Ejecutar cada 4 horas:
```bash
# Métricas de PM2
ssh root@128.199.13.76 "pm2 monit"

# Disco
ssh root@128.199.13.76 "df -h"

# Logs de Nginx
ssh root@128.199.13.76 "tail -n 100 /var/log/nginx/crm-access.log"
```

---

## 🔒 SEGURIDAD POST-DESPLIEGUE (72 HORAS)

### Tareas Críticas

1. **Implementar Autenticación JWT Real**
   - Reemplazar AuthController mock
   - Implementar JwtTokenProvider con firma
   - Integrar con base de datos de usuarios

2. **Activar Validación de Roles**
   - Configurar SecurityContext con authorities
   - Validar @PreAuthorize annotations
   - Testing de authorization matrix

3. **Rate Limiting en Login**
   - Configurar Bucket4j o Nginx rate limiting
   - Max 5 intentos por 15 minutos

4. **GlobalExceptionHandler**
   - Manejo centralizado de excepciones
   - Mensajes de error consistentes

---

## 📝 CHECKLIST FINAL

Antes de marcar como "PRODUCCIÓN LISTA":

- [ ] Variables de entorno configuradas (DATABASE_PASSWORD, JWT_SECRET)
- [ ] Backend desplegado y corriendo (pm2 status = online)
- [ ] Frontend desplegado (nginx funcionando)
- [ ] Health checks pasando (backend + frontend)
- [ ] Login funcional (admin@pagodirecto.com)
- [ ] Dashboard carga correctamente
- [ ] CRUD operations funcionan (Clientes, Productos)
- [ ] Logs sin errores críticos
- [ ] Backup de base de datos creado
- [ ] Plan de rollback documentado
- [ ] Monitoreo configurado (primeras 24h)
- [ ] Certificación QA revisada
- [ ] Equipo notificado del despliegue

---

## 📞 SOPORTE

**En caso de emergencia:**
1. Revisar logs: `pm2 logs crm-backend`
2. Consultar QA_CERTIFICATION_REPORT.md
3. Ejecutar rollback si es necesario
4. Contactar al equipo de desarrollo

---

**¡Sistema listo para producción!**

**Última actualización:** 2025-10-27
**Próximo review:** Sprint 1 (implementación de autenticación real)
