package com.taller.resource.dto;

import com.taller.model.enums.RepairStatusEnum;

import java.time.LocalDateTime;

public record DeviceRepairHistoryItemDTO(
        String id,
        String orderNumber,
        RepairStatusEnum status,
        String description,
        LocalDateTime receiveDateTime,
        LocalDateTime returnDateTime
) {
}
