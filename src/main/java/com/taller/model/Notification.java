package com.taller.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@AttributeOverride(name = "id", column = @Column(name = "id_notification"))
public class Notification extends BasicEntity {

    @Column(name = "title")
    private String title;

    @Column(name = "message")
    private String message;

    @Column(name = "readed")
    private Boolean readed;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @Column(name = "type")
    private String type;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "repair_id")
    private String repairId;

    public Notification() {
    }

    public Notification(String title, String message, Boolean readed, LocalDateTime eventDate, String type, String entityId, String repairId) {
        this.title = title;
        this.message = message;
        this.readed = readed;
        this.eventDate = eventDate;
        this.type = type;
        this.entityId = entityId;
        this.repairId = repairId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getMessage() {
        return this.message;
    }

    public Boolean getReaded() {
        return this.readed;
    }

    public LocalDateTime getEventDate() {
        return this.eventDate;
    }

    public String getType() {
        return this.type;
    }

    public String getEntityId() {
        return this.entityId;
    }

    public String getRepairId() {
        return this.repairId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setReaded(Boolean readed) {
        this.readed = readed;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public void setRepairId(String repairId) {
        this.repairId = repairId;
    }

    public static NotificationBuilder builder() {
        return new NotificationBuilder();
    }

    public static class NotificationBuilder {
        private String title;
        private String message;
        private Boolean readed;
        private LocalDateTime eventDate;
        private String type;
        private String entityId;
        private String repairId;

        public NotificationBuilder title(String title) {
            this.title = title;
            return this;
        }

        public NotificationBuilder message(String message) {
            this.message = message;
            return this;
        }

        public NotificationBuilder readed(Boolean readed) {
            this.readed = readed;
            return this;
        }

        public NotificationBuilder eventDate(LocalDateTime eventDate) {
            this.eventDate = eventDate;
            return this;
        }

        public NotificationBuilder type(String type) {
            this.type = type;
            return this;
        }

        public NotificationBuilder entityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        public NotificationBuilder repairId(String repairId) {
            this.repairId = repairId;
            return this;
        }

        public Notification build() {
            return new Notification(title, message, readed, eventDate, type, entityId, repairId);
        }
    }
}
