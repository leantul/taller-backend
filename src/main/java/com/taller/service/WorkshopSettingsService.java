package com.taller.service;

import com.taller.model.WorkshopSettings;
import com.taller.model.repository.WorkshopSettingsRepository;
import com.taller.resource.dto.WorkshopSettingsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkshopSettingsService {

    public static final String DEFAULT_LOGO_ASSET_PATH = "report/logo-light.png";
    private static final String DEFAULT_BUSINESS_NAME = "Taller";

    private final WorkshopSettingsRepository workshopSettingsRepository;

    @Transactional
    public WorkshopSettingsDTO getSettings() {
        return toDto(resolveSettings());
    }

    @Transactional
    public WorkshopSettingsDTO save(WorkshopSettingsDTO dto) {
        WorkshopSettings settings = resolveSettings();
        settings.setBusinessName(normalizeRequired(dto.getBusinessName(), DEFAULT_BUSINESS_NAME));
        settings.setWhatsapp(normalizeOptional(dto.getWhatsapp()));
        settings.setInstagram(normalizeOptional(dto.getInstagram()));
        settings.setLogoAssetPath(DEFAULT_LOGO_ASSET_PATH);
        return toDto(workshopSettingsRepository.save(settings));
    }

    public WorkshopSettings resolveSettings() {
        return workshopSettingsRepository.findAll().stream().findFirst()
                .orElseGet(this::createDefaultSettings);
    }

    private WorkshopSettings createDefaultSettings() {
        WorkshopSettings settings = WorkshopSettings.builder()
                .businessName(DEFAULT_BUSINESS_NAME)
                .whatsapp("")
                .instagram("")
                .logoAssetPath(DEFAULT_LOGO_ASSET_PATH)
                .build();
        return workshopSettingsRepository.save(settings);
    }

    private WorkshopSettingsDTO toDto(WorkshopSettings settings) {
        WorkshopSettingsDTO dto = new WorkshopSettingsDTO();
        dto.setId(settings.getId());
        dto.setBusinessName(settings.getBusinessName());
        dto.setWhatsapp(settings.getWhatsapp());
        dto.setInstagram(settings.getInstagram());
        dto.setLogoAssetPath(settings.getLogoAssetPath());
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
