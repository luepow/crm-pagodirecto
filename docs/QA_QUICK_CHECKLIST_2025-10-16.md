# CHECKLIST RAPIDO DE VERIFICACION QA

**Sistema**: PagoDirecto CRM/ERP
**Fecha**: 2025-10-16
**Uso**: Verificacion rapida antes de deployments

---

## BUGS CRITICOS (BLOQUEANTES)

- [ ] **BUG-002/005**: Operaciones POST/PUT/DELETE funcionan sin error de UserDetails
- [ ] **BUG-006**: Tabla `oportunidades_etapas_pipeline` tiene 7 registros
- [ ] **BUG-008**: Endpoint POST /v1/auth/login retorna token JWT valido

**Comando de verificacion rapida**:
```bash
# Verificar login
curl -X POST http://localhost:28080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq '.token'

# Verificar etapas
PGPASSWORD=dev_password_123 psql -h localhost -p 28432 -U pagodirecto_dev \
  -d pagodirecto_crm_dev -c "SELECT COUNT(*) FROM oportunidades_etapas_pipeline;"

# Verificar creacion de cliente
TOKEN="..." # Token del login
curl -X POST http://localhost:28080/api/v1/clientes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "unidadNegocioId":"00000000-0000-0000-0000-000000000001",
    "codigo":"CLI-TEST-001",
    "nombre":"Test Cliente",
    "email":"test@example.com",
    "tipo":"EMPRESA",
    "status":"LEAD"
  }' | jq '.id'
```

**Status esperado**:
- Login: HTTP 200 + token presente
- Etapas: 7
- Cliente: HTTP 201 + UUID generado

---

## BUGS ALTA SEVERIDAD

- [ ] **BUG-001**: GET /v1/clientes/search?q=Maria retorna HTTP 200 (no 400)
- [ ] **BUG-003**: GET /v1/clientes/{id-inexistente} retorna HTTP 404 (no 500)
- [ ] **BUG-004**: GET /v1/oportunidades/{id-inexistente} retorna HTTP 404 (no 500)

**Comando de verificacion rapida**:
```bash
# Test busqueda
curl -s -w "\nHTTP:%{http_code}\n" \
  "http://localhost:28080/api/v1/clientes/search?q=Maria" | tail -n 1

# Test 404 cliente
curl -s -w "\nHTTP:%{http_code}\n" \
  "http://localhost:28080/api/v1/clientes/99999999-9999-9999-9999-999999999999" | tail -n 1

# Test 404 oportunidad
curl -s -w "\nHTTP:%{http_code}\n" \
  "http://localhost:28080/api/v1/oportunidades/99999999-9999-9999-9999-999999999999" | tail -n 1
```

**Status esperado**:
- Search: HTTP:200
- Clientes inexistente: HTTP:404
- Oportunidad inexistente: HTTP:404

---

## ENDPOINTS CRITICOS - SMOKE TEST

### CLIENTES
```bash
# GET lista
curl -s "http://localhost:28080/api/v1/clientes?page=0&size=5" | jq '.totalElements'

# GET por ID
curl -s "http://localhost:28080/api/v1/clientes/c1111111-0000-0000-0000-000000000001" | jq '.id'

# POST crear (requiere token)
TOKEN="..."
curl -X POST http://localhost:28080/api/v1/clientes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"unidadNegocioId":"00000000-0000-0000-0000-000000000001","codigo":"CLI-SMOKE-001","nombre":"Smoke Test","email":"smoke@test.com","tipo":"PERSONA","status":"LEAD"}' \
  | jq '.id'
```

**Esperado**: Todos retornan HTTP 200/201 sin errores

### OPORTUNIDADES
```bash
# GET lista
curl -s "http://localhost:28080/api/v1/oportunidades?page=0&size=5" | jq '.totalElements'

# POST crear (requiere token y cliente existente)
TOKEN="..."
curl -X POST http://localhost:28080/api/v1/oportunidades \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "unidadNegocioId":"00000000-0000-0000-0000-000000000001",
    "clienteId":"c1111111-0000-0000-0000-000000000001",
    "titulo":"Oportunidad Smoke Test",
    "valorEstimado":10000.00,
    "moneda":"MXN",
    "probabilidad":25.0,
    "etapaId":"e0000000-0000-0000-0000-000000000002",
    "propietarioId":"30000000-0000-0000-0000-000000000001"
  }' | jq '.id'
```

**Esperado**: HTTP 201 + UUID generado

---

## SEGURIDAD

- [ ] Endpoints protegidos requieren token
- [ ] Token invalido retorna HTTP 401
- [ ] Headers de seguridad presentes

```bash
# Test sin token (debe fallar)
curl -s -w "\nHTTP:%{http_code}\n" \
  -X POST http://localhost:28080/api/v1/clientes \
  -H "Content-Type: application/json" \
  -d '{"codigo":"TEST"}' | tail -n 1
# Esperado: HTTP:401

# Test token invalido (debe fallar)
curl -s -w "\nHTTP:%{http_code}\n" \
  -X POST http://localhost:28080/api/v1/clientes \
  -H "Authorization: Bearer INVALID_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"codigo":"TEST"}' | tail -n 1
# Esperado: HTTP:401

# Verificar headers de seguridad
curl -s -I http://localhost:28080/api/v1/clientes | grep -E "X-Frame-Options|X-Content-Type|Content-Security-Policy"
# Esperado: Headers presentes
```

---

## PERFORMANCE

- [ ] GET /clientes responde en <50ms (promedio de 10 requests)
- [ ] GET /clientes/{id} responde en <50ms
- [ ] POST /clientes responde en <500ms

```bash
# Test performance (10 requests)
for i in {1..10}; do
  curl -s -w "Request $i: %{time_total}s\n" \
    -o /dev/null \
    "http://localhost:28080/api/v1/clientes?page=0&size=20"
done

# Esperado: Todos <0.050s (50ms)
```

---

## BASE DE DATOS

- [ ] Clientes activos: >=2
- [ ] Etapas de pipeline: 7
- [ ] Soft delete funciona
- [ ] Auditoria funciona

```bash
# Verificar datos
PGPASSWORD=dev_password_123 psql -h localhost -p 28432 -U pagodirecto_dev -d pagodirecto_crm_dev << 'EOF'
SELECT 'Clientes activos:', COUNT(*) FROM clientes_clientes WHERE deleted_at IS NULL;
SELECT 'Etapas pipeline:', COUNT(*) FROM oportunidades_etapas_pipeline;
SELECT 'Oportunidades:', COUNT(*) FROM oportunidades_oportunidades WHERE deleted_at IS NULL;
EOF
```

**Esperado**:
- Clientes activos: >=2
- Etapas pipeline: 7
- Oportunidades: >=0

---

## SERVICIOS

- [ ] Backend API: http://localhost:28080/api/actuator/health retorna UP
- [ ] Frontend: http://localhost:28000 carga correctamente
- [ ] PostgreSQL: puerto 28432 accesible
- [ ] Adminer: http://localhost:28081 accesible

```bash
# Verificar servicios
curl -s http://localhost:28080/api/actuator/health | jq '.status'
# Esperado: "UP"

curl -s -w "\nHTTP:%{http_code}\n" http://localhost:28000 | tail -n 1
# Esperado: HTTP:200

nc -zv localhost 28432 2>&1 | grep succeeded
# Esperado: Connection succeeded

curl -s -w "\nHTTP:%{http_code}\n" http://localhost:28081 | tail -n 1
# Esperado: HTTP:200
```

---

## INTEGRACION FRONTEND-BACKEND

- [ ] Frontend puede listar clientes
- [ ] Frontend puede crear clientes (con autenticacion)
- [ ] Frontend muestra errores correctamente

**Verificacion manual** (abrir en navegador):
1. http://localhost:28000
2. Navegar a /clientes
3. Verificar que la lista carga
4. Intentar crear un cliente

---

## LOGS

- [ ] No hay errores 500 inesperados en logs
- [ ] Warnings de seguridad son claros
- [ ] Queries SQL no tienen errores

```bash
# Ver logs del backend (ultimas 50 lineas)
docker-compose logs --tail=50 backend | grep -E "ERROR|WARN|Exception"

# O si se ejecuta directamente:
tail -n 50 backend/logs/application.log | grep -E "ERROR|WARN"
```

**Esperado**: No errores inesperados, solo warnings controlados

---

## CHECKLIST PRE-DEPLOYMENT

### STAGING
- [ ] Todos los bugs criticos resueltos
- [ ] Todos los bugs alta severidad resueltos
- [ ] Smoke tests pasan
- [ ] Tests E2E pasan
- [ ] Autenticacion funcional
- [ ] Seed data presente

### PRODUCCION
- [ ] Todos los bugs resueltos (incluido P2)
- [ ] Load testing completado (1000 usuarios)
- [ ] Security audit aprobado
- [ ] Backups configurados y probados
- [ ] Monitoreo configurado
- [ ] Logs enviados a ELK/CloudWatch
- [ ] Variables de entorno configuradas
- [ ] Secrets en vault (no hardcoded)
- [ ] HSTS habilitado
- [ ] CORS configurado para produccion
- [ ] Rate limiting habilitado
- [ ] Documentacion actualizada
- [ ] Runbooks creados

---

## SCRIPT DE VERIFICACION AUTOMATICA

```bash
#!/bin/bash
# qa-smoke-test.sh
# Ejecuta verificacion rapida de todos los endpoints criticos

set -e

BASE_URL="http://localhost:28080/api"
FRONTEND_URL="http://localhost:28000"

echo "=== SMOKE TEST QA - PagoDirecto CRM/ERP ==="
echo ""

# 1. Verificar servicios
echo "[1/8] Verificando servicios..."
curl -sf "$BASE_URL/actuator/health" > /dev/null && echo "  ✓ Backend UP" || echo "  ✗ Backend DOWN"
curl -sf "$FRONTEND_URL" > /dev/null && echo "  ✓ Frontend UP" || echo "  ✗ Frontend DOWN"

# 2. Autenticacion
echo "[2/8] Verificando autenticacion..."
TOKEN=$(curl -sf -X POST "$BASE_URL/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.token')

if [ -n "$TOKEN" ]; then
  echo "  ✓ Login exitoso"
else
  echo "  ✗ Login fallido"
  exit 1
fi

# 3. GET endpoints
echo "[3/8] Verificando endpoints GET..."
curl -sf "$BASE_URL/v1/clientes?page=0&size=5" > /dev/null && echo "  ✓ GET /clientes" || echo "  ✗ GET /clientes"
curl -sf "$BASE_URL/v1/oportunidades?page=0&size=5" > /dev/null && echo "  ✓ GET /oportunidades" || echo "  ✗ GET /oportunidades"

# 4. POST endpoints (con autenticacion)
echo "[4/8] Verificando endpoints POST..."
CLIENTE_ID=$(curl -sf -X POST "$BASE_URL/v1/clientes" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "unidadNegocioId":"00000000-0000-0000-0000-000000000001",
    "codigo":"SMOKE-'$(date +%s)'",
    "nombre":"Smoke Test Cliente",
    "email":"smoke@test.com",
    "tipo":"PERSONA",
    "status":"LEAD"
  }' | jq -r '.id')

if [ -n "$CLIENTE_ID" ] && [ "$CLIENTE_ID" != "null" ]; then
  echo "  ✓ POST /clientes (ID: $CLIENTE_ID)"
else
  echo "  ✗ POST /clientes fallido"
fi

# 5. Verificar error 404 (no 500)
echo "[5/8] Verificando manejo de errores..."
HTTP_CODE=$(curl -sf -w "%{http_code}" -o /dev/null "$BASE_URL/v1/clientes/99999999-9999-9999-9999-999999999999" || echo "404")
if [ "$HTTP_CODE" = "404" ]; then
  echo "  ✓ Error 404 correcto"
else
  echo "  ✗ Error 404 retorna HTTP $HTTP_CODE"
fi

# 6. Verificar busqueda
echo "[6/8] Verificando busqueda..."
SEARCH_RESULT=$(curl -sf "$BASE_URL/v1/clientes/search?q=Test" | jq -r '.totalElements')
if [ -n "$SEARCH_RESULT" ]; then
  echo "  ✓ Search funcional ($SEARCH_RESULT resultados)"
else
  echo "  ✗ Search fallido"
fi

# 7. Verificar headers de seguridad
echo "[7/8] Verificando headers de seguridad..."
curl -sI "$BASE_URL/v1/clientes" | grep -q "X-Frame-Options" && echo "  ✓ X-Frame-Options presente" || echo "  ✗ X-Frame-Options ausente"
curl -sI "$BASE_URL/v1/clientes" | grep -q "X-Content-Type-Options" && echo "  ✓ X-Content-Type-Options presente" || echo "  ✗ X-Content-Type-Options ausente"

# 8. Verificar performance
echo "[8/8] Verificando performance..."
RESPONSE_TIME=$(curl -sf -w "%{time_total}" -o /dev/null "$BASE_URL/v1/clientes?page=0&size=20")
if (( $(echo "$RESPONSE_TIME < 0.1" | bc -l) )); then
  echo "  ✓ Performance OK (${RESPONSE_TIME}s)"
else
  echo "  ⚠ Performance lenta (${RESPONSE_TIME}s)"
fi

echo ""
echo "=== SMOKE TEST COMPLETADO ==="
```

**Uso**:
```bash
chmod +x qa-smoke-test.sh
./qa-smoke-test.sh
```

---

## METRICAS OBJETIVO

| Metrica | Valor minimo | Estado actual |
|---------|--------------|---------------|
| Tasa de exito endpoints | >95% | A verificar |
| Tiempo respuesta GET | <200ms | ~4.5ms ✓ |
| Tiempo respuesta POST | <500ms | A verificar |
| Bugs criticos | 0 | 4 ✗ |
| Bugs alta severidad | 0 | 3 ✗ |
| Cobertura tests | >80% | A verificar |
| Uptime | >99.9% | A medir |

---

**FIN DEL CHECKLIST RAPIDO**
