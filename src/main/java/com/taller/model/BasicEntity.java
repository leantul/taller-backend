package com.taller.model;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public class BasicEntity {
    @Id
    private String id;

    private LocalDateTime creationDateTime;
    private LocalDateTime modificationDatetime;

    /**
     * Generates a unique identifier for the entity and sets the creation and modification timestamps
     * when the entity is persisted.
     *
     * @PrePersist annotation indicates that this method will be automatically called by the persistence
     * provider before the entity is persisted to the database.
     */
    @PrePersist
    private void onPersist() {
        this.id = UUID.randomUUID().toString();
        this.creationDateTime = LocalDateTime.now();
        this.modificationDatetime = LocalDateTime.now();
    }

    /**
     * Updates the modification datetime of the entity to the current time when the entity is updated.
     *
     * @PreUpdate annotation indicates that this method will be automatically called by the persistence
     * provider before the entity is updated in the database.
     */
    @PreUpdate
    private void onUpdate() {
        this.modificationDatetime = LocalDateTime.now();
    }
}
