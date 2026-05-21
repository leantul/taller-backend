package com.taller.service;

import com.taller.model.Device;
import com.taller.model.DevicePasswordHistory;
import com.taller.model.repository.DevicePasswordHistoryRepository;
import com.taller.model.repository.DeviceRepository;
import com.taller.resource.dto.DeviceDTO;
import com.taller.resource.dto.DevicePasswordHistoryDTO;
import com.taller.resource.dto.DevicePasswordUpsertDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    private final DevicePasswordHistoryRepository devicePasswordHistoryRepository;

    public DeviceDTO save(DeviceDTO deviceDTO) {
        Device device = deviceDTO.getId() != null
                ? deviceRepository.findById(deviceDTO.getId()).orElseGet(Device::new)
                : new Device();
        boolean isNew = device.getId() == null;
        device.setBrand(deviceDTO.getBrand());
        device.setSerialNumber(deviceDTO.getSerialNumber());
        device.setModel(deviceDTO.getModel());
        device.setDeviceType(deviceDTO.getDeviceType());
        device.setAccessories(deviceDTO.getAccessories());
        device.setAestheticCondition(deviceDTO.getAestheticCondition());
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

    public List<DeviceDTO> getAllDevices() {
        List<Device> devices = deviceRepository.findAll();
        Map<String, List<DevicePasswordHistory>> historiesByDeviceId = historiesByDeviceId(devices.stream().map(Device::getId).toList());
        return devices.stream().map(device -> toDto(device, false, historiesByDeviceId.get(device.getId()))).toList();
    }

    public DeviceDTO getDeviceById(String id) {
        return deviceRepository.findById(id).map(device -> toDto(device, true)).orElse(null);
    }

    public List<DeviceDTO> search(String term) {
        List<Device> devices = deviceRepository.search(term);
        Map<String, List<DevicePasswordHistory>> historiesByDeviceId = historiesByDeviceId(devices.stream().map(Device::getId).toList());
        return devices.stream().map(device -> toDto(device, false, historiesByDeviceId.get(device.getId()))).toList();
    }

    public void delete(String id) {
        deviceRepository.deleteById(id);
    }

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

    public DeviceDTO updatePassword(String deviceId, String passwordId, DevicePasswordUpsertDTO request) {
        DevicePasswordHistory history = devicePasswordHistoryRepository.findByIdAndDeviceId(passwordId, deviceId).orElseThrow();
        history.setPasswordValue(request.getValue().trim());
        devicePasswordHistoryRepository.save(history);
        syncCurrentPasswordField(deviceRepository.findById(deviceId).orElseThrow());
        return getDeviceById(deviceId);
    }

    public DeviceDTO makeCurrentPassword(String deviceId, String passwordId) {
        Device device = deviceRepository.findById(deviceId).orElseThrow();
        clearCurrentPassword(deviceId);
        DevicePasswordHistory history = devicePasswordHistoryRepository.findByIdAndDeviceId(passwordId, deviceId).orElseThrow();
        history.setIsCurrent(true);
        devicePasswordHistoryRepository.save(history);
        syncCurrentPasswordField(device);
        return getDeviceById(deviceId);
    }

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
        dto.setDeviceType(device.getDeviceType());
        dto.setAccessories(device.getAccessories());
        dto.setAestheticCondition(device.getAestheticCondition());
        dto.setClientId(device.getClientId());
        dto.setCurrentPassword(resolveCurrentPassword(device, histories));
        if (includeHistory) {
            dto.setPasswordHistory(histories.stream().sorted(Comparator.comparing(DevicePasswordHistory::getCreationDateTime).reversed()).map(this::toPasswordHistoryDto).toList());
        }
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

    private Map<String, List<DevicePasswordHistory>> historiesByDeviceId(Collection<String> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return Map.of();
        }
        return devicePasswordHistoryRepository.findByDeviceIdIn(deviceIds).stream()
                .collect(Collectors.groupingBy(DevicePasswordHistory::getDeviceId));
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
