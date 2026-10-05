package org.example.studentmanagementsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Parent of every entity. It is not a table itself: its four columns are added to the
 * table of each subclass (students, courses, departments).
 *
 * Spring fills the fields automatically when JPA auditing is enabled (see JpaAuditingConfig):
 *   createdAt / createdBy  -> set once, when the row is first saved
 *   updatedAt / updatedBy  -> refreshed on every update
 *
 * The columns are nullable on purpose: rows that existed before auditing was added keep NULL,
 * and "ddl-auto=update" can add the columns to a table that already has data.
 * Instant is stored in UTC, so the value does not depend on the server's time zone.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
