# Deployment Checklist - PagoDirecto CRM/ERP

Use this checklist to ensure a successful deployment. Check off each item as you complete it.

---

## Pre-Deployment Checklist

### Environment Setup
- [ ] Docker 20.10+ installed and running
- [ ] Docker Compose 2.0+ installed
- [ ] Server meets minimum requirements (8GB RAM, 4 CPU cores, 50GB disk)
- [ ] Git repository cloned
- [ ] Environment file created from template
- [ ] All ports available (80, 443, 5432, 8080, 3000)

### Configuration
- [ ] Environment file reviewed and customized (.env)
- [ ] All default passwords changed
- [ ] JWT secret generated (use: `openssl rand -base64 64`)
- [ ] Database credentials configured
- [ ] CORS origins updated to production domains
- [ ] API URL configured correctly
- [ ] SSL certificates obtained (if using HTTPS)
- [ ] SSL paths configured in .env

### Security Review
- [ ] No default passwords in use
- [ ] JWT secret is strong (64+ characters)
- [ ] Database password is strong
- [ ] CORS restricted to actual domains
- [ ] Adminer disabled or secured (production)
- [ ] Debug endpoints disabled
- [ ] Security headers configured
- [ ] Rate limiting enabled
- [ ] Firewall rules planned

---

## Deployment Steps

### Initial Deployment

#### 1. Build Images
```bash
cd infra/docker
docker-compose --profile production build
```
- [ ] Backend image built successfully
- [ ] Frontend image built successfully
- [ ] No build errors in logs

#### 2. Start Database
```bash
docker-compose up -d postgres
```
- [ ] PostgreSQL container started
- [ ] Health check passes
- [ ] Database initialized
- [ ] Schemas created
- [ ] Extensions installed

**Verify**:
```bash
docker-compose exec postgres pg_isready -U pagodirecto
docker-compose exec postgres psql -U pagodirecto -d pagodirecto_crm -c "\dt"
```

#### 3. Start Backend
```bash
docker-compose up -d backend
```
- [ ] Backend container started
- [ ] Flyway migrations executed
- [ ] No migration errors
- [ ] Health check passes
- [ ] Actuator endpoints accessible

**Verify**:
```bash
curl http://localhost:8080/actuator/health | jq
curl http://localhost:8080/swagger-ui.html
```

#### 4. Start Frontend
```bash
docker-compose up -d frontend
```
- [ ] Frontend container started
- [ ] Nginx configured correctly
- [ ] Static files served
- [ ] API proxy working
- [ ] Health check passes

**Verify**:
```bash
curl http://localhost:3000/health
curl http://localhost:3000
```

#### 5. Start Nginx Proxy (Production)
```bash
docker-compose --profile production up -d nginx
```
- [ ] Nginx proxy started
- [ ] SSL configured (if applicable)
- [ ] Reverse proxy working
- [ ] Security headers present

**Verify**:
```bash
curl -I http://localhost | grep "X-Frame-Options"
```

---

## Post-Deployment Verification

### Health Checks
- [ ] PostgreSQL: `docker-compose exec postgres pg_isready`
- [ ] Backend: `curl http://localhost:8080/actuator/health`
- [ ] Frontend: `curl http://localhost:3000/health`
- [ ] API Proxy: `curl http://localhost/api/health`

### Functional Tests

#### Database
```bash
docker-compose exec postgres psql -U pagodirecto -d pagodirecto_crm
```
- [ ] Can connect to database
- [ ] All schemas present (seguridad, clientes, productos, ventas, etc.)
- [ ] Extensions loaded (uuid-ossp, pgcrypto, pg_stat_statements)
- [ ] Sample queries execute successfully

#### Backend API
- [ ] API documentation accessible: http://localhost:8080/swagger-ui.html
- [ ] Health endpoint responds: `/actuator/health`
- [ ] Metrics endpoint responds: `/actuator/metrics`
- [ ] Authentication endpoints work
- [ ] Sample API calls succeed

**Test Commands**:
```bash
# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# API test (adjust endpoint)
curl http://localhost:8080/api/v1/health
```

#### Frontend
- [ ] Application loads in browser
- [ ] No console errors
- [ ] Static assets load correctly
- [ ] API calls succeed
- [ ] Routing works (SPA navigation)
- [ ] Responsive design working

**Test in browser**:
- Open: http://localhost:3000
- Check browser console for errors
- Test navigation
- Test form submissions

### Security Tests

#### Headers
```bash
curl -I http://localhost:3000
```
- [ ] X-Frame-Options present
- [ ] X-Content-Type-Options present
- [ ] X-XSS-Protection present
- [ ] Content-Security-Policy present
- [ ] Referrer-Policy present

#### CORS
```bash
curl -H "Origin: http://unauthorized.com" \
     -H "Access-Control-Request-Method: GET" \
     -X OPTIONS http://localhost:8080/api/
```
- [ ] Unauthorized origins blocked
- [ ] Authorized origins allowed
- [ ] Preflight requests handled

#### Authentication
- [ ] JWT authentication works
- [ ] Invalid tokens rejected
- [ ] Token expiration enforced
- [ ] Refresh tokens work

### Performance Tests

#### Response Times
- [ ] API response < 200ms (p95)
- [ ] Frontend load < 2s
- [ ] Database queries < 100ms

**Test Commands**:
```bash
# API latency
time curl http://localhost:8080/actuator/health

# Database query time
docker-compose exec postgres psql -U pagodirecto -d pagodirecto_crm \
  -c "EXPLAIN ANALYZE SELECT * FROM seguridad.usuarios LIMIT 10;"
```

#### Load Testing (Optional)
```bash
# Using Apache Bench
ab -n 1000 -c 10 http://localhost:8080/actuator/health

# Or k6
k6 run load-test.js
```
- [ ] System handles expected load
- [ ] No errors under load
- [ ] Response times acceptable
- [ ] Resource usage reasonable

---

## Backup Configuration

### Initial Backup
```bash
cd infra/scripts
./backup-db.sh initial_backup
```
- [ ] Backup created successfully
- [ ] Backup file exists in backups/
- [ ] Backup file has reasonable size
- [ ] Backup compressed (*.gz)

### Restore Test
```bash
./restore-db.sh initial_backup
```
- [ ] Restore completed without errors
- [ ] Data intact after restore
- [ ] Application functional after restore

### Automated Backups
- [ ] Backup script added to cron
- [ ] Backup retention configured (30 days)
- [ ] Backup location has sufficient space
- [ ] Off-site backup configured (optional)

**Cron entry**:
```bash
crontab -e
# Add: 0 2 * * * /path/to/infra/scripts/backup-db.sh
```

---

## Monitoring Setup

### Logging
- [ ] Log levels configured appropriately
- [ ] Log rotation configured
- [ ] Logs accessible via `./logs.sh`
- [ ] Error logs reviewed

### Metrics
- [ ] Prometheus metrics accessible
- [ ] Key metrics being collected
- [ ] Grafana dashboard configured (optional)

**Access metrics**:
```bash
curl http://localhost:8080/actuator/prometheus
```

### Alerts (Optional)
- [ ] Alert manager configured
- [ ] Critical alerts defined
- [ ] Alert notifications working
- [ ] Escalation procedures documented

---

## Documentation

### Runbooks Created
- [ ] Startup procedure
- [ ] Shutdown procedure
- [ ] Backup procedure
- [ ] Restore procedure
- [ ] Scaling procedure
- [ ] Incident response plan
- [ ] Disaster recovery plan

### Knowledge Transfer
- [ ] Team trained on Docker commands
- [ ] Access credentials distributed
- [ ] On-call procedures defined
- [ ] Escalation contacts documented

---

## Production Readiness

### Infrastructure
- [ ] DNS configured
- [ ] SSL certificates valid
- [ ] Firewall rules applied
- [ ] Load balancer configured (if applicable)
- [ ] CDN configured (if applicable)
- [ ] Backup infrastructure tested

### Security
- [ ] Security scan completed (no critical issues)
- [ ] Dependency audit passed
- [ ] Penetration testing completed (optional)
- [ ] Security policies enforced
- [ ] Audit logging enabled
- [ ] Secrets management configured

### Performance
- [ ] Load testing passed
- [ ] Performance benchmarks met
- [ ] Resource limits configured
- [ ] Auto-scaling configured (if applicable)
- [ ] Database optimized
- [ ] Caching configured

### Compliance
- [ ] Data privacy requirements met
- [ ] Regulatory compliance verified
- [ ] Audit trail implemented
- [ ] Data retention policy defined
- [ ] Backup encryption enabled

---

## Go-Live Checklist

### Final Verification (Day Before)
- [ ] All services healthy
- [ ] All tests passing
- [ ] Backups working
- [ ] Monitoring active
- [ ] Team notified
- [ ] Rollback plan prepared

### Launch Day
- [ ] Fresh backup created
- [ ] Services restarted
- [ ] Health checks passed
- [ ] Smoke tests passed
- [ ] Monitoring verified
- [ ] Team standing by

### Post-Launch (First 24 Hours)
- [ ] Monitor error rates
- [ ] Check performance metrics
- [ ] Review logs for issues
- [ ] Verify backups running
- [ ] User feedback collected
- [ ] Issues documented

### Post-Launch (First Week)
- [ ] Performance analysis
- [ ] Capacity review
- [ ] Security audit
- [ ] User feedback review
- [ ] Documentation updates
- [ ] Lessons learned session

---

## Rollback Plan

### Preparation
- [ ] Backup created before deployment
- [ ] Previous version images tagged
- [ ] Rollback procedure documented
- [ ] Team trained on rollback

### Rollback Triggers
- [ ] Critical errors in production
- [ ] Performance degradation > 50%
- [ ] Security vulnerability discovered
- [ ] Data integrity issues
- [ ] Service availability < 99%

### Rollback Procedure
```bash
# 1. Stop current services
docker-compose down

# 2. Restore database backup
./scripts/restore-db.sh pre_deployment_backup

# 3. Switch to previous images
docker-compose up -d --force-recreate

# 4. Verify rollback
./scripts/logs.sh backend
curl http://localhost:8080/actuator/health
```

---

## Success Criteria

### Technical
- [ ] All services running and healthy
- [ ] Zero critical errors in logs
- [ ] API response time < 200ms (p95)
- [ ] Frontend load time < 2s
- [ ] Database queries < 100ms
- [ ] Uptime > 99.9%

### Business
- [ ] All critical features working
- [ ] User authentication functional
- [ ] Data integrity maintained
- [ ] Reports generating correctly
- [ ] No data loss
- [ ] Users can complete workflows

### Operational
- [ ] Backups running automatically
- [ ] Monitoring operational
- [ ] Alerts configured
- [ ] Team can access logs
- [ ] Documentation complete
- [ ] On-call schedule set

---

## Sign-Off

### Technical Lead
- **Name**: ___________________________
- **Date**: ___________________________
- **Signature**: ___________________________

### Operations Lead
- **Name**: ___________________________
- **Date**: ___________________________
- **Signature**: ___________________________

### Project Manager
- **Name**: ___________________________
- **Date**: ___________________________
- **Signature**: ___________________________

---

## Notes and Issues

**Deployment Date**: ___________________________

**Issues Encountered**:
```
1.
2.
3.
```

**Resolutions**:
```
1.
2.
3.
```

**Lessons Learned**:
```
1.
2.
3.
```

**Follow-Up Actions**:
```
1.
2.
3.
```

---

**Version**: 1.0.0
**Last Updated**: 2024-10-13
