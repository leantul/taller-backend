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

    public void save(DeviceDTO deviceDTO) {
        Device device = Device.builder()
                .brand(deviceDTO.getBrand())
                .serialNumber(deviceDTO.getSerialNumber())
                .model(deviceDTO.getModel())
                .build();

        deviceRepository.save(device);
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Device getDeviceById(String id) {
        return deviceRepository.findById(id).orElse(null);
    }
}
