package com.taller.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@Table(name = "notifications")
@AllArgsConstructor
@NoArgsConstructor
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
}
