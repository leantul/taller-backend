package com.taller.resource.controller;

import com.taller.resource.dto.DeviceDTO;
import com.taller.resource.mapper.DeviceMapper;
import com.taller.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(originPatterns = "*", maxAge = 3600)
@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceMapper deviceMapper;

    @PostMapping
    public void saveDevice(@RequestBody DeviceDTO deviceDTO) {
        deviceService.save(deviceDTO);
    }

    @GetMapping
    public List<DeviceDTO> getDevices() {
        return deviceMapper.devicesToDeviceDTOList(deviceService.getAllDevices());
    }

    @GetMapping("/{id}")
    public DeviceDTO getDeviceById(@PathVariable String id) {
        return deviceMapper.deviceToDeviceDTO(deviceService.getDeviceById(id));
    }
}
