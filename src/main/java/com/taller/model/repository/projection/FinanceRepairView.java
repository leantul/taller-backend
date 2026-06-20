package com.taller.model.repository.projection;

import com.taller.model.enums.RepairStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface FinanceRepairView {
    String getId();
    String getClientName();
    String getClientLastName();
    RepairStatusEnum getStatus();
    LocalDateTime getReceiveDateTime();
    LocalDateTime getReturnDateTime();
    BigDecimal getPrice();
    BigDecimal getLaborAmount();
    BigDecimal getQuotedAmount();
    BigDecimal getPartsCost();
    BigDecimal getPartsProfit();
}
