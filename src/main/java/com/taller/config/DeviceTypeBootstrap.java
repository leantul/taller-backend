package com.taller.config;

import com.taller.model.DeviceType;
import com.taller.model.repository.DeviceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DeviceTypeBootstrap implements ApplicationRunner {

    private static final List<String> DEFAULT_TYPES = List.of(
            "Desktop", "Notebook", "Tablet", "Celular", "Otros");

    private final DeviceTypeRepository deviceTypeRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Set<String> existingNames = deviceTypeRepository.findAllByOrderByNameAsc().stream()
                .map(DeviceType::getName)
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        deviceTypeRepository.saveAll(DEFAULT_TYPES.stream()
                .filter(name -> !existingNames.contains(name.toLowerCase(Locale.ROOT)))
                .map(DeviceType::new)
                .toList());
    }
}
