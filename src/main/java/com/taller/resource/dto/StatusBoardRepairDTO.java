package com.taller.resource.dto;

import com.taller.model.enums.RepairStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StatusBoardRepairDTO(
        String id,
        String idDevice,
        String idClient,
        String orderNumber,
        String description,
        RepairStatusEnum status,
        LocalDateTime receiveDateTime,
        LocalDateTime returnDateTime,
        BigDecimal price,
        BigDecimal quotedAmount,
        String clientName,
        String deviceLabel
) {
}
