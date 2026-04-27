package com.taller.model.repository;

import com.taller.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findTop20ByOrderByEventDateDesc();
}
