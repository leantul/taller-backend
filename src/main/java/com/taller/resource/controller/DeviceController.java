package com.taller.resource.controller;

import com.taller.resource.dto.DeviceDTO;
import com.taller.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(originPatterns = "*", maxAge = 3600)
@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    public DeviceDTO saveDevice(@RequestBody DeviceDTO deviceDTO) {
        return deviceService.save(deviceDTO);
    }

    @PutMapping
    public DeviceDTO updateDevice(@RequestBody DeviceDTO deviceDTO) {
        return deviceService.save(deviceDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable String id) {
        deviceService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public List<DeviceDTO> getDevices() {
        return deviceService.getAllDevices();
    }

    @GetMapping("/{id}")
    public DeviceDTO getDeviceById(@PathVariable String id) {
        return deviceService.getDeviceById(id);
    }

    @GetMapping("/search")
    public List<DeviceDTO> search(@RequestParam String term) {
        return deviceService.search(term);
    }
}
