package com.taller.resource.dto;

import com.taller.model.enums.DeviceTypeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceDTO {
    private String id;
    private String brand;
    private String model;
    private String serialNumber;
    private DeviceTypeEnum deviceType;
    private String password;
    private String accessories;
    private String aestheticCondition;
    private String clientId;
}
