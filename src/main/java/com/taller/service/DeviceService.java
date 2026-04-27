package com.taller.service;

import com.taller.model.Device;
import com.taller.model.repository.DeviceRepository;
import com.taller.resource.dto.DeviceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceDTO save(DeviceDTO deviceDTO) {
        Device device = Device.builder()
                .brand(deviceDTO.getBrand())
                .serialNumber(deviceDTO.getSerialNumber())
                .model(deviceDTO.getModel())
                .deviceType(deviceDTO.getDeviceType())
                .password(deviceDTO.getPassword())
                .accessories(deviceDTO.getAccessories())
                .aestheticCondition(deviceDTO.getAestheticCondition())
                .clientId(deviceDTO.getClientId())
                .build();

        return toDto(deviceRepository.save(device));
    }

    public List<DeviceDTO> getAllDevices() {
        return deviceRepository.findAll().stream().map(this::toDto).toList();
    }

    public DeviceDTO getDeviceById(String id) {
        return deviceRepository.findById(id).map(this::toDto).orElse(null);
    }

    public List<DeviceDTO> search(String term) {
        return deviceRepository.search(term).stream().map(this::toDto).toList();
    }

    private DeviceDTO toDto(Device device) {
        DeviceDTO dto = new DeviceDTO();
        dto.setId(device.getId());
        dto.setBrand(device.getBrand());
        dto.setModel(device.getModel());
        dto.setSerialNumber(device.getSerialNumber());
        dto.setDeviceType(device.getDeviceType());
        dto.setPassword(device.getPassword());
        dto.setAccessories(device.getAccessories());
        dto.setAestheticCondition(device.getAestheticCondition());
        dto.setClientId(device.getClientId());
        return dto;
    }
}
