package com.taller.model.repository.projection;

import com.taller.model.enums.RepairStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface StatusBoardRepairView {
    String getId();
    String getIdDevice();
    String getIdClient();
    String getOrderNumber();
    String getDescription();
    RepairStatusEnum getStatus();
    LocalDateTime getReceiveDateTime();
    LocalDateTime getReturnDateTime();
    BigDecimal getPrice();
    BigDecimal getQuotedAmount();
    String getClientName();
    String getClientLastName();
    String getDeviceTypeName();
    String getDeviceBrand();
    String getDeviceModel();
}
