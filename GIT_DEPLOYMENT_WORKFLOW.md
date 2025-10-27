# 🚀 Flujo de Deployment con Git - PagoDirecto CRM

**Fecha**: 27 de octubre de 2025

Este documento describe cómo usar Git para gestionar el código y hacer deployments automáticos del CRM PagoDirecto.

---

## 📋 Tabla de Contenidos

1. [Configuración Inicial](#configuración-inicial)
2. [Flujo de Desarrollo Local](#flujo-de-desarrollo-local)
3. [Deployment al Servidor](#deployment-al-servidor)
4. [Scripts de Deployment](#scripts-de-deployment)
5. [Configuración del Servidor Remoto](#configuración-del-servidor-remoto)
6. [Troubleshooting](#troubleshooting)

---

## 1. Configuración Inicial

### 1.1 Inicializar Git en el Proyecto

Si aún no tienes Git inicializado:

```bash
cd /Users/lperez/Workspace/Development/fullstack/crm_pd

# Inicializar repositorio
git init

# Crear .gitignore
cat > .gitignore << 'EOF'
# Backend
backend/application/target/
backend/logs/
backend/*.log

# Frontend
frontend/apps/web/dist/
frontend/apps/web/node_modules/
frontend/node_modules/
frontend/**/.vite/
frontend/**/.turbo/

# Environment files
.env
.env.local
.env.production
*.env.backup

# IDEs
.idea/
.vscode/
*.iml
.DS_Store

# System
logs/
*.log
.pm2/

# Secrets
ecosystem.config.js  # Contiene credenciales - crear versión template
EOF

# Primer commit
git add .
git commit -m "Initial commit: CRM PagoDirecto setup"
```

### 1.2 Crear Template de Configuración (Sin Secrets)

Crea un archivo `ecosystem.config.template.js` sin credenciales:

```bash
cp backend/ecosystem.config.js backend/ecosystem.config.template.js

# Editar el template para quitar secrets (usa variables de entorno)
```

**ecosystem.config.template.js**:
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
    cwd: '/opt/crm-backend',  // Ajustar según servidor
    instances: 1,
    autorestart: true,
    watch: false,
    max_memory_restart: '1G',
    env: {
      NODE_ENV: 'production',
      SERVER_PORT: 8082,
      DATABASE_URL: process.env.DATABASE_URL,
      DATABASE_USERNAME: process.env.DATABASE_USERNAME,
      DATABASE_PASSWORD: process.env.DATABASE_PASSWORD,
      JWT_SECRET: process.env.JWT_SECRET,
      JWT_EXPIRATION: '86400000',
      JWT_REFRESH_EXPIRATION: '604800000',
      SPRING_FLYWAY_ENABLED: 'false',
      SPRING_JPA_HIBERNATE_DDL_AUTO: 'none'
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

### 1.3 Conectar con Repositorio Remoto

```bash
# Opción 1: GitHub
git remote add origin https://github.com/tu-usuario/crm-pagodirecto.git

# Opción 2: GitLab
git remote add origin https://gitlab.com/tu-usuario/crm-pagodirecto.git

# Opción 3: Bitbucket
git remote add origin https://bitbucket.org/tu-usuario/crm-pagodirecto.git

# Subir código
git branch -M main
git push -u origin main
```

---

## 2. Flujo de Desarrollo Local

### 2.1 Trabajar en una Nueva Funcionalidad

```bash
# Crear rama para la funcionalidad
git checkout -b feature/nueva-funcionalidad

# Hacer cambios en el código...

# Ver cambios
git status
git diff

# Agregar archivos modificados
git add .

# Commit con mensaje descriptivo
git commit -m "feat: Implementar módulo de inventario

- Agregar entidad Producto
- Crear endpoints CRUD
- Implementar UI de gestión de productos"

# Subir rama al remoto
git push origin feature/nueva-funcionalidad
```

### 2.2 Convenciones de Commits

Usa [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` - Nueva funcionalidad
- `fix:` - Corrección de bug
- `docs:` - Cambios en documentación
- `style:` - Formato de código (sin cambios funcionales)
- `refactor:` - Refactorización de código
- `test:` - Agregar o modificar tests
- `chore:` - Tareas de mantenimiento

**Ejemplos**:
```bash
git commit -m "feat: Agregar módulo de reportes financieros"
git commit -m "fix: Corregir cálculo de totales en ventas"
git commit -m "docs: Actualizar README con instrucciones de deployment"
git commit -m "refactor: Mejorar performance de consultas de clientes"
```

### 2.3 Integrar Cambios a Main

```bash
# Volver a main
git checkout main

# Actualizar con últimos cambios remotos
git pull origin main

# Mergear la funcionalidad
git merge feature/nueva-funcionalidad

# Subir a remoto
git push origin main

# Eliminar rama local (opcional)
git branch -d feature/nueva-funcionalidad

# Eliminar rama remota (opcional)
git push origin --delete feature/nueva-funcionalidad
```

---

## 3. Deployment al Servidor

### 3.1 Deployment del Backend

Después de hacer commit y push de cambios en el backend:

```bash
# Opción 1: Deploy local y luego subir al servidor
./deploy-backend.sh --remote

# Opción 2: Deploy solo local (para testing)
./deploy-backend.sh
```

**Proceso del Script**:
1. ✅ Compila el backend con Maven: `mvn clean package -DskipTests`
2. ✅ Verifica que el JAR se generó correctamente
3. ✅ Crea backup del JAR anterior en el servidor
4. ✅ Sube el nuevo JAR via SCP
5. ✅ Reinicia el servicio con PM2 en el servidor
6. ✅ Verifica que el servicio arrancó correctamente

### 3.2 Deployment del Frontend

Después de hacer cambios en el frontend:

```bash
# Opción 1: Deploy local y luego subir al servidor
./deploy-frontend.sh --remote

# Opción 2: Deploy solo local (para testing)
./deploy-frontend.sh
```

**Proceso del Script**:
1. ✅ Limpia el build anterior (`rm -rf dist`)
2. ✅ Compila el frontend con Vite: `npx vite build --mode production`
3. ✅ Crea backup del frontend anterior en el servidor
4. ✅ Sube los archivos via rsync/SCP
5. ✅ Recarga Nginx en el servidor
6. ✅ Verifica que Nginx está corriendo

### 3.3 Deployment Completo (Full Stack)

Para hacer deployment de backend y frontend juntos:

```bash
# Deploy completo al servidor
./deploy-backend.sh --remote && ./deploy-frontend.sh --remote

# Deploy completo local (testing)
./deploy-backend.sh && ./deploy-frontend.sh
```

---

## 4. Scripts de Deployment

### 4.1 Scripts Disponibles

| Script | Descripción | Uso |
|--------|-------------|-----|
| `deploy-backend.sh` | Compila y despliega backend Java | `./deploy-backend.sh [--remote]` |
| `deploy-frontend.sh` | Compila y despliega frontend React | `./deploy-frontend.sh [--remote]` |
| `start-nginx.sh` | Inicia o recarga Nginx local | `./start-nginx.sh` |
| `deploy.sh` | Gestión completa de servicios | `./deploy.sh [start\|stop\|restart\|status]` |

### 4.2 Configuración de Variables de Entorno

Antes de hacer deployment remoto, configura las variables:

```bash
# Backend
export REMOTE_USER="root"
export REMOTE_HOST="your-server.com"
export REMOTE_PATH="/opt/crm-backend"

# Frontend
export REMOTE_USER="root"
export REMOTE_HOST="your-server.com"
export REMOTE_PATH="/opt/crm-frontend"

# Ahora ejecuta el deployment
./deploy-backend.sh --remote
./deploy-frontend.sh --remote
```

O edita los scripts directamente en las líneas:
```bash
REMOTE_USER="${REMOTE_USER:-root}"
REMOTE_HOST="${REMOTE_HOST:-your-server.com}"
REMOTE_PATH="${REMOTE_PATH:-/opt/crm-backend}"
```

### 4.3 Hacer Scripts Ejecutables

```bash
chmod +x deploy-backend.sh
chmod +x deploy-frontend.sh
chmod +x start-nginx.sh
chmod +x deploy.sh
```

---

## 5. Configuración del Servidor Remoto

### 5.1 Requisitos en el Servidor

El servidor remoto debe tener instalado:
- ✅ Java 17 o superior
- ✅ Node.js (para PM2)
- ✅ PM2 (`npm install -g pm2`)
- ✅ Nginx
- ✅ PostgreSQL (o acceso a la BD)

### 5.2 Configurar Acceso SSH sin Contraseña

Para que los scripts funcionen sin pedir contraseña cada vez:

```bash
# En tu máquina local, genera un par de claves SSH (si no tienes)
ssh-keygen -t rsa -b 4096 -C "tu-email@ejemplo.com"

# Copia la clave pública al servidor
ssh-copy-id root@your-server.com

# Prueba la conexión
ssh root@your-server.com "echo 'SSH funcionando correctamente'"
```

### 5.3 Estructura de Directorios en el Servidor

```bash
# Conectarse al servidor
ssh root@your-server.com

# Crear directorios necesarios
mkdir -p /opt/crm-backend/logs
mkdir -p /opt/crm-frontend
mkdir -p /etc/nginx/sites-available
mkdir -p /etc/nginx/sites-enabled

# Crear usuario para la aplicación (recomendado)
useradd -m -s /bin/bash crmuser
chown -R crmuser:crmuser /opt/crm-backend
chown -R crmuser:crmuser /opt/crm-frontend
```

### 5.4 Configurar Variables de Entorno en el Servidor

Crear archivo con secrets en el servidor:

```bash
# En el servidor
cat > /opt/crm-backend/.env << 'EOF'
DATABASE_URL=jdbc:postgresql://YOUR_DATABASE_HOST:25060/defaultdb?sslmode=require
DATABASE_USERNAME=YOUR_DATABASE_USER
DATABASE_PASSWORD=YOUR_DATABASE_PASSWORD_HERE
JWT_SECRET=YOUR_JWT_SECRET_BASE64
EOF

# Proteger el archivo
chmod 600 /opt/crm-backend/.env
chown crmuser:crmuser /opt/crm-backend/.env
```

### 5.5 Copiar ecosystem.config.js al Servidor

```bash
# Desde tu máquina local
scp backend/ecosystem.config.js root@your-server.com:/opt/crm-backend/

# Ajustar rutas en el servidor
ssh root@your-server.com "sed -i 's|/Users/lperez/.*backend|/opt/crm-backend|g' /opt/crm-backend/ecosystem.config.js"
```

### 5.6 Configurar Nginx en el Servidor

```bash
# Copiar configuración de Nginx
scp nginx.conf root@your-server.com:/etc/nginx/sites-available/crm.conf

# Ajustar rutas
ssh root@your-server.com << 'EOF'
sed -i 's|/Users/lperez/.*|/opt/crm-frontend|g' /etc/nginx/sites-available/crm.conf
sed -i 's|root.*dist;|root /opt/crm-frontend/dist;|g' /etc/nginx/sites-available/crm.conf

# Habilitar el sitio
ln -sf /etc/nginx/sites-available/crm.conf /etc/nginx/sites-enabled/

# Verificar configuración
nginx -t

# Reiniciar Nginx
systemctl restart nginx
EOF
```

### 5.7 Configurar PM2 en el Servidor

```bash
# Conectarse al servidor
ssh root@your-server.com

# Iniciar el backend con PM2
cd /opt/crm-backend
pm2 start ecosystem.config.js

# Guardar configuración de PM2
pm2 save

# Configurar PM2 para arrancar automáticamente
pm2 startup
# Copiar y ejecutar el comando que te muestra

# Verificar estado
pm2 status
pm2 logs crm-backend
```

---

## 6. Troubleshooting

### 6.1 Error: "Permission denied (publickey)"

**Problema**: No se puede conectar via SSH sin contraseña

**Solución**:
```bash
# Verificar que la clave pública está en el servidor
ssh root@your-server.com "cat ~/.ssh/authorized_keys"

# Volver a copiar la clave
ssh-copy-id root@your-server.com
```

### 6.2 Error: "JAR file not found after compilation"

**Problema**: Maven no genera el JAR correctamente

**Solución**:
```bash
# Limpiar y recompilar con verbose
cd backend
./mvnw clean package -DskipTests -X

# Verificar que el JAR existe
ls -lh application/target/*.jar
```

### 6.3 Error: "Backend fails to start after deployment"

**Problema**: El backend no arranca en el servidor

**Solución**:
```bash
# Ver logs en el servidor
ssh root@your-server.com "pm2 logs crm-backend --lines 100"

# Verificar variables de entorno
ssh root@your-server.com "pm2 env 0"

# Verificar conectividad a la base de datos
ssh root@your-server.com "telnet YOUR_DATABASE_HOST 25060"

# Reiniciar manualmente
ssh root@your-server.com "cd /opt/crm-backend && pm2 restart crm-backend"
```

### 6.4 Error: "Nginx fails to reload"

**Problema**: Nginx no recarga la configuración

**Solución**:
```bash
# Verificar configuración de Nginx
ssh root@your-server.com "nginx -t"

# Ver logs de error
ssh root@your-server.com "tail -f /var/log/nginx/error.log"

# Reiniciar Nginx completamente
ssh root@your-server.com "systemctl restart nginx"

# Verificar que está corriendo
ssh root@your-server.com "systemctl status nginx"
```

### 6.5 Error: "Port already in use"

**Problema**: El puerto 8082 (u otro) ya está ocupado

**Solución**:
```bash
# Ver qué proceso usa el puerto
ssh root@your-server.com "lsof -i :8082"

# Matar el proceso (cambia PID)
ssh root@your-server.com "kill -9 <PID>"

# O cambiar puerto en ecosystem.config.js
# Y también en nginx.conf (upstream)
```

### 6.6 Frontend no muestra cambios después de deployment

**Problema**: El navegador muestra versión antigua

**Solución**:
```bash
# Limpiar caché del navegador: Cmd+Shift+R (Mac) o Ctrl+Shift+R (Windows)

# Verificar que los archivos se subieron correctamente
ssh root@your-server.com "ls -lh /opt/crm-frontend/dist/"

# Verificar fecha de los archivos
ssh root@your-server.com "stat /opt/crm-frontend/dist/index.html"

# Forzar recarga completa de Nginx
ssh root@your-server.com "systemctl restart nginx"
```

---

## 📊 Workflow Visual

```
┌─────────────────────────────────────────────────────────────────┐
│                      DESARROLLO LOCAL                            │
│                                                                   │
│  1. Hacer cambios en código (backend o frontend)                 │
│  2. Hacer commit: git commit -m "feat: ..."                      │
│  3. Subir a Git: git push origin main                            │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                    COMPILACIÓN LOCAL                              │
│                                                                   │
│  Backend:  ./deploy-backend.sh                                    │
│  Frontend: ./deploy-frontend.sh                                   │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                  DEPLOYMENT AL SERVIDOR                           │
│                                                                   │
│  Backend:  ./deploy-backend.sh --remote                           │
│    ├─ Sube JAR via SCP                                            │
│    └─ Reinicia PM2                                                │
│                                                                   │
│  Frontend: ./deploy-frontend.sh --remote                          │
│    ├─ Sube dist/ via rsync                                        │
│    └─ Recarga Nginx                                               │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                   VERIFICACIÓN                                    │
│                                                                   │
│  ✅ Backend: curl http://your-server.com/api/actuator/health     │
│  ✅ Frontend: http://your-server.com en navegador                │
│  ✅ PM2: ssh root@your-server.com "pm2 status"                   │
│  ✅ Nginx: ssh root@your-server.com "systemctl status nginx"     │
└───────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Checklist de Deployment

Antes de hacer deployment a producción:

### Backend
- [ ] Código compilado sin errores: `mvn clean package`
- [ ] Tests pasando: `mvn test`
- [ ] Variables de entorno configuradas en servidor
- [ ] PM2 configurado con auto-restart
- [ ] Logs configurados y rotando
- [ ] Backup del JAR anterior creado

### Frontend
- [ ] Código compilado sin errores: `npm run build`
- [ ] Bundle optimizado y comprimido (gzip)
- [ ] Variables de API apuntando a producción
- [ ] Nginx configurado correctamente
- [ ] Certificado SSL instalado (si aplica)
- [ ] Backup del frontend anterior creado

### Infraestructura
- [ ] SSH sin contraseña configurado
- [ ] Firewall configurado (puertos 80, 443, 8082)
- [ ] PostgreSQL accesible desde el servidor
- [ ] Dominio DNS apuntando al servidor (si aplica)
- [ ] Monitoreo configurado (logs, métricas)

### Post-Deployment
- [ ] Verificar login funciona
- [ ] Probar módulos principales (clientes, ventas, productos)
- [ ] Verificar permisos de roles
- [ ] Ver logs por errores: `pm2 logs`
- [ ] Monitorear recursos: `pm2 monit`

---

## 📞 Soporte

Si encuentras problemas con el deployment:

1. Revisa los logs:
   - Backend: `pm2 logs crm-backend`
   - Nginx: `tail -f /var/log/nginx/error.log`

2. Verifica la configuración:
   - PM2: `pm2 show crm-backend`
   - Nginx: `nginx -t`

3. Consulta la documentación:
   - `DEPLOYMENT.md` - Guía completa
   - `DEPLOYMENT_STATUS.md` - Estado y troubleshooting
   - `FINAL_DEPLOYMENT_INSTRUCTIONS.md` - Instrucciones finales

---

**Última actualización**: 27 de octubre de 2025
**Versión**: 1.0.0
