package com.taller.resource.dto;

import com.taller.model.enums.RepairStatusEnum;

import java.time.LocalDateTime;

public record RepairStatusUpdateDTO(
        RepairStatusEnum status,
        LocalDateTime receiveDateTime,
        LocalDateTime returnDateTime
) {
}
