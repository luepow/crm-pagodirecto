# PagoDirecto CRM - Guía de Despliegue

## 🚀 Arquitectura de Despliegue

El sistema utiliza:
- **PM2**: Gestión de procesos para el backend Java
- **Nginx**: Servidor web y proxy inverso
- **PostgreSQL**: Base de datos (en DigitalOcean)

```
┌─────────────┐
│   Nginx     │ :80
│  (Proxy)    │
└──────┬──────┘
       │
       ├──────> Frontend (static files)
       │        /Users/.../frontend/apps/web/dist
       │
       └──────> Backend API (Spring Boot) :8080
                Gestionado por PM2
                /Users/.../backend/application/target/*.jar
```

## 📋 Prerrequisitos

### 1. Instalar PM2
```bash
npm install -g pm2
```

### 2. Instalar Nginx
```bash
# macOS
brew install nginx

# Ubuntu/Debian
sudo apt-get install nginx

# Verificar instalación
nginx -v
```

### 3. Verificar Java
```bash
java -version
# Debe ser Java 17 o superior
```

## 🔧 Archivos de Configuración

### PM2 Configuration (`backend/ecosystem.config.js`)
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
      './application/target/application-1.0.0-SNAPSHOT.jar'
    ],
    cwd: '/Users/lperez/Workspace/Development/fullstack/crm_pd/backend',
    autorestart: true,
    max_memory_restart: '1G'
  }]
};
```

### Nginx Configuration (`nginx.conf`)
```nginx
upstream crm_backend {
    server 127.0.0.1:8080;
}

server {
    listen 80;
    server_name localhost;

    root /Users/.../frontend/apps/web/dist;

    # API Proxy
    location /api/ {
        proxy_pass http://crm_backend/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Frontend (React Router)
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

## 🚀 Despliegue Rápido

### Opción 1: Script Automatizado

```bash
# Dar permisos de ejecución
chmod +x deploy.sh

# Iniciar todo (PM2 + Nginx)
./deploy.sh start

# Ver estado
./deploy.sh status

# Reiniciar servicios
./deploy.sh restart

# Detener todo
./deploy.sh stop
```

### Opción 2: Comandos Manuales

#### Backend con PM2
```bash
cd backend

# Iniciar backend
pm2 start ecosystem.config.js

# Ver logs
pm2 logs crm-backend

# Monitorear
pm2 monit

# Reiniciar
pm2 restart crm-backend

# Detener
pm2 stop crm-backend
pm2 delete crm-backend

# Guardar configuración para auto-inicio
pm2 save
pm2 startup  # Ejecutar el comando que muestra PM2
```

#### Nginx
```bash
# Iniciar Nginx con configuración personalizada
sudo nginx -c /Users/lperez/Workspace/Development/fullstack/crm_pd/nginx.conf

# Verificar configuración
sudo nginx -t -c /Users/lperez/Workspace/Development/fullstack/crm_pd/nginx.conf

# Recargar configuración
sudo nginx -s reload -c /Users/lperez/Workspace/Development/fullstack/crm_pd/nginx.conf

# Detener
sudo nginx -s stop
```

## 📊 Verificación

### 1. Verificar Backend
```bash
# Ver procesos PM2
pm2 list

# Verificar logs
pm2 logs crm-backend --lines 50

# Verificar endpoint de salud (si existe)
curl http://localhost:8080/actuator/health
```

### 2. Verificar Nginx
```bash
# Ver procesos
ps aux | grep nginx

# Verificar acceso
curl http://localhost/health
curl http://localhost/api/v1/auth/login
```

### 3. Verificar Frontend
```bash
# Abrir en navegador
open http://localhost
```

## 🔍 URLs de Acceso

- **Frontend**: http://localhost
- **Backend API**: http://localhost/api
- **Health Check**: http://localhost/health
- **Login**: http://localhost (credenciales: admin@admin.com / admin123)

## 📝 Logs

### PM2 Logs
```bash
# Ver todos los logs
pm2 logs

# Logs del backend específico
pm2 logs crm-backend

# Limpiar logs
pm2 flush
```

### Nginx Logs
```bash
# Access log
tail -f /Users/lperez/Workspace/Development/fullstack/crm_pd/logs/nginx-access.log

# Error log
tail -f /Users/lperez/Workspace/Development/fullstack/crm_pd/logs/nginx-error.log
```

### Backend Application Logs
```bash
# Ver logs de la aplicación
tail -f backend/logs/*.log
```

## 🔧 Troubleshooting

### Backend no inicia
```bash
# Verificar puerto 8080 libre
lsof -i :8080

# Si está ocupado, matar proceso
kill -9 $(lsof -t -i:8080)

# Verificar permisos del JAR
ls -la backend/application/target/application-1.0.0-SNAPSHOT.jar

# Verificar logs de PM2
pm2 logs crm-backend --lines 100 --err
```

### Nginx no inicia
```bash
# Verificar sintaxis
sudo nginx -t -c /path/to/nginx.conf

# Verificar puerto 80 libre
sudo lsof -i :80

# Ver errores detallados
sudo nginx -c /path/to/nginx.conf 2>&1
```

### 502 Bad Gateway
Significa que Nginx no puede conectar con el backend:

```bash
# Verificar que el backend esté corriendo
pm2 list

# Verificar que escuche en el puerto correcto
lsof -i :8080

# Revisar configuración del upstream en nginx.conf
grep -A 5 "upstream crm_backend" nginx.conf
```

### Frontend muestra página en blanco
```bash
# Verificar que dist existe
ls -la frontend/apps/web/dist/

# Verificar index.html
cat frontend/apps/web/dist/index.html

# Reconstruir frontend
cd frontend/apps/web
npx vite build --mode production
```

## 🔄 Actualización del Sistema

### 1. Actualizar Backend
```bash
cd backend

# Compilar (si es necesario)
mvn clean package -DskipTests

# Reiniciar con PM2
pm2 restart crm-backend
```

### 2. Actualizar Frontend
```bash
cd frontend/apps/web

# Reconstruir
npx vite build --mode production

# Nginx detectará los cambios automáticamente
# O recargar nginx si es necesario
sudo nginx -s reload
```

## 🌐 Variables de Entorno

### Backend (application-prod.yml o environment)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://YOUR_DATABASE_HOST:25060/defaultdb?sslmode=require
    username: YOUR_DATABASE_USER
    password: ${DB_PASSWORD}

server:
  port: 8080
```

### Frontend (.env.production)
```bash
VITE_API_BASE_URL=/api
VITE_ENV=production
```

## 📦 Estructura de Archivos

```
crm_pd/
├── backend/
│   ├── ecosystem.config.js       # Configuración PM2
│   ├── application/target/
│   │   └── application-*.jar     # JAR ejecutable
│   └── logs/                     # Logs del backend
├── frontend/apps/web/
│   └── dist/                     # Build del frontend
├── nginx.conf                    # Configuración Nginx
├── deploy.sh                     # Script de despliegue
├── logs/
│   ├── nginx-access.log
│   └── nginx-error.log
└── DEPLOYMENT.md                 # Esta guía
```

## 🔐 Seguridad

### Checklist de Seguridad
- [ ] Cambiar contraseñas por defecto
- [ ] Configurar HTTPS con certificado SSL
- [ ] Configurar firewall (solo puertos 80, 443)
- [ ] Habilitar rate limiting en Nginx
- [ ] Configurar CORS correctamente
- [ ] Actualizar secrets de JWT
- [ ] Habilitar logs de auditoría
- [ ] Configurar backups automáticos

## 📞 Soporte

Para problemas o preguntas sobre el despliegue:
1. Revisar logs: `pm2 logs` y `tail -f logs/nginx-error.log`
2. Verificar estado: `./deploy.sh status`
3. Consultar esta guía de troubleshooting

---

**Nota**: Esta configuración es para desarrollo/staging. Para producción, se recomienda:
- Usar un dominio real y HTTPS
- Configurar un reverse proxy adicional (como Cloudflare)
- Implementar balanceo de carga si es necesario
- Configurar monitoreo con herramientas como Prometheus/Grafana
