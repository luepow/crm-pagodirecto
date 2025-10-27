# Spidi Real-Time Presence Monitoring - C4 Architecture Diagrams

## Level 1: System Context

```
┌─────────────────────────────────────────────────────────────────────┐
│                     PagoDirecto CRM/ERP System                      │
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐ │
│  │                    Spidi Presence Module                       │ │
│  │                                                                │ │
│  │  • Real-time presence tracking                                │ │
│  │  • Room management and metrics                                │ │
│  │  • Alert monitoring                                           │ │
│  └───────────────────────────────────────────────────────────────┘ │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
         │                    │                    │
         │                    │                    │
         ▼                    ▼                    ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  Web Dashboard  │  │  Mobile Clients │  │ External Systems│
│   (Admins)      │  │   (End Users)   │  │ (Webhooks)      │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

**External Actors:**
- **Web Dashboard Users**: Administrators monitoring room metrics and managing rooms
- **Mobile/Web Clients**: End users connecting to rooms and sending heartbeats
- **External Systems**: Third-party systems receiving webhook notifications (Slack, PagerDuty)

## Level 2: Container Diagram

```
┌───────────────────────────────────────────────────────────────────────────┐
│                         Spidi Bounded Context                             │
│                                                                           │
│  ┌────────────────┐         ┌────────────────┐        ┌───────────────┐ │
│  │   REST API     │◄────────│   WebSocket    │────────►│  Dashboard UI │ │
│  │                │         │   (STOMP)      │         │   (React)     │ │
│  │  /api/spd/*    │         │  /ws/presence  │         │               │ │
│  └────────┬───────┘         └────────┬───────┘         └───────────────┘ │
│           │                          │                                    │
│           │                          │                                    │
│           ▼                          ▼                                    │
│  ┌────────────────────────────────────────────────┐                      │
│  │         Application Services Layer              │                      │
│  │                                                 │                      │
│  │  • RoomService                                  │                      │
│  │  • PresenceService                              │                      │
│  │  • SessionService                               │                      │
│  │  • AlertService                                 │                      │
│  │  • MetricsService                               │                      │
│  └────────┬──────────────────────┬─────────────────┘                      │
│           │                      │                                        │
│           ▼                      ▼                                        │
│  ┌────────────────┐     ┌────────────────┐                               │
│  │  Domain Layer  │     │  Domain Events │                               │
│  │                │     │                │                               │
│  │  • Room        │     │  • SessionStarted                              │
│  │  • Session     │     │  • SessionEnded                                │
│  │  • AlertRule   │     │  • AlertTriggered                              │
│  └────────┬───────┘     └────────────────┘                               │
│           │                                                               │
│           ▼                                                               │
│  ┌────────────────────────────────────────────────┐                      │
│  │         Infrastructure Layer                    │                      │
│  │                                                 │                      │
│  │  • JPA Repositories                             │                      │
│  │  • Redis Cache Manager                          │                      │
│  │  • WebSocket Connection Handler                 │                      │
│  │  • Webhook Client                               │                      │
│  └────────┬──────────────────────┬─────────────────┘                      │
│           │                      │                                        │
└───────────┼──────────────────────┼────────────────────────────────────────┘
            │                      │
            ▼                      ▼
   ┌─────────────────┐    ┌─────────────────┐
   │   PostgreSQL    │    │     Redis       │
   │                 │    │                 │
   │  • dat_spd_*    │    │  • sessions     │
   │  • tba_spd_*    │    │  • metrics      │
   └─────────────────┘    │  • presence     │
                          └─────────────────┘
```

**Key Containers:**
- **REST API**: Admin operations (room CRUD, session queries, alert management)
- **WebSocket Server**: Real-time presence protocol (HELLO, HEARTBEAT, LEAVE)
- **Dashboard UI**: React-based monitoring interface
- **Application Services**: Business logic and use cases
- **Domain Layer**: Core entities and domain events
- **Infrastructure**: Database, cache, and external integrations

## Level 3: Component Diagram - Presence Service

```
┌─────────────────────────────────────────────────────────────────┐
│                    Presence Service Component                   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │               PresenceService                            │   │
│  │                                                          │   │
│  │  + handleHello(roomCode, clientId, metadata)            │   │
│  │  + handleHeartbeat(sessionId, latency)                  │   │
│  │  + handleLeave(sessionId)                               │   │
│  │  + getActiveSessionsByRoom(roomId)                      │   │
│  │  + getRoomMetrics(roomId)                               │   │
│  └──────┬──────────────────────┬──────────────────────────┘   │
│         │                      │                              │
│         ▼                      ▼                              │
│  ┌─────────────────┐   ┌─────────────────┐                   │
│  │  SessionManager │   │  MetricsCollector│                   │
│  │                 │   │                  │                   │
│  │  • createSession│   │  • updateMetrics │                   │
│  │  • updateHeart  │   │  • calculatePeak │                   │
│  │  • expireSession│   │  • aggregateStats│                   │
│  └────────┬────────┘   └────────┬─────────┘                   │
│           │                     │                             │
│           ▼                     ▼                             │
│  ┌───────────────────────────────────┐                        │
│  │      CacheManager (Redis)         │                        │
│  │                                   │                        │
│  │  • session:{id} → Hash            │                        │
│  │  • room:{id}:online → SortedSet   │                        │
│  │  • room:{id}:metrics → Hash       │                        │
│  └───────────────────────────────────┘                        │
│                                                                │
│  ┌───────────────────────────────────┐                        │
│  │  SessionRepository (PostgreSQL)   │                        │
│  │                                   │                        │
│  │  • persist(session) [deferred]    │                        │
│  │  • findByRoomId(roomId)           │                        │
│  │  • findExpiredSessions()          │                        │
│  └───────────────────────────────────┘                        │
└─────────────────────────────────────────────────────────────────┘
```

## Level 4: Code-Level View - Session Entity

```java
// Domain Entity
@Entity
@Table(name = "dat_spd_session")
public class Session {
    private UUID id;
    private UUID roomId;
    private UUID userId;
    private String clientId;
    private String device;
    private String os;
    private String appVersion;
    private String ipAddress;
    private Instant startedAt;
    private Instant lastHeartbeatAt;
    private Integer avgLatencyMs;
    private SessionStatus status;

    public void recordHeartbeat(Integer latencyMs) {
        this.lastHeartbeatAt = Instant.now();
        this.avgLatencyMs = calculateRollingAverage(latencyMs);
    }

    public boolean isExpired(int timeoutSeconds) {
        return Duration.between(lastHeartbeatAt, Instant.now())
            .getSeconds() > timeoutSeconds;
    }
}
```

## Data Flow Diagrams

### Heartbeat Flow (Happy Path)

```
Client                  WebSocket Handler        PresenceService         Redis           PostgreSQL
  │                            │                       │                  │                  │
  ├──HEARTBEAT────────────────►│                       │                  │                  │
  │  {sessionId, latency}      │                       │                  │                  │
  │                            ├──validate session────►│                  │                  │
  │                            │                       ├──update TTL─────►│                  │
  │                            │                       │  EXPIRE session  │                  │
  │                            │                       │      3600s       │                  │
  │                            │                       ├──update metrics─►│                  │
  │                            │                       │  HSET metrics    │                  │
  │                            │                       │  ZADD online     │                  │
  │                            │                       │◄─────OK──────────┤                  │
  │                            │◄────ACK───────────────┤                  │                  │
  │◄───────ACK─────────────────┤                       │                  │                  │
  │                            │                       │                  │                  │
  │                            │                       │  [After 30s]     │                  │
  │                            │                       ├──batch persist───┼─────────────────►│
  │                            │                       │                  │  INSERT/UPDATE   │
  │                            │                       │                  │  dat_spd_session │
```

### Room Metrics Broadcast Flow

```
PresenceService         Redis           WebSocket Handler       Dashboard Clients
      │                   │                    │                        │
      ├──calculate────────►│                    │                        │
      │  room metrics      │                    │                        │
      │                    │                    │                        │
      ├──HGET all─────────►│                    │                        │
      │  room:{id}:metrics │                    │                        │
      │◄──metrics data─────┤                    │                        │
      │                    │                    │                        │
      ├──broadcast─────────┼───────────────────►│                        │
      │  ROOM_METRICS      │   /topic/room/{id} │                        │
      │  {online, peak}    │                    ├───WebSocket push──────►│
      │                    │                    │                        │
      │                    │                    │                        │
```

## Deployment Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                          Kubernetes Cluster                         │
│                                                                     │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │                  Ingress Controller                         │    │
│  │  (SSL Termination, WebSocket Upgrade)                       │    │
│  └────────────┬──────────────────────────┬────────────────────┘    │
│               │                           │                         │
│               ▼                           ▼                         │
│  ┌─────────────────────┐    ┌─────────────────────────┐           │
│  │  Spidi Backend Pod  │    │  Frontend Nginx Pod     │           │
│  │  (Spring Boot)      │    │  (React SPA)            │           │
│  │                     │    │                         │           │
│  │  Replicas: 3        │    │  Replicas: 2            │           │
│  │  Resources:         │    └─────────────────────────┘           │
│  │    CPU: 2 cores     │                                           │
│  │    Memory: 4 GB     │                                           │
│  └──────────┬──────────┘                                           │
│             │                                                       │
│             ▼                                                       │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │                    Service Layer                            │  │
│  │  • ClusterIP: spidi-service:8080                            │  │
│  │  • LoadBalancer: /api/spd (REST), /ws (WebSocket)          │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
         │                                   │
         ▼                                   ▼
┌──────────────────┐              ┌──────────────────────┐
│  PostgreSQL      │              │  Redis Cluster       │
│  (StatefulSet)   │              │  (StatefulSet)       │
│                  │              │                      │
│  Replicas: 1     │              │  Master: 1           │
│  Storage: 100GB  │              │  Replicas: 2         │
│  Backup: Daily   │              │  Memory: 4GB         │
└──────────────────┘              │  Persistence: RDB    │
                                  └──────────────────────┘
```

## Security Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Security Layers                          │
│                                                             │
│  1. Network Layer                                           │
│     ├── TLS 1.3 encryption (certificate-based)             │
│     ├── Firewall rules (allow only 443, 8080)              │
│     └── DDoS protection (rate limiting)                    │
│                                                             │
│  2. Authentication Layer                                    │
│     ├── JWT tokens (15 min expiry, refresh rotation)       │
│     ├── OAuth2/OIDC integration                            │
│     └── WebSocket handshake validation                     │
│                                                             │
│  3. Authorization Layer                                     │
│     ├── RBAC (spidi:admin, spidi:monitor, spidi:user)      │
│     ├── Method-level @PreAuthorize annotations             │
│     └── PostgreSQL RLS (row-level security)                │
│                                                             │
│  4. Data Protection Layer                                   │
│     ├── Encrypted connections (TLS)                        │
│     ├── Encrypted at rest (PostgreSQL TDE)                 │
│     ├── PII masking in logs                                │
│     └── Audit trail (dat_audit_log)                        │
│                                                             │
│  5. Application Layer                                       │
│     ├── Input validation (JSR-303 Bean Validation)         │
│     ├── SQL injection prevention (JPA/Hibernate)           │
│     ├── XSS protection (Content Security Policy)           │
│     └── CSRF protection (Spring Security)                  │
└─────────────────────────────────────────────────────────────┘
```

## Scalability Considerations

**Horizontal Scaling:**
- Backend pods: Scale based on WebSocket connection count (target: 500 connections/pod)
- Redis: Redis Cluster with 3 masters + 3 replicas for high availability
- PostgreSQL: Read replicas for reporting queries

**Vertical Scaling:**
- Increase pod memory for higher WebSocket connection limits
- Increase Redis memory for larger datasets (formula: sessions × 1KB average)

**Caching Strategy:**
- L1: In-memory cache per pod (Caffeine) - 100MB per pod
- L2: Redis cluster - 4GB shared cache
- TTL: 1 hour for session data, 5 minutes for metrics

**Database Optimization:**
- Connection pooling: HikariCP with max 50 connections
- Read replicas for dashboard queries (eventual consistency acceptable)
- Partitioning for time-series data (monthly partitions)

## Monitoring Dashboard

```
┌─────────────────────────────────────────────────────────────┐
│           Grafana Dashboard - Spidi Monitoring              │
│                                                             │
│  ┌────────────────────┐  ┌────────────────────┐            │
│  │  Total Sessions    │  │  Total Rooms       │            │
│  │    10,542          │  │     237            │            │
│  └────────────────────┘  └────────────────────┘            │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Connections per Second (time series)                │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │        /\    /\                                 │  │  │
│  │  │       /  \  /  \    /\                          │  │  │
│  │  │  ────/────\/────\──/──\─────────                │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌────────────────────┐  ┌────────────────────┐            │
│  │  Avg Latency       │  │  Alert Count       │            │
│  │    127 ms          │  │     3 (last hour)  │            │
│  └────────────────────┘  └────────────────────┘            │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Top 10 Rooms by Concurrency                         │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  1. Room A ████████████████████ 1,234 users   │  │  │
│  │  │  2. Room B ██████████████ 892 users           │  │  │
│  │  │  3. Room C ████████ 456 users                 │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## References

- [ADR-0001: Spidi Architecture Decisions](/docs/adrs/0001-spidi-presence-monitoring-architecture.md)
- [Spring WebSocket Reference](https://docs.spring.io/spring-framework/reference/web/websocket.html)
- [Redis Pub/Sub Patterns](https://redis.io/docs/latest/develop/interact/pubsub/)
- [C4 Model Documentation](https://c4model.com/)
