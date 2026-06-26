package com.taller.service;

import com.taller.model.Device;
import com.taller.model.DeviceObservation;
import com.taller.model.DevicePasswordHistory;
import com.taller.model.repository.DeviceObservationRepository;
import com.taller.model.repository.DevicePasswordHistoryRepository;
import com.taller.model.repository.DeviceRepository;
import com.taller.model.repository.DeviceTypeRepository;
import com.taller.model.repository.projection.DeviceListView;
import com.taller.resource.dto.DeviceDTO;
import com.taller.resource.dto.DeviceObservationDTO;
import com.taller.resource.dto.DevicePasswordHistoryDTO;
import com.taller.resource.dto.DevicePasswordUpsertDTO;
import com.taller.resource.dto.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final DevicePasswordHistoryRepository devicePasswordHistoryRepository;
    private final DeviceObservationRepository deviceObservationRepository;

    @Transactional
    public DeviceDTO save(DeviceDTO deviceDTO) {
        Device device = deviceDTO.getId() != null
                ? deviceRepository.findById(deviceDTO.getId()).orElseGet(Device::new)
                : new Device();
        boolean isNew = device.getId() == null;
        device.setBrand(deviceDTO.getBrand());
        device.setSerialNumber(deviceDTO.getSerialNumber());
        device.setModel(deviceDTO.getModel());
        var deviceType = deviceTypeRepository.findById(deviceDTO.getDeviceTypeId()).orElseThrow();
        device.setDeviceTypeId(deviceType.getId());
        device.setDeviceType(deviceType);
        device.setTechnicalDetails(deviceDTO.getTechnicalDetails());
        device.setClientId(deviceDTO.getClientId());
        if (deviceDTO.getId() != null) device.setId(deviceDTO.getId());

        Device saved = deviceRepository.save(device);
        if (isNew && hasText(deviceDTO.getCurrentPassword())) {
            addPassword(saved.getId(), toPasswordRequest(deviceDTO.getCurrentPassword()));
            return getDeviceById(saved.getId());
        }
        if (!isNew && hasText(deviceDTO.getCurrentPassword()) && devicePasswordHistoryRepository.countByDeviceId(saved.getId()) == 0) {
            addPassword(saved.getId(), toPasswordRequest(deviceDTO.getCurrentPassword()));
            return getDeviceById(saved.getId());
        }
        syncCurrentPasswordField(saved);
        return toDto(saved, true);
    }

    @Transactional(readOnly = true)
    public List<DeviceDTO> getAllDevices() {
        List<DeviceListView> rows = deviceRepository.findListRows();
        Map<String, List<DeviceObservation>> observationsByDeviceId = observationsByDeviceId(rows.stream().map(DeviceListView::getId).toList());
        return rows.stream()
                .map(device -> toListDto(device, observationsByDeviceId.getOrDefault(device.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageDTO<DeviceDTO> findPage(int page, int size, String term, String clientId, String clientTerm, String sortBy, String sortDir) {
        Page<DeviceListView> result = deviceRepository.findPage(
                normalizeTerm(term),
                normalizeTerm(clientId),
                normalizeTerm(clientTerm),
                normalizeDeviceSortBy(sortBy),
                normalizeSortDir(sortDir),
                pageRequest(page, size, 100));
        Map<String, List<DeviceObservation>> observationsByDeviceId = observationsByDeviceId(result.getContent().stream().map(DeviceListView::getId).toList());
        return toPage(result, result.getContent().stream()
                .map(device -> toListDto(device, observationsByDeviceId.getOrDefault(device.getId(), List.of())))
                .toList());
    }

    @Transactional(readOnly = true)
    public DeviceDTO getDeviceById(String id) {
        return deviceRepository.findById(id).map(device -> toDto(device, true)).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<DeviceDTO> search(String term) {
        List<DeviceListView> rows = deviceRepository.searchListRows(term);
        Map<String, List<DeviceObservation>> observationsByDeviceId = observationsByDeviceId(rows.stream().map(DeviceListView::getId).toList());
        return rows.stream()
                .map(device -> toListDto(device, observationsByDeviceId.getOrDefault(device.getId(), List.of())))
                .toList();
    }

    @Transactional
    public void delete(String id) {
        deviceRepository.deleteById(id);
    }

    @Transactional
    public DeviceDTO addPassword(String deviceId, DevicePasswordUpsertDTO request) {
        Device device = deviceRepository.findById(deviceId).orElseThrow();
        clearCurrentPassword(deviceId);
        devicePasswordHistoryRepository.save(DevicePasswordHistory.builder()
                .deviceId(deviceId)
                .passwordValue(request.getValue().trim())
                .isCurrent(true)
                .build());
        syncCurrentPasswordField(device);
        return getDeviceById(deviceId);
    }

    @Transactional
    public DeviceDTO updatePassword(String deviceId, String passwordId, DevicePasswordUpsertDTO request) {
        DevicePasswordHistory history = devicePasswordHistoryRepository.findByIdAndDeviceId(passwordId, deviceId).orElseThrow();
        history.setPasswordValue(request.getValue().trim());
        devicePasswordHistoryRepository.save(history);
        syncCurrentPasswordField(deviceRepository.findById(deviceId).orElseThrow());
        return getDeviceById(deviceId);
    }

    @Transactional
    public DeviceDTO makeCurrentPassword(String deviceId, String passwordId) {
        Device device = deviceRepository.findById(deviceId).orElseThrow();
        clearCurrentPassword(deviceId);
        DevicePasswordHistory history = devicePasswordHistoryRepository.findByIdAndDeviceId(passwordId, deviceId).orElseThrow();
        history.setIsCurrent(true);
        devicePasswordHistoryRepository.save(history);
        syncCurrentPasswordField(device);
        return getDeviceById(deviceId);
    }

    @Transactional
    public DeviceDTO deletePassword(String deviceId, String passwordId) {
        Device device = deviceRepository.findById(deviceId).orElseThrow();
        DevicePasswordHistory history = devicePasswordHistoryRepository.findByIdAndDeviceId(passwordId, deviceId).orElseThrow();
        boolean wasCurrent = Boolean.TRUE.equals(history.getIsCurrent());
        devicePasswordHistoryRepository.delete(history);
        if (wasCurrent) {
            promoteLatestPassword(deviceId);
        }
        syncCurrentPasswordField(device);
        return getDeviceById(deviceId);
    }

    @Transactional
    public DeviceDTO addObservation(String deviceId, DeviceObservationDTO request) {
        deviceRepository.findById(deviceId).orElseThrow();
        if (!hasText(request.getNote())) {
            throw new IllegalArgumentException("Observation note is required");
        }
        deviceObservationRepository.save(toObservation(deviceId, null, request, new DeviceObservation()));
        return getDeviceById(deviceId);
    }

    @Transactional
    public DeviceDTO updateObservation(String deviceId, String observationId, DeviceObservationDTO request) {
        DeviceObservation observation = deviceObservationRepository.findByIdAndDeviceId(observationId, deviceId).orElseThrow();
        if (!hasText(request.getNote())) {
            throw new IllegalArgumentException("Observation note is required");
        }
        deviceObservationRepository.save(toObservation(deviceId, observation.getRepairId(), request, observation));
        return getDeviceById(deviceId);
    }

    @Transactional
    public DeviceDTO resolveObservation(String deviceId, String observationId) {
        DeviceObservation observation = deviceObservationRepository.findByIdAndDeviceId(observationId, deviceId).orElseThrow();
        observation.setResolvedAt(LocalDateTime.now());
        deviceObservationRepository.save(observation);
        return getDeviceById(deviceId);
    }

    @Transactional
    public DeviceDTO deleteObservation(String deviceId, String observationId) {
        DeviceObservation observation = deviceObservationRepository.findByIdAndDeviceId(observationId, deviceId).orElseThrow();
        deviceObservationRepository.delete(observation);
        return getDeviceById(deviceId);
    }

    private DeviceDTO toDto(Device device, boolean includeHistory) {
        List<DevicePasswordHistory> histories = devicePasswordHistoryRepository.findByDeviceIdOrderByCreationDateTimeDesc(device.getId());
        return toDto(device, includeHistory, histories);
    }

    private DeviceDTO toDto(Device device, boolean includeHistory, List<DevicePasswordHistory> histories) {
        DeviceDTO dto = new DeviceDTO();
        dto.setId(device.getId());
        dto.setBrand(device.getBrand());
        dto.setModel(device.getModel());
        dto.setSerialNumber(device.getSerialNumber());
        dto.setDeviceTypeId(device.getDeviceTypeId());
        dto.setDeviceTypeName(device.getDeviceType() != null ? device.getDeviceType().getName() : null);
        dto.setTechnicalDetails(device.getTechnicalDetails());
        dto.setClientId(device.getClientId());
        dto.setCurrentPassword(resolveCurrentPassword(device, histories));
        if (includeHistory) {
            dto.setPasswordHistory(histories.stream().sorted(Comparator.comparing(DevicePasswordHistory::getCreationDateTime).reversed()).map(this::toPasswordHistoryDto).toList());
            dto.setObservations(deviceObservationRepository.findByDeviceIdOrderByObservedAtDesc(device.getId()).stream().map(this::toObservationDto).toList());
        } else {
            dto.setObservations(deviceObservationRepository.findByDeviceIdAndResolvedAtIsNullOrderByObservedAtDesc(device.getId()).stream().map(this::toObservationDto).toList());
        }
        return dto;
    }

    private DeviceDTO toListDto(DeviceListView device, List<DeviceObservation> observations) {
        DeviceDTO dto = new DeviceDTO();
        dto.setId(device.getId());
        dto.setBrand(device.getBrand());
        dto.setModel(device.getModel());
        dto.setSerialNumber(device.getSerialNumber());
        dto.setDeviceTypeId(device.getDeviceTypeId());
        dto.setDeviceTypeName(device.getDeviceTypeName());
        dto.setClientId(device.getClientId());
        dto.setClientName(joinLabel(device.getClientName(), device.getClientLastName()));
        dto.setCurrentPassword(device.getCurrentPassword());
        dto.setObservations(observations.stream().map(this::toObservationDto).toList());
        return dto;
    }

    private DevicePasswordHistoryDTO toPasswordHistoryDto(DevicePasswordHistory history) {
        DevicePasswordHistoryDTO dto = new DevicePasswordHistoryDTO();
        dto.setId(history.getId());
        dto.setValue(history.getPasswordValue());
        dto.setIsCurrent(Boolean.TRUE.equals(history.getIsCurrent()));
        dto.setCreatedAt(history.getCreationDateTime());
        dto.setUpdatedAt(history.getModificationDatetime());
        return dto;
    }

    private Map<String, List<DeviceObservation>> observationsByDeviceId(Collection<String> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return Map.of();
        }
        return deviceObservationRepository.findByDeviceIdInAndResolvedAtIsNullOrderByObservedAtDesc(deviceIds).stream()
                .collect(Collectors.groupingBy(DeviceObservation::getDeviceId));
    }

    private String resolveCurrentPassword(Device device, List<DevicePasswordHistory> histories) {
        Optional<DevicePasswordHistory> current = histories == null
                ? Optional.empty()
                : histories.stream().filter(history -> Boolean.TRUE.equals(history.getIsCurrent())).findFirst();
        if (current.isPresent()) {
            return current.get().getPasswordValue();
        }
        return hasText(device.getPassword()) ? device.getPassword() : null;
    }

    private void clearCurrentPassword(String deviceId) {
        List<DevicePasswordHistory> histories = devicePasswordHistoryRepository.findByDeviceIdOrderByCreationDateTimeDesc(deviceId);
        boolean changed = false;
        for (DevicePasswordHistory history : histories) {
            if (Boolean.TRUE.equals(history.getIsCurrent())) {
                history.setIsCurrent(false);
                changed = true;
            }
        }
        if (changed) {
            devicePasswordHistoryRepository.saveAll(histories);
        }
    }

    private void promoteLatestPassword(String deviceId) {
        devicePasswordHistoryRepository.findByDeviceIdOrderByCreationDateTimeDesc(deviceId).stream()
                .max(Comparator.comparing(DevicePasswordHistory::getCreationDateTime))
                .ifPresent(history -> {
                    history.setIsCurrent(true);
                    devicePasswordHistoryRepository.save(history);
                });
    }

    private void syncCurrentPasswordField(Device device) {
        String currentPassword = devicePasswordHistoryRepository.findFirstByDeviceIdAndIsCurrentTrue(device.getId())
                .map(DevicePasswordHistory::getPasswordValue)
                .orElse(null);
        device.setPassword(currentPassword);
        deviceRepository.save(device);
    }

    private DevicePasswordUpsertDTO toPasswordRequest(String currentPassword) {
        DevicePasswordUpsertDTO request = new DevicePasswordUpsertDTO();
        request.setValue(currentPassword);
        return request;
    }

    private DeviceObservation toObservation(String deviceId, String repairId, DeviceObservationDTO dto, DeviceObservation observation) {
        LocalDateTime observedAt = dto.getObservedAt() != null
                ? dto.getObservedAt()
                : (observation.getObservedAt() != null ? observation.getObservedAt() : LocalDateTime.now());
        observation.setDeviceId(deviceId);
        observation.setRepairId(repairId != null ? repairId : dto.getRepairId());
        observation.setNote(dto.getNote() != null ? dto.getNote().trim() : null);
        observation.setObservedAt(observedAt);
        observation.setFollowUpAt(dto.getFollowUpAt() != null ? dto.getFollowUpAt() : observedAt.plusMonths(3));
        observation.setResolvedAt(dto.getResolvedAt());
        return observation;
    }

    private DeviceObservationDTO toObservationDto(DeviceObservation observation) {
        DeviceObservationDTO dto = new DeviceObservationDTO();
        dto.setId(observation.getId());
        dto.setDeviceId(observation.getDeviceId());
        dto.setRepairId(observation.getRepairId());
        dto.setNote(observation.getNote());
        dto.setObservedAt(observation.getObservedAt());
        dto.setFollowUpAt(observation.getFollowUpAt());
        dto.setResolvedAt(observation.getResolvedAt());
        return dto;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private <T> PageDTO<T> toPage(Page<?> page, List<T> content) {
        return new PageDTO<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private String normalizeTerm(String term) {
        return term == null ? "" : term.trim();
    }

    private String normalizeDeviceSortBy(String sortBy) {
        return switch (sortBy == null ? "" : sortBy.trim()) {
            case "deviceType", "brand", "model", "client", "observations", "password" -> sortBy.trim();
            default -> "createdAt";
        };
    }

    private String normalizeSortDir(String sortDir) {
        return "asc".equalsIgnoreCase(sortDir) ? "asc" : "desc";
    }

    private PageRequest pageRequest(int page, int size, int maximumSize) {
        return PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), maximumSize));
    }

    private String joinLabel(String... values) {
        return java.util.Arrays.stream(values)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" "));
    }
}
