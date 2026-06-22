package com.taller.service;

import com.taller.model.SoftwareCatalogItem;
import com.taller.model.repository.SoftwareCatalogItemRepository;
import com.taller.resource.dto.SoftwareCatalogItemDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SoftwareCatalogService {

    private final SoftwareCatalogItemRepository softwareCatalogItemRepository;

    @Transactional(readOnly = true)
    public List<SoftwareCatalogItemDTO> getAll() {
        return softwareCatalogItemRepository.findAllByOrderByNameAsc().stream().map(this::toDto).toList();
    }

    @Transactional
    public SoftwareCatalogItemDTO save(SoftwareCatalogItemDTO dto) {
        SoftwareCatalogItem item = dto.getId() != null
                ? softwareCatalogItemRepository.findById(dto.getId()).orElseGet(SoftwareCatalogItem::new)
                : new SoftwareCatalogItem();
        item.setName(normalizeRequired(dto.getName(), "Software"));
        item.setDetail(normalizeOptional(dto.getDetail()));
        return toDto(softwareCatalogItemRepository.save(item));
    }

    @Transactional
    public void delete(String id) {
        softwareCatalogItemRepository.deleteById(id);
    }

    private SoftwareCatalogItemDTO toDto(SoftwareCatalogItem item) {
        SoftwareCatalogItemDTO dto = new SoftwareCatalogItemDTO();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDetail(item.getDetail());
        return dto;
    }

    private String normalizeOptional(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeRequired(String value, String fallback) {
        String normalized = normalizeOptional(value);
        return normalized == null || normalized.isBlank() ? fallback : normalized;
    }
}
