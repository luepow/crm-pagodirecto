# CHECKLIST: CONFIGURAR GITHUB SECRETS PARA CI/CD

**Fecha:** 2025-10-27
**Repositorio:** https://github.com/luepow/crm-pagodirecto
**Estado Workflows:** ✅ Corregidos y listos para usar

---

## ✅ WORKFLOWS CORREGIDOS

Los workflows de GitHub Actions han sido corregidos para funcionar con la estructura monorepo:

- ✅ **frontend-ci.yml** - Compila y testea frontend desde `frontend/apps/web/`
- ✅ **backend-ci.yml** - Compila y testea backend con Maven
- ✅ **deploy-production.yml** - Despliega backend y frontend a servidor

**Cambios realizados:**
- Cambiado `working-directory` de `frontend/` a `frontend/apps/web/`
- Frontend se despliega a `/var/www/crm-pd/` (directorio de Nginx)
- Backend se despliega a `/opt/crm-backend/`
- Backups automáticos antes de cada deploy

---

## 🔐 CONFIGURAR SECRETS EN GITHUB

Para activar el deployment automático, necesitas configurar **5 secrets** en GitHub.

### Paso 1: Ir a GitHub Secrets

1. Ve a: https://github.com/luepow/crm-pagodirecto/settings/secrets/actions
2. O navega: **Settings** → **Secrets and variables** → **Actions**
3. Click en **"New repository secret"**

---

### Secret 1: SSH_PRIVATE_KEY

**¿Qué es?** Tu llave SSH privada para conectar al servidor

**Cómo obtenerla:**
```bash
# En tu máquina local
cat ~/.ssh/id_ed25519
# O si usas RSA:
cat ~/.ssh/id_rsa
```

**Copiar:**
- TODO el contenido, incluyendo:
  ```
  -----BEGIN OPENSSH PRIVATE KEY-----
  ... contenido ...
  -----END OPENSSH PRIVATE KEY-----
  ```

**En GitHub:**
- **Name:** `SSH_PRIVATE_KEY`
- **Secret:** Pegar todo el contenido de la llave privada
- Click **Add secret**

---

### Secret 2: REMOTE_USER

**¿Qué es?** Usuario del servidor (root)

**En GitHub:**
- **Name:** `REMOTE_USER`
- **Secret:** `root`
- Click **Add secret**

---

### Secret 3: REMOTE_HOST

**¿Qué es?** IP del servidor DigitalOcean

**En GitHub:**
- **Name:** `REMOTE_HOST`
- **Secret:** `128.199.13.76`
- Click **Add secret**

---

### Secret 4: REMOTE_BACKEND_PATH

**¿Qué es?** Directorio donde se despliega el backend

**En GitHub:**
- **Name:** `REMOTE_BACKEND_PATH`
- **Secret:** `/opt/crm-backend`
- Click **Add secret**

---

### Secret 5: REMOTE_FRONTEND_PATH

**¿Qué es?** Directorio donde se despliega el frontend (debe ser el mismo que usa Nginx)

**En GitHub:**
- **Name:** `REMOTE_FRONTEND_PATH`
- **Secret:** `/var/www/crm-pd`
- Click **Add secret**

⚠️ **IMPORTANTE:** Este debe ser `/var/www/crm-pd`, NO `/opt/crm-frontend`

---

## ✅ VERIFICAR CONFIGURACIÓN

Una vez agregados los 5 secrets, deberías ver:

```
SSH_PRIVATE_KEY         ******** (Updated X minutes ago)
REMOTE_USER             ******** (Updated X minutes ago)
REMOTE_HOST             ******** (Updated X minutes ago)
REMOTE_BACKEND_PATH     ******** (Updated X minutes ago)
REMOTE_FRONTEND_PATH    ******** (Updated X minutes ago)
```

---

## 🚀 PROBAR WORKFLOWS

### Opción 1: Push a main (Automático)

Cualquier push a `main` activará el workflow de deployment:

```bash
git push origin main
```

### Opción 2: Manual Dispatch

1. Ve a: https://github.com/luepow/crm-pagodirecto/actions
2. Click en **"Deploy to Production"**
3. Click en **"Run workflow"**
4. Selecciona branch: `main`
5. Environment: `production`
6. Click **"Run workflow"**

---

## 📊 QUÉ HACEN LOS WORKFLOWS

### Backend CI (`backend-ci.yml`)
**Se activa:** Push/PR a `main` o `develop` con cambios en `backend/**`

Hace:
- ✅ Compila con Maven + Java 21
- ✅ Ejecuta tests
- ✅ Sube JAR como artifact
- ✅ Genera reporte de coverage
- ✅ Security scan con OWASP

### Frontend CI (`frontend-ci.yml`)
**Se activa:** Push/PR a `main` o `develop` con cambios en `frontend/**`

Hace:
- ✅ Compila con Vite desde `frontend/apps/web/`
- ✅ Ejecuta linter y type-check
- ✅ Genera bundle de producción
- ✅ Analiza tamaño del bundle
- ✅ Lighthouse audit

### Deploy Production (`deploy-production.yml`)
**Se activa:**
- Push a `main`
- Crear tag `v*`
- Manual dispatch

Hace:
1. **Build Backend:**
   - Compila JAR con Maven
   - Sube artifact

2. **Build Frontend:**
   - Instala deps en `frontend/apps/web/`
   - Compila con `npm run build`
   - Sube dist/ artifact

3. **Deploy Backend:**
   - Crea backup: `application.jar.backup.YYYYMMDD_HHMMSS`
   - Sube JAR a `/opt/crm-backend/application.jar`
   - Sube `ecosystem.config.js`
   - Reinicia PM2
   - Health check en puerto 8082

4. **Deploy Frontend:**
   - Crea backup: `/var/www/crm-pd.backup.YYYYMMDD_HHMMSS`
   - Sube dist/ a `/var/www/crm-pd/`
   - Recarga Nginx
   - Health check en puerto 80

5. **Notificación:**
   - ✅ Success: Muestra URLs de producción
   - ❌ Failure: Falla el workflow

---

## 🐛 TROUBLESHOOTING

### Workflow falla en "Setup SSH"

**Error:** `SSH_PRIVATE_KEY not found`

**Solución:**
1. Verificar que el secret existe en: https://github.com/luepow/crm-pagodirecto/settings/secrets/actions
2. Verificar que el nombre es exactamente `SSH_PRIVATE_KEY` (sensible a mayúsculas)

---

### Workflow falla en "Upload JAR to server"

**Error:** `Permission denied (publickey)`

**Problema:** La llave SSH no tiene permisos en el servidor

**Solución:**
```bash
# Copiar llave pública al servidor
ssh-copy-id root@128.199.13.76

# O manualmente
cat ~/.ssh/id_ed25519.pub | ssh root@128.199.13.76 "mkdir -p ~/.ssh && cat >> ~/.ssh/authorized_keys"

# Verificar permisos
ssh root@128.199.13.76 "chmod 700 ~/.ssh && chmod 600 ~/.ssh/authorized_keys"
```

---

### Workflow falla en "Install dependencies"

**Error:** `npm error code ENOENT` o `package.json not found`

**Problema:** Este error ya fue corregido en el commit `9261323`

**Verificar:**
```bash
git log --oneline -1
# Debe mostrar: 9261323 fix: corregir workflows de GitHub Actions para estructura monorepo
```

Si el workflow sigue usando `frontend/` en lugar de `frontend/apps/web/`, pull los últimos cambios:
```bash
git pull origin main
```

---

### Backend no arranca después de deploy

**Error:** PM2 muestra "errored" o "waiting restart"

**Diagnóstico:**
```bash
ssh root@128.199.13.76 "pm2 logs crm-backend --lines 50"
```

**Causa común:** Archivo `ecosystem.config.js` no tiene las credenciales correctas

**Solución:**
El workflow sube `backend/ecosystem.config.js` que NO tiene credenciales (por seguridad).
Después del primer deploy automático, necesitas configurar manualmente:

```bash
ssh root@128.199.13.76

# Editar ecosystem.config.js con credenciales reales
nano /opt/crm-backend/ecosystem.config.js

# Agregar datasource en args:
'-Dspring.datasource.url=jdbc:postgresql://[DB_HOST]:[DB_PORT]/[DB_NAME]?sslmode=require',
'-Dspring.datasource.username=[DB_USER]',
'-Dspring.datasource.password=[DB_PASS]',

# Reiniciar
pm2 restart crm-backend
```

---

## 🔄 ALTERNATIVA: DEPLOYMENT MANUAL

Si prefieres NO usar GitHub Actions (más rápido y confiable por ahora):

```bash
cd /Users/lperez/Workspace/Development/fullstack/crm_pd

# Deploy backend
./deploy-backend.sh --remote

# Deploy frontend
./deploy-frontend.sh --remote
```

Los scripts manuales están 100% operacionales y son más rápidos que GitHub Actions.

---

## 📝 COMPARACIÓN: MANUAL vs AUTOMÁTICO

### Deployment Manual (Scripts)
**Ventajas:**
- ✅ Más rápido (no espera queue de GitHub)
- ✅ Usa tu máquina local (compilación más rápida)
- ✅ Debugging más fácil (logs inmediatos)
- ✅ No requiere configurar secrets
- ✅ Ya funciona 100%

**Desventajas:**
- ❌ Requiere acceso manual
- ❌ No hay historial de deployments
- ❌ No hay rollback automático

### Deployment Automático (GitHub Actions)
**Ventajas:**
- ✅ Automático en cada push
- ✅ Historial completo en GitHub Actions
- ✅ Puede fallar sin afectar tu máquina
- ✅ Integración con pull requests

**Desventajas:**
- ❌ Más lento (runners de GitHub)
- ❌ Requiere configurar 5 secrets
- ❌ Debugging más complejo
- ❌ Requiere configuración manual post-deploy (ecosystem.config.js)

---

## 💡 RECOMENDACIÓN

**Para desarrollo rápido:** Usa scripts manuales (`./deploy-backend.sh --remote`)

**Para producción con equipo:** Configura GitHub Actions para deployment automático

**Flujo híbrido (recomendado):**
1. Durante desarrollo: Deploy manual para iteración rápida
2. Push a `main`: GitHub Actions ejecuta CI (tests, linting)
3. Para releases importantes: GitHub Actions hace deployment automático
4. Si falla algo: Rollback manual con scripts

---

## 🎯 PRÓXIMO PASO

### Si quieres deployment automático:
```bash
# 1. Configurar los 5 secrets en GitHub
# 2. Push algo a main
git push origin main

# 3. Ver el workflow en acción
# https://github.com/luepow/crm-pagodirecto/actions

# 4. Después del primer deploy, configurar manualmente:
ssh root@128.199.13.76
nano /opt/crm-backend/ecosystem.config.js
# (Agregar credenciales DB)
pm2 restart crm-backend
```

### Si prefieres deployment manual:
```bash
# Ya está funcionando 100%
./deploy-backend.sh --remote
./deploy-frontend.sh --remote
```

---

**Última actualización:** 2025-10-27
**Workflows corregidos:** Commit `9261323`
