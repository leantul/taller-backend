package com.taller.model.repository.projection;

import java.time.LocalDateTime;

public interface OverdueRepairPaymentView {
    String getRepairId();
    LocalDateTime getStatusChangedAt();
}
