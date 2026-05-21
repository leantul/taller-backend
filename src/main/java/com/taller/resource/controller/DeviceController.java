package com.taller.resource.controller;

import com.taller.resource.dto.DeviceDTO;
import com.taller.resource.dto.DevicePasswordUpsertDTO;
import com.taller.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/{id}/passwords")
    public DeviceDTO addPassword(@PathVariable String id, @RequestBody DevicePasswordUpsertDTO request) {
        return deviceService.addPassword(id, request);
    }

    @PutMapping("/{id}/passwords/{passwordId}")
    public DeviceDTO updatePassword(@PathVariable String id, @PathVariable String passwordId, @RequestBody DevicePasswordUpsertDTO request) {
        return deviceService.updatePassword(id, passwordId, request);
    }

    @DeleteMapping("/{id}/passwords/{passwordId}")
    public DeviceDTO deletePassword(@PathVariable String id, @PathVariable String passwordId) {
        return deviceService.deletePassword(id, passwordId);
    }

    @PostMapping("/{id}/passwords/{passwordId}/make-current")
    public DeviceDTO makeCurrentPassword(@PathVariable String id, @PathVariable String passwordId) {
        return deviceService.makeCurrentPassword(id, passwordId);
    }
}
