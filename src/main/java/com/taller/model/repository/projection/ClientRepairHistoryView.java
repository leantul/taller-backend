package com.taller.model.repository.projection;

import com.taller.model.enums.RepairStatusEnum;

import java.time.LocalDateTime;

public interface ClientRepairHistoryView {
    String getId();
    String getOrderNumber();
    RepairStatusEnum getStatus();
    String getDeviceBrand();
    String getDeviceModel();
    LocalDateTime getReceiveDateTime();
    LocalDateTime getReturnDateTime();
}
