package com.taller.model.repository.projection;

import java.math.BigDecimal;

public interface FinancePaymentSummaryView {
    Long getRepairCount();
    BigDecimal getTotalIncome();
}
