# Spidi Module - Implementation Guide

## Status: 20% Complete (Foundation Laid)

### ✅ **Completed Components**

1. **Architecture Documentation**
   - ADR-0001: Complete architectural decisions
   - C4 Diagrams: All architecture levels documented
   - Location: `docs/adrs/`, `docs/c4/`

2. **Database Schema**
   - V10: Complete schema (7 tables, 30+ indexes, RLS policies)
   - V11: Seed data (10 room types, permissions)
   - Location: `backend/application/src/main/resources/db/migration/`

3. **Module Structure**
   - Maven module configured with all dependencies
   - Directory structure following Clean Architecture
   - Location: `backend/spidi/`

4. **Domain Layer (Partial)**
   - ✅ 5 Enums: RoomStatus, SessionStatus, AlertSeverity, AlertRuleType, BucketInterval
   - ✅ 1 Entity: RoomType
   - ⏳ 6 Entities remaining: Room, RoomAttr, Session, RoomStats, AlertRule, AlertEvent

---

## 🔧 **Implementation Patterns & Templates**

### Domain Entity Template

Use this pattern for all remaining entities (Room, Session, etc.):

```java
package com.pagodirecto.spidi.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dat_spd_room", indexes = {
    @Index(name = "idx_dat_spd_room_code", columnList = "code"),
    @Index(name = "idx_dat_spd_room_status", columnList = "status")
})
@SQLDelete(sql = "UPDATE dat_spd_room SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "unidad_negocio_id", nullable = false)
    private UUID unidadNegocioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RoomStatus status = RoomStatus.ACTIVE;

    @Column(name = "capacity", nullable = false)
    @Builder.Default
    private Integer capacity = 100;

    @Column(name = "ttl_seconds", nullable = false)
    @Builder.Default
    private Integer ttlSeconds = 3600;

    @Column(name = "tags", columnDefinition = "jsonb")
    private String tags; // Store as JSON string, parse/serialize in service layer

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // Domain methods
    public boolean isActive() {
        return RoomStatus.ACTIVE.equals(this.status);
    }

    public void activate() {
        this.status = RoomStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void putInMaintenance() {
        this.status = RoomStatus.MAINTENANCE;
        this.updatedAt = Instant.now();
    }

    public void disable() {
        this.status = RoomStatus.DISABLED;
        this.updatedAt = Instant.now();
    }

    public double getCapacityPercentage(int currentOnline) {
        return (currentOnline * 100.0) / capacity;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
```

### Repository Template

```java
package com.pagodirecto.spidi.infrastructure.repository;

import com.pagodirecto.spidi.domain.Room;
import com.pagodirecto.spidi.domain.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    Optional<Room> findByCodeAndDeletedAtIsNull(String code);

    List<Room> findByStatusAndDeletedAtIsNull(RoomStatus status);

    @Query("""
        SELECT r FROM Room r
        WHERE r.unidadNegocioId = :unidadNegocioId
        AND r.deletedAt IS NULL
        AND r.status = :status
        ORDER BY r.name ASC
    """)
    List<Room> findActiveRoomsByUnidadNegocio(
        @Param("unidadNegocioId") UUID unidadNegocioId,
        @Param("status") RoomStatus status
    );

    @Query("""
        SELECT COUNT(s) FROM Session s
        WHERE s.room.id = :roomId
        AND s.status = 'ACTIVE'
    """)
    Long countActiveSessions(@Param("roomId") UUID roomId);
}
```

### DTO Template

```java
package com.pagodirecto.spidi.application.dto;

import com.pagodirecto.spidi.domain.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Room DTO for API responses and requests")
public class RoomDTO {

    @Schema(description = "Room unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @NotNull(message = "Room type ID is required")
    @Schema(description = "Room type ID", required = true)
    private UUID roomTypeId;

    @Schema(description = "Room type name", example = "WebRTC Video Conference")
    private String roomTypeName;

    @NotBlank(message = "Room code is required")
    @Size(min = 3, max = 100, message = "Code must be between 3 and 100 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Code must contain only uppercase letters, numbers, hyphens and underscores")
    @Schema(description = "Unique room code", example = "ROOM-EVENT-001", required = true)
    private String code;

    @NotBlank(message = "Room name is required")
    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    @Schema(description = "Room display name", example = "Main Event Hall", required = true)
    private String name;

    @Schema(description = "Room description", example = "Primary conference room for large events")
    private String description;

    @NotNull(message = "Status is required")
    @Schema(description = "Room status", example = "ACTIVE", required = true)
    private RoomStatus status;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10000, message = "Capacity cannot exceed 10000")
    @Schema(description = "Maximum user capacity", example = "500", required = true)
    private Integer capacity;

    @Min(value = 60, message = "TTL must be at least 60 seconds")
    @Max(value = 86400, message = "TTL cannot exceed 86400 seconds (24 hours)")
    @Schema(description = "Session TTL in seconds", example = "3600", required = true)
    private Integer ttlSeconds;

    @Schema(description = "Room tags for classification", example = "[\"premium\", \"event\", \"video\"]")
    private List<String> tags;

    @Schema(description = "Currently online users count", example = "42")
    private Integer onlineCount;

    @Schema(description = "Capacity usage percentage", example = "8.4")
    private Double capacityPercent;

    @Schema(description = "Average latency in milliseconds", example = "127")
    private Integer avgLatencyMs;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    @Schema(description = "Last update timestamp")
    private Instant updatedAt;
}
```

### Service Template

```java
package com.pagodirecto.spidi.application.service;

import com.pagodirecto.spidi.application.dto.RoomDTO;
import com.pagodirecto.spidi.application.mapper.RoomMapper;
import com.pagodirecto.spidi.domain.Room;
import com.pagodirecto.spidi.domain.RoomStatus;
import com.pagodirecto.spidi.infrastructure.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final PresenceService presenceService; // For real-time metrics

    @Cacheable(value = "rooms", key = "#id")
    public RoomDTO findById(UUID id) {
        log.debug("Finding room by ID: {}", id);
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + id));

        RoomDTO dto = roomMapper.toDto(room);
        enrichWithRealTimeMetrics(dto);
        return dto;
    }

    public List<RoomDTO> findActiveRooms(UUID unidadNegocioId) {
        log.debug("Finding active rooms for unidad negocio: {}", unidadNegocioId);
        List<Room> rooms = roomRepository.findActiveRoomsByUnidadNegocio(
            unidadNegocioId,
            RoomStatus.ACTIVE
        );

        return rooms.stream()
            .map(room -> {
                RoomDTO dto = roomMapper.toDto(room);
                enrichWithRealTimeMetrics(dto);
                return dto;
            })
            .toList();
    }

    @Transactional
    @CacheEvict(value = "rooms", key = "#dto.id")
    public RoomDTO create(RoomDTO dto, UUID currentUserId) {
        log.info("Creating room: {}", dto.getCode());

        // Validate code uniqueness
        roomRepository.findByCodeAndDeletedAtIsNull(dto.getCode())
            .ifPresent(existing -> {
                throw new BusinessException("Room code already exists: " + dto.getCode());
            });

        Room room = roomMapper.toEntity(dto);
        room.setCreatedBy(currentUserId);
        room.setUpdatedBy(currentUserId);

        Room saved = roomRepository.save(room);
        log.info("Room created successfully: {} (ID: {})", saved.getCode(), saved.getId());

        return roomMapper.toDto(saved);
    }

    @Transactional
    @CacheEvict(value = "rooms", key = "#id")
    public RoomDTO update(UUID id, RoomDTO dto, UUID currentUserId) {
        log.info("Updating room: {}", id);

        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + id));

        // Update fields
        room.setName(dto.getName());
        room.setDescription(dto.getDescription());
        room.setStatus(dto.getStatus());
        room.setCapacity(dto.getCapacity());
        room.setTtlSeconds(dto.getTtlSeconds());
        room.setUpdatedBy(currentUserId);

        Room updated = roomRepository.save(room);
        log.info("Room updated successfully: {}", updated.getId());

        return roomMapper.toDto(updated);
    }

    @Transactional
    @CacheEvict(value = "rooms", key = "#id")
    public void delete(UUID id) {
        log.info("Soft deleting room: {}", id);

        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + id));

        roomRepository.delete(room); // Triggers SQL DELETE (soft delete)
        log.info("Room deleted successfully: {}", id);
    }

    private void enrichWithRealTimeMetrics(RoomDTO dto) {
        // Get real-time metrics from Redis cache
        Integer onlineCount = presenceService.getOnlineCount(dto.getId());
        dto.setOnlineCount(onlineCount);
        dto.setCapacityPercent((onlineCount * 100.0) / dto.getCapacity());

        Integer avgLatency = presenceService.getAverageLatency(dto.getId());
        dto.setAvgLatencyMs(avgLatency);
    }
}
```

### Controller Template

```java
package com.pagodirecto.spidi.api.controller;

import com.pagodirecto.spidi.application.dto.RoomDTO;
import com.pagodirecto.spidi.application.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/spd/rooms")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Spidi Rooms", description = "Room management API")
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('spidi:monitor') or hasAuthority('spidi:admin')")
    @Operation(summary = "Get room by ID", description = "Returns room details with real-time metrics")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable UUID id) {
        log.debug("GET /api/v1/spd/rooms/{}", id);
        RoomDTO room = roomService.findById(id);
        return ResponseEntity.ok(room);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('spidi:monitor') or hasAuthority('spidi:admin')")
    @Operation(summary = "List active rooms", description = "Returns all active rooms for current business unit")
    public ResponseEntity<List<RoomDTO>> listRooms(Authentication authentication) {
        log.debug("GET /api/v1/spd/rooms");
        UUID unidadNegocioId = extractUnidadNegocioId(authentication);
        List<RoomDTO> rooms = roomService.findActiveRooms(unidadNegocioId);
        return ResponseEntity.ok(rooms);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('spidi:admin')")
    @Operation(summary = "Create room", description = "Creates a new room")
    public ResponseEntity<RoomDTO> createRoom(
        @Valid @RequestBody RoomDTO dto,
        Authentication authentication
    ) {
        log.info("POST /api/v1/spd/rooms - Creating room: {}", dto.getCode());
        UUID currentUserId = extractUserId(authentication);
        RoomDTO created = roomService.create(dto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('spidi:admin')")
    @Operation(summary = "Update room", description = "Updates an existing room")
    public ResponseEntity<RoomDTO> updateRoom(
        @PathVariable UUID id,
        @Valid @RequestBody RoomDTO dto,
        Authentication authentication
    ) {
        log.info("PUT /api/v1/spd/rooms/{} - Updating room", id);
        UUID currentUserId = extractUserId(authentication);
        RoomDTO updated = roomService.update(id, dto, currentUserId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('spidi:admin')")
    @Operation(summary = "Delete room", description = "Soft deletes a room")
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID id) {
        log.info("DELETE /api/v1/spd/rooms/{} - Deleting room", id);
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserId(Authentication authentication) {
        // Extract from JWT claims or UserDetails
        return UUID.randomUUID(); // Placeholder
    }

    private UUID extractUnidadNegocioId(Authentication authentication) {
        // Extract from JWT claims or UserDetails
        return UUID.randomUUID(); // Placeholder
    }
}
```

---

## 📋 **Remaining Implementation Checklist**

### Domain Layer (4-6 hours)

- [ ] **Room.java** - Main room entity (use template above)
- [ ] **RoomAttr.java** - Custom room attributes (EAV pattern)
- [ ] **Session.java** - User connection sessions (CRITICAL for presence)
- [ ] **RoomStats.java** - Time-series statistics
- [ ] **AlertRule.java** - Alert rule definitions
- [ ] **AlertEvent.java** - Alert event history

### Infrastructure Layer (3-4 hours)

- [ ] **Repositories** (7 files)
  - RoomRepository, RoomAttrRepository, SessionRepository
  - RoomStatsRepository, AlertRuleRepository, AlertEventRepository
  - RoomTypeRepository

- [ ] **Redis Cache Manager** (`infrastructure/cache/`)
  ```java
  @Component
  public class RedisCacheManager {
      // session:{sessionId} → Hash
      // room:{roomId}:online → SortedSet
      // room:{roomId}:metrics → Hash
  }
  ```

- [ ] **WebSocket Configuration** (`infrastructure/websocket/`)
  ```java
  @Configuration
  @EnableWebSocketMessageBroker
  public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
      @Override
      public void registerStompEndpoints(StompEndpointRegistry registry) {
          registry.addEndpoint("/ws/presence")
              .setAllowedOrigins("*")
              .withSockJS();
      }
  }
  ```

### Application Layer (4-5 hours)

- [ ] **Services** (5 files)
  - RoomService (template above)
  - PresenceService (CRITICAL - handles HELLO, HEARTBEAT, LEAVE)
  - SessionService
  - AlertService
  - MetricsService

- [ ] **DTOs** (10+ files)
  - RoomDTO, SessionDTO, AlertRuleDTO, AlertEventDTO
  - RoomStatsDTO, RoomMetricsDTO
  - WebSocket message DTOs: HelloMessage, HeartbeatMessage, etc.

- [ ] **Mappers** (MapStruct)
  - RoomMapper, SessionMapper, AlertMapper, StatsMapper

### API Layer (2-3 hours)

- [ ] **Controllers** (4 files)
  - RoomController (template above)
  - SessionController
  - AlertController
  - StatsController

- [ ] **WebSocket Handler** (CRITICAL)
  ```java
  @MessageMapping("/presence/hello")
  @SendTo("/topic/room/{roomId}")
  public void handleHello(HelloMessage message) {
      // Validate room
      // Create session in Redis
      // Return ACK with sessionId and TTL
  }
  ```

### Jobs & Background Tasks (2-3 hours)

- [ ] **HousekeepingJob** - Close expired sessions
- [ ] **StatsConsolidationJob** - Aggregate stats to PostgreSQL
- [ ] **AlertEvaluationJob** - Evaluate alert rules

### Testing (4-6 hours)

- [ ] Unit tests for services (>80% coverage)
- [ ] Integration tests for REST APIs
- [ ] WebSocket integration tests
- [ ] Load tests (1k connections/room)

### Frontend (8-10 hours)

- [ ] React components structure
- [ ] Global dashboard
- [ ] Room detail view
- [ ] Room management UI
- [ ] WebSocket client integration

---

## 🚀 **Quick Start Commands**

### Build Module
```bash
cd backend
mvn clean install -pl spidi -am
```

### Run Migrations
```bash
cd backend/application
mvn flyway:migrate
```

### Run Tests
```bash
cd backend/spidi
mvn test
```

### Add Module to Application
In `backend/application/pom.xml`, add dependency:
```xml
<dependency>
    <groupId>com.pagodirecto</groupId>
    <artifactId>spidi</artifactId>
</dependency>
```

---

## 📚 **Key References**

- **Architecture**: `docs/adrs/0001-spidi-presence-monitoring-architecture.md`
- **Database Schema**: `backend/application/src/main/resources/db/migration/V10__create_spidi_schema.sql`
- **API Contracts**: To be created in `docs/api/spidi-api.yaml`
- **Existing Patterns**: Review `backend/clientes/` module for consistent patterns

---

## ⚠️ **Critical Implementation Notes**

1. **Session Expiry**: Implement timeout check every 10 seconds
2. **Redis Fallback**: If Redis fails, gracefully degrade (no real-time metrics, but keep logging)
3. **Multi-tenancy**: Always filter by `unidad_negocio_id` in queries
4. **Audit Trail**: Log all admin actions (create/update/delete rooms)
5. **Rate Limiting**: Implement on WebSocket endpoints (max 1 HEARTBEAT per 10s)

---

## 📊 **Estimated Completion Time**

| Phase | Time | Complexity |
|-------|------|------------|
| Domain Layer | 5 hours | Medium |
| Infrastructure | 4 hours | High (Redis + WebSocket) |
| Application Layer | 5 hours | Medium |
| API Layer | 3 hours | Low |
| Jobs | 3 hours | Medium |
| Testing | 6 hours | Medium |
| Frontend | 10 hours | High |
| **Total** | **36 hours** | **~5 days** |

---

## 🎯 **Next Immediate Steps**

1. Complete domain entities (Room, Session, etc.)
2. Implement repositories
3. Create PresenceService (core of real-time tracking)
4. Implement WebSocket handler
5. Test with Postman + WebSocket client

**Start with**: Implementing the `Session.java` entity and `PresenceService`, as these are the core of the presence tracking system.
