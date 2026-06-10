package com.taller.resource.dto;

import com.taller.model.enums.RepairStatusEnum;

import java.time.LocalDateTime;

public record ClientRepairHistoryItemDTO(
        String id,
        String orderNumber,
        RepairStatusEnum status,
        String deviceBrand,
        String deviceModel,
        LocalDateTime receiveDateTime,
        LocalDateTime returnDateTime
) {
}
