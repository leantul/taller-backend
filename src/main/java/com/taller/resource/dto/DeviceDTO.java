package com.taller.resource.dto;

import com.taller.model.enums.DeviceTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DeviceDTO {
    private String id;
    private String brand;
    private String model;
    private String serialNumber;
    private DeviceTypeEnum deviceType;
    private String currentPassword;
    private String accessories;
    private String aestheticCondition;
    private String clientId;
    private List<DevicePasswordHistoryDTO> passwordHistory;
    private List<DeviceObservationDTO> observations;
}
