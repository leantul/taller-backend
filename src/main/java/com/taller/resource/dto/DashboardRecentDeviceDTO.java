package com.taller.resource.dto;

import com.taller.model.enums.DeviceTypeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardRecentDeviceDTO {
    private String id;
    private DeviceTypeEnum deviceType;
    private String brand;
    private String model;
}
