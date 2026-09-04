package com.taller.model.repository.projection;

import java.math.BigDecimal;

/** Totals calculated from the same repair set as the finance detail grid. */
public interface FinanceActivitySummaryView {
    Long getRepairCount();
    BigDecimal getTotalIncome();
    BigDecimal getTotalPartsCost();
}
