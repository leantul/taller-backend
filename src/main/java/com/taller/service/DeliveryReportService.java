package com.taller.service;

import com.taller.model.RepairPart;
import com.taller.model.RepairReport;
import com.taller.model.RepairReportHardwareItem;
import com.taller.model.RepairReportSoftwareItem;
import com.taller.model.repository.RepairPartRepository;
import com.taller.model.repository.RepairReportHardwareItemRepository;
import com.taller.model.repository.RepairReportRepository;
import com.taller.model.repository.RepairReportSoftwareItemRepository;
import com.taller.model.repository.RepairRepository;
import com.taller.model.repository.projection.DeliveryReportSourceView;
import com.taller.model.repository.projection.RepairReportHardwareItemView;
import com.taller.model.repository.projection.RepairReportSoftwareItemView;
import com.taller.model.repository.projection.RepairReportView;
import com.taller.resource.dto.RepairReportDTO;
import com.taller.resource.dto.RepairReportHardwareItemDTO;
import com.taller.resource.dto.RepairReportSoftwareItemDTO;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryReportService {

    private final RepairRepository repairRepository;
    private final RepairPartRepository repairPartRepository;
    private final RepairReportRepository repairReportRepository;
    private final RepairReportHardwareItemRepository repairReportHardwareItemRepository;
    private final RepairReportSoftwareItemRepository repairReportSoftwareItemRepository;
    private final WorkshopSettingsService workshopSettingsService;
    private final DeliveryReportPdfService deliveryReportPdfService;

    @Transactional(readOnly = true)
    public RepairReportDTO getByRepairId(String repairId) {
        return repairReportRepository.findViewByRepairId(repairId)
                .map(this::toDto)
                .orElseGet(() -> buildDefaultReport(repairId));
    }

    @Transactional
    public RepairReportDTO save(String repairId, RepairReportDTO dto) {
        DeliveryReportSourceView repair = findRepairSource(repairId);
        RepairReport report = repairReportRepository.findIdViewByRepairId(repairId)
                .map(idView -> repairReportRepository.getReferenceById(idView.getId()))
                .orElseGet(RepairReport::new);

        report.setRepairId(repairId);
        report.setOrderNumber(valueOrFallback(dto.getOrderNumber(), repair.getOrderNumber()));
        report.setIssuedAt(dto.getIssuedAt() != null ? dto.getIssuedAt() : java.time.LocalDateTime.now());
        report.setClientName(normalizeOptional(dto.getClientName()));
        report.setClientLastName(normalizeOptional(dto.getClientLastName()));
        report.setClientPhone(normalizeOptional(dto.getClientPhone()));
        report.setClientEmail(normalizeOptional(dto.getClientEmail()));
        report.setClientDni(normalizeOptional(dto.getClientDni()));
        report.setDeviceTypeName(normalizeOptional(dto.getDeviceTypeName()));
        report.setDeviceBrand(normalizeOptional(dto.getDeviceBrand()));
        report.setDeviceModel(normalizeOptional(dto.getDeviceModel()));
        report.setDeviceSerialNumber(normalizeOptional(dto.getDeviceSerialNumber()));
        report.setReportedIssue(normalizeOptional(dto.getReportedIssue()));
        report.setWorkPerformed(normalizeOptional(dto.getWorkPerformed()));
        report.setFinalObservations(normalizeOptional(dto.getFinalObservations()));
        report.setShowPartPrices(Boolean.TRUE.equals(dto.getShowPartPrices()));
        report.setFinalAmount(dto.getFinalAmount());

        RepairReport saved = repairReportRepository.save(report);

        repairReportHardwareItemRepository.deleteAllByRepairReportId(saved.getId());
        repairReportSoftwareItemRepository.deleteAllByRepairReportId(saved.getId());

        List<RepairReportHardwareItem> hardwareItems = new ArrayList<>();
        for (RepairReportHardwareItemDTO item : defaultList(dto.getHardwareItems())) {
            if (item.getPartName() == null || item.getPartName().isBlank()) {
                continue;
            }
            hardwareItems.add(RepairReportHardwareItem.builder()
                    .repairReportId(saved.getId())
                    .partName(item.getPartName().trim())
                    .quantity(item.getQuantity() != null && item.getQuantity() > 0 ? item.getQuantity() : 1)
                    .detail(normalizeOptional(item.getDetail()))
                    .unitPrice(item.getUnitPrice())
                    .includePrice(!Boolean.FALSE.equals(item.getIncludePrice()))
                    .build());
        }
        repairReportHardwareItemRepository.saveAll(hardwareItems);

        List<RepairReportSoftwareItem> softwareItems = new ArrayList<>();
        for (RepairReportSoftwareItemDTO item : defaultList(dto.getSoftwareItems())) {
            if (item.getSoftwareName() == null || item.getSoftwareName().isBlank()) {
                continue;
            }
            softwareItems.add(RepairReportSoftwareItem.builder()
                    .repairReportId(saved.getId())
                    .softwareName(item.getSoftwareName().trim())
                    .detail(normalizeOptional(item.getDetail()))
                    .build());
        }
        repairReportSoftwareItemRepository.saveAll(softwareItems);

        return repairReportRepository.findViewByRepairId(repairId)
                .map(this::toDto)
                .orElseThrow();
    }

    @Transactional(readOnly = true)
    public byte[] generatePdf(String repairId) {
        RepairReportDTO report = getByRepairId(repairId);
        return deliveryReportPdfService.generate(report, workshopSettingsService.resolveSettings());
    }

    private RepairReportDTO buildDefaultReport(String repairId) {
        DeliveryReportSourceView repair = findRepairSource(repairId);

        RepairReportDTO dto = new RepairReportDTO();
        dto.setRepairId(repairId);
        dto.setOrderNumber(repair.getOrderNumber());
        dto.setIssuedAt(java.time.LocalDateTime.now());
        dto.setClientName(repair.getClientName());
        dto.setClientLastName(repair.getClientLastName());
        dto.setClientPhone(repair.getClientPhone());
        dto.setClientEmail(repair.getClientEmail());
        dto.setClientDni(repair.getClientDni());
        dto.setDeviceTypeName(repair.getDeviceTypeName());
        dto.setDeviceBrand(repair.getDeviceBrand());
        dto.setDeviceModel(repair.getDeviceModel());
        dto.setDeviceSerialNumber(repair.getDeviceSerialNumber());
        dto.setReportedIssue(repair.getReportedIssue());
        dto.setWorkPerformed(repair.getWorkPerformed());
        dto.setFinalObservations(null);
        dto.setShowPartPrices(false);
        dto.setFinalAmount(repair.getFinalAmount());
        dto.setHardwareItems(repairPartRepository.findByRepairId(repairId).stream().map(this::toHardwareDto).toList());
        dto.setSoftwareItems(List.of());
        return dto;
    }

    private DeliveryReportSourceView findRepairSource(String repairId) {
        return repairRepository.findDeliveryReportSourceById(repairId).orElseThrow();
    }

    private RepairReportDTO toDto(RepairReportView report) {
        RepairReportDTO dto = new RepairReportDTO();
        dto.setId(report.getId());
        dto.setRepairId(report.getRepairId());
        dto.setOrderNumber(report.getOrderNumber());
        dto.setIssuedAt(report.getIssuedAt());
        dto.setClientName(report.getClientName());
        dto.setClientLastName(report.getClientLastName());
        dto.setClientPhone(report.getClientPhone());
        dto.setClientEmail(report.getClientEmail());
        dto.setClientDni(report.getClientDni());
        dto.setDeviceTypeName(report.getDeviceTypeName());
        dto.setDeviceBrand(report.getDeviceBrand());
        dto.setDeviceModel(report.getDeviceModel());
        dto.setDeviceSerialNumber(report.getDeviceSerialNumber());
        dto.setReportedIssue(report.getReportedIssue());
        dto.setWorkPerformed(report.getWorkPerformed());
        dto.setFinalObservations(report.getFinalObservations());
        dto.setShowPartPrices(report.getShowPartPrices());
        dto.setFinalAmount(report.getFinalAmount());
        dto.setHardwareItems(repairReportHardwareItemRepository.findViewByRepairReportIdOrderByCreationDateTimeAsc(report.getId()).stream().map(this::toHardwareDto).toList());
        dto.setSoftwareItems(repairReportSoftwareItemRepository.findViewByRepairReportIdOrderByCreationDateTimeAsc(report.getId()).stream().map(this::toSoftwareDto).toList());
        return dto;
    }

    private RepairReportHardwareItemDTO toHardwareDto(RepairPart item) {
        RepairReportHardwareItemDTO dto = new RepairReportHardwareItemDTO();
        dto.setPartName(item.getName());
        dto.setQuantity(item.getQuantity());
        dto.setDetail(item.getProvider());
        dto.setUnitPrice(item.getSalePrice());
        dto.setIncludePrice(true);
        return dto;
    }

    private RepairReportHardwareItemDTO toHardwareDto(RepairReportHardwareItemView item) {
        RepairReportHardwareItemDTO dto = new RepairReportHardwareItemDTO();
        dto.setId(item.getId());
        dto.setPartName(item.getPartName());
        dto.setQuantity(item.getQuantity());
        dto.setDetail(item.getDetail());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setIncludePrice(item.getIncludePrice());
        return dto;
    }

    private RepairReportSoftwareItemDTO toSoftwareDto(RepairReportSoftwareItemView item) {
        RepairReportSoftwareItemDTO dto = new RepairReportSoftwareItemDTO();
        dto.setId(item.getId());
        dto.setSoftwareName(item.getSoftwareName());
        dto.setDetail(item.getDetail());
        return dto;
    }

    private String normalizeOptional(String value) {
        return value == null ? null : value.trim();
    }

    private String valueOrFallback(String primary, String fallback) {
        String normalized = normalizeOptional(primary);
        return normalized == null || normalized.isBlank() ? fallback : normalized;
    }

    private <T> List<T> defaultList(List<T> value) {
        return value == null ? List.of() : value;
    }
}
