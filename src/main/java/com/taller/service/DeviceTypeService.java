package com.taller.service;

import com.taller.model.repository.DeviceTypeRepository;
import com.taller.resource.dto.DeviceTypeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceTypeService {

    private final DeviceTypeRepository deviceTypeRepository;

    public List<DeviceTypeDTO> getAll() {
        return deviceTypeRepository.findAllByOrderByNameAsc().stream()
                .map(type -> new DeviceTypeDTO(type.getId(), type.getName()))
                .toList();
    }
}
