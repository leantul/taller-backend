package com.taller.model.repository.projection;

import com.taller.model.enums.RepairStatusEnum;

import java.time.LocalDateTime;

public interface DeviceRepairHistoryView {
    String getId();
    String getOrderNumber();
    RepairStatusEnum getStatus();
    String getDescription();
    LocalDateTime getReceiveDateTime();
    LocalDateTime getReturnDateTime();
}
