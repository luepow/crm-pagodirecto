# ADR-0001: Spidi Real-Time Presence Monitoring Architecture

**Status**: Accepted
**Date**: 2025-10-13
**Deciders**: Chief Systems Engineer, Development Team
**Technical Story**: Implementation of real-time presence monitoring for Spidi communication architecture

## Context

The organization needs to monitor a distributed communication architecture called "Spidi" where each room (sala) represents a communication node. The system must track in real-time:
- Number of connected users per room
- User roster with connection metadata (device, OS, app version, IP, latency)
- Room metrics and statistics
- Alert conditions (capacity thresholds, heartbeat failures, high latency)

This is a **read-heavy, real-time monitoring system** with the following characteristics:
- High concurrency: Target 1,000 concurrent connections per room
- Real-time updates: Sub-second latency for presence changes
- No message content storage: Only metadata and presence tracking
- Multi-session support: Users can connect from multiple devices
- Historical metrics: Time-series data for capacity planning

## Decision

We will implement the Spidi module as a new bounded context in the existing CRM/ERP system with the following architectural decisions:

### 1. Bounded Context Design

**Decision**: Create a standalone "spidi" module following Clean/Hexagonal Architecture

**Structure**:
```
backend/
  └── spidi/
      ├── domain/              # Entities, value objects, domain events
      ├── application/         # Use cases, DTOs, services
      ├── infrastructure/      # Repositories, WebSocket, Redis
      └── api/                 # REST controllers, WebSocket handlers
```

**Rationale**:
- Maintains consistency with existing modules (clientes, ventas, etc.)
- Clear separation of concerns
- Independent deployment capability
- Testability and maintainability

### 2. Real-Time Communication Protocol

**Decision**: Use **WebSocket with STOMP protocol** for real-time bidirectional communication

**Protocol Messages**:
- `HELLO {roomCode, clientId, appVersion}` - Initial connection
- `HEARTBEAT {sessionId, latencyMs}` - Keep-alive with latency reporting
- `LEAVE {sessionId}` - Explicit disconnect
- `ROOM_METRICS {roomId, online, peak, avgLatency}` - Server broadcast to dashboards
- `ROOM_ROSTER {roomId, users[]}` - User list updates

**Heartbeat Strategy**:
- Client sends heartbeat every 15 seconds
- Server timeout after 45 seconds (3 missed heartbeats)
- Exponential backoff for reconnection

**Rationale**:
- WebSocket provides low-latency bidirectional communication
- STOMP provides message routing and pub/sub capabilities
- Spring WebSocket has native support with good documentation
- Alternative considered: Server-Sent Events (SSE) - rejected because unidirectional
- Alternative considered: Polling - rejected due to inefficiency and latency

### 3. Data Storage Strategy: Hybrid Approach

**Decision**: Use **Redis for hot data + PostgreSQL for cold data** with deferred writes

**Redis (In-Memory Hot Storage)**:
- Active sessions: `session:{sessionId}` hash with TTL
- Room presence counters: `room:{roomId}:online` sorted set
- Real-time metrics: `room:{roomId}:metrics` hash
- TTL: 1 hour (auto-cleanup)

**PostgreSQL (Persistent Cold Storage)**:
- `dat_spd_session`: Session history with full metadata
- `dat_spd_room_stats`: Aggregated time-series data (hourly/daily buckets)
- Batch writes every 30 seconds via scheduled job

**Rationale**:
- Redis provides sub-millisecond read/write for real-time queries
- PostgreSQL provides durability and complex query capabilities
- Deferred writes reduce database load by 95%+
- If Redis fails, system degrades gracefully (no real-time metrics, but history preserved)
- Alternative considered: TimescaleDB - may be adopted later for time-series optimization

### 4. Database Schema Design

**Tables**:
1. `tba_spd_room_type` - Catalog of room types (WebRTC, Chat, Notification, etc.)
2. `dat_spd_room` - Room definitions with capacity, TTL, status
3. `dat_spd_room_attr` - EAV pattern for extensible room metadata
4. `dat_spd_session` - Connection sessions with device/client info
5. `dat_spd_room_stats` - Time-bucketed aggregated metrics
6. `dat_spd_alert_rule` - Alerting rule definitions
7. `dat_spd_alert_event` - Triggered alerts history

**Indexing Strategy**:
- `dat_spd_session`: B-tree on `room_id`, `user_id`, `last_heartbeat_at`
- `dat_spd_room_stats`: Composite on `(room_id, ts_bucket)` for time-series queries
- Partial indexes: `WHERE deleted_at IS NULL` for soft-deleted records

**Partitioning**:
- `dat_spd_session`: Partition by `started_at` (monthly) for efficient purging
- `dat_spd_room_stats`: Partition by `ts_bucket` (quarterly) for data lifecycle

**Rationale**:
- Follows existing schema conventions (UUID PKs, audit fields, soft delete)
- EAV pattern provides flexibility for different room types
- Partitioning enables efficient data archival (compliance requirement: 90 days hot, 2 years cold)
- Alternative considered: JSONB for room attributes - rejected for query complexity

### 5. Alerting System

**Decision**: Rule-based alerting with webhook notifications

**Rules Engine**:
- Capacity: `online_count / capacity > threshold` (default 80%)
- Latency: `avg_latency_ms > threshold` (default 500ms)
- Heartbeat: `seconds_since_heartbeat > threshold` (default 60s)
- Custom: Groovy-based expression evaluator for complex rules

**Notification Channels**:
- In-app notifications (WebSocket broadcast to admin panel)
- Webhook POST to external systems (Slack, Teams, PagerDuty)
- Email notifications (via existing notification service)

**Rationale**:
- Flexible rule definition without code deployment
- Multi-channel notifications for different severity levels
- Prevents alert fatigue with rate limiting (max 1 per rule per 5 minutes)

### 6. Security and Authorization

**Decision**: Integrate with existing seguridad module using JWT + RBAC

**Roles and Permissions**:
- `spidi:admin` - Full CRUD on rooms, view all sessions
- `spidi:monitor` - Read-only access to dashboards and metrics
- `spidi:user` - Connect to rooms, send heartbeats (default role)

**Row-Level Security**:
- PostgreSQL RLS policies based on `unidad_negocio_id`
- Multi-tenant isolation enforced at database level

**Rationale**:
- Consistency with existing security model
- Zero trust approach with database-level enforcement
- Audit trail for all administrative actions

### 7. Observability and Monitoring

**Metrics** (Prometheus format):
- `spidi_rooms_total` - Total number of rooms
- `spidi_sessions_active` - Currently active sessions
- `spidi_websocket_connections` - Open WebSocket connections
- `spidi_heartbeat_latency_ms` - Histogram of heartbeat latencies
- `spidi_alerts_triggered_total` - Counter of triggered alerts

**Structured Logging**:
```json
{
  "timestamp": "2025-10-13T10:30:00Z",
  "level": "INFO",
  "trace_id": "abc123",
  "user_id": "uuid",
  "room_id": "uuid",
  "event": "session_started",
  "client_info": {...}
}
```

**Health Checks**:
- `/actuator/health/spidi` - Component health
- Redis connectivity check
- WebSocket thread pool status

**Rationale**:
- Aligns with existing observability stack
- Enables proactive monitoring and capacity planning
- Facilitates troubleshooting with distributed tracing

### 8. Performance Targets

**Latency Budgets** (p95):
- REST API reads: <200ms
- REST API writes: <500ms
- WebSocket message processing: <50ms
- Dashboard metric refresh: <500ms

**Throughput Targets**:
- 1,000 concurrent connections per room
- 10,000 total concurrent WebSocket connections
- 20,000 heartbeats/second processing capacity

**Resource Allocation**:
- Redis: 4GB RAM minimum
- PostgreSQL: Connection pool size = 50
- WebSocket: Thread pool size = 200

**Rationale**:
- Based on industry benchmarks for real-time systems
- Provides headroom for growth (current target: 500 rooms × 1,000 users = 500k total)

## Consequences

### Positive

1. **Scalability**: Hybrid storage approach scales horizontally
2. **Performance**: Redis enables sub-second real-time updates
3. **Reliability**: Deferred writes prevent database overload
4. **Flexibility**: EAV pattern supports diverse room types
5. **Observability**: Comprehensive metrics and logging
6. **Security**: Defense-in-depth with RLS and JWT

### Negative

1. **Complexity**: Dual storage (Redis + PostgreSQL) increases operational overhead
2. **Data Consistency**: Eventual consistency model requires careful handling of race conditions
3. **Cost**: Redis adds infrastructure cost (~$50/month for 4GB)
4. **Learning Curve**: Team needs WebSocket and Redis expertise

### Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Redis failure loses real-time data | High | Low | Graceful degradation; rebuild from PostgreSQL |
| WebSocket connection storms | High | Medium | Rate limiting, connection pooling, backpressure |
| Message ordering issues | Medium | Medium | Sequence numbers in messages, idempotent handlers |
| Memory exhaustion in Redis | High | Low | TTL enforcement, max memory policy, monitoring |

## Alternatives Considered

### Alternative 1: Polling-Based Architecture
**Rejected**: High latency (5-10s), excessive database load, poor user experience

### Alternative 2: Pure PostgreSQL with LISTEN/NOTIFY
**Rejected**: Limited scalability, no native WebSocket integration, connection pooling issues

### Alternative 3: Dedicated Message Queue (RabbitMQ/Kafka)
**Rejected**: Over-engineering for current scale, adds operational complexity, cost

### Alternative 4: Third-Party SaaS (Pusher, Ably)
**Rejected**: Vendor lock-in, cost at scale, data sovereignty concerns

## Implementation Plan

**Phase 1: Foundation (Week 1-2)**
- Database schema and migrations
- Domain entities and repositories
- Basic REST API for room CRUD

**Phase 2: Real-Time Core (Week 3-4)**
- WebSocket integration with STOMP
- Redis cache layer
- Presence tracking logic

**Phase 3: Dashboards (Week 5)**
- Global dashboard UI
- Room detail view
- Real-time updates via WebSocket

**Phase 4: Alerting (Week 6)**
- Rules engine
- Webhook integrations
- Alert history and management

**Phase 5: Production Readiness (Week 7-8)**
- Load testing (1k connections/room)
- Security audit
- Documentation and runbooks
- Monitoring and alerting setup

## References

- [Spring WebSocket Documentation](https://docs.spring.io/spring-framework/reference/web/websocket.html)
- [Redis Best Practices for Real-Time Systems](https://redis.io/docs/latest/develop/use/patterns/)
- [CLAUDE.md - Project Architecture Guidelines](/CLAUDE.md)
- [Existing Database Schema](/docs/erd/database-schema.md)

## Notes

This ADR supersedes no previous decisions as this is a new bounded context.

Future ADRs may address:
- Migration to TimescaleDB for time-series optimization
- Horizontal scaling with Redis Cluster
- Geographic distribution with edge caching
- ML-based anomaly detection in metrics
