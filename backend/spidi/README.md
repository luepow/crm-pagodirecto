# Spidi - Real-Time Presence Monitoring Module

## Overview

The Spidi module provides real-time presence monitoring for communication nodes (salas) in the PagoDirecto ERP system. It tracks user connections, heartbeats, and provides live metrics dashboards.

## Features

- **Real-Time Presence Tracking**: WebSocket-based presence with 15s heartbeats
- **Room Management**: Create/edit rooms with capacity, TTL, and custom attributes
- **Live Metrics**: Online users, peak concurrency, average latency per room
- **Alerting System**: Configurable rules for capacity, latency, and heartbeat failures
- **Multi-Session Support**: Users can connect from multiple devices simultaneously
- **Historical Statistics**: Time-series data aggregation (hourly/daily buckets)
- **RBAC Integration**: Permissions (spidi:admin, spidi:monitor, spidi:user)

## Architecture

### Clean Architecture Layers

```
spidi/
├── domain/              # Entities, enums, domain logic
│   ├── Room.java
│   ├── Session.java
│   ├── AlertRule.java
│   └── ...
├── application/         # Use cases, DTOs, services
│   ├── dto/
│   ├── mapper/
│   └── service/
├── infrastructure/      # Persistence, cache, external systems
│   ├── repository/
│   ├── cache/
│   └── websocket/
└── api/                 # REST controllers, WebSocket handlers
    └── controller/
```

### Technology Stack

- **Database**: PostgreSQL (persistent storage) + Redis (hot cache)
- **Real-Time**: Spring WebSocket with STOMP protocol
- **Caching**: Redis (L2) + Caffeine (L1 in-memory)
- **Scheduling**: Quartz for housekeeping jobs
- **Metrics**: Micrometer + Prometheus
- **Testing**: JUnit 5, Testcontainers

## Database Schema

### Tables

1. **tba_spd_room_type** - Catalog of room types (WebRTC, Chat, etc.)
2. **dat_spd_room** - Room definitions
3. **dat_spd_room_attr** - Custom room attributes (EAV pattern)
4. **dat_spd_session** - User connection sessions
5. **dat_spd_room_stats** - Time-series aggregated statistics
6. **dat_spd_alert_rule** - Alert rule definitions
7. **dat_spd_alert_event** - Alert event history

### Migrations

- **V10__create_spidi_schema.sql**: Complete schema (7 tables, 30+ indexes)
- **V11__seed_spidi_room_types.sql**: 10 predefined room types + permissions

## API Endpoints

### REST API (Admin)

```
GET    /api/v1/spd/rooms           - List rooms
GET    /api/v1/spd/rooms/{id}      - Get room details
POST   /api/v1/spd/rooms           - Create room
PUT    /api/v1/spd/rooms/{id}      - Update room
DELETE /api/v1/spd/rooms/{id}      - Delete room (soft)

GET    /api/v1/spd/rooms/{id}/stats        - Get room statistics
GET    /api/v1/spd/sessions?room={id}      - List active sessions
POST   /api/v1/spd/sessions/{id}/terminate - Force disconnect

GET    /api/v1/spd/alerts/rules    - List alert rules
POST   /api/v1/spd/alerts/rules    - Create alert rule
GET    /api/v1/spd/alerts/events   - List alert events
```

### WebSocket (Presence)

**Endpoint**: `ws://localhost:8080/ws/presence`

**Client Messages:**

```json
// HELLO - Initial connection
{
  "type": "HELLO",
  "roomCode": "ROOM-001",
  "clientId": "uuid-v4",
  "appVersion": "2.5.1",
  "device": "iPhone 13",
  "os": "iOS 16.4"
}

// Server responds with ACK
{
  "type": "ACK",
  "sessionId": "session-uuid",
  "ttl": 3600
}

// HEARTBEAT - Keep-alive (every 15 seconds)
{
  "type": "HEARTBEAT",
  "sessionId": "session-uuid",
  "latencyMs": 127
}

// LEAVE - Explicit disconnect
{
  "type": "LEAVE",
  "sessionId": "session-uuid"
}
```

**Server Broadcasts:**

```json
// ROOM_METRICS - Broadcast to /topic/room/{roomId}
{
  "type": "ROOM_METRICS",
  "roomId": "uuid",
  "online": 42,
  "peak": 58,
  "avgLatency": 135,
  "timestamp": "2025-10-13T10:30:00Z"
}

// ROOM_ROSTER - User list update
{
  "type": "ROOM_ROSTER",
  "roomId": "uuid",
  "users": [
    {
      "sessionId": "uuid",
      "userId": "uuid",
      "username": "john.doe",
      "device": "Chrome on Windows",
      "latency": 120,
      "connectedAt": "2025-10-13T10:15:00Z"
    }
  ]
}
```

## Configuration

### application.yml

```yaml
spring:
  # Redis Configuration
  data:
    redis:
      host: localhost
      port: 6379
      password: ${REDIS_PASSWORD:}
      timeout: 2000
      lettuce:
        pool:
          max-active: 50
          max-idle: 10
          min-idle: 2

  # Cache Configuration
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=5m

# Spidi Configuration
spidi:
  presence:
    heartbeat-interval: 15000  # 15 seconds
    heartbeat-timeout: 45000   # 45 seconds (3 missed heartbeats)
    batch-persist-interval: 30000  # 30 seconds

  housekeeping:
    session-cleanup-cron: "0 */5 * * * *"  # Every 5 minutes
    stats-consolidation-cron: "0 */10 * * * *"  # Every 10 minutes

  alerts:
    evaluation-cron: "0 * * * * *"  # Every minute
    rate-limit-minutes: 5

# Actuator (Metrics)
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  metrics:
    export:
      prometheus:
        enabled: true
```

### Environment Variables

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/crm_erp
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=secret

# Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
REDIS_PASSWORD=secret

# JWT Secret
JWT_SECRET=your-secret-key-here
```

## Security

### Permissions

- **spidi:admin** - Full CRUD on rooms, manage alerts, force disconnect
- **spidi:monitor** - Read-only access to dashboards and metrics
- **spidi:user** - Connect to rooms, send heartbeats (default)

### Row-Level Security (PostgreSQL)

Multi-tenant isolation enforced at database level via RLS policies:

```sql
-- Users only see rooms from their business unit
CREATE POLICY tenant_isolation_spd_room ON dat_spd_room
    USING (unidad_negocio_id = current_setting('app.current_tenant_id')::UUID);
```

### Audit Trail

All admin actions logged to `seguridad_audit_log`:
- Room creation/modification/deletion
- Alert rule changes
- Forced session terminations

## Performance

### Latency Budgets (p95)

- REST API reads: <200ms
- REST API writes: <500ms
- WebSocket message processing: <50ms
- Dashboard metric refresh: <500ms

### Capacity Targets

- 1,000 concurrent connections per room
- 10,000 total concurrent WebSocket connections
- 20,000 heartbeats/second processing capacity

### Caching Strategy

1. **L1 (Caffeine)**: In-memory per pod, 5-minute TTL, 100MB max
2. **L2 (Redis)**: Shared cache, 1-hour TTL
   - `session:{sessionId}` → Hash (session data)
   - `room:{roomId}:online` → SortedSet (active sessionIds)
   - `room:{roomId}:metrics` → Hash (live metrics)

## Monitoring

### Prometheus Metrics

```
# Room metrics
spidi_rooms_total{status="ACTIVE"}
spidi_rooms_capacity_percent{room_id="uuid"}

# Session metrics
spidi_sessions_active{room_id="uuid"}
spidi_sessions_total{status="ACTIVE|EXPIRED|DISCONNECTED"}

# WebSocket metrics
spidi_websocket_connections
spidi_heartbeat_latency_ms{quantile="0.5|0.95|0.99"}

# Alert metrics
spidi_alerts_triggered_total{severity="WARNING|ERROR|CRITICAL"}
```

### Health Checks

```
GET /actuator/health/spidi

{
  "status": "UP",
  "components": {
    "spidi": {
      "status": "UP",
      "details": {
        "redis": "UP",
        "websocket": "UP",
        "activeRooms": 42,
        "activeSessions": 1253
      }
    }
  }
}
```

## Development

### Build

```bash
cd backend
mvn clean install -pl spidi -am
```

### Run Tests

```bash
cd backend/spidi
mvn test
```

### Run with Docker Compose

```bash
cd infra/docker
docker-compose up -d postgres redis
cd ../../backend/application
mvn spring-boot:run
```

### Load Testing

```bash
# Install Artillery
npm install -g artillery

# Run load test (1k connections)
artillery run tests/load/spidi-websocket.yml
```

## Troubleshooting

### Common Issues

**1. WebSocket connections failing**
- Check firewall rules (port 8080/443 open)
- Verify CORS configuration
- Check Redis connectivity

**2. High latency on heartbeats**
- Monitor Redis performance (`INFO stats`)
- Check network latency to Redis
- Verify Redis connection pool size

**3. Sessions not expiring**
- Check housekeeping job is running (`/actuator/scheduledtasks`)
- Verify cron expression in configuration
- Check application logs for errors

**4. Missing real-time metrics**
- Verify Redis is running and accessible
- Check WebSocket broadcast configuration
- Verify STOMP subscriptions in frontend

## References

- **Architecture**: `/docs/adrs/0001-spidi-presence-monitoring-architecture.md`
- **Database Schema**: `/backend/application/src/main/resources/db/migration/V10__create_spidi_schema.sql`
- **Implementation Guide**: `IMPLEMENTATION_GUIDE.md` (this directory)
- **C4 Diagrams**: `/docs/c4/spidi-architecture.md`

## Contributing

Follow existing code patterns from `backend/clientes/` module:
- Clean Architecture layers
- MapStruct for DTOs
- Lombok for boilerplate
- OpenAPI annotations
- >80% test coverage

## License

Proprietary - PagoDirecto CRM Team © 2025
