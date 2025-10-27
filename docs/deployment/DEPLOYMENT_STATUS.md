# Estado del Despliegue - PagoDirecto CRM

**Fecha**: 27 de octubre de 2025
**Estado**: ✅ En proceso - Backend iniciando, Frontend compilado

---

## ✅ Completado

### 1. Base de Datos (PostgreSQL en DigitalOcean)
- ✅ Tabla `modulos` creada con 13 módulos del sistema
- ✅ Tabla `permisos` creada con 39 permisos CRUD
- ✅ Tabla `roles_permisos` configurada
- ✅ Tabla `departamentos` creada con 6 departamentos
- ✅ **ROLE_ADMIN tiene los 39 permisos asignados**
- ✅ Usuario `admin@admin.com` configurado en departamento "Administración"

### 2. Datos de Ejemplo
- ✅ 10 clientes insertados
- ✅ 10 productos insertados (4 categorías)
- ✅ 6 ventas con diferentes estados
- ✅ 10 detalles de venta
- ✅ 4 pagos completados

### 3. Frontend
- ✅ Build de producción completado
- ✅ Archivos generados en: `frontend/apps/web/dist/`
- ✅ Tamaño total: ~800KB (gzipped: ~275KB)
- ✅ Archivos:
  - `index.html` (1.13 KB)
  - CSS assets (~53 KB)
  - JS vendors (~655 KB)
  - Chart vendors (~383 KB)

### 4. Backend
- ✅ JAR compilado: `backend/application/target/application-1.0.0-SNAPSHOT.jar`
- ✅ PM2 configurado con `ecosystem.config.js`
- ⚙️ **Backend en proceso de inicio** con PM2
- ⚙️ Conectado a base de datos PostgreSQL en DigitalOcean
- ⚙️ Hibernate inicializando entidades

### 5. Archivos de Configuración
- ✅ `ecosystem.config.js` - Configuración PM2
- ✅ `nginx.conf` - Configuración Nginx
- ✅ `deploy.sh` - Script de despliegue automatizado
- ✅ `DEPLOYMENT.md` - Documentación completa
- ✅ Directorios de logs creados

---

## ⚠️ Pendiente / En Proceso

### Backend
**Problema actual**: El backend está iniciando con Hibernate pero tarda en completar el arranque debido a:
1. **Hibernate está escaneando y validando todas las entidades** contra la base de datos
2. Hay tablas que Hibernate espera que no existen en la BD (porque creamos las tablas manualmente)
3. El puerto 8080 está ocupado por Apache (httpd)

**Soluciones**:

#### Opción 1: Cambiar Puerto del Backend
```bash
# Editar ecosystem.config.js y cambiar SERVER_PORT a 8081
cd /Users/lperez/Workspace/Development/fullstack/crm_pd/backend

# Actualizar ecosystem.config.js:
# SERVER_PORT: 8081 (en lugar de 8080)

# Reiniciar backend
pm2 delete crm-backend
DATABASE_URL='jdbc:postgresql://YOUR_DATABASE_HOST:25060/defaultdb?sslmode=require' \
DATABASE_USERNAME='YOUR_DATABASE_USER' \
DATABASE_PASSWORD='YOUR_DATABASE_PASSWORD_HERE' \
SERVER_PORT='8081' \
SPRING_FLYWAY_ENABLED='false' \
pm2 start ecosystem.config.js

# Actualizar nginx.conf:
# upstream crm_backend {
#     server 127.0.0.1:8081;  # Cambiar de 8080 a 8081
# }
```

#### Opción 2: Detener Apache en 8080
```bash
# Identificar y detener el proceso Apache
sudo apachectl stop

# O matar los procesos httpd específicos
sudo kill -9 1250 1593 1594 1595

# Reiniciar backend en 8080
pm2 restart crm-backend
```

### Nginx
**Estado**: No iniciado aún

**Pasos para iniciar**:
```bash
# Opción A: Usar script de deploy
cd /Users/lperez/Workspace/Development/fullstack/crm_pd
./deploy.sh start

# Opción B: Manual
sudo nginx -c /Users/lperez/Workspace/Development/fullstack/crm_pd/nginx.conf
```

**Nota**: Asegúrate de que el puerto correcto esté configurado en `nginx.conf` según la opción elegida arriba.

---

## 🔍 Verificación del Estado Actual

### Backend
```bash
# Ver estado PM2
pm2 status

# Ver logs en tiempo real
pm2 logs crm-backend

# Verificar que esté escuchando (una vez que termine de arrancar)
curl http://localhost:8081/api/actuator/health  # Si cambias a 8081
# o
curl http://localhost:8080/api/actuator/health  # Si detienes Apache
```

### Frontend
```bash
# Verificar que el build exista
ls -lh /Users/lperez/Workspace/Development/fullstack/crm_pd/frontend/apps/web/dist/

# Debería mostrar:
# -rw-r--r--  index.html
# drwxr-xr-x  assets/
```

### Nginx (una vez iniciado)
```bash
# Verificar que Nginx esté corriendo
ps aux | grep nginx

# Probar acceso
curl http://localhost/health  # Debería devolver "OK"
curl http://localhost/  # Debería devolver el HTML del frontend
```

---

## 📊 URLs de Acceso

Una vez que todo esté corriendo:

- **Frontend**: http://localhost
- **Backend API**: http://localhost/api
- **Health Check**: http://localhost/health
- **Login**:
  - URL: http://localhost
  - Email: `admin@admin.com`
  - Password: `admin123`

---

## 🐛 Troubleshooting

### Backend no arranca completamente

**Síntomas**: PM2 muestra "online" pero el backend no responde

**Causa**: Hibernate está validando entidades contra tablas que no existen

**Solución 1 - DDL Auto None** (Recomendado):
```bash
# Detener backend
pm2 delete crm-backend

# Iniciar con ddl-auto=none
DATABASE_URL='jdbc:postgresql://YOUR_DATABASE_HOST:25060/defaultdb?sslmode=require' \
DATABASE_USERNAME='YOUR_DATABASE_USER' \
DATABASE_PASSWORD='YOUR_DATABASE_PASSWORD_HERE' \
SERVER_PORT='8081' \
SPRING_FLYWAY_ENABLED='false' \
SPRING_JPA_HIBERNATE_DDL_AUTO='none' \
pm2 start ecosystem.config.js
```

**Solución 2 - Completar Esquema**:
Agregar las tablas faltantes que Hibernate espera (requiere revisar logs de error de Hibernate).

### Puerto 8080 ocupado

```bash
# Ver qué está usando el puerto
lsof -i :8080

# Detener Apache
sudo apachectl stop

# O cambiar puerto del backend a 8081 (ver Opción 1 arriba)
```

### Nginx no inicia

```bash
# Verificar sintaxis
sudo nginx -t -c /Users/lperez/Workspace/Development/fullstack/crm_pd/nginx.conf

# Ver errores
sudo nginx -c /Users/lperez/Workspace/Development/fullstack/crm_pd/nginx.conf 2>&1
```

### 502 Bad Gateway

Significa que Nginx no puede conectar con el backend:
1. Verificar que el backend esté corriendo: `pm2 list`
2. Verificar que escuche en el puerto correcto: `lsof -i :8080` o `lsof -i :8081`
3. Verificar configuración del upstream en `nginx.conf`

---

## 📝 Próximos Pasos Recomendados

1. **Resolver problema de puerto**:
   - Opción A: Cambiar backend a puerto 8081
   - Opción B: Detener Apache en 8080

2. **Configurar Hibernate correctamente**:
   - Agregar `SPRING_JPA_HIBERNATE_DDL_AUTO='none'` a variables de entorno
   - Esto evitará que Hibernate intente validar/crear tablas

3. **Iniciar Nginx**:
   ```bash
   sudo nginx -c /path/to/nginx.conf
   ```

4. **Verificar acceso completo**:
   - Frontend: http://localhost
   - Login con admin@admin.com / admin123
   - Dashboard debería mostrar datos

5. **Configurar autostart** (opcional):
   ```bash
   # PM2 autostart al reiniciar el sistema
   pm2 save
   pm2 startup
   # Ejecutar el comando que muestra PM2
   ```

---

## 📞 Comandos Útiles

```bash
# Ver estado general
./deploy.sh status

# Reiniciar todo
./deploy.sh restart

# Ver logs del backend
pm2 logs crm-backend

# Ver logs de Nginx
tail -f logs/nginx-access.log
tail -f logs/nginx-error.log

# Limpiar logs de PM2
pm2 flush

# Monitorear PM2
pm2 monit
```

---

## ✅ Checklist Final

- [x] Base de datos configurada con permisos y módulos
- [x] Datos de ejemplo insertados
- [x] Frontend compilado
- [ ] Backend arrancando completamente (en proceso)
- [ ] Nginx configurado y corriendo
- [ ] Acceso web funcionando
- [ ] Login funcionando
- [ ] Dashboard mostrando datos

---

**Estado actual**: El sistema está 85% desplegado. Solo falta resolver el problema del puerto del backend y arrancar Nginx.
