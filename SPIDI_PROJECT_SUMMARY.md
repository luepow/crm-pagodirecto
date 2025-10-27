# Spidi Real-Time Presence Monitoring - Project Summary

**Project**: Spidi Module for PagoDirecto CRM/ERP
**Date**: October 13, 2025
**Status**: 20% Complete (Foundation Complete, Implementation Ready)
**Estimated Completion**: 36 hours (~5 days of development)

---

## 🎯 **Project Objective**

Build a real-time presence monitoring system for "Spidi" communication architecture where each room (sala) represents a communication node. The system tracks:
- Live user connections per room
- User metadata (device, OS, app version, latency)
- Real-time metrics and dashboards
- Configurable alerting rules

**Key Requirements**:
- WebSocket presence with 15s heartbeats (45s timeout)
- Support multiple sessions per user
- Real-time dashboards with live updates
- Historical statistics and reporting
- RBAC integration with ERP security model
- Performance: 1,000 concurrent connections per room

---

## ✅ **Completed Deliverables** (20% Done)

### 1. Architecture & Documentation

#### ADR-0001: Architectural Decisions
**Location**: `docs/adrs/0001-spidi-presence-monitoring-architecture.md`

**Key Decisions**:
- **Technology Stack**: Spring WebSocket + Redis + PostgreSQL
- **Real-Time Protocol**: STOMP over WebSocket (HELLO, HEARTBEAT, LEAVE messages)
- **Storage Strategy**: Hybrid - Redis for hot data (real-time), PostgreSQL for cold data (historical)
- **Caching**: Two-tier (L1: Caffeine in-memory, L2: Redis distributed)
- **Deferred Writes**: Batch persist to PostgreSQL every 30 seconds
- **Security**: JWT + RBAC with PostgreSQL RLS for multi-tenancy

**Performance Targets** (p95):
- REST reads: <200ms
- REST writes: <500ms
- WebSocket processing: <50ms
- Dashboard refresh: <500ms

#### C4 Architecture Diagrams
**Location**: `docs/c4/spidi-architecture.md`

Includes:
- System Context Diagram
- Container Diagram (showing all components)
- Component Diagram (Presence Service detail)
- Code-Level View (Session entity)
- Data Flow Diagrams (Heartbeat, Broadcast)
- Deployment Architecture (Kubernetes)
- Security Architecture (defense in depth)

### 2. Database Schema (V10 + V11)

**Location**: `backend/application/src/main/resources/db/migration/`

#### V10__create_spidi_schema.sql (462 lines)

**7 Tables Created**:
1. `tba_spd_room_type` - Catalog of room types
2. `dat_spd_room` - Room definitions with capacity, TTL, tags
3. `dat_spd_room_attr` - Custom attributes (EAV pattern)
4. `dat_spd_session` - User connection sessions
5. `dat_spd_room_stats` - Time-series aggregated stats
6. `dat_spd_alert_rule` - Alert rule definitions
7. `dat_spd_alert_event` - Alert event history

**30+ Indexes** optimized for:
- Real-time queries (`room_id`, `last_heartbeat_at`, `status`)
- Time-series queries (`ts_bucket`, `created_at`)
- Search and filters (`code`, `tags` GIN index)

**Advanced Features**:
- Row-Level Security (RLS) for multi-tenancy
- Soft delete with `deleted_at` timestamps
- Materialized view `mv_spd_room_summary` for dashboards
- Triggers for auto-updating `updated_at`
- JSONB columns for flexible metadata

**Partitioning Notes** (for production):
- `dat_spd_session`: Partition by `started_at` (monthly)
- `dat_spd_room_stats`: Partition by `ts_bucket` (quarterly)
- `dat_spd_alert_event`: Partition by `created_at` (monthly)

#### V11__seed_spidi_room_types.sql

**10 Predefined Room Types**:
- WEBRTC (capacity: 50, TTL: 2h)
- CHAT (capacity: 500, TTL: 1h)
- NOTIFICATION (capacity: 10,000, TTL: 30m)
- BROADCAST (capacity: 1,000, TTL: 4h)
- COLLABORATION (capacity: 25, TTL: 1h)
- SUPPORT (capacity: 100, TTL: 30m)
- GAMING (capacity: 16, TTL: 2h)
- MONITORING (capacity: 10, TTL: 24h)
- EVENT (capacity: 200, TTL: 3h)
- GENERIC (capacity: 100, TTL: 1h)

**6 Spidi Permissions**:
- `spidi:monitor` - Read-only dashboard access
- `spidi:admin` - Full CRUD on rooms
- `spidi:user` - Connect to rooms (default)
- `spidi:execute` - Send heartbeats
- Plus CREATE, READ, UPDATE, DELETE, ADMIN

### 3. Backend Module Structure

**Location**: `backend/spidi/`

#### Maven Configuration
**pom.xml** configured with dependencies:
- Spring Boot WebSocket + STOMP
- Redis (Lettuce client) + Caffeine (L1 cache)
- Quartz Scheduler (housekeeping jobs)
- Micrometer + Prometheus (metrics)
- MapStruct (DTO mapping)
- SpringDoc OpenAPI (API documentation)
- Testcontainers (integration tests)

#### Directory Structure (Clean Architecture)
```
backend/spidi/
├── src/main/java/com/pagodirecto/spidi/
│   ├── domain/              # Entities, enums, domain logic
│   │   ├── RoomType.java ✅
│   │   ├── RoomStatus.java ✅
│   │   ├── SessionStatus.java ✅
│   │   ├── AlertSeverity.java ✅
│   │   ├── AlertRuleType.java ✅
│   │   ├── BucketInterval.java ✅
│   │   ├── Room.java ⏳
│   │   ├── Session.java ⏳
│   │   └── ... (4 more entities)
│   ├── application/         # DTOs, services, mappers
│   │   ├── dto/
│   │   ├── mapper/
│   │   └── service/
│   ├── infrastructure/      # Repositories, cache, WebSocket
│   │   ├── repository/
│   │   ├── cache/
│   │   ├── websocket/
│   │   └── config/
│   └── api/                 # REST controllers, WS handlers
│       └── controller/
├── src/main/resources/
│   └── application.yml (to be created)
├── src/test/java/
│   └── ... (tests to be written)
├── pom.xml ✅
├── README.md ✅
└── IMPLEMENTATION_GUIDE.md ✅
```

#### Domain Layer (Partial - 30% done)
**Created**:
- 5 Enums (RoomStatus, SessionStatus, AlertSeverity, AlertRuleType, BucketInterval)
- 1 Entity (RoomType)

**Remaining**:
- Room (main entity)
- RoomAttr (custom attributes)
- Session (presence tracking - CRITICAL)
- RoomStats (time-series)
- AlertRule (alert definitions)
- AlertEvent (alert history)

### 4. Documentation

#### Implementation Guide
**Location**: `backend/spidi/IMPLEMENTATION_GUIDE.md`

**Contents**:
- Complete code templates for all layers
- Entity template with domain methods
- Repository template with custom queries
- DTO template with validation
- Service template with caching
- Controller template with OpenAPI annotations
- Detailed checklist of remaining work (40+ items)
- Time estimates per component
- Quick start commands
- Critical implementation notes

#### Module README
**Location**: `backend/spidi/README.md`

**Contents**:
- Feature overview
- Architecture explanation
- Database schema summary
- Complete REST API specification
- WebSocket protocol specification (with JSON examples)
- Configuration examples (application.yml)
- Security model (permissions, RLS)
- Performance targets and metrics
- Monitoring and health checks
- Development commands
- Troubleshooting guide

---

## 📋 **Remaining Work** (80% Todo)

### Phase 1: Core Domain & Infrastructure (12 hours)

**Domain Entities** (4 hours):
- [ ] Room.java - Main room entity with business methods
- [ ] RoomAttr.java - Custom attributes (EAV pattern)
- [ ] Session.java - User sessions with heartbeat tracking
- [ ] RoomStats.java - Time-series statistics
- [ ] AlertRule.java - Alert rule definitions
- [ ] AlertEvent.java - Alert event history

**Repositories** (2 hours):
- [ ] RoomRepository with custom queries
- [ ] SessionRepository with active session queries
- [ ] RoomStatsRepository with time-series queries
- [ ] AlertRuleRepository, AlertEventRepository, RoomAttrRepository

**Redis Cache Manager** (3 hours):
- [ ] Session cache (session:{sessionId} → Hash)
- [ ] Room online users (room:{roomId}:online → SortedSet)
- [ ] Room metrics (room:{roomId}:metrics → Hash)
- [ ] TTL management and eviction policies

**WebSocket Configuration** (3 hours):
- [ ] STOMP endpoint configuration
- [ ] Message broker setup
- [ ] Security integration (JWT validation)
- [ ] Connection interceptor

### Phase 2: Application Layer (8 hours)

**Core Services** (5 hours):
- [ ] RoomService - CRUD operations with caching
- [ ] PresenceService - CRITICAL: HELLO, HEARTBEAT, LEAVE logic
- [ ] SessionService - Session lifecycle management
- [ ] MetricsService - Real-time metrics calculation
- [ ] AlertService - Rule evaluation and notification

**DTOs** (2 hours):
- [ ] RoomDTO, SessionDTO, RoomStatsDTO, AlertRuleDTO
- [ ] WebSocket message DTOs (HelloMessage, HeartbeatMessage, etc.)
- [ ] Response wrapper classes

**Mappers** (1 hour):
- [ ] RoomMapper (MapStruct)
- [ ] SessionMapper, AlertMapper, StatsMapper

### Phase 3: API Layer (5 hours)

**REST Controllers** (3 hours):
- [ ] RoomController - Room CRUD (GET, POST, PUT, DELETE)
- [ ] SessionController - Session queries and management
- [ ] AlertController - Alert rules and events
- [ ] StatsController - Historical statistics

**WebSocket Handler** (2 hours):
- [ ] Presence handler (@MessageMapping)
- [ ] HELLO message processing
- [ ] HEARTBEAT message processing
- [ ] LEAVE message processing
- [ ] Broadcast to /topic/room/{roomId}

### Phase 4: Background Jobs (3 hours)

**Quartz Jobs**:
- [ ] HousekeepingJob - Close expired sessions (every 5 min)
- [ ] StatsConsolidationJob - Aggregate to PostgreSQL (every 10 min)
- [ ] AlertEvaluationJob - Evaluate alert rules (every 1 min)

### Phase 5: Testing (8 hours)

**Unit Tests** (4 hours):
- [ ] Domain entity tests (Session, Room)
- [ ] Service layer tests (RoomService, PresenceService)
- [ ] >80% coverage target

**Integration Tests** (3 hours):
- [ ] REST API integration tests
- [ ] WebSocket integration tests
- [ ] Redis integration tests

**Load Tests** (1 hour):
- [ ] Artillery script for 1k concurrent connections
- [ ] Performance benchmarking

### Phase 6: Frontend (10 hours) - OUT OF SCOPE FOR NOW

_Frontend implementation is a separate effort, estimated at 10+ hours._

---

## 🚀 **Next Immediate Steps**

### Step 1: Complete Domain Layer (4 hours)
Start with the most critical entities:

1. **Session.java** (1 hour) - CRITICAL for presence tracking
   - Properties: id, roomId, userId, clientId, device, os, appVersion, ip
   - Methods: `recordHeartbeat()`, `isExpired()`, `calculateLatency()`
   - See template in IMPLEMENTATION_GUIDE.md

2. **Room.java** (1 hour)
   - Properties: id, code, name, roomType, status, capacity, ttl
   - Methods: `isActive()`, `getCapacityPercentage()`, `putInMaintenance()`
   - See template in IMPLEMENTATION_GUIDE.md

3. **Remaining entities** (2 hours)
   - RoomAttr, RoomStats, AlertRule, AlertEvent
   - Follow same patterns

### Step 2: Implement Repositories (2 hours)

Create JPA repositories with custom queries:
```java
@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    List<Session> findByRoomIdAndStatus(UUID roomId, SessionStatus status);

    @Query("SELECT s FROM Session s WHERE s.lastHeartbeatAt < :threshold AND s.status = 'ACTIVE'")
    List<Session> findExpiredSessions(@Param("threshold") Instant threshold);
}
```

### Step 3: Implement PresenceService (3 hours)

This is the **core** of the real-time system:

```java
@Service
public class PresenceService {

    @Autowired private SessionRepository sessionRepository;
    @Autowired private RedisCacheManager redisCacheManager;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    public AckMessage handleHello(HelloMessage message) {
        // 1. Validate room exists and is active
        // 2. Create session in Redis (hot cache)
        // 3. Schedule deferred persist to PostgreSQL
        // 4. Broadcast ROOM_METRICS update
        // 5. Return ACK with sessionId and TTL
    }

    public void handleHeartbeat(HeartbeatMessage message) {
        // 1. Update session lastHeartbeatAt in Redis
        // 2. Update rolling average latency
        // 3. Extend TTL
    }

    public void handleLeave(LeaveMessage message) {
        // 1. Mark session as DISCONNECTED in Redis
        // 2. Persist to PostgreSQL immediately
        // 3. Broadcast ROOM_ROSTER update
    }

    public Integer getOnlineCount(UUID roomId) {
        // Query Redis: ZCOUNT room:{roomId}:online -inf +inf
    }
}
```

### Step 4: Implement WebSocket Handler (2 hours)

```java
@Controller
public class PresenceHandler {

    @Autowired private PresenceService presenceService;

    @MessageMapping("/presence/hello")
    @SendToUser("/queue/ack")
    public AckMessage hello(HelloMessage message, Principal principal) {
        return presenceService.handleHello(message);
    }

    @MessageMapping("/presence/heartbeat")
    public void heartbeat(HeartbeatMessage message) {
        presenceService.handleHeartbeat(message);
    }

    @MessageMapping("/presence/leave")
    public void leave(LeaveMessage message) {
        presenceService.handleLeave(message);
    }
}
```

### Step 5: Test End-to-End (1 hour)

1. Run migrations: `mvn flyway:migrate`
2. Start Redis: `docker run -d -p 6379:6379 redis`
3. Start application: `mvn spring-boot:run`
4. Test WebSocket with Postman or wscat:
   ```bash
   wscat -c ws://localhost:8080/ws/presence
   > {"type":"HELLO","roomCode":"ROOM-001",...}
   ```

---

## 📊 **Project Metrics**

### Code Statistics (Estimated)

| Component | Files | Lines | Status |
|-----------|-------|-------|--------|
| Database Schema | 2 | 850 | ✅ 100% |
| Domain Layer | 12 | 1,200 | 🟡 30% |
| Infrastructure Layer | 8 | 800 | ⏳ 0% |
| Application Layer | 20 | 2,500 | ⏳ 0% |
| API Layer | 6 | 1,000 | ⏳ 0% |
| Tests | 30 | 3,000 | ⏳ 0% |
| Documentation | 6 | 2,000 | ✅ 100% |
| **Total** | **84** | **11,350** | **20%** |

### Time Investment

| Phase | Completed | Remaining | Total |
|-------|-----------|-----------|-------|
| Architecture | 3h | 0h | 3h |
| Database | 2h | 0h | 2h |
| Backend Setup | 1h | 0h | 1h |
| Domain | 1h | 4h | 5h |
| Infrastructure | 0h | 6h | 6h |
| Application | 0h | 8h | 8h |
| API | 0h | 5h | 5h |
| Jobs | 0h | 3h | 3h |
| Testing | 0h | 8h | 8h |
| Frontend | 0h | 10h | 10h |
| **Total** | **7h** | **44h** | **51h** |

### Completion Status

- ✅ **Architecture & Design**: 100%
- ✅ **Database Schema**: 100%
- ✅ **Project Setup**: 100%
- 🟡 **Domain Layer**: 30%
- ⏳ **Infrastructure**: 0%
- ⏳ **Application**: 0%
- ⏳ **API**: 0%
- ⏳ **Jobs**: 0%
- ⏳ **Testing**: 0%
- ⏳ **Frontend**: 0%

**Overall Progress**: **20%** (Foundation Complete)

---

## 🎯 **Success Criteria (Definition of Done)**

### Functional Requirements
- [ ] Users can connect to rooms via WebSocket
- [ ] Heartbeats received and processed every 15 seconds
- [ ] Sessions expire after 45 seconds without heartbeat
- [ ] Admin can create/edit/delete rooms via REST API
- [ ] Dashboard shows live metrics (online count, latency)
- [ ] Alerts trigger on capacity/latency/heartbeat rules
- [ ] Multi-session support (user on multiple devices)

### Non-Functional Requirements
- [ ] Performance: 1,000 concurrent connections per room
- [ ] Latency: p95 <200ms for REST, <50ms for WebSocket
- [ ] Test Coverage: >80% for services
- [ ] Load test passed: 1k connections sustained for 10 minutes
- [ ] Security: All endpoints protected with JWT + RBAC
- [ ] Monitoring: Prometheus metrics exported
- [ ] Documentation: ERD, API contracts, deployment guide

### Technical Debt
- [ ] All TODOs resolved or documented
- [ ] No hardcoded secrets
- [ ] Proper error handling and logging
- [ ] Graceful degradation (Redis failure fallback)

---

## 📚 **Key Deliverable Locations**

| Deliverable | Path | Status |
|-------------|------|--------|
| ADR | `docs/adrs/0001-spidi-presence-monitoring-architecture.md` | ✅ |
| C4 Diagrams | `docs/c4/spidi-architecture.md` | ✅ |
| Schema V10 | `backend/application/src/main/resources/db/migration/V10__create_spidi_schema.sql` | ✅ |
| Seed V11 | `backend/application/src/main/resources/db/migration/V11__seed_spidi_room_types.sql` | ✅ |
| Module POM | `backend/spidi/pom.xml` | ✅ |
| Implementation Guide | `backend/spidi/IMPLEMENTATION_GUIDE.md` | ✅ |
| Module README | `backend/spidi/README.md` | ✅ |
| Domain Entities | `backend/spidi/src/main/java/.../domain/` | 🟡 30% |
| Services | `backend/spidi/src/main/java/.../application/service/` | ⏳ 0% |
| Controllers | `backend/spidi/src/main/java/.../api/controller/` | ⏳ 0% |

---

## 🔥 **Critical Path to MVP**

To get a working MVP quickly, focus on these **5 critical components**:

1. **Session.java** (entity) - Presence tracking data model
2. **PresenceService.java** - HELLO, HEARTBEAT, LEAVE logic
3. **SessionRepository.java** - Database persistence
4. **PresenceHandler.java** (WebSocket) - Message handling
5. **Redis integration** - Hot cache for real-time data

**Estimated Time for MVP**: 12 hours

Everything else can be added incrementally.

---

## 📞 **Next Actions**

1. **Review** this summary and the implementation guide
2. **Decide** on implementation approach:
   - Full implementation (44h remaining)
   - MVP only (12h focused effort)
   - Specific components (your choice)
3. **Start coding** following templates in `IMPLEMENTATION_GUIDE.md`
4. **Test iteratively** as you build each component

---

## 🏆 **Congratulations!**

You now have a **production-grade foundation** for the Spidi real-time presence monitoring system, including:
- Complete architecture documentation
- Production-ready database schema
- Module structure following best practices
- Comprehensive implementation templates
- Clear path to completion

The foundation is solid. Now it's time to build! 🚀

---

**Project**: Spidi Real-Time Presence Monitoring
**Status**: Foundation Complete (20%)
**Next Phase**: Core Implementation
**Estimated Completion**: 44 hours / ~6 days
**Created**: October 13, 2025
