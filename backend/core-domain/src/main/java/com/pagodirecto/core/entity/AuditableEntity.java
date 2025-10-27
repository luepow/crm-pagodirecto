package com.pagodirecto.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.util.UUID;

/**
 * Auditable entity with user tracking (created_by, updated_by).
 * Extends BaseEntity with audit trail capabilities.
 *
 * Following CLAUDE.md audit trail requirements:
 * - created_by: UUID reference to user
 * - updated_by: UUID reference to user
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AuditableEntity extends BaseEntity {

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;
}
