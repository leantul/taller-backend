package com.taller.resource.controller;

import com.taller.service.NotificationService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificationControllerTest {

    private final NotificationService notificationService = mock(NotificationService.class);
    private final NotificationController controller = new NotificationController(notificationService);

    @Test
    void latest_synchronizesBeforeReadingNotifications() {
        when(notificationService.latest()).thenReturn(List.of());

        assertEquals(List.of(), controller.latest());

        var order = inOrder(notificationService);
        order.verify(notificationService).synchronize();
        order.verify(notificationService).latest();
    }

    @Test
    void unreadCount_synchronizesBeforeReadingCount() {
        when(notificationService.unreadCount()).thenReturn(3L);

        assertEquals(3L, controller.unreadCount());

        var order = inOrder(notificationService);
        order.verify(notificationService).synchronize();
        order.verify(notificationService).unreadCount();
    }
}
