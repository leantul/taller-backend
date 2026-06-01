package com.taller.config;

import com.taller.model.Device;
import com.taller.model.DevicePasswordHistory;
import com.taller.model.repository.DevicePasswordHistoryRepository;
import com.taller.model.repository.DeviceRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DevicePasswordHistoryBootstrap implements ApplicationRunner {

    private final DeviceRepository deviceRepository;
    private final DevicePasswordHistoryRepository devicePasswordHistoryRepository;

    @Override
    public void run(ApplicationArguments args) {
        for (Device device : deviceRepository.findAll()) {
            if (device.getPassword() == null || device.getPassword().isBlank()) {
                continue;
            }
            if (devicePasswordHistoryRepository.countByDeviceId(device.getId()) > 0) {
                continue;
            }
            devicePasswordHistoryRepository.save(DevicePasswordHistory.builder()
                    .deviceId(device.getId())
                    .passwordValue(device.getPassword())
                    .isCurrent(true)
                    .build());
        }
    }

    public DevicePasswordHistoryBootstrap(DeviceRepository deviceRepository, DevicePasswordHistoryRepository devicePasswordHistoryRepository) {
        this.deviceRepository = deviceRepository;
        this.devicePasswordHistoryRepository = devicePasswordHistoryRepository;
    }
}
