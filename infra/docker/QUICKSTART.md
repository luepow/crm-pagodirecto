# Quick Start Guide - PagoDirecto CRM/ERP

Get the entire application running in **under 5 minutes**!

## Prerequisites

- Docker 20.10+ installed ([Get Docker](https://docs.docker.com/get-docker/))
- Docker Compose 2.0+ installed
- 8GB RAM available
- 10GB disk space

## 5-Minute Setup

### Step 1: Clone the Repository (30 seconds)

```bash
git clone https://github.com/pagodirecto/crm-erp.git
cd crm-erp/infra/docker
```

### Step 2: Configure Environment (1 minute)

```bash
# Copy the development environment file
cp .env.development .env

# That's it! Development defaults are ready to use.
```

For production, use `.env.production` instead and **change all passwords!**

### Step 3: Start Everything (3 minutes)

```bash
# Navigate to scripts directory
cd ../scripts

# Start all services
./start.sh development
```

The script will:
1. Build Docker images
2. Start all services (PostgreSQL, Backend, Frontend, Adminer)
3. Wait for services to be healthy
4. Display access URLs

### Step 4: Access Your Application (immediate)

In desarrollo, los puertos pueden ser asignados aleatoriamente para evitar conflictos locales. Revisa la salida de `./start.sh` para ver los puertos efectivos. También puedes usar `docker-compose ps`.

Ejemplos (los puertos reales pueden variar):
```
Frontend:    http://localhost:<FRONTEND_PORT>
Backend API: http://localhost:<BACKEND_PORT>/api
API Docs:    http://localhost:<BACKEND_PORT>/swagger-ui.html
Database UI: http://localhost:<ADMINER_PORT>
Health:      http://localhost:<BACKEND_PORT>/actuator/health
```

**Database Credentials** (for Adminer):
- System: PostgreSQL
- Server: postgres
- Username: pagodirecto_dev
- Password: dev_password_123
- Database: pagodirecto_crm_dev

## Verify Installation

```bash
# Check all services are running
docker-compose ps

# Should show:
# ✔ pagodirecto_postgres   (healthy)
# ✔ pagodirecto_backend    (healthy)
# ✔ pagodirecto_frontend   (healthy)
# ✔ pagodirecto_adminer    (healthy)
```

## Common Commands

```bash
# View logs
./logs.sh                # All services
./logs.sh backend        # Just backend
./logs.sh backend -f     # Follow backend logs

# Restart services
./restart.sh             # All services
./restart.sh backend     # Just backend

# Stop services
./stop.sh

# Clean up everything
./clean.sh --all

# Backup database
./backup-db.sh

# Restore database
./restore-db.sh backup_name
```

## Troubleshooting

### Port Already in Use

```bash
# Check what's using the port
lsof -i :8080

# Change port in .env
BACKEND_PORT=8081
```

### Services Won't Start

```bash
# Check Docker is running
docker info

# View detailed logs
docker-compose logs postgres
docker-compose logs backend
```

### Out of Memory

```bash
# Check Docker memory allocation
docker stats

# Increase Docker Desktop memory:
# Preferences → Resources → Memory (set to 8GB+)
```

### Reset Everything

```bash
# Nuclear option: removes all data!
./clean.sh --volumes --force
./start.sh development
```

## Next Steps

1. **Explore the API**
   - Visit http://localhost:8080/swagger-ui.html
   - Try the health endpoint: `curl http://localhost:8080/actuator/health`

2. **Check the Database**
   - Open Adminer: http://localhost:8081
   - Browse schemas: seguridad, clientes, productos, ventas

3. **Read Full Documentation**
   - Complete guide: [README.md](./README.md)
   - Architecture: [/docs/c4/](../../docs/c4/)
   - API docs: http://localhost:8080/swagger-ui.html

4. **Development Workflow**
   - Backend code: `/backend/application/src/`
   - Frontend code: `/frontend/apps/web/src/`
   - Database migrations: `/backend/application/src/main/resources/db/migration/`

## Production Deployment

For production deployment, see the [Production Deployment](./README.md#production-deployment) section in the full README.

**Critical Steps:**
1. Use `.env.production` as template
2. Change ALL default passwords
3. Generate strong JWT secret: `openssl rand -base64 64`
4. Configure SSL certificates
5. Set up automated backups
6. Enable monitoring/alerting

---

**Need Help?**
- Full documentation: [README.md](./README.md)
- Issues: [GitHub Issues](https://github.com/pagodirecto/crm-erp/issues)
- Email: devops@pagodirecto.com

**Happy coding! 🚀**
