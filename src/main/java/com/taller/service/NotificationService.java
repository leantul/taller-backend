package com.taller.service;

import com.taller.model.Notification;
import com.taller.model.repository.NotificationRepository;
import com.taller.resource.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationDTO> latest() {
        return notificationRepository.findTop20ByOrderByEventDateDesc().stream().map(this::toDto).toList();
    }

    public NotificationDTO save(NotificationDTO dto) {
        Notification notification = Notification.builder()
                .title(dto.getTitle())
                .message(dto.getMessage())
                .readed(Boolean.FALSE)
                .eventDate(dto.getEventDate())
                .type(dto.getType())
                .entityId(dto.getEntityId())
                .build();

        if (dto.getId() != null) {
            notification.setId(dto.getId());
            notification.setReaded(dto.getReaded());
        }

        return toDto(notificationRepository.save(notification));
    }

    public void markAsRead(String id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setReaded(Boolean.TRUE);
            notificationRepository.save(notification);
        });
    }

    private NotificationDTO toDto(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setReaded(notification.getReaded());
        dto.setEventDate(notification.getEventDate());
        dto.setType(notification.getType());
        dto.setEntityId(notification.getEntityId());
        return dto;
    }
}
