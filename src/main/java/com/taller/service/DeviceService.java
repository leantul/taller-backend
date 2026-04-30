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
        Device device = deviceDTO.getId() != null
                ? deviceRepository.findById(deviceDTO.getId()).orElseGet(Device::new)
                : new Device();
        device.setBrand(deviceDTO.getBrand());
        device.setSerialNumber(deviceDTO.getSerialNumber());
        device.setModel(deviceDTO.getModel());
        device.setDeviceType(deviceDTO.getDeviceType());
        device.setPassword(deviceDTO.getPassword());
        device.setAccessories(deviceDTO.getAccessories());
        device.setAestheticCondition(deviceDTO.getAestheticCondition());
        device.setClientId(deviceDTO.getClientId());
        if (deviceDTO.getId() != null) device.setId(deviceDTO.getId());

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

    public void delete(String id) {
        deviceRepository.deleteById(id);
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
