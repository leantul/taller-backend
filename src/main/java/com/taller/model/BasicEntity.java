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
    protected void onPersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.creationDateTime == null) {
            this.creationDateTime = now;
        }
        this.modificationDatetime = now;
    }

    /**
     * Updates the modification datetime of the entity to the current time when the entity is updated.
     *
     * @PreUpdate annotation indicates that this method will be automatically called by the persistence
     * provider before the entity is updated in the database.
     */
    @PreUpdate
    protected void onUpdate() {
        this.modificationDatetime = LocalDateTime.now();
    }
}
