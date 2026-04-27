package com.taller.resource.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationDTO {
    private String id;
    private String title;
    private String message;
    private Boolean readed;
    private LocalDateTime eventDate;
    private String type;
    private String entityId;
}
