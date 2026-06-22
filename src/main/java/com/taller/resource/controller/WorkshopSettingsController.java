package com.taller.resource.controller;

import com.taller.resource.dto.WorkshopSettingsDTO;
import com.taller.service.WorkshopSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workshop-settings")
@RequiredArgsConstructor
public class WorkshopSettingsController {

    private final WorkshopSettingsService workshopSettingsService;

    @GetMapping
    public WorkshopSettingsDTO get() {
        return workshopSettingsService.getSettings();
    }

    @PutMapping
    public ResponseEntity<WorkshopSettingsDTO> update(@RequestBody WorkshopSettingsDTO dto) {
        return ResponseEntity.ok(workshopSettingsService.save(dto));
    }
}
