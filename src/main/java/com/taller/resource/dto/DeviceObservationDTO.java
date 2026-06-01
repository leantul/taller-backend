package com.taller.resource.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DeviceObservationDTO {
    private String id;
    private String deviceId;
    private String repairId;
    private String note;
    private LocalDateTime observedAt;
    private LocalDateTime followUpAt;
    private LocalDateTime resolvedAt;
}
