package com.taller.resource.controller;

import com.taller.resource.dto.DeviceTypeDTO;
import com.taller.service.DeviceTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/device-type")
@RequiredArgsConstructor
public class DeviceTypeController {

    private final DeviceTypeService deviceTypeService;

    @GetMapping
    public List<DeviceTypeDTO> getAll() {
        return deviceTypeService.getAll();
    }
}
