package com.taller.resource.dto;

import com.taller.model.enums.RepairStatusEnum;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public record RepairStatusUpdateDTO(
        RepairStatusEnum status,
        LocalDateTime receiveDateTime,
        LocalDateTime returnDateTime,
        PaymentType paymentType,
        BigDecimal paymentAmount
) {
    public enum PaymentType { FULL, PARTIAL }
}
