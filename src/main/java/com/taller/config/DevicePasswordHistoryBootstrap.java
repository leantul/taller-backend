package com.taller.config;

import com.taller.model.repository.DevicePasswordHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DevicePasswordHistoryBootstrap implements ApplicationRunner {

    private final DevicePasswordHistoryRepository devicePasswordHistoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        devicePasswordHistoryRepository.bootstrapLegacyPasswords();
        devicePasswordHistoryRepository.normalizeCurrentPasswords();
        devicePasswordHistoryRepository.ensureSingleCurrentPasswordIndex();
    }
}
