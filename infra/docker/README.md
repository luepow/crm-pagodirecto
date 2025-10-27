# PagoDirecto CRM/ERP - Docker Infrastructure

> Complete Docker infrastructure for running the entire PagoDirecto CRM/ERP system with a single command.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Services](#services)
- [Networking](#networking)
- [Data Persistence](#data-persistence)
- [Security](#security)
- [Monitoring](#monitoring)
- [Backup & Recovery](#backup--recovery)
- [Troubleshooting](#troubleshooting)
- [Performance Tuning](#performance-tuning)
- [Production Deployment](#production-deployment)
- [Development Workflow](#development-workflow)
- [Maintenance](#maintenance)

---

## Overview

This Docker infrastructure provides a complete, production-ready environment for the PagoDirecto CRM/ERP system. It implements best practices for security, performance, scalability, and maintainability.

### What's Included

- **PostgreSQL 16** - Primary database with advanced extensions
- **Spring Boot Backend** - Java 17 RESTful API with Clean Architecture
- **React Frontend** - Modern SPA with Vite and TypeScript
- **Nginx** - High-performance reverse proxy and static file server
- **Adminer** - Database management UI (development only)

### Key Features

- **One-Command Deployment** - Start entire stack with `docker-compose up`
- **Multi-Stage Builds** - Optimized Docker images (backend: ~180MB, frontend: ~40MB)
- **Health Checks** - Automatic health monitoring for all services
- **Security Hardening** - Non-root users, minimal images, security headers
- **Resource Management** - Configurable CPU and memory limits
- **Data Persistence** - Named volumes for database and cache
- **Hot Reload Support** - Development mode with live code updates
- **Automated Backups** - Database backup and restore scripts
- **Comprehensive Logging** - Structured logs for debugging and monitoring

---

## Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Internet                              │
└────────────────────────┬────────────────────────────────────┘
                         │
                    Port 80/443
                         │
         ┌───────────────▼──────────────┐
         │     Nginx Reverse Proxy      │  (Production only)
         │  - SSL Termination           │
         │  - Load Balancing            │
         │  - Security Headers          │
         └───────┬──────────────┬───────┘
                 │              │
        Port 80  │              │  Port 8080
                 │              │
    ┌────────────▼────┐    ┌───▼────────────────┐
    │   Frontend      │    │   Backend API      │
    │   (React+Nginx) │    │   (Spring Boot)    │
    │                 │    │                    │
    │  - SPA Routing  │    │  - REST API        │
    │  - Static Files │    │  - Business Logic  │
    │  - Security     │    │  - Authentication  │
    └─────────────────┘    └───┬────────────────┘
                               │
                          Port 5432
                               │
                   ┌───────────▼────────────┐
                   │    PostgreSQL 16       │
                   │                        │
                   │  - Core Domain         │
                   │  - Multi-tenant        │
                   │  - Row-Level Security  │
                   └────────────────────────┘
```

### Docker Network

All services communicate through an isolated bridge network (`pagodirecto_network`):

- **DNS Resolution** - Services accessible by name (e.g., `postgres`, `backend`)
- **Subnet** - 172.28.0.0/16
- **Isolation** - No direct external access (except exposed ports)

### Data Flow

1. **User Request** → Nginx → Frontend (for static files)
2. **API Request** → Nginx → Backend → PostgreSQL
3. **Database Query** → Backend → PostgreSQL (with connection pooling)

---

## Prerequisites

### Required Software

- **Docker** 20.10+ ([Install Docker](https://docs.docker.com/get-docker/))
- **Docker Compose** 2.0+ ([Install Compose](https://docs.docker.com/compose/install/))
- **Git** (for cloning repository)

### System Requirements

#### Development

- **CPU**: 2 cores minimum, 4 cores recommended
- **RAM**: 4GB minimum, 8GB recommended
- **Disk**: 10GB free space
- **OS**: Linux, macOS, or Windows with WSL2

#### Production

- **CPU**: 4 cores minimum, 8+ cores recommended
- **RAM**: 8GB minimum, 16GB+ recommended
- **Disk**: 50GB+ SSD storage
- **OS**: Linux (Ubuntu 22.04 LTS or CentOS 8+)

### Verify Installation

```bash
# Check Docker
docker --version
# Expected: Docker version 20.10.0 or higher

# Check Docker Compose
docker-compose --version
# Expected: Docker Compose version 2.0.0 or higher

# Check Docker daemon
docker info
# Should show system information without errors
```

---

## Quick Start

### 1. Clone Repository

```bash
git clone https://github.com/pagodirecto/crm-erp.git
cd crm-erp/infra/docker
```

### 2. Configure Environment

```bash
# Copy example environment file
cp .env.example .env

# Edit with your settings (IMPORTANT: Change passwords!)
nano .env

# Or use development defaults
cp .env.development .env
```

### 3. Start Services

```bash
# Using the start script (recommended)
cd ../scripts
./start.sh development

# Or using docker-compose directly
cd ../docker
docker-compose --profile development up -d
```

### 4. Verify Services

```bash
# Check service status
docker-compose ps

# Check logs
docker-compose logs -f

# Health check
curl http://localhost:8080/actuator/health
```

### 5. Access Application

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Database Admin**: http://localhost:8081 (Adminer)
- **Health Check**: http://localhost:8080/actuator/health

---

## Configuration

### Environment Variables

Configuration is managed through `.env` files:

- **`.env.example`** - Template with all available options
- **`.env.development`** - Development defaults
- **`.env.production`** - Production template (requires customization)

#### Key Variables

```bash
# Environment
ENVIRONMENT=development|production
VERSION=1.0.0

# Database
POSTGRES_DB=pagodirecto_crm
POSTGRES_USER=pagodirecto
POSTGRES_PASSWORD=change_this_password

# Backend
SPRING_PROFILES_ACTIVE=development|production
JWT_SECRET=your-strong-secret-key
CORS_ALLOWED_ORIGINS=http://localhost:3000

# Frontend
VITE_API_URL=http://localhost:8080/api

# Ports
POSTGRES_PORT=5432
BACKEND_PORT=8080
FRONTEND_PORT=3000
ADMINER_PORT=8081
```

### Docker Compose Profiles

Use profiles to control which services run:

```bash
# Development (all services including Adminer)
docker-compose --profile development up -d

# Production (no Adminer, with nginx proxy)
docker-compose --profile production up -d

# Testing (isolated test environment)
docker-compose --profile testing up -d
```

---

## Services

### PostgreSQL Database

**Image**: `postgres:16-alpine`
**Port**: 5432
**Memory**: 2GB
**CPU**: 2 cores

#### Features

- UUID generation (`uuid-ossp`)
- Cryptographic functions (`pgcrypto`)
- Query performance monitoring (`pg_stat_statements`)
- Fuzzy text search (`pg_trgm`)
- Row-Level Security (RLS) for multi-tenancy
- Automated schema initialization
- Performance tuning for OLTP workloads

#### Database Schemas

- `core_domain` - Shared domain primitives
- `seguridad` - Security and IAM
- `clientes` - Customer management
- `oportunidades` - Sales opportunities
- `tareas` - Task management
- `productos` - Product catalog
- `ventas` - Sales transactions
- `reportes` - Reporting and analytics

#### Connection Details

```bash
# From host machine
psql -h localhost -p 5432 -U pagodirecto -d pagodirecto_crm

# From within Docker network
psql -h postgres -p 5432 -U pagodirecto -d pagodirecto_crm

# Using Adminer
http://localhost:8081
Server: postgres
Username: pagodirecto
Password: (from .env)
Database: pagodirecto_crm
```

### Spring Boot Backend

**Image**: Custom multi-stage build
**Port**: 8080
**Memory**: 1.5GB
**CPU**: 2 cores

#### Features

- Java 17 + Spring Boot 3.2.5
- Clean/Hexagonal Architecture
- Domain-Driven Design (DDD)
- JWT Authentication with rotation
- RBAC/ABAC Authorization
- Flyway database migrations
- Spring Data JPA with Hibernate
- OpenAPI/Swagger documentation
- Actuator health checks
- Prometheus metrics

#### Endpoints

- **API Base**: `http://localhost:8080/api`
- **Health**: `http://localhost:8080/actuator/health`
- **Metrics**: `http://localhost:8080/actuator/metrics`
- **API Docs**: `http://localhost:8080/swagger-ui.html`

#### JVM Configuration

```bash
# Memory settings (configurable via JAVA_OPTS)
-Xms512m          # Initial heap size
-Xmx1024m         # Maximum heap size
-XX:+UseG1GC      # G1 garbage collector
-XX:MaxGCPauseMillis=200  # Target GC pause time
```

### React Frontend

**Image**: Custom multi-stage build
**Port**: 3000 (development), 80 (production)
**Memory**: 512MB
**CPU**: 1 core

#### Features

- React 18 with TypeScript
- Vite build tool (fast HMR)
- TailwindCSS for styling
- React Router for navigation
- React Query for server state
- Zustand for client state
- Form validation with React Hook Form + Zod
- Responsive design
- Progressive Web App (PWA) support

#### Build Modes

- **Development**: Hot Module Replacement (HMR), source maps
- **Production**: Minified, tree-shaken, optimized bundles

### Nginx

**Image**: `nginx:alpine`
**Port**: 80 (HTTP), 443 (HTTPS)
**Memory**: 256MB
**CPU**: 1 core

#### Features

- Reverse proxy to backend API
- Static file serving for frontend
- Gzip compression
- Security headers (CSP, HSTS, X-Frame-Options)
- CORS configuration
- Rate limiting
- SSL/TLS termination (when configured)
- Access and error logging

### Adminer (Development Only)

**Image**: `adminer:latest`
**Port**: 8081
**Memory**: 256MB
**CPU**: 0.5 cores

#### Features

- Web-based database management
- SQL query editor
- Schema visualization
- Data import/export
- Multi-database support

**Security Note**: Disable in production or implement strong authentication.

---

## Networking

### Bridge Network

All services communicate through `pagodirecto_network`:

```yaml
networks:
  pagodirecto_network:
    driver: bridge
    subnet: 172.28.0.0/16
```

### Service Discovery

Services use container names as hostnames:

```bash
# Backend connects to database
jdbc:postgresql://postgres:5432/pagodirecto_crm

# Frontend connects to backend (via nginx)
http://backend:8080/api

# Nginx proxies to backend
proxy_pass http://backend:8080;
```

### Port Mapping

| Service   | Internal Port | External Port (default) |
|-----------|---------------|-------------------------|
| PostgreSQL| 5432          | 5432                    |
| Backend   | 8080          | 8080                    |
| Frontend  | 80            | 3000                    |
| Nginx     | 80/443        | 80/443                  |
| Adminer   | 8080          | 8081                    |

---

## Data Persistence

### Named Volumes

Data persists across container restarts:

```yaml
volumes:
  postgres_data:        # Database files
  postgres_backups:     # Backup storage
  maven_cache:          # Maven dependencies
```

### Volume Management

```bash
# List volumes
docker volume ls

# Inspect volume
docker volume inspect pagodirecto_postgres_data

# Backup volume (database)
docker run --rm -v pagodirecto_postgres_data:/data \
  -v $(pwd):/backup alpine tar czf /backup/postgres_data.tar.gz /data

# Restore volume
docker run --rm -v pagodirecto_postgres_data:/data \
  -v $(pwd):/backup alpine tar xzf /backup/postgres_data.tar.gz -C /

# Remove volumes (WARNING: deletes data!)
docker-compose down -v
```

---

## Security

### Security Best Practices Implemented

#### 1. Non-Root Users

All containers run as non-root users:

- **Backend**: `appuser` (UID 1000+)
- **Frontend**: `nginx` (UID 101)
- **PostgreSQL**: `postgres` (UID 999)

#### 2. Minimal Base Images

- **Alpine Linux** - Reduces attack surface (~5MB vs ~200MB)
- **Multi-stage builds** - Build tools not in production images

#### 3. Security Headers

Nginx adds comprehensive security headers:

```nginx
X-Frame-Options: SAMEORIGIN
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Content-Security-Policy: (configured)
Referrer-Policy: strict-origin-when-cross-origin
```

#### 4. Secret Management

**Development**:
```bash
# Secrets in .env file (not committed to git)
JWT_SECRET=dev-secret
POSTGRES_PASSWORD=dev-password
```

**Production**:
```bash
# Use Docker secrets or external vault
docker secret create jwt_secret jwt_secret.txt
docker secret create db_password db_password.txt

# Or use environment variables from CI/CD
export JWT_SECRET=$(vault read -field=value secret/jwt)
```

#### 5. Network Isolation

- Services only accessible through defined ports
- Inter-service communication on private network
- Firewall rules to restrict external access

#### 6. Database Security

- Row-Level Security (RLS) for multi-tenant isolation
- Prepared statements (SQL injection prevention)
- Connection pooling with limits
- Encrypted passwords (`scram-sha-256`)

#### 7. Input Validation

- Backend: Bean Validation (JSR-380)
- Frontend: Zod schema validation
- SQL injection protection (JPA/Hibernate)
- XSS protection (React escaping + CSP)

### Security Checklist for Production

- [ ] Change all default passwords
- [ ] Generate strong JWT secret (64+ characters)
- [ ] Configure SSL/TLS certificates
- [ ] Restrict CORS to actual domains
- [ ] Disable Adminer or secure it
- [ ] Implement IP whitelisting
- [ ] Enable rate limiting
- [ ] Set up Web Application Firewall (WAF)
- [ ] Configure database SSL connections
- [ ] Implement secrets management (Vault, AWS Secrets Manager)
- [ ] Enable audit logging
- [ ] Regular security scanning (SAST/DAST)
- [ ] Dependency vulnerability scanning
- [ ] Penetration testing
- [ ] Incident response plan

---

## Monitoring

### Health Checks

All services include health checks:

```bash
# Backend health
curl http://localhost:8080/actuator/health

# Frontend health
curl http://localhost:3000/health

# Database health
docker-compose exec postgres pg_isready -U pagodirecto
```

### Metrics (Prometheus)

Backend exposes Prometheus metrics:

```bash
# Metrics endpoint
curl http://localhost:8080/actuator/prometheus

# Common metrics:
# - http_server_requests_seconds_count (request count)
# - http_server_requests_seconds_sum (total duration)
# - jvm_memory_used_bytes (memory usage)
# - hikaricp_connections_active (DB connections)
```

### Logging

#### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend

# Last N lines
docker-compose logs --tail=100 backend

# Since timestamp
docker-compose logs --since="2024-01-01T00:00:00" backend
```

#### Log Locations

```bash
# Container logs
/var/log/nginx/          # Nginx logs
/app/logs/               # Backend logs
/var/log/postgresql/     # PostgreSQL logs (if configured)

# Host logs (via volumes)
./logs/backend/          # Backend application logs
./logs/frontend/         # Nginx access/error logs
```

#### Log Aggregation (Optional)

Integrate with ELK Stack or similar:

```yaml
# docker-compose.yml addition
services:
  backend:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

---

## Backup & Recovery

### Automated Backups

Use the provided backup script:

```bash
# Create backup
./scripts/backup-db.sh

# Create named backup
./scripts/backup-db.sh my_backup_name

# Backups are stored in: infra/docker/backups/
```

### Restore Database

```bash
# List available backups
ls -lh infra/docker/backups/

# Restore backup
./scripts/restore-db.sh backup_name

# Example
./scripts/restore-db.sh pagodirecto_crm_20240101_120000
```

### Backup Strategy (Production)

#### Daily Automated Backups

```bash
# Add to crontab
0 2 * * * /path/to/scripts/backup-db.sh

# Retention: 30 days (automatically cleaned up)
```

#### Off-site Backup

```bash
# Sync to S3
aws s3 sync ./backups/ s3://pagodirecto-backups/database/ \
  --exclude "*" --include "*.sql.gz"

# Or rsync to remote server
rsync -avz ./backups/ user@backup-server:/backups/pagodirecto/
```

#### Point-in-Time Recovery (PITR)

Enable WAL archiving in PostgreSQL:

```bash
# postgres.conf
archive_mode = on
archive_command = 'test ! -f /backups/archive/%f && cp %p /backups/archive/%f'

# Perform base backup
docker exec postgres pg_basebackup -D /backups/base -F tar -X fetch -U pagodirecto
```

---

## Troubleshooting

### Common Issues

#### 1. Port Already in Use

```bash
# Error: Bind for 0.0.0.0:8080 failed: port is already allocated

# Solution: Find and stop conflicting process
lsof -i :8080
kill -9 <PID>

# Or change port in .env
BACKEND_PORT=8081
```

#### 2. Database Connection Failed

```bash
# Error: Connection to postgres:5432 refused

# Check if PostgreSQL is running
docker-compose ps postgres

# Check PostgreSQL logs
docker-compose logs postgres

# Verify credentials in .env
POSTGRES_USER=pagodirecto
POSTGRES_PASSWORD=correct_password

# Test connection
docker-compose exec postgres psql -U pagodirecto -d pagodirecto_crm
```

#### 3. Out of Memory

```bash
# Error: Container killed (OOMKilled)

# Check Docker resources
docker stats

# Increase memory limits in .env
BACKEND_MEMORY_LIMIT=2G

# Or adjust Docker daemon settings
# Docker Desktop: Preferences → Resources → Memory
```

#### 4. Build Failures

```bash
# Error: Maven build failed / NPM install failed

# Clean Docker build cache
docker builder prune -a

# Rebuild without cache
docker-compose build --no-cache

# Check network connectivity (for dependency downloads)
ping maven.apache.org
ping registry.npmjs.org
```

#### 5. Slow Performance

```bash
# Check resource usage
docker stats

# Optimize PostgreSQL (adjust shared_buffers, work_mem)
# Edit: infra/docker/postgres.conf

# Enable connection pooling (HikariCP already configured)
# Increase pool size in .env if needed
DB_POOL_SIZE=50

# Check query performance
docker-compose exec postgres psql -U pagodirecto -d pagodirecto_crm \
  -c "SELECT * FROM pg_stat_statements ORDER BY total_exec_time DESC LIMIT 10;"
```

### Debug Mode

#### Enable Verbose Logging

```bash
# Backend (in .env)
LOG_LEVEL_ROOT=DEBUG
LOG_LEVEL_APP=DEBUG
LOG_LEVEL_SQL=DEBUG
SHOW_SQL=true

# PostgreSQL
POSTGRES_LOG_STATEMENT=all
POSTGRES_LOG_MIN_DURATION=0

# Restart services
docker-compose restart backend postgres
```

#### Interactive Debugging

```bash
# Access container shell
docker-compose exec backend sh
docker-compose exec frontend sh
docker-compose exec postgres bash

# Run commands inside container
docker-compose exec backend ./mvnw test
docker-compose exec postgres psql -U pagodirecto -d pagodirecto_crm
```

---

## Performance Tuning

### Database Optimization

#### Connection Pooling

```bash
# HikariCP settings (in .env)
DB_POOL_SIZE=50              # Max connections
DB_POOL_MIN_IDLE=10          # Min idle connections
DB_CONNECTION_TIMEOUT=30000  # Connection timeout (ms)
```

#### PostgreSQL Tuning

```bash
# Memory settings (postgres.conf)
shared_buffers = 512MB           # 25% of RAM
effective_cache_size = 1536MB    # 75% of RAM
work_mem = 16MB                  # Per-operation memory
maintenance_work_mem = 128MB     # For VACUUM, INDEX

# Performance settings
random_page_cost = 1.1           # SSD optimization
effective_io_concurrency = 200   # SSD concurrent I/O
```

#### Query Optimization

```bash
# Enable query logging
POSTGRES_LOG_MIN_DURATION=1000  # Log queries > 1 second

# Analyze slow queries
docker-compose exec postgres psql -U pagodirecto -d pagodirecto_crm
pagodirecto_crm=# SELECT query, calls, total_exec_time, mean_exec_time
                  FROM pg_stat_statements
                  ORDER BY total_exec_time DESC
                  LIMIT 10;

# Explain query plan
pagodirecto_crm=# EXPLAIN ANALYZE SELECT * FROM clientes.clientes WHERE email = 'test@example.com';
```

### Backend Optimization

#### JVM Tuning

```bash
# Adjust heap size (in .env or docker-compose.yml)
JAVA_OPTS="-Xms1024m -Xmx2048m -XX:+UseG1GC"

# For production
JAVA_OPTS="-Xms2048m -Xmx4096m
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/app/logs/heap_dump.hprof"
```

#### Application-Level Caching

```bash
# Spring Cache (configure in application.yml)
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=10000,expireAfterWrite=600s
```

### Frontend Optimization

#### Build Optimization

```javascript
// vite.config.ts
export default defineConfig({
  build: {
    target: 'esnext',
    minify: 'esbuild',
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          router: ['react-router-dom'],
        },
      },
    },
  },
});
```

#### Nginx Caching

```nginx
# Cache static assets (nginx-default.conf)
location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}
```

---

## Production Deployment

### Pre-Deployment Checklist

#### Security

- [ ] Changed all default passwords
- [ ] Generated strong JWT secret
- [ ] Configured SSL certificates
- [ ] Updated CORS to production domains
- [ ] Disabled debug endpoints
- [ ] Implemented rate limiting
- [ ] Set up WAF (Web Application Firewall)
- [ ] Configured secrets management

#### Configuration

- [ ] Set `ENVIRONMENT=production`
- [ ] Set `NODE_ENV=production`
- [ ] Set `SPRING_PROFILES_ACTIVE=production`
- [ ] Configured production database
- [ ] Set appropriate resource limits
- [ ] Configured backup strategy
- [ ] Set up monitoring/alerting

#### Testing

- [ ] Load testing completed
- [ ] Security scanning passed
- [ ] Database migrations tested
- [ ] Backup and restore tested
- [ ] Failover testing completed
- [ ] Disaster recovery plan documented

### Deployment Steps

#### 1. Prepare Environment

```bash
# Copy production template
cp .env.production .env

# Edit with production values
nano .env

# IMPORTANT: Change all passwords and secrets!
```

#### 2. Build Images

```bash
# Build production images
docker-compose --profile production build

# Tag images with version
docker tag pagodirecto/crm-backend:latest pagodirecto/crm-backend:1.0.0
docker tag pagodirecto/crm-frontend:latest pagodirecto/crm-frontend:1.0.0
```

#### 3. Database Migration

```bash
# Start database only
docker-compose up -d postgres

# Wait for database
sleep 30

# Run migrations (automatic via Flyway on backend start)
docker-compose up -d backend

# Verify migrations
docker-compose exec postgres psql -U pagodirecto -d pagodirecto_crm \
  -c "SELECT * FROM flyway_schema_history ORDER BY installed_on DESC LIMIT 5;"
```

#### 4. Start All Services

```bash
# Start all production services
docker-compose --profile production up -d

# Monitor startup
docker-compose logs -f
```

#### 5. Verify Deployment

```bash
# Health checks
curl https://yourdomain.com/health
curl https://api.yourdomain.com/actuator/health

# Check logs
docker-compose logs --tail=100

# Monitor metrics
curl https://api.yourdomain.com/actuator/metrics
```

### SSL/TLS Configuration

#### Using Let's Encrypt

```bash
# Install Certbot
sudo apt-get install certbot

# Obtain certificate
sudo certbot certonly --standalone -d yourdomain.com -d www.yourdomain.com

# Copy certificates
sudo cp /etc/letsencrypt/live/yourdomain.com/fullchain.pem ./certs/
sudo cp /etc/letsencrypt/live/yourdomain.com/privkey.pem ./certs/

# Update .env
SSL_ENABLED=true
SSL_CERT_PATH=./certs/fullchain.pem
SSL_KEY_PATH=./certs/privkey.pem

# Restart nginx
docker-compose restart nginx
```

#### Auto-Renewal

```bash
# Add to crontab
0 0 * * * certbot renew --quiet && docker-compose restart nginx
```

### Horizontal Scaling

#### Multiple Backend Instances

```yaml
# docker-compose.yml
services:
  backend:
    deploy:
      replicas: 3  # Run 3 backend instances

  nginx:
    # Nginx will load balance across all backend instances
```

#### Database Replication

```yaml
# docker-compose.yml (add read replicas)
services:
  postgres-replica:
    image: postgres:16-alpine
    environment:
      POSTGRES_PRIMARY_HOST: postgres
      POSTGRES_PRIMARY_PORT: 5432
    # Configure streaming replication
```

---

## Development Workflow

### Local Development Setup

```bash
# 1. Clone repository
git clone https://github.com/pagodirecto/crm-erp.git
cd crm-erp

# 2. Setup environment
cd infra/docker
cp .env.development .env

# 3. Start services
cd ../scripts
./start.sh development

# 4. Access services
# Frontend: http://localhost:3000 (with HMR)
# Backend: http://localhost:8080 (with auto-reload)
# Database: http://localhost:8081 (Adminer)
```

### Hot Reload

#### Backend (Spring Boot DevTools)

```bash
# Enable DevTools in .env
SPRING_DEVTOOLS_ENABLED=true

# Mount source code (for live reload)
volumes:
  - ../../backend:/app:ro

# Make changes to Java files
# Backend automatically reloads on save
```

#### Frontend (Vite HMR)

```bash
# Already enabled in development mode

# Mount source code
volumes:
  - ../../frontend/apps/web:/app:ro
  - /app/node_modules  # Preserve node_modules

# Make changes to React components
# Browser automatically refreshes
```

### Testing

#### Unit Tests

```bash
# Backend
docker-compose exec backend ./mvnw test

# Frontend
docker-compose exec frontend npm test
```

#### Integration Tests

```bash
# Backend with test database
docker-compose --profile testing up -d
docker-compose exec backend ./mvnw verify
```

#### E2E Tests

```bash
# Using Playwright or Cypress
docker-compose exec frontend npm run test:e2e
```

### Database Management

#### Run Migrations

```bash
# Migrations run automatically on backend startup via Flyway

# Manually run migrations
docker-compose exec backend ./mvnw flyway:migrate

# Check migration status
docker-compose exec backend ./mvnw flyway:info
```

#### Create New Migration

```bash
# Create new migration file
# Location: backend/src/main/resources/db/migration/
# Naming: V{version}__{description}.sql
# Example: V001__create_users_table.sql

# Migration will run on next backend startup
```

#### Reset Database (Development)

```bash
# WARNING: This deletes all data!
docker-compose down -v
docker-compose up -d
```

---

## Maintenance

### Regular Maintenance Tasks

#### Daily

- Monitor application logs
- Check health endpoints
- Review error rates
- Monitor disk space

#### Weekly

- Review slow query logs
- Analyze database performance
- Check backup integrity
- Review security alerts

#### Monthly

- Update dependencies (security patches)
- Review and optimize database indexes
- Analyze application metrics
- Capacity planning review
- Disaster recovery drill

### Updates and Upgrades

#### Update Docker Images

```bash
# Pull latest base images
docker-compose pull

# Rebuild applications
docker-compose build

# Restart services
docker-compose up -d
```

#### Update Dependencies

```bash
# Backend (Maven)
docker-compose exec backend ./mvnw versions:display-dependency-updates

# Frontend (npm)
docker-compose exec frontend npm outdated
docker-compose exec frontend npm update
```

#### Database Maintenance

```bash
# Vacuum database (reclaim storage)
docker-compose exec postgres psql -U pagodirecto -d pagodirecto_crm \
  -c "VACUUM ANALYZE;"

# Reindex database
docker-compose exec postgres psql -U pagodirecto -d pagodirecto_crm \
  -c "REINDEX DATABASE pagodirecto_crm;"

# Update statistics
docker-compose exec postgres psql -U pagodirecto -d pagodirecto_crm \
  -c "ANALYZE;"
```

### Monitoring Disk Usage

```bash
# Check Docker disk usage
docker system df

# Clean up unused resources
docker system prune -a

# Check volume usage
docker volume ls
docker volume inspect pagodirecto_postgres_data
```

---

## Additional Resources

### Documentation

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/16/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev/)
- [Nginx Documentation](https://nginx.org/en/docs/)

### Tools

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [pgAdmin](https://www.pgadmin.org/) - PostgreSQL administration
- [Postman](https://www.postman.com/) - API testing
- [k6](https://k6.io/) - Load testing
- [Trivy](https://github.com/aquasecurity/trivy) - Container security scanning

### Support

- **Issues**: [GitHub Issues](https://github.com/pagodirecto/crm-erp/issues)
- **Documentation**: [Wiki](https://github.com/pagodirecto/crm-erp/wiki)
- **Email**: devops@pagodirecto.com

---

## License

Proprietary - PagoDirecto © 2024

---

**Last Updated**: 2024-10-13
**Version**: 1.0.0
**Maintainer**: PagoDirecto Infrastructure Team
