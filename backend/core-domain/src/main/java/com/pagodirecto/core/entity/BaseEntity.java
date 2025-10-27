package com.pagodirecto.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Base entity with UUID primary key, timestamps, and soft delete support.
 * All domain entities should extend this class.
 *
 * Following CLAUDE.md guidelines for database design:
 * - UUID primary keys for distributed systems
 * - Timestamps with timezone
 * - Soft delete pattern
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * Soft delete implementation
     */
    public void delete() {
        this.deletedAt = Instant.now();
    }

    /**
     * Check if entity is deleted
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Restore soft-deleted entity
     */
    public void restore() {
        this.deletedAt = null;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
