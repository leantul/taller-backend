package com.taller.model.repository.projection;

import com.taller.model.enums.RepairStatusEnum;

import java.time.LocalDateTime;

public interface RepairStatusHistoryView {
    String getId();
    String getRepairId();
    RepairStatusEnum getStatus();
    LocalDateTime getChangedAt();
}
