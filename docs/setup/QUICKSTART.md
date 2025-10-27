# 🚀 Guía de Inicio Rápido - CRM Backend

## ⚡ Start en 3 Pasos

### 1️⃣ Levantar Base de Datos

```bash
cd /Users/lperez/Workspace/Development/fullstack/crm_pd
docker-compose up postgres -d
```

### 2️⃣ Ejecutar Backend

```bash
cd crm-backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3️⃣ Probar API

Abrir navegador en: **http://localhost:8080/swagger-ui.html**

---

## 🔐 Login

1. En Swagger, ir a **Auth Controller**
2. Ejecutar `POST /api/v1/auth/login`
3. Body:
```json
{
  "username": "admin",
  "password": "admin123"
}
```
4. Copiar el `token` de la respuesta
5. Click en **Authorize** (arriba a la derecha)
6. Ingresar: `Bearer {tu-token-aqui}`
7. Click **Authorize**

---

## 📋 Endpoints Disponibles

### Clientes
- `GET /api/v1/clientes` - Listar todos
- `POST /api/v1/clientes` - Crear nuevo (necesita ADMIN o MANAGER)

### Productos
- `GET /api/v1/productos` - Listar todos
- `GET /api/v1/productos/stock-bajo` - Stock bajo

### Ventas
- `GET /api/v1/ventas` - Listar todas
- `POST /api/v1/ventas` - Crear nueva (necesita ADMIN o MANAGER)

### Pagos
- `GET /api/v1/pagos` - Listar todos
- `POST /api/v1/pagos` - Registrar pago (necesita ADMIN o FINANCE)

### Cuentas
- `GET /api/v1/cuentas/vencidas` - Cuentas vencidas
- `POST /api/v1/cuentas` - Crear cuenta (necesita ADMIN o FINANCE)

### Reportes
- `GET /api/v1/reportes/ventas/periodo?fechaInicio=2025-01-01&fechaFin=2025-10-20`
- `GET /api/v1/reportes/productos/mas-vendidos?fechaInicio=2025-01-01&fechaFin=2025-10-20`
- `GET /api/v1/reportes/productos/stock-bajo`

---

## 🛠️ Comandos Útiles

### Maven
```bash
# Compilar
./mvnw clean compile

# Tests
./mvnw test

# Package JAR
./mvnw clean package

# Ver dependencias
./mvnw dependency:tree
```

### Docker
```bash
# Ver logs de PostgreSQL
docker-compose logs -f postgres

# Conectar a PostgreSQL
docker-compose exec postgres psql -U crm_user -d crm_db

# Reiniciar PostgreSQL
docker-compose restart postgres

# Detener todo
docker-compose down
```

### Database
```bash
# Ver migraciones aplicadas
./mvnw flyway:info

# Aplicar migraciones manualmente
./mvnw flyway:migrate
```

---

## 📚 URLs Importantes

| Servicio | URL |
|----------|-----|
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/v3/api-docs |
| **Health Check** | http://localhost:8080/actuator/health |
| **Info** | http://localhost:8080/actuator/info |

---

## 🐛 Troubleshooting

### Puerto 8080 ocupado
```bash
# Encontrar proceso
lsof -i :8080

# Matar proceso
kill -9 <PID>
```

### Error de conexión a BD
```bash
# Verificar que PostgreSQL está corriendo
docker-compose ps

# Ver logs
docker-compose logs postgres
```

### Flyway migration failed
```bash
# Solo en DEV - limpiar y volver a ejecutar
./mvnw flyway:clean
./mvnw flyway:migrate
```

---

## 📞 Soporte

Si necesitas ayuda:
1. Revisar logs: `docker-compose logs -f backend`
2. Verificar configuración: `crm-backend/src/main/resources/application-dev.yml`
3. Consultar documentación completa: `crm-backend/README.md`

---

**¡Listo para desarrollar!** 🎉
