package com.taller.resource.controller;

import com.taller.resource.dto.NotificationDTO;
import com.taller.resource.dto.PageDTO;
import com.taller.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationDTO> latest() {
        notificationService.synchronizeIfNeeded();
        return notificationService.latest();
    }

    @GetMapping("/page")
    public PageDTO<NotificationDTO> latestPage(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        notificationService.synchronizeIfNeeded();
        return notificationService.latestPage(page, size);
    }

    @GetMapping("/unread-count")
    public long unreadCount() {
        notificationService.synchronizeIfNeeded();
        return notificationService.unreadCount();
    }

    @PostMapping
    public NotificationDTO save(@RequestBody NotificationDTO dto) {
        return notificationService.save(dto);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
