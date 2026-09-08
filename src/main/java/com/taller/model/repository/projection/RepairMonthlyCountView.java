package com.taller.model.repository.projection;

import java.time.LocalDateTime;

public interface RepairMonthlyCountView {
    LocalDateTime getMonth();
    Long getTotal();
}
