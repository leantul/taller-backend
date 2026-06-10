package com.taller.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationSynchronizationScheduler {

    private final NotificationService notificationService;

    @Scheduled(initialDelayString = "${app.notifications.initial-delay-ms:0}",
            fixedDelayString = "${app.notifications.sync-delay-ms:60000}")
    public void synchronize() {
        notificationService.synchronize();
    }
}
