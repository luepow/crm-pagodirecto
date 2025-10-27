# CONFIGURACIÓN DE GITHUB ACTIONS PARA CI/CD

**Fecha:** 2025-10-27
**Repositorio:** https://github.com/luepow/crm-pagodirecto

---

## ⚠️ RECOMENDACIÓN IMPORTANTE

**POR AHORA, USA DEPLOYMENT MANUAL:**

Los workflows de GitHub Actions tienen problemas de configuración que requieren ajustes:
- Frontend: Estructura de monorepo no compatible con el workflow actual
- Backend: Requiere configuración manual del archivo `.env` en el servidor

**SOLUCIÓN INMEDIATA:** Usa los scripts de deployment manual que están funcionando perfectamente:

```bash
cd /Users/lperez/Workspace/Development/fullstack/crm_pd

# Deploy backend
./deploy-backend.sh --remote

# Deploy frontend
./deploy-frontend.sh --remote
```

**ACTUALIZACIÓN (2025-10-27):** Los workflows han sido corregidos para usar `frontend/apps/web/` como directorio de trabajo. Si configuras los GitHub Secrets correctamente, el deployment automático debería funcionar.

---

## 🔧 PROBLEMA IDENTIFICADO

Los workflows de GitHub Actions están configurados pero tienen issues que impiden el despliegue automático:

---

## 📋 SECRETS REQUERIDOS

Para que el workflow `deploy-production.yml` funcione, necesitas configurar los siguientes secrets en GitHub:

### Secrets Necesarios

| Secret | Descripción | Ejemplo |
|--------|-------------|---------|
| `SSH_PRIVATE_KEY` | Tu llave SSH privada para conectar al servidor | `-----BEGIN OPENSSH PRIVATE KEY-----...` |
| `REMOTE_USER` | Usuario del servidor | `root` |
| `REMOTE_HOST` | IP o dominio del servidor | `128.199.13.76` |
| `REMOTE_BACKEND_PATH` | Path del backend en servidor | `/opt/crm-backend` |
| `REMOTE_FRONTEND_PATH` | Path del frontend en servidor | `/var/www/crm-pd` |

---

## 🔐 CÓMO CONFIGURAR GITHUB SECRETS

### Paso 1: Obtener tu SSH Private Key

```bash
# En tu máquina local
cat ~/.ssh/id_ed25519
# O si usas RSA:
cat ~/.ssh/id_rsa

# Copia TODO el contenido, incluyendo:
# -----BEGIN OPENSSH PRIVATE KEY-----
# ... contenido ...
# -----END OPENSSH PRIVATE KEY-----
```

### Paso 2: Ir a GitHub Secrets

1. Ve a tu repositorio: https://github.com/luepow/crm-pagodirecto
2. Click en **Settings** (arriba derecha)
3. En el menú izquierdo, click en **Secrets and variables** > **Actions**
4. Click en **New repository secret**

### Paso 3: Agregar cada Secret

**Secret 1: SSH_PRIVATE_KEY**
- Name: `SSH_PRIVATE_KEY`
- Value: Pegar todo el contenido de tu llave privada SSH
- Click **Add secret**

**Secret 2: REMOTE_USER**
- Name: `REMOTE_USER`
- Value: `root`
- Click **Add secret**

**Secret 3: REMOTE_HOST**
- Name: `REMOTE_HOST`
- Value: `128.199.13.76`
- Click **Add secret**

**Secret 4: REMOTE_BACKEND_PATH**
- Name: `REMOTE_BACKEND_PATH`
- Value: `/opt/crm-backend`
- Click **Add secret**

**Secret 5: REMOTE_FRONTEND_PATH**
- Name: `REMOTE_FRONTEND_PATH`
- Value: `/var/www/crm-pd`
- Click **Add secret**

---

## ✅ VERIFICAR CONFIGURACIÓN

Una vez agregados los secrets, deberías ver algo como:

```
SSH_PRIVATE_KEY         ******** (Updated X minutes ago)
REMOTE_USER             ******** (Updated X minutes ago)
REMOTE_HOST             ******** (Updated X minutes ago)
REMOTE_BACKEND_PATH     ******** (Updated X minutes ago)
REMOTE_FRONTEND_PATH    ******** (Updated X minutes ago)
```

---

## 🚀 CÓMO ACTIVAR EL WORKFLOW

### Opción 1: Push a main (Automático)

El workflow se activa automáticamente cuando haces push a `main` o creas un tag:

```bash
git push origin main

# O crear un tag para release
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```

### Opción 2: Manual Dispatch

1. Ve a: https://github.com/luepow/crm-pagodirecto/actions
2. Click en el workflow **"Deploy to Production"**
3. Click en **"Run workflow"** (botón azul)
4. Selecciona el ambiente: `production` o `staging`
5. Click **"Run workflow"**

---

## 📊 WORKFLOWS DISPONIBLES

### 1. Backend CI (`backend-ci.yml`)
**Se activa:** En push/PR a `main` o `develop` con cambios en `backend/**`

**Hace:**
- ✅ Compila el backend con Maven
- ✅ Ejecuta tests
- ✅ Genera reporte de tests
- ✅ Sube JAR como artifact
- ✅ Genera reporte de code coverage
- ✅ Ejecuta security scan (OWASP)

**No requiere secrets** - Se ejecuta automáticamente

---

### 2. Frontend CI (`frontend-ci.yml`)
**Se activa:** En push/PR a `main` o `develop` con cambios en `frontend/**`

**Hace:**
- ✅ Compila el frontend con Vite
- ✅ Ejecuta linter
- ✅ Ejecuta type-check
- ✅ Genera bundle de producción
- ✅ Analiza tamaño del bundle
- ✅ Ejecuta Lighthouse audit

**No requiere secrets** - Se ejecuta automáticamente

---

### 3. Deploy Production (`deploy-production.yml`)
**Se activa:**
- Push a `main`
- Crear tag `v*`
- Manual dispatch

**Hace:**
- ✅ Compila backend y frontend
- ✅ Crea backup en servidor
- ✅ Sube JAR al servidor via SCP
- ✅ Sube frontend dist al servidor via rsync
- ✅ Reinicia PM2
- ✅ Recarga Nginx
- ✅ Ejecuta health checks
- ✅ Notifica resultado

**REQUIERE SECRETS** ⚠️

---

## 🐛 TROUBLESHOOTING

### Error: "SSH_PRIVATE_KEY not found"

**Problema:** El secret no está configurado
**Solución:** Sigue los pasos en "Paso 2: Ir a GitHub Secrets"

---

### Error: "Permission denied (publickey)"

**Problema:** La llave SSH no tiene permisos en el servidor
**Solución:**
```bash
# En tu máquina local
ssh-copy-id root@128.199.13.76

# O manualmente en el servidor
cat ~/.ssh/id_ed25519.pub | ssh root@128.199.13.76 "mkdir -p ~/.ssh && cat >> ~/.ssh/authorized_keys"
```

---

### Error: "Host key verification failed"

**Problema:** GitHub Actions no conoce la huella del servidor
**Solución:** Ya está resuelto con `StrictHostKeyChecking=no` en el workflow

---

### Error: "pm2 command not found"

**Problema:** PM2 no está instalado en el servidor
**Solución:**
```bash
ssh root@128.199.13.76
npm install -g pm2
```

---

## 📝 WORKFLOW SIMPLIFICADO (SIN SECRETS)

Si prefieres **NO usar GitHub Actions para deploy**, puedes:

1. **Deshabilitar el workflow de deploy:**
   - Renombrar `.github/workflows/deploy-production.yml` a `deploy-production.yml.disabled`

2. **Usar los scripts locales:**
   ```bash
   # Desde tu máquina
   ./deploy-backend.sh --remote
   ./deploy-frontend.sh --remote
   ```

3. **Los workflows de CI seguirán funcionando** automáticamente:
   - ✅ `backend-ci.yml` - Compila y testea backend
   - ✅ `frontend-ci.yml` - Compila y testea frontend

---

## 🎯 RECOMENDACIÓN

### Para Desarrollo Rápido (Ahora)
Usa los **scripts locales** para deploy manual:
```bash
./deploy-backend.sh --remote
./deploy-frontend.sh --remote
```

### Para Producción (Futuro)
Configura los **GitHub Secrets** para tener:
- ✅ Deploy automático en cada push a `main`
- ✅ CI/CD completo
- ✅ Rollback automático si falla deploy
- ✅ Notificaciones de deploy

---

## 📞 COMANDOS ÚTILES

### Ver status de workflows
```bash
# Ver último run
gh run list --limit 5

# Ver logs de un run
gh run view <run-id> --log

# Re-ejecutar un workflow fallido
gh run rerun <run-id>
```

### Ejecutar workflow manualmente
```bash
gh workflow run deploy-production.yml \
  --ref main \
  --field environment=production
```

---

## ✅ CHECKLIST DE CONFIGURACIÓN

- [ ] SSH Private Key obtenida
- [ ] GitHub Secrets configurados (5 secrets)
- [ ] SSH key agregada a `authorized_keys` en servidor
- [ ] PM2 instalado en servidor
- [ ] Nginx configurado en servidor
- [ ] Directorios creados en servidor (`/opt/crm-backend`, `/opt/crm-frontend`)
- [ ] Primer deploy manual exitoso (para validar)
- [ ] Workflow de deploy testeado

---

## 🚀 SIGUIENTE PASO

1. **Configurar los secrets** en GitHub (5 minutos)
2. **Hacer un push a main** para activar el workflow
3. **Verificar en GitHub Actions** que todo funciona
4. **Validar en el servidor** que el deploy fue exitoso

O alternativamente:

1. **Desplegar manualmente** con los scripts locales
2. **Configurar secrets más tarde** cuando tengas tiempo

---

**¿Prefieres despliegue manual o automático?**

Si prefieres manual por ahora, simplemente ejecuta:
```bash
./deploy-backend.sh --remote
./deploy-frontend.sh --remote
```

Si quieres automatización completa, configura los 5 secrets en GitHub y todo funcionará automáticamente.
