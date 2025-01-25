package com.taller.resource.dto;

import com.taller.model.enums.RepairStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class RepairDTO {
    private String id;
    private DeviceDTO device;
    private ClientDTO client;
    private String description;
    private RepairStatusEnum status;
    private LocalDateTime receiveDateTime;
    private LocalDateTime returnDateTime;
    private BigDecimal price;
}
