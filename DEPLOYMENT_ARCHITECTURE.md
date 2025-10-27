# ARQUITECTURA DE DEPLOYMENT - PagoDirecto CRM

**Fecha:** 2025-10-27
**Servidor:** 128.199.13.76 (DigitalOcean)
**Estado:** ✅ En Producción

---

## 📐 ARQUITECTURA DEL SISTEMA

```
┌─────────────────────────────────────────────────────────────┐
│                    Internet / Usuario                        │
└───────────────────────┬─────────────────────────────────────┘
                        │ HTTP :80
                        ▼
┌─────────────────────────────────────────────────────────────┐
│                  NGINX (Reverse Proxy)                       │
│                                                              │
│  • Sirve frontend estático desde /var/www/crm-pd            │
│  • Proxy para /api/* → localhost:8082                       │
│  • Proxy para /actuator/health → localhost:8082             │
│                                                              │
└────────────┬───────────────────────┬──────────────────────┘
             │                       │
    Frontend │                       │ Backend
             ▼                       ▼
┌──────────────────────┐   ┌─────────────────────────────────┐
│  /var/www/crm-pd/    │   │  PM2: crm-backend               │
│                      │   │  • Java 21 + Spring Boot 3.3.5  │
│  • index.html        │   │  • Puerto: 8082                 │
│  • assets/*.js       │   │  • Context: /api                │
│  • assets/*.css      │   │  • JAR: application.jar         │
│                      │   │  • Working dir: /opt/crm-backend│
└──────────────────────┘   └────────────┬────────────────────┘
                                        │
                                        │ JDBC
                                        ▼
                           ┌──────────────────────────────────┐
                           │  PostgreSQL (DigitalOcean)       │
                           │  • Host: erppd-dp-do-user-...    │
                           │  • Port: 25060                   │
                           │  • SSL: Required                 │
                           └──────────────────────────────────┘
```

---

## 🗂️ ESTRUCTURA DE DIRECTORIOS EN SERVIDOR

```
/opt/
├── crm-backend/
│   ├── application.jar                    # JAR ejecutable (59 MB)
│   ├── ecosystem.config.js                # Configuración PM2 (con credenciales)
│   └── logs/
│       ├── pm2-out.log                    # Logs de salida estándar
│       └── pm2-error.log                  # Logs de errores
│
└── crm-frontend/                          # (Backup/staging, no se usa en producción)
    └── dist/                              # Build compilado con Vite

/var/www/
└── crm-pd/                                # ✅ DIRECTORIO SERVIDO POR NGINX
    ├── index.html                         # SPA entry point
    └── assets/
        ├── index-D7CdfuqU.js              # Bundle principal
        ├── react-vendor-_s22iUKS.js       # React + ReactDOM
        ├── query-vendor-BKpv0yIg.js       # TanStack Query
        ├── chart-vendor-Dwvb4o4R.js       # Chart.js
        ├── form-vendor-CD4TCSAS.js        # React Hook Form
        └── index-BAVmwSEG.css             # Estilos

/etc/nginx/
├── sites-available/
│   └── crm-pd                             # Configuración Nginx del CRM
└── sites-enabled/
    └── crm-pd -> ../sites-available/crm-pd
```

---

## ⚙️ CONFIGURACIÓN DE NGINX

**Archivo:** `/etc/nginx/sites-available/crm-pd`

```nginx
server {
    listen 80 default_server;
    listen [::]:80 default_server;
    server_name _;

    root /var/www/crm-pd;
    index index.html;

    # Frontend - SPA routing
    location / {
        try_files $uri $uri/ /index.html;
        add_header Cache-Control "no-cache";
    }

    # Static assets caching
    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Backend API proxy
    location /api/ {
        proxy_pass http://localhost:8082/api/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        proxy_read_timeout 90s;
    }

    # Health check endpoint
    location /actuator/health {
        proxy_pass http://localhost:8082/api/actuator/health;
        access_log off;
    }
}
```

**Comandos útiles:**
```bash
# Test configuración
nginx -t

# Recargar Nginx
systemctl reload nginx

# Ver logs
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log
```

---

## 🔧 CONFIGURACIÓN DE PM2

**Archivo:** `/opt/crm-backend/ecosystem.config.js`

```javascript
module.exports = {
  apps: [{
    name: 'crm-backend',
    script: 'java',
    args: [
      '-jar',
      '-Xms512m',
      '-Xmx1024m',
      '-Dspring.profiles.active=prod',
      '-Dspring.datasource.url=jdbc:postgresql://[DB_HOST]:[DB_PORT]/[DB_NAME]?sslmode=require',
      '-Dspring.datasource.username=[DB_USERNAME]',
      '-Dspring.datasource.password=[DB_PASSWORD]',
      '-Dspring.flyway.enabled=false',
      '-Dspring.jpa.hibernate.ddl-auto=none',
      './application.jar'
    ],
    cwd: '/opt/crm-backend',
    instances: 1,
    autorestart: true,
    watch: false,
    max_memory_restart: '1G',
    env: {
      SERVER_PORT: '8082',
      JWT_SECRET: '[BASE64_ENCODED_SECRET]',
      JWT_EXPIRATION: '86400000',
      JWT_REFRESH_EXPIRATION: '604800000',
      NODE_ENV: 'production'
    },
    error_file: './logs/pm2-error.log',
    out_file: './logs/pm2-out.log',
    log_date_format: 'YYYY-MM-DD HH:mm:ss Z',
    merge_logs: true,
    min_uptime: '10s',
    max_restarts: 10,
    restart_delay: 4000
  }]
};
```

**¿Por qué datasource en args y no en env?**

Spring Boot NO lee archivos `.env` nativamente. PM2's `env_file` solo funciona para aplicaciones Node.js. Para Java, debemos pasar las propiedades como:
- **Opción 1:** System properties (`-D...`) en args ✅ (lo que usamos)
- **Opción 2:** Variables de entorno (`env: {}`) - pero Spring requiere nombres específicos
- **Opción 3:** application.properties en el JAR (no recomendado para secrets)

**Comandos útiles:**
```bash
# Ver estado
pm2 status

# Reiniciar backend
pm2 restart crm-backend

# Ver logs en tiempo real
pm2 logs crm-backend

# Ver logs históricos
pm2 logs crm-backend --lines 100

# Guardar configuración
pm2 save

# Auto-start al reiniciar servidor
pm2 startup
```

---

## 🚀 PROCESO DE DEPLOYMENT

### Frontend

**Opción 1: Script Automático (Recomendado)**
```bash
cd /Users/lperez/Workspace/Development/fullstack/crm_pd
./deploy-frontend.sh --remote
```

El script hace:
1. Limpia build anterior
2. Compila con `vite build --mode production`
3. Crea backup en servidor: `/var/www/crm-pd.backup.YYYYMMDD_HHMMSS`
4. Sube archivos con rsync a `/var/www/crm-pd/`
5. Recarga Nginx

**Opción 2: Manual**
```bash
# Local
cd frontend/apps/web
npm install
npx vite build --mode production

# Deploy
rsync -avz --delete dist/ root@128.199.13.76:/var/www/crm-pd/

# Reload Nginx
ssh root@128.199.13.76 "nginx -t && systemctl reload nginx"
```

---

### Backend

**Opción 1: Script Automático (Recomendado)**
```bash
cd /Users/lperez/Workspace/Development/fullstack/crm_pd
./deploy-backend.sh --remote
```

El script hace:
1. Configura Java 21 localmente
2. Compila con `mvn clean package -DskipTests`
3. Crea backup en servidor: `application.jar.backup.YYYYMMDD_HHMMSS`
4. Sube JAR con SCP a `/opt/crm-backend/application.jar`
5. Sube `ecosystem.config.js`
6. Reinicia PM2
7. Verifica health check

**Opción 2: Manual**
```bash
# Local - Compilar
cd backend
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./mvnw clean package -DskipTests

# Deploy JAR
scp backend/application/target/application-1.0.0-SNAPSHOT.jar \
    root@128.199.13.76:/opt/crm-backend/application.jar

# Restart PM2
ssh root@128.199.13.76 "cd /opt/crm-backend && pm2 restart crm-backend"

# Verify
curl http://128.199.13.76:8082/api/actuator/health
```

---

## 🔍 VERIFICACIÓN POST-DEPLOYMENT

### 1. Backend Health Check
```bash
curl http://128.199.13.76/api/actuator/health
# Esperado: {"status":"UP"}
```

### 2. Frontend Carga
```bash
curl -I http://128.199.13.76/
# Esperado: HTTP/1.1 200 OK
```

### 3. PM2 Status
```bash
ssh root@128.199.13.76 "pm2 status"
# Esperado: crm-backend | online | 0 restarts
```

### 4. Nginx Status
```bash
ssh root@128.199.13.76 "systemctl status nginx"
# Esperado: active (running)
```

### 5. Test API desde Frontend
```bash
curl http://128.199.13.76/api/auth/health
# O cualquier endpoint público
```

---

## 🐛 TROUBLESHOOTING

### Backend no arranca

**Síntoma:** PM2 muestra "errored" o "waiting restart"

**Diagnóstico:**
```bash
ssh root@128.199.13.76 "pm2 logs crm-backend --lines 50"
```

**Causas comunes:**
- **Java version mismatch:** Verificar con `java -version` (debe ser 21+)
- **Database connection refused:** Verificar credenciales y conectividad
- **Port already in use:** Verificar con `lsof -i :8082`
- **JAR corrupto:** Re-compilar y re-subir

### Frontend no carga

**Síntoma:** HTTP 404 o página en blanco

**Diagnóstico:**
```bash
ssh root@128.199.13.76 "ls -lah /var/www/crm-pd/"
ssh root@128.199.13.76 "tail -f /var/log/nginx/error.log"
```

**Causas comunes:**
- **Archivos no desplegados:** Verificar que existe `/var/www/crm-pd/index.html`
- **Nginx no recargado:** Ejecutar `systemctl reload nginx`
- **Permisos incorrectos:** `chmod -R 755 /var/www/crm-pd`

### API proxy no funciona

**Síntoma:** Frontend carga pero llamadas a `/api/*` fallan con 502 Bad Gateway

**Diagnóstico:**
```bash
curl http://localhost:8082/api/actuator/health
# Debe funcionar desde el servidor
```

**Causas comunes:**
- **Backend no está corriendo:** `pm2 status`
- **Puerto incorrecto en Nginx:** Verificar `proxy_pass` en `/etc/nginx/sites-available/crm-pd`
- **Backend escuchando en puerto diferente:** Verificar logs de Spring Boot

---

## 📊 MONITOREO

### Logs Backend
```bash
# Tiempo real
ssh root@128.199.13.76 "pm2 logs crm-backend"

# Últimas 100 líneas
ssh root@128.199.13.76 "pm2 logs crm-backend --lines 100 --nostream"

# Solo errores
ssh root@128.199.13.76 "tail -f /opt/crm-backend/logs/pm2-error.log"
```

### Logs Nginx
```bash
# Access log
ssh root@128.199.13.76 "tail -f /var/log/nginx/access.log"

# Error log
ssh root@128.199.13.76 "tail -f /var/log/nginx/error.log"
```

### Métricas del Sistema
```bash
# CPU y memoria
ssh root@128.199.13.76 "pm2 monit"

# Uso de disco
ssh root@128.199.13.76 "df -h"

# Procesos Java
ssh root@128.199.13.76 "ps aux | grep java"
```

---

## 🔐 SEGURIDAD

### Secrets Almacenados

**En Servidor (NUNCA commitear a Git):**
- `/opt/crm-backend/ecosystem.config.js` - Contiene credenciales de DB

**En Repositorio (sin secrets):**
- `backend/ecosystem.config.js` - Template genérico sin credenciales

### Rotación de Secrets

Si necesitas cambiar credenciales:
```bash
# 1. Editar ecosystem.config.js en servidor
ssh root@128.199.13.76 "nano /opt/crm-backend/ecosystem.config.js"

# 2. Reiniciar backend
ssh root@128.199.13.76 "cd /opt/crm-backend && pm2 restart crm-backend"
```

---

## 📝 PRÓXIMOS PASOS RECOMENDADOS

### Corto Plazo
- [ ] Configurar SSL/TLS con Let's Encrypt
- [ ] Configurar dominio DNS (ej: crm.pagodirecto.com)
- [ ] Configurar alertas de PM2 (email/Slack cuando backend falla)
- [ ] Configurar log rotation para logs de PM2 y Nginx

### Mediano Plazo
- [ ] Configurar GitHub Actions para deployment automático
- [ ] Implementar healthchecks periódicos externos
- [ ] Configurar backups automáticos de base de datos
- [ ] Implementar monitoring con Prometheus + Grafana

### Largo Plazo
- [ ] Configurar CDN para assets estáticos
- [ ] Implementar CI/CD completo con testing automático
- [ ] Configurar ambiente de staging separado
- [ ] Implementar blue-green deployment

---

## 🔗 URLS DE PRODUCCIÓN

- **Frontend:** http://128.199.13.76/
- **API Health:** http://128.199.13.76/api/actuator/health
- **API Base:** http://128.199.13.76/api/

---

## 📞 CONTACTOS Y RECURSOS

**Documentación:**
- CLAUDE.md - Directivas del proyecto
- GITHUB_ACTIONS_SETUP.md - Configuración de CI/CD
- DEPLOYMENT_ARCHITECTURE.md - Este archivo

**Scripts de Deployment:**
- `deploy-backend.sh` - Deployment del backend
- `deploy-frontend.sh` - Deployment del frontend

**Servidor:**
- IP: 128.199.13.76
- Provider: DigitalOcean
- OS: Ubuntu 24.04
- Java: OpenJDK 21.0.8
- Node: 20.x
- PM2: Latest
- Nginx: Latest

---

**Última actualización:** 2025-10-27
**Sistema operacional:** ✅ En producción
