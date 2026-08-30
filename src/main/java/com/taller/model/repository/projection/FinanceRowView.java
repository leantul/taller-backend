package com.taller.model.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface FinanceRowView {
    String getRepairId();
    String getClientName();
    LocalDateTime getDate();
    BigDecimal getIncome();
    BigDecimal getPartsSale();
    BigDecimal getNet();
}
