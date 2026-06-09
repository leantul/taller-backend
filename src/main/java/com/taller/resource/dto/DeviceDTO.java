package com.taller.resource.dto;

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
    private String deviceTypeId;
    private String deviceTypeName;
    private String currentPassword;
    private String technicalDetails;
    private String clientId;
    private List<DevicePasswordHistoryDTO> passwordHistory;
    private List<DeviceObservationDTO> observations;
}
