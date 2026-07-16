package com.taller.model.repository.projection;

import java.math.BigDecimal;

public interface FinanceRepairSummaryView {
    Long getRepairCount();
    BigDecimal getTotalIncome();
    BigDecimal getTotalLabor();
    BigDecimal getTotalQuoted();
    Long getZeroFinalAmountCount();
    Long getPositiveFinalAmountCount();
}
