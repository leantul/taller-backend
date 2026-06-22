package com.taller.resource.controller;

import com.taller.resource.dto.SoftwareCatalogItemDTO;
import com.taller.service.SoftwareCatalogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/software-catalog")
@RequiredArgsConstructor
public class SoftwareCatalogController {

    private final SoftwareCatalogService softwareCatalogService;

    @GetMapping
    public List<SoftwareCatalogItemDTO> getAll() {
        return softwareCatalogService.getAll();
    }

    @PostMapping
    public ResponseEntity<SoftwareCatalogItemDTO> save(@RequestBody SoftwareCatalogItemDTO dto) {
        return ResponseEntity.ok(softwareCatalogService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        softwareCatalogService.delete(id);
        return ResponseEntity.ok().build();
    }
}
