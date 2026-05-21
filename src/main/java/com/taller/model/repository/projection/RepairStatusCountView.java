package com.taller.model.repository.projection;

import com.taller.model.enums.RepairStatusEnum;

public interface RepairStatusCountView {
    RepairStatusEnum getStatus();
    Long getTotal();
}
