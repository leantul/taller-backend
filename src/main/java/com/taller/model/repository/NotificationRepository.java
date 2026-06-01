package com.taller.model.repository;

import com.taller.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.Collection;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByReadedFalseOrderByEventDateDesc();
    long countByReadedFalse();
    Optional<Notification> findByEntityIdAndTypeAndEventDate(String entityId, String type, LocalDateTime eventDate);
    List<Notification> findByEntityIdInAndType(Collection<String> entityIds, String type);
}
